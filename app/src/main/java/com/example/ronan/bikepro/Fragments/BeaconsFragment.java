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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.ronan.bikepro.Activities.MainActivity;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.estimote.sdk.EstimoteSDK.getApplicationContext;
import static com.example.ronan.bikepro.R.id.bike_image;
import static com.example.ronan.bikepro.R.id.choose;
import static com.example.ronan.bikepro.R.id.model;


public class BeaconsFragment extends Fragment {

    TextView text;
    TextView listArea;
    BeaconManager beaconManager;
    private Region region;


    private ImageView info;
    private ImageView link;
    private ImageView bike_image;
    private ImageView bike_imageSelected;
    private ListView listViewChooseBike;
    private LinearLayout selectedBike;
    private LinearLayout reportArea;
    private FloatingActionButton floatingConfirmReport;

    private DatabaseReference myBikesDB;
    private DatabaseReference selectedBikeToLinksTo;
    private Query queryBikesUsingBeacons;
    private FirebaseUser mFirebaseUser;
    private String uniqueIdentifier;

    private int selectedBikeUUID;

    private ArrayList<BikeData> bikes = new ArrayList();
    private TextView makeView;
    private TextView modelView;
    private TextView colorView;

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
        listArea = (TextView) rootView.findViewById(R.id.choose);
        link = (ImageView) rootView.findViewById(R.id.link);
        listViewChooseBike = (ListView) rootView.findViewById(R.id.listViewChooseBike);
        floatingConfirmReport = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmReport);
        selectedBike = (LinearLayout) rootView.findViewById(R.id.selectedBike);
        reportArea = (LinearLayout) rootView.findViewById(R.id.reportArea);
        b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_directions_bike_black_24dp);

        makeView = (TextView) rootView.findViewById(R.id.make);
        modelView = (TextView) rootView.findViewById(model);
        colorView = (TextView) rootView.findViewById(R.id.color);
        bike_imageSelected = (ImageView) rootView.findViewById(R.id.bike_image);

        listViewChooseBike.setVisibility(View.GONE);
        selectedBike.setVisibility(View.GONE);
        reportArea.setVisibility(View.INVISIBLE);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            uniqueIdentifier = mFirebaseUser.getEmail();
            uniqueIdentifier = uniqueIdentifier.split("@")[0];
        }

        //refrence to reurn all bikes registered by that user which are not null
        myBikesDB = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(uniqueIdentifier);


        queryBikesUsingBeacons = myBikesDB.orderByChild("beaconUUID").startAt("!").endAt("~");


        queryBikesUsingBeacons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 1) {
                    Log.d("Count", "more than one " + dataSnapshot.getChildrenCount());
                    listViewChooseBike.setVisibility(View.VISIBLE);

                    setUpListView();
                    Log.v("***", "returned UUIDs " + bikes.size());
                    Log.v("**test**Output2", Arrays.toString(bikes.toArray()));
                } else {
                    Log.d("Count", "just one " + dataSnapshot.getChildrenCount());

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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


        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onEnteredRegion(com.estimote.sdk.Region region, List<Beacon> list) {

                beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(1));

                showNotification("Beacon in range", "Link established with bike");
                text.setText("Connection established.\n\nYou will receive a notification if your bike begins to move.\n\nBeacon ID: " + list.get(0).getMinor());
                link.setImageResource(R.drawable.ic_bluetooth_connected_green_48dp);
                // showStolen(selectedBikeToLinksTo);
                listArea.setText("Link established with");
                listViewChooseBike.setVisibility(View.GONE);
                selectedBike.setVisibility(View.VISIBLE);
                Log.v("**test", "enter");
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onExitedRegion(com.estimote.sdk.Region region) {
                showNotification("Beacon out of range", "Link with bike lost check on bike ASAP");
                link.setImageResource(R.drawable.ic_bluetooth_connected_red_48dp);
                text.setText("Connection lost.\n\nLast seen: " + getTime());
                listArea.setText("Link lost to the following bike");
                reportArea.setVisibility(View.VISIBLE);
                //showStolen(selectedBikeToLinksTo);
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

    public void setUpListView() {


        final FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item_monitering, queryBikesUsingBeacons) {
            @Override
            protected void populateView(View v, BikeData model, int position) {
                //handling displaying of loading bar once data is recieved hide it.
                queryBikesUsingBeacons.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("*count", " " + dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                // Find the TextView IDs of list_item.xml
                TextView makeView = (TextView) v.findViewById(R.id.make);
                TextView modelView = (TextView) v.findViewById(R.id.model);
                TextView colorView = (TextView) v.findViewById(R.id.color);
                bike_image = (ImageView) v.findViewById(R.id.bike_image);

                //setting the textViews to Bike data
                makeView.setText(model.getMake());
                modelView.setText(model.getModel());
                colorView.setText(model.getColor());

                //call method to set image, which turns base64 string to image
                getBitMapFromString(model.getImageBase64(), bike_image);

            }
        };

        listViewChooseBike.setAdapter(bikeAdapter);

        //what happens when user clicks on am item
        listViewChooseBike.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //get the RD refrence to the Item clicked and store this in data holder class
                selectedBikeToLinksTo = bikeAdapter.getRef(i);


                selectedBikeToLinksTo.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        BikeData b = dataSnapshot.getValue(BikeData.class);

                       // selectedBike.setVisibility();

                        makeView.setText(b.getMake());
                        modelView.setText(b.getModel());
                        colorView.setText(b.getColor());

                        getBitMapFromString(b.getImageBase64(), bike_imageSelected);


                        //testing delete
                        Toast.makeText(getActivity().getApplicationContext(), "ref: " + b.getModel() + " uuid: " + b.getBeaconUUID(), Toast.LENGTH_SHORT).show();

                        selectedBikeUUID = Integer.parseInt(b.getBeaconUUID());
                        linkBike();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


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

    public void linkBike() {
        //start monitoring for the selected bike
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(region = new com.estimote.sdk.Region("monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        selectedBikeUUID, null));

                Log.v("**test", "connect");
            }
        });
    }//end linkbike

//       public void showStolen(DatabaseReference selectedBikeToLinksTo) {
//
//        final FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
//                (getActivity(), BikeData.class, R.layout.list_item_monitering, selectedBikeToLinksTo) {
//            @Override
//            protected void populateView(View v, BikeData model, int position) {
//                //handling displaying of loading bar once data is recieved hide it.
////                queryBikesUsingBeacons.addListenerForSingleValueEvent(new ValueEventListener() {
////                    @Override
////                    public void onDataChange(DataSnapshot dataSnapshot) {
////                        Log.d("*count", " " + dataSnapshot.getChildrenCount());
////                    }
////
////                    @Override
////                    public void onCancelled(DatabaseError databaseError) {
////                    }
////                });
//
//                // Find the TextView IDs of list_item.xml
//                TextView makeView = (TextView) v.findViewById(R.id.make);
//                TextView modelView = (TextView) v.findViewById(model);
//                TextView colorView = (TextView) v.findViewById(R.id.color);
//                bike_image = (ImageView) v.findViewById(R.id.bike_image);
//
//                //setting the textViews to Bike data
//                makeView.setText(model.getMake());
//                modelView.setText(model.getModel());
//                colorView.setText(model.getColor());
//
//                //call method to set image, which turns base64 string to image
//                getBitMapFromString(model.getImageBase64());
//
//            }
//        };
//        listViewChooseBike.setAdapter(bikeAdapter);
//
//    }

}
