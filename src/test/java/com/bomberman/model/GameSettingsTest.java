package com.bomberman.model;

import javafx.beans.property.IntegerProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSettingsTest {

    @BeforeEach
    void resetAIDifficulty() {
        // On s'assure que le niveau d'IA est bien remis à zéro avant chaque test
        GameSettings.setAiLevelIndex(0);
    }

    @Test
    void testDefaultAILevelIndex() {
        assertEquals(0, GameSettings.getAiLevelIndex());
        assertEquals(AIDifficulty.EASY, GameSettings.getSelectedAIDifficulty());
    }

    @Test
    void testSetAILevelIndex() {
        GameSettings.setAiLevelIndex(1);
        assertEquals(1, GameSettings.getAiLevelIndex());
        assertEquals(AIDifficulty.NORMAL, GameSettings.getSelectedAIDifficulty());

        GameSettings.setAiLevelIndex(2);
        assertEquals(2, GameSettings.getAiLevelIndex());
        assertEquals(AIDifficulty.HARD, GameSettings.getSelectedAIDifficulty());
    }

    @Test
    void testAILevelIndexPropertyBinding() {
        IntegerProperty prop = GameSettings.aiLevelIndexProperty();
        assertEquals(0, prop.get());
        prop.set(2);
        assertEquals(2, GameSettings.getAiLevelIndex());
        assertEquals(AIDifficulty.HARD, GameSettings.getSelectedAIDifficulty());

        // Test de binding (JavaFX)
        prop.set(1);
        assertEquals(AIDifficulty.NORMAL, GameSettings.getSelectedAIDifficulty());
    }

    @Test
    void testInvalidIndexThrowsException() {
        // Si un index invalide est utilisé, valueOf lancera une IllegalArgumentException
        GameSettings.setAiLevelIndex(0);
        assertDoesNotThrow(() -> GameSettings.getSelectedAIDifficulty());
        GameSettings.setAiLevelIndex(1);
        assertDoesNotThrow(() -> GameSettings.getSelectedAIDifficulty());
        GameSettings.setAiLevelIndex(2);
        assertDoesNotThrow(() -> GameSettings.getSelectedAIDifficulty());

        // Index hors bornes
        GameSettings.setAiLevelIndex(99);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> GameSettings.getSelectedAIDifficulty());
    }
}