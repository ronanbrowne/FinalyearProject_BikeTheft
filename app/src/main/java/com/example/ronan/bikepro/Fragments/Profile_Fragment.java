package com.example.ronan.bikepro.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronan.bikepro.Activities.MainActivity;
import com.example.ronan.bikepro.DataModel.UserData;
import com.example.ronan.bikepro.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private LinearLayout containerCircle;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    // storage
    FirebaseStorage storage = FirebaseStorage.getInstance();

    private EditText usernameET;
    private EditText emailET;
    private EditText addressET;
    private ImageView profielPic;

    private ImageView facePic;
    private ImageView emailPic;
    private ImageView placePic;


    TextView profileHeading;
    FloatingActionButton update;
    FloatingActionButton picUpdate;

    String base64 = "imageValue";
    String imageValue = "";
    Bitmap bitmap;

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
            if (!Strings.isNullOrEmpty(user.getUsername())) {
                profileHeading.setText(user.getUsername());
            } else {
                profileHeading.setText(email);
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

        loadProfileImage(uniqueIdentifier);


        usernameET = (EditText) rootView.findViewById(R.id.username);
        emailET = (EditText) rootView.findViewById(R.id.email);
        addressET = (EditText) rootView.findViewById(R.id.address);
        profileHeading = (TextView) rootView.findViewById(R.id.userProfile);
        update = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmEditProfile);
        picUpdate = (FloatingActionButton) rootView.findViewById(R.id.updatePic);
        profielPic = (ImageView) rootView.findViewById(R.id.profile_image);
        placePic = (ImageView) rootView.findViewById(R.id.placeImage);
        facePic = (ImageView) rootView.findViewById(R.id.faceImage);
        emailPic = (ImageView) rootView.findViewById(R.id.emailImage);
        containerCircle = (LinearLayout) rootView.findViewById(R.id.viewA);
        checkTheme();

        // containerCircle.containerCircle
        //   containerCircle.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mDatabase.child(uniqueIdentifier).addValueEventListener(userDataListener);

        picUpdate.setVisibility(View.VISIBLE);

        emailET.setText(email);
        // profileHeading.setText(uniqueIdentifier);

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


                mDatabase = FirebaseDatabase.getInstance().getReference().child("User Profile Data");
                long date = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM MM dd, yyyy h:mm a");
                String dateString = sdf.format(date);


                UserData userData = new UserData(address, username, base64, dateString, email);

                MainActivity activity = (MainActivity) getActivity();
                activity.loadProfileImage(uniqueIdentifier);


                mDatabase.child(uniqueIdentifier).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity().getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();

                    }
                });


                mDatabase.child(uniqueIdentifier).addValueEventListener(userDataListener);


            }
        });

        profielPic.setLayerType(View.LAYER_TYPE_NONE, null);

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
                //  profielPic.setImageBitmap(bitmap);


                // Create storage reference
                StorageReference storageRef = storage.getReferenceFromUrl("gs://findmybike-1a1af.appspot.com/Profilers/");

                // Create a reference to user picture
                StorageReference imageRef = storageRef.child(uniqueIdentifier);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] dataBitmap = baos.toByteArray();

                UploadTask uploadTask = imageRef.putBytes(dataBitmap);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Log.i("**", "uploaded from galery");
                        loadProfileImage(uniqueIdentifier);

                    }
                });


            } else if (resultCode == RESULT_CANCELED) {
                Log.i("message", "the user cancelled the request");
            }
        }
        //if gallery
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //profielPic.setImageBitmap(imageBitmap);

                //  saveToInternalStorage(imageBitmap);

                // Create storage reference
                StorageReference storageRef = storage.getReferenceFromUrl("gs://findmybike-1a1af.appspot.com/Profilers/");

                // Create a reference to user picture
                StorageReference imageRef = storageRef.child(uniqueIdentifier);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] dataBitmap = baos.toByteArray();

                UploadTask uploadTask = imageRef.putBytes(dataBitmap);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        //  Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.i("**", "uploaded from cam");
                        loadProfileImage(uniqueIdentifier);

                    }
                });


            } else if (resultCode == RESULT_CANCELED) {
                Log.i("message", "the user cancelled the request");
            }
        }
    }


    //fill users image to selected view
    public String loadProfileImage(final String userToLoad) {

        // Create storage reference
        final StorageReference storageRef = storage.getReferenceFromUrl("gs://findmybike-1a1af.appspot.com/Profilers/");

        //set image based on user id
        StorageReference myProfilePic = storageRef.child(userToLoad);

        //set max image download size
        final long ONE_MEGABYTE = 10000 * 10000;
        myProfilePic.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                //decode image
                Bitmap userImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                profielPic.setImageBitmap(userImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //reset to default image if no image is selected
                StorageReference myProfilePic = storageRef.child("default.jpg");
                myProfilePic.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //decode image
                        Bitmap userImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        profielPic.setImageBitmap(userImage);
                    }
                });


            }
        });

        return null;
    }


    public void checkTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String themePref = preferences.getString("list_preference", "");

        if (themePref.equals("AppTheme"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            placePic.setImageResource(R.drawable.ic_add_location_white_24dp);
            facePic.setImageResource(R.drawable.ic_mail_outline_white_48dp);
            emailPic.setImageResource(R.drawable.ic_face_white_48dp);

        }    }

}// end class
