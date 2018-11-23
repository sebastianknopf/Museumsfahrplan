package de.mfpl.staticnet;

import org.junit.Test;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Place;
import de.mfpl.staticnet.lib.data.Position;
import de.mfpl.staticnet.lib.data.Route;
import de.mfpl.staticnet.lib.data.Stop;
import de.mfpl.staticnet.lib.data.Trip;

public class StaticLibraryTest {

    private final static String APP_ID = "<AppID>";
    private final static String API_KEY = "<ApiKey>";

    private StaticRequest createRequest(StaticRequest.Listener listener) {
        final StaticRequest request = new StaticRequest();
        request.setAppId(APP_ID);
        request.setApiKey(API_KEY);
        request.setAsync(false);
        request.setListener(listener);

        return request;
    }

    @Test
    public void loadStopsTest() {
        StaticRequest request = this.createRequest(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                for(Stop stop : delivery.getStops()) {
                    System.out.println(stop.getStopName());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        Position position = new Position();
        position.setLatitude(49.0068901);
        position.setLongitude(8.4036527);

        request.loadStops(position, 1000, null);
    }

    @Test
    public void loadRoutesTest() {
        StaticRequest request = this.createRequest(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                for(Route route : delivery.getRoutes()) {
                    System.out.println(route.getRouteShortName() + " " + route.getRouteLongName());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        Position position = new Position();
        position.setLatitude(49.0068901);
        position.setLongitude(8.4036527);

        Request.Filter filter = new Request.Filter();
        filter.setDate(new Request.Filter.Date().setSingle("20181118"));

        request.loadRoutes(position, 50000, filter);
    }

    @Test
    public void loadPlacesTest() {
        StaticRequest request = this.createRequest(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                for(Place place : delivery.getPlaces()) {
                    System.out.println(place.getPlaceName());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        request.loadPlaces("Pfo");
    }

    @Test
    public void loadDeparturesTest() {
        StaticRequest request = this.createRequest(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                for(Trip trip : delivery.getTrips()) {
                    System.out.println(trip.getTripShortName() + " " + trip.getTripHeadsign());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        request.loadDepartures("RK14", null);
    }

    @Test
    public void loadTripsTest() {
        StaticRequest request = this.createRequest(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                for(Trip trip : delivery.getTrips()) {
                    System.out.println(trip.getTripShortName() + " " + trip.getTripHeadsign());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        request.loadTrips("TEST-TEST", null);
    }

    @Test
    public void loadTripDetailsTest() {
        StaticRequest request = this.createRequest(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                System.out.println("found "  + delivery.getTrips().size() + " trip(s)");
                if(delivery.getTrips().size() != 1) {
                    return;
                }

                Trip trip = delivery.getTrips().get(0);
                System.out.println("this trip is " + (trip == null ? "null" : "not null") + " - try loading statistics ...");
                System.out.println("trip_id: " + trip.getTripId());
                System.out.println("trip_short_name: " + trip.getTripShortName());
                System.out.println("trip_headsign: " + trip.getTripHeadsign());
                System.out.println("bikes_allowed: " + trip.getBikesAllowed().name());
                System.out.println("wheelchair_accessible: " + trip.getWheelchairAccessible().name());
                System.out.println("route: " + trip.getRoute().getRouteId());
                System.out.println("frequency is " + (trip.getFrequency() == null ? "null" : "not null"));

                if(trip.getFrequency() != null) {
                    System.out.println("frequency start: " + trip.getFrequency().getStartTime());
                    System.out.println("frequency end: " + trip.getFrequency().getEndTime());
                    System.out.println("frequency headway: " + trip.getFrequency().getHeadway() + " minutes");
                    System.out.println("frequency specifies exact times: " + trip.getFrequency().getExactTimes().name());
                }

                System.out.println("shape is " + (trip.getShape() == null ? "null" : "not null"));

                if(trip.getShape() != null) {
                    System.out.println("shape contains " + trip.getShape().getPoints().size() + " points");
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        request.loadTripDetails("TEST-TEST-s18-FreqTest", true);
    }
}