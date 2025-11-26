package fr.upjv.lesombresduson;

import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Contrôleur principal pour l'activité du jeu Cecilia.
 * Gère l'UI, le cycle de vie Android, les médias et implémente GestureListener
 * pour interagir avec SensorGameManager.
 */
public class CeciliaGameActivity extends AppCompatActivity implements GestureListener {

    private Button btnBack;
    private MediaPlayer mediaPlayerIntro;
    private Vibrator vibrator;
    private SensorGameManager gameManager; // Instance du manager de logique

    /**
     * Initialise l'activité, configure l'UI, le Vibrator, le MediaPlayer et le manager de jeu.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay_cecilia);

        btnBack = findViewById(R.id.button_back);

        // Initialisation du Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialisation du manager de jeu
        gameManager = new SensorGameManager(this, this);

        // Initialisation de la voix off.
        if (mediaPlayerIntro == null) {
            // Utiliser R.raw.cecilia_intro pour la voix off
            mediaPlayerIntro = MediaPlayer.create(this, R.raw.cecilia_intro);
            mediaPlayerIntro.setLooping(false);
            mediaPlayerIntro.start();

            // L'écoute des capteurs ne commence qu'après la fin de la voix off
            mediaPlayerIntro.setOnCompletionListener(mp -> {
                // Appelle la méthode d'instruction de l'Activity via le listener
                onInstructionReady("Inclinez votre téléphone à droite !");
            });
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(CeciliaGameActivity.this, StartChoiseCharacter.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // --- Implémentation de GestureListener (Réactions du jeu) ---

    /**
     * Prépare l'instruction après la fin de la voix off et démarre l'écoute du capteur si disponible.
     * @param instruction Texte de l'instruction à afficher.
     */
    @Override
    public void onInstructionReady(String instruction) {
        Toast.makeText(this, "Voix off terminée. " + instruction, Toast.LENGTH_LONG).show();
        if (gameManager.isAccelerometerAvailable()) {
            gameManager.startListening();
        } else {
            Toast.makeText(this, "Capteur d'accélération non disponible.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Gère la validation d'un geste : vibration, ding et message.
     * @param isGameComplete Indique si tous les gestes sont complétés.
     * @param nextInstruction Instruction pour le prochain geste.
     */
    @Override
    public void onGestureValidated(boolean isGameComplete, String nextInstruction) {
        // Le SensorGameManager a demandé une validation, gérons le feedback

        // Exécuter la vibration et le son de validation (ding)
        vibrator.vibrate(200);
        MediaPlayer dingPlayer = MediaPlayer.create(this, R.raw.ding);
        if (dingPlayer != null) {
            dingPlayer.setOnCompletionListener(MediaPlayer::release);
            dingPlayer.start();
        }

        if (isGameComplete) {
            Toast.makeText(this, "🎉 Tous les gestes sont complétés. Le jeu peut continuer !", Toast.LENGTH_LONG).show();
            gameManager.stopListening();
            // TODO: Code pour démarrer la prochaine étape du jeu
        } else {
            Toast.makeText(this, "✅ Geste Validé ! Relâchez et inclinez " + nextInstruction + ".", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Fournit un feedback visuel ou textuel selon le message reçu.
     * @param message Message de feedback à afficher.
     */
    @Override
    public void onFeedbackNeeded(String message) {
        // Afficher des messages de maintien/annulation
        if (message.equals("VALIDATE")) {
            // L'action de validation est déjà gérée dans onGestureValidated pour centraliser le ding/vibrateur.
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    // --- Gestion du cycle de vie Android ---

    /**
     * Reprend le jeu après une pause : audio et écoute des gestes si nécessaire.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Reprendre l'audio s'il était en pause
        if (mediaPlayerIntro != null && !mediaPlayerIntro.isPlaying() && gameManager.getGestureCount() == 0) {
            mediaPlayerIntro.start();
        }

        // Si l'intro est finie ET que le jeu n'est pas terminé, on relance l'écoute
        boolean isIntroFinished = (mediaPlayerIntro != null && !mediaPlayerIntro.isPlaying());

        if (gameManager.getGestureCount() < 4 && isIntroFinished) {
            gameManager.startListening();
        }
    }

    /**
     * Met le jeu en pause : stoppe l'écoute des gestes et pause la voix off si elle joue.
     */
    @Override
    protected void onPause() {
        super.onPause();
        gameManager.stopListening();

        if (mediaPlayerIntro != null && mediaPlayerIntro.isPlaying()) {
            mediaPlayerIntro.pause();
        }
    }

    /**
     * Nettoie les ressources lors de la destruction de l'activité : arrête capteurs et libère MediaPlayer.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameManager.stopListening(); // Assure l'arrêt du capteur et du Handler

        if (mediaPlayerIntro != null) {
            mediaPlayerIntro.stop();
            mediaPlayerIntro.release();
            mediaPlayerIntro = null;
        }
    }
}