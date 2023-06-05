/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_ViewerTablePanelTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 8, 2010, 4:02:33 PM
 */
public class PB_ViewerTablePanelTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.ViewerUniqueTabName + "     VARCHAR(1000) NOT NULL," +
            "    " + Schema.FontFamily + "     VARCHAR(255)," +
            "    " + Schema.FontStyle + "     INTEGER," +
            "    " + Schema.FontSize + "     INTEGER," +
            "    " + Schema.GeneralColor + "     INTEGER," +
            "    " + Schema.PAForegroundRGB + "     INTEGER," +
            "    " + Schema.PABackgroundRGB + "     INTEGER," +
            "    " + Schema.PBForegroundRGB + "     INTEGER," +
            "    " + Schema.PBBackgroundRGB + "     INTEGER," +
            "    " + Schema.BPAForegroundRGB + "     INTEGER," +
            "    " + Schema.BPABackgroundRGB + "     INTEGER," +
            "    " + Schema.PbimQtForegroundRGB + "     INTEGER," +
            "    " + Schema.PbimQtBackgroundRGB + "     INTEGER," +
            "    " + Schema.SkippedQtForegroundRGB + "     INTEGER," +
            "    " + Schema.SkippedQtBackgroundRGB + "     INTEGER," +
            "    " + Schema.QtForegroundRGB + "     INTEGER," +
            "    " + Schema.QtBackgroundRGB + "     INTEGER," +
            "    " + Schema.MsgForegroundRGB + "     INTEGER," +
            "    " + Schema.MsgBackgroundRGB + "     INTEGER," +
            "    " + Schema.OutgoingForegroundRGB + "     INTEGER," +
            "    " + Schema.OutgoingBackgroundRGB + "     INTEGER," +
            "    " + Schema.SelectedRowForegroundRGB + "     INTEGER," +
            "    " + Schema.SelectedRowBackgroundRGB + "     INTEGER," +
            "    " + Schema.LatestRowForegroundRGB + "     INTEGER," +
            "    " + Schema.LatestRowBackgroundRGB + "     INTEGER," +
            "    PRIMARY KEY (" + Schema.UserOwner + ", " + Schema.ViewerUniqueTabName + ") " +
            ")";
    
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.UserOwner + ", " +
            Schema.ViewerUniqueTabName + ", " +
            Schema.FontFamily + ", " +
            Schema.FontStyle + ", " +
            Schema.FontSize + ", " +
            Schema.GeneralColor + ", " +
            Schema.PAForegroundRGB + ", " +
            Schema.PABackgroundRGB + ", "+
            Schema.PBForegroundRGB + ", " +
            Schema.PBBackgroundRGB + ", "+
            Schema.BPAForegroundRGB + ", " +
            Schema.BPABackgroundRGB + ", "+
            Schema.PbimQtForegroundRGB + ", " +
            Schema.PbimQtBackgroundRGB + ", "+
            Schema.SkippedQtForegroundRGB + ", " +
            Schema.SkippedQtBackgroundRGB + ", "+
            Schema.QtForegroundRGB + ", " +
            Schema.QtBackgroundRGB + ", "+
            Schema.MsgForegroundRGB + ", " +
            Schema.MsgBackgroundRGB + ", "+
            Schema.OutgoingForegroundRGB + ", " +
            Schema.OutgoingBackgroundRGB + ", " +
            Schema.SelectedRowForegroundRGB + ", " +
            Schema.SelectedRowBackgroundRGB + ", " +
            Schema.LatestRowForegroundRGB + ", "+
            Schema.LatestRowBackgroundRGB + ") "+
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public final static String SelectSpecificRecord =  "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.ViewerUniqueTabName + " = ?";

    public final static String SelectRecordsByOwner =  "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    public final static String DeleteSpecificRecord =  "delete from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.ViewerUniqueTabName + " = ?";

    public final static String DeleteAllRecords =  "delete from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    public static enum Schema {

        TableName("APP.PB_ViewerTablePanel"),

        UserOwner("UserOwner"), //who own this config
        ViewerUniqueTabName("ViewerUniqueTabName"),
        FontFamily("FontFamily"),   //String
        FontStyle("FontStyle"),   //int
        FontSize("FontSize"),   //int
        GeneralColor("GeneralColor"),
        PAForegroundRGB("PAForegroundRGB"), //int
        PABackgroundRGB("PABackgroundRGB"), //int
        PBForegroundRGB("PBForegroundRGB"), //int
        PBBackgroundRGB("PBBackgroundRGB"), //int
        BPAForegroundRGB("BPAForegroundRGB"), //int
        BPABackgroundRGB("BPABackgroundRGB"), //int
        PbimQtForegroundRGB("PbimQtForegroundRGB"), //int
        PbimQtBackgroundRGB("PbimQtBackgroundRGB"), //int
        SkippedQtForegroundRGB("SkippedQtForegroundRGB"), //int
        SkippedQtBackgroundRGB("SkippedQtBackgroundRGB"), //int
        QtForegroundRGB("QtForegroundRGB"), //int
        QtBackgroundRGB("QtBackgroundRGB"), //int
        MsgForegroundRGB("MsgForegroundRGB"), //int
        MsgBackgroundRGB("MsgBackgroundRGB"),
        OutgoingForegroundRGB("OutgoingForegroundRGB"),
        OutgoingBackgroundRGB("OutgoingBackgroundRGB"),
        SelectedRowForegroundRGB("SelectedRowForegroundRGB"),
        SelectedRowBackgroundRGB("SelectedRowBackgroundRGB"),
        LatestRowForegroundRGB("LatestRowForegroundRGB"),
        LatestRowBackgroundRGB("LatestRowBackgroundRGB"); //int

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
