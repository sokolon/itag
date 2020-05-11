package com.example.ble.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.UUID;

public class ITagService extends Service {

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

    public class BackgroundBluetoothLEBinder extends Binder {
        public ITagService service() {
            return ITagService.this;
        }
    }

    private BackgroundBluetoothLEBinder myBinder = new BackgroundBluetoothLEBinder();
    private LocalBroadcastManager broadcaster;
    private BluetoothDevice mDevice;
    private HashMap<String, BluetoothGatt> bluetoothGatt = new HashMap<>();
    private BluetoothGattService immediateAlertService;
    private BluetoothGattService linkLossService;

    private BluetoothGattCharacteristic batteryCharacteristic;

    private BluetoothGattCharacteristic buttonCharacteristic;

    private long lastChange;

    private UUID lastUuid;

    private String lastAddress;

    private Runnable r;

    private Handler handler = new Handler();

    private Runnable trackRemoteRssi = null;

    private Button button_connect;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy() {
        if (trackRemoteRssi != null) {
            handler.removeCallbacks(trackRemoteRssi);
        }

        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.connect(intent.getData().getHost());

        if (intent.getData() != null) {
            final String address = intent.getData().getHost();
            this.immediateAlert(address, HIGH_ALERT);
        }

        return START_STICKY;
    }

    public class Discovering implements Runnable {
        private String address;
        public Discovering(String _data) {
            this.address = _data;
        }

        @Override
        public void run() {
            boolean state = bluetoothGatt.get(address).discoverServices();
            Log.d(TAG,"State Service Discovered: "+state);
        }
    }

    public void connect(String address) {
        if (!bluetoothGatt.containsKey(address) || bluetoothGatt.get(address) == null) {
            Log.d(TAG, "connect() - (new link) to device " + address);
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            bluetoothGatt.put(address, mDevice.connectGatt(this, true, new CustomBluetoothGattCallback(address)));
        } else {
            Log.d(TAG, "connect() - discovering services for " + address);
            bluetoothGatt.get(address).discoverServices();
        }
    }

    private void setCharacteristicNotification(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, boolean flag) {
        bluetoothgatt.setCharacteristicNotification(bluetoothgattcharacteristic, flag);
        BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothgatt.writeDescriptor(descriptor);
        }
    }

    public void setLinkLossNotificationLevel(String address, int alertType) {
        Log.d(TAG, "setLinkLossNotificationLevel() - the device " + address);
        if (bluetoothGatt.get(address) == null || linkLossService == null || linkLossService.getCharacteristics() == null || linkLossService.getCharacteristics().size() == 0) {
            somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = linkLossService.getCharacteristics().get(0);
        characteristic.setValue(alertType, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bluetoothGatt.get(address).writeCharacteristic(characteristic);
    }

    public void immediateAlert(String address, int alertType) {
        Log.d(TAG, "immediateAlert() - the device " + address);
        if (bluetoothGatt.get(address) == null || immediateAlertService == null || immediateAlertService.getCharacteristics() == null || immediateAlertService.getCharacteristics().size() == 0) {
            somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
        characteristic.setValue(alertType, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bluetoothGatt.get(address).writeCharacteristic(characteristic);
    }

    private synchronized void somethingGoesWrong() {
        Toast.makeText(this, "Something goes wrong!", Toast.LENGTH_LONG).show();
    }

    public void enablePeerDeviceNotifyMe(BluetoothGatt bluetoothgatt, boolean flag) {
        BluetoothGattCharacteristic bluetoothgattcharacteristic = getCharacteristic(bluetoothgatt, FIND_ME_SERVICE, FIND_ME_CHARACTERISTIC);
        if (bluetoothgattcharacteristic != null && (bluetoothgattcharacteristic.getProperties() | 0x10) > 0) {
            setCharacteristicNotification(bluetoothgatt, bluetoothgattcharacteristic, flag);
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt bluetoothgatt, UUID serviceUuid, UUID characteristicUuid) {
        if (bluetoothgatt != null) {
            BluetoothGattService service = bluetoothgatt.getService(serviceUuid);
            if (service != null) {
                return service.getCharacteristic(characteristicUuid);
            }
        }

        return null;
    }

    private class CustomBluetoothGattCallback extends BluetoothGattCallback {

        private final String address;

        CustomBluetoothGattCallback(final String address) {
            this.address = address;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange() address: " + address + " status => " + status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d(TAG, "onConnectionStateChange() address: " + address + " newState => " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    broadcaster.sendBroadcast(new Intent(GATT_CONNECTED));
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                    gatt.discoverServices();
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close();
                }
            }

            final boolean actionOnPowerOff = Preferences.isActionOnPowerOff(ITagService.this, this.address);
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
            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
            Log.d(TAG, "onServicesDiscovered()");

            //Toast.makeText(ITagService.this, "Connected!", Toast.LENGTH_LONG).show();


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
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
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

        private void sendAction(Preferences.Source source, String action) {
            final Intent intent = new Intent(BROADCAST_INTENT_ACTION.equals(action) ? ACTION_PREFIX + action + "." + source : ACTION_PREFIX + action);
            intent.putExtra(Devices.ADDRESS, address);
            intent.putExtra(Devices.SOURCE, source.name());
            sendBroadcast(intent);
            Log.d(TAG, "onCharacteristicChanged() address: " + address + " - sendBroadcast action: " + intent.getAction());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead()");
            if (characteristic.getValue() != null && characteristic.getValue().length > 0) {
                final Intent batteryLevel = new Intent(BATTERY_LEVEL);
                final byte level = characteristic.getValue()[0];
                batteryLevel.putExtra(BATTERY_LEVEL, Integer.valueOf(level));
                broadcaster.sendBroadcast(batteryLevel);
            }
        }
    }
}
