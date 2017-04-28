package com.example.ronan.bikepro.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private final String DEFAULT_BIKE_IMAGE = "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";
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
    String base64 = DEFAULT_BIKE_IMAGE ;
    private ImageView info;
    private ImageView infoUUID;
    private LinearLayout container_image;
    private LinearLayout container_main;

    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    //Fier base Vars
    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private FirebaseUser mFirebaseUser;
    Activity contex;

    private String uniqueIdentifier = "";
    private String make;
    private String model;
    private String color;
    private String other;
    private String frameSizeString;
    private String beacon_UUID;
    int frameSize = 0;
    boolean stolen = false;

    public RegisterFragment() {
        // Required empty public constructor
    }



    //======================================================================================
    // dialog listener for proceeding with no image
    //======================================================================================
    DialogInterface.OnClickListener dialogClickListenerNoImage = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    //newBike object using constructor to populate attributes
                    BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, "N/A", 0, 0, uniqueIdentifier, beacon_UUID, 0);

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
                    edit_bike_UUID.setText("");
                    mThumbnailPreview.setImageResource(R.drawable.uploadimage);

                    //return user to welcome screen
                    FragmentManager fm = getFragmentManager();
                     fm.beginTransaction().replace(R.id.fragment_container, new WelcomeFragment()).commit();

                    //user feedback
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Bike Data Registered without image", Toast.LENGTH_SHORT);
                    toast.show();


                    break;

                case DialogInterface.BUTTON_NEGATIVE:

                    AlertDialog.Builder builder = new AlertDialog.Builder(contex);
                    builder.setMessage("Use picture from Gallery or launch camera?").setPositiveButton("Gallery", dialogClickListener)
                            .setNegativeButton("Camera", dialogClickListener).show();

                    //feedback
                   // Toast.makeText(contex.getApplicationContext(), "Select image", Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };




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
        contex= getActivity();

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
        container_image = (LinearLayout) rootView.findViewById(R.id.container_image);
        container_main = (LinearLayout) rootView.findViewById(R.id.container_main);


        setBackGroundImage();

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
                        .setTextColor(ContextCompat.getColor(getContext(), R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan)).show();
            }
        });


        infoUUID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tooltip tooltip = new Tooltip.Builder(infoUUID)
                        .setText("This is the unique sensor code\nThis will allow your bike\nTo be tracked in event of theft")
                        .setTextColor(ContextCompat.getColor(getContext(), R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan)).show();
            }
        });

        //when a user click add bike
        addBikeFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //set up


                //grab Text from UI
                 make = bikeMake.getText().toString();
                 model = bikeModel.getText().toString();
                 color = bikeColor.getText().toString();
                 other = bikeOther.getText().toString();
                 frameSizeString = bikeFrameSize.getText().toString();
                 beacon_UUID = edit_bike_UUID.getText().toString();

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
                    Toast.makeText(getActivity().getApplicationContext(), "All fields are required except \"other\" and \"Beacon ID\"", Toast.LENGTH_SHORT).show();
                }
                //if all fields are vilid
                else {

                    if(base64.equals(DEFAULT_BIKE_IMAGE)){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("No image set");
                        builder.setMessage("Are you sure you wish to proceed without uploading a bike image?\n\n" +
                                "Having a picture of your bike will greatly increase the chances of it being returned if stolen.").setPositiveButton("Proceed with no image", dialogClickListenerNoImage)
                                .setNegativeButton("Select image now", dialogClickListenerNoImage).show();
                    }
                    else{

                        //newBike object using constructor to populate attributes
                        BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, "N/A", 0, 0, uniqueIdentifier, beacon_UUID, 0);

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
                        edit_bike_UUID.setText("");
                        mThumbnailPreview.setImageResource(R.drawable.uploadimage);

                   //     return user to welcome screen
                        FragmentManager fm = getFragmentManager();
                         fm.beginTransaction().replace(R.id.fragment_container, new WelcomeFragment()).commit();

                        //user feedback
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Bike Data Registered", Toast.LENGTH_SHORT);
                        toast.show();


                    }


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

    public void setBackGroundImage(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String themePref = preferences.getString("list_preference", "");

        if (themePref.equals("AppThemeSecondary")){
            container_image.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.background_shadow_night));
            container_main.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.background_shadow_night));
        }
        else{
            container_image.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.background_shadow));
            container_main.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.background_shadow));

        }
    }


}//end class
