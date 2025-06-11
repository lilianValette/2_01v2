package com.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActiveBonusTest {

    @Test
    void testConstructorAndGetters() {
        ActiveBonus ab = new ActiveBonus(ActiveBonus.Type.FLAME, 2, 3.0);
        assertEquals(ActiveBonus.Type.FLAME, ab.getType());
        assertEquals(2, ab.getExtraValue());

        ActiveBonus ab2 = new ActiveBonus(ActiveBonus.Type.JACKET, 0, 5.5);
        assertEquals(ActiveBonus.Type.JACKET, ab2.getType());
        assertEquals(0, ab2.getExtraValue());
    }

    @Test
    void testTickAndIsExpired() {
        ActiveBonus ab = new ActiveBonus(ActiveBonus.Type.FLAME, 1, 1.0);
        assertFalse(ab.isExpired());
        ab.tick(0.5);
        assertFalse(ab.isExpired());
        ab.tick(0.5);
        assertTrue(ab.isExpired());
        // Peut dépasser zéro
        ab.tick(1.0);
        assertTrue(ab.isExpired());
    }

    @Test
    void testIsExpiredInitialZero() {
        ActiveBonus ab = new ActiveBonus(ActiveBonus.Type.LIFE, 0, 0.0);
        assertTrue(ab.isExpired());
    }
}