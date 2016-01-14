package com.fisincorporated.languagetutorial;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
	TextView tvAbout;
	private ActionBar actionBar;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.about_activity );
		// tvAbout = (TextView) findViewById(R.id.about_activity_tvAbout);
		// tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
		// tvAbout.setText(Html.fromHtml(FileUtil.readAssetsText(this,
		// "about.txt")));
		setContentView(R.layout.about_activity_with_demo_load);
		((TextView) findViewById(R.id.tv_about_string_1)).setText(getResources().getText(R.string.about_string_1));
		((TextView) findViewById(R.id.tv_about_string_2)).setText(getResources().getText(R.string.about_string_2));
		((TextView) findViewById(R.id.tv_about_string_3)).setText(getResources().getText(R.string.about_string_3));
		((TextView) findViewById(R.id.tv_about_string_4)).setText(getResources().getText(R.string.about_string_4));
		 ((Button) findViewById (R.id.btn_load_turkish_lesson)).setOnClickListener(new View.OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 loadDemoLesson(1);
			 }
		 });
		 ((Button) findViewById (R.id.btn_load_japanese_lesson)).setOnClickListener(new View.OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 loadDemoLesson(2);

			 }
		 });
		actionBar = getSupportActionBar();
		actionBar.setTitle(getResources().getString(R.string.about));
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
		 
		 private void loadDemoLesson(int i) {
			 Intent intent = new Intent(this,LanguageMaintenanceActivity.class);
			 switch (i){
			 case 1:
				 intent.putExtra(LanguageMaintenanceActivity.LOAD_FROM_BUNDLE, getResources().getString(R.string.demo_turkish_lesson_url));
				 break;
			 case 2:
				 intent.putExtra(LanguageMaintenanceActivity.LOAD_FROM_BUNDLE, getResources().getString(R.string.demo_japanese_lesson_url));
			 }
			 startActivity(intent);
		 }

}
