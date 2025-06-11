package com.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlameBonusTest {

    @Test
    void testConstructorSetsFields() {
        FlameBonus bonus = new FlameBonus(2, 3, 5);
        assertEquals(2, bonus.getX());
        assertEquals(3, bonus.getY());
        // The sprite will usually be null during tests (unless resource exists in test env)
        assertEquals(false, bonus.isCollected());
    }

    @Test
    void testApplyToPlayerIncreasesRangeAndSetsCollected() {
        Player player = new Player(1, 0, 0, true);
        int initialRange = player.getBombRange();
        FlameBonus bonus = new FlameBonus(1, 1, 2);
        assertFalse(bonus.isCollected());

        bonus.applyTo(player);
        assertTrue(bonus.isCollected());
        // The player's bomb range should be incremented by 2
        assertEquals(initialRange + 2, player.getBombRange());

        // Applying again should have no effect
        bonus.applyTo(player);
        assertEquals(initialRange + 2, player.getBombRange());
    }

    @Test
    void testApplyToPlayerAddsActiveBonus() {
        Player player = new Player(1, 0, 0, true);
        FlameBonus bonus = new FlameBonus(1, 1, 2);
        bonus.applyTo(player);
        boolean found = player.getActiveBonuses().stream()
                .anyMatch(ab -> ab.getType() == ActiveBonus.Type.FLAME && ab.getExtraValue() == 2);
        assertTrue(found);
    }
}