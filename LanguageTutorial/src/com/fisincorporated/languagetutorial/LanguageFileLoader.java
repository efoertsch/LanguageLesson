package com.fisincorporated.languagetutorial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.fisincorporated.languagetutorial.db.ClassName;
import com.fisincorporated.languagetutorial.db.ClassNameDao;
import com.fisincorporated.languagetutorial.db.CompoundPhrase;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.LanguageCode;
import com.fisincorporated.languagetutorial.db.LanguageCodeDao;
import com.fisincorporated.languagetutorial.db.LanguagePhrase;
import com.fisincorporated.languagetutorial.db.LanguagePhraseDao;
import com.fisincorporated.languagetutorial.db.LanguageXref;
import com.fisincorporated.languagetutorial.db.LanguageXrefDao;
import com.fisincorporated.languagetutorial.db.Lesson;
import com.fisincorporated.languagetutorial.db.LessonDao;
import com.fisincorporated.languagetutorial.db.LessonPhrase;
import com.fisincorporated.languagetutorial.db.LessonPhraseDao;
import com.fisincorporated.languagetutorial.db.Teacher;
import com.fisincorporated.languagetutorial.db.TeacherDao;
import com.fisincorporated.languagetutorial.db.TeacherLanguage;
import com.fisincorporated.languagetutorial.db.TeacherLanguageDao;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;

//http://stackoverflow.com/questions/8417885/android-fragments-retaining-an-asynctask-during-screen-rotation-or-configuratio
// this is equivalent to MyTask in SO example  by Timmmmm
public class LanguageFileLoader extends AsyncTask<Void, Integer, Boolean> {
	private static final String TAG = "LanguageFileLoader";

	private DaoSession daoSession;
	private TeacherDao teacherDao;
	private Teacher teacher;
	private TeacherLanguageDao teacherLanguageDao;
	private TeacherLanguage teacherLanguage;
	private LanguageCodeDao languageCodeDao;
	private LanguageCode learningLanguageCode;
	private LanguageCode knownLanguageCode;
	private LanguagePhraseDao languagePhraseDao;
	private LanguagePhrase learningLanguagePhrase;
	private LanguagePhrase knownLanguagePhrase;
	private LanguageXrefDao languageXrefDao;
	// private LanguageXref languageXref;
	// private CompoundPhraseDao compoundPhraseDao;
	// private ClassName className;
	private ClassNameDao classNameDao;
	private Lesson lesson;
	private LessonDao lessonDao;
	// private LessonPhrase lessonPhrase;
	private LessonPhraseDao lessonPhraseDao;

	private Query<Teacher> teacherQuery = null;
	private Query<LanguageCode> languageQuery = null;
	private Query<TeacherLanguage> teacherLanguageQuery = null;
	private Query<LanguagePhrase> languagePhraseQuery = null;
	private Query<LanguageXref> languageXrefQuery = null;
	private Query<ClassName> classNameQuery = null;
	private Query<ClassName> classNameByTitleQuery = null;
	private Query<Lesson> lessonQuery = null;
	private Query<Lesson> lessonListQuery = null;
	private Query<LanguageXref> languageXrefListQuery = null;
	private Query<LessonPhrase> lessonPhraseQuery = null;
	private DeleteQuery<CompoundPhrase> compoundPhraseDeleteQuery = null;
	private DeleteQuery<LessonPhrase> lessonPhraseDeleteQuery = null;
	private DeleteQuery<Lesson> lessonDeleteQuery = null;
	private DeleteQuery<ClassName> classNameDeleteQuery = null;

	private String previousClass = "";
	private String previousLesson = "";
	private List<Lesson> lessonList;
	private ArrayList<Lesson> deleteLessonList = new ArrayList<Lesson>();
	private List<LanguageXref> languageXrefList;
	private String errorMsg = null;
	private Resources res;

	// these values must match what is used by Excel spreadsheet macro that
	// creates the language file
	private static final String PIPE_DELIMITER = "\\|";
	private static final String TEACHER = "Teacher";
	private static final String TEACHER_LANGUAGE = "TeacherLanguage";
	private static final String LANGUAGE_PHRASE = "LanguagePhrase";
	private static final String CLASS = "Class";
	private static final String LESSON = "Lesson";
	private static final String LESSON_PHRASE = "LessonPhrase";

