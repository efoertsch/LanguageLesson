package com.fisincorporated.airportweather;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.dagger.WeatherApplication;
import com.fisincorporated.metar.R;

import javax.inject.Inject;

public class AirportWeatherActivity extends AppCompatActivity {

    public static final String METAR_LIST = "METAR_LIST";

    @Inject
    public AirportWeatherViewModel airportWeatherViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_airport_weather);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Go to activity to find/enter additional airports", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

         //String airportList = getIntent().getStringExtra(METAR_LIST);
        String airportList = "KORH, KFIT";

        ((WeatherApplication) getApplication()).getComponent().inject(this);

        airportWeatherViewModel.setView((ViewGroup) findViewById(android.R.id.content)).setAirportList(airportList);

    }

    public void onResume() {
        super.onResume();
        airportWeatherViewModel.onResume();

    }

    public void onPause() {
        super.onPause();
        airportWeatherViewModel.onPause();

    }
}


