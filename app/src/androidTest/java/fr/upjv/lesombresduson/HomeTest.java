package fr.upjv.lesombresduson;

import android.Manifest;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.upjv.lesombresduson.ui.Home;
import fr.upjv.lesombresduson.ui.MainActivity;
import fr.upjv.lesombresduson.ui.StartChoiseCharacter;
import fr.upjv.lesombresduson.ui.settings.SettingsActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasFlag;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;

/**
 * Classe de test d'intégration pour l'activité Home.
 * Vérifie l'affichage des éléments et la navigation entre les activités.
 * Note : Les appels Firebase réels sont effectués mais ignorés ici pour la stabilité du test.
 */
@RunWith(AndroidJUnit4.class)
public class HomeTest {

    /**
     * Règle accordant automatiquement les permissions MICRO et CAMERA.
     * Indispensable pour tester le bouton "Démarrer" sans être bloqué par la demande système.
     */
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    );

    /**
     * Initialisation d'Espresso Intents pour surveiller et vérifier la navigation.
     */
    @Before
    public void setUp() {
        Intents.init();
    }

    /**
     * Libération des ressources Intents après chaque test.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Vérifie que les éléments principaux de l'interface (boutons) sont bien affichés.
     */
    @Test
    public void uiElements_areDisplayed() {
        try (ActivityScenario<Home> scenario = ActivityScenario.launch(Home.class)) {
            onView(withId(R.id.button_start)).check(matches(isDisplayed()));
            onView(withId(R.id.button_setting)).check(matches(isDisplayed()));
            onView(withId(R.id.button_logout)).check(matches(isDisplayed()));
        }
    }

    /**
     * Vérifie que le clic sur le bouton "Paramètres" redirige vers SettingsActivity.
     */
    @Test
    public void clickSettings_opensSettingsActivity() {
        try (ActivityScenario<Home> scenario = ActivityScenario.launch(Home.class)) {
            onView(withId(R.id.button_setting)).perform(click());

            intended(hasComponent(SettingsActivity.class.getName()));
        }
    }

    /**
     * Vérifie que le clic sur "Démarrer" lance le choix du personnage.
     * Les permissions étant accordées via la Rule, le jeu doit se lancer directement.
     */
    @Test
    public void clickStart_withPermissionsGranted_opensCharacterChoice() {
        try (ActivityScenario<Home> scenario = ActivityScenario.launch(Home.class)) {
            onView(withId(R.id.button_start)).perform(click());

            intended(hasComponent(StartChoiseCharacter.class.getName()));
        }
    }

    /**
     * Vérifie la navigation de déconnexion.
     * Le test s'assure que l'utilisateur est renvoyé vers MainActivity
     * et que l'historique de navigation est nettoyé (Flags NEW_TASK et CLEAR_TASK).
     */
    @Test
    public void clickLogout_redirectsToMainWithClearFlags() {
        try (ActivityScenario<Home> scenario = ActivityScenario.launch(Home.class)) {
            onView(withId(R.id.button_logout)).perform(click());

            // On vérifie uniquement la navigation (l'appel interne à signOut() n'est pas vérifié ici pour éviter le mock statique)
            intended(allOf(
                    hasComponent(MainActivity.class.getName()),
                    hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK),
                    hasFlag(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ));
        }
    }
}