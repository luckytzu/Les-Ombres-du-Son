package fr.upjv.lesombresduson.ui.game.cecilia

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import fr.upjv.lesombresduson.R
import fr.upjv.lesombresduson.data.remote.FirebaseHelper
import fr.upjv.lesombresduson.manager.input.GestureListener
import fr.upjv.lesombresduson.manager.input.TouchNavigationManager
import fr.upjv.lesombresduson.manager.sensor.MicrophoneManager
import fr.upjv.lesombresduson.manager.sensor.SensorGameManager
import fr.upjv.lesombresduson.ui.StartChoiseCharacter

/**
 * Contrôleur principal pour l'activité du jeu Cecilia.
 * Implémente GestureListener pour les retours du SensorGameManager.
 */
class CeciliaGameActivity : AppCompatActivity(), GestureListener {

    // Utilisation de lateinit pour les variables initialisées dans onCreate
    private lateinit var btnBack: Button

    // Utilisation de 'by lazy' pour initialiser le Vibrator une seule fois.
    private val vibrator: Vibrator by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Managers de jeu
    private lateinit var gameManager: SensorGameManager
    private var touchManager: TouchNavigationManager? = null
    private var micManager: MicrophoneManager? = null

    // MediaPlayers (peut être null avant l'initialisation ou après la libération)
    private var mediaPlayerIntro: MediaPlayer? = null
    private var mediaPlayerAfterIntro: MediaPlayer? = null

    companion object {
        // Code de permission pour le microphone (pour la phase du chien)
        private const val MICROPHONE_PERMISSION_CODE = 102
    }

    // --- Cycle de vie Android ---

    /**
     * Méthode de création de l'Activity, appelée au démarrage.
     * Initialise l'interface utilisateur, le SensorGameManager et l'audio d'introduction.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay_cecilia)

        btnBack = findViewById(R.id.button_back)

        // Initialisation du manager de jeu
        gameManager = SensorGameManager(this, this)

        // Initialisation de la voix off.
        if (mediaPlayerIntro == null) {
            mediaPlayerIntro = MediaPlayer.create(this, R.raw.cecilia_intro)?.apply {
                isLooping = false
                start()
                // Utilisation d'un lambda pour le listener de complétion
                setOnCompletionListener { mp: MediaPlayer -> // Correction pour éviter l'ambiguïté du type
                    // Appelle la méthode d'instruction de l'Activity via le listener
                    onInstructionReady("Inclinez votre téléphone à droite ! L'intro est finie.")
                }
            }
        }

        // Listener simplifié en Kotlin (lambda)
        btnBack.setOnClickListener {
            val intent = Intent(this, StartChoiseCharacter::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Appelée lorsque l'Activity redevient visible (après onCreate ou onPause).
     * Reprend l'audio en pause et redémarre l'écoute des capteurs si l'intro est terminée.
     */
    override fun onResume() {
        super.onResume()

        // Reprendre l'audio s'il était en pause
        if (mediaPlayerIntro?.isPlaying == false && gameManager.gestureCount == 0) {
            mediaPlayerIntro?.start()
        }

        if (mediaPlayerAfterIntro?.isPlaying == false && gameManager.gestureCount == 0) {
            mediaPlayerAfterIntro?.start()
        }

        // Si l'audio intro est fini ET que le jeu n'est pas terminé, on relance l'écoute
        val isIntroFinished = (mediaPlayerIntro == null || mediaPlayerIntro?.isPlaying == false)

        if (gameManager.gestureCount < 4 && isIntroFinished) {
            gameManager.startListening()
        }
    }

    /**
     * Appelée lorsque l'Activity passe en arrière-plan (mais est toujours en mémoire).
     * Met en pause l'écoute des capteurs et l'audio pour économiser la batterie.
     */
    override fun onPause() {
        super.onPause()
        gameManager.stopListening()

        mediaPlayerIntro?.pause()
        mediaPlayerAfterIntro?.pause()
    }

    /**
     * Appelée lorsque l'Activity est définitivement détruite.
     * Nettoie toutes les ressources : capteurs, managers et MediaPlayers.
     */
    override fun onDestroy() {
        super.onDestroy()
        gameManager.stopListening()

        // Utilisation de l'opérateur 'safe call' (?) et 'let' pour le nettoyage
        mediaPlayerIntro?.let {
            it.stop()
            it.release()
            mediaPlayerIntro = null
        }

        mediaPlayerAfterIntro?.let {
            it.stop()
            it.release()
            mediaPlayerAfterIntro = null
        }

        touchManager?.cleanup()
        micManager?.stopListening()
    }

    // --- Implémentation de GestureListener (Réactions du jeu) ---

