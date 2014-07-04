package com.fisincorporated.languagetutorial;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.languagetutorial.LanguagePhraseAdapter.IAssignLessonLineText;
import com.fisincorporated.languagetutorial.LanguagePhraseAdapter.LanguagePhraseViewHolder;
import com.fisincorporated.languagetutorial.LanguagePhraseLoader.LanguagePhraseLoadListener;
import com.fisincorporated.languagetutorial.LanguagePhrasePlayer.LanguagePhrasePlayerListener;
import com.fisincorporated.languagetutorial.db.ClassName;
import com.fisincorporated.languagetutorial.db.ClassNameDao;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.LanguagePhrase;
import com.fisincorporated.languagetutorial.db.Lesson;
import com.fisincorporated.languagetutorial.db.LessonDao;
import com.fisincorporated.languagetutorial.db.LessonPhrase;
import com.fisincorporated.languagetutorial.db.LessonPhraseDao;
import com.fisincorporated.languagetutorial.db.TeacherLanguage;
import com.fisincorporated.languagetutorial.db.TeacherLanguageDao;
import com.fisincorporated.languagetutorial.interfaces.IPauseMedia;
import com.fisincorporated.languagetutorial.interfaces.IStartNewLesson;
import com.fisincorporated.languagetutorial.utility.LanguagePhraseRequest;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;
import com.fisincorporated.languagetutorial.utility.PhrasePlayRequest;

import de.greenrobot.dao.query.Query;

