package com.fisincorporated.airportweather;


import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.fisincorporated.airportweather.metars.Metar;
import com.fisincorporated.airportweather.metars.MetarResponse;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherAPI;
import com.fisincorporated.metar.BR;
import com.fisincorporated.metar.R;
import com.fisincorporated.metar.databinding.ActivityRecyclerViewBinding;

import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinder;
import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinderBase;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AirportWeatherViewModel  extends BaseObservable {

    private Call<MetarResponse> metarCall;

    private String airportList;

    private boolean metarCallComplete = false;

    private RecyclerView recyclerView;

    public ObservableArrayList<Metar> metars = new ObservableArrayList<>();

    private ActivityRecyclerViewBinding viewDataBinding;

    @Inject
    public AirportWeatherAdapter airportWeatherAdapter;

    @Inject
    public AirportWeatherViewModel() {
    }

    /**
     * This gets called via xml parms in recylerview in activity_recycler_view
     * and binds a metar to a row (airport_metar_taf) in the recyclerview
     */
    public ItemBinder<Metar> itemViewBinder()
    {
        return new ItemBinderBase<>(BR.metar, R.layout.airport_metar_taf);
    }


    public AirportWeatherViewModel setView(View view) {
        view = view.findViewById(R.id.activity_weather_view);
        viewDataBinding = DataBindingUtil.bind(view);
        recyclerView = viewDataBinding.activityMetarRecyclerView;
        setup();
        // This binding is to  handle indeterminate progress bar
        viewDataBinding.setMetars(metars);
        // This binding is to handle metar detail
        viewDataBinding.setViewmodel(this);
        return this;
    }

    public AirportWeatherViewModel setAirportList(String airportList) {
        this.airportList = airportList;
        return this;
    }

    public void setup() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }


    public void onResume() {
        metarCallComplete = false;
        callForMetar();
    }

    public void onPause() {
        if (metarCall != null) {
            metarCall.cancel();
        }
    }

    public List<Metar> getMetarList() {
        return metars;
    }

    public void callForMetar() {

        AviationWeatherAPI.CurrentMetar client = AppRetrofit.get().create(AviationWeatherAPI
                .CurrentMetar.class);

        metarCall = client.mostRecentMetarForEachAirport(airportList, 1);

        // Execute the call asynchronously. Get a positive or negative callback.
        metarCall.enqueue(new Callback<MetarResponse>() {
            @Override
            public void onResponse(Call<MetarResponse> call, Response<MetarResponse> response) {
                Log.d("AirportWeatherActivity", "Got response");
                if (response != null && response.body().getErrors() == null) {
                    metars.clear();
                    metars.addAll(response.body().getData().getMetars());
                  // airportWeatherAdapter.setMetarList(response.body().getData().getMetars());

                } else {
                    Log.d("AirportWeatherActivity", "response with error:" + response.body()
                            .getErrors());
                }
                metarCallComplete = true;
            }

            @Override
            public void onFailure(Call<MetarResponse> call, Throwable t) {
                Log.d("AirportWeatherActivity", t.toString());
                metarCallComplete = true;
            }
        });
    }

}
