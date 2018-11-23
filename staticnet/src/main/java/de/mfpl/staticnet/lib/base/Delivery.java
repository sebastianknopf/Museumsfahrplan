package de.mfpl.staticnet.lib.base;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import de.mfpl.staticnet.lib.data.Error;
import de.mfpl.staticnet.lib.data.Place;
import de.mfpl.staticnet.lib.data.Route;
import de.mfpl.staticnet.lib.data.Stop;
import de.mfpl.staticnet.lib.data.Trip;

public final class Delivery {

    @SerializedName("stops")
    private List<Stop> stops;

    @SerializedName("routes")
    private List<Route> routes;

    @SerializedName("places")
    private List<Place> places;

    @SerializedName("trips")
    private List<Trip> trips;

    @SerializedName("error")
    private Error error;

    public List<Stop> getStops() {
        return stops;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public Error getError() {
        return error;
    }

}
