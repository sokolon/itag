package com.example.ble.ble;

import java.util.LinkedList;

public final class BeaconStorage {
    private BeaconStorage() {}

    public static class ListOfBeacons {
        public static LinkedList<Beacon> List = new LinkedList<Beacon>();

        public static LinkedList<String> BeaconNames () {
                 LinkedList<String> names = new LinkedList<String>();

            for (Beacon beacon : List) {
                names.add(beacon.name);
            }

            return names;
        }
    }
}
