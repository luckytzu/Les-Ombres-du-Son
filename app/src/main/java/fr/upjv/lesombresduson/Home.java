package fr.upjv.lesombresduson;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    private Button btnLogout;
    private Button btnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnLogout = findViewById(R.id.button_logout);
        btnSetting = findViewById(R.id.button_setting);

        btnLogout.setOnClickListener(v -> {
            // Déconnexion Firebase
            FirebaseAuth.getInstance().signOut();

            // Redirection vers la page de login
            Intent intent = new Intent(Home.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // supprime l'historique
            startActivity(intent);
            finish();
        });

        btnSetting.setOnClickListener(v -> {
            // Redirection vers la page des paramétres
            Intent intent = new Intent(Home.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
