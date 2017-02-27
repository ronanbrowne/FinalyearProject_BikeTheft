package com.example.ronan.bikepro;


import android.os.Bundle;
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
import android.widget.ListView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.Helpers.BeaconListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class Scan_For_Stolen extends Fragment {

    BeaconManager beaconManager;

    //used to start stop ranging
    private Region region;
    private ArrayList<BikeData> bikes = new ArrayList<>();
    private DatabaseReference usersBikesDatabase;


    private BeaconListAdapter adapter;
    ArrayList<BikeData> matchedStolen = new ArrayList<>();

    List<BikeData> stolenBikes;

    private ImageView infoLocator;


    public Scan_For_Stolen() {
        // Required empty public constructor
    }

    ValueEventListener bikeDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.v("***", "bikeDataListener called");

            if (dataSnapshot.getValue() != null) {
                Log.v("***", "datasnapshot not null");


                for (DataSnapshot dataSnapshotTemp : dataSnapshot.getChildren()) {
                    if (dataSnapshotTemp.child("beaconUUID").getValue(String.class) != null) {
                        bikes.add(dataSnapshotTemp.getValue(BikeData.class));
                        Log.v("***", "bike found with UUID");
                    } else {
                        Log.v("***", "No bike Found with UUID");
                    }
                }
                Log.v("***", "returned UUIDs " + bikes.size());
                Log.v("**test**Output2", Arrays.toString(bikes.toArray()));
            }


        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scan__for__stolen, container, false);

         infoLocator = (ImageView) rootView.findViewById(R.id.infoLocator);


        // set up adapter for list view
        adapter = new BeaconListAdapter(getContext());
        ListView list = (ListView) rootView.findViewById(R.id.listRanging);
        list.setAdapter(adapter);

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //set up DB
        usersBikesDatabase = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        usersBikesDatabase.addValueEventListener(bikeDataListener);


        beaconManager = new BeaconManager(getActivity().getApplicationContext());

        //  set up what beacons we will "listen for"
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        //listener to take action when beacons are descovered nearby
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {

                    //method to compare neary beacons to those listed as stollen
                    //returns a list of positive matches
                    stolenBikes = StolenBikesInArea(list);

                    Log.v("**test**", "Stolen bikes nearby: " + stolenBikes.size());

                    // UI update
                    adapter.replaceWith(stolenBikes);
                    stolenBikes.clear();

                    //list all beacons nearby for debug
                    for (Beacon temp : list) {
                        Log.v("**test**", "major code: in range : " + temp.getMajor());
                    }
                }
            }
        });

        //tool tip helper at top of UI
        infoLocator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tooltip tooltip = new Tooltip.Builder(infoLocator)
                        .setText("Searching for stolen bikes Within current area\nList and proximity will update as you move\nStronger colours indicate moving closer to target")
                        .setTextColor(ContextCompat.getColor(getContext(), R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan)).show();

                final Animation animation = new AlphaAnimation((float) 0.5, 0); // Change alpha from fully visible to invisible
                animation.setDuration(500); // duration - half a second
                animation.setInterpolator(new LinearInterpolator());
                animation.setRepeatCount(1); // Repeat animation
                animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
                infoLocator.startAnimation(animation);
            }
        });


        return rootView;
    }// end onCreateView


    //scan the list of beacons reterned by ranging and compare to firebase listed as stolen
    private List<BikeData> StolenBikesInArea(List<Beacon> beacon) {
        for (Beacon b : beacon) {
            //get the unique ID of beacon

            if(matchedStolen.size()>3)
                matchedStolen.clear();

            String beaconKey = String.valueOf(b.getMajor());
            for (BikeData data : bikes) {

                Log.v("**test", "Major ID from bikes registered as stolen " + data.getBeaconUUID());
                Log.v("**test", "comparing the above to  " + beaconKey);

                String key = data.getBeaconUUID();
                String major = key.split(":")[0];

                //see if Firebase stored UUID matches that of ones in proximity, pull back match if so
                if (major.equals(beaconKey)) {
                    // check its aprox distance and set in Bike Object, need this for adapter class when populating UI
                    data.setBeaconAccuracy(Utils.computeAccuracy(b));
                    matchedStolen.add(data);
                    Log.v("**test", "match found");
                } else {
                    Log.v("**test", "no match found");
                }
            }//end bike for
        }//end beacon for
        Log.v("**test", "match size: "+matchedStolen.size());

        return matchedStolen;
    }//end method


    @Override
    public void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    public void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }


}
