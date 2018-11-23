package de.mfpl.staticnet.lib.data;

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

    public enum ExactTimes {
        @SerializedName("0")
        NO,

        @SerializedName("1")
        YES
    }

}
