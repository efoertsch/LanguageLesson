package com.fisincorporated.wearable;

import android.databinding.ObservableArrayList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class PatientViewModel {

    private ObservableArrayList<Patient> patientList = new ObservableArrayList<>();

    public PatientViewModel(View view) {

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.patient_recyclerView);
        setupRecyclerView(recyclerView);
        addPatients();

        PatientAdapter patientAdapter = new PatientAdapter(patientList);
        recyclerView.setAdapter(patientAdapter);
    }

    public void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    private void addPatients() {
        Patient patient = new Patient("John Doe", "120/80", 92);
        patientList.add(patient);

        patient = new Patient("Jane Smith", "110/70", 72);
        patientList.add(patient);

        patient = new Patient("Iam Critical", "90/60", 52);
        patientList.add(patient);

        patient = new Patient("Iam Lazarus", "120/70", 62);
        patientList.add(patient);

        patient = new Patient("Young Blood", "130/80", 72);
        patientList.add(patient);

    }
}
