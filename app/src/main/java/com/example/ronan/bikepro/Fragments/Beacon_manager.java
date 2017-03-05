package com.example.ronan.bikepro.Fragments;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.ronan.bikepro.Activities.MainActivity;
import com.example.ronan.bikepro.R;
import com.example.ronan.bikepro.Scan_For_Stolen;
import com.tooltip.Tooltip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static android.R.attr.bitmap;

/**
 * A simple {@link Fragment} subclass.
 */
public class Beacon_manager extends Fragment {

    LinearLayout scanForStolen;
    LinearLayout linkToBike;
    LinearLayout helpArea;
    ImageView infobeacon;

    LocationManager locationManager;
    boolean GpsStatus ;

    public void CheckGpsStatus() {

        locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (GpsStatus == true) {
            Log.v("*gps","true");
        } else {

            new AlertDialog.Builder(getActivity())
                    .setTitle("Enable location services")
                    .setMessage("Location Services must be enabled to continue")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setIcon(R.drawable.ic_gps_not_fixed_black_24dp)
                    .show();

            Log.v("*gps","false");

        }

    }


    public Beacon_manager() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacon_manager, container, false);


        linkToBike = (LinearLayout) rootView.findViewById(R.id.monitering);
        scanForStolen = (LinearLayout) rootView.findViewById(R.id.ranging);
        helpArea = (LinearLayout) rootView.findViewById(R.id.help);
        infobeacon = (ImageView) rootView.findViewById(R.id.infobeacon);


        //Button click to launch edit profile page
        scanForStolen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckGpsStatus();


                //setFragment
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.fragment_container, new Scan_For_Stolen()).commit();

            }
        });

        //Button click to launch edit profile page
        linkToBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckGpsStatus();


                //setFragment
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.fragment_container, new BeaconsFragment()).commit();

            }
        });


        infobeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!helpArea.isShown()) {
                    helpArea.setVisibility(View.VISIBLE);
                }
                else
                {
                    helpArea.setVisibility(View.GONE);
                }

            }
        });

        return rootView;
    }//end on create

}//end class
