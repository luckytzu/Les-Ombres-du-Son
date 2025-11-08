package fr.upjv.lesombresduson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

// Imports nécessaires pour la gestion des permissions
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import fr.upjv.lesombresduson.SettingsConstants;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    // Codes pour identifier les requêtes de permission
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int VIBRATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPrefs = getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE);

        loadSettings();
        setupListeners();

        MaterialButton buttonBackHome = findViewById(R.id.button_back_home);
        buttonBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Charge les paramètres enregistrés à partir des préférences partagées (SharedPreferences)
     * et met à jour les éléments de l’interface utilisateur en conséquence.
     */
    private void loadSettings() {
        // --- Audio Settings ---
        ((SeekBar) findViewById(R.id.seekbar_music_volume)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_MUSIC_VOLUME, SettingsConstants.DEFAULT_MUSIC_VOLUME));
        ((SeekBar) findViewById(R.id.seekbar_sfx_volume)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_SFX_VOLUME, SettingsConstants.DEFAULT_SFX_VOLUME));
        ((SeekBar) findViewById(R.id.seekbar_voice_rate)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_VOICE_RATE, SettingsConstants.DEFAULT_VOICE_RATE));

        // --- Interaction Settings ---
        ((SeekBar) findViewById(R.id.seekbar_motion_sensitivity)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_MOTION_SENSITIVITY, SettingsConstants.DEFAULT_MOTION_SENSITIVITY));
        ((SwitchMaterial) findViewById(R.id.switch_vibration)).setChecked(sharedPrefs.getBoolean(
                SettingsConstants.KEY_VIBRATION_ENABLED, SettingsConstants.DEFAULT_VIBRATION_ENABLED));
        ((SwitchMaterial) findViewById(R.id.switch_camera_usage)).setChecked(sharedPrefs.getBoolean(
                SettingsConstants.KEY_CAMERA_USAGE_ENABLED, SettingsConstants.DEFAULT_CAMERA_USAGE_ENABLED));
    }

    /**
     * Configure les listeners pour les SeekBars et les interrupteurs afin d’enregistrer la nouvelle valeur et de demander les autorisations.
     */
    private void setupListeners() {
        final SharedPreferences.Editor editor = sharedPrefs.edit();

        OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                String key;
                int id = seekBar.getId();

                if (id == R.id.seekbar_music_volume) {
                    key = SettingsConstants.KEY_MUSIC_VOLUME;
                } else if (id == R.id.seekbar_sfx_volume) {
                    key = SettingsConstants.KEY_SFX_VOLUME;
                } else if (id == R.id.seekbar_voice_rate) {
                    key = SettingsConstants.KEY_VOICE_RATE;
                } else if (id == R.id.seekbar_motion_sensitivity) {
                    key = SettingsConstants.KEY_MOTION_SENSITIVITY;
                } else {
                    return;
                }
                editor.putInt(key, progress).apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* Not used */ }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* Not used */ }
        };

        // --- Appliquer le listener à toutes les SeekBars ---
        ((SeekBar) findViewById(R.id.seekbar_music_volume)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_sfx_volume)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_voice_rate)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_motion_sensitivity)).setOnSeekBarChangeListener(seekBarListener);

        // INTERRUPTEUR DE VIBRATION / HAPTIQUE (Autorisation requise obligatoire)
        ((SwitchMaterial) findViewById(R.id.switch_vibration)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                    // Si la permission n'est pas accordée, la demander
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.VIBRATE},
                            VIBRATION_PERMISSION_CODE);
                } else {
                    // Si accordée, sauvegarder l'activation
                    editor.putBoolean(SettingsConstants.KEY_VIBRATION_ENABLED, true).apply();
                }
            } else {
                // Si désactivé, sauvegarder
                editor.putBoolean(SettingsConstants.KEY_VIBRATION_ENABLED, false).apply();
            }
        });

        // CAMERA USAGE SWITCH (L’autorisation est obligatoire.)
        ((SwitchMaterial) findViewById(R.id.switch_camera_usage)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Si la permission n'est pas accordée, la demander
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_CODE);
                } else {
                    // Si accordée, sauvegarder l'activation
                    editor.putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, true).apply();
                }
            } else {
                // Si désactivé, sauvegarder
                editor.putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, false).apply();
            }
        });
    }

    /**
     * Gère la réponse à la demande d’autorisation.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        SwitchMaterial switchVibration = findViewById(R.id.switch_vibration);
        SwitchMaterial switchCamera = findViewById(R.id.switch_camera_usage);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission accordée
            if (requestCode == CAMERA_PERMISSION_CODE) {
                editor.putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, true).apply();
            } else if (requestCode == VIBRATION_PERMISSION_CODE) {
                editor.putBoolean(SettingsConstants.KEY_VIBRATION_ENABLED, true).apply();
            }
        } else {
            // Permission refusée
            if (requestCode == CAMERA_PERMISSION_CODE) {
                // Remettre le switch à 'false' (désactivé) dans l'UI et SharedPreferences
                switchCamera.setChecked(false);
                editor.putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, false).apply();
            } else if (requestCode == VIBRATION_PERMISSION_CODE) {
                // Remettre le switch à 'false' (désactivé) dans l'UI et SharedPreferences
                switchVibration.setChecked(false);
                editor.putBoolean(SettingsConstants.KEY_VIBRATION_ENABLED, false).apply();
            }
        }
    }
}