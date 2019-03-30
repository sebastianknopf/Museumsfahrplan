package de.mfpl.staticnet.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mfpl.staticnet.lib.base.Container;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.LocationReference;
import de.mfpl.staticnet.lib.data.Position;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class DataRequest {

    private final static String API_ENDPOINT = "https://gtfs.svtest02.info/w/";

    private Listener listener;
    private String appId = "";
    private String apiKey = "";
    private int defaultLimit = 10;
    private boolean isAsync = true;

    private DataAPI createClient() {
        Gson gson = new GsonBuilder().setLenient().create();

        ///do NOT add custom client! This seems to increase the loading speed significantly!!!
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                        //.readTimeout(60, TimeUnit.SECONDS)
                                        //.connectTimeout(60, TimeUnit.SECONDS)
                                        .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_ENDPOINT)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .client(okHttpClient)
                                .build();

        return retrofit.create(DataAPI.class);
    }

    private Container createRequestContainer(Request request, Request.Options options, Request.Filter filter) {
        if(options == null) {
            options = new Request.Options();
        }
        request.setOptions(options);

        if(filter == null) {
            filter = new Request.Filter();
        }
        request.setFilter(filter);

        Container requestContainer = new Container();
        requestContainer.setAppId(this.appId);
        requestContainer.setApiKey(this.apiKey);
        requestContainer.setTimestamp(System.currentTimeMillis() / 1000L);
        requestContainer.setRequest(request);

        return requestContainer;
    }

    private void executeCall(Call<Container> apiCall) {
        if(this.isAsync) {
            apiCall.enqueue(new Callback<Container>() {
                @Override
                public void onResponse(Call<Container> call, Response<Container> response) {
                    if(response.isSuccessful()) {
                        Delivery delivery = response.body().getDelivery();
                        if(delivery.getError() == null) {
                            triggerListenerSuccess(delivery);
                        } else {
                            triggerListenerError(new Exception(delivery.getError().toString()));
                        }
                    } else {
                        triggerListenerError(new Exception(response.errorBody().toString()));
                    }
                }

                @Override
                public void onFailure(Call<Container> call, Throwable t) {
                    triggerListenerError(t);
                }
            });
        } else {
            try {
                Delivery delivery = apiCall.execute().body().getDelivery();

                if(delivery.getError() == null) {
                    triggerListenerSuccess(delivery);
                } else {
                    triggerListenerError(new Exception(delivery.getError().toString()));
                }
            } catch (Exception e) {
                this.triggerListenerError(e);
            }
        }
    }

    private void triggerListenerSuccess(Delivery delivery) {
        if(this.listener != null) {
            this.listener.onSuccess(delivery);
        }
    }

    private void triggerListenerError(Throwable throwable) {
        if(this.listener != null) {
            this.listener.onError(throwable);
        }
    }

    public DataRequest setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public void setAsync(boolean async) {
        this.isAsync = async;
    }

    public void loadStops(Position positionReference, int radius, Request.Filter filter) {
        Request request = new Request();

        LocationReference locationReference = new LocationReference();
        locationReference.setPosition(positionReference);
        locationReference.setRadius(radius);
        request.setLocationReference(locationReference);

        Request.Options options = new Request.Options();
        options.setIncludeStops(true);
        options.setIncludeRealtime(true);

        Container requestContainer = this.createRequestContainer(request, options, filter);

        DataAPI dataApi = this.createClient();
        Call<Container> apiCall = dataApi.locationInformationService(requestContainer);
        this.executeCall(apiCall);
    }

    public void loadRoutes(Position positionReference, int radius, Request.Filter filter) {
        Request request = new Request();

        LocationReference locationReference = new LocationReference();
        locationReference.setPosition(positionReference);
        locationReference.setRadius(radius);
        request.setLocationReference(locationReference);

        Request.Options options = new Request.Options();
        options.setIncludeRoutes(true);
        options.setIncludeAgency(true);
        options.setIncludeRealtime(true);
        options.setLimit(this.defaultLimit);

        Container requestContainer = this.createRequestContainer(request, options, filter);

        DataAPI dataApi = this.createClient();
        Call<Container> apiCall = dataApi.locationInformationService(requestContainer);
        this.executeCall(apiCall);
    }

    public void loadDepartures(String stopId, Request.Filter filter) {
        Request request = new Request();
        request.setStopId(stopId);

        Request.Options options = new Request.Options();
        //options.setIncludeFullStopTimes(true);
        options.setIncludeStops(true);
        options.setIncludeRoutes(true);
        options.setIncludeAgency(true);
        options.setIncludeRealtime(true);
        options.setLimit(this.defaultLimit);

        Container requestContainer = this.createRequestContainer(request, options, filter);

        DataAPI dataApi = this.createClient();
        Call<Container> apiCall = dataApi.stopInformationService(requestContainer);
        this.executeCall(apiCall);
    }

    public void loadTrips(String routeId, Request.Filter filter) {
        Request request = new Request();
        request.setRouteId(routeId);

        Request.Options options = new Request.Options();
        options.setIncludeFullStopTimes(true);
        options.setIncludeStops(true);
        options.setIncludeRoutes(true);
        options.setIncludeAgency(true);
        options.setIncludeRealtime(true);
        options.setLimit(this.defaultLimit);

        Container requestContainer = this.createRequestContainer(request, options, filter);

        DataAPI dataApi = this.createClient();
        Call<Container> apiCall = dataApi.tripInformationService(requestContainer);
        this.executeCall(apiCall);
    }

    public void loadTripDetails(String tripId, Request.Filter filter, boolean selectShape) {
        Request request = new Request();
        request.setTripId(tripId);

        Request.Options options = new Request.Options();
        options.setIncludeFullStopTimes(true);
        options.setIncludeStops(true);
        options.setIncludeShape(selectShape);
        options.setIncludeRoutes(true);
        options.setIncludeAgency(true);
        options.setIncludeRealtime(true);

        Container requestContainer = this.createRequestContainer(request, options, filter);

        DataAPI dataApi = this.createClient();
        Call<Container> apiCall = dataApi.tripInformationService(requestContainer);
        this.executeCall(apiCall);
    }

    public void loadCalendar(String startDate, String endDate, Request.Filter filter) {
        Request request = new Request();

        Request.Filter.Date.Range rng = new Request.Filter.Date.Range();
        rng.setStartDate(startDate);
        rng.setEndDate(endDate);
        Request.Filter.Date dateRange = new Request.Filter.Date();
        dateRange.setRange(rng);
        request.setDateRange(dateRange);

        Request.Options options = new Request.Options();
        options.setIncludeRoutes(true);
        options.setIncludeAgency(true);
        options.setIncludeRealtime(true);

        Container requestContainer = this.createRequestContainer(request, options, filter);

        DataAPI dataApi = this.createClient();
        Call<Container> apiCall = dataApi.calendarService(requestContainer);
        this.executeCall(apiCall);
    }

    public void loadCalendar(String stopId, String startDate, String endDate, Request.Filter filter) {
        Request request = new Request();
        request.setStopId(stopId);

        Request.Filter.Date.Range rng = new Request.Filter.Date.Range();
        rng.setStartDate(startDate);
        rng.setEndDate(endDate);
        Request.Filter.Date dateRange = new Request.Filter.Date();
        dateRange.setRange(rng);
        request.setDateRange(dateRange);

        Request.Options options = new Request.Options();
        options.setIncludeRoutes(true);
        options.setIncludeAgency(true);
        options.setIncludeRealtime(true);

        Container requestContainer = this.createRequestContainer(request, options, filter);

        DataAPI dataApi = this.createClient();
        Call<Container> apiCall = dataApi.calendarService(requestContainer);
        this.executeCall(apiCall);
    }

    public interface Listener {

        void onSuccess(Delivery delivery);
        void onError(Throwable throwable);

    }

}
