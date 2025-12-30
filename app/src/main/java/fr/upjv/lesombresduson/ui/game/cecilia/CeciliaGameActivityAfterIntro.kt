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
import fr.upjv.lesombresduson.R
import fr.upjv.lesombresduson.manager.sensor.Level1SensorManager
import fr.upjv.lesombresduson.ui.StartChoiseCharacter

class CeciliaGameActivityAfterIntro : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var vibrator: Vibrator
    private lateinit var level1SensorManager: Level1SensorManager

    // Audio Narratif
    private var soundIntroVoice: MediaPlayer? = null
    private var isIntroFinished = false

    // État du jeu
    private var isWon = false // Nouveau : Flag pour savoir si le combo est réussi

    // Audio Gameplay (SoundPool pour la réactivité des clics)
    private lateinit var soundPool: SoundPool
    private val randomSoundIds = mutableListOf<Int>()
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

    private fun initAudioEngine() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10) // Augmenté pour gérer plusieurs sons
            .setAudioAttributes(audioAttributes)
            .build()

        // Chargement des sons aléatoires
        val resIds = listOf(R.raw.son1, R.raw.son2, R.raw.son3, R.raw.son4, R.raw.son5)
        resIds.forEach { id -> randomSoundIds.add(soundPool.load(this, id, 1)) }

        // Chargement du son de voiture dans le SoundPool pour un déclenchement instantané au clic
        carSoundId = soundPool.load(this, R.raw.ambiance_carrefour, 1)
    }

    private fun lancerVoixOff() {
        soundIntroVoice = MediaPlayer.create(this, R.raw.voix_off_niveau1)
        soundIntroVoice?.setOnCompletionListener {
            isIntroFinished = true
            level1SensorManager.startListening()
            vibrer(200)
            Toast.makeText(this, "À vous de jouer : trouvez le carrefour.", Toast.LENGTH_SHORT).show()
            it.release()
            soundIntroVoice = null
        }
        soundIntroVoice?.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isIntroFinished && event.action == MotionEvent.ACTION_DOWN) {
            // LOGIQUE DE TAP :
            if (isWon) {
                // Si gagné : on entend la voiture
                soundPool.play(carSoundId, 1f, 1f, 1, 0, 1f)
            } else {
                // Si pas encore gagné : sons aléatoires (canne, vent, etc.)
                jouerSonAleatoire()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun jouerSonAleatoire() {
        if (randomSoundIds.isNotEmpty()) {
            val soundId = randomSoundIds.random()
            soundPool.play(soundId, 0.6f, 0.6f, 1, 0, 1f)
        }
    }

    fun onFeedbackNeeded(message: String) {
        if (!isIntroFinished) return
        if (message == "VALIDATE") {
            vibrer(100)
        } else {
            // Pour éviter que les Toasts ne s'accumulent
            // Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onGestureValidated(isGameComplete: Boolean, nextInstruction: String) {
        if (isGameComplete && isIntroFinished) {
            reussiteCarrefour()
        }
    }

    private fun reussiteCarrefour() {
        isWon = true // On active le mode "Voitures au clic"
        level1SensorManager.stopListening() // On arrête de surveiller les mouvements
        vibrer(500)
        Toast.makeText(this, "C'est ça ! Touche l'écran pour localiser les voitures.", Toast.LENGTH_LONG).show()
    }

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
        if (isIntroFinished && !isWon) {
            level1SensorManager.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        level1SensorManager.stopListening()
        soundIntroVoice?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundIntroVoice?.release()
        soundPool.release()
    }
}