package com.fisincorporated.languagetutorial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


// code from http://stackoverflow.com/questions/6540906/android-simple-export-and-import-of-sqlite-database
// with slight modifications
public class BackupRestoreFragment extends MasterFragment  {
	private Button btnBackup;
	private Button btnRestore;
	private String externalDir = "";
   private String backupFileName = 	LanguageApplication.LANGUAGE_DB;
	private TextView backupLoc  ;
	private static final int BACKUP = 0;
	private static final int RESTORE = 1;
	private Resources res;

  	
   @Override
   public void onCreate(Bundle savedInstanceState) {
   	super.onCreate(savedInstanceState);
   	res = getResources();
   }
   
   @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.backup_restore_db, 
					container, false);
			backupLoc =(TextView) view.findViewById(R.id.backup_restore_db_backup_loc);
	  		backupLoc.setText(externalDir.toString());
 		btnBackup = (Button) view.findViewById(R.id.backup_restore_db_btnBackup);
 		btnBackup.setOnClickListener(new View.OnClickListener() {
 			@SuppressLint("DefaultLocale")
 			public void onClick(View v) {
					showBackupDialog();
 				}
 			});
 		 

 		btnRestore = (Button) view.findViewById(R.id.backup_restore_db_btnRestore);
 		btnRestore.setOnClickListener(new View.OnClickListener() {
 			@SuppressLint("DefaultLocale")
 			public void onClick(View v) {
 				showRestoreDialog() ;
 			}
 		});
 		
 		return view;
 		
   }
   
   private void showBackupDialog() {
		LanguageDialogFragment dialog = LanguageDialogFragment.newInstance(-1,
				R.string.press_backup_to_make_backup,
				R.string.backup, -1,
				R.string.cancel);
		dialog.setTargetFragment(BackupRestoreFragment.this, BACKUP);
		dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
	}
   
   private void showRestoreDialog() {
 		LanguageDialogFragment dialog = LanguageDialogFragment.newInstance(-1,
 				R.string.press_restore_to_restore,
 				R.string.restore, -1,
 				R.string.cancel);
 		dialog.setTargetFragment(BackupRestoreFragment.this, RESTORE);
 		dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
 	}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	if (resultCode != Activity.RESULT_OK)
		return;
	if (requestCode == BACKUP) {
		int buttonPressed = intent.getIntExtra(
				LanguageDialogFragment.DIALOG_RESPONSE, -1);
		if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
			 exportDB();
		} else {
			Toast.makeText(getActivity(),getResources().getText(R.string.backup_cancelled), Toast.LENGTH_SHORT).show();
		}
		return;
	} else if (requestCode == RESTORE) {
		int buttonPressed = intent.getIntExtra(
				LanguageDialogFragment.DIALOG_RESPONSE, -1);
		if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
			importDB();
		} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
			Toast.makeText(getActivity(),getResources().getText(R.string.restore_cancelled), Toast.LENGTH_SHORT).show();
		}
	}
}
 		
 		private boolean createExternalBackupDir(){
 		//creating a new folder for the database to be backed up to
       File direct = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); 
       externalDir = direct.toString();
       if(! direct.exists())
           {
               if( direct.isDirectory() || direct.mkdirs() ) 
                 {
                  return true;
                 }
               else  {
               	Toast.makeText(getActivity(), "Error creating directory on SD card: " + direct.toString(),Toast.LENGTH_LONG).show();
               	return false;
               }
           }
       return true;
    }

//exporting database 
   @SuppressWarnings("resource")
	private void exportDB() {
   	FileChannel src = null;
   	FileChannel dst = null;
   	if (!createExternalBackupDir()) return;
       try {
           File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
           File data = Environment.getDataDirectory();

           if (sd.canWrite()) {
         	  String  currentDBPath= "//data//" + LanguageApplication.PACKAGE_NAME
                    + "//databases//" + LanguageApplication.LANGUAGE_DB;
               File currentDB = new File(data, currentDBPath);
               File backupDB = new File(sd,    backupFileName);
               src = new FileInputStream(currentDB).getChannel();
               dst = new FileOutputStream(backupDB).getChannel();
               dst.transferFrom(src, 0, src.size());
               Toast.makeText(getActivity(), "Backup written to " +  backupDB.toString(),
                       Toast.LENGTH_LONG).show();
           }
       } catch (Exception e) {
           Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
       }
       finally{
      	  try {if (src != null)  src.close(); } catch (Exception e) {}
           try {if (dst != null)   dst.close(); } catch (Exception e) {}

      	 
       }
   }
   
 //importing database
   @SuppressWarnings("resource")
	private void importDB() {
   	FileChannel src = null;
   	FileChannel dst = null;
       try {
      	 
           File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
           File data  = Environment.getDataDirectory();

           if (sd.canWrite()) {
               String  currentDBPath= "//data//" + LanguageApplication.PACKAGE_NAME
                       + "//databases//" + LanguageApplication.LANGUAGE_DB;
               
               File backupDB= new File(data, currentDBPath);
               File currentDB  = new File(sd,  backupFileName); 

               src = new FileInputStream(currentDB).getChannel();
               dst = new FileOutputStream(backupDB).getChannel();
               dst.transferFrom(src, 0, src.size());
               Toast.makeText(getActivity(), "Application database restored.",
                       Toast.LENGTH_LONG).show();
           }
       } catch (Exception e) {
      	 Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
        }
       finally{
     	  try {if (src != null)  src.close(); } catch (Exception e) {}
          try {if (dst != null)   dst.close(); } catch (Exception e) {}

     	 
      }
   }
   
   /* Checks if external storage is available for read and write */
   public boolean isExternalStorageWritable() {
       String state = Environment.getExternalStorageState();
       if (Environment.MEDIA_MOUNTED.equals(state)) {
           return true;
       }
       return false;
   }

   /* Checks if external storage is available to at least read */
   public boolean isExternalStorageReadable() {
       String state = Environment.getExternalStorageState();
       if (Environment.MEDIA_MOUNTED.equals(state) ||
           Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
           return true;
       }
       return false;
   }

}
