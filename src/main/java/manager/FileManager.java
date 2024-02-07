package manager;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileManager {
    public Object choose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        return selectedFile;
    }
}
