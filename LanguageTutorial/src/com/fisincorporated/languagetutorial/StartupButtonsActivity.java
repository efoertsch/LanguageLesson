package com.fisincorporated.languagetutorial;

import android.support.v4.app.Fragment;
 

public class StartupButtonsActivity extends MasterActivity {

	protected Fragment createFragment() {
		return new StartupButtonsFragment();
	}

	// added for tablet
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}


}
