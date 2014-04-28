package com.fisincorporated.languagetutorial;

import java.io.File;
import java.util.ArrayList;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

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

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;

public class TeacherLanguageDelete extends AsyncTask<Void, Integer, Boolean> {
	private static final String TAG = "TeacherLanguageDelete";

	private DaoSession daoSession;
	private TeacherDao teacherDao;
	//private Teacher teacher;
	private TeacherLanguageDao teacherLanguageDao;
	private TeacherLanguage teacherLanguage;
	//private LanguageCodeDao languageCodeDao;
	//private LanguageCode learningLanguageCode;
	//private LanguageCode knownLanguageCode;
	private LanguagePhraseDao languagePhraseDao;
	//private LanguagePhrase learningLanguagePhrase;
	//private LanguagePhrase knownLanguagePhrase;
	private LanguageXrefDao languageXrefDao;
	// private LanguageXref languageXref;
	// private CompoundPhraseDao compoundPhraseDao;
	// private ClassName className;
	private ClassNameDao classNameDao;
	//private Lesson lesson;
	private LessonDao lessonDao;
	// private LessonPhrase lessonPhrase;
	private LessonPhraseDao lessonPhraseDao;

	//private Query<Teacher> teacherQuery = null;
	//private Query<LanguageCode> languageQuery = null;
	//private Query<TeacherLanguage> teacherLanguageQuery = null;
	//private Query<LanguagePhrase> languagePhraseQuery = null;
	//private Query<LanguageXref> languageXrefQuery = null;
	//private Query<ClassName> classNameQuery = null;
	//private Query<ClassName> classNameByTitleQuery = null;
	//private Query<Lesson> lessonQuery = null;
	private Query<Lesson> lessonListQuery = null;
	//private Query<LanguageXref> languageXrefListQuery = null;
	//private Query<LessonPhrase> lessonPhraseQuery = null;
	//private DeleteQuery<CompoundPhrase> compoundPhraseDeleteQuery = null;
	private DeleteQuery<LessonPhrase> lessonPhraseDeleteQuery = null;
	private DeleteQuery<Lesson> lessonDeleteQuery = null;
	private DeleteQuery<ClassName> classNameDeleteQuery = null;
	private DeleteQuery<LanguageXref> languageXrefDeleteQuery = null;
	private DeleteQuery<LanguagePhrase> languagePhraseDeleteQuery;
	private DeleteQuery<TeacherLanguage> teacherLanguageDeleteQuery;
	private DeleteQuery<Teacher> teacherDeleteQuery;

	//private ArrayList<ClassName> deleteClassList = new ArrayList<ClassName>();
	private ArrayList<Lesson> deleteLessonList = new ArrayList<Lesson>();

	//private long teacherId;
	//private long teacherLanguageId;
	private DeleteTeacherLanguageFragment deleteTeacherLanguageFragment;

	private boolean deleteError = false;
	private boolean cancel = false;
	private String errorMsg = "";
	private Resources res;

	private TeacherFromToLanguage teacherFromToLanguage;

	private boolean dontDeletePhrasesAndMedia = false;

	private ArrayList<ClassName> classNameList;
	private int teacherCount = 0;

