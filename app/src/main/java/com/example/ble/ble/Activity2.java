package com.example.ble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;
import java.util.stream.Collectors;

public class Activity2 extends AppCompatActivity {
    private ArrayAdapter adapter;
    private List<String> beaconNames;
    private BluetoothAdapter BTAdapter;

    public static int REQUEST_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAllBeacons();
        setContentView(R.layout.activity_2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        beaconNames = BeaconStorage.ListOfBeacons.BeaconNames;

        adapter = new ArrayAdapter(this, R.layout.elementy_listy, R.id.textView, beaconNames);


        ListView list = findViewById(R.id.listView);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BeaconStorage.ListOfBeacons.ActiveId = l;
                openActivity3(view);
            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        BTAdapter.cancelDiscovery();
    }

    public void openActivity3(View view) {
                Intent intent = new Intent(this, Activity3.class);
                startActivity(intent);
            }

    private void getAllBeacons() {

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

            Log.d("DEVICELIST", "Super called for DeviceListFragment onCreate\n");

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
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);


                // Create a new device item
                Beacon newDevice = new Beacon(device.getName(), device.getAddress(), rssi);
                // Add it to our adapter
                BeaconStorage.ListOfBeacons.List.add(newDevice);
                BeaconStorage.ListOfBeacons.UpdateBeaconNames();
                adapter.notifyDataSetChanged();

                // TODO Get from Database
                if(newDevice.IsTag()){
                    newDevice.AssignBeacon("This is known Beacon", R.drawable.plomba);
                }
            }

        }
    };
}



