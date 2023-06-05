/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author Zhijun Zhang
 */
abstract class DatabaseCursor implements IDatabaseCursor {
    protected Object value;

    public Object getDatabaseCursorValue() {
        return value;
    }

    /**
     *
     * @return face value for this cursor, e.g., Integer returns Integer.toDtring()
     */
    @Override
    public String toString() {
        return value.toString();
    }
}
