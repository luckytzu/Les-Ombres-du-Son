package fr.upjv.lesombresduson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SettingsConstantsTest {

    @Test
    public void defaultValues_areConsistent() {
        assertEquals(70, SettingsConstants.DEFAULT_MUSIC_VOLUME);
        assertEquals(100, SettingsConstants.DEFAULT_SFX_VOLUME);
        assertEquals(50, SettingsConstants.DEFAULT_VOICE_RATE);
        assertEquals(50, SettingsConstants.DEFAULT_MOTION_SENSITIVITY);
        assertFalse(SettingsConstants.DEFAULT_VIBRATION_ENABLED);
        assertFalse(SettingsConstants.DEFAULT_CAMERA_USAGE_ENABLED);
        assertFalse(SettingsConstants.DEFAULT_MICROPHONE_USAGE_ENABLED);
    }
}
