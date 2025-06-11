package com.bomberman.model;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Singleton pour la gestion des comptes utilisateurs avec persistance sur fichier texte.
 */
public class UserManager {
    private static final String USERS_FILE = "users.txt";
    private static UserManager instance;
    private final Map<String, User> users = new HashMap<>();
    private User currentUser;

    private UserManager() {
        loadUsers();
    }

    /**
     * Retourne l'instance unique du gestionnaire d'utilisateurs.
     *
     * @return instance de UserManager
     */
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Charge les utilisateurs depuis le fichier de sauvegarde.
     */
    private void loadUsers() {
        Path path = Paths.get(USERS_FILE);
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (!line.isBlank()) {
                        User user = User.fromString(line);
                        if (user != null) {
                            users.put(user.getUsername(), user);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
            }
        }
    }

    /**
     * Sauvegarde tous les utilisateurs dans le fichier.
     */
    private void saveUsers() {
        try {
            List<String> lines = new ArrayList<>();
            for (User user : users.values()) {
                lines.add(user.toString());
            }
            Files.write(Paths.get(USERS_FILE), lines);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des utilisateurs : " + e.getMessage());
        }
    }

    /**
     * Crée un nouveau compte utilisateur.
     *
     * @param username nom d'utilisateur
     * @param password mot de passe
     * @return true si l'utilisateur a été créé, false si déjà existant ou champs invalides
     */
    public boolean createAccount(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank() || users.containsKey(username)) {
            return false;
        }
        users.put(username, new User(username, password));
        saveUsers();
        return true;
    }

    /**
     * Authentifie un utilisateur.
     *
     * @param username nom d'utilisateur
     * @param password mot de passe
     * @return true si la connexion réussit, false sinon
     */
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    /**
     * Déconnecte l'utilisateur actuellement connecté.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Retourne l'utilisateur actuellement connecté.
     *
     * @return utilisateur connecté ou null
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Indique si un utilisateur est actuellement connecté.
     *
     * @return true si connecté, false sinon
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Vérifie si un nom d'utilisateur existe déjà.
     *
     * @param username nom d'utilisateur
     * @return true si l'utilisateur existe
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Met à jour les statistiques de l'utilisateur connecté, puis sauvegarde.
     *
     * @param won   true si la partie est gagnée
     * @param score score à ajouter
     */
    public void updateCurrentUserStats(boolean won, int score) {
        if (currentUser != null) {
            currentUser.incrementGamesPlayed();
            if (won) {
                currentUser.incrementGamesWon();
            }
            currentUser.addScore(score);
            saveUsers();
        }
    }
}