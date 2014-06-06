package com.fisincorporated.languagetutorial.mediaplayers;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.fisincorporated.languagetutorial.R;

public class VideoPlayerFragment extends AbstractMediaPlayerFragment {

	private RelativeLayout rlVideoView;
	private TextView tvLessonTitle;
	private VideoView vvVideoView = null;
	private View rootView = null;

	public VideoPlayerFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.media_player_video, parent, false);
		rlVideoView = (RelativeLayout) v.findViewById(R.id.rlVideoView);
		tvLessonTitle = (TextView) v.findViewById(R.id.tvLessonTitle);
		tvLessonTitle.setText(lessonTitle);
		vvVideoView = (VideoView) v.findViewById(R.id.vvVideoView);
		rootView = v;
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		vvVideoView.setVideoURI(Uri.parse(getDataSource()));
		mediaController = new MediaController(getActivity());
		mediaController.setMediaPlayer(this);
		vvVideoView.setMediaController(mediaController);
		vvVideoView.setOnPreparedListener(this);
		vvVideoView.setOnCompletionListener(this);
		vvVideoView.setOnErrorListener(this);
		// setOnInfoListener for API level 17 and above
		// vvVideoView.setOnInfoListener(this);
		vvVideoView.requestFocus();
		vvVideoView.start();
	}

   @Override 
   public void onPause(){
   	super.onPause();
   	if (mediaPlayer != null && !playerIdle) {
			currentPosition = mediaPlayer.getCurrentPosition();
		}
		if (vvVideoView != null) {
			vvVideoView.stopPlayback();
		}
   }

   

//	@Override
//	public void onDestroyView() {
//		super.onDestroyView();
//		if (mediaPlayer != null && !playerIdle) {
//			currentPosition = mediaPlayer.getCurrentPosition();
//		}
//		if (vvVideoView != null) {
//			vvVideoView.stopPlayback();
//		}
//	}

	
	 void resetAnchorView(){
		 // not needed as videoview takes care of positioning of controller positioning
	 }

}
