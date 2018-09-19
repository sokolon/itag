package com.example.ble.ble;

import java.util.UUID;

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
        this.UUID = UUID.randomUUID();
        this.name = name;
        this.Address = address;
        this.Rssi = rssi;
        SetBeaconRange();
    }

    private void SetBeaconRange(){
        this.Distance = ConvertRssiToDistance();
        if(this.Distance < 0.5)
        {
            beaconRange = DistanceRange.Immediate;
        } else if (this.Distance < 1.5) {
            beaconRange = DistanceRange.Near;
        } else {
            beaconRange = DistanceRange.Far;
        }
    }

    private double ConvertRssiToDistance(){
        // TODO Verify it
        int txPower = -55;

        return Math.pow(10d, ((double) txPower - Rssi) / (10 * 2));
    }

    public String getName() {
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

    public double getDistance() { return Distance; }

    public DistanceRange getBeaconRange() {
        return beaconRange;
    }

    // TODO make from DB
    public Boolean IsTag() {
        return Address.equals("9C:AD:97:C8:EC:A4");
    }

    public void AssignBeacon(String description, int imageID)
    {
        this.Description = description;
        this.ImageID = imageID;
    }

    public int getImage() {
        return ImageID;
    }
}
