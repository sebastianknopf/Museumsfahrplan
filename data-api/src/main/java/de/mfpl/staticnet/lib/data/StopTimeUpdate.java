package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

public final class StopTimeUpdate {

    @SerializedName("update_id")
    private String updateId;

    @SerializedName("arrival_delay")
    private int arrivalDelay;

    @SerializedName("departure_delay")
    private int departureDelay;

    @SerializedName("schedule_relationship")
    private ScheduleRelationship scheduleRelationship;

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }

    public int getArrivalDelay() {
        return arrivalDelay;
    }

    public void setArrivalDelay(int arrivalDelay) {
        this.arrivalDelay = arrivalDelay;
    }

    public int getDepartureDelay() {
        return departureDelay;
    }

    public void setDepartureDelay(int departureDelay) {
        this.departureDelay = departureDelay;
    }

    public ScheduleRelationship getScheduleRelationship() {
        return scheduleRelationship;
    }

    public void setScheduleRelationship(ScheduleRelationship scheduleRelationship) {
        this.scheduleRelationship = scheduleRelationship;
    }

    public enum ScheduleRelationship {
        @SerializedName("0")
        SCHEDULED,

        @SerializedName("1")
        SKIPPED,

        @SerializedName("2")
        NO_DATA
    }
}
