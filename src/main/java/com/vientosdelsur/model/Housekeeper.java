package com.vientosdelsur.model;

import com.vientosdelsur.enums.Shift;

public record Housekeeper(Shift shift, String name, String preferredFloor, String preferredColor) {
}
