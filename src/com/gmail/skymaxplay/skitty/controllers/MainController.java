package com.gmail.skymaxplay.skitty.controllers;

import com.gmail.skymaxplay.skitty.SKiTTY;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    public MenuItem sessionMenu;
    @FXML
    public TabPane tabPane;


    public void onClickMenu(ActionEvent event) {
        // menu 32px
        tabPane.setMinHeight(tabPane.getScene().getHeight() - 32);
        tabPane.getScene().heightProperty().addListener((obs, oldVal, newVal) -> {
            tabPane.setMinHeight((double) newVal - 32);
        });

        try {
            Parent root = FXMLLoader.load(getClass().getResource("../../../../../view/sessionStage.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Lista sesji SSH");
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SKiTTY.getInstance().setMainController(this);
    }
}
