package com.fisincorporated.languagetutorial;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.languagetutorial.db.DaoMaster;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.LanguageCode;
import com.fisincorporated.languagetutorial.db.LanguageCodeDao;
import com.fisincorporated.languagetutorial.db.Teacher;
import com.fisincorporated.languagetutorial.db.TeacherDao;
import com.fisincorporated.languagetutorial.db.TeacherLanguage;
import com.fisincorporated.languagetutorial.db.TeacherLanguageDao;
import com.fisincorporated.languagetutorial.interfaces.IDialogResultListener;
import com.fisincorporated.languagetutorial.utility.DomainObject;

import de.greenrobot.dao.query.QueryBuilder;

public class TeacherLanguageSelectDialog extends DialogFragment {

	protected SQLiteDatabase database = null;

	protected DaoMaster daoMaster;
	protected DaoSession daoSession;

	protected Teacher teacher;
	protected TeacherDao teacherDao;
	private Spinner spnrTeacher;
	private ArrayAdapter<Teacher> teacherArrayAdapter;
	private static ArrayList<Teacher> teacherList;
	private TextView lblTeacher;
	private static Long teacherId;
	private static String teacherName = "";

	protected TeacherLanguage teacherLanguage;
	protected TeacherLanguageDao teacherLanguageDao;
	private Spinner spnrTeacherLanguage;
	private ArrayAdapter<TeacherFromToLanguage> teacherLanguageArrayAdapter;
	private static ArrayList<TeacherFromToLanguage> teacherFromToLanguageList = new ArrayList<TeacherFromToLanguage>();
	private TextView lblTeacherLanguage;
	private static Long teacherLanguageId;
	

	protected LanguageCode languageCode;
	protected LanguageCodeDao languageCodeDao;

	private QueryBuilder<Teacher> qbTeacher;

	private int teacherPosition = -1;
	private int teacherLanguagePosition = -1;

	// spinners onItemSelected fire when spinners first initialized
	// these counters are used so first fire event ignored
	private int spinnerTeacherCount = 0;
	private int spinnerTeacherLanguageCount = 0;

	// used to store values to SharedPreferences file
	private static Resources res;
	
	// teacherLanguageTitle is concatenated language names (e.g. English/Turkish)
	//private static String teacherLanguageTitle = "";
	private TeacherFromToLanguage teacherFromToLanguage = null;

	private IDialogResultListener iDialogResultListener = null;
	private int requestCode = -1;
	private AlertDialog thisDialog;

	public void setOnDialogResultListener(IDialogResultListener listener,
			int requestCode) {
		this.iDialogResultListener = listener;
		this.requestCode = requestCode;
	}

	public static TeacherLanguageSelectDialog newInstance(Long inTeacherId,
			Long inTeacherLanguageId) {
		TeacherLanguageSelectDialog f = new TeacherLanguageSelectDialog();
		teacherId = inTeacherId;
		teacherLanguageId = inTeacherLanguageId;
		return f;
	}

	// use by implementing class
	protected void getDatabaseSetup() {
		daoSession = LanguageApplication.getInstance().getDaoSession();
		teacherDao = daoSession.getTeacherDao();
		teacherLanguageDao = daoSession.getTeacherLanguageDao();
		languageCodeDao = daoSession.getLanguageCodeDao();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDatabaseSetup();

	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		res = getResources();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(res.getString(R.string.select_to_delete));
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.teacher_language_select, null);
		builder.setView(v);

		spnrTeacher = (Spinner) v.findViewById(R.id.spnrTeacher);
		// lblTeacher = (TextView) v.findViewById(R.id.lblTeacher);

		spnrTeacherLanguage = (Spinner) v.findViewById(R.id.spnrTeacherLanguage);
		lblTeacherLanguage = (TextView) v.findViewById(R.id.lblTeacherLanguage);

