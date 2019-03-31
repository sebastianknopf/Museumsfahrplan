package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Route {

    @SerializedName("route_id")
    private String routeId;

    @SerializedName("agency_id")
    private String agencyId;

    @SerializedName("route_short_name")
    private String routeShortName;

    @SerializedName("route_long_name")
    private String routeLongName;

    @SerializedName("route_desc")
    private String routeDescription;

    @SerializedName("route_type")
    private RouteType routeType;

    @SerializedName("route_url")
    private String routeUrl;

    @SerializedName("route_color")
    private String routeColor;

    @SerializedName("route_text_color")
    private String routeTextColor;

    @SerializedName("position")
    private Position position;

    @SerializedName("agency")
    private Agency agency;

    @SerializedName("realtime")
    private Realtime realtime;

    public String getRouteId() {
        return routeId;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public String getRouteUrl() {
        return routeUrl;
    }

    public String getRouteColor() {
        return routeColor;
    }

    public String getRouteTextColor() {
        return routeTextColor;
    }

    public Position getPosition() {
        return position;
    }

    public Agency getAgency() {
        return agency;
    }

    public Realtime getRealtime() {
        return this.realtime;
    }

    public enum RouteType {
        @SerializedName("0")
        TRAM,

        @SerializedName("1")
        SUBWAY,

        @SerializedName("2")
        RAIL,

        @SerializedName("3")
        BUS,

        @SerializedName("4")
        FERRY,

        @SerializedName("5")
        CABLE_CAR,

        @SerializedName("6")
        GONDOLA,

        @SerializedName("7")
        FUNICULAR
    }

}
