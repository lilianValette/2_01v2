package com.bomberman.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class EndGameScreenController {
    @FXML
    private Label messageLabel;

    private Runnable onReturnCallback;

    @FXML
    private ImageView backgroundImage;

    @FXML
    public void initialize() {
        // Charge l'image avec rendu rétro (pas de lissage)
        Image img = new Image(getClass().getResourceAsStream("/images/menu/Bomber_fond.jpg"));
        backgroundImage.setImage(img);
        backgroundImage.setFitWidth(800);
        backgroundImage.setFitHeight(600);
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setSmooth(false); // effet pixel rétro

        // Ajuste dynamiquement la taille de la fenêtre à 800x600, comme le menu
        Platform.runLater(() -> {
            Stage stage = (Stage) backgroundImage.getScene().getWindow();
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setMaxWidth(800);
            stage.setMaxHeight(600);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setResizable(false);
        });
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setOnReturnCallback(Runnable callback) {
        this.onReturnCallback = callback;
    }

    @FXML
    private void handleReturnToMenu(ActionEvent event) {
        if (onReturnCallback != null) {
            onReturnCallback.run();
        }
    }
}