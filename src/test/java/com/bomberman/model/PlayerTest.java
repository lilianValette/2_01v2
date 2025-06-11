package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;
    private Grid grid;

    @BeforeEach
    void setUp() {
        // Grille vide 5x5, toutes cases EMPTY
        int[][] layout = new int[5][5];
        Level level = new Level("test", "author", "easy", "desc", layout);
        grid = new Grid(5, 5, level);
        player = new Player(1, 2, 2, true);
    }

    @Test
    void testPlayerInitialization() {
        assertEquals(1, player.getId());
        assertEquals(2, player.getX());
        assertEquals(2, player.getY());
        assertTrue(player.isAlive());
        assertTrue(player.isHuman());
        assertFalse(player.isAI());
        assertEquals(3, player.getLives());
        assertEquals(1, player.getBombRange());
        assertEquals(1, player.getMaxBombs());
    }

    @Test
    void testMoveValidAndInvalid() {
        // Case vide déplacement valide
        player.move(1, 0, grid);
        assertEquals(3, player.getX());
        assertEquals(2, player.getY());

        // Place un mur (non vide) à (4,2), déplacement impossible
        grid.setCell(4, 2, Grid.CellType.INDESTRUCTIBLE);
        player.move(1, 0, grid); // tente d'aller sur (4,2)
        assertEquals(3, player.getX()); // doit rester sur place

        // Déplacement hors grille
        player.move(10, 0, grid);
        assertEquals(3, player.getX()); // doit rester sur place
    }

    @Test
    void testMoveDirections() {
        player.moveUp(grid);
        assertEquals(2, player.getX());
        assertEquals(1, player.getY());

        player.moveDown(grid);
        assertEquals(2, player.getX());
        assertEquals(2, player.getY());

        player.moveLeft(grid);
        assertEquals(1, player.getX());
        assertEquals(2, player.getY());

        player.moveRight(grid);
        assertEquals(2, player.getX());
        assertEquals(2, player.getY());
    }

    @Test
    void testKillAndTakeDamage() {
        player.kill();
        assertFalse(player.isAlive());

        player = new Player(1, 2, 2, true);
        player.takeDamage();
        assertEquals(2, player.getLives());
        assertTrue(player.isAlive());
        player.takeDamage();
        player.takeDamage();
        assertEquals(0, player.getLives());
        assertFalse(player.isAlive());
    }

    @Test
    void testAddLife() {
        player.addLife();
        assertEquals(4, player.getLives());
    }

    @Test
    void testBombDropLimit() {
        List<Bomb> activeBombs = new ArrayList<>();
        Bomb bomb1 = player.dropBomb(3, activeBombs);
        assertNotNull(bomb1);
        activeBombs.add(bomb1);
        // limite maxBombs = 1
        Bomb bomb2 = player.dropBomb(3, activeBombs);
        assertNull(bomb2);

        player.setMaxBombs(2);
        Bomb bomb3 = player.dropBomb(3, activeBombs);
        assertNotNull(bomb3);
        activeBombs.add(bomb3);

        // Atteint la limite 2
        Bomb bomb4 = player.dropBomb(3, activeBombs);
        assertNull(bomb4);
    }

    @Test
    void testBombRangeModification() {
        player.setBombRange(5);
        assertEquals(5, player.getBombRange());
    }

    @Test
    void testActiveBonusesAndInvincibility() {
        assertFalse(player.isInvincibleToBombs());
        player.addJacketBonusTemp(2.0);
        assertTrue(player.isInvincibleToBombs());

        // Test que le bonus expire bien après updateActiveBonuses
        ActiveBonus ab = player.getActiveBonuses().get(0);
        ab.tick(2.0); // fait expirer le bonus
        player.updateActiveBonuses();
        assertFalse(player.isInvincibleToBombs());
    }

    @Test
    void testAddFlameBonusTemp() {
        int rangeBefore = player.getBombRange();
        player.addFlameBonusTemp(2, 1.0);
        assertEquals(rangeBefore + 2, player.getBombRange());
        assertEquals(1, player.getActiveBonuses().size());
        ActiveBonus bonus = player.getActiveBonuses().get(0);
        assertEquals(ActiveBonus.Type.FLAME, bonus.getType());
        // Après expiration, la portée diminue
        bonus.tick(1.0);
        player.updateActiveBonuses();
        assertEquals(rangeBefore, player.getBombRange());
        assertEquals(0, player.getActiveBonuses().size());
    }
}