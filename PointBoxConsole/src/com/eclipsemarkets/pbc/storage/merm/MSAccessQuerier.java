/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MSAccessQuerier is Thread-safe but not singleton
 * @author Zhijun Zhang
 */
class MSAccessQuerier extends MSAccessSQLEngine implements IDatabaseQuerier{

    private MSAccessQuerier(File mdbFile, String userName, String password) {
        super(mdbFile, userName, password);
    }

    /**
     * MSAccessQuerier is not singleton
     * @param mdbFile
     * @return if mdbFile is not valid, it will return null
     */
    public static MSAccessQuerier getMSAccessQuerierInstance (File mdbFile, String userName, String password) {
        MSAccessQuerier instance = new MSAccessQuerier(mdbFile, userName, password);
        return instance;
    }

    /**
     *
     * @param sqlCommand
     * @return could be null if there is no connection
     */
    public synchronized ResultSet execute(DatabaseSelectCommand sqlCommand) throws SQLException {
        if (isStarted()){
            if (conn == null){
                conn = createConnectionInstance();
            }
            if (sqlCommand != null){
                Statement stmt = conn.createStatement();
                return stmt.executeQuery(sqlCommand.getSQLString());
            }else{
                throw new SQLException("sqlCommand cannot be NULL.");
            }
        }else{
            throw new SQLException("MSAccessQuerier is not started yet");
        }
    }

}
