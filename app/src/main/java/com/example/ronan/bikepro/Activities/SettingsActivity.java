package com.example.ronan.bikepro.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import com.codevscolor.materialpreference.activity.MaterialPreferenceActivity;
import com.codevscolor.materialpreference.callback.MaterialPreferenceCallback;
import com.codevscolor.materialpreference.util.MaterialPrefUtil;
/**
 * Created by ronan.browne on 14/03/2017.
 */

public class SettingsActivity extends MaterialPreferenceActivity implements MaterialPreferenceCallback  {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        //register this class as listener for preference change
        setPreferenceChangedListener(this);

        //use dark theme or not . Default is light theme
        useDarkTheme(false);

        //set toolbar title
        setToolbarTitle("My Settings");

        //set primary color
        setPrimaryColor(MaterialPrefUtil.COLOR_BLUE);

        //default secondary color for tinting widgets, if no secondary color is used yet
        setDefaultSecondaryColor(this, MaterialPrefUtil.COLOR_BLUE);



        //set application package name and xml resource name of preference
        setAppPackageName("com.example.ronan.bikepro");
        setXmlResourceName("settingspreference");

        //optional
        //if you are using color picker, set the key used in the xml preference
        setColorPickerKey("secondary_color_position");

    }


    /**
     * callback for preference changes
     *
     * @param sharedPreferences
     * @param name
     */
    @Override
    public void onPreferenceSettingsChanged(SharedPreferences sharedPreferences, String name) {


        String themePref = sharedPreferences.getString(
                "list_preference",
                "");


        if (themePref.equals("AppThemeSecondary")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Intent i=new Intent(SettingsActivity.this,MainActivity.class);
            startActivity(i);}

        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Intent i=new Intent(SettingsActivity.this,MainActivity.class);
            startActivity(i);
        }


        Toast.makeText(this, "preference with key " + themePref + " changed", Toast.LENGTH_LONG).show();


    }
}
