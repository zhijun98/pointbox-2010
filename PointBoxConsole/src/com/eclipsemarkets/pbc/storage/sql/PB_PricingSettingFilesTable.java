/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_PricingSettingFilesTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jul 15, 2010, 12:59:39 PM
 */
public class PB_PricingSettingFilesTable {

    public final static String DropTable = "drop table " + Schema.TableName;

    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
                "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
                "    " + Schema.FileUniqueName + "     VARCHAR(255) NOT NULL," +
                "    " + Schema.FilePath + "        VARCHAR(1000), " +
                "    " + Schema.Commodity + "     VARCHAR(50)," +
                "    " + Schema.Underlier + "     VARCHAR(50)," +
                "    " + Schema.Security + "     VARCHAR(50)," +
                "    " + Schema.LAST_UPDATE + "            TIMESTAMP, " +
                "    PRIMARY KEY (" + Schema.UserOwner +
                ", " + Schema.FileUniqueName + ") " +
            ")";

    public final static String InsertNewRecord =  "insert into " + Schema.TableName +
                                                " (" + Schema.UserOwner + ", " +
                                                       Schema.FileUniqueName + ", " +
                                                       Schema.FilePath + ", " +
                                                       Schema.Commodity + ", " +
                                                       Schema.Underlier + ", " +
                                                       Schema.Security + ", " +
                                                       Schema.LAST_UPDATE + ") "+
                                                 "values (?, ?, ?, ?, ?, ?, ?)";

    public final static String SelectSpecificRecordByOwnerFileIdCommodityType = "select * from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ? and " + Schema.FileUniqueName + " = ? and " + Schema.Commodity + " = ?" ;

    public final static String DeleteRecordsByOwnerFileIdCommodity = "delete from " + Schema.TableName + " where " +
            Schema.UserOwner + " = ? and " + Schema.FileUniqueName + " = ? and " + Schema.Commodity + " = ?" ;

    public static enum Schema {

        TableName("APP.PB_PricingSettingFiles"),

        UserOwner("UserOwner"),
        FileUniqueName("FileName"),
        FilePath("FilePath"),
        Commodity("QuoteCommodityType"),     //QuoteCommodityType
        Underlier("QuoteUnderlierType"),     //QuoteUnderlierType
        Security("QuoteSecurityType"),       //QuoteSecurityType
        LAST_UPDATE("last_update");

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
