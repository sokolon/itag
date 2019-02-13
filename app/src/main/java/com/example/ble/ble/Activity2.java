package com.example.ble.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import java.util.Collection;
import java.util.Collections;


public class Activity2 extends AppCompatActivity { //tworzymy klasę o nazwię Activity2, zaw w sobie AppCompatActivity
    private final static int REQUEST_PERMISSION_REQ_CODE = 76; // any 8-bit number // zmienna typu private final static int, nazwa REQUEST...(), wart =76
    public static int REQUEST_BLUETOOTH = 1; //analogicznie
    private CustomAdapter adapter;  // zmienna/atrybut klasy, typu private, nazwa Adapter, typ Array, wartosc=znak adapter
    private BluetoothAdapter btAdapter; // analogicznie

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



        adapter = new CustomAdapter(BeaconStorage.ListOfBeacons.List, getApplicationContext());

        ListView list = findViewById(R.id.listView); //połączenie listview z adapterem wyzej

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { //onItemClick - klikanie na itemy
                                                                    // bedace na naszej activity

                BeaconStorage.ListOfBeacons.ActiveId = l;           // lista beacnow
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

    private void stopScanLeDevice() {
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

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            
            // Create a new device item
            if (device.getName() == null || !device.getName().toLowerCase().equals("itag"))
            {
                return;
            }

            // Add it to our adapter
            Beacon newDevice = new Beacon(device.getName(), device.getAddress(), rssi);

            BeaconStorage.ListOfBeacons.List.add(newDevice);

            adapter.notifyDataSetChanged();
        }
    };


}



