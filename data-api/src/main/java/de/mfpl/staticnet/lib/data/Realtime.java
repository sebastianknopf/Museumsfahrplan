package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class Realtime {

    @SerializedName("alerts")
    List<Alert> alerts;

    @SerializedName("trip_update")
    private TripUpdate tripUpdate;

    @SerializedName("stop_time_update")
    private StopTimeUpdate stopTimeUpdate;

    public List<Alert> getAlerts() {
        return alerts;
    }

    public boolean hasAlerts() {
        return this.alerts != null && this.alerts.size() > 0;
    }

    public TripUpdate getTripUpdate() {
        return this.tripUpdate;
    }

    public StopTimeUpdate getStopTimeUpdate() {
        return this.stopTimeUpdate;
    }

}
