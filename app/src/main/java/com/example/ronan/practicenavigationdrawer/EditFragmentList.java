package com.example.ronan.practicenavigationdrawer;


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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragmentList extends Fragment {

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseStolen;
    private String email;
    ImageView bike_image;


    public EditFragmentList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser!=null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_list, container, false);

        final ListView myListView = (ListView) rootView.findViewById(R.id.list);

        //set the divider
        myListView.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        myListView.setDividerHeight(1);

        //Firebase DB setup
        mDatabaseStolen = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(email);

        // here we set content of list items
        final FirebaseListAdapter<BikeData> bikeAdapter = new FirebaseListAdapter<BikeData>
                (getActivity(), BikeData.class, R.layout.list_item, mDatabaseStolen) {
            @Override
            protected void populateView(View v, BikeData model, int position) {

                // Find the TextView IDs of list_item.xml
                TextView makeView = (TextView) v.findViewById(R.id.make);
                TextView sizeView = (TextView) v.findViewById(R.id.size);
                TextView colorView = (TextView) v.findViewById(R.id.color);
                TextView otherView = (TextView) v.findViewById(R.id.other);
                bike_image = (ImageView) v.findViewById(R.id.bike_image);
                TextView lastlocationView = (TextView) v.findViewById(R.id.loaction);

                //setting the textViews to Bike data
                makeView.setText(model.getMake());
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


        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {



                DatabaseReference itemRef = bikeAdapter.getRef(i);

                //now set from anywhere(Fragment, activity, class) at any event before you move to new screen
                //http://stackoverflow.com/questions/27484245/pass-data-between-two-fragments-without-using-activity
                DataHolderClass.getInstance().setDistributor_id(itemRef.getKey());

                Toast toast = Toast.makeText(getActivity().getApplicationContext(), itemRef.getKey(), Toast.LENGTH_SHORT);
                toast.show();

                EditFragment editFragment = new EditFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, editFragment);
                fragmentTransaction.commit();


            }
        });

        return rootView;
    }

    //extract bitmap helper, this sets image view
    public void getBitMapFromString(String imageAsString) {
        if (imageAsString == "No image" || imageAsString == null) {
            // bike_image.setImageResource(R.drawable.not_uploaded);
            Log.v("***","No image Found");
        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bike_image.setImageBitmap(bitmap);
        }
    }

}
