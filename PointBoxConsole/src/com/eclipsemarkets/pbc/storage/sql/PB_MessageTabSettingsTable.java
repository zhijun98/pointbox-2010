/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_MessageTabSettingsTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Aug 27, 2010, 1:07:10 PM
 */
public class PB_MessageTabSettingsTable {

    public final static String DropTable = "drop table " + Schema.TableName;

    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.MessageTabID + "     VARCHAR(1000) NOT NULL," +
            "    " + Schema.MyFontFamily + "     VARCHAR(255)," +
            "    " + Schema.MyFontStyle + "     INTEGER," +
            "    " + Schema.MyFontSize + "     INTEGER," +
            "    " + Schema.MyForeground + "     INTEGER," +
            "    " + Schema.BuddyFontFamily + "     VARCHAR(255)," +
            "    " + Schema.BuddyFontStyle + "     INTEGER," +
            "    " + Schema.BuddyFontSize + "     INTEGER," +
            "    " + Schema.BuddyForeground + "     INTEGER," +
            "    " + Schema.DisplayTimestamp + "     SMALLINT," +
            "    " + Schema.DisplayPrices + "     SMALLINT," +
            "    PRIMARY KEY (" + Schema.UserOwner + ", " + Schema.MessageTabID + ") " +
            ")";

    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.UserOwner + ", " +
            Schema.MessageTabID + ", " +
            Schema.MyFontFamily + ", " +
            Schema.MyFontStyle + ", " +
            Schema.MyFontSize + ", " +
            Schema.MyForeground + ", " +
            Schema.BuddyFontFamily + ", " +
            Schema.BuddyFontStyle + ", " +
            Schema.BuddyFontSize + ", " +
            Schema.BuddyForeground + ", " +
            Schema.DisplayTimestamp + ", " +
            Schema.DisplayPrices + ") "+
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public final static String SelectSpecificRecord =  "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.MessageTabID + " = ?";

    public final static String SelectRecordsByOwner =  "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    public final static String DeleteRecordsByOwner =  "delete from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    public final static String DeleteSpecificRecord =  "delete from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.MessageTabID + " = ?";


    public static enum Schema {
        TableName("APP.PB_MessageTabSettings"),
        UserOwner("UserOwner"),
        MessageTabID("MessageTabID"),
        MyFontFamily("MyFontFamily"),
        MyFontStyle("MyFontStyle"),
        MyFontSize("MyFontSize"),
        MyForeground("MyForeground"),
        BuddyFontFamily("BuddyFontFamily"),
        BuddyFontStyle("BuddyFontStyle"),
        BuddyFontSize("BuddyFontSize"),
        BuddyForeground("BuddyForeground"),
        DisplayTimestamp("Timestamp"),
        DisplayPrices("DisplayPrices");

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
