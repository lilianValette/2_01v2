package com.bomberman.controller;

import com.bomberman.model.User;
import com.bomberman.model.UserManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Contrôleur de la gestion de compte utilisateur (connexion, création, profil).
 */
public class AccountController {
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;
    @FXML private StackPane loginContainer;
    @FXML private StackPane createContainer;
    @FXML private StackPane profileContainer;
    @FXML private Button showCreateButton;
    @FXML private Button showLoginButton;
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;
    @FXML private TextField createUsername;
    @FXML private PasswordField createPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Button createButton;
    @FXML private Label createMessage;
    @FXML private Label profileUsername;
    @FXML private Label profileGamesPlayed;
    @FXML private Label profileGamesWon;
    @FXML private Label profileWinRate;
    @FXML private Label profileTotalScore;
    @FXML private Button logoutButton;
    @FXML private Button backButton;

    private Stage stage;
    private UserManager userManager;

    /**
     * Définit la fenêtre principale et applique la taille fixe.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setMaxWidth(800);
        stage.setMaxHeight(600);
        stage.setResizable(false);
        stage.centerOnScreen();

        Platform.runLater(() -> {
            loadStylesheet();
            setupButtonStyles();
        });
    }

    @FXML
    public void initialize() {
        userManager = UserManager.getInstance();
        loadBackgroundImage();

        loginButton.setOnAction(e -> handleLogin());
        createButton.setOnAction(e -> handleCreateAccount());
        logoutButton.setOnAction(e -> handleLogout());
        backButton.setOnAction(e -> returnToMenu());
        showCreateButton.setOnAction(e -> showCreateView());
        showLoginButton.setOnAction(e -> showLoginView());

        updateUI();

        createUsername.textProperty().addListener((obs, oldVal, newVal) -> validateCreateForm());
        createPassword.textProperty().addListener((obs, oldVal, newVal) -> validateCreateForm());
        confirmPassword.textProperty().addListener((obs, oldVal, newVal) -> validateCreateForm());

        Platform.runLater(this::setupButtonStyles);
    }

    /**
     * Charge le fichier CSS principal du jeu.
     */
    private void loadStylesheet() {
        try {
            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null && stage.getScene() != null) {
                String cssPath = cssUrl.toExternalForm();
                stage.getScene().getStylesheets().clear();
                stage.getScene().getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS : " + e.getMessage());
        }
    }

