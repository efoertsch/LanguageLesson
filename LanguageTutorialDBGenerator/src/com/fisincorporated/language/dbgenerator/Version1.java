package com.fisincorporated.language.dbgenerator;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Index;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

/**
 * Version 1 of the Schema definition
 * 
 * from http://www.androidanalyse.com/greendao-schema-generation/ Modified for
 * Language Tutorial database
 */
public class Version1 extends SchemaVersion {
	private static final int VERSION_NUMBER = 1;
	
	Entity languageCode;
	Entity teacher;
	Entity className;
	Entity lesson;
	Entity lessonPhrase;
	Entity languagePhrase;
	Entity compoundPhrase;
	Entity teacherLanguage;
	Entity languageXref;

	/**
	 * Constructor
	 * 
	 * @param current
	 */
	public Version1(boolean current) {
		super(current);

		Schema schema = getSchema();
		// Not sure how the following interacts with this schema generator
		//schema.setDefaultJavaPackageTest("com.fisincorporated.languagetutorial.test");
		//schema.setDefaultJavaPackageDao("com.fisincorporated.languagetutorial.dao");
		// The schema also has two default flags for entities, which can be
		// overridden.
		// The flags tell if entities are active, and if keep sections should be
		// used.
		// Those features are not yet documented; have a look at the test project
		// in the source code distribution.
		// keepSections enabled in SchemaVersion so not needed here
		schema.enableKeepSectionsByDefault();
		 
		// schema.enableActiveEntitiesByDefault();

		languageCode = addLanguageCodeEntity(schema);
		teacher = addTeacherEntity(schema);
		teacherLanguage = addTeacherLanguage(schema);
		className = addClassNameEntity(schema);
		lesson = addLessonEntity(schema);
		languagePhrase = addLanguagePhraseEntity(schema);
		languageXref = addLanguageXrefEntity(schema);
		lessonPhrase = addLessonPhraseEntity(schema);
		compoundPhrase = addCompoundPhraseEntity(schema);
		System.out.println("!!!!!!!!!!!!!!!!!!!");
		System.out.println("If keep sections not being retained, add the following to Teacher, Class and Lesson");
		System.out.println("import com.fisincorporated.languagetutorial.utility.DomainObject;");
		System.out.println(" Class ...  implements DomainObject;");
		System.out.println("public String toString(){   	"
				+ " return teacherName (or classTitle or lessonTitle) ;"
				+ "    }");
		System.out.println("!!!!!!!!!!!!!!!!!!!");
	}

	private Entity addLanguageCodeEntity(Schema schema) {
		Entity languageCode = schema.addEntity("LanguageCode");
		languageCode.addIdProperty().autoincrement();
//		languageCode.addStringProperty("languageCode").notNull()
//				.indexAsc("languageCode_ix", true);
		languageCode.addStringProperty("languageName").notNull();
		return languageCode;
	}

	private Entity addTeacherEntity(Schema schema) {
		Entity teacher = schema.addEntity("Teacher");
		teacher.addIdProperty().autoincrement();
		teacher.addStringProperty("teacherName").notNull().unique();
		teacher.addStringProperty("teacherURL");
		return teacher;
	}

	private Entity addTeacherLanguage(Schema schema) {
		Entity teacherLanguage = schema.addEntity("TeacherLanguage");
		teacherLanguage.addIdProperty().autoincrement();
		Property teacherId = teacherLanguage.addLongProperty("teacherId")
				.notNull().index().getProperty();
		Property learningLanguageId = teacherLanguage
				.addLongProperty("learningLanguageId").notNull().index()
				.getProperty();
		teacherLanguage.addStringProperty("learningLanguageMediaDirectory");
		Property knownLanguageId = teacherLanguage
				.addLongProperty("knownLanguageId").index().getProperty();
		teacherLanguage.addStringProperty("knownLanguageMediaDirectory");

		// a teacherLanguage belongs to one teacher (dependent on teacher being
		// defined)
		teacherLanguage.addToOne(teacher, teacherId);
		// a teacher can have multiple teacherLanguages
		ToMany teacherToManyLanguages = teacher.addToMany(teacherLanguage,
				teacherId);
		teacherToManyLanguages.setName("teacherToLanguage");

		// a teacherLanguage points to one learning language (dependent on
		// language defined)
		teacherLanguage.addToOne(languageCode, learningLanguageId).setName(
				"learningLanguageCheck");
		// a teacherLanguage can have one known language (optional) (dependent on
		// langurage defined)
		teacherLanguage.addToOne(languageCode, knownLanguageId).setName(
				"knownLanguageCheck");

		return teacherLanguage;

	}

