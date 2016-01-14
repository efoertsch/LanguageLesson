package com.fisincorporated.languagetutorial;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;



import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import  com.fisincorporated.languagetutorial.R;
 
public class StartupFragment extends MenuListFragment {

    @Override
    void prepareMenu() {
   	 //addMenuItem("1. Start Activity", StartExercise.class);
   	 //addMenuItem("2. List Prior Activities", PriorActivitiesListDoNotUse.class);
       //addMenuItem("3. Exercise Setup", ExerciseList.class);
       //addMenuItem("4. Program Options", ProgramOptions.class);
   	 addMenuItem(getResources().getString(R.string.start_learning), LessonListActivity.class);
       //addMenuItem(getResources().getString(R.string.start_lesson), LessonListActivity.class);
   	 addMenuItem(getResources().getString(R.string.language_db_maintenance), LanguageMaintenanceActivity.class);
        
    }
    
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 	}
 	
 	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//setHasOptionsMenu(true);
		return super.onCreateView(inflater, container, savedInstanceState);
	}


// 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
// 		super.onCreateOptionsMenu(menu, inflater);
// 		// Add the menu
// 		inflater.inflate(R.menu.main_menu, menu);
// 	}
// 	
// 	// handle the selected menu option
// 	public boolean onOptionsItemSelected(MenuItem item) {
// 		switch (item.getItemId()) {
// 		case R.id.language_db_maintenance:
// 			Intent intent = new Intent(getActivity(), LanguageMaintenanceActivity.class);
// 			startActivity(intent);
// 		default:
// 			// pass up to superclass
// 			return super.onOptionsItemSelected(item);
// 		}
// 	}

}