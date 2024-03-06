package com.vientosdelsur.customnodes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class HouseKeeperLabel extends Label {
    public HouseKeeperLabel(String text, Color color) {
        super(text);
        setGraphicTextGap(5);
        setAlignment(Pos.CENTER_LEFT);
        setGraphic(new Rectangle(15, 15, color));
        setMaxWidth(Integer.MAX_VALUE);
        setPadding(new Insets(5));
        getStyleClass().add("housekeeper-label");
    }
}
