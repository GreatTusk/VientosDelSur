package com.vientosdelsur.data;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    public void makeDirectory(String directory) {
        var file = new File(directory);
        if (!file.exists()) {
            File dir = new File(directory);
            dir.mkdir();
        }
    }

    public void createJSON(String json) throws IOException {
        var file = new File(json);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public FileUtil() {
    }

}
