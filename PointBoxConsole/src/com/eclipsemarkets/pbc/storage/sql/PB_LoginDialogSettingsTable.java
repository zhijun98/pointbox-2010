/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_LoginDialogSettingsTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 8, 2010, 11:16:10 AM
 */
public class PB_LoginDialogSettingsTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
                "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
                "    " + Schema.ServerType + "     VARCHAR(50) NOT NULL," +
                "    " + Schema.Account + "        VARCHAR(100)," +
                "    " + Schema.Password + "        VARCHAR(100), " +
                "    " + Schema.SelectedConnectionHost + "        VARCHAR(255), " +
                "    " + Schema.RememberAccount + "        SMALLINT, " +
                "    " + Schema.SavePassword + "        SMALLINT, " +
                "    " + Schema.AutoLogin + "        SMALLINT, " +
                "    PRIMARY KEY (" + Schema.UserOwner +
                ", " + Schema.ServerType + ") " +
                ")";
    
    public final static String AddCommodityTypeColumn =  "ALTER TABLE " + Schema.TableName 
            + " ADD "+Schema.CommodityType+" VARCHAR(50)";
    
//    public final static String DropPrimaryKey =  "ALTER TABLE " + Schema.TableName 
//            + " DROP PRIMARY KEY";
    
//    //ALTER TABLE `data` MODIFY COLUMN `sample_id` INTEGER UNSIGNED NOT NULL
//    public final static String ChangeAccountColumn =  "ALTER TABLE " + Schema.TableName 
//            + " MODIFY COLUMN " + Schema.Account + " VARCHAR(100) NOT NULL";
//    
//    public final static String AddPrimaryKey03232011 =  "ALTER TABLE " + Schema.TableName 
//            + " ADD PRIMARY KEY (" + Schema.UserOwner +
//                ", " + Schema.ServerType + ", " + Schema.Account +") ";
    
    public final static String SelectSpecificRecord =  "select * from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.ServerType + " = ?";
    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                                                " (" + Schema.UserOwner + ", " +
                                                       Schema.ServerType + ", " +
                                                       Schema.Account + ", " +
                                                       Schema.Password + ", " +
                                                       Schema.SelectedConnectionHost + ", " +
                                                       Schema.RememberAccount + ", " +
                                                       Schema.SavePassword + ", " +
                                                       Schema.AutoLogin + ", " +
                                                       Schema.CommodityType + ") "+
                                                 "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public final static String DeleteSpecificRecord = "delete from " +
                                                        Schema.TableName + " where " +
                                                        Schema.UserOwner + " = ? AND " +
                                                        Schema.ServerType + " = ?";
    public static enum Schema {

        TableName("APP.PB_LoginDialogSettings"),

        CommodityType("CommodityType"),
        UserOwner("UserOwner"),
        ServerType("ServerType"),
        Account("Account"),
        Password("Password"),
        SelectedConnectionHost("SelectedConnectionHost"),   //user-chosen server-host for connection on the dialog
        RememberAccount("RememberAccount"),
        SavePassword("SavePassword"),
        AutoLogin("AutoLogin");

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
