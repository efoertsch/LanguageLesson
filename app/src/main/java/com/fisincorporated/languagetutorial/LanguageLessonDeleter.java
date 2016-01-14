package com.fisincorporated.languagetutorial;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;

import com.fisincorporated.languagetutorial.db.ClassName;
import com.fisincorporated.languagetutorial.db.ClassNameDao;
import com.fisincorporated.languagetutorial.db.DaoSession;
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
import com.fisincorporated.languagetutorial.interfaces.ILessonMaintanceCallBack;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

import java.io.File;
import java.util.ArrayList;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;

public class LanguageLessonDeleter {

	private static final String TAG = "LanguageLessonDeleter";

	private DaoSession daoSession;
	private TeacherDao teacherDao;
	private TeacherLanguageDao teacherLanguageDao;
	private TeacherLanguage teacherLanguage;
	private LanguagePhraseDao languagePhraseDao;
	private LanguageXrefDao languageXrefDao;
	private ClassNameDao classNameDao;

	private LessonDao lessonDao;
	private LessonPhraseDao lessonPhraseDao;

	private Query<Lesson> lessonListQuery = null;
	private DeleteQuery<LessonPhrase> lessonPhraseDeleteQuery = null;
	private DeleteQuery<Lesson> lessonDeleteQuery = null;
	private DeleteQuery<ClassName> classNameDeleteQuery = null;
	private DeleteQuery<LanguageXref> languageXrefDeleteQuery = null;
	private DeleteQuery<LanguagePhrase> languagePhraseDeleteQuery;
	private DeleteQuery<TeacherLanguage> teacherLanguageDeleteQuery;
	private DeleteQuery<Teacher> teacherDeleteQuery;

	private ArrayList<Lesson> deleteLessonList = new ArrayList<Lesson>();

	private boolean deleteError = false;
	private boolean cancel = false;
	private String message = "";
	private Resources res;

	private TeacherFromToLanguage teacherFromToLanguage;
	LanguageSettings languageSettings;

	private boolean dontDeletePhrasesAndMedia = false;

	private ArrayList<ClassName> classNameList;
	private int teacherCount = 0;

	ILessonMaintanceCallBack lessonMaintanceCallBack;

	// The fragment passed in should be non-ui fragment
	public LanguageLessonDeleter(Context context,
			ILessonMaintanceCallBack lessonMaintanceCallBack,
			TeacherFromToLanguage teacherFromToLanguage) {
		res = context.getResources();
		languageSettings = LanguageSettings.getInstance(context);
		this.lessonMaintanceCallBack = lessonMaintanceCallBack;
		this.teacherFromToLanguage = teacherFromToLanguage;
		setupDao();

	}

	private void setupDao() {
		daoSession = LanguageApplication.getInstance().getDaoSession();
		teacherDao = daoSession.getTeacherDao();
		teacherLanguageDao = daoSession.getTeacherLanguageDao();
		languagePhraseDao = daoSession.getLanguagePhraseDao();
		languageXrefDao = daoSession.getLanguageXrefDao();
		// compoundPhraseDao = daoSession.getCompoundPhraseDao();
		classNameDao = daoSession.getClassNameDao();
		lessonDao = daoSession.getLessonDao();
		lessonPhraseDao = daoSession.getLessonPhraseDao();
	}

	protected void runDelete() {
		checkResetLanguageSettings();
		deleteProcess();
		finishProcess();

	}

	private void checkResetLanguageSettings() {
		if (teacherFromToLanguage.getId() == languageSettings
				.getTeacherLanguageId()) {
			languageSettings.setTeacherId(-1l).setTeacherLanguageId(-1l)
					.setTeacherName("").setClassId(-1l).setClassTitle("")
					.setLessonId(-1l).setLessonTitle("").setLastLessonPhraseLine(-1)
					.commit();
		}
	}

	// ILessonMaintenaceCallBack methods - start
	private void passbackMessage(String message, boolean error) {
		this.message = this.message + "\n" + message;
		lessonMaintanceCallBack.passbackMessage(message, error);
	}

	private void publishProgress(int percentComplete, String message) {
		lessonMaintanceCallBack.passbackPercentComplete(percentComplete, message);
	}

