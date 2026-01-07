package fr.upjv.lesombresduson.ui.game.cecilia.util

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fr.upjv.lesombresduson.ui.StartChoiseCharacter

open class BackGameActivity : AppCompatActivity() {

    /**
     * Méthode à appeler dans vos activités pour configurer le bouton retour
     */
    protected fun setupBackButton(button: View) {
        button.setOnClickListener {
            val intent = Intent(this, StartChoiseCharacter::class.java).apply {
                // On nettoie la pile pour ne pas empiler les activités
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}