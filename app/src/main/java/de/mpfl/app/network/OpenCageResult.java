package de.mpfl.app.network;

import com.google.gson.annotations.SerializedName;

public final class OpenCageResult {

    @SerializedName("geometry")
    private OpenCageGeometry geometry;

    @SerializedName("components")
    private OpenCageComponents components;

    @SerializedName("formatted")
    private String formatted;

    public OpenCageGeometry getGeometry() {
        return geometry;
    }

    public OpenCageComponents getComponents() { return components; }

    public String getFormatted() {
        return formatted;
    }

    @Override
    public String toString() {
        if(this.components != null) {
            if(this.components.getCity() != null && !this.components.getCity().equals("")) {
                return this.components.getCity();
            } else if(this.components.getTown() != null && !this.components.getTown().equals("")) {
                return this.components.getTown();
            } else if(this.components.getVillage() != null && !this.components.getVillage().equals("")) {
                return this.components.getVillage();
            } else if(this.components.getCounty() != null && !this.components.getCounty().equals("")) {
                return this.components.getCounty();
            }
        }

        return new String();
    }
}
