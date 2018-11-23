package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Trip {

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("route_id")
    private String routeId;

    @SerializedName("trip_headsign")
    private String tripHeadsign;

    @SerializedName("trip_short_name")
    private String tripShortName;

    @SerializedName("direction_id")
    private Direction direction;

    @SerializedName("block_id")
    private String blockId;

    @SerializedName("wheelchair_accessible")
    private WheelchairAccessible wheelchairAccessible;

    @SerializedName("bikes_allowed")
    private BikesAllowed bikesAllowed;

    @SerializedName("route")
    private Route route;

    @SerializedName("frequency")
    private Frequency frequency;

    @SerializedName("shape")
    private Shape shape;

    public String getTripId() {
        return tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public String getTripShortName() {
        return tripShortName;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getBlockId() {
        return blockId;
    }

    public WheelchairAccessible getWheelchairAccessible() {
        return wheelchairAccessible;
    }

    public BikesAllowed getBikesAllowed() {
        return bikesAllowed;
    }

    public Route getRoute() {
        return route;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public Shape getShape() {
        return shape;
    }

    public enum Direction {
        @SerializedName("0")
        OUTBOUND,

        @SerializedName("1")
        INBOUND
    }

    public enum WheelchairAccessible {
        @SerializedName("2")
        NO,

        @SerializedName("1")
        YES,

        @SerializedName("0")
        UNDEFINED
    }

    public enum BikesAllowed {
        @SerializedName("2")
        NO,

        @SerializedName("1")
        YES,

        @SerializedName("0")
        UNDEFINED
    }

}
