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
import static com.example.ronan.practicenavigationdrawer.MainFragment.REQUEST_IMAGE_CAPTURE;


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
    private CheckBox bikeStolen;
    private FloatingActionButton imageUpload;
    private FloatingActionButton comfirmEdit;
    private FloatingActionButton geoCode;
    private FloatingActionButton floatingDelete;
    private ImageView upload_image;
    private boolean geoCodeClicked;

    String base64 = "No image";
    String email = "";

    String key_passed_fromList;
    double latitude = 0;
    double longitud = 0;
    TextView infoText;
    String name;

    Bitmap bitmap;


    private static final int SELECT_PICTURE = 0;


    //Firebase variables
    private DatabaseReference mDatabase;
    private DatabaseReference stolenBikesDatabse;
    private boolean inStolenDB = false;
    private FirebaseUser mFirebaseUser;

    //holds all DB keys for bikes listed as stolen
    List<String> stolenKeysList;

    //dialog listener for pop up to confirm delete
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    stolenBikesDatabse.child(key_passed_fromList).removeValue();
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

    //event listener for checking if bike is on stolen DB used to give correct user feedback
    ValueEventListener ifStolen = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            stolenKeysList = new ArrayList<>();
            //adding keys to array list for later oomparison
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                stolenKeysList.add(snapshot.getKey());
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("*", "Error on ifStolen ValueEventListener: " + databaseError.toString());

        }
    }; //end listener

    //declaring ValueEvent Listener to poulate UI fields from DB
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.getValue(BikeData.class) == null) {

                Log.v(TAG, "doing nothing snapshot null");
                return;

            }

            //grab snapshot and put in bike Object
            mybike = dataSnapshot.getValue(BikeData.class);

            //set UI fields from data
            bikeMake.setText(mybike.getMake());
            bikeModel.setText(mybike.getModel());
            bikeColor.setText(mybike.getColor());
            bikeSize.setText(String.valueOf(mybike.getFrameSize()));
            bikeOther.setText(mybike.getOther());
            bikeLastSeen.setText(mybike.getLastSeen());
            base64 = mybike.getImageBase64();
            getBitMapFromString(base64);

            //handel chackbox
            if (mybike.isStolen()) {
                bikeStolen.setChecked(true);
                Log.v(TAG, "if**" + bikeStolen.toString());
            } else {
                bikeStolen.setChecked(false);
                Log.v(TAG, "else**" + bikeStolen.toString());
            }

        }


        BikeData mybike = new BikeData("test make", 22, "red", "other", true, "dfsffdss", "Model", "last seen", latitude, longitud);

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    public EditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        geoCodeClicked = false;
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        //now get anywhere(Fragment, activity, class)
        //http://stackoverflow.com/questions/27484245/pass-data-between-two-fragments-without-using-activity
        key_passed_fromList = DataHolderClass.getInstance().getDistributor_id();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }


        //get UI id's
        infoText = (TextView) rootView.findViewById(R.id.infoText);
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
        // update = (Button) rootView.findViewById(R.id.button);
        // Inflate the layout for this fragment

        //listener for checkbox, reveal extra stolen options if stolen
        bikeStolen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (bikeStolen.isChecked()) {
                    bikeLastSeen.setVisibility(View.VISIBLE);
                    geoCode.setVisibility(View.VISIBLE);
                } else {
                    bikeLastSeen.setVisibility(View.INVISIBLE);
                    geoCode.setVisibility(View.INVISIBLE);

                }
            }
        });

        //upload image
        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Use picture from Gallery or launch camera?").setPositiveButton("Gallery", getDialogClickListenerImage)
                        .setNegativeButton("Camera", dialogClickListener).show();

            }
        });


        //seting up firebase DB refrences
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(email).child(key_passed_fromList);
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");

        mDatabase.addValueEventListener(bikeListener);
        stolenBikesDatabse.addValueEventListener(ifStolen);
        //update buton
        comfirmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stolenBikesDatabse.addValueEventListener(ifStolen);

                //grab data from fields
                boolean stolen = bikeStolen.isChecked();
                String make = bikeMake.getText().toString();
                String model = bikeModel.getText().toString();
                int frameSize = Integer.parseInt(bikeSize.getText().toString());
                String color = bikeColor.getText().toString();
                String other = bikeOther.getText().toString();
                String lastSeen = bikeLastSeen.getText().toString();


                BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model, lastSeen, latitude, longitud);
                mDatabase.setValue(newBike);


                //looping through arraylist of DB keys for all stolen bikes
                for (String temp : stolenKeysList) {
                    //if the currect keq is also in stolen db mark as true
                    if (key_passed_fromList.equals(temp)) {
                        inStolenDB = true;
                    } else {
                        inStolenDB = false;
                    }
                }//end for

                if (stolen) {

                    if ((lastSeen != null && !lastSeen.isEmpty() && !lastSeen.equals("N/A"))) {

                        if (geoCodeClicked) {
                            //add current bike to stolen DB use same key value.
                            stolenBikesDatabse.child(key_passed_fromList).setValue(newBike);
                            //user feedback
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Added to stolen DB", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "retrieve geo code first", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Last seen can not be blank if stolen", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }

                //bike stolen checkbox is false
                if (!stolen) {
                    //make sure its in stolen DB to Display collect message
                    if (inStolenDB) {
                        // remove from DB
                        stolenBikesDatabse.child(key_passed_fromList).removeValue();

                        //user output
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Removed from stolen DB", Toast.LENGTH_SHORT);
                        toast.show();
                        //reset check
                        inStolenDB = false;
                    }
                    //other wise diffrent output to user
                    else {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Bike updated", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }//on click
        });

        //floating action button for the geocoding - gets lat and long co-ordinates
        geoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GeocodeAsyncTask().execute();

            }
        });

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


    //AsyncTask for getting geocoiding from user input
    class GeocodeAsyncTask extends AsyncTask<Void, Void, Address> {

        String errorMessage = "";

        @Override
        protected void onPreExecute() {
            infoText.setVisibility(View.INVISIBLE);
            name = bikeLastSeen.getText().toString();
            // latitude = Double.parseDouble(latitudeEdit.getText().toString());
            // longitude = Double.parseDouble(longitudeEdit.getText().toString());
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
                infoText.setVisibility(View.VISIBLE);
                infoText.setText("No addrress");
            } else {
                String addressName = "";
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressName += " --- " + address.getAddressLine(i);
                }
                //assigning class variables
                latitude = address.getLatitude();
                longitud = address.getLongitude();

                infoText.setVisibility(View.VISIBLE);
                infoText.setText("Latitude: " + address.getLatitude() + "\n" +
                        "Longitude: " + address.getLongitude() + "\n" +
                        "Address: " + addressName);


                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Co-ordinates added to theft hotspot map", Toast.LENGTH_SHORT);
                toast.show();
                geoCodeClicked = true;
            }
        }
    }//end async


    //extract bitmap helper, this sets image view
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

    //Do this on result of activity , after we get the pic.
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

}//end class
