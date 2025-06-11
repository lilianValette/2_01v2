package com.bomberman.model;

/**
 * Bonus FLAME : augmente temporairement la portée des bombes.
 */
public class FlameBonus extends Bonus {
    private final int extraRange;
    private static final double DURATION_SECONDS = 10.0; // dure 10 secondes

    /**
     * Crée un bonus Flame à la position donnée.
     * @param x position X
     * @param y position Y
     * @param extraRange portée supplémentaire accordée
     */
    public FlameBonus(int x, int y, int extraRange) {
        super(x, y, "/images/items/flame_bonus.png");
        this.extraRange = extraRange;
    }

    /**
     * Applique le bonus au joueur (portée accrue temporaire).
     * @param player joueur cible
     */
    @Override
    public void applyTo(Player player) {
        if (!isCollected()) {
            player.addFlameBonusTemp(extraRange, DURATION_SECONDS);
            setCollected(true);
        }
    }
}