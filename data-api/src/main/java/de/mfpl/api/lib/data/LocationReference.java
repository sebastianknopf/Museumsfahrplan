package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

public final class LocationReference {

    @SerializedName("position")
    private Position position;

    @SerializedName("radius")
    private int radius;

    @SerializedName("initial_input")
    private String initialInput;

    public LocationReference setPosition(Position position) {
        this.position = position;
        return this;
    }

    public Position getPosition() {
        return this.position;
    }

    public LocationReference setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public int getRadius() {
        return this.radius;
    }

    public LocationReference setInitialInput(String initialInput) {
        this.initialInput = initialInput;
        return this;
    }

    public String getInitialInput() {
        return this.initialInput;
    }
}
