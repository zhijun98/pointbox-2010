/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_UserProfileTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:34:25 AM
 */
public class PB_UserProfileTable {
    public final static String CreateNewTable =
            "create table "+Schema.TableName+" (" +
            "    " + Schema.USER_OWNER + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.PROFILE_UUID + "       VARCHAR(50) NOT NULL," +
            "    NICKNAME               VARCHAR(50), " +
            "    FIRST_NAME             VARCHAR(50), " +
            "    MIDDLE_NAME            VARCHAR(50), " +
            "    LAST_NAME              VARCHAR(50), " +
            "    BIRTHDAY               DATE, " +
            "    HOME_STREET            VARCHAR(1000), " +
            "    HOME_CITY              VARCHAR(100), " +
            "    HOME_STATE             VARCHAR(50), " +
            "    HOME_ZIP               VARCHAR(50), " +
            "    HOME_COUNTRY           VARCHAR(50), " +
            "    HOME_PHONE             VARCHAR(50), " +
            "    CELL_PHONE             VARCHAR(50), " +
            "    PAGER                  VARCHAR(50), " +
            "    PERSONAL_EMAIL         VARCHAR(150), " +
            "    PERSONAL_WEB_SITE      VARCHAR(1000), " +
            "    WORK_TITLE             VARCHAR(100), " +
            "    WORK_COMPANY           VARCHAR(150), " +
            "    WORK_STREET            VARCHAR(1000), " +
            "    WORK_CITY              VARCHAR(100), " +
            "    WORK_STATE             VARCHAR(50), " +
            "    WORK_ZIP               VARCHAR(50), " +
            "    WORK_COUNTRY           VARCHAR(50), " +
            "    WORK_PHONE             VARCHAR(50), " +
            "    WORK_FAX               VARCHAR(50), " +
            "    WORK_EMAIL             VARCHAR(150), " +
            "    WORK_WEB_SITE          VARCHAR(1000), " +
            "    NOTES                  VARCHAR(1000), " +
            "    LAST_UPDATE            TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.USER_OWNER +
            ", " + Schema.PROFILE_UUID + ") " +
            ")";



    /**
     * Query
     */
    public final static String QueryRecord =
            "select * from " + Schema.TableName
          + " where " + Schema.USER_OWNER + " =  ?  "
          + " and " + Schema.PROFILE_UUID + " =  ?"
          ;

    /**
     * Query all records
     */
    public final static String QueryAllRecord =
            "select * from " + Schema.TableName;



    /**
     * Insert
     */
    public final static String InsertRecord =
               "insert into " + Schema.TableName
             + " (" + Schema.USER_OWNER
             + ", " + Schema.PROFILE_UUID
             + ", " + Schema.FIRST_NAME
             + ", " + Schema.LAST_NAME
             + ", " + Schema.NICKNAME
             + ", " + Schema.NOTES
             + ", " + Schema.WORK_STREET
             + ", " + Schema.WORK_CITY
             + ", " + Schema.WORK_STATE
             + ", " + Schema.WORK_ZIP
             + ", " + Schema.WORK_PHONE
             + ", " + Schema.CELL_PHONE
             + ", " + Schema.WORK_FAX
             + ", " + Schema.PAGER
             + ", " + Schema.WORK_EMAIL
             + ") values ( "
             + " ?, ?, ?, ?, ?, "
             + " ?, ?, ?, ?, ?, "
             + " ?, ?, ?, ?, ? "
             + ")"
             ;

    /**
     * Update
     */
    public final static String UpdateRecord =
               "update " + Schema.TableName
            +  " set " +  Schema.FIRST_NAME +  " =  ?,"
            +  " set " +  Schema.LAST_NAME +  " =  ?,"
            +  " set " +  Schema.NICKNAME +  " =  ?,"
            +  " set " +  Schema.NOTES +  " =  ?,"
            +  " set " +  Schema.WORK_STREET +  " =  ?,"
            +  " set " +  Schema.WORK_CITY +  " =  ?,"
            +  " set " +  Schema.WORK_STATE +  " =  ?,"
            +  " set " +  Schema.WORK_ZIP +  " =  ?,"
            +  " set " +  Schema.WORK_PHONE +  " =  ?,"
            +  " set " +  Schema.CELL_PHONE +  " =  ?,"
            +  " set " +  Schema.WORK_FAX +  " =  ?,"
            +  " set " +  Schema.PAGER +  " =  ?,"
            +  " set " +  Schema.WORK_EMAIL +  " =  ?,"
            + " where " + Schema.USER_OWNER + " =  ?  "
            + " and " + Schema.PROFILE_UUID + " =  ?"
            ;


    /**
     * Delete
     */
    public final static String DeleteRecord =
             "delete from  " + Schema.TableName
           + " where " + Schema.USER_OWNER + " =  ?  "
           + " and " + Schema.PROFILE_UUID + " =  ?"
           ;



    public static enum Schema {
        TableName("APP.PB_UserProfile"),
        USER_OWNER("UserOwner"),
        PROFILE_UUID("PROFILE_ID"),
        NICKNAME("NICKNAME"),
        FIRST_NAME("FIRST_NAME"),
        MIDDLE_NAME("MIDDLE_NAME"),
        LAST_NAME("LAST_NAME"),
        NOTES("NOTES"),
        WORK_STREET("WORK_STREET"),
        WORK_CITY("WORK_CITY"),
        WORK_STATE("WORK_STATE"),
        WORK_ZIP("WORK_ZIP"),
        WORK_PHONE("WORK_PHONE"),
        CELL_PHONE("CELL_PHONE"),
        WORK_FAX("WORK_FAX"),
        PAGER("PAGER"),
        WORK_EMAIL("WORK_EMAIL"),
        BIRTHDAY("BIRTHDAY"),
        LAST_UPDATE("LAST_UPDATE");

      private Schema() {

      }


        private String term;
        Schema(String term){
            this.term = term;
        }

        @Override
        public String toString() {
            return term;
        }
    }
}
