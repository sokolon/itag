package com.example.ble.ble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Activity3 extends AppCompatActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity4(view);
            }

        });

        Beacon activeBeacon = BeaconStorage.ListOfBeacons.List.get((int) BeaconStorage.ListOfBeacons.ActiveId);


        TextView nameTextView = findViewById(R.id.nameValueTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionValueTextView);
        TextView UUIDTextView = findViewById(R.id.UUIDValueTextView);
        TextView AddressTextView = findViewById(R.id.AddressValueTextView);
        ImageView ImageView = findViewById(R.id.ImageView);

        nameTextView.setText(activeBeacon.getName());
        descriptionTextView.setText(activeBeacon.getDescription());
        UUIDTextView.setText(activeBeacon.getUUID().toString());
        AddressTextView.setText(activeBeacon.getAddress());

        ImageView.setBackgroundResource(activeBeacon.getImage());
    }

    public void openActivity4(View view) {
        Intent intent = new Intent(this, Activity4.class);
        startActivity(intent);
    }
}

