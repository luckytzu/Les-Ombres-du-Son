package fr.upjv.lesombresduson;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

/**
 * Gère la logique des capteurs, la détection des gestes, le chronométrage
 * et l'état du jeu, et communique les événements à l'Activity via GestureListener.
 */
public class SensorGameManager implements SensorEventListener {

    private final GestureListener listener;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;

    private int gestureCount = 0; // Compteur de gestes (0:droite, 1:gauche, 2:haut, 3:bas)

    private static final long DELAY_MS = 3000;
    private final float TILT_THRESHOLD = 5.0f;
    private final Handler validationHandler = new Handler();
    private final Runnable validationRunnable;
    private boolean isValidationPending = false;

    // Instructions pour le jeu
    private final String[] gestureInstructions = {
        "à droite", // Geste 0
        "à gauche", // Geste 1
        "vers le haut", // Geste 2
        "vers le bas"   // Geste 3
    };

    /**
     * Gère l’accéléromètre, la détection des gestes et leur validation dans le mini-jeu.
     */
    public SensorGameManager(Context context, GestureListener listener) {
        this.listener = listener;

        // Initialisation des capteurs
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            this.accelerometer = null;
        }

        // Initialisation du Runnable de validation
        validationRunnable = () -> {
            // Le geste a été maintenu pendant 3 secondes, on valide

            // 1. Demande de feedback de validation à l'Activity (vibration et ding)
            listener.onFeedbackNeeded("VALIDATE");

            // 2. Passage à l'état suivant
            gestureCount++;
            isValidationPending = false;

            // 3. Communiquer l'état final à l'Activity
            boolean isGameComplete = (gestureCount >= 4);
            String nextInstruction = isGameComplete ? "" : gestureInstructions[gestureCount];

            listener.onGestureValidated(isGameComplete, nextInstruction);
        };
    }

    /**
     * Enregistre l'écouteur du capteur.
     */
    public void startListening() {
        if (accelerometer != null && gestureCount < 4) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Désenregistre l'écouteur du capteur.
     */
    public void stopListening() {
        sensorManager.unregisterListener(this);
        cancelPendingValidation();
    }

    /**
     * Annule toute validation en attente dans le Handler.
     */
    private void cancelPendingValidation() {
        if (validationHandler != null) {
            validationHandler.removeCallbacks(validationRunnable);
            isValidationPending = false;
        }
    }

    // --- Implémentation de SensorEventListener ---

    /**
     * Détecte les mouvements de l'accéléromètre pour valider les gestes du jeu.
     * @param event Événement du capteur contenant les valeurs d'accélération.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            boolean tiltDetected = false;

            // Logique de détection des gestes (avec correction Droite/Gauche)
            switch (gestureCount) {
                case 0: // Droite
                    if (x < -TILT_THRESHOLD) tiltDetected = true;
                    break;
                case 1: // Gauche
                    if (x > TILT_THRESHOLD) tiltDetected = true;
                    break;
                case 2: // Haut
                    if (y > -TILT_THRESHOLD) tiltDetected = true;
                    break;
                case 3: // Bas
                    if (y < TILT_THRESHOLD) tiltDetected = true;
                    break;
            }

            if (tiltDetected && !isValidationPending) {
                // 1. Geste détecté : Démarrer la minuterie de 3 secondes
                isValidationPending = true;
                validationHandler.postDelayed(validationRunnable, DELAY_MS);

                // Demander à l'Activity d'afficher le Toast de maintien
                String direction = gestureInstructions[gestureCount];
                listener.onFeedbackNeeded("Inclinaison " + direction + " détectée. Maintenez pendant 3s...");

            } else if (!tiltDetected && isValidationPending) {
                // 2. Le téléphone a été relâché : Annuler la validation
                cancelPendingValidation();
                // Demander à l'Activity d'afficher le Toast d'annulation
                listener.onFeedbackNeeded("Relâchement détecté. Annulation de la validation.");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignoré
    }

    // Méthodes utilitaires
    public boolean isAccelerometerAvailable() {
        return accelerometer != null;
    }

    public int getGestureCount() {
        return gestureCount;
    }
}