package fr.upjv.lesombresduson;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.View;
import android.widget.SeekBar;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.UiController;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
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
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static androidx.test.espresso.intent.Intents.intending;

import fr.upjv.lesombresduson.ui.Home;
import fr.upjv.lesombresduson.ui.settings.SettingsActivity;
import fr.upjv.lesombresduson.util.SettingsConstants;

/**
 * Classe de test d'intégration pour SettingsActivity.
 * Couvre l'interface utilisateur (UI), la navigation, la persistance des préférences
 * et la logique des permissions système via Espresso.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    /**
     * Règle JUnit qui lance l'activité SettingsActivity avant chaque test
     * et la ferme après. Permet d'isoler les tests.
     */
    @Rule
    public ActivityScenarioRule<SettingsActivity> activityRule =
            new ActivityScenarioRule<>(SettingsActivity.class);

    /**
     * Règle accordant automatiquement les permissions critiques avant les tests.
     * Cela simule un état où l'utilisateur a déjà tout accepté, permettant de tester
     * la logique de "désactivation" (qui doit rediriger vers les paramètres système).
     */
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    );

    /**
     * Configuration initiale avant chaque test.
     * Initialise Espresso Intents pour pouvoir surveiller et vérifier les Intent sortants.
     */
    @Before
    public void setUp() {
        Intents.init();
    }

    /**
     * Nettoyage après chaque test.
     * 1. Libère les ressources d'Intents.
     * 2. Efface les SharedPreferences pour garantir que le test suivant part d'un état propre.
     */
    @After
    public void tearDown() {
        Intents.release();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    // -------------------------------------------------------------------------
    // 1. TESTS DE NAVIGATION
    // -------------------------------------------------------------------------

    /**
     * Vérifie que le clic sur le bouton "Retour Accueil" lance bien l'activité Home.
     * Valide la navigation entre les écrans.
     */
    @Test
    public void backHomeButton_launchesHomeActivity() {
        onView(withId(R.id.button_back_home))
                .check(matches(isDisplayed()))
                .perform(click());

        intended(hasComponent(Home.class.getName()));
    }

    // -------------------------------------------------------------------------
    // 2. TESTS DE LOGIQUE SEEKBAR (PERSISTANCE)
    // -------------------------------------------------------------------------

    /**
     * Teste la logique de sauvegarde du volume musique.
     * Simule un glissement de la SeekBar et vérifie directement dans le fichier XML
     * des SharedPreferences que la valeur a bien été enregistrée.
     */
    @Test
    public void volumeMusic_changes_savedInSharedPreferences() {
        int testValue = 80;

        // Action : Modifier l'UI
        onView(withId(R.id.seekbar_music_volume))
                .perform(setProgress(testValue));

        // Vérification : Lire la donnée persistée
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE);

        int savedValue = prefs.getInt(SettingsConstants.KEY_MUSIC_VOLUME, -1);
        assertEquals("La valeur de la musique doit être sauvegardée", testValue, savedValue);
    }

    /**
     * Teste la logique de sauvegarde de la sensibilité de mouvement.
     * Vérifie que l'interaction UI se traduit par une donnée persistée correcte.
     */
    @Test
    public void sensitivity_changes_savedInSharedPreferences() {
        int testValue = 25;

        onView(withId(R.id.seekbar_motion_sensitivity))
                .perform(setProgress(testValue));

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE);

        int savedValue = prefs.getInt(SettingsConstants.KEY_MOTION_SENSITIVITY, -1);
        assertEquals("La sensibilité doit être sauvegardée", testValue, savedValue);
    }

    // -------------------------------------------------------------------------
    // 3. TESTS DES SWITCHES & INTENTS SYSTÈME
    // -------------------------------------------------------------------------

    /**
     * Teste le scénario critique de désactivation de la caméra.
     * Étant donné que l'app ne peut pas révoquer une permission elle-même,
     * ce test vérifie que l'app redirige l'utilisateur vers les Paramètres Android
     * via un Intent ACTION_APPLICATION_DETAILS_SETTINGS.
     */
    @Test
    public void switchCamera_turnOff_opensSystemSettings() {
        // 1. Préparer le "Bouchon" (Stub)
        // On dit à Espresso : "Si tu vois une demande pour aller vers les Settings,
        // intercepte-la, ne lance pas vraiment les Settings, et renvoie OK."
        Matcher<Intent> expectedIntent = hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intending(expectedIntent).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // 2. Condition initiale
        onView(withId(R.id.switch_camera_usage)).check(matches(isChecked()));

        // 3. Action : On clique pour désactiver
        onView(withId(R.id.switch_camera_usage)).perform(click());

        // 4. Vérification : On vérifie que l'intent a bien été "tiré" par l'application
        // Grâce au stub ligne 1, l'activité SettingsActivity reste ouverte (RESUMED) et le test ne plante pas.
        intended(expectedIntent);
    }

    /**
     * Teste le scénario de désactivation du microphone.
     * Vérifie non seulement l'action de l'Intent, mais aussi que l'URI de données
     * pointe bien vers le package de notre application ("package:fr.upjv...").
     */
    @Test
    public void switchMicrophone_turnOff_opensSystemSettings() {
        // 1. Préparer le "Bouchon" (Stub)
        Matcher<Intent> expectedIntent = hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intending(expectedIntent).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // 2. Condition initiale
        onView(withId(R.id.switch_microphone_usage)).check(matches(isChecked()));

        // 3. Action : Désactiver
        onView(withId(R.id.switch_microphone_usage)).perform(click());

        // 4. Vérification
        intended(expectedIntent);

        // Vérification supplémentaire sur les données de l'intent
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        intended(hasData("package:" + context.getPackageName()));
    }

    // -------------------------------------------------------------------------
    // UTILITAIRES (Custom ViewAction)
    // -------------------------------------------------------------------------

    /**
     * Action Espresso personnalisée permettant de définir programmatiquement la valeur d'une SeekBar.
     * Nécessaire car les ViewActions standard (click, typeText) ne gèrent pas le glissement précis d'une SeekBar.
     *
     * @param progress La valeur entière à définir sur la SeekBar.
     * @return Une ViewAction exécutable par Espresso.
     */
    public static ViewAction setProgress(final int progress) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(SeekBar.class);
            }

            @Override
            public String getDescription() {
                return "Set SeekBar progress to " + progress;
            }

            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }
        };
    }
}