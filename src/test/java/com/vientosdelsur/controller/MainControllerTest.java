package com.vientosdelsur.controller;

import com.vientosdelsur.data.Database;
import com.vientosdelsur.model.Room;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;

class MainControllerTest {

    @Test
    void assignWorkToTheFullArray() {

        var mainController = new MainController();
        ArrayList<Room> roomArray = Database.createRoomArray();
        assertAll(() -> mainController.assignWork(roomArray));

    }


    @Test
    void assignWorkToRandomArray() {
        var mainController = new MainController();
        ArrayList<Room> roomArray = Database.createRoomArray();

        int con = 100000;
        while (con > 0) {
            var random = new Random();

            roomArray = Database.createRoomArray();

            int randomIndex = random.nextInt(roomArray.size() - 5);

            for (int i = 0; i < randomIndex; i++) {
                int i1 = random.nextInt( roomArray.size());
                roomArray.remove(i1);

            }

            ArrayList<ArrayList<Room>> arrayLists = mainController.assignWork(roomArray, true);

            con--;
        }
    }
}