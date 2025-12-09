package fr.upjv.lesombresduson.ui.game.cecilia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.upjv.lesombresduson.R;
import fr.upjv.lesombresduson.ui.StartChoiseCharacter;

/**
 * Activité pour la suite du jeu Cécilia après que l'introduction soit complétée.
 */
public class CeciliaGameActivityAfterIntro extends AppCompatActivity {
    private Button btnBack;

    /**
     * Initialise l'activité.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay_cecilia);

        btnBack = findViewById(R.id.button_back);

        Toast.makeText(this, "Partie continuée ! C'est ici que commence le niveau suivant pour Cécilia.", Toast.LENGTH_LONG).show();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(CeciliaGameActivityAfterIntro.this, StartChoiseCharacter.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}