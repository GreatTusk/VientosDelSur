package com.vientosdelsur.enums;

public enum RoomType {

    M(2),
    O(1),
    MA(3),
    C(4);

    private final int value;

    RoomType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
