package com.fisincorporated.languagetutorial;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.fisincorporated.languagetutorial.utility.SearchActivity;

// This template is for a FragmentActivity that can implement fragments either with
// 1 fragment for small screen (phone) or 2 fragments (parent/child) if on larger screen (tablet)
// Create concrete FragmentActivity class by extending this class.
// The IHandleSelectedAction is to handle callbacks from the Fragment
public abstract class MasterActivity extends AppCompatActivity implements
		 ActionBar.OnMenuVisibilityListener {

	protected static final String TAG = "MasterActivity";
	protected ActionBar actionBar;
	private SearchView searchView = null;
	private SearchManager searchManager = null; 

	protected abstract Fragment createFragment();

	// added for master/detail fragments as on tablet
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}

	// Called at the start of the full lifetime.
	// Initialize Activity
	// Inflate the UI (handled in superclass.
	// Get references to fragments
	// Allocate references to class variables
	// Bind data to controls
	// Start Services and Timers
	// Use Bundle as needed to restore the UI to its previous state (or wait to
	// do it in onRestoreInstanceState)
	// Create any objects needed during life of activity
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		
		// do whatever needed for action bar https://developer.android.com/guide/topics/ui/actionbar.html#SplitBar
 		actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}
	}

	@Override
	public void onMenuVisibilityChanged(boolean arg0) {
		supportInvalidateOptionsMenu();

	}
	
// Add the menu , in this case just search icon
	@Override
	@TargetApi(11)
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getMenuInflater().inflate(R.menu.search_menu, menu);
			searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			// Pull out search view
			MenuItem searchItem = menu.findItem(R.id.menu_item_search);
			searchView = (SearchView) searchItem.getActionView();
			// Get data from searchable.xml as Searchable info
			searchView.setOnQueryTextListener(new OnQueryTextListener(){

				@Override
				public boolean onQueryTextChange(String arg0) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean onQueryTextSubmit(String query) {
					Log.i(TAG,"Got click on search submit");
					doSearch(query);
					return true;
				}});
		
			ComponentName name = getComponentName();
			SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
			searchView.setSearchableInfo(searchInfo);
			return true;
		}
		return false;
		
	}
	
	public void doSearch(String query){
		Intent intent = new Intent(this,SearchActivity.class);
		intent.setAction(Intent.ACTION_SEARCH);
		intent.putExtra(SearchManager.QUERY, query);
		startActivity(intent);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_search:
			onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume(); 
		if (searchManager != null){
			searchManager.stopSearch();
		}
	}

	

}
