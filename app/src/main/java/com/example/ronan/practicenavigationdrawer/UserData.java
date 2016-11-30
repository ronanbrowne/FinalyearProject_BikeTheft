package com.example.ronan.practicenavigationdrawer;

import java.util.Date;

import static com.example.ronan.practicenavigationdrawer.R.id.username;

/**
 * Created by Ronan on 05/10/2016.
 */

public class UserData {

    private String username;
    private String email;
    private String address;
    private String signUpdate;
    private String user_image_In_Base64;

    public UserData(String address, String username, String user_image_In_Base64, String signUpdate, String email) {
        this.address = address;
        this.username = username;
        this.user_image_In_Base64 = user_image_In_Base64;
        this.signUpdate = signUpdate;
        this.email = email;
    }

    public UserData() {
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getSignUpdate() {
        return signUpdate;
    }

    public String getUser_image_In_Base64() {
        return user_image_In_Base64;
    }

    public String getUsername() {
        return username;
    }
}
