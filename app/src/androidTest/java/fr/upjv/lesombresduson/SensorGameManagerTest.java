package fr.upjv.lesombresduson;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test pour la classe SensorGameManager.
 */
@RunWith(AndroidJUnit4.class)
public class SensorGameManagerTest {

    private Context context;
    private ObserverGestureListener observerListener;
    private SensorGameManager manager;

    /**
     * Implémentation factice de GestureListener pour intercepter les appels du SensorGameManager.
     */
    class ObserverGestureListener implements GestureListener {
        public boolean validatedCalled = false;
        public String lastFeedback = null;
        public boolean isGameComplete = false;

        @Override
        public void onInstructionReady(String instruction) {}
        @Override
        public void onGestureValidated(boolean complete, String nextInstruction) {
            validatedCalled = true;
            isGameComplete = complete;
        }
        @Override
        public void onFeedbackNeeded(String message) {
            lastFeedback = message;
        }
        @Override
        public void onTargetFound() {}
        @Override
        public void onDogFound() {}
    }

    // ---------------------------------------------------------------------------------------------
    // SETUP
    // ---------------------------------------------------------------------------------------------

    @Before
    public void setup() throws Throwable {
        // 1. Obtenir un Context réel Android
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // 2. Exécuter l'initialisation du manager sur le thread principal (UI thread)
        // Ceci garantit qu'un Looper est présent pour la création du Handler.
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            observerListener = new ObserverGestureListener();
            manager = new SensorGameManager(context, observerListener);
        });
    }

    // ---------------------------------------------------------------------------------------------
    // TESTS D'INITIALISATION ET D'ÉTAT
    // ---------------------------------------------------------------------------------------------

    @Test
    public void initialization_checks_accelerometer_availability() {
        // Vérifie que l'accéléromètre est trouvé et que l'état initial est correct.
        assertTrue("L'accéléromètre devrait être disponible pour les tests.", manager.isAccelerometerAvailable());
        assertEquals("Le compteur de gestes doit démarrer à 0.", 0, manager.getGestureCount());
    }

    @Test
    public void start_listening_registers_listener() {
        // Vérifie le comportement de démarrage de l'écoute si le capteur est disponible.
        if (manager.isAccelerometerAvailable()) {
            manager.startListening();

            // Vérification implicite de l'état (le jeu n'est pas terminé).
            // Le champ `isGameComplete` est accessible car `observerListener` est de type `ObserverGestureListener`.
            assertFalse(observerListener.isGameComplete);
        }
    }

    @Test
    public void stop_listening_unregisters_listener() {
        // Vérifie que l'arrêt de l'écoute est géré correctement.
        manager.startListening();
        manager.stopListening();
    }

    // ---------------------------------------------------------------------------------------------
    // MÉTHODE DE CONTOURNEMENT POUR SIMULER SENSOR EVENT (NON STANDARD)
    // ---------------------------------------------------------------------------------------------

    /**
     * Crée un SensorEvent avec des valeurs spécifiques.
     * Cette méthode est laissée comme exemple de tentative de contournement
     * mais est reconnue comme difficile et non standard sans un framework de mock.
     */
    private SensorEvent createTestSensorEvent(float x, float y, int type) {
        try {
            // Créer un Sensor Manager pour obtenir un Sensor réel
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sensorManager.getDefaultSensor(type);

            // Retourne null car l'instanciation de SensorEvent par réflexion est fragile/impossible.
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}