package com.example.ble.ble;

import java.util.UUID;
import java.util.jar.Attributes;

public class Beacon {

    java.util.UUID UUID;
    String name;
    String Address;
    String Description;
    double Distance;
    int Rssi;
    int ImageID;
    DistanceRange beaconRange;

    public Beacon(String name, String address, int rssi) {
        if(name == null || name.isEmpty())
        {
            name = address;
        }

        this.UUID = java.util.UUID.randomUUID();
        this.name = name;
        this.Address = address;
        this.Rssi = rssi;
        SetBeaconRange();
    }

    private void SetBeaconRange(){
        if(this.Rssi > -60)
        {
            beaconRange = DistanceRange.Immediate;
        } else if (this.Rssi > -60) {
            beaconRange = DistanceRange.Near;
        } else if (this.Rssi > -100){
            beaconRange = DistanceRange.Far;
        } else {
            beaconRange = DistanceRange.FarerThanFar;
        }
    }

    private double ConvertRssiToDistance(){
        // TODO Verify it
        int txPower = -55;

        return Math.pow(10d, ((double) txPower - Rssi) / (10 * 2));

    }

    public String getName()
    {
        return name;
    }

    public void setRssi(int rssi){
        Rssi = rssi;
        SetBeaconRange();
    }

    public String getDescription() { return Description;}

    public UUID getUUID() {
        return UUID;
    }

    public String getAddress() { return Address;}

    public double getDistance() {
        return Math.round(Distance * 100.0) / 100.0;
    }

    public DistanceRange getBeaconRange() {
        return beaconRange;
    }

    public void AssignBeacon(String description, int imageID, String name)
    {
        this.Description = description;
        this.ImageID = imageID;
        this.name = name;
    }

    public int getImage() {
        return ImageID;
    }

    public int getRssi() { return this.Rssi; }
}
