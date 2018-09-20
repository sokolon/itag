package com.example.ble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Activity4 extends AppCompatActivity {

    public static int REQUEST_BLUETOOTH = 1;

    TextView distanceView;

    ProgressBar distanceBar;

    BluetoothAdapter BTAdapter;
    //BluetoothLeScanner BLEsScanner;
    //ScanCallback scan;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Button button = findViewById(R.id.scanButton);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               // BTAdapter.cancelDiscovery();

                BTAdapter.startDiscovery();
                if(BTAdapter.isDiscovering())
                {
                    return;
                }
            }
        });



        distanceView = findViewById(R.id.distanceValueTextView);

        distanceBar= findViewById(R.id.distanceBar);
        
        distanceView.setText((Double.toString(BeaconStorage.ListOfBeacons.getActiveBeacon().getDistance())));
        setProgress(BeaconStorage.ListOfBeacons.getActiveBeacon().beaconRange);

        BeaconStorage.ListOfBeacons.getActiveBeacon().getRSSI();

        scanDistance();

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                if(BluetoothDevice.EXTRA_NAME == BeaconStorage.ListOfBeacons.getActiveBeacon().getName()){
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                    distanceView.setText((Double.toString(BeaconStorage.ListOfBeacons.getActiveBeacon().getRSSI())));
                }
            }
        }
    };

    private void setProgress(DistanceRange range){
        
        switch (range)
        {
            case Far:
                distanceBar.setProgress(33);
                break;
            case Near:
                distanceBar.setProgress(66);
                break;
            case Immediate:
                distanceBar.setProgress(100);
                break;
                default:
                    throw new IndexOutOfBoundsException("Did not implenent " + range);
        }
    }

    private void scanDistance() {

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        if (BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);

            IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(bReciever, ifilter);
            BTAdapter.startDiscovery();
        }
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!device.getAddress().equals(BeaconStorage.ListOfBeacons.getActiveBeacon().Address))
                {
                    return;
                }

                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                BeaconStorage.ListOfBeacons.getActiveBeacon().setRssi(rssi);
                distanceView.setText(Double.toString(BeaconStorage.ListOfBeacons.getActiveBeacon().getDistance()));
                setProgress(BeaconStorage.ListOfBeacons.getActiveBeacon().getBeaconRange());
            }
        }
    };

    @Override
    protected void onDestroy() {
        BTAdapter.cancelDiscovery();
        super.onDestroy();
    }
}
