package com.fisincorporated.languagetutorial;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
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

import com.fisincorporated.languagetutorial.db.ClassName;
import com.fisincorporated.languagetutorial.db.ClassNameDao;
import com.fisincorporated.languagetutorial.db.DaoMaster;
import com.fisincorporated.languagetutorial.db.DaoSession;
import com.fisincorporated.languagetutorial.db.LanguageCode;
import com.fisincorporated.languagetutorial.db.LanguageCodeDao;
import com.fisincorporated.languagetutorial.db.Lesson;
import com.fisincorporated.languagetutorial.db.LessonDao;
import com.fisincorporated.languagetutorial.db.Teacher;
import com.fisincorporated.languagetutorial.db.TeacherDao;
import com.fisincorporated.languagetutorial.db.TeacherLanguage;
import com.fisincorporated.languagetutorial.db.TeacherLanguageDao;
import com.fisincorporated.languagetutorial.interfaces.IDialogResultListener;
import com.fisincorporated.languagetutorial.utility.DomainObject;
import com.fisincorporated.languagetutorial.utility.LanguageSettings;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

// logic on how to handle initial unwanted onItemSelected firings 
// http://stackoverflow.com/questions/21747917/undesired-onitemselected-calls/21751327#_=_
public class LessonSelectionDialog extends DialogFragment {
	// used to determine the extent of what can be selected
	private static final String DIALOG_FUNCTION = "DialogFunction";
	public static final String CLASS_LESSON_SELECT = "ClassLessonSelect";
	public static final String TEACHER_LANGUAGE_DELETE = "TeacherLanguageDelete";
	private static final String LESSON_LEVEL = "lessonLevel";
	public static final int SELECT_TO_LESSON = 1;
	public static final int SELECT_TO_CLASS = 2;
	// private static LessonSelectionDialog lessonSelectionDialog = null;

	protected SQLiteDatabase database = null;

	protected DaoMaster daoMaster;
	protected DaoSession daoSession;

	protected Teacher teacher;
	protected TeacherDao teacherDao;
	private Spinner spnrTeacher;
	private ArrayAdapter<Teacher> teacherArrayAdapter;
	private static ArrayList<Teacher> teacherList = new ArrayList<Teacher>();
	private TextView lblTeacher;
	private static Long teacherId;
	// private static String teacherName = "";
	private Teacher selectedTeacher;

	protected TeacherLanguage teacherLanguage;
	protected TeacherLanguageDao teacherLanguageDao;
	private Spinner spnrTeacherLanguage;
	private ArrayAdapter<TeacherFromToLanguage> teacherLanguageArrayAdapter;
	private static ArrayList<TeacherFromToLanguage> teacherFromToLanguageList = new ArrayList<TeacherFromToLanguage>();
	private TextView lblTeacherLanguage;
	private static Long teacherLanguageId;
	// teacherLanguageTitle is concatenated language names (e.g. English/Turkish)
	// private static String teacherLanguageTitle = "";
	// private TeacherFromToLanguage teacherFromToLanguage;
	private TeacherFromToLanguage selectedTeacherFromToLanguage;

	protected ClassName className;
	protected ClassNameDao classNameDao;
	private Spinner spnrClassName;
	private ArrayAdapter<ClassName> classNameArrayAdapter;
	private static ArrayList<ClassName> classNameList = new ArrayList<ClassName>();
	private TextView lblClass;
	private static Long classId;
	// private static String classTitle = "";
	private ClassName selectedClassName;

	protected Lesson lesson;
	protected LessonDao lessonDao;
	private Spinner spnrLesson;
	private ArrayAdapter<Lesson> lessonArrayAdapter;
	private static ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
	private TextView lblLesson;
	private static Long lessonId;
	// private static String lessonTitle = "";
	private Lesson selectedLesson;

	protected LanguageCode languageCode;
	protected LanguageCodeDao languageCodeDao;

	private QueryBuilder<Teacher> qbTeacher;
	private Query<ClassName> classNameQuery = null;
	private Query<Lesson> lessonQuery;

	private int teacherPosition = -1;
	private int teacherLanguagePosition = -1;
	private int classNamePosition = -1;
	private int lessonPosition = -1;

	// used to store values to SharedPreferences file
	private static LanguageSettings languageSettings;
	private static Resources res;

	private IDialogResultListener iDialogResultListener = null;
	private String dialogFunction = "";
	private int lessonLevel = -1;
	private int requestCode = -1;
	private int dialogTitle = -1;
	private int positiveButton = -1;
	private int negativeButton = -1;

