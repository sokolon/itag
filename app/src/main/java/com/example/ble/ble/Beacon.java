package com.example.ble.ble;

import android.support.annotation.NonNull;

import java.util.UUID;

public class Beacon implements Comparable<Beacon>{

    java.util.UUID UUID;
    String name;
    String Address;
    String Description;
    double Distance;
    int Rssi;
    int ImageID;
    DistanceRange beaconRange;

    public Beacon(String name, String address, int rssi) {
        if (name == null || name.isEmpty()) {
            name = address;
        }

        this.UUID = java.util.UUID.randomUUID();
        this.name = name;
        this.Address = address;
        this.Rssi = rssi;
    }

    private double ConvertRssiToDistance() {
        // TODO Verify it
        int txPower = -55;

        return Math.pow(10d, ((double) txPower - Rssi) / (10 * 2));

    }

    public String getName() {
        return name;
    }

    public int getRSSI() {
        return Rssi;
    }

    public String getDescription() {
        return Description;
    }

    public UUID getUUID() {
        return UUID;
    }

    public String getAddress() {
        return Address;
    }

    public double getDistance() {
        return Math.round(Distance * 100.0) / 100.0;
    }

    public DistanceRange getBeaconRange() {
        if (this.Rssi > -60) {
            return beaconRange = DistanceRange.Immediate;
        } else if (this.Rssi > -80) {
            return beaconRange = DistanceRange.Near;
        } else if (this.Rssi > -100) {
            return beaconRange = DistanceRange.Far;
        } else {
            return beaconRange = DistanceRange.FarerThanFar;
        }
    }

    public void AssignBeacon(String description, int imageID, String name) {
        this.Description = description;
        this.ImageID = imageID;
        this.name = name;
    }

    public int getImage() {
        return ImageID;
    }

    public int getRssi() {
        return this.Rssi;
    }

    public void setRssi(int rssi) {
        Rssi = rssi;
    }

    @Override
    public int compareTo(@NonNull Beacon o) {
        if (this.beaconRange.getValue() < o.beaconRange.getValue()){
            return 1;
        }

        if (this.beaconRange.getValue() == o.beaconRange.getValue())
        {
            return 0;
        }

        return -1;
    }
}
