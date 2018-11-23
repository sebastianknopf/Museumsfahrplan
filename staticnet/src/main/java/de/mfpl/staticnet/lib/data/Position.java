package de.mfpl.staticnet.lib.data;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

public final class Position {

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lng")
    private double longitude;

    @SerializedName("distance")
    private int distance;

    public static Position formAndroidLocation(Location location) {
        Position result = new Position();
        result.setLatitude(location.getLatitude());
        result.setLongitude(location.getLongitude());

        return result;
    }

    public Position setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLatitude() {
        return this.latitude = latitude;
    }

    public Position setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public int getDistance() {
        return this.distance;
    }
}
