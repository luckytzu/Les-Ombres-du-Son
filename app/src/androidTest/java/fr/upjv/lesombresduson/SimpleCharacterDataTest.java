package fr.upjv.lesombresduson;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleCharacterDataTest {
    private static class MockCharacter {
        final int id;
        final String name;
        final int imageResId;

        MockCharacter(int id, String name, int imageResId) {
            this.id = id;
            this.name = name;
            this.imageResId = imageResId;
        }
    }

    // Définition des "personnages" utilisés par l'activité
    private final MockCharacter CECILIA = new MockCharacter(1, "Cécilia (cécité totale)", 1);
    private final MockCharacter LUM = new MockCharacter(2, "Lum (cécité partielle)", 2);

    // ===========================================
    // TEST DE LA LOGIQUE DES DONNÉES INTERNES
    // ===========================================

    @Test
    public void testCeciliaCharacterData() {
        // Vérifie si les données de Cécilia sont correctement définies
        assertEquals("L'ID de Cécilia doit être 1", 1, CECILIA.id);
        assertEquals("Le nom de Cécilia est incorrect", "Cécilia (cécité totale)", CECILIA.name);
        assertTrue("L'ID d'image de Cécilia doit être définie (non zéro)", CECILIA.imageResId > 0);
    }

    @Test
    public void testLumCharacterData() {
        // Vérifie si les données de Lum sont correctement définies
        assertEquals("L'ID de Lum doit être 2", 2, LUM.id);
        assertEquals("Le nom de Lum est incorrect", "Lum (cécité partielle)", LUM.name);
        assertTrue("L'ID d'image de Lum doit être définie (non zéro)", LUM.imageResId > 0);
    }

    @Test
    public void testCharacterIDsAreUnique() {
        // S'assurer que les IDs sont distincts
        assertNotEquals("Les IDs des personnages doivent être uniques", CECILIA.id, LUM.id);
    }

    // ===========================================
    // TEST DE LA LOGIQUE DE SÉLECTION (Simulée)
    // ===========================================

    private MockCharacter simulate_getLastSelectedCharacter(String nameFromTextView) {
        if (nameFromTextView.equals(CECILIA.name)) {
            return CECILIA;
        } else if (nameFromTextView.equals(LUM.name)) {
            return LUM;
        }
        return null;
    }

    @Test
    public void testGetLastSelectedCharacter_Cecilia() {
        // Simule que la TextView contient le nom de Cécilia
        MockCharacter selected = simulate_getLastSelectedCharacter("Cécilia (cécité totale)");
        assertNotNull("Un personnage doit être retourné pour Cécilia", selected);
        assertEquals("Le personnage retourné doit être Cécilia", CECILIA.id, selected.id);
    }

    @Test
    public void testGetLastSelectedCharacter_Lum() {
        // Simule que la TextView contient le nom de Lum
        MockCharacter selected = simulate_getLastSelectedCharacter("Lum (cécité partielle)");
        assertNotNull("Un personnage doit être retourné pour Lum", selected);
        assertEquals("Le personnage retourné doit être Lum", LUM.id, selected.id);
    }

    @Test
    public void testGetLastSelectedCharacter_None() {
        // Simule qu'une valeur inconnue est dans la TextView
        MockCharacter selected = simulate_getLastSelectedCharacter("Personnage Inconnu");
        assertNull("Aucun personnage ne doit être retourné pour un nom inconnu", selected);
    }
}