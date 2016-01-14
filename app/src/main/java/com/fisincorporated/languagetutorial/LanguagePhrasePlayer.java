package com.fisincorporated.languagetutorial;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;

import com.fisincorporated.languagetutorial.LanguagePhraseLoader.LanguagePhraseLoadListener;
import com.fisincorporated.languagetutorial.db.LanguagePhrase;
import com.fisincorporated.languagetutorial.db.LanguageXref;
import com.fisincorporated.languagetutorial.utility.LanguagePhraseRequest;
import com.fisincorporated.languagetutorial.utility.PhrasePlayRequest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class LanguagePhrasePlayer extends HandlerThread {
	private static final String TAG = "LanguagePhrasePlayer";
	public static final int PLAY_PHRASE = 0;
	private Handler mHandler;
	private Handler mResponseHandler;
	private Context mContext;
	private LanguagePhrasePlayerListener mListener;
	private Resources res;
	// Don't make static, causes problem clearing errors
	private boolean errorInMedia = false;
	
	
	private static MediaPlayer mediaPlayer = null;
	// orientation changes can occur in middle of playback so make vars static
	// (Still need to figure out why this is needed)
	// the duration in millisecs of the last phrase played
	private static long mediaDuration = 0;
	// time in millisecs that the last phrase was played
	private static long timeAtEndOfLastPhrase = 0;
	// and the difference between current time and timeOfLastPhrase
	private static long millisecsSinceLastPhrase = 0;
	private static  boolean playingSomething = false;
	// if something playing sleep (in millisecs) before polling again to see if
	// it completed
	private static long sleepForCompletion = 10;
	private static Thread currentThreadId = null;
	private static boolean looksLikeShutdown = false;
	private static final String mp3_suffix = ".mp3";

	public interface LanguagePhrasePlayerListener {
		void onErrorOccurred(String errorMessage);
	}

	public void setListener(LanguagePhrasePlayerListener listener) {
		mListener = listener;
	}

	public LanguagePhrasePlayer(Handler responseHandler, Context context) {
		super(TAG);
		mResponseHandler = responseHandler;
		mContext = context;
		res = mContext.getResources();
		// MediaPlayer on UI thread I think.
		setupMediaPlayer();

	}

	private void setupMediaPlayer() {
		Log.i(TAG, "Setting Up MediaPlayer");
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mediaPlayer) {
					playingSomething = true;
					mediaPlayer.start();
				}
			});
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					if (!errorInMedia){
						mListener.onErrorOccurred(res.getString(
								R.string.error_in_media_player, what, extra));
					}
					errorInMedia = true;
					playingSomething = false;
					return true;
				}
			});
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					timeAtEndOfLastPhrase = System.currentTimeMillis();
					mediaDuration = mp.getDuration();
					Log.i(TAG, "onCompletion doing stop, duration is: "   + mediaDuration  + " reset at timeAtEndOfLastPhrase: " + timeAtEndOfLastPhrase );
					playingSomething = false;
					mediaPlayer.stop();
					mediaPlayer.reset();
					
				}
			});
		}
	}

	// return error msg on UI thread
	private void sendError(final String errorMessage) {
		// ignore errors if you are shutting down. Perhaps due to orientation
		// change. The callback needs to run on UI thread.
		if (!looksLikeShutdown) {
			mResponseHandler.post(new Runnable() {
				public void run() {
					// check to see if on same teacher/class/lesson
					// load language phrases to display arrays
					mListener.onErrorOccurred(errorMessage);
				}
			});
		}
	}

	// this is called by LessonPhraseFragment so on UI thread
	public void queuePlayRequest(int what, PhrasePlayRequest phrasePlayRequest) {
		// Make sure to append .sendToTarget()!
		if (!errorInMedia) {
			mHandler.obtainMessage(what, phrasePlayRequest).sendToTarget();
		}
	}

	@SuppressLint("HandlerLeak")
	@Override
	// overridden so we can create Handler and implement handleMessage that
	// handles the messages on the queue
	// (that are sent here by the looper)
	protected void onLooperPrepared() {
		// the handler created here is associated with LanguagePhrasePlayer
		// (HandlerThread)
		mHandler = new Handler() {
			@Override
			// Called by the looper as it processes the messages on queue one by
			// one
			public void handleMessage(Message msg) {
				handleRequest(msg.what, (PhrasePlayRequest) msg.obj);
			}
		};
	}

	// This is being executed on background thread
	private void handleRequest(int command, PhrasePlayRequest phrasePlayRequest) {
		// make sure these variables are defined as final
		// make sure all variables defined. If not just exit
		if (command == PLAY_PHRASE) {
			Log.i(TAG, "Taking request off queue to play " + phrasePlayRequest.getFilename());
			looksLikeShutdown = false;
			playLessonPhraseAudio(phrasePlayRequest);
		}
	}

	// Since handleRequest being called on background thread, this should also be
	// executing on background thread.
	private void playLessonPhraseAudio(PhrasePlayRequest phrasePlayRequest) {
		final String languageMediaDirectory = phrasePlayRequest.getDirectory();
		final String audioFile = phrasePlayRequest.getFilename();
		float playDelay = phrasePlayRequest.getPlayDelayFor();
		long playDelayForMillis = phrasePlayRequest.getPlayDelayMillis();
		currentThreadId = Thread.currentThread();
		boolean wasInterrupted = false;
		if (playingSomething) {
			Log.i(TAG, "Somethings playing so may need to wait");
		}
		while (playingSomething && !errorInMedia
				&& !wasInterrupted && !looksLikeShutdown) {
			try {
				// sleep hangs on orientation change so make sure to interrupt it
				sleep(10);
				if (currentThreadId.isInterrupted()) {
					wasInterrupted = true;
					Log.i(TAG, "Current sleep thread interrupted");
				}
			} catch (InterruptedException e) {
				Log.i(TAG,"Exception caught on sleep(sleepForCompletion)"
								+ e.toString());
			}
		}
		currentThreadId = null;
		if (errorInMedia || looksLikeShutdown || wasInterrupted)
			return;
		Log.i(TAG, "Nothing playing now");
		Log.i(TAG, "Check if need to pause - playDelay:" + playDelay
				+ " or  playDelayForMillis:" + playDelayForMillis);
		long delayForMillisecs = 0;
		long currentTimeMillis = System.currentTimeMillis();
		Log.i(TAG, "CurrentTimeMillis: " + currentTimeMillis + "  TimeAtEndOfLastPhrase: " + timeAtEndOfLastPhrase);
		if (playDelay > 0) {
			millisecsSinceLastPhrase = currentTimeMillis
					- timeAtEndOfLastPhrase;
			delayForMillisecs = ( (long) (playDelay * mediaDuration))
					- millisecsSinceLastPhrase;
		} else if (playDelayForMillis > 0) {
			millisecsSinceLastPhrase = currentTimeMillis
					- timeAtEndOfLastPhrase;
			delayForMillisecs = playDelayForMillis - millisecsSinceLastPhrase;
		}

		if (delayForMillisecs > 0) {
			try {
				Log.i(TAG, "playLessonPhraseAudio sleeping" + delayForMillisecs);
				sleep(delayForMillisecs);
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		}
		if (wasInterrupted == true){
			return;
		}
		File file = findFile(  languageMediaDirectory,   audioFile);
		if (file != null) {
			try {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
					mediaPlayer.reset();
				}
				//mediaPlayer.setDataSource("file:///" + languageMediaDirectory + "/"	+ audioFile);
				setDataSourcePerVersion( file);
				
				// once prepared the onPreparedListener will be called and
				// logic in listener will play but set flag here (as prepare might
				// take a bit and don't want to process another request
				// till current one completed )
				playingSomething = true;
				mediaPlayer.prepare();

			} catch (IllegalArgumentException e) {
				if (!errorInMedia)
					sendError(res.getString(R.string.error_playing_audio_file));
				errorInMedia = true;
			} catch (SecurityException e) {
				if (!errorInMedia)
					sendError(res.getString(R.string.error_playing_audio_file));
				errorInMedia = true;
			} catch (IllegalStateException e) {
				if (!errorInMedia)
					sendError(res.getString(R.string.error_playing_audio_file));
				errorInMedia = true;
			} catch (IOException e) {
				if (!errorInMedia)
					sendError(res.getString(R.string.error_playing_audio_file));
				errorInMedia = true;
			}
		}
	}
	
	private void setDataSourcePerVersion( File file) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
			mediaPlayer.setDataSource("file:///" + file.getAbsolutePath() );
		}
		else {
			// earlier version of Mediaplayer do not have authorization to open file in /data/data/....
			// but it can read file based on file descriptor so...
			mediaPlayer.setDataSource(new FileInputStream(file).getFD());
		}
		
		
	}

	private File findFile(String languageMediaDirectory, String audioFile){
		String modifiedName = audioFile;
		File file = new File(languageMediaDirectory, modifiedName);
		if (file.exists()) {
			return file;
		}
		if (!modifiedName.endsWith(mp3_suffix)){
				modifiedName = audioFile+ mp3_suffix;
			  file = new File(languageMediaDirectory, audioFile + mp3_suffix);
			  if (file.exists()){
				  return file;
			  }
		}
		if (modifiedName.contains("_")){
			modifiedName.replaceAll("_", " ");
			file = new File(languageMediaDirectory, audioFile+ mp3_suffix);
			  if (file.exists()){
				  return file;
			  }
		}
		return null;
	 
		 
	}

	public void clearQueue() {
		mHandler.removeMessages(PLAY_PHRASE);
		Log.i(TAG,"clearQueue called");
		 stopCurrentPlay();

	}

	public boolean quit() {
		looksLikeShutdown = true;
		stopCurrentPlay();
		return super.quit();
	}

	private void stopCurrentPlay() {
		if (currentThreadId != null) {
			currentThreadId.interrupt();
		}
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.reset();
			playingSomething = false;
		}
	}
}
