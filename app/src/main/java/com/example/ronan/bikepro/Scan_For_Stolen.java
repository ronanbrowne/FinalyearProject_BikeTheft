package com.example.ronan.bikepro;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.Helpers.BeaconListAdapter;
import com.example.ronan.bikepro.Helpers.GetAddressFromLOcation;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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
public class Scan_For_Stolen extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    protected static final int PERMISSION_ACCESS_COARSE_LOCATION = 0;

    BeaconManager beaconManager;

    //used to start stop ranging
    private Region region;
    private ArrayList<BikeData> bikes = new ArrayList<>();
    private DatabaseReference usersBikesDatabase;
    private TextView searchAreaHeading;
    private ProgressBar loading_indicator_scan;


    private BeaconListAdapter adapter;
    ArrayList<BikeData> matchedStolen = new ArrayList<>();

    List<BikeData> stolenBikes;

    private ImageView infoLocator;
    private BikeData bikeSelectedFromListAdapter;

    private GoogleApiClient client;
    protected Location mLastLocation;
    private double latitude;
    private double longditude;
    private String LocationResult;
    private String locationForMail;


    public Scan_For_Stolen() {
        // Required empty public constructor
    }

    //===================================================================================
    //=        dialog listener for pop up to send email to origional user
    //===================================================================================
    DialogInterface.OnClickListener dialogClickListenerForEmail = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    /// TODO: 30/12/2016  chage this to a email address
                    String[] email = {bikeSelectedFromListAdapter.getRegisteredBy()};
                    String subject = "Re: Confirmed sighting of your bike: " + bikeSelectedFromListAdapter.getMake();
                    String body = "Hello, \n\n Regarding the sighting of your bike  (" + (bikeSelectedFromListAdapter.getColor() + " " + bikeSelectedFromListAdapter.getMake()) + "). " +
                            "\n\n At the location " + searchAreaHeading.getText() + "\n\n" +
                            "..." +
                            "\n\n Regards.";


                    composeEmail(email, subject, body);


                    break;

                case DialogInterface.BUTTON_NEGATIVE:

                    //feedback
                    Toast toastCanceled = Toast.makeText(getActivity().getApplicationContext(), " canceled", Toast.LENGTH_SHORT);
                    toastCanceled.show();
                    break;
            }
        }
    };

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

        //set up location client
        client = new GoogleApiClient.Builder(getActivity().getApplicationContext()).addApi(AppIndex.API).build();
        buildGoogleApiClient();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String themePref = preferences.getString("list_preference", "");


        // set up adapter for list view
        adapter = new BeaconListAdapter(getContext(),themePref);
        ListView list = (ListView) rootView.findViewById(R.id.listRanging);
        loading_indicator_scan = (ProgressBar) rootView.findViewById(R.id.loading_indicator_scan);
        searchAreaHeading = (TextView) rootView.findViewById(R.id.temp);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                bikeSelectedFromListAdapter = (BikeData) adapterView.getAdapter().getItem(i);
                Log.d("*click", " " + bikeSelectedFromListAdapter.getMake());

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Contact Owner").setMessage("Contact owner of this bike and report confirmed sighting?\n\n" +
                        "This will launch your email client.").setPositiveButton("Proceed", dialogClickListenerForEmail)
                        .setNegativeButton("Cancel", dialogClickListenerForEmail).show();
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

                    loading_indicator_scan.setVisibility(View.GONE);

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

            if (matchedStolen.size() > 3)
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
        Log.v("**test", "match size: " + matchedStolen.size());

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


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("*test", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {




        if (ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);}
     else {

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                latitude = mLastLocation.getLatitude();
                longditude = mLastLocation.getLongitude();

                Log.i("*location", "" + latitude + " : " + longditude);

                Log.v("*ya", "connected");
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
                    Log.i("*test", LocationResult);

                    break;
                default:
                    LocationResult = null;
            }

            searchAreaHeading.setText(LocationResult);

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


    //================================================================================
    //   Method to compose a email called when a user clicks on bike item in listView.
    //   Email generated to send to origional user.
    //=================================================================================
    public void composeEmail(String[] addresses, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        //,ake sure user has a app capable of carrying out this intent
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }//end method

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        beaconManager.stopRanging(region);
        client.disconnect();
        Log.d("*cycle", "stop");
    }

}
