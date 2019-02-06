package com.example.ble.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

public class Activity2 extends AppCompatActivity { //tworzymy klasę o nazwię Activity2, zaw w sobie AppCompatActivity
    private CustomAdapter adapter;  // zmienna/atrybut klasy, typu private, nazwa Adapter, typ Array, wartosc=znak adapter
    private BluetoothAdapter btAdapter; // analogicznie
    private final static int REQUEST_PERMISSION_REQ_CODE = 76; // any 8-bit number // zmienna typu private final static int, nazwa REQUEST...(), wart =76
    public static int REQUEST_BLUETOOTH = 1; //analogicznie

    private BackgroundBluetoothLEBinder myBinder = new BackgroundBluetoothLEBinder();

    private LocalBroadcastManager broadcaster;

    private BluetoothDevice mDevice;

    private HashMap<String, BluetoothGatt> bluetoothGatt = new HashMap<>();

    private BluetoothGattService immediateAlertService;

    private BluetoothGattService linkLossService;

    public static final int NO_ALERT = 0x00;
    public static final int MEDIUM_ALERT = 0x01;
    public static final int HIGH_ALERT = 0x02;

    public static final String IMMEDIATE_ALERT_AVAILABLE = "IMMEDIATE_ALERT_AVAILABLE";
    public static final String BATTERY_LEVEL = "BATTERY_LEVEL";
    public static final String GATT_CONNECTED = "GATT_CONNECTED";
    public static final String SERVICES_DISCOVERED = "SERVICES_DISCOVERED";
    public static final String RSSI_RECEIVED = "RSSI_RECEIVED";

