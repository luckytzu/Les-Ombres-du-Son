package fr.upjv.lesombresduson;

import android.content.Intent;
import android.media.MediaPlayer; // 1. Importez MediaPlayer
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CeciliaGameActivity extends AppCompatActivity {
    private Button btnBack;
    private MediaPlayer mediaPlayer; // 2. Déclarez une variable MediaPlayer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay_cecilia);

        btnBack = findViewById(R.id.button_back);

        // 3. Initialisez et démarrez la lecture audio
        mediaPlayer = MediaPlayer.create(this, R.raw.cecilia_intro);
        mediaPlayer.setLooping(false); // Pour ne pas répéter
        mediaPlayer.start(); // Démarre la lecture

        btnBack.setOnClickListener(v -> {
            // Redirection vers la page de selection personnage
            Intent intent = new Intent(CeciliaGameActivity.this, StartChoiseCharacter.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // supprime l'historique
            startActivity(intent);
            finish();
        });
    }

    /**
     * S'assurer que le MediaPlayer est libéré lorsque l'activité est détruite.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}