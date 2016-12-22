package com.example.ronan.practicenavigationdrawer.Fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronan.practicenavigationdrawer.DataModel.BikeData;
import com.example.ronan.practicenavigationdrawer.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;


public class GmapFragment extends Fragment implements OnMapReadyCallback {



    private GoogleMap gMap;
    private TileOverlay mOverlay;

    private Button heatMap;
    private Button defaultMap;


    private DatabaseReference stolenBikesDatabse;
    private BikeData mybike = new BikeData();

    private List<LatLng> mapLocations = new ArrayList<>();


    //======================================================================================
    // FireBase listener to add markers to the Map
    //======================================================================================
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mapLocations.clear();

            if (dataSnapshot.getValue(BikeData.class) == null) {
                Toast.makeText(getActivity().getApplicationContext(), "No bikes have been listed as stolen", Toast.LENGTH_SHORT).show();
            }

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                mybike = snapshot.getValue(BikeData.class);
                //use this for default camera loaction
                LatLng dub = new LatLng(53.3498 ,-6.2603);

                //create LatLong obj to store co-ordinates returned from bike date.
                LatLng coOrdinates = new LatLng(mybike.getLatitude(), mybike.getLongditude());
                mapLocations.add(coOrdinates);

                //move Camera and add marker to coOrdinates position
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dub, 6));
                gMap.addMarker(new MarkerOptions().title("Make: " + mybike.getMake()).snippet("Model:" + mybike.getModel() + "\nColour: " + mybike.getColor()+ "\nLast seen: " + mybike.getLastSeen()).position(coOrdinates));

                //Override the default layout for marker info window. if dont do this window is not large enough to properly display snippet
                gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }
                    //set the layout and properties of the contents of the marker details.
                    @Override
                    public View getInfoContents(Marker marker) {
                        //set layout and textviews
                        LinearLayout info = new LinearLayout(getActivity().getApplicationContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getActivity().getApplicationContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getActivity().getApplicationContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());


                        info.addView(title);
                        info.addView(snippet);
                        return info;
                    }
                });
            }//end for datasnapshot
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("***", "marker error : " + databaseError.toString());
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        //  firebase
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");

        //IDs
        heatMap = (Button) rootView.findViewById(R.id.heatMap);
        defaultMap = (Button) rootView.findViewById(R.id.normalMap);


        //following is a workaround that removes the map fragment before i switch to another screen via Nav bar
        //bug that causes map view to superimpose its self on other fragments if i go straight to that screen. was not abel to resolve issue fully.
        //whe user slides out shinking map fragment this prevent this overlay bug.
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
                Log.v("**", "open");
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        }); //end drawer listener

        //heatmap button listener
        heatMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeatMap();

            }
        });

        //default map buttn listener.
        defaultMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeHeatMap();
                stolenBikesDatabse.addValueEventListener(bikeListener);

            }
        });

        return rootView;
    }//end on create view

    //set google maps fragment
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapf);
        fragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        //clear any old map data
        googleMap.clear();
        this.gMap = googleMap;
//        LatLng dub = new LatLng(53.3498 ,-6.2603);
//        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dub, 6));
        stolenBikesDatabse.addValueEventListener(bikeListener);

    }// end onMapReady


    //======================================================================================
    // change map view to heat map
    //======================================================================================
    private void addHeatMap() {
        gMap.clear();

        if(!mapLocations.isEmpty()) {
            // Get the data: latitude/longitude positions of police stations.
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .data(mapLocations).radius(35)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mOverlay = gMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            mOverlay.setVisible(true);
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "No map data available", Toast.LENGTH_SHORT).show();
        }
    }

    //======================================================================================
    //remove heat map method
    //======================================================================================
    public void removeHeatMap() {
        if(mOverlay!=null) {
            mOverlay.remove();
        }
    }


}// end class








