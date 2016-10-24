package com.example.ronan.practicenavigationdrawer;

import java.util.Date;

import static com.example.ronan.practicenavigationdrawer.R.id.username;

/**
 * Created by Ronan on 05/10/2016.
 */

public class UserData {

    private String username;
    private String email;
    private String city;
    private String country;
    private String signUpdate;
    private String user_image_In_Base64;

    public UserData(String city, String username, String user_image_In_Base64, String signUpdate, String email, String country) {
        this.city = city;
        this.username = username;
        this.user_image_In_Base64 = user_image_In_Base64;
        this.signUpdate = signUpdate;
        this.email = email;
        this.country = country;
    }

    public UserData() {
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
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
