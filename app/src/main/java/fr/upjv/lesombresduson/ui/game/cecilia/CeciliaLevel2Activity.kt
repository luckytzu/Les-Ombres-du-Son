package fr.upjv.lesombresduson.ui.game.cecilia

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.upjv.lesombresduson.R

class CeciliaLevel2Activity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private lateinit var vibrator: Vibrator

    // IDs des sons
    private var soundAmbianceId: Int = -1
    private var soundFeuRougeId: Int = -1
    private var soundFeuVertId: Int = -1

    // IDs des flux (streams) pour modifier le volume en temps réel
    private var streamAmbianceId: Int = -1
    private var streamFeuId: Int = -1

    private var isFeuVert = false
    private val handler = Handler(Looper.getMainLooper())
    private var isLevelComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Utilisez un layout simple, idéalement un fond noir pour l'immersion
        setContentView(R.layout.activity_gameplay_cecilia)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        initAudio()
        startTrafficCycle()
    }

    private fun initAudio() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attrs)
            .build()

        // Chargement des ressources (Assurez-vous d'avoir ces fichiers dans res/raw)
        soundAmbianceId = soundPool.load(this, R.raw.ambiance_trafic_dense, 1)
        soundFeuRougeId = soundPool.load(this, R.raw.feu_sonore_rouge, 1) // battement lent
        soundFeuVertId = soundPool.load(this, R.raw.feu_sonore_vert, 1)   // bip rapide

        soundPool.setOnLoadCompleteListener { _, _, _ ->
            // On lance l'ambiance dès que c'est chargé
            streamAmbianceId = soundPool.play(soundAmbianceId, 1f, 1f, 1, -1, 1f)
            lancerCycleFeu()
        }
    }

    /**
     * Alterne entre le feu rouge et le feu vert de manière aléatoire
     */
    private fun lancerCycleFeu() {
        if (isLevelComplete) return

        // On arrête le son précédent du feu
        soundPool.stop(streamFeuId)

        isFeuVert = !isFeuVert
        val currentSound = if (isFeuVert) soundFeuVertId else soundFeuRougeId

        // Le feu est audible mais faible par défaut (masqué par le trafic)
        streamFeuId = soundPool.play(currentSound, 0.1f, 0.1f, 1, -1, 1f)

        // Change d'état toutes les 4 à 7 secondes
        val prochainChange = (4000..7000).random().toLong()
        handler.postDelayed({ lancerCycleFeu() }, prochainChange)
    }

    /**
     * Mécanique de FOCUS : Appuyer baisse la ville et monte le feu.
     * Relâcher simule la traversée.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLevelComplete) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Focus : On réduit le bruit de la ville pour mieux entendre le signal
                soundPool.setVolume(streamAmbianceId, 0.15f, 0.15f)
                soundPool.setVolume(streamFeuId, 0.9f, 0.9f)
                vibrer(50) // Petit retour haptique de concentration
            }
            MotionEvent.ACTION_UP -> {
                // Relâcher = Décider de traverser
                tenterTraversee()
            }
        }
        return true
    }

    private fun tenterTraversee() {
        if (isFeuVert) {
            victoire()
        } else {
            // Échec : Danger !
            vibrer(800)
            Toast.makeText(this, "Danger ! Le feu était rouge.", Toast.LENGTH_SHORT).show()
            // On remet le son normal
            soundPool.setVolume(streamAmbianceId, 1f, 1f)
            soundPool.setVolume(streamFeuId, 0.1f, 0.1f)
        }
    }

    private fun victoire() {
        isLevelComplete = true
        handler.removeCallbacksAndMessages(null)
        vibrer(1000)

        Toast.makeText(this, "Traversée réussie avec succès !", Toast.LENGTH_LONG).show()

        // Transition vers Niveau 3 après 3 secondes
        handler.postDelayed({
            // val intent = Intent(this, CeciliaLevel3Activity::class.java)
            // startActivity(intent)
            finish()
        }, 3000)
    }

    private fun startTrafficCycle() {
        // Si le niveau est déjà fini, on ne relance pas le cycle
        if (isLevelComplete) return

        // 1. Arrêter le son du feu actuel s'il existe
        soundPool.stop(streamFeuId)

        // 2. Basculer l'état (Si c'était rouge, ça devient vert, et inversement)
        isFeuVert = !isFeuVert

        // 3. Choisir le bon son selon l'état
        val soundId = if (isFeuVert) soundFeuVertId else soundFeuRougeId

        // 4. Jouer le son en boucle (-1) à un volume faible (masqué par le trafic)
        // Le volume augmentera quand l'utilisateur utilisera la mécanique de "Focus"
        streamFeuId = soundPool.play(soundId, 0.1f, 0.1f, 1, -1, 1f)

        // 5. Déterminer la durée du prochain cycle (ex: entre 5 et 10 secondes)
        val cycleDuration = (5000..10000).random().toLong()

        // 6. Planifier le prochain changement d'état
        handler.postDelayed({
            startTrafficCycle()
        }, cycleDuration)
    }

    private fun vibrer(duree: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duree, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duree)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
    }
}