package com.example.ronan.bikepro.Fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.ronan.bikepro.Activities.MainActivity;
import com.example.ronan.bikepro.R;
import com.tooltip.Tooltip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.estimote.sdk.EstimoteSDK.getApplicationContext;


public class BeaconsFragment extends Fragment {

    TextView text;
    BeaconManager beaconManager;
    private Region region;


    private ImageView info;
    private ImageView link;

    public BeaconsFragment() {
        // Required empty public constructor
    }

Bitmap b;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacons, container, false);

        info = (ImageView) rootView.findViewById(R.id.infobeacon);
        link = (ImageView) rootView.findViewById(R.id.link);
        b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_directions_bike_black_24dp);


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tooltip tooltip = new Tooltip.Builder(info)
                        .setText("How it works:\n\nYour phone will automatically link to your bikes sensor once in range\n\n" +
                                "Should there be a break in the communication for any reason you will be alerted.")
                        .setTextColor(ContextCompat.getColor(getContext(), R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan)).show();


                final Animation animation = new AlphaAnimation((float) 0.5, 0); // Change alpha from fully visible to invisible
                animation.setDuration(500); // duration - half a second
                animation.setInterpolator(new LinearInterpolator()); // do not alter
                // animation
                // rate
                animation.setRepeatCount(1); // Repeat animation
                // infinitely
                animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
                // end so the button will
                // fade back in
                info.startAnimation(animation);


            }
        });


        text = (TextView) rootView.findViewById(R.id.text);

        beaconManager = new BeaconManager(getActivity().getApplicationContext());
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(1));
        beaconManager.setRegionExitExpiration(TimeUnit.SECONDS.toMillis(10));


        text = (TextView) rootView.findViewById(R.id.text);


        //moinitering
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(region = new com.estimote.sdk.Region("monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        43739,null));

                Log.v("**test", "connect");
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onEnteredRegion(com.estimote.sdk.Region region, List<Beacon> list) {

                beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(1));

                showNotification("Beacon in range","Link established with bike");
                text.setText("Connection established.\n\nYou will receive a notification if your bike begins to move.\n\nBeacon ID: "+list.get(0).getMinor());
                link.setImageResource(R.drawable.ic_bluetooth_connected_green_48dp);
                Log.v("**test", "enter");
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onExitedRegion(com.estimote.sdk.Region region) {
                showNotification("Beacon out of range","Link with bike lost check on bike ASAP");
                link.setImageResource(R.drawable.ic_bluetooth_connected_red_48dp);
                text.setText("Connection lost.\n\nLast seen: "+getTime());
                Log.v("**test", "exit");


            }


        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());
    }

    @Override
    public void onPause() {
        beaconManager.stopMonitoring(region);
        super.onPause();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(getActivity().getApplicationContext(), 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getActivity().getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setLargeIcon(b)
                .setSmallIcon(R.drawable.ic_motorcycle_black_24dp)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


    public String getTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
        String datetime = dateformat.format(c.getTime());
        return datetime;
    }
}
