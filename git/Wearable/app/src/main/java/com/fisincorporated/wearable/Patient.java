package com.fisincorporated.wearable;


import android.databinding.BaseObservable;
import android.databinding.Bindable;

public class Patient  extends BaseObservable {
    private String name;
    private String bp;
    private int pulse;

    public Patient(String name, String bp, int pulse){
        this.name = name;
        this.bp = bp;
        this.pulse = pulse;
    };

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
        notifyPropertyChanged(BR.bp);
    }

    @Bindable
    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
        notifyPropertyChanged(BR.pulse);
    }
}
