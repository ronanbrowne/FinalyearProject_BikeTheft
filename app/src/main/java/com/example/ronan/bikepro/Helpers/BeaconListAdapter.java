package com.example.ronan.bikepro.Helpers;

//import android.content.Context;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.estimote.sdk.Utils;
//import com.example.ronan.bikepro.DataModel.BikeData;
//import com.example.ronan.bikepro.R;
//
//import java.util.ArrayList;
//
//public class CustomAdapter extends ArrayAdapter<BikeData> implements View.OnClickListener{
//
//    private ArrayList<BikeData> dataSet;
//    Context mContext;
//
//    // View lookup cache
//    private static class ViewHolder {
//        TextView txtName;
//        TextView txtType;
//        TextView txtVersion;
//        ImageView info;
//
//        TextView makeView;
//        TextView modelView ;
//        TextView sizeView ;
//        TextView colorView ;
//        TextView otherView ;
//        ImageView bike_image ;
//        TextView lastlocationView ;
//    }
//
//    public CustomAdapter(ArrayList<BikeData> data, Context context) {
//        super(context, R.layout.list_item, data);
//        this.dataSet = data;
//        this.mContext=context;
//
//    }
//
//    @Override
//    public void onClick(View v) {
////
////        int position=(Integer) v.getTag();
////        Object object= getItem(position);
////        BikeData dataModel=(BikeData) object;
////
////        switch (v.getId())
////        {
////            case R.id.item_info:
////                Snackbar.make(v, "Release date " +dataModel.getFeature(), Snackbar.LENGTH_LONG)
////                        .setAction("No action", null).show();
////                break;
////        }
//    }
//
//    private int lastPosition = -1;
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // Get the data item for this position
//        BikeData dataModel = getItem(position);
//        // Check if an existing view is being reused, otherwise inflate the view
//        ViewHolder viewHolder; // view lookup cache stored in tag
//
//        final View result;
//
//        if (convertView == null) {
//
//
//            TextView makeView;
//            TextView modelView ;
//            TextView sizeView ;
//            TextView colorView ;
//            TextView otherView ;
//            ImageView bike_image ;
//            TextView lastlocationView ;
//
//            viewHolder = new ViewHolder();
//            LayoutInflater inflater = LayoutInflater.from(getContext());
//            convertView = inflater.inflate(R.layout.list_item, parent, false);
//            viewHolder.makeView = (TextView) convertView.findViewById(R.id.make);
//            viewHolder.modelView = (TextView) convertView.findViewById(R.id.model);
//            viewHolder.colorView = (TextView) convertView.findViewById(R.id.color);
//            //   viewHolder.txtVersion = (TextView) convertView.findViewById(R.id.version_number);
//            //  viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);
//
//            result=convertView;
//
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//            result=convertView;
//        }
//
//        //  Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        //result.startAnimation(animation);
//        lastPosition = position;
//
//        viewHolder.makeView.setText(dataModel.getMake ());
//        viewHolder.modelView.setText(dataModel.getModel());
//        viewHolder.colorView.setText(dataModel.getColor());
//        //  viewHolder.info.setOnClickListener(this);
//        //viewHolder.info.setTag(position);
//        // Return the completed view to render on screen
//        return convertView;
//    }
//}

//*****

import android.content.Context;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.TextView;

        import com.estimote.sdk.Beacon;
        import com.estimote.sdk.Utils;
import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;

import java.util.ArrayList;
        import java.util.Collection;


/**
 * Displays basic information about beacon.
 *
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

