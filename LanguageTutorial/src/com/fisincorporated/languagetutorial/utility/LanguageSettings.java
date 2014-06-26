package com.fisincorporated.languagetutorial.utility;

import com.fisincorporated.languagetutorial.GlobalValues;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.TextView;

//TODO return sPrefs after set statements so you can chain series of sets together
// followed by commit;
@SuppressLint("CommitPrefEdits")
public class LanguageSettings {

	private static SharedPreferences sPrefs;
	private static LanguageSettings languageSettings = null;
	private static Editor editor;
	private static Context sContext = null;

	// values used in preferences file
	private static final String PREFS_FILE = "LanguageTutorial";
	private static final String TEACHER_ID = "TeacherId";
	private static final String TEACHER_NAME = "TeacherName";
	private static final String TEACHER_URL = "TeacherURL";
	private static final String CLASS_ID = "ClassId";
	private static final String CLASS_TITLE = "ClassTitle";
	private static final String CLASS_DESCRIPTION = "ClassDescription";
	private static final String TEACHER_LANGUAGE_ID = "TeacherLanguageId";

	private static final String LESSON_ID = "LessonId";
	private static final String LESSON_TITLE = "LessonTitle";
	private static final String LESSON_DESCRIPTION = "LessonDescription";
	private static final String LESSON_PHRASE_ID = "LessonId";
	private static final String LESSON_TYPE="LessonType";

	private static final String LEARNING_LANGUAGE_ID = "LearningLanguageId";
	private static final String LEARNING_LANGUAGE_NAME = "LearningLanguageName";
	private static final String LEARNING_LANGUAGE_MEDIA_DIRECTORY = "LearningLanguageMediaDirectory";
	private static final String KNOWN_LANGUAGE_ID = "KnowLanguageId";
	private static final String KNOWN_LANGUAGE_NAME = "KnowLanguageName";
	private static final String KNOWN_LANGUAGE_MEDIA_DIRECTORY = "KnowLanguageNameMediaDirectory";
	private static final String LAST_LESSON_PHRASE_LINE = "LastLessonPhraseLine";
	private static final String PLAY_AUDIO_WHEN_AVAILABLE = "PlayAudioWhenAvailable";
	private static final String REPEAT_PHRASE = "RepeatPhrase";
	private static final String REPEAT_X_TIMES = "RepeatXTimes";
	private static final String PHRASE_DELAY_BY_DURATION = "PhraseDelayByDuration";
	private static final String WAIT_X_SECONDS = "WaitXSeconds";
	private static final String PLAY_KNOWN_PHRASE = "PlayKnownPhrase";
	private static final String PLAY_BEFORE_LEARNING_PHRASE = "PlayBeforeLearningPhrase";
	private static final String PLAY_AFTER_LEARNING_PHRASE = "PlayAfterLearningPhrase";
	private static final String STEP_THROUGH_LESSON_AUTOMATICALLY = "StepThroughLessonAutomatically";

	private static final String DISPLAY_KNOWN_TEXT_WHEN_AVAILABLE = "DisplayKnownTextWhenAvailable";
	private static final String DISPLAY_KNOWN_TEXT_BEFORE_LEARNING_PHRASE = "DisplayKnownTextBeforeLearningPhrase";
	private static final String DISPLAY_KNOWN_TEXT_AFTER_LEARNING_PHRASE = "DisplayKnownTextAfterLearningPhrase";
	
	private static final String PHRASE_TEXT_SIZE = "PhraseTextSize";

	// For loading and deleting language files
	private static final String MAINTENANCE_TYPE = "MAINTENANCE_TYPE";
	private static final String MAINTENANCE_STATUS = "MAINTENANCE_STATUS";
	private static final String MAINTENANCE_DETAILS = "MAINTENANCE_DETAILS";
	private static final String MEDIA_DIRECTORY = "MediaDirectory";

