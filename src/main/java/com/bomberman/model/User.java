package com.bomberman.model;

/**
 * Représente un utilisateur du jeu Bomberman, avec ses informations de compte et statistiques.
 */
public class User {
    private final String username;
    private final String password;
    private int gamesPlayed;
    private int gamesWon;
    private int totalScore;

    /**
     * Crée un utilisateur avec un nom et un mot de passe, sans statistiques initiales.
     * @param username nom d'utilisateur
     * @param password mot de passe
     */
    public User(String username, String password) {
        this(username, password, 0, 0, 0);
    }

    /**
     * Crée un utilisateur complet avec statistiques.
     * @param username nom d'utilisateur
     * @param password mot de passe
     * @param gamesPlayed parties jouées
     * @param gamesWon parties gagnées
     * @param totalScore score total
     */
    public User(String username, String password, int gamesPlayed, int gamesWon, int totalScore) {
        this.username = username;
        this.password = password;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.totalScore = totalScore;
    }

    /** @return le nom d'utilisateur */
    public String getUsername() {
        return username;
    }

    /** @return le mot de passe (en clair) */
    public String getPassword() {
        return password;
    }

    /** @return le nombre de parties jouées */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /** @return le nombre de parties gagnées */
    public int getGamesWon() {
        return gamesWon;
    }

    /** @return le score total */
    public int getTotalScore() {
        return totalScore;
    }

    /** Incrémente le nombre de parties jouées. */
    public void incrementGamesPlayed() {
        gamesPlayed++;
    }

    /** Incrémente le nombre de parties gagnées. */
    public void incrementGamesWon() {
        gamesWon++;
    }

    /**
     * Ajoute un score au score total.
     * @param score nombre de points à ajouter
     */
    public void addScore(int score) {
        totalScore += score;
    }

    /**
     * Calcule le taux de victoire en pourcentage.
     * @return taux de victoire (0.0 si aucune partie jouée)
     */
    public double getWinRate() {
        return gamesPlayed == 0 ? 0.0 : (double) gamesWon / gamesPlayed * 100.0;
    }

    /**
     * Sérialise l'utilisateur en chaîne pour la persistance.
     * Format: username,password,gamesPlayed,gamesWon,totalScore
     */
    @Override
    public String toString() {
        return String.format("%s,%s,%d,%d,%d", username, password, gamesPlayed, gamesWon, totalScore);
    }

    /**
     * Crée un utilisateur à partir d'une ligne sérialisée.
     * @param line chaîne issue du fichier utilisateur
     * @return User ou null si format incorrect
     */
    public static User fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length == 5) {
            try {
                return new User(
                        parts[0],
                        parts[1],
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4])
                );
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}