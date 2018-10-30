package com.example.ble.ble;

import java.util.LinkedList;

public final class BeaconStorage {
    private BeaconStorage() {}

    public static class ListOfBeacons {
        public static BeaconList List = new BeaconList();

        public static long ActiveId = -1;

        public static Beacon getActiveBeacon() {
            if (ActiveId >= 0 && List.size() > ActiveId) {
                return List.get((int) ActiveId);
            }

            return null;
        }

        public static LinkedList<String> BeaconNames = new LinkedList<>();

        public static void UpdateBeaconNames () {
            BeaconNames.clear();

            for (Beacon beacon : List) {
                BeaconNames.add(beacon.name);
            }
        }
    }
}
