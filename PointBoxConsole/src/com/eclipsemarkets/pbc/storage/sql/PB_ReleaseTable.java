/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_ReleaseTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:01:23 AM
 */
public class PB_ReleaseTable {
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.SOFTWARE_NAME + "     VARCHAR(50) NOT NULL PRIMARY KEY," +
            "    " + Schema.SOFTWARE_VERSION + "        VARCHAR(50), " +
            "    " + Schema.RELEASE_COMPANY + "        VARCHAR(50), " +
            "    " + Schema.RELEASE_CODE + "        VARCHAR(50), " +
            "    " + Schema.LAST_UPDATE + "        TIMESTAMP " +
            ")";
    public final static String SelectAllRecords = "select * from " + Schema.TableName;
    public final static String DeleteAllRecords = "delete from " + Schema.TableName;
    public final static String InsertNewRecord = "insert into " + Schema.TableName +
                                                " (" + Schema.SOFTWARE_NAME + ", " +
                                                Schema.SOFTWARE_VERSION + ", " +
                                                Schema.RELEASE_COMPANY + ", " +
                                                Schema.RELEASE_CODE + ", " +
                                                Schema.LAST_UPDATE + ", " +
                                                Schema.ProxyHost + ", " +
                                                Schema.ProxyPort + ", " +
                                                Schema.ProxyUser + ", " +
                                                Schema.ProxyPassword + ", " +
                                                Schema.ClientUuidInfo + ") "+
                                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public final static String SelectReleaseCode = "select " + Schema.RELEASE_CODE + " from " +
                                                    Schema.TableName;
    
    public final static String AddProxyHostColumn =  "ALTER TABLE " + Schema.TableName 
            + " ADD "+Schema.ProxyHost+" VARCHAR(255)";
    public final static String AddProxyPortColumn =  "ALTER TABLE " + Schema.TableName 
            + " ADD "+Schema.ProxyPort+" VARCHAR(50)";
    public final static String AddProxyUserColumn =  "ALTER TABLE " + Schema.TableName 
            + " ADD "+Schema.ProxyUser+" VARCHAR(100)";
    public final static String AddProxyPasswordColumn =  "ALTER TABLE " + Schema.TableName 
            + " ADD "+Schema.ProxyPassword+" VARCHAR(100)";
    public final static String AddClientUuidInfoColumn =  "ALTER TABLE " + Schema.TableName 
            + " ADD "+Schema.ClientUuidInfo+" VARCHAR(100)";

    public static enum Schema {
        TableName("APP.PB_Release"),
        
        ClientUuidInfo("ClientUuidInfo"),
        ProxyHost("ProxyHost"),
        ProxyPort("ProxyPort"),
        ProxyUser("ProxyUser"),
        ProxyPassword("ProxyPassword"),
        SOFTWARE_NAME("SOFTWARE_NAME"),
        SOFTWARE_VERSION("SOFTWARE_VERSION"),
        RELEASE_CODE("RELEASE_CODE"),
        RELEASE_COMPANY("RELEASE_COMPANY"),
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
