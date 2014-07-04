package com.fisincorporated.languagetutorial;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.languagetutorial.db.ClassNameDao;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.Lesson;
import com.fisincorporated.languagetutorial.db.LessonDao;
import com.fisincorporated.languagetutorial.db.TeacherLanguageDao;
import com.fisincorporated.languagetutorial.interfaces.IHandleSelectedAction;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

import de.greenrobot.dao.query.QueryBuilder;

public class LessonListFragment extends MasterFragment {

	private DaoSession daoSession;
	private LessonDao lessonDao;
	// private ClassNameDao classNameDao;
	private ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
	// private QueryBuilder<Lesson> qb;
	// private Long teacherId;
	// private Long teacherLanguageId;
	private String teacherName;
	private Long classId;
	private String className;
	// private Long learningLanguageId;
	private String learningLanguageName;
	// used to store values to SharedPreferences file
	private LanguageSettings languageSettings;
	// private LessonSelectionDialog lessonSelectionDialog;
	private LessonListAdapter lessonListAdapter;
	private TextView tvTeacherLanguage;
	// private TextView tvLanguage;
	private TextView tvClass;
	private View mainView;
	private ListView listView;

	// values for dialog
	private static final int SELECT_CLASS = 1;

	protected IHandleSelectedAction callBacks;
	private ActionBarActivity actionBarActivity;
	private int selectedLessonPosition = -1;
	 View highlightedLesson = null;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callBacks = (IHandleSelectedAction) activity;
		actionBarActivity = ((ActionBarActivity) activity);
	}

	public void onDetach() {
		super.onDetach();
		callBacks = null;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		daoSession = LanguageApplication.getInstance().getDaoSession();
		lessonDao = daoSession.getLessonDao();
		languageSettings = LanguageSettings.getInstance(getActivity());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//actionBarActivity.getSupportActionBar().setTitle(R.string.lesson);
		 highlightedLesson = new View(getActivity());
		mainView = inflater.inflate(R.layout.lesson_list, container, false);
		tvTeacherLanguage = (TextView) mainView
				.findViewById(R.id.tvTeacherLanguage);
		// tvLanguage = (TextView) mainView.findViewById(R.id.tvLanguage);
		tvClass = (TextView) mainView.findViewById(R.id.tvClass);
		listView = (ListView) mainView.findViewById(R.id.lvListView);
		lessonListAdapter = new LessonListAdapter(lessonList);
		listView.setAdapter(lessonListAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Store the selected lesson values in the languageSettings file and
				// then display the lesson
				// saving highlight
				// http://stackoverflow.com/questions/9281000/android-keep-listviews-item-highlighted-once-one-has-been-clicked
				if (position != selectedLessonPosition) {
//					turnOffSelectedHightlight(highlightedLesson);
//					view.setSelected(true);
//					view.setBackgroundColor(R.drawable.list_pressed_languagelessonstyle1);
					//view.setBackgroundColor(R.drawable.list_focused_languagelessonstyle1);
					view.setBackgroundColor(getResources().getColor(R.color.lesson_selection));
 					highlightedLesson = view;
					view.setSelected(true);
					selectedLessonPosition = position;
					lessonListAdapter.notifyDataSetChanged();
					if (!lessonList.get(position).getId()
							.equals(languageSettings.getLessonId())) {
						languageSettings.setLastLessonPhraseLine(-1);
					}
					languageSettings.setLessonId(lessonList.get(position).getId())
							.setLessonTitle(lessonList.get(position).getLessonTitle())
							.commit();
					callBacks.onSelectedAction(null);
				}
			}
		});

		return mainView;
	}

	// Called by activity when user presses back button after lesson
	// selected
	public void turnOffSelectedHightlight() {
			selectedLessonPosition = -1;
			if (lessonListAdapter != null){
				lessonListAdapter.notifyDataSetChanged();
			}
	
	}

	@Override
	public void onResume() {
		super.onResume();
		getCurrentLessonInfo();
		if (classId == -1) {
			if (teacherLanguagesExist()) {
				// have the user select a teach/class/lesson
				// Here if at least one teacher/language loaded to database
				// displayLessonSelectionDialog();
				Toast.makeText(getActivity(),
						R.string.a_class_must_be_selected_first, Toast.LENGTH_LONG)
						.show();

				return;
			} else {
				goToLanguageMaintenance();
				getActivity().finish();
				return;
			}
		}
		displayTeacherLanguageClass();
		loadLessonList();
		mainView.invalidate();

		lessonListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//highlightedLesson = null;
	}

	private void getCurrentLessonInfo() {
		// teacherId = languageSettings.getTeacherId();
		teacherName = languageSettings.getTeacherName();
		// teacherLanguageId = languageSettings.getTeacherLanguageId();
		classId = languageSettings.getClassId();
		className = languageSettings.getClassTitle();
		// learningLanguageId = languageSettings.getLearningLanguageId();
		learningLanguageName = languageSettings.getLearningLanguageName();
	}

	private void displayTeacherLanguageClass() {
		tvTeacherLanguage.setText(teacherName + "/" + learningLanguageName);
		// tvLanguage.setText(learningLanguageName);
		tvClass.setText(className);
	}

	private void loadLessonList() {
		// eventually update to the current teacher/class (if any)
		lessonList.clear();
		lessonList.addAll(lessonDao.queryBuilder()
				.where(LessonDao.Properties.ClassId.eq(classId)).list());
	}

	// check to make sure at least 1 teacher/language exists
	// if not then you want to go to language maintenance activity
	private boolean teacherLanguagesExist() {
		String sql;
		Cursor cursor;
		int teacherCount = 0;
		sql = "select count(*) from " + TeacherLanguageDao.TABLENAME;
		cursor = daoSession.getDatabase().rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			teacherCount = cursor.getInt(0);
		}
		cursor.close();
		return (teacherCount > 0);
	}

	private void goToLanguageMaintenance() {
		Intent intent = new Intent(getActivity(),
				LanguageMaintenanceActivity.class);
		getActivity().startActivity(intent);
	}

	private class LessonListAdapter extends ArrayAdapter<Lesson> {
		public class LessonViewHolder {
			public View detailView;
			public TextView tvLessonTitle;
			public TextView tvDescription;
		}

		// textViewResourceId should be the detail row view (containing other
		// textview views to be assigned date in getView)
		public LessonListAdapter(ArrayList<Lesson> lessonList) {
			super(getActivity(), android.R.layout.simple_list_item_single_choice,
					lessonList);
		}

		@Override
		// see http://www.vogella.com/tutorials/AndroidListView/article.html for
		// optimization of inflate/findViewById
		// do minimal stuff here, do most in interface callback
		// needs cleanup
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			LessonListAdapter.LessonViewHolder viewHolder;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				viewHolder = new LessonViewHolder();
				rowView = inflater.inflate(R.layout.lesson_list_detail, parent,
						false);
				viewHolder.detailView = rowView;
				viewHolder.tvLessonTitle = (TextView) rowView
						.findViewById(R.id.tvLessonTitle);
				viewHolder.tvDescription = (TextView) rowView
						.findViewById(R.id.tvDescription);
				rowView.setTag(viewHolder);
			}
			Lesson lesson = this.getItem(position);
			viewHolder = (LessonListAdapter.LessonViewHolder) rowView.getTag();
			// viewHolder.tvLessonNumber.setText((position + 1) + "");
			viewHolder.tvLessonTitle.setText(lesson.getLessonTitle());
			viewHolder.tvDescription.setText(lesson.getDescription());
			// viewHolder.tvLessonType.setText(lesson.getLessonType());

			if (selectedLessonPosition != -1 && position == selectedLessonPosition) {
//				viewHolder.detailView.setBackgroundColor(getResources().getColor(
//						R.color.lesson_selection));
				//viewHolder.detailView.setBackgroundColor(R.drawable.list_pressed_languagelessonstyle1);
				//viewHolder.detailView.setBackgroundColor(R.drawable.list_focused_languagelessonstyle1);
				viewHolder.detailView.setBackgroundColor(getResources().getColor(R.color.lesson_selection));
				// on orientation change make sure you save the highlighted row here
				// also
				 highlightedLesson = viewHolder.detailView;
			} else {
				viewHolder.detailView.setBackgroundColor(Color.TRANSPARENT);
			}
			
			return rowView;
		}

	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//		if (resultCode != Activity.RESULT_OK)
//			return;
//		if (requestCode == SELECT_CLASS) {
//			displayLessonList();
//			selectedLessonPosition = -1;
//			return;
//		}
//	}

	public void displayLessonList() {
		getCurrentLessonInfo();
		displayTeacherLanguageClass();
		selectedLessonPosition = -1;
		loadLessonList();
		mainView.invalidate();
		lessonListAdapter.notifyDataSetChanged();
	}

}
