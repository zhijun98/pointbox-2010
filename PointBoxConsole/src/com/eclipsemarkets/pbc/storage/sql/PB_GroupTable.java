/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_GroupTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:10:48 AM
 */
public class PB_GroupTable {

    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.GROUP_UNIQUE_NAME + " VARCHAR(50) NOT NULL," +
            "    " + Schema.IM_SERVER_TYPE + "      VARCHAR(100), " +
            "    " + Schema.GROUP_NAME + "      VARCHAR(100), " +
            "    " + Schema.GROUP_DESCRIPTION + "      VARCHAR(1000), " +
            "    " + Schema.LAST_UPDATE + "            TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.UserOwner +
            ", " + Schema.GROUP_UNIQUE_NAME + ") " +
            ")";
    
    public final static String SelectAllRecords =
            "select * from " + Schema.TableName;
    
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.UserOwner + ", " +
            Schema.GROUP_UNIQUE_NAME + ", " +
            Schema.IM_SERVER_TYPE + ", " +
            Schema.GROUP_NAME + ", " +
            Schema.GROUP_DESCRIPTION + ", " +
            Schema.LAST_UPDATE + ") " +
            " values (?,?,?,?,?,?)";

    public final static String SelectSpecificRecord =
            "select * from " + Schema.TableName +
            " where " + Schema.UserOwner + " = ? AND " +
            Schema.GROUP_UNIQUE_NAME + " = ?";

    public final static String DeleteSpecificRecord =
            "delete from " + Schema.TableName +
            " where " + Schema.UserOwner + " = ? AND " +
            Schema.GROUP_UNIQUE_NAME + " = ?";

    public final static String DeleteSpecificRecordByOwner =
            "delete from " + Schema.TableName +
            " where " + Schema.UserOwner + " = ?";
    
    
    public static enum Schema {
        TableName("APP.PB_DistributionGroup"),

        UserOwner("UserOwner"),
        GROUP_UNIQUE_NAME("GROUP_UNIQUE_NAME"),
        IM_SERVER_TYPE("IM_SERVER_TYPE"),
        GROUP_NAME("GROUP_NAME"),
        GROUP_DESCRIPTION("GROUP_DESCRIPTION"),
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
