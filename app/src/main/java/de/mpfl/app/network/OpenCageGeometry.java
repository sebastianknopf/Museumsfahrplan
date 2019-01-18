package de.mpfl.app.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sebastian on 17.01.2019.
 */

public final class OpenCageGeometry {

    @SerializedName("lat")
    private float latitude;

    @SerializedName("lng")
    private float longitude;

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

}
