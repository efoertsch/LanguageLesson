package com.fisincorporated.languagetutorial.mediaplayers;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.languagetutorial.R;
import com.fisincorporated.languagetutorial.interfaces.IPauseMedia;

// see http://stackoverflow.com/questions/2961749/mediacontroller-with-mediaplayer
//, OnAudioFocusChangeListener 
//OnVideoSizeChangedListener, 
public abstract class AbstractMediaPlayerFragment extends Fragment implements
		OnCompletionListener, OnPreparedListener, OnErrorListener,
		OnInfoListener, MediaController.MediaPlayerControl, IPauseMedia {

	private static final String TAG = "MediaPlayerFragment";
	private static final String MEDIA_FILE = "MediaFile";
	private static final String MEDIA_DIRECTORY = "MediaDirectory";
	private static final String LESSON_TITLE = "LessonTitle";
	private static final String LESSON_DESCRIPTION = "LessonDescription";
	private static final String CURRENT_POSITION = "CurrentPosition";
	private static final String LESSON_TYPE = null;

	// setting to default access so instantiating class can reference them
	boolean errorInMedia = false;

	Resources res;
	String mediaFile = "";
	String mediaDirectory = "";
	TextView tvLessonTitle;
	String lessonTitle = "";
	String lessonDescription = "";
	String lessonType;

	AudioManager audioManager = null;
	WifiLock wifiLock = null;
	MediaPlayer mediaPlayer = null;
	MediaController mediaController = null;
	boolean playerIdle = true;
	boolean alwaysDisplay = false;
	FrameLayout anchorView;
	boolean playerPaused = false;
	int currentPosition = 0;

	/**
	 * The media file should either be if web based http://... or https://... if
	 * device based directory/file name
	 **/
	public void createMediaInfoBundle(String mediaFile, String mediaDirectory,
			String lessonTitle, String lessonDescription, String lessonType) {
		Bundle bundle = new Bundle();
		bundle.putString(MEDIA_FILE, mediaFile);
		bundle.putString(MEDIA_DIRECTORY, mediaDirectory);
		bundle.putString(LESSON_TITLE, lessonTitle);
		bundle.putString(LESSON_DESCRIPTION, lessonDescription);
		bundle.putString(LESSON_TYPE, lessonType);
		setArguments(bundle);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		lookForArguments(savedInstanceState);
		res = getResources();
	}

	// Need to be careful as logic below may need modification per the
	// circumstances
	private void lookForArguments(Bundle savedInstanceState) {
		Bundle bundle = null;
		if (getArguments() != null) {
			bundle = getArguments();
			mediaFile = bundle.getString(MEDIA_FILE);
			mediaDirectory = bundle.getString(MEDIA_DIRECTORY);
			lessonTitle = bundle.getString(LESSON_TITLE);
			lessonDescription = bundle.getString(LESSON_DESCRIPTION);
			lessonType = bundle.getString(LESSON_TYPE);
		}
		// If fragment destroyed but then later recreated,
		// the savedInstanceState will hold info (assuming saved via
		// onSaveInstanceState(... )
		if (savedInstanceState != null) {
			currentPosition = savedInstanceState.getInt(CURRENT_POSITION);
		}
	}

	// abstract void defineMediaController();
	void showController() {
		if (mediaController != null) {
			mediaController.show();
		}
	}

	// @Override
	// public void onStart() {
	// super.onStart();
	// setupMediaPlayer();
	// }

	// so that mediacontroller view displays properly in anchor view
	abstract void resetAnchorView();

	// @Override
	// public abstract View onCreateView(LayoutInflater inflater, ViewGroup
	// parent,
	// Bundle savedInstanceState);

	// Add the menu - Will add to any menu items added by parent activity
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// Add the menu
		inflater.inflate(R.menu.media_player_menu, menu);
	}

	// handle the selected menu option
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.restart_lesson:
			if (mediaPlayer != null) {
				currentPosition = 0;
				playMedia();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void setupMediaPlayer() {
		Log.i(TAG, "SetUpMediaPlayer");
		// audioManager = (AudioManager) getActivity().getSystemService(
		// Context.AUDIO_SERVICE);
		mediaPlayer = null;
		mediaPlayer = new MediaPlayer();
		Log.i(TAG, " SetupMediaPlayer  created MediaPlayer");
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		mediaController = null;
		mediaController = new MediaController(getActivity());
		// If controller is always displayed the back button won't work nor will
		// the menu items fire
		// mediaController = new MediaController(getActivity()){
		// @Override
		// public void hide() {
		// //Do not hide.
		// }
		// };
		resetAnchorView();
		mediaController.setAnchorView(anchorView);
		mediaController.setMediaPlayer(this);

	}

	// play the Media
	void playMedia() {
		String datasource = "";
		// int result = audioManager.requestAudioFocus(this,
		// AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		// if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
		// // could not get audio focus.
		// Toast.makeText(getActivity(), R.string.system_not_allowing_this_audio,
		// Toast.LENGTH_LONG).show();
		// }

		try {
			// if (mediaPlayer.isPlaying()) {
			if (isPlaying() || playerPaused) {
				mediaPlayer.stop();
				playerIdle = true;
				playerPaused = false;
			}
			mediaPlayer.reset();
			mediaPlayer.setDataSource(getDataSource());
			playerIdle = false;
			// mediaPlayer.setOnVideoSizeChangedListener(this);
			mediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			if (!errorInMedia)
				sendError(res.getString(R.string.error_playing_media_file,
						datasource));
			errorInMedia = true;
		} catch (SecurityException e) {
			if (!errorInMedia)
				sendError(res.getString(R.string.error_playing_media_file,
						datasource));
			errorInMedia = true;
		} catch (IllegalStateException e) {
			if (!errorInMedia) {
				sendError(res.getString(R.string.error_playing_media_file,
						datasource));
				errorInMedia = true;
			}
		} catch (IOException e) {
			if (!errorInMedia)
				sendError(res.getString(R.string.error_playing_media_file,
						datasource));
			errorInMedia = true;
		}
	}

	// if mediafile starts with http then is on web
	// if mediafile doesn't start with http then assume on device (may be
	// audio or video)
	String getDataSource() {
		String datasource = "";
		if (mediaFile.startsWith("http") || mediaFile.startsWith("https")) {
			datasource = mediaFile;
			wifiLock = ((WifiManager) getActivity().getSystemService(
					Context.WIFI_SERVICE)).createWifiLock(
					WifiManager.WIFI_MODE_FULL, "mylock");
			wifiLock.acquire();
		} else {
			// assume file on device
			File file = new File(Environment.getExternalStorageDirectory() + "/"
					+ Environment.DIRECTORY_DOWNLOADS + "/" + mediaDirectory
					+ File.separator + mediaFile);
			datasource = "file:///" + file.getAbsolutePath();

		}
		return datasource;
	}

	public void onPrepared(MediaPlayer mediaPlayer) {
		mediaPlayer.setWakeMode(getActivity().getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		// if using w/videoview this is only way to get mediaplayer from videoview
		this.mediaPlayer = mediaPlayer;
		seekTo(currentPosition);
		start();
		playerIdle = false;
	}

	public void onCompletion(MediaPlayer mp) {
		// wakelock automagically releases with mediaPlayer.stop, .release and
		// .reset
		mediaPlayer.reset();
		playerIdle = true;
		playerPaused = false;
		// playMedia();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.i(TAG, "onInfo  what:" + what + " extra:" + extra);
		return false;
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (!errorInMedia) {
			// mListener.onErrorOccurred(res.getString(
			// R.string.error_in_media_player, what, extra));
		}
		errorInMedia = true;
		playerIdle = true;
		playerPaused = false;
		return true;
	}

	private void sendError(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}

	// ------------------------------------------------------
	// IPauseMedia interface
	public void pauseMedia() {
		pause();
	}

	// ------------------------------------------------------
	// MediaPlayerControl methods
	public void start() {
		if (mediaPlayer != null) {
			playerPaused = false;
			mediaPlayer.start();
			mediaController.show(0);
		}
	}

	public void pause() {
		if (!playerIdle && !playerPaused) {
			currentPosition = getCurrentPosition();
			playerPaused = true;
			mediaPlayer.pause();
		}

	}

	public int getDuration() {
		if (mediaPlayer != null && !playerIdle) {
			return mediaPlayer.getDuration();
		} else
			return 0;
	}

	public int getCurrentPosition() {
		if (mediaPlayer != null && !playerIdle) {
			return mediaPlayer.getCurrentPosition();
		} else
			return 0;
	}

	public void seekTo(int i) {
		if (mediaPlayer != null && !playerIdle) {
			mediaPlayer.seekTo(i);
		}
	}

	public boolean isPlaying() {
		if (mediaPlayer != null && !playerIdle) {
			return mediaPlayer.isPlaying();
		}
		return false;
	}

	public int getBufferPercentage() {
		return 0;
	}

	public boolean canPause() {
		return true;
	}

	public boolean canSeekBackward() {
		return true;
	}

	public boolean canSeekForward() {
		return true;
		// return false;
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setAlwaysDisplay(boolean alwaysDisplay) {
		this.alwaysDisplay = alwaysDisplay;
	}

	// --------------------------------------------------------------------------------

	// From OnAudioFocusChangeListener
	// public void onAudioFocusChange(int focusChange) {
	// switch (focusChange) {
	// case AudioManager.AUDIOFOCUS_GAIN:
	// // resume playback
	// if (mediaPlayer == null)
	// setupMediaPlayer();
	// else if (!mediaPlayer.isPlaying())
	// mediaPlayer.start();
	// mediaPlayer.setVolume(1.0f, 1.0f);
	// break;
	//
	// case AudioManager.AUDIOFOCUS_LOSS:
	// // Lost focus for an unbounded amount of time: stop playback and
	// // release media player
	// if (mediaPlayer != null) {
	// if (mediaPlayer.isPlaying())
	// mediaPlayer.stop();
	// mediaPlayer.release();
	// mediaPlayer = null;
	// }
	// break;
	//
	// case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	// // Lost focus for a short time, but we have to stop
	// // playback. We don't release the media player because playback
	// // is likely to resume
	// if (mediaPlayer != null) {
	// if (mediaPlayer.isPlaying())
	// mediaPlayer.pause();
	// }
	// break;
	//
	// case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	// // Lost focus for a short time, but it's ok to keep playing
	// // at an attenuated level
	// if (mediaPlayer != null) {
	// if (mediaPlayer.isPlaying())
	// mediaPlayer.setVolume(0.1f, 0.1f);
	// }
	// break;
	// }
	// }

}
