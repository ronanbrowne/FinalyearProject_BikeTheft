package com.example.ronan.bikepro.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;

import static com.example.ronan.bikepro.R.layout.fragment_view_reported_sightings;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewReportedSightingsFragment extends Fragment {

    private FirebaseUser mFirebaseUser;
    private DatabaseReference usersSightings;
    private DatabaseReference databaseReported;
    private String email;
    private String dB_KeyRefrence;
    private ImageView bike_image;
    private ImageView info;
    private BikeData stolenBike;

    public ViewReportedSightingsFragment() {
        // Required empty public constructor
    }


    //===================================================================================
    //=        dialog listener for pop up to send email to origional user
    //===================================================================================
    DialogInterface.OnClickListener dialogClickListenerForEmail = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    /// TODO: 30/12/2016  chage this to a email address
                    String[] email = {stolenBike.getRegisteredBy()};
                    String subject = "Re: Suspected sighting of my bike: " + stolenBike.getMake();
                    String body = "Hello, \n\n Regarding the potentially sighting of my bike  (" + (stolenBike.getColor() + " " + stolenBike.getMake()) + "). " +
                            "\n\n At the location " + stolenBike.getReportedLocation() + "\n\n" +
                            "Can you please provide me with some futher info so i can look into this." +
                            "\n\n Regards.";


                    composeEmail(email, subject, body);


                    break;

                case DialogInterface.BUTTON_NEGATIVE:

                    //feedback
                    Toast toastCanceled = Toast.makeText(getActivity().getApplicationContext(), " canceled", Toast.LENGTH_SHORT);
                    toastCanceled.show();
                    break;
            }
        }
    };


    //===================================================================================
    //=        dialog listener for pop up to confirm clear from sightings list
    //===================================================================================
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //remove from your list
                    usersSightings.child(dB_KeyRefrence).removeValue();
                    //and the sightings node
                    databaseReported.child(dB_KeyRefrence).removeValue();

                    //feedback
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Sighting cleared successfully", Toast.LENGTH_SHORT);
                    toast.show();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:

                    //feedback
                    Toast toastCanceled = Toast.makeText(getActivity().getApplicationContext(), "Canceled", Toast.LENGTH_SHORT);
                    toastCanceled.show();
                    break;
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }

        databaseReported = FirebaseDatabase.getInstance().getReference().child("Reported Bikes") ;
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(fragment_view_reported_sightings, container, false);

        info = (ImageView) rootView.findViewById(R.id.infoReport);

        final ListView myListView = (ListView) rootView.findViewById(R.id.list_sightings);
        //  get ID of loading bar
        final View loadingIndicator = rootView.findViewById(R.id.loading_indicator_edit);


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Tooltip tooltip = new Tooltip.Builder(info)
                        .setText("Click a sighting to contact the person who filed report.\n\n Long click to clear an item.")
                        .setTextColor(ContextCompat.getColor(getContext(), R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan)).show();


                final Animation animation = new AlphaAnimation((float) 0.5, 0); // Change alpha from fully visible to invisible
                animation.setDuration(500); // duration - half a second
                animation.setInterpolator(new LinearInterpolator()); // do not alter
                // animation
                // rate
                animation.setRepeatCount(1); // Repeat animation
                // infinitely
                animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
                // end so the button will
                // fade back in
                info.startAnimation(animation);

                // Toast.makeText(getActivity().getApplication(), "long press a bike to interact with sighting", Toast.LENGTH_SHORT).show();


            }
        });
        //set the divider
      myListView.setDivider(ContextCompat.getDrawable(getActivity(), R.color.transperent_color));
        myListView.setDividerHeight(40);

        //Firebase DB setup
        usersSightings = FirebaseDatabase.getInstance().getReference().child("Viewing bikes Reported Stolen").child(email);

        //listAdapter
        // here we set content of list items
        final FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item_sightings, usersSightings) {
            @Override
            protected void populateView(View v, BikeData model, int position) {
                //handeling diplaying of loading bar
                usersSightings.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        loadingIndicator.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                // Find the TextView IDs of list_item.xml
                TextView makeView = (TextView) v.findViewById(R.id.make);
                TextView modelView = (TextView) v.findViewById(R.id.model);
                TextView sizeView = (TextView) v.findViewById(R.id.size);
                TextView colorView = (TextView) v.findViewById(R.id.color);
                TextView otherView = (TextView) v.findViewById(R.id.other);
                bike_image = (ImageView) v.findViewById(R.id.bike_image);
                TextView lastlocationView = (TextView) v.findViewById(R.id.loaction);
                TextView reportedlocationView = (TextView) v.findViewById(R.id.sighting);
                TextView reportedByUserView = (TextView) v.findViewById(R.id.reportedBy);
                TextView dateReportedView = (TextView) v.findViewById(R.id.dateReported);
                LinearLayout main = (LinearLayout) v.findViewById(R.id.container);



                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String themePref = preferences.getString("list_preference", "");
                if (themePref.equals("AppThemeSecondary")) {
                    main.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_shadow_night));
                } else {
                    main.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.background_shadow));
                }


                //setting the textViews to Bike data
                makeView.setText(model.getMake());
                modelView.setText(model.getModel());
                sizeView.setText(String.valueOf(model.getFrameSize()));
                colorView.setText(model.getColor());
                otherView.setText(model.getOther());
                lastlocationView.setText(model.getLastSeen());
                reportedlocationView.setText(model.getReportedLocation());
                reportedByUserView.setText(model.getReportedBy());
                dateReportedView.setText(model.getReportedDate());

                //call method to set image, which turns base64 string to image
                getBitMapFromString(model.getImageBase64());

            }
        };

        //set adapter on our listView
        myListView.setAdapter(bikeAdapter);


        //what happens when user clicks on am item
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                stolenBike = bikeAdapter.getItem(i);

                //launch Gmail

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Investigate Further").setMessage("Contact person who reported the suspected sighting for more information.\n\n" +
                        "This will launch your email client").setPositiveButton("Proceed", dialogClickListenerForEmail)
                        .setNegativeButton("Cancel", dialogClickListenerForEmail).show();

            }
        });


        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {

                DatabaseReference itemRef = bikeAdapter.getRef(arg2);
                dB_KeyRefrence = itemRef.getKey();


                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Clear from sightings").setMessage("Are you sure you wish to remove this item?" +
                        "\n\nThis indicates that you have dealt with this suspected sighting.\n\n" +
                        "Once removed it will no longer appear on this list.").setPositiveButton("Proceed", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).show();


                //for testing remove
                // Toast.makeText(getActivity().getApplication(), "Long Clicked Trigger: "+dB_KeyRefrence, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    //extract bitmap helper, this sets image view
    public void getBitMapFromString(String imageAsString) {
        if (imageAsString == "No image" || imageAsString == null) {
            // bike_image.setImageResource(R.drawable.not_uploaded);
            Log.v("***", "No image Found");
        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bike_image.setImageBitmap(bitmap);
        }
    }


    //================================================================================
    //   Method to compose a email called when a user clicks on bike item in listView.
    //   Email generated to send to origional user.
    //=================================================================================
    public void composeEmail(String[] addresses, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        //,ake sure user has a app capable of carrying out this intent
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }//end method

}
