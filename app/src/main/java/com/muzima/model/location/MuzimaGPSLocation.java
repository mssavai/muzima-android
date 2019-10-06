package com.muzima.model.location;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MuzimaGPSLocation {

    private String longitude;
    private String latitude;
    private long timeStamp;
    private String altitude;
    private String speed;
    private String bearing;
    private String accuracy;
    private String provider;

    private Location location;

    public MuzimaGPSLocation(final Location location) {
        this.location = location;

        if (location != null){
            initializeLocationData();
        }
    }

    private void setAccuracy() {
        this.accuracy = String.valueOf(location.getAccuracy());
    }

    private void setLongitude() {
        this.longitude = Double.toString(location.getLongitude());
    }

    private void setLatitude() {
        this.latitude = Double.toString(location.getLatitude());
    }

    private void setTimeStamp() {
        this.timeStamp = location.getTime();
    }

    private void setAltitude() {
        this.altitude = Double.toString(location.getAltitude());
    }

    private void setSpeed() {
        this.speed = String.valueOf(location.getSpeed());
    }

    private void setBearing() {
        this.bearing = String.valueOf(location.getBearing());
    }

    private void setProvider() {
        this.provider = location.getProvider();
    }

    private String getFormartedDateTime(){
        Date date = new Date();
        date.setTime(location.getTime());

        return  date.toString();
    }

    private void initializeLocationData(){
        setAltitude();
        setBearing();
        setLatitude();
        setLongitude();
        setSpeed();
        setAccuracy();
        setTimeStamp();
        setProvider();
    }

    @Override
    public String toString() {
        return "GPS: {" +
                "[ Latitude : " + latitude +
                "] Longitude : " + longitude +
                "] [ Altitude : " + altitude +
                "] [ Timestamp : " + timeStamp +
                "] [ Speed : " + speed +
                "] [ Bearing : " + bearing +
                "] }";
    }

    @Override
    public boolean equals(Object obj) {
        obj = (Location)obj;
        return latitude.equals(((Location) obj).getLatitude()) && longitude.equals(((Location) obj).getLongitude()) && altitude.equals(((Location) obj).getAltitude());
    }

    public JSONObject toJsonObject() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("latitude",latitude);
        jsonObject.put("longitude", longitude);
        jsonObject.put("altitude",altitude);
        jsonObject.put("timestamp", timeStamp);
        jsonObject.put("speed", speed);
        jsonObject.put("bearing",bearing);
        jsonObject.put("accuracy",accuracy);

        return jsonObject;
    }

    public JSONArray toJsonArray() throws  JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("latitude",latitude);
        jsonObject.put("longitude", longitude);
        jsonObject.put("altitude",altitude);
        jsonObject.put("timestamp", timeStamp);
        jsonObject.put("speed", speed);
        jsonObject.put("bearing",bearing);
        jsonObject.put("accuracy",accuracy);

        jsonArray.put(jsonObject);

        return jsonArray;
    }
}
