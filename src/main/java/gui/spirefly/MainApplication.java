package gui.spirefly;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader mainLoader;

        mainLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene mainScene = new Scene(mainLoader.load(), 890, 550);

        MainController controller = mainLoader.getController();

        stage.setMinWidth(600);
        stage.setMinHeight(400);

        stage.setTitle("NoNameAlready");
        stage.setScene(mainScene);
        stage.show();

        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });

        mainScene.setOnKeyPressed(event ->{
            switch (event.getCode()){
                case UP    -> controller.next();
                case DOWN  -> controller.previous();
                case SPACE -> controller.play();
                case M     -> controller.mute();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}