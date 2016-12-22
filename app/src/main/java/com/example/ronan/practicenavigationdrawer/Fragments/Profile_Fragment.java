package com.example.ronan.practicenavigationdrawer.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronan.practicenavigationdrawer.DataModel.UserData;
import com.example.ronan.practicenavigationdrawer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile_Fragment extends Fragment {

    //variables

    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String uniqueIdentifier;
    private String email;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    private EditText usernameET;
    private EditText emailET;
    private EditText addressET;
    private ImageView upload_image;


    TextView profileHeading;
    FloatingActionButton update;
    FloatingActionButton picUpdate;

    Bitmap bitmap;
    String base64 = "imageValue";
    String imageValue = "";


    //================================================================================
    //=      Grab current users data from DB
    //=================================================================================
    ValueEventListener userDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue(UserData.class) == null) {
                Log.v("Profile_fragment", "datasnap shot returned null in userDataListener");
                return;
            }

            user = dataSnapshot.getValue(UserData.class);
            usernameET.setText(user.getUsername());
            emailET.setText(user.getEmail());
            addressET.setText(user.getAddress());
            imageValue = user.getUser_image_In_Base64();

            //if no username is set use uniqueIdentifier from users email
            if (user.getUsername() != null) {
                profileHeading.setText(user.getUsername());
            }else{
                profileHeading.setText(uniqueIdentifier);
            }

            //if there was a image set grab it and set pic
            if (!imageValue.equals("imageValue")) {
                getBitMapFromString(imageValue);
            }

        }



        UserData user = new UserData();

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    //constructor
    public Profile_Fragment() {
        // Required empty public constructor
    }


    //================================================================================
    //=      onCreateView
    //=================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("User Profile Data");


        //get instance of current user
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            uniqueIdentifier = email.split("@")[0];
        }



        usernameET = (EditText) rootView.findViewById(R.id.username);
        emailET = (EditText) rootView.findViewById(R.id.email);
        addressET = (EditText) rootView.findViewById(R.id.address);
        profileHeading = (TextView) rootView.findViewById(R.id.userProfile);
        update = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmEditProfile);
        picUpdate = (FloatingActionButton) rootView.findViewById(R.id.updatePic);
        upload_image = (ImageView) rootView.findViewById(R.id.profile_image);

        //get default
        //   Bitmap bitmap = ((BitmapDrawable)upload_image.getDrawable()).getBitmap();
        //  base64= imageConvertBase64(bitmap);
        mDatabase.child(uniqueIdentifier).addValueEventListener(userDataListener);

        picUpdate.setVisibility(View.VISIBLE);

        emailET.setText(email);
        profileHeading.setText(uniqueIdentifier);

        //upload image
        picUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Use picture from Gallery or launch camera?").setPositiveButton("Gallery", getDialogClickListenerImage)
                        .setNegativeButton("Camera", getDialogClickListenerImage).show();

            }
        });

        //listener for putton to update profile
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //grab UI field data
                String username = usernameET.getText().toString();
                String email = emailET.getText().toString();
                String address = addressET.getText().toString();


                //todo remove date
                mDatabase = FirebaseDatabase.getInstance().getReference().child("User Profile Data");
                long date = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM MM dd, yyyy h:mm a");
                String dateString = sdf.format(date);


                UserData userData = new UserData(address, username, base64, dateString, email);


                mDatabase.child(uniqueIdentifier).setValue(userData);

                Toast.makeText(getActivity().getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();

                mDatabase.child(uniqueIdentifier).addValueEventListener(userDataListener);


            }
        });

        return rootView;
    } //end onCreateView





    //================================================================================
    //=   Dialog listener for pop up to decide to launch camera or gallery
    //=================================================================================
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

    //================================================================================
    //=   method to launch camera
    //=================================================================================
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check a app can handel this
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //================================================================================
    //=   Dialog listener for pop up to decide to launch camera or gallery
    //=================================================================================
    public void dispatchGrabImageFromGalleryItent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), SELECT_PICTURE);
    }

    //================================================================================
    //=   Do this on result of activity , after we get the pic.
    //=================================================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if it was cam
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v("Exception", " : " + e.toString());
                }
                upload_image.setImageBitmap(bitmap);
                base64 = imageConvertBase64(bitmap);
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("message", "the user cancelled the request");
            }
        }
        //if gallery
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                upload_image.setImageBitmap(imageBitmap);
                base64 = imageConvertBase64(imageBitmap);
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("message", "the user cancelled the request");
            }
        }
    }

    //================================================================================
    //=  helper method to covert bitmap image into base64 for storage in Firebase DB
    //=================================================================================
    private String imageConvertBase64(Bitmap pic) {
        Bitmap image = pic;//your image
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        // image.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return imageFile;
    }

    //=================================================================================
    //extract bitmap helper, this sets image view
    //=================================================================================
    public void getBitMapFromString(String imageAsString) {
        if (imageAsString != null) {
            if (imageAsString.equals("No image") || imageAsString == null) {
                // bike_image.setImageResource(R.drawable.not_uploaded);
                Log.v("***", "No image Found");
            } else {
                byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                upload_image.setImageBitmap(bitmap);
            }
        } else {
            Log.v("***", "Null paramater passed into getBitMapFromString");
        }
    }//end method

}// end class