	public static LanguageSettings getInstance(Context context) {
		if (languageSettings == null) {
			languageSettings = new LanguageSettings(context);
			 sContext = context;
		}
		return languageSettings;
	}

	private LanguageSettings(Context context) {
		sPrefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
		editor = sPrefs.edit();
	}

	public Long getTeacherId() {
		return sPrefs.getLong(TEACHER_ID, -1);
	}

	public LanguageSettings setTeacherId(Long id) {
		if (id == null)
			id = -1l;
		editor.putLong(TEACHER_ID, id);
		return this;
	}

	public String getTeacherName() {
		return sPrefs.getString(TEACHER_NAME, "");
	}

	public LanguageSettings setTeacherName(String value) {
		if (value == null)
			value = "";
		editor.putString(TEACHER_NAME, value);
		return this;
	}

	public String getTeacherURL() {
		return sPrefs.getString(TEACHER_URL, "");
	}

	public LanguageSettings setTeacherURL(String value) {
		if (value == null)
			value = "";
		editor.putString(TEACHER_URL, value);
		return this;
	}

	public Long getClassId() {
		return sPrefs.getLong(CLASS_ID, -1);
	}

	public LanguageSettings setClassId(Long id) {
		if (id == null)
			id = -1l;
		editor.putLong(CLASS_ID, id);
		return this;
	}

	public String getClassTitle() {
		return sPrefs.getString(CLASS_TITLE, "");
	}

	public LanguageSettings setClassTitle(String value) {
		if (value == null)
			value = "";
		editor.putString(CLASS_TITLE, value);
		return this;
	}

	public String getClassDescription() {
		return sPrefs.getString(CLASS_DESCRIPTION, "");
	}

	public LanguageSettings setClassDescription(String value) {
		if (value == null)
			value = "";
		editor.putString(CLASS_DESCRIPTION, value);
		return this;
	}

	public Long getTeacherLanguageId() {
		return sPrefs.getLong(TEACHER_LANGUAGE_ID, -1);
	}

	public LanguageSettings setTeacherLanguageId(Long id) {
		if (id == null)
			id = -1l;
		editor.putLong(TEACHER_LANGUAGE_ID, id);
		return this;
	}

	public Long getLessonId() {
		return sPrefs.getLong(LESSON_ID, -1);
	}

	public LanguageSettings setLessonId(Long id) {
		if (id == null)
			id = -1l;
		editor.putLong(LESSON_ID, id);
		return this;
	}

	public String getLessonTitle() {
		return sPrefs.getString(LESSON_TITLE, "");
	}

	public LanguageSettings setLessonTitle(String value) {
		if (value == null)
			value = "";
		editor.putString(LESSON_TITLE, value);
		return this;
	}

	public String getLessonDescription() {
		return sPrefs.getString(LESSON_DESCRIPTION, "");
	}

	public LanguageSettings setLessonDescription(String value) {
		if (value == null)
			value = "";
		editor.putString(LESSON_DESCRIPTION, value);
		return this;
	}
	 
	public String getLessonType() {
		return sPrefs.getString(LESSON_TYPE, "");
	}

	public LanguageSettings setLessonType(String value) {
		if (value == null)
			value = "";
		editor.putString(LESSON_TYPE, value);
		return this;
	}
	
 
	public Long getLessonPhraseId() {
		return sPrefs.getLong(LESSON_PHRASE_ID, -1);
	}

	public LanguageSettings setLessonPhraseId(Long id) {
		if (id == null)
			id = -1l;
		editor.putLong(LESSON_PHRASE_ID, id);
		return this;
	}

	public Long getLearningLanguageId() {
		return sPrefs.getLong(LEARNING_LANGUAGE_ID, -1);
	}

	public LanguageSettings setLearningLanguageId(Long id) {
		if (id == null)
			id = -1l;
		editor.putLong(LEARNING_LANGUAGE_ID, id);
		return this;
	}

	public String getLearningLanguageName() {
		return sPrefs.getString(LEARNING_LANGUAGE_NAME, "");
	}