    /**
     * Répond à l'événement indiquant que la voix off d'instruction est prête.
     * Affiche l'instruction et démarre l'écoute de l'accéléromètre.
     * @param instruction Le texte de l'instruction.
     */
    override fun onInstructionReady(instruction: String) {
        Toast.makeText(this, "Voix off terminée. $instruction", Toast.LENGTH_LONG).show()
        if (gameManager.isAccelerometerAvailable) {
            gameManager.startListening()
        } else {
            Toast.makeText(this, "Capteur d'accélération non disponible.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Répond à l'événement de validation d'un geste par le SensorGameManager.
     * Fournit un retour haptique (vibration) et sonore (ding), puis passe à l'étape suivante.
     * @param isGameComplete Indique si la série de gestes est terminée.
     * @param nextInstruction La prochaine instruction à donner au joueur.
     */
    override fun onGestureValidated(isGameComplete: Boolean, nextInstruction: String) {
        @Suppress("DEPRECATION") // Utilisation standard du Vibrator
        vibrator.vibrate(200)

        // Utilisation de 'use' pour garantir que le MediaPlayer est bien relâché (release)
        MediaPlayer.create(this, R.raw.ding)?.apply {
            setOnCompletionListener { mp ->
                mp.release() // Libère la ressource après la lecture
            }
            start()
        }

        if (isGameComplete) {
            Toast.makeText(this, "🎉 Tous les gestes sont complétés. Le jeu peut continuer !", Toast.LENGTH_LONG).show()
            gameManager.stopListening()

            mediaPlayerAfterIntro = MediaPlayer.create(this, R.raw.cecilia_after_intro)?.apply {
                isLooping = false
                start()
                setOnCompletionListener { mp: MediaPlayer -> // Correction pour éviter l'ambiguïté du type
                    startTouchNavigationPhase()
                }
            }
        } else {
            Toast.makeText(this, "✅ Geste Validé ! Relâchez et inclinez $nextInstruction.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Répond aux retours (feedback) du SensorGameManager (ex: "Maintenez 3s..." ou "Annulation").
     * Affiche un Toast informatif pour le joueur.
     * @param message Le message à afficher.
     */
    override fun onFeedbackNeeded(message: String) {
        if (message == "VALIDATE") {
            // L'action de validation est déjà gérée dans onGestureValidated
            return
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Démarre la phase de navigation tactile
     */
    private fun startTouchNavigationPhase() {
        Toast.makeText(this, "Glissez votre doigt sur l'écran pour chercher la porte. Le son vous guidera.", Toast.LENGTH_LONG).show()

        // 1. Obtenir les dimensions de l'écran (pour la cible)
        val rootView = window.decorView
        val width = rootView.width
        val height = rootView.height

        // 2. Initialiser le manager
        touchManager = TouchNavigationManager(this, this, width, height)

        // 3. Attacher le manager à l'écoute des événements tactiles sur la vue racine
        rootView.setOnTouchListener(touchManager)
    }

    /**
     * Répond à l'événement de détection de la cible (la porte) par le TouchNavigationManager.
     * Arrête la phase tactile, joue un son de succès et lance la phase microphone.
     */
    override fun onTargetFound() {
        // 1. Nettoyage du manager tactile
        touchManager?.let {
            val rootView = window.decorView
            rootView.setOnTouchListener(null)
            it.cleanup()
            touchManager = null
        }

        Toast.makeText(this, "VICTOIRE ! La porte est trouvée. Bravo !", Toast.LENGTH_LONG).show()

        // Utilisation de 'use' pour garantir la libération de la ressource audio
        MediaPlayer.create(this, R.raw.success_chime)?.apply {
            setOnCompletionListener { mp: MediaPlayer ->
                mp.release() // Libérer la ressource après la lecture
                startMicrophonePhase() // Ensuite, démarrer la phase suivante
            }
            start()
        } ?: startMicrophonePhase() // Si la création du player échoue, démarrer la phase suivante immédiatement
    }

    /**
     * Démarre la phase de détection du soufflement
     */
    private fun startMicrophonePhase() {
        // Vérification et demande de permission RECORD_AUDIO si nécessaire
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MICROPHONE_PERMISSION_CODE)
        } else {
            initAndStartMicManager()
        }
    }

    // Initialisation et démarrage du MicrophoneManager
    private fun initAndStartMicManager() {
        micManager = MicrophoneManager(this)
        micManager?.startListening()
    }

    /**
     * Gère le résultat de la demande de permission (après la demande de RECORD_AUDIO).
     * Si la permission est accordée, initialise et démarre le MicrophoneManager.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MICROPHONE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initAndStartMicManager() // Permission accordée
            } else {
                Toast.makeText(this, "Le microphone est nécessaire pour cette phase du jeu.", Toast.LENGTH_LONG).show()
                // Le jeu peut continuer d'une autre manière ou s'arrêter.
            }
        }
    }

    /**
     * Répond à l'événement de détection du chien (soufflement détecté par le micro).
     * Joue un son d'aboiement, arrête le micro et envoie la progression à Firebase.
     */
    override fun onDogFound() {
        // Jouer le son de l'aboiement du chien (Validation du micro)
        MediaPlayer.create(this, R.raw.dog_bark)?.apply {
            setOnCompletionListener { mp: MediaPlayer ->
                mp.release()
            }
            start()
        }

        // Assurez-vous que l'utilisateur est connecté pour éviter un crash
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val characterName = "Cécilia (cécité totale)"

        if (userId != null) {
            // Envoi des données de progression au backend Firebase/GCP
            FirebaseHelper.getInstance().updateGameProgress(
                userId,
                characterName,
                "introFinished",
                true
            )
        }

        // Nettoyage et Feedback
        micManager?.stopListening()
        Toast.makeText(this, "VICTOIRE ! Le chien a aboyé. Vous êtes en sécurité !", Toast.LENGTH_LONG).show()
    }
}