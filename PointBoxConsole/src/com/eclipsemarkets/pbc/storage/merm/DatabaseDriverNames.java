/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author Zhijun.Zhang
 */
public enum DatabaseDriverNames {
    JDBC_ODBC_BRIDGE("sun.jdbc.odbc.JdbcOdbcDriver");

    private String term;
    DatabaseDriverNames(String term){
        this.term = term;
    }

    @Override
    public String toString() {
        return term;
    }

}
