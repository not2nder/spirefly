package filemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FileManager {
    public void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public ArrayList<File> listFiles(File path){

        ArrayList<File> list = new ArrayList<>();

        FilenameFilter audioFilter = (dir,name) -> {
            for (String ext: new String[]{"mp3", "wav","ogg"}) {
                if (name.toLowerCase().endsWith("."+ext)){
                    return true;
                }
            }
            return false;
        };
        Collections.addAll(list, Objects.requireNonNull(path.listFiles()));
        return list;
    }
}
