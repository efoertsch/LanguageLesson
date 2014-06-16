package com.fisincorporated.languagetutorial;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fisincorporated.languagetutorial.utility.FileUtil;
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
			tv.setText( res.getString(R.string.last_change,
				getMaintenanceOpString(), maintenanceDetails,
				getMaintenanceStatusString(maintenanceStatus))) ; 
		}
		else {
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			tv.setText(Html.fromHtml(FileUtil.readAssetsText(getActivity(), "firstload.txt")));
 		 }
			
	}
	
	// maintenanceType must be properly assigned prior to calling this routinue.
	private String getMaintenanceOpString(){
		switch (maintenanceType){
		case  GlobalValues.LOAD:
			return res.getString(R.string.load_from_this_device); 
		case GlobalValues.LOAD_FROM_WEB:
			return res.getString(R.string.load_from_web);
		case GlobalValues.DELETE:
			return res.getString(R.string.delete);
		default:
			return res.getString(R.string.unspecified_maintenance_opcode,maintenanceType );
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
