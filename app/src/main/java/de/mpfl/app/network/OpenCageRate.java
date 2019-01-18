package de.mpfl.app.network;

import com.google.gson.annotations.SerializedName;

public final class OpenCageRate {

    @SerializedName("limit")
    private int limit;

    @SerializedName("remaining")
    private int remaining;

    @SerializedName("reset")
    private long reset;

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getReset() {
        return reset;
    }
}
