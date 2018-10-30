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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class Activity2 extends AppCompatActivity { //tworzymy klasę o nazwię Activity2, zaw w sobie AppCompatActivity
    private ArrayAdapter adapter;  // zmienna/atrybut klasy, typu private, nazwa Adapter, typ Array, wartosc=znak adapter
    private List<String> beaconNames; // analogicznie
    private BluetoothAdapter btAdapter; // analogicznie
    private boolean mIsScanning = false; //typ prawda/falsz

    private Button mScanButton;

    private Button button_scan_again; //zadeklarowałam mój button nowy który jest na activity2


    private final static int REQUEST_PERMISSION_REQ_CODE = 76; // any 8-bit number // zmienna typu private final static int, nazwa REQUEST...(), wart =76

    public static int REQUEST_BLUETOOTH = 1; //analogicznie


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

        beaconNames = BeaconStorage.ListOfBeacons.BeaconNames; //deklaracja i inicjalizacja zmiennej nazwy beaconow gdzie wartos znajdziemy w pamieci beacona, z listy beaconow a na koncu pobieramy names

        adapter = new ArrayAdapter(this, R.layout.elementy_listy, R.id.textView, beaconNames); // INICJALIZACJA ADAPTERA
        // za kazdym razem gdy chcemuy utworzyc przewijana liste uzywamy listview za pomoca
        //adaptera. Najprostszym jest ArrayAdapter ponieważ adapter przekształca ArrayList obiektów w View items załadowanych do kontenera/zbiornika/zbioru  ListView.
        //ArrayAdapter mieści się pomiędzy ArrayList (źródło danych) i ListView (reprezentacja wizualna) i konfiguruje dwa aspekty:
        //Która tablica ma być używana jako źródło danych dla listy
        //Jak przekonwertować dowolny element w tablicy na odpowiedni obiekt View

        ListView list = findViewById(R.id.listView); //połączenie listview z adapterem wyzej

        list.setAdapter(adapter); //tak samo jw

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() { //pobieranie wartosci i wrzucanie na liste

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { //onItemClick - klikanie na itemy
                // bedace na naszej activity

                BeaconStorage.ListOfBeacons.ActiveId = l; // lista beacnow
                openActivity3(view);
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
        //
        super.onPause();// szybki start poprzedniej czynnosci - w tym przypadku skan
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(mScanCallback);
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

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).setUseHardwareFilteringIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().build());
        scanner.startScan(filters, settings, mScanCallback);

    }

        private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(List<no.nordicsemi.android.support.v18.scanner.ScanResult> results) {
            BeaconStorage.ListOfBeacons.List.clear();
            for (no.nordicsemi.android.support.v18.scanner.ScanResult result : results){
                // Create a new device item
                BluetoothDevice btDevice = result.getDevice();
                Beacon newDevice = new Beacon(btDevice.getName(), btDevice.getAddress(), result.getRssi());
                // Add it to our adapter
                BeaconStorage.ListOfBeacons.List.add(newDevice);
                BeaconStorage.ListOfBeacons.UpdateBeaconNames();
            }
            adapter.notifyDataSetChanged();
            super.onBatchScanResults(results);
        }

    };


}



