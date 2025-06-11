package com.bomberman.model;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private static final Path USERS_FILE_PATH = Paths.get("users.txt");

    @BeforeEach
    void cleanUpBefore() throws Exception {
        // Remove users.txt before each test to ensure a clean state
        if (Files.exists(USERS_FILE_PATH)) {
            Files.delete(USERS_FILE_PATH);
        }
        // Reset the singleton
        var field = UserManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    @AfterEach
    void cleanUpAfter() throws Exception {
        if (Files.exists(USERS_FILE_PATH)) {
            Files.delete(USERS_FILE_PATH);
        }
        // Reset the singleton again for isolation
        var field = UserManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    void testCreateAccountAndPersistence() throws IOException {
        UserManager um = UserManager.getInstance();
        assertTrue(um.createAccount("alice", "pw123"));
        assertTrue(um.userExists("alice"));
        assertFalse(um.createAccount("alice", "something")); // already exists
        assertFalse(um.createAccount("", "pw")); // blank username
        assertFalse(um.createAccount("bob", "")); // blank password
        assertTrue(Files.exists(USERS_FILE_PATH));

        // Check file is not empty and contains alice
        List<String> lines = Files.readAllLines(USERS_FILE_PATH);
        assertTrue(lines.stream().anyMatch(l -> l.contains("alice")));
    }

    @Test
    void testLoginAndLogout() {
        UserManager um = UserManager.getInstance();
        um.createAccount("bob", "qwerty");
        assertFalse(um.isLoggedIn());
        assertTrue(um.login("bob", "qwerty"));
        assertTrue(um.isLoggedIn());
        assertEquals("bob", um.getCurrentUser().getUsername());
        assertFalse(um.login("bob", "badpw")); // wrong password
        um.logout();
        assertFalse(um.isLoggedIn());
        assertNull(um.getCurrentUser());
    }

    @Test
    void testUserExists() {
        UserManager um = UserManager.getInstance();
        assertFalse(um.userExists("notfound"));
        um.createAccount("test", "pw");
        assertTrue(um.userExists("test"));
    }

    @Test
    void testStatsUpdateAndPersistence() throws Exception {
        UserManager um = UserManager.getInstance();
        um.createAccount("player", "pw");
        um.login("player", "pw");
        User user = um.getCurrentUser();
        int gamesBefore = user.getGamesPlayed();
        int wonBefore = user.getGamesWon();
        int scoreBefore = user.getTotalScore();

        um.updateCurrentUserStats(true, 50);

        assertEquals(gamesBefore + 1, user.getGamesPlayed());
        assertEquals(wonBefore + 1, user.getGamesWon());
        assertEquals(scoreBefore + 50, user.getTotalScore());

        // Should be persisted in file
        List<String> lines = Files.readAllLines(USERS_FILE_PATH);
        assertTrue(lines.stream().anyMatch(l -> l.contains("player")));

        // Simulate reload
        var field = UserManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
        UserManager um2 = UserManager.getInstance();
        assertTrue(um2.userExists("player"));
        assertTrue(um2.login("player", "pw"));
        User loaded = um2.getCurrentUser();
        assertEquals(user.getGamesPlayed(), loaded.getGamesPlayed());
        assertEquals(user.getGamesWon(), loaded.getGamesWon());
        assertEquals(user.getTotalScore(), loaded.getTotalScore());
    }
}