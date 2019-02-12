package com.example.ble.ble;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Beacon> {

    private ITagService service;

    public CustomAdapter(ArrayList<Beacon> data, Context context) {
        super(context, R.layout.customlayout, data);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            if (iBinder instanceof ITagService.BackgroundBluetoothLEBinder) {
                service = ((ITagService.BackgroundBluetoothLEBinder) iBinder).service();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            Log.d(ITagService.TAG, "onServiceDisconnected()");
        }
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Beacon beacon = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.customlayout, parent, false);

        //ImageView imageView2 = convertView.findViewById(R.id.imageView2);
        TextView textViewName = convertView.findViewById(R.id.textViewName);
        ProgressBar progressBar = convertView.findViewById(R.id.distanceBar);

        textViewName.setText(beacon.getName());
        //imageView2.setBackgroundResource(beacon.getImage());
        setProgress(beacon.getBeaconRange(), progressBar);


        Button connectButton = convertView.findViewById(R.id.button_connect);


        Button alertButton = convertView.findViewById(R.id.button_alert);


        return convertView;
    }


    private void setProgress(DistanceRange range, ProgressBar progressBar) {

        switch (range) {
            case FarerThanFar:
                progressBar.setProgress(0);
                break;
            case Far:
                progressBar.setProgress(20);
                break;
            case Near:
                progressBar.setProgress(40);
                break;
            case Immediate:
                progressBar.setProgress(100);
                break;
            default:
                throw new IndexOutOfBoundsException("Did not implenent " + range);
        }
    }
}

