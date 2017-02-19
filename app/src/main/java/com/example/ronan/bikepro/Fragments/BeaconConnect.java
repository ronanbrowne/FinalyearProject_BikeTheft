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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.ronan.bikepro.Activities.MainActivity;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.ronan.bikepro.R.id.model;

/**
 * A simple {@link Fragment} subclass.
 */
public class BeaconConnect extends Fragment {

    private ImageView info;
    private ImageView link;
    private ImageView bike_image;
    private LinearLayout selectedBike;
    private LinearLayout reportArea;
    private FloatingActionButton floatingConfirmReport;

    private TextView makeView;
    private TextView modelView;
    private TextView colorView;

    private Bitmap b;
    private BeaconManager beaconManager;
    private TextView textStatus;
    private TextView choose;

    private Region region;
    private DatabaseReference mDatabase;


    public BeaconConnect() {
        // Required empty public constructor
    }

    String dB_KeyRefrence_fromBundle;
    int BeaconMajorID;
    int BeaconMinorID;
    private FirebaseUser mFirebaseUser;
    private String uniqueIdentifier;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacon_connect, container, false);

        //get the DB reference key for this particular bike.
        //We set this in previous screen EditBikeList by passin git in arguments as a bundle to this fragment
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            dB_KeyRefrence_fromBundle = bundle.getString("dB_Ref");
            BeaconMajorID = bundle.getInt("BeaconMajorID");
            BeaconMinorID = bundle.getInt("BeaconMinorID");
            Log.d("*bundle", "id: " + BeaconMajorID + " : " + BeaconMinorID);
            Log.d("*bundle", "ref: " + dB_KeyRefrence_fromBundle);

        }


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            uniqueIdentifier = mFirebaseUser.getEmail();
            uniqueIdentifier = uniqueIdentifier.split("@")[0];
        }


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(uniqueIdentifier).child(dB_KeyRefrence_fromBundle);


        info = (ImageView) rootView.findViewById(R.id.infobeacon);
        link = (ImageView) rootView.findViewById(R.id.link);
        floatingConfirmReport = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmReport);
        selectedBike = (LinearLayout) rootView.findViewById(R.id.selectedBike);
        reportArea = (LinearLayout) rootView.findViewById(R.id.reportArea);
        b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_directions_bike_black_24dp);

        makeView = (TextView) rootView.findViewById(R.id.make);
        modelView = (TextView) rootView.findViewById(model);
        colorView = (TextView) rootView.findViewById(R.id.color);
        bike_image = (ImageView) rootView.findViewById(R.id.bike_image);

        selectedBike.setVisibility(View.INVISIBLE);
        reportArea.setVisibility(View.INVISIBLE);
        textStatus = (TextView) rootView.findViewById(R.id.textStatus);
        choose = (TextView) rootView.findViewById(R.id.chooseConnected);

        beaconManager = new BeaconManager(getActivity().getApplicationContext());

        //moinitering
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(region = new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        BeaconMajorID, BeaconMinorID));
                Log.v("**test", "connect");
            }
        });

        beaconManager.setBackgroundScanPeriod(1000, 0);

        beaconManager.setRegionExitExpiration(TimeUnit.SECONDS.toMillis(10));


        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {

                Log.v("**region", "Entered region");

                // beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(1));

                showNotification("Beacon in range", "Link established with bike");
                textStatus.setText("Connection established.\n\nYou will receive a notification if your bike begins to move.\n\n\n");
                link.setImageResource(R.drawable.ic_bluetooth_connected_green_48dp);
                selectedBike.setVisibility(View.VISIBLE);
                reportArea.setVisibility(View.GONE);
                choose.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                populateConnectedUIArea();
                Log.v("**test", "Major ID: " + list.get(0).getMajor());
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onExitedRegion(Region region) {
                showNotification("Beacon out of range", "Link with bike lost check on bike ASAP");
                link.setImageResource(R.drawable.ic_bluetooth_connected_red_48dp);
                textStatus.setText("Connection lost.\n\nLast seen: " + getTime());
                choose.setText("**Link lost to the following bike**");
                choose.setTextColor(ContextCompat.getColor(getContext(), R.color.proximity6));
                reportArea.setVisibility(View.VISIBLE);
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
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
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


    public String getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
        String datetime = dateformat.format(c.getTime());
        return datetime;
    }

    public void populateConnectedUIArea() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BikeData b = dataSnapshot.getValue(BikeData.class);

                makeView.setText(b.getMake());
                modelView.setText(b.getModel());
                colorView.setText(b.getColor());
                getBitMapFromString(b.getImageBase64(), bike_image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //===============================================
    // extract bitmap helper, this sets image view
    //===============================================
    public void getBitMapFromString(String imageAsString, ImageView iv) {
        if (imageAsString == "No image" || imageAsString == null) {
            Log.v("***", "No image Found");
        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            iv.setImageBitmap(bitmap);
        }
    }// end getBitMapFromString

}
