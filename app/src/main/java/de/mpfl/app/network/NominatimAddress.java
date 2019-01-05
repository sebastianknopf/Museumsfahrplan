package de.mpfl.app.network;

import com.google.gson.annotations.SerializedName;

public final class NominatimAddress {

    @SerializedName("village")
    private String village;

    @SerializedName("city")
    private String city;

    @SerializedName("suburb")
    private String suburb;

    @SerializedName("town")
    private String town;

    @SerializedName("postcode")
    private String postcode;

    @SerializedName("state")
    private String state;

    @SerializedName("country")
    private String country;

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        if(this.village != null && !this.village.equals("")) {
            return this.village;
        } else if(this.town != null && !this.town.equals("")) {
            return this.town;
        } else if(this.city != null && !this.city.equals("")) {
            return this.city;
        } else if(this.suburb != null && !this.suburb.equals("")) {
            return this.suburb;
        } else {
            return new String();
        }
    }
}
