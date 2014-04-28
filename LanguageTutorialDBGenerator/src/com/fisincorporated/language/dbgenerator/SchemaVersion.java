package com.fisincorporated.language.dbgenerator;

import javax.validation.constraints.*;

import de.greenrobot.daogenerator.Schema;

/**
 * A Version of the Schema.
 * 
 * from http://www.androidanalyse.com/greendao-schema-generation/
 */
public abstract class SchemaVersion {
     
    @NotNull
        // TODO #1 Update this to a package definition which makes sense for your project
    public static final String CURRENT_SCHEMA_PACKAGE = "com.fisincorporated.languagetutorial.db";
    private static final String DEFAULT_TEST_PACKAGE = "com.fisincorporated.languagetutorial.test";
 	private static final String DEFAULT_DAO_PACKAGE ="com.fisincorporated.languagetutorial.db";
     
    @NotNull
    private final Schema schema;
     
    private final boolean current;
     
    /**
     * Constructor 
     * 
     * @param current indicating if this is the current schema.
     */
    public SchemaVersion(boolean current) {
        int version = getVersionNumber();
        String packageName = CURRENT_SCHEMA_PACKAGE;
        //if (!current) {
        //    packageName += ".v" + version;
       // }
        this.schema = new Schema(version, packageName);
        this.schema.enableKeepSectionsByDefault();
        this.schema.setDefaultJavaPackageTest(DEFAULT_TEST_PACKAGE + ((!current)? ".v" + version :""));
  		  this.schema.setDefaultJavaPackageDao( DEFAULT_DAO_PACKAGE +  ((!current)? ".v" + version : ""));
        this.current = current;
    }
 
    /**
     * @return the GreenDAO schema. 
     */
    @NotNull
    protected Schema getSchema() {
        return schema;
    }
     
    /**
     * @return boolean indicating if this is the highest or current schema version.
     */
    public boolean isCurrent() {
        return current;
    }
     
    /**
     * @return unique integer schema version identifier. 
     */
    public abstract int getVersionNumber();
}
