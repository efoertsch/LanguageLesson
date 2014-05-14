package com.fisincorporated.languagetutorial;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fisincorporated.languagetutorial.utility.FileUtil;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;


public class AboutActivity   extends ActionBarActivity {
	TextView tvAbout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity );
		tvAbout = (TextView) findViewById(R.id.about_activity_tvAbout);
		tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
		tvAbout.setText(Html.fromHtml(FileUtil.readAssetsText(this, "about.txt")));
		 


	}

	
}
