package com.example.ronan.practicenavigationdrawer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.google.android.gms.fitness.data.zzs.Re;


public class WelcomeFragment extends Fragment {

    TextView registered;
    TextView stolen;
    TextView systemStolen;
    TextView user;
    String key_passed_fromList;
    String email = "";


    long countStolen;
    long countReg;
    long thisStolen = 0;

    //Firebase variables
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private DatabaseReference stolenBikesDatabse;

    public WelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        final View loadingIndicator = rootView.findViewById(R.id.loading_indicator_edit);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }


        Log.v("EMAIL", email);
        //seting up firebase DB refrences
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(email);
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");


        registered = (TextView) rootView.findViewById(R.id.bikesRegistered);
        stolen = (TextView) rootView.findViewById(R.id.personalStolen);
        systemStolen = (TextView) rootView.findViewById(R.id.totalStolen);
        user = (TextView) rootView.findViewById(R.id.userProfile);


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


                        if (bike.getRegisteredBy().equals(email)) {
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




