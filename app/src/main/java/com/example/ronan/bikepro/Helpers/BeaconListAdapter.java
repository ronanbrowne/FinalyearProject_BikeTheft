package com.example.ronan.bikepro.Helpers;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Displays basic information about beacon.
 *http://www.programcreek.com/java-api-examples/index.php?source_dir=crowdalert-hackdelhi-master/crowdalert-androidclient-main/app/src/main/java/com/roalts/hackdelhiclient/BeaconListAdapter.java
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class BeaconListAdapter extends BaseAdapter {

    private ArrayList<BikeData> bikes;
    private LayoutInflater inflater;

    public BeaconListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.bikes = new ArrayList<>();
    }

    public void replaceWith(Collection<BikeData> newBikes) {
        this.bikes.clear();
        this.bikes.addAll(newBikes);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return bikes.size();
    }

    @Override
    public BikeData getItem(int position) {
        return bikes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view);
        return view;
    }

    private void bind(BikeData bikeData, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();


        //  Log.d("Value : ", "" + Utils.computeAccuracy(bikeData));
        holder.makeView.setText(bikeData.getMake());
        holder.modelTextView.setText(bikeData.getModel());
        holder.colourTextView.setText(bikeData.getColor());
//                holder.measuredPowerTextView.setText("MPower: " + beacon.getMeasuredPower());
//                holder.rssiTextView.setText("RSSI: " + beacon.getRssi());

    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    static class ViewHolder {
        final TextView makeView;
        final TextView modelTextView;
        final TextView colourTextView;


        ViewHolder(View view) {
            // makeView = (TextView) view.findViewById(ma)
            makeView = (TextView) view.findViewById(R.id.make);
            modelTextView = (TextView) view.findViewById(R.id.model);
            colourTextView = (TextView) view.findViewById(R.id.color);


        }
    }
}

