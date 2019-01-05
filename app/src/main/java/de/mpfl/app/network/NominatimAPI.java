package de.mpfl.app.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface NominatimAPI {

    @GET("search/?format=json&namedetails=1&addressdetails=1&countrycodes=de&limit=10")
    Call<List<NominatimResult>> forwardSearch(@Query("q") String queryString);

    @GET("reverse/?format=json&namedetails=1&addressdetails=1")
    Call<NominatimResult> reverseSearch(@Query("lat") double lat, @Query("lon") double lon);

}
