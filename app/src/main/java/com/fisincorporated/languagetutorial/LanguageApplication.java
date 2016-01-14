package com.fisincorporated.languagetutorial;

import com.fisincorporated.languagetutorial.db.DaoMaster;
import com.fisincorporated.languagetutorial.db.DaoMaster.DevOpenHelper;
import com.fisincorporated.languagetutorial.db.DaoSession;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.widget.Toast;

public class LanguageApplication extends Application {

	private static LanguageApplication singleton = null;
	private static DaoMaster daoMaster;
	private static DaoSession daoSession;
	private static SQLiteDatabase db;
	public static final String LANGUAGE_DB = "language_db";
	public static String PACKAGE_NAME ;
	private static String DB_PATH;
	private static boolean  DB_EXISTS = false;
	private static DevOpenHelper  helper;

	public static LanguageApplication getInstance() {
		return singleton;
	}

	// Android should call this when app first started
	public final void onCreate() {
		super.onCreate();
		singleton = this;
		PACKAGE_NAME = getPackageName();
		//new CreateDatabaseAsync().execute();
		helper = new DaoMaster.DevOpenHelper(getApplicationContext(), LANGUAGE_DB,null);
		try {
			db = helper.getWritableDatabase();
			daoMaster = new DaoMaster(db);
			daoSession = daoMaster.newSession();
			DB_EXISTS = true;
		}
		catch (SQLiteException sqle){
			Toast.makeText(getApplicationContext(),R.string.sqlite_open_error, Toast.LENGTH_LONG).show();
			DB_EXISTS = false;
		}
		 

	}

	public  DaoMaster getDaoMaster() {
		if (DB_EXISTS){	
		return daoMaster;
	}
	else return null;
	}

	// to access database tables 
	// daoSession = daoMaster.newSession();
   // noteDao = daoSession.getSomeObjectDao();
	public   DaoSession getDaoSession() {
		if (DB_EXISTS){
		return daoSession;
		}
		else return null;
	}

	public   SQLiteDatabase getDb() {
		if (DB_EXISTS){
			return  db;
		}
		else return null;
	}

	public static String getLanguageDb() {
		return LANGUAGE_DB;
	}
	
	public static String getDBPath(Context context){
		if(android.os.Build.VERSION.SDK_INT >= 17){
	       DB_PATH = context.getApplicationInfo().dataDir + "/databases/";         
	    }
	    else
	    {
	       DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
	    }
		return DB_PATH;

	}
	
	private class CreateDatabaseAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			helper = new DaoMaster.DevOpenHelper(getApplicationContext(), LANGUAGE_DB,null);
			return null;
		}
		// remember to include (in this case) Void parm to match the AsyncTask definition
		protected void onPostExecute(Void result) {
			 // check for database created/available
			// and if available open, assign daoMaster and daoSession
			try {
				db = helper.getWritableDatabase();
				daoMaster = new DaoMaster(db);
				daoSession = daoMaster.newSession();
				DB_EXISTS = true;
			}
			catch (SQLiteException sqle){
				Toast.makeText(getApplicationContext(),R.string.sqlite_open_error, Toast.LENGTH_LONG).show();
				DB_EXISTS = false;
			}
				
		}
	}

}
