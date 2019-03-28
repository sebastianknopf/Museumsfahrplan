package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Stop {

    @SerializedName("stop_id")
    private String stopId;

    @SerializedName("stop_name")
    private String stopName;

    @SerializedName("stop_desc")
    private String stopDesc;

    @SerializedName("location_type")
    private LocationType locationType;

    @SerializedName("parent_station")
    private String parentStation;

    @SerializedName("wheelchair_boarding")
    private WheelchairBoarding wheelchairBoarding;

    @SerializedName("position")
    private Position position;

    @SerializedName("realtime")
    private Realtime realtime;

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public String getStopDesc() {
        return stopDesc;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public String getParentStation() {
        return parentStation;
    }

    public WheelchairBoarding getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    public Position getPosition() {
        return position;
    }

    public Realtime getRealtime() {
        return this.realtime;
    }

    public enum LocationType {
        @SerializedName("0")
        STOP,

        @SerializedName("1")
        STATION,

        @SerializedName("2")
        ENTRANCE
    }

    public enum WheelchairBoarding {
        @SerializedName("2")
        NO,

        @SerializedName("1")
        YES,

        @SerializedName("0")
        UNDEFINED
    }

}
