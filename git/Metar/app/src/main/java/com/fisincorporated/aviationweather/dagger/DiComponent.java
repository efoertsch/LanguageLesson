package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.airportweather.AirportWeatherActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component
public interface DiComponent {

    void inject(AirportWeatherActivity activity);

}

