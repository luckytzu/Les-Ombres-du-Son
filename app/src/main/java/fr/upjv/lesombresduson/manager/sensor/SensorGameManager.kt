package fr.upjv.lesombresduson.manager.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import fr.upjv.lesombresduson.ui.game.cecilia.CeciliaGameActivity

/**
 * Gère la logique des capteurs, la détection des gestes, le chronométrage
 * et l'état du jeu, et communique les événements à l'Activity via GestureListener.
 */
class SensorGameManager(private val context: Context, private val listener: CeciliaGameActivity) : SensorEventListener {

    private val sensorManager: SensorManager
    private val accelerometer: Sensor?

    // Utilisation de var pour les variables d'état
    var gestureCount = 0 // Compteur de gestes (0:droite, 1:gauche, 2:haut, 3:bas)
        private set // Rendre le setter privé pour contrôler les modifications

    // Constantes de jeu
    private val DELAY_MS = 3000L // 3 secondes
    private val TILT_THRESHOLD = 5.0f

    // Gestion du chronométrage et de l'état
    private val validationHandler = Handler(Looper.getMainLooper())
    private var isValidationPending = false

    // Instructions pour le jeu
    private val gestureInstructions = arrayOf(
        "à droite", // Geste 0
        "à gauche", // Geste 1
        "vers le haut", // Geste 2
        "vers le bas"   // Geste 3
    )

    // Initialisation du Runnable de validation (Lambda)
    private val validationRunnable = Runnable {
        // Le geste a été maintenu pendant 3 secondes, on valide

        // 1. Demande de feedback de validation à l'Activity (vibration et ding)
        listener.onFeedbackNeeded("VALIDATE")

        // 2. Passage à l'état suivant
        gestureCount++
        isValidationPending = false

        // 3. Communiquer l'état final à l'Activity
        val isGameComplete = (gestureCount >= 4)
        val nextInstruction = if (isGameComplete) "" else gestureInstructions[gestureCount]

        listener.onGestureValidated(isGameComplete, nextInstruction)
    }

    /**
     * Bloc d'initialisation (équivalent au code du constructeur Java)
     */
    init {
        // Initialisation des capteurs
        this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    /**
     * Enregistre l'écouteur du capteur.
     */
    fun startListening() {
        if (accelerometer != null && gestureCount < 4) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Désenregistre l'écouteur du capteur.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        cancelPendingValidation()
    }

    /**
     * Annule toute validation en attente dans le Handler.
     */
    private fun cancelPendingValidation() {
        validationHandler.removeCallbacks(validationRunnable)
        isValidationPending = false
    }

    // --- Implémentation de SensorEventListener ---

    /**
     * Détecte les mouvements de l'accéléromètre pour valider les gestes du jeu.
     * @param event Événement du capteur contenant les valeurs d'accélération.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            var tiltDetected = false

            // Logique de détection des gestes (switch remplacé par when)
            when (gestureCount) {
                0 -> { // Droite
                    if (x < -TILT_THRESHOLD) tiltDetected = true
                }
                1 -> { // Gauche
                    if (x > TILT_THRESHOLD) tiltDetected = true
                }
                2 -> { // Haut (Correction: y > 0 quand l'écran est incliné vers le haut)
                    if (y < -TILT_THRESHOLD) tiltDetected = true // y devient négatif quand l'appareil est tourné vers le haut
                }
                3 -> { // Bas (Correction: y < 0 quand l'écran est incliné vers le bas)
                    if (y > TILT_THRESHOLD) tiltDetected = true // y devient positif quand l'appareil est tourné vers le bas
                }
            }

            if (tiltDetected && !isValidationPending) {
                // 1. Geste détecté : Démarrer la minuterie de 3 secondes
                isValidationPending = true
                validationHandler.postDelayed(validationRunnable, DELAY_MS)

                // Demander à l'Activity d'afficher le Toast de maintien
                val direction = gestureInstructions[gestureCount]
                listener.onFeedbackNeeded("Inclinaison $direction détectée. Maintenez pendant 3s...")

            } else if (!tiltDetected && isValidationPending) {
                // 2. Le téléphone a été relâché : Annuler la validation
                cancelPendingValidation()
                // Demander à l'Activity d'afficher le Toast d'annulation
                listener.onFeedbackNeeded("Relâchement détecté. Annulation de la validation.")
            }
        }
    }

    /**
     * Appelée par le système lorsque la précision du capteur change.
     * Nous ignorons les changements de précision pour ne pas complexifier le code inutilement.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Ignoré
    }

    // Méthodes utilitaires
    val isAccelerometerAvailable: Boolean
        get() = accelerometer != null
}