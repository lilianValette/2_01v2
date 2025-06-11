package com.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifeBonusTest {

    @Test
    void testConstructorSetsFields() {
        LifeBonus bonus = new LifeBonus(5, 6);
        assertEquals(5, bonus.getX());
        assertEquals(6, bonus.getY());
        assertFalse(bonus.isCollected());
        // No assertion on sprite: may be null in test env
    }

    @Test
    void testApplyToPlayerWithLessThan3Lives() {
        Player player = new Player(1, 0, 0, true);
        // By default Player starts with 3 lives, so first lose one
        player.takeDamage();
        assertEquals(2, player.getLives());
        LifeBonus bonus = new LifeBonus(1, 1);
        bonus.applyTo(player);
        assertTrue(bonus.isCollected());
        assertEquals(3, player.getLives());
    }

    @Test
    void testApplyToPlayerWith3LivesDoesNothing() {
        Player player = new Player(1, 0, 0, true);
        assertEquals(3, player.getLives());
        LifeBonus bonus = new LifeBonus(1, 1);
        bonus.applyTo(player);
        assertTrue(bonus.isCollected());
        assertEquals(3, player.getLives());
    }

    @Test
    void testApplyToCanBeCalledMultipleTimesButOnlySetsCollectedOnce() {
        Player player = new Player(1, 0, 0, true);
        player.takeDamage();
        LifeBonus bonus = new LifeBonus(1, 1);
        bonus.applyTo(player);
        assertTrue(bonus.isCollected());
        assertEquals(3, player.getLives());
        // Calling again should not add an extra life (player is already at max)
        bonus.applyTo(player);
        assertEquals(3, player.getLives());
    }
}