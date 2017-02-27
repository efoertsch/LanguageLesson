package com.fisincorporated.aviationweather.retrofit;

import com.fisincorporated.airportweather.metars.MetarResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class AviationWeatherAPI {

    public interface CurrentMetar {


        @GET("httpparam?dataSource=metars&requestType=retrieve&format=xml&mostRecentForEachStation=constraint")
        Call<MetarResponse> mostRecentMetarForEachAirport(@Query("stationString") String icaoIdentifiers, @Query("hoursBeforeNow") int hoursBeforeNow);

    }

}
