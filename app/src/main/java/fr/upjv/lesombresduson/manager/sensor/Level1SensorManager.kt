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
 * Version spécifique pour le Niveau 1 (Le Carrefour).
 */
class Level1SensorManager(
    context: Context,
    private val listener: CeciliaGameActivityAfterIntro
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var gestureCount = 0
    private val START_THRESHOLD = 5.0f  // Seuil pour déclencher
    private val KEEP_THRESHOLD = 3.5f   // Seuil plus bas pour maintenir (tolérance)
    private val DELAY_MS = 2000L        // 2 secondes sont souvent suffisantes pour l'immersion

    private val validationHandler = Handler(Looper.getMainLooper())
    private var isValidationPending = false

    private val gestureInstructions = arrayOf(
        "vers le haut", "vers le bas",
        "vers le haut", "vers le bas",
        "à droite", "à gauche"
    )

    private val validationRunnable = Runnable {
        listener.onFeedbackNeeded("VALIDATE")
        gestureCount++
        isValidationPending = false

        val isGameComplete = (gestureCount >= 6)
        val nextInstruction = if (isGameComplete) "" else gestureInstructions[gestureCount]

        listener.onGestureValidated(isGameComplete, nextInstruction)
    }

    fun startListening() {
        if (accelerometer != null && gestureCount < 6) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        cancelPendingValidation()
    }

    private fun cancelPendingValidation() {
        validationHandler.removeCallbacks(validationRunnable)
        isValidationPending = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            // On définit quel seuil utiliser :
            // Si on est déjà en train de valider, on utilise le seuil de tolérance (plus facile à garder)
            val threshold = if (isValidationPending) KEEP_THRESHOLD else START_THRESHOLD

            var tiltDetected = false

            when (gestureCount) {
                0, 2 -> if (y < -threshold) tiltDetected = true // HAUT
                1, 3 -> if (y > threshold) tiltDetected = true  // BAS
                4 -> if (x < -threshold) tiltDetected = true    // DROITE
                5 -> if (x > threshold) tiltDetected = true     // GAUCHE
            }

            if (tiltDetected && !isValidationPending) {
                // DÉBUT DE LA DÉTECTION
                isValidationPending = true
                validationHandler.postDelayed(validationRunnable, DELAY_MS)
                listener.onFeedbackNeeded("Mouvement détecté... Ne bougez plus !")

            } else if (!tiltDetected && isValidationPending) {
                // SORTIE DE LA ZONE DE TOLÉRANCE
                cancelPendingValidation()
                listener.onFeedbackNeeded("Position perdue.")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}