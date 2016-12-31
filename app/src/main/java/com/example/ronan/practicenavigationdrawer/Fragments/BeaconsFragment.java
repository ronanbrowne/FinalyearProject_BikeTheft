package com.example.ronan.practicenavigationdrawer.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.example.ronan.practicenavigationdrawer.R;
import com.tooltip.Tooltip;


public class BeaconsFragment extends Fragment {


    private ImageView info;

    public BeaconsFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacons, container, false);

        info = (ImageView) rootView.findViewById(R.id.infobeacon);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Tooltip tooltip = new Tooltip.Builder(info)
                        .setText("How it works:\n\n Your phone 'talks' to the sensor placed on your bike\n\n" +
                                "Should there be a break in the communication for any reason you will be alerted.")
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


            }
        });

        return rootView;
    }







}
