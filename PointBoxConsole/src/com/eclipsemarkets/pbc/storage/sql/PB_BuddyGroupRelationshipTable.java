/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_BuddyGroupRelationshipTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:19:21 AM
 */
public class PB_BuddyGroupRelationshipTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.GROUP_UNIQUE_NAME + "            VARCHAR(50) NOT NULL," +
            "    " + Schema.BUDDY_UNIQUE_NAME + "            VARCHAR(50) NOT NULL, " +
            "    " + Schema.LAST_UPDATE + "            TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.UserOwner + ", " +
                                  Schema.GROUP_UNIQUE_NAME + ", " +
                                  Schema.BUDDY_UNIQUE_NAME + ") " +
            ")";
    
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.UserOwner + ", " +
            Schema.GROUP_UNIQUE_NAME + ", " +
            Schema.BUDDY_UNIQUE_NAME + ", " +
            Schema.LAST_UPDATE + ") " +
            "values (?,?,?,?)";

    public final static String DeleteSpecificRecord =
            "delete from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.GROUP_UNIQUE_NAME + " = ? AND " +
            Schema.BUDDY_UNIQUE_NAME + " = ?";

    public final static String DeleteSpecificRecordByOwnerGroup =
            "delete from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.GROUP_UNIQUE_NAME + " = ?";

    public final static String DeleteSpecificRecordByOwner =
            "delete from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    public final static String SelectSpecificRecord = "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.GROUP_UNIQUE_NAME + " = ? AND " +
            Schema.BUDDY_UNIQUE_NAME + " = ?";

    public final static String SelectSpecificRecordByGroup = "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.GROUP_UNIQUE_NAME + " = ?";

    public final static String SelectSpecificRecordByOwner = "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    public final static String SelectJoinedGroupsBuddies = "select " +
            PB_GroupTable.Schema.TableName + "." + PB_GroupTable.Schema.GROUP_UNIQUE_NAME + ", " +
            PB_GroupTable.Schema.TableName + "." + PB_GroupTable.Schema.GROUP_NAME + ", " +
            PB_GroupTable.Schema.TableName + "." + PB_GroupTable.Schema.GROUP_DESCRIPTION + ", " +
            PB_BuddyTable.Schema.TableName + "." + PB_BuddyTable.Schema.IM_SERVER_TYPE + ", " +
            PB_BuddyTable.Schema.TableName + "." + PB_BuddyTable.Schema.BUDDY_UNIQUE_NAME + ", " +
            PB_BuddyTable.Schema.TableName + "." + PB_BuddyTable.Schema.BUDDY_SCREEN_NAME + ", " +
            PB_BuddyTable.Schema.TableName + "." + PB_BuddyTable.Schema.NICKNAME + ", " +
            PB_BuddyTable.Schema.TableName + "." + PB_BuddyTable.Schema.PASSWORD + ", " +
            PB_BuddyTable.Schema.TableName + "." + PB_BuddyTable.Schema.PROFILE_ID + " from "+
            PB_BuddyTable.Schema.TableName + ", " + PB_GroupTable.Schema.TableName + ", " + Schema.TableName + " where " +
            PB_GroupTable.Schema.TableName + "." + PB_GroupTable.Schema.GROUP_UNIQUE_NAME + " = " + 
            Schema.TableName + "." + Schema.GROUP_UNIQUE_NAME + " and " +
            PB_BuddyTable.Schema.TableName + "." + PB_BuddyTable.Schema.BUDDY_UNIQUE_NAME + " = " +
            Schema.TableName + "." + Schema.BUDDY_UNIQUE_NAME + " and " +
            Schema.TableName + "." + Schema.UserOwner + " = ?";
    public static enum Schema {
        TableName("APP.PB_BuddyGroupRelationship"),

        UserOwner("UserOwner"),
        GROUP_UNIQUE_NAME("GROUP_UNIQUE_NAME"),
        BUDDY_UNIQUE_NAME("BUDDY_UNIQUE_NAME"),
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
