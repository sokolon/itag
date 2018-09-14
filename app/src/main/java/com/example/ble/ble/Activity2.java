package com.example.ble.ble;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class Activity2 extends AppCompatActivity {
        private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        //button = findViewById(R.id.button);
        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity3();
            }

        });*/


        String[] elementy = {"Beacon 1", "Beacon 2", "Beacon 1", "Beacon 2"};

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.elementy_listy, R.id.textView, elementy);

        ListView lista = findViewById(R.id.listView);

        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openActivity3(view);
            }
        });
    }



    public void openActivity3(View view) {
                Intent intent = new Intent(this, Activity3.class);
                startActivity(intent);
            }

        }

