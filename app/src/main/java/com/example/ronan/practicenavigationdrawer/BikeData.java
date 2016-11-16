package com.example.ronan.practicenavigationdrawer;


public class BikeData {

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

    public String getImageBase64() {
        return imageBase64;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public BikeData() {

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
}