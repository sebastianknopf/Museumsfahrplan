package de.mpfl.app.network;

import com.google.gson.annotations.SerializedName;

public final class NominatimResult {

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    @SerializedName("type")
    private String type;

    @SerializedName("address")
    private NominatimAddress nominatimAddress;

    @SerializedName("namedetails")
    private NominatimNameDetails nameDetails;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NominatimAddress getAddress() {
        return nominatimAddress;
    }

    public void setAddress(NominatimAddress nominatimAddress) {
        this.nominatimAddress = nominatimAddress;
    }

    public NominatimNameDetails getNameDetails() {
        return nameDetails;
    }

    public void setNameDetails(NominatimNameDetails nameDetails) {
        this.nameDetails = nameDetails;
    }

    public String getName() {
        String nameString = null;
        if(this.getAddress() != null) {
            nameString = this.getAddress().toString();
        }

        if((nameString == null || nameString.equals("")) && this.getNameDetails() != null) {
            nameString = this.getNameDetails().getName();
        }

        return nameString;
    }

    public String getState() {
        if(this.getAddress() != null) {
            if(!this.getAddress().getState().equals("")) {
                return this.getAddress().getState();
            } else {
                return this.getAddress().getCountry();
            }
        }

        return new String();
    }
}
