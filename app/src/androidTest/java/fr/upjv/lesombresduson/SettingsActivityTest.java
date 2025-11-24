package fr.upjv.lesombresduson;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.Manifest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    // 1. Règle pour lancer l'activité
    @Rule
    public ActivityScenarioRule<SettingsActivity> activityRule =
            new ActivityScenarioRule<>(SettingsActivity.class);

    // 2. Initialiser la surveillance des Intents AVANT chaque test
    @Before
    public void setUp() {
        Intents.init();
    }

    // 3. Libérer la surveillance des Intents APRÈS chaque test
    @After
    public void tearDown() {
        Intents.release();
    }

    // Règle pour accorder automatiquement les permissions nécessaires avant chaque test
    // Cela nous permet de tester le cas où ContextCompat.checkSelfPermission renvoie PERMISSION_GRANTED
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE
    );

    // -------------------------------------------------------------------------
    // 1. TESTS DE NAVIGATION
    // -------------------------------------------------------------------------

    @Test
    public void backHomeButton_launchesHomeActivity() {
        // Vérifie si le bouton est présent et clique dessus
        onView(withId(R.id.button_back_home))
                .check(matches(isDisplayed()))
                .perform(click());

        // Vérifie que l'Intent lancé cible l'activité Home
        intended(hasComponent(Home.class.getName()));
    }

    // -------------------------------------------------------------------------
    // 2. TESTS DES SEEKBARS
    // -------------------------------------------------------------------------

    @Test
    public void allSeekbars_areDisplayed() {
        // Vérifie simplement que toutes les SeekBars sont visibles pour éviter un crash au démarrage
        onView(withId(R.id.seekbar_music_volume)).check(matches(isDisplayed()));
        onView(withId(R.id.seekbar_sfx_volume)).check(matches(isDisplayed()));
        onView(withId(R.id.seekbar_voice_rate)).check(matches(isDisplayed()));
        onView(withId(R.id.seekbar_motion_sensitivity)).check(matches(isDisplayed()));
    }

    // -------------------------------------------------------------------------
    // 3. TESTS DES SWITCHES (Avec Permissions Accédées)
    // -------------------------------------------------------------------------

    @Test
    public void switchVibration_click_isSavedWhenPermissionGranted() {
        // Supposons que l'état initial par défaut est OFF (isNotChecked)
        onView(withId(R.id.switch_vibration)).check(matches(isNotChecked()));

        // Clique pour ACTIVER (puisque la permission est accordée par GrantPermissionRule)
        onView(withId(R.id.switch_vibration)).perform(click());

        // Vérifie que le Switch est MAINTENANT coché
        onView(withId(R.id.switch_vibration)).check(matches(isChecked()));

        // Clique pour DÉSACTIVER
        onView(withId(R.id.switch_vibration)).perform(click());

        // Vérifie que le Switch est MAINTENANT décoché
        onView(withId(R.id.switch_vibration)).check(matches(isNotChecked()));
    }

    @Test
    public void switchCamera_click_isSavedWhenPermissionGranted() {
        // Clique pour ACTIVER (la permission est accordée)
        onView(withId(R.id.switch_camera_usage)).perform(click());
        onView(withId(R.id.switch_camera_usage)).check(matches(isChecked()));
    }

    @Test
    public void switchMicrophone_click_isSavedWhenPermissionGranted() {
        // Clique pour ACTIVER (la permission est accordée)
        onView(withId(R.id.switch_microphone_usage)).perform(click());
        onView(withId(R.id.switch_microphone_usage)).check(matches(isChecked()));
    }
}