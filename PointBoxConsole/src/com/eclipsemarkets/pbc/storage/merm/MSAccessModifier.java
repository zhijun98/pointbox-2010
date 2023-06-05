/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MSAccessModifier is not only "Thread-safe" but also "singleton"
 * @author Zhijun Zhang
 */
class MSAccessModifier extends MSAccessSQLEngine implements IDatabaseModifer{

    private static MSAccessModifier self;
    static {
        self = null;
    }

    private MSAccessModifier(File mdbFile, String userName, String password) {
        super(mdbFile, userName, password);
    }

    /**
     *
     * @param mdbFile
     * @return if mdbFile is not valid, it will return null
     */
    public static MSAccessModifier getMSAccessModifierSingleton(File mdbFile, String userName, String password) {
        if (self == null){
            self = new MSAccessModifier(mdbFile, userName, password);
        }
        return self;
    }

    /**
     *
     * @param sqlCommand
     * @throws SQLException
     */
    public synchronized int execute(DatabaseInsertCommand sqlCommand, long timeout) throws SQLException{
        return executeModificationSQL(sqlCommand, timeout);
    }

    public synchronized int execute(DatabaseDeleteCommand sqlCommand, long timeout) throws SQLException {
        return executeModificationSQL(sqlCommand, timeout);
    }

    public synchronized int execute(DatabaseUpdateCommand sqlCommand, long timeout) throws SQLException {
        return executeModificationSQL(sqlCommand, timeout);
    }

    private synchronized int executeModificationSQL(IDatabaseSQLCommand sqlCommand, long timeout) throws SQLException{
        if (isStarted()){
            if (conn == null){
                conn = createConnectionInstance();
            }
            if (sqlCommand != null){
                Statement stmt = null;
                int counter = 0;
                while(true){
                    try{
                        stmt = conn.createStatement();
                        return stmt.executeUpdate(sqlCommand.getSQLString());
                    }catch (SQLException ex){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException exx) {
                            return 0;
                        }
                        counter++;
                        if ((counter * 1000) > timeout){
                            throw ex;
                        }
                    }
                }//while
            }else{
                throw new SQLException("sqlCommand cannot be NULL.");
            }
        }else{
            throw new SQLException("MSAccessModifier is not started.");
        }
    }

}
