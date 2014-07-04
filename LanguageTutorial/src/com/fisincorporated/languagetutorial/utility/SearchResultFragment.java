package com.fisincorporated.languagetutorial.utility;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fisincorporated.languagetutorial.LanguagePhrasePlayer;
import com.fisincorporated.languagetutorial.LanguagePhrasePlayer.LanguagePhrasePlayerListener;
import com.fisincorporated.languagetutorial.MasterFragment;
import com.fisincorporated.languagetutorial.R;
import com.fisincorporated.languagetutorial.db.LanguageCodeDao;
import com.fisincorporated.languagetutorial.db.LanguagePhraseDao;
import com.fisincorporated.languagetutorial.db.LanguageXrefDao;
import com.fisincorporated.languagetutorial.db.SQLiteCursorLoader;
import com.fisincorporated.languagetutorial.db.TeacherLanguageDao;

public class SearchResultFragment extends MasterFragment implements
		LoaderCallbacks<Cursor> {
	protected static final String TAG = "SearchResultFragment";
	private ListView lvSearchListView;
	private String query = "";
	private String fromLanguage = "fromLanguage";
	private String toLanguage = "toLanguage";
	private String audioAvailable = "audioAvailable";
	private LanguagePhrasePlayer languagePhrasePlayer;
	private static SearchResultFragment searchResultFragment;
	private static LanguageSettings languageSettings;
	private String mediaDirectory;
	private SearchRecentSuggestions suggestions;

	public SearchResultFragment() {
		searchResultFragment = this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
		setRetainInstance(true);
		createLanguagePhrasePlayer();
		suggestions = new SearchRecentSuggestions(getActivity(),
            MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
       
	}

	private void createLanguagePhrasePlayer() {
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
		
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		languageSettings = LanguageSettings.getInstance(getActivity());
		mediaDirectory = languageSettings.getMediaDirectory();
		
		View view = inflater.inflate(R.layout.search_results, container, false);
		lvSearchListView = (ListView) view.findViewById(R.id.lvSearchList);
		lvSearchListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (((ImageView) view.findViewById(R.id.ivAudioImage)).getVisibility() == View.VISIBLE){
					String languageMediaDirectory = ((TextView)view.findViewById(R.id.tvMediaDirectory)).getText().toString();
					String audioFile = ((TextView)view.findViewById(R.id.tvAudioFile)).getText().toString();
					playLearningLanguagePhrase(mediaDirectory + System.getProperty("file.separator") + languageMediaDirectory, audioFile);
				}
				
			}});
				return view;
	}
	
	private void playLearningLanguagePhrase(String mediaDirectory, String audioFile){
		PhrasePlayRequest phrasePlayRequest = new PhrasePlayRequest(mediaDirectory,
				audioFile , 0,	0);
		languagePhrasePlayer.queuePlayRequest(
				LanguagePhrasePlayer.PLAY_PHRASE, phrasePlayRequest);
	}
	
	public void doSearch(String query) {
		this.query = query;
      suggestions.saveRecentQuery(query, null);
		getLoaderManager().restartLoader(0, null, this);

	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		languagePhrasePlayer.clearQueue();
		Log.i(TAG, "onDestroyView  cleared loader and player queues");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Now you can terminate thread as you are destroying fragment
		languagePhrasePlayer.quit();
		Log.i(TAG, "Background threads destroyed");
	}
	

	// LoaderCallBacks interface methods
	// #1
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// we only ever load the runs, so assume this is the case
		return new ListCursorLoader(getActivity());
	}

	// #2
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Note the version without the last parm (0 - flags) is deprecated
		// Just pass in zero to get it to use non-deprecated version.
		// Also remember your sql MUST have a _id column
