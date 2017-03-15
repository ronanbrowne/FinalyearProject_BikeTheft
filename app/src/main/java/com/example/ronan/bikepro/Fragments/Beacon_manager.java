package com.example.ronan.bikepro.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.ronan.bikepro.R;
import com.example.ronan.bikepro.Scan_For_Stolen;

/**
 * A simple {@link Fragment} subclass.
 */
public class Beacon_manager extends Fragment {

    LinearLayout scanForStolen;
    LinearLayout linkToBike;
    LinearLayout helpArea;
    ImageView infobeacon;

    LocationManager locationManager;
    boolean GpsStatus;



    public Beacon_manager() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacon_manager, container, false);


        CheckGpsStatus();

        linkToBike = (LinearLayout) rootView.findViewById(R.id.monitering);
        scanForStolen = (LinearLayout) rootView.findViewById(R.id.ranging);
        helpArea = (LinearLayout) rootView.findViewById(R.id.help);
        infobeacon = (ImageView) rootView.findViewById(R.id.infobeacon);

        setBackGroundImage();

        //Button click to launch edit profile page
        scanForStolen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckGpsStatus();

                if (GpsStatus) {
                    //setFragment
                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction().replace(R.id.fragment_container, new Scan_For_Stolen()).commit();
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
                }


            }
        });

        //Button click to launch edit profile page
        linkToBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckGpsStatus();

                if (GpsStatus) {
                    //setFragment
                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction().replace(R.id.fragment_container, new BeaconsFragment()).commit();
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
                }

            }
        });


        infobeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!helpArea.isShown()) {
                    helpArea.setVisibility(View.VISIBLE);
                } else {
                    helpArea.setVisibility(View.GONE);
                }

            }
        });

        return rootView;
    }//end on create

    public void CheckGpsStatus() {
        locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (GpsStatus == true) {
            Log.v("*gps", "true");
        } else {
            Log.v("*gps", "false");
        }
    }

    public void setBackGroundImage(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String themePref = preferences.getString("list_preference", "");

        if (themePref.equals("AppThemeSecondary")){
            linkToBike.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.border_night));
            scanForStolen.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.border_night));
        }
        else{
            linkToBike.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.border));
            scanForStolen.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.border));

        }
    }


}//end class
