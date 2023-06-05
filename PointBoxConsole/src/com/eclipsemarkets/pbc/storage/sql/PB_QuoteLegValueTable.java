/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

import com.eclipsemarkets.pbc.storage.PbcDatabaseInstance;

/**
 * PB_QuoteLegValueTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:26:36 AM
 */
public class PB_QuoteLegValueTable {

    public final static String AddUserOwnerColumn = "ALTER TABLE " + Schema.TableName + " " +
                                                  "ADD COLUMN " + Schema.UserOwner + " VARCHAR(100) NOT NULL " +
                                                  "DEFAULT '" + PbcDatabaseInstance.DefaultEmsUser + "'";

    public final static String RecycleByDays = "delete from " + Schema.TableName +
            " where " + Schema.LAST_UPDATE + " < ? ";

    public final static String DropTable = "drop table " + Schema.TableName;
    
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.QUOTE_UUID + "       VARCHAR(100) NOT NULL, " +
            "    " + Schema.QUOTE_LEG_ID + "   INTEGER NOT NULL," +
            "    " + Schema.VALUE_INDEX + "    INTEGER NOT NULL, " +   //e.g., strike_1's 1
            "    " + Schema.STRIKE + "         NUMERIC(10,5), " +
            "    " + Schema.RATIO + "          NUMERIC(10,5), " +
            "    " + Schema.LAST_UPDATE + "    TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.QUOTE_UUID + ", " +
            Schema.QUOTE_LEG_ID + ", " + Schema.VALUE_INDEX + ") " +
            ")";
    public final static String SelectSpecificQuoteLegValues = "select * from " +
            Schema.TableName + " where " +
            Schema.QUOTE_UUID + " = ? and " +
            Schema.QUOTE_LEG_ID + " = ?";
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.QUOTE_UUID + ", " +
            Schema.QUOTE_LEG_ID + ", " +
            Schema.VALUE_INDEX + ", " +
            Schema.STRIKE + ", " +
            Schema.RATIO + ", " +
            Schema.LAST_UPDATE + ", " +
            Schema.UserOwner + ") " +
            "values (?,?,?,?,?,?,?)";
    public enum Schema {
        TableName("APP.PB_QuoteLegValue"),

        UserOwner("UserOwner"),
        QUOTE_UUID("QUOTE_ID"),
        QUOTE_LEG_ID("QUOTE_LEG_ID"),
        VALUE_INDEX("VALUE_INDEX"),
        STRIKE("STRIKE"),
        RATIO("RATIO"),
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
