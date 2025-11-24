package fr.upjv.lesombresduson;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    private Button btnLogout;
    private Button btnSetting;
    private Button btnStart;

    // Définition des permissions requises
    private static final String RECORD_AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO;
    private static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;
    private static final String[] REQUIRED_PERMISSIONS = {RECORD_AUDIO_PERMISSION, CAMERA_PERMISSION};

    // Lanceur d'activité pour gérer les résultats des demandes de permissions
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnLogout = findViewById(R.id.button_logout);
        btnSetting = findViewById(R.id.button_setting);
        btnStart = findViewById(R.id.button_start);

        // Initialisation du lanceur de permissions
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // Vérifie si toutes les permissions requises sont accordées
                    boolean allGranted = true;
                    for (String permission : REQUIRED_PERMISSIONS) {
                        if (permissions.get(permission) == null || !permissions.get(permission)) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        // Toutes les permissions sont accordées, lancer l'activité de choix
                        launchStartChoiseCharacterActivity();
                    } else {
                        // Au moins une permission a été refusée, rediriger vers les paramètres
                        Toast.makeText(this, "Permissions requises refusées. Veuillez les accorder dans les paramètres de l'application.", Toast.LENGTH_LONG).show();
                        launchSettingsActivity();
                    }
                }
        );

        // Logique du bouton "Déconnexion"
        btnLogout.setOnClickListener(v -> {
            // Déconnexion Firebase
            FirebaseAuth.getInstance().signOut();

            // Redirection vers la page de login
            Intent intent = new Intent(Home.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // supprime l'historique
            startActivity(intent);
            finish();
        });

        // Logique du bouton "Paramètres"
        btnSetting.setOnClickListener(v -> {
            launchSettingsActivity();
        });

        // Logique du bouton "Démarrer"
        btnStart.setOnClickListener(v -> {
            checkAndRequestPermissions();
        });
    }

    /**
     * Vérifie si les permissions sont déjà accordées. Sinon, les demande.
     */
    private void checkAndRequestPermissions() {
        boolean audioGranted = ContextCompat.checkSelfPermission(this, RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        boolean cameraGranted = ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;

        if (audioGranted && cameraGranted) {
            // Toutes les permissions sont déjà accordées
            launchStartChoiseCharacterActivity();
        } else {
            // Demander les permissions
            permissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    /**
     * Lance l'activité de sélection de personnage.
     */
    private void launchStartChoiseCharacterActivity() {
        Intent intent = new Intent(Home.this, StartChoiseCharacter.class);
        startActivity(intent);
    }

    /**
     * Lance l'activité des paramètres.
     */
    private void launchSettingsActivity() {
        Intent intent = new Intent(Home.this, SettingsActivity.class);
        startActivity(intent);
    }
}
