package com.example.ronan.practicenavigationdrawer.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronan.practicenavigationdrawer.DataModel.BikeData;
import com.example.ronan.practicenavigationdrawer.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.example.ronan.practicenavigationdrawer.R.id.mapwhere;
import static com.google.android.gms.wearable.DataMap.TAG;


public class DatabaseFragment extends Fragment {

    public DatabaseFragment() {
        // Required empty public constructor
    }

    //global variables

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseStolen;
    private DatabaseReference mDatabaseQuery;
    private SupportMapFragment mSupportMapFragment;

    private ImageView bike_image;
    private EditText street;
    private Button query;
    private Button closeMap;
    private SeekBar seekBar;
    private TextView radiousTV;
    private TextView noDataMessage;
    private View loadingIndicator;


    private LatLng userInput1 = new LatLng(53.3498, 6.2603);
    private LatLng userInput = new LatLng(53.3498, -6.2603);
    private double latitude = 0;
    private double Longitude = 0;
    private boolean isMapFragmentVisavle = false;

    private FrameLayout frameLayout;
    private String userInputAddress;
    private String email = "";


    private Circle circle;
    private GoogleMap googleMap;

    private BikeData mybike = new BikeData();
    private BikeData stolenBike;

    private ArrayList<Double> latitudeArray = new ArrayList<>();
    private ArrayList<Double> longditudeArray = new ArrayList<>();
    private ArrayList<BikeData> bikeReturned = new ArrayList<>();
    private ArrayList<String> bikekey = new ArrayList<>();


    private ListView myListView = null;
    private int progress = 0;

    private String input_from_reported_Location = "";

    //==============================================================================================
    //=          dialog listener for pop up to confirm report sightings
    //==============================================================================================
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:


