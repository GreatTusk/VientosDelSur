package com.vientosdelsur.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vientosdelsur.model.Housekeeper;
import com.vientosdelsur.model.HousekeeperTypeAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Json {

    public Json() {
    }

    private static final Gson HOUSEKEEPER_JSON = new GsonBuilder()
            .registerTypeAdapter(Housekeeper.class, new HousekeeperTypeAdapter())
            .create();

    public ObservableList<Housekeeper> readHousekeeperList(String filePath) {

        try (Reader reader = new FileReader(filePath)) {
            ArrayList<Housekeeper> housekeepers = HOUSEKEEPER_JSON.fromJson(reader, new TypeToken<>() {
            });
            return FXCollections.observableList(housekeepers);
        } catch (Exception e) {
            return null;
        }

    }

    public void writeHousekeeperList(ObservableList<Housekeeper> albumArray, String jsonPath) throws IOException {

        // Iterating over the sorted entries and adding them to the children
        try (Writer writer = new FileWriter(jsonPath)) {
            albumArray.sort(Comparator.comparing(Housekeeper::shift).reversed().thenComparing(Housekeeper::preferredFloor));
            HOUSEKEEPER_JSON.toJson(albumArray, new TypeToken<ObservableList<Housekeeper>>() {
            }.getType(), writer);
        }
    }
}
