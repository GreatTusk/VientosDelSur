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

}
