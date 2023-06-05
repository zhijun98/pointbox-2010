/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.eclipsemarkets.global.NIOGlobal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe
 * @author Zhijun Zhang
 */
abstract class MSAccessSQLEngine implements IDatabaseSQLEngine {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(MSAccessSQLEngine.class.getName());
    }

    protected static final String msAccessDBURLPrefix = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
    protected static final String msAccessDBURLSuffix = ";DriverID=22;READONLY=true}";

    protected Driver driver;
    protected Connection conn;
    protected String userName;
    protected String password;
    protected boolean started;

    private File mdbFile;
    
    public MSAccessSQLEngine() {
        started = false;
    }

    MSAccessSQLEngine(File mdbFile, String userName, String password) {
        started = false;
        if (NIOGlobal.isValidFile(mdbFile, ".mdb")){
            startSQLEngine(mdbFile, userName, password);
        }
    }

    public synchronized void setMdbFile(File mdb) {
        File rollback = mdbFile;
        try {
            if (!conn.isClosed()){
                conn.close();
            }
            mdbFile = mdb;
            conn = createConnectionInstance();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            mdbFile = rollback;
        }
    }

    void shutdown() {
        if (conn == null){
            return;
        }
        try {
            if (!conn.isClosed()){
                conn.close();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized File getMdbFile() {
        return mdbFile;
    }

    public synchronized boolean isStarted() {
        return started;
    }

    public synchronized String getDatabaseLocation() {
        if (isStarted()){
            return mdbFile.getAbsolutePath();
        }else{
            return "";
        }
    }

    public synchronized void startSQLEngine(File mdbFile, String userName, String password) {
        if (!isStarted()){
            try {
                if (NIOGlobal.isValidFile(mdbFile, ".mdb")){
                    this.mdbFile = mdbFile;
                    if (loadMSAccessDriver()){  //driver
                        //conn, userName, password
                        conn = createConnectionInstance();
                        this.mdbFile = mdbFile;
                        this.userName = userName;
                        this.password = password;
                        started = true;
                    }else{
                        unloadMSAccessDriver();
                        throw new Exception("MSAccess Driver cannot be registered");
                    }
                }else{
                    throw new Exception("MSAccess file is not valid: " + ((mdbFile == null)? "NULL MDB PATH" : mdbFile.getPath()));
                }
            } catch(Exception ex) {

                logger.log(Level.SEVERE, ex.getMessage(), ex);

                driver = null;
                conn = null;
                this.mdbFile = null;
                this.userName = null;
                this.password = null;
                started = false;
            }
        }
    }

    private synchronized boolean loadMSAccessDriver(){
        boolean result = false;
        try {
            driver = (Driver) (Class.forName(DatabaseDriverNames.JDBC_ODBC_BRIDGE.toString()).newInstance());
            DriverManager.registerDriver(driver);
            result = true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            result = false;
        }
        return result;
    }

    private synchronized boolean unloadMSAccessDriver(){
        if (driver == null){
            return true;
        }
        boolean result = false;
        try {
            //unload driver
            DriverManager.deregisterDriver(driver);
            driver = null;
            result = true;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            result = false;
        }
        return result;
    }

    public synchronized void stopSQLEngine() {
        if (isStarted()){
            try {
                destroyConnection();
            } catch (SQLException ex) {
                conn = null;
            }
            this.mdbFile = null;
            this.userName = null;
            this.password = null;
            started = false;
            unloadMSAccessDriver();
        }
    }

    /**
     * create a fresh connection instance from DiverManager
     * @return
     * @throws SQLException
     */
    synchronized Connection createConnectionInstance() throws SQLException{
        return DriverManager.getConnection(msAccessDBURLPrefix + mdbFile.getAbsolutePath() + msAccessDBURLSuffix, "", "");
    }

    /**
     * Close and nullify the current connection for this engine
     * @throws SQLException
     */
    synchronized void destroyConnection() throws SQLException{
        if (conn == null){
            return;
        }
        if (!conn.isClosed()){
            conn.close();
        }
        conn = null;
    }
}
