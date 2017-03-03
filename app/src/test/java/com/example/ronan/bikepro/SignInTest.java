package com.example.ronan.bikepro;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;

import com.example.ronan.bikepro.Activities.MainActivity;
import com.example.ronan.bikepro.Activities.SignIn;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static com.example.ronan.bikepro.R.id.textView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * Created by Ronan on 28/02/2017.
 */


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 22)
public class SignInTest {

    private SignIn activity;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Before
    public void setup() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application);
        mAuth = FirebaseAuth.getInstance();

        activity = Robolectric.buildActivity(SignIn.class)
                .create().get();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(activity);
    }


    @Test
    public void buttonClickShouldStartNewActivity() throws Exception
    {
      //  MainActivity activity = Robolectric.setupActivity(MainActivity.class);
       EditText email = (EditText) activity.findViewById(R.id.field_email);
       EditText pass = (EditText) activity.findViewById(R.id.field_email);

        email.setText("testing@gmail.com");
        pass.setText("testing");

        assertThat(email).isNotNull();
        assertThat(pass).isNotNull();

        activity.findViewById(R.id.email_sign_in_button).performClick();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    startActivity(new Intent(SignIn.this, MainActivity.class));


            }
        };
    }

}
