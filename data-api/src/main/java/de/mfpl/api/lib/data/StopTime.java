package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

public final class StopTime {

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("stop_id")
    private String stopId;

    @SerializedName("arrival_time")
    private String arrivalTime;

    @SerializedName("departure_time")
    private String departureTime;

    @SerializedName("pickup_type")
    private ChangeType pickupType;

    @SerializedName("drop_off_type")
    private ChangeType dropOffType;

    @SerializedName("stop")
    private Stop stop;

    @SerializedName("realtime")
    private Realtime realtime;

    public String getTripId() {
        return tripId;
    }

    public String getStopId() {
        return stopId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public ChangeType getPickupType() {
        return pickupType;
    }

    public ChangeType getDropOffType() {
        return dropOffType;
    }

    public Stop getStop() {
        return stop;
    }

    public Realtime getRealtime() {
        return this.realtime;
    }

    public boolean hasStopTimeUpdate() {
        return this.realtime != null && this.realtime.getStopTimeUpdate() != null;
    }

    public StopTimeUpdate getStopTimeUpdate() {
        if(this.hasStopTimeUpdate()) {
            return this.realtime.getStopTimeUpdate();
        } else {
            return null;
        }
    }

    public enum ChangeType {
        @SerializedName("0")
        REGULAR,

        @SerializedName("1")
        NO_DROP_OFF,

        @SerializedName("2")
        NO_PICKUP,

        @SerializedName("3")
        DEMAND
    }

}
