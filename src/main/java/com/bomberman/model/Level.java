package com.bomberman.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Représente un niveau du jeu Bomberman, incluant son nom, ses images et sa disposition.
 */
public class Level {
    private final String name;
    private final String groundImagePath;
    private final String wallIndestructibleImagePath;
    private final String wallDestructibleImagePath;
    private final int[][] layout; // 0: sol, 1: mur indestructible, 2: destructible

    /**
     * Construit un niveau avec ses propriétés.
     * @param name nom du niveau
     * @param groundImagePath chemin de l'image du sol
     * @param wallIndestructibleImagePath chemin image mur indestructible
     * @param wallDestructibleImagePath chemin image mur destructible
     * @param layout disposition du niveau
     */
    public Level(String name, String groundImagePath, String wallIndestructibleImagePath, String wallDestructibleImagePath, int[][] layout) {
        this.name = name;
        this.groundImagePath = normalizeResourcePath(groundImagePath);
        this.wallIndestructibleImagePath = normalizeResourcePath(wallIndestructibleImagePath);
        this.wallDestructibleImagePath = normalizeResourcePath(wallDestructibleImagePath);
        this.layout = layout;
    }

    /** @return nom du niveau */
    public String getName() { return name; }
    /** @return chemin image sol */
    public String getGroundImagePath() { return groundImagePath; }
    /** @return chemin image mur indestructible */
    public String getWallIndestructibleImagePath() { return wallIndestructibleImagePath; }
    /** @return chemin image mur destructible */
    public String getWallDestructibleImagePath() { return wallDestructibleImagePath; }
    /** @return disposition du niveau */
    public int[][] getLayout() { return layout; }

    /**
     * Crée un niveau à partir d'un fichier .level.
     * @param file chemin du fichier
     * @return le niveau chargé
     * @throws IOException en cas d'erreur de lecture
     */
    public static Level fromFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        String name = "", ground = "", ind = "", des = "";
        int layoutStart = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("name:")) name = line.substring(5).trim();
            else if (line.startsWith("groundImage:")) ground = line.substring(12).trim();
            else if (line.startsWith("wallIndestructibleImage:")) ind = line.substring(25).trim();
            else if (line.startsWith("wallDestructibleImage:")) des = line.substring(22).trim();
            else if (line.startsWith("layout:")) { layoutStart = i + 1; break; }
        }
        if (layoutStart < 0) throw new IOException("Fichier de niveau invalide (pas de layout)");
        int rows = lines.size() - layoutStart;
        int cols = lines.get(layoutStart).trim().length();
        int[][] layout = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            String l = lines.get(layoutStart + r).trim();
            for (int c = 0; c < cols; c++) {
                layout[r][c] = l.charAt(c) - '0';
            }
        }
        return new Level(name, ground, ind, des, layout);
    }

    /**
     * Sauvegarde le niveau dans un fichier .level.
     * @param file chemin du fichier de destination
     * @throws IOException en cas d'erreur d'écriture
     */
    public void saveToFile(Path file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write("name: " + name + "\n");
            w.write("groundImage: " + groundImagePath + "\n");
            w.write("wallIndestructibleImage: " + wallIndestructibleImagePath + "\n");
            w.write("wallDestructibleImage: " + wallDestructibleImagePath + "\n");
            w.write("layout:\n");
            for (int[] row : layout) {
                for (int cell : row) w.write(Integer.toString(cell));
                w.write("\n");
            }
        }
    }

    /**
     * Charge tous les niveaux d'un dossier donné.
     * @param dir dossier contenant les fichiers .level
     * @return liste des niveaux chargés
     * @throws IOException en cas de problème de lecture
     */
    public static List<Level> loadLevelsFromDirectory(Path dir) throws IOException {
        List<Level> levels = new ArrayList<>();
        if (!Files.exists(dir)) return levels;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.level")) {
            for (Path f : stream) {
                try {
                    levels.add(Level.fromFile(f));
                } catch (Exception ignored) {
                    // Ignorer les fichiers invalides, aucun message de debug n'est affiché
                }
            }
        }
        return levels;
    }

    /**
     * Normalise le chemin d'une ressource image.
     * @param path chemin initial
     * @return chemin normalisé commençant par /
     */
    private static String normalizeResourcePath(String path) {
        if (path == null) return null;
        String p = path.startsWith("/") ? path : "/" + path;
        p = p.replaceAll("\\.PNG$", ".png");
        p = p.replace("BonhommeNeige", "bonhommeNeige");
        p = p.replace("MurDestructible", "murDestructible");
        p = p.replace("MurIndestructible", "murIndestructible");
        p = p.replace("FondFeuilles", "fondFeuilles");
        return p;
    }
}