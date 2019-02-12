package com.example.ble.ble;

public enum DistanceRange {
    Immediate(0),
    Near(1),
    Far(2),
    FarerThanFar(3);

    private int value;
    DistanceRange(int i) {
        this.value = i;
    }

    public int getValue()
    {
        return value;
    }
}
