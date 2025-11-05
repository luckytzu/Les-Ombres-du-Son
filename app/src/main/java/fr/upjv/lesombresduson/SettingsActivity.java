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

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link to the XML layout file
        setContentView(R.layout.activity_settings);

        // 1. Initialize SharedPreferences
        sharedPrefs = getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE);

        // 2. Load the saved values and set up the UI
        loadSettings();

        // 3. Set up listeners to save changes immediately
        setupListeners();

        // 4. Setup the Back button
        MaterialButton buttonBackHome = findViewById(R.id.button_back_home);
        buttonBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Closes the current activity and returns to the previous one (Home)
                finish();
            }
        });
    }

    /**
     * Loads saved settings from SharedPreferences and updates the UI elements (SeekBars, Switches).
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
     * Sets up listeners for SeekBars and Switches to save the new value immediately upon change.
     */
    private void setupListeners() {
        final SharedPreferences.Editor editor = sharedPrefs.edit();

        // Helper method to create the listener object once
        OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return; // Only save changes made by the user

                // Determine which key to use based on the SeekBar's ID
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

                // Save the new value immediately
                editor.putInt(key, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* Not used */ }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* Not used */ }
        };

        // --- Apply Listener to all SeekBars ---
        ((SeekBar) findViewById(R.id.seekbar_music_volume)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_sfx_volume)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_voice_rate)).setOnSeekBarChangeListener(seekBarListener);
        ((SeekBar) findViewById(R.id.seekbar_motion_sensitivity)).setOnSeekBarChangeListener(seekBarListener);


        // --- Setup Switches ---
        ((SwitchMaterial) findViewById(R.id.switch_vibration)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(SettingsConstants.KEY_VIBRATION_ENABLED, isChecked).apply();
        });

        ((SwitchMaterial) findViewById(R.id.switch_camera_usage)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(SettingsConstants.KEY_CAMERA_USAGE_ENABLED, isChecked).apply();
        });
    }
}