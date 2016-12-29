package com.example.ronan.practicenavigationdrawer.Activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronan.practicenavigationdrawer.DataModel.BikeData;
import com.example.ronan.practicenavigationdrawer.DataModel.UserData;
import com.example.ronan.practicenavigationdrawer.Fragments.DatabaseFragment;
import com.example.ronan.practicenavigationdrawer.Fragments.EditFragmentList;
import com.example.ronan.practicenavigationdrawer.Fragments.GmapFragment;
import com.example.ronan.practicenavigationdrawer.Fragments.Profile_Fragment;
import com.example.ronan.practicenavigationdrawer.Fragments.RegisterFragment;
import com.example.ronan.practicenavigationdrawer.Fragments.ViewReportedSightingsFragment;
import com.example.ronan.practicenavigationdrawer.Fragments.WelcomeFragment;
import com.example.ronan.practicenavigationdrawer.R;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Toolbar toolbar = null;


    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference userDataBase;
    private DatabaseReference stolenBikesDatabse;
    private DatabaseReference usersBikesDatabase;
    private DatabaseReference userSightings;


    private String mUsername;
    private String mPhotoUrl;
    private String mEmail;
    private TextView emailNavBar;
    private TextView userNameNavBar;
    private MenuItem menuItem;
    View rootView;
    View cv;
    GmapFragment fragment;
    ArrayList<BikeData> usersStolenBikes = new ArrayList<>();
    ArrayList<String> keysForStolenBikes = new ArrayList<>();

    private boolean mapOpen = false;
private long sightingsCount;

    //===================================================================================
    // Firebase event listener for counting "Mail"
    //===================================================================================
    ValueEventListener countMail = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            sightingsCount = dataSnapshot.getChildrenCount();
            Log.v("*", "sight: " + sightingsCount);
            Toast.makeText(MainActivity.this, "(testing, delete later) mail Box:   "+sightingsCount, Toast.LENGTH_SHORT).show();

            if (menuItem!=null) {
                menuItem.setIcon(buildCounterDrawable((int) sightingsCount, R.drawable.ic_mail_outline_white_24dp));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("*", "Error on ifStolen ValueEventListener: " + databaseError.toString());

        }
    }; //end listener


    //dialog listener for pop up to confirm delete all bike data registered to a user
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    usersBikesDatabase.removeValue();

                    for (String temp : keysForStolenBikes) {
                        stolenBikesDatabse.child(temp).removeValue();
                    }

                    //Jump back to welcome fragment after clear data to allow user see there system summary
                    WelcomeFragment welcomeFragment = new WelcomeFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, welcomeFragment);
                    fragmentTransaction.commit();
                    //feedback

                    Toast.makeText(MainActivity.this, "All associated bike data deleted", Toast.LENGTH_SHORT).show();


                    break;

                case DialogInterface.BUTTON_NEGATIVE:

                    //feedback
                    Toast.makeText(MainActivity.this, "Delete canceled", Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };

    //Listener called to delete all users bike data associated with a AC
    //used when a user clicks clear data from toolbar menu
    ValueEventListener deleteBikeData = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            usersStolenBikes.clear();
            if (dataSnapshot.getValue() != null) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BikeData bike = snapshot.getValue(BikeData.class);
                    //check registered by field is not null
                    if (bike.getRegisteredBy() != null) {
                        if (bike.getRegisteredBy().equals(mEmail)) {
                            Log.v("**current user:  ", "" + mEmail);
                            usersStolenBikes.add(bike);
                            keysForStolenBikes.add(snapshot.getKey());
                            Log.v("Stolen bikes ", (Arrays.toString(usersStolenBikes.toArray())));
                            Log.v("Stolen keys* ", (Arrays.toString(keysForStolenBikes.toArray())));

                            Log.v("**registered by: ", bike.getRegisteredBy());
                        } else {
                            Log.v("**reg", "no user");
                        }
                    }
                }

            } else {
                Log.v("MainActivity", "data snapshot is null");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //get Username from DB we use this in navagtion bar
    ValueEventListener fetchUserData = new ValueEventListener() {
        UserData user = new UserData();

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                user = dataSnapshot.getValue(UserData.class);
                if (user.getUsername() != null) {
                    userNameNavBar.setText(user.getUsername());
                } else {
                    userNameNavBar.setText("Set user name ");
                }
            } else {
                Log.v("MainActivity", "data snapshot is null");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //setFragment
        WelcomeFragment mainFragment = new WelcomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        fragmentTransaction.commit();

        mUsername = "";


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("look_here***", "onAuthStateChanged:signed_in:" + user.getUid() + user.getEmail());
                    mEmail = user.getEmail();
                    mUsername = mEmail.split("@")[0];
                    Log.d("look_here***", "onAuthStateChanged:signed_in:" + mEmail);

                } else {
                    // User is signed out
                    //  Not signed in, launch the Sign In activity
                    startActivity(new Intent(MainActivity.this, SignIn.class));
                    finish();
                    //return;
                    Log.d("look_here***", "onAuthStateChanged:signed_out");
                    return;
                }
                // ...
            }
        };

        mFirebaseAuth.addAuthStateListener(mAuthListener);


        String email = "";
        //get current user
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //if its not null grab email address then remove the @ bit , firebase cant take special symbols in node names
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }
        usersBikesDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(email);

        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        stolenBikesDatabse.addValueEventListener(deleteBikeData);

        userDataBase = FirebaseDatabase.getInstance().getReference().child("User Profile Data").child(email);
        userDataBase.addValueEventListener(fetchUserData);



        userSightings= FirebaseDatabase.getInstance().getReference().child("Viewing bikes Reported Stolen").child(email);
        userSightings.addValueEventListener(countMail);
        Log.v("4", String.valueOf(sightingsCount));



        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Do whatever you want here
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView)
                ;
            }
        };