	public final static String LESSON_DIALOG_RESPONSE = "com.fisincorporated.languagetutorial.lesson.dialog.response";

	public void setOnDialogResultListener(IDialogResultListener listener,
			int requestCode) {
		this.iDialogResultListener = listener;
		this.requestCode = requestCode;
	}

	// Select either up to a class or a lesson
	public static LessonSelectionDialog newInstance(String dialogFunction,
			int lessonLevel) {
		LessonSelectionDialog f = new LessonSelectionDialog();
		Bundle args = new Bundle();
		args.putString(DIALOG_FUNCTION, dialogFunction);
		args.putInt(LESSON_LEVEL, lessonLevel);
		f.setArguments(args);
		return f;
	}

	public LessonSelectionDialog() {

	}

	// use by implementing class
	protected void getDatabaseSetup() {
		daoSession = LanguageApplication.getInstance().getDaoSession();
		teacherDao = daoSession.getTeacherDao();
		teacherLanguageDao = daoSession.getTeacherLanguageDao();
		languageCodeDao = daoSession.getLanguageCodeDao();
		classNameDao = daoSession.getClassNameDao();
		lessonDao = daoSession.getLessonDao();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		languageSettings = LanguageSettings.getInstance(getActivity());
		getDatabaseSetup();
		getCurrentLessonSettings();
		setRetainInstance(true);

	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		res = getResources();
		getDialogDetails(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(res.getString(dialogTitle));
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.class_selection, null);
		builder.setView(v);

		lblTeacher = (TextView) v.findViewById(R.id.lblTeacher);
		spnrTeacher = (Spinner) v.findViewById(R.id.spnrTeacher);
		teacherArrayAdapter = new ArrayAdapter<Teacher>(getActivity(),
				android.R.layout.simple_spinner_item, teacherList);
		teacherArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrTeacher.setAdapter(teacherArrayAdapter);
		spnrTeacher.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if((Integer)spnrTeacher.getTag(R.id.spnrTeacherPos) == position){
					return;
				}
				spnrTeacher.setTag((R.id.spnrTeacherPos), (Integer) position);
				selectedTeacher = ((Teacher) parent.getItemAtPosition(position));
				teacherId = selectedTeacher.getId();
				teacherLanguageId = -1l;
				classId = -1l;
				lessonId = -1l;
				setUpTeacherLanguageDropDown();
				// reloadTeacherLanguageList();
				if (teacherFromToLanguageList.size() == 0) {
					Toast.makeText(
							getActivity(),
							res.getString(R.string.no_languages_defined_for_this_teacher),
							Toast.LENGTH_LONG).show();
					setTeacherLanguageClassAndLessonInvisible();
				}  
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		lblTeacherLanguage = (TextView) v.findViewById(R.id.lblTeacherLanguage);
		spnrTeacherLanguage = (Spinner) v.findViewById(R.id.spnrTeacherLanguage);
		teacherLanguageArrayAdapter = new ArrayAdapter<TeacherFromToLanguage>(
				getActivity(), android.R.layout.simple_spinner_item,
				teacherFromToLanguageList);
		teacherLanguageArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrTeacherLanguage.setAdapter(teacherLanguageArrayAdapter);
		spnrTeacherLanguage
		.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if((Integer)spnrTeacherLanguage.getTag(R.id.spnrTeacherLanguagePos) == position){
					return;
				}
				spnrTeacherLanguage.setTag((R.id.spnrTeacherLanguagePos), (Integer) position);
				selectedTeacherFromToLanguage = ((TeacherFromToLanguage) parent
						.getItemAtPosition(position));
				teacherLanguageId = selectedTeacherFromToLanguage.getId();
				// teacherLanguageTitle = selectedTeacherFromToLanguage
				// .getTeacherLanguageTitle();
				classId = -1l;
				// classTitle = "";
				lessonId = -1l;
				// lessonTitle = "";
				if (!dialogFunction
						.equals(LessonSelectionDialog.CLASS_LESSON_SELECT)) {
					return;
				}
				// should only be here if selecting a class/lesson
				setupClassDropdown();
				if (classNameList.size() == 0) {
					Toast.makeText(
							getActivity(),
							res.getString(R.string.no_classes_defined_for_this_teacher),
							Toast.LENGTH_LONG).show();
					setClassAndLessonInvisible();
				} else {
					setClassVisibleLessonInvisible();
					// spnrClassName.performClick();
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		

		lblClass = (TextView) v.findViewById(R.id.lblClass);
		spnrClassName = (Spinner) v.findViewById(R.id.spnrClass);
		classNameArrayAdapter = new ArrayAdapter<ClassName>(getActivity(),
				android.R.layout.simple_spinner_item, classNameList);
		classNameArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrClassName.setAdapter(classNameArrayAdapter);
		spnrClassName.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if((Integer)spnrClassName.getTag(R.id.spnrClassNamePos) == position){
					return;
				}
				spnrClassName.setTag((R.id.spnrClassNamePos), (Integer) position);
				selectedClassName = ((ClassName) parent.getItemAtPosition(position));
				classId = selectedClassName.getId();
				lessonId = -1l;
				if (lessonLevel != SELECT_TO_LESSON) {
					return;
				}
				setupLessonDropdown();
				if (lessonList.size() == 0) {
					setClassVisibleLessonInvisible();
					Toast.makeText(getActivity(),
							res.getString(R.string.no_lessons_defined_for_this_class),
							Toast.LENGTH_LONG).show();
				}
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		lblLesson = (TextView) v.findViewById(R.id.lblLesson);
		spnrLesson = (Spinner) v.findViewById(R.id.spnrLesson);
		lessonArrayAdapter = new ArrayAdapter<Lesson>(getActivity(),
				android.R.layout.simple_spinner_item, lessonList);
		lessonArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrLesson.setAdapter(lessonArrayAdapter);
		spnrLesson.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if((Integer)spnrLesson.getTag(R.id.spnrLessonPos) == position){
					return;
				}
				spnrLesson.setTag((R.id.spnrLesson), (Integer) position);
				selectedLesson = (Lesson) parent.getItemAtPosition(position);
				lessonId = selectedLesson.getId();
				// lessonTitle = selectedLesson.getLessonTitle();
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// The OK button saves whatever selected
		builder.setPositiveButton(positiveButton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						sendResult(Activity.RESULT_OK, id, saveSelections());
					}

				});
		// Negative button is Cancel so return without saving anything
		builder.setNegativeButton(negativeButton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						sendResult(Activity.RESULT_CANCELED, id, null);
					}
				});

		setUpTeacherDropdown();
		if (dialogFunction.equals(LessonSelectionDialog.CLASS_LESSON_SELECT)) {
			// already being done in setUpTeacherLanguageDropDown so don't repeat
			// here
			// setupClassDropdown();
			// if (lessonLevel == SELECT_TO_LESSON) {
			// setupLessonDropdown();
			// }
		} else {
			classId = -1l;
			lessonId = -1l;
		}
		//v.invalidate();
		return builder.create();
	}
	
	 

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(LESSON_LEVEL, lessonLevel);
		super.onSaveInstanceState(savedInstanceState);

	}

	public void getDialogDetails(Bundle savedInstanceState) {
		Bundle bundle = null;
		if (getArguments() != null)
			bundle = getArguments();
		else if (savedInstanceState != null) {
			bundle = savedInstanceState;
		}
		if (bundle != null) {
			dialogFunction = bundle
					.getString(LessonSelectionDialog.DIALOG_FUNCTION);
			lessonLevel = bundle.getInt(LESSON_LEVEL);

		} else {
			dialogFunction = "";
			lessonLevel = SELECT_TO_LESSON;
		}
		if (dialogFunction.equals(LessonSelectionDialog.TEACHER_LANGUAGE_DELETE)) {
			dialogTitle = R.string.select_to_delete;
			positiveButton = R.string.delete;
			negativeButton = R.string.cancel;
		} else if (dialogFunction
				.equals(LessonSelectionDialog.CLASS_LESSON_SELECT)) {
			dialogTitle = R.string.select;
			positiveButton = R.string.save;
			negativeButton = R.string.cancel;
		} else {
			dialogTitle = -1;
		}

	}

	private void getCurrentLessonSettings() {
		teacherId = languageSettings.getTeacherId();
		teacherLanguageId = languageSettings.getTeacherLanguageId();
		classId = languageSettings.getClassId();
		lessonId = languageSettings.getLessonId();
		classNameList = new ArrayList<ClassName>();
		lessonList = new ArrayList<Lesson>();
	}

	private void setUpTeacherDropdown() {
		// Start of teacher dropdown stuff
		fillTeacherList();
		// if no teachers - big trouble
		if (teacherList.size() == 0) {
			Toast.makeText(getActivity(),
					res.getString(R.string.no_teachers_defined), Toast.LENGTH_LONG)
					.show();
			setTeachersInvisible();
			setClassAndLessonInvisible();
			return;
		}
		// see if teacher was saved at some point
		if (teacherId != -1) {
			teacherPosition = getPosition(teacherList, teacherId);
		}
		// is teacher still in list? If not set to first teacher in list
		if (teacherPosition == -1) {
			teacherPosition = 0;
			teacherLanguageId = -1l;
			classId = -1l;
			lessonId = -1l;
		}
		// teacher valid or if invalid now pointing to first teacher in list
		selectedTeacher = teacherList.get(teacherPosition);
		spnrTeacher.setSelection(teacherPosition);
		spnrTeacher.setTag(R.id.spnrTeacherPos, (Integer) teacherPosition);
		teacherId = selectedTeacher.getId();
		setUpTeacherLanguageDropDown();

	}

	private void setUpTeacherLanguageDropDown() {
		// Start of language list dropdown stuff
		// if valid teacher then get the languages they teach
		teacherLanguagePosition = -1;
		if (teacherId != -1) {
			reloadTeacherLanguageList();
			if (teacherFromToLanguageList.size() == 0) {
				teacherLanguageId = -1l;
				Toast.makeText(getActivity(),
						res.getString(R.string.teacher_language_no_longer_defined),
						Toast.LENGTH_LONG).show();
				setTeacherLanguageClassAndLessonInvisible();
				return;
			}
		}
		// here if at least one teacher/language record loaded
		// so find out position where teacherLanguageId is in the list
		if (teacherLanguageId != -1) {
			teacherLanguagePosition = getPosition(teacherFromToLanguageList,
					teacherLanguageId);
		}
		// the teacherLanguageId may no longer be valid and if so set dropdown to
		// first in list
		if (teacherLanguagePosition == -1) {
			teacherLanguagePosition = 0;
			classId = -1l;
			lessonId = -1l;
		}
		setTeacherLanguageVisibleClassAndLessonInvisible();
		spnrTeacherLanguage.setSelection(teacherLanguagePosition);
		spnrTeacherLanguage.setTag(R.id.spnrTeacherLanguagePos, (Integer)teacherLanguagePosition);
		selectedTeacherFromToLanguage = teacherFromToLanguageList
				.get(teacherLanguagePosition);
		teacherLanguageId = selectedTeacherFromToLanguage.getId();
		if (dialogFunction.equals(LessonSelectionDialog.CLASS_LESSON_SELECT)) {
			setupClassDropdown();
		}

	}

	private void setupClassDropdown() {
		// Start of class dropdown stuff
		// if valid teacher/language then get their classes
		classNamePosition = -1;
		if (teacherLanguageId != -1) {
			reloadClassNameList();
			if (classNameList.size() == 0) {
				Toast.makeText(getActivity(),
						res.getString(R.string.class_no_longer_defined),
						Toast.LENGTH_LONG).show();
				setClassAndLessonInvisible();
			}
		}
		if (classId != -1) {
			classNamePosition = getPosition(classNameList, classId);
		}
		if (classNamePosition == -1) {
			classNamePosition = 0;
			lessonId = -1l;
		}
		spnrClassName.setSelection(classNamePosition);
		spnrClassName.setTag(R.id.spnrClassNamePos, (Integer)classNamePosition);
		selectedClassName = classNameList.get(classNamePosition);
		classId = selectedClassName.getId();
		setClassVisibleLessonInvisible();
		if (lessonLevel == SELECT_TO_LESSON) {
			setupLessonDropdown();
		}
		
	}

	private void setupLessonDropdown() {
		// start of lesson stuff
		lessonPosition = -1;
		if (classId != -1) {
			// get all lessons for the class
			reloadLessonList();
			if (lessonList.size() == 0) {
				Toast.makeText(getActivity(),
						res.getString(R.string.lesson_no_longer_defined),
						Toast.LENGTH_LONG).show();
				setLessonVisible(View.INVISIBLE);
				classId = -1l;
				return;
			}
		}
		// if here at least have one lesson
		if (lessonId != -1) {
			lessonPosition = getPosition(lessonList, lessonId);
		}
		if (lessonPosition == -1) {
			lessonPosition = 0;
		}
		spnrLesson.setSelection(lessonPosition);
		spnrLesson.setTag(R.id.spnrLessonPos,(Integer)lessonPosition);
		selectedLesson = lessonList.get(lessonPosition);
		lessonId = selectedLesson.getId();
		setLessonVisible(View.VISIBLE);

	}

	private void setTeachersInvisible() {
		lblTeacher.setVisibility(View.INVISIBLE);
		spnrTeacher.setVisibility(View.INVISIBLE);
	}

	private void setTeacherLanguageVisible() {
		lblTeacherLanguage.setVisibility(View.VISIBLE);
		spnrTeacherLanguage.setVisibility(View.VISIBLE);
	}

	private void setTeacherLanguageClassAndLessonInvisible() {
		lblTeacherLanguage.setVisibility(View.INVISIBLE);
		spnrTeacherLanguage.setVisibility(View.INVISIBLE);
		setClassAndLessonInvisible();
	}

	private void setTeacherLanguageVisibleClassAndLessonInvisible() {
		setTeacherLanguageVisible();
		setClassAndLessonInvisible();
	}

	private void setClassAndLessonInvisible() {
		lblClass.setVisibility(View.INVISIBLE);
		spnrClassName.setVisibility(View.INVISIBLE);
		lblLesson.setVisibility(View.INVISIBLE);
		spnrLesson.setVisibility(View.INVISIBLE);
	}

	private void setClassVisibleLessonInvisible() {
		lblClass.setVisibility(View.VISIBLE);
		spnrClassName.setVisibility(View.VISIBLE);
		lblLesson.setVisibility(View.INVISIBLE);
		spnrLesson.setVisibility(View.INVISIBLE);
	}

	private void setLessonVisible(int visible) {
		lblLesson.setVisibility(visible);
		spnrLesson.setVisibility(visible);
	}

	@Override
	public void onResume() {
		super.onResume();
		// spnrTeacher.setSelection(0);
	}

	// following methods are duplicated in DeleteTeacherLanguageDialog.
	// At some point move common logic to respective Dao's
	private void fillTeacherList() {
		qbTeacher = teacherDao.queryBuilder().orderAsc(TeacherDao.Properties.TeacherName);
		teacherList.clear();
		teacherList.addAll((ArrayList<Teacher>) qbTeacher.list());
		teacherArrayAdapter.notifyDataSetChanged();
	}

	private int getPosition(ArrayList<? extends DomainObject> list, Long id) {
		for (int i = 0; i < list.size(); ++i) {
			if (id.equals(list.get(i).getId())) {
				return i;
			}
		}
		return -1;
	}

	// Teacher language methods
	private void reloadTeacherLanguageList() {
		fillTeacherFromToLanguageList();
		teacherLanguageArrayAdapter.notifyDataSetChanged();
	}

	private void fillTeacherFromToLanguageList() {
		TeacherLanguage teacherLanguage;
		TeacherFromToLanguage tftl;
		List<TeacherLanguage> teacherLanguageList = teacherLanguageDao.queryDeep(
				" where T." + TeacherLanguageDao.Properties.TeacherId.columnName
						+ "= ?", teacherId + "");
		teacherFromToLanguageList.clear();
		for (int i = 0; i < teacherLanguageList.size(); ++i) {
			teacherLanguage = teacherLanguageList.get(i);
			if (teacherLanguage.getLearningLanguageCheck() != null
					&& teacherLanguage.getKnownLanguageCheck() != null) {
				tftl = new TeacherFromToLanguage(teacherLanguage.getId(),
						teacherLanguage.getKnownLanguageCheck().getLanguageName()
								.trim()
								+ "/"
								+ teacherLanguage.getLearningLanguageCheck()
										.getLanguageName().trim());
				tftl.setKnownLanguageId(teacherLanguage.getKnownLanguageId());
				tftl.setKnownLanguageName(teacherLanguage.getKnownLanguageCheck()
						.getLanguageName());
				tftl.setLearningLanguageId(teacherLanguage.getLearningLanguageId());
				tftl.setLearningLanguageName(teacherLanguage
						.getLearningLanguageCheck().getLanguageName());
				teacherFromToLanguageList.add(tftl);
			}
		}
		// ArrayAdapter.addAll only from V11 on
		// teacherLanguageArrayAdapter.clear();
		// teacherLanguageArrayAdapter.addAll(teacherFromToLanguageList);
	}

	// Class Name methods
	@SuppressLint("NewApi")
	private void reloadClassNameList() {
		fillClassList();
		classNameArrayAdapter.notifyDataSetChanged();
	}

	private void fillClassList() {
		if (classNameQuery == null) {
			classNameQuery = classNameDao
					.queryBuilder()
					.where(
							ClassNameDao.Properties.TeacherId.eq(teacherId),
							ClassNameDao.Properties.TeacherLanguageId
									.eq(teacherLanguageId))
					.orderAsc(ClassNameDao.Properties.ClassOrder).build();
		} else {
			classNameQuery.setParameter(0, teacherId);
			classNameQuery.setParameter(1, teacherLanguageId);
		}
		classNameList.clear();
		classNameList.addAll(classNameQuery.list());
		// ArrayAdapter.addAll only from V11 on
		// classNameArrayAdapter.clear();
		// classNameArrayAdapter.addAll(classNameQuery.list());
	}

	@SuppressLint("NewApi")
	private void reloadLessonList() {
		fillLessonList();
		lessonArrayAdapter.notifyDataSetChanged();
	}

	private void fillLessonList() {
		if (lessonQuery == null) {
			lessonQuery = lessonDao.queryBuilder()
					.where(LessonDao.Properties.ClassId.eq(classId))
					.orderAsc(LessonDao.Properties.LessonOrder).build();
		} else {
			lessonQuery.setParameter(0, classId);
		}
		lessonList.clear();
		lessonList.addAll(lessonQuery.list());

	}

	private void sendResult(int resultCode, int button_pressed, Bundle bundle) {
		if (iDialogResultListener != null) {
			iDialogResultListener.onDialogResult(requestCode, resultCode,
					button_pressed, bundle);
			iDialogResultListener = null;
			dismiss();
		}
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,
				intent);
	}

	private Bundle saveSelections() {
		Bundle bundle;
		if (dialogFunction.equals(LessonSelectionDialog.TEACHER_LANGUAGE_DELETE)) {
			TeacherFromToLanguage teacherFromToLanguage = new TeacherFromToLanguage(
					selectedTeacherFromToLanguage.getId(),
					selectedTeacherFromToLanguage.getKnownLanguageName() + " to "
							+ selectedTeacherFromToLanguage.getLearningLanguageName(),
					selectedTeacherFromToLanguage.getLearningLanguageId(),
					selectedTeacherFromToLanguage.getLearningLanguageName(),
					selectedTeacherFromToLanguage.getKnownLanguageId(),
					selectedTeacherFromToLanguage.getKnownLanguageName(),
					selectedTeacher.getId(), selectedTeacher.getTeacherName());
			bundle = new Bundle();
			bundle.putParcelable(GlobalValues.TEACHER_FROM_TO_LANGUAGE,
					teacherFromToLanguage);
		} else {
			saveLanguagePreferences();
			bundle = null;
		}
		return bundle;

	}

	// save results in preferences,
	private void saveLanguagePreferences() {
		languageSettings
				.setTeacherId(selectedTeacher.getId())
				.setTeacherName(selectedTeacher.getTeacherName())
				.setTeacherLanguageId(selectedTeacherFromToLanguage.getId())
				.setLearningLanguageId(
						selectedTeacherFromToLanguage.getLearningLanguageId())
				.setLearningLanguageName(
						selectedTeacherFromToLanguage.getLearningLanguageName())
				.setKnownLanguageId(
						selectedTeacherFromToLanguage.getKnownLanguageId())
				.setKnownLanguageName(
						selectedTeacherFromToLanguage.getKnownLanguageName())
				.setClassId(classId == -1 ? -1 : selectedClassName.getId())
				.setClassTitle(
						classId == -1 ? "" : selectedClassName.getClassTitle())
				.setLessonId(
						lessonId == -1 ? -1
								: (lessonLevel == SELECT_TO_LESSON) ? selectedLesson
										.getId() : -1)
				.setLessonType(
						lessonId == -1 ? ""
								: (lessonLevel == SELECT_TO_LESSON) ? selectedLesson
										.getLessonType() : "")
				.setLessonTitle(
						lessonId == -1 ? ""
								: (lessonLevel == SELECT_TO_LESSON) ? selectedLesson
										.getLessonTitle() : "")
				.setLastLessonPhraseLine(-1).commit();

	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			// if crash occurs try getDialog().setDismissMessage(null);
			getDialog().setOnDismissListener(null);
		}
		super.onDestroyView();
	}

}
