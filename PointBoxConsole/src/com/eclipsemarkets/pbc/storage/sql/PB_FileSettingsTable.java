/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_FileSettingsTable.java
 * <p>
 * a collection of pricing settings file
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 1:06:14 PM
 */
public class PB_FileSettingsTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
                "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
                "    " + Schema.FileID + "     VARCHAR(50) NOT NULL," +
                "    " + Schema.FilePath + "        VARCHAR(1000), " +
                "    PRIMARY KEY (" + Schema.UserOwner +
                ", " + Schema.FileID + ") " +
            ")";
    public final static String SelectRecordsByOwner =  "select * from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ?";
    public final static String SelectSpecificRecord =  "select * from " +
                                                        Schema.TableName + " where " +
                                                        Schema.FileID + " = ? AND " +
                                                        Schema.UserOwner + " = ?";
    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                                                " (" + Schema.UserOwner + ", " +
                                                Schema.FileID + ", " +
                                                Schema.FilePath + ") "+
                                                "values (?, ?, ?)";
    public final static String DeleteRecordsByOwner = "delete from " +
                                                    Schema.TableName + " where " +
                                                    Schema.UserOwner + " = ?";
    public final static String DeleteSpecificRecord = "delete from " +
                                                    Schema.TableName + " where " +
                                                    Schema.UserOwner + " = ? AND " +
                                                    Schema.FileID + " = ?";
    public static enum Schema {

        TableName("APP.PB_PointBoxFileSettings"),

        UserOwner("UserOwner"),
        FileID("FileID"),
        FilePath("FilePath");

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
