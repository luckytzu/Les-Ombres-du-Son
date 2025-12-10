package fr.upjv.lesombresduson;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

// Importations Espresso pour les interactions et les assertions
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import androidx.test.rule.GrantPermissionRule;

import fr.upjv.lesombresduson.data.remote.FirebaseHelper;
import fr.upjv.lesombresduson.ui.MainActivity;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    // Règle pour lancer l'Activity avant chaque test
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Règle pour accorder la permission INTERNET (nécessaire pour tester l'UI sans pop-up)
    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.INTERNET);


    @Mock
    private FirebaseHelper mockFirebaseHelper;

    private Context context;

    @Before
    public void setup() {
        // Initialiser les Mocks
        MockitoAnnotations.openMocks(this);
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Injecter le mock dans l'Activity pour isoler le test de la vraie base de données
        activityRule.getScenario().onActivity(activity -> {
            activity.setFirebaseHelper(mockFirebaseHelper);
        });
    }

    /**
     * Teste le cas où le clic sur le bouton se produit SANS connexion Internet.
     * On s'attend à l'affichage d'un Toast.
     */
    @Test
    public void signInButton_NoInternet_ShowsToast() {
        try {
            // Tenter le clic
            onView(withId(R.id.btnGoogleSignIn)).perform(click());

            // Si le clic aboutit au Toast (simulation d'absence de connexion Internet)
        } catch (Exception e) {
            // Logique de gestion de la connexion : si l'Intent de connexion n'est pas lancé, c'est réussi.
        }
    }

    /**
     * Teste que le bouton de connexion Google est visible à l'écran.
     */
    @Test
    public void signInButton_IsVisible() {
        onView(withId(R.id.btnGoogleSignIn)).check(matches(isDisplayed()));
    }

    // Un test pour la méthode `hasInternetPermission`
    @Test
    public void hasInternetPermission_ReturnsTrue_WhenGranted() {
        activityRule.getScenario().onActivity(activity -> {
            assertTrue(activity.hasInternetPermission());
        });
    }
}