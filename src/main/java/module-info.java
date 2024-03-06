module com.vientosdelsur.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires com.google.gson;
    requires javafx.graphics;
    requires org.controlsfx.controls;

    opens com.vientosdelsur.main to javafx.fxml;
    exports com.vientosdelsur.main;
    exports com.vientosdelsur.controller;
    exports com.vientosdelsur.model;
    opens com.vientosdelsur.controller to javafx.fxml;
    exports com.vientosdelsur.enums;
}