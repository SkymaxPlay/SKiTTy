package com.gmail.skymaxplay.skitty;

import com.gmail.skymaxplay.skitty.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class SKiTTy extends Application {

    @Getter
    private static SKiTTy instance;
    @Getter
    private Stage primaryStage;
    @Getter
    @Setter
    private MainController mainController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(getClass().getResource("/view/mainStage.fxml"));
        primaryStage.setTitle("SKiTTy");
        primaryStage.setScene(new Scene(root, 1280, 720));

        primaryStage.show();
        this.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
