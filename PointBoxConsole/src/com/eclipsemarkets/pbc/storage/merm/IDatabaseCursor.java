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
public interface IDatabaseCursor {

    public DatabaseCursorTypes getDatabaseCursorTypes();

    public Object getDatabaseCursorValue();

    public void setDatabaseCursorValue(Object value) throws DatabaseCursorTypeException;

}
