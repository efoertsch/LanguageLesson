package com.fisincorporated.languagetutorial;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class LessonListFragment extends ListFragment {

	private DaoSession daoSession;
	private LessonDao lessonDao;
	//private ClassNameDao classNameDao;
	private ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
	//private QueryBuilder<Lesson> qb;
	//private Long teacherId;
	//private Long teacherLanguageId;
	private String teacherName;
	private Long classId;
	private String className;
	//private Long learningLanguageId;
	private String learningLanguageName;
	// used to store values to SharedPreferences file
	private LanguageSettings languageSettings;
	//private LessonSelectionDialog lessonSelectionDialog;
	private LessonListAdapter lessonListAdapter;
	private TextView tvTeacherLanguage;
	//private TextView tvLanguage;
	private TextView tvClass;
	private View mainView;

	// values for dialog
	private static final int SELECT_CLASS = 1;

	protected IHandleSelectedAction callBacks;
	private ActionBarActivity actionBarActivity;

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
		daoSession = LanguageApplication.getInstance().getDaoSession();
		lessonDao = daoSession.getLessonDao();
		languageSettings = LanguageSettings.getInstance(getActivity());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		actionBarActivity.getSupportActionBar().setTitle(R.string.lesson);
		// lesson_list contains 'listview'
		mainView = inflater.inflate(R.layout.lesson_list, container, false);
		tvTeacherLanguage = (TextView) mainView
				.findViewById(R.id.tvTeacherLanguage);
		// tvLanguage = (TextView) mainView.findViewById(R.id.tvLanguage);
		tvClass = (TextView) mainView.findViewById(R.id.tvClass);
		// ListView listView = (ListView) view.findViewById(android.R.id.list);
		// View header = inflater.inflate(R.layout.lesson_list_header, null);
		// listView.addHeaderView(header);
		lessonListAdapter = new LessonListAdapter(lessonList);
		setListAdapter(lessonListAdapter);
		return mainView;

	}

	public void onListItemClick(ListView l, View v, int position, long id) {
//		Toast.makeText(getActivity(),
//				"Clicked on position:" + position + "  Row id:" + id,
//				Toast.LENGTH_LONG).show();
		// Store the selected lesson values in the languageSettings file and
		// then display the lesson
		if (lessonList.get(position).getId() != languageSettings.getLessonId()){
			languageSettings.setLastLessonPhraseLine(-1);
		}
		languageSettings.setLessonId(lessonList.get(position).getId())
				.setLessonTitle(lessonList.get(position).getLessonTitle()).commit();
		callBacks.onSelectedAction(null);
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

	private void getCurrentLessonInfo() {
		//teacherId = languageSettings.getTeacherId();
		teacherName = languageSettings.getTeacherName();
		//teacherLanguageId = languageSettings.getTeacherLanguageId();
		classId = languageSettings.getClassId();
		className = languageSettings.getClassTitle();
		//learningLanguageId = languageSettings.getLearningLanguageId();
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
	// if not then you want to go to language maintenace activity
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
			public TextView tvLessonTitle;
			public TextView tvDescription;
		}

		// textViewResourceId should be the detail row view (containing other
		// textview views to be assigned date in getView)
		public LessonListAdapter(ArrayList<Lesson> lessonList) {
			super(getActivity(), 0, lessonList);
		}

		@Override
		// see http://www.vogella.com/tutorials/AndroidListView/article.html for
		// optimization of inflate/findViewById
		// do minimal stuff here, do most in interface callback
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LessonListAdapter.LessonViewHolder viewHolder = new LessonViewHolder();
				rowView = inflater.inflate(R.layout.lesson_list_detail, parent,
						false);
				viewHolder.tvLessonTitle = (TextView) rowView
						.findViewById(R.id.tvLessonTitle);
				viewHolder.tvDescription = (TextView) rowView
						.findViewById(R.id.tvDescription);
				rowView.setTag(viewHolder);
			}

			Lesson lesson = this.getItem(position);
			LessonListAdapter.LessonViewHolder viewHolder = (LessonListAdapter.LessonViewHolder) rowView
					.getTag();
			// viewHolder.tvLessonNumber.setText((position + 1) + "");
			viewHolder.tvLessonTitle.setText(lesson.getLessonTitle());
			viewHolder.tvDescription.setText(lesson.getDescription());
			// viewHolder.tvLessonType.setText(lesson.getLessonType());
			return rowView;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == SELECT_CLASS) {
			displayLessonList();
			return;
		}
	}

	public void displayLessonList() {
		getCurrentLessonInfo();
		displayTeacherLanguageClass();
		loadLessonList();
		mainView.invalidate();
		lessonListAdapter.notifyDataSetChanged();
	}

}
