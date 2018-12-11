package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class Realtime {

    @SerializedName("alerts")
    List<Alert> alerts;

    public List<Alert> getAlerts() {
        return alerts;
    }

}
