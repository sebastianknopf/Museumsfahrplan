package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Day {

    @SerializedName("date")
    private String date;

    @SerializedName("route")
    private Route route;

    public String getDate() {
        return date;
    }

    public Route getRoute() {
        return route;
    }

}
