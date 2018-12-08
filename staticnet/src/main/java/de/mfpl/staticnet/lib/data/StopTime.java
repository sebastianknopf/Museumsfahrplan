package de.mfpl.staticnet.lib.data;

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