public class LessonPhraseFragment extends ListFragment implements
		IAssignLessonLineText, IStartNewLesson, IPauseMedia {
	private static final String TAG = "LessonPhraseFragment";
	private DaoSession daoSession;
	private LessonDao lessonDao;
	private LessonPhraseDao lessonPhraseDao;
	private Lesson lesson;
	private TeacherLanguageDao teacherLanguageDao;
	private ClassNameDao classNameDao;
	private Query<Lesson> lessonQuery = null;
	private Query<LessonPhrase> lessonPhraseQuery = null;
	private Long teacherId;
	private String teacherName;
	// private Long teacherLanguageId;

	private Long classId;
	private String classTitle;
	private Long lessonId;
	private String lessonTitle = "";
	//private LessonSelectionDialog lessonSelectionDialog;
	private ArrayList<LessonPhrase> lessonPhraseList = new ArrayList<LessonPhrase>();
	private ArrayList<LanguagePhrase> learningLanguagePhraseList = new ArrayList<LanguagePhrase>();
	private ArrayList<LanguagePhrase> knownLanguagePhraseList = new ArrayList<LanguagePhrase>();
	private boolean[] learningTextDisplayed;
	private int lessonLineIndex = 0;
	private Button btnBack;
	private Button btnForward;
	private Button btnRepeat;
	private LanguagePhraseAdapter languagePhraseAdapter;
	private LanguageDialogFragment dialog;
	private TextView tvHeader;
	private Resources res;
	// values for dialog
	// private static final int SELECT_LESSON = 1;
	private static final int ERROR = 2;
	private static final int AUDIO_ERROR = 3;
	public static final int LESSON_AUDIO_OPTIONS = 4;
	public static final int LESSON_OPTIONS = 5;

	// used to store values to SharedPreferences file
	private static LanguageSettings languageSettings;

	private String learningLanguageDirectory = "";
	private String knownLanguageDirectory = "";
	private boolean errorInMedia = false;
	private boolean firstTimeIn = true;

	private LanguagePhraseLoader languagePhraseLoader;
	private LanguagePhrasePlayer languagePhrasePlayer;
	private static int queueAdditional = 2;

	// setting on how to run the lesson, values from stored preferences
	private boolean playAudioWhenAvailable;
	private boolean shouldRepeatPhrase;
	private int repeatXTimes;
	private float phraseDelayByDuration;
	private float waitXSeconds;
	private boolean playKnownPhrase;
	private boolean playBeforeLearningPhrase;
	private boolean playAfterLearningPhrase;
	// private boolean stepThruLessonAutomatically;
	private boolean displayKnownTextWhenAvailable;
	private boolean displayKnownTextBeforeLearningPhrase;
	// private boolean displayKnownTextAfterLearningPhrase;
	private float phraseTextSize;
	// Declare the in and out animations and initialize them
	private Animation inText;
	private Animation outText;
	private int[] backgroundColors;
	private int backgroundColor;
	private StringBuilder sb = new StringBuilder();
	private String lineSeparator = System.getProperty("line.separator");
	private int[] baseMargins = new int[] { 0, 0, 0, 0 };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		// set the volume control for audio and not ringer
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setupDatabaseAccess();
		getAndInitLanguageSettings();
		
		res = getResources();
		
		// This controls background load of language phrases to be displayed
		languagePhraseLoader = new LanguagePhraseLoader(new Handler());
		languagePhraseLoader.setListener(new LanguagePhraseLoadListener() {
			@Override
			public void onLanguagePhraseAdded(long fromLessonId,
					int forLessonOrder, LanguagePhrase learningLanguagePhrase,
					LanguagePhrase knownLanguagePhrase) {
				// if still on same lesson id, go ahead and add it
				// only add if still on same lesson and play related media if this
				// is the phrase you are currently on
				if (fromLessonId == lessonId) {
					addLanguagePhraseToDisplay(learningLanguagePhrase,
							knownLanguagePhrase,
							forLessonOrder == lessonLineIndex ? true : false);
					setBackButtonVisibility();
				}
			}

			@Override
			public void onErrorOccurred() {
				showDialog(res.getString(
						R.string.language_phrase_xref_missing_for_lesson_phrase2,
						lessonLineIndex, teacherName, classTitle, lessonTitle),
						ERROR, R.string.ok, -1, -1);
			}
		});
		// getLooper goes after start to ensure thread is ready
		languagePhraseLoader.start();
		languagePhraseLoader.getLooper();
		Log.i(TAG, "LanguagePhraseLoader background thread started");

		// This controls background playing of any audio that goes with the text
		languagePhrasePlayer = new LanguagePhrasePlayer(new Handler(),
				getActivity());
		languagePhrasePlayer.setListener(new LanguagePhrasePlayerListener() {
			@Override
			public void onErrorOccurred(String errorMessage) {
				// showDialog(errorMessage, ERROR, R.string.ok, -1, -1);
				// errorInMedia = true;
			}

		});
		languagePhrasePlayer.start();
		languagePhrasePlayer.getLooper();
		Log.i(TAG, "LanguagePhrasePlayer background thread started");

		loadLessonOptions();
		inText = AnimationUtils.loadAnimation(getActivity(),
				android.R.anim.slide_in_left);
		outText = AnimationUtils.loadAnimation(getActivity(),
				android.R.anim.slide_out_right);

		backgroundColors = new int[] { res.getColor(R.color.light_yellow),
				res.getColor(R.color.light_green),
				res.getColor(R.color.light_blue), res.getColor(R.color.light_red) };

	}

	private void getAndInitLanguageSettings() {
		languageSettings = LanguageSettings.getInstance(getActivity());
		// always start at beginning of lesson
		lessonLineIndex = -1;
		languageSettings.setLastLessonPhraseLine(lessonLineIndex).commit();
	}

	private void setupDatabaseAccess() {
		daoSession = LanguageApplication.getInstance().getDaoSession();
		lessonDao = daoSession.getLessonDao();
		lessonPhraseDao = daoSession.getLessonPhraseDao();
		teacherLanguageDao = daoSession.getTeacherLanguageDao();
		classNameDao = daoSession.getClassNameDao();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// lesson_list contains 'listview'
		View view = inflater.inflate(R.layout.lesson_contents, container, false);
		languagePhraseAdapter = new LanguagePhraseAdapter(getActivity(), this,
				R.layout.phrase_detail, learningLanguagePhraseList);
		setListAdapter(languagePhraseAdapter);

		btnBack = (Button) view.findViewById(R.id.btnPrevious);
		btnBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				languagePhrasePlayer.clearQueue();
				removeLastPhraseFromDisplay();
				getListView().smoothScrollToPosition(
						learningLanguagePhraseList.size());
				setBackButtonVisibility();
			}
		});

		btnForward = (Button) view.findViewById(R.id.btnNext);
		btnForward.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// next lesson phrase
				languagePhrasePlayer.clearQueue();
				addNextPhraseToDisplay();
				getListView().smoothScrollToPosition(
						learningLanguagePhraseList.size());
				setBackButtonVisibility();
			}
		});

		btnRepeat = (Button) view.findViewById(R.id.btnRepeat);
		btnRepeat.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// repeat the last lesson phrase
				if (!errorInMedia && playAudioWhenAvailable) {
					repeatLearningPhrase(learningLanguageDirectory,
							learningLanguagePhraseList.get(lessonLineIndex)
									.getMediaFile());
				}
			}
		});

		tvHeader = (TextView) view.findViewById(R.id.tvHeader);
		tvHeader.setText(lessonTitle);
		tvHeader.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO add learning language
				Toast.makeText(
						getActivity(),
						String.format(
								res.getString(R.string.teacher_class_lesson_title),
								teacherName, classTitle, lessonTitle),
						Toast.LENGTH_LONG).show();
			}
		});

		ListView lv = (ListView) view.findViewById(android.R.id.list);
		lv.setBackgroundResource(R.drawable.rounded_edges);

		firstTimeIn = true;
		return view;
	}

	private void setBackButtonVisibility() {
		if (btnBack != null) {
			if (lessonLineIndex > 0) {
				btnBack.setVisibility(View.VISIBLE);
			} else {
				btnBack.setVisibility(View.INVISIBLE);
			}
		}
	}

	// Add the menu - Will add to any menu items added by parent activity
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// Add the menu
		inflater.inflate(R.menu.lesson_phrase_menu, menu);
	}

	// handle the selected menu option
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.restart_lesson:
			restartLesson();
			return true;
		case R.id.select_lesson_options:
			displayLessonOptions();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// eventually replace with logic to replay audio, display known language
	// phrase,
	// etc.
	public void onListItemClick(ListView l, View v, int position, long id) {
		TextSwitcher txtswtchrTvPhrase = (TextSwitcher) v
				.findViewById(R.id.txtswtchrTvPhrase);
		txtswtchrTvPhrase.setInAnimation(inText);
		txtswtchrTvPhrase.setOutAnimation(outText);
		Log.i(TAG, ((TextView) txtswtchrTvPhrase.getChildAt(0)).getText()
				.toString());
		if (displayKnownTextWhenAvailable) {
			return;
		}
		if (learningTextDisplayed[position] == true) {
			txtswtchrTvPhrase
					.setBackgroundColor(getBackgroundColor(lessonPhraseList.get(
							position).getSpeaker()));
			txtswtchrTvPhrase.setText(knownLanguagePhraseList.get(position)
					.getWrittenPhrase());
		} else {
			txtswtchrTvPhrase.setText(learningLanguagePhraseList.get(position)
					.getWrittenPhrase());
		}

		learningTextDisplayed[position] = !learningTextDisplayed[position];
	}

	public void resetToFirstTime() {
		firstTimeIn = true;
		languageSettings.setLastLessonPhraseLine(-1);
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		if (firstTimeIn) {
			getInitialLessonPhrase();
			firstTimeIn = false;
		}
		// show or not based on option
		btnRepeat.setVisibility(playAudioWhenAvailable ? View.VISIBLE
				: View.INVISIBLE);
		// languagePhraseAdapter.notifyDataSetChanged();
	}

	// starting lesson from scratch
	// the lesson phrases must be loaded prior to calling this method
	private void restartLesson() {
		learningLanguagePhraseList.clear();
		knownLanguagePhraseList.clear();
		lessonLineIndex = -1;
		addNextPhraseToDisplay();
	}

	private void getMediaDirectories() {
		ClassName className = null;
		TeacherLanguage teacherLanguage = null;
		if (classId != null && classId != -1) {
			className = classNameDao.queryBuilder()
					.where(ClassNameDao.Properties.Id.eq(classId)).unique();
		}
		if (className != null) {
			teacherLanguage = teacherLanguageDao
					.queryBuilder()
					.where(
							TeacherLanguageDao.Properties.Id.eq(className
									.getTeacherLanguageId())).unique();
		}
		if (teacherLanguage != null) {
			// switch to where the media was loaded
			String mediaDirectory = languageSettings.getMediaDirectory();
			learningLanguageDirectory = mediaDirectory + "/"
					+ teacherLanguage.getLearningLanguageMediaDirectory();
			knownLanguageDirectory = mediaDirectory + "/"
					+ teacherLanguage.getKnownLanguageMediaDirectory();
			//learningLanguageDirectory = Environment.getExternalStorageDirectory()
			//		+ "/" + Environment.DIRECTORY_DOWNLOADS + "/"
			//		+ teacherLanguage.getLearningLanguageMediaDirectory();
			//knownLanguageDirectory = Environment.getExternalStorageDirectory()
			//		+ "/" + Environment.DIRECTORY_DOWNLOADS + "/"
			//		+ teacherLanguage.getKnownLanguageMediaDirectory();
		}
	}

	private void getInitialLessonPhrase() {
		getCurrentLessonInfo();
		if (teacherId == -1 || classId == -1 || lessonId == -1) {
			// first make sure languages loaded, if not then go to language
			// maintenace
			if (teacherLanguagesExist()) {
				// have the user select a teach/class/lesson
				// Here if at least one teacher/language loaded to database
				// displayLessonSelectionDialog();
				Toast.makeText(getActivity(),
						R.string.a_lesson_must_be_selected_first, Toast.LENGTH_LONG)
						.show();
				return;
			} else {
				goToLanguageMaintenance();
				getActivity().finish();
				return;
			}
		}
		tvHeader.setText(res.getString(R.string.lesson) + ": " + lessonTitle);
		if (lessonPhraseList.size() == 0) {
			loadLessonPhrases(lessonId);
			// if still no lesson phrases found then perhaps lesson file
			// deleted/reloaded or ...
			// Reset lesson info in shared preferences and have user define where
			// they want to be
			if (lessonPhraseList.size() == 0) {
				resetLessonInfo();
				Toast.makeText(getActivity(),
						R.string.no_lesson_phrases_defined_for_this_lesson,
						Toast.LENGTH_LONG).show();
				// displayLessonSelectionDialog();
				return;
			}
		}
		getMediaDirectories();
		// here if you have phrases for the lesson
		// pick up where you left off - either at start of lesson or
		// somewhere in middle
		lessonLineIndex = languageSettings.getLastLessonPhraseLine();
		if (lessonLineIndex < 0) {
			lessonLineIndex = 0;
			// addNextPhraseToDisplay();
		}
		// perhaps resumed from saved lesson so load up display array to where you
		// should be
		if (learningLanguagePhraseList.size() <= lessonLineIndex) {
			for (int i = learningLanguagePhraseList.size(); i <= lessonLineIndex
					&& i < lessonPhraseList.size(); ++i) {
				// displayNextLessonPhrase(i, i < lessonLineIndex ? false:true );
				requestBackgroundLoad(i, LanguagePhraseLoader.LOAD_LANGUAGE_PHRASE);

			}
		}
		getListView().smoothScrollToPosition(learningLanguagePhraseList.size());
		preloadNextRequests(lessonLineIndex);
	}

	private void addNextPhraseToDisplay() {
		// see if phrase has already been set, if so display it
		if (lessonLineIndex < lessonPhraseList.size() - 1) {
			++lessonLineIndex;
			// get the next languagexref for this phrase
			// displayNextLessonPhrase(lessonLineIndex , true);
			requestBackgroundLoad(lessonLineIndex,
					LanguagePhraseLoader.LOAD_LANGUAGE_PHRASE);
			preloadNextRequests(lessonLineIndex);

			languageSettings.setLastLessonPhraseLine(lessonLineIndex);
			languageSettings.commit();
		} else {
			if (lessonLineIndex <= 0 || lessonPhraseList.size() == 0) {
				Toast.makeText(getActivity(),
						R.string.looks_like_an_error_loading_lesson,
						Toast.LENGTH_LONG).show();
			} else if (lessonLineIndex > 0) {
				// eventually replace this with a dialog
				// with congratulations and see if user wants to go to
				// next lesson, restart this lesson, or exit
				Toast.makeText(getActivity(),
						R.string.congratulations_end_of_lesson, Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private void requestBackgroundLoad(int lessonLine, int loadRequestType) {
		// loadRequestType should be LanguagePhraseLoader.LOAD_LANGUAGE_PHRASE or
		// LanguagePhraseLoader.PRELOAD_LANGUAGE_PHRASE
		queueLoadRequest(lessonLine, LanguagePhraseLoader.LOAD_LANGUAGE_PHRASE);
	}

	// queue up the next couple of lesson lines.
	private void preloadNextRequests(int currentLessonIndex) {
		for (int i = currentLessonIndex + 1; i < lessonPhraseList.size() - 1
				&& i <= currentLessonIndex + queueAdditional; ++i) {
			queueLoadRequest(i, LanguagePhraseLoader.PRELOAD_LANGUAGE_PHRASE);
		}
	}

	private void queueLoadRequest(int lessonLine, int loadRequestType) {
		LanguagePhraseRequest languagePhraseRequest = new LanguagePhraseRequest(
				lessonId, lessonLine, lessonPhraseList.get(lessonLine)
						.getLanguageXrefId());
		languagePhraseLoader.queueLoadRequest(loadRequestType,
				languagePhraseRequest);
	}

	// add the phrase to the adapter and redraw screen
	// also play the audio if it exists
	private void addLanguagePhraseToDisplay(
			LanguagePhrase learningLanguagePhrase,
			LanguagePhrase knownLanguagePhrase, boolean playMedia) {
		learningLanguagePhraseList.add(learningLanguagePhrase);
		knownLanguagePhraseList.add(knownLanguagePhrase);
		// play the corresponding media file if there is one.
		languagePhraseAdapter.notifyDataSetChanged();
		if (isVisible() && playMedia && !errorInMedia && playAudioWhenAvailable) {
			// playLessonPhraseAudio(learningLanguageDirectory,learningLanguagePhrase.getAudioFile());
			queueUpAudio(learningLanguageDirectory, learningLanguagePhraseList
					.get(lessonLineIndex).getMediaFile(), knownLanguageDirectory,
					knownLanguagePhraseList.get(lessonLineIndex).getMediaFile());
		}

	}

	// You should only get here if playing the audio was requested (via options
	// menu)
	private void queueUpAudio(String learningLanguageDirectory,
			String learningLanguageAudioFile, String knownLanguageDirectory,
			String knownLanguageAudioFile) {
		PhrasePlayRequest phrasePlayRequest;
		// see if you want to play the known phrase first
		if (playKnownPhrase && playBeforeLearningPhrase
				&& !knownLanguageAudioFile.equals("")) {
			phrasePlayRequest = new PhrasePlayRequest(knownLanguageDirectory,
					knownLanguageAudioFile, 0, 1000);
			languagePhrasePlayer.queuePlayRequest(
					LanguagePhrasePlayer.PLAY_PHRASE, phrasePlayRequest);
		}
		// Play the phrase to be learned
		if (!learningLanguageAudioFile.equals("")) {
			phrasePlayRequest = new PhrasePlayRequest(learningLanguageDirectory,
					learningLanguageAudioFile, phraseDelayByDuration,
					(long) waitXSeconds * 1000);
			languagePhrasePlayer.queuePlayRequest(
					LanguagePhrasePlayer.PLAY_PHRASE, phrasePlayRequest);
			// and repeat it if options specify it
			for (int i = 1; i <= repeatXTimes && shouldRepeatPhrase; ++i) {
				phrasePlayRequest = new PhrasePlayRequest(
						learningLanguageDirectory, learningLanguageAudioFile,
						phraseDelayByDuration, (long) waitXSeconds * 1000);
				languagePhrasePlayer.queuePlayRequest(
						LanguagePhrasePlayer.PLAY_PHRASE, phrasePlayRequest);
			}
		}
		// see if you want to play the known phrase after the phrase to be learned
		if (playKnownPhrase && playAfterLearningPhrase
				&& !knownLanguageAudioFile.equals("")) {
			phrasePlayRequest = new PhrasePlayRequest(knownLanguageDirectory,
					knownLanguageAudioFile, phraseDelayByDuration,
					(long) waitXSeconds * 1000);
			languagePhrasePlayer.queuePlayRequest(
					LanguagePhrasePlayer.PLAY_PHRASE, phrasePlayRequest);
		}

	}

	// You should only get here if playing the audio was requested and the repeat
	// button pressed
	private void repeatLearningPhrase(String learningLanguageDirectory,
			String learningLanguageAudioFile) {
		PhrasePlayRequest phrasePlayRequest;
		// Play the phrase to be learned
		if (!learningLanguageAudioFile.equals("")) {
			phrasePlayRequest = new PhrasePlayRequest(learningLanguageDirectory,
					learningLanguageAudioFile, 0, 0);
			languagePhrasePlayer.queuePlayRequest(
					LanguagePhrasePlayer.PLAY_PHRASE, phrasePlayRequest);

		}
	}

	private void removeLastPhraseFromDisplay() {
		// remove the last phrase from the display
		// back up one position
		if (lessonLineIndex > 0) {
			learningLanguagePhraseList.remove(lessonLineIndex);
			knownLanguagePhraseList.remove(lessonLineIndex);
			--lessonLineIndex;
			languageSettings.setLastLessonPhraseLine(lessonLineIndex);
			languageSettings.commit();
			languagePhraseAdapter.notifyDataSetChanged();
			// playLessonPhraseAudio(learningLanguageDirectory,learningLanguagePhraseList.get(lessonLineIndex).getAudioFile());
			// note the if doesn't include playMedia as you will always play it
			// (assuming of course all other conditions are true)
			if (isVisible() && !errorInMedia && playAudioWhenAvailable) {
				queueUpAudio(learningLanguageDirectory, learningLanguagePhraseList
						.get(lessonLineIndex).getMediaFile(), knownLanguageDirectory,
						knownLanguagePhraseList.get(lessonLineIndex).getMediaFile());
			}
		}
	}

	private void loadLessonPhrases(Long lessonId) {
		if (lessonQuery == null) {
			lessonQuery = lessonDao.queryBuilder()
					.where(LessonDao.Properties.Id.eq(lessonId)).build();
		} else {
			lessonQuery.setParameter(0, lessonId);
		}
		lesson = lessonQuery.unique();

		if (lessonPhraseQuery == null) {
			lessonPhraseQuery = lessonPhraseDao.queryBuilder()
					.where(LessonPhraseDao.Properties.LessonId.eq(lessonId))
					.orderAsc(LessonPhraseDao.Properties.LessonOrder).build();
		} else {
			lessonPhraseQuery.setParameter(0, lessonId);
		}
		lessonPhraseList.clear();
		lessonPhraseList.addAll((ArrayList<LessonPhrase>) lessonPhraseQuery
				.list());
		setLearningTextDisplay(lessonPhraseList.size());
	}

	private void setLearningTextDisplay(int size) {
		learningTextDisplayed = new boolean[size];
		for (int i = 0; i < size; ++i) {
			learningTextDisplayed[i] = true;
		}

	}

	private void displayLessonOptions() {
		Intent intent = new Intent(getActivity(), LessonOptionActivity.class);
		startActivityForResult(intent, LESSON_OPTIONS);
	}

	private void getCurrentLessonInfo() {
		teacherId = languageSettings.getTeacherId();
		teacherName = languageSettings.getTeacherName();
		classId = languageSettings.getClassId();
		classTitle = languageSettings.getClassTitle();
		lessonId = languageSettings.getLessonId();
		lessonTitle = languageSettings.getLessonTitle();
		// teacherLanguageId = languageSettings.getTeacherLanguageId();

	}

	private void resetLessonInfo() {
		teacherId = -1l;
		languageSettings.setTeacherId(teacherId);
		teacherName = "";
		languageSettings.setTeacherName(teacherName);
		classId = -1l;
		languageSettings.setClassId(classId);
		classTitle = "";
		languageSettings.setClassTitle(classTitle);
		lessonId = -1l;
		languageSettings.setLessonId(lessonId);
		lessonTitle = "";
		languageSettings.setLessonTitle(lessonTitle);
		lessonLineIndex = -1;
		languageSettings.setLastLessonPhraseLine(lessonLineIndex);
		languageSettings.commit();

	}

	public void showDialog(String loadMsg, int requestCode, int yesResource,
			int noResource, int cancelResource) {
		dialog = LanguageDialogFragment.newInstance(-1, loadMsg, yesResource,
				noResource, cancelResource);
		dialog.setTargetFragment(LessonPhraseFragment.this, requestCode);
		dialog.show(getActivity().getSupportFragmentManager(), "ErrorDialog");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == LESSON_OPTIONS) {
			loadLessonOptions();
			languagePhraseAdapter.notifyDataSetChanged();
			return;
		}
		if (requestCode == AUDIO_ERROR) {
			// an audio error occurred. Keep on trucking...
			// at some point provide code to display error info and option to email
			// errors to me
			return;
		}
		if (requestCode == ERROR) {
			// some major error occurred. So leave this activity
			getActivity().finish();
			return;
		}
		
	}

	public void startNewLesson() {
		getCurrentLessonInfo();
		tvHeader.setText(res.getString(R.string.lesson) + ": " + lessonTitle);
		loadLessonPhrases(lessonId);
		getMediaDirectories();
		restartLesson();
	}

	// Get options on how lesson should be conducted.
	// Add text options
	public void loadLessonOptions() {
		playAudioWhenAvailable = languageSettings.getPlayAudioWhenAvailable();
		shouldRepeatPhrase = languageSettings.getRepeatPhrase();
		repeatXTimes = languageSettings.getRepeatXTimes();
		phraseDelayByDuration = languageSettings.getPhraseDelayByDuration();
		waitXSeconds = languageSettings.getWaitXSeconds();
		playKnownPhrase = languageSettings.getPlayKnownPhrase();
		playBeforeLearningPhrase = languageSettings.getPlayBeforeLearningPhrase();
		playAfterLearningPhrase = languageSettings.getPlayAfterLearningPhrase();
		// stepThruLessonAutomatically =
		// languageSettings.getStepThroughLessonAutomatically();
		displayKnownTextWhenAvailable = languageSettings
				.getDisplayKnownTextWhenAvailable();
		displayKnownTextBeforeLearningPhrase = languageSettings
				.getDisplayKnownTextBeforeLearningPhrase();
		// displayKnownTextAfterLearningPhrase =
		// languageSettings.getDisplayKnownTextAfterLearningPhrase();
		phraseTextSize = languageSettings.getPhraseTextSize();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// if no Fragment view then clear queue as no place to display lesson
		// Note that you haven't destroyed languagePhraseLoader thread yet.
		languagePhraseLoader.clearQueue();
		languagePhrasePlayer.clearQueue();
		Log.i(TAG, "onDestroyView  cleared loader and player queues");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Now you can terminate thread as you are destroying fragment
		languagePhraseLoader.quit();
		languagePhrasePlayer.quit();
		Log.i(TAG, "Background threads destroyed");
	}

	@Override
	public void onStop() {
		// if (mediaPlayer != null) {
		// mediaPlayer.release();
		// mediaPlayer = null;
		// }
		super.onStop();
	}

	// Was passing more and more variables to LanguagePhraseAdapter so
	// implemented interface
	// and moved assignment logic to here
	@Override
	public void onAssignLessonLineText(LanguagePhraseViewHolder viewHolder,
			int position) {
		TextView tv;
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.llPhraseDetail
				.getLayoutParams();
		viewHolder.txtswtchrTvPhrase.setCurrentText(getDisplayText(position));
		viewHolder.tvNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				phraseTextSize);
		if (!playAudioWhenAvailable){
			viewHolder.audioIconImage.setVisibility(View.VISIBLE);
			viewHolder.audioIconImage.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i(TAG, "Onclick on ImageView position" + v.getTag());
					playLearningLanguagePhrase((Integer)v.getTag());
					
				}});
		}
		else {
			viewHolder.audioIconImage.setVisibility(View.GONE);
		}
			
		 
		// if lesson is number lesson
		if (lesson != null
				&& lesson.getLessonType().equalsIgnoreCase(GlobalValues.NUMBER)) {
			String englishNumeral = learningLanguagePhraseList.get(position)
					.getEnglishNumeral();

			params.setMargins(0, 0, 0, 0);
			if (englishNumeral == null || englishNumeral.equals("")) {
				viewHolder.tvNumber.setText("");
				viewHolder.tvNumber.setVisibility(View.GONE);
			} else {
				viewHolder.tvNumber.setText(englishNumeral.trim());
				viewHolder.tvNumber.setVisibility(View.VISIBLE);
				viewHolder.tvNumber.setBackgroundColor(backgroundColors[1]);
			}
		} else {
			// anything else
			viewHolder.tvNumber.setText("");
			viewHolder.tvNumber.setVisibility(View.GONE);
			params.weight = 0;
			int[] newMargins = calcMargins(lessonPhraseList.get(position)
					.getSpeaker());
			// substitute parameters for left and right
			params.setMargins(newMargins[0], 0, newMargins[2], 0);
			viewHolder.llPhraseDetail.setLayoutParams(params);
		}
		backgroundColor = getBackgroundColor(lessonPhraseList.get(position)
				.getSpeaker());
		// set both textview's in textswitcher as you don't know which will be
		// used on initial display
		tv = ((TextView) viewHolder.txtswtchrTvPhrase.getChildAt(0));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, phraseTextSize);
		tv.setBackgroundColor(backgroundColor);
		// also set text for alternate view (which can be main view if did
		// textswitch on prior view)
		if (null != (tv = ((TextView) viewHolder.txtswtchrTvPhrase.getChildAt(1)))) {
			tv.setBackgroundColor(backgroundColor);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, phraseTextSize);
		}

	}
	
	private void playLearningLanguagePhrase(int position){
		PhrasePlayRequest phrasePlayRequest = new PhrasePlayRequest(learningLanguageDirectory,
				learningLanguagePhraseList.get(position).getMediaFile(), 0,	0);
		languagePhrasePlayer.queuePlayRequest(
				LanguagePhrasePlayer.PLAY_PHRASE, phrasePlayRequest);
	}

	private String getDisplayText(int position) {
		sb.setLength(0);
		if (displayKnownTextWhenAvailable) {
			if (displayKnownTextBeforeLearningPhrase) {
				sb.append(knownLanguagePhraseList.get(position).getWrittenPhrase()
						+ lineSeparator
						+ learningLanguagePhraseList.get(position).getWrittenPhrase());
				return sb.toString();
			} else {
				sb.append(learningLanguagePhraseList.get(position)
						.getWrittenPhrase()
						+ lineSeparator
						+ knownLanguagePhraseList.get(position).getWrittenPhrase());
				return sb.toString();
			}

		}
		return learningLanguagePhraseList.get(position).getWrittenPhrase();
	}

	// margins are left,top,right, bottom
	// just need to change left and right
	private int[] calcMargins(int speaker) {
		int newMargins[] = baseMargins.clone();

		if (speaker <= 0) {
			return newMargins;
		}
		int remainder = (speaker - 1) % 2;
		remainder = (speaker - 1) % 2;
		if (remainder == 0) {
			newMargins[2] = 30;
		} else {
			newMargins[0] = 30;
		}
		return newMargins;

	}

	// don't count on nice numbering of speakers
	private int getBackgroundColor(int speaker) {
		// white will be default.
		int backgroundColor = res.getColor(R.color.white);
		if (speaker <= 0) {
			return backgroundColor;
		}
		return backgroundColors[(speaker - 1) % backgroundColors.length];

	}

	// check to make sure at least 1 teacher/language exists
	// if not then you want to go to language maintenace activity
	private boolean teacherLanguagesExist() {
		String sql;
		Cursor cursor;
		int teacherCount = 0;
		sql = "select count(*) from " + TeacherLanguageDao.TABLENAME;
		cursor = daoSession.getDatabase().rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			teacherCount = cursor.getInt(0);
		}
		cursor.close();
		return (teacherCount > 0);
	}

	private void goToLanguageMaintenance() {
		Intent intent = new Intent(getActivity(),
				LanguageMaintenanceActivity.class);
		getActivity().startActivity(intent);
	}

	
	// for IPauseMedia interface
	@Override
	public void pauseMedia() {
		languagePhrasePlayer.clearQueue();
	}

}
