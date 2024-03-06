package com.vientosdelsur.controller;

import com.vientosdelsur.data.Constants;
import com.vientosdelsur.data.Database;
import com.vientosdelsur.data.Json;
import com.vientosdelsur.enums.Shift;
import com.vientosdelsur.model.Housekeeper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class StaffRegistration implements Initializable {

    @FXML
    private TableView<Housekeeper> staffTableView;
    @FXML
    private ComboBox<String> cboFloors;
    @FXML
    private RadioButton rdbFullTime, rdbPartTime;
    @FXML
    private TextField txtName;
    @FXML
    private ColorPicker colorHousekeeper;
    private ObservableList<Housekeeper> housekeepers;
    private Json json;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        json = new Json();
        populateFloorsCbox();

    }

    public void populateHousekeeperTable() {
        var firstNameCol = new TableColumn<Housekeeper, String>("Nombre");
        firstNameCol.setCellValueFactory(p -> (new SimpleStringProperty(p.getValue().name())));

        var workloadCol = new TableColumn<Housekeeper, String>("Turno");
        workloadCol.setCellValueFactory(p -> (new SimpleStringProperty(getWorkloadStringValue(p.getValue().shift()))));

        var preferredFloorCol = new TableColumn<Housekeeper, String>("Piso preferencial");
        preferredFloorCol.setCellValueFactory(p -> (new SimpleStringProperty(p.getValue().preferredFloor().isEmpty() ? "N/A" : p.getValue().preferredFloor())));

        var colorCol = new TableColumn<Housekeeper, Rectangle>("Color");
        colorCol.setCellValueFactory(p -> new SimpleObjectProperty<>(new Rectangle(15, 15, Color.valueOf(p.getValue().preferredColor()))));

        staffTableView.getColumns().addAll(firstNameCol, workloadCol, preferredFloorCol, colorCol);
        staffTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        staffTableView.setItems(housekeepers);
        staffTableView.setRowFactory(tv -> {
            TableRow<Housekeeper> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Housekeeper selectedItem = row.getItem();
                    txtName.setText(selectedItem.name());
                    cboFloors.getSelectionModel().select(selectedItem.preferredFloor());
                    colorHousekeeper.setValue(Color.valueOf(selectedItem.preferredColor()));
                    if (selectedItem.shift() == Shift.PART_TIME){
                        rdbPartTime.setSelected(true);
                    } else {
                        rdbFullTime.setSelected(true);
                    }

                }
            });
            return row;
        });
    }

    private String getWorkloadStringValue(Shift workload) {
        return
                switch (workload) {
                    case FULL_TIME -> "09:00 - 17:00";
                    case PART_TIME -> "11:00 - 16:00";
                };
    }

    private void populateFloorsCbox() {
        String[] floors = new String[]{"1", "2", "3", "4"};
        for (var floor : floors) {
            cboFloors.getItems().add(floor);
        }
    }

    public void registerHousekeeper() {

        var selectedFloor = cboFloors.getSelectionModel().getSelectedItem();
        var name = txtName.getText().trim();
        var preferredColor = colorHousekeeper.getValue().toString();
        var shift = rdbFullTime.isSelected() ? Shift.FULL_TIME : Shift.PART_TIME;

        var housekeeper = new Housekeeper(shift, name, selectedFloor, preferredColor);
        if (!housekeepers.contains(housekeeper)) {
            housekeepers.add(housekeeper);
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setHeaderText("Housekeeper has been successfully registered.");
//            alert.setTitle("Success");
//            alert.setContentText(name + " has been successfully registered.");
        }


    }

    public ObservableList<Housekeeper> getHousekeepers() {
        return housekeepers;
    }

    public void setHousekeepers(ObservableList<Housekeeper> housekeepers) {
        this.housekeepers = housekeepers;
    }
}
