package com.fisincorporated.languagetutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

// This template is for a FragmentActivity that can implement fragments either with
// 1 fragment for small screen (phone) or 2 fragments (parent/child) if on larger screen (tablet)
// Create concrete FragmentActivity class by extending this class.
// The IHandleSelectedAction is to handle callbacks from the Fragment
public abstract class MasterActivity extends ActionBarActivity implements
		 ActionBar.OnMenuVisibilityListener {

	protected ActionBar actionBar;

	protected abstract Fragment createFragment();

	// added for master/detail fragments as on tablet
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}

	// Called at the start of the full lifetime.
	// Initialize Activity
	// Inflate the UI (handled in superclass.
	// Get references to fragments
	// Allocate references to class variables
	// Bind data to controls
	// Start Services and Timers
	// Use Bundle as needed to restore the UI to its previous state (or wait to
	// do it in onRestoreInstanceState)
	// Create any objects needed during life of activity
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		// do whatever needed
		actionBar = getSupportActionBar();
		actionBar.setTitle(getResources().getString(R.string.app_name));
		
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}
	}

	@Override
	public void onMenuVisibilityChanged(boolean arg0) {
		supportInvalidateOptionsMenu();

	}

	

}
