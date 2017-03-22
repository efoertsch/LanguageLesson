package com.fisincorporated.wearable;


import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.wearable.databinding.PatientLayoutBinding;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {
    private ObservableArrayList<Patient> patientList = new ObservableArrayList<>();


    public PatientAdapter(List<Patient> patientList) {
        this.patientList.addAll(patientList);
    }

    @Override
    public PatientAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PatientLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false);
        return new PatientAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(PatientAdapter.ViewHolder holder, int position) {
        // Note that binding is direct to StationControl via it's interface (as opposed to having ViewModel)
        holder.binding.setPatient(patientList.get(position));

    }


    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected static final int LAYOUT_RESOURCE = R.layout.patient_layout;

        private PatientLayoutBinding binding;

        public ViewHolder(PatientLayoutBinding bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }


}
