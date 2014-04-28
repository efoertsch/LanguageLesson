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

public class LessonSelectionDialog extends DialogFragment {
	// used to determine the extent of what can be selected
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
	private static ArrayList<Teacher> teacherList;
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

	// spinners onItemSelected fire when spinners first initialized
	// these counters are used so first fire event ignored
	private int spinnerTeacherCount = 0;
	private int spinnerTeacherLanguageCount = 0;
	private int spinnerClassNameCount = 0;
	private int spinnerLessonCount = 0;

	// used to store values to SharedPreferences file
	private static LanguageSettings languageSettings;
	private static Resources res;

	private IDialogResultListener iDialogResultListener = null;
	private int lessonLevel = -1;

	public final static String LESSON_DIALOG_RESPONSE = "com.fisincorporated.languagetutorial.lesson.dialog.response";

	public void setOnDialogResultListener(IDialogResultListener listener) {
		this.iDialogResultListener = listener;

	}

	// // Should I really be saving these values in a bundle and pick them up
	// from
	// // bundle in onCreate?
	// public static LessonSelectionDialog newInstance(Long inTeacherId,
	// Long inTeacherLanguageId, Long inClassId, Long inLessonId) {
	// LessonSelectionDialog f = new LessonSelectionDialog();
	// teacherId = inTeacherId;
	// teacherLanguageId = inTeacherLanguageId;
	// classId = inClassId;
	// lessonId = inLessonId;
	// classNameList = new ArrayList<ClassName>();
	// lessonList = new ArrayList<Lesson>();
	// return f;
	// }
	public static LessonSelectionDialog newInstance(int lessonLevel) {
		LessonSelectionDialog f = new LessonSelectionDialog();
		Bundle args = new Bundle();
		args.putInt(LESSON_LEVEL, lessonLevel);
		f.setArguments(args);
		return f;
	}

	public LessonSelectionDialog() {
		languageSettings = LanguageSettings.getInstance(getActivity());
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

		getDatabaseSetup();
		getCurrentLessonSettings();
		setRetainInstance(true);

	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		res = getActivity().getResources();
		getLessonLevel(savedInstanceState);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(res.getString(R.string.select));
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.class_selection, null);
		builder.setView(v);

		spnrTeacher = (Spinner) v.findViewById(R.id.spnrTeacher);
		lblTeacher = (TextView) v.findViewById(R.id.lblTeacher);

		spnrTeacherLanguage = (Spinner) v.findViewById(R.id.spnrTeacherLanguage);
		lblTeacherLanguage = (TextView) v.findViewById(R.id.lblTeacherLanguage);

		lblClass = (TextView) v.findViewById(R.id.lblClass);
		spnrClassName = (Spinner) v.findViewById(R.id.spnrClass);

		lblLesson = (TextView) v.findViewById(R.id.lblLesson);
		spnrLesson = (Spinner) v.findViewById(R.id.spnrLesson);

