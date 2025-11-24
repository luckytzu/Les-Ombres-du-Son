package fr.upjv.lesombresduson;

import androidx.test.espresso.intent.Intents; // Importez Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After; // Pour libérer Intents
import org.junit.Before; // Pour initialiser Intents
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class HomeTest {

    // Règle 1 : Lance l'activité Home
    @Rule
    public ActivityScenarioRule<Home> activityRule = new ActivityScenarioRule<>(Home.class);

    // Initialise la surveillance des Intents avant chaque test
    @Before
    public void setUp() {
        Intents.init();
    }

    // Libère la surveillance des Intents après chaque test
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Teste si les trois boutons principaux sont affichés.
     */
    @Test
    public void checkAllButtonsAreDisplayed() {
        onView(withId(R.id.button_logout)).check(matches(isDisplayed()));
        onView(withId(R.id.button_setting)).check(matches(isDisplayed()));
        onView(withId(R.id.button_start)).check(matches(isDisplayed()));
    }

    /**
     * Teste que cliquer sur le bouton "Setting" lance SettingsActivity.
     */
    @Test
    public void settingButtonNavigatesToSettingsActivity() {
        onView(withId(R.id.button_setting)).perform(click());
        // Vérifie que l'Intent lancé est bien celui vers SettingsActivity
        intended(hasComponent(SettingsActivity.class.getName()));
    }

    /**
     * Teste que cliquer sur le bouton "Start" lance StartChoiseCharacter.
     */
    @Test
    public void startButtonNavigatesToStartChoiseCharacter() {
        onView(withId(R.id.button_start)).perform(click());
        // Vérifie que l'Intent lancé est bien celui vers StartChoiseCharacter
        intended(hasComponent(StartChoiseCharacter.class.getName()));
    }
}