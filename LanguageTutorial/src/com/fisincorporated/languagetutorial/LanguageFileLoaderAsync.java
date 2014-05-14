package com.fisincorporated.languagetutorial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.res.Resources;
import android.os.AsyncTask;

import com.fisincorporated.languagetutorial.interfaces.ILoadLessonCallBack;

// based on some logic from
//http://stackoverflow.com/questions/8417885/android-fragments-retaining-an-asynctask-during-screen-rotation-or-configuratio
public class LanguageFileLoaderAsync extends AsyncTask<Void, Integer, Boolean> implements ILoadLessonCallBack {
private static final String TAG = "LanguageFileLoaderAsync";
 
	private String message = null;
 
	private LoadFileTaskFragment loadFileTaskFragment;
	private File languageFile;

	private float languageFileSize = 0;
	private long numberFileCharsRead = 0;
	private int percentFileRead = 0;
	private boolean loadError = false;
	private boolean cancel = false;
	private Resources res;
	 
	
	LanguageLessonLoader languageLessonLoader;

	// The fragment passed in should be non-ui fragment
	public LanguageFileLoaderAsync(LoadFileTaskFragment fragment, File file) {
		loadFileTaskFragment = fragment;
		languageFile = file;
	}

	@Override
	protected void onPreExecute() {
		if (loadFileTaskFragment == null)
			return;
		loadFileTaskFragment.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// Need to wait till here to make sure fragment is attached to activity
		// before trying to get resources
		res = loadFileTaskFragment.getActivity().getResources();
		languageLessonLoader = new LanguageLessonLoader(loadFileTaskFragment.getActivity(), this);
		loadLanguageSpreadSheet(languageFile);
		return loadError || cancel;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		if (loadFileTaskFragment == null)
			return;
		loadFileTaskFragment.updateProgress(progress[0]);
	}

	@Override
	// The result value is coming from value returned at end of doInBackground
	protected void onPostExecute(Boolean result) {
		if (loadFileTaskFragment == null)
			return;
		// pass back result code cancelled, error or ok plus message (if any)
		loadFileTaskFragment.taskFinished(cancel ? GlobalValues.CANCELLED
				: (loadError ? GlobalValues.FINISHED_WITH_ERROR
						: GlobalValues.FINISHED_OK), message);
	}

	// call back with error message
	// Save the error msg for passing back on onPostExecute - and via UI thread
	public void passbackMessage(String message, boolean error) {
		this.message = message;
		loadError = error;
	}

 
	public void cancelTask(boolean cancel) {
		this.cancel = cancel;
	}
	
	public void loadLanguageSpreadSheet(File file) {
		BufferedReader br = null;
		int offset;
		// Read text from file
		try {
			languageFileSize = file.length();
			numberFileCharsRead = 0;
			// read unicode properly
			// first byte in file appears to be byte order mark (BOM)
			// http://en.wikipedia.org/wiki/Byte_order_mark
			// so on first read drop the BOM
//			br = new BufferedReader(new InputStreamReader(
//					new FileInputStream(file), "UTF-16LE"));
			// Spreadsheet file now created with UTF-8 which is default for java so don't specify
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null && (!loadError) && (!cancel)) {
				numberFileCharsRead += line.length();
				if (languageFileSize > 0) {
					percentFileRead = (int) ((numberFileCharsRead / (float) languageFileSize) * 100);
					publishProgress(percentFileRead);
				}
				// Windows UTF-16LE has some non-text char at start of file so account for it
				// Lesson file now written in utf8 so may not need to worry about offset but leaving in for now
				if (i == 0 ) {
					 offset = line.indexOf("Teacher");
					 if (offset == -1){
						 passbackMessage(res.getString(R.string.first_record_not_teacher_record,line),true);
						 return;
					 }
					line = line.substring(offset);
				}
				languageLessonLoader.processLine(line.trim());
				++i;
				if (cancel) {
					break;
				}
			}
		} catch (IOException e) {
			passbackMessage(String.format(
					res.getString(R.string.error_reading_language_file),
					file.getAbsolutePath()), true);
		} catch (Exception e) {
			passbackMessage(res.getString(R.string.unexpected_exception)
					+ " " + e.toString(), true);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					;
				}
			}
		}
	}



}
