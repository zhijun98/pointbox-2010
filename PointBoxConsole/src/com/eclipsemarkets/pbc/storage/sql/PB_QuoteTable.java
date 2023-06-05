/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage.sql;

/**
 * PB_QuoteTable.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 4, 2010, 11:23:23 AM
 */
public class PB_QuoteTable {

    public final static String RecycleByDays = "delete from " + Schema.TableName +
            " where " + Schema.LAST_UPDATE + " < ? ";

    public final static String DropTable = "drop table " + Schema.TableName;
    
    public final static String CreateNewTable =
            "create table " + Schema.TableName + " (" +
            "    " + Schema.UserOwner + "        VARCHAR(100) NOT NULL, " +
            "    " + Schema.QUOTE_UUID + "               VARCHAR(100) NOT NULL," +
            "    " + Schema.QUOTE_TYPE + "             VARCHAR(50)," +
            "    " + Schema.MESSAGE_UUID + "           VARCHAR(100) NOT NULL, " +   //foreign key
            "    " + Schema.BID + "                    NUMERIC(10,5), " +
            "    " + Schema.ASK + "                    NUMERIC(10,5), " +
            "    " + Schema.TRADE + "                  NUMERIC(10,5), " +
            "    " + Schema.PRICE + "                  NUMERIC(10,5), " +
            "    " + Schema.DELTA + "                  NUMERIC(10,5), " +
            "    " + Schema.VEGA + "                   NUMERIC(10,5), " +
            "    " + Schema.THETA + "                  NUMERIC(10,5), " +
            "    " + Schema.GAMMA + "                  NUMERIC(10,5), " +
            "    " + Schema.DDELTA + "                 NUMERIC(10,5), " +
            "    " + Schema.DGAMMA + "                 NUMERIC(10,5), " +
            "    " + Schema.MEANF01 + "                NUMERIC(10,5), " +
            "    " + Schema.MEANF02 + "                NUMERIC(10,5), " +
            "    " + Schema.PRICING_TIMESTAMP + "      TIMESTAMP, " +
            "    " + Schema.ISSUFFICIENTFORPRICING + " SMALLINT, " +   //YES OR NO
            "    " + Schema.LAST_UPDATE + "            TIMESTAMP, " +
            "    PRIMARY KEY (" + Schema.UserOwner +
            ", " + Schema.QUOTE_UUID + ") " +
            ")";
    public final static String InsertNewRecord =
            "insert into " + Schema.TableName + " (" +
            Schema.UserOwner + ", " +
            Schema.QUOTE_UUID + ", " +
            Schema.QUOTE_TYPE + ", " +
            Schema.MESSAGE_UUID + ", " +
            Schema.BID + ", " +
            Schema.ASK + ", " +
            Schema.TRADE + ", " +
            Schema.PRICE + ", " +
            Schema.DELTA + ", " +
            Schema.VEGA + ", " +
            Schema.THETA + ", " +
            Schema.GAMMA + ", " +
            Schema.DDELTA + ", " +
            Schema.DGAMMA + ", " +
            Schema.MEANF01 + ", " +
            Schema.MEANF02 + ", " +
            Schema.PRICING_TIMESTAMP + ", " +
            Schema.ISSUFFICIENTFORPRICING + ", " +
            Schema.LAST_UPDATE + ") " +
            "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public final static String SelectSpecificQuoteByMessageID = "select * from " + 
            Schema.TableName + " where " + Schema.MESSAGE_UUID + " = ?";
    public static enum Schema {
        TableName("APP.PB_Quote"),

        UserOwner("UserOwner"),
        QUOTE_UUID("QUOTE_ID"),
        QUOTE_TYPE("QUOTE_TYPE"),
        MESSAGE_UUID("MESSAGE_ID"),
        BID("BID"),
        ASK("ASK"),
        TRADE("TRADE"),
        PRICE("PRICE"),
        DELTA("DELTA"),
        VEGA("VEGA"),
        THETA("THETA"),
        GAMMA("GAMMA"),
        DDELTA("DDELTA"),
        DGAMMA("DGAMMA"),
        MEANF01("MEANF01"),
        MEANF02("MEANF02"),
        PRICING_TIMESTAMP("PRICING_TIMESTAMP"),
        ISSUFFICIENTFORPRICING("ISSUFFICIENTFORPRICING"),
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