		// The OK button saves whatever selected
		builder.setPositiveButton(R.string.delete,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// send results back to calling routine
						Bundle bundle = new Bundle();
						bundle.putParcelable(GlobalValues.TEACHER_FROM_TO_LANGUAGE, teacherFromToLanguage); 
						sendResult(Activity.RESULT_OK, id, bundle);
					}
				});
		// Negative button is Cancel so return without saving anything
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						sendResult(Activity.RESULT_CANCELED, id, null);
					}
				});

		// Start of teacher dropdown stuff
		fillTeacherList();
		teacherArrayAdapter = new ArrayAdapter<Teacher>(getActivity(),
				android.R.layout.simple_spinner_item, teacherList);
		teacherArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrTeacher.setAdapter(teacherArrayAdapter);
		spnrTeacher.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				++spinnerTeacherCount;
				if (spinnerTeacherCount <= 1)
					return;
				Teacher selectedTeacher = ((Teacher) parent
						.getItemAtPosition(position));
				teacherId = selectedTeacher.getId();
				teacherName = selectedTeacher.getTeacherName();
				teacherLanguageId = -1l;
				//teacherLanguageTitle = "";
				reloadTeacherLanguageList();
				if (teacherFromToLanguageList.size() == 0) {
					setTeacherLanguageVisible(false);
					
					Toast.makeText(
							getActivity(),
							res.getString(R.string.no_languages_defined_for_this_teacher),
							Toast.LENGTH_LONG).show();
				}

			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// if only one teacher use it
		if (teacherList.size() == 1) {
			teacherId = teacherList.get(0).getId();
		}
		// if no teachers - big trouble
		if (teacherList.size() == 0) {
			Toast.makeText(getActivity(),
					res.getString(R.string.no_teachers_defined), Toast.LENGTH_LONG)
					.show();
			setTeachersInvisible();

		}
		// if valid teacherId then set spinner
		if (teacherId != -1) {
			teacherPosition = getPosition(teacherList, teacherId);
			teacherName = teacherList.get(teacherPosition).getTeacherName();
			spnrTeacher.setSelection(teacherPosition);
			spnrTeacher.setSelected(true);
			fillTeacherFromToLanguageList();
		}

		// Start of language list dropdown stuff
		// if valid teacher then get the languages they teach
		if (teacherLanguageId != -1) {
			fillTeacherFromToLanguageList();
		}
		teacherLanguageArrayAdapter = new ArrayAdapter<TeacherFromToLanguage>(
				getActivity(), android.R.layout.simple_spinner_item,
				teacherFromToLanguageList);
		teacherLanguageArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrTeacherLanguage.setAdapter(teacherLanguageArrayAdapter);
		if (teacherFromToLanguageList.size() == 1) {
			spnrTeacherLanguage.setSelection(0);
		}
		spnrTeacherLanguage
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view,
							int position, long id) {
						++spinnerTeacherLanguageCount;
						if (spinnerTeacherLanguageCount <= 1)
							return;
						teacherFromToLanguage = ((TeacherFromToLanguage) parent
								.getItemAtPosition(position));
						teacherLanguageId = teacherFromToLanguage.getId();
						//teacherLanguageTitle = teacherFromToLanguage
						//		.getTeacherLanguageTitle();
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

		// if only one teacher/language or teacher/language not specified pick the
		// first(or only) one
		// in the list
		if (teacherFromToLanguageList.size() == 1
				|| (teacherLanguageId == -1 && teacherFromToLanguageList.size() > 0)) {
			teacherLanguageId = teacherFromToLanguageList.get(0).getId();
			teacherFromToLanguage = teacherFromToLanguageList.get(0);
		}
		if (teacherLanguageId != -1) {
			teacherLanguagePosition = getPosition(
					teacherFromToLanguageList, teacherLanguageId);
			if (teacherLanguagePosition > -1) {
				spnrTeacherLanguage.setSelection(teacherLanguagePosition);
				//teacherLanguageTitle = teacherFromToLanguageList.get(
				//		teacherLanguagePosition).getTeacherLanguageTitle();
				teacherFromToLanguage = teacherFromToLanguageList.get(teacherLanguagePosition);
				spnrTeacherLanguage.setSelected(true);
				setTeacherLanguageVisible(true);
			} else {
				Toast.makeText(getActivity(),
						res.getString(R.string.teacher_language_no_longer_defined),
						Toast.LENGTH_LONG).show();
				setTeacherLanguageVisible(false);
			}
		}

		thisDialog = builder.create();
		return thisDialog;
		  
	}

	private void setTeachersInvisible() {
		lblTeacher.setVisibility(View.INVISIBLE);
		spnrTeacher.setVisibility(View.INVISIBLE);
	}

	private void setTeacherLanguageVisible(boolean visible) {
		lblTeacherLanguage.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		spnrTeacherLanguage
				.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		spnrTeacher.setSelection(0);
	}

	// following methods are duplicated in LessonSelectionDialog.
	// At some point move common logic to respective Dao's
	private void fillTeacherList() {
		qbTeacher = teacherDao.queryBuilder();
		teacherList = (ArrayList<Teacher>) qbTeacher.list();
	}
	
	private int getPosition(
			ArrayList<? extends DomainObject> list, Long id) {
		for (int i = 0; i < list.size(); ++i) {
			if (id == list.get(i).getId()) {
				return i;
			}
		}
		return -1;
	}

