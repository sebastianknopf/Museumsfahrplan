package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

public final class TripUpdate {

    @SerializedName("update_id")
    private String updateId;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("route_id")
    private String routeId;

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("trip_date")
    private String tripDate;

    @SerializedName("trip_time")
    private String tripTime;

    @SerializedName("schedule_relationship")
    private ScheduleRelationship scheduleRelationship;

    @SerializedName("vehicle_label")
    private String vehicleLabel;

    @SerializedName("vehicle_position")
    private Position vehiclePosition;

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public String getTripTime() {
        return tripTime;
    }

    public void setTripTime(String tripTime) {
        this.tripTime = tripTime;
    }

    public ScheduleRelationship getScheduleRelationship() {
        return scheduleRelationship;
    }

    public void setScheduleRelationship(ScheduleRelationship scheduleRelationship) {
        this.scheduleRelationship = scheduleRelationship;
    }

    public String getVehicleLabel() {
        return vehicleLabel;
    }

    public void setVehicleLabel(String vehicleLabel) {
        this.vehicleLabel = vehicleLabel;
    }

    public Position getVehiclePosition() {
        return vehiclePosition;
    }

    public void setVehiclePosition(Position vehiclePosition) {
        this.vehiclePosition = vehiclePosition;
    }

    public enum ScheduleRelationship {
        @SerializedName("0")
        SCHEDULED,

        @SerializedName("1")
        ADDED,

        @SerializedName("2")
        UNSCHEDULED,

        @SerializedName("3")
        CANCELED
    }

}
