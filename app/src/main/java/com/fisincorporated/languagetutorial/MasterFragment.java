package com.fisincorporated.languagetutorial;

 
import com.fisincorporated.languagetutorial.db.DaoMaster;
import com.fisincorporated.languagetutorial.db.DaoSession;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public class MasterFragment extends Fragment {
	protected SQLiteDatabase database = null;
	 
	protected DaoMaster daoMaster ;
	protected DaoSession daoSession;


	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 getDaoSession();
	}
		
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);

	}
	public void onDetach(){
		super.onDetach();
	}

 
	
// this is for GreenDAO SQLite setup
// normally use daoSession with DAO objects
	// but database set in case you want to run sql directly
	public void getDatabaseSetup() {
	database = LanguageApplication.getInstance().getDb();
  
	}

public void getDaoSession() {
	daoSession = LanguageApplication.getInstance().getDaoSession();
}
   
	
	public void onDestroy() {
		if (database != null) {
			if (database.isOpen())
				database.close();
			database = null;
		}
		super.onDestroy();
	}

	@Override
	public void finalize() {
		if (database != null) {
			if (database.isOpen())
					database.close();
			database = null;
		}
	}

 
 
	
}
