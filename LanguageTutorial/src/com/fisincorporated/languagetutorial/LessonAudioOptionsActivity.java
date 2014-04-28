package com.fisincorporated.languagetutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class LessonAudioOptionsActivity extends MasterActivity {
	
	public LessonAudioOptionsActivity() {
		// TODO Auto-generated constructor stub
	}

	 
	

	@Override
	protected Fragment createFragment() {
		actionBar.setTitle(getResources().getString(R.string.lesson_audio_options_title));
		return new LessonAudioOptionsFragment();
	}


}
