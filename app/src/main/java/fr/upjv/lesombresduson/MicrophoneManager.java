package fr.upjv.lesombresduson;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import android.os.Handler;
import android.util.Log;
import android.annotation.SuppressLint;

public class MicrophoneManager {
    private static final String TAG = "MicrophoneManager";
    private GestureListener listener;

    // Configuration Audio
    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord = null;
    private int bufferSize;

    // Logique de Validation
    private static final int BLOW_THRESHOLD = 5000; // Seuil d'amplitude pour détecter le soufflement
    private static final long VALIDATION_DURATION_MS = 1000; // 1 secondes
    private final Handler handler = new Handler();

    private boolean isListening = false;
    private long startTime = 0;

    // Runnable pour la validation réussie
    private final Runnable validationSuccessRunnable = () -> {
        isListening = false;
        stopListening();
        listener.onDogFound();
    };

    // Runnable pour le monitoring (vérifie l'amplitude en continu)
    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isListening) return;

            int amplitude = getAmplitude();
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;

            if (amplitude > BLOW_THRESHOLD) {
                // Le soufflement est maintenu
                if (startTime == 0) {
                    // Premier détection : Démarrer le chrono
                    startTime = currentTime;
                    handler.postDelayed(validationSuccessRunnable, VALIDATION_DURATION_MS);
                    listener.onFeedbackNeeded("Soufflement détecté. Maintenez pendant 1 secondes...");
                } else {
                    // Soufflement maintenu : Mettre à jour le feedback de temps
                    long remainingTime = VALIDATION_DURATION_MS - elapsedTime;
                    listener.onFeedbackNeeded("Maintenu: " + (remainingTime / 1000 + 1) + "s restantes.");
                }
            } else {
                // Soufflement interrompu ou trop faible
                if (startTime != 0) {
                    // Réinitialiser le chronométrage
                    handler.removeCallbacks(validationSuccessRunnable);
                    startTime = 0;
                    listener.onFeedbackNeeded("Soufflement interrompu. Veuillez souffler à nouveau.");
                }
            }

            // Continuer le monitoring
            handler.postDelayed(this, 100); // Vérifier toutes les 100 ms
        }
    };

    /**
     * Constructeur pour initialiser le manager du microphone.
     * Calcule la taille minimale du buffer pour l'enregistrement audio.
     * * @param listener L'instance du GestureListener (l'Activity) pour le feedback.
     */
    public MicrophoneManager(GestureListener listener) {
        this.listener = listener;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    }

    /**
     * Tente de démarrer l'enregistrement audio et l'écoute des amplitudes.
     * L'annotation @SuppressLint est utilisée car la permission RECORD_AUDIO est vérifiée
     * et demandée dans l'Activity appelante.
     */
    @SuppressLint("MissingPermission")
    public void startListening() {
        if (isListening) return;

        if (audioRecord == null) {
            try {
                // L'appel nécessite la permission RECORD_AUDIO
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        bufferSize);
            } catch (SecurityException e) {
                // Gère explicitement le cas où la permission est manquante au runtime
                Log.e(TAG, "Erreur de sécurité : Permission RECORD_AUDIO manquante. " + e.getMessage());
                listener.onFeedbackNeeded("Erreur micro: Permission manquante. Veuillez redémarrer l'application.");
                return;
            } catch (Exception e) {
                Log.e(TAG, "Erreur d'initialisation AudioRecord: " + e.getMessage());
                listener.onFeedbackNeeded("Erreur micro: impossible d'initialiser l'appareil.");
                return;
            }
        }

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecord.startRecording();
            isListening = true;
            startTime = 0; // Réinitialiser le chrono au début
            listener.onFeedbackNeeded("Soufflez dans le micro pour appeler le chien !");
            handler.post(monitorRunnable); // Démarrer la boucle de monitoring
        } else {
            listener.onFeedbackNeeded("Le micro n'est pas prêt. Réessayez.");
        }
    }

    /**
     * Arrête l'enregistrement audio et le monitoring.
     * Libère les ressources du Handler et de l'AudioRecord.
     * Doit être appelée dans onPause() ou onDestroy() de l'Activity.
     */
    public void stopListening() {
        isListening = false;
        handler.removeCallbacks(monitorRunnable);
        handler.removeCallbacks(validationSuccessRunnable);
        startTime = 0;
        if (audioRecord != null) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }
    }

    /**
     * Lit les données du micro et retourne l'amplitude maximale (volume).
     */
    private int getAmplitude() {
        short[] buffer = new short[bufferSize];
        int readSize = audioRecord.read(buffer, 0, bufferSize);

        int maxAmplitude = 0;
        for (int i = 0; i < readSize; i++) {
            if (Math.abs(buffer[i]) > maxAmplitude) {
                maxAmplitude = Math.abs(buffer[i]);
            }
        }
        return maxAmplitude;
    }
}