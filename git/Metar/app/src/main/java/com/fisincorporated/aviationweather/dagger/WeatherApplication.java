package com.fisincorporated.aviationweather.dagger;

import android.app.Application;

public class WeatherApplication extends Application {
    DiComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerDiComponent.builder().build();
    }

    public DiComponent getComponent() {
        return component;
    }


}
