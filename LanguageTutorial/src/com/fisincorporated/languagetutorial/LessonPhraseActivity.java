package com.fisincorporated.languagetutorial;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class LessonPhraseActivity extends MasterActivity {

	private LessonPhraseFragment lessonPhraseFragment = null;
	public LessonPhraseActivity() {
		// TODO Auto-generated constructor stub
	}
		 

		@Override
		protected Fragment createFragment() {
			actionBar.setTitle(getResources().getString(R.string.lesson));
			lessonPhraseFragment = new LessonPhraseFragment();
			return lessonPhraseFragment;
		}
	
	}

