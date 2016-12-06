package com.example.ronan.practicenavigationdrawer;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.ronan.practicenavigationdrawer.R.drawable.user;
import static com.google.android.gms.internal.zzsr.My;


public class GmapFragment extends Fragment implements OnMapReadyCallback {



    private GoogleMap gMap;
    TileOverlay mOverlay;

    // Declare a variable for the cluster manager.
   // private ClusterManager<MyItemMapClusters> mClusterManager;

    private View myView;
    ImageView image;

    Button heatMap;
    Button defaultMap;


    private DatabaseReference stolenBikesDatabse;
    BikeData mybike = new BikeData();

    List<LatLng> coOr = new ArrayList<>();


    //declaring ValueEvent Listener
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            coOr.clear();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                // latitude.add(mybike.getLatitude());
                //  longditude.add(mybike.getLongditude());


                mybike = snapshot.getValue(BikeData.class);

                //create LatLong obj to store co-ordinates returned from bike date.
                LatLng coOrdinates = new LatLng(mybike.getLatitude(), mybike.getLongditude());
                coOr.add(coOrdinates);

                //  **CODE RELATED TO CLUSTERS . COMPLEX TO IMPLEMEND TITLE SNIPPET, RETURN TO AT LATER STAGE **

//                MyItemMapClusters offsetItem = new MyItemMapClusters(mybike.getLatitude(), mybike.getLongditude());
//                mClusterManager.addItem(offsetItem);
//                gMap.setOnCameraIdleListener(mClusterManager);
//                gMap.setOnMarkerClickListener(mClusterManager);



                //move Camera and add marker to coOrdinates position
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coOrdinates, 6));



                  //  if (mybike.getImageBase64().equals("No image") || mybike.getImageBase64() == null){
                        gMap.addMarker(new MarkerOptions().title("Make: " + mybike.getMake()).snippet("Model:" + mybike.getModel() + "\nLast seen: " + mybike.getColor()).position(coOrdinates));

//                    }else{
//                        int height = 100;
//                        int width = 100;
//                        Bitmap smallMarker = Bitmap.createScaledBitmap(getBitMapFromString(mybike.getImageBase64()), width, height, false);
//
//                        gMap.addMarker(new MarkerOptions().title("Make: " + mybike.getMake()).snippet("Model:" + mybike.getModel() + "\nLast seen: " + mybike.getColor()).position(coOrdinates).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//
//                    }



                //HEATMAP
                // Create a heat map tile provider, passing it the latlngs of the police stations.
                // gMap.addTileOverlay(mOverlay);

                //http://stackoverflow.com/questions/13904651/android-google-maps-v2-how-to-add-marker-with-multiline-snippet

                gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    //set the layout and properties of the contents of the marker details.
                    @Override
                    public View getInfoContents(Marker marker) {
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

            }


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
        myView = rootView;

        //  firebase
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");

        heatMap = (Button) rootView.findViewById(R.id.heatMap);
        defaultMap = (Button) rootView.findViewById(R.id.normalMap);


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
        });


        heatMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeatMap();

            }
        });

        defaultMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeHeatMap();
                stolenBikesDatabse.addValueEventListener(bikeListener);


            }
        });


        return rootView;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapf);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.clear();

        this.gMap = googleMap;
        LatLng dub = new LatLng(53.3498 ,-6.2603);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dub, 6));

     //   mClusterManager = new ClusterManager<MyItemMapClusters>(getActivity().getApplicationContext(), gMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
      //  gMap.setOnCameraIdleListener(mClusterManager);
       // gMap.setOnMarkerClickListener(mClusterManager);


        stolenBikesDatabse.addValueEventListener(bikeListener);

    }// end onMapReady

    //extract bitmap helper, this sets image view
    public Bitmap getBitMapFromString(String imageAsString) {
        Bitmap bitmap = null;

        if (imageAsString != null) {
            if (imageAsString.equals("No image") || imageAsString == null) {
                Log.v("***", "No image Found");
            } else {
                byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            }
        } else {
            Log.v("***", "Null paramater passed into getBitMapFromString");
        }
        return bitmap;
    }

    private void addHeatMap() {
        gMap.clear();
        // Get the data: latitude/longitude positions of police stations.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(coOr).radius(35)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = gMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        mOverlay.setVisible(true);
    }

    public void removeHeatMap() {
        mOverlay.remove();
    }


}// end class








