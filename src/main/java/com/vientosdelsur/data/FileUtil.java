package com.vientosdelsur.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


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
        String content = """
                [
                  {
                    "shift": "PART_TIME",
                    "name": "Macarena",
                    "color": "0x006400ff",
                    "preferredFloor": ""
                  },
                  {
                    "shift": "PART_TIME",
                    "name": "Flor",
                    "color": "0x009600ff",
                    "preferredFloor": ""
                  },
                  {
                    "shift": "FULL_TIME",
                    "name": "Graciela",
                    "color": "0x717a00ff",
                    "preferredFloor": "2"
                  },
                  {
                    "shift": "FULL_TIME",
                    "name": "Bárbara",
                    "color": "0x94a400ff",
                    "preferredFloor": "3"
                  },
                  {
                    "shift": "FULL_TIME",
                    "name": "Yanira",
                    "color": "0x003200ff",
                    "preferredFloor": "4"
                  }
                ]""";
        if (!file.exists()) {
            Files.write(Paths.get(json), content.getBytes());
        }
    }

    public FileUtil() {
    }

}
