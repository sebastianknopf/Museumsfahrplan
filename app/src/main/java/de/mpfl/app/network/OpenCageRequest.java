package de.mpfl.app.network;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class OpenCageRequest {

    private final static String API_ENDPOINT = "https://api.opencagedata.com/geocode/v1/";

    private static OpenCageAPI openCageApi = null;

    private static OpenCageAPI getInstance() {
        if(openCageApi == null) {
            Gson gson = new GsonBuilder().setLenient().create();

            Retrofit retrofit = new Retrofit.Builder().baseUrl(API_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            openCageApi = retrofit.create(OpenCageAPI.class);
        }

        return openCageApi;
    }

    public static Call<OpenCageResponse> createForwardCall(String apiKey, String queryString) {
        OpenCageAPI api = OpenCageRequest.getInstance();
        return api.apiCall(apiKey, queryString);
    }

    public static Call<OpenCageResponse> createReverseCall(String apiKey, double latitude, double longitude) {
        OpenCageAPI api = OpenCageRequest.getInstance();
        return api.apiCall(apiKey, String.valueOf(latitude) + "," + String.valueOf(longitude));
    }
}
