package com.example.ble.ble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;
import java.util.stream.Collectors;

public class Activity2 extends AppCompatActivity {
        private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<String> beaconNames = BeaconStorage.ListOfBeacons.BeaconNames();

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.elementy_listy, R.id.textView, beaconNames);


        ListView lista = findViewById(R.id.listView);

        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BeaconStorage.ListOfBeacons.ActiveId = l;
                openActivity3(view);
            }
        });
    }



    public void openActivity3(View view) {
                Intent intent = new Intent(this, Activity3.class);
                startActivity(intent);
            }

        }

