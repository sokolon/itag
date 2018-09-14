package com.example.ble.ble;

public class Beacon {
    String UUID;
    String name;

    public Beacon(String UUID, String name) {
        this.UUID = UUID;
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }
}
