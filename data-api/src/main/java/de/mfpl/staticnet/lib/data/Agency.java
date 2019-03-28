package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Agency {

    @SerializedName("agency_id")
    private String agencyId;

    @SerializedName("agency_name")
    private String agencyName;

    @SerializedName("agency_timezone")
    private String agencyTimezone;

    @SerializedName("agency_lang")
    private String agencyLang;

    @SerializedName("agency_phone")
    private String agencyPhone;

    @SerializedName("agency_url")
    private String agencyUrl;

    @SerializedName("realtime")
    private Realtime realtime;

    public String getAgencyId() {
        return agencyId;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public String getAgencyTimezone() {
        return agencyTimezone;
    }

    public String getAgencyLang() {
        return agencyLang;
    }

    public String getAgencyPhone() {
        return agencyPhone;
    }

    public String getAgencyUrl() {
        return agencyUrl;
    }

    public Realtime getRealtime() {
        return this.realtime;
    }

}
