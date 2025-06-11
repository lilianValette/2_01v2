package com.bomberman.model;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BonusTest {

    // Dummy concrete subclass for testing
    static class TestBonus extends Bonus {
        boolean applied = false;
        public TestBonus(int x, int y, String resource) {
            super(x, y, resource);
        }
        @Override
        public void applyTo(Player player) {
            applied = true;
        }
    }

    @Test
    void testConstructorAndGetters() {
        // "resource" does not exist, so sprite should be null (no exception thrown)
        TestBonus bonus = new TestBonus(2, 3, "/not/found.png");
        assertEquals(2, bonus.getX());
        assertEquals(3, bonus.getY());
        assertFalse(bonus.isCollected());
        assertNull(bonus.getSprite());
    }

    @Test
    void testSetCollected() {
        TestBonus bonus = new TestBonus(0, 0, "/not/found.png");
        assertFalse(bonus.isCollected());
        bonus.setCollected(true);
        assertTrue(bonus.isCollected());
        bonus.setCollected(false);
        assertFalse(bonus.isCollected());
    }

    @Test
    void testApplyToIsCalled() {
        TestBonus bonus = new TestBonus(1, 1, "/not/found.png");
        Player player = new Player(1, 0, 0, true);
        assertFalse(bonus.applied);
        bonus.applyTo(player);
        assertTrue(bonus.applied);
    }
}