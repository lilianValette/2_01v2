package com.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JacketBonusTest {

    @Test
    void testConstructorSetsFields() {
        JacketBonus bonus = new JacketBonus(3, 4);
        assertEquals(3, bonus.getX());
        assertEquals(4, bonus.getY());
        assertFalse(bonus.isCollected());
        // No assertion on sprite: may be null for test env
    }

    @Test
    void testApplyToPlayerAddsInvincibilityAndSetsCollected() {
        Player player = new Player(1, 0, 0, true);
        assertFalse(player.isInvincibleToBombs());
        JacketBonus bonus = new JacketBonus(1, 1);
        assertFalse(bonus.isCollected());

        bonus.applyTo(player);
        assertTrue(bonus.isCollected());
        assertTrue(player.isInvincibleToBombs());

        // Applying again should have no effect
        bonus.applyTo(player);
        assertTrue(player.isInvincibleToBombs());
    }

    @Test
    void testInvincibilityExpires() {
        Player player = new Player(1, 0, 0, true);
        JacketBonus bonus = new JacketBonus(1, 1);

        bonus.applyTo(player);
        // There should be a JACKET bonus active, for 20.0 seconds
        boolean found = player.getActiveBonuses().stream()
                .anyMatch(ab -> ab.getType() == ActiveBonus.Type.JACKET);
        assertTrue(found);

        // Simulate time passing: tick for 20.0 seconds
        for (int i = 0; i < 40; i++) { // 40 * 0.5 = 20.0
            player.updateActiveBonuses();
        }
        assertFalse(player.isInvincibleToBombs());
    }
}