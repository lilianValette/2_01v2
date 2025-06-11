package com.bomberman.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Représente un joueur (humain ou IA) dans le jeu Bomberman.
 */
public class Player {
    private final int id;
    private int x, y;
    private boolean alive = true;
    private int lives = 3;
    private int bombRange = 1;   // portée des bombes
    private int maxBombs = 1;    // bombes simultanées autorisées
    private final List<ActiveBonus> activeBonuses = new ArrayList<>();
    private final boolean isHuman;

    /**
     * Crée un joueur.
     * @param id identifiant du joueur (1, 2, ...)
     * @param startX position X initiale
     * @param startY position Y initiale
     * @param isHuman true si humain, false si IA
     */
    public Player(int id, int startX, int startY, boolean isHuman) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.isHuman = isHuman;
    }

    /** @return identifiant du joueur */
    public int getId() { return id; }
    /** @return position X */
    public int getX() { return x; }
    /** @return position Y */
    public int getY() { return y; }
    /** @return true si vivant */
    public boolean isAlive() { return alive; }
    /** @return nombre de vies restantes */
    public int getLives() { return lives; }
    /** @return true si joueur humain */
    public boolean isHuman() { return isHuman; }
    /** @return true si IA */
    public boolean isAI() { return !isHuman; }

    /** Tue le joueur (plus vivant) */
    public void kill() { this.alive = false; }

    /** Inflige des dégâts et gère la mort éventuelle */
    public void takeDamage() {
        if (lives > 0 && --lives == 0) alive = false;
    }

    /**
     * Déplace le joueur si la case est vide.
     * @param dx déplacement en X
     * @param dy déplacement en Y
     * @param grid grille du jeu
     */
    public void move(int dx, int dy, Grid grid) {
        if (!alive) return;
        int newX = x + dx, newY = y + dy;
        if (grid.isInBounds(newX, newY) && grid.getCell(newX, newY) == Grid.CellType.EMPTY) {
            this.x = newX;
            this.y = newY;
        }
    }

    /** Déplacements utilitaires */
    public void moveUp(Grid grid)    { move(0, -1, grid); }
    public void moveDown(Grid grid)  { move(0,  1, grid); }
    public void moveLeft(Grid grid)  { move(-1, 0, grid); }
    public void moveRight(Grid grid) { move(1,  0, grid); }

    /** @return portée des bombes */
    public int getBombRange() { return bombRange; }
    /** Modifie la portée des bombes */
    public void setBombRange(int bombRange) { this.bombRange = bombRange; }
    /** @return nombre max de bombes simultanées */
    public int getMaxBombs() { return maxBombs; }
    /** Modifie le nombre max de bombes */
    public void setMaxBombs(int maxBombs) { this.maxBombs = maxBombs; }
    /** @return liste des bonus actifs */
    public List<ActiveBonus> getActiveBonuses() { return activeBonuses; }

    /**
     * Pose une bombe si le joueur n'a pas atteint sa limite de bombes actives.
     * @param timer durée avant explosion
     * @param activeBombs liste des bombes déjà posées par ce joueur
     * @return nouvelle bombe ou null si limite atteinte
     */
    public Bomb dropBomb(int timer, List<Bomb> activeBombs) {
        if (activeBombs.size() < maxBombs) {
            return new Bomb(this.x, this.y, timer, this.bombRange, this);
        }
        return null;
    }

    /**
     * Met à jour la durée des bonus actifs et retire ceux expirés.
     */
    public void updateActiveBonuses() {
        double tickDuration = 0.5;
        Iterator<ActiveBonus> it = activeBonuses.iterator();
        while (it.hasNext()) {
            ActiveBonus ab = it.next();
            ab.tick(tickDuration);
            if (ab.isExpired()) {
                if (ab.getType() == ActiveBonus.Type.FLAME) {
                    bombRange -= ab.getExtraValue();
                }
                it.remove();
            }
        }
    }

    /**
     * Ajoute un bonus JACKET temporaire (invincibilité).
     * @param durationSeconds durée en secondes
     */
    public void addJacketBonusTemp(double durationSeconds) {
        activeBonuses.add(new ActiveBonus(ActiveBonus.Type.JACKET, 0, durationSeconds));
    }

    /** @return true si joueur invincible face aux bombes */
    public boolean isInvincibleToBombs() {
        return activeBonuses.stream().anyMatch(ab -> ab.getType() == ActiveBonus.Type.JACKET);
    }

    /** Ajoute une vie au joueur */
    public void addLife() { lives++; }

    /**
     * Ajoute un bonus FLAME temporaire (portée accrue).
     * @param extraRange portée supplémentaire
     * @param durationSeconds durée en secondes
     */
    public void addFlameBonusTemp(int extraRange, double durationSeconds) {
        bombRange += extraRange;
        activeBonuses.add(new ActiveBonus(ActiveBonus.Type.FLAME, extraRange, durationSeconds));
    }
}