//		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
//				android.R.layout.simple_expandable_list_item_2, cursor,
//				new String[] { fromLanguage, toLanguage }, new int[] {
//						android.R.id.text1, android.R.id.text2 }, 0);
	// Strangely - last row of search sometimes missing, so implemented setViewBinder to check results
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.search_list_detail, cursor,
				new String[] { fromLanguage, toLanguage, LanguagePhraseDao.Properties.MediaFile.columnName
						,TeacherLanguageDao.Properties.LearningLanguageMediaDirectory.columnName, audioAvailable  }, 
						new int[] {	R.id.tvSearchFrom, R.id.tvSearchTo, R.id.tvAudioFile, R.id.tvMediaDirectory, R.id.ivAudioImage}, 0);
		
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				//Log.i(TAG,"columIndex:" + columnIndex + " Value:" + cursor.getString(columnIndex));
				 switch (view.getId()) {
				 case R.id.tvAudioFile:
					 ((TextView) view).setText(cursor.getString(cursor.getColumnIndex(LanguagePhraseDao.Properties.MediaFile.columnName)));
					 return true;
				 case R.id.tvMediaDirectory:
					 ((TextView) view).setText(cursor.getString(cursor.getColumnIndex(TeacherLanguageDao.Properties.LearningLanguageMediaDirectory.columnName)));
					 return true;
				 case R.id.ivAudioImage:
					 if (cursor.getString(cursor.getColumnIndex(audioAvailable)).equals("")){
						 view.setVisibility(View.INVISIBLE);
					 }
					 return true;
				default:
					return false;
				 }
				 
				//return true;
			}
		});
		lvSearchListView.setAdapter(adapter);

	}

	// #3
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// stop using the cursor (via the adapter)
		lvSearchListView.setAdapter(null);
	}

	private static class ListCursorLoader extends SQLiteCursorLoader {
		public ListCursorLoader(Context context) {
			super(context);
		}

		@Override
		protected Cursor loadCursor() {
			return searchResultFragment.showSearchList();
		}
	}

	private Cursor showSearchList() {
		Cursor cursor = null;

		StringBuffer sb = new StringBuffer();
		// GreenDao does have its disadvantages when writing complete queries and still trying to use 
		// it's naming standards
		sb.append("select " + " T3." + LanguagePhraseDao.Properties.Id.columnName
				+ " , T3." + LanguageCodeDao.Properties.LanguageName.columnName
				+ " || ':' || T1."
				+ LanguagePhraseDao.Properties.WrittenPhrase.columnName + " "
				+ fromLanguage + ", T5."
				+ LanguageCodeDao.Properties.LanguageName.columnName
				+ " || ':' || T4."
				+ LanguagePhraseDao.Properties.WrittenPhrase.columnName + " "	+ toLanguage
				+ " ,IFNULL(T1." + LanguagePhraseDao.Properties.MediaFile.columnName + ", '') " +  LanguagePhraseDao.Properties.MediaFile.columnName
				+ " ,IFNULL(T6." + TeacherLanguageDao.Properties.LearningLanguageMediaDirectory.columnName + ", '' ) " +  TeacherLanguageDao.Properties.LearningLanguageMediaDirectory.columnName
				+ " ,IFNULL(T1." + LanguagePhraseDao.Properties.MediaFile.columnName + ", '') ||" 
				+ "    IFNULL(T6." + TeacherLanguageDao.Properties.LearningLanguageMediaDirectory.columnName + ", '' ) "
				+  audioAvailable  
				+ " from " + LanguagePhraseDao.TABLENAME + " T1"
				+ " inner join " + LanguageXrefDao.TABLENAME + " T2 " + " on  T1."
				+ LanguagePhraseDao.Properties.Id.columnName + " =  T2."
				+ LanguageXrefDao.Properties.LearningPhraseId.columnName
				+ " inner join " + LanguageCodeDao.TABLENAME + " T3 " + " on T1."
				+ LanguagePhraseDao.Properties.LanguageId.columnName + " =  T3."
				+ LanguageCodeDao.Properties.Id.columnName + " left outer join "
				+ LanguagePhraseDao.TABLENAME + " T4  on  T2."
				+ LanguageXrefDao.Properties.KnownPhraseId.columnName + " =  T4."
				+ LanguagePhraseDao.Properties.Id.columnName + " left outer join "
				+ LanguageCodeDao.TABLENAME + " T5 on T4."
				+ LanguagePhraseDao.Properties.LanguageId.columnName + " = T5."
				+ LanguageCodeDao.Properties.Id.columnName
				+ " inner join " 
				+ TeacherLanguageDao.TABLENAME + " T6 on T2."
				+ LanguageXrefDao.Properties.TeacherLanguageId.columnName + " = T6."
				+ TeacherLanguageDao.Properties.Id.columnName 
				+ " where ( T1."
				+ LanguagePhraseDao.Properties.WrittenPhrase.columnName
				+ " like '%" + query.trim() + "%' or  T4."
				+ LanguagePhraseDao.Properties.WrittenPhrase.columnName
				+ " like '%" + query.trim() + "%' )  "
				+ " and "+ " T1." + LanguagePhraseDao.Properties.PhraseType.columnName + " not in ('A','V') "
				+ " COLLATE NOCASE");
		cursor = daoSession.getDatabase().rawQuery(sb.toString(), null);

		if (cursor.getCount() == 0){
			return null;
		}
		return cursor;

	}

	
	
	 


	

}
