package com.example.ronan.practicenavigationdrawer;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.ronan.practicenavigationdrawer.R.drawable.user;


public class GmapFragment extends Fragment implements OnMapReadyCallback {

public MainActivity mainActivity;


    private GoogleMap gMap;

    private View myView;

    private DatabaseReference stolenBikesDatabse;
    BikeData mybike = new BikeData();
    ArrayList<Double> latitude = new ArrayList<>();
    ArrayList<Double> longditude = new ArrayList<>();

    //declaring ValueEvent Listener
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            for (DataSnapshot snapshot :  dataSnapshot.getChildren()) {

                mybike = snapshot.getValue(BikeData.class);

                latitude.add(mybike.getLatitude());
                longditude.add(mybike.getLongditude());
                Log.v("look_here_lat***", Arrays.toString(latitude.toArray()));
                Log.v("look_here_long***", Arrays.toString(longditude.toArray()));

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("***","marker error : "+databaseError.toString());
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    FrameLayout frameLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView =  inflater.inflate(R.layout.fragment_map, container,false);
        myView=rootView;

        mainActivity = (MainActivity) getActivity();

      //  firebase
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        stolenBikesDatabse.addValueEventListener(bikeListener);

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                rootView.getLayoutParams().height = 1;
                rootView.getLayoutParams().width = 1;
                rootView.invalidate();
                rootView.requestLayout();
                Log.v("**","open");
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        return rootView;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.mapf);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.clear();
        this.gMap = googleMap;
        stolenBikesDatabse.addValueEventListener(bikeListener);

        //create arraylist of markers that will hold co-ordinates
        List<LatLng> marker = new ArrayList<>();
        //loop through all co ordinates
       for(int i =0 ;  i < latitude.size();i++){
               //create new marker with co-ordinates
               marker.add(new LatLng(latitude.get(i), longditude.get(i)));

          googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0), 6));
               googleMap.addMarker(new MarkerOptions().title("**Specific details to be put here **!").position(marker.get(i)));


       }//end for


    }







}
