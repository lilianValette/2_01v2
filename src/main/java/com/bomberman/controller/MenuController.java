package com.bomberman.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Contrôleur du menu principal de Bomberman.
 * Gère la navigation, l'affichage des images et l'animation de l'écran principal.
 */
public class MenuController {

    @FXML private Button playButton;
    @FXML private Button accountButton;
    @FXML private Button settingsButton;
    @FXML private Button quitButton;

    @FXML private ImageView backgroundImage;
    @FXML private ImageView planeImage;
    @FXML private ImageView balloonImage;
    @FXML private ImageView logoImage;
    @FXML private StackPane rootPane;

    private Stage stage;
    private Timeline planeTimeline;
    private Timeline balloonTimeline;

    /**
     * Définit la fenêtre principale JavaFX et adapte sa taille à l'image de fond.
     * @param stage Fenêtre principale de l'application
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        adaptStageToBackgroundImage();
    }

    /**
     * Initialise les composants graphiques et les événements du menu.
     */
    @FXML
    public void initialize() {
        backgroundImage.setImage(loadImage("/images/menu/Bomber_fond.jpg"));
        planeImage.setImage(loadImage("/images/menu/Bomber_plane-removebg-preview.png"));
        balloonImage.setImage(loadImage("/images/menu/Bomber_balloon-removebg-preview.png"));
        logoImage.setImage(loadImage("/images/menu/logo.gif"));

        planeImage.setFitWidth(400);
        balloonImage.setFitWidth(140);

        backgroundImage.imageProperty().addListener((obs, oldImg, newImg) -> adaptStageToBackgroundImage());
        adaptStageToBackgroundImage();

        playButton.setOnAction(e -> startGameSetup());
        accountButton.setOnAction(e -> openAccount());
        settingsButton.setOnAction(e -> openSettings());
        quitButton.setOnAction(e -> System.exit(0));

        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> startAnimations(newVal.doubleValue()));
    }

    /**
     * Adapte la taille de la fenêtre à celle de l'image de fond.
     */
    private void adaptStageToBackgroundImage() {
        if (stage != null && backgroundImage != null) {
            double w = backgroundImage.getFitWidth();
            double h = backgroundImage.getFitHeight();
            if (w > 0 && h > 0) {
                stage.setMinWidth(w);
                stage.setMinHeight(h);
                stage.setMaxWidth(w);
                stage.setMaxHeight(h);
                stage.setWidth(w);
                stage.setHeight(h);
                stage.setResizable(false);
            }
        }
    }

    /**
     * Lance les animations du dirigeable et du ballon.
     * @param paneWidth Largeur du panneau principal
     */
    private void startAnimations(double paneWidth) {
        // Animation du dirigeable
        planeImage.setScaleX(1);
        planeImage.setTranslateY(30);
        double planeWidth = planeImage.getFitWidth();
        double planeStartX = paneWidth / 2 + planeWidth + 150;
        double planeEndX   = -paneWidth / 2 - planeWidth;
        double planeDuration = 14.0;
        double planeTotalDistance = planeStartX - planeEndX;
        double planeSpeedPerFrame = planeTotalDistance / (planeDuration * 60.0);
        planeImage.setTranslateX(planeStartX);

        if (planeTimeline != null) planeTimeline.stop();
        planeTimeline = new Timeline(new KeyFrame(Duration.millis(1000.0/60.0), e -> {
            double currentX = planeImage.getTranslateX() - planeSpeedPerFrame;
            if (currentX <= planeEndX) currentX = planeStartX;
            planeImage.setTranslateX(currentX);
        }));
        planeTimeline.setCycleCount(Timeline.INDEFINITE);
        planeTimeline.play();

        // Animation du ballon
        balloonImage.setScaleX(1);
        double balloonWidth = balloonImage.getFitWidth();
        double balloonStartX = -paneWidth / 2 - (balloonWidth + 300);
        double balloonEndX   = paneWidth / 2 + (balloonWidth - 300);
        double balloonDuration = 22.0;
        double balloonTotalDistance = balloonEndX - balloonStartX;
        double balloonSpeedPerFrame = balloonTotalDistance / (balloonDuration * 60.0);
        double balloonBaseY = 160;
        double oscillationAmplitude = 18.0;
        double oscillationFrequency = 0.7;

        balloonImage.setTranslateX(balloonStartX);

        if (balloonTimeline != null) balloonTimeline.stop();
        final double[] balloonFrame = {0};
        balloonTimeline = new Timeline(new KeyFrame(Duration.millis(1000.0/60.0), e -> {
            double currentX = balloonImage.getTranslateX() + balloonSpeedPerFrame;
            if (currentX >= balloonEndX) {
                currentX = balloonStartX;
                balloonFrame[0] = 0;
            }
            balloonImage.setTranslateX(currentX);
            double t = balloonFrame[0] / 60.0;
            double offsetY = balloonBaseY + oscillationAmplitude * Math.sin(2 * Math.PI * oscillationFrequency * t);
            balloonImage.setTranslateY(offsetY);
            balloonFrame[0]++;
        }));
        balloonTimeline.setCycleCount(Timeline.INDEFINITE);
        balloonTimeline.play();
    }

    /**
     * Ouvre l'écran de configuration de la partie.
     */
    @FXML
    private void startGameSetup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/gameSetup.fxml"));
            Parent root = loader.load();
            GameSetupController controller = loader.getController();
            controller.setStage(stage);
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'écran de configuration de partie : " + e.getMessage());
        }
    }

    /**
     * Ouvre l'écran de gestion des comptes.
     */
    @FXML
    private void openAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/account.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AccountController accountController) {
                accountController.setStage(stage);
            }
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'écran de comptes : " + e.getMessage());
        }
    }

    /**
     * Ouvre l'écran des paramètres.
     */
    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/settings.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            SettingsController controller = loader.getController();
            controller.setStage(stage);
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'écran des paramètres : " + e.getMessage());
        }
    }

    /**
     * Charge une image à partir des ressources.
     * @param resourcePath Chemin de la ressource
     * @return Image chargée, ou null si non trouvée
     */
    private Image loadImage(String resourcePath) {
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.err.println("Image non trouvée : " + resourcePath);
            return null;
        }
        return new Image(url.toExternalForm());
    }
}