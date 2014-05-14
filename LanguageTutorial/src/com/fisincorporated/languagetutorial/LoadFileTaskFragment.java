package com.fisincorporated.languagetutorial;

import java.io.File;

import com.fisincorporated.languagetutorial.interfaces.IAsyncCallBacks;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

//http://stackoverflow.com/questions/8417885/android-fragments-retaining-an-asynctask-during-screen-rotation-or-configuratio
//http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html

// Non-UI (a.k.a. headless) fragment that holds reference to Aysnc LanguageFileLoaderAsync
public class LoadFileTaskFragment extends Fragment {
	// The task we are running.
	private static LanguageFileLoaderAsync languageFileLoader = null;
	private IAsyncCallBacks callBack;
	private static LoadFileTaskFragment taskFragment = null;
	private String TAG = "LoadFileTaskFragment";

	

	/**
	 * Hold a reference to the parent Activity so we can report the task's
	 * current progress and results. The Android framework will pass us a
	 * reference to the newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callBack = (IAsyncCallBacks) activity;
		Log.i(TAG, "Attached to activity");
	}

	/**
	 * Set the callback to null so we don't accidentally leak the Activity
	 * instance.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		callBack = null;
		Log.i(TAG, "Detached from activity");
	}

	public static LoadFileTaskFragment getInstance() {
		if (taskFragment == null) {
			taskFragment = new LoadFileTaskFragment();
		}
		return taskFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ****Retain this instance so it isn't destroyed when Activity and
		// fragment change configuration. ****
		setRetainInstance(true);
	}

	public void startFileLoad(File file) {
		// Free up any prior references and start fresh
		if (languageFileLoader != null){
			languageFileLoader = null;
		}
		languageFileLoader = new LanguageFileLoaderAsync(taskFragment, file);
		languageFileLoader.execute();
	}

	// Called by UI
	public void cancelTask() {
		if (languageFileLoader != null){
			languageFileLoader.cancelTask(true);
		}
	}

	public void onPreExecute() {
		if (callBack != null) {
			callBack.onPreExecute();
		}

	}

	// This is called by the AsyncTask.
	public void updateProgress(final int percent) {
		if (callBack != null) {
			callBack.onProgressUpdate(percent);
		}
	}

	// This is called by the file loader but must be on the UI thread
	public void taskFinished(int completionCode, String errorMsg) {
		if (callBack != null) {
			callBack.onPostExecute(completionCode, errorMsg);
		}
	}

}