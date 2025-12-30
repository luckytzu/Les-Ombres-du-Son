package fr.upjv.lesombresduson

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.upjv.lesombresduson.ui.game.cecilia.CeciliaGameActivityAfterIntro
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests d'intégration pour l'activité CeciliaGameActivityAfterIntro.
 */
@RunWith(AndroidJUnit4::class)
class CeciliaGameActivityAfterIntroTest {

    @Test
    fun testActivityLaunch() {
        // ActivityScenario gère le cycle de vie de l'activité pour le test
        ActivityScenario.launch(CeciliaGameActivityAfterIntro::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("L'activité doit être initialisée correctement", activity)
            }
        }
    }

    @Test
    fun testWinStateTransition() {
        ActivityScenario.launch(CeciliaGameActivityAfterIntro::class.java).use { scenario ->
            scenario.onActivity { activity ->
                // Pour que cela fonctionne, onGestureValidated doit être publique ou interne
                activity.onGestureValidated(true, "")

                // Ici, le test passe si l'appel ne provoque pas d'exception audio ou haptique
            }
        }
    }
}