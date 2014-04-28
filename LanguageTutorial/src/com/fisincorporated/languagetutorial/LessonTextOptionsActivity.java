package com.fisincorporated.languagetutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class LessonTextOptionsActivity extends MasterActivity {

	public LessonTextOptionsActivity() {
		// TODO Auto-generated constructor stub
	}

	 
	

	@Override
	protected Fragment createFragment() {
		actionBar.setTitle(getResources().getString(R.string.lesson_text_options_title));
		return new LessonTextOptionsFragment();
	}
}



 
