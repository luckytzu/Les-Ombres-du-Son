package fr.upjv.lesombresduson;

import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.Toast;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class CeciliaGameActivity extends AppCompatActivity implements SensorEventListener {
    private Button btnBack;
    private MediaPlayer mediaPlayerIntro;
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor accelerometer;


    // Compteur de gestes (0:droite, 1:gauche, 2:haut, 3:bas)
    private int gestureCount = 0;

    // Seuil d'inclinaison pour la validation (en m/s^2)
    private final float TILT_THRESHOLD = 5.0f;

    // Durée de maintien nécessaire
    private static final long DELAY_MS = 3000;

    // Handler pour planifier et annuler l'action de validation
    private Handler validationHandler = new Handler();

    // Indique si le geste est en cours de validation (pour éviter les démarrages multiples)
    private boolean isValidationPending = false;

    // Runnable pour l'action de validation différée
    private Runnable validationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay_cecilia);

        btnBack = findViewById(R.id.button_back);

        // Initialisation du Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialisation des capteurs
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Initialisation du Runnable de validation
        validationRunnable = () -> {
            // Le geste a été maintenu pendant 3 secondes, on valide

            // 1. Vibration
            vibrator.vibrate(200);

            // 2. Son de validation (ding)
            MediaPlayer dingPlayer = MediaPlayer.create(this, R.raw.ding);
            if (dingPlayer != null) {
                dingPlayer.setOnCompletionListener(MediaPlayer::release);
                dingPlayer.start();
            }

            // 3. Passage à l'état suivant
            gestureCount++;
            isValidationPending = false; // Réinitialise l'état

            if (gestureCount < 4) {
                String nextGesture = "";
                if (gestureCount == 1) nextGesture = "à gauche";
                else if (gestureCount == 2) nextGesture = "vers le haut";
                else if (gestureCount == 3) nextGesture = "vers le bas";
                Toast.makeText(this, "✅ Geste Validé ! Relâchez et inclinez " + nextGesture + ".", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "🎉 Tous les gestes sont complétés. Le jeu peut continuer !", Toast.LENGTH_LONG).show();
                sensorManager.unregisterListener(this);
                // TODO: Code pour démarrer la prochaine étape du jeu
            }
        };

        // Initialisation de la voix off et du son de validation.
        // On vérifie s'ils existent déjà (utile si l'Activity est recréée).
        if (mediaPlayerIntro == null) {
            mediaPlayerIntro = MediaPlayer.create(this, R.raw.ding);
            mediaPlayerIntro.setLooping(false);
            mediaPlayerIntro.start();

            // L'écoute des capteurs ne commence qu'après la fin de la voix off
            mediaPlayerIntro.setOnCompletionListener(mp -> {
                Toast.makeText(this, "Voix off terminée. Inclinez votre téléphone à droite !", Toast.LENGTH_LONG).show();
                if (accelerometer != null) {
                    // Enregistre l'Activity comme écouteur du capteur d'accélération
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                } else {
                    Toast.makeText(this, "Capteur d'accélération non disponible.", Toast.LENGTH_LONG).show();
                }
            });
        }

        btnBack.setOnClickListener(v -> {
            sensorManager.unregisterListener(this);
            Intent intent = new Intent(CeciliaGameActivity.this, StartChoiseCharacter.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Détection des changements de données du capteur d'accélération.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            // On vérifie si une condition de geste est remplie
            boolean tiltDetected = false;

            switch (gestureCount) {
                case 0: // Droite
                    if (x < TILT_THRESHOLD) tiltDetected = true;
                    break;
                case 1: // Gauche
                    if (x > -TILT_THRESHOLD) tiltDetected = true;
                    break;
                case 2: // Haut
                    if (y < -TILT_THRESHOLD) tiltDetected = true;
                    break;
                case 3: // Bas
                    if (y > TILT_THRESHOLD) tiltDetected = true;
                    break;
            }

            if (tiltDetected && !isValidationPending) {
                // 1. Geste détecté : Démarrer la minuterie de 3 secondes
                isValidationPending = true;
                validationHandler.postDelayed(validationRunnable, DELAY_MS);

                String direction = "";
                if (gestureCount == 0) direction = "à droite";
                else if (gestureCount == 1) direction = "à gauche";
                else if (gestureCount == 2) direction = "vers le haut";
                else if (gestureCount == 3) direction = "vers le bas";

            } else if (!tiltDetected && isValidationPending) {
                // 2. Le téléphone a été relâché : Annuler la validation en attente
                validationHandler.removeCallbacks(validationRunnable);
                isValidationPending = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Ré-enregistre l'écouteur du capteur si le défi n'est pas terminé.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Si les gestes ne sont pas terminés et que l'accéléromètre est disponible
        boolean isIntroFinished = (mediaPlayerIntro != null && !mediaPlayerIntro.isPlaying());

        if (gestureCount < 4 && accelerometer != null && isIntroFinished) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Désenregistre l'écouteur du capteur pour économiser la batterie lorsque l'application passe en arrière-plan.
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Libération de toutes les ressources lorsque l'activité est détruite.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);

        if (validationHandler != null) {
            validationHandler.removeCallbacksAndMessages(null);
        }

        if (mediaPlayerIntro != null) {
            mediaPlayerIntro.stop();
            mediaPlayerIntro.release();
            mediaPlayerIntro = null;
        }
    }
}