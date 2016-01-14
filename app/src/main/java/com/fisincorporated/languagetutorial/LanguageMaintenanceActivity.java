package com.fisincorporated.languagetutorial;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import com.fisincorporated.languagetutorial.interfaces.IDialogResultListener;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

// Used logic from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html and modified as needed
// Control load/delete for languages plus display status of last language op
// http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

public class LanguageMaintenanceActivity extends MasterActivity implements
		IDialogResultListener {
	public static final String LOAD_FROM_BUNDLE = "com.fisincorporated.languagetutorial.LoadFromBundle";
	// Tag so we can find the task fragment again should there be an orientation
	// change
	// static final String LOAD_FILE_TASK_FRAGMENT = "LoadFileTaskFragment";
	// private static final String DELETE_TEACHER_TASK_FRAGMENT =
	// "DeleteTeacherTaskFragment";

	private LanguageDialogFragment dialog;
	private boolean loadError = false;

	private LanguageMaintenanceFragment languageMaintenanceFragment = null;
	private static Menu myMenu;
	private ProgressDialog progressDialog = null;

	private static final String TEACHER = "Teacher";
	private static final String TEACHER_LANGUAGE = "TeacherLanguage";
	private static final String PIPE_DELIMITER = "\\|";
	//
	private static String lineSeparator = "\n";
	// Requestcodes for maintenance status and Dialog return codes
	private static final int LOAD_ERROR = 10;
	private static final int CONFIRM_LOAD = 11;
	private static final int LOAD_CANCELLED = 12;
	private static final int LOAD_SUCCESSFUL = 13;
	private static final int CONFIRM_DELETE = 14;
	private static final int DELETE_CANCELLED = 15;
	private static final int DELETE_SUCCESSFUL = 16;
	private static final int SELECT_TEACHER_LANGUAGE_TO_DELETE = 17;
	private static final int CANCEL_OP = 18;
	private static final int CONFIRM_WEB_LOAD = 19;
	private static final int CONFIRM_WEB_LOAD_FROM_BUNDLE = 20;

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

	private String loadFileUrl = "";
	private String loadFileDevice = "";
	private String bundleFileUrl = null;
	private String statusMessage;
	private int percentComplete;

	// added for tablet

	protected int getLayoutResId() {
		//return R.layout.activity_masterdetail;
		return R.layout.activity_fragment;
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
		// actionBar = getSupportActionBar();
		// actionBar.setTitle(res.getString(R.string.language_maintenance));
		languageSettings = LanguageSettings.getInstance(this);
		checkForLoadRequest();
		FragmentManager fm = getSupportFragmentManager();
		List<Fragment> list = fm.getFragments();
		if (list != null) {
			for (int i = 0; i < list.size(); ++i) {
				if (list.get(i) instanceof LanguageMaintenanceFragment) {
					languageMaintenanceFragment = (LanguageMaintenanceFragment) list
							.get(i);
					break;
				}
			}
		}
	}

	private void checkForLoadRequest() {
		bundleFileUrl = getIntent().getStringExtra(LOAD_FROM_BUNDLE);
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
			loadFromDevice();
			return true;
		case R.id.load_from_web:
			loadError = false;
			selectedOp = GlobalValues.LOAD_FROM_WEB;
			loadFromWeb();
			return true;
		case R.id.delete_teacher_language_class:
			// display teacher/language dialog and get what to delete
			// and go ahead and delete it
			selectedOp = GlobalValues.DELETE;
			showTeacherLanguageSelectDialog();
			return true;
		case R.id.cancel_task:
			showDialog(
					res.getString(R.string.do_you_want_to_cancel_current_operation),
					CANCEL_OP, R.string.cancel, R.string.continuex, -1);
			return true;
//		case R.id.backup_restore_language_database:
//			Intent intent = new Intent(this, BackupRestoreActivity.class);
//			startActivity(intent);
//			return true;
		default:
			// pass up to superclass
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		checkForBundleLoadRequest();
		updateMenuOptions();
		updateMenuAndStatusDisplay();
		registerBroadcastReceiver();

	}

	private void checkForBundleLoadRequest() {
		if (bundleFileUrl != null) {
			// ensure cancel is done
			//cancelService();
			// then display confirm dialog
			showDialog(res.getString(R.string.languge_file_to_be_loaded_from_url,
					bundleFileUrl), CONFIRM_WEB_LOAD_FROM_BUNDLE, R.string.load,
					R.string.cancel, -1);

		}
	}

	private void registerBroadcastReceiver() {
		LocalBroadcastManager
				.getInstance(this)
				.registerReceiver(
						webLoadReceiver,
						new IntentFilter(
								LanguageMaintenanceService.LOAD_LANGUAGE_LESSON_SERVICE_UPDATE));
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

	// public void onBackPressed() {
	// if (languageSettings.getMaintenanceStatus() == GlobalValues.RUNNING) {
	// showDialog(
	// res.getString(R.string.do_you_want_to_cancel_current_operation),
	// CANCEL_OP, R.string.cancel, R.string.continuex, -1);
	// } else
	// finish();
	// }

	private boolean checkForMaintenanceTask() {
		maintenanceType = languageSettings.getMaintenanceType();
		maintenanceStatus = languageSettings.getMaintenanceStatus();
		if (maintenanceStatus == GlobalValues.RUNNING)
			return true;
		return false;
	}

	private void loadFromDevice() {
		// display dialog to get/confirm languagefile to load from device
		// use demo file as default
		dialog = LanguageDialogFragment.newInstance(
				R.layout.load_lesson_from_device, R.id.tvLessonFilename,
				res.getString(R.string.languagelesson_txt),
				R.string.load_from_this_device,
				res.getString(R.string.enter_lesson_file_name), R.string.load,
				R.string.cancel, -1);
		dialog.setOnDialogResultListener(this, GlobalValues.LOAD);
		dialog.show(this.getSupportFragmentManager(), "loadFileDeviceDialog");

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
		}
	}

	private File checkForFile() {
		// Find the directory for the SD Card using the API
		// *Don't* hardcode "/sdcard"
		String filename = loadFileDevice;
		// this is OK for pre 4.4 
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
			// br = new BufferedReader(new InputStreamReader(
			// new FileInputStream(file), "UTF-16LE"));
			// Spreadsheet file now created with UTF-8 which is default for java so
			// don't specify
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

	// For 'manual load'
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
			if (!tokens[2].equals("")) {
				learningLanguageMediaDirectory = Environment.DIRECTORY_DOWNLOADS
						+ "/" + tokens[2];
				sb.append(res.getText(R.string.media_files_directory) + ":"
						+ learningLanguageMediaDirectory + lineSeparator
						+ lineSeparator);
				sb.append(res.getText(R.string.known_language) + ":" + tokens[3]
						+ lineSeparator);
				knownLanguageMediaDirectory = Environment.DIRECTORY_DOWNLOADS + "/"
						+ tokens[4];
				sb.append(res.getText(R.string.media_files_directory) + ":"
						+ knownLanguageMediaDirectory);
			}
			// if no learning media directory defined either an error or all the
			// media needs to be read from some website (eg. Youtube)
			else {
				sb.append(res.getText(R.string.no_learning_media_directory_defined)
						+ lineSeparator + lineSeparator);
			}
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

	// TODO - refactor along with similar logic in LanguageMaintenanceService.getMediaDirectory()
	private boolean createMediaDirectory(String languageMediaDirectory) {
		boolean success = true;
		// see if directory exists
//		File dir = new File(Environment.getExternalStorageDirectory() + "/"
//				+ languageMediaDirectory);
		File dir = new File( getFilesDir() + "/"	+ languageMediaDirectory); 
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
			else {
				languageSettings.setMediaDirectory(getFilesDir().getPath()).commit();
			}
		}
		return success;
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

	// display DeleteTeacherLanguageDialog
	private void showTeacherLanguageSelectDialog() {
		LessonSelectionDialog deleteTeacherLanguageDialog = LessonSelectionDialog
				.newInstance(LessonSelectionDialog.TEACHER_LANGUAGE_DELETE, -1);
		deleteTeacherLanguageDialog.setOnDialogResultListener(this,
				SELECT_TEACHER_LANGUAGE_TO_DELETE);
		deleteTeacherLanguageDialog.show(this.getSupportFragmentManager(),
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

	// At some point put into a switch statement and break up sublogic into
	// separate methods
	@Override
	public void onDialogResult(int requestCode, int resultCode,
			int buttonPressed, Bundle bundle) {
		if (requestCode == LOAD_ERROR) {
			// doesn't matter what is pressed, end this but status probably updated
			// so
			updateMenuAndStatusDisplay();
			return;
		}
		// -----------------------------------------------------------------------------------
		// This is for Load from device
		if (requestCode == GlobalValues.LOAD) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					// get the load file name
					if (bundle != null) {
						loadFileDevice = bundle
								.getString(LanguageDialogFragment.LANGUAGE_DIALOG_TEXT_ENTRY);
						if (loadFileDevice != null && !loadFileDevice.equals("")) {
							checkFileAndConfirmDetails();
						} else {
							showDialog(
									res.getString(R.string.no_file_name_entered_loading_cancelled),
									LOAD_ERROR, R.string.ok, -1, -1);
						}
					}
				}
			}
			return;
		}
		if (requestCode == CONFIRM_LOAD) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					startLoadFromDevice();
				}
				return;
			}
		}
		// -----------------------------------------------------------------------------------
		// This is for Load from the web
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
		// -----------------------------------------------------------------------------------
		// This is for Load from the web but the request was initiated by the url
		// that came in
		// on a bundle in onCreate
		if (requestCode == CONFIRM_WEB_LOAD_FROM_BUNDLE) {
			if (resultCode != Activity.RESULT_OK) {
				// if not ok return from whence you came
				finish();
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					loadFileUrl = bundleFileUrl;
					selectedOp = GlobalValues.LOAD_FROM_WEB;
					startLoadFromURL();
					// this is so if there is an orientation change, onResume won't display the load dialog again.
					getIntent().putExtra(LOAD_FROM_BUNDLE, (String) null);
				}
			}
			return;
		}
		// -----------------------------------------------------------------------------------
		// This is for deleting a teacher/language
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
		// -----------------------------------------------------------------------------------
		// This is to cancel whatever you were doing
		if (requestCode == CANCEL_OP) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else {
				if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
					// service may or may not be running
					cancelService();
				} else {
					if (progressDialog != null) {
						progressDialog.show();
					}
				}
			}
			return;
		}

	}

	private void cancelService() {
		languageSettings.setMaintenanceStatus(GlobalValues.CANCELLED).commit();
		stopService(new Intent(this, LanguageMaintenanceService.class));
		updateMenuAndStatusDisplay();
	}

	private void startLoadFromDevice() {
		updateMenuOptions();
		if (checkCreateMediaDirectories()) {
			// loadFileTaskFragment.startFileLoad(languageFile);
			// receiver should be registered before this is called
			Intent intent = new Intent(this, LanguageMaintenanceService.class);
			intent.putExtra(LanguageMaintenanceService.SERVICE_CALL,
					LanguageMaintenanceService.SERVICE_LOAD);
			intent.putExtra(LanguageMaintenanceService.DOWNLOAD_TYPE,
					LanguageMaintenanceService.FROM_FILE);
			intent.putExtra(LanguageMaintenanceService.LOAD_URL, loadFileDevice);
			startService(intent);
			saveBackgroundTaskDetails(selectedOp, GlobalValues.RUNNING,
					maintOpDetails.toString());
			updateMenuAndStatusDisplay();
		}
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
		Intent intent = new Intent(this, LanguageMaintenanceService.class);
		intent.putExtra(LanguageMaintenanceService.SERVICE_CALL,
				LanguageMaintenanceService.SERVICE_LOAD);
		intent.putExtra(LanguageMaintenanceService.DOWNLOAD_TYPE,
				LanguageMaintenanceService.FROM_WEB);
		intent.putExtra(LanguageMaintenanceService.LOAD_URL, loadFileUrl);
		startService(intent);
		saveBackgroundTaskDetails(selectedOp, GlobalValues.RUNNING,
				maintOpDetails.toString());
		updateMenuAndStatusDisplay();

	}

	private void startDeleteProcess() {
		updateMenuOptions();
		maintOpDetails.setLength(0);
		maintOpDetails.append(res.getString(R.string.delete_details,
				teacherFromToLanguage.getTeacherName(),
				teacherFromToLanguage.getLearningLanguageName(),
				teacherFromToLanguage.getKnownLanguageName()));
		Intent intent = new Intent(this, LanguageMaintenanceService.class);
		intent.putExtra(LanguageMaintenanceService.SERVICE_CALL,
				LanguageMaintenanceService.SERVICE_DELETE);
		intent.putExtra(LanguageMaintenanceService.TEACHER_LANGUAGE,
				teacherFromToLanguage);
		startService(intent);
		saveBackgroundTaskDetails(selectedOp, GlobalValues.RUNNING,
				maintOpDetails.toString());

	}

	// Handle the load update - basically tell the LanguageMaintenanceFragment to
	// update display
	private BroadcastReceiver webLoadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// get current status message
			// see if %complete being populated
			statusMessage = intent
					.getStringExtra(LanguageMaintenanceService.STATUS);
			percentComplete = intent.getIntExtra(
					LanguageMaintenanceService.PERCENT_COMPLETE, -1);
			updateMenuAndStatusDisplay();
			if (percentComplete < 100) {
				showProgressDialog(percentComplete, statusMessage);
			} else {
				// a bit of hack until the load process rewritten to be like the
				// delete process
				onCompletionOfService(languageSettings.getMaintenanceStatus(),
						statusMessage);
			}
		}
	};

	private void showProgressDialog(int percentComplete, String statusMessage) {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
		}
		if (percentComplete < 0) {
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		} else {
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(percentComplete);
			progressDialog.setMax(100);
		}
		progressDialog.setMessage(statusMessage);
		// progressDialog.setIndeterminate(true);
		// progressDialog.setOnCancelListener(new OnCancelListener() {
		// public void onCancel(DialogInterface arg0) {
		// // back button pressed
		// showDialog(res
		// .getString(R.string.do_you_want_to_cancel_current_operation),
		// CANCEL_OP, R.string.cancel, R.string.continuex, -1);
		//
		// }
		// });
		// progressDialog.setCancelable(true);
		progressDialog.show();
	}

	public void onCompletionOfService(int completionCode, String errorMsg) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		maintenanceStatus = completionCode;
		languageSettings.setMaintenanceStatus(maintenanceStatus).commit();
		// updateMenuOptions();
		if (completionCode == GlobalValues.CANCELLED) {
			showDialog(
					res.getString((selectedOp == GlobalValues.LOAD || selectedOp == GlobalValues.LOAD_FROM_WEB) ? R.string.load_cancelled
							: R.string.delete_cancelled),
					(selectedOp == GlobalValues.LOAD || selectedOp == GlobalValues.LOAD_FROM_WEB) ? LOAD_CANCELLED
							: DELETE_CANCELLED, R.string.ok, -1, -1);
		} else if (completionCode == GlobalValues.FINISHED_WITH_ERROR) {
			showErrorDialog(errorMsg);

		} else if (completionCode == GlobalValues.FINISHED_OK) {
			showDialog(
					res.getString((selectedOp == GlobalValues.LOAD || selectedOp == GlobalValues.LOAD_FROM_WEB) ? R.string.language_file_loaded_successfully
							: R.string.deletion_completed_successfully),
					(selectedOp == GlobalValues.LOAD || selectedOp == GlobalValues.LOAD_FROM_WEB) ? LOAD_SUCCESSFUL
							: DELETE_SUCCESSFUL, R.string.ok, -1, -1);
		}
		updateMenuAndStatusDisplay();
	}

}
