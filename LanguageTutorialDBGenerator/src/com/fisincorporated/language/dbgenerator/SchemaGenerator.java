package com.fisincorporated.language.dbgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import de.greenrobot.daogenerator.DaoGenerator;

/**
 * The {@link SchemaGenerator} which manages the registered schema versions and
 * performs validation prior to generation.
 * 
 * from http://www.androidanalyse.com/greendao-schema-generation/
 */
public class SchemaGenerator {
 
    @NotNull
    // TODO Update this to your Android projects base source directory
    private static final String SCHEMA_OUTPUT_DIR = "../LanguageTutorialDBGenerator/src";
 
    /**
     * Generator main application which builds all of the schema versions
     * (including older versions used for migration test purposes) and ensures
     * business rules are met; these include ensuring we only have a single
     * current schema instance and the version numbering is correct.
     * 
     * @param args
     * 
     * @throws Exception
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        List<SchemaVersion> versions = new ArrayList<SchemaVersion>();
 
        versions.add(new Version1(true));
        // when version 2 comes along, comment out above line and uncomment next 2
        //versions.add(new Version1(false));
        //versions.add(new Version2(true));

 
        validateSchemas(versions);
 
        for (SchemaVersion version : versions) {
            // NB: Test output creates stubs, we have an established testing
            // standard which should be followed in preference to generating
            // these stubs.
            new DaoGenerator().generateAll(version.getSchema(),
                    SCHEMA_OUTPUT_DIR);
        }
    }
 
    /**
     * Validate the schema, throws
     * 
     * @param versions
     * @throws IllegalArgumentException
     *             if data is invalid
     */
    public static void validateSchemas(@NotNull List<SchemaVersion> versions)
            throws IllegalArgumentException {
        int numCurrent = 0;
        Set<Integer> versionNumbers = new HashSet<Integer>();
 
        for (SchemaVersion version : versions) {
            if (version.isCurrent()) {
                numCurrent++;
            }
 
            int versionNumber = version.getVersionNumber();
            if (versionNumbers.contains(versionNumber)) {
                throw new IllegalArgumentException(
                        "Unable to process schema versions, multiple instances with version number : "
                                + version.getVersionNumber());
            }
            versionNumbers.add(versionNumber);
        }
 
        if (numCurrent != 1) {
            throw new IllegalArgumentException(
                    "Unable to generate schema, exactly one schema marked as current is required.");
        }
    }
}
