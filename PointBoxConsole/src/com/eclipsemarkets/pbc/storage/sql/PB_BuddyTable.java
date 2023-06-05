/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_BuddyTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:15:30 AM
 */
public class PB_BuddyTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.BUDDY_UNIQUE_NAME + "  VARCHAR(50) NOT NULL, " +
            "    " + Schema.IM_SERVER_TYPE + "     VARCHAR(50), " +
            "    " + Schema.BUDDY_SCREEN_NAME + "  VARCHAR(50), " +
            "    " + Schema.PASSWORD + "               VARCHAR(50), " +
            "    " + Schema.NICKNAME + "               VARCHAR(50), " +
            "    " + Schema.PROFILE_ID + "                VARCHAR(50), " +
            "    " + Schema.LAST_UPDATE + "            TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.UserOwner +
            ", " + Schema.BUDDY_UNIQUE_NAME + ") " +
            ")";

    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.UserOwner + ", " +
            Schema.BUDDY_UNIQUE_NAME + ", " +
            Schema.IM_SERVER_TYPE + ", " +
            Schema.BUDDY_SCREEN_NAME + ", " +
            Schema.PASSWORD + ", " +
            Schema.NICKNAME + ", " +
            Schema.PROFILE_ID + ", " +
            Schema.LAST_UPDATE + ") " +
            "values (?,?,?,?,?,?,?,?)";
    
    public final static String SelectSpecificRecord =
            "select * from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.BUDDY_UNIQUE_NAME + " = ?";

    public final static String DeleteSpecificRecord =
            "delete from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.BUDDY_UNIQUE_NAME + " = ?";

    public final static String DeleteSpecificRecordByOwner =
            "delete from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    public static enum Schema {
        TableName("APP.PB_Buddy"),
        
        UserOwner("UserOwner"),
        BUDDY_UNIQUE_NAME("BUDDY_UNIQUE_NAME"),
        IM_SERVER_TYPE("IM_SERVER_TYPE"),
        BUDDY_SCREEN_NAME("BUDDY_SCREEN_NAME"),
        PASSWORD("PASSWORD"),
        NICKNAME("NICKNAME"),
        PROFILE_ID("PROFILE_ID"),
        LAST_UPDATE("LAST_UPDATE");

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
