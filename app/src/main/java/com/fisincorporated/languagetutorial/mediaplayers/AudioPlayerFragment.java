package com.fisincorporated.languagetutorial.mediaplayers;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fisincorporated.languagetutorial.R;

// on orientation change getting error on mediaController.show(0)

// tried http://stackoverflow.com/questions/17897162/mediacontroller-error-when-show-called
public class AudioPlayerFragment extends AbstractMediaPlayerFragment {
	private static final String TAG = "AudioPlayerFragment";
	private RelativeLayout rlAudioView = null;
	private TextView tvLessonDescription = null;
	private TextView tvLessonTitle;
	private View rootView = null;
    

	public AudioPlayerFragment() {
	}
 
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		Log.i(TAG, " onCreateView");
		View v = inflater.inflate(R.layout.media_player_audio, parent, false);
		rootView = v;
		anchorView = (FrameLayout) v.findViewById(R.id.controllerAnchor);
		rlAudioView = (RelativeLayout) v.findViewById(R.id.rlAudioView);
		tvLessonTitle = (TextView) v.findViewById(R.id.tvLessonTitle);
		tvLessonTitle.setText(lessonTitle);
		tvLessonDescription = (TextView) v.findViewById(R.id.tvLessonDescription);
		tvLessonDescription.setText(lessonDescription);
		
		rlAudioView.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				showController();
				return true;
			}});
		rootView = v;
		return v;
	}
	
	 void resetAnchorView(){
		 anchorView = (FrameLayout) rootView.findViewById(R.id.controllerAnchor);     
	 }
	 
	 @Override
		public void onResume() {
			Log.i(TAG, " on Start");
			super.onStart();
			setupMediaPlayer();
			playMedia();
		}
	 
	
	 
		@Override
		public void onPause() {
			Log.i(TAG, " onPause");
//			if (mediaController != null) {
//				mediaController.hide();
//				Log.i(TAG, " onPause  - hide controller");
//			}
			if (mediaPlayer != null) {
				try {
					currentPosition = mediaPlayer.getCurrentPosition();
					mediaPlayer.stop();
					// should release be in onDestroyView?
					mediaPlayer.release();
					playerIdle = true;
					playerPaused = false;
					mediaPlayer = null;
					mediaController = null;
					Log.i(TAG,
							" onDestroyView - stopped/released and set to null mediaPlayer/mediaController");
				} catch (Exception e) {
					;
				}
			}
			if (wifiLock != null) {
				wifiLock.release();
			}
			super.onPause();

		}
}

 
	 
 