package de.mpfl.app.network;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NominatimRequest {

    private final static String API_ENDPOINT = "https://nominatim.openstreetmap.org/";

    private static NominatimAPI createClient() {
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(NominatimAPI.class);
    }

    public static Call<NominatimResult> createReverseCall(double lat, double lon) {
        NominatimAPI api = NominatimRequest.createClient();
        return api.reverseSearch(lat, lon);
    }

    public static Call<List<NominatimResult>> createForwardCall(String queryString) {
        NominatimAPI api = NominatimRequest.createClient();
        return api.forwardSearch(queryString);
    }
}
