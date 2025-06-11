package com.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserConstructorWithoutStats() {
        User user = new User("alice", "password123");
        assertEquals("alice", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals(0, user.getGamesPlayed());
        assertEquals(0, user.getGamesWon());
        assertEquals(0, user.getTotalScore());
    }

    @Test
    void testUserConstructorWithStats() {
        User user = new User("bob", "secret", 10, 6, 800);
        assertEquals("bob", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals(10, user.getGamesPlayed());
        assertEquals(6, user.getGamesWon());
        assertEquals(800, user.getTotalScore());
    }

    @Test
    void testIncrementGamesPlayed() {
        User user = new User("charlie", "pw");
        user.incrementGamesPlayed();
        assertEquals(1, user.getGamesPlayed());
        user.incrementGamesPlayed();
        assertEquals(2, user.getGamesPlayed());
    }

    @Test
    void testIncrementGamesWon() {
        User user = new User("dan", "pw");
        user.incrementGamesWon();
        assertEquals(1, user.getGamesWon());
        user.incrementGamesWon();
        assertEquals(2, user.getGamesWon());
    }

    @Test
    void testAddScore() {
        User user = new User("eve", "pw");
        user.addScore(150);
        assertEquals(150, user.getTotalScore());
        user.addScore(50);
        assertEquals(200, user.getTotalScore());
    }

    @Test
    void testWinRate() {
        User user = new User("fred", "pw");
        // Aucun match joué
        assertEquals(0.0, user.getWinRate());
        // 2 victoires, 4 parties
        user = new User("fred", "pw", 4, 2, 0);
        assertEquals(50.0, user.getWinRate());
        // 5 victoires, 10 parties
        user = new User("fred", "pw", 10, 5, 0);
        assertEquals(50.0, user.getWinRate());
    }

    @Test
    void testToStringAndFromString() {
        User user = new User("gina", "pw", 12, 4, 350);
        String serialized = user.toString();
        assertEquals("gina,pw,12,4,350", serialized);

        User parsed = User.fromString(serialized);
        assertNotNull(parsed);
        assertEquals("gina", parsed.getUsername());
        assertEquals("pw", parsed.getPassword());
        assertEquals(12, parsed.getGamesPlayed());
        assertEquals(4, parsed.getGamesWon());
        assertEquals(350, parsed.getTotalScore());
    }

    @Test
    void testFromStringMalformed() {
        // Pas assez de champs
        assertNull(User.fromString("bad,format"));
        // Champs non numériques
        assertNull(User.fromString("u,pw,xx,yy,zz"));
        // Trop de champs
        assertNull(User.fromString("a,b,1,2,3,4"));
    }
}