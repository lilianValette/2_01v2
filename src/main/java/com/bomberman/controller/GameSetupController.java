package com.bomberman.controller;

import com.bomberman.model.AIDifficulty;
import com.bomberman.model.GameSettings;
import com.bomberman.model.Level;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur de l'écran de configuration de partie.
 * Permet de sélectionner le nombre de joueurs, d'IAs, le niveau et de lancer la partie.
 * Tous les messages d'erreur et commentaires sont en français, l'interface utilisateur reste en anglais.
 */
public class GameSetupController {

    @FXML private StackPane themePreviewPane;
    @FXML private Label backButton;
    @FXML private Label
            playerTextLabel, iaTextLabel, themeTextLabel, playTextLabel,
            playerLeftArrow, playerRightArrow,
            iaLeftArrow, iaRightArrow,
            themeLeftArrow, themeRightArrow,
            playerCountLabel, iaCountLabel, themeLabel;

    private final IntegerProperty playerCount = new SimpleIntegerProperty(2);
    private final IntegerProperty iaCount = new SimpleIntegerProperty(0);
    private final IntegerProperty levelIndex = new SimpleIntegerProperty(0);
    private final Level[] levels = loadAllLevels();

    private Stage stage;
    private int selectedField = 0; // 0 = joueur, 1 = IA, 2 = niveau, 3 = JOUER, 4 = RETOUR

    /**
     * Charge tous les niveaux présents dans les dossiers predefined et custom.
     */
    private Level[] loadAllLevels() {
        List<Level> allLevels = new ArrayList<>();
        try {
            allLevels.addAll(Level.loadLevelsFromDirectory(
                    Path.of("src/main/resources/levels/predefined")
            ));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des niveaux prédéfinis : " + e.getMessage());
        }
        try {
            allLevels.addAll(Level.loadLevelsFromDirectory(
                    Path.of("src/main/resources/levels/custom")
            ));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des niveaux personnalisés : " + e.getMessage());
        }
        return allLevels.toArray(new Level[0]);
    }

