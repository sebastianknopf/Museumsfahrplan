package de.mfpl.app.network;

import com.google.gson.annotations.SerializedName;

public final class OpenCageComponents {

    @SerializedName("_type")
    private String type;

    @SerializedName("state")
    private String state;

    @SerializedName("city")
    private String city;

    @SerializedName("suburb")
    private String suburb;

    @SerializedName("town")
    private String town;

    @SerializedName("village")
    private String village;

    @SerializedName("county")
    private String county;

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getVillage() {
        return village;
    }

    public String getTown() { return town; }

    public String getCounty() {return county;}

}
