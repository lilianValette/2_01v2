package com.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BombTest {

    @Test
    void testConstructorWithOwnerAndGetters() {
        Player player = new Player(1, 2, 3, true);
        Bomb bomb = new Bomb(4, 5, 6, 7, player);

        assertEquals(4, bomb.getX());
        assertEquals(5, bomb.getY());
        assertEquals(6, bomb.getTimer());
        assertEquals(7, bomb.getRange());
        assertEquals(player, bomb.getOwner());
        assertFalse(bomb.isExploded());
    }

    @Test
    void testConstructorWithoutOwner() {
        Bomb bomb = new Bomb(1, 2, 3, 4);

        assertEquals(1, bomb.getX());
        assertEquals(2, bomb.getY());
        assertEquals(3, bomb.getTimer());
        assertEquals(4, bomb.getRange());
        assertNull(bomb.getOwner());
        assertFalse(bomb.isExploded());
    }

    @Test
    void testTickDecrementsTimerAndIsExploded() {
        Bomb bomb = new Bomb(0, 0, 2, 1);
        assertFalse(bomb.isExploded());
        bomb.tick();
        assertFalse(bomb.isExploded());
        bomb.tick();
        assertTrue(bomb.isExploded());
        // Timer peut être négatif
        bomb.tick();
        assertTrue(bomb.isExploded());
    }

    @Test
    void testForceExplode() {
        Bomb bomb = new Bomb(0, 0, 10, 1);
        assertEquals(10, bomb.getTimer());
        bomb.forceExplode();
        assertEquals(0, bomb.getTimer());
        assertTrue(bomb.isExploded());
    }
}