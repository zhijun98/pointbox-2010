/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import com.eclipsemarkets.pbc.storage.exceptions.DatabaseCursorTypeException;

/**
 *
 * @author Zhijun Zhang
 */
class DatabaseIntegerCursor extends DatabaseCursor{

    DatabaseIntegerCursor(int value) {
        this.value = new Integer(value);
    }

    public DatabaseCursorTypes getDatabaseCursorTypes() {
        return DatabaseCursorTypes.INTEGER;
    }

    public void setDatabaseCursorValue(Object value) throws DatabaseCursorTypeException {
        if ((value != null) && (value instanceof Integer)){
            this.value = value;
        }else{
            throw new DatabaseCursorTypeException("Integer value was expected by this DatabaseCursor instance.");
        }
    }

}
