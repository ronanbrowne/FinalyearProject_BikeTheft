package com.example.ronan.practicenavigationdrawer.DataModel;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.google.firebase.database.Exclude;
import com.firebase.client.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class BikeData {
//======================================================================================
// this is the data model class for all bike attributes
//======================================================================================


    //variables
    private String make;
    private String model;
    private String registeredBy;
    private int frameSize;
    private String color;
    private String otherFeatures;
    private String imageBase64;
    private String lastSeen;
    private boolean stolen;
    private double latitude;
    private double longditude;

    private String reportedBy;
    private String reportedLocation;
    private String reportedDate;
    private boolean reportedSigting;
   // private Map<String, String> creationDate = new HashMap<String, String>();
      private HashMap<String, Object> timestampCreated;
  //  private Long creationDate;


    //constructor
    public BikeData(String make, int frameSize, String color, String other, boolean stolen, String imageBase64, String model, String lastSeen, double latitude, double longditude, String registeredBy) {
        this.make = make;
        this.registeredBy = registeredBy;
        this.frameSize = frameSize;
        this.color = color;
        this.otherFeatures = other;
        this.stolen = stolen;
        this.imageBase64 = imageBase64;
        this.model = model;
        this.lastSeen = lastSeen;
        this.latitude = latitude;
        this.longditude = longditude;

        // TIME STAMP
        HashMap<String, Object> timestampCreatedObj = new HashMap<String, Object>();
        timestampCreatedObj.put("date", ServerValue.TIMESTAMP);
        this.timestampCreated = timestampCreatedObj;


    }


    //constructor for reporting sighting
    public BikeData(String make, int frameSize, String color, String other, boolean stolen, String imageBase64, String model, String lastSeen, double latitude, double longditude, String registeredBy, String reportedLocation, boolean reportedSigting, String reportedBy, String reportedDate) {
        this.color = color;
        this.frameSize = frameSize;
        this.imageBase64 = imageBase64;
        this.lastSeen = lastSeen;
        this.latitude = latitude;
        this.longditude = longditude;
        this.make = make;
        this.model = model;
        this.otherFeatures = other;
        this.registeredBy = registeredBy;
        this.reportedBy = reportedBy;
        this.reportedDate = reportedDate;
        this.reportedLocation = reportedLocation;
        this.reportedSigting = reportedSigting;
        this.stolen = stolen;
    }


    public HashMap<String, Object> getTimestampCreated(){
        return timestampCreated;
    }

    @Exclude
    public long getTimestampCreatedLong(){
        return (long)timestampCreated.get("date");
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public void setReportedDate(String reportedDate) {
        this.reportedDate = reportedDate;
    }

    public void setReportedLocation(String reportedLocation) {
        this.reportedLocation = reportedLocation;
    }

    public void setReportedSigting(boolean reportedSigting) {
        this.reportedSigting = reportedSigting;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public String getReportedDate() {
        return reportedDate;
    }

    public String getReportedLocation() {
        return reportedLocation;
    }

    public boolean isReportedSigting() {
        return reportedSigting;
    }


    public BikeData() {
    }


    //getters

    public String getRegisteredBy() {
        return registeredBy;
    }

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