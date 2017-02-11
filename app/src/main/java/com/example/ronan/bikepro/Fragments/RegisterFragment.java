package com.example.ronan.bikepro.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.tooltip.Tooltip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    // variables
    private EditText bikeMake;
    private EditText bikeColor;
    private EditText bikeFrameSize;
    private EditText bikeOther;
    private EditText bikeModel;
    private EditText edit_bike_UUID;
    private FloatingActionButton upload;
    private FloatingActionButton addBikeFloatingActionButton;
    private ImageView mThumbnailPreview;
    Bitmap bitmap;
    String base64 = "No image";
    private ImageView info;
    private ImageView infoUUID;

    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    //Fier base Vars
    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private FirebaseUser mFirebaseUser;

    private String uniqueIdentifier = "";


    public RegisterFragment() {
        // Required empty public constructor
    }

    //======================================================================================
    // dialog listener for pop up to decide to launch camera or gallery
    //======================================================================================
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
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


    //======================================================================================
    // onCreateView
    //======================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get current user
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //if its not null grab email address use ths as uniqe id for parent node on DB for all this users bikes
        if (mFirebaseUser != null) {
            uniqueIdentifier = mFirebaseUser.getEmail();
        }


        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //get IDs
        bikeMake = (EditText) rootView.findViewById(R.id.edit_bike_make);
        bikeModel = (EditText) rootView.findViewById(R.id.edit_bike_model);
        bikeColor = (EditText) rootView.findViewById(R.id.edit_bike_colour);
        edit_bike_UUID = (EditText) rootView.findViewById(R.id.edit_bike_UUID);
        bikeFrameSize = (EditText) rootView.findViewById(R.id.edit_bike_size);
        bikeOther = (EditText) rootView.findViewById(R.id.edit_othe_features);
        mThumbnailPreview = (ImageView) rootView.findViewById(R.id.upload_image);
        upload = (FloatingActionButton) rootView.findViewById(R.id.floatingUpload);
        addBikeFloatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floatingAdd);
        info = (ImageView) rootView.findViewById(R.id.infoSize);
        infoUUID = (ImageView) rootView.findViewById(R.id.infoUUID);

        //setting up FireBase DB
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User");


        //handel click event to upload a picter to app
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Use picture from Gallery or launch camera?").setPositiveButton("Gallery", dialogClickListener)
                        .setNegativeButton("Camera", dialogClickListener).show();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tooltip tooltip = new Tooltip.Builder(info)
                        .setText("This is the height of the bike.\nAverage adult male frame size: 22 \nAverage adult female frame size: 18\nAverage child's size (10+ years): 13")
                        .setTextColor(ContextCompat.getColor(getContext(),R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.cyan)).show();
            }
        });


        infoUUID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tooltip tooltip = new Tooltip.Builder(infoUUID)
                        .setText("This is the unique sensor code\nThis will allow your bike\nTo be tracked in event of theft")
                        .setTextColor(ContextCompat.getColor(getContext(),R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.cyan)).show();
            }
        });

        //when a user click add bike
        addBikeFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //set up
                int frameSize = 0;
                boolean stolen = false;

                //grab Text from UI
                String make = bikeMake.getText().toString();
                String model = bikeModel.getText().toString();
                String color = bikeColor.getText().toString();
                String other = bikeOther.getText().toString();
                String frameSizeString = bikeFrameSize.getText().toString();
                String beacon_UUID = edit_bike_UUID.getText().toString();

                //if framesize has data turn into int and catch exception
                if (frameSizeString != null || !frameSizeString.isEmpty()) {
                    try {
                        frameSize = Integer.parseInt(frameSizeString);
                        ;
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity().getApplicationContext(), "All fields are required except \"other\"", Toast.LENGTH_SHORT).show();
                    }
                }


                //Validate editText fields that they are not empty
                if ((bikeMake.getText().toString().trim().length() == 0) || (bikeModel.getText().toString().trim().length() == 0) || (bikeColor.getText().toString().trim().length() == 0) || (bikeFrameSize.getText().toString().trim().length() == 0)) {
                    Toast.makeText(getActivity().getApplicationContext(), "All fields are required except \"other\"", Toast.LENGTH_SHORT).show();
                }
                //if all fields are vilid
                else {

                    //newBike object using constructor to populate attributes
                    BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, "N/A", 0, 0, uniqueIdentifier,beacon_UUID);

                    //get id part of email use this for where to place in DB. Firebase cant have a @ in DB refrence
                    uniqueIdentifier = uniqueIdentifier.split("@")[0];

                    //push this newBike object to the DB under the child node of a users uniqueIdentifier
                    //in this case uniqueIdentifier would be ronan if email address was ronan@gmail.com
                    mDatabase.child(uniqueIdentifier).push().setValue(newBike);

                    //re set fields after
                    bikeMake.setText("");
                    bikeModel.setText("");
                    bikeColor.setText("");
                    bikeFrameSize.setText("");
                    bikeOther.setText("");
                    mThumbnailPreview.setImageResource(R.drawable.uploadimage);

                    //return user to welcome screen
                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction().replace(R.id.fragment_container, new WelcomeFragment()).commit();

                    //user feedback
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Bike Data Registered", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    //======================================================================================
    // method to launch cam
    //======================================================================================
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check a app can handel this
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    //======================================================================================
    // method to launch gallery
    //======================================================================================
    public void dispatchGrabImageFromGalleryItent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), SELECT_PICTURE);
    }


    //======================================================================================
    // Do this on result of activity , choose depending on result code carry out diff action
    //======================================================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //from gallery
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
                mThumbnailPreview.setImageBitmap(bitmap);
                base64 = imageConvertBase64(bitmap);
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("message", "the user cancelled the request");
            }
        }
        //from cam
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mThumbnailPreview.setImageBitmap(imageBitmap);
                base64 = imageConvertBase64(imageBitmap);
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("message", "the user cancelled the request");
            }
        }
    }


    //======================================================================================
    // helper method to covert bitmap image into base64 for storage in Firebase Database
    //======================================================================================
    private String imageConvertBase64(Bitmap pic) {
        Bitmap image = pic;
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return imageFile;
    }


}//end class
