package com.fisincorporated.languagetutorial;

import java.util.ArrayList;

import com.fisincorporated.languagetutorial.db.LanguagePhrase;
import com.fisincorporated.languagetutorial.db.Lesson;
import com.fisincorporated.languagetutorial.db.LessonPhrase;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar.LayoutParams;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class LanguagePhraseAdapter extends ArrayAdapter<LanguagePhrase> {
	private final String TAG = "LanguagePhraseAdapter";
	private final Context context;
	private ArrayList<LanguagePhrase> learningLanguagePhraseList ;
	private Resources res;
	IAssignLessonLineText lessonLineCallback;
 
	public interface IAssignLessonLineText {
		void onAssignLessonLineText(LanguagePhraseViewHolder viewHolder, int position);
	}

	public class LanguagePhraseViewHolder {
		public LinearLayout llPhraseDetail;
		public TextView tvNumber;
		public TextSwitcher txtswtchrTvPhrase;
	}

	// textViewResourceId should be the detail row view (containing other
	// textview views to be assigned date in getView)
	public LanguagePhraseAdapter(Context context, IAssignLessonLineText lessonLineCallback, int textViewResourceId,
			ArrayList<LanguagePhrase> learningLanguagePhraseList ){
		// super(context, R.layout.phrase_detail, languagePhrases);
		super(context, textViewResourceId, learningLanguagePhraseList);
		this.context = context;
		this.lessonLineCallback = lessonLineCallback;
		this.learningLanguagePhraseList = learningLanguagePhraseList;
		res = context.getResources();
	}

	public void clear() {
		this.learningLanguagePhraseList = null;
	}

	public int getCount() {
		if (learningLanguagePhraseList == null) {
			return 0;
		} else
			return learningLanguagePhraseList.size();

	}

	public void resetValues(ArrayList<LanguagePhrase> learningLanguagePhraseList
			) {
		this.learningLanguagePhraseList = learningLanguagePhraseList;
		this.notifyDataSetChanged();
	}

	@Override
	// see http://www.vogella.com/tutorials/AndroidListView/article.html for
	// optimization of inflate/findViewById
	// do minimal stuff here, do most in interface callback
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv;
		View rowView = convertView;
		 if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LanguagePhraseAdapter.LanguagePhraseViewHolder viewHolder = new LanguagePhraseViewHolder();
			rowView = inflater.inflate(R.layout.phrase_detail, parent, false);
			viewHolder.llPhraseDetail = (LinearLayout) rowView
					.findViewById(R.id.llPhraseDetail);
			viewHolder.txtswtchrTvPhrase = (TextSwitcher) rowView.findViewById(R.id.txtswtchrTvPhrase);
			viewHolder.txtswtchrTvPhrase.setFactory(new ViewFactory() {
             public View makeView() {
                 TextView tv = new TextView(context);
                 tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                 return tv;
             }
         });
			viewHolder.tvNumber = (TextView) rowView.findViewById(R.id.tvNumber);
			rowView.setTag(viewHolder);
		 }

		LanguagePhraseAdapter.LanguagePhraseViewHolder viewHolder = (LanguagePhraseAdapter.LanguagePhraseViewHolder) rowView
				.getTag();
		// fill in all the blanks
		lessonLineCallback.onAssignLessonLineText(viewHolder, position);
		parent.requestLayout();
		return rowView;
	}

}
