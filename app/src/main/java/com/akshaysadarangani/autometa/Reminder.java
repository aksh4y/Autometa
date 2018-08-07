package com.akshaysadarangani.autometa;

import com.google.android.gms.maps.model.LatLng;

public class Reminder {

    private String rid;
    private String userID;
    private String userName;
    private String description;
    private String phone;
    private String email;
    private int distance;
    private String unit;
    private LatLng location;
    private String type;

    public Reminder() {    }

    public Reminder(String rid, String userID, String userName, String type, String description, String phone, String email, int distance, String unit, LatLng location){
        this.rid = rid;
        this.userID = userID;
        this.userName = userName;
        this.type = type;
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.distance = distance;
        this.unit = unit;
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }
}
