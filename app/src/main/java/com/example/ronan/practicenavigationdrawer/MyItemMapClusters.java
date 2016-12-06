package com.example.ronan.practicenavigationdrawer;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Ronan on 02/12/2016.
 */

public class MyItemMapClusters implements ClusterItem {

    private final LatLng mPosition;

    public MyItemMapClusters(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
