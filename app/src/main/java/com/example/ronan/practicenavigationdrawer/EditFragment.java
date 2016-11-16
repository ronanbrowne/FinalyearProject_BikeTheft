package com.example.ronan.practicenavigationdrawer;


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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.List;
import java.util.Locale;

import static android.R.attr.bitmap;
import static android.R.attr.data;
import static android.R.attr.name;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {

    //For logcat
    private static final String TAG = "Edit_Bike_Fragment";

    //variables
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
    private ImageView upload_image;
    String base64 = "No image";
    String email="";
    private FirebaseUser mFirebaseUser;
    String key_passed_fromList;
    double latitude = 0;
    double longitud = 0;
    TextView infoText;
    String name;


    private static final int SELECT_PICTURE = 0;



    //Firebase variables
    private DatabaseReference mDatabase;
    private DatabaseReference stolenBikesDatabse;

    //declaring ValueEvent Listener
    ValueEventListener bikeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if(dataSnapshot.getValue(BikeData.class)==null){

                Log.v(TAG, "doing nothing" );
                return;
            }

            mybike = dataSnapshot.getValue(BikeData.class);
//***is null pointer            Log.v(TAG, "object**" + mybike.toString());



            bikeMake.setText(mybike.getMake());
            bikeModel.setText(mybike.getModel());
            bikeLastSeen.setText(mybike.getLastSeen());
            bikeColor.setText(mybike.getColor());
            bikeSize.setText(String.valueOf(mybike.getFrameSize()));
            bikeOther.setText(mybike.getOther());
            base64 = mybike.getImageBase64();
            getBitMapFromString(base64);

           //   bikeStolen.setChecked(mybike.isStolen());



            if (mybike.isStolen()) {
                bikeStolen.setChecked(true);
                Log.v(TAG, "if**" + bikeStolen.toString());
            } else {
                bikeStolen.setChecked(false);
                Log.v(TAG, "else**" + bikeStolen.toString());
            }

        }


        BikeData mybike = new BikeData("test make",22,"red","other",true,"dfsffdss","Model","last seen",latitude,longitud);

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

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        //now get anywhere(Fragment, activity, class)
        //http://stackoverflow.com/questions/27484245/pass-data-between-two-fragments-without-using-activity
        key_passed_fromList = DataHolderClass.getInstance().getDistributor_id();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser!=null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }



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
       // update = (Button) rootView.findViewById(R.id.button);
        // Inflate the layout for this fragment

        bikeStolen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(bikeStolen.isChecked()){
                    bikeLastSeen.setVisibility(View.VISIBLE);
                    geoCode.setVisibility(View.VISIBLE);
                }else{
                    bikeLastSeen.setVisibility(View.INVISIBLE);
                    geoCode.setVisibility(View.INVISIBLE);

                }
            }
        });

        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

            }
        });




        //seting up firebase DB refrences
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(email).child(key_passed_fromList);
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");

        mDatabase.addValueEventListener(bikeListener);

        //update buton
        comfirmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //grab data from fields
                boolean stolen = bikeStolen.isChecked();
                String make = bikeMake.getText().toString();
                String model = bikeModel.getText().toString();
                int frameSize = Integer.parseInt(bikeSize.getText().toString());
                String color = bikeColor.getText().toString();
                String other = bikeOther.getText().toString();
                String lastSeen = bikeLastSeen.getText().toString();



                BikeData newBike = new BikeData(make, frameSize, color, other, stolen, base64, model,lastSeen,latitude,longitud);
                mDatabase.setValue(newBike);

                if (stolen) {



                    //  bikeStolen.setChecked(true);
                    stolenBikesDatabse.child(key_passed_fromList).setValue(newBike);

                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Added to stolen DB", Toast.LENGTH_SHORT);
                    toast.show();
                }

                if (!stolen) {
                    //  bikeStolen.setChecked(false);
                    stolenBikesDatabse.child(key_passed_fromList).removeValue();

                   // stolenBikesDatabse.addValueEventListener(bikeListener);

                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Removed from stolen DB", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });

        geoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GeocodeAsyncTask().execute();
            }
        });

        mDatabase.addValueEventListener(bikeListener);

        return rootView;
    }// end oncreate





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
        protected Address doInBackground(Void ... none) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;


                try {
                    addresses = geocoder.getFromLocationName(name, 1);



                } catch (IOException e) {
                    errorMessage = "Service not available";
                    Log.e(TAG, errorMessage, e);
                }


            if(addresses != null && addresses.size() > 0)
                return addresses.get(0);

            return null;
        }

        protected void onPostExecute(Address address) {
            if(address == null) {

                infoText.setVisibility(View.VISIBLE);
                infoText.setText("No addrress");
            }
            else {
                String addressName = "";
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
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
            }
        }
    }//end async


    //extract bitmap helper, this sets image view
    public void getBitMapFromString(String imageAsString) {

        if(imageAsString!=null){
        if (imageAsString.equals("No image") || imageAsString == null) {
            // bike_image.setImageResource(R.drawable.not_uploaded);
            Log.v("***", "No image Found");

        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            upload_image.setImageBitmap(bitmap);
        }
        }else{
            Log.v("***", "Null paramater passed into getBitMapFromString");
        }
    }

    Bitmap bitmap;

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

            upload_image.setImageBitmap(bitmap);
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
