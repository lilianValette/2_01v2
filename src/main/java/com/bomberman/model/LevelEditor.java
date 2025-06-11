package com.bomberman.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Permet la création, modification et conversion de niveaux personnalisés Bomberman.
 */
public class LevelEditor {

    private final int width;
    private final int height;
    private final List<List<CellType>> grid;

    /**
     * Types de cases supportés dans l'éditeur.
     */
    public enum CellType {
        EMPTY,
        WALL,           // Mur indestructible
        BREAKABLE,      // Mur destructible
        PLAYER_SPAWN,
        AI_SPAWN,
        BONUS
    }

    /**
     * Initialise un éditeur de niveau vide avec la taille donnée.
     * @param width largeur
     * @param height hauteur
     */
    public LevelEditor(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            List<CellType> row = new ArrayList<>();
            for (int x = 0; x < width; x++) {
                row.add(CellType.EMPTY);
            }
            grid.add(row);
        }
    }

    /** @return largeur du niveau */
    public int getWidth() { return width; }

    /** @return hauteur du niveau */
    public int getHeight() { return height; }

    /**
     * Retourne le type de case aux coordonnées données.
     * @param x abscisse
     * @param y ordonnée
     * @return type de case ou null si hors limites
     */
    public CellType getCell(int x, int y) {
        return isInBounds(x, y) ? grid.get(y).get(x) : null;
    }

    /**
     * Définit le type de case aux coordonnées données.
     * @param x abscisse
     * @param y ordonnée
     * @param type type de case
     */
    public void setCell(int x, int y, CellType type) {
        if (isInBounds(x, y)) {
            grid.get(y).set(x, type);
        }
    }

    /** Remet toutes les cases à EMPTY. */
    public void clear() {
        for (List<CellType> row : grid) {
            row.replaceAll(cell -> CellType.EMPTY);
        }
    }

    /** @return grille éditée */
    public List<List<CellType>> getGrid() {
        return grid;
    }

    /** @return true si (x, y) est dans la grille */
    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Convertit la grille de l'éditeur en layout int[][] compatible avec Level.
     * 0: sol, 1: mur indestructible, 2: destructible
     */
    public int[][] toIntLayout() {
        int[][] layout = new int[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                layout[y][x] = switch (grid.get(y).get(x)) {
                    case WALL -> 1;
                    case BREAKABLE -> 2;
                    default -> 0;
                };
        return layout;
    }

    /**
     * Charge l'état de la grille depuis un layout int[][].
     * @param layout matrice de cases
     */
    public void fromIntLayout(int[][] layout) {
        for (int y = 0; y < height && y < layout.length; y++)
            for (int x = 0; x < width && x < layout[y].length; x++)
                grid.get(y).set(x, switch (layout[y][x]) {
                    case 1 -> CellType.WALL;
                    case 2 -> CellType.BREAKABLE;
                    default -> CellType.EMPTY;
                });
    }

    /**
     * Crée un objet Level à partir de l'état de l'éditeur.
     * @param name nom du niveau
     * @param groundImg chemin image sol
     * @param wallIndImg chemin image mur indestructible
     * @param wallDesImg chemin image mur destructible
     * @return Level
     */
    public Level toLevel(String name, String groundImg, String wallIndImg, String wallDesImg) {
        return new Level(name, groundImg, wallIndImg, wallDesImg, toIntLayout());
    }

    /**
     * Charge l'état de l'éditeur depuis un Level.
     * @param level niveau source
     */
    public void loadFromLevel(Level level) {
        fromIntLayout(level.getLayout());
    }
}