/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

import com.eclipsemarkets.pbc.storage.PbcDatabaseInstance;

/**
 * PB_QuoteLegTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:25:09 AM
 */
public class PB_QuoteLegTable {

    public final static String AddUserOwnerColumn = "ALTER TABLE " + Schema.TableName + " " +
                                                  "ADD COLUMN " + Schema.UserOwner + " VARCHAR(100) NOT NULL " +
                                                  "DEFAULT '" + PbcDatabaseInstance.DefaultEmsUser + "'";

    public final static String RecycleByDays = "delete from " + Schema.TableName +
            " where " + Schema.LAST_UPDATE + " < ? ";

    public final static String DropTable = "drop table " + Schema.TableName;
    
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.QUOTE_UUID + "             VARCHAR(100) NOT NULL, " +
            "    " + Schema.QUOTE_LEG_ID + "           INTEGER NOT NULL," +
            "    " + Schema.STRUCTURE + "              VARCHAR(100), " +
            "    " + Schema.CONTRACT_START + "         DATE, " +
            "    " + Schema.CONTRACT_END + "           DATE, " +
            "    " + Schema.CROSS + "                  NUMERIC(10,5), " +
            "    " + Schema.LOCATION + "               VARCHAR(100), " +
            "    " + Schema.LAST_UPDATE + "            TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.QUOTE_UUID + ", " + Schema.QUOTE_LEG_ID + ") " +
            ")";
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.QUOTE_UUID + ", " +
            Schema.QUOTE_LEG_ID + ", " +
            Schema.STRUCTURE + ", " +
            Schema.CONTRACT_START + ", " +
            Schema.CONTRACT_END + ", " +
            Schema.CROSS + ", " +
            Schema.LOCATION + ", " +
            Schema.LAST_UPDATE + ", " +
            Schema.UserOwner + ") " +
            "values (?,?,?,?,?,?,?,?,?)";
    public final static String SelectSpecificQuoteLeg = "select * from " +
            Schema.TableName + " where " +
            Schema.QUOTE_UUID + " = ? AND " +
            Schema.QUOTE_LEG_ID + " = ?";
    public enum Schema {
        TableName("APP.PB_QuoteLeg"),
    
        UserOwner("UserOwner"),
        QUOTE_UUID("QUOTE_ID"),
        QUOTE_LEG_ID("QUOTE_LEG_ID"),
        STRUCTURE("STRUCTURE"),
        CONTRACT_START("CONTRACT_START"),
        CONTRACT_END("CONTRACT_END"),
        CROSS("CROSS"),
        LOCATION("LOCATION"),
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
