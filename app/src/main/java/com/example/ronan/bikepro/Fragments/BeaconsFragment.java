package com.example.ronan.bikepro.Fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;


public class BeaconsFragment extends Fragment {

    private TextView listArea;
    private ImageView info;
    private ImageView bike_image;
    private ListView listViewChooseBike;

    private DatabaseReference myBikesDB;
    private DatabaseReference selectedBikeToLinksTo;
    private Query queryBikesUsingBeacons;
    private FirebaseUser mFirebaseUser;
    private String uniqueIdentifier;
    private RelativeLayout main_choose_bike;


    public BeaconsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacons, container, false);

        info = (ImageView) rootView.findViewById(R.id.infobeacon);
        main_choose_bike = (RelativeLayout) rootView.findViewById(R.id.main_choose_bike);
        listArea = (TextView) rootView.findViewById(R.id.choose);
        listViewChooseBike = (ListView) rootView.findViewById(R.id.listViewChooseBike);

        setBackGroundImage();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            uniqueIdentifier = mFirebaseUser.getEmail();
            uniqueIdentifier = uniqueIdentifier.split("@")[0];
        }

        //refrence to reurn all bikes registered by that user which are not null
        myBikesDB = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(uniqueIdentifier);


        queryBikesUsingBeacons = myBikesDB.orderByChild("beaconUUID").startAt("!").endAt("~");


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tooltip tooltip = new Tooltip.Builder(info)
                        .setText("How it works:\n\nYour phone will automatically link to your bikes sensor once in range\n\n" +
                                "Should there be a break in the communication for any reason you will be alerted.")
                        .setTextColor(ContextCompat.getColor(getContext(), R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan)).show();


                final Animation animation = new AlphaAnimation((float) 0.5, 0); // Change alpha from fully visible to invisible
                animation.setDuration(500); // duration - half a second
                animation.setInterpolator(new LinearInterpolator()); // do not alter
                animation.setRepeatCount(1); // Repeat animation
                animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the

                info.startAnimation(animation);


            }
        });

        final FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item_monitering, queryBikesUsingBeacons) {
            @Override
            protected void populateView(View v, BikeData model, int position) {
                //handling displaying of loading bar once data is recieved hide it.
                queryBikesUsingBeacons.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("*count", " " + dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                // Find the TextView IDs of list_item.xml
                TextView makeView = (TextView) v.findViewById(R.id.make);
                TextView modelView = (TextView) v.findViewById(R.id.model);
                TextView colorView = (TextView) v.findViewById(R.id.color);
                bike_image = (ImageView) v.findViewById(R.id.bike_image);

                //setting the textViews to Bike data
                makeView.setText(model.getMake());
                modelView.setText(model.getModel());
                colorView.setText(model.getColor());

                //call method to set image, which turns base64 string to image
                getBitMapFromString(model.getImageBase64(), bike_image);

            }
        };

        listViewChooseBike.setAdapter(bikeAdapter);

        //what happens when user clicks on am item
        listViewChooseBike.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //get the RD refrence to the Item clicked and store this in data holder class
                selectedBikeToLinksTo = bikeAdapter.getRef(i);


                selectedBikeToLinksTo.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        BikeData b = dataSnapshot.getValue(BikeData.class);

                        String key = b.getBeaconUUID();
                        String major = key.split(":")[0];
                        String minor = key.split(":")[1];
                        int selectedBikeMajor = Integer.parseInt(major);
                        int selectedBikeMinor = Integer.parseInt(minor);

                        Bundle bundle = new Bundle();
                        //    bundle.putString("dB_Ref", itemRef.getKey());
                        bundle.putString("dB_Ref", selectedBikeToLinksTo.getKey());
                        bundle.putInt("BeaconMinorID", selectedBikeMinor);
                        bundle.putInt("BeaconMajorID", selectedBikeMajor);


                        BeaconConnect connectFragment = new BeaconConnect();
                        connectFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, connectFragment);
                        fragmentTransaction.commit();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        return rootView;
    } //end onCreate


    //===============================================
    // extract bitmap helper, this sets image view
    //===============================================
    public void getBitMapFromString(String imageAsString, ImageView iv) {
        if (imageAsString == "No image" || imageAsString == null) {
            Log.v("***", "No image Found");
        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            iv.setImageBitmap(bitmap);
        }
    }// end getBitMapFromString

    public void setBackGroundImage() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String themePref = preferences.getString("list_preference", "");

        if (themePref.equals("AppThemeSecondary")) {
            main_choose_bike.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_shadow_night));
        } else {
            main_choose_bike.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_shadow));
        }
    }


}