	private void finishProcess() {
		int maintenanceStatus = (cancel ? GlobalValues.CANCELLED
				: (deleteError ? GlobalValues.FINISHED_WITH_ERROR
						: GlobalValues.FINISHED_OK));
		languageSettings.setMaintenanceStatus(maintenanceStatus).commit();
		lessonMaintanceCallBack.completedProcess(maintenanceStatus, message);
	}

	// ILessonMaintenaceCallBack methods - end

	public void cancelTask(boolean cancel) {
		this.cancel = cancel;
	}

	// delete stuff and update progress every now and then
	// percent completes are arbitrarily assigned.
	private void deleteProcess() {
		setupDao();
		// see if language phrases and media may be used in teacher (say in
		// opposite manner Turkish -> English)
		getTeacherInfo(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getLearningLanguageId(),
				teacherFromToLanguage.getKnownLanguageId());
		if (deleteError || cancel == true)
			return;
		publishProgress(2, res.getString(R.string.deleting_lessons_by_class));
		// get list of classes for this teacher/language
		// for for each class delete the lesson phrases and lessons
		classNameList = getClassNames(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getId());
		if (classNameList != null) {
			for (int i = 0; i < classNameList.size(); ++i) {
				if (deleteError || cancel == true)
					return;
				deleteLessonList(classNameList.get(i).getId());
			}
		}
		if (deleteError || cancel == true)
			return;
		publishProgress(10, res.getString(R.string.deleting_classes));
		// delete all the classes for that teacher language
		deleteClassesForTeacher(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getId());
		if (deleteError || cancel == true)
			return;
		publishProgress(30,res.getString(R.string.deleting_cross_references));
		// delete the LanguageXrefs
		deleteLanguageXrefs(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getId());
		if (deleteError || cancel == true)
			return;
		publishProgress(40, res.getString(R.string.deleting_language_phrases));
		// if language phrases not used elsewhere delete them and also media files
		if (dontDeletePhrasesAndMedia)
			return;
		// not used so delete
		deleteLanguagePhrases(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getLearningLanguageId());
		if (deleteError || cancel == true)
			return;
		publishProgress(50,res.getString(R.string.deleting_language_phrases));
		deleteLanguagePhrases(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getKnownLanguageId());
		if (deleteError || cancel == true)
			return;
		publishProgress(60, res.getString(R.string.deleting_language_media));
		if (!deleteMediaFiles(teacherLanguage.getLearningLanguageMediaDirectory())) {
			// display some error that media files not all deleted
			passbackMessage(res.getString(
					R.string.media_directory_could_not_be_deleted,
					teacherLanguage.getLearningLanguageMediaDirectory()), true);
			deleteError = true;
		}
		if (!deleteMediaFiles(teacherLanguage.getKnownLanguageMediaDirectory())) {
			// display some error that media files not all deleted
			passbackMessage(res.getString(
					R.string.media_directory_could_not_be_deleted,
					teacherLanguage.getKnownLanguageMediaDirectory()), true);
			deleteError = true;
		}
		publishProgress(80, res.getString(R.string.deleting_language_media));

		if (teacherCount > 1)
			return;

		if (deleteError || cancel == true)
			return;
		// delete the teacherLanguageRecord then the teacher only teachers the one
		// language
		deleteTeacherLanguage(teacherFromToLanguage.getId());
		deleteTeacher(teacherFromToLanguage.getTeacherId());
		publishProgress(100, "");

	}

	private void deleteLanguageXrefs(long teacherId, long teacherLanguageId) {
		languageXrefDeleteQuery = languageXrefDao
				.queryBuilder()
				.where(
						LanguageXrefDao.Properties.TeacherId.eq(teacherId),
						LanguageXrefDao.Properties.TeacherLanguageId
								.eq(teacherLanguageId)).buildDelete();
		languageXrefDeleteQuery.executeDeleteWithoutDetachingEntities();

	}

	private ArrayList<ClassName> getClassNames(long teacherId,
			long teacherLanguageId) {
		return (ArrayList<ClassName>) classNameDao
				.queryBuilder()
				.where(
						ClassNameDao.Properties.TeacherId.eq(teacherId),
						ClassNameDao.Properties.TeacherLanguageId
								.eq(teacherLanguageId)).list();
	}

