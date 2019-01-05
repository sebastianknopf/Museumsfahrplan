package de.mpfl.app.network;

import com.google.gson.annotations.SerializedName;

public final class NominatimNameDetails {

    @SerializedName("name")
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
