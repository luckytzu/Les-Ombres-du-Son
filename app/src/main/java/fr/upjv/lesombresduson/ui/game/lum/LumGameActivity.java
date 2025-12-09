package fr.upjv.lesombresduson.ui.game.lum;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import fr.upjv.lesombresduson.R;
import fr.upjv.lesombresduson.ui.StartChoiseCharacter;

public class LumGameActivity extends AppCompatActivity {
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay_lum);

        btnBack = findViewById(R.id.button_back);

        btnBack.setOnClickListener(v -> {
            // Redirection vers la page de selection personnage
            Intent intent = new Intent(LumGameActivity.this, StartChoiseCharacter.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // supprime l'historique
            startActivity(intent);
            finish();
        });
    }
}