		// The OK button saves whatever selected
		builder.setPositiveButton(R.string.save,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// saveSelections();
						saveLanguagePreferences();
						sendResult(Activity.RESULT_OK, id);
					}
				});
		// Negative button is Cancel so return without saving anything
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						sendResult(Activity.RESULT_CANCELED, id);
					}
				});

		setUpTeacherDropdown();

		setUpTeacherLanguageDropDown();

		setupClassDropdown();
		if (lessonLevel == SELECT_TO_LESSON) {
			setupLessonDropdown();
		}
		v.invalidate();
		return builder.create();

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(LESSON_LEVEL, lessonLevel);
		super.onSaveInstanceState(savedInstanceState);

	}

	public void getLessonLevel(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			lessonLevel = savedInstanceState.getInt(LESSON_LEVEL);
		} else if (getArguments() != null) {
			lessonLevel = getArguments().getInt(LESSON_LEVEL);
		} else
			lessonLevel = SELECT_TO_LESSON;
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
		teacherArrayAdapter = new ArrayAdapter<Teacher>(getActivity(),
				android.R.layout.simple_spinner_item, teacherList);
		teacherArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrTeacher.setAdapter(teacherArrayAdapter);

		// if only one teacher use it
		if (teacherList.size() == 1) {
			selectedTeacher = teacherList.get(0);
			teacherId = selectedTeacher.getId();
		}
		// if no teachers - big trouble
		if (teacherList.size() == 0) {
			Toast.makeText(getActivity(),
					res.getString(R.string.no_teachers_defined), Toast.LENGTH_LONG)
					.show();
			setTeachersInvisible();
			setClassAndLessonInvisible();
		}
		// if valid teacherId then set spinner
		if (teacherId != -1) {
			// teacherPosition = getTeacherPosition(teacherList, teacherId);
			teacherPosition = getPosition(teacherList, teacherId);
			selectedTeacher = teacherList.get(teacherPosition);
			// teacherName = selectedTeacher.getTeacherName();
			spnrTeacher.setSelection(teacherPosition);
			// spnrTeacher.setSelected(true);
			fillTeacherFromToLanguageList();
		}

		spnrTeacher.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				++spinnerTeacherCount;
				if (spinnerTeacherCount <= 1)
					return;
				selectedTeacher = ((Teacher) parent.getItemAtPosition(position));
				teacherId = selectedTeacher.getId();
				// teacherName = selectedTeacher.getTeacherName();
				teacherLanguageId = -1l;
				// teacherLanguageTitle = "";
				classId = -1l;
				// classTitle = "";
				lessonId = -1l;
				// lessonTitle = "";
				reloadTeacherLanguageList();
				if (teacherFromToLanguageList.size() == 0) {
					Toast.makeText(
							getActivity(),
							res.getString(R.string.no_languages_defined_for_this_teacher),
							Toast.LENGTH_LONG).show();
					setTeacherLanguageClassAndLessonInvisible();
				} else {
					setTeacherLanguageVisibleClassAndLessonInvisible();
				}

			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

	}

	private void setUpTeacherLanguageDropDown() {
		// Start of language list dropdown stuff
		// if valid teacher then get the languages they teach
		if (teacherId != -1) {
			fillTeacherFromToLanguageList();
		}
		teacherLanguageArrayAdapter = new ArrayAdapter<TeacherFromToLanguage>(
				getActivity(), android.R.layout.simple_spinner_item,
				teacherFromToLanguageList);
		teacherLanguageArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrTeacherLanguage.setAdapter(teacherLanguageArrayAdapter);

		// if only one teacher/language or teacher/language not specified pick the
		// first(or only) one
		// in the list
		if (teacherFromToLanguageList.size() == 1
				|| (teacherLanguageId == -1 && teacherFromToLanguageList.size() > 0)) {
			spnrTeacherLanguage.setSelection(0);
			selectedTeacherFromToLanguage = teacherFromToLanguageList.get(0);
			teacherLanguageId = selectedTeacherFromToLanguage.getId();
		}
		if (teacherLanguageId != -1) {
			// teacherLanguagePosition = getTeacherFromToLanguagePosition(
			// teacherFromToLanguageList, teacherLanguageId);
			teacherLanguagePosition = getPosition(teacherFromToLanguageList,
					teacherLanguageId);
			if (teacherLanguagePosition > -1) {
				spnrTeacherLanguage.setSelection(teacherLanguagePosition);
				selectedTeacherFromToLanguage = teacherFromToLanguageList
						.get(teacherLanguagePosition);
				// teacherLanguageTitle =
				// selectedTeacherFromToLanguage.getTeacherLanguageTitle();
				// spnrTeacherLanguage.setSelected(true);
				setTeacherLanguageVisibleClassAndLessonInvisible();
				// spnrClassName.setBackgroundColor(Color.GREEN);
			} else {
				Toast.makeText(getActivity(),
						res.getString(R.string.teacher_language_no_longer_defined),
						Toast.LENGTH_LONG).show();
				setTeacherLanguageClassAndLessonInvisible();
			}
		}
		spnrTeacherLanguage
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view,
							int position, long id) {
						++spinnerTeacherLanguageCount;
						if (spinnerTeacherLanguageCount <= 1)
							return;
						selectedTeacherFromToLanguage = ((TeacherFromToLanguage) parent
								.getItemAtPosition(position));
						teacherLanguageId = selectedTeacherFromToLanguage.getId();
						// teacherLanguageTitle = selectedTeacherFromToLanguage
						// .getTeacherLanguageTitle();
						classId = -1l;
						// classTitle = "";
						lessonId = -1l;
						// lessonTitle = "";
						reloadClassNameList();
						if (classNameList.size() == 0) {
							Toast.makeText(
									getActivity(),
									res.getString(R.string.no_classes_defined_for_this_teacher),
									Toast.LENGTH_LONG).show();
							setClassAndLessonInvisible();
						} else {
							setClassVisibleLessonInvisible();
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

	}

	private void setupClassDropdown() {
		// Start of class dropdown stuff
		// if valid teacher/language then get their classes
		if (teacherLanguageId != -1) {
			fillClassList();
		}
		classNameArrayAdapter = new ArrayAdapter<ClassName>(getActivity(),
				android.R.layout.simple_spinner_item, classNameList);
		classNameArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrClassName.setAdapter(classNameArrayAdapter);
		if (classNameList.size() == 1) {
			spnrClassName.setSelection(0);
			selectedClassName = classNameList.get(0);
		}

		// if only one class or class not specified pick the first(or only) class
		// in the list
		if (classNameList.size() == 1
				|| (classId == -1 && classNameList.size() > 0)) {
			classId = classNameList.get(0).getId();
		}
		if (classId != -1) {
			// classNamePosition = getClassNamePosition(classNameList, classId);
			classNamePosition = getPosition(classNameList, classId);
			if (classNamePosition > -1) {
				selectedClassName = classNameList.get(classNamePosition);
				// classTitle = selectedClassName.getClassTitle();
				spnrClassName.setSelection(classNamePosition);
				// spnrClassName.setSelected(true);
				setClassVisibleLessonInvisible();
				// spnrClassName.setBackgroundColor(Color.GREEN);
			} else {
				Toast.makeText(getActivity(),
						res.getString(R.string.class_no_longer_defined),
						Toast.LENGTH_LONG).show();
				setClassAndLessonInvisible();
			}
		}

		spnrClassName.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				++spinnerClassNameCount;
				if (spinnerClassNameCount <= 1)
					return;
				selectedClassName = ((ClassName) parent.getItemAtPosition(position));
				classId = selectedClassName.getId();
				// classTitle = selectedClassName.getClassTitle();
				lessonId = -1l;
				// lessonTitle = "";
				if (lessonLevel != SELECT_TO_LESSON) {
					return;
				}
				reloadLessonList();
				if (lessonList.size() == 0) {
					setClassVisibleLessonInvisible();
					Toast.makeText(getActivity(),
							res.getString(R.string.no_lessons_defined_for_this_class),
							Toast.LENGTH_LONG).show();
					setClassVisibleLessonInvisible();
				} else {
					// set the lessonId and title in case user doesn't select it
					// because it is already displayed
					selectedLesson = lessonList.get(0);
					lessonId = selectedLesson.getId();
					// lessonTitle = selectedLesson.getLessonTitle();
					if (lessonLevel == SELECT_TO_LESSON) {
						setLessonVisible();
					}
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

	}

	private void setupLessonDropdown() {
		// start of lesson stuff
		if (classId != -1) {
			// get all lessons for the class class
			fillLessonList();
		}
		lessonArrayAdapter = new ArrayAdapter<Lesson>(getActivity(),
				android.R.layout.simple_spinner_item, lessonList);
		lessonArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnrLesson.setAdapter(lessonArrayAdapter);

		// if only one lesson or lesson not specified pick the first (or only)
		// lesson in the list
		if (lessonList.size() == 1 || (lessonId == -1 && lessonList.size() > 0)) {
			selectedLesson = lessonList.get(0);
			lessonId = selectedLesson.getId();
		}
		if (lessonId != -1) {
			// lessonPosition = getLessonPosition(lessonList, lessonId);
			lessonPosition = getPosition(lessonList, lessonId);
			if (lessonPosition > -1) {
				selectedLesson = lessonList.get(lessonPosition);
				// lessonTitle = selectedLesson.getLessonTitle();
				spnrLesson.setSelection(lessonPosition);
				// spnrLesson.setSelected(true);
				setLessonVisible();
			} else {
				Toast.makeText(getActivity(),
						res.getString(R.string.lesson_no_longer_defined),
						Toast.LENGTH_LONG).show();
				setClassVisibleLessonInvisible();
			}
		}
		spnrLesson.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				++spinnerLessonCount;
				if (spinnerLessonCount <= 1)
					return;
				selectedLesson = (Lesson) parent.getItemAtPosition(position);
				lessonId = selectedLesson.getId();
				// lessonTitle = selectedLesson.getLessonTitle();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

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

	private void setLessonVisible() {
		lblLesson.setVisibility(View.VISIBLE);
		spnrLesson.setVisibility(View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		// spnrTeacher.setSelection(0);
	}

	// following methods are duplicated in DeleteTeacherLanguageDialog.
	// At some point move common logic to respective Dao's
	private void fillTeacherList() {
		qbTeacher = teacherDao.queryBuilder();
		teacherList = (ArrayList<Teacher>) qbTeacher.list();
	}

	private int getPosition(ArrayList<? extends DomainObject> list, Long id) {
		for (int i = 0; i < list.size(); ++i) {
			if (id == list.get(i).getId()) {
				return i;
			}
		}
		return -1;
	}

	// private int getTeacherPosition(ArrayList<Teacher> list, Long id) {
	// for (int i = 0; i < list.size(); ++i) {
	// if (id == list.get(i).getId()) {
	// return i;
	// }
	// }
	// return -1;
	// }

	// Teacher language methods
	private void reloadTeacherLanguageList() {
		teacherLanguageArrayAdapter.clear();
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
	}

	// private int getTeacherFromToLanguagePosition(
	// ArrayList<TeacherFromToLanguage> list, Long id) {
	// for (int i = 0; i < list.size(); ++i) {
	// if (id == list.get(i).getId()) {
	// return i;
	// }
	// }
	// return -1;
	// }

	// Class Name methods
	@SuppressLint("NewApi")
	private void reloadClassNameList() {
		classNameArrayAdapter.clear();
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
		}
		classNameList.clear();
		classNameList.addAll(classNameQuery.list());

	}

	// private int getClassNamePosition(ArrayList<ClassName> list, Long id) {
	// for (int i = 0; i < list.size(); ++i) {
	// if (id == list.get(i).getId()) {
	// return i;
	// }
	// }
	// return -1;
	// }

	@SuppressLint("NewApi")
	private void reloadLessonList() {
		lessonArrayAdapter.clear();

		fillLessonList();
		// no need to read to adapter as added to lessonList in fillLessonList
		// above
		// if (android.os.Build.VERSION.SDK_INT >=
		// android.os.Build.VERSION_CODES.HONEYCOMB) {
		// lessonArrayAdapter.addAll(lessonList);
		// } else {
		// for (int i = 0; i < lessonList.size(); ++i) {
		// lessonArrayAdapter.add((Lesson) lessonList.get(i));
		// }
		// }
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

	// private int getLessonPosition(ArrayList<Lesson> list, Long id) {
	// for (int i = 0; i < list.size(); ++i) {
	// if (id == list.get(i).getId()) {
	// return i;
	// }
	// }
	// return -1;
	// }

	// updated selections should be saved in preferences file prior to returning

	private void sendResult(int resultCode, int button_pressed) {
		if (iDialogResultListener != null) {
			iDialogResultListener.onDialogResult(lessonLevel, resultCode,
					button_pressed, null);
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
				.setClassId(selectedClassName.getId())
				.setClassTitle(selectedClassName.getClassTitle())
				.setLessonId(
						(lessonLevel == SELECT_TO_LESSON) ? selectedLesson.getId()
								: -1)
				.setLessonTitle(
						(lessonLevel == SELECT_TO_LESSON) ? selectedLesson
								.getLessonTitle() : "") 
				.setLastLessonPhraseLine(-1)
				.commit();

	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			// if crash occurs try getDialog().setDismissMessage(null);
			spinnerTeacherCount = 0;
			spinnerTeacherLanguageCount = 0;
			spinnerClassNameCount = 0;
			spinnerLessonCount = 0;
			getDialog().setOnDismissListener(null);
		}
		super.onDestroyView();
	}

}
