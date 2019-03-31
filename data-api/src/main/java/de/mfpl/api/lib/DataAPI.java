package de.mfpl.api.lib;

import de.mfpl.api.lib.base.Container;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DataAPI {

    @POST("LocationInformationService")
    Call<Container> locationInformationService(@Body Container body);

    @POST("TripInformationService")
    Call<Container> tripInformationService(@Body Container body);

    @POST("StopInformationService")
    Call<Container> stopInformationService(@Body Container body);

    @POST("CalendarService")
    Call<Container> calendarService(@Body Container body);

}
