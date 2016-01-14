package com.fisincorporated.languagetutorial;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
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
import com.fisincorporated.languagetutorial.utility.SearchActivity;

// display lesson list and lesson phrase fragments (for large screen)
// or lesson phrase fragment for small screen.
// If either  show teacher/language/class (for large screen)
// or teacher/language/class/lesson (for small screens) not defined
// show the lesson selection dialog first

// TODO refactor up to MasterActivity
public class LessonListActivity extends AppCompatActivity implements
		IHandleSelectedAction, IDialogResultListener,
		ActionBar.OnMenuVisibilityListener {
	protected static final String TAG = "LessonListActivity";
	private static final String MASTER_CONTAINER = "MasterContainer";
	private static final String CHILD_CONTAINER = "ChildContainer";
	private static LanguageSettings languageSettings;
	private LessonSelectionDialog lessonSelectionDialog;
	private LessonListFragment lessonListFragment;
	private boolean onLargeScreen = false;
	private DaoSession daoSession;
	private Lesson lesson;
	private Long lessonId;
	private ClassName className;
	private Long classId;
	private SearchView searchView = null;
	private SearchManager searchManager = null;

	public LessonListActivity() {
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_masterdetail);
		// see if large or small screen (via if detailFragmentContainer exists in
		// layout)
		onLargeScreen = (findViewById(R.id.detailFragmentContainer) != null) ? true
				: false;
		languageSettings = LanguageSettings.getInstance(this);

		validateClassOrLesson();
		// See if class or lesson selected before displaying fragment
		if (onLargeScreen && classId == -1 || !onLargeScreen && lessonId == -1) {
			displayLessonSelectionDialog();
		} else {
			// either displaying the lesson list (getClassId <> -1) or lesson
			// (getLessonId <> -1)
			// only do if not orientation change (I think)
			// http://stackoverflow.com/questions/8474104/android-fragment-lifecycle-over-orientation-changes
			if (savedInstanceState == null) {
				addFragments();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		reacquireLessonListFragment();
	}

	// on orientation change need to re-acquire lessonListFragment (if one
	// exists)
	// but if first time in it won't exist - which is ok
	private void reacquireLessonListFragment() {
		if (onLargeScreen) {
			FragmentManager fm = getSupportFragmentManager();
			lessonListFragment = (LessonListFragment) fm
					.findFragmentByTag(MASTER_CONTAINER);
		}

	}

	private void validateClassOrLesson() {
		// make sure lesson still exists (if indeed it was previously set)
		classId = languageSettings.getClassId();
		lessonId = languageSettings.getLessonId();
		if (classId != -1) {
			loadClassName(classId);
			if (className == null) {
				resetLanguageSettings();
				lessonId = -1l;
				lesson = null;
				return;
			}
		}
		if ((!onLargeScreen) && lessonId != -1) {
			loadLesson(lessonId);
			if (lesson == null) {
				// lessons must have been deleted so reset
				resetLanguageSettings();
				lessonId = -1l;
			}
		}
	}

	private void resetLanguageSettings() {
		languageSettings.setTeacherId(-1l).setTeacherLanguageId(-1l)
				.setTeacherName("").setClassId(-1l).setClassTitle("")
				.setLessonId(-1l).setLessonTitle("").setLastLessonPhraseLine(-1)
				.commit();

	}

	// If a detail fragment container exists must be on a larger screen so will
	// have lesson list on left (in fragmentContainer) and
	// lesson on right (in detailFragmentContainer)
	// on smaller screen you will have lesson in fragmentContainer
	// This is strictly for use via onCreate (and no orientation change)
	private void addFragments() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if (onLargeScreen) {
			lessonListFragment = new LessonListFragment();
			ft.add(R.id.fragmentContainer, lessonListFragment, MASTER_CONTAINER)
					.addToBackStack(null);
		} else { // else small screen
			ft.add(R.id.fragmentContainer, getLessonFragment(), MASTER_CONTAINER)
					.addToBackStack(null);
		}
		ft.commit();
	}

	// If on small device finish this activity
	// If on large device,
	// If fragment exists and is in detailFragmentContainer and is
	// AudioPlayerFragment or VideoPlayerFragment call stopPlayer() and remove
	// fragment
	// else (LessonPhraseFragment) remove fragment
	// If this not working make sure that mediacontroller controls not on all the
	// time, if so the back keypress doesn't make it to here.
	// Just popping fragments off backstack and then checking for number of
	// fragments doesn't work. Fragment manager still holds on to fragments
	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();
		if (!onLargeScreen) {
			Fragment lessonFragment = (Fragment) fm
					.findFragmentById(R.id.fragmentContainer);
			if (lessonFragment != null) {
				fm.beginTransaction().remove(lessonFragment).commit();
			}
			finish();
		} else {
			// on large screen,
			// if lessonFragment showing first press of back button will remove the
			// fragment
			// the next press will finish activity
			fm.popBackStackImmediate();
			if (fm.getBackStackEntryCount() == 0) {
				finish();
				return;
			}
			// still going so lessonListFragment still exists, turn off high light
			lessonListFragment.turnOffSelectedHightlight();
		}
	}

	// A lesson must be validat before calling this routine
	private Fragment getLessonFragment() {
		return MediaPlayerFragmentFactory.getMediaPlayerFragment(lesson);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	// Don't care about bundle, if this is called then display lesson
	@Override
	public void onSelectedAction(Bundle args) {
		if (!onLargeScreen) {
			// must be on small screen phone so go to LessonPhraseActivity
			Intent intent = new Intent(this, LessonPhraseActivity.class);
			startActivity(intent);
		} else {
			loadLesson(languageSettings.getLessonId());
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
			// ft.remove(lessonFragment) ;
			fm.popBackStackImmediate();

		}
		lessonFragment = getLessonFragment();
		ft.add(screenId, lessonFragment,
				onLargeScreen ? CHILD_CONTAINER : MASTER_CONTAINER)
				.addToBackStack(null).commit();

	}

	// Add the menu - Will add to any menu items added by parent activity
	@Override
	@TargetApi(11)
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getMenuInflater().inflate(R.menu.search_menu, menu);
			searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			// Pull out search view
			MenuItem searchItem = menu.findItem(R.id.menu_item_search);
			searchView = (SearchView) searchItem.getActionView();
			// Get data from searchable.xml as Searchable info
			searchView.setOnQueryTextListener(new OnQueryTextListener() {
				@Override
				public boolean onQueryTextChange(String arg0) {
					// TODO Auto-generated method stub
					return false;
				}
				@Override
				public boolean onQueryTextSubmit(String query) {
					Log.i(TAG, "Got click on search submit");
					doSearch(query);
					return true;
				}
			});

			ComponentName name = getComponentName();
			SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
			searchView.setSearchableInfo(searchInfo);
		}
		getMenuInflater().inflate(R.menu.select_tutorial, menu);
		return true;
	}
	
	public void doSearch(String query){
		Intent intent = new Intent(this,SearchActivity.class);
		intent.setAction(Intent.ACTION_SEARCH);
		intent.putExtra(SearchManager.QUERY, query);
		startActivity(intent);
	}

	// handle the selected menu option
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.select_teacher_class_lesson:
			displayLessonSelectionDialog();
			return true;
		case R.id.menu_item_search:
			onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void displayLessonSelectionDialog() {
		// note that 'requestCode' is doing double duty here
		pauseMedia();
		int requestCode = onLargeScreen ? LessonSelectionDialog.SELECT_TO_CLASS
				: LessonSelectionDialog.SELECT_TO_LESSON;
		lessonSelectionDialog = LessonSelectionDialog.newInstance(
				LessonSelectionDialog.CLASS_LESSON_SELECT, requestCode);
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
			if (lessonFragment instanceof IPauseMedia) {
				((IPauseMedia) lessonFragment).pauseMedia();
			}
		}
	}

	public void onDialogResult(int requestCode, int resultCode,
			int buttonPressed, Bundle bundle) {
		// can only get cancel if no lessons loaded at all, so handle

		// ---------------------------------------------------------------------------------------------
		// If SELECT_TO_LESSON then on small screen and you selected a particular
		// lesson
		if (requestCode == LessonSelectionDialog.SELECT_TO_LESSON) {
			lessonId = languageSettings.getLessonId();
			if (resultCode != Activity.RESULT_OK && lessonId == -1) {
				Toast.makeText(this,
						R.string.no_class_selected_do_you_need_to_load_lessons,
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				// tell LessonPhraseFragment/MediaPlayerFragment to start the new
				// lesson saved in shared preferences
				if (lessonId != -1) {
					// display appropriate lessonFragment
					loadLesson(lessonId);
					startLesson();
					return;
				} else {
					Toast.makeText(this,
							R.string.no_lesson_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
					return;
				}

			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				// User cancelled so see if already on lesson and if so stay on it
				if (lessonId != -1)
					return;
				else {
					Toast.makeText(this,
							R.string.no_class_selected_do_you_need_to_load_lessons,
							Toast.LENGTH_LONG).show();
					finish();
					return;
				}
			}
		}
		// ---------------------------------------------------------------------------------------------
		// here if on larger screen, LessonListFragment displayed and new set of
		// lessons displayed
		if (requestCode == LessonSelectionDialog.SELECT_TO_CLASS) {
			if (resultCode != Activity.RESULT_OK
					&& languageSettings.getClassId() == -1) {
				Toast.makeText(this,
						R.string.no_class_selected_do_you_need_to_load_lessons,
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				// tell LessonListFragment to start to list the new set of lessons
				// and remove the lessonFragment until lesson selected in list
				if (languageSettings.getClassId() != -1) {
					// if very first time in after initial load of first class
					// lessonListFragment won't have been created yet
					if (lessonListFragment == null) {
						addFragments();
					} else {
						lessonListFragment.turnOffSelectedHightlight();
						lessonListFragment.displayLessonList();
						removeLessonFragment();
						return;
					}
				} else {
					Toast.makeText(this,
							R.string.no_class_selected_use_menu_to_select,
							Toast.LENGTH_LONG).show();
					return;
				}
			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				// User cancelled so see if already on lesson and if so stay on it
				if (-1 != languageSettings.getLessonId())
					return;
				else {
					// Toast.makeText(this,
					// R.string.no_class_selected_use_menu_to_select,
					// Toast.LENGTH_LONG).show();

					Toast.makeText(this,
							R.string.no_class_selected_do_you_need_to_load_lessons,
							Toast.LENGTH_LONG).show();
					finish();
					return;
				}
			}
		}
	}

	// Remove LessonPhraseFragment or Audio/VideoPlayerFragment until lesson
	// selected
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

	// Use this both to see if lesson still exists (the teacher/language may have
	// been deleted)
	private void loadLesson(Long lessonId) {
		lesson = null;
		if (lessonId != null && lessonId > -1) {
			daoSession = LanguageApplication.getInstance().getDaoSession();
			LessonDao lessonDao = daoSession.getLessonDao();
			lesson = lessonDao.load(lessonId);
		}
	}

	// Use this both to see if lesson still exists (the teacher/language may have
	// been deleted)
	private void loadClassName(Long classId) {
		className = null;
		if (classId != null && classId > -1) {
			daoSession = LanguageApplication.getInstance().getDaoSession();
			ClassNameDao classNameDao = daoSession.getClassNameDao();
			className = classNameDao.load(classId);
		}
	}

}