	// The fragment passed in should be non-ui fragment
	public TeacherLanguageDelete(
			DeleteTeacherLanguageFragment deleteTeacherLanguageFragment,
			TeacherFromToLanguage teacherFromToLanguage) {
		this.deleteTeacherLanguageFragment = deleteTeacherLanguageFragment;
		this.teacherFromToLanguage = teacherFromToLanguage;
		res = deleteTeacherLanguageFragment.getActivity().getResources();

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

	@Override
	protected void onPreExecute() {
		if (deleteTeacherLanguageFragment == null)
			return;
		deleteTeacherLanguageFragment.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		deleteProcess();
		return deleteError || cancel;

	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		if (deleteTeacherLanguageFragment == null)
			return;
		deleteTeacherLanguageFragment.updateProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (deleteTeacherLanguageFragment == null)
			return;
		deleteTeacherLanguageFragment.taskFinished(
				cancel ? GlobalValues.CANCELLED
						: (deleteError ? GlobalValues.FINISHED_WITH_ERROR
								: GlobalValues.FINISHED_OK), errorMsg);
	}

	// call back with error message
	// Save the error msg for passing back on onPostExecute - and via UI thread
	private void passbackErrorMessage(String errorMsg) {
		this.errorMsg = this.errorMsg + "\n" +  errorMsg;
	}

	 

	public void cancelTask(boolean cancel) {
		this.cancel = cancel;
	}

	// delete stuff and update progress every now and then
	private void deleteProcess() {
		setupDao();
		// see if language phrases and media may be used in teacher (say in
		// opposite manner Turkish -> English)
		getTeacherInfo(teacherFromToLanguage.getTeacherId(), teacherFromToLanguage.getLearningLanguageId(), teacherFromToLanguage.getKnownLanguageId());
		if (deleteError || cancel == true)
			return;
		publishProgress(2);
		// get list of classes for this teacher/language
		// for for each class delete the lesson phrases and lessons
		classNameList = getClassNames(teacherFromToLanguage.getTeacherId(), teacherFromToLanguage.getId());
		if (classNameList != null) {
			for (int i = 0; i < classNameList.size(); ++i) {
				if (deleteError || cancel == true)
					return;
				deleteLessonList(classNameList.get(i).getId());
			}
		}
		if (deleteError || cancel == true)
			return;
		publishProgress(10);
		// delete all the classes for that teacher language
		deleteClassesForTeacher(teacherFromToLanguage.getTeacherId(), teacherFromToLanguage.getId());
		if (deleteError || cancel == true)
			return;
		publishProgress(30);
		// delete the LanguageXrefs
		deleteLanguageXrefs(teacherFromToLanguage.getTeacherId(), teacherFromToLanguage.getId());
		if (deleteError || cancel == true)
			return;
		publishProgress(40);
		// if language phrases not used elsewhere delete them and also media files
		if (dontDeletePhrasesAndMedia)
			return;
		// not used so delete
		deleteLanguagePhrases(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getLearningLanguageId());
		if (deleteError || cancel == true)
			return;
		publishProgress(50);
		deleteLanguagePhrases(teacherFromToLanguage.getTeacherId(),
				teacherFromToLanguage.getKnownLanguageId());
		if (deleteError || cancel == true)
			return;
		publishProgress(60);
		if (!deleteMediaFiles(teacherLanguage.getLearningLanguageMediaDirectory())) {
			// display some error that media files not all deleted
			passbackErrorMessage(res.getString(R.string.media_directory_could_not_be_deleted,teacherLanguage.getLearningLanguageMediaDirectory() ));
			deleteError = true;
		}
		if (!deleteMediaFiles(teacherLanguage.getKnownLanguageMediaDirectory())) {
				// display some error that media files not all deleted
				passbackErrorMessage(res.getString(R.string.media_directory_could_not_be_deleted,teacherLanguage.getKnownLanguageMediaDirectory() ));
				deleteError = true;
		}
		publishProgress(80);

		if (teacherCount > 1)
			return;

		if (deleteError || cancel == true)
			return;
		// delete the teacherLanguageRecord then the teacher only teachers the one
		// language
		deleteTeacherLanguage(teacherFromToLanguage.getId());
		deleteTeacher(teacherFromToLanguage.getTeacherId());
		publishProgress(100);

	}

	private void deleteLanguageXrefs(long teacherId, long teacherLanguageId) {
		languageXrefDeleteQuery = languageXrefDao
				.queryBuilder()
				.where(
						LanguageXrefDao.Properties.TeacherId.eq(teacherId),
						LanguageXrefDao.Properties.TeacherLanguageId
								.eq(teacherLanguageId))
				.buildDelete();
		languageXrefDeleteQuery.executeDeleteWithoutDetachingEntities();

	}

	private ArrayList<ClassName> getClassNames(long teacherId, long teacherLanguageId) {
		return (ArrayList<ClassName>) classNameDao
				.queryBuilder()
				.where(
						ClassNameDao.Properties.TeacherId.eq(teacherId),
						ClassNameDao.Properties.TeacherLanguageId
								.eq(teacherLanguageId))
				.list();
	}

	private void getTeacherInfo(long teacherId, long learningLanguageId, long knownLanguageId) {
		Cursor cursor = null;
		int count = 0;
		// See if teacher has reversed teaching language (English ->Turkish and
		// Turkish->English)
		String sql = "select count(*) from " + TeacherLanguageDao.TABLENAME
				+ " where " + TeacherLanguageDao.Properties.TeacherId.columnName
				+ " = " + teacherId + " and "
				+ TeacherLanguageDao.Properties.LearningLanguageId.columnName
				+ " = " + knownLanguageId + " and "
				+ TeacherLanguageDao.Properties.KnownLanguageId.columnName + " = "
				+ learningLanguageId ;

		cursor = daoSession.getDatabase().rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			// don't delete phrases and media as there are used by another class
			// from the same teacher
			if (cursor.getInt(0) > 0){
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
		if (cursor.getCount() > 0){
			cursor.moveToFirst();
			teacherCount = cursor.getInt(0);
		}
		cursor.close();
		// get media directories
		teacherLanguage = teacherLanguageDao
				.queryBuilder()
				.where(
						TeacherLanguageDao.Properties.Id.eq(teacherId)).unique();
		if (teacherLanguage == null) {
			// record not found error should not occur
			passbackErrorMessage(res
					.getString(R.string.teacher_language_not_found));
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
			classNameDeleteQuery = classNameDao.queryBuilder()
					.where(ClassNameDao.Properties.TeacherId.eq(teacherId),
							ClassNameDao.Properties.TeacherLanguageId.eq(teacherLanguageId))
					.buildDelete();
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
		File dir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
						+ "/" + directory);
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				f.delete();
			}
		}
		// The directory is now empty so delete it
		return dir.delete();

	}
}