package com.fisincorporated.languagetutorial;

import java.util.Hashtable;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.LanguagePhrase;
import com.fisincorporated.languagetutorial.db.LanguagePhraseDao;
import com.fisincorporated.languagetutorial.db.LanguageXref;
import com.fisincorporated.languagetutorial.db.LanguageXrefDao;
import com.fisincorporated.languagetutorial.utility.LanguagePhraseRequest;

import de.greenrobot.dao.query.Query;

public class LanguagePhraseLoader extends HandlerThread {
	private static final String TAG = "LanguagePhraseLoader";
	public static final int LOAD_LANGUAGE_PHRASE = 0;
	public static final int PRELOAD_LANGUAGE_PHRASE = 1;
	public static final int CLEAR = 2;
	private Handler mResponseHandler;
	private LanguagePhraseLoadListener mListener;
	private Hashtable<Long, LanguageXref> languageXrefTable = new Hashtable<Long, LanguageXref>();
	private Hashtable<Long, LanguagePhrase> languagePhraseTable = new Hashtable<Long, LanguagePhrase>();
	private DaoSession daoSession;
	private LanguageXrefDao languageXrefDao;
	private LanguagePhraseDao languagePhraseDao;
	private Query<LanguageXref> languageXrefQuery = null;
	private Query<LanguagePhrase> languagePhraseQuery = null;
	

	private Handler mHandler;

	public interface LanguagePhraseLoadListener {
		void onLanguagePhraseAdded(long lessonId, int lessonOrder,
				LanguagePhrase learningLanguagePhrase,
				LanguagePhrase knownLanguagePhrase);

		void onErrorOccurred();

	}

	public void setListener(LanguagePhraseLoadListener listener) {
		mListener = listener;
	}

	public LanguagePhraseLoader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
		daoSession = LanguageApplication.getInstance().getDaoSession();
		languageXrefDao = daoSession.getLanguageXrefDao();
		languagePhraseDao = daoSession.getLanguagePhraseDao();
	}

	// Either getting a request to find and send back language phrases for a
	// particular lesson phrase
	// Or got a request to clear the current hashmaps as the lesson has changed.
	// This method is running on UI thread from call but just puts message on
	// queue
	// So fast operation is no problemo
	public void queueLoadRequest(int what,
			LanguagePhraseRequest languagePhraseRequest) {
		// Make sure to append .sendToTarget()!
		mHandler.obtainMessage(what, languagePhraseRequest).sendToTarget();
	}

	@SuppressLint("HandlerLeak")
	@Override
	// overridden so we can create Handler and implement handleMessage that
	// handles the messages on the queue
	// (that are sent here by the looper)
	protected void onLooperPrepared() {
		// the handler created here is associated with LanguagePhraseLoader
		// (HandlerThread) not the UI thread
		mHandler = new Handler() {
			@Override
			// Called by the looper as it processes the messages on queue one by
			// one
			public void handleMessage(Message msg) {
				if (msg.what == LOAD_LANGUAGE_PHRASE
						|| msg.what == PRELOAD_LANGUAGE_PHRASE) {
					LanguagePhraseRequest languagePhraseRequest = (LanguagePhraseRequest) msg.obj;
					Log.i(TAG, "Got request for langauge_xref_id:"
							+ languagePhraseRequest.getLanguagePhraseXrefId());
					handleRequest(msg.what, languagePhraseRequest);
				}
				if (msg.what == CLEAR) {
					clearHashMaps();

				}
			}
		};
	}

	// This is being executed on background thread
	// Read the LanguageXref record for the languageXrefId
	// Then for the learningPhraseId and the knownPhraseId on the LanguageXref
	// record read the corresponding LanguagePhrase records
	// when read, return to LessonPhraseFragment
	private void handleRequest(int command,
			LanguagePhraseRequest languagePhraseRequest) {
		// make sure these variables are defined as final 
		// if not then a subsequent request may be handled before the post below occurs
		// and post will send the subsequent request values.
		final LanguageXref languageXref;
		final LanguagePhrase learningLanguagePhrase;
		final LanguagePhrase knownLanguagePhrase ;
		final Long lessonId = languagePhraseRequest.getLessonId();
		final int lessonOrder = languagePhraseRequest.getLessonOrder();
		languageXref = getLanguageXrefForPhrase(languagePhraseRequest
				.getLanguagePhraseXrefId());

		if (languageXref == null) {
			mListener.onErrorOccurred();
			return;
		}

		// get the learning languagePhrase that the languageXref points to
		learningLanguagePhrase = getLanguagePhraseForXref(languageXref
				.getLearningPhraseId());
		if (learningLanguagePhrase == null) {
			mListener.onErrorOccurred();
			return;
		}
		// see if known phrase and if so add to table also
		if (languageXref.getKnownPhraseId() != null
				&& languageXref.getKnownPhraseId() != -1) {
			knownLanguagePhrase = getLanguagePhraseForXref(languageXref
					.getKnownPhraseId());
		}
		else {
			knownLanguagePhrase = null;
		}

		// got this far so pass back the languagePhrases
		// mResponseHandler is handler for UI thread, the update will run on
		// main thread
		if (command == LOAD_LANGUAGE_PHRASE) {
			mResponseHandler.post(new Runnable() {
				public void run() {
					// check to see if on same teacher/class/lesson
					// load language phrases to display arrays
					mListener.onLanguagePhraseAdded(lessonId, lessonOrder,
							learningLanguagePhrase, knownLanguagePhrase);
				}
			});
		}
	}

	public void clearHashMaps() {
		languageXrefTable.clear();
		languagePhraseTable.clear();
	}

	private LanguageXref getLanguageXrefForPhrase(long id) {
		// see if LanguageXref in hashTable
		// if not read from database (be able to handle possibility that it is not
		// there)
		LanguageXref languageXref = languageXrefTable.get(id);
		if (languageXref == null) {
			languageXref = loadLanguageXrefToTable(id);
		}
		return languageXref;
	}

	private LanguageXref loadLanguageXrefToTable(long id) {
		LanguageXref languageXref = null;
		if (languageXrefQuery == null) {
			languageXrefQuery = languageXrefDao.queryBuilder()
					.where(LanguageXrefDao.Properties.Id.eq(id)).build();
		} else {
			languageXrefQuery.setParameter(0, id);
		}
		languageXref = (LanguageXref) languageXrefQuery.unique();
		languageXrefTable.put(id, languageXref);
		return languageXref;
	}

	private LanguagePhrase getLanguagePhraseForXref(long id) {
		// see if LanguageXref in hashTable
		// if not read from database (be able to handle possibility that it is not
		// there)
		LanguagePhrase languagePhrase = languagePhraseTable.get(id);
		if (languagePhrase == null) {
			languagePhrase = loadLanguagePhraseToTable(id);
		}
		return languagePhrase;
	}

	private LanguagePhrase loadLanguagePhraseToTable(long id) {
		LanguagePhrase languagePhrase = null;
		if (languagePhraseQuery == null) {
			languagePhraseQuery = languagePhraseDao.queryBuilder()
					.where(LanguagePhraseDao.Properties.Id.eq(id)).build();
		} else {
			languagePhraseQuery.setParameter(0, id);
		}
		languagePhrase = (LanguagePhrase) languagePhraseQuery.unique();
		languagePhraseTable.put(id, languagePhrase);
		return languagePhrase;
	}
	
	public void clearQueue() {
		mHandler.removeMessages(LOAD_LANGUAGE_PHRASE);
		mHandler.removeMessages(PRELOAD_LANGUAGE_PHRASE);
		mHandler.removeMessages(CLEAR);
		clearHashMaps();
	}

}
