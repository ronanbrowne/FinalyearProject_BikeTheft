package com.example.ronan.practicenavigationdrawer;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {

    //For logcat
    private static final String TAG = "Edit_Bike_Fragment";

    //variables for UI
    private EditText bikeMake;
    private EditText bikeModel;
    private EditText bikeColor;
    private EditText bikeSize;
    private EditText bikeLastSeen;
    private EditText bikeOther;
    private TextView lastSeen;
    private CheckBox bikeStolen;
    private FloatingActionButton imageUpload;
    private FloatingActionButton comfirmEdit;
    private FloatingActionButton geoCode;
    private FloatingActionButton floatingDelete;
    private LinearLayout geoCodeArea;
    private ImageView upload_image;
    private boolean geoCodeClicked;

    private String base64 = "No image";
    private String uniqueIdentifier = "";
    private String emailFull ="";

    private String dB_KeyRefrence_fromBundle;
    private double latitude = 0;
    private double longitud = 0;
    private String name;

    private Bitmap bitmap;

    private boolean successfullEdit = false;


    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    //Firebase variables
    private DatabaseReference mDatabase;
    private DatabaseReference stolenBikesDatabse;
    private boolean inStolenDB = false;
    private FirebaseUser mFirebaseUser;

    //holds all DB keys for bikes listed as stolen
    List<String> stolenKeysList;

    //===================================================================================
    //=        dialog listener for pop up to confirm delete
    //===================================================================================
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    stolenBikesDatabse.child(dB_KeyRefrence_fromBundle).removeValue();
                    mDatabase.removeValue();

                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction().replace(R.id.fragment_container, new EditFragmentList()).commit();

                    //feedback
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Delete successful", Toast.LENGTH_SHORT);
                    toast.show();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:

                    //feedback
                    Toast toastCanceled = Toast.makeText(getActivity().getApplicationContext(), "Delete canceled", Toast.LENGTH_SHORT);
                    toastCanceled.show();
                    break;
            }
        }
    };


    //===================================================================================
    //=        dialog listener for pop up to decide to launch camera or gallery
    //===================================================================================
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


    //===================================================================================
    // Firebase event listener for checking if bike is on stolen DB used to give correct user feedback
    //===================================================================================
    ValueEventListener ifStolen = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            stolenKeysList = new ArrayList<>();
            //adding keys to array list for later oomparison
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                stolenKeysList.add(snapshot.getKey());
            }

            //looping through arraylist of DB keys for all stolen bikes
            for (String temp : stolenKeysList) {
                //if the currect keq is also in stolen db mark as true
                if (dB_KeyRefrence_fromBundle.equals(temp)) {
                    inStolenDB = true;
                }
            }//end for
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("*", "Error on ifStolen ValueEventListener: " + databaseError.toString());

        }
    }; //end listener


    //===================================================================================
    // Firebase event listener for populateing UI fields from DB
    //===================================================================================
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.getValue(BikeData.class) == null) {
                Log.v(TAG, "doing nothing snapshot null");
                return;
            }
            //grab snapshot and put in bike Object
            BikeData mybike = dataSnapshot.getValue(BikeData.class);

            //set UI fields from data
            bikeMake.setText(mybike.getMake());
            bikeModel.setText(mybike.getModel());
            bikeColor.setText(mybike.getColor());
            bikeSize.setText(String.valueOf(mybike.getFrameSize()));
            bikeOther.setText(mybike.getOther());
            bikeLastSeen.setText(mybike.getLastSeen());
            base64 = mybike.getImageBase64();
            getBitMapFromString(base64);

            //handel checkbox
            if (mybike.isStolen()) {
                bikeStolen.setChecked(true);
                Log.v(TAG, "if**" + bikeStolen.toString());
            } else {
                bikeStolen.setChecked(false);
                Log.v(TAG, "else**" + bikeStolen.toString());
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    public EditFragment() {
        // Required empty public constructor
    }


    //===================================================================================
    // onCreateView
    //===================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get the DB reference key for this particular bike.
        //We set this in previous screen EditBikeList by passin git in arguments as a bundle to this fragment
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            dB_KeyRefrence_fromBundle = bundle.getString("dB_Ref");
            Toast.makeText(getActivity().getApplicationContext(),dB_KeyRefrence_fromBundle , Toast.LENGTH_SHORT).show();

        }

        //DB set up ad attach listeer
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        stolenBikesDatabse.addValueEventListener(ifStolen);

        //boolean tracking to see if a user has retrieved geo-coOrdinates for bike, needed if listed stolen
        geoCodeClicked = false;

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        //get FireBase user. im using email to uniquely ID JSON nodes in DB. Firebase cant accept special characters in node reference
        // i get around this by splitting the email address and dropping everything after @ symbol. whats remaining is used as a ID in DB
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            emailFull = mFirebaseUser.getEmail();
            uniqueIdentifier = emailFull.split("@")[0];
        }

        //get UI id's
        lastSeen = (TextView) rootView.findViewById(R.id.lastSeen);
        bikeMake = (EditText) rootView.findViewById(R.id.edit_bike_make);
        bikeLastSeen = (EditText) rootView.findViewById(R.id.edit_last_seen);
        bikeModel = (EditText) rootView.findViewById(R.id.edit_bike_model);
        bikeColor = (EditText) rootView.findViewById(R.id.edit_bike_colour);
        bikeSize = (EditText) rootView.findViewById(R.id.edit_bike_size);
        bikeOther = (EditText) rootView.findViewById(R.id.edit_othe_features);
        bikeStolen = (CheckBox) rootView.findViewById(R.id.bike_stolen);
        upload_image = (ImageView) rootView.findViewById(R.id.upload_image);
        imageUpload = (FloatingActionButton) rootView.findViewById(R.id.floatingUpload);
        comfirmEdit = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmEdit);
        geoCode = (FloatingActionButton) rootView.findViewById(R.id.floatingGeoCode);
        floatingDelete = (FloatingActionButton) rootView.findViewById(R.id.floatingDelete);
        geoCodeArea = (LinearLayout) rootView.findViewById(R.id.geoLocationLayout);


        //listener for checkbox, reveal extra stolen options if stolen
        bikeStolen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (bikeStolen.isChecked()) {
                    geoCodeArea.setVisibility(View.VISIBLE);
                } else {
                    geoCodeArea.setVisibility(View.GONE);
                }
            }
        });

        //upload image listeer
        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Use picture from Gallery or launch camera?").setPositiveButton("Gallery", getDialogClickListenerImage)
                        .setNegativeButton("Camera", getDialogClickListenerImage).show();

            }
        });


        //seting up firebase DB refrences, where we will be storing the object we push
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(uniqueIdentifier).child(dB_KeyRefrence_fromBundle);
        mDatabase.addValueEventListener(bikeListener);

        //update button listeer
        comfirmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stolenBikesDatabse.addValueEventListener(ifStolen);

                //grab data from fields
                int frameSize = 0;
                boolean stolen = bikeStolen.isChecked();
                String make = bikeMake.getText().toString();
                String model = bikeModel.getText().toString();
                String frameSizeString = bikeSize.getText().toString();
                String color = bikeColor.getText().toString();
                String other = bikeOther.getText().toString();
                String lastSeen = bikeLastSeen.getText().toString();

                //if frame size has data turn into int and catch exception
                if (frameSizeString != null || !frameSizeString.isEmpty()) {
                    try {
                        frameSize = Integer.parseInt(frameSizeString);
                        ;
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity().getApplicationContext(), "All fields are required except \"other\"", Toast.LENGTH_SHORT).show();
                    }
                }

                //validation of edittext fields
                if ((bikeMake.getText().toString().trim().length() == 0) || (bikeModel.getText().toString().trim().length() == 0) ||  (bikeColor.getText().toString().trim().length() == 0)||  (bikeSize.getText().toString().trim().length() == 0)) {
                    Toast.makeText(getActivity().getApplicationContext(), "\"All fields are required except \"other\"", Toast.LENGTH_SHORT).show();
                }
                else{

                    BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, lastSeen, latitude, longitud, emailFull);
                    mDatabase.setValue(newBike);

                    if (stolen) {

                        if ((lastSeen != null && !lastSeen.isEmpty() && !lastSeen.equals("N/A"))) {

                            if (geoCodeClicked) {
                                //add current bike to stolen DB use same key value.
                                stolenBikesDatabse.child(dB_KeyRefrence_fromBundle).setValue(newBike);
                                //user feedback
                                Toast.makeText(getActivity().getApplicationContext(), "Added to stolen DB", Toast.LENGTH_SHORT).show();
                                successfullEdit = true;
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "retrieve geo code first", Toast.LENGTH_SHORT).show();
                                successfullEdit = false;
                            }
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Last seen can not be blank if stolen", Toast.LENGTH_SHORT).show();
                            successfullEdit = false;

                        }

                    }// end if(stolen)

                    //bike stolen checkbox is false
                    if (!stolen) {
                        //make sure its in stolen DB to Display collect message
                        if (inStolenDB) {
                            // remove from DB
                            stolenBikesDatabse.child(dB_KeyRefrence_fromBundle).removeValue();
                            //user output
                            Toast.makeText(getActivity().getApplicationContext(), "Removed from stolen DB", Toast.LENGTH_SHORT).show();
                            //reset check
                            inStolenDB = true;
                        }
                        //other wise different output to user
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), "Bike updated", Toast.LENGTH_SHORT).show();
                            successfullEdit = true;
                        }
                    }

                    //if the edit has bee successfull return user to main screen
                    if (successfullEdit) {
                        FragmentManager fm = getFragmentManager();
                        fm.beginTransaction().replace(R.id.fragment_container, new WelcomeFragment()).commit();
                    }
                }
            }//on click
        });

        //floating action button listener for the geocoding - gets lat and long co-ordinates
        geoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GeocodeAsyncTask().execute();

            }
        });

        //button to delete bike listeer
        floatingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Are you sure you wish to delete?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });

        mDatabase.addValueEventListener(bikeListener);

        return rootView;
    }// end oncreate



    //===================================================================================
    //     AsyncTask for getting geocoiding from user input
    //===================================================================================
    class GeocodeAsyncTask extends AsyncTask<Void, Void, Address> {

        String errorMessage = "";

        @Override
        protected void onPreExecute() {
            name = bikeLastSeen.getText().toString();
        }

        @Override
        protected Address doInBackground(Void... none) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(name, 1);
            } catch (IOException e) {
                errorMessage = "Service not available";
                Log.e(TAG, errorMessage, e);
            }
            if (addresses != null && addresses.size() > 0)
                return addresses.get(0);
            return null;
        }

        protected void onPostExecute(Address address) {
            if (address == null) {
                Log.v("EditFragment", "No address Found");
            } else {
                //assigning class variables
                latitude = address.getLatitude();
                longitud = address.getLongitude();

                Toast.makeText(getActivity().getApplicationContext(), "Co-ordinates added to theft hotspot map", Toast.LENGTH_SHORT).show();

                //note taht button has been clicked to get co-ordonates
                geoCodeClicked = true;
            }
        }
    }//end async


    //===================================================================================
    //     extract bitmap helper, this sets image view
    //===================================================================================
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
    }

    //===================================================================================
    //     Do this on result of activity , after we get the pic.
    //===================================================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //depeing on request code result will carry out diffrent result as two methods i calss request a result
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

    //===================================================================================
    //    helper method to covert bitmap image into base64 for storage in Firebase DB
    //===================================================================================
    private String imageConvertBase64(Bitmap pic) {
        Bitmap image = pic;//your image
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bYtE);

        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return imageFile;
    }


    //===================================================================================
    //   method to launch camera
    //===================================================================================
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check a app can handel this
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    //===================================================================================
    //  method to launch gallery
    //===================================================================================
    public void dispatchGrabImageFromGalleryItent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), SELECT_PICTURE);
    }



}//end class
