package com.fisincorporated.languagetutorial;

import java.io.BufferedReader;
import java.io.File;
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

import com.fisincorporated.languagetutorial.interfaces.ILoadLessonCallBack;
import com.fisincorporated.languagetutorial.utility.FileUtil;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

// passing back info to starting routine
//http://stackoverflow.com/questions/7871521/how-to-collect-info-from-intentservice-and-update-android-ui
// http://www.mysamplecode.com/2011/10/android-intentservice-example-using.html
//http://stackoverflow.com/questions/6099429/how-to-read-text-file-in-android-from-web

public class LoadLanguageLessonService extends IntentService implements
		ILoadLessonCallBack {
	private static final String TAG = "LoadLanguageLessonService";
	public static final String LANGUAGE_LESSON_URL = "LanguageLessonURL";
	public static final String LOAD_LANGUAGE_LESSON_SERVICE_UPDATE = "com.fisincorporated.languagetutorial.LOAD_LANGUAGE_LESSON_SERVICE_UPDATE";
	private String lessonMasterFileURL = "";
	private String lessonFileUrl = "null";
	private String learningMediaUrl = "null";
	private String knownMediaUrl = "null";
	private StringBuilder maintOpDetails = new StringBuilder();

	private Resources res;
	private boolean loadError = false;
	private boolean cancel = false;

	private String learningLanguageMediaDirectory = "";
	private String knownLanguageMediaDirectory = "";

	// used to store values to SharedPreferences file
	private LanguageSettings languageSettings;

	public LoadLanguageLessonService() {
		super(LoadLanguageLessonService.TAG);
	}

	public LoadLanguageLessonService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		res = getResources();
		languageSettings = LanguageSettings.getInstance(this);
		lessonMasterFileURL = intent.getStringExtra(LANGUAGE_LESSON_URL);
		maintOpDetails.append(res.getString(R.string.based_on_url,
				lessonMasterFileURL));
		setStartStatus();
		loadLanguageLessonDetails(lessonMasterFileURL);
	}

	public boolean stopService(Intent name) {
		// update languagesettings to indicate cancel
		cancel = true;
		updateCompletionStatus();
		return super.stopService(name);
	}

	private void loadLanguageLessonDetails(String url) {
		getWebLoadFileDetails(url);
		if ((!cancel && !loadError) && !lessonFileUrl.equalsIgnoreCase("null")) {
			loadLanguageLessons();
		}
		if ((!cancel && !loadError) && !learningMediaUrl.equalsIgnoreCase("null")) {
			maintOpDetails.append(res.getString(
					R.string.learning_language_media_from, learningMediaUrl));
			updateLoadDetails(maintOpDetails.toString()
					+ res.getString(R.string.load_in_progress));
			unizpMediaFile(learningLanguageMediaDirectory, learningMediaUrl);
		}
		if ((!cancel && !loadError) && !knownMediaUrl.equalsIgnoreCase("null")) {
			maintOpDetails.append(res.getString(
					R.string.known_language_media_from, knownMediaUrl));
			updateLoadDetails(maintOpDetails.toString()
					+ res.getString(R.string.load_in_progress));
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

	private void loadLanguageLessons() {
		int offset;
		int recCount = 0;
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
			// Need to create method to try a series of encodings and first one to
			// actually find Teacher
			// in first record will be one to use (and if none found either error
			// or new encoding
			// r = new BufferedReader(new InputStreamReader(is, "UTF-16LE"));
			// UTF-8 is default
			r = new BufferedReader(new InputStreamReader(is));
			sb.append(r.readLine());
			// // Windows UTF-16LE has some non-text char at start of file so
			// account for it
			offset = sb.toString().indexOf(GlobalValues.TEACHER);
			if (offset == -1) {
				storeErrorMessage(res.getString(
						R.string.first_record_not_teacher_record, sb.toString()));
				return;
			} else {
				sb.delete(0, offset);
			}
			while ((!sb.toString().equalsIgnoreCase("null")) && loadError == false) {
				++recCount;
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
		loadError = true;

	}

	// Callback from LanguageLessonLoader
	@Override
	public void passbackMessage(String message, boolean error) {
		storeErrorMessage(message);
		// bit of hack here. Just in case
		if (error != true)
			loadError = false;
	}

	private void setStartStatus() {
		languageSettings.setMaintenanceType(GlobalValues.LOAD_FROM_WEB)
				.setMaintenanceStatus(GlobalValues.RUNNING)
				.setMaintenanceDetails(maintOpDetails.toString());
		sendMessage();
	}

	private void updateLoadDetails(String loadDetails) {
		languageSettings.setMaintenanceDetails(loadDetails).commit();
		sendMessage();
	}

	private void updateCompletionStatus() {
		int maintenanceStatus;
		maintenanceStatus = (cancel ? GlobalValues.CANCELLED
				: (loadError ? GlobalValues.FINISHED_WITH_ERROR
						: GlobalValues.FINISHED_OK));
		languageSettings.setMaintenanceStatus(maintenanceStatus).commit();
		sendMessage();
	}

	// Let any receivers know that update has been written to shared preferences
	// file
	private void sendMessage() {
		// no data needed to be put into intent. receivers need to get update from
		// shared preferences file
		Intent intent = new Intent(LOAD_LANGUAGE_LESSON_SERVICE_UPDATE);
		LocalBroadcastManager.getInstance(this.getApplicationContext())
				.sendBroadcast(intent);
	}

	// from
	// http://stackoverflow.com/questions/3975847/extrakting-zip-to-sd-card-is-very-slow-how-can-i-optimize-performance/10312761#10312761
	// modified for url access and somewhat different requirements
	private void unizpMediaFile(String mediaDirectory, String zipFileURL) {
		InputStream inputStream = null;
		//BufferedInputStream bif = null;
		//OutputStream out = null;
		File mediaFileDirectory;
		//String tempZipFile = getDownloadDirectory() + File.separator
		//		+ "LanguageLesson_temp.zip";
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
			FileUtil.unzipArchive(inputStream,	targetDirectory);
//  copy the zip from from the server to the device - this was used for debug
//			bif = new BufferedInputStream(inputStream);
//			out = new BufferedOutputStream(new FileOutputStream(tempZipFile));
//			FileUtil.copyInputStreamToOutputStream(bif, out, 8096);
// 			FileUtil.showZipFileEntries(tempZipFile);
// 			FileUtil.unzip(tempZipFile, targetDirectory);
//			FileUtil.unzipArchive(tempZipFile, targetDirectory);

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
		}
		 finally {
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

}
