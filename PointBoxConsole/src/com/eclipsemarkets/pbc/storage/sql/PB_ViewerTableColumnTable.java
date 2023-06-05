/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_ViewerTableColumnTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 1:04:52 PM
 */
public class PB_ViewerTableColumnTable {
    
    public final static String DropTable = "drop table " + Schema.TableName;

    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.viewerTabName + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.identifier + "     VARCHAR(100) NOT NULL," +
            "    " + Schema.headerValue + "        VARCHAR(50), " +
            "    " + Schema.width + "        INTEGER, " +
            "    " + Schema.position + "        INTEGER, " +
            "    " + Schema.resizable + "        SMALLINT, " +
            "    " + Schema.visible + "        SMALLINT, " +
            "    PRIMARY KEY (" + Schema.UserOwner +
            ", " + Schema.identifier +
            ", " + Schema.viewerTabName + ") " +
            ")";
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName +
            " (" + Schema.UserOwner + ", " +
            Schema.viewerTabName + ", " +
            Schema.identifier + ", " +
            Schema.headerValue + ", " +
            Schema.width + ", " +
            Schema.position + ", " +
            Schema.resizable + ", " +
            Schema.visible + ", " +
            Schema.SortOrder + ") "+
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public final static String SelectSpecificRecord =  "select * from " +
            Schema.TableName + " where " +
            Schema.viewerTabName + " = ? AND " +
            Schema.identifier + " = ? AND " +
            Schema.UserOwner + " = ?";
    public final static String DeleteSpecificRecord =  "delete from " +
            Schema.TableName + " where " +
            Schema.viewerTabName + " = ? AND " +
            Schema.identifier + " = ? AND " +
            Schema.UserOwner + " = ?";
    public final static String SelectRecords =  "select * from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.viewerTabName + " = ?";
    public final static String DeleteRecords =  "delete from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ? AND " +
            Schema.viewerTabName + " = ?";
    public final static String DeleteAllRecords =  "delete from " +
            Schema.TableName + " where " +
            Schema.UserOwner + " = ?";

    
    public final static String AddSortOrderColumn =  "ALTER TABLE " + Schema.TableName 
            + " ADD "+Schema.SortOrder+" VARCHAR(50)";
    
    public static enum Schema {

        TableName("APP.PB_ViewerTableColumn"),

        identifier("identifier"), //private String identifier; key, it should be unique
        headerValue("headerValue"), //private String headerValue;
        width("width"), //private int width;
        resizable("resizable"), //private boolean resizable;
        visible("visible"), //private boolean visible;
        position("position"), //private int position;

        SortOrder("SortOrder"),
        
        viewerTabName("viewerTablePanelName"),   //foreign key

        //there is a special account "pbconsole_anonymous" which hold the standard default config
        UserOwner("UserOwner"); //who own this config

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
