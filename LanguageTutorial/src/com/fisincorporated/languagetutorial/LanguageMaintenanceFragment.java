package com.fisincorporated.languagetutorial;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fisincorporated.languagetutorial.utility.LanguageSettings;

// Just display status of last load/delete op
public class LanguageMaintenanceFragment extends MasterFragment {

	private TextView tv;

	// used to store values to SharedPreferences file
	private LanguageSettings languageSettings;

	// values used to hold maintenance status values
	private static int maintenanceType;
	private static int maintenanceStatus;
	private static String maintenanceDetails;

	private Resources res;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		languageSettings = LanguageSettings.getInstance(getActivity());
		res = getResources();
	}

	// Called (after onCreate) when the Fragment is attached to its parent
	// Activity.
	// Create, or inflate the Fragment's UI, and return it.
	// Wait till this point if fragment needs to interact with UI of parent
	// Activity
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.langauge_maint_status, container,
				false);
		tv = (TextView) view.findViewById(R.id.db_status);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateStatusDisplay();
	}

	public void updateStatusDisplay() {
		getMaintenanceStatus();
		displayLastTaskDetails();
		tv.invalidate();
	}

	private void getMaintenanceStatus() {
		maintenanceType = languageSettings.getMaintenanceType();
		maintenanceStatus = languageSettings.getMaintenanceStatus();
		maintenanceDetails = languageSettings.getMaintenanceDetails();
	}

	// display prior load/delete information
	// see if last was load or delete and display accordingly
	private void displayLastTaskDetails() {
		if (maintenanceType != -1) {
			tv.setText(String.format(
				res.getString(R.string.last_change),
				(maintenanceType == LanguageMaintenanceActivity.LOAD ? res.getString(R.string.load) : res
						.getString(R.string.delete)), maintenanceDetails,
				getMaintenanceStatusString(maintenanceStatus))) ; 
		}
		else {
				tv.setText(res.getString(
						R.string.no_language_maintence_done_yet,
						Environment.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_DOWNLOADS).getPath()
								+ "/" + res.getString(R.string.languagetutorial_txt)));
		 }
	}

	private String getMaintenanceStatusString(int status) {
		switch (status) {
		case GlobalValues.RUNNING:
			return res.getString(R.string.running);
		case GlobalValues.FINISHED_OK:
			return res.getString(R.string.finished_successfully);
		case GlobalValues.FINISHED_WITH_ERROR:
			return res.getString(R.string.finished_with_error);
			// case RESET:
			// return res.getString(R.string.reset);
		case GlobalValues.CANCELLED:
			return res.getString(R.string.cancelled);
		default:
			return String.format(
					res.getString(R.string.undefined_maintenance_status_value),
					status);
		}

	}

}