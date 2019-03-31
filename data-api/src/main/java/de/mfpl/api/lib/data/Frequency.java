package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Frequency {

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("headway")
    private int headway;

    @SerializedName("exact_times")
    private ExactTimes exactTimes;

    @SerializedName("trip_time")
    private String tripTime;

    @SerializedName("trip_date")
    private String tripDate;

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getHeadway() {
        return headway;
    }

    public ExactTimes getExactTimes() {
        return exactTimes;
    }

    public String getTripTime() {
        return tripTime;
    }

    public String getTripDate() {
        return tripDate;
    }

    public enum ExactTimes {
        @SerializedName("0")
        NO,

        @SerializedName("1")
        YES
    }

}
