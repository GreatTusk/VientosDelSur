package com.vientosdelsur.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vientosdelsur.enums.Shift;
import javafx.scene.paint.Color;

import java.io.IOException;

public class HousekeeperTypeAdapter extends TypeAdapter<Housekeeper> {

    @Override
    public void write(JsonWriter out, Housekeeper housekeeper) throws IOException {
        out.beginObject();
        out.name("shift").value(housekeeper.shift().name());
        out.name("name").value(housekeeper.name());
        out.name("color").value(housekeeper.preferredColor());
        out.name("preferredFloor").value(housekeeper.preferredFloor());
        out.endObject();
    }

    @Override
    public Housekeeper read(JsonReader in) throws IOException {

        String name = "", preferredFloor = "", color = "";
        Shift shift = null;

        in.beginObject();

        while (in.hasNext()){
            String field= in.nextName();
            switch (field){
                case "name" -> name = in.nextString();
                case "preferredFloor" -> preferredFloor = in.nextString();
                case "color" -> color = in.nextString();
                case "shift" -> shift = Shift.valueOf(in.nextString());
            }
        }
        in.endObject();
        return  new Housekeeper(shift, name, preferredFloor, color);

    }

    private static Color parseColor(String colorString) {
        if (colorString == null || colorString.length() != 10 || !colorString.startsWith("0x")) {
            throw new IllegalArgumentException("Invalid color string format");
        }

        // Extract individual color components
        int red = Integer.parseInt(colorString.substring(2, 4), 16);
        int green = Integer.parseInt(colorString.substring(4, 6), 16);
        int blue = Integer.parseInt(colorString.substring(6, 8), 16);
        int alpha = Integer.parseInt(colorString.substring(8), 16);

        // Normalize alpha value
        double opacity = alpha / 255.0;

        // Create JavaFX Color object
        return Color.rgb(red, green, blue, opacity);
    }
}
