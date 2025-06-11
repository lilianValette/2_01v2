package com.bomberman.model;

/**
 * Bonus JACKET : rend temporairement invincible face aux bombes.
 */
public class JacketBonus extends Bonus {
    private static final double DURATION_SECONDS = 20.0;

    /**
     * Crée un bonus Jacket à la position donnée.
     * @param x position X
     * @param y position Y
     */
    public JacketBonus(int x, int y) {
        super(x, y, "/images/items/jacket_bonus.png");
    }

    /**
     * Applique le bonus au joueur (invincibilité temporaire).
     * @param player joueur cible
     */
    @Override
    public void applyTo(Player player) {
        if (!isCollected()) {
            player.addJacketBonusTemp(DURATION_SECONDS);
            setCollected(true);
        }
    }
}