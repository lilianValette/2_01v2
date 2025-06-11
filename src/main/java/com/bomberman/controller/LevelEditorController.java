package com.bomberman.controller;

import com.bomberman.model.Level;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Contrôleur de l'éditeur de niveaux.
 * Permet la création, la modification et la sauvegarde de niveaux personnalisés.
 * Les textes d'interface utilisateur sont en anglais, les commentaires et erreurs en français.
 */
public class LevelEditorController {
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;
    @FXML private Button clearButton, saveButton, loadButton, backButton;
    @FXML private ToggleButton emptyButton, wallButton, breakableButton;
    @FXML private GridPane gridPane;

    @FXML private ImageView groundPreview, indestructiblePreview, destructiblePreview;
    @FXML private Button chooseGroundBtn, chooseIndestructibleBtn, chooseDestructibleBtn;

    private ToggleGroup paletteGroup;
    private Stage stage;
    private static final int COLUMNS = 15;
    private static final int ROWS = 13;
    private static final int CELL_SIZE = 35;

    private String groundImagePath = "/images/elementsMap/herbe.png";
    private String wallIndestructibleImagePath = "/images/elementsMap/murIndestructible.png";
    private String wallDestructibleImagePath = "/images/elementsMap/murDestructible.png";

    private Image cachedGroundImage, cachedIndestructibleImage, cachedDestructibleImage;
    private final char[][] gridData = new char[ROWS][COLUMNS];
    private final Button[][] gridButtons = new Button[ROWS][COLUMNS];

    private boolean isDrawing = false;
    private char currentType = ' ';

    /**
     * Définit la fenêtre principale, la taille et applique le CSS.
     * @param stage fenêtre principale JavaFX
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setWidth(900);
        stage.setHeight(700);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.setMaxWidth(900);
        stage.setMaxHeight(700);
        stage.setResizable(false);
        stage.centerOnScreen();
        Platform.runLater(this::loadStylesheet);
    }

    /**
     * Initialise l'éditeur de niveau.
     */
    @FXML
    public void initialize() {
        loadBackgroundImage();
        setupPalette();
        setupGrid();
        setupImageChoosers();
        updatePreviews();

        clearButton.setOnAction(e -> clearGrid());
        saveButton.setOnAction(e -> saveLevel());
        loadButton.setOnAction(e -> loadLevel());
        backButton.setOnAction(e -> returnToSettings());

        gridPane.setOnMouseReleased(e -> isDrawing = false);
        gridPane.setOnMouseExited(e -> isDrawing = false);
        gridPane.setOnMouseDragReleased(e -> isDrawing = false);
    }

