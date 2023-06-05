/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_IMServerTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:08:40 AM
 */
public class PB_IMServerTable {
    
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.IM_SERVER_TYPE + "     VARCHAR(50) NOT NULL," +
            "    " + Schema.DESCRIPTION + "        VARCHAR(1000), " +
            "    " + Schema.LAST_UPDATE + "        TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.UserOwner +
            ", " + Schema.IM_SERVER_TYPE + ") " +
            ")";

    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                " (" + Schema.UserOwner + ", " +
                Schema.IM_SERVER_TYPE + ", " +
                Schema.DESCRIPTION + ", " +
                Schema.LAST_UPDATE + ") values (?, ?, ?, ?)";

    public static enum Schema {
        TableName("APP.PB_IMServer"),

        UserOwner("UserOwner"),
        IM_SERVER_TYPE("IM_SERVER_TYPE"),
        DESCRIPTION("DESCRIPTION"),
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
