package com.fisincorporated.languagetutorial.db;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.fisincorporated.languagetutorial.db.ClassName;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table CLASS_NAME.
*/
public class ClassNameDao extends AbstractDao<ClassName, Long> {

    public static final String TABLENAME = "CLASS_NAME";

    /**
     * Properties of entity ClassName.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property TeacherId = new Property(1, long.class, "teacherId", false, "TEACHER_ID");
        public final static Property ClassOrder = new Property(2, int.class, "classOrder", false, "CLASS_ORDER");
        public final static Property ClassTitle = new Property(3, String.class, "classTitle", false, "CLASS_TITLE");
        public final static Property Description = new Property(4, String.class, "description", false, "DESCRIPTION");
        public final static Property TeacherLanguageId = new Property(5, long.class, "teacherLanguageId", false, "TEACHER_LANGUAGE_ID");
    };

    private DaoSession daoSession;

    private Query<ClassName> teacher_TeacherToClassesQuery;
    private Query<ClassName> teacherLanguage_TeacherLanguageToClassesQuery;

    public ClassNameDao(DaoConfig config) {
        super(config);
    }
    
    public ClassNameDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'CLASS_NAME' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'TEACHER_ID' INTEGER NOT NULL ," + // 1: teacherId
                "'CLASS_ORDER' INTEGER NOT NULL ," + // 2: classOrder
                "'CLASS_TITLE' TEXT NOT NULL ," + // 3: classTitle
                "'DESCRIPTION' TEXT NOT NULL ," + // 4: description
                "'TEACHER_LANGUAGE_ID' INTEGER NOT NULL );"); // 5: teacherLanguageId
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_CLASS_NAME_TEACHER_ID ON CLASS_NAME" +
                " (TEACHER_ID);");
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "classOrderIx ON CLASS_NAME" +
                " (TEACHER_ID,TEACHER_LANGUAGE_ID,CLASS_ORDER);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'CLASS_NAME'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, ClassName entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getTeacherId());
        stmt.bindLong(3, entity.getClassOrder());
        stmt.bindString(4, entity.getClassTitle());
        stmt.bindString(5, entity.getDescription());
        stmt.bindLong(6, entity.getTeacherLanguageId());
    }

    @Override
    protected void attachEntity(ClassName entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public ClassName readEntity(Cursor cursor, int offset) {
        ClassName entity = new ClassName( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // teacherId
            cursor.getInt(offset + 2), // classOrder
            cursor.getString(offset + 3), // classTitle
            cursor.getString(offset + 4), // description
            cursor.getLong(offset + 5) // teacherLanguageId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, ClassName entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTeacherId(cursor.getLong(offset + 1));
        entity.setClassOrder(cursor.getInt(offset + 2));
        entity.setClassTitle(cursor.getString(offset + 3));
        entity.setDescription(cursor.getString(offset + 4));
        entity.setTeacherLanguageId(cursor.getLong(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(ClassName entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(ClassName entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "teacherToClasses" to-many relationship of Teacher. */
    public List<ClassName> _queryTeacher_TeacherToClasses(long teacherId) {
        synchronized (this) {
            if (teacher_TeacherToClassesQuery == null) {
                QueryBuilder<ClassName> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.TeacherId.eq(null));
                queryBuilder.orderRaw("CLASS_ORDER ASC");
                teacher_TeacherToClassesQuery = queryBuilder.build();
            }
        }
        Query<ClassName> query = teacher_TeacherToClassesQuery.forCurrentThread();
        query.setParameter(0, teacherId);
        return query.list();
    }

    /** Internal query to resolve the "teacherLanguageToClasses" to-many relationship of TeacherLanguage. */
    public List<ClassName> _queryTeacherLanguage_TeacherLanguageToClasses(long teacherId) {
        synchronized (this) {
            if (teacherLanguage_TeacherLanguageToClassesQuery == null) {
                QueryBuilder<ClassName> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.TeacherId.eq(null));
                queryBuilder.orderRaw("CLASS_ORDER ASC");
                teacherLanguage_TeacherLanguageToClassesQuery = queryBuilder.build();
            }
        }
        Query<ClassName> query = teacherLanguage_TeacherLanguageToClassesQuery.forCurrentThread();
        query.setParameter(0, teacherId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getTeacherDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T1", daoSession.getTeacherLanguageDao().getAllColumns());
            builder.append(" FROM CLASS_NAME T");
            builder.append(" LEFT JOIN TEACHER T0 ON T.'TEACHER_ID'=T0.'_id'");
            builder.append(" LEFT JOIN TEACHER_LANGUAGE T1 ON T.'TEACHER_ID'=T1.'_id'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected ClassName loadCurrentDeep(Cursor cursor, boolean lock) {
        ClassName entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Teacher teacher = loadCurrentOther(daoSession.getTeacherDao(), cursor, offset);
         if(teacher != null) {
            entity.setTeacher(teacher);
        }
        offset += daoSession.getTeacherDao().getAllColumns().length;

        TeacherLanguage teacherLanguage = loadCurrentOther(daoSession.getTeacherLanguageDao(), cursor, offset);
         if(teacherLanguage != null) {
            entity.setTeacherLanguage(teacherLanguage);
        }

        return entity;    
    }

    public ClassName loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<ClassName> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<ClassName> list = new ArrayList<ClassName>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<ClassName> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<ClassName> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