	public LanguageSettings setLearningLanguageName(String value) {
		if (value == null)
			value = "";
		editor.putString(LEARNING_LANGUAGE_NAME, value);
		return this;
	}

	public String getLearningLanguageMediaDirectory() {
		return sPrefs.getString(LEARNING_LANGUAGE_NAME, "");
	}

	public LanguageSettings setLearningLanguageMediaDirectory(String value) {
		if (value == null)
			value = "";
		editor.putString(LEARNING_LANGUAGE_MEDIA_DIRECTORY, value);
		return this;
	}

	public Long getKnownLanguageId() {
		return sPrefs.getLong(KNOWN_LANGUAGE_ID, -1);
	}

	public LanguageSettings setKnownLanguageId(Long id) {
		if (id == null)
			id = -1l;
		editor.putLong(KNOWN_LANGUAGE_ID, id);
		return this;
	}

	public String getKnownLanguageName() {
		return sPrefs.getString(KNOWN_LANGUAGE_NAME, "");
	}

	public LanguageSettings setKnownLanguageName(String value) {
		if (value == null)
			value = "";
		editor.putString(KNOWN_LANGUAGE_NAME, value);
		return this;
	}

	public int getLastLessonPhraseLine() {
		return sPrefs.getInt(LAST_LESSON_PHRASE_LINE, -1);
	}

	public LanguageSettings setLastLessonPhraseLine(Integer value) {
		if (value == null)
			value = -1;
		editor.putInt(LAST_LESSON_PHRASE_LINE, value);
		return this;
	}

	public String getKnownLanguageMediaDirectory() {
		return sPrefs.getString(KNOWN_LANGUAGE_NAME, "");
	}

	public LanguageSettings setKnownLanguageMediaDirectory(String value) {
		if (value == null)
			value = "";
		editor.putString(KNOWN_LANGUAGE_MEDIA_DIRECTORY, value);
		return this;
	}

	public int getMaintenanceType() {
		return sPrefs.getInt(MAINTENANCE_TYPE, -1);
	}

	public LanguageSettings setMaintenanceType(Integer id) {
		if (id == null)
			id = -1;
		editor.putInt(MAINTENANCE_TYPE, id);
		return this;
	}

	public int getMaintenanceStatus() {
		return sPrefs.getInt(MAINTENANCE_STATUS, -1);
	}

	public LanguageSettings setMaintenanceStatus(Integer value) {
		if (value == null)
			value = -1;
		editor.putInt(MAINTENANCE_STATUS, value);
		return this;
	}

	public String getMaintenanceDetails() {
		return sPrefs.getString(MAINTENANCE_DETAILS, "");
	}

	public LanguageSettings setMaintenanceDetails(String value) {
		if (value == null)
			value = "";
		editor.putString(MAINTENANCE_DETAILS, value);
		return this;
	}

	public LanguageSettings setPlayAudioWhenAvailable(boolean value) {
		editor.putBoolean(PLAY_AUDIO_WHEN_AVAILABLE, value);
		return this;
	}

	public boolean getPlayAudioWhenAvailable() {
		return sPrefs.getBoolean(PLAY_AUDIO_WHEN_AVAILABLE, true);
	}

	public LanguageSettings setRepeatPhrase(boolean value) {
		editor.putBoolean(REPEAT_PHRASE, value);
		return this;
	}

	public boolean getRepeatPhrase() {
		return sPrefs.getBoolean(REPEAT_PHRASE, true);
	}

	public LanguageSettings setRepeatXTimes(int value) {
		editor.putInt(REPEAT_X_TIMES, value);
		return this;
	}

	public int getRepeatXTimes() {
		return sPrefs.getInt(REPEAT_X_TIMES, 1);
	}

	public LanguageSettings setPhraseDelayByDuration(float value) {
		editor.putFloat(PHRASE_DELAY_BY_DURATION, value);
		return this;
	}

