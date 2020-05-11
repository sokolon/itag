package com.example.ble.ble;

import java.util.ArrayList;

public class BeaconList extends ArrayList<Beacon> {
    @Override
    public boolean add(Beacon beacon) { // prawda/fałsz czy dodany beacon

        for (Beacon addedBeacon : this) {
            if (addedBeacon.getAddress().equals(beacon.getAddress())) {


                //beacon.getRssi();
                addedBeacon.setRssi(beacon.getRssi());
                //beacon.getName();

                return true; // koniec metoddy
            }

        }

        if (beacon.getAddress().equals("FC:58:FA:59:6F:B8")) {
            beacon.AssignBeacon("The best seal!", R.drawable.newproduct, "Seal");
        } else if (beacon.getAddress().equals("F4:6A:1C:97:E3:D7")) {
            beacon.AssignBeacon("Business card", R.drawable.kodqrkarol, "Karol Szostak");
        } else if (beacon.getAddress().equals(("C1:6C:87:52:E6:83"))) {
            beacon.AssignBeacon("The best RFID gate", R.drawable.bramkarfid, "Gate RFID");
        }
        return super.add(beacon); //dodanie elementu do listy
    }


}
