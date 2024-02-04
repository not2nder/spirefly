package gui.spirefly;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MainApplication extends Application {
    private static Stage stage;
    protected static Scene mainScene;
    private static Object data;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader mainLoader, configLoader;

        mainLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        mainScene = new Scene(mainLoader.load(), 890, 550);

        MainController controller = mainLoader.getController();

        stage.setMinWidth(600);
        stage.setMinHeight(400);

        stage.setTitle("NoNameAlready");
        stage.setScene(mainScene);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });

        mainScene.setOnKeyPressed(event ->{
            switch (event.getCode()){
                case UP -> {
                    controller.next();
                }
                case DOWN -> {
                    controller.previous();
                }
                case SPACE -> {
                    controller.play();
                }
                case M -> {
                    controller.mute();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}