package com.fisincorporated.languagetutorial;

import com.fisincorporated.languagetutorial.interfaces.IDialogResultListener;
import com.fisincorporated.languagetutorial.interfaces.IHandleSelectedAction;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

// display lesson list and lesson phrase fragments (for large screen)
// or lesson phrase fragment for small screen.
// If either  sure teacher/language/class (for large screen)
// or teacher/language/class/lesson (for small screens) not defined
// show the lesson selection dialog first

public class LessonListActivity extends ActionBarActivity implements
		IHandleSelectedAction, IDialogResultListener,
		ActionBar.OnMenuVisibilityListener {
	// used to store values to SharedPreferences file
	private static LanguageSettings languageSettings;
	private LessonSelectionDialog lessonSelectionDialog;
	private LessonPhraseFragment lessonPhraseFragment = null;
	private LessonListFragment lessonListFragment;

	private ActionBar actionBar;
	private boolean onLargeScreen = false;

	public LessonListActivity() {

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_masterdetail);
		// do whatever needed
		actionBar = getSupportActionBar();
		actionBar.setTitle(getResources().getString(R.string.app_name));

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}
		languageSettings = LanguageSettings.getInstance(this);
		// see if large or small screen (via if detailFragmentContainer exists in
		// layout)
		onLargeScreen = (findViewById(R.id.detailFragmentContainer) != null) ? true
				: false;
		// but see if class or lesson selected before displaying fragment
		if (onLargeScreen && languageSettings.getClassId() == -1
				|| !onLargeScreen && languageSettings.getLessonId() == -1) {
			displayLessonSelectionDialog();
		}
	}

	private Fragment createFragment() {
		//Fragment fragment = null;
		if (findViewById(R.id.detailFragmentContainer) == null) {
			// must be on small screen so create just LessonPhraseFragment()
			return lessonPhraseFragment = new LessonPhraseFragment();
		} else {
			// large screen so display LessonListFragment
			return lessonListFragment = new LessonListFragment();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	// Don't care about bundle, if this is called then display lesson
	@Override
	public void onSelectedAction(Bundle args) {

		if (findViewById(R.id.detailFragmentContainer) == null) {
			// must be on small screen phone so go to LessonPhraseActivity
			Intent intent = new Intent(this, LessonPhraseActivity.class);
			startActivity(intent);
		} else {
			startLesson();
		}
	}

	private void startLesson() {
		FragmentManager fm = getSupportFragmentManager();
		//FragmentTransaction ft = fm.beginTransaction();
		int screenId = onLargeScreen ? R.id.detailFragmentContainer : R.id.fragmentContainer;
		lessonPhraseFragment = (LessonPhraseFragment) fm
				.findFragmentById(screenId);
		if (lessonPhraseFragment == null) {
			lessonPhraseFragment = new LessonPhraseFragment();
			fm.beginTransaction().add(screenId , lessonPhraseFragment).commit();
			//ft.add(R.id.detailFragmentContainer, lessonPhraseFragment);
			//ft.commit();
		} else {
			// doing detach/reattach will cause onResume to run
			lessonPhraseFragment.startNewLesson();
			// ft.detach(lessonPhraseFragment);
			// ft.attach(lessonPhraseFragment);
			// ft.replace(R.id.detailFragmentContainer, lessonPhraseFragment);
			// ft.commit();
		}
	}

	// Add the menu - Will add to any menu items added by parent activity
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.select_tutorial, menu);
		return true;
	}

	// handle the selected menu option
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.select_teacher_class_lesson:
			displayLessonSelectionDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void displayLessonSelectionDialog() {
		lessonSelectionDialog = LessonSelectionDialog
				.newInstance(onLargeScreen ? LessonSelectionDialog.SELECT_TO_CLASS
						: LessonSelectionDialog.SELECT_TO_LESSON);
		lessonSelectionDialog.setOnDialogResultListener(this);
		lessonSelectionDialog.show(this.getSupportFragmentManager(),
				"lessonSelectionDialog");
	}

	public void onDialogResult(int requestCode, int resultCode,
			int buttonPressed, Bundle bundle) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == LessonSelectionDialog.SELECT_TO_LESSON) {
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				// tell LessonPhraseFragment to start the new lesson saved in shared
				// preferences
				if (languageSettings.getLessonId() != -1) {
					// if null then an orientation change occurred and need to recreate) 
						if (lessonPhraseFragment == null){
							startLesson();
					}
						else {
							lessonPhraseFragment.startNewLesson();
						}
				
				} else {
					Toast.makeText(this,
							R.string.no_lesson_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
				}

			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				// User cancelled so see if already on lesson and if so stay on it
				if (-1 != languageSettings.getLessonId())
					return;
				else {
					Toast.makeText(this,
							R.string.no_lesson_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		if (requestCode == LessonSelectionDialog.SELECT_TO_CLASS) {
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				// tell LessonListFragment to start to list the new set of lessons
				// and remove LessonPhraseFragment  until lesson selected in list
				if (languageSettings.getClassId() != -1) {
					lessonListFragment.displayLessonList();
					removeLessonPhraseFragment();  
				} else {
					Toast.makeText(this,
							R.string.no_class_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
				}
			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				// User cancelled so see if already on lesson and if so stay on it
				if (-1 != languageSettings.getLessonId())
					return;
				else
					Toast.makeText(this,
							R.string.no_class_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void removeLessonPhraseFragment(){
		FragmentManager fm = getSupportFragmentManager();
		LessonPhraseFragment lessonPhraseFragment = (LessonPhraseFragment) fm
				.findFragmentById(R.id.detailFragmentContainer);
		if (lessonPhraseFragment != null){
			// I should probably do detach then attach but this is working now.
			FragmentTransaction ft = fm.beginTransaction();
				//ft.detach(lessonPhraseFragment );
				ft.remove(lessonPhraseFragment);
				ft.commit();
		}
	}

	@Override
	public void onMenuVisibilityChanged(boolean arg0) {
		supportInvalidateOptionsMenu();

	}

}
