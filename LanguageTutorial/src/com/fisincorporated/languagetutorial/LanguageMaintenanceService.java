package com.fisincorporated.languagetutorial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fisincorporated.languagetutorial.interfaces.ILessonMaintanceCallBack;
import com.fisincorporated.languagetutorial.utility.FileUtil;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

// passing back info to starting routine
//http://stackoverflow.com/questions/7871521/how-to-collect-info-from-intentservice-and-update-android-ui
// http://www.mysamplecode.com/2011/10/android-intentservice-example-using.html
//http://stackoverflow.com/questions/6099429/how-to-read-text-file-in-android-from-web

//IntentService runs on background thread
// The interface is to receive callbacks from LanguageLessonLoader and LanguageLessonDeleter
// TO DO
// Move load logic LanguageLessonLoader (make similar to LanguageLessonDeleter)
public class LanguageMaintenanceService extends IntentService implements
	ILessonMaintanceCallBack {
	private static final String TAG = "LanguageMaintenanceService";
	public static final String SERVICE_CALL = "ServiceType";
	public static final String SERVICE_DELETE = "Delete";
	public static final String SERVICE_LOAD = "Load";
	public static final String TEACHER_LANGUAGE = "TeacherLanguage";
	public static final String DOWNLOAD_TYPE = "DownloadType";
	public static final String FROM_WEB = "Web";
	public static final String FROM_FILE = "File";
	public static final String LOAD_URL = "LoadURL";
	public static final String PERCENT_COMPLETE = "Percent Complete";
	public static final String PROCESS_COMPLETE = "Process Complete";
	public static final String STATUS = "Status";
	
	private static final String STARTING_LOAD = "Starting Load";
	private static final String LOADING_LESSON_FILE = "Loading Lesson File";
	private static final String STARTING_DELETE = "Starting Delete";
	

	public static final String LOAD_LANGUAGE_LESSON_SERVICE_UPDATE = "com.fisincorporated.languagetutorial.LOAD_LANGUAGE_LESSON_SERVICE_UPDATE";

	private String lessonMasterFileURL = "";
	private String lessonFileUrl = "null";
	private String learningMediaUrl = "null";
	private String knownMediaUrl = "null";
	private StringBuilder maintOpDetails = new StringBuilder();

	private Resources res;
	private boolean serviceError = false;
	private boolean cancel = false;

	private String learningLanguageMediaDirectory = "";
	private String knownLanguageMediaDirectory = "";

	// used to store values to SharedPreferences file
	private LanguageSettings languageSettings;
	private String downloadType = "";
	private int percentComplete = 0;
	private TeacherFromToLanguage teacherFromToLanguage;
	private String serviceRequested = "";
	private long contentLength;

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public LanguageMaintenanceService() {
		super(LanguageMaintenanceService.TAG);
	}

	public LanguageMaintenanceService(String name) {
		super(name);
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns,
	 * IntentService stops the service, as appropriate. Handle requests
	 * sequentially (but in this case you should get only 1)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		res = getResources();
		languageSettings = LanguageSettings.getInstance(this);
		serviceRequested = intent.getStringExtra(LanguageMaintenanceService.SERVICE_CALL);
		if (serviceRequested.equals(LanguageMaintenanceService.SERVICE_DELETE)){
			teacherFromToLanguage = intent.getParcelableExtra(LanguageMaintenanceService.TEACHER_LANGUAGE);
			deleteLanguageLessons();
		}
		else {
			// must be load of some type
			downloadType = intent.getStringExtra(DOWNLOAD_TYPE);
			lessonMasterFileURL = intent.getStringExtra(LOAD_URL);
			maintOpDetails.append(res.getString(R.string.based_on_url,
					lessonMasterFileURL));
			loadLanguageLessonDetails(lessonMasterFileURL);
		}
	}

	
	public boolean stopService(Intent name) {
		// update languagesettings to indicate cancel
		cancel = true;
		updateCompletionStatus();
		return super.stopService(name);
	}

	private void loadLanguageLessonDetails(String url) {
		setStartStatus();
		if (downloadType.equals(FROM_FILE)) {
			broadcastStatus(0,LOADING_LESSON_FILE);
			loadLanguageFile(url);
		} else {
			getWebLoadFileDetails(url);
			if ((!cancel && !serviceError) && !lessonFileUrl.equalsIgnoreCase("null")) {
				broadcastStatus(0,LOADING_LESSON_FILE);
				loadLanguageLessonsFromWeb();
			}
		}
		// Completed load of language lesson file.
		// You may or may not have media files to load.
		if ((!cancel && !serviceError) && !learningMediaUrl.equalsIgnoreCase("null")
				&& !learningMediaUrl.trim().equals("")) {
			maintOpDetails.append(res.getString(
					R.string.learning_language_media_from, learningMediaUrl));
			updateLoadDetails(maintOpDetails.toString()
					+ res.getString(R.string.load_in_progress));
			broadcastStatus(30,res.getString(R.string.unzipping_file)
					+ learningMediaUrl);
			unizpMediaFile(learningLanguageMediaDirectory, learningMediaUrl);
		}
		if ((!cancel && !serviceError) && !knownMediaUrl.equalsIgnoreCase("null")
				&& !knownMediaUrl.trim().equals("")) {
			maintOpDetails.append(res.getString(
					R.string.known_language_media_from, knownMediaUrl));
			updateLoadDetails(maintOpDetails.toString()
					+ res.getString(R.string.load_in_progress));
			broadcastStatus(60, res.getString(R.string.unzipping_file) + knownMediaUrl);
			unizpMediaFile(knownLanguageMediaDirectory, knownMediaUrl);
		}
		updateCompletionStatus();
	}

	private void getWebLoadFileDetails(String url) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStream is = getHttpInputStream(url);
			if (is == null) {
				// if inputstream null getHttpInputStream should have handled error
				// so just return;
				return;
			}
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			for (int i = 0; i < 3; ++i) {
				sb.append(r.readLine());
				switch (i) {
				case 0:
					lessonFileUrl = sb.toString();
					break;
				case 1:
					learningMediaUrl = sb.toString();
					break;
				case 2:
					knownMediaUrl = sb.toString();
					break;
				}
				sb.setLength(0);
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "readTextFile" + e.toString());
			storeErrorMessage(res.getString(R.string.error_reading_file_at_url,
					url, e.toString()));
		} catch (IOException e) {
			Log.e(TAG, "readTextFile" + e.toString());
			storeErrorMessage(res.getString(R.string.error_reading_file_at_url,
					url, e.toString()));
		}
	}

	// Loading from a file on this device
	private void loadLanguageFile(String lessonFileUrl) {
		int offset;
		long languageFileSize = 0;
		BufferedReader br = null;
		long numberFileCharsRead = 0;
		int percentFileRead = 0;
		LanguageLessonLoader languageLessonLoader = new LanguageLessonLoader(
				this.getApplicationContext(), this);
		File sdcard = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		// Get the text file
		File file = new File(sdcard, lessonFileUrl);
		 
		// Read text from file
		try {
			languageFileSize = file.length();
			numberFileCharsRead = 0;
			// Spreadsheet file now created with UTF-8 which is default for java so
			// don't specify
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null && (!serviceError) && (!cancel)) {
				numberFileCharsRead += line.length();
				if (languageFileSize > 0) {
					percentFileRead = (int) ((numberFileCharsRead / (float) languageFileSize) * 100);
					if (percentFileRead >= percentComplete + 5) {
						percentComplete = percentFileRead;
						broadcastStatus(percentComplete, LOADING_LESSON_FILE);
					}
				}
				if (i == 0) {
					offset = line.indexOf("Teacher");
					if (offset == -1) {
						passbackMessage(res.getString(
								R.string.first_record_not_teacher_record, line), true);
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
			passbackMessage(
					String.format(
							res.getString(R.string.error_reading_language_file),
							file.getAbsolutePath()), true);
		} catch (Exception e) {
			passbackMessage(
					res.getString(R.string.unexpected_exception) + " "
							+ e.toString(), true);
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

	private void loadLanguageLessonsFromWeb() {
		int offset;
		int recCount = 0;
		int numberFileCharsRead = 0;
		int percentFileRead = 0;
		StringBuilder sb = new StringBuilder();
		maintOpDetails.append(res.getString(R.string.language_lessons_from,
				lessonFileUrl));
		updateLoadDetails(maintOpDetails.toString()
				+ res.getString(R.string.load_in_progress));
		LanguageLessonLoader languageLessonLoader = new LanguageLessonLoader(
				this.getApplicationContext(), this);
		// try {
		InputStream is = getHttpInputStream(lessonFileUrl);
		if (is == null) {
			// if inputstream null getHttpInputStream should have handled error
			// so just return;
			return;
		}
		BufferedReader r;
		try {
			// UTF-8 is default
			r = new BufferedReader(new InputStreamReader(is));
			sb.append(r.readLine());
			// First record must be Teacher record
			offset = sb.toString().indexOf(GlobalValues.TEACHER);
			if (offset == -1) {
				storeErrorMessage(res.getString(
						R.string.first_record_not_teacher_record, sb.toString()));
				return;
			} else {
				sb.delete(0, offset);
			}
			while ((!sb.toString().equalsIgnoreCase("null")) && serviceError == false) {
				++recCount;
				numberFileCharsRead =  numberFileCharsRead + sb.length();
				if (contentLength > 0) {
					 percentFileRead = (int) ((numberFileCharsRead / (float) contentLength) * 100);
					if (percentFileRead >= percentComplete + 5) {
						percentComplete = percentFileRead;
						broadcastStatus(percentComplete, LOADING_LESSON_FILE);
					}
				}
				languageLessonLoader.processLine(sb.toString());
				sb.setLength(0);
				sb.append(r.readLine());
			}
			maintOpDetails.append(res.getString(R.string.load_complete));
			updateLoadDetails(maintOpDetails.toString());
			Log.i(TAG, "Number of records read : " + recCount);
			learningLanguageMediaDirectory = languageLessonLoader
					.getLearningLanguageMediaDirectory();
			knownLanguageMediaDirectory = languageLessonLoader
					.getKnownLanguageMediaDirectory();

		} catch (UnsupportedEncodingException uee) {
			storeErrorMessage(res.getString(R.string.error_reading_file_at_url,
					lessonFileUrl, uee.toString()));
		} catch (IOException e) {
			storeErrorMessage(res.getString(R.string.error_reading_file_at_url,
					lessonFileUrl, e.toString()));
		}
	}

	private void storeErrorMessage(String errorMsg) {
		languageSettings.setMaintenanceDetails(
				maintOpDetails.append(errorMsg).toString()).commit();
		serviceError = true;

	}

	// Callback from ILessonMaintanceCallbacks and may also come from this class
	@Override
	public void passbackMessage(String message, boolean error) {
		storeErrorMessage(message);
		// bit of hack here. Just in case
		if (error != true)
			serviceError = false;
	}
	
	

	@Override
	public void passbackPercentComplete(int percentComplete, String message) {
		this.percentComplete = percentComplete;
		broadcastStatus(percentComplete, message);
	}

	@Override
	public void completedProcess(int completionCode, String message) {
		// set here for current loader, but remove once this activity/loader refactored
		languageSettings.setMaintenanceStatus(completionCode).commit();
		broadcastStatus(100, PROCESS_COMPLETE);
		
	}

	private void setStartStatus() {
		if (serviceRequested.equals(SERVICE_DELETE)){
			languageSettings.setMaintenanceType(GlobalValues.DELETE);
			languageSettings.setMaintenanceStatus(GlobalValues.RUNNING).commit();
			broadcastStatus(0, STARTING_DELETE);
		}
		else {
			if (downloadType.equals(FROM_FILE)) {
				languageSettings.setMaintenanceType(GlobalValues.LOAD);
			} else {
				languageSettings.setMaintenanceType(GlobalValues.LOAD_FROM_WEB);
			}
			languageSettings.setMaintenanceStatus(GlobalValues.RUNNING)
				.setMaintenanceDetails(maintOpDetails.toString()).commit();
			broadcastStatus(0, STARTING_LOAD);
		}
	}

	private void updateLoadDetails(String loadDetails) {
		languageSettings.setMaintenanceDetails(loadDetails).commit();
	}

	private void updateCompletionStatus() {
		int maintenanceStatus;
		maintenanceStatus = (cancel ? GlobalValues.CANCELLED
				: (serviceError ? GlobalValues.FINISHED_WITH_ERROR
						: GlobalValues.FINISHED_OK));
		languageSettings.setMaintenanceStatus(maintenanceStatus).commit();
		broadcastStatus(100, PROCESS_COMPLETE);
	}

	// Let any receivers know that update has been written to shared preferences
	// file
	private void broadcastStatus(int percentComplete, String statusMessage) {
		Intent intent = new Intent(LOAD_LANGUAGE_LESSON_SERVICE_UPDATE);
			intent.putExtra(PERCENT_COMPLETE, percentComplete);
		intent.putExtra(STATUS, statusMessage);
		LocalBroadcastManager.getInstance(this.getApplicationContext())
				.sendBroadcast(intent);
	}

	// from
	// http://stackoverflow.com/questions/3975847/extrakting-zip-to-sd-card-is-very-slow-how-can-i-optimize-performance/10312761#10312761
	// modified for url access and somewhat different requirements
	private void unizpMediaFile(String mediaDirectory, String zipFileURL) {
		InputStream inputStream = null;
		// BufferedInputStream bif = null;
		// OutputStream out = null;
		File mediaFileDirectory;
		// String tempZipFile = getDownloadDirectory() + File.separator
		// + "LanguageLesson_temp.zip";
		String targetDirectory = getDownloadDirectory() + File.separator
				+ mediaDirectory;
		// copy the zip file to the download directory
		// then unzip from there to the media directory
		// make sure can write to (probably) sd card
		try {
			mediaFileDirectory = createMediaDirectory(mediaDirectory);
			if (mediaFileDirectory == null)
				return;
			inputStream = getHttpInputStream(zipFileURL);
			if (inputStream == null) {
				// display error that media file can't be found
				return;
			}
			FileUtil.unzipArchive(inputStream, targetDirectory);
			// copy the zip from from the server to the device - this was used for
			// debug
			// bif = new BufferedInputStream(inputStream);
			// out = new BufferedOutputStream(new FileOutputStream(tempZipFile));
			// FileUtil.copyInputStreamToOutputStream(bif, out, 8096);
			// FileUtil.showZipFileEntries(tempZipFile);
			// FileUtil.unzip(tempZipFile, targetDirectory);
			// FileUtil.unzipArchive(tempZipFile, targetDirectory);

			maintOpDetails.append(res.getString(R.string.load_complete));
			updateLoadDetails(maintOpDetails.toString());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unizpMediaFile" + e.toString());
			storeErrorMessage(res.getString(R.string.error_reading_media_zip_file,
					zipFileURL, e.toString()));
		} catch (IOException e) {
			Log.e(TAG, "unizpMediaFile" + e.toString());
			storeErrorMessage(res.getString(R.string.error_reading_media_zip_file,
					zipFileURL, e.toString()));
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception e) {
					}
				}
			}
		}

	}

	private InputStream getHttpInputStream(String url) {
		HttpResponse response;
		InputStream is = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httppost = new HttpGet("http://" + url);
		try {
			response = httpClient.execute(httppost);
			HttpEntity ht = response.getEntity();
			contentLength = ht.getContentLength();
			BufferedHttpEntity buf;
			buf = new BufferedHttpEntity(ht);
			is = buf.getContent();
			
			
		} catch (ClientProtocolException e) {
			Log.e(TAG, "getHttpInputStream" + e.toString());
			storeErrorMessage(res.getString(R.string.error_reading_file_at_url,
					url, e.toString()));
		} catch (ConnectTimeoutException cte) {
			Log.e(TAG, "getHttpInputStream" + cte.toString());
			storeErrorMessage(res.getString(R.string.connect_timetout_error, url));

		} catch (IOException e) {
			Log.e(TAG, "getHttpInputStream" + e.toString());
			storeErrorMessage(res.getString(R.string.error_reading_file_at_url,
					url, e.toString()));
		}
		return is;
	}

	private File createMediaDirectory(String directory) {
		File sd = getDownloadDirectory();
		if (!sd.exists()) {
			if (sd.isDirectory() || sd.mkdirs()) {
				// directory is created;
			} else {
				storeErrorMessage(res.getString(
						R.string.can_not_write_to_download_directory,
						sd.getAbsoluteFile()));
				return null;
			}
		}
		if (!sd.canWrite()) {
			storeErrorMessage(res.getString(
					R.string.can_not_write_to_download_directory,
					sd.getAbsoluteFile()));
			return null;
		}
		File fileDirectory = new File(sd.getAbsoluteFile() + File.separator
				+ directory);
		if (!fileDirectory.exists()) {
			if (!fileDirectory.mkdirs()) {
				storeErrorMessage(res.getString(
						R.string.unable_to_create_media_directory, directory));
				return null;
			}
		}
		return fileDirectory;
	}

	private File getDownloadDirectory() {
		return Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	}
	
	// all the delete logic
	private void deleteLanguageLessons() {
		LanguageLessonDeleter languageLessonDeleter = new LanguageLessonDeleter(this.getApplicationContext(), this, teacherFromToLanguage);
		languageLessonDeleter.runDelete();
		
	}


}
