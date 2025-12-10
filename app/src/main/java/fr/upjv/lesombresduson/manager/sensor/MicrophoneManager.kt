package fr.upjv.lesombresduson.manager.sensor

import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.annotation.SuppressLint
import fr.upjv.lesombresduson.manager.input.GestureListener
import kotlin.math.abs

/**
 * Gère la logique de la phase de détection du soufflement via le microphone.
 */
class MicrophoneManager(private val listener: GestureListener) {
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "MicrophoneManager"

        // Configuration Audio (Constantes)
        private const val SAMPLE_RATE = 8000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        // Logique de Validation (Constantes)
        private const val BLOW_THRESHOLD = 5000 // Seuil d'amplitude pour détecter le soufflement
        private const val VALIDATION_DURATION_MS = 1000L // 1 seconde (L pour Long)
        private const val MONITOR_INTERVAL_MS = 100L // Intervalle de monitoring (100 ms)
    }

    private var audioRecord: AudioRecord? = null
    private val bufferSize: Int

    private var isListening = false
    private var startTime: Long = 0

    init {
        // Calcul de la taille minimale du buffer dans le bloc init
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    }

    // Runnable pour la validation réussie (Lambda simplifiée)
    private val validationSuccessRunnable = Runnable {
        if (!isListening) return@Runnable
        isListening = false
        stopListening()
        listener.onDogFound()
    }

    // Runnable pour le monitoring (vérifie l'amplitude en continu)
    private val monitorRunnable = object : Runnable {
        override fun run() {
            if (!isListening) return

            val amplitude = getAmplitude()
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime

            if (amplitude > BLOW_THRESHOLD) {
                // Le soufflement est maintenu
                if (startTime == 0L) {
                    // Premier détection : Démarrer le chrono
                    startTime = currentTime
                    handler.postDelayed(validationSuccessRunnable, VALIDATION_DURATION_MS)
                    listener.onFeedbackNeeded("Soufflement détecté. Maintenez pendant 1 seconde...")
                } else {
                    // Soufflement maintenu : Mettre à jour le feedback de temps
                    val remainingTime = VALIDATION_DURATION_MS - elapsedTime
                    // Kotlin String Template pour le message
                    listener.onFeedbackNeeded("Maintenu: ${(remainingTime / 1000 + 1)}s restantes.")
                }
            } else {
                // Soufflement interrompu ou trop faible
                if (startTime != 0L) {
                    // Réinitialiser le chronométrage
                    handler.removeCallbacks(validationSuccessRunnable)
                    startTime = 0
                    listener.onFeedbackNeeded("Soufflement interrompu. Veuillez souffler à nouveau.")
                }
            }

            // Continuer le monitoring
            handler.postDelayed(this, MONITOR_INTERVAL_MS) // Vérifier toutes les 100 ms
        }
    }

    /**
     * Tente de démarrer l'enregistrement audio et l'écoute des amplitudes.
     * L'annotation @SuppressLint est utilisée car la permission RECORD_AUDIO est vérifiée
     * et demandée dans l'Activity appelante.
     */
    @SuppressLint("MissingPermission")
    fun startListening() {
        if (isListening) return

        if (audioRecord == null) {
            try {
                // L'appel nécessite la permission RECORD_AUDIO
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
                )
            } catch (e: SecurityException) {
                // Gestion explicite de l'exception de sécurité
                Log.e(TAG, "Erreur de sécurité : Permission RECORD_AUDIO manquante. ${e.message}")
                listener.onFeedbackNeeded("Erreur micro: Permission manquante. Veuillez redémarrer l'application.")
                return
            } catch (e: Exception) {
                Log.e(TAG, "Erreur d'initialisation AudioRecord: ${e.message}")
                listener.onFeedbackNeeded("Erreur micro: impossible d'initialiser l'appareil.")
                return
            }
        }

        // Utilisation de ?.let pour un contrôle de nullité concis et sûr
        audioRecord?.let { record ->
            if (record.state == AudioRecord.STATE_INITIALIZED) {
                record.startRecording()
                isListening = true
                startTime = 0L // Réinitialiser le chrono au début
                listener.onFeedbackNeeded("Soufflez dans le micro pour appeler le chien !")
                handler.post(monitorRunnable) // Démarrer la boucle de monitoring
            } else {
                listener.onFeedbackNeeded("Le micro n'est pas prêt. Réessayez.")
            }
        }
    }

    /**
     * Arrête l'enregistrement audio et le monitoring.
     * Libère les ressources du Handler et de l'AudioRecord.
     * Doit être appelée dans onPause() ou onDestroy() de l'Activity.
     */
    fun stopListening() {
        isListening = false
        handler.removeCallbacks(monitorRunnable)
        handler.removeCallbacks(validationSuccessRunnable)
        startTime = 0

        audioRecord?.let { record ->
            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop()
            }
            record.release()
            audioRecord = null
        }
    }

    /**
     * Lit les données du micro et retourne l'amplitude maximale (volume).
     */
    private fun getAmplitude(): Int {
        // Utilisation de `val` pour les variables
        val record = audioRecord ?: return 0 // Retourne 0 si AudioRecord est null

        val buffer = ShortArray(bufferSize)
        val readSize = record.read(buffer, 0, bufferSize)

        var maxAmplitude = 0
        // Utilisation de la méthode abs de kotlin.math pour la valeur absolue
        for (i in 0 until readSize) {
            if (abs(buffer[i].toInt()) > maxAmplitude) {
                maxAmplitude = abs(buffer[i].toInt())
            }
        }
        return maxAmplitude
    }
}