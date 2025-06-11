package com.bomberman.controller;

import com.bomberman.model.GameSettings;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Contrôleur du menu des paramètres.
 * Gère la navigation, le choix du niveau d'IA, et l'application du style.
 */
public class SettingsController {

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;
    @FXML private Button levelEditorButton;
    @FXML private Button backButton;

    @FXML private HBox aiLevelBox;
    @FXML private Label aiLevelTextLabel;
    @FXML private Label aiLevelLeftArrow;
    @FXML private Label aiLevelLabel;
    @FXML private Label aiLevelRightArrow;

    private final IntegerProperty aiLevelIndex = GameSettings.aiLevelIndexProperty();
    private int selectedField = 0; // 0 : levelEditor, 1 : AI, 2 : retour

    private Stage stage;

    /**
     * Définit la fenêtre principale et applique la taille et le centrage.
     * @param stage fenêtre principale JavaFX
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
        Platform.runLater(this::loadStylesheet);
    }

    /**
     * Initialise le contrôleur et les liaisons de l'UI.
     */
    @FXML
    public void initialize() {
        loadBackgroundImage();

        backButton.setOnAction(e -> returnToMenu());
        levelEditorButton.setOnAction(e -> openLevelEditor());

        // Liaison du label de niveau d'IA à la propriété globale
        aiLevelLabel.textProperty().bind(aiLevelIndex.asString().map(idx -> GameSettings.AI_LEVELS[Integer.parseInt(idx)]));

        aiLevelLeftArrow.setOnMouseClicked(e -> selectFieldAndAdjustAILevel(1, this::decrementAiLevel));
        aiLevelRightArrow.setOnMouseClicked(e -> selectFieldAndAdjustAILevel(1, this::incrementAiLevel));
        aiLevelBox.setOnMouseEntered(e -> selectField(1));
        levelEditorButton.setOnMouseEntered(e -> { selectField(0); levelEditorButton.requestFocus(); });
        backButton.setOnMouseEntered(e -> { selectField(2); backButton.requestFocus(); });

        aiLevelLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleArrowKey);
            }
        });

        aiLevelLeftArrow.visibleProperty().bind(aiLevelIndex.greaterThan(0));
        aiLevelRightArrow.visibleProperty().bind(aiLevelIndex.lessThan(GameSettings.AI_LEVELS.length - 1));

        updateAILevelHighlight();
    }

    /**
     * Sélectionne un champ et ajuste éventuellement le niveau d'IA.
     * @param fieldIndex index du champ sélectionné
     * @param aiLevelAction action d'ajustement du niveau d'IA (peut être null)
     */
    private void selectFieldAndAdjustAILevel(int fieldIndex, Runnable aiLevelAction) {
        selectedField = fieldIndex;
        updateAILevelHighlight();
        if (aiLevelAction != null) aiLevelAction.run();
    }

    /**
     * Sélectionne un champ et met à jour la surbrillance.
     * @param fieldIndex index du champ sélectionné
     */
    private void selectField(int fieldIndex) {
        selectedField = fieldIndex;
        updateAILevelHighlight();
    }

    /**
     * Met à jour la surbrillance du champ actuellement sélectionné.
     */
    private void updateAILevelHighlight() {
        aiLevelTextLabel.getStyleClass().removeAll("menu-highlighted");
        aiLevelLabel.getStyleClass().removeAll("value-highlighted");
        levelEditorButton.getStyleClass().remove("menu-highlighted");
        backButton.getStyleClass().remove("menu-highlighted");
        switch (selectedField) {
            case 1 -> {
                aiLevelTextLabel.getStyleClass().add("menu-highlighted");
                aiLevelLabel.getStyleClass().add("value-highlighted");
            }
            case 0 -> levelEditorButton.getStyleClass().add("menu-highlighted");
            case 2 -> backButton.getStyleClass().add("menu-highlighted");
            default -> {}
        }
    }

    /**
     * Gère la navigation et l'activation au clavier.
     * @param event événement clavier à traiter
     */
    private void handleArrowKey(KeyEvent event) {
        switch (event.getCode()) {
            case UP -> moveSelection(-1);
            case DOWN -> moveSelection(1);
            case LEFT -> { if (selectedField == 1) decrementAiLevel(); }
            case RIGHT -> { if (selectedField == 1) incrementAiLevel(); }
            case ENTER, SPACE -> activateSelectedField();
            default -> {}
        }
        event.consume();
    }

    /**
     * Change le champ sélectionné de manière cyclique.
     * @param delta direction (+1 pour bas, -1 pour haut)
     */
    private void moveSelection(int delta) {
        selectedField = (selectedField + delta + 3) % 3;
        updateAILevelHighlight();
        focusSelectedField();
    }

    /**
     * Met le focus sur le champ actuellement sélectionné.
     */
    private void focusSelectedField() {
        switch (selectedField) {
            case 0 -> levelEditorButton.requestFocus();
            case 1 -> aiLevelBox.requestFocus();
            case 2 -> backButton.requestFocus();
            default -> {}
        }
    }

    /**
     * Active l'action associée au champ actuellement sélectionné.
     */
    private void activateSelectedField() {
        switch (selectedField) {
            case 0 -> openLevelEditor();
            case 2 -> returnToMenu();
            default -> {}
        }
    }

    /**
     * Décrémente le niveau d'IA.
     */
    private void decrementAiLevel() {
        if (aiLevelIndex.get() > 0) {
            aiLevelIndex.set(aiLevelIndex.get() - 1);
        }
    }

    /**
     * Incrémente le niveau d'IA.
     */
    private void incrementAiLevel() {
        if (aiLevelIndex.get() < GameSettings.AI_LEVELS.length - 1) {
            aiLevelIndex.set(aiLevelIndex.get() + 1);
        }
    }

    /**
     * Charge l'image de fond du menu des paramètres.
     */
    private void loadBackgroundImage() {
        java.net.URL url = getClass().getResource("/images/menu/Bomber_fond.jpg");
        if (url != null) {
            Image image = new Image(url.toExternalForm());
            backgroundImage.setImage(image);
            backgroundImage.setFitWidth(800);
            backgroundImage.setFitHeight(600);
            backgroundImage.setPreserveRatio(false);
        } else {
            System.err.println("Image de fond non trouvée : /images/menu/Bomber_fond.jpg");
        }
    }

    /**
     * Charge et applique la feuille de style du menu des paramètres.
     */
    private void loadStylesheet() {
        java.net.URL cssUrl = getClass().getResource("/css/settings-menu.css");
        if (cssUrl != null && stage.getScene() != null) {
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    /**
     * Retourne à l'écran du menu principal.
     */
    private void returnToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/menu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            java.net.URL cssUrl = getClass().getResource("/css/settings-menu.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            MenuController menuController = loader.getController();
            stage.setScene(scene);
            menuController.setStage(stage);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du menu principal : " + e.getMessage());
        }
    }

    /**
     * Ouvre l'écran de l'éditeur de niveaux.
     */
    private void openLevelEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/level-editor.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            java.net.URL cssUrl = getClass().getResource("/css/settings-menu.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            Object ctrl = loader.getController();
            if (ctrl instanceof LevelEditorController lec) {
                lec.setStage(stage);
            }
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'éditeur de niveaux : " + e.getMessage());
        }
    }
}