package fr.upjv.lesombresduson;

/**
 * Contient les constantes des clés SharedPreferences utilisées dans l'application
 * pour stocker et récupérer les paramètres du jeu.
 */
public class SettingsConstants {
    public static final String PREFS_NAME = "GameSettings";

    // --- Clés audio (0-100) ---
    public static final String KEY_MUSIC_VOLUME = "music_volume";
    public static final String KEY_SFX_VOLUME = "sfx_volume";
    public static final String KEY_VOICE_RATE = "voice_rate";

    // --- Clés d’interaction ---
    public static final String KEY_MOTION_SENSITIVITY = "motion_sensitivity";
    public static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    public static final String KEY_CAMERA_USAGE_ENABLED = "camera_usage_enabled";

    // --- Valeur par défauts ---
    public static final int DEFAULT_MUSIC_VOLUME = 70;
    public static final int DEFAULT_SFX_VOLUME = 100;
    public static final int DEFAULT_VOICE_RATE = 50;
    public static final int DEFAULT_MOTION_SENSITIVITY = 50;
    public static final boolean DEFAULT_VIBRATION_ENABLED = false;
    public static final boolean DEFAULT_CAMERA_USAGE_ENABLED = false;

    // Constructeur privé pour empêcher l’instanciation de la classe utilitaire.
    private SettingsConstants() {}
}