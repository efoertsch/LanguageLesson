package com.fisincorporated.languagetutorial;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fisincorporated.languagetutorial.db.ClassName;
import com.fisincorporated.languagetutorial.db.ClassNameDao;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.Lesson;
import com.fisincorporated.languagetutorial.db.LessonDao;
import com.fisincorporated.languagetutorial.interfaces.IDialogResultListener;
import com.fisincorporated.languagetutorial.interfaces.IHandleSelectedAction;
import com.fisincorporated.languagetutorial.interfaces.IPauseMedia;
import com.fisincorporated.languagetutorial.mediaplayers.MediaPlayerFragmentFactory;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

// display lesson list and lesson phrase fragments (for large screen)
// or lesson phrase fragment for small screen.
// If either  show teacher/language/class (for large screen)
// or teacher/language/class/lesson (for small screens) not defined
// show the lesson selection dialog first

public class LessonListActivity extends ActionBarActivity implements
		IHandleSelectedAction, IDialogResultListener,
		ActionBar.OnMenuVisibilityListener {

	// private static final String LESSON_V_OR_A = "lessonVorA";
	// private static final String LESSON_TEXT = "lessonText";
	//private static final String LESSON_FRAGMENT = "LessonFragment";
	private static final String MASTER_CONTAINER = "MasterContainer";
	private static final String CHILD_CONTAINER ="ChildContainer";
	private static LanguageSettings languageSettings;
	private LessonSelectionDialog lessonSelectionDialog;
	//private LessonPhraseFragment lessonPhraseFragment = null;
	private LessonListFragment lessonListFragment;

	private ActionBar actionBar;
	private boolean onLargeScreen = false;

	// private DaoMaster daoMaster;
	private DaoSession daoSession;
	//private LessonDao lessonDao;
	private Lesson lesson;
	private Long lessonId;
	private ClassName className;
	private Long classId;


	//private Long lessonId = -1l;
	//private Lesson lesson = null;

	public LessonListActivity() {
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_masterdetail);
		// do whatever needed
//		actionBar = getSupportActionBar();
//		actionBar.setTitle(getResources().getString(R.string.app_name));
//		// ff0000ff solid blue
//		actionBar.setBackgroundDrawable(new ColorDrawable(0xff0000ff));

		// see if large or small screen (via if detailFragmentContainer exists in
		// layout)
		onLargeScreen = (findViewById(R.id.detailFragmentContainer) != null) ? true
				: false;
		languageSettings = LanguageSettings.getInstance(this);
	 	
		validateClassOrLesson();
		// See if class or lesson selected before displaying fragment
		if (onLargeScreen &&  classId == -1
					|| !onLargeScreen &&  lessonId == -1) {
			displayLessonSelectionDialog();
		} else {
			// either displaying the lesson list (getClassId <> -1) or lesson
			// (getLessonId <> -1)
			addFragments();
		}
	}

	private void validateClassOrLesson() {
		// make sure lesson still exists (if indeed it was previously set)
		classId =  languageSettings.getClassId();
		lessonId = languageSettings.getLessonId();
		if (classId != -1){
			loadClassName(classId);
			if (className == null){
				resetLanguageSettings();
				lessonId = -1l;
				lesson = null;
				return;
			}
		}
		if ((!onLargeScreen) &&  lessonId != -1){
			loadLesson(lessonId);
			if (lesson == null){
			// lessons must have been deleted so reset
				resetLanguageSettings();
				lessonId = -1l;
			}
		}
	}


	private void resetLanguageSettings() {
		languageSettings.setTeacherId(-1l)
		.setTeacherLanguageId(-1l) 
		.setTeacherName("")
		.setClassId(-1l) 
		.setClassTitle("") 
		.setLessonId(-1l) 
		.setLessonTitle("") 
		.setLastLessonPhraseLine(-1) 
		.commit();
		
	}

	// If a detail fragment container exists must be on a larger screen so will
	// have lesson list on left (in fragmentContainer) and
	// lesson on right (in detailFragmentContainer)
	// on smaller screen you will have lesson in fragmentContainer
	private void addFragments() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment;
		if (onLargeScreen) {
			fragment = fm.findFragmentByTag(CHILD_CONTAINER);
			if (fragment != null){
				ft.attach(fragment).addToBackStack(null);
			}
			// if no fragment found then lesson not yet selected and nothing to add yet
//			else {
//				fragment = getLessonFragment();
//				ft.add(R.id.detailFragmentContainer, fragment, CHILD_CONTAINER);
//			}
		}
		//small screen or large screen something goes in MASTER_CONTAINER	
		// see if LessonListFragment already exists and if so just add it back
		fragment = fm.findFragmentByTag(MASTER_CONTAINER);
		if (fragment != null){
			ft.attach(fragment).addToBackStack(null) ;
		}
		else {
			// large screen add lessonList, small screen add lessonFragment
			if (onLargeScreen){
			  fragment = new LessonListFragment();
			}
			else {
				fragment = getLessonFragment();
			}
			ft.add(R.id.fragmentContainer, fragment, MASTER_CONTAINER).addToBackStack(null);
		}
		ft.commit();
	}
	
	// If on small device finish this activity
	// If on large device, 
	//     If fragment exists and is in detailFragmentContainer and is AudioPlayerFragment or VideoPlayerFragment call stopPlayer() and remove fragment
	//     else (LessonPhraseFragment) remove fragment
	// If this not working make sure that mediacontroller controls not on all the time, if so the back keypress doesn't make it to here.
	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();
		if (!onLargeScreen){
			Fragment lessonFragment = (Fragment) fm.findFragmentById( R.id.fragmentContainer);
			if (lessonFragment != null) {
				fm.beginTransaction().remove(lessonFragment).commit();
			}
			finish();
		}
		else {
			// if lessonFragment showing first press of back button will remove the fragment
			// the next press will finish activity
			Fragment lessonFragment = (Fragment) fm.findFragmentById( R.id.detailFragmentContainer);
			if (lessonFragment != null) {
				fm.beginTransaction().remove(lessonFragment).commit();
			}
			else finish();
		}
		//super.onBackPressed();
	}
	
 

	// A lesson must be validate before calling this routine
	private Fragment getLessonFragment() {
		return MediaPlayerFragmentFactory.getMediaPlayerFragment(lesson);
	}

