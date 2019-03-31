package de.mfpl.api.lib.base;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import de.mfpl.api.lib.data.Calendar;
import de.mfpl.api.lib.data.Error;
import de.mfpl.api.lib.data.Route;
import de.mfpl.api.lib.data.Stop;
import de.mfpl.api.lib.data.Trip;

public final class Delivery {

    @SerializedName("stops")
    private List<Stop> stops;

    @SerializedName("routes")
    private List<Route> routes;

    @SerializedName("trips")
    private List<Trip> trips;

    @SerializedName("calendar")
    private Calendar calendar;

    @SerializedName("error")
    private Error error;

    public List<Stop> getStops() {
        return stops;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public Calendar getCalendar() { return calendar; }

    public Error getError() {
        return error;
    }

}
