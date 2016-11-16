package com.example.ronan.practicenavigationdrawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Toolbar toolbar = null;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;


    private String mUsername;
    private String mPhotoUrl;
    private String mEmail;
    private TextView emailNavBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setFragment
        MainFragment mainFragment = new MainFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        fragmentTransaction.commit();

        mUsername = "ANONYMOUS1";


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();





        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    Log.d("look_here***", "onAuthStateChanged:signed_in:" + user.getUid()+user.getEmail());
                    mEmail= user.getEmail();
                    mUsername  = mEmail.split("@")[0];
                    Log.d("look_here***", "onAuthStateChanged:signed_in:" + mEmail);

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("User Profile Data");

                    long date = System.currentTimeMillis();

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM MM dd, yyyy h:mm a");
                    String dateString = sdf.format(date);

                    UserData userDaat = new UserData("Enter City",mUsername,"imageValue",dateString,mEmail,"Enter Country");

                    mDatabase.child(mUsername).setValue(userDaat);


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


//        if (mFirebaseUser == null) {
//            // Not signed in, launch the Sign In activity
//            startActivity(new Intent(this, SignIn.class));
//            finish();
//            return;
//        } else {
//            mUsername = mFirebaseUser.getDisplayName();
//
//            //if log in doesnt include pic set string so app does not crash
//            if (mFirebaseUser.getPhotoUrl().toString() == null) {
//                mPhotoUrl = "No photo associated with this account";
//                Log.v("MainActivity.class", "No photo associated with user name");
//            } else {
//                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
//            }
//        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //set navagation drawer
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set text in Navagation bar
        View header=navigationView.getHeaderView(0);
        emailNavBar = (TextView)header.findViewById(R.id.email);

        if(mFirebaseUser ==null){
            emailNavBar.setText("no login");
        }
      else {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.action_sign_out){
            mFirebaseAuth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fm = getFragmentManager();

        if (id == R.id.nav_register) {
            //setFragment
            MainFragment mainFragment = new MainFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, mainFragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_edit) {
            //setFragment
            EditFragmentList editFragment = new EditFragmentList();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, editFragment);
            fragmentTransaction.commit();


        } else if (id == R.id.nav_db) {
            //setFragment
            DatabaseFragment databaseFragment = new DatabaseFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, databaseFragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_profile) {
            //setFragment
            Profile_Fragment profileFragment = new Profile_Fragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, profileFragment);
            fragmentTransaction.commit();

        } else if (id == R.id.map_data) {

//
//            Intent a = new Intent(MainActivity.this, MapsActivity.class);
//            startActivity(a);


            fm.beginTransaction().replace(R.id.fragment_container, new GmapFragment()).commit();

//            GmapFragment mapFragment = new GmapFragment();
//            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, mapFragment);
//            fragmentTransaction.commit();



        } else if (id == R.id.nav_contact) {

            //setFragment
            ContactUsFragment contactUsFragment = new ContactUsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, contactUsFragment);
            fragmentTransaction.commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setnav(String email){
        emailNavBar.setText(mEmail);
    }
}