    /**
     * Applique les classes CSS personnalisées aux boutons.
     */
    private void setupButtonStyles() {
        Platform.runLater(() -> {
            setupButtonStyle(loginButton, "account-button");
            setupButtonStyle(createButton, "account-button");
            setupButtonStyle(showCreateButton, "account-button");
            setupButtonStyle(showLoginButton, "account-button");
            setupButtonStyle(logoutButton, "game-button-danger");
            setupButtonStyle(backButton, "game-button-secondary");
            if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                stage.getScene().getRoot().applyCss();
            }
        });
    }

    /**
     * Applique la classe CSS à un bouton.
     */
    private void setupButtonStyle(Button button, String styleClass) {
        if (button != null) {
            button.getStyleClass().removeAll("button", "account-button", "game-button-danger", "game-button-secondary");
            button.getStyleClass().add(styleClass);
            button.applyCss();
        }
    }

    /**
     * Charge l'image de fond du compte.
     */
    private void loadBackgroundImage() {
        try {
            java.net.URL url = getClass().getResource("/images/menu/Bomber_fond.jpg");
            if (url != null) {
                Image image = new Image(url.toExternalForm());
                backgroundImage.setImage(image);
                backgroundImage.setFitWidth(800);
                backgroundImage.setFitHeight(600);
                backgroundImage.setPreserveRatio(false);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image de fond : " + e.getMessage());
        }
    }

    /**
     * Affiche le formulaire de connexion.
     */
    private void showLoginView() {
        setContainerVisibility(true, false, false);
        clearMessages();
        clearLoginForm();
    }

    /**
     * Affiche le formulaire de création de compte.
     */
    private void showCreateView() {
        setContainerVisibility(false, true, false);
        clearMessages();
        clearCreateForm();
    }

    /**
     * Affiche le profil utilisateur connecté.
     */
    private void showProfileView() {
        setContainerVisibility(false, false, true);
        updateProfileInfo();
    }

    /**
     * Met à jour la visibilité des différents conteneurs.
     */
    private void setContainerVisibility(boolean login, boolean create, boolean profile) {
        loginContainer.setVisible(login);
        createContainer.setVisible(create);
        profileContainer.setVisible(profile);
    }

    /**
     * Traite la connexion utilisateur.
     * Tous les textes affichés à l'utilisateur sont en anglais.
     */
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginMessage("Please fill in all fields.", true);
            return;
        }

        loginButton.setDisable(true);

        if (userManager.login(username, password)) {
            showLoginMessage("Login successful! Welcome " + username, false);
            Task<Void> delayTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(1000);
                    return null;
                }
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        updateUI();
                        loginButton.setDisable(false);
                    });
                }
            };
            new Thread(delayTask).start();
        } else {
            showLoginMessage("Incorrect username or password.", true);
            loginButton.setDisable(false);
        }
    }

    /**
     * Traite la création d'un nouveau compte utilisateur.
     * Tous les textes affichés à l'utilisateur sont en anglais.
     */
    private void handleCreateAccount() {
        String username = createUsername.getText().trim();
        String password = createPassword.getText();
        String confirm = confirmPassword.getText();

        if (!validateAccountCreation(username, password, confirm)) {
            return;
        }

        createButton.setDisable(true);

        if (userManager.createAccount(username, password)) {
            showCreateMessage("Account created successfully! You can now log in.", false);
            Task<Void> delayTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(1500);
                    return null;
                }
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        showLoginView();
                        createButton.setDisable(false);
                    });
                }
            };
            new Thread(delayTask).start();
        } else {
            showCreateMessage("This username is already taken.", true);
            createButton.setDisable(false);
        }
    }

    /**
     * Valide les champs de création de compte.
     * Tous les textes affichés à l'utilisateur sont en anglais.
     */
    private boolean validateAccountCreation(String username, String password, String confirm) {
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showCreateMessage("Please fill in all fields.", true);
            return false;
        }

        if (username.length() < 3) {
            showCreateMessage("Username must be at least 3 characters long.", true);
            return false;
        }

        if (password.length() < 4) {
            showCreateMessage("Password must be at least 4 characters long.", true);
            return false;
        }

        if (!password.equals(confirm)) {
            showCreateMessage("Passwords do not match.", true);
            return false;
        }

        return true;
    }

    /**
     * Déconnecte l'utilisateur courant.
     * Texte affiché à l'utilisateur en anglais.
     */
    private void handleLogout() {
        userManager.logout();
        clearAllForms();
        updateUI();
        showLoginMessage("You have been logged out.", false);
    }

    /**
     * Validation en temps réel pour le formulaire de création.
     */
    private void validateCreateForm() {
        String username = createUsername.getText().trim();
        String password = createPassword.getText();
        String confirm = confirmPassword.getText();

        boolean isValid = username.length() >= 3 &&
                password.length() >= 4 &&
                password.equals(confirm) &&
                !confirm.isEmpty();

        createButton.setDisable(!isValid);
    }

    /**
     * Met à jour l'affichage selon l'état de connexion.
     */
    private void updateUI() {
        boolean isLoggedIn = userManager.isLoggedIn();

        if (isLoggedIn) {
            showProfileView();
        } else {
            showLoginView();
        }
    }

    /**
     * Met à jour les informations du profil utilisateur.
     */
    private void updateProfileInfo() {
        User user = userManager.getCurrentUser();
        if (user != null) {
            profileUsername.setText(user.getUsername());
            profileGamesPlayed.setText(String.valueOf(user.getGamesPlayed()));
            profileGamesWon.setText(String.valueOf(user.getGamesWon()));
            profileWinRate.setText(String.format("%.1f%%", user.getWinRate()));
            profileTotalScore.setText(String.valueOf(user.getTotalScore()));
        }
    }

    /**
     * Affiche un message dans le formulaire de connexion.
     * @param message Texte en anglais à afficher.
     * @param isError true si c'est une erreur.
     */
    private void showLoginMessage(String message, boolean isError) {
        setMessageStyle(loginMessage, message, isError);
    }

    /**
     * Affiche un message dans le formulaire de création.
     * @param message Texte en anglais à afficher.
     * @param isError true si c'est une erreur.
     */
    private void showCreateMessage(String message, boolean isError) {
        setMessageStyle(createMessage, message, isError);
    }

    /**
     * Applique le style d'un message (succès ou erreur).
     */
    private void setMessageStyle(Label messageLabel, String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().clear();
        if (isError) {
            messageLabel.getStyleClass().add("error-message");
        } else {
            messageLabel.getStyleClass().add("success-message");
        }
    }

    /**
     * Efface tous les messages d'information.
     */
    private void clearMessages() {
        clearMessage(loginMessage);
        clearMessage(createMessage);
    }

    /**
     * Efface le message d'un label.
     */
    private void clearMessage(Label messageLabel) {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("success-message", "error-message");
    }

    /**
     * Efface le formulaire de connexion.
     */
    private void clearLoginForm() {
        loginUsername.clear();
        loginPassword.clear();
    }

    /**
     * Efface le formulaire de création.
     */
    private void clearCreateForm() {
        createUsername.clear();
        createPassword.clear();
        confirmPassword.clear();
        clearMessage(createMessage);
        createButton.setDisable(true);
    }

    /**
     * Efface tous les formulaires et messages.
     */
    private void clearAllForms() {
        clearLoginForm();
        clearCreateForm();
        clearMessages();
    }

    /**
     * Retour au menu principal.
     */
    private void returnToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/menu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                String cssPath = cssUrl.toExternalForm();
                scene.getStylesheets().add(cssPath);
            }

            MenuController menuController = loader.getController();
            stage.setScene(scene);
            menuController.setStage(stage);

        } catch (Exception ex) {
            System.err.println("Erreur lors du retour au menu : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}