package com.vientosdelsur.main;

import com.vientosdelsur.controller.MainController;
import com.vientosdelsur.data.Constants;
import com.vientosdelsur.data.FileUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class VientosDelSur extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        var fileUtil = new FileUtil();
        fileUtil.makeDirectory(Constants.HOUSEKEEPER_JSON_DIR_LOCATION);
        fileUtil.createJSON(Constants.HOUSEKEEPER_JSON_LOCATION);
        setUpMainScene(stage);
    }

    private void setUpMainScene(Stage stage) throws IOException {
        var fxmlLoader = new FXMLLoader(Main.class.getResource("/com/vientosdelsur/fxml/mainController.fxml"));
        Parent root = fxmlLoader.load();
        var scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/vientosdelsur/css/mainController.css")).toExternalForm());
        MainController mainController = fxmlLoader.getController();
        stage.setMinHeight(750);
        stage.setMinWidth(860);
        stage.setTitle("Vientos del Sur");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
