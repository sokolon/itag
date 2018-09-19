package com.example.ble.ble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Activity4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        TextView distancView = findViewById(R.id.distanceValueTextView);
        
        
        
        distancView.setText((Double.toString(BeaconStorage.ListOfBeacons.getActiveBeacon().getDistance())));
        setProgress(BeaconStorage.ListOfBeacons.getActiveBeacon().beaconRange);
/*        int maxValue=simpleProgressBar.getMax();
        int progressValue=simpleProgressBar.getProgress();
        simpleProgressBar.setMax(100);
        simpleProgressBar.setProgress(50);*/
    }

    private void setProgress(DistanceRange range){
        final ProgressBar simpleProgressBar=(ProgressBar) findViewById(R.id.simpleProgressBar);
        simpleProgressBar.setIndeterminate(false);
        
        switch (range)
        {
            case Far:
                simpleProgressBar.setProgress(33);
                break;
            case Near:
                simpleProgressBar.setProgress(66);
                break;
            case Immediate:
                simpleProgressBar.setProgress(100);
                break;
                default:
                    throw new IndexOutOfBoundsException("Did not implenent " + range);
        }
    }

}
