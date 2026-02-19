package me.bramar.maze;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MazeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/maze-app-view.fxml"));
        Scene scene = new Scene(loader.load(), 720, 600);

        stage.initStyle(StageStyle.UNDECORATED);

        Image icon = new Image("icon.png");
        stage.getIcons().add(icon);

        stage.setTitle("Maze Simulation");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        MazeController controller = loader.getController();
        controller.initInScene();
    }

    public static void main(String[] args) {
        launch();
    }
}