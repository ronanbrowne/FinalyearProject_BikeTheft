package com.example.ronan.practicenavigationdrawer;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.ronan.practicenavigationdrawer.R.drawable.user;


public class GmapFragment extends Fragment implements OnMapReadyCallback {

//    private MapView mapView;
//    private GoogleMap gMap;
//
    private DatabaseReference stolenBikesDatabse;

    BikeData mybike = new BikeData("test make",22,"red","other",true,"dfsffdss","Model","last seen",0,0);

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



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      //  firebase
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");


        return inflater.inflate(R.layout.fragment_map, container,false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        stolenBikesDatabse.addValueEventListener(bikeListener);


        LatLng marker = new LatLng(53.3498, -6.2603);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13));

        googleMap.addMarker(new MarkerOptions().title("Hello Dublin!").position(marker));
    }
}