	public float getPhraseDelayByDuration() {
		return sPrefs.getFloat(PHRASE_DELAY_BY_DURATION, 1.5f);
	}

	public LanguageSettings setWaitXSeconds(float value) {
		editor.putFloat(WAIT_X_SECONDS, value);
		return this;
	}

	public float getWaitXSeconds() {
		return sPrefs.getFloat(WAIT_X_SECONDS, 0.0f);
	}

	public LanguageSettings setPlayKnownPhrase(boolean value) {
		editor.putBoolean(PLAY_KNOWN_PHRASE, value);
		return this;
	}

	public boolean getPlayKnownPhrase() {
		return sPrefs.getBoolean(PLAY_KNOWN_PHRASE, false);
	}

	public LanguageSettings setPlayBeforeLearningPhrase(boolean value) {
		editor.putBoolean(PLAY_BEFORE_LEARNING_PHRASE, value);
		return this;
	}

	public boolean getPlayBeforeLearningPhrase() {
		return sPrefs.getBoolean(PLAY_BEFORE_LEARNING_PHRASE, true);
	}

	public LanguageSettings setPlayAfterLearningPhrase(boolean value) {
		editor.putBoolean(PLAY_AFTER_LEARNING_PHRASE, value);
		return this;
	}

	public boolean getPlayAfterLearningPhrase() {
		return sPrefs.getBoolean(PLAY_AFTER_LEARNING_PHRASE, false);
	}

	public LanguageSettings setStepThroughLessonAutomatically(boolean value) {
		editor.putBoolean(STEP_THROUGH_LESSON_AUTOMATICALLY, value);
		return this;
	}

	public boolean getStepThroughLessonAutomatically() {
		return sPrefs.getBoolean(STEP_THROUGH_LESSON_AUTOMATICALLY, false);
	}

	public LanguageSettings setDisplayKnownTextWhenAvailable(boolean value) {
		editor.putBoolean(DISPLAY_KNOWN_TEXT_WHEN_AVAILABLE, value);
		return this;
	}

	public boolean getDisplayKnownTextWhenAvailable() {
		return sPrefs.getBoolean(DISPLAY_KNOWN_TEXT_WHEN_AVAILABLE, false);
	}

	public LanguageSettings setDisplayKnownTextBeforeLearningPhrase(boolean value) {
		editor.putBoolean(DISPLAY_KNOWN_TEXT_BEFORE_LEARNING_PHRASE, value);
		return this;
	}

	public boolean getDisplayKnownTextBeforeLearningPhrase() {
		return sPrefs
				.getBoolean(DISPLAY_KNOWN_TEXT_BEFORE_LEARNING_PHRASE, false);
	}

	public LanguageSettings setDisplayKnownTextAfterLearningPhrase(boolean value) {
		editor.putBoolean(DISPLAY_KNOWN_TEXT_AFTER_LEARNING_PHRASE, value);
		return this;
	}

	public boolean getDisplayKnownTextAfterLearningPhrase() {
		return sPrefs.getBoolean(DISPLAY_KNOWN_TEXT_AFTER_LEARNING_PHRASE, false);
	}
	
	public LanguageSettings setPhraseTextSize(Float textSize) {
		editor.putFloat(PHRASE_TEXT_SIZE, textSize);
		return this;
	}
	
	public float getPhraseTextSize() {
		float textSize = sPrefs.getFloat(PHRASE_TEXT_SIZE, -1f);
		if (textSize == -1){
			textSize =  new TextView(sContext).getTextSize();
		}
		return textSize;
	}
	
	public String getMediaDirectory() {
		return sPrefs.getString(MEDIA_DIRECTORY, "");
	}

	public LanguageSettings setMediaDirectory(String value) {
		if (value == null)
			value = "";
		editor.putString(MEDIA_DIRECTORY, value);
		return this;
	}

	

	@SuppressLint("NewApi")
	public void commit() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			editor.apply();
		} else {
			editor.commit();
		}

		editor.commit();

	}

}