    /**
     * Définit la fenêtre principale et adapte sa taille au canvas de preview.
     * @param stage La fenêtre principale JavaFX
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        Platform.runLater(this::adaptStageToPreview);
    }

    /**
     * Initialise l'écran de configuration de partie.
     */
    @FXML
    public void initialize() {
        updateUI();
        setupArrowsVisibility();
        playTextLabel.setOnMouseClicked(e -> startGame());
        playTextLabel.setOnMouseEntered(e -> { selectedField = 3; updateHighlight(); });

        backButton.setOnMouseClicked(e -> goBackToMenu());
        backButton.setOnMouseEntered(e -> { selectedField = 4; updateHighlight(); });

        playerLeftArrow.setOnMouseClicked(e -> { selectedField = 0; decrementSelected(); });
        playerRightArrow.setOnMouseClicked(e -> { selectedField = 0; incrementSelected(); });
        iaLeftArrow.setOnMouseClicked(e -> { selectedField = 1; decrementSelected(); });
        iaRightArrow.setOnMouseClicked(e -> { selectedField = 1; incrementSelected(); });
        themeLeftArrow.setOnMouseClicked(e -> { selectedField = 2; decrementSelected(); });
        themeRightArrow.setOnMouseClicked(e -> { selectedField = 2; incrementSelected(); });

        playerCountLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleArrowKey);
            }
        });
    }

    /**
     * Gère la visibilité des flèches de sélection selon les valeurs courantes.
     */
    private void setupArrowsVisibility() {
        playerLeftArrow.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> playerCount.get() > (iaCount.get() == 0 ? 2 : 1),
                playerCount, iaCount
        ));
        playerRightArrow.visibleProperty().bind(playerCount.lessThan(2));
        iaLeftArrow.visibleProperty().bind(iaCount.greaterThan(0));
        iaRightArrow.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> playerCount.get() + iaCount.get() < 4,
                playerCount, iaCount
        ));
        boolean hasMultipleLevels = levels.length > 1;
        themeLeftArrow.setVisible(hasMultipleLevels);
        themeRightArrow.setVisible(hasMultipleLevels);
    }

    /**
     * Met à jour la surbrillance du champ sélectionné.
     */
    private void updateHighlight() {
        playerTextLabel.getStyleClass().removeAll("menu-highlighted");
        iaTextLabel.getStyleClass().removeAll("menu-highlighted");
        themeTextLabel.getStyleClass().removeAll("menu-highlighted");
        playTextLabel.getStyleClass().removeAll("menu-highlighted");
        backButton.getStyleClass().removeAll("back-highlighted");
        playerCountLabel.getStyleClass().removeAll("value-highlighted");
        iaCountLabel.getStyleClass().removeAll("value-highlighted");
        themeLabel.getStyleClass().removeAll("value-highlighted");

        switch (selectedField) {
            case 0 -> {
                playerTextLabel.getStyleClass().add("menu-highlighted");
                playerCountLabel.getStyleClass().add("value-highlighted");
            }
            case 1 -> {
                iaTextLabel.getStyleClass().add("menu-highlighted");
                iaCountLabel.getStyleClass().add("value-highlighted");
            }
            case 2 -> {
                themeTextLabel.getStyleClass().add("menu-highlighted");
                themeLabel.getStyleClass().add("value-highlighted");
            }
            case 3 -> playTextLabel.getStyleClass().add("menu-highlighted");
            case 4 -> backButton.getStyleClass().add("back-highlighted");
        }
    }

    /**
     * Met à jour l'affichage des labels et du preview.
     */
    private void updateUI() {
        if (levels.length == 0) {
            themeLabel.setText("AUCUN NIVEAU");
            themePreviewPane.getChildren().clear();
            return;
        }
        playerCountLabel.setText(String.valueOf(playerCount.get()));
        iaCountLabel.setText(String.valueOf(iaCount.get()));
        themeLabel.setText(levels[levelIndex.get()].getName().toUpperCase());
        showLevelPreview(levels[levelIndex.get()]);
        updateHighlight();
    }

    /**
     * Affiche le canvas de prévisualisation du niveau sélectionné.
     */
    private void showLevelPreview(Level level) {
        themePreviewPane.getChildren().clear();
        Canvas preview = GameController.createLevelPreviewCanvas(level, GameController.getCellSize());

        preview.widthProperty().addListener((obs, oldW, newW) -> adaptStageToPreview());
        preview.heightProperty().addListener((obs, oldH, newH) -> adaptStageToPreview());

        StackPane.setAlignment(preview, javafx.geometry.Pos.CENTER);
        StackPane.setMargin(preview, javafx.geometry.Insets.EMPTY);

        themePreviewPane.getChildren().add(0, preview);

        Platform.runLater(this::adaptStageToPreview);
    }

    /**
     * Adapte la fenêtre à la taille du canvas de preview.
     */
    private void adaptStageToPreview() {
        if (stage != null && !themePreviewPane.getChildren().isEmpty()) {
            Canvas preview = (Canvas) themePreviewPane.getChildren().get(0);
            double w = preview.getWidth();
            double h = preview.getHeight();
            double fudge = 0.5;
            if (w > 0 && h > 0) {
                stage.setMinWidth(w + fudge);
                stage.setMinHeight(h + fudge);
                stage.setMaxWidth(w + fudge);
                stage.setMaxHeight(h + fudge);
                stage.setWidth(w + fudge);
                stage.setHeight(h + fudge);
                stage.setResizable(false);
            }
        }
    }

    /**
     * Retourne au menu principal.
     */
    private void goBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/menu.fxml"));
            Parent root = loader.load();
            MenuController menuController = loader.getController();
            menuController.setStage(stage);
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.err.println("Erreur lors du retour au menu : " + e.getMessage());
        }
    }

    /**
     * Démarre la partie avec les paramètres sélectionnés.
     */
    private void startGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/game-view.fxml"));
            Parent root = loader.load();
            GameController gameController = loader.getController();
            gameController.setStage(stage);
            gameController.setLevel(levels[levelIndex.get()]);
            gameController.setPlayerCount(playerCount.get());
            gameController.setIaCount(iaCount.get());
            gameController.setAIDifficulty(GameSettings.getSelectedAIDifficulty());
            gameController.startGame();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            System.err.println("Erreur lors du lancement de la partie : " + ex.getMessage());
        }
    }

    /**
     * Gère les actions clavier pour la navigation et la sélection.
     */
    private void handleArrowKey(KeyEvent event) {
        switch (event.getCode()) {
            case UP    -> { selectedField = (selectedField + 4) % 5; updateHighlight(); }
            case DOWN  -> { selectedField = (selectedField + 1) % 5; updateHighlight(); }
            case LEFT  -> { if (selectedField < 3) decrementSelected(); }
            case RIGHT -> { if (selectedField < 3) incrementSelected(); }
            case ENTER, SPACE -> {
                if (selectedField == 3) startGame();
                else if (selectedField == 4) goBackToMenu();
            }
            case ESCAPE -> goBackToMenu();
            default -> { return; }
        }
        event.consume();
    }

    /**
     * Décrémente la valeur du champ sélectionné (joueur, IA, niveau).
     */
    private void decrementSelected() {
        switch (selectedField) {
            case 0 -> {
                int minPlayer = (iaCount.get() == 0) ? 2 : 1;
                if (playerCount.get() > minPlayer) {
                    playerCount.set(playerCount.get() - 1);
                    updateUI();
                }
            }
            case 1 -> {
                if (iaCount.get() > 0) {
                    iaCount.set(iaCount.get() - 1);
                    if (iaCount.get() == 0 && playerCount.get() < 2) {
                        playerCount.set(2);
                    }
                    updateUI();
                }
            }
            case 2 -> {
                if (levels.length == 0) return;
                levelIndex.set((levelIndex.get() - 1 + levels.length) % levels.length);
                updateUI();
            }
        }
    }

    /**
     * Incrémente la valeur du champ sélectionné (joueur, IA, niveau).
     */
    private void incrementSelected() {
        switch (selectedField) {
            case 0 -> {
                if (playerCount.get() < 2 && playerCount.get() + iaCount.get() < 4) {
                    playerCount.set(playerCount.get() + 1);
                    updateUI();
                }
            }
            case 1 -> {
                if (playerCount.get() + iaCount.get() < 4) {
                    iaCount.set(iaCount.get() + 1);
                    updateUI();
                }
            }
            case 2 -> {
                if (levels.length == 0) return;
                levelIndex.set((levelIndex.get() + 1) % levels.length);
                updateUI();
            }
        }
    }
}