// Set the drawer toggle as the DrawerListener
        drawer.addDrawerListener(toggle);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        //set navagation drawer
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set text in Navagation bar
        View header = navigationView.getHeaderView(0);
        emailNavBar = (TextView) header.findViewById(R.id.email);
        userNameNavBar = (TextView) header.findViewById(R.id.username);

        if (mFirebaseUser == null) {
            emailNavBar.setText("no login");
        } else {
            emailNavBar.setText(mFirebaseUser.getEmail());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    Fragment fragmentr;
    SupportMapFragment mapFragment1;


    int counInt = 5;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);


        Log.v("4", String.valueOf(sightingsCount));



        menuItem = menu.findItem(R.id.testAction);
      //  menuItem.setIcon(R.drawable.ic_mail_outline_white_24dp);

        //  menuItem.setIcon(buildCounterDrawable( counInt,  R.drawable.ic_mail_outline_white_24dp));

        return true;
    }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.badge_layout_messenger_icon, null);
        //view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setTextSize(40);
            textView.setText("" + count);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

//
//                bitmap.setWidth(90);
//                bitmap.setHeight(90);

            return new BitmapDrawable(getResources(), bitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.clearData) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Remove all registered bikes");
            builder.setMessage("Are you sure you wish to clear all bike data associated with this account?\n\n" +
                    "This action is permanent and can not be undone.").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else if (id == R.id.action_sign_out) {
            mFirebaseAuth.signOut();
        } else if (id == R.id.testAction) {
            ViewReportedSightingsFragment reportSightingFragment = new ViewReportedSightingsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, reportSightingFragment);
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fm = getFragmentManager();


        if (id == R.id.nav_welcome) {
            //setFragment
            WelcomeFragment welcomeFragment = new WelcomeFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, welcomeFragment);
            fragmentTransaction.commit();

            mapOpen = false;

        } else if (id == R.id.nav_register) {
            //setFragment
            RegisterFragment mainFragment = new RegisterFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, mainFragment);
            fragmentTransaction.commit();

            mapOpen = false;

        } else if (id == R.id.nav_edit) {

            getFragmentManager().beginTransaction().remove(new GmapFragment()).commit();
            //setFragment
            EditFragmentList editFragment = new EditFragmentList();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, editFragment);
            fragmentTransaction.commit();

            mapOpen = false;

        } else if (id == R.id.nav_db) {
//
//            fm.beginTransaction().detach(new GmapFragment()).commit();
//            getSupportFragmentManager().executePendingTransactions();
//

            //setFragment
            DatabaseFragment databaseFragment = new DatabaseFragment();
            FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
            fts.replace(R.id.fragment_container, databaseFragment);
            fts.commit();

            mapOpen = false;

        }
// else if (id == R.id.nav_profile) {
//            //setFragment
//            Profile_Fragment profileFragment = new Profile_Fragment();
//            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, profileFragment);
//            fragmentTransaction.commit();
//
//            mapOpen=false;
//
//        }
        else if (id == R.id.map_data) {

            mapOpen = true;


            Profile_Fragment profileFragment = new Profile_Fragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, profileFragment);
            fragmentTransaction.commit();
            fm.beginTransaction().replace(R.id.fragment_container, new GmapFragment(), "mapp").commit();


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }





}
