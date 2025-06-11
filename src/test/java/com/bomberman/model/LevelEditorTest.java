package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.bomberman.model.LevelEditor.CellType;
import static org.junit.jupiter.api.Assertions.*;

class LevelEditorTest {

    private LevelEditor editor;

    @BeforeEach
    void setUp() {
        editor = new LevelEditor(4, 3);
    }

    @Test
    void testInitialGridIsEmpty() {
        for (int y = 0; y < editor.getHeight(); y++) {
            for (int x = 0; x < editor.getWidth(); x++) {
                assertEquals(CellType.EMPTY, editor.getCell(x, y));
            }
        }
    }

    @Test
    void testSetAndGetCell() {
        editor.setCell(1, 1, CellType.WALL);
        assertEquals(CellType.WALL, editor.getCell(1, 1));

        editor.setCell(2, 2, CellType.BREAKABLE);
        assertEquals(CellType.BREAKABLE, editor.getCell(2, 2));

        // Hors limites
        assertNull(editor.getCell(-1, 0));
        assertNull(editor.getCell(10, 1));
        assertNull(editor.getCell(1, 10));
    }

    @Test
    void testClearResetsAllCellsToEmpty() {
        editor.setCell(0, 0, CellType.WALL);
        editor.setCell(3, 2, CellType.BREAKABLE);
        editor.clear();
        for (int y = 0; y < editor.getHeight(); y++) {
            for (int x = 0; x < editor.getWidth(); x++) {
                assertEquals(CellType.EMPTY, editor.getCell(x, y));
            }
        }
    }

    @Test
    void testToIntLayoutAndFromIntLayout() {
        // Setup
        editor.setCell(0, 0, CellType.WALL);
        editor.setCell(1, 0, CellType.BREAKABLE);
        editor.setCell(2, 0, CellType.EMPTY);
        editor.setCell(3, 0, CellType.PLAYER_SPAWN);

        int[][] layout = editor.toIntLayout();
        assertEquals(1, layout[0][0]); // WALL
        assertEquals(2, layout[0][1]); // BREAKABLE
        assertEquals(0, layout[0][2]); // EMPTY
        assertEquals(0, layout[0][3]); // PLAYER_SPAWN is mapped to 0

        // Test fromIntLayout
        int[][] newLayout = {
                {2, 1, 0, 1},
                {0, 2, 2, 0},
                {1, 0, 0, 2}
        };
        editor.fromIntLayout(newLayout);
        assertEquals(CellType.BREAKABLE, editor.getCell(0, 0));
        assertEquals(CellType.WALL, editor.getCell(1, 0));
        assertEquals(CellType.EMPTY, editor.getCell(2, 0));
        assertEquals(CellType.WALL, editor.getCell(3, 0));
        assertEquals(CellType.BREAKABLE, editor.getCell(1, 1));
    }

    @Test
    void testToLevelAndLoadFromLevel() {
        editor.setCell(0, 0, CellType.WALL);
        editor.setCell(1, 0, CellType.BREAKABLE);

        Level level = editor.toLevel("MyLevel", "ground.png", "wallInd.png", "wallDes.png");
        assertEquals("MyLevel", level.getName());
        assertEquals("/ground.png", level.getGroundImagePath());
        assertEquals("/wallInd.png", level.getWallIndestructibleImagePath());
        assertEquals("/wallDes.png", level.getWallDestructibleImagePath());

        // Test loadFromLevel
        LevelEditor otherEditor = new LevelEditor(4, 3);
        otherEditor.loadFromLevel(level);
        assertEquals(CellType.WALL, otherEditor.getCell(0, 0));
        assertEquals(CellType.BREAKABLE, otherEditor.getCell(1, 0));
    }

    @Test
    void testGetGridReturnsCorrectReference() {
        List<List<CellType>> grid = editor.getGrid();
        assertEquals(editor.getHeight(), grid.size());
        assertEquals(editor.getWidth(), grid.get(0).size());
        // Modification du grid directement doit affecter l'éditeur
        grid.get(0).set(0, CellType.BONUS);
        assertEquals(CellType.BONUS, editor.getCell(0, 0));
    }
}