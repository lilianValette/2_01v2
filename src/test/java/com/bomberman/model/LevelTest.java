package com.bomberman.model;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {

    private int[][] layout;
    private Level level;

    @BeforeEach
    void setUp() {
        layout = new int[][] {
                {0, 1, 2},
                {2, 0, 1}
        };
        level = new Level("TestLevel", "ground.png", "MurIndestructible.PNG", "MurDestructible.PNG", layout);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("TestLevel", level.getName());
        // Chemins normalisés
        assertEquals("/ground.png", level.getGroundImagePath());
        assertEquals("/murIndestructible.png", level.getWallIndestructibleImagePath());
        assertEquals("/murDestructible.png", level.getWallDestructibleImagePath());
        assertArrayEquals(layout, level.getLayout());
    }

    @Test
    void testNormalizeResourcePath() {
        Level l = new Level("n", "FondFeuilles.PNG", "BonhommeNeige.PNG", "MurDestructible.PNG", new int[1][1]);
        assertEquals("/fondFeuilles.png", l.getGroundImagePath());
        assertEquals("/BonhommeNeige.png", l.getWallIndestructibleImagePath());
        assertEquals("/murDestructible.png", l.getWallDestructibleImagePath());
    }

    @Test
    void testSaveToFileAndFromFile() throws IOException {
        Path tempFile = Files.createTempFile("leveltest", ".level");
        try {
            level.saveToFile(tempFile);
            Level loaded = Level.fromFile(tempFile);
            assertEquals(level.getName(), loaded.getName());
            assertEquals(level.getGroundImagePath(), loaded.getGroundImagePath());
            assertEquals(level.getWallIndestructibleImagePath(), loaded.getWallIndestructibleImagePath());
            assertEquals(level.getWallDestructibleImagePath(), loaded.getWallDestructibleImagePath());
            assertArrayEquals(level.getLayout(), loaded.getLayout());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testFromFileWithInvalidFileThrows() throws IOException {
        Path tempFile = Files.createTempFile("invalid", ".level");
        // Ecris un fichier sans "layout:"
        Files.writeString(tempFile, "name: Bad\n");
        assertThrows(IOException.class, () -> Level.fromFile(tempFile));
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testLoadLevelsFromDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("leveldir");
        try {
            // Créé deux fichiers valides et un invalide
            Level l1 = new Level("L1", "g1.png", "i1.png", "d1.png", new int[][]{{0}});
            Level l2 = new Level("L2", "g2.png", "i2.png", "d2.png", new int[][]{{1}});
            Path f1 = tempDir.resolve("one.level");
            Path f2 = tempDir.resolve("two.level");
            Path fBad = tempDir.resolve("bad.level");
            l1.saveToFile(f1);
            l2.saveToFile(f2);
            Files.writeString(fBad, "malformed file");
            List<Level> levels = Level.loadLevelsFromDirectory(tempDir);
            // Le fichier mal formé doit être ignoré
            assertEquals(2, levels.size());
            assertTrue(levels.stream().anyMatch(l -> l.getName().equals("L1")));
            assertTrue(levels.stream().anyMatch(l -> l.getName().equals("L2")));
        } finally {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
    }
}