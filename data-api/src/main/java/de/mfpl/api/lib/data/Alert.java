package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Alert {

    @SerializedName("alert_id")
    private String alertId;

    @SerializedName("alert_header")
    private String alertHeader;

    @SerializedName("alert_description")
    private String alertDescription;

    @SerializedName("alert_url")
    private String alertUrl;

    @SerializedName("alert_cause")
    private Cause alertCause;

    @SerializedName("alert_effect")
    private Effect alertEffect;

    public String getAlertId() {
        return alertId;
    }

    public String getAlertHeader() {
        return alertHeader;
    }

    public String getAlertDescription() {
        return alertDescription;
    }

    public String getAlertUrl() {
        return alertUrl;
    }

    public Cause getAlertCause() {
        return alertCause;
    }

    public Effect getAlertEffect() {
        return alertEffect;
    }

    public enum Cause {
        @SerializedName("0")
        UNKNOWN,

        @SerializedName("1")
        OTHER,

        @SerializedName("2")
        TECHNICAL_PROBLEM,

        @SerializedName("3")
        STRIKE,

        @SerializedName("4")
        DEMONSTRATION,

        @SerializedName("5")
        ACCIDENT,

        @SerializedName("6")
        HOLIDAY,

        @SerializedName("7")
        WEATHER,

        @SerializedName("8")
        MAINTENANCE,

        @SerializedName("9")
        CONSTRUCTION,

        @SerializedName("10")
        POLICE_ACTIVITY,

        @SerializedName("11")
        MEDICAL_EMERGENCY
    }

    public enum Effect {
        @SerializedName("0")
        NO_SERVICE,

        @SerializedName("1")
        REDUCED_SERVICE,

        @SerializedName("2")
        SIGNIFICANT_DELAYS,

        @SerializedName("3")
        DETOUR,

        @SerializedName("4")
        ADDITIONAL_SERVICE,

        @SerializedName("5")
        MODIFIED_SERVICE,

        @SerializedName("6")
        STOP_MOVED,

        @SerializedName("7")
        OTHER,

        @SerializedName("8")
        UNKNOWN
    }

}
