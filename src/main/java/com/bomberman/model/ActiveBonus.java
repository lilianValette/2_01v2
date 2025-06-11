package com.bomberman.model;

/**
 * Représente un bonus appliqué à un joueur,
 * avec son type, sa valeur (extraRange) et sa durée restante en secondes.
 */
public class ActiveBonus {
    public enum Type { FLAME, JACKET, LIFE }

    private final Type type;
    private final int extraValue;
    private double secondsRemaining;  // durée restante en secondes

    /**
     * @param type           type de bonus (ex : FLAME, JACKET, LIFE)
     * @param extraValue     valeur associée au bonus (ex : +1 portée)
     * @param durationSeconds durée du bonus en secondes
     */
    public ActiveBonus(Type type, int extraValue, double durationSeconds) {
        this.type = type;
        this.extraValue = extraValue;
        this.secondsRemaining = durationSeconds;
    }

    /** @return le type du bonus */
    public Type getType() {
        return type;
    }

    /** @return la valeur associée au bonus (ex : portée supplémentaire) */
    public int getExtraValue() {
        return extraValue;
    }

    /**
     * À appeler à chaque tick (par ex. 0,5 s) pour décrémenter la durée réelle.
     * @param tickDuration durée d'un tick en secondes
     */
    public void tick(double tickDuration) {
        secondsRemaining -= tickDuration;
    }

    /** @return true si le bonus est expiré */
    public boolean isExpired() {
        return secondsRemaining <= 0;
    }
}