package com.fisincorporated.languagetutorial;


import android.os.Bundle;
import android.support.v4.app.Fragment;

public class StartupActivity extends MasterActivity {


	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new StartupFragment();
	}

	 

	@Override
	public void onMenuVisibilityChanged(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}