//	private int getTeacherPosition(ArrayList<Teacher> list, Long id) {
//		for (int i = 0; i < list.size(); ++i) {
//			if (id == list.get(i).getId()) {
//				return i;
//			}
//		}
//		return -1;
//	}

	// Teacher language methods
	private void reloadTeacherLanguageList() {
		teacherLanguageArrayAdapter.clear();
		fillTeacherFromToLanguageList();
		if (teacherFromToLanguageList.size() > 0){
			spnrTeacherLanguage.setSelection(0);
			thisDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
		}
		else {
			thisDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
		}
		teacherLanguageArrayAdapter.notifyDataSetChanged();
	}

	private void fillTeacherFromToLanguageList() {
		TeacherLanguage teacherLanguage;
		List<TeacherLanguage> teacherLanguageList = teacherLanguageDao.queryDeep(
				" where T." + TeacherLanguageDao.Properties.TeacherId.columnName
						+ "= ?", teacherId + "");
		teacherFromToLanguageList.clear();
		for (int i = 0; i < teacherLanguageList.size(); ++i) {
			teacherLanguage = teacherLanguageList.get(i);
			if (teacherLanguage.getLearningLanguageCheck() != null
					&& teacherLanguage.getKnownLanguageCheck() != null) {
				teacherFromToLanguageList.add(new TeacherFromToLanguage(
						teacherLanguage.getId()
						, teacherLanguage.getKnownLanguageCheck().getLanguageName().trim()
								+ " to "
								+ teacherLanguage.getLearningLanguageCheck()
										.getLanguageName().trim()  
					,teacherLanguage.getLearningLanguageId()
					, teacherLanguage.getLearningLanguageCheck().getLanguageName().trim()
					, teacherLanguage.getKnownLanguageId()
					, teacherLanguage.getKnownLanguageCheck().getLanguageName().trim(), teacherId, teacherName));
			}
		}
	}


//	private int getTeacherFromToLanguagePosition(
//			ArrayList<TeacherFromToLanguage> list, Long id) {
//		for (int i = 0; i < list.size(); ++i) {
//			if (id == list.get(i).getTeacherLanguageId()) {
//				return i;
//			}
//		}
//		return -1;
//	}

	private void sendResult(int resultCode, int button_pressed, Bundle bundle) {
		// first see if activity (or some other object that implemented interface)
		if (iDialogResultListener != null) {
			iDialogResultListener.onDialogResult(requestCode, resultCode,
					button_pressed, bundle);
			iDialogResultListener = null;
			dismiss();
			return;
		}
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(GlobalValues.BUNDLE, bundle);
		intent.putExtra(LanguageDialogFragment.DIALOG_RESPONSE, button_pressed);
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,
				intent);
	}

}
