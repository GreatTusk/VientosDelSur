package com.vientosdelsur.customnodes;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;

public class RoomCheckbox extends CheckBox {

    public RoomCheckbox(String text) {

        super(text);
        setPadding(new Insets(10));
        setSelected(true);
        getStyleClass().add("room-checkbox");
        setOnAction(event -> setStyle("-fx-background-color: null;"));
    }


}