	private LoadFileTaskFragment loadFileTaskFragment;
	private File languageFile;

	private float languageFileSize = 0;
	private long numberFileCharsRead = 0;
	private int percentFileRead = 0;
	private boolean loadError = false;
	private boolean cancel = false;

	// The fragment passed in should be non-ui fragment
	public LanguageFileLoader(LoadFileTaskFragment fragment, File file) {
		loadFileTaskFragment = fragment;
		languageFile = file;
	}

	@Override
	protected void onPreExecute() {
		if (loadFileTaskFragment == null)
			return;
		loadFileTaskFragment.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// Need to wait till here to make sure fragment is attached to activity
		// before trying to get resources
		res = LanguageApplication.getInstance().getResources();
		setupDao();
		loadLanguageSpreadSheet(languageFile);
		return loadError || cancel;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		if (loadFileTaskFragment == null)
			return;
		loadFileTaskFragment.updateProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (loadFileTaskFragment == null)
			return;
		loadFileTaskFragment.taskFinished(cancel ? GlobalValues.CANCELLED
				: (loadError ? GlobalValues.FINISHED_WITH_ERROR
						: GlobalValues.FINISHED_OK), errorMsg);
	}

	// call back with error message
	// Save the error msg for passing back on onPostExecute - and via UI thread
	private void passbackErrorMessage(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	private void setupDao() {
		daoSession = LanguageApplication.getInstance().getDaoSession();
		teacherDao = daoSession.getTeacherDao();
		teacherLanguageDao = daoSession.getTeacherLanguageDao();
		languageCodeDao = daoSession.getLanguageCodeDao();
		languagePhraseDao = daoSession.getLanguagePhraseDao();
		languageXrefDao = daoSession.getLanguageXrefDao();
		// compoundPhraseDao = daoSession.getCompoundPhraseDao();
		classNameDao = daoSession.getClassNameDao();
		lessonDao = daoSession.getLessonDao();
		lessonPhraseDao = daoSession.getLessonPhraseDao();
	}

	public void cancelTask(boolean cancel) {
		this.cancel = cancel;
	}
	
	public void loadLanguageSpreadSheet(File file) {
		BufferedReader br = null;
		// Read text from file
		try {
			languageFileSize = file.length();
			numberFileCharsRead = 0;
			// read unicode properly
			// first byte in file appears to be byte order mark (BOM)
			// http://en.wikipedia.org/wiki/Byte_order_mark
			// so on first read drop the BOM
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-16LE"));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null && (!loadError) && (!cancel)) {
				numberFileCharsRead += line.length();
				if (languageFileSize > 0) {
					percentFileRead = (int) ((numberFileCharsRead / (float) languageFileSize) * 100);
					publishProgress(percentFileRead);
				}
				processLine(i == 0 ? line.substring(1).trim() : line.trim());
				++i;
				if (cancel) {
					break;
				}
			}
		} catch (IOException e) {
			passbackErrorMessage(String.format(
					res.getString(R.string.error_reading_language_file),
					file.getAbsolutePath()));
			loadError = true;
		} catch (Exception e) {
			passbackErrorMessage(res.getString(R.string.unexpected_exception)
					+ " " + e.toString());
			loadError = true;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					;
				}
			}
		}
	}

	private void processLine(String line) {
		String[] tokens = parseLine(line);
		if (0 == tokens[0].compareTo(TEACHER)) {
			processTeacherLine(tokens);
			return;
		}
		if (0 == tokens[0].compareTo(TEACHER_LANGUAGE)) {
			processTeacherLanguageLine(tokens);
			return;
		}
		if (0 == tokens[0].compareTo(LANGUAGE_PHRASE)) {
			processLanguagePhraseLine(tokens);
			return;
		}

		if (0 == tokens[0].compareTo(CLASS)) {
			processClassNameLine(tokens);
			return;
		}
		if (0 == tokens[0].compareTo(LESSON)) {
			processLessonLine(tokens);
			return;
		}
		if (0 == tokens[0].compareTo(LESSON_PHRASE)) {
			processLessonPhraseLine(tokens);
			return;
		}

	}

	private String[] parseLine(String line) {
		// http://blog.mgm-tp.com/2012/05/regexp-java-puzzler/ to get empty
		// columns after last non-empty column
		String[] tokens = line.split(PIPE_DELIMITER, -1);
		return tokens;
	}

	private void processTeacherLine(String tokens[]) {
		if (tokens.length >= 3) {
			teacher = loadTeacher(tokens[1], tokens[2]);
		} else {
			passbackErrorMessage(String.format(
					res.getString(R.string.wrong_number_teacher_fields),
					listTokens(tokens)));
			loadError = true;
		}
	}

	public String listTokens(String[] tokens) {
		StringBuilder sb = new StringBuilder();
		if (tokens == null)
			return "No values found";
		else {
			for (int i = 0; i < tokens.length; ++i) {
				sb.append(i + ". " + tokens[i]);
			}
			return sb.toString();
		}
	}

	private Teacher loadTeacher(String teacherName, String teacherURL) {
		if (teacherQuery == null) {
			teacherQuery = teacherDao.queryBuilder()
					.where(TeacherDao.Properties.TeacherName.eq(teacherName))
					.build();
		} else {
			teacherQuery.setParameter(0, teacherName);
		}
		teacher = (Teacher) teacherQuery.unique();
		if (teacher == null) {
			teacher = new Teacher(null, teacherName, teacherURL);
			teacherDao.insert(teacher);
		} else {
			if (teacherURL != null && !teacherURL.equals(teacher.getTeacherURL())
					&& !teacherURL.equals("")) {
				teacher.setTeacherURL(teacherURL);
				teacher.update();
			}
		}
		return teacher;
	}

	public void processTeacherLanguageLine(String[] tokens) {
		if (tokens.length >= 6) {
			learningLanguageCode = loadLanguageCode(tokens[1]);
			knownLanguageCode = loadLanguageCode(tokens[3]);

			// tokens 2 and 4 are the media directories
			teacherLanguage = loadTeacherLanguage(teacher.getId(),
					learningLanguageCode.getId(), knownLanguageCode.getId(),
					tokens[2], tokens[4]);
		} else {
			passbackErrorMessage(String
					.format(
							res.getString(R.string.too_few_teacher_language_fieldswrong_number_teacher_fields),
							listTokens(tokens)));
			loadError = true;
		}

	}

	private LanguageCode loadLanguageCode(String languageName) {
		LanguageCode languageCode = null;
		if (languageName.equals("")) {
			// if no languageName (this should only happen on known Language)
			// return languageCode with null id
			languageCode = new LanguageCode();
			return languageCode;
		}
		if (languageQuery == null) {
			languageQuery = languageCodeDao.queryBuilder()
					.where(LanguageCodeDao.Properties.LanguageName.eq(languageName))
					.build();
		} else {
			languageQuery.setParameter(0, languageName);
		}

		languageCode = (LanguageCode) languageQuery.unique();
		if (languageCode == null) {
			languageCode = new LanguageCode(null, languageName);
			languageCodeDao.insert(languageCode);
		}
		return languageCode;
	}

	private TeacherLanguage loadTeacherLanguage(Long teacherId,
			Long learningLanguageId, Long knownLanguageId,
			String learningLanguageMediaDirectory,
			String knownLanguageMediaDirectory) {
		TeacherLanguage teacherLanguage = null;
		if (teacherLanguageQuery == null) {
			teacherLanguageQuery = teacherLanguageDao
					.queryBuilder()
					.where(
							TeacherLanguageDao.Properties.TeacherId.eq(teacherId),
							TeacherLanguageDao.Properties.LearningLanguageId
									.eq(learningLanguageId),
							TeacherLanguageDao.Properties.KnownLanguageId
									.eq(knownLanguageId)).build();
		} else {
			teacherLanguageQuery.setParameter(0, teacherId);
			teacherLanguageQuery.setParameter(1, learningLanguageId);
			teacherLanguageQuery.setParameter(2, knownLanguageId);
		}
		teacherLanguage = (TeacherLanguage) teacherLanguageQuery.unique();
		if (teacherLanguage == null) {
			teacherLanguage = new TeacherLanguage(null, teacherId,
					learningLanguageId, learningLanguageMediaDirectory,
					knownLanguageId, knownLanguageMediaDirectory);
			teacherLanguageDao.insert(teacherLanguage);
		}
		return teacherLanguage;
	}

	// the languagePhrase tokens have both the language to be learned and
	// optionally the language that is known
	// the teacher, teacherLanguage, and langaugeCode objects should be defined
	// by now
	// what is left to create is languageXref, languagePhrase, and compoundPhrase
	// (not yet implemented)
	// The index order of tokens is
	// 0. "LanguagePhrase"
	// 1. Phrase to be learned
	// 2. Phrase type
	// 3. Audio filename (optional)
	// 4. Video filename (optional)
	// 5. Pronunciation phrase (optional)
	// 6. EnglishNumeral (eg, 1,2 , 3 optional)
	// 7. CompoundPhrase (optional)
	// 8. Known phrase (optional)
	// 9. Phrase type (optional)
	// 10. Audio filename (optional)
	// 11. Video filename (optional)
	// 12. Pronunciation phrase (optional)
	// 13. EnglishNumeral (eg, 1,2 , 3 optional)
	// 14. CompoundPhrase (optional)
	private void processLanguagePhraseLine(String[] tokens) {
		if (tokens.length >= 15) {
			// first load the learningLanguagePhrase
			learningLanguagePhrase = loadLanguagePhrase(teacher.getId(),
					teacherLanguage.getLearningLanguageId(), tokens[1], tokens[2],
					tokens[3], tokens[4], tokens[5], tokens[6], null);
			// load the knownLanguage (you can end up with 'empty' class if
			// knownlanguage not specified on spreadsheet
			// and this causes all known phrases to be ignored
			knownLanguagePhrase = loadLanguagePhrase(teacher.getId(),
					teacherLanguage.getKnownLanguageId(), tokens[8], tokens[9],
					tokens[10], tokens[11], tokens[12], tokens[13], null);

			// Now load the LanguageXref (even if knownLanguage not specified)
			loadLanguageXref(teacher.getId(), teacherLanguage.getId(),
					learningLanguagePhrase.getId(), knownLanguagePhrase.getId());
		} else {
			passbackErrorMessage(String.format(
					res.getString(R.string.too_few_language_phrase_fields),
					listTokens(tokens)));
			loadError = true;
		}

	}

	private LanguagePhrase loadLanguagePhrase(Long teacherId, Long languageId,
			String writtenPhrase, String phraseType, String audioFile,
			String videoFile, String pronunciation, String englishNumeral,
			String compoundPhrase) {
		LanguagePhrase languagePhrase = null;
		if (languageId == null) {
			languagePhrase = new LanguagePhrase();
			return languagePhrase;
		}

		languagePhrase = getLanguagePhrase(teacherId, languageId, writtenPhrase);

		// currently compoundPhraseId not implemented
		Long compoundPhraseId = null;
		// If it happens:
		// 1. Unstring compoundPhrase via some to-be-determined separator
		// 2. Find each individual string in the LanguagePhrase table to get
		// languagePhraseIds
		// 3. Find max + 1 compound_phrase_id in CompoundPhrase table
		// 4. Compose and insert the list of CompoundPhrase records, incrementing
		// phraseOrder by 1 after each insert
		// 5. Assign the compoundPhraseId to the languagePhrase record
		if (languagePhrase == null) {

			languagePhrase = new LanguagePhrase(null, teacherId, languageId,
					writtenPhrase, audioFile, videoFile, phraseType, pronunciation,
					englishNumeral, null);
			languagePhraseDao.insert(languagePhrase);
		} else {
			// not implemented
			// delete/recreate compoundphrase records then re-add to ensure correct
			// on update
			// if (languagePhrase.getCompoundPhraseId() != null) {
			// deleteCompoundPhrase(languagePhrase.getCompoundPhraseId());
			// }

			languagePhrase.setAudioFile(audioFile);
			languagePhrase.setVideoFile(videoFile);
			languagePhrase.setPhraseType(phraseType);
			languagePhrase.setPronunciation(pronunciation);
			languagePhrase.setEnglishNumeral(englishNumeral);
			languagePhrase.setCompoundPhraseId(compoundPhraseId);
			languagePhraseDao.update(languagePhrase);
		}
		return languagePhrase;

	}

	private LanguagePhrase getLanguagePhrase(Long teacherId, Long languageId,
			String writtenPhrase) {
		LanguagePhrase languagePhrase;
		if (languagePhraseQuery == null) {
			languagePhraseQuery = languagePhraseDao
					.queryBuilder()
					.where(
							LanguagePhraseDao.Properties.TeacherId.eq(teacherId),
							LanguagePhraseDao.Properties.LanguageId.eq(languageId),
							LanguagePhraseDao.Properties.WrittenPhrase
									.eq(writtenPhrase)).build();
		} else {
			languagePhraseQuery.setParameter(0, teacherId);
			languagePhraseQuery.setParameter(1, languageId);
			languagePhraseQuery.setParameter(2, writtenPhrase);

		}
		languagePhrase = (LanguagePhrase) languagePhraseQuery.unique();
		return languagePhrase;
	}

	// not currently implemented
	// private void deleteCompoundPhrase(Long compoundPhraseId) {
	// if (compoundPhraseDeleteQuery == null) {
	// compoundPhraseDeleteQuery = compoundPhraseDao
	// .queryBuilder()
	// .where(
	// CompoundPhraseDao.Properties.CompoundPhraseId
	// .eq(compoundPhraseId)).buildDelete();
	// } else {
	// compoundPhraseDeleteQuery.setParameter(0, compoundPhraseId);
	// }
	// compoundPhraseDeleteQuery.executeDeleteWithoutDetachingEntities();
	//
	// }

	private LanguageXref loadLanguageXref(Long teacherId,
			Long teacherLanguageId, Long learningPhraseId, Long knownPhraseId) {
		LanguageXref languageXref = null;
		if (languageXrefQuery == null) {
			languageXrefQuery = languageXrefDao
					.queryBuilder()
					.where(
							LanguageXrefDao.Properties.TeacherId.eq(teacherId),
							LanguageXrefDao.Properties.LearningPhraseId
									.eq(learningPhraseId),
							LanguageXrefDao.Properties.KnownPhraseId.eq(knownPhraseId))
					.build();
		} else {
			languageXrefQuery.setParameter(0, teacherId);
			languageXrefQuery.setParameter(1, learningPhraseId);
			languageXrefQuery.setParameter(2, knownPhraseId);
		}
		languageXref = (LanguageXref) languageXrefQuery.unique();
		if (languageXref == null) {
			// currently compoundPhraseId not implemented
			languageXref = new LanguageXref(null, teacherId, teacherLanguageId,
					learningPhraseId, knownPhraseId);
			languageXrefDao.insert(languageXref);
		}
		return languageXref;

	}

	// always add, make sure any prior set of records for the languagePhrase are
	// deleted before (re)inserting
	// not currently implemented
	// private CompoundPhrase loadCompoundPhrase(Long compoundPhraseId,
	// Integer phraseOrder, Long languagePhraseId, Float phraseInterval) {
	// CompoundPhrase compoundPhrase = new CompoundPhrase(null,
	// compoundPhraseId, phraseOrder, languagePhraseId, phraseInterval);
	// compoundPhraseDao.insert(compoundPhrase);
	// return compoundPhrase;
	// }

	// add a class for a teacher
	// delete any lesson/lesson phrases before adding/updating
	private void processClassNameLine(String tokens[]) {
		int order;
		if (tokens.length >= 4) {
			try {
				order = Integer.parseInt(tokens[1]);
			} catch (NumberFormatException nfe) {
				passbackErrorMessage(String.format(
						res.getString(R.string.invalid_order_number_for_class),
						tokens[2], tokens[1]));
				loadError = true;
				return;
			}
			if (tokens[2].equals("")) {
				passbackErrorMessage(String.format(
						res.getString(R.string.name_of_class_must_be_specified),
						tokens[1]));
				loadError = true;
				return;
			}
			loadClassName(teacher.getId(), order, tokens[2], tokens[3],
					teacherLanguage.getId());
		} else {
			passbackErrorMessage(String
					.format(res.getString(R.string.too_few_class_fields),
							listTokens(tokens)));
			loadError = true;
			return;
		}
	}

	private ClassName loadClassName(Long teacherId, int order,
			String classTitle, String description, Long teacherLanguageId) {
		ClassName className = getClassName(teacherId, order, teacherLanguageId);
		if (className == null) {
			className = new ClassName(null, teacherId, order, classTitle,
					description, teacherLanguageId);
			classNameDao.insert(className);
		} else {
			// first delete all lesson phrases for each lesson under that class
			deleteLessonList = getLessonListForClass(className.getId());
			if (deleteLessonList != null) {
				for (int i = 0; i < deleteLessonList.size(); ++i) {
					deleteLessonPhrasesForLesson(deleteLessonList.get(i).getId());
				}
			}
			// now delete the lessons
			deleteLessonsForClass(className.getId());
			// update ClassName fields
			className.setClassTitle(classTitle);
			className.setDescription(description);
			className.update();
		}
		return className;
	}

	private ArrayList<Lesson> getLessonListForClass(Long classId) {
		if (lessonListQuery == null) {
			lessonListQuery = lessonDao.queryBuilder()
					.where(LessonDao.Properties.ClassId.eq(classId)).build();
		} else {
			lessonListQuery.setParameter(0, classId);
		}
		return (ArrayList<Lesson>) lessonListQuery.list();
	}

	private void deleteClassesForTeacher(Long teacherId) {
		if (classNameDeleteQuery == null) {
			classNameDeleteQuery = classNameDao.queryBuilder()
					.where(ClassNameDao.Properties.TeacherId.eq(teacherId))
					.buildDelete();
		} else {
			classNameDeleteQuery.setParameter(0, teacherId);
		}
		classNameDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	private ClassName getClassName(Long teacherId, int order,
			Long teacherLanguageId) {
		if (classNameQuery == null) {
			classNameQuery = classNameDao
					.queryBuilder()
					.where(
							ClassNameDao.Properties.TeacherId.eq(teacherId),
							ClassNameDao.Properties.TeacherLanguageId
									.eq(teacherLanguageId),
							ClassNameDao.Properties.ClassOrder.eq(order)).build();
		} else {
			classNameQuery.setParameter(0, teacherId);
			classNameQuery.setParameter(1, teacherLanguageId);
			classNameQuery.setParameter(2, order);
		}
		return (ClassName) classNameQuery.unique();
	}

	private ClassName getClassNameByTitle(Long teacherId, String classTitle,
			Long teacherLanguageId) {
		if (classNameByTitleQuery == null) {
			classNameByTitleQuery = classNameDao
					.queryBuilder()
					.where(
							ClassNameDao.Properties.TeacherId.eq(teacherId),
							ClassNameDao.Properties.TeacherLanguageId
									.eq(teacherLanguageId),
							ClassNameDao.Properties.ClassTitle.eq(classTitle)).build();
		} else {
			classNameByTitleQuery.setParameter(0, teacherId);
			classNameByTitleQuery.setParameter(1, teacherLanguageId);
			classNameByTitleQuery.setParameter(2, classTitle);
		}
		return (ClassName) classNameByTitleQuery.unique();
	}

	// Tokens
	// 1 Class name (must match back to 'Class' class name
	// 2 Lesson Order
	// 3 Lesson Title
	// 4 Lesson Description
	// 5 lesson Type (W -words, S- sentence, etc)
	//
	private void processLessonLine(String tokens[]) {
		ClassName className = null;
		int order;
		if (tokens.length >= 6) {
			className = getClassNameByTitle(teacher.getId(), tokens[1],
					teacherLanguage.getId());
			if (className == null || className.getClassTitle() == null) {
				passbackErrorMessage(String
						.format(
								res.getString(R.string.class_title_for_this_lesson_not_found),
								tokens[1], tokens[3]));
				loadError = true;
				return;
			}
			try {
				order = Integer.parseInt(tokens[2]);
			} catch (NumberFormatException nfe) {
				passbackErrorMessage(String.format(
						res.getString(R.string.invalid_lesson_order_number),
						tokens[1], tokens[3], tokens[2]));
				loadError = true;
				return;
			}
			if (tokens[3].equals("")) {
				passbackErrorMessage(String.format(
						res.getString(R.string.title_of_lesson_must_be_specified),
						tokens[1], tokens[2]));
				loadError = true;
				return;
			}

			lesson = loadLesson(className.getId(), order, tokens[3], tokens[4],
					tokens[5]);
		} else {
			passbackErrorMessage(String.format(
					res.getString(R.string.too_few_lesson_fields),
					listTokens(tokens)));
			loadError = true;
		}
	}

	private void deleteLessonsForClass(Long classId) {
		if (lessonDeleteQuery == null) {
			lessonDeleteQuery = lessonDao.queryBuilder()
					.where(LessonDao.Properties.ClassId.eq(classId)).buildDelete();
		} else {
			lessonDeleteQuery.setParameter(0, classId);
		}
		lessonDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	private Lesson loadLesson(Long classId, int order, String lessonTitle,
			String description, String lessonType) {
		if (lessonQuery == null) {
			lessonQuery = lessonDao
					.queryBuilder()
					.where(LessonDao.Properties.LessonOrder.eq(order),
							LessonDao.Properties.ClassId.eq(classId)).build();
		} else {
			lessonQuery.setParameter(0, order);
			lessonQuery.setParameter(1, classId);
		}
		lesson = (Lesson) lessonQuery.unique();
		if (lesson == null) {
			lesson = new Lesson(null, classId, order, lessonTitle, description,
					lessonType);
			lessonDao.insert(lesson);
		} else {
			lesson.setDescription(description);
			lesson.setLessonType(lessonType);
			lessonDao.update(lesson);
		}
		return lesson;
	}

	private void deleteLessonPhrasesForLesson(Long lessonId) {
		if (lessonPhraseDeleteQuery == null) {
			lessonPhraseDeleteQuery = lessonPhraseDao.queryBuilder()
					.where(LessonPhraseDao.Properties.LessonId.eq(lessonId))
					.buildDelete();
		} else {
			lessonPhraseDeleteQuery.setParameter(0, lessonId);
		}
		lessonPhraseDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	// LessonPhrase tokens are
	// 1 Class
	// 2 Lesson
	// 3 Lesson Order
	// 4 Speaker number
	// 5 Language phrase
	// 6 phrase interval
	private void processLessonPhraseLine(String[] tokens) {
		int order;
		int speaker;
		float phraseInterval;
		if (tokens.length >= 7) {
			// make sure you have current lesson_id (multiple classes/lessons can
			// be loaded)
			if (tokens[1].equals("") && !previousClass.equals("")) {
				tokens[1] = previousClass;
			}
			if (tokens[2].equals("") && !previousLesson.equals("")) {
				tokens[2] = previousLesson;
			}
			if (!tokens[1].equalsIgnoreCase(previousClass)
					|| !tokens[2].equalsIgnoreCase(previousLesson)) {
				previousClass = tokens[1];
				previousLesson = tokens[2];
				lessonList = lessonDao.queryDeep(" where T."
						+ LessonDao.Properties.LessonTitle.columnName + "= ?"
						+ " and T0." + ClassNameDao.Properties.ClassTitle.columnName
						+ "= ?" + " and T0."
						+ ClassNameDao.Properties.TeacherId.columnName + "= ?",
						previousLesson, previousClass, teacher.getId() + "");
			}
			if (lessonList.size() == 0) {
				passbackErrorMessage(String.format(
						res.getString(R.string.class_lesson_not_defined), tokens[1],
						tokens[2]));
				loadError = true;
				return;
			}
			// make sure lesson phrase order and speaker numbers are good numbers
			try {
				order = Integer.parseInt(tokens[3]);
				speaker = Integer.parseInt(tokens[4]);
				if (!tokens[6].equals("")) {
					phraseInterval = Float.parseFloat(tokens[6]);
				} else
					phraseInterval = -1;
			} catch (NumberFormatException nfe) {
				passbackErrorMessage(String
						.format(
								res.getString(R.string.invalid_order_or_speaker_or_interval_number),
								tokens[1], tokens[2], tokens[3], tokens[4],
								((!tokens[6].equals("")) ? "" : " interval = "
										+ tokens[6])));
				loadError = true;
				return;
			}
			// Make sure the phrase is in the LanguagePhrase table and get the
			// languagePhraseId
			LanguagePhrase languagePhrase = null;
			languagePhrase = getLanguagePhrase(teacher.getId(),
					teacherLanguage.getLearningLanguageId(), tokens[5]);
			if (languagePhrase == null) {
				passbackErrorMessage(String.format(res
						.getString(R.string.lesson_phrase_missing_in_language_table),
						tokens[5], tokens[1], tokens[2], tokens[3], tokens[4]));
				loadError = true;
				return;
			}
			// almost there, find the Language xref record
			if (languageXrefListQuery == null) {
				languageXrefListQuery = languageXrefDao
						.queryBuilder()
						.where(
								LanguageXrefDao.Properties.TeacherId
										.eq(teacher.getId()),
								LanguageXrefDao.Properties.TeacherLanguageId
										.eq(teacherLanguage.getId()),
								LanguageXrefDao.Properties.LearningPhraseId
										.eq(languagePhrase.getId())).build();
			} else {
				languageXrefListQuery.setParameter(0, teacher.getId());
				languageXrefListQuery.setParameter(1, teacherLanguage.getId());
				languageXrefListQuery.setParameter(2, languagePhrase.getId());
			}
			languageXrefList = languageXrefListQuery.list();
			if (languageXrefList.size() == 0) {
				passbackErrorMessage(String
						.format(
								res.getString(R.string.language_phrase_xref_missing_for_lesson_phrase),
								tokens[5], tokens[1], tokens[2], tokens[3], tokens[4]));
				loadError = true;
				return;
			}
			// made it this far
			loadLessonPhrase(lessonList.get(0).getId(), order, speaker,
					languageXrefList.get(0).getId(), phraseInterval);
		} else {
			passbackErrorMessage(String.format(
					res.getString(R.string.too_few_lessonphrase_fields),
					listTokens(tokens)));
			loadError = true;
		}
	}

	private LessonPhrase loadLessonPhrase(Long lessonId, int order, int speaker,
			Long languageXrefId, Float phraseInterval) {
		LessonPhrase lessonPhrase;
		if (lessonPhraseQuery == null) {
			lessonPhraseQuery = lessonPhraseDao
					.queryBuilder()
					.where(
							LessonPhraseDao.Properties.LessonId.eq(lessonId),
							LessonPhraseDao.Properties.LessonOrder.eq(order),
							LessonPhraseDao.Properties.Speaker.eq(speaker),
							LessonPhraseDao.Properties.LanguageXrefId
									.eq(languageXrefId)).build();
		} else {
			lessonPhraseQuery.setParameter(0, lessonId);
			lessonPhraseQuery.setParameter(1, order);
			lessonPhraseQuery.setParameter(2, speaker);
			lessonPhraseQuery.setParameter(3, languageXrefId);
		}

		lessonPhrase = (LessonPhrase) lessonPhraseQuery.unique();
		if (lessonPhrase == null) {
			lessonPhrase = new LessonPhrase(null, lessonId, order, speaker,
					languageXrefId, phraseInterval);
			lessonPhraseDao.insert(lessonPhrase);
		} else {

			lessonPhrase.setPhraseInterval(phraseInterval);
		}
		return lessonPhrase;

	}

}
