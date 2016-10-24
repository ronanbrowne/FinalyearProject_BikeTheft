package com.example.ronan.practicenavigationdrawer;


import android.content.Intent;
import android.graphics.Bitmap;
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

    //DB refrence
    private DatabaseReference mDatabase;
    private  StorageReference storageRef;

    private FirebaseUser mFirebaseUser;

    String email = "User email";



    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get current user
            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //if its not null grab email address
        if (mFirebaseUser!=null) {
            email = mFirebaseUser.getEmail();
        }

        //for dev purposes output whos logged in , may remove
        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "user email"+email, Toast.LENGTH_SHORT);
        toast.show();


        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

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
        mThumbnailPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
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
                if ((make != null && !make.isEmpty()) || (model != null && !model.isEmpty()) ||  (color != null && !color.isEmpty()) || (framSizeString != null && !framSizeString.isEmpty())) {
                    BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, "Last seen location");

                    //get id part of email
                 email = email.split("@")[0];
                    //single entry
                    mDatabase.child(email).push().setValue(newBike);

                    //push is for multiple objects gives unique ID
                    // mDatabase.push().setValue(newBike);
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "BikeData Registered", Toast.LENGTH_SHORT);
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

    //Do this on result of activity , after we get the pic.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {

            Uri imageUri = data.getData();

            //
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("Exception", " : " + e.toString());
            }

            mThumbnailPreview.setImageBitmap(bitmap);
            base64 = imageConvertBase64(bitmap);
        }
    }

    //helper method to covert bitmap image into base64 for storage in Firebase DB
    private String imageConvertBase64(Bitmap pic) {
        Bitmap image = pic;//your image
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        // image.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return imageFile;

    }



}