	private Entity addClassNameEntity(Schema schema) {
		Entity className = schema.addEntity("ClassName");
		className.addIdProperty().autoincrement();
		Property teacherId = className.addLongProperty("teacherId").notNull()
				.index().getProperty();
		Property order = className.addIntProperty("classOrder").notNull().getProperty();
		Property classTitle = className.addStringProperty("classTitle")
				.notNull().getProperty();
		Property description = className.addStringProperty("description")
				.notNull().getProperty();
		Property teacherLanguageId = className
				.addLongProperty("teacherLanguageId").notNull().getProperty();
		
		Index index = new Index().makeUnique().setName("classOrderIx");
		index.addProperty(teacherId);
		index.addProperty(teacherLanguageId);
		index.addProperty(order);
		className.addIndex(index);

		// a class belongs to one teacher
		className.addToOne(teacher, teacherId);
		// a teacher can have multiple classes
		ToMany teacherToClasses = teacher.addToMany(className, teacherId);
		teacherToClasses.orderAsc(order);
		teacherToClasses.setName("teacherToClasses");
		
		// a class belongs to one teacher/language
		className.addToOne(teacherLanguage, teacherId);

		// a TeacherLanguage can have many classes
		ToMany teacherLanguageToClasses =  teacherLanguage.addToMany(className, teacherId);
		teacherLanguageToClasses.orderAsc(order);
		teacherLanguageToClasses.setName("teacherLanguageToClasses");

		return className;
	}

	private Entity addLessonEntity(Schema schema) {
		Entity lesson = schema.addEntity("Lesson");
		lesson.addIdProperty().autoincrement();
		Property classId = lesson.addLongProperty("classId").notNull().index()
				.getProperty();
		//order of the lessons in the class 
		Property lessonOrder = lesson.addIntProperty("lessonOrder").notNull().getProperty();
		lesson.addStringProperty("lessonTitle") ;
		lesson.addStringProperty("description") ;
		lesson.addStringProperty("lessonType") ;
		// a lesson belongs to one class
		lesson.addToOne(className, classId);
		// a class can have multiple lessons
		ToMany classesToLessons = 	className.addToMany(lesson, classId);
		classesToLessons.orderAsc(lessonOrder);
		classesToLessons.setName("ClassToLessons");

		return lesson;
	}

	private Entity addLanguageXrefEntity(Schema schema) {
		Entity languageXref = schema.addEntity("LanguageXref");
		languageXref.addIdProperty().autoincrement();
		Property teacherId = languageXref.addLongProperty("teacherId").notNull()
				.index().getProperty();
		Property teacherLanguageId = languageXref.addLongProperty("teacherLanguageId").notNull()
				.index().getProperty();
		Property learningPhraseId = languageXref
				.addLongProperty("learningPhraseId").notNull().index()
				.getProperty();
		Property knownPhraseId = languageXref.addLongProperty("knownPhraseId")
				.index().getProperty();

		// a languageXref belongs to one teacher
		languageXref.addToOne(teacher, teacherId);
		// a teacher can have multiple languageXrefs
		ToMany teacherToManyLanguageXrefs = teacher.addToMany(languageXref,
				teacherId);
		teacherToManyLanguageXrefs.orderAsc(teacherLanguageId);
		teacherToManyLanguageXrefs.setName("teacherToLanguageXrefs");

		// a teacher_language can point to many languageXrefs
		ToMany teacherLanguageToManyLanguageXrefs = teacherLanguage.addToMany(languageXref, teacherLanguageId);
		teacherLanguageToManyLanguageXrefs.setName("TeacherLanguageToManyLanguageXrefs");
		
		// a languageXref points to one language phrase
		// languagePhrase must be defined first
		languageXref.addToOne(languagePhrase, learningPhraseId).setName("languageXrefToOneLearningLanguagePhrase");
		// A languageXref may point to a known language phrase
		languageXref.addToOne(languagePhrase, knownPhraseId).setName("languageXrefToOneKnownLanguagePhrase");

		return languageXref;
	}

