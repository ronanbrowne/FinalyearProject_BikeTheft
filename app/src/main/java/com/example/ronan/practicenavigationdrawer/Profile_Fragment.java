package com.example.ronan.practicenavigationdrawer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.ronan.practicenavigationdrawer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.ronan.practicenavigationdrawer.R.drawable.user;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile_Fragment extends Fragment {

    private String usernameGlobal;
    private String email;
    private String city;
    private String country;

    private FirebaseUser mFirebaseUser;
    //DB refrence
    private DatabaseReference mDatabase;

    EditText usernameET;
    EditText emailET;
    EditText cityET;
    EditText countryET;
    Button update;



    ValueEventListener userDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue(UserData.class)==null){

                Log.v("Profile_fragment", "datasnap shot returned null in userDataListener" );
                return;
            }

            user = dataSnapshot.getValue(UserData.class);
            Log.v("Profile_fragment", user.getUsername());
            Log.v("Profile_fragment", user.getUsername());
        }
        UserData user = new UserData();

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    public Profile_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("User Profile Data");




        //get instance of current user
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser!=null) {
            email = mFirebaseUser.getEmail();
            usernameGlobal = email.split("@")[0];
        }

        mDatabase.child(usernameGlobal).addValueEventListener(userDataListener);


        usernameET = (EditText) rootView.findViewById(R.id.username);
        emailET = (EditText) rootView.findViewById(R.id.email);
        cityET = (EditText) rootView.findViewById(R.id.city);
        countryET = (EditText) rootView.findViewById(R.id.country);
        update = (Button) rootView.findViewById(R.id.profile_button);



        emailET.setText(email);


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username =  usernameET.getText().toString();
                String email =  usernameET.getText().toString();
                String city =  usernameET.getText().toString();
                String Country =  usernameET.getText().toString();

                UserData userData = new UserData(city,username,"imageValue","dateString",email,Country);

                mDatabase.child(usernameGlobal).setValue(userData);


            }
        });





        return rootView;
    }

}
