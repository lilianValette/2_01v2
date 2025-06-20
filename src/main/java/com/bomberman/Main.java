package com.bomberman;

import com.bomberman.controller.MenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/menu.fxml"));
        Parent root = loader.load();
        MenuController menuController = loader.getController();
        menuController.setStage(primaryStage);

        primaryStage.getIcons().add(
                new javafx.scene.image.Image(getClass().getResourceAsStream("/images/avatarsJoueurs/avatarBlanc.png"))
        );

        primaryStage.setTitle("Bomberman");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}