	private Entity addLessonPhraseEntity(Schema schema) {
		Entity lessonPhrase = schema.addEntity("LessonPhrase");
		lessonPhrase.addIdProperty().autoincrement();
		Property lessonId = lessonPhrase.addLongProperty("lessonId").notNull()
				.getProperty();
		Property lessonOrder = lessonPhrase.addIntProperty("lessonOrder")
				.notNull().getProperty();
		lessonPhrase.addIntProperty("speaker").notNull();
		Property languageXrefId = lessonPhrase.addLongProperty("languageXrefId")
				.notNull().getProperty();
		lessonPhrase.addFloatProperty("phraseInterval").notNull();

		Index index = new Index();
		index.addProperty(lessonId);
		index.addProperty(lessonOrder);
		lessonPhrase.addIndex(index);
		// a lessonPhrase belongs to one lesson
		lessonPhrase.addToOne(lesson, lessonId).setName("lessonPhraseToLesson");
		// a lesson can have multiple lesson phrases
		ToMany lessonToManyLessonPhrases = lesson.addToMany(lessonPhrase,
				lessonId);
		lessonToManyLessonPhrases.orderAsc(lessonOrder);
		lessonToManyLessonPhrases.setName("lessonToManyLessonPhrases");

		// a lessonPhrase points to one language xref phrase
		// languagePhrase must be defined first
		lessonPhrase.addToOne(languageXref, languageXrefId).setName("lessonPhraseToOneLanguageXref");
		return lessonPhrase;
	}

	private Entity addLanguagePhraseEntity(Schema schema) {
		Entity languagePhrase = schema.addEntity("LanguagePhrase");
		languagePhrase.addIdProperty().autoincrement();
		Property teacherId = languagePhrase.addLongProperty("teacherId")
				.notNull().getProperty();
		Property languageId = languagePhrase.addLongProperty("languageId")
				.notNull().index().getProperty();
		Property writtenPhrase =languagePhrase.addStringProperty("writtenPhrase").notNull().getProperty();
		languagePhrase.addStringProperty("audioFile");
		languagePhrase.addStringProperty("videoFile");
		languagePhrase.addStringProperty("phraseType").notNull();
		languagePhrase.addStringProperty("pronunciation");
		languagePhrase.addStringProperty("englishNumeral");
		languagePhrase.addLongProperty("compoundPhraseId");
		Index index = new Index();
		index.addProperty(teacherId);
		index.addProperty(languageId);
		index.addProperty(writtenPhrase);
		languagePhrase.addIndex(index);

		// a languagePhrase belongs to a teacher
		languagePhrase.addToOne(teacher, teacherId).setName("languagePhraseToOneTeacher");

		// a languagePhrase belongs to one language
		languagePhrase.addToOne(languageCode, languageId).setName("languagePhraseToOneLanguageCode");

		// a teacher can have many languagePhrases
		// a lesson can have multiple lesson phrases
		teacher.addToMany(languagePhrase, teacherId).setName("teachertoManyLanguagePhrases");

		return languagePhrase;
	}

	private Entity addCompoundPhraseEntity(Schema schema) {
		Entity compoundPhrase = schema.addEntity("CompoundPhrase");
		Property compoundId = compoundPhrase.addIdProperty().autoincrement()
				.getProperty();
		Property compoundPhraseId = compoundPhrase
				.addLongProperty("compoundPhraseId").notNull().index().getProperty();
		Property phraseOrder = compoundPhrase.addIntProperty("phraseOrder")
				.notNull().getProperty();
		Property languagePhraseId = compoundPhrase
				.addLongProperty("languagePhraseId").index()
				.getProperty();
		compoundPhrase.addFloatProperty("phraseInterval").notNull();
		Index index = new Index();
		index.addProperty(compoundPhraseId);
		index.addProperty(phraseOrder);
		compoundPhrase.addIndex(index);

		// a languagePhrase can have many compoundPhrases
		languagePhrase.addToMany(compoundPhrase, compoundPhraseId).setName(
				"languagePhraseToManyCompoundPhrases");

		// a compound phrase points to one language phrase
		compoundPhrase.addToOne(languagePhrase, languagePhraseId).setName("compoundPhraseToOneLanguagePhrase");

		return compoundPhrase;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getVersionNumber() {
		return VERSION_NUMBER ;
	}

 

}
