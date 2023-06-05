/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_InstantMessageTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:21:04 AM
 */
public class PB_InstantMessageTable {

    public final static String DropTable = "drop table " + Schema.TableName;
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.MESSAGE_UUID + "             VARCHAR(100) NOT NULL," +
            "    " + Schema.MESSAGE + "                VARCHAR(1000), " +
            "    " + Schema.MESSAGE_TIMESTAMP + "      TIMESTAMP, " +
            "    " + Schema.FROM_SCREEN_NAME + "       VARCHAR(50), " +
            "    " + Schema.TO_SCREEN_NAME + "         VARCHAR(50), " +
            "    " + Schema.OUTGOING + "               SMALLINT, " +   //YES OR NO
            "    " + Schema.IM_SERVER_TYPE + "         VARCHAR(50), " +
            "    " + Schema.LAST_UPDATE + "            TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.UserOwner +
            ", " + Schema.MESSAGE_UUID + ") " +
            ")";
    public final static String SelectAllMessages = "select * from " + Schema.TableName;
    public final static String SelectHistoricalMessages =
            "select " + Schema.TableName + ".* from " + Schema.TableName + " " +
            "where " + Schema.TableName + "." + Schema.MESSAGE_TIMESTAMP + " > ? " +
            "and " + Schema.TableName + "." + Schema.MESSAGE_TIMESTAMP + " < ? " +
            "and " + Schema.TableName + "." + Schema.UserOwner + " = ? " +
            "order by " + Schema.MESSAGE_TIMESTAMP;
    
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.UserOwner + ", " +
            Schema.MESSAGE_UUID + ", " +
            Schema.MESSAGE + ", " +
            Schema.MESSAGE_TIMESTAMP + ", " +
            Schema.FROM_SCREEN_NAME + ", " +
            Schema.TO_SCREEN_NAME + ", " +
            Schema.OUTGOING + ", " +
            Schema.IM_SERVER_TYPE + ", " +
            Schema.LAST_UPDATE + ") " +
            "values (?,?,?,?,?,?,?,?,?)";

    public final static String RecycleByDays = "delete from " + Schema.TableName +
            " where " + Schema.LAST_UPDATE + " < ? ";

    public static enum Schema {
        TableName("APP.PB_InstantMessage"),

        UserOwner("UserOwner"),
        MESSAGE_UUID("MESSAGE_ID"),
        MESSAGE("MESSAGE"),
        MESSAGE_TIMESTAMP("MESSAGE_TIMESTAMP"),
        FROM_SCREEN_NAME("FROM_SCREEN_NAME"),
        TO_SCREEN_NAME("TO_SCREEN_NAME"),
        OUTGOING("OUTGOING"),
        IM_SERVER_TYPE("IM_SERVER_TYPE"),
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
