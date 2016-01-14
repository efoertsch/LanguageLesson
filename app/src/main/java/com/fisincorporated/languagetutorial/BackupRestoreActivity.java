package com.fisincorporated.languagetutorial;

import android.support.v4.app.Fragment;
 

public class BackupRestoreActivity extends MasterActivity {

	protected Fragment createFragment() {
		return new BackupRestoreFragment();
	}

	// added for tablet
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}


}
