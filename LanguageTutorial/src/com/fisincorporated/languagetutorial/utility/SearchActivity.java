package com.fisincorporated.languagetutorial.utility;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.widget.ListView;

import com.fisincorporated.languagetutorial.LanguageApplication;
import com.fisincorporated.languagetutorial.MasterActivity;
import com.fisincorporated.languagetutorial.R;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.LanguageCodeDao;
import com.fisincorporated.languagetutorial.db.LanguagePhraseDao;
import com.fisincorporated.languagetutorial.db.LanguageXrefDao;
import com.fisincorporated.languagetutorial.db.SQLiteCursorLoader;

// For LoadManager some info from http://www.androiddesignpatterns.com/2012/07/understanding-loadermanager.html
public class SearchActivity extends MasterActivity {
	private String query = "";
	private SearchResultFragment searchResultFragment = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.language_search_results);
		if (savedInstanceState != null){
			// here due to orientation change and onSaveInstanceState fired prior to Activity being destroyed
			query = savedInstanceState.getString(SearchManager.QUERY);
		}
		else {
			getSearchQuery(getIntent());
			//	handleIntent(getIntent());
		}
	}

	@Override
	protected Fragment createFragment() {
		// Only fragment
		searchResultFragment = new SearchResultFragment();
		return searchResultFragment;
	}

	@Override
	public void onResume() {
		super.onResume();
		// in orientation change get reference to SearchResultFragment
		if (searchResultFragment == null){
			FragmentManager fm = getSupportFragmentManager();
			searchResultFragment = (SearchResultFragment) fm.findFragmentById(R.id.fragmentContainer);
		}

		searchResultFragment.doSearch(query);
	}

	// called if new search requested (SearchActivity still exists)
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		getSearchQuery(intent);
		// handleIntent(intent);
		searchResultFragment.doSearch(query);
	}

	// This is called before the activity is destroyed
	// Save query value so it can be picked up on orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SearchManager.QUERY, query);
		
	}

	public void getSearchQuery(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
		}
	}

	// Get the intent, verify the action and get the query
	// private void handleIntent(Intent intent) {
	// if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	// query = intent.getStringExtra(SearchManager.QUERY);
	// searchResultFragment.doSearch(query);
	// }
	// }

}