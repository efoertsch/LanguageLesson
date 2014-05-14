package com.fisincorporated.languagetutorial;

public class GlobalValues {

	// types of lessons
	public static final String PRONUNCIATION = "P";
	public static final String NUMBER = "N";
	public static final String SENTENCE = "S";
	public static final String CONVERSATION = "C";

	// Language maintenance values
	public static final int LOAD = 1;
	public static final int DELETE = 2;
	public static final int LOAD_FROM_WEB = 3;

	// Language file load/delete maintenance status codes
	public static final int RUNNING = 1;
	public static final int FINISHED_OK = 2;
	public static final int FINISHED_WITH_ERROR = 3;
	// public static final int RESET = 4;
	public static final int CANCELLED = 5;

	// Values generally used for passing values in intents
	public static final String TEACHER_ID = "TeacherId";
	public static final String TEACHER_NAME = "TeacherName";
	public static final String TEACHER_LANGUAGE_ID = "TeacherLanguageId";
	public static final String TEACHER_LANGUAGE_TITLE = "TeacherLanguageTitle";
	public static final String LEARNING_LANGUAGE_ID = "LearningLanguageId";
	public static final String LEARNING_LANGUAGE_NAME = "LearningLanguageName";
	public static final String KNOWN_LANGUAGE_ID = "KnowLanguageId";
	public static final String KNOWN_LANGUAGE_NAME = "KnowLanguageName";
	public static final String BUNDLE = "bundle";
	public static final String TEACHER_FROM_TO_LANGUAGE = "TeacherFromToLanguage";

	// these values must match record types in the language file
	// (currently created by Excel spreadsheet macro that creates the language
	// text file)
	public static final String TEACHER = "Teacher";
	public static final String TEACHER_LANGUAGE = "TeacherLanguage";
	public static final String LANGUAGE_PHRASE = "LanguagePhrase";
	public static final String CLASS = "Class";
	public static final String LESSON = "Lesson";
	public static final String LESSON_PHRASE = "LessonPhrase";
	// field separator in language file
	public static final String PIPE_DELIMITER = "\\|";

}
