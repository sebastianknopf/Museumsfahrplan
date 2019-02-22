package de.mpfl.app.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface OpenCageAPI {

    @GET("json?language=de&no_annotations=1&pretty=0&countrycode=de")
    Call<OpenCageResponse> apiCall(@Query("key") String apiKey, @Query("q") String queryString);

}
