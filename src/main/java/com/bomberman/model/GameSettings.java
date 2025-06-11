package com.bomberman.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Paramètres globaux du jeu (notamment niveau d'IA).
 * Permet des bindings JavaFX entre contrôleurs.
 */
public class GameSettings {
    /** Liste des niveaux d'IA disponibles (ordre = enum AIDifficulty). */
    public static final String[] AI_LEVELS = {"EASY", "NORMAL", "HARD"};
    private static final IntegerProperty aiLevelIndex = new SimpleIntegerProperty(0);

    /** @return propriété JavaFX du niveau d'IA sélectionné */
    public static IntegerProperty aiLevelIndexProperty() { return aiLevelIndex; }

    /** @return index du niveau d'IA sélectionné */
    public static int getAiLevelIndex() { return aiLevelIndex.get(); }

    /** Définit l'index du niveau d'IA sélectionné */
    public static void setAiLevelIndex(int idx) { aiLevelIndex.set(idx); }

    /** @return niveau d'IA sélectionné sous forme d'enum */
    public static AIDifficulty getSelectedAIDifficulty() {
        return AIDifficulty.valueOf(AI_LEVELS[getAiLevelIndex()]);
    }
}