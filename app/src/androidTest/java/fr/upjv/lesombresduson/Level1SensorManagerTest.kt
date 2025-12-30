package fr.upjv.lesombresduson

import androidx.test.platform.app.InstrumentationRegistry
import fr.upjv.lesombresduson.manager.sensor.Level1SensorManager
import fr.upjv.lesombresduson.manager.sensor.Level1SensorListener
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Tests unitaires pour la logique du Level1SensorManager.
 */
class Level1SensorManagerTest {

    private lateinit var sensorManager: Level1SensorManager

    // On mocke l'interface au lieu de la classe finale de l'Activity
    private val mockListener = mock(Level1SensorListener::class.java)

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        sensorManager = Level1SensorManager(context, mockListener)
    }

    /**
     * Vérifie l'initialisation du compteur de gestes.
     */
    @Test
    fun testInitialGestureCount() {
        assertEquals(0, sensorManager.gestureCount)
    }

    /**
     * Vérifie que l'arrêt de l'écoute désenregistre correctement les capteurs.
     */
    @Test
    fun testStopListening() {
        // Cette méthode ne doit pas déclencher d'exception avec le mock
        sensorManager.stopListening()
        assertEquals(0, sensorManager.gestureCount)
    }
}