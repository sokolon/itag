package com.example.ble.ble;

import java.util.UUID;

public class Beacon {
    java.util.UUID UUID;
    String name;

    public Beacon(String name) {
        this.UUID = UUID.randomUUID();
        this.name = name;
    }

    public UUID getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }
}