	private void getTeacherInfo(long teacherId, long learningLanguageId,
			long knownLanguageId) {
		Cursor cursor = null;
		// See if teacher has reversed teaching language (English ->Turkish and
		// Turkish->English)
		String sql = "select count(*) from " + TeacherLanguageDao.TABLENAME
				+ " where " + TeacherLanguageDao.Properties.TeacherId.columnName
				+ " = " + teacherId + " and "
				+ TeacherLanguageDao.Properties.LearningLanguageId.columnName
				+ " = " + knownLanguageId + " and "
				+ TeacherLanguageDao.Properties.KnownLanguageId.columnName + " = "
				+ learningLanguageId;

		cursor = daoSession.getDatabase().rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			// don't delete phrases and media as there are used by another class
			// from the same teacher
			if (cursor.getInt(0) > 0) {
				dontDeletePhrasesAndMedia = true;
				return;
			}
		}
		cursor.close();
		// See if teacher teaches multiple languages (and if so don't delete
		// teacher at end)
		sql = "select count(*) from " + TeacherLanguageDao.TABLENAME + " where "
				+ TeacherLanguageDao.Properties.TeacherId.columnName + " = "
				+ teacherId;
		cursor = daoSession.getDatabase().rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			teacherCount = cursor.getInt(0);
		}
		cursor.close();
		// get media directories
		teacherLanguage = teacherLanguageDao.queryBuilder()
				.where(TeacherLanguageDao.Properties.Id.eq(teacherId)).unique();
		if (teacherLanguage == null) {
			// record not found error should not occur
			passbackMessage(res.getString(R.string.teacher_language_not_found),
					true);
			deleteError = true;
			return;
		}

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

	private void deleteClassesForTeacher(long teacherId, long teacherLanguageId) {
		if (classNameDeleteQuery == null) {
			classNameDeleteQuery = classNameDao
					.queryBuilder()
					.where(
							ClassNameDao.Properties.TeacherId.eq(teacherId),
							ClassNameDao.Properties.TeacherLanguageId
									.eq(teacherLanguageId)).buildDelete();
		} else {
			classNameDeleteQuery.setParameter(0, teacherId);
		}
		classNameDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	public void deleteLessonList(long classNameId) {
		// first delete all lesson phrases for each lesson under that class
		deleteLessonList = getLessonListForClass(classNameId);
		if (deleteLessonList != null) {
			for (int i = 0; i < deleteLessonList.size(); ++i) {
				deleteLessonPhrasesForLesson(deleteLessonList.get(i).getId());
			}
		}
		// now delete the lessons
		deleteLessonsForClass(classNameId);
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

	private void deleteLessonsForClass(Long classId) {
		if (lessonDeleteQuery == null) {
			lessonDeleteQuery = lessonDao.queryBuilder()
					.where(LessonDao.Properties.ClassId.eq(classId)).buildDelete();
		} else {
			lessonDeleteQuery.setParameter(0, classId);
		}
		lessonDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	private void deleteLanguagePhrases(long teacherId, long languageId) {
		if (languagePhraseDeleteQuery == null) {
			languagePhraseDeleteQuery = languagePhraseDao
					.queryBuilder()
					.where(LanguagePhraseDao.Properties.TeacherId.eq(teacherId),
							LanguagePhraseDao.Properties.LanguageId.eq(languageId))
					.buildDelete();
		} else {
			languagePhraseDeleteQuery.setParameter(0, teacherId);
			languagePhraseDeleteQuery.setParameter(1, languageId);
		}
		languagePhraseDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	private void deleteTeacherLanguage(long languageId) {
		teacherLanguageDeleteQuery = teacherLanguageDao.queryBuilder()
				.where(TeacherLanguageDao.Properties.Id.eq(languageId))
				.buildDelete();
		teacherLanguageDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	private void deleteTeacher(long teacherId) {
		teacherDeleteQuery = teacherDao.queryBuilder()
				.where(TeacherDao.Properties.Id.eq(teacherId)).buildDelete();
		teacherDeleteQuery.executeDeleteWithoutDetachingEntities();
	}

	private boolean deleteMediaFiles(String directory) {
		// if directory exists, delete all files in directory then delete the
		// directory
		// else if no directory defined then perhaps all media via web so return
		// true also
		if (directory == null || directory.equals(""))
			return true;
		File dir = new File(
				languageSettings.getMediaDirectory() + "/" + directory);
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				f.delete();
			}
		}
		// The directory is now empty so delete it
		return dir.delete();

	}

}
