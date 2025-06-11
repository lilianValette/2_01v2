package com.bomberman.model;

public class LifeBonus extends Bonus {

    /**
     * Bonus de vie : ajoute une vie au joueur si moins de 3.
     * @param x position X du bonus
     * @param y position Y du bonus
     */
    public LifeBonus(int x, int y) {
        super(x, y, "/images/items/life_bonus.png");
    }

    /**
     * Applique le bonus au joueur (ajoute une vie si < 3).
     * @param player joueur cible
     */
    @Override
    public void applyTo(Player player) {
        if (player.getLives() < 3) {
            player.addLife();
        }
        setCollected(true);
    }
}