    /**
     * Charge l'image de fond.
     */
    private void loadBackgroundImage() {
        try {
            java.net.URL url = getClass().getResource("/images/menu/Bomber_fond.jpg");
            if (url != null) {
                Image image = new Image(url.toExternalForm());
                backgroundImage.setImage(image);
                backgroundImage.setFitWidth(900);
                backgroundImage.setFitHeight(700);
                backgroundImage.setPreserveRatio(false);
            } else {
                System.err.println("Image de fond non trouvée : /images/menu/Bomber_fond.jpg");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du fond : " + e.getMessage());
        }
    }

    /**
     * Charge la feuille de style CSS pour l'éditeur.
     */
    private void loadStylesheet() {
        try {
            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null && stage.getScene() != null) {
                stage.getScene().getStylesheets().clear();
                stage.getScene().getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS : " + e.getMessage());
        }
    }

    /**
     * Met en place les boutons du choix de palette.
     */
    private void setupPalette() {
        paletteGroup = new ToggleGroup();
        emptyButton.setToggleGroup(paletteGroup);
        wallButton.setToggleGroup(paletteGroup);
        breakableButton.setToggleGroup(paletteGroup);
        paletteGroup.selectToggle(emptyButton);
    }

    /**
     * Met en place la grille et son comportement.
     */
    private void setupGrid() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        for (int col = 0; col < COLUMNS; col++) {
            ColumnConstraints cc = new ColumnConstraints(CELL_SIZE, CELL_SIZE, CELL_SIZE);
            gridPane.getColumnConstraints().add(cc);
        }
        for (int row = 0; row < ROWS; row++) {
            RowConstraints rc = new RowConstraints(CELL_SIZE, CELL_SIZE, CELL_SIZE);
            gridPane.getRowConstraints().add(rc);
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (gridData[row][col] == 0) gridData[row][col] = ' ';
                final int finalRow = row;
                final int finalCol = col;

                Button cell = new Button();
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setMaxSize(CELL_SIZE, CELL_SIZE);
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.getStyleClass().add("editor-grid-cell");
                updateCellVisual(cell, gridData[row][col]);
                cell.setFocusTraversable(false);

                cell.setOnMousePressed(e -> handleCellPressed(finalRow, finalCol, cell));
                cell.setOnDragDetected(e -> cell.startFullDrag());
                cell.setOnMouseDragEntered(e -> {
                    if (isDrawing) {
                        gridData[finalRow][finalCol] = currentType;
                        updateCellVisual(cell, currentType);
                    }
                });
                cell.setOnMouseReleased(e -> isDrawing = false);
                cell.setOnMouseDragReleased(e -> isDrawing = false);

                gridButtons[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
    }

    /**
     * Gère l'appui sur une cellule de la grille.
     */
    private void handleCellPressed(int row, int col, Button cell) {
        Toggle selected = paletteGroup.getSelectedToggle();
        currentType = ' ';
        if (selected == wallButton) currentType = '#';
        else if (selected == breakableButton) currentType = '%';
        isDrawing = true;
        gridData[row][col] = currentType;
        updateCellVisual(cell, currentType);
    }

    /**
     * Met à jour l'affichage d'une cellule selon son type.
     */
    private void updateCellVisual(Button cell, char type) {
        cell.setText("");
        Image img = switch (type) {
            case '#' -> cachedIndestructibleImage;
            case '%' -> cachedDestructibleImage;
            default -> cachedGroundImage;
        };
        ImageView icon = (img != null) ? new ImageView(img) : null;
        if (icon != null) {
            icon.setFitWidth(CELL_SIZE - 8);
            icon.setFitHeight(CELL_SIZE - 8);
            icon.setPreserveRatio(true);
        }
        cell.setGraphic(icon);
        cell.setStyle("-fx-background-color: #222; -fx-border-color: #FFD700; -fx-border-width: 1;");
    }

    /**
     * Vide la grille.
     */
    private void clearGrid() {
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLUMNS; col++)
                gridData[row][col] = ' ';
        refreshGridGraphics();
    }

    /**
     * Prépare les boutons de sélection d'image.
     */
    private void setupImageChoosers() {
        chooseGroundBtn.setOnAction(e -> chooseImage("Choisir une image de sol", path -> {
            groundImagePath = path;
            updatePreviews();
        }));
        chooseIndestructibleBtn.setOnAction(e -> chooseImage("Choisir une image de mur indestructible", path -> {
            wallIndestructibleImagePath = path;
            updatePreviews();
        }));
        chooseDestructibleBtn.setOnAction(e -> chooseImage("Choisir une image de mur destructible", path -> {
            wallDestructibleImagePath = path;
            updatePreviews();
        }));
    }

    /**
     * Met à jour les aperçus d'images et le cache.
     */
    private void updatePreviews() {
        setPreview(groundPreview, groundImagePath);
        setPreview(indestructiblePreview, wallIndestructibleImagePath);
        setPreview(destructiblePreview, wallDestructibleImagePath);
        reloadCachedImages();
        refreshGridGraphics();
    }

    /**
     * Met à jour l'aperçu d'un bouton.
     */
    private void setPreview(ImageView view, String path) {
        try {
            Image img = path.startsWith("/") && getClass().getResource(path) != null
                    ? new Image(getClass().getResource(path).toExternalForm(), 32, 32, true, true, true)
                    : new Image("file:" + path, 32, 32, true, true, true);
            view.setImage(img);
        } catch (Exception e) {
            view.setImage(null);
        }
    }

    /**
     * Recharge les images en cache.
     */
    private void reloadCachedImages() {
        cachedGroundImage = loadImage(groundImagePath);
        cachedIndestructibleImage = loadImage(wallIndestructibleImagePath);
        cachedDestructibleImage = loadImage(wallDestructibleImagePath);
    }

    /**
     * Charge une image (depuis ressources ou fichier).
     */
    private Image loadImage(String path) {
        try {
            if (path.startsWith("/") && getClass().getResource(path) != null)
                return new Image(getClass().getResource(path).toExternalForm(), CELL_SIZE - 8, CELL_SIZE - 8, true, true, true);
            else
                return new Image("file:" + path, CELL_SIZE - 8, CELL_SIZE - 8, true, true, true);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Ouvre une boîte de dialogue pour choisir une image.
     */
    private void chooseImage(String title, java.util.function.Consumer<String> callback) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        File initialDir = new File("src/main/resources/images/elementsMap");
        if (initialDir.exists()) {
            fc.setInitialDirectory(initialDir);
        }

        File f = fc.showOpenDialog(stage);
        if (f != null) callback.accept(f.getAbsolutePath());
    }

    /**
     * Rafraîchit l'affichage graphique de la grille.
     */
    private void refreshGridGraphics() {
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLUMNS; col++)
                updateCellVisual(gridButtons[row][col], gridData[row][col]);
    }

    /**
     * Sauvegarde le niveau dans un fichier.
     */
    private void saveLevel() {
        TextInputDialog dialog = new TextInputDialog("MonNiveau");
        dialog.setTitle("Nom du niveau");
        dialog.setHeaderText("Entrez le nom du niveau à sauvegarder :");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isEmpty()) return;
        String levelName = result.get();

        int[][] layoutInt = new int[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLUMNS; col++)
                layoutInt[row][col] = gridData[row][col] == '#' ? 1 : gridData[row][col] == '%' ? 2 : 0;

        Level lvl = new Level(levelName, groundImagePath, wallIndestructibleImagePath, wallDestructibleImagePath, layoutInt);

        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder niveau");
        File customDir = new File("src/main/resources/levels/custom");
        if (!customDir.exists()) customDir.mkdirs();
        fc.setInitialDirectory(customDir);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier de niveau", "*.level"));
        fc.setInitialFileName(levelName.replaceAll("\\s+", "_") + ".level");
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                lvl.saveToFile(file.toPath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Niveau sauvegardé avec succès !", ButtonType.OK);
                alert.showAndWait();
            } catch (Exception ex) {
                showError("Erreur lors de la sauvegarde : " + ex.getMessage());
            }
        }
    }

    /**
     * Charge un niveau depuis un fichier.
     */
    private void loadLevel() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Charger un niveau");
        File customDir = new File("src/main/resources/levels/custom");
        if (!customDir.exists()) customDir.mkdirs();
        fc.setInitialDirectory(customDir);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier de niveau", "*.level"));
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            try {
                Level lvl = Level.fromFile(file.toPath());
                groundImagePath = lvl.getGroundImagePath();
                wallIndestructibleImagePath = lvl.getWallIndestructibleImagePath();
                wallDestructibleImagePath = lvl.getWallDestructibleImagePath();

                int[][] layout = lvl.getLayout();
                for (int row = 0; row < ROWS; row++)
                    for (int col = 0; col < COLUMNS; col++)
                        gridData[row][col] = layout[row][col] == 1 ? '#' : layout[row][col] == 2 ? '%' : ' ';
                updatePreviews();
            } catch (Exception ex) {
                showError("Erreur lors du chargement : " + ex.getMessage());
            }
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Retourne à l'écran des paramètres.
     */
    private void returnToSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/settings.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            SettingsController controller = loader.getController();
            controller.setStage(stage);
            stage.setScene(scene);

        } catch (Exception ex) {
            System.err.println("Erreur lors du retour aux paramètres : " + ex.getMessage());
        }
    }
}