package com.fisincorporated.languagetutorial.utility;

import com.fisincorporated.languagetutorial.R;

import android.app.Service ;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
 

public class MediaPlayerService extends Service  {
	private static final String TAG = "MediaPlayerService";
	 private final IBinder mBinder = new  MediaPlayerBinder();
	protected boolean errorInMedia;
	protected long timeAtEndOfLastPhrase;
	protected int mediaDuration;
	 private static MediaPlayer mediaPlayer = null;
	 
	  public class  MediaPlayerBinder extends Binder {
		  MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	 @Override
    public void onCreate() {
      setupMediaPlayer();
    }

	 private void setupMediaPlayer() {
			Log.i(TAG, "Setting Up MediaPlayer");
			if (mediaPlayer == null) {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mediaPlayer) {
						mediaPlayer.start();
					}
				});
				mediaPlayer.setOnErrorListener(new OnErrorListener() {
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						errorInMedia = true;
						return true;
					}
				});
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						timeAtEndOfLastPhrase = System.currentTimeMillis();
						mediaDuration = mp.getDuration();
						Log.i(TAG, "onCompletion doing stopreset, duration is: "   + mediaDuration  + " reset at timeAtEndOfLastPhrase: " + timeAtEndOfLastPhrase );
						mediaPlayer.stop();
						mediaPlayer.reset();
						
					}
				});
			}
		}
}
