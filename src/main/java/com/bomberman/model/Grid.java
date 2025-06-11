package com.bomberman.model;

/**
 * Représente la grille de jeu Bomberman avec gestion des types de cases.
 */
public class Grid {
    /**
     * Types de cases possibles sur la grille.
     */
    public enum CellType {
        EMPTY,
        INDESTRUCTIBLE,
        DESTRUCTIBLE,
        BOMB,
        EXPLOSION
    }

    private final int width;
    private final int height;
    private final CellType[][] cells;

    /**
     * Initialise la grille à partir d'un niveau (layout) ou de façon procédurale si null.
     * @param width largeur de la grille
     * @param height hauteur de la grille
     * @param level niveau à appliquer (peut être null)
     */
    public Grid(int width, int height, Level level) {
        this.width = width;
        this.height = height;
        cells = new CellType[width][height];

        int[][] layout = (level != null) ? level.getLayout() : null;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout != null && y < layout.length && x < layout[0].length) {
                    cells[x][y] = switch (layout[y][x]) {
                        case 1 -> CellType.INDESTRUCTIBLE;
                        case 2 -> CellType.DESTRUCTIBLE;
                        default -> CellType.EMPTY;
                    };
                } else {
                    if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                        cells[x][y] = CellType.INDESTRUCTIBLE;
                    } else if (x % 2 == 0 && y % 2 == 0) {
                        cells[x][y] = CellType.INDESTRUCTIBLE;
                    } else if (Math.random() < 0.2) {
                        cells[x][y] = CellType.DESTRUCTIBLE;
                    } else {
                        cells[x][y] = CellType.EMPTY;
                    }
                }
            }
        }
    }

    /** @return largeur de la grille */
    public int getWidth() { return width; }

    /** @return hauteur de la grille */
    public int getHeight() { return height; }

    /**
     * Retourne le type de case à la position (x,y), ou null si hors limites.
     */
    public CellType getCell(int x, int y) {
        return isInBounds(x, y) ? cells[x][y] : null;
    }

    /**
     * Définit le type de case à la position (x,y) si dans la grille.
     */
    public void setCell(int x, int y, CellType cellType) {
        if (isInBounds(x, y)) cells[x][y] = cellType;
    }

    /**
     * Vérifie si les coordonnées (x,y) sont dans la grille.
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}