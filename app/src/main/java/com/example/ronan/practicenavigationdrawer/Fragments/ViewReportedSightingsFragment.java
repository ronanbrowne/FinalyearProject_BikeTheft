package com.example.ronan.practicenavigationdrawer.Fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronan.practicenavigationdrawer.Activities.MainActivity;
import com.example.ronan.practicenavigationdrawer.DataModel.BikeData;
import com.example.ronan.practicenavigationdrawer.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.games.request.GameRequestEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;

import static com.example.ronan.practicenavigationdrawer.R.layout.fragment_view_reported_sightings;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewReportedSightingsFragment extends Fragment  {

    private FirebaseUser mFirebaseUser;
    private DatabaseReference usersSightings;
    private String email;
    private ImageView bike_image;
    private ImageView info;


    public ViewReportedSightingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }

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
                        .setText("long press a bike to interact with sighting")
                        .setTextColor(ContextCompat.getColor(getContext(),R.color.white))
                        .setDismissOnClick(true)
                        .setCancelable(true)
                        .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.cyan)).show();


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
        myListView.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        myListView.setDividerHeight(2);

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




}
