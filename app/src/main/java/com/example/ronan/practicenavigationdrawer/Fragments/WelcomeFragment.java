package com.example.ronan.practicenavigationdrawer.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ronan.practicenavigationdrawer.DataModel.BikeData;
import com.example.ronan.practicenavigationdrawer.DataModel.UserData;
import com.example.ronan.practicenavigationdrawer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class WelcomeFragment extends Fragment {

    private TextView registered;
    private TextView stolen;
    private TextView systemStolen;
    private TextView userHeading;
    TextView reportedSigntings;
    private CircleImageView profielPic;
    private FloatingActionButton floatingEditProfile;
    private String uniqueIdentifier = "";
    private String emailFull = "";


    long countStolen;
    long countReg;
    long thisStolen = 0;

    //Firebase variables
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private DatabaseReference stolenBikesDatabse;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference reportedStolen;
    private DatabaseReference readReportOfStolenQuery;

    ArrayList<String> registeredBikeKeys = new ArrayList<>();
    ArrayList<String> sightingBikeKeys = new ArrayList<>();
    ArrayList<BikeData> reportedSightingsList = new ArrayList<>();
    ArrayList<BikeData> registeredBikesList = new ArrayList<>();


    public WelcomeFragment() {
        // Required empty public constructor
    }

    private String imageValue = "";

    //======================================================================================
    // FireBase listener to grab the specific user data
    //======================================================================================
    ValueEventListener userDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.getValue(UserData.class) == null) {
                Log.v("Profile_fragment", "datasnap shot returned null in userDataListener");
                return;
            }

            user = dataSnapshot.getValue(UserData.class);
            imageValue = user.getUser_image_In_Base64();

            if (!imageValue.equals("imageValue")) {
                getBitMapFromString(imageValue);
            }

            //if no username is set use uniqueIdentifier from users email
            if (user.getUsername() != null) {
                userHeading.setText(user.getUsername());
            }else{
                userHeading.setText(uniqueIdentifier);
            }

        }

        UserData user = new UserData();

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    //======================================================================================
    // extract bitmap helper, this sets image view
    //======================================================================================
    public void getBitMapFromString(String imageAsString) {
        if (imageAsString != null) {
            if (imageAsString.equals("No image") || imageAsString == null) {
                // bike_image.setImageResource(R.drawable.not_uploaded);
                Log.v("***", "No image Found");
            } else {
                byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profielPic.setImageBitmap(bitmap);
            }
        } else {
            Log.v("***", "Null paramater passed into getBitMapFromString");
        }
    }


    //======================================================================================
    // onCreateView
    //======================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        final View loadingIndicator = rootView.findViewById(R.id.loading_indicator_edit);
//
//        if(!imageValue.equals("")){
//        }

        //get user uniqueIdentifier
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            emailFull = mFirebaseUser.getEmail();
            uniqueIdentifier = emailFull.split("@")[0];
        }


        //seting up firebase DB refrences
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(uniqueIdentifier);
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("User Profile Data");
        readReportOfStolenQuery = FirebaseDatabase.getInstance().getReference().child("Viewing bikes Reported Stolen").child(uniqueIdentifier);
        reportedStolen = FirebaseDatabase.getInstance().getReference().child("Reported Bikes");

        mDatabaseUsers.child(uniqueIdentifier).addValueEventListener(userDataListener);

        //get IDs
        registered = (TextView) rootView.findViewById(R.id.bikesRegistered);
        stolen = (TextView) rootView.findViewById(R.id.personalStolen);
        systemStolen = (TextView) rootView.findViewById(R.id.totalStolen);
        userHeading = (TextView) rootView.findViewById(R.id.userProfile);
        reportedSigntings = (TextView) rootView.findViewById(R.id.reportedSigntings);
        floatingEditProfile = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmEdit);
        profielPic = (CircleImageView) rootView.findViewById(R.id.profile_image);

        //Button click to launch edit profile page
        floatingEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setFragment
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.fragment_container, new Profile_Fragment()).commit();

            }
        });


        //event listener for checking how many bikes registered to you
        ValueEventListener CountRegListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //cout children nodes in this DB area.
                countReg = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    //grab all ket data from bikes registered, and grab al lbikes registered
                    registeredBikeKeys.add(snapshot.getKey().toString());
                    BikeData bike = snapshot.getValue(BikeData.class);
                    registeredBikesList.add(bike);
                }
                registered.setText("Bikes registered to you: " + countReg);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener


        //event listener for checking if any of your bikes are in stolrn system and how many total in DB
        ValueEventListener CountStolenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //show loading bar while working
                loadingIndicator.setVisibility(View.GONE);
                countStolen = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BikeData bike = snapshot.getValue(BikeData.class);
 ;
                    //check field is not null
                    if (bike.getRegisteredBy() != null) {
                        //check bikes in stolen DB to see if ay were registered by curret user if so note it
                        if (bike.getRegisteredBy().equals(emailFull)) {
                            thisStolen++;
                            Log.v("**reg", bike.getRegisteredBy());
                        } else {
                            Log.v("**reg", "no user");
                        }
                    }
                }//end for

               //set UI
                stolen.setText("Bikes you've listed as stolen: " + thisStolen);
                systemStolen.setText("Total bikes stolen in system: " + countStolen);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener



        //event listener for checking if bike is on stolen DB used to give correct user feedback
        ValueEventListener reportedStolenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                countReg = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //keys for all sightings
                    sightingBikeKeys.add(snapshot.getKey().toString());
                    BikeData bike = snapshot.getValue(BikeData.class);

                    //if a bike you have registered has been reported grab that
                    if (registeredBikeKeys.contains(snapshot.getKey().toString())) {
                        reportedSightingsList.add(bike);
                        Log.v("**rprint", Arrays.toString(reportedSightingsList.toArray()));
                        Log.v("**rprint make:", bike.getMake() + "Model: " + bike.getModel());

                        readReportOfStolenQuery.child(snapshot.getKey().toString()).setValue(bike);
                        reportedSigntings.setText("*Another User has reported a potental sighting your bikes, check mail");
                    }


                }


                reportedSightingsList.size();
                List<String> list3 = new ArrayList<>();

                for (String matches : registeredBikeKeys) {
                    if (sightingBikeKeys.contains(matches)) {
                        list3.add(matches);
                        Log.v("**size", "" + list3.size());
                    }
                }

                if (!list3.isEmpty()) {
                    // startAnim();
                }


                //   registered.setText("Bikes registered to you: " + countReg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener



        //call the listeners that set UI data
        mDatabase.addValueEventListener(CountRegListener);
        stolenBikesDatabse.addValueEventListener(CountStolenListener);
        reportedStolen.addValueEventListener(reportedStolenListener);

        return rootView;

    }// end onCreateView

}//end class




