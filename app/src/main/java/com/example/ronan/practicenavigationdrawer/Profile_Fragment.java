package com.example.ronan.practicenavigationdrawer;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ronan.practicenavigationdrawer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.ronan.practicenavigationdrawer.MainFragment.REQUEST_IMAGE_CAPTURE;
import static com.example.ronan.practicenavigationdrawer.R.drawable.user;
import static com.example.ronan.practicenavigationdrawer.R.id.userProfile;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile_Fragment extends Fragment {

    private static final int SELECT_PICTURE = 0;
    private String usernameGlobal;
    private String email;
    private String address;

    private FirebaseUser mFirebaseUser;
    //DB refrence
    private DatabaseReference mDatabase;

    EditText usernameET;
    EditText emailET;
    EditText addressET;

    TextView profileHeading;
    FloatingActionButton update;
    FloatingActionButton picUpdate;



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
        addressET = (EditText) rootView.findViewById(R.id.address);
        profileHeading = (TextView) rootView.findViewById(R.id.userProfile);
        update = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmEditProfile);
        picUpdate = (FloatingActionButton) rootView.findViewById(R.id.updatePic);

        emailET.setText(email);
        profileHeading.setText(usernameGlobal);

        //upload image
        picUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Use picture from Gallery or launch camera?").setPositiveButton("Gallery", getDialogClickListenerImage)
                        .setNegativeButton("Camera", getDialogClickListenerImage).show();

            }
        });


                update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username =  usernameET.getText().toString();
                String email =  emailET.getText().toString();
                String address =  addressET.getText().toString();


                UserData userData = new UserData(address,username,"imageValue","dateString",email);

                mDatabase.child(usernameGlobal).setValue(userData);


            }
        });

        return rootView;
    }


    //dialog listener for pop up to decide to launch camera or gallery
    DialogInterface.OnClickListener getDialogClickListenerImage = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //gallery
                    dispatchGrabImageFromGalleryItent();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //camera
                    dispatchTakePictureIntent();
                    break;
            }
        }
    };

    //method to launch camera
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check a app can handel this
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //method to launch gallery
    public void dispatchGrabImageFromGalleryItent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), SELECT_PICTURE);
    }

}
