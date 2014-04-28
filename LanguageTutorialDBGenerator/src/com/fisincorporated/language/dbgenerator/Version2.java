package com.fisincorporated.language.dbgenerator;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

 

/**
 * Version 2 of the Schema definition
 * 
 * from http://www.androidanalyse.com/greendao-schema-generation/
 * Update this when you need to add to your version 1 schema
 * This is not currently being generated, but is here for an example
 */
public class Version2 extends SchemaVersion {
 
    /**
     * Constructor
     * 
     * @param current
     */
    public Version2(boolean current) {
        super(current);
         
        Schema schema = getSchema();
        addNewEntity(schema);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersionNumber() {
        return 2;
    }
     
    private static Entity addNewEntity(Schema schema) {
        Entity someNewEntity = schema.addEntity("NewEntity");
        someNewEntity.addIdProperty();
        someNewEntity.addStringProperty("name").notNull();
        someNewEntity.addStringProperty("breed").notNull();
        return someNewEntity;
    }
}
