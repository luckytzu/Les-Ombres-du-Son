package fr.upjv.lesombresduson;

/**

 * Contient les constantes des clés SharedPreferences utilisées dans l'application
 * pour stocker et récupérer les paramètres du jeu.
 */

public class SettingsConstants {
    // SharedPreferences file name
    public static final String PREFS_NAME = "GameSettings";

    // --- Audio Keys (0-100) ---
    public static final String KEY_MUSIC_VOLUME = "music_volume";
    public static final String KEY_SFX_VOLUME = "sfx_volume";
    public static final String KEY_VOICE_RATE = "voice_rate";

    // --- Interaction Keys ---
    public static final String KEY_MOTION_SENSITIVITY = "motion_sensitivity";
    public static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    public static final String KEY_CAMERA_USAGE_ENABLED = "camera_usage_enabled";

    // --- Default Values ---
    public static final int DEFAULT_MUSIC_VOLUME = 70;
    public static final int DEFAULT_SFX_VOLUME = 100;
    public static final int DEFAULT_VOICE_RATE = 50;
    public static final int DEFAULT_MOTION_SENSITIVITY = 50;
    public static final boolean DEFAULT_VIBRATION_ENABLED = false;
    public static final boolean DEFAULT_CAMERA_USAGE_ENABLED = false;

    // Private constructor to prevent instantiation of a utility class
    private SettingsConstants() {}
}