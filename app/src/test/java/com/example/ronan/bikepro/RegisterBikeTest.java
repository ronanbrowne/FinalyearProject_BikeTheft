package com.example.ronan.bikepro;

import com.example.ronan.bikepro.Fragments.RegisterFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;

/**
 * Created by ronan.browne on 04/03/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk =18)
public class RegisterBikeTest {

    @Test
    public void shouldNotBeNull() throws Exception
    {
        RegisterFragment fragment = new RegisterFragment();
        startFragment( fragment );
        assertNotNull( fragment );
    }

}
