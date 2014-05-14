package com.fisincorporated.languagetutorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class OptionsPagerAdapter extends FragmentPagerAdapter {
	private static final String TAG = "OptionsPagerAdapter";
	public OptionsPagerAdapter(FragmentManager fm) {
		super(fm);
		 
	}

	@Override
   public Fragment getItem(int index) {

       switch (index) {
       case 0:
           // Audio Options
      	 Log.i(TAG, "Returning OptionsLessonAudioFragment");
           return OptionsLessonAudioFragment.getInstance();
           
       case 1:
           // Text Options
      	 Log.i(TAG, "Returning OptionsLessonTextFragment");
           return OptionsLessonTextFragment.getInstance();
        }

       return null;
   }

	@Override
	public int getCount() {
		return 2;
	}

}
