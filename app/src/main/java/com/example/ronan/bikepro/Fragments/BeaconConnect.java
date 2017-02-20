package com.example.ronan.bikepro.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.ronan.bikepro.Activities.MainActivity;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.Helpers.GetAddressFromLOcation;
import com.example.ronan.bikepro.R;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.ronan.bikepro.R.id.model;

/**
 * A simple {@link Fragment} subclass.
 */
public class BeaconConnect extends Fragment implements DatePickerDialog.OnDateSetListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    private Bitmap bitmap;
    private BeaconManager beaconManager;
    private TextView textStatus;
    private TextView choose;
    private TextView lastKnownLocation;

    private Region region;
    private DatabaseReference mDatabase;


    private String dB_KeyRefrence_fromBundle;
    private int BeaconMajorID;
    private int BeaconMinorID;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference stolenBikesDatabse;
    private String uniqueIdentifier;
    private String email;

    private String makeGlobal;
    private String modelGlobal;
    private String colourGlobal;
    private String lastSeenTimeGlobal;
    private String datetFromPicker;
    private String LocationResult;

    private TextView lastDate;

    private GoogleApiClient client;
    protected Location mLastLocation;
    private double latitude;
    private double longditude;


    public BeaconConnect() {
        // Required empty public constructor
    }


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

        client = new GoogleApiClient.Builder(getActivity().getApplicationContext()).addApi(AppIndex.API).build();

        buildGoogleApiClient();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            uniqueIdentifier = email.split("@")[0];
        }


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(uniqueIdentifier).child(dB_KeyRefrence_fromBundle);
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");

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
        beaconManager.setRegionExitExpiration(TimeUnit.SECONDS.toMillis(2));


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
                lastSeenTimeGlobal = getTime();
                textStatus.setText("Connection lost.\n\nLast knon location: " + LocationResult
                        +"\n\nLast seen at: " + lastSeenTimeGlobal);
                choose.setText("**Link lost to the following bike**");
                choose.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                reportArea.setVisibility(View.VISIBLE);
                Log.v("**test", "exit");
            }
        });

        floatingConfirmReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater factory = LayoutInflater.from(v.getContext());
                final View popup = factory.inflate(R.layout.custom_dialog_link_broken_report_stolen, null);
                final AlertDialog alertDialogBuilder = new AlertDialog.Builder(getActivity()).create();


                FloatingActionButton floatingConfirmPopUp = (FloatingActionButton) popup.findViewById(R.id.floatingConfirmPopUp);
                ImageView bike_image = (ImageView) popup.findViewById(R.id.bike_image);
                final ImageView infoDialog = (ImageView) popup.findViewById(R.id.infoDialog);
                TextView makeView = (TextView) popup.findViewById(R.id.make);
                TextView modelView = (TextView) popup.findViewById(R.id.model);
                TextView colorView = (TextView) popup.findViewById(R.id.color);
                lastKnownLocation = (TextView) popup.findViewById(R.id.lastKnownLocation);
                lastDate = (TextView) popup.findViewById(R.id.lastKnownDate);


                lastDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar now = Calendar.getInstance();
                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                BeaconConnect.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );

                        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
                        dpd.setVersion(DatePickerDialog.Version.VERSION_2);

                        lastDate.setText(datetFromPicker);
                    }
                });

                infoDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tooltip tooltip = new Tooltip.Builder(infoDialog)
                                .setText("Confirm details")
                                .setTextColor(ContextCompat.getColor(getContext(), R.color.white))
                                .setDismissOnClick(true)
                                .setCancelable(true)
                                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan)).show();


                        final Animation animation = new AlphaAnimation((float) 0.5, 0); // Change alpha from fully visible to invisible
                        animation.setDuration(500); // duration - half a second
                        animation.setInterpolator(new LinearInterpolator()); // do not alter
                        animation.setRepeatCount(1); // Repeat animation
                        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the

                        info.startAnimation(animation);
                    }
                });


                bike_image.setImageBitmap(bitmap);
                makeView.setText(makeGlobal);
                modelView.setText(modelGlobal);
                colorView.setText(colourGlobal);
                lastKnownLocation.setText(LocationResult);

                alertDialogBuilder.setView(popup);
                alertDialogBuilder.show();
                lastDate.setText(lastSeenTimeGlobal);

                floatingConfirmPopUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.addListenerForSingleValueEvent(reportStolenLinkBroken);
                        beaconManager.stopMonitoring(region);
                        FragmentManager fm = getFragmentManager();
                        fm.beginTransaction().replace(R.id.fragment_container, new WelcomeFragment()).commit();
                        Toast.makeText(getActivity().getApplicationContext(), "Bike Reported stolen", Toast.LENGTH_LONG).show();
                        alertDialogBuilder.dismiss();
                    }
                });

            }

        }); //end onClick report / custome dialog set up

        return rootView;
    }//end onCreate


    @Override
    public void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");
        datetFromPicker = dateformat.format(c.getTime());

        //   Toast.makeText(getActivity().getApplicationContext(), "d: "+datetime, Toast.LENGTH_LONG).show();

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
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");
        String datetime = dateformat.format(c.getTime());
        return datetime;
    }

    public void populateConnectedUIArea() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BikeData b = dataSnapshot.getValue(BikeData.class);

                makeGlobal = b.getMake();
                modelGlobal = b.getModel();
                colourGlobal = b.getColor();

                makeView.setText(makeGlobal);
                modelView.setText(modelGlobal);
                colorView.setText(colourGlobal);
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
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            iv.setImageBitmap(bitmap);
        }
    }// end getBitMapFromString


    protected static final int PERMISSION_ACCESS_COARSE_LOCATION = 0;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("*test", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            latitude = mLastLocation.getLatitude();
            longditude = mLastLocation.getLongitude();

            GetAddressFromLOcation.getAddressFromLocation(mLastLocation, getActivity().getApplicationContext(), new GeocoderHandler());


        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    LocationResult = bundle.getString("address");
                    break;
                default:
                    LocationResult = null;
            }
            // replace by what you need to do
            //myLabel.setText(result);
            //   Toast.makeText(getActivity().getApplicationContext(), LocationResult, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("*test", "Connection suspended");
        client.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    //===================================================================================
    // Firebase event listener for populateing UI fields from DB
    //===================================================================================
    ValueEventListener reportStolenLinkBroken = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.getValue(BikeData.class) == null) {
                Log.v("error", "doing nothing snapshot null");
                return;
            }

            //grab snapshot and put in bike Object
            BikeData mybike = dataSnapshot.getValue(BikeData.class);


            boolean stolen = true;
            String make = mybike.getMake();
            String model = mybike.getModel();
            int frameSize = mybike.getFrameSize();
            String color = mybike.getColor();
            String other = mybike.getOther();
            String lastSeen = LocationResult;
            String beaconID = mybike.getBeaconUUID();
            String base64 = mybike.getImageBase64();

            BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, lastSeen, latitude, longditude, email,beaconID,0);
            stolenBikesDatabse.child(dB_KeyRefrence_fromBundle).setValue(newBike);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
