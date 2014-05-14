package com.fisincorporated.languagetutorial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.fisincorporated.languagetutorial.OptionsFragment.INotifyOptionChanged;
import com.fisincorporated.languagetutorial.interfaces.IHandleSelectedAction;

public class LessonOptionActivity extends ActionBarActivity implements
		TabListener, IHandleSelectedAction, INotifyOptionChanged {
	private static final String TAG = "LessonOptionActivity";
	private Button btnSave;
	private Button btnCancel;
	
	private ViewPager viewPager;
	private OptionsPagerAdapter optionsPagerAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs;
	private boolean optionsChanged = false;
	Resources res;
	private int currentTabPosition = 0;
	private int goingToTabPosition = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		setContentView(R.layout.options_view_pager);
		tabs = new String[] { res.getString(R.string.audio),
				res.getString(R.string.text) };

		// Initialization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getSupportActionBar();
		actionBar.setTitle(res.getString(R.string.options));
		actionBar.setHomeButtonEnabled(true);
		optionsPagerAdapter = new OptionsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(optionsPagerAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}
		currentTabPosition = 0;

		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make requested tab selected
				goingToTabPosition = position;
				actionBar.setSelectedNavigationItem(position);
				currentTabPosition = position;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				return;
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				return;
			}
		});
		
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				validateAndSave();
			}
		});
		
		btnCancel = (Button)  findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED,null);
				finish();

			}
		});
		Log.i(TAG,"Finishing onCreate");
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		goingToTabPosition = tab.getPosition();
//		if (optionsChanged) {
//			showSaveCancelDialog();
//			return;
//		} else {
			viewPager.setCurrentItem(tab.getPosition());
//		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onSelectedAction(Bundle args) {
		
	}
	
	private void validateAndSave(){
		for (int i = 0 ; i < optionsPagerAdapter.getCount(); ++i){
			if (!((OptionsFragment)optionsPagerAdapter.getItem(i)).isValidInput()){
				viewPager.setCurrentItem(i);
				actionBar.setSelectedNavigationItem(i);
				currentTabPosition = i;
				return  ;
			}
		}
		for (int i = 0 ; i < optionsPagerAdapter.getCount(); ++i){
			((OptionsFragment)optionsPagerAdapter.getItem(i)).storeOptionSettings();
		}
		Toast.makeText(getBaseContext(), R.string.options_saved,
				Toast.LENGTH_SHORT).show();
		 setResult(Activity.RESULT_OK, null);
		 finish();
		 
	}

	// called if change made and not yet saved
	// called if tab change, back pressed, or orientation change
	// 
	public void showSaveCancelDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set title
		alertDialogBuilder.setTitle(res
				.getString(R.string.save_option_changes_question));

		// set dialog message
		alertDialogBuilder
				.setMessage(
						res.getString(R.string.options_have_changed_save_or_cancel))
				.setCancelable(false)
				.setPositiveButton(res.getString(R.string.save),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								validateAndSave();
							}
						})
				.setNegativeButton(res.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, reset values on current
								// option fragment
								// and set changed flag to false
								finish();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
	}

	@Override
	public void onBackPressed(){
		if (optionsChanged) {
			showSaveCancelDialog();
			return;
		}
		 finish ();
	}
	 
	// for INotifyOptionChanged 
	@Override
	public void optionsChanged() {
		Log.i(TAG,"Options changed notification");
		optionsChanged = true;
	}
	
	@Override
	public void optionsSaved() {
		optionsChanged = false;
	}

	@Override
	public void optionChangesCancelled() {
		optionsChanged = false;

	}

	
	@Override
	public void optionBeforeAfter(int beforeAfter) {
		for (int i = 0 ; i < optionsPagerAdapter.getCount(); ++i){
			if (i != viewPager.getCurrentItem()){
			((OptionsFragment)optionsPagerAdapter.getItem(i)).syncBeforeAfter(beforeAfter);
			}
		}
	}

}
