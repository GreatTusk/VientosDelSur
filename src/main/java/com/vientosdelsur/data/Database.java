package com.vientosdelsur.data;

import com.vientosdelsur.enums.RoomType;
import com.vientosdelsur.enums.Shift;
import com.vientosdelsur.model.Housekeeper;
import com.vientosdelsur.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;

public class Database {

    public static ArrayList<Room> createRoomArray() {

        var roomArray = new ArrayList<Room>();

        // First floor

        roomArray.add(new Room(101, RoomType.MA));
        roomArray.add(new Room(102, RoomType.M));
        roomArray.add(new Room(105, RoomType.M));
        roomArray.add(new Room(104, RoomType.O));

        // Second flor

        for (int i = 206; i < 217; i++) {
            roomArray.add(new Room(i, RoomType.M));
        }

        // Third floor

        roomArray.add(new Room(317, RoomType.MA));
        roomArray.add(new Room(318, RoomType.C));
        roomArray.add(new Room(319, RoomType.M));
        roomArray.add(new Room(320, RoomType.M));
        roomArray.add(new Room(321, RoomType.MA));
        roomArray.add(new Room(322, RoomType.C));
        roomArray.add(new Room(323, RoomType.M));

        // Fourth floor

        roomArray.add(new Room(424, RoomType.C));
        roomArray.add(new Room(425, RoomType.C));
        roomArray.add(new Room(426, RoomType.M));
        roomArray.add(new Room(427, RoomType.M));
        roomArray.add(new Room(428, RoomType.M));
        roomArray.add(new Room(429, RoomType.M));

        return roomArray;
    }


    public static void createHouseKeeperArray() {
        ObservableList<Housekeeper> maidArray = FXCollections.observableArrayList();

        maidArray.add(new Housekeeper(Shift.PART_TIME, "Macarena", "", Color.rgb(0, 100, 0).toString()));
        maidArray.add(new Housekeeper(Shift.PART_TIME, "Flor", "", Color.rgb(0, 150, 0).toString()));
        maidArray.add(new Housekeeper(Shift.FULL_TIME, "Graciela", "2", Color.rgb(113, 122, 0).toString()));
        maidArray.add(new Housekeeper(Shift.FULL_TIME, "Bárbara", "3", Color.rgb(148, 164, 0).toString()));
        maidArray.add(new Housekeeper(Shift.FULL_TIME, "Yanira", "4", Color.rgb(0, 50, 0).toString()));

        try {
            new Json().writeHousekeeperList(maidArray, Constants.HOUSEKEEPER_JSON_LOCATION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObservableList<Housekeeper> readHouseKeeperArray() {
        return new Json().readHousekeeperList(Constants.HOUSEKEEPER_JSON_LOCATION);
    }
}
