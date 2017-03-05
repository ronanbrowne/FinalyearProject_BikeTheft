package com.example.ronan.bikepro;

import android.support.annotation.NonNull;
import android.widget.EditText;

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
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Ronan on 28/02/2017.
 */


@RunWith(RobolectricTestRunner.class)
@Config(manifest = "app/src/main/AndroidManifest.xml", sdk = 23)
public class SignInTest {

    private SignIn activity;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText email;
    EditText pass;

    @Before
    public void setup() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application);

        activity = Robolectric.buildActivity(SignIn.class)
                .create().get();

        email = (EditText) activity.findViewById(R.id.field_email);
        pass = (EditText) activity.findViewById(R.id.field_email);

        email.setText("testing@gmail.com");
        pass.setText("testing");

    }

    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(activity);
    }

    @Test
    public void fieldsNotEmpty() throws Exception {
        assertThat(email).isNotNull();
        assertThat(pass).isNotNull();
    }


    @Test
    public void buttonClickShouldLogInUser() throws Exception {

        activity.findViewById(R.id.email_sign_in_button).performClick();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                assertThat(user).isNotNull();
            }
        };
    }






}

