/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_AutoPricerSettingsTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 10, 2010, 6:13:48 PM
 */
public class PB_AutoPricerSettingsTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
                "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
                "    " + Schema.RecordUUID + "     VARCHAR(100) NOT NULL," +
                "    " + Schema.RefreshLatestQuotes + "     INTEGER," +
                "    " + Schema.RefreshDelay + "        BIGINT, " +
                "    " + Schema.RefreshFrequency + "        BIGINT, " +
                "    " + Schema.RefreshAllQuotes + "        SMALLINT, " +
                "    " + Schema.StopRefreshingPrice + "     SMALLINT, " +
                "    PRIMARY KEY (" + Schema.UserOwner +
                ", " + Schema.RecordUUID + ") " +
                ")";
    public final static String SelectSpecificRecord =  "select * from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.RecordUUID + " = ?";
    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                                                " (" + Schema.UserOwner + ", " +
                                                       Schema.RecordUUID + ", " +
                                                       Schema.RefreshLatestQuotes + ", " +
                                                       Schema.RefreshDelay + ", " +
                                                       Schema.RefreshFrequency + ", " +
                                                       Schema.RefreshAllQuotes + ", " +
                                                       Schema.StopRefreshingPrice + ") "+
                                                 "values (?, ?, ?, ?, ?, ?, ?)";
    public final static String DeleteSpecificRecord = "delete from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.RecordUUID + " = ?";

    public static enum Schema {

        TableName("APP.PB_AutoPricerSettings"),

        UserOwner("UserOwner"),
        RecordUUID("RecordId"),
        RefreshLatestQuotes("RefreshLatestQuotes"),
        RefreshDelay("RefreshDelay"),
        RefreshFrequency("RefreshFrequency"),
        RefreshAllQuotes("RefreshAllQuotes"),
        StopRefreshingPrice("StopRefreshingPrice");

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
