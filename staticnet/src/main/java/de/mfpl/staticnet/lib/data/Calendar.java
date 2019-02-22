package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class Calendar {

    @SerializedName("days")
    private List<Day> days;

    public List<Day> getDays() { return this.days; }

}
