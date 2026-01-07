package fr.upjv.lesombresduson.ui.game.cecilia

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import fr.upjv.lesombresduson.R
import fr.upjv.lesombresduson.data.remote.FirebaseHelper
import fr.upjv.lesombresduson.manager.sensor.Level1SensorListener
import fr.upjv.lesombresduson.manager.sensor.Level1SensorManager
import fr.upjv.lesombresduson.ui.StartChoiseCharacter

/**
 * Activité gérant le premier niveau de jeu pour le personnage de Cécilia.
 * Implémente une progression sonore basée sur la détection de mouvements
 * et une interaction tactile pour l'exploration de l'environnement.
 */
class CeciliaGameActivityAfterIntro : AppCompatActivity(), Level1SensorListener {

    private lateinit var btnBack: Button
    private lateinit var vibrator: Vibrator
    private lateinit var level1SensorManager: Level1SensorManager

    private var soundIntroVoice: MediaPlayer? = null
    private var isIntroFinished = false
    private var isWon = false

    private lateinit var soundPool: SoundPool
    private val progressionSoundIds = mutableListOf<Int>()
    private var carSoundId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay_cecilia)

        btnBack = findViewById(R.id.button_back)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        level1SensorManager = Level1SensorManager(this, this)

        initAudioEngine()
        lancerVoixOff()

        btnBack.setOnClickListener {
            val intent = Intent(this, StartChoiseCharacter::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Configure le moteur audio SoundPool pour les effets sonores à faible latence.
     */
    private fun initAudioEngine() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        val resIds = listOf(R.raw.son1, R.raw.son2, R.raw.son3, R.raw.son4, R.raw.son5)
        resIds.forEach { id -> progressionSoundIds.add(soundPool.load(this, id, 1)) }

        carSoundId = soundPool.load(this, R.raw.ambiance_carrefour, 1)
    }

    /**
     * Gère la lecture de la narration initiale et débloque le gameplay à la fin de celle-ci.
     */
    private fun lancerVoixOff() {
        soundIntroVoice = MediaPlayer.create(this, R.raw.voix_off_niveau1)
        soundIntroVoice?.setOnCompletionListener {
            isIntroFinished = true
            level1SensorManager.startListening()
            vibrer(200)
            it.release()
            soundIntroVoice = null
        }
        soundIntroVoice?.start()
    }

    /**
     * Intercepte les interactions tactiles pour déclencher les retours sonores.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isIntroFinished && event.action == MotionEvent.ACTION_DOWN) {
            if (isWon) {
                soundPool.play(carSoundId, 1f, 1f, 1, 0, 1f)
            } else {
                jouerSonProgression()
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Joue le son correspondant à l'étape actuelle de validation des mouvements.
     */
    private fun jouerSonProgression() {
        val currentStep = level1SensorManager.gestureCount

        if (currentStep < progressionSoundIds.size) {
            val soundId = progressionSoundIds[currentStep]
            soundPool.play(soundId, 0.7f, 0.7f, 1, 0, 1f)
        } else if (progressionSoundIds.isNotEmpty()) {
            soundPool.play(progressionSoundIds.last(), 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    /**
     * Reçoit les événements de détection du sensor manager pour fournir un feedback haptique ou visuel.
     */
    override fun onFeedbackNeeded(message: String) {
        if (!isIntroFinished) return
        if (message == "VALIDATE") {
            vibrer(100)
            Toast.makeText(this, "Son identifié", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Appelé lorsque la séquence complète de mouvements est validée.
     */
    override fun onGestureValidated(isGameComplete: Boolean, nextInstruction: String) {
        if (isGameComplete && isIntroFinished) {
            reussiteCarrefour()
        }
    }

    /**
     * Active l'état de victoire et modifie l'environnement sonore.
     */
    private fun reussiteCarrefour() {
        isWon = true
        level1SensorManager.stopListening()
        vibrer(500)
        Toast.makeText(this, "Objectif atteint : carrefour localisé", Toast.LENGTH_LONG).show()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseHelper.getInstance()
                .saveLevelProgression(user.uid, "Cécilia (cécité totale)", 2)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            goToLevel2()
        }, 10000)
    }

    /**
    Passer au niveau 2
     */
    private fun goToLevel2() {
        // Vérifier si l'activité n'est pas déjà fermée
        if (!isFinishing) {
            val intent = Intent(this, CeciliaLevel2Activity::class.java)
            startActivity(intent)
            finish() // Ferme le niveau 1 pour libérer la mémoire
        }
    }

    /**
     * Déclenche une vibration unique sur l'appareil.
     */
    private fun vibrer(duree: Long) {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duree, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(duree)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isIntroFinished && !isWon) level1SensorManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        level1SensorManager.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
        soundIntroVoice?.release()
    }
}