//	@Override
//	public void onSaveInstanceState(Bundle savedInstanceState) {
//		super.onSaveInstanceState(savedInstanceState);
//	}

	// Don't care about bundle, if this is called then display lesson
	@Override
	public void onSelectedAction(Bundle args) {
		if (!onLargeScreen) {
			// must be on small screen phone so go to LessonPhraseActivity
			Intent intent = new Intent(this, LessonPhraseActivity.class);
			startActivity(intent);
		} else {
			startLesson();
		}
	}

	// depending on screen the lesson may either be in the
	// detailFragmentContainer(large screen) or fragmentContainer(small screen)
	// make this simple by removing current fragment (if any) and add new
	// fragment (either LessonPhraseFragment or MediaPlayerFragment)
	private void startLesson() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		int screenId = onLargeScreen ? R.id.detailFragmentContainer
				: R.id.fragmentContainer;
		Fragment lessonFragment = (Fragment) fm.findFragmentById(screenId);
		if (lessonFragment != null) {
			ft.remove(lessonFragment) ;
		}
		lessonFragment = getLessonFragment();
		ft.add(screenId, lessonFragment, onLargeScreen? CHILD_CONTAINER : MASTER_CONTAINER).addToBackStack(null).commit();
		 
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
		// note that 'requestCode' is doing double duty here
		pauseMedia();
		int requestCode = onLargeScreen ? LessonSelectionDialog.SELECT_TO_CLASS : LessonSelectionDialog.SELECT_TO_LESSON; 
		lessonSelectionDialog = LessonSelectionDialog
				.newInstance(LessonSelectionDialog.CLASS_LESSON_SELECT, requestCode);
		lessonSelectionDialog.setOnDialogResultListener(this, requestCode);
		lessonSelectionDialog.show(this.getSupportFragmentManager(),
				"lessonSelectionDialog");
	}

	// pause any audio/video media
	private void pauseMedia() {
		FragmentManager fm = getSupportFragmentManager();
		int screenId = onLargeScreen ? R.id.detailFragmentContainer
				: R.id.fragmentContainer;
		Fragment lessonFragment = (Fragment) fm.findFragmentById(screenId);
		if (lessonFragment != null) {
			if (lessonFragment instanceof IPauseMedia){
				((IPauseMedia)lessonFragment).pauseMedia();
			}
		}
	}

	public void onDialogResult(int requestCode, int resultCode,
			int buttonPressed, Bundle bundle) {
		if (resultCode != Activity.RESULT_OK)
			return;
		// If SELECT_TO_LESSON then on small screen and you selected a particular
		// lesson
		if (requestCode == LessonSelectionDialog.SELECT_TO_LESSON) {
			lessonId = languageSettings.getLessonId();
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				// tell LessonPhraseFragment/MediaPlayerFragment to start the new
				// lesson saved in shared preferences
				if (lessonId != -1) {
					// display appropriate lessonFragment
					loadLesson(lessonId);
					startLesson();
				} else {
					Toast.makeText(this,
							R.string.no_lesson_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
				}

			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				// User cancelled so see if already on lesson and if so stay on it
				if (lessonId != -1)
					return;
				else {
					Toast.makeText(this,
							R.string.no_lesson_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		// here if on larger screen, LessonListFragment displayed and new set of
		// lessons displayed
		if (requestCode == LessonSelectionDialog.SELECT_TO_CLASS) {
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				// tell LessonListFragment to start to list the new set of lessons
				// and remove the lessonFragment until lesson selected in list
				if (languageSettings.getClassId() != -1) {
					lessonListFragment.displayLessonList();
					removeLessonFragment();
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

	// Remove LessonPhraseFragment or Audio/VideoPlayerFragment until lesson selected
	private void removeLessonFragment() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = (Fragment) fm
				.findFragmentById(R.id.detailFragmentContainer);
		if (fragment != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(fragment);
			ft.commit();
		}
	}

	@Override
	public void onMenuVisibilityChanged(boolean arg0) {
		supportInvalidateOptionsMenu();
	}

	// Use this both to see if lesson still exists (the teacher/language may have been deleted)
	private void loadLesson(Long lessonId) {
		lesson = null;
		if (lessonId != null && lessonId > -1) {
			daoSession = LanguageApplication.getInstance().getDaoSession();
			LessonDao lessonDao = daoSession.getLessonDao();
			lesson = lessonDao.load(lessonId);
		}
	}
	
// Use this both to see if lesson still exists (the teacher/language may have been deleted)
	private void loadClassName(Long classId) {
		className = null;
		if (classId != null && classId > -1) {
			daoSession = LanguageApplication.getInstance().getDaoSession();
			ClassNameDao classNameDao = daoSession.getClassNameDao();
			className = classNameDao.load(classId);
		}
	}
	
 
			

		
		
}

	


