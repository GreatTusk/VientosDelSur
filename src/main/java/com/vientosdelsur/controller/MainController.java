package com.vientosdelsur.controller;

import com.vientosdelsur.customnodes.HouseKeeperLabel;
import com.vientosdelsur.customnodes.RoomCheckbox;
import com.vientosdelsur.data.Constants;
import com.vientosdelsur.data.Database;
import com.vientosdelsur.data.Json;
import com.vientosdelsur.enums.RoomType;
import com.vientosdelsur.enums.Shift;
import com.vientosdelsur.model.Housekeeper;
import com.vientosdelsur.model.Room;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainController implements Initializable {

    @FXML
    private GridPane roomsPane;
    @FXML
    private VBox employeesVBox;
    @FXML
    private SplitPane roomsSplitPane;
    @FXML
    private BorderPane root, staffPane;
    private ArrayList<Room> rooms;
    private ObservableList<Housekeeper> housekeepers;
    private int indexRoomQuota = -1;
    private int employeeIndex = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialize();
        loadStaffPane();
    }

    /**
     * Loads the StaffRegistration pane and initializes it.
     */
    private void loadStaffPane() {
        try {
            var staffReg = new FXMLLoader(getClass().getResource("/com/vientosdelsur/fxml/staffRegistration.fxml"));
            staffPane = staffReg.load();
            StaffRegistration staffRegistration = staffReg.getController();


            staffPane.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/com/vientosdelsur/css/staffRegistration.css")).toExternalForm()
            );
            housekeepers.addListener((ListChangeListener<Housekeeper>) c -> {
                try {
                    new Json().writeHousekeeperList(housekeepers, Constants.HOUSEKEEPER_JSON_LOCATION);
                } catch (IOException ignored) {
                }
            });
            staffRegistration.setHousekeepers(housekeepers);
            staffRegistration.populateHousekeeperTable();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MainController() {
//        housekeepers = Database.readHouseKeeperArray();
//        rooms = Database.createRoomArray();
    }

    /**
     * When the user presses the "Mucamas" button, the center of the borderPane is set to staffPane.
     */
    public void switchToStaffRegistration() {
        root.setCenter(staffPane);
    }

    /**
     * When the user presses the "Habitaciones" button, the center of the borderPane is set to roomsSplitPane.
     */
    public void switchToRoomsPane() {
        root.setCenter(roomsSplitPane);
    }

    /**
     * Initializes MainController by creating and displaying UI components.
     */
    private void initialize() {
        housekeepers = Database.readHouseKeeperArray();
        displayMaidLabels(housekeepers);
        displayRoomMatrix();
    }

    /**
     * Centers each element in the gridPane and populates it with data (rooms).
     */
    private void displayRoomMatrix() {
        rooms = Database.createRoomArray();
        roomsPane.getColumnConstraints().forEach(columnConstraints -> columnConstraints.setHalignment(HPos.CENTER));
        roomsPane.getRowConstraints().forEach(rowConstraints -> rowConstraints.setValignment(VPos.CENTER));
        populateRoomsPane(createRoomMatrix(rooms));
    }

    /**
     * Organizes the rooms in a HashMap for the purpose of separating them by the floor they are in.
     * This will be useful later when representing the layout of the hotel graphically.
     *
     * @param rooms the complete ArrayList of rooms containing all the rooms in the hotel
     * @return a HashMap with a size of 4, one entry for each floor
     */
    private HashMap<Integer, ArrayList<Room>> createRoomMatrix(ArrayList<Room> rooms) {
        HashMap<Integer, ArrayList<Room>> roomMatrix = new HashMap<>();

        rooms.forEach(room -> {
            int floor = Integer.parseInt(String.valueOf(room.id()).substring(0, 1));
            roomMatrix.putIfAbsent(floor, new ArrayList<>());
            roomMatrix.get(floor).add(room);
        });

        return roomMatrix;
    }

    /**
     * Triggers all the methods that calculate which rooms should be assigned to each housekeeper,
     * and the methods that represent in the GUI the results.
     * This method is called when the button "Generar distribuciones" is pressed
     */
    public void distributeWork() {
        ObservableList<Housekeeper> selectedHousekeepers = processSelectedHousekeepers();
        if (!selectedHousekeepers.isEmpty()) {
            assignWork(getOccupiedRooms(), selectedHousekeepers);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Asignación de trabajo");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, seleccione al menos una mucama antes de continuar.");
            alert.showAndWait();
        }

    }

    /**
     * Populates roomsPane with the hotel's rooms, each represented by a checkbox.
     * Currently, there are 4 columns and 11 rows.
     * This method will work even if more rooms or floors are built.
     *
     * @param rooms the original ArrayList of Rooms that contains each Room in the hotel.
     */
    private void populateRoomsPane(HashMap<Integer, ArrayList<Room>> rooms) {

        int floors = rooms.keySet().size();

        for (int i = 1; i <= floors; i++) {

            ArrayList<Room> roomList = rooms.get(i);

            for (int j = 0; j < roomList.size(); j++) {
                var roomCheckbox = new RoomCheckbox(roomList.get(j).id() + " " + roomList.get(j).roomType().name());
                roomsPane.add(roomCheckbox, i - 1, j);
            }

        }

    }

    /**
     * Returns the room that matches the id provided as a String.
     *
     * @param id the id of the room to look for
     * @return a Room object if found, else null
     */
    private Room matchRoom(String id) {

        int roomId = Integer.parseInt(id);

        return rooms
                .stream()
                .filter(room -> room.id() == roomId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Iterates through the rooms (represented as checkboxes in the GUI) and returns an ArrayList of the
     * rooms that are selected.
     *
     * @return an ArrayList of the rooms that are occupied
     */
    private ArrayList<Room> getOccupiedRooms() {

        ArrayList<Room> roomList = new ArrayList<>();

        /*
        While a stream could potentially be utilized, it would not allow us to harness the benefits
        of the Pattern Matching for instanceof feature introduced in Java 16, which facilitates the
        extraction of whether the checkbox is selected (and obtaining its text later).
        */

        roomsPane.getChildren().forEach(room -> {
            if (room instanceof CheckBox checkBox && checkBox.isSelected()) {
                String id = checkBox.getText().split(" ")[0];
                roomList.add(matchRoom(id));
            }
        });


        return roomList;
    }

    private Housekeeper matchHousekeeper(String color, ObservableList<Housekeeper> housekeepers) {

        return housekeepers
                .stream()
                .filter(housekeeper -> housekeeper.preferredColor().equals(color))
                .findAny()
                .orElseThrow();
    }

    /**
     * Clear the background of each room checkbox, distributes the amount of work each housekeeper will
     * have to fulfill, and assigns rooms accordingly. Finally, the results are represented graphically
     * in the GUI.
     *
     * @param selectedRooms the array of rooms the user selected
     */
    public void assignWork(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers) {

        resetRoomColors();
        // Distribution by shift
        long start = System.nanoTime();
        var workloadPartFull = distributeWorkload(selectedRooms, selectedHousekeepers);
        ArrayList<ArrayList<Room>> arrayLists = arrangeRooms(selectedRooms, selectedHousekeepers, workloadPartFull);
        long end = System.nanoTime();
        long elapsedTime = end - start;
        double seconds = (double) elapsedTime / 1_000_000_000.0;
        System.out.println("Time elapsed: " + seconds + " seconds");
        int housekeeperArraySeparatorIndex = getShiftSeparatorIndex(selectedHousekeepers);

        Platform.runLater(() -> {
            paintLabels(getPartTimeRooms(arrayLists, housekeeperArraySeparatorIndex),
                    getPartTimeHouseKeepers(housekeeperArraySeparatorIndex, selectedHousekeepers));
            paintLabels(getFullTimeRooms(arrayLists, housekeeperArraySeparatorIndex),
                    getFullTimeHousekeepers(arrayLists, housekeeperArraySeparatorIndex, selectedHousekeepers));
        });

    }

    private ObservableList<Housekeeper> processSelectedHousekeepers() {
        ArrayList<Housekeeper> selectedList = new ArrayList<>();

        employeesVBox.getChildren().forEach(node -> {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                var label = (Label) checkBox.getGraphic();
                var rectangle = (Rectangle) label.getGraphic();
                var color = (Color) rectangle.getFill();
                String colorToString = color.toString();
                selectedList.add(matchHousekeeper(colorToString, housekeepers));
            }
        });

        return FXCollections.observableList(selectedList);
    }

    /**
     * For testing purposes.
     *
     * @param selectedRooms the rooms to work with
     * @param isTrue        just here to overload assignWork
     */
    public ArrayList<ArrayList<Room>> assignWork(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers, boolean isTrue) {
        if (isTrue) {
            // Distribution of shift
            var workloadPartFull = distributeWorkload(selectedRooms, selectedHousekeepers);
            return arrangeRooms(selectedRooms, housekeepers, workloadPartFull);
        }
        return null;

    }

    /**
     * Iterates through each checkbox in the gridPane and sets its background color to null.
     */
    private void resetRoomColors() {

        roomsPane
                .getChildren()
                .stream()
                .filter(node -> node instanceof CheckBox)
                .filter(checkbox -> checkbox.getStyle().isBlank())
                .forEach(checkbox -> checkbox.setStyle("-fx-background-color: null;"));

    }

    /**
     * Helper method to return a sublist containing only the rooms distributions
     * associated with part-time housekeepers.
     *
     * @param arrayLists                     the original ArrayList of distributions.
     * @param housekeeperArraySeparatorIndex the index that separates full-time and part-time distributions.
     * @return a List of part-time room distributions
     */
    private List<ArrayList<Room>> getPartTimeRooms(ArrayList<ArrayList<Room>> arrayLists, int housekeeperArraySeparatorIndex) {
        return arrayLists.subList(0, housekeeperArraySeparatorIndex);
    }

    /**
     * Helper method to return a sublist containing only the rooms distributions
     * associated with full-time housekeepers.
     *
     * @param arrayLists                     the original ArrayList of distributions.
     * @param housekeeperArraySeparatorIndex the index that separates full-time and part-time distributions.
     * @return a List of full-time room distributions
     */
    private List<ArrayList<Room>> getFullTimeRooms(ArrayList<ArrayList<Room>> arrayLists, int housekeeperArraySeparatorIndex) {
        return arrayLists.subList(housekeeperArraySeparatorIndex, arrayLists.size());
    }

    /**
     * Helper method to return a sublist containing only part-time housekeepers.
     *
     * @param housekeeperArraySeparatorIndex the index that separates full-time and part-time housekeepers.
     * @return a List of part-time housekeepers.
     */
    private List<Housekeeper> getPartTimeHouseKeepers(int housekeeperArraySeparatorIndex, ObservableList<Housekeeper> selectedHousekeepers) {
        return selectedHousekeepers.subList(0, housekeeperArraySeparatorIndex);
    }

    /**
     * Helper method to return a sublist containing only full-time housekeepers.
     *
     * @param housekeeperArraySeparatorIndex the index that separates full-time and part-time housekeepers.
     * @return a List of full-time housekeepers.
     */
    private List<Housekeeper> getFullTimeHousekeepers(ArrayList<ArrayList<Room>> arrayLists, int housekeeperArraySeparatorIndex, ObservableList<Housekeeper> selectedHousekeepers) {
        return selectedHousekeepers.subList(housekeeperArraySeparatorIndex, arrayLists.size());
    }

    /**
     * Returns the index that divides full-time and part-time housekeepers.
     * The Shift to compare to is Shift.PART_TIME, as the first housekeepers in the
     * list will always be part-time.
     *
     * @param housekeepers the ObservableList that contains all housekeepers
     * @return the index where the previous Shift is different from the current one
     */
    private int getShiftSeparatorIndex(ObservableList<Housekeeper> housekeepers) {
        Shift currentShift; // Initialize with null
        Shift previousShift = null; // Initialize with null

        for (int i = 0; i < housekeepers.size(); i++) {
            currentShift = housekeepers.get(i).shift();
            if (!Objects.equals(currentShift, previousShift)) { // Compare using equals method
                return i;
            }
            previousShift = currentShift;
        }
        return -1;
    }


    /**
     * Arranges selected rooms among housekeepers according to workload quotas.
     * The logic is as follows:
     * When all but one housekeeper have been assigned all their rooms, the remaining rooms are assigned to the last
     * housekeeper remaining.
     * When one of the housekeeper's work quota can be satisfied by assigning one last room, it is assigned.
     * If one of the values in the array workloadPerEmployee is an odd number, MA rooms are assigned to even out the
     * present odd numbers.
     * As a last resort, if the only way to finish assigning rooms is by doing it unequally, it is done and the while loop is exited.
     * If a six is not present in workloadPerEmployee, rooms of the highest remaining room type are assigned equally to
     * each housekeeper.
     * If assigning two rooms could satisfy one remaining workload, and it is appropriate to do so, it is done.
     *
     * @param selectedRooms        the rooms we have to work with
     * @param workloadPerEmployee  the shift quota that should be satisfied for each housekeeper
     * @param selectedHousekeepers the list of housekeepers
     */
    public ArrayList<ArrayList<Room>> arrangeRooms(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers,
                                                   int[] workloadPerEmployee) {

        ArrayList<ArrayList<Room>> roomArrangement = new ArrayList<>(selectedHousekeepers.size());
        var values = RoomType.values();
        int bedPool = Arrays.stream(workloadPerEmployee).sum();
        var roomQuantity = new int[values.length];
        var workloadPerRoom = new int[values.length];

        initializeArrays(selectedRooms, selectedHousekeepers.size(), roomArrangement, values,
                roomQuantity, workloadPerRoom);

        bedPool = assignORoomIfExists(selectedRooms, selectedHousekeepers, workloadPerEmployee, bedPool,
                roomQuantity, workloadPerRoom, values, roomArrangement);

        bedPool = assignRoomsEquallyIfLessThanSixRoomsOccupied(selectedRooms, selectedHousekeepers, roomQuantity,
                roomArrangement, bedPool);

        int failSafe = 0;

        while (bedPool > 0) {

            failSafe++;
//            if (failSafe == 50) {
//                throw new RuntimeException();
//            } else
            if (isLastWorkload(workloadPerEmployee)) {

                bedPool = assignAllRemainingRooms(selectedRooms, workloadPerEmployee, roomArrangement);

            } else if (canWorkloadQuotaBeSatisfied(workloadPerEmployee, values, roomQuantity)) {
                // This is actually safe
                bedPool = processEntries(selectedRooms, selectedHousekeepers, workloadPerEmployee, employeeIndex,
                        indexRoomQuota, bedPool, roomQuantity,
                        workloadPerRoom, values, roomArrangement);

            } else if (oddNumberedWorkloadAmountExists(workloadPerEmployee, roomQuantity)) {
                bedPool = getRidOfOddNumberedWorkloads(selectedRooms, selectedHousekeepers, workloadPerEmployee, bedPool,
                        roomQuantity, workloadPerRoom, values, roomArrangement);

            } else if (failSafe > 15 || stalemateReached(workloadPerEmployee, roomQuantity)) {

                while (bedPool != 0) {
                    bedPool = processEntriesUnchecked(selectedRooms, selectedHousekeepers, workloadPerEmployee,
                            indexOfNonZeroItem(workloadPerEmployee), indexOfNonZeroItem(roomQuantity),
                            bedPool, roomQuantity, workloadPerRoom, values, roomArrangement);
                }
            } else if (!isThereSixInArray(workloadPerEmployee, roomQuantity)) {
                bedPool = assignRoomsEqually(selectedRooms, selectedHousekeepers, workloadPerEmployee, roomQuantity, bedPool, workloadPerRoom, values, roomArrangement);

            } else if (shouldAssignTwoRooms(workloadPerEmployee, roomQuantity, values)) {

                bedPool = assignTwoRooms(selectedRooms, selectedHousekeepers, workloadPerEmployee,
                        workloadPerRoom, bedPool, roomQuantity, values, roomArrangement);
            }

        }

        return roomArrangement;
    }

    /**
     * Assigns all rooms to the last remaining housekeeper.
     *
     * @param selectedRooms       the rooms left to be assigned
     * @param workloadPerEmployee the workload quota to be fulfilled for each employee
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return 0; no rooms will remain unassigned
     */
    private int assignAllRemainingRooms(ArrayList<Room> selectedRooms, int[] workloadPerEmployee, ArrayList<ArrayList<Room>> roomArrangement) {
        int bedPool;
        setLastWorkloadIndex(workloadPerEmployee);

        bedPool = processLastEntry(selectedRooms, employeeIndex,
                roomArrangement);
        return bedPool;
    }

    /**
     * Distributes rooms of the highest remaining room type equally among all employees.
     * Usually, they are M rooms, as they are the most numerous.
     *
     * @param selectedRooms       the remaining rooms
     * @param workloadPerEmployee the workload quota to be fulfilled for each employee
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param bedPool             the remaining workload value
     * @param workloadPerRoom     workload remaining for each room type
     * @param roomTypes           array of roomType enums
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return the remaining bedPool
     */
    private int assignRoomsEqually(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers, int[] workloadPerEmployee, int[] roomQuantity,
                                   int bedPool, int[] workloadPerRoom, RoomType[] roomTypes,
                                   ArrayList<ArrayList<Room>> roomArrangement) {

        // Simulating that we've gone through i0, 1
        int[] largestQuantityWIndex = findMaxNumberWIndex(roomQuantity);

        int maxValue = largestQuantityWIndex[0];
        int maxIndex = largestQuantityWIndex[1];

        // Will only assign a room to each employee if there are more than x rooms
        if (maxValue - Arrays.stream(workloadPerEmployee).filter(value -> value != 0).toArray().length >= 0) {

            for (int i = 0; i < workloadPerEmployee.length; i++) {
                // The shift for each employee is reduced by the shift value associated with the room type
                bedPool = processEntries(selectedRooms, selectedHousekeepers, workloadPerEmployee, i, maxIndex,
                        bedPool, roomQuantity, workloadPerRoom, roomTypes, roomArrangement);

            }
        }
        return bedPool;
    }

    /**
     * This method is only executed in the real-world rare case that only 6 in the hotel are occupied.
     * If so, one room, regardless of its work value, is assigned to each housekeeper until there are
     * no rooms left.
     *
     * @param selectedRooms   the ArrayList of rooms the user selected
     * @param maids           the list of housekeepers
     * @param roomQuantity    the array of the remaining rooms of each type
     * @param roomArrangement the arrayList where each distribution is allocated
     * @param bedPool         the remaining workload value
     * @return 0, as all rooms will be assigned if the condition is met, or the original bedPool amount,
     * if there were more than 6 rooms left
     */
    private int assignRoomsEquallyIfLessThanSixRoomsOccupied(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> maids,
                                                             int[] roomQuantity,
                                                             ArrayList<ArrayList<Room>> roomArrangement, int bedPool) {
        int totalRooms = Arrays.stream(roomQuantity).sum();

        if (totalRooms <= 6) {
            while (totalRooms > 0) {

                for (int i = 0; i < maids.size(); i++) {
                    if (totalRooms > 0) {
                        var room = selectedRooms.getFirst();
                        roomArrangement.get(i).add(room);
                        selectedRooms.remove(room);
                        totalRooms--;
                    } else {
                        break;
                    }

                }
            }
            bedPool = 0;
        }
        return bedPool;
    }

    /**
     * Iterates through a given array and returns the index of the first greater than 0 value
     *
     * @param workloadPerEmployee the array
     * @return the index of the non 0 value
     */
    private int indexOfNonZeroItem(int[] workloadPerEmployee) {
        for (int i = 0; i < workloadPerEmployee.length; i++) {
            if (workloadPerEmployee[i] > 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determines whether a stalemate has been reached, i.e., if the only way to
     * distribute all rooms accordingly is to assign "0.5" rooms to one of the housekeepers.
     *
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param roomQuantity        the array of the remaining rooms of each type
     * @return true if it is impossible to assign the remaining rooms equally, false if not
     */
    private boolean stalemateReached(int[] workloadPerEmployee, int[] roomQuantity) {

        int[] roomQuantityWithNoZeros = Arrays
                .stream(roomQuantity)
                .filter(value -> value != 0)
                .toArray();
        boolean oneTypeOfRoomLeft = roomQuantityWithNoZeros.length == 1;

        int housekeepersLeft = Arrays
                .stream(workloadPerEmployee)
                .filter(value -> value != 0)
                .toArray().length;

        if (oneTypeOfRoomLeft) {
            return roomQuantityWithNoZeros[0] % housekeepersLeft != 0;
        }
        return false;
    }

    /**
     * Assigns rooms with a work value of 3 (MA rooms) to housekeepers that have an odd-numbered remaining workload.
     * Within the while loop, MA rooms are the only room type that can be assigned to satisfy odd-numbered
     * remaining workloads, so they have to be assigned at the start to avoid being left with workloads that are
     * impossible to satisfy with twos and fours.
     *
     * @param selectedRooms       the rooms to work with
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param bedPool             the remaining workload value
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param workloadPerRoom     workload remaining for each room type
     * @param roomTypes           array of roomType enums
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return the remaining bedPool
     */
    private int getRidOfOddNumberedWorkloads(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers,
                                             int[] workloadPerEmployee, int bedPool,
                                             int[] roomQuantity, int[] workloadPerRoom,
                                             RoomType[] roomTypes, ArrayList<ArrayList<Room>> roomArrangement) {


        for (int i = 0; i < workloadPerEmployee.length; i++) {

            if (workloadPerEmployee[i] % 2 != 0 && roomQuantity[2] > 0) {
                bedPool = processEntries(selectedRooms, selectedHousekeepers, workloadPerEmployee, i, 2,
                        bedPool, roomQuantity, workloadPerRoom, roomTypes, roomArrangement);
            }

        }


        return bedPool;
    }

    /**
     * Assigns two rooms if convenient.
     *
     * @param selectedRooms       the rooms to work with
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param workloadPerRoom     workload remaining for each room type
     * @param bedPool             the remaining workload value
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param roomTypes           array of roomType enums
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return the remaining bedPool
     */
    private int assignTwoRooms(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers,
                               int[] workloadPerEmployee,
                               int[] workloadPerRoom, int bedPool, int[] roomQuantity,
                               RoomType[] roomTypes, ArrayList<ArrayList<Room>> roomArrangement) {
        // Begin by iterating over all the employees
        for (int i = 0; i < workloadPerEmployee.length; i++) {
            // Only continue if the current employee has a remaining workload greater than 0
            if (workloadPerEmployee[i] > 0) {
                // For the first roomType value to be evaluated (which would correspond to the type of room to be assigned twice)
                for (int j = 0; j < roomTypes.length; j++) {
                    // In order to assign two rooms, the current roomType j has to have more than 2 rooms remaining.
                    if (roomQuantity[j] >= 2) {
                        /*
                        If two rooms of the same type can be assigned to complete the required workload for
                        the i employee, assign them and return the new bedPool
                        */
                        if (workloadPerEmployee[i] - roomTypes[j].getValue() * 2 == 0) {
                            for (int l = 0; l < 2; l++) {
                                bedPool = processEntries(selectedRooms, selectedHousekeepers, workloadPerEmployee, i,
                                        j, bedPool, roomQuantity, workloadPerRoom,
                                        roomTypes, roomArrangement);
                            }
                            return bedPool;

                        }
                        /*
                        Else, if the previous condition was false, evaluate if two j rooms minus one room of
                        a different type (k) can complete employee[i]'s workload.
                        Just like the previous case, only two j rooms will be assigned if the condition is met.
                        k will be assigned by canWorkloadBeSatisfied() in the next iteration.
                        */
                        for (int k = 0; k < roomTypes.length; k++) {

                            if (roomQuantity[k] > 0
                                &&
                                roomTypes[j].getValue() != roomTypes[k].getValue()
                                &&
                                workloadPerEmployee[i] - roomTypes[j].getValue() * 2 - roomTypes[k].getValue() == 0) {

                                for (int l = 0; l < 2; l++) {
                                    bedPool = processEntries(selectedRooms, selectedHousekeepers, workloadPerEmployee, i,
                                            j, bedPool, roomQuantity, workloadPerRoom,
                                            roomTypes, roomArrangement);
                                }

                                return bedPool;

                            }

                        }
                    }
                }
            }
        }
        return bedPool;
    }

    /**
     * Evaluates if two rooms should be assigned.
     *
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param roomTypes           array of roomType enums
     * @return true if two rooms should be assigned, false if not
     */
    private boolean shouldAssignTwoRooms(int[] workloadPerEmployee, int[] roomQuantity, RoomType[] roomTypes) {

        return Arrays.stream(workloadPerEmployee)
                .anyMatch(workload -> workload > 0 &&
                                      // For the first roomType shift to be evaluated (which would correspond to the type of room to be assigned twice)
                                      IntStream.range(0, roomTypes.length)
                                              /*
                                              If two rooms of the same type can be assigned to complete the required workload for
                                              the i employee, assign them and return the new bedPool
                                              */
                                              .filter(roomIndex -> roomQuantity[roomIndex] >= 2)
                                              .anyMatch(roomIndex -> {
                                                  if (workload - roomTypes[roomIndex].getValue() * 2 == 0) {
                                                      return true;
                                                  } else {
                                                      return IntStream.range(0, roomTypes.length)
                                                              .filter(parallelRoomIndex -> roomQuantity[parallelRoomIndex] > 0 &&
                                                                                           roomTypes[roomIndex].getValue() != roomTypes[parallelRoomIndex].getValue())
                                                              .anyMatch(parallelRoomIndex -> workload - roomTypes[roomIndex].getValue() * 2 - roomTypes[parallelRoomIndex].getValue() == 0);
                                                  }
                                              }));

//        for (int workload : workloadPerEmployee) {
//            if (workload > 0) {
//                // For the first roomType shift to be evaluated (which would correspond to the type of room to be assigned twice)
//                for (int j = 0; j < roomTypes.length; j++) {
//                    if (roomQuantity[j] >= 2) {
//                        /*
//                        If two rooms of the same type can be assigned to complete the required workload for
//                        the i employee, assign them and return the new bedPool
//                        */
//                        if (workload - roomTypes[j].getValue() * 2 == 0) {
//                            return true;
//                        }
//                        /*
//                        Else, if the previous condition was false, evaluate if two j rooms minus one room of
//                        a different type (k) can complete employee[i]'s workload.
//                        Just like the previous case, only two j rooms will be assigned if the condition is met.
//                        k will be assigned by canWorkloadBeSatisfied() in the next iteration.
//                        */
//                        for (int k = 0; k < roomTypes.length; k++) {
//
//                            if (roomQuantity[k] > 0
//                                && roomTypes[j].getValue() != roomTypes[k].getValue()
//                                && workload - roomTypes[j].getValue() * 2 - roomTypes[k].getValue() == 0) {
//                                return true;
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//        return false;
    }

    /**
     * If it was selected by the user, the office is assigned to one of the housekeepers
     * that have an odd-numbered workload.
     *
     * @param originalRooms       the rooms to work with
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param bedPool             the remaining workload value
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param workloadPerRoom     workload remaining for each room type
     * @param roomTypes           roomTypes enum array
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return the remaining bedPool
     */
    private int assignORoomIfExists(ArrayList<Room> originalRooms, ObservableList<Housekeeper> selectedHousekeepers,
                                    int[] workloadPerEmployee,
                                    int bedPool, int[] roomQuantity, int[] workloadPerRoom,
                                    RoomType[] roomTypes, ArrayList<ArrayList<Room>> roomArrangement) {

        if (roomQuantity[1] > 0) {
            int[] largestQuantityWIndex = findOddMaxNumberWIndex(workloadPerEmployee);

            if (largestQuantityWIndex[0] > 0) {
                return processEntries(originalRooms, selectedHousekeepers, workloadPerEmployee, largestQuantityWIndex[1],
                        1, bedPool, roomQuantity, workloadPerRoom, roomTypes, roomArrangement);

            }
        }

        return bedPool;
    }

    /**
     * Assesses if there is an odd number in workloadPerEmployee and if it can be evened out.
     *
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param roomQuantity        the array of the remaining rooms of each type
     * @return true if there was at least one odd number in workloadPerEmployee and there is at least one MA room.
     */
    private boolean oddNumberedWorkloadAmountExists(int[] workloadPerEmployee, int[] roomQuantity) {

        return Arrays.stream(workloadPerEmployee)
                       .anyMatch(workload -> workload % 2 != 0)
               && roomQuantity[2] > 0;

    }

    /**
     * Invokes assignRoom() if the following conditions are met:
     * The workload of the current employee is not 0.
     * Assigning a room would complete its work quota, or it would result in it being greater than 1.
     *
     * @param selectedRooms       the rooms to work with
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param employeeIndex       the index of the employee in workloadPerEmployee
     * @param roomIndex           the index of the room in roomQuantity, workloadPerRoom and roomTypes
     * @param bedPool             the remaining bedPool value
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param workloadPerRoom     workload remaining for each room type
     * @param roomTypes           roomTypes enum array
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return the remaining bedPool
     */
    private int processEntries(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers, int[] workloadPerEmployee, int employeeIndex,
                               int roomIndex, int bedPool, int[] roomQuantity,
                               int[] workloadPerRoom, RoomType[] roomTypes, ArrayList<ArrayList<Room>> roomArrangement) {

        if (workloadPerEmployee[employeeIndex] != 0 &&
            (workloadPerEmployee[employeeIndex] - roomTypes[roomIndex].getValue() == 0 || workloadPerEmployee[employeeIndex] - roomTypes[roomIndex].getValue() > 1)) {

            bedPool = assignRoom(selectedRooms, selectedHousekeepers, workloadPerEmployee,
                    employeeIndex, roomIndex, bedPool, roomQuantity, workloadPerRoom, roomTypes, roomArrangement);
        }

        return bedPool;
    }

    /**
     * Assigns a room to a housekeeper on their preferred floor if available.
     * Reduces the housekeeper's remaining workload and updates relevant parameters accordingly.
     *
     * <p>If a room of the specified type is available on the preferred floor of the housekeeper,
     * the housekeeper's workload is reduced by the value of the room type, as well as the remaining bedPool.
     * Additionally, the room quantity of the assigned type is decremented by one.
     * The assigned room is then added to the employee's allocation list in roomArrangement,
     * and removed from the list of remaining rooms.
     *
     * @param originalRooms       the rooms to work with
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param employeeIndex       the index of the employee in workloadPerEmployee
     * @param roomIndex           the index of the room in roomQuantity, workloadPerRoom and roomTypes
     * @param bedPool             the remaining bedPool value
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param workloadPerRoom     workload remaining for each room type
     * @param roomTypes           roomTypes enum array
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return the remaining bedPool
     */
    private int assignRoom(ArrayList<Room> originalRooms, ObservableList<Housekeeper> selectedHousekeepers, int[] workloadPerEmployee, int employeeIndex,
                           int roomIndex, int bedPool, int[] roomQuantity,
                           int[] workloadPerRoom, RoomType[] roomTypes, ArrayList<ArrayList<Room>> roomArrangement) {

        workloadPerEmployee[employeeIndex] -= roomTypes[roomIndex].getValue();
        bedPool -= roomTypes[roomIndex].getValue();
        roomQuantity[roomIndex]--;
        workloadPerRoom[roomIndex] -= roomTypes[roomIndex].getValue();
        String floor = selectedHousekeepers.get(employeeIndex).preferredFloor();
        var room = Objects.requireNonNull(getAnyRoomOfType(roomTypes[roomIndex], originalRooms, floor));
        roomArrangement.get(employeeIndex).add(room);
        originalRooms.remove(room);
        return bedPool;
    }

    /**
     * Invokes assignRoom() with looser conditions:
     * The only condition is that the remaining workload for an employee has to be equal or greater than 0
     *
     * @param originalRooms       the rooms to work with
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @param employeeIndex       the index of the employee in workloadPerEmployee
     * @param roomIndex           the index of the room in roomQuantity, workloadPerRoom and roomTypes
     * @param bedPool             the remaining bedPool value
     * @param roomQuantity        the array of the remaining rooms of each type
     * @param workloadPerRoom     workload remaining for each room type
     * @param roomArrangement     the arrayList where each distribution is allocated
     * @return the remaining bedPool
     */
    private int processEntriesUnchecked(ArrayList<Room> originalRooms, ObservableList<Housekeeper> selectedHousekeepers, int[] workloadPerEmployee, int employeeIndex,
                                        int roomIndex, int bedPool, int[] roomQuantity,
                                        int[] workloadPerRoom, RoomType[] values, ArrayList<ArrayList<Room>> roomArrangement) {

        if (workloadPerEmployee[employeeIndex] >= 0) {
            bedPool = assignRoom(originalRooms, selectedHousekeepers, workloadPerEmployee, employeeIndex, roomIndex,
                    bedPool, roomQuantity, workloadPerRoom, values, roomArrangement);
        }
        return bedPool;

    }

    /**
     * Adds all the remaining rooms to the last remaining employee and returns a bedPool of 0.
     * The while loop will be exited after this method's execution.
     *
     * @param originalRooms   the last remaining rooms
     * @param employeeIndex   the employee's index in roomArrangement
     * @param roomArrangement the arrayList where each distribution is allocated
     * @return 0
     */
    private int processLastEntry(ArrayList<Room> originalRooms, int employeeIndex,
                                 ArrayList<ArrayList<Room>> roomArrangement) {

        roomArrangement.get(employeeIndex).addAll(originalRooms);
        originalRooms.clear();

        return 0;
    }

    /**
     * Sets the global variable employeeIndex to the index of the last remaining employee.
     *
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     */
    private void setLastWorkloadIndex(int[] workloadPerEmployee) {

        for (int i = 0; i < workloadPerEmployee.length; i++) {
            if (workloadPerEmployee[i] != 0) {
                employeeIndex = i;
            }
        }

    }

    /**
     * Evaluates whether there is only one employee remaining. This is determined
     * by the length of workloadPerEmployee after subtracting all elements that are not 0
     *
     * @param workloadPerEmployee the remaining work quota each employee has to meet
     * @return true if only one employee remains
     */
    private boolean isLastWorkload(int[] workloadPerEmployee) {
        return Arrays.stream(workloadPerEmployee).filter(value -> value != 0).toArray().length == 1;
    }


    /**
     * Iterates through each employee's shift quota and evaluates if it could be satisfied by assigning one room
     * of any given type.
     *
     * @param workloadPerEmployee array of workload quotas
     * @param roomTypes           the roomType values
     * @param roomQuantity        the remaining amount of rooms of a given type
     * @return true if the shift quota for any employee can be satisfied, false if not
     */
    private boolean canWorkloadQuotaBeSatisfied(int[] workloadPerEmployee, RoomType[] roomTypes, int[] roomQuantity) {

        for (int i = 0; i < workloadPerEmployee.length; i++) {

            for (int j = 0; j < roomTypes.length; j++) {

                if (roomQuantity[j] > 0 && workloadPerEmployee[i] - roomTypes[j].getValue() == 0) {
                    indexRoomQuota = j;
                    employeeIndex = i;
                    return true;
                }

            }

        }
        return false;
    }

    /**
     * Initializes roomArrangement, roomQuantity and workloadPerRoom.
     *
     * @param originalRooms   the selected rooms
     * @param numberOfMaids   the number of housekeepers
     * @param roomArrangement the arrayList where each distribution is allocated
     * @param roomTypes       roomTypes enum array
     * @param roomQuantity    the remaining amount of rooms of a given type
     * @param workloadPerRoom workload remaining for each room type
     */
    private void initializeArrays(ArrayList<Room> originalRooms, int numberOfMaids,
                                  ArrayList<ArrayList<Room>> roomArrangement, RoomType[] roomTypes,
                                  int[] roomQuantity, int[] workloadPerRoom) {

        // Each array needs to be initialized
        for (int i = 0; i < numberOfMaids; i++) {
            roomArrangement.add(new ArrayList<>());
        }

        for (int i = 0; i < roomTypes.length; i++) {
            // Amount of rooms of each type
            roomQuantity[i] = getQuantityOfRoomType(originalRooms, roomTypes[i]);
            // The total workload calculated by rooms * shift value
            workloadPerRoom[i] = roomTypes[i].getValue() * roomQuantity[i];
        }

    }

    /**
     * Evaluates whether there is a 6 in workloadPerEmployee, and if there are at least two MA rooms.
     *
     * @param workloadPerEmployee array of workload quotas
     * @param roomQuantity        the remaining amount of rooms of a given type
     * @return true if there is a six and two or more MA rooms.
     */
    private boolean isThereSixInArray(int[] workloadPerEmployee, int[] roomQuantity) {

        return Arrays.stream(workloadPerEmployee)
                       .anyMatch(value -> value == 6)
               && roomQuantity[2] >= 2;
    }

    /**
     * Returns the max value in an array along with its index.
     *
     * @param array the provided array
     * @return an int[] with the max value and its index
     */
    private int[] findMaxNumberWIndex(int[] array) {

        int maxValue = -1;
        int maxIndex = -1;

        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }

        return new int[]{maxValue, maxIndex};

    }

    /**
     * Returns the odd-numbered max value in an array along with its index.
     * If there are no odd numbers in the array, the last number in the array with
     * its index is returned.
     *
     * @param array the provided array
     * @return an int[] with the max odd value and its index
     */
    private int[] findOddMaxNumberWIndex(int[] array) {

        int maxIndex = array.length - 1;
        int maxValue = array[maxIndex];


        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] > maxValue && array[i] % 2 != 0) {
                maxValue = array[i];
                maxIndex = i;
            }
        }

        return new int[]{maxValue, maxIndex};

    }

    /**
     * Creates an ArrayList of the remaining rooms of a given type, shuffles it, and if possible returns
     * a room in the desired floor. By nature, the returned room will be random.
     *
     * @param roomType       roomTypes enum array
     * @param remainingRooms the remaining rooms
     * @param preferredFloor the preferred floor of a housekeeper
     * @return a Room object
     */
    private Room getAnyRoomOfType(RoomType roomType, ArrayList<Room> remainingRooms, String preferredFloor) {

        List<Room> filteredRoom = new ArrayList<>(remainingRooms.stream()
                .filter(room -> room.roomType().equals(roomType))
                .toList());

        if (!filteredRoom.isEmpty()) {

            Collections.shuffle(filteredRoom);

            Room room;
            if (!preferredFloor.isEmpty()) {
                room = getPreferredRoom(preferredFloor, filteredRoom);
            } else {
                room = getPreferredRoom("1", filteredRoom);
            }
            if (room != null) return room;

            return filteredRoom.getFirst();
        } else {
            return null;
        }

    }

    /**
     * Returns a room in the desired floor if possible.
     *
     * @param preferredFloor the preferred floor of a housekeeper
     * @param filteredRoom   a list of all the remaining rooms of a desired type
     * @return a room in the desired floor if one was found, else null
     */
    private Room getPreferredRoom(String preferredFloor, List<Room> filteredRoom) {

        return filteredRoom
                .stream()
                .filter(room -> String.valueOf(room.id()).substring(0, 1).equals(preferredFloor))
                .findAny()
                .orElse(null);
    }

    /**
     * Returns the amount of rooms of a given type.
     *
     * @param rooms    the selected rooms
     * @param roomType the provided roomType
     * @return the amount of rooms
     */
    private int getQuantityOfRoomType(ArrayList<Room> rooms, RoomType roomType) {
        return (int) rooms.stream().filter(room -> room.roomType().equals(roomType)).count();
    }

    /**
     * Displays a label for each housekeeper. It indicates their name, preferred floor, and a small square
     * that represents their color.
     *
     * @param housekeepers the list of housekeepers
     */
    private void displayMaidLabels(ObservableList<Housekeeper> housekeepers) {
        displayHotelLogo();

        for (int i = 0; i < housekeepers.size(); i++) {
            var checkbox = new CheckBox();
            var houseKeeperLabel = getHouseKeeperLabel(housekeepers, i, Color.valueOf(housekeepers.get(i).preferredColor()));

            checkbox.setGraphic(houseKeeperLabel);
            employeesVBox.getChildren().
                    add(checkbox);
        }

    }

    /**
     * Creates a ImageView to display the hotel's logo.
     */
    private void displayHotelLogo() {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vientosdelsur/img/logo.png")));
        ImageView e = new ImageView(image);
        employeesVBox.getChildren().add(e);
    }

    /**
     * Generates a label for a housekeeper with relevant information.
     *
     * @param housekeepers  the list of housekeepers
     * @param employeeIndex the index of a housekeeper in housekeepers
     * @param color         a housekeeper's signature color
     * @return a custom label
     */
    private Label getHouseKeeperLabel(ObservableList<Housekeeper> housekeepers, int employeeIndex, Color color) {

        var housekeeper = housekeepers.get(employeeIndex);

        String workHours =
                switch (housekeeper.shift().name()) {
                    case "FULL_TIME" -> "09-17";
                    case "PART_TIME" -> "11-16";
                    default -> throw new IllegalStateException("Unexpected value: " + housekeeper.shift().name());
                };

        return new HouseKeeperLabel(housekeeper.name() + " " + workHours +
                                    "\nPiso: " + (housekeeper.preferredFloor().isEmpty() ? "N/A" : housekeeper.preferredFloor()), color);
    }

    /**
     * Paints each room a housekeeper was assigned with their signature color.
     *
     * @param employeeRooms a list of room distributions
     * @param housekeepers  the list of housekeepers
     */
    private void paintLabels(List<ArrayList<Room>> employeeRooms, List<Housekeeper> housekeepers) {

        for (int i = 0; i < employeeRooms.size(); i++) {

            List<String> roomIds = employeeRooms.get(i)
                    .stream()
                    .map(room -> String.valueOf(room.id()))
                    .toList();

            for (var room : roomsPane.getChildren()) {
                if (room instanceof CheckBox checkBox) {
                    String id = checkBox.getText().split(" ")[0];
                    if (roomIds.contains(id)) {
                        checkBox.setStyle("-fx-background-color: " + toRGBCode(Color.valueOf(housekeepers.get(i).preferredColor())) + ";");
                    }
                }
            }

        }

//        employeeRooms.forEach(roomsArrangement -> {
//            List<String> roomIds = roomsArrangement
//                    .stream()
//                    .map(room -> String.valueOf(room.id()))
//                    .toList();
//
//            roomsPane.getChildren()
//                    .stream()
//                    .filter(node -> node instanceof CheckBox checkBox && roomIds.contains(checkBox.getText().split(" ")[0]))
//                    .forEach(node -> {
//                        int employeeIndex = employeeRooms.indexOf(roomsArrangement);
//                        String preferredColor = housekeepers.get(employeeIndex).preferredColor();
//                        node.setStyle("-fx-background-color: " + toRGBCode(Color.valueOf(preferredColor)) + ";");
//                    });
//        });

    }

    /**
     * Generates a String representation of a Color object to be used in CSS properties
     *
     * @param color the color object
     * @return a String in the format #%02X%02X%02X
     */
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Distributes workload from selected rooms among housekeepers, accounting for full-time and part-time distinctions.
     * Full-time housekeepers are assigned 1.6 times the workload of part-time ones.
     * Workloads are rounded to the nearest integer, which may result in uneven distribution requiring adjustment.
     * This will be done either by subtraction or addition until the sum of workloadDistribution is
     * equal to workload.
     *
     * @param selectedRooms The list of rooms from which workload is to be distributed.
     * @return An array representing the workload distribution among housekeepers.
     */
    private int[] distributeWorkload(ArrayList<Room> selectedRooms, ObservableList<Housekeeper> selectedHousekeepers) {

        int workload = getTotalWorkload(selectedRooms);

        int numberOfMaids = selectedHousekeepers.size();
        int[] maidDistribution = getMaidDistribution(selectedHousekeepers);

        int partTimeMaids = maidDistribution[0];
        int fullTimeMaids = maidDistribution[1];

        float ratio = 1.6f;

        int[] workloadDistribution = new int[numberOfMaids];

        float divider = partTimeMaids + ratio * fullTimeMaids;

        int equalParts = Math.round((float) workload / divider);

        for (int i = 0; i < numberOfMaids; i++) {

            if (i < partTimeMaids) {
                workloadDistribution[i] = equalParts;
            } else {
                workloadDistribution[i] = Math.round(equalParts * ratio);
            }

        }

        int totalCalculatedWorkload = Arrays.stream(workloadDistribution).sum();

        if (totalCalculatedWorkload > workload) {
            var run = true;
            while (run) {
                for (int i = numberOfMaids - 1; i >= 0; i--) {
                    workloadDistribution[i] -= 1;
                    totalCalculatedWorkload -= 1;
                    if (totalCalculatedWorkload == workload) {
                        run = false;
                        break;
                    }
                }
            }


        } else if (totalCalculatedWorkload < workload) {
            var run = true;
            while (run) {
                for (int i = 0; i < numberOfMaids; i++) {
                    workloadDistribution[i] += 1;
                    totalCalculatedWorkload += 1;
                    if (totalCalculatedWorkload == workload) {
                        run = false;
                        break;
                    }
                }
            }

        }

        return workloadDistribution;
    }

    /**
     * Obtains the total amount of work that is to be distributed among each housekeeper.
     *
     * @param roomArray all the selected rooms
     * @return the total amount of work in roomArray
     */
    private int getTotalWorkload(ArrayList<Room> roomArray) {

        return roomArray
                .stream()
                .mapToInt(room -> room.roomType().getValue())
                .sum();

    }

    /**
     * Returns an array with the amount of part-time and full-time housekeepers.
     *
     * @param houseKeeperArray list of housekeepers
     * @return int[] number of part-time and full time housekeepers
     */
    private int[] getMaidDistribution(ObservableList<Housekeeper> houseKeeperArray) {

        Map<Shift, Integer> shiftCounts = houseKeeperArray
                .stream()
                // Sum 1 for each type of shift
                .collect(Collectors.groupingBy(Housekeeper::shift, Collectors.summingInt(housekeeper -> 1)));

        int partTimeM = shiftCounts.getOrDefault(Shift.PART_TIME, 0);
        int fullTimeM = shiftCounts.getOrDefault(Shift.FULL_TIME, 0);

        return new int[]{partTimeM, fullTimeM};
    }


}