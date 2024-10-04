package main;

import controller.CommonResourcesPaths;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.net.URL;
import controller.AppController;

public class MainController extends Application {

    private BorderPane root;
    private AppController appController;

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(CommonResourcesPaths.APP_FXML_LIGHT_RESOURCE);
        fxmlLoader.setLocation(url);
        root = fxmlLoader.load(url.openStream());
        appController = fxmlLoader.getController();
        appController.setMainController(this);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
