package fr.upjv.lesombresduson.manager.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import fr.upjv.lesombresduson.ui.game.cecilia.CeciliaGameActivityAfterIntro

/**
 * Manager de détection de mouvements par accéléromètre.
 * Gère une séquence de 5 inclinaisons (Haut, Bas, Haut, Droite, Gauche)
 * avec un système de validation par maintien temporel.
 */
class Level1SensorManager(
    context: Context,
    private val listener: CeciliaGameActivityAfterIntro
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Paramètres de détection
    var gestureCount = 0
    private val START_THRESHOLD = 5.0f
    private val KEEP_THRESHOLD = 3.5f
    private val DELAY_MS = 2000L

    // Gestion de la validation
    private val validationHandler = Handler(Looper.getMainLooper())
    private var isValidationPending = false

    private val gestureInstructions = arrayOf(
        "vers le haut", "vers le bas",
        "vers le haut",
        "à droite", "à gauche"
    )

    private val validationRunnable = Runnable {
        listener.onFeedbackNeeded("VALIDATE")
        gestureCount++
        isValidationPending = false

        val isGameComplete = (gestureCount >= 5)
        val nextInstruction = if (isGameComplete) "" else gestureInstructions[gestureCount]

        listener.onGestureValidated(isGameComplete, nextInstruction)
    }

    /**
     * Initialise l'écoute du capteur si l'accéléromètre est disponible.
     */
    fun startListening() {
        if (accelerometer != null && gestureCount < 6) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Arrête l'écoute et annule toute validation en cours.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        cancelPendingValidation()
    }

    private fun cancelPendingValidation() {
        validationHandler.removeCallbacks(validationRunnable)
        isValidationPending = false
    }

    /**
     * Analyse les données de l'accéléromètre pour identifier l'inclinaison.
     * Utilise une hystérésis (START vs KEEP) pour stabiliser la détection.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            val threshold = if (isValidationPending) KEEP_THRESHOLD else START_THRESHOLD
            var tiltDetected = false

            when (gestureCount) {
                0, 2 -> if (y < -threshold) tiltDetected = true
                1 -> if (y > threshold) tiltDetected = true
                3 -> if (x < -threshold) tiltDetected = true
                4 -> if (x > threshold) tiltDetected = true
            }

            if (tiltDetected && !isValidationPending) {
                isValidationPending = true
                validationHandler.postDelayed(validationRunnable, DELAY_MS)
                listener.onFeedbackNeeded("Mouvement ${gestureInstructions[gestureCount]} détecté...")
            } else if (!tiltDetected && isValidationPending) {
                cancelPendingValidation()
                listener.onFeedbackNeeded("Position perdue.")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}