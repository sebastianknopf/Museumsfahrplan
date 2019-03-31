package de.mfpl.api.lib.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class Shape {

    @SerializedName("points")
    private List<Position> points;

    public List<Position> getPoints() {
        return points;
    }

}
