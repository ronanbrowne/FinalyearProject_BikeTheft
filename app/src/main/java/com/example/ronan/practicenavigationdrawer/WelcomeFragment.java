package com.example.ronan.practicenavigationdrawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.ronan.practicenavigationdrawer.R.id.upload_image;
import static com.google.android.gms.fitness.data.zzs.Re;


public class WelcomeFragment extends Fragment {

    TextView registered;
    TextView stolen;
    TextView systemStolen;
    TextView user;
    CircleImageView profielPic;
    FloatingActionButton floatingEditProfile;
    String key_passed_fromList;
    String email = "";
    String emailFull = "";


    long countStolen;
    long countReg;
    long thisStolen = 0;

    //Firebase variables
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private DatabaseReference stolenBikesDatabse;
    private DatabaseReference mDatabaseUsers;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    String imageValue = "";


    ValueEventListener userDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {


            if (dataSnapshot.getValue(UserData.class) == null) {

                Log.v("Profile_fragment", "datasnap shot returned null in userDataListener");
                return;
            }

        user = dataSnapshot.getValue(UserData.class);
//            usernameET.setText(user.getUsername());
//            String email.setText(user.getEmail());
//            addressET.setText(user.getAddress());

            imageValue = user.getUser_image_In_Base64();



            if(!imageValue.equals("imageValue")){
                getBitMapFromString(imageValue);}

        }    UserData user = new UserData();

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //extract bitmap helper, this sets image view
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



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        final View loadingIndicator = rootView.findViewById(R.id.loading_indicator_edit);

        if(!imageValue.equals("")){

        }


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            emailFull = mFirebaseUser.getEmail();
            email = emailFull.split("@")[0];
        }


        Log.v("EMAIL", email);
        //seting up firebase DB refrences
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(email);
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("User Profile Data");
        mDatabaseUsers.child(email).addValueEventListener(userDataListener);


        registered = (TextView) rootView.findViewById(R.id.bikesRegistered);
        stolen = (TextView) rootView.findViewById(R.id.personalStolen);
        systemStolen = (TextView) rootView.findViewById(R.id.totalStolen);
       user = (TextView) rootView.findViewById(R.id.userProfile);
        floatingEditProfile = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmEdit);
        profielPic = (CircleImageView) rootView.findViewById(R.id.profile_image);

        floatingEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //setFragment
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.fragment_container, new Profile_Fragment()).commit();

            }
        });


        //event listener for checking if bike is on stolen DB used to give correct user feedback
        ValueEventListener CountRegListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                countReg = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // countReg++;
                }
                registered.setText("Bikes registered to you: " + countReg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener


        //event listener for checking if bike is on stolen DB used to give correct user feedback
        ValueEventListener CountStolenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadingIndicator.setVisibility(View.GONE);
                countStolen = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    BikeData bike = snapshot.getValue(BikeData.class);

                        //check field is not null
                    if (bike.getRegisteredBy() != null) {


                        if (bike.getRegisteredBy().equals(emailFull)) {
                            thisStolen++;
                            Log.v("**reg", bike.getRegisteredBy());

                        } else {
                            Log.v("**reg", "no user");
                        }

                    }
                }

                stolen.setText("Bikes you've listed as stolen: " + thisStolen);

                systemStolen.setText("Total bikes stolen in system: " + countStolen);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener

        user.setText(email);

        mDatabase.addValueEventListener(CountRegListener);
        stolenBikesDatabse.addValueEventListener(CountStolenListener);

        return rootView;

    }

}




