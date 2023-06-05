/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_SystemFrameSettingsTable.java
 * <p>
 * it is table-abstraction for every JFrame instance loaded in the system.
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 9, 2010, 6:53:56 AM
 */
public class PB_SystemFrameSettingsTable {

    public final static String AddRecycleDaysColumn = "ALTER TABLE " + Schema.TableName + " " +
                                                      "ADD COLUMN " + Schema.QuoteRecycleDays + " INTEGER NOT NULL " +
                                                      "DEFAULT 7";

    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
                "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
                "    " + Schema.FrameUniqueName + "     VARCHAR(100) NOT NULL," +
                "    " + Schema.NaturalGas_LAF + "     VARCHAR(100)," +
                "    " + Schema.CrudeOil_LAF + "     VARCHAR(100)," +
                "    " + Schema.FrameLayout + "        SMALLINT," +
                "    " + Schema.FrameStyle + "        SMALLINT, " +
                "    " + Schema.FrameWidth + "        DOUBLE, " +
                "    " + Schema.FrameHeight + "        DOUBLE, " +
                "    " + Schema.FrameLocationX + "        DOUBLE, " +
                "    " + Schema.FrameLocationY + "        DOUBLE, " +
                "    PRIMARY KEY (" + Schema.UserOwner +
                ", " + Schema.FrameUniqueName + ") " +
                ")";
    public final static String SelectSpecificRecord =  "select * from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.FrameUniqueName + " = ?";
    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                                                " (" + Schema.UserOwner + ", " +
                                                       Schema.FrameUniqueName + ", " +
                                                       Schema.NaturalGas_LAF + ", " +
                                                       Schema.CrudeOil_LAF + ", " +
                                                       Schema.FrameLayout + ", " +
                                                       Schema.FrameStyle + ", " +
                                                       Schema.FrameWidth + ", " +
                                                       Schema.FrameHeight + ", " +
                                                       Schema.FrameLocationX + ", " +
                                                       Schema.FrameLocationY + ", " +
                                                       Schema.QuoteRecycleDays + ") "+
                                                 "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public final static String DeleteSpecificRecord = "delete from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.FrameUniqueName + " = ?";
    public static enum Schema {

        TableName("APP.PB_SystemFrameSettings"),

        UserOwner("UserOwner"), //who own this settings
        FrameUniqueName("FrameInstanceName"),     //frame instance id
        NaturalGas_LAF("NaturalGas_LAF"),
        CrudeOil_LAF("CrudeOil_LAF"),
        FrameLayout("FrameLayout"), //0 = Vertical; 1 = Horizontal
        FrameStyle("FrameStyle"), //0 = Docked; 1 = Floating
        FrameWidth("FrameWidth"),
        FrameHeight("FrameHeight"),
        FrameLocationX("FrameLocationX"),
        FrameLocationY("FrameLocationY"),
        QuoteRecycleDays("QuoteRecycleDays");

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
