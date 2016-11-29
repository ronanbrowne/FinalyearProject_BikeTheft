package com.example.ronan.practicenavigationdrawer;


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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    //initilize variables
    private EditText bikeMake;
    private EditText bikeColor;
    private EditText bikeFrameSize;
    private EditText bikeOther;
    private EditText bikeModel;
    private FloatingActionButton upload;
    private FloatingActionButton addFloat;


    private Button add;
    // private Button imageUpload;
    private ImageView mThumbnailPreview;

    Bitmap bitmap;

    String base64 = "No image";
    private static final int SELECT_PICTURE = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    //DB refrence
    private DatabaseReference mDatabase;
    private StorageReference storageRef;

    private FirebaseUser mFirebaseUser;

    String email = "User email";


    public MainFragment() {
        // Required empty public constructor
    }

    //dialog listener for pop up to decide to launch camera or gallery
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get current user
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //if its not null grab email address
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
        }

        //for dev purposes output whos logged in , may remove
        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Logged in as: " + email, Toast.LENGTH_SHORT);
        toast.show();


        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //get map

        //get IDs
        bikeMake = (EditText) rootView.findViewById(R.id.edit_bike_make);
        bikeModel = (EditText) rootView.findViewById(R.id.edit_bike_model);
        bikeColor = (EditText) rootView.findViewById(R.id.edit_bike_colour);
        bikeFrameSize = (EditText) rootView.findViewById(R.id.edit_bike_size);
        bikeOther = (EditText) rootView.findViewById(R.id.edit_othe_features);
        mThumbnailPreview = (ImageView) rootView.findViewById(R.id.upload_image);
        upload = (FloatingActionButton) rootView.findViewById(R.id.floatingUpload);
        addFloat = (FloatingActionButton) rootView.findViewById(R.id.floatingAdd);
        //imageUpload = (Button) rootView.findViewById(R.id.imageupload_button);
        add = (Button) rootView.findViewById(R.id.add_button);

        //seting up firebase DB
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


        addFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int frameSize;
                boolean stolen = false;
                String make = bikeMake.getText().toString();
                String model = bikeModel.getText().toString();


                String framSizeString = bikeFrameSize.getText().toString();

                if (framSizeString != null || !framSizeString.isEmpty()) {
                    frameSize = Integer.parseInt(framSizeString);
                } else {
                    frameSize = 0;
                }
                String color = bikeColor.getText().toString();
                String other = bikeOther.getText().toString();

                //error handeling
                if ((make != null && !make.isEmpty()) || (model != null && !model.isEmpty()) || (color != null && !color.isEmpty()) || (framSizeString != null && !framSizeString.isEmpty())) {
                    BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, "N/A", 0, 0);

                    //get id part of email
                    email = email.split("@")[0];
                    //single entry
                    mDatabase.child(email).push().setValue(newBike);

                    //push is for multiple objects gives unique ID
                    // mDatabase.push().setValue(newBike);
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Bike Data Registered", Toast.LENGTH_SHORT);
                    toast.show();

                } else {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Please fill all required fields", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;


    }

    //method to launch cam
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check a app can handel this
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //method to launch cGalery

    public void dispatchGrabImageFromGalleryItent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), SELECT_PICTURE);
    }


    //Do this on result of activity , choose depending on result code
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    //helper method to covert bitmap image into base64 for storage in Firebase Database
    private String imageConvertBase64(Bitmap pic) {
        Bitmap image = pic;//your image
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return imageFile;

    }


}
