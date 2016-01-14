package com.fisincorporated.languagetutorial.mediaplayers;

import android.database.Cursor;
import android.support.v4.app.Fragment;

import com.fisincorporated.languagetutorial.GlobalValues;
import com.fisincorporated.languagetutorial.LanguageApplication;
import com.fisincorporated.languagetutorial.LessonPhraseFragment;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.LanguagePhraseDao;
import com.fisincorporated.languagetutorial.db.LanguageXrefDao;
import com.fisincorporated.languagetutorial.db.Lesson;
import com.fisincorporated.languagetutorial.db.LessonPhraseDao;
import com.fisincorporated.languagetutorial.db.TeacherLanguageDao;

public class MediaPlayerFragmentFactory {

	// Lesson should not be null
	public static Fragment getMediaPlayerFragment(Lesson lesson) {
		String[] mediaFileDir = new String[] { "", "" };
		if (lesson.getLessonType().equalsIgnoreCase("A")
				|| lesson.getLessonType().equalsIgnoreCase("V")) {
			getMediaFile(lesson.getId(), mediaFileDir);
			if (mediaFileDir[0].startsWith(GlobalValues.YOUTUBE_IND)) {
				return YouTubeLessonFragment.newInstance(mediaFileDir[0]
						.substring(GlobalValues.YOUTUBE_IND.length()));
			} else {
				if (lesson.getLessonType().equalsIgnoreCase("A")) {
					AudioPlayerFragment fragment = new AudioPlayerFragment();
					fragment.createMediaInfoBundle(mediaFileDir[0], mediaFileDir[1],
							lesson.getLessonTitle(), lesson.getDescription(),
							lesson.getLessonType());
					return fragment;
				} else {
					// create video player - testing - use for audio also
					VideoPlayerFragment fragment = new VideoPlayerFragment();
					fragment.createMediaInfoBundle(mediaFileDir[0], mediaFileDir[1],
							lesson.getLessonTitle(), lesson.getDescription(),
							lesson.getLessonType());
					return fragment;
				}
			}
		}
		else {
			return new LessonPhraseFragment();
		}		
	}

	// If lesson V or A
	// Find first lesson phrase for the lesson
	// Via the langauge_xref record find the language phrase media_file
	// Use the media file to determine if 1) youtube file or 2) some other file
	// put into AsyncTask once basic logic done
	private static void getMediaFile(Long lessonId, String[] mediaFileDir) {
		DaoSession daoSession;
		daoSession = LanguageApplication.getInstance().getDaoSession();

		Cursor cursor = null;
		String sql = "select "
				+ LanguagePhraseDao.Properties.MediaFile.columnName
				+ ", "
				+ TeacherLanguageDao.Properties.LearningLanguageMediaDirectory.columnName
				+ " from " + LessonPhraseDao.TABLENAME + " T " + " inner join "
				+ LanguageXrefDao.TABLENAME + " T1 on T."
				+ LessonPhraseDao.Properties.LanguageXrefId.columnName + " =  T1."
				+ LanguageXrefDao.Properties.Id.columnName + " inner join "
				+ LanguagePhraseDao.TABLENAME + " T2 on T1."
				+ LanguageXrefDao.Properties.LearningPhraseId.columnName + " = "
				+ " T2." + LanguagePhraseDao.Properties.Id.columnName
				+ " inner join " + TeacherLanguageDao.TABLENAME + " T3 " + "on T1."
				+ LanguageXrefDao.Properties.TeacherLanguageId.columnName
				+ " =  T3." + TeacherLanguageDao.Properties.Id.columnName
				+ " where T." + LessonPhraseDao.Properties.LessonId.columnName
				+ " = " + lessonId + " order by T."
				+ LessonPhraseDao.Properties.LessonOrder.columnName + " asc";
		try {
			cursor = daoSession.getDatabase().rawQuery(sql, null);
			if (cursor.getCount() > 0) {
				// Only 1 lessonPhrase allowed and read for V or A lesson
				cursor.moveToFirst();
				mediaFileDir[0] = cursor.getString(0);
				mediaFileDir[1] = cursor.getString(1);
			}
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
					;
				}
			}

		}
	}

}
