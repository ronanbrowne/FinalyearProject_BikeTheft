package com.example.ronan.practicenavigationdrawer;

//this is the data model class for all bike attributes
//upon registration of a bike a new bike object is created and pushed to the firebase DB (cloud based JSON db)
//this is stored in a json node specifically related to the currently signed in user.

public class BikeData {

    //variables
    private String make;
    private String model;
    private int frameSize;
    private String color;
    private String otherFeatures;
    private String imageBase64;
    private String lastSeen;
    private boolean stolen;
    private double latitude;
    private double longditude;



    //constructor
    public BikeData(String make, int frameSize, String color, String other, boolean  stolen, String imageBase64, String model,String lastSeen, double latitude, double longditude) {
        this.make = make;
        this.frameSize = frameSize;
        this.color = color;
        this.otherFeatures = other;
        this.stolen = stolen;
        this.imageBase64 = imageBase64;
        this.model = model;
        this.lastSeen = lastSeen;
        this.latitude = latitude;
        this.longditude = longditude;

    }

    public BikeData() {
    }


    //getters
    public String getImageBase64() {
        return imageBase64;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongditude() {
        return longditude;
    }

    public String getOtherFeatures() {
        return otherFeatures;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public String getModel() {
        return model;
    }

    public boolean isStolen() {
        return stolen;
    }

    public String getOther() {
        return otherFeatures;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public String getColor() {
        return color;
    }

    public String getMake() {
        return make;
    }

    //to stirng
    @Override
    public String toString() {
        return super.toString();
    }


}// end class