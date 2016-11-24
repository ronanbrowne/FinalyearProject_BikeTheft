package com.example.ronan.practicenavigationdrawer;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.ronan.practicenavigationdrawer.R.id.mapwhere;
import static com.google.android.gms.wearable.DataMap.TAG;


public class DatabaseFragment extends Fragment {

    private DatabaseReference mDatabaseStolen;
    SupportMapFragment mSupportMapFragment;
    ImageView bike_image;

    EditText street;
    EditText radius;
    Button query;
    Button closeMap;

    double latitude = 0;
    double Longitude = 0;

    FrameLayout frameLayout;

    String name;
    int r;

    Circle circle;

    public DatabaseFragment() {
        // Required empty public constructor
    }

    private GoogleMap googleMap;

    BikeData mybike = new BikeData();

    ArrayList<Double> latitudeArray = new ArrayList<>();
    ArrayList<Double> longditudeArray = new ArrayList<>();

    //declaring ValueEvent Listener
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                mybike = snapshot.getValue(BikeData.class);
                latitudeArray.add(mybike.getLatitude());
                longditudeArray.add(mybike.getLongditude());
                Log.v("nci", Arrays.toString(latitudeArray.toArray()));
                Log.v("nci", Arrays.toString(longditudeArray.toArray()));

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("nci", "marker error : " + databaseError.toString());
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Firebase DB setup
        mDatabaseStolen = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        //mDatabaseStolen.addValueEventListener(bikeListener);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_database, container, false);


        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(mapwhere);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(mapwhere, mSupportMapFragment).commit();
        }


        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap gMap) {
                    googleMap = gMap;
                    mDatabaseStolen.addValueEventListener(bikeListener);
                    if (googleMap != null) {
                        googleMap.getUiSettings().setAllGesturesEnabled(true);
                        LatLng dub = new LatLng(53.3498, -6.2603);
                        // Marker marker = new Marker(sydney);

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(dub).zoom(10f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        googleMap.moveCamera(cameraUpdate);
                    }
                }
            });
        }


        street = (EditText) rootView.findViewById(R.id.streetgeo);
        radius = (EditText) rootView.findViewById(R.id.radius);
        query = (Button) rootView.findViewById(R.id.runQuery);
        closeMap = (Button) rootView.findViewById(R.id.closeMap);
        frameLayout = (FrameLayout) rootView.findViewById(R.id.mapwhere);
        frameLayout.setVisibility(View.GONE);

        ListView myListView = (ListView) rootView.findViewById(R.id.list);
        myListView.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        myListView.setDividerHeight(1);


        //  Query bikeQuery = mDatabaseStolen.orderByChild("other").equalTo("It's Class");

        //  get ID of loading bar
        final View loadingIndicator = rootView.findViewById(R.id.loading_indicator);


        // set up the Firebase Specific ListAdapter
        // here we set content of list items
        FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item, mDatabaseStolen) {
            @Override
            protected void populateView(View v, BikeData model, int position) {
                //handeling diplaying of loading bar
                mDatabaseStolen.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        loadingIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                // Find the TextView IDs of list_item.xml
                TextView makeView = (TextView) v.findViewById(R.id.make);
                TextView modelView = (TextView) v.findViewById(R.id.model);
                TextView sizeView = (TextView) v.findViewById(R.id.size);
                TextView colorView = (TextView) v.findViewById(R.id.color);
                TextView otherView = (TextView) v.findViewById(R.id.other);
                TextView lastlocationView = (TextView) v.findViewById(R.id.loaction);
                bike_image = (ImageView) v.findViewById(R.id.bike_image);

                //setting the textViews to Bike data
                makeView.setText(model.getMake());
                modelView.setText(model.getModel());
                sizeView.setText(String.valueOf(model.getFrameSize()));
                colorView.setText(model.getColor());
                otherView.setText(model.getOther());
                lastlocationView.setText(model.getLastSeen());
                //call method to set image, which turns base64 string to image
                getBitMapFromString(model.getImageBase64());


            }
        };
        //set adapter on our listView
        myListView.setAdapter(bikeAdapter);

        closeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation backDoww = AnimationUtils.loadAnimation(getContext(),
                        R.anim.slidedown);
                frameLayout.startAnimation(backDoww);
                frameLayout.setVisibility(View.GONE);
            }
        });


        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                        R.anim.slide);
                frameLayout.startAnimation(bottomUp);
                frameLayout.setVisibility(View.VISIBLE);
                r = Integer.parseInt(radius.getText().toString());
                GeocodeAsyncTaskForQuery asyncTaskForQuery = new GeocodeAsyncTaskForQuery();
                asyncTaskForQuery.execute();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        });

        return rootView;
    }


    //extract bitmap helper, this sets image view
    public void getBitMapFromString(String imageAsString) {
        if (imageAsString == "No image" || imageAsString == null) {
            // bike_image.setImageResource(R.drawable.not_uploaded);
            Log.v("***", "No image Found");
        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bike_image.setImageBitmap(bitmap);
        }
    }


    //AsyncTask for getting geocoiding from user input
    class GeocodeAsyncTaskForQuery extends AsyncTask<Void, Void, Address> {
        String errorMessage = "";

        @Override
        protected void onPreExecute() {
            name = street.getText().toString();
        }

        @Override
        protected Address doInBackground(Void... none) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(name, 1);
            } catch (IOException e) {
                errorMessage = "Service not available";
                Log.e(TAG, errorMessage, e);
            }
            if (addresses != null && addresses.size() > 0)
                return addresses.get(0);
            return null;
        }

        protected void onPostExecute(Address address) {
            if (address == null) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "address null", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                String addressName = "";
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressName += " --- " + address.getAddressLine(i);
                }
                //assigning class variables
                latitude = address.getLatitude();
                Longitude = address.getLongitude();
                LatLng userInput = new LatLng(latitude, Longitude);
                drawOnMap(userInput, r);

                Log.v("Co-ordinates", "Latitude: " + address.getLatitude() + "\n" +
                        "Longitude: " + address.getLongitude() + "\n" +
                        "Address: ");
            }
        }
    }//end async


    /**
     * function to load map. If map is not created it will create it for you
     */
    public void drawOnMap(LatLng latLng, int radius) {

        googleMap.clear();

        if (circle != null) {
            circle.remove();
        }

        circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.rgb(0, 136, 255))
                .fillColor(Color.argb(20, 0, 136, 255)));


        //create arraylist of markers and co-ordinates that will hold co-ordinates
        List<LatLng> coordinatesList = new ArrayList<>();
        List<Marker> markers = new ArrayList<>();

        //loop through all co ordinates
        for (int i = 0; i < latitudeArray.size(); i++) {
            //create new marker with co-ordinates
            coordinatesList.add(new LatLng(latitudeArray.get(i), longditudeArray.get(i)));
            //  googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0), 3));
            Marker marker = googleMap.addMarker(new MarkerOptions().title("**Specific details to be put here **!")
                    .position(coordinatesList.get(i)).visible(false));
            markers.add(marker);
        }//end for

        for (Marker marker : markers) {
            if (SphericalUtil.computeDistanceBetween(latLng, marker.getPosition()) < radius) {
                marker.setVisible(true);
            }
        }
    }

}//end class
