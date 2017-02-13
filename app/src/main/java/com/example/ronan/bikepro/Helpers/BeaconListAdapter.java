package com.example.ronan.bikepro.Helpers;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ronan.bikepro.DataModel.BikeData;
import com.example.ronan.bikepro.R;
import com.example.ronan.bikepro.Scan_For_Stolen;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Displays basic information about beacon.
 * http://www.programcreek.com/java-api-examples/index.php?source_dir=crowdalert-hackdelhi-master/crowdalert-androidclient-main/app/src/main/java/com/roalts/hackdelhiclient/BeaconListAdapter.java
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class BeaconListAdapter extends BaseAdapter {

    private ArrayList<BikeData> bikes;
    private LayoutInflater inflater;
    Context myContext;

    public BeaconListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.bikes = new ArrayList<>();
        this.myContext = context;
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
        holder.ownerTextView.setText(bikeData.getRegisteredBy());
        holder.lastLocationTextView.setText(bikeData.getLastSeen());
        holder.bikePic.setImageBitmap(getBitMapFromString(bikeData.getImageBase64()));

        DecimalFormat df = new DecimalFormat("#.00");
        String RangeFormated = df.format(bikeData.getBeaconAccuracy());
        double proxDecimal = Double.parseDouble(df.format(bikeData.getBeaconAccuracy()));
        holder.aproxDistance.setText(RangeFormated);

        // Set the proper background color on the proximity circle.
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable proximityCircle = (GradientDrawable) holder.aproxDistance.getBackground();
        // Get the appropriate background color based on the current earthquake proximity
        int proximittColour = getproximityColor(proxDecimal);
        // Set the color on the proximity circle
        proximityCircle.setColor(proximittColour);



    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_ranging, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    static class ViewHolder {
        final TextView makeView;
        final TextView modelTextView;
        final TextView colourTextView;
        final TextView ownerTextView;
        final TextView lastLocationTextView;
        final TextView aproxDistance;
        final ImageView bikePic;


        ViewHolder(View view) {
            // makeView = (TextView) view.findViewById(ma)
            makeView = (TextView) view.findViewById(R.id.make_ranging);
            modelTextView = (TextView) view.findViewById(R.id.model_ranging);
            colourTextView = (TextView) view.findViewById(R.id.color_ranging);
            ownerTextView = (TextView) view.findViewById(R.id.owner_ranging);
            lastLocationTextView = (TextView) view.findViewById(R.id.loaction_ranging);
            aproxDistance = (TextView) view.findViewById(R.id.Distance_ranging);
            bikePic = (ImageView) view.findViewById(R.id.bike_image_ranging);


        }
    }

    //===============================================
    // extract bitmap helper, this sets image view
    //===============================================
    public Bitmap getBitMapFromString(String imageAsString) {
        Bitmap b = null;
        if (imageAsString == "No image" || imageAsString == null) {
            Log.v("***", "No image Found");
        } else {
            byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
            b = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }

        return b;
    }// end getBitMapFromString


    //change background based on location
    private int getproximityColor(double proximity) {
        int proximityColorResourceId;
        int proximityFloor = (int) Math.floor(proximity);
        switch (proximityFloor) {
            case 0:
                proximityColorResourceId = R.color.proximity10plus;
                break;
            case 9:
                proximityColorResourceId = R.color.proximity1;
                break;
            case 8:
                proximityColorResourceId = R.color.proximity2;
                break;
            case 7:
                proximityColorResourceId = R.color.proximity2;
                break;
            case 6:
                proximityColorResourceId = R.color.proximity3;
                break;
            case 5:
                proximityColorResourceId = R.color.proximity3;
                break;
            case 4:
                proximityColorResourceId = R.color.proximity4;
                break;
            case 3:
                proximityColorResourceId = R.color.proximity5;
                break;
            case 2:
                proximityColorResourceId = R.color.proximity7;
                break;
            case 1:
                proximityColorResourceId = R.color.proximity9;
                break;
            default:
                proximityColorResourceId = R.color.proximity10plus;
                break;
        }

        return ContextCompat.getColor(myContext, proximityColorResourceId);
    }


}

