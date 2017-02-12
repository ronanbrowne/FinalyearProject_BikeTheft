package com.example.ronan.bikepro;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.Helpers.BeaconListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private ArrayList mylist = new ArrayList();


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


    List<BikeData> stolenBikes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scan__for__stolen, container, false);

        // Configure device list.
        adapter = new BeaconListAdapter(getContext());
        ListView list = (ListView) rootView.findViewById(R.id.listRanging);
        list.setAdapter(adapter);
        //list.setOnItemClickListener(createOnItemClickListener());

        usersBikesDatabase = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        usersBikesDatabase.addValueEventListener(bikeDataListener);

//        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                getActivity().getApplicationContext(), android.R.layout.simple_list_item_1);
//        list.setAdapter(adapter);

        beaconManager = new BeaconManager(getActivity().getApplicationContext());

        //   ranging

        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {

                    Log.v("**list**", "size: " + list.size());

                    //   Beacon nearestBeacon = list.get(0);
                    //  nearestBeacon.getMinor();

                    //ArrayList<BikeData> stolenBikes = (ArrayList<BikeData>) StolenBikesInArea(nearestBeacon);
                    //ArrayList<BikeData> stolenBikes = (ArrayList<BikeData>) StolenBikesInArea(nearestBeacon);

//                    if (!stolenBikes.isEmpty()) {
//                    }
                    stolenBikes = StolenBikesInArea(list);


                    for (BikeData s : stolenBikes) {
                        Log.v("**test", "Nearest places: " + s.getMake() + " " + s.getModel());
                        mylist.add(s.getModel());
                        Log.v("**test**", "size: " + mylist.size());

                    }
                    Log.v("**test**", "Nearest places: " + mylist.size());


                    // TODO: update the UI here
                    //adapter.clear();
                    adapter.replaceWith(stolenBikes);
                    stolenBikes.clear();


                    //   Log.v("**test", "Nearest places: " + stolenBikes);
                    //  beaconsUUIDInrange.clear();;
                    for (Beacon temp : list) {

                        Log.v("**test***LIst", "Minor code: in range : " + temp.getMinor());

                        //list of all nearby beacons.
                        //  beaconsUUIDInrange.add(String.format("%d:%d", temp.getMajor(), temp.getMinor()));
                    }


                }
            }
        });

        //adapter.addAll(mylist);

        return rootView;
    }

    ArrayList<BikeData> matchedStolen = new ArrayList<>();

    //scan the list of beacons reterned by ranging and compare to firebase listed as stolem
    private List<BikeData> StolenBikesInArea(List<Beacon> beacon) {

        //mylist.clear();

        for (Beacon b : beacon) {

            String beaconKey = String.valueOf(b.getMinor());


            for (BikeData data : bikes) {

                Log.v("**test", "UUID from bikes registered as stolen " + data.getBeaconUUID());
                Log.v("**test", "comparing the above to  " + beaconKey);

                if (data.getBeaconUUID().equals(beaconKey)) {
                    matchedStolen.add(data);
                    Log.v("**test", "match found");

                } else {
                    Log.v("**test", "no match found");

                }

            }


        }//end beacon list


        return matchedStolen;
    }


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
