package fr.upjv.lesombresduson;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fr.upjv.lesombresduson.manager.input.GestureListener;
import fr.upjv.lesombresduson.manager.input.TouchNavigationManager;

/**
 * Test pour la classe TouchNavigationManager.
 */
@RunWith(AndroidJUnit4.class)
public class TouchNavigationManagerTest {

    private Context context;
    private MockGestureListener mockListener;
    private TouchNavigationManager manager;
    private static final int SCREEN_WIDTH = 1080;
    private static final int SCREEN_HEIGHT = 1920;

    private class MockGestureListener implements GestureListener {
        public boolean targetFoundCalled = false;
        public String lastFeedback = null;

        @Override
        public void onInstructionReady(String instruction) {}
        @Override
        public void onGestureValidated(boolean isGameComplete, String nextInstruction) {}
        @Override
        public void onFeedbackNeeded(String message) {
            lastFeedback = message;
        }
        @Override
        public void onTargetFound() {
            targetFoundCalled = true;
        }
        @Override
        public void onDogFound() {}
    }


    @Before
    public void setup() {
        // Obtenir le contexte réel de l'application de test Android
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mockListener = new MockGestureListener();

        // Le constructeur est appelé. Les dépendances Android (Vibrator, MediaPlayer)
        manager = new TouchNavigationManager(context, mockListener, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /**
     * Crée un MotionEvent simulé pour tester la méthode onTouch.
     */
    private MotionEvent createMotionEvent(float x, float y, int action) {
        long downTime = 0;
        long eventTime = 0;
        int metaState = 0;
        float pressure = 1.0f;
        float size = 1.0f;
        int deviceId = 0;
        int edgeFlags = 0;
        int source = 0;
        int flags = 0;

        return MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, size, metaState, x, y, deviceId, edgeFlags);
    }

    // --- TESTS DE LOGIQUE onTouch ---

    @Test
    public void onTouch_returns_true_for_all_actions() {
        View mockView = new View(context);

        // DOWN, MOVE, UP doivent tous consommer l'événement
        assertTrue(manager.onTouch(mockView, createMotionEvent(0, 0, MotionEvent.ACTION_DOWN)));
        assertTrue(manager.onTouch(mockView, createMotionEvent(0, 0, MotionEvent.ACTION_MOVE)));
        assertTrue(manager.onTouch(mockView, createMotionEvent(0, 0, MotionEvent.ACTION_UP)));
    }

    @Test
    public void onTouch_ACTION_UP_outside_target_radius_gives_feedback() {
        View mockView = new View(context);

        // Testons une position aux coins qui est très probablement loin de la cible (cible centrée)
        float farX = 10f;
        float farY = 10f;

        // Simuler le mouvement et la levée du doigt
        manager.onTouch(mockView, createMotionEvent(farX, farY, MotionEvent.ACTION_MOVE));
        manager.onTouch(mockView, createMotionEvent(farX, farY, MotionEvent.ACTION_UP));

        // 2. Vérifier que la cible n'a pas été trouvée
        assertFalse(mockListener.targetFoundCalled);

        // 3. Vérifier que le feedback d'échec est envoyé
        assertTrue(mockListener.lastFeedback.contains("Vous vous éloignez, réessayez."));
    }
}