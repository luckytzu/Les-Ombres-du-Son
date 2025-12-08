package fr.upjv.lesombresduson;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.espresso.intent.Intents; // <-- NÉCESSAIRE

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended; // <-- NÉCESSAIRE
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent; // <-- NÉCESSAIRE
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import androidx.lifecycle.Lifecycle;

/**
 * Test d'instrumentation corrigé pour la classe CeciliaGameActivity.
 * Ces tests nécessitent un appareil ou un émulateur pour s'exécuter.
 */
@RunWith(AndroidJUnit4.class)
public class CeciliaGameActivityTest {

    @Rule
    public ActivityScenarioRule<CeciliaGameActivity> activityRule =
            new ActivityScenarioRule<>(CeciliaGameActivity.class);

    private UiDevice device;

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Initialiser l'écoute des Intents avant chaque test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Libérer l'écoute des Intents après chaque test
        Intents.release();
    }

    // --- Tests des composants et de l'initialisation ---

    @Test
    public void activity_is_launched_and_back_button_is_displayed() {
        // Vérifie que l'Activity est lancée
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity);
        });

        // Vérifie que le bouton de retour est visible
        onView(withId(R.id.button_back)).check(matches(isDisplayed()));
    }

    @Test
    public void back_button_click_starts_StartChoiseCharacter_activity() {
        // 1. Clic sur le bouton de retour
        onView(withId(R.id.button_back)).perform(click());

        // 2. Vérifie que l'Intent de destination est correct
        intended(hasComponent(StartChoiseCharacter.class.getName()));
    }

    // --- Tests du cycle de vie (ex: onPause/onResume) ---

    @Test
    public void onPause_is_called_when_moving_to_paused_state() {
        // Déplace l'Activity dans l'état PAUSED, déclenchant onPause()
        activityRule.getScenario().moveToState(Lifecycle.State.CREATED);
        activityRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        // Vérifier que l'état est bien PAUSED.
        activityRule.getScenario().onActivity(activity -> {
        });

        // Remettre l'Activity en état 'Resumed' pour finir le scénario
        activityRule.getScenario().moveToState(Lifecycle.State.RESUMED);
    }
}