/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;


/**
 *
 * @author Zhijun Zhang
 */
abstract class DatabaseSQLCommand implements IDatabaseSQLCommand {
    protected String sqlString;

    public DatabaseSQLCommand(String sqlString) {
        if (sqlString == null){
            sqlString = "";
        }
        this.sqlString = sqlString;
    }

    public String getSQLString() {
        return sqlString;
    }
}
