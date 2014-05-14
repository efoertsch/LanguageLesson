package com.fisincorporated.languagetutorial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.fisincorporated.languagetutorial.interfaces.IAsyncCallBacks;
import com.fisincorporated.languagetutorial.interfaces.IDialogResultListener;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

// Used logic from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html and modified as needed
// Control load/delete for languages plus display status of last language op
// Now that this is done this whole process (Activity/Headless fragments/Asynctasks) logic should probably be rewritten
// For LoadLanguageLessonService
// http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

public class LanguageMaintenanceActivity extends MasterActivity implements
		IAsyncCallBacks, IDialogResultListener {

	// Tag so we can find the task fragment again should there be an orientation
	// change
	static final String LOAD_FILE_TASK_FRAGMENT = "LoadFileTaskFragment";
	private static final String DELETE_TEACHER_TASK_FRAGMENT = "DeleteTeacherTaskFragment";

	private LanguageDialogFragment dialog;
	private boolean loadError = false;

	private LanguageMaintenanceFragment languageMaintenanceFragment = null;
	private LoadFileTaskFragment loadFileTaskFragment;
	private DeleteTeacherLanguageFragment deleteTeacherLanguageFragment = null;
	private static Menu myMenu;
	private ProgressDialog progressDialog = null;

	private static final String TEACHER = "Teacher";
	private static final String TEACHER_LANGUAGE = "TeacherLanguage";
	private static final String PIPE_DELIMITER = "\\|";
	//
	private static String lineSeparator = "\n";
	// Requestcodes for maintenance status and Dialog return codes
	private static final int LOAD_ERROR = 1;
	private static final int CONFIRM_LOAD = 2;
	private static final int LOAD_CANCELLED = 3;
	private static final int LOAD_SUCCESSFUL = 4;
	private static final int CONFIRM_DELETE = 5;
	private static final int DELETE_CANCELLED = 6;
	private static final int DELETE_SUCCESSFUL = 7;
	private static final int SELECT_TEACHER_LANGUAGE_TO_DELETE = 8;
	private static final int CANCEL_OP = 9;
	private static final int CONFIRM_WEB_LOAD = 10;

	// //Maintenance type being performed used with MAINTENANCE_TYPE below
	// public static final int LOAD = 1;
	// public static final int DELETE = 2;

	private static int selectedOp;
	private File languageFile = null;
	private StringBuilder maintOpDetails = new StringBuilder();

	private TeacherFromToLanguage teacherFromToLanguage;
	private String formattedString;
	private String learningLanguageMediaDirectory = "";
	private String knownLanguageMediaDirectory = "";

	// used to store values to SharedPreferences file
	private LanguageSettings languageSettings;
	// values used to hold maintenance status values
	private static int maintenanceType;
	private static int maintenanceStatus;

	private Resources res;
	private long teacherId;
	// private long teacherLanguageId;
	// private String teacherName;
	// private String teacherLanguageTitle;
	private TeacherLanguageSelectDialog teacherLanguageSelectDialog;
	private String loadFileUrl = "";

	// added for tablet

	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}

	@Override
	protected Fragment createFragment() {
		languageMaintenanceFragment = new LanguageMaintenanceFragment();
		return languageMaintenanceFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		actionBar = getSupportActionBar();
		actionBar.setTitle(res.getString(R.string.language_maintenance));
		languageSettings = LanguageSettings.getInstance(this);

		FragmentManager fm = getSupportFragmentManager();
		List<Fragment> list = fm.getFragments();
		if (list != null) {
			for (int i = 0; i < list.size(); ++i) {
				if (list.get(i) instanceof LanguageMaintenanceFragment){
					languageMaintenanceFragment = (LanguageMaintenanceFragment) list
							.get(i);
				break;
				}
			}
		}
	}

	private void createLoadFileTaskFragment() {
		FragmentManager fm = getSupportFragmentManager();
		// see if fragment was already created
		loadFileTaskFragment = (LoadFileTaskFragment) fm
				.findFragmentByTag(LOAD_FILE_TASK_FRAGMENT);
		// If the Fragment is non-null, then it is currently being
		// retained across a configuration change.
		if (loadFileTaskFragment == null) {
			loadFileTaskFragment = LoadFileTaskFragment.getInstance();
			fm.beginTransaction()
					.add(loadFileTaskFragment, LOAD_FILE_TASK_FRAGMENT).commit();
		}
	}

	private void createDeleteTeacherLanguageFragment() {
		FragmentManager fm = getSupportFragmentManager();
		deleteTeacherLanguageFragment = (DeleteTeacherLanguageFragment) fm
				.findFragmentByTag(DELETE_TEACHER_TASK_FRAGMENT);
		if (deleteTeacherLanguageFragment == null) {
			deleteTeacherLanguageFragment = DeleteTeacherLanguageFragment
					.getInstance();
			fm.beginTransaction()
					.add(deleteTeacherLanguageFragment, DELETE_TEACHER_TASK_FRAGMENT)
					.commit();
		}
	}

	// The interface methods below are called by the LoadFileTaskFragment when
	// new progress updates or results are available. Call dialog and
	// LanguageMaintenanceFragment to update UI's
	// IAsyncCallBacks method
	@Override
	public void onPreExecute() {
		saveBackgroundTaskDetails(selectedOp, GlobalValues.RUNNING,
				maintOpDetails.toString());
		updateMenuAndStatusDisplay();
		if (progressDialog == null) {
			showProgressDialog();
		}
	}

	// IAsyncCallBacks method
	@Override
	public void onProgressUpdate(final int percent) {
		if (progressDialog == null) {
			showProgressDialog();
		}
		progressDialog.setProgress(percent);

	}

	// IAsyncCallBacks method
	@Override
	public void onPostExecute(int completionCode, String errorMsg) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		maintenanceStatus = completionCode;
		languageSettings.setMaintenanceStatus(maintenanceStatus).commit();
		// updateMenuOptions();
		if (completionCode == GlobalValues.CANCELLED) {
			showDialog(
					res.getString((selectedOp == GlobalValues.LOAD) ? R.string.load_cancelled
							: R.string.delete_cancelled),
					(selectedOp == GlobalValues.LOAD) ? LOAD_CANCELLED
							: DELETE_CANCELLED, R.string.ok, -1, -1);
		} else if (completionCode == GlobalValues.FINISHED_WITH_ERROR) {
			showErrorDialog(errorMsg);

		} else if (completionCode == GlobalValues.FINISHED_OK) {
			showDialog(
					res.getString((selectedOp == GlobalValues.LOAD) ? R.string.language_file_loaded_successfully
							: R.string.deletion_completed_successfully),
					(selectedOp == GlobalValues.LOAD) ? LOAD_SUCCESSFUL
							: DELETE_SUCCESSFUL, R.string.ok, -1, -1);
		}
		updateMenuAndStatusDisplay();
	}

	// end of interface methods

	private void showProgressDialog() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// progressDialog.setMessage("Loading...");
		progressDialog.setProgress(0);
		progressDialog.setMax(100);
		// progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(new OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				// back button pressed
				showDialog(res
						.getString(R.string.do_you_want_to_cancel_current_operation),
						CANCEL_OP, R.string.cancel, R.string.continuex, -1);

			}
		});
		progressDialog.setCancelable(true);

		progressDialog.show();
	}

	private void updateMenuAndStatusDisplay() {
		supportInvalidateOptionsMenu();
		// tell LanguageMaintenanceFragment to update status display
		if (languageMaintenanceFragment != null) {
			languageMaintenanceFragment.updateStatusDisplay();
		}
	}

	// Add the menu - Will add to any menu items added by parent activity
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Add the menu
		getMenuInflater().inflate(R.menu.language_db_options, menu);
		myMenu = menu;
		updateMenuOptions();
		return true;
	}

	// Note that onCreateOptionsMenu not called until menu needed so check for
	// null myMenu first
	private void updateMenuOptions() {
		if (myMenu != null) {
			boolean isTaskRunning = checkForMaintenanceTask();
			myMenu.findItem(R.id.load_language_file).setVisible(!isTaskRunning);
			myMenu.findItem(R.id.delete_teacher_language_class).setVisible(
					!isTaskRunning);
			myMenu.findItem(R.id.cancel_task).setVisible(isTaskRunning);
			// myMenu.findItem(R.id.reset_background_task).setVisible(isTaskRunning);
		}
	}

	// handle the selected menu option
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.load_from_this_device:
			loadError = false;
			selectedOp = GlobalValues.LOAD;
			checkFileAndConfirmDetails();
			return true;
		case R.id.load_from_web:
			loadError = false;
			selectedOp = GlobalValues.LOAD;
			loadFromWeb();
			return true;
		case R.id.delete_teacher_language_class:
			// display teacher/language dialog and get what to delete
			// and go ahead and delete it
			selectedOp = GlobalValues.DELETE;
			showTeacherLanguageSelectDialog();
			return true;
		case R.id.cancel_task:
			if (loadFileTaskFragment != null) {
				loadFileTaskFragment.cancelTask();
			}
			languageSettings.setMaintenanceStatus(GlobalValues.CANCELLED).commit();
			updateMenuAndStatusDisplay();
			return true;
			// case R.id.reset_background_task:
			// maintenanceStatus = RESET;
			// cancel = true;
			// languageSettings.setMaintenanceStatus(maintenanceStatus);
			// languageSettings.commit();
			// displayLastTaskDetails();
			// return true;

		case R.id.backup_restore_language_database:
			Intent intent = new Intent(this, BackupRestoreActivity.class);
			startActivity(intent);
			return true;
		default:
			// pass up to superclass
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateMenuOptions();
		updateMenuAndStatusDisplay();
