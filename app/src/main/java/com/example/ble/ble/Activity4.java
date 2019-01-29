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
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class Activity4 extends AppCompatActivity {

    TextView distanceView;

    ProgressBar distanceBar;

    BluetoothAdapter BTAdapter;

    Boolean isScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isScanning = false;
        setContentView(R.layout.activity_4);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        distanceView = findViewById(R.id.distanceValueTextView);

        distanceBar = findViewById(R.id.distanceBar);

        distanceView.setText((Double.toString(BeaconStorage.ListOfBeacons.getActiveBeacon().getRssi())));
        setProgress(BeaconStorage.ListOfBeacons.getActiveBeacon().beaconRange);

        scanDistance();

    }

    private void setProgress(DistanceRange range) {

        switch (range) {
            case FarerThanFar:
                distanceBar.setProgress(0);
                break;
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

    @Override
    protected void onResume() {
        super.onResume();
        scanDistance();
    }

    @Override
    protected void onPause() {
        if(isScanning) {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mScanCallback);
        }
        super.onPause();
    }

    private void scanDistance() {
        if(isScanning)
        {
            return;
        }

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).setUseHardwareFilteringIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setDeviceAddress(BeaconStorage.ListOfBeacons.getActiveBeacon().getAddress()).build()); //.setServiceUuid(mUuid).build());
        scanner.startScan(filters, settings, mScanCallback);
        isScanning = true;
    }


    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (no.nordicsemi.android.support.v18.scanner.ScanResult result : results){

                BeaconStorage.ListOfBeacons.getActiveBeacon().setRssi(result.getRssi());
                distanceView.setText(Double.toString(result.getRssi()));
                setProgress(BeaconStorage.ListOfBeacons.getActiveBeacon().getBeaconRange());

                TextView updateView = findViewById(R.id.imthere);
                updateView.setText("Update at " + Calendar.getInstance().getTime() );
            }

            super.onBatchScanResults(results);
        }
    };
}
