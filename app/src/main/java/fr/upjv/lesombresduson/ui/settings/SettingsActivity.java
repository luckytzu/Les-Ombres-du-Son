package fr.upjv.lesombresduson.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import fr.upjv.lesombresduson.R;
import fr.upjv.lesombresduson.ui.Home;
import fr.upjv.lesombresduson.util.SettingsConstants;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;
    private SwitchMaterial switchCamera;
    private SwitchMaterial switchMicrophone;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int MICROPHONE_PERMISSION_CODE = 102;

    /**
     * Initialise l'activité, lie les vues, charge les préférences et configure la navigation.
     * @param savedInstanceState État sauvegardé de l'instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPrefs = getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE);

        switchCamera = findViewById(R.id.switch_camera_usage);
        switchMicrophone = findViewById(R.id.switch_microphone_usage);

        loadSettings();
        setupListeners();

        MaterialButton buttonBackHome = findViewById(R.id.button_back_home);
        buttonBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, Home.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Appelé à chaque retour sur l'activité (y compris après un passage dans les paramètres système).
     * Force la synchronisation visuelle des switchs avec l'état réel des permissions Android.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateSwitchesFromSystemState();
    }

    /**
     * Vérifie les permissions système réelles et met à jour l'état des switchs UI.
     * Désactive temporairement les listeners pour éviter de déclencher des actions inutiles lors de la mise à jour visuelle.
     */
    private void updateSwitchesFromSystemState() {
        boolean isCameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean isMicGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        switchCamera.setOnCheckedChangeListener(null);
        switchMicrophone.setOnCheckedChangeListener(null);

        switchCamera.setChecked(isCameraGranted);
        switchMicrophone.setChecked(isMicGranted);

        sharedPrefs.edit().putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, isCameraGranted).apply();
        sharedPrefs.edit().putBoolean(SettingsConstants.KEY_MICROPHONE_USAGE_ENABLED, isMicGranted).apply();

        setupSwitchListeners();
    }

    /**
     * Charge les valeurs de volume et de sensibilité depuis les SharedPreferences
     * et met à jour la position des barres de défilement (SeekBars).
     */
    private void loadSettings() {
        ((SeekBar) findViewById(R.id.seekbar_music_volume)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_MUSIC_VOLUME, SettingsConstants.DEFAULT_MUSIC_VOLUME));
        ((SeekBar) findViewById(R.id.seekbar_sfx_volume)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_SFX_VOLUME, SettingsConstants.DEFAULT_SFX_VOLUME));
        ((SeekBar) findViewById(R.id.seekbar_voice_rate)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_VOICE_RATE, SettingsConstants.DEFAULT_VOICE_RATE));
        ((SeekBar) findViewById(R.id.seekbar_motion_sensitivity)).setProgress(sharedPrefs.getInt(
                SettingsConstants.KEY_MOTION_SENSITIVITY, SettingsConstants.DEFAULT_MOTION_SENSITIVITY));
    }

    /**
     * Initialise les écouteurs sur les SeekBars pour sauvegarder automatiquement les changements.
     * Appelle également la configuration des switchs.
     */
    private void setupListeners() {
        final SharedPreferences.Editor editor = sharedPrefs.edit();

        OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String key;
                int id = seekBar.getId();

                if (id == R.id.seekbar_music_volume) key = SettingsConstants.KEY_MUSIC_VOLUME;
                else if (id == R.id.seekbar_sfx_volume) key = SettingsConstants.KEY_SFX_VOLUME;
                else if (id == R.id.seekbar_voice_rate) key = SettingsConstants.KEY_VOICE_RATE;
                else if (id == R.id.seekbar_motion_sensitivity) key = SettingsConstants.KEY_MOTION_SENSITIVITY;
                else return;

                editor.putInt(key, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        };

        ((SeekBar) findViewById(R.id.seekbar_music_volume)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_sfx_volume)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_voice_rate)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_motion_sensitivity)).setOnSeekBarChangeListener(seekBarListener);

        setupSwitchListeners();
    }

    /**
     * Configure la logique d'interaction des switchs Caméra et Micro.
     * - Si l'utilisateur active : Demande la permission système.
     * - Si l'utilisateur désactive : Redirige vers les paramètres du téléphone (révocation impossible par code).
     */
    private void setupSwitchListeners() {
        final SharedPreferences.Editor editor = sharedPrefs.edit();

        switchCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    buttonView.setChecked(false);
                } else {
                    editor.putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, true).apply();
                }
            } else {
                Toast.makeText(this, "Modifiez les autorisations dans les paramètres pour désactiver.", Toast.LENGTH_LONG).show();
                openAppSettings();
                buttonView.setChecked(true);
            }
        });

        switchMicrophone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
                    buttonView.setChecked(false);
                } else {
                    editor.putBoolean(SettingsConstants.KEY_MICROPHONE_USAGE_ENABLED, true).apply();
                }
            } else {
                Toast.makeText(this, "Modifiez les autorisations dans les paramètres pour désactiver.", Toast.LENGTH_LONG).show();
                openAppSettings();
                buttonView.setChecked(true);
            }
        });
    }

    /**
     * Ouvre l'écran des paramètres de l'application dans le système Android via un Intent.
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Gère la réponse de l'utilisateur à la boîte de dialogue système de demande de permission.
     * Met à jour les SharedPreferences et l'UI si la permission est accordée.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                editor.putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, true).apply();
                switchCamera.setChecked(true);
            } else if (requestCode == MICROPHONE_PERMISSION_CODE) {
                editor.putBoolean(SettingsConstants.KEY_MICROPHONE_USAGE_ENABLED, true).apply();
                switchMicrophone.setChecked(true);
            }
        }
    }
}