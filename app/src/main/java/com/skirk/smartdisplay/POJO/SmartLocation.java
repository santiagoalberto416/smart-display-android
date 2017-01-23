package com.skirk.smartdisplay.POJO;
import  android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by santiago on 22/01/2017.
 */

public class SmartLocation {
    /*
                         "id" : 1,
						"lat" : "36.66",
						"long" : "-122.22",
						"description" : "Pasillo Lacteos"
    */

    public SmartLocation(android.location.Location location, int id, String description) {
        this.location = location;
        this.id = id;
        this.description = description;
    }

    public SmartLocation(JSONObject jsonData) {
        try {
            Location object = new Location("dummyprovider");
            object.setLatitude(jsonData.getDouble("lat"));
            object.setLongitude(jsonData.getDouble("long"));
            int id = jsonData.getInt("id");
            String description = jsonData.getString("description");
            this.location = object;
            this.id = id;
            this.description = description;
        }catch (JSONException ex){
            Log.d("error", "error parsing locations");
            this.location = null;
            this.id = 0;
            this.description = "";
        }
    }

    @Override
    public String toString() {
        return "SmartLocation{" +
                "id=" + id +
                ", latitude=" + location.getLatitude() +
                ", longitude=" + location.getLongitude() +
                ", description='" + description + '\'' +
                '}';
    }

    public android.location.Location getLocation() {
        return location;
    }

    public void setLocation(android.location.Location location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;
    private android.location.Location location;
    private String description;

}
