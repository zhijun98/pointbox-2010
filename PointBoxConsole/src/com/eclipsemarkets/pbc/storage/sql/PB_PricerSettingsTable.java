/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_PricerSettingsTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 10, 2010, 10:34:33 PM
 */
public class PB_PricerSettingsTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
                "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
                "    " + Schema.RecordUUID + "     VARCHAR(100) NOT NULL," +
                "    " + Schema.FiveYearLimit + "        SMALLINT, " +
                "    PRIMARY KEY (" + Schema.UserOwner +
                ", " + Schema.RecordUUID + ") " +
                ")";
    public final static String AddTValueAtExpColumn = "ALTER TABLE " + Schema.TableName + " " +
                                                  "ADD COLUMN " + Schema.TValueAtExp + " INT NOT NULL " +
                                                  "DEFAULT 0";
    
    public final static String SelectSpecificRecord =  "select * from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.RecordUUID + " = ?";
    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                                                " (" + Schema.UserOwner + ", " +
                                                       Schema.RecordUUID + ", " +
                                                       Schema.FiveYearLimit + ", " +
                                                       Schema.TValueAtExp + ") "+
                                                 "values (?, ?, ?, ?)";
    public final static String DeleteSpecificRecord = "delete from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.RecordUUID + " = ?";

    public static enum Schema {

        TableName("APP.PB_PricerSettings"),

        UserOwner("UserOwner"),
        RecordUUID("RecordId"),
        TValueAtExp("TValueAtExp"),
        FiveYearLimit("FiveYearLimit");
        
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