                    //custom alert box
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Sighting Location");
                    //grab custom layout
                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.report_stolen_dialog, (ViewGroup) getView(), false);
                    // Set up the input
                    final EditText input = (EditText) viewInflated.findViewById(R.id.input);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    builder.setView(viewInflated);

                    // Set up the buttons
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            //validate input
                            if(input.getText().toString().trim().length() == 0){
                                Toast.makeText(getActivity().getApplicationContext(), "You must specify where you saw this bike", Toast.LENGTH_SHORT).show();
                            }else{

                                //build up the email to send to user
                                input_from_reported_Location = input.getText().toString();

                                String[] email = {stolenBike.getRegisteredBy()};
                                String subject = "Suspected sighting of your bike: " + stolenBike.getMake();
                                String body = "Hello, \n\n I have potentially spotted the bike you registered as stolen (" + (stolenBike.getColor() + " " + stolenBike.getMake()) + "). " +
                                        "\n\n This sighting was at the following location " + input_from_reported_Location + "\n\n" +
                                        "Please reply to this email for further details." +
                                        "\n\n Regards.";

                                //ethod to send emial
                                composeEmail(email, subject, body);

                                //feedback
                           Toast.makeText(getActivity().getApplicationContext(), "Tell origional owner where you may have seen their bike", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();


                    break;

                case DialogInterface.BUTTON_NEGATIVE:

                    //feedback
                    Toast toastCanceled = Toast.makeText(getActivity().getApplicationContext(), "Canceled", Toast.LENGTH_SHORT);
                    toastCanceled.show();
                    break;
            }
        }
    };


    //===================================================================================
    //=         Firebase listener to retrieve bikes in DB labeled as stolen sets up data for query also
    //===================================================================================

    //declaring ValueEvent Listener
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.getValue() != null) {
                noDataMessage.setVisibility(View.GONE);
                latitudeArray.clear();
                longditudeArray.clear();
                bikeReturned.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mybike = snapshot.getValue(BikeData.class);
                    latitudeArray.add(mybike.getLatitude());
                    longditudeArray.add(mybike.getLongditude());
                    Log.v("nci", Arrays.toString(latitudeArray.toArray()));
                    Log.v("nci", Arrays.toString(longditudeArray.toArray()));
                    bikeReturned.add(mybike);
                }//end loop
            }//end if
            else {
                noDataMessage.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);


            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("nci", "marker error : " + databaseError.toString());
        }
    };


    //===================================================================================
    //=         onCreateView
    //===================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //set up firebase instances also get user email we use this for uniqe DB refrences
        mDatabaseQuery = FirebaseDatabase.getInstance().getReference().child("QueryResults").child(email);
        mDatabaseStolen = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
        }

        //firbase DB does not allow @ in names of nodes so we split email at the @ take first bit
        email = email.split("@")[0];

        //Firebase DB setup
        mDatabaseStolen.addValueEventListener(bikeListener);

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_database, container, false);


        //handel the sliding map fragment
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction = null;

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(mapwhere);
        if (mSupportMapFragment == null) {
            fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(mapwhere, mSupportMapFragment).commit();
            //fragmentTransaction.remove(mSupportMapFragment);

        }

        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap gMap) {
                    googleMap = gMap;
                    googleMap.clear();
                    mDatabaseStolen.addValueEventListener(bikeListener);
                    if (googleMap != null) {
                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                        //change location of camra based on user input
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(userInput1).zoom(7f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        googleMap.moveCamera(cameraUpdate);
                    }
                }
            });
        }


        street = (EditText) rootView.findViewById(R.id.streetgeo);
        //  radius = (EditText) rootView.findViewById(R.id.radius);
        query = (Button) rootView.findViewById(R.id.runQuery);
        closeMap = (Button) rootView.findViewById(R.id.closeMap);
        loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        radiousTV = (TextView) rootView.findViewById(R.id.radiusTV);
        noDataMessage = (TextView) rootView.findViewById(R.id.empty_view_Notification);

        radiousTV.setText("Radius: " + seekBar.getProgress() + "km");

        //handel seekbar used for radius input
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = 0;
                progress = progresValue;
                radiousTV.setText("Radius: " + progress + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        //get and initally hide slide up map fragment
        frameLayout = (FrameLayout) rootView.findViewById(R.id.mapwhere);
        frameLayout.setVisibility(View.GONE);

        //  ListView
        myListView = (ListView) rootView.findViewById(R.id.list);
        myListView.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        myListView.setDividerHeight(1);


        //===================================================================================
        //=         Firebase specif list adapter loaded on first viewing
        //===================================================================================
        final FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item, mDatabaseStolen) {
            @Override
            protected void populateView(View v, BikeData model, int position) {

                //listening to see when data is called we hide loading bar then
                mDatabaseStolen.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        loadingIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });//end listener

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


        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                stolenBike = bikeAdapter.getItem(i);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Are you sure you wish to report a sighting of this bike?" +
                        "\nthis will notify the origional owner")
                        .setPositiveButton("Report Sighting", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).show();

            }
        });//end onClick for listView


        //click to close map and re-set the listview returning default query of all
        closeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation backDoww = AnimationUtils.loadAnimation(getContext(),
                        R.anim.slidedown);
                frameLayout.startAnimation(backDoww);
                frameLayout.setVisibility(View.GONE);
                isMapFragmentVisavle = false;
                myListView.setAdapter(bikeAdapter);
                //re set adapter pull into method later **********
                myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        stolenBike = bikeAdapter.getItem(i);

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("Are you sure you wish to report a sighting of this bike?" +
                                "\nthis will notify the origional owner")
                                .setPositiveButton("Report Sighting", dialogClickListener)
                                .setNegativeButton("Cancel", dialogClickListener).show();

                    }
                });//end onClick for listView
                seekBar.setProgress(0);
                street.setText("");
            }
        });

        //run query button
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //only do slide up animation if map was previously hidden
                if (!isMapFragmentVisavle) {
                    isMapFragmentVisavle = true;
                    Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                            R.anim.slide);
                    frameLayout.startAnimation(bottomUp);
                }

                userInputAddress = street.getText().toString();

                //user validation make sure inputs not null
                if ((userInputAddress != null && !userInputAddress.isEmpty()) && (progress > 0)) {
                    //getting co-ordinates
                    GeocodeAsyncTaskForQuery asyncTaskForQuery = new GeocodeAsyncTaskForQuery();
                    frameLayout.setVisibility(View.VISIBLE);
                    asyncTaskForQuery.execute();

                    //hide keyboard
                    hideKeyboardFrom(getActivity().getApplicationContext(), rootView);

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Query fields can not be left blank", Toast.LENGTH_SHORT).show();
                }

            }
        });


        return rootView;
    }

    //method to hide the keyboard just makes UI a bit cleaner after we run query
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    //===================================================================================
    //=         extract bitmap helper, this sets image view
    //===================================================================================

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


    //==============================================================================================
    //=          getting the Geocoding from user input - this is the lat / long co-ordinates
    //==============================================================================================
    class GeocodeAsyncTaskForQuery extends AsyncTask<Void, Void, Address> {
        String errorMessage = "";

        //nothing to do pre
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Address doInBackground(Void... none) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(userInputAddress, 1);
            } catch (IOException e) {
                errorMessage = "Service not available";

                Log.e(TAG, errorMessage, e);
            }
            if (addresses != null && addresses.size() > 0)
                return addresses.get(0);
            return null;
        }

        //update various variables that need lat long
        protected void onPostExecute(Address address) {
            if (address == null) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "address null", Toast.LENGTH_SHORT);
                toast.show();
            } else {

                //assigning class variables
                latitude = address.getLatitude();
                Longitude = address.getLongitude();
                userInput = new LatLng(latitude, Longitude);

                //make sure seekbar is not zero
                if (progress != 0) {
                    drawOnMap(userInput, (progress * 1000));
                }

                //debugging
                Log.v("Co-ordinates***", "Latitude: " + address.getLatitude() + "\n" +
                        "Longitude: " + address.getLongitude() + "\n" +
                        "Address L: " + address.getLocality() + "\n" +
                        "Address L: " + address.getPostalCode());
            }
        }
    }//end async


    //================================================================================
    //=         method to draw circle and markers on map and change list view
    //=================================================================================
    public void drawOnMap(LatLng latLng, int radius) {

        Log.v("LAt***", "Latitude: " + latLng.latitude + "Longitude: " + latLng.longitude);

        googleMap.clear();

        //remove previous circle
        if (circle != null) {
            circle.remove();
        }

        //draw radious
        circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.rgb(0, 136, 255))
                .fillColor(Color.argb(20, 0, 136, 255)));


        //create arraylist of markers and co-ordinates that will hold co-ordinates
        List<LatLng> coordinatesList = new ArrayList<>();
        List<Marker> markers = new ArrayList<>();
        List<BikeData> queryBike = new ArrayList<>();

        //loop through all co ordinates add markers but make them invisbile first, query will reveal them as needed.
        for (int i = 0; i < latitudeArray.size(); i++) {
            //create new marker with co-ordinates
            coordinatesList.add(new LatLng(latitudeArray.get(i), longditudeArray.get(i)));
            //  googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0), 3));
            Marker marker = googleMap.addMarker(new MarkerOptions().title("Bike found")
                    .position(coordinatesList.get(i)).visible(false));
            markers.add(marker);

            //override defaukt marker layout properties if i dit do this marker info window does not display correct
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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


        }//end for


        int i = 0;
        //clear array list that we use to populate query
        queryBike.clear();
        //clear previous query in DB we use this to generate listview with firebase
        mDatabaseQuery.removeValue();

        //reveal markers we want based on query. and set data to be in marker on click
        for (Marker marker : markers) {
            if (SphericalUtil.computeDistanceBetween(latLng, marker.getPosition()) < radius) {
                marker.setVisible(true);
                marker.setTitle("Make: " + bikeReturned.get(i).getMake());
                marker.setSnippet ("Model:" + bikeReturned.get(i).getModel() + "\nColour " + bikeReturned.get(i).getColor());;

                queryBike.add(bikeReturned.get(i));
            }
            i++;
        }//end for

        //show user feedback based on query
        if (queryBike.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "No bikes in that area", Toast.LENGTH_SHORT).show();
        }
       else{
            Toast.makeText(getActivity().getApplicationContext(), queryBike.size()+" results returned", Toast.LENGTH_SHORT).show();
        }

        //we store bike within the radus in  queryBike. we then push this to seprate DB node
        //we use this to populate our updated list adapter then
        for (BikeData bike : queryBike) {
            mDatabaseQuery.push().setValue(bike);
        }

        //method to populate list adapter
        handelQuery();

        //Moves camera to where ever user specified on input
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(8f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);

    }

    //================================================================================
    //=         query specif logic - populates list view for query and handels on click
    //=================================================================================

    public void handelQuery() {

        final FirebaseListAdapter<BikeData> bikeAdapterQuery = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item, mDatabaseQuery) {
            @Override
            protected void populateView(View v, BikeData model, int position) {


                // Find the TextView IDs of list_item.xml
                TextView makeView = (TextView) v.findViewById(R.id.make);
                TextView modelView = (TextView) v.findViewById(R.id.model);
                TextView sizeView = (TextView) v.findViewById(R.id.size);
                TextView colorView = (TextView) v.findViewById(R.id.color);
                TextView otherView = (TextView) v.findViewById(R.id.other);
                TextView lastlocationView = (TextView) v.findViewById(R.id.loaction);
                bike_image = (ImageView) v.findViewById(R.id.bike_image);

                // Log.v("***here", model.getModel());


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


        myListView.setAdapter(bikeAdapterQuery);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                stolenBike = bikeAdapterQuery.getItem(i);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Are you sure you wish to report a sighting of this bike?" +
                        "\nthis will notify the origional owner")
                        .setPositiveButton("Report Sighting", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).show();

            }
        });//end onClick for listView


    }//end query


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
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }//end method




}//end class



