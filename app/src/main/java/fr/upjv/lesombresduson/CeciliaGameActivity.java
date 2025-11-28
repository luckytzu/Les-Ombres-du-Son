package fr.upjv.lesombresduson;

import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Contrôleur principal pour l'activité du jeu Cecilia.
 */
public class CeciliaGameActivity extends AppCompatActivity implements GestureListener {
    private Button btnBack;
    private MediaPlayer mediaPlayerIntro;
    private MediaPlayer mediaPlayerAfterIntro;
    private Vibrator vibrator;
    private SensorGameManager gameManager; // Instance du manager de logique
    private TouchNavigationManager touchManager;
    private MicrophoneManager micManager;

    /**
     * Initialise l'activité.
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
        vibrator.vibrate(200);
        MediaPlayer dingPlayer = MediaPlayer.create(this, R.raw.ding);
        if (dingPlayer != null) {
            dingPlayer.setOnCompletionListener(MediaPlayer::release);
            dingPlayer.start();
        }

        if (isGameComplete) {
            Toast.makeText(this, "🎉 Tous les gestes sont complétés. Le jeu peut continuer !", Toast.LENGTH_LONG).show();
            gameManager.stopListening();
            if (mediaPlayerAfterIntro == null) {
                mediaPlayerAfterIntro = MediaPlayer.create(this, R.raw.cecilia_after_intro);
                mediaPlayerAfterIntro.setLooping(false);
                mediaPlayerAfterIntro.start();

                mediaPlayerAfterIntro.setOnCompletionListener(mp -> {
                    startTouchNavigationPhase();
                });
            }
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
            // L'action de validation est déjà gérée dans onGestureValidated
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Démarre la phase de navigation tactile
     */
    private void startTouchNavigationPhase() {
        Toast.makeText(this, "Glissez votre doigt sur l'écran pour chercher la porte. Le son vous guidera.", Toast.LENGTH_LONG).show();

        // 1. Obtenir les dimensions de l'écran (pour la cible)
        View rootView = getWindow().getDecorView();
        int width = rootView.getWidth();
        int height = rootView.getHeight();

        // 2. Initialiser le manager
        touchManager = new TouchNavigationManager(this, this, width, height);

        // 3. Attacher le manager à l'écoute des événements tactiles sur la vue racine
        rootView.setOnTouchListener(touchManager);
    }

    /**
     * Fin de l'intro de la chambre lorsque le joueur trouve la porte
     */
    @Override
    public void onTargetFound() {
        // 1. Nettoyage du manager tactile
        if (touchManager != null) {
            View rootView = getWindow().getDecorView();
            rootView.setOnTouchListener(null);
            touchManager.cleanup();
            touchManager = null;
        }

        Toast.makeText(this, "VICTOIRE ! La porte est trouvée. Bravo !", Toast.LENGTH_LONG).show();

        MediaPlayer successPlayer = MediaPlayer.create(this, R.raw.success_chime);
        if (successPlayer != null) {
            successPlayer.setOnCompletionListener(mp -> {
                mp.release();
                startMicrophonePhase();
            });
            successPlayer.start();
        } else {
            startMicrophonePhase();
        }
    }

    /**
     * Démarre la phase de détection du soufflement
     */
    private void startMicrophonePhase() {
        // 1. Initialiser le manager
        micManager = new MicrophoneManager(this);

        // 2. Démarrer l'écoute du micro
        micManager.startListening();
    }

    /**
     * Fin de l'intro lorsque le joueur trouve le chien
     */
    @Override
    public void onDogFound() {
        // Jouer le son de l'aboiement du chien (Validation du micro)
        MediaPlayer dogPlayer = MediaPlayer.create(this, R.raw.dog_bark);
        if (dogPlayer != null) {
            dogPlayer.setOnCompletionListener(MediaPlayer::release);
            dogPlayer.start();
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String characterName = "Cécilia (cécité totale)";

        FirebaseHelper.getInstance().updateGameProgress(
                userId,
                characterName,
                "introFinished",
                true
        );

        // Feedback et suite du jeu
        Toast.makeText(this, "VICTOIRE ! Le chien a aboyé. Vous êtes en sécurité !", Toast.LENGTH_LONG).show();
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

        if (mediaPlayerAfterIntro != null && !mediaPlayerAfterIntro.isPlaying() && gameManager.getGestureCount() == 0) {
            mediaPlayerAfterIntro.start();
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

        if (mediaPlayerAfterIntro != null && mediaPlayerAfterIntro.isPlaying()) {
            mediaPlayerAfterIntro.pause();
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

        if (mediaPlayerAfterIntro != null) {
            mediaPlayerAfterIntro.stop();
            mediaPlayerAfterIntro.release();
            mediaPlayerAfterIntro = null;
        }

        if (touchManager != null) {
            touchManager.cleanup();
        }

        if (micManager != null) {
            micManager.stopListening();
        }
    }
}