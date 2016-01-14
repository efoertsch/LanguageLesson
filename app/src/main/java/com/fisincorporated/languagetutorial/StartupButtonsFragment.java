package com.fisincorporated.languagetutorial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
public class StartupButtonsFragment extends MasterFragment  {
	private Button btnStartLesson;
	private Button btnLanguageMaintenance;
	private Button btnAbout;
  

	public StartupButtonsFragment() {
		// TODO Auto-generated constructor stub
	}
  	
   @Override
   public void onCreate(Bundle savedInstanceState) {
   	super.onCreate(savedInstanceState);

   }
   
   @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.startup_buttons, 
					container, false);
		btnStartLesson =(Button) view.findViewById(R.id.btnStartLesson);
	  	btnLanguageMaintenance = (Button) view.findViewById(R.id.btnLanguageMaintenance);
	  	btnAbout = (Button) view.findViewById(R.id.btnAbout);
	  	
	  	btnStartLesson.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(getActivity(), LessonListActivity.class));
 				}
 			});
	  	btnLanguageMaintenance.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(getActivity(), LanguageMaintenanceActivity.class));
 				}
 			});
	  	
	  	btnAbout.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(getActivity(), AboutActivity.class));
 				}
 			});
 
 		return view;
 		
   }
 
}
