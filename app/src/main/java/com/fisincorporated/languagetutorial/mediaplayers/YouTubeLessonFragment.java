package com.fisincorporated.languagetutorial.mediaplayers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.languagetutorial.R;
import com.fisincorporated.languagetutorial.interfaces.IPauseMedia;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
 
public class YouTubeLessonFragment  extends YouTubePlayerSupportFragment implements IPauseMedia  {
	private static final String TAG = "YouTubeLessonFragment";
  // private String currentVideoID = "video_id";
   private YouTubePlayer activePlayer = null;

   public static YouTubeLessonFragment newInstance(String url) {

   	YouTubeLessonFragment youTubeLessonFragment = new YouTubeLessonFragment();

       Bundle bundle = new Bundle();
       bundle.putString("url", url);

       youTubeLessonFragment.setArguments(bundle);

       return youTubeLessonFragment;
   }
   
   public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//initialize();
   }
   
   @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater,container,savedInstanceState);
		initialize();
		return view;
	}

   private void initialize() {
       initialize(getActivity().getResources().getString(R.string.YoutubeKey), new OnInitializedListener()  {
           @Override
           public void onInitializationFailure(Provider arg0, YouTubeInitializationResult arg1) { 
         	  Log.i(TAG, " onInitializationFailure");
           }

           @Override
           public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
               activePlayer = player;
               activePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
               if (!wasRestored) {
                   activePlayer.loadVideo(getArguments().getString("url"), 0);
               }
           }
       });
   }
   
   public void pauseMedia(){
   	if (activePlayer != null && activePlayer.isPlaying()){
   		activePlayer.pause();
   	}
   	
   }
   
 
    
}