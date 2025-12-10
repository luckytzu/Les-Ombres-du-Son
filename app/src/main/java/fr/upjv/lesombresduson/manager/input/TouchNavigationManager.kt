package fr.upjv.lesombresduson.manager.input

import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import fr.upjv.lesombresduson.R
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Gère la logique de la phase de navigation tactile (recherche de la cible).
 * Doit être attaché à une View ou à l'Activity pour intercepter les événements tactiles.
 */
class TouchNavigationManager(
    private val context: Context,
    private val listener: GestureListener,
    screenWidth: Int,
    screenHeight: Int
) : View.OnTouchListener {

    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var navigationSoundPlayer: MediaPlayer? = null

    // Logique de Jeu (constantes)
    private val MAX_DISTANCE = 1000 // Distance maximale de référence pour le volume
    private val TARGET_RADIUS = 150  // Rayon en pixels pour valider la sortie

    // Position de la cible (définies dans le constructeur)
    private val targetX: Int
    private val targetY: Int

    init {
        // Initialisation de la position de la cible dans le bloc init
        val marginX = (screenWidth * 0.1).toInt()
        val marginY = (screenHeight * 0.1).toInt()

        // X cible aléatoire entre 10% de la largeur et 90% de la largeur
        this.targetX = marginX + (Math.random() * (screenWidth - 2 * marginX)).toInt()
        // Y cible aléatoire entre 10% de la hauteur et 90% de la hauteur
        this.targetY = marginY + (Math.random() * (screenHeight - 2 * marginY)).toInt()

        initializeNavigationSound()
    }

    /**
     * Initialise le MediaPlayer pour le son de navigation (son constant).
     * Définit la boucle (looping) et démarre la lecture avec un volume minimal (0.1f).
     */
    private fun initializeNavigationSound() {
        if (navigationSoundPlayer == null) {
            navigationSoundPlayer = MediaPlayer.create(context, R.raw.ambiance_murmur)?.apply {
                isLooping = true
                setVolume(0.1f, 0.1f) // Démarrer doucement
                start()
            }
        }
    }

    /**
     * Nettoie les ressources. À appeler dans onDestroy().
     */
    fun cleanup() {
        navigationSoundPlayer?.let { player ->
            player.stop()
            player.release()
        }
        navigationSoundPlayer = null
    }

    /**
     * Méthode de rappel (callback) appelée lors d'un événement tactile sur la vue attachée.
     * Gère le mouvement du doigt (MOVE) pour ajuster le feedback, et la fin du toucher (UP)
     * pour vérifier si la cible a été trouvée.
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val currentX = event.x
        val currentY = event.y

        // Calcul de la distance. Conversion implicite en Double pour les fonctions Math.pow/sqrt
        val distance = sqrt(
            (currentX - targetX).toDouble().pow(2.0) +
                    (currentY - targetY).toDouble().pow(2.0)
        )

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                handleTouchFeedback(distance)
            }
            MotionEvent.ACTION_UP -> {
                // Arrêt du son si le doigt est levé
                stopTouchFeedback()

                // Validation de la cible
                if (distance < TARGET_RADIUS) {
                    listener.onTargetFound()
                } else {
                    listener.onFeedbackNeeded("Vous vous éloignez, réessayez.")
                }
            }
        }
        return true
    }

    /**
     * Ajuste le volume du son de navigation et active une vibration en fonction de la distance.
     * Le volume augmente avec la proximité (gain), et la vibration devient plus longue/fréquente.
     *
     * @param distance La distance calculée en pixels entre le doigt et la cible.
     */
    private fun handleTouchFeedback(distance: Double) {
        navigationSoundPlayer ?: return

        // 1. Calcul du Gain
        val normalizedDistance = (distance.coerceAtMost(MAX_DISTANCE.toDouble()) / MAX_DISTANCE).toFloat()
        val gain = 1.0f - normalizedDistance

        val minGain = 0.1f
        val finalVolume = minGain + (1.0f - minGain) * gain

        // 2. Appliquer le volume
        navigationSoundPlayer?.setVolume(finalVolume, finalVolume)

        // 3. Feedback Haptique (Vibrer plus fort/longtemps quand on est proche)
        @Suppress("DEPRECATION")
        if (distance < 200) {
            if (distance < TARGET_RADIUS * 2) {
                vibrator.vibrate(100)
            } else {
                vibrator.vibrate(50)
            }
        }
    }

    /**
     * Réduit le volume du son de navigation au niveau minimum (0.1f) lorsque le doigt est levé (ACTION_UP).
     * Le son ne s'arrête pas complètement, permettant au joueur de reprendre la recherche rapidement.
     */
    private fun stopTouchFeedback() {
        navigationSoundPlayer?.setVolume(0.1f, 0.1f)
    }
}