package com.fisincorporated.languagetutorial;

import com.fisincorporated.languagetutorial.interfaces.IAsyncCallBacks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class DeleteTeacherLanguageFragment extends Fragment {
	private static IAsyncCallBacks callBack;
	private String TAG = "DeleteTeacherLanguageTaskFragment";
	private TeacherLanguageDelete teacherLanguageDelete;
	private static DeleteTeacherLanguageFragment taskFragment = null;

 

	/**
	 * Hold a reference to the parent Activity so we can report the task's
	 * current progress and results. The Android framework will pass us a
	 * reference to the newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof IAsyncCallBacks){
			callBack = (IAsyncCallBacks) activity;
		}
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

	public static DeleteTeacherLanguageFragment getInstance() {
		if (taskFragment == null) {
			taskFragment = new DeleteTeacherLanguageFragment();
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
	
	

	public void startDeleteProcess(TeacherFromToLanguage teacherFromToLanguage) {
		// Free up any prior references and start fresh
		if (teacherLanguageDelete != null) {
			teacherLanguageDelete = null;
		}
		teacherLanguageDelete = new TeacherLanguageDelete(this, teacherFromToLanguage);
		teacherLanguageDelete.execute();
	}

	// Called by UI
	public void cancelTask() {
		if (teacherLanguageDelete != null) {
			teacherLanguageDelete.cancelTask(true);
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
