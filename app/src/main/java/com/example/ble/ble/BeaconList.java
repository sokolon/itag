package com.example.ble.ble;

import java.util.LinkedList;

public class BeaconList extends LinkedList<Beacon> {
    @Override
    public boolean add(Beacon beacon) {
        for(Beacon addedBeacon : this)
        {
            if(addedBeacon.getAddress().equals(beacon.getAddress())){
                return true;
            }
        }

        if(beacon.getAddress().equals("F6:8E:A3:B4:D7:FB"))
        {
            beacon.AssignBeacon("Zapraszamy na dział drogierie!", R.drawable.drogeria, "DZIAŁ DROGERIE");
        }
        else if(beacon.getAddress().equals("F4:6A:1C:97:E3:D7"))
        {
            beacon.AssignBeacon("Beacon description", R.drawable.beacon1, "ISSRFID");
        }

        return super.add(beacon);
    }
}