    public static final UUID IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID LINK_LOSS_SERVICE = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID ALERT_LEVEL_CHARACTERISTIC = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public static final String TAG = Activity2.class.toString();
    public static final String ACTION_PREFIX = "net.sylvek.itracing2.action.";
    public static final long TRACK_REMOTE_RSSI_DELAY_MILLIS = 5000L;
    public static final int FOREGROUND_ID = 1664;
    public static final String BROADCAST_INTENT_ACTION = "BROADCAST_INTENT";
    @Override
    protected void onCreate(Bundle savedInstanceState) { //metoda onCreate - tu zaczyna się poelcenie skanowania
        super.onCreate(savedInstanceState); //jesli chcemy dziedziczyc w javie klasy, aby wykonac metode klasy nalezy uzyc metody super.onCreate
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { //pobieranie pakietów na potrzeby urządzenia-tel
            Toast.makeText(this, "BLE Not Supported", // wyświetlenie informacji, jeżeli nie znajdzie urządzeń
                    Toast.LENGTH_SHORT).show(); // długość czasu trwania tej informacji
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter(); //jakiś manager, punkt zarządzania urządzeniami bluetooth który jest niezbędny do wyszukiwania i zarządzania bluetooth

        if (btAdapter == null || !btAdapter.isEnabled()) { //adapter bluetooth, jeśli nie ma dost urządzen lub jeśli są dostępne
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH); // linijka 71-73 przyznanie uprawnien /wlaczenie bluetooth na urzadzeniu
        }
        setContentView(R.layout.activity_2);
        Toolbar toolbar = findViewById(R.id.toolbar); //76-76 powiazanie xml z metodami (funkcja-wyglad)


        setSupportActionBar(toolbar); //zainicjowanie actionbarra

        adapter = new CustomAdapter(BeaconStorage.ListOfBeacons.List,getApplicationContext());

        ListView list = findViewById(R.id.listView); //połączenie listview z adapterem wyzej

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {



            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { //onItemClick - klikanie na itemy
                // bedace na naszej activity

                connect(BeaconStorage.ListOfBeacons.List.get((int)l).Address);
                BeaconStorage.ListOfBeacons.ActiveId = l; // lista beacnow
                openActivity3(view);
            }
        });

    }

    public void ShowPopupWindow(Beacon beacon) {

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        //show image
        final ImageView ImageView = popupView.findViewById(R.id.imageView3);
        ImageView.setBackgroundResource(beacon.getImage());

        //create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //show the popup window
        LinearLayout layout = new LinearLayout(this);
        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);


        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        stopScanLeDevice();


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                scanLeDevice();

            }

        });
    }

    @Override
    protected void onDestroy() { //koniec zadan. Wyswietlilo nam liste i starczy
        super.onDestroy();

    }

   @Override
    protected void onPause() { // pausa na programie, wykorzystywane do niezapisanych
       //zmian w trwalych danych, zatrzymanie animacji czy innych danych - procesor

       super.onPause();// szybki start poprzedniej czynnosci - w tym przypadku skan


       stopScanLeDevice();

   }

   private void stopScanLeDevice(){
       btAdapter.stopLeScan(mLeScanCallback);
   }


    @Override
    protected void onResume() { //start when is interacting with the user
        scanLeDevice(); //always followed
        super.onResume();
    }

    public void openActivity3(View view) {
                Intent intent = new Intent(this, Activity3.class);
                startActivity(intent);
            }

    private void scanLeDevice() { //startScan - a dalej 136-139 ne wiem co sie dzieje
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                return;
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return; // nie wiem co sie dzieje 142-143
        }


        btAdapter.startLeScan(mLeScanCallback);

    }
    
    private void connect(String address)
    {
        BluetoothDevice mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        BluetoothGatt gat = mDevice.connectGatt(this, true, new CustomBluetoothGattCallback(address));

        for (BluetoothGattService service : gat.getServices())
        {
            if(service.getUuid().equals(IMMEDIATE_ALERT_SERVICE))
            {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(0);
                characteristic.setValue(HIGH_ALERT, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gat.writeCharacteristic(characteristic);
            }
        }

    }

        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                // Create a new device item

                Beacon newDevice = new Beacon(device.getName(), device.getAddress(), rssi);
                // Add it to our adapter

                if (newDevice.getAddress().equals("F6:8E:A3:B4:D7:FB")) {
                    newDevice.AssignBeacon("The best seal!", R.drawable.newproduct, "Seal");
                } else if (newDevice.getAddress().equals("F4:6A:1C:97:E3:D7")) {
                    newDevice.AssignBeacon("Business card", R.drawable.kodqrkarol, "Karol Szostak");
                } else if (newDevice.getAddress().equals(("C1:6C:87:52:E6:83"))) {
                    newDevice.AssignBeacon("The best RFID gate", R.drawable.bramkarfid, "Gate RFID");
                }
                BeaconStorage.ListOfBeacons.List.add(newDevice);

                if (newDevice.getBeaconRange() == DistanceRange.Immediate) {
                    ShowPopupWindow(newDevice);
                }

                adapter.notifyDataSetChanged();
            }
        };

    private class CustomBluetoothGattCallback extends BluetoothGattCallback {

        private final String address;

        CustomBluetoothGattCallback(final String address) {
            this.address = address;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange() address: " + address + " status => " + status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d(TAG, "onConnectionStateChange() address: " + address + " newState => " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    broadcaster.sendBroadcast(new Intent(GATT_CONNECTED));
                    gatt.discoverServices();
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close();
                }
            }

            final boolean actionOnPowerOff = Preferences.isActionOnPowerOff(Activity2.this, this.address);
            if (actionOnPowerOff || status == 8) {
                Log.d(TAG, "onConnectionStateChange() address: " + address + " newState => " + newState);
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    for (String action : Preferences.getActionOutOfBand(getApplicationContext(), this.address)) {
                        sendAction(Preferences.Source.out_of_range, action);
                    }
                    enablePeerDeviceNotifyMe(gatt, false);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            final Intent rssiIntent = new Intent(RSSI_RECEIVED);
            rssiIntent.putExtra(RSSI_RECEIVED, rssi);
            broadcaster.sendBroadcast(rssiIntent);
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered()");

            launchTrackingRemoteRssi(gatt);

            broadcaster.sendBroadcast(new Intent(SERVICES_DISCOVERED));
            if (BluetoothGatt.GATT_SUCCESS == status) {

                for (String action : Preferences.getActionConnected(getApplicationContext(), this.address)) {
                    sendAction(Preferences.Source.connected, action);
                }

                for (BluetoothGattService service : gatt.getServices()) {

                    Log.d(TAG, "service discovered: " + service.getUuid());

                    if (IMMEDIATE_ALERT_SERVICE.equals(service.getUuid())) {
                        immediateAlertService = service;
                        broadcaster.sendBroadcast(new Intent(IMMEDIATE_ALERT_AVAILABLE));
                        gatt.readCharacteristic(getCharacteristic(gatt, IMMEDIATE_ALERT_SERVICE, ALERT_LEVEL_CHARACTERISTIC));
                        setCharacteristicNotification(gatt, immediateAlertService.getCharacteristics().get(0), true);
                    }

                    if (BATTERY_SERVICE.equals(service.getUuid())) {
                        batteryCharacteristic = service.getCharacteristics().get(0);
                        gatt.readCharacteristic(batteryCharacteristic);
                    }

                    if (FIND_ME_SERVICE.equals(service.getUuid())) {
                        if (!service.getCharacteristics().isEmpty()) {
                            buttonCharacteristic = service.getCharacteristics().get(0);
                            setCharacteristicNotification(gatt, buttonCharacteristic, true);
                        }
                    }

                    if (LINK_LOSS_SERVICE.equals(service.getUuid())) {
                        linkLossService = service;
                    }
                }
                enablePeerDeviceNotifyMe(gatt, true);
            }
        }

        private void launchTrackingRemoteRssi(final BluetoothGatt gatt) {
            if (trackRemoteRssi != null) {
                handler.removeCallbacks(trackRemoteRssi);
            }

            trackRemoteRssi = new Runnable() {
                @Override
                public void run() {
                    gatt.readRemoteRssi();
                    handler.postDelayed(this, TRACK_REMOTE_RSSI_DELAY_MILLIS);
                }
            };
            handler.post(trackRemoteRssi);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorWrite()");
            gatt.readCharacteristic(batteryCharacteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged()");
            final long delayDoubleClick = Preferences.getDoubleButtonDelay(getApplicationContext());

            final long now = SystemClock.elapsedRealtime();
            if (lastChange + delayDoubleClick > now && characteristic.getUuid().equals(lastUuid) && gatt.getDevice().getAddress().equals(lastAddress)) {
                Log.d(TAG, "onCharacteristicChanged() - double click");
                lastChange = 0;
                lastUuid = null;
                lastAddress = "";
                handler.removeCallbacks(r);
                for (String action : Preferences.getActionDoubleButton(getApplicationContext(), address)) {
                    sendAction(Preferences.Source.double_click, action);
                }
            } else {
                lastChange = now;
                lastUuid = characteristic.getUuid();
                lastAddress = gatt.getDevice().getAddress();
                r = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onCharacteristicChanged() - simple click");
                        for (String action : Preferences.getActionSimpleButton(getApplicationContext(), CustomBluetoothGattCallback.this.address)) {
                            sendAction(Preferences.Source.single_click, action);
                        }
                    }
                };
                handler.postDelayed(r, delayDoubleClick);
            }
        }
}


