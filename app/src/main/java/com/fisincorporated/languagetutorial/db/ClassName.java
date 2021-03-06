package com.fisincorporated.languagetutorial.db;

import java.util.List;
import com.fisincorporated.languagetutorial.db.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import com.fisincorporated.languagetutorial.utility.DomainObject;
// KEEP INCLUDES END
/**
 * Entity mapped to table CLASS_NAME.
 */
public class ClassName implements DomainObject {

    private Long id;
    private long teacherId;
    private int classOrder;
    /** Not-null value. */
    private String classTitle;
    /** Not-null value. */
    private String description;
    private long teacherLanguageId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient ClassNameDao myDao;

    private Teacher teacher;
    private Long teacher__resolvedKey;

    private TeacherLanguage teacherLanguage;
    private Long teacherLanguage__resolvedKey;

    private List<Lesson> ClassToLessons;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public ClassName() {
    }

    public ClassName(Long id) {
        this.id = id;
    }

    public ClassName(Long id, long teacherId, int classOrder, String classTitle, String description, long teacherLanguageId) {
        this.id = id;
        this.teacherId = teacherId;
        this.classOrder = classOrder;
        this.classTitle = classTitle;
        this.description = description;
        this.teacherLanguageId = teacherLanguageId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getClassNameDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }

    public int getClassOrder() {
        return classOrder;
    }

    public void setClassOrder(int classOrder) {
        this.classOrder = classOrder;
    }

    /** Not-null value. */
    public String getClassTitle() {
        return classTitle;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setClassTitle(String classTitle) {
        this.classTitle = classTitle;
    }

    /** Not-null value. */
    public String getDescription() {
        return description;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDescription(String description) {
        this.description = description;
    }

    public long getTeacherLanguageId() {
        return teacherLanguageId;
    }

    public void setTeacherLanguageId(long teacherLanguageId) {
        this.teacherLanguageId = teacherLanguageId;
    }

    /** To-one relationship, resolved on first access. */
    public Teacher getTeacher() {
        long __key = this.teacherId;
        if (teacher__resolvedKey == null || !teacher__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TeacherDao targetDao = daoSession.getTeacherDao();
            Teacher teacherNew = targetDao.load(__key);
            synchronized (this) {
                teacher = teacherNew;
            	teacher__resolvedKey = __key;
            }
        }
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new DaoException("To-one property 'teacherId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.teacher = teacher;
            teacherId = teacher.getId();
            teacher__resolvedKey = teacherId;
        }
    }

    /** To-one relationship, resolved on first access. */
    public TeacherLanguage getTeacherLanguage() {
        long __key = this.teacherId;
        if (teacherLanguage__resolvedKey == null || !teacherLanguage__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TeacherLanguageDao targetDao = daoSession.getTeacherLanguageDao();
            TeacherLanguage teacherLanguageNew = targetDao.load(__key);
            synchronized (this) {
                teacherLanguage = teacherLanguageNew;
            	teacherLanguage__resolvedKey = __key;
            }
        }
        return teacherLanguage;
    }

    public void setTeacherLanguage(TeacherLanguage teacherLanguage) {
        if (teacherLanguage == null) {
            throw new DaoException("To-one property 'teacherId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.teacherLanguage = teacherLanguage;
            teacherId = teacherLanguage.getId();
            teacherLanguage__resolvedKey = teacherId;
        }
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<Lesson> getClassToLessons() {
        if (ClassToLessons == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LessonDao targetDao = daoSession.getLessonDao();
            List<Lesson> ClassToLessonsNew = targetDao._queryClassName_ClassToLessons(id);
            synchronized (this) {
                if(ClassToLessons == null) {
                    ClassToLessons = ClassToLessonsNew;
                }
            }
        }
        return ClassToLessons;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetClassToLessons() {
        ClassToLessons = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    public String toString(){
   	 return classTitle;
    }
    // KEEP METHODS END

}
