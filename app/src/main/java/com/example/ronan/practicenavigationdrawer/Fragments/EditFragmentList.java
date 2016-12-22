package com.example.ronan.practicenavigationdrawer.Fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ronan.practicenavigationdrawer.DataModel.BikeData;
import com.example.ronan.practicenavigationdrawer.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragmentList extends Fragment {

    private FirebaseUser mFirebaseUser;
    private DatabaseReference usersBikesDatabase;
    private String uniqueIdentifier;
    private ImageView bike_image;
    private TextView noData;
    private View loadingIndicator;

    public EditFragmentList() {
        // Required empty public constructor
    }

    //===========================================================================================================
    //  Firebase listeer to handel displaying either a loading bar or empty message depending in state of DB
    //===========================================================================================================
    ValueEventListener checkList = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                noData.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //===========================================================================================================
    //  onCreateView
    //===========================================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get current user. im using users email to uniquely ID JSON nodes in DB.
        // Firebase cant accept special characters in node reference
        // i get around this by splitting the email address and dropping everything after @ symbol. whats remaining is used as a ID in DB
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            uniqueIdentifier = mFirebaseUser.getEmail();
            uniqueIdentifier = uniqueIdentifier.split("@")[0];
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_list, container, false);


        //  get IDs
        final ListView myListView = (ListView) rootView.findViewById(R.id.list);
        loadingIndicator = rootView.findViewById(R.id.loading_indicator_edit);
        noData = (TextView) rootView.findViewById(R.id.empty_view_Notification);

        //set the divider
        myListView.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        myListView.setDividerHeight(1);

        //Firebase DB setup
        usersBikesDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(uniqueIdentifier);
        usersBikesDatabase.addValueEventListener(checkList);


        //=====================================================================================================================
        //  FireBase ListAdapter custom class provided by FireBase to allow implementation of custom list adapter on DB data
        //===================================================================================================================
        final FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item, usersBikesDatabase) {
            @Override
            protected void populateView(View v, BikeData model, int position) {
                //handling displaying of loading bar once data is recieved hide it.
                usersBikesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
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

                //setting the textViews to Bike data
                makeView.setText(model.getMake());
                modelView.setText(model.getModel());
                sizeView.setText(String.valueOf(model.getFrameSize()));
                colorView.setText(model.getColor());
                otherView.setText(model.getOther());
                lastlocationView.setText(model.getLastSeen());

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

                //get the RD refrence to the Item clicked and store this in data holder class
                DatabaseReference itemRef = bikeAdapter.getRef(i);

                //use bundle to pass itemRef to next fragment
                //pass with setArguments(bundle)
                Bundle bundle = new Bundle();
                bundle.putString("dB_Ref", itemRef.getKey());

                //open the edit bike fragment. where the bike a user clicked on here will be edited.
                //we will use the above itemRef value stored in the bundle  to load the chosen bike info on editFragment
                EditFragment editFragment = new EditFragment();
                editFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, editFragment);
                fragmentTransaction.commit();
            }
        });

        return rootView;

    }//end onCreate View

    //===============================================
    // extract bitmap helper, this sets image view
    //===============================================
    public void getBitMapFromString(String imageAsString) {
        if (imageAsString == "No image" || imageAsString == null) {
            Log.v("***", "No image Found");
        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bike_image.setImageBitmap(bitmap);
        }
    }// end getBitMapFromString

}//end class