//		if (maintenanceType != -1) {
//			if (languageMaintenanceFragment != null) {
//				languageMaintenanceFragment.updateStatusDisplay();
//			}
//		}
		LocalBroadcastManager
				.getInstance(this)
				.registerReceiver(
						webLoadReceiver,
						new IntentFilter(
								LoadLanguageLessonService.LOAD_LANGUAGE_LESSON_SERVICE_UPDATE));

	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				webLoadReceiver);
	}

	@Override
	public void onStop() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		super.onStop();
	}

	private boolean checkForMaintenanceTask() {
		maintenanceType = languageSettings.getMaintenanceType();
		maintenanceStatus = languageSettings.getMaintenanceStatus();
		if (maintenanceStatus == GlobalValues.RUNNING)
			return true;
		return false;
	}

	private void checkFileAndConfirmDetails() {
		languageFile = checkForFile();
		if (languageFile == null) {
			return;
		}
		if (confirmFileDetails(languageFile)) {
			showDialog(res.getString(R.string.language_file_details_to_confirm,
					maintOpDetails), CONFIRM_LOAD, R.string.load, R.string.cancel,
					-1);
			// created fragment here as onAttach in fragment happening after
			// onPreExecute() from asynctask (I am guessing that onPreExecute is
			// still on UI thread)
			createLoadFileTaskFragment();
		}
	}

	private File checkForFile() {
		// Find the directory for the SD Card using the API
		// *Don't* hardcode "/sdcard"
		String filename = res.getString(R.string.languagelesson_txt);
		File sdcard = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		// Get the text file
		File file = new File(sdcard, filename);
		if (!file.exists()) {
			formattedString = String.format(
					res.getString(R.string.languagetutorial_txt_not_found),
					filename, sdcard.toString());
			languageSettings.setMaintenanceDetails(formattedString);
			showErrorDialog(formattedString);

			return null;
		}
		return file;
	}

	// Read first 2 lines of file to get details for display and confirmation
	private boolean confirmFileDetails(File file) {
		BufferedReader br = null;
		maintOpDetails.setLength(0);
		int linesFound = 0;
		// Read text from file
		try {
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
			while ((line = br.readLine()) != null && (!loadError) && i < 2) {
				line = (i == 0 ? line.substring(1).trim() : line.trim());
				linesFound = linesFound + getFileDetails(line, maintOpDetails);
				++i;
			}
		} catch (IOException ioe) {
			showErrorDialog(res.getString(R.string.error_reading_language_file,
					file.getAbsolutePath()));

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					showErrorDialog(res.getString(
							R.string.error_reading_language_file, ioe.toString()));
				}
			}
		}
		if (linesFound == 2)
			return true;
		else
			return false;

	}

	// get details to confirm this is file to load
	// must read a Teacher and TeacherLanguage line to get details, if not the
	// first 2 lines of file
	// then something wrong
	private int getFileDetails(String line, StringBuilder sb) {
		learningLanguageMediaDirectory = "";
		knownLanguageMediaDirectory = "";
		int i = 0;
		String[] tokens = parseLine(line);
		if (0 == tokens[0].compareTo(TEACHER)) {
			sb.append(lineSeparator);
			sb.append(res.getText(R.string.teacher) + ":" + tokens[1]
					+ lineSeparator + lineSeparator);
			return ++i;
		}
		if (0 == tokens[0].compareTo(TEACHER_LANGUAGE)) {
			sb.append(res.getText(R.string.language_to_be_learned) + ":"
					+ tokens[1] + lineSeparator);
			learningLanguageMediaDirectory = Environment.DIRECTORY_DOWNLOADS + "/"
					+ tokens[2];
			sb.append(res.getText(R.string.media_files_directory) + ":"
					+ learningLanguageMediaDirectory + lineSeparator + lineSeparator);
			sb.append(res.getText(R.string.known_language) + ":" + tokens[3]
					+ lineSeparator);
			knownLanguageMediaDirectory = Environment.DIRECTORY_DOWNLOADS + "/"
					+ tokens[4];
			sb.append(res.getText(R.string.media_files_directory) + ":"
					+ knownLanguageMediaDirectory);
			return ++i;
		}
		return i;

	}

	private String[] parseLine(String line) {
		// http://blog.mgm-tp.com/2012/05/regexp-java-puzzler/ to get empty
		// columns after last non-empty column
		String[] tokens = line.split(PIPE_DELIMITER, -1);
		return tokens;
	}

	private boolean checkCreateMediaDirectories() {
		boolean success = true;
		if (!learningLanguageMediaDirectory.equals("")) {
			success = createMediaDirectory(learningLanguageMediaDirectory);
		}
		if (success && !knownLanguageMediaDirectory.equals("")) {
			success = createMediaDirectory(knownLanguageMediaDirectory);
		}
		return success;
	}

	private boolean createMediaDirectory(String languageMediaDirectory) {
		boolean success = true;
		// see if directory exists
		File dir = new File(Environment.getExternalStorageDirectory() + "/"
				+ languageMediaDirectory);
		if (dir.exists() && !dir.isDirectory()) {
			// trouble - name exists but is not a directory.
			// display error, and exit
			showErrorDialog(res
					.getString(R.string.media_directory_not_a_directory,
							languageMediaDirectory));
			return false;
		}
		// directory doesn't exist so create it
		if (!dir.exists()) {
			success = dir.mkdirs();
			if (!success) {
				showErrorDialog(res.getString(
						R.string.media_directory_could_not_be_created,
						languageMediaDirectory));
			}
		}
		return success;
	}

	private void showTeacherLanguageSelectDialog() {
		// display DeleteTeacherLanguageDialog
		teacherLanguageSelectDialog = TeacherLanguageSelectDialog.newInstance(
				-1l, -1l);
		teacherLanguageSelectDialog.setOnDialogResultListener(this,
				SELECT_TEACHER_LANGUAGE_TO_DELETE);
		teacherLanguageSelectDialog.show(this.getSupportFragmentManager(),
				"selectTeacherLanguage");

	}

	private void saveBackgroundTaskDetails(int maintenanceType,
			int maintenanceStatus, String details) {
		languageSettings.setMaintenanceType(maintenanceType)
				.setMaintenanceStatus(maintenanceStatus)
				.setMaintenanceDetails(details).commit();
	}

	private void showErrorDialog(String message) {
		languageSettings.setMaintenanceStatus(GlobalValues.FINISHED_WITH_ERROR);
		languageSettings.commit();
		showDialog(message, LOAD_ERROR, R.string.ok, -1, -1);
	}

	public void showDialog(String loadMsg, int requestCode, int yesResource,
			int noResource, int cancelResource) {
		dialog = LanguageDialogFragment.newInstance(-1, loadMsg, yesResource,
				noResource, cancelResource);
		dialog.setOnDialogResultListener(this, requestCode);
		dialog.show(this.getSupportFragmentManager(), "confirmDialog");
	}

	private void loadFromWeb() {
		// first check to make sure have web access turned on
		// display dialog to get Web URL from which to load file
		dialog = LanguageDialogFragment.newInstance(
				R.layout.load_lesson_from_web, R.id.tvLessonWebURL,
				res.getString(R.string.demo_lesson_url),
				R.string.load_language_lesson_from_web,
				res.getString(R.string.enter_lesson_web_url), R.string.load,
				R.string.cancel, -1);
		dialog.setOnDialogResultListener(this, GlobalValues.LOAD_FROM_WEB);
		dialog.show(this.getSupportFragmentManager(), "loadFileURLDialog");

	}

	@Override
	public void onDialogResult(int requestCode, int resultCode,
			int buttonPressed, Bundle bundle) {
		if (requestCode == LOAD_ERROR) {
			// doesn't matter what is pressed, end this but status probably updated
			// so
			updateMenuAndStatusDisplay();
		}
		if (requestCode == CONFIRM_LOAD) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					startLoadProcess();
				}
				return;
			}
		}
		if (requestCode == SELECT_TEACHER_LANGUAGE_TO_DELETE) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			}
			if (bundle == null) {
				return;
			}
			// save delete details
			teacherFromToLanguage = bundle
					.getParcelable(GlobalValues.TEACHER_FROM_TO_LANGUAGE);
			teacherId = teacherFromToLanguage.getTeacherId();
			if (teacherId == -1)
				return;
			showDialog(res.getString(
					R.string.delete_teacher_language_confirmation,
					teacherFromToLanguage.getTeacherName(),
					teacherFromToLanguage.getLearningLanguageName(),
					teacherFromToLanguage.getKnownLanguageName()), CONFIRM_DELETE,
					R.string.delete, R.string.cancel, -1);
			// created fragment here as onAttach in fragment happening after
			// onPreExecute() from asynctask (I am guessing that onPreExecute is
			// still on UI thread)
			createDeleteTeacherLanguageFragment();
			return;
		}
		if (requestCode == CONFIRM_DELETE) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					startDeleteProcess();
				}
			}
			return;
		}
		if (requestCode == CANCEL_OP) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					if (selectedOp == GlobalValues.LOAD)
						loadFileTaskFragment.cancelTask();
					else {
						deleteTeacherLanguageFragment.cancelTask();
					}
				} else {
					progressDialog.show();

				}
			}
			return;
		}
		if (requestCode == GlobalValues.LOAD_FROM_WEB) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					// get the load file URL from bundle
					if (bundle != null) {
						loadFileUrl = bundle
								.getString(LanguageDialogFragment.LANGUAGE_DIALOG_TEXT_ENTRY);
						if (!loadFileUrl.equals("")) {
							// display confirmation dialog
							showDialog(res.getString(
									R.string.languge_file_to_be_loaded_from_url,
									loadFileUrl), CONFIRM_WEB_LOAD, R.string.load,
									R.string.cancel, -1);
						} else {
							showDialog(
									res.getString(R.string.no_url_entered_loading_cancelled),
									LOAD_ERROR, R.string.ok, -1, -1);
						}
					}
				}
			}
			return;
		}
		if (requestCode == CONFIRM_WEB_LOAD) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					startLoadFromURL();
				}
			}
			return;
		}
	}

	private void startDeleteProcess() {
		updateMenuOptions();
		maintOpDetails.setLength(0);
		maintOpDetails.append(res.getString(R.string.delete_details,
				teacherFromToLanguage.getTeacherName(),
				teacherFromToLanguage.getLearningLanguageName(),
				teacherFromToLanguage.getKnownLanguageName()));
		deleteTeacherLanguageFragment.startDeleteProcess(teacherFromToLanguage);

	}

	private void startLoadProcess() {
		updateMenuOptions();
		if (checkCreateMediaDirectories()) {
			loadFileTaskFragment.startFileLoad(languageFile);
		}
	}

	public void onBackPressed() {
		if (languageSettings.getMaintenanceStatus() == GlobalValues.RUNNING) {
			showDialog(
					res.getString(R.string.do_you_want_to_cancel_current_operation),
					CANCEL_OP, R.string.cancel, R.string.continuex, -1);
		} else
			finish();
	}

	private void startLoadFromURL() {
		// request the service to start
		// getActive NetworkInfo() requires ACCESS_NETWORK_STATE permission in
		// manifest
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		boolean isNetworkAvailable = cm.getBackgroundDataSetting()
				&& cm.getActiveNetworkInfo() != null;
		if (!isNetworkAvailable) {
			showDialog(res.getString(R.string.network_not_available), LOAD_ERROR,
					R.string.ok, -1, -1);
			return;
		}

		// receiver should be registered before this is called
		Intent intent = new Intent(this, LoadLanguageLessonService.class);
		intent.putExtra(LoadLanguageLessonService.LANGUAGE_LESSON_URL,
				loadFileUrl);
		startService(intent);

	}

	// Handle the load update - basically tell the LanguageMaintenanceFragment to
	// update display
	private BroadcastReceiver webLoadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// currently not expecting any data in intent
			// tell fragment to update status display
			updateMenuAndStatusDisplay();
		}
	};

}
