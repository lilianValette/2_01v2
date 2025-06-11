package com.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    @Test
    void testGridInitializationWithLevel() {
        int[][] layout = {
                {1, 2, 0},
                {0, 1, 2},
                {2, 0, 1}
        };
        Level level = new Level("lvl", "author", "easy", "desc", layout);
        Grid grid = new Grid(3, 3, level);

        // Les valeurs du layout doivent être correctement traduites
        assertEquals(Grid.CellType.INDESTRUCTIBLE, grid.getCell(0, 0));
        assertEquals(Grid.CellType.DESTRUCTIBLE, grid.getCell(1, 0));
        assertEquals(Grid.CellType.EMPTY, grid.getCell(2, 0));
        assertEquals(Grid.CellType.DESTRUCTIBLE, grid.getCell(0, 2));
        assertEquals(Grid.CellType.INDESTRUCTIBLE, grid.getCell(2, 2));
    }

    @Test
    void testGridInitializationProcedural() {
        // Teste la grille générée procéduralement sans Level
        Grid grid = new Grid(5, 5, null);
        // Les bords doivent être indestructibles
        for (int i = 0; i < 5; i++) {
            assertEquals(Grid.CellType.INDESTRUCTIBLE, grid.getCell(i, 0));
            assertEquals(Grid.CellType.INDESTRUCTIBLE, grid.getCell(i, 4));
            assertEquals(Grid.CellType.INDESTRUCTIBLE, grid.getCell(0, i));
            assertEquals(Grid.CellType.INDESTRUCTIBLE, grid.getCell(4, i));
        }
        // Les cases centrales sont soit EMPTY, soit DESTRUCTIBLE, soit INDESTRUCTIBLE selon la génération
        assertNotNull(grid.getCell(2, 2));
    }

    @Test
    void testSetAndGetCell() {
        int[][] layout = new int[3][3];
        Level level = new Level("lvl", "author", "easy", "desc", layout);
        Grid grid = new Grid(3, 3, level);

        grid.setCell(1, 1, Grid.CellType.DESTRUCTIBLE);
        assertEquals(Grid.CellType.DESTRUCTIBLE, grid.getCell(1, 1));

        grid.setCell(1, 1, Grid.CellType.EMPTY);
        assertEquals(Grid.CellType.EMPTY, grid.getCell(1, 1));
    }

    @Test
    void testIsInBounds() {
        Level level = new Level("lvl", "author", "easy", "desc", new int[2][2]);
        Grid grid = new Grid(2, 2, level);

        assertTrue(grid.isInBounds(0, 0));
        assertTrue(grid.isInBounds(1, 1));
        assertFalse(grid.isInBounds(-1, 0));
        assertFalse(grid.isInBounds(0, 2));
        assertFalse(grid.isInBounds(2, 0));
    }

    @Test
    void testGetCellOutOfBoundsReturnsNull() {
        Level level = new Level("lvl", "author", "easy", "desc", new int[2][2]);
        Grid grid = new Grid(2, 2, level);

        assertNull(grid.getCell(-1, 0));
        assertNull(grid.getCell(2, 1));
        assertNull(grid.getCell(0, 2));
    }
}