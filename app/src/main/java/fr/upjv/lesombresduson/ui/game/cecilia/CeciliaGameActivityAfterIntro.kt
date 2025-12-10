package fr.upjv.lesombresduson.ui.game.cecilia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.upjv.lesombresduson.R
import fr.upjv.lesombresduson.ui.StartChoiseCharacter

/**
 * Activité pour la suite du jeu Cécilia après que l'introduction soit complétée.
 */
class CeciliaGameActivityAfterIntro : AppCompatActivity() {

    // Déclaration de la vue avec lateinit pour une initialisation différée dans onCreate
    private lateinit var btnBack: Button

    /**
     * Initialise l'activité.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay_cecilia)

        // Initialisation de la vue
        btnBack = findViewById(R.id.button_back)

        Toast.makeText(this, "Partie continuée ! C'est ici que commence le niveau suivant pour Cécilia.", Toast.LENGTH_LONG).show()

        btnBack.setOnClickListener {
            val intent = Intent(this, StartChoiseCharacter::class.java).apply {
                // Utilisation de .apply pour configurer l'Intent
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}