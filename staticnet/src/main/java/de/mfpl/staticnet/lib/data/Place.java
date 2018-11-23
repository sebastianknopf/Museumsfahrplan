package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Place {

    @SerializedName("place_id")
    private String placeId;

    @SerializedName("place_name")
    private String placeName;

    @SerializedName("position")
    private Position position;

    public String getPlaceId() {
        return placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Position getPosition() {
        return position;
    }

}
