/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_MermSettingsTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 1:09:42 PM
 */
public class PB_MermSettingsTable {
    public final static String CreateNewTable=
            "create table " + Schema.TableName + " (" +
                "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
                "    " + Schema.Subject + "     VARCHAR(100) NOT NULL," +
                "    " + Schema.Descritpion + "        VARCHAR(1000), " +
                "    PRIMARY KEY (" + Schema.UserOwner +
                ", " + Schema.Subject + ") " +
            ")";
    public final static String SelectSpecificRecord =  "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.Subject + " = ?";
    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                                                " (" + Schema.UserOwner + ", " +
                                                Schema.Subject + ", " +
                                                Schema.Descritpion + ") "+
                                                "values (?, ?, ?)";
    public final static String DeleteSpecificRecord= "delete from " +
                                                    Schema.TableName + " where " +
                                                    Schema.UserOwner + " = ? AND " +
                                                    Schema.Subject + " = ?";

    public static enum Schema {

        TableName("APP.PB_MermSettings"),

        UserOwner("UserOwner"),     //who own this settings
        Subject("Subject"),          //the information subject, e.g., MS-Path or anything that can be described by
        Descritpion("Descritpion");

        private String term;
        Schema(String term){
            this.term = term;
        }

        @Override
        public String toString() {
            return term;
        }
    }

    private PB_MermSettingsTable() {
    }
}
