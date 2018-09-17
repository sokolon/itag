package com.example.ble.ble;

import java.util.UUID;

public class Beacon {
    java.util.UUID UUID;
    String name;
    String Address;

    public Beacon(String name, String address) {
        this.UUID = UUID.randomUUID();
        this.name = name;
        this.Address = address;
    }

    public UUID getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }
}
