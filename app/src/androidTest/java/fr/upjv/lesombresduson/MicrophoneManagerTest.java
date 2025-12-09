package fr.upjv.lesombresduson;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fr.upjv.lesombresduson.manager.input.GestureListener;
import fr.upjv.lesombresduson.manager.sensor.MicrophoneManager;

/**
 * Test d'Instrumentation pour la classe MicrophoneManager.
 */
@RunWith(AndroidJUnit4.class)
public class MicrophoneManagerTest {

    private Context context;
    private ObserverGestureListener observerListener;
    private MicrophoneManager manager;

    // ---------------------------------------------------------------------------------------------
    // CLASSE FACTICE POUR L'OBSERVATION
    // ---------------------------------------------------------------------------------------------
    /**
     * Cette version du listener utilise un CountDownLatch pour synchroniser les tests
     * avec les appels asynchrones de MicrophoneManager.
     */
    class ObserverGestureListener implements GestureListener {
        public boolean targetFoundCalled = false;
        public boolean dogFoundCalled = false;
        public String lastFeedback = null;
        public CountDownLatch feedbackLatch = new CountDownLatch(1);

        @Override public void onInstructionReady(String instruction) {}
        @Override public void onGestureValidated(boolean complete, String nextInstruction) {}

        @Override
        public void onFeedbackNeeded(String message) {
            lastFeedback = message;
            // Libère le latch pour signaler au test que le feedback a été reçu
            feedbackLatch.countDown();
        }

        @Override public void onTargetFound() { targetFoundCalled = true; }
        @Override public void onDogFound() { dogFoundCalled = true; }

        /**
         * Réinitialise le latch pour le prochain test ou la prochaine action.
         */
        public void resetLatch() {
            feedbackLatch = new CountDownLatch(1);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // SETUP
    // ---------------------------------------------------------------------------------------------
    @Before
    public void setup() throws Throwable {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Lancement de l'initialisation sur le thread principal pour éviter l'erreur de Handler/Looper
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            observerListener = new ObserverGestureListener();
            manager = new MicrophoneManager(observerListener);
        });
    }

    // ---------------------------------------------------------------------------------------------
    // TESTS DU CYCLE DE VIE ET D'INITIALISATION
    // ---------------------------------------------------------------------------------------------

    @Test
    public void initialization_succeeds_and_manager_is_created() {
        assertNotNull(manager);
    }


    // ---------------------------------------------------------------------------------------------
    // TESTS DE LOGIQUE
    // ---------------------------------------------------------------------------------------------

    @Test
    public void onDogFound_isNotCalled_withoutSufficientAmplitude() throws Throwable {
        // Teste que sans entrée audio forte, onDogFound n'est pas appelé.
        // Ce test ne peut pas simuler une forte amplitude, il vérifie seulement le cas "silencieux".

        // Simuler le démarrage
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            manager.startListening();
        });

        // Attendre un peu pour que le gestionnaire puisse écouter l'amplitude.
        Thread.sleep(500);

        // Vérification de l'état : onDogFound ne doit pas avoir été appelé.
        assertFalse("onDogFound ne devrait pas être appelé sans un souffle fort.", observerListener.dogFoundCalled);

        // Arrêter pour nettoyer
        manager.stopListening();
    }
}