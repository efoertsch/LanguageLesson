package com.fisincorporated.aviationweather.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class AppRetrofit {

    public static final String AVIATION_WEATHER_URL = "https://aviationweather.gov/adds/dataserver_current/";

    private static Retrofit retrofit;

    private AppRetrofit(){};

    public static Retrofit get() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(AVIATION_WEATHER_URL)
                    .client(new OkHttpClient())
                    .addConverterFactory(SimpleXmlConverterFactory.create());

            retrofit = builder.client(httpClient.build()).build();
        }
        return retrofit;
    }

}
