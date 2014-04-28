package com.fisincorporated.languagetutorial;

import com.fisincorporated.languagetutorial.utility.LanguageSettings;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class OptionsFragment extends MasterFragment {
	private static final String  TAG = "OptionsFragment";
	protected static LanguageSettings languageSettings;
	protected static Resources res;
	private INotifyOptionChanged optionChangeCallback;
	// turn off notification of option changes when you (re)set them
	protected static boolean ignoreTextChanges = true;
	
	
	public interface INotifyOptionChanged {
		  void optionsChanged();
		  void optionsSaved();
		  void optionChangesCancelled();
	}
	
	public OptionsFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		Log.i(TAG, "Attached options fragment");
		optionChangeCallback = (INotifyOptionChanged) activity;
	}
	
	public void onDetach(){
		super.onDetach();
		Log.i(TAG, "Detached options fragment");
		optionChangeCallback = null;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getActivity().getResources();
		setRetainInstance(true);
		languageSettings = LanguageSettings.getInstance(getActivity());
		getOptionValues();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(getMainLayout(), container, false);
		// set action bar setTitle(res.getString(R.string.audio_options));
		getLayoutFields(view);
		setOptionDisplayValues();
		checkForUpdates();
		return view;
	}
	  
	protected abstract void getOptionValues() ;
	
	protected abstract int getMainLayout() ;
	
	protected abstract void getLayoutFields(View view);
		 
	public void onResume() {
		super.onResume();
//		setOptionDisplayValues();
//		checkForUpdates();
	}

	protected abstract void checkForUpdates(); 

	protected void notifyOptionsChanged(){
		optionChangeCallback.optionsChanged();
	}
	
	protected void notifyOptionsSaved(){
		// TODO prevent crash on orientation change as value goes to null
		// implement storage of updates and hold between orientation changes
		// then final check if returning without saving changes
		
		if (optionChangeCallback == null) return;
		optionChangeCallback.optionsSaved();
	}
	
	protected void notifyOptionChangesCancelled(){
		optionChangeCallback.optionChangesCancelled();
	}
	
	protected abstract void storeOptionSettings(); 
	
	protected abstract void setOptionDisplayValues();

	public abstract boolean isValidInput() ;


	
	
	
	
}
