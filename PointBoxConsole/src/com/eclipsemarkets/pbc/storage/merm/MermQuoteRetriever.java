/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import com.eclipsemarkets.global.CalendarGlobal;
import java.io.File;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysQuote;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import com.eclipsemarkets.pbc.PbcProperties;
import com.eclipsemarkets.pbc.PbcReleaseUserType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
class MermQuoteRetriever implements IMermQuoteRetriever{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(MermQuoteRetriever.class.getName());
    }
    private MSAccessQuerier querier = null;
    private File mdb;
    private boolean ready;

    MermQuoteRetriever(String dbURL) {
        this.mdb = new File(dbURL);
        querier = MSAccessQuerier.getMSAccessQuerierInstance(mdb, "", "");
        ready = (NIOGlobal.isValidFile(mdb, ".mdb") && querier.isStarted());
    }
    /**
     * This is called "readBadQuotes" in Excel which is used by Merm's in-house application
     */
    @Override
    public synchronized ArrayList<IPbsysQuote> retrieveBadQuoteTL(){
        return new ArrayList<IPbsysQuote>();
    }
    
    @Override
    public synchronized void changeMdbPath(String mdbPath){
        if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
            this.mdb = new File(mdbPath);
            querier = MSAccessQuerier.getMSAccessQuerierInstance(mdb, "", "");
            querier.setMdbFile(mdb);
            ready = (NIOGlobal.isValidFile(mdb, ".mdb") && querier.isStarted());
        }
    }

    @Override
    public synchronized boolean isReady() {
        if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
            return ready;
        }else{
            return false;
        }
    }

    @Override
    public void shutdown() {
        if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
            if (querier != null){
                querier.shutdown();
            }
        }
    }

    public ArrayList<String> retrieveAllHistoricalMessages(PbcReleaseUserType type) {
        ArrayList<String> messages = new ArrayList<String>();
        if (PbcReleaseUserType.MERM_USERS.equals(type)){
            String sqlSelect = "SELECT * from all_q";
            try {
                ResultSet rs = querier.execute(new DatabaseSelectCommand(sqlSelect));
                while (rs.next()){
                    messages.add(rs.getString("Quote"));
                }
            } catch (SQLException ex) {
            }
        }
        return messages;
    }

    public void autoFixQuote(IPbsysOptionQuote targetQuote, String fixMapping){
        if (!PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
            return;
        }
        String sqlSelect = "SELECT * from main_q WHERE ID = '" + fixMapping + "'";
        IPbsysQuoteLeg leg1 = targetQuote.getOptionStrategyLegs().get(0);
    	IPbsysQuoteLeg leg2 = targetQuote.getOptionStrategyLegs().get(1);

        try{
            //ResultSet rs = stmt.executeQuery(sqlSelect);
            ResultSet rs = querier.execute(new DatabaseSelectCommand(sqlSelect));
            String regex = "([0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2})";
            while (rs.next()){
                //read field by field
                try {
                    targetQuote.setOriginalInstantMessage("[a-fix] " + targetQuote.getOriginalInstantMessage());
                    //targetQuote.setDbID(rs.getString("UserID"));
                    //targetQuote.setRemoteBroker(new MermulatorUserInfo(rs.getString("Broker")));
                    //targetQuote.setOriginalInstantMessageTimestamp(rs.getString("qTime"));
                    leg1.setOptionContractStartDate(CalendarGlobal.createCalendarInstrance(rs.getString("qStart"), regex, "-"));
                    leg1.setOptionContractEndDate(CalendarGlobal.createCalendarInstrance(rs.getString("qEnd"), regex, "-"));
                    leg1.setOptionStrategy(rs.getString("Structure"));
                    leg1.getOptionStrikes()[0] = DataGlobal.convertToDouble(filter(rs.getString("Strike1")));
                    leg1.getOptionStrikes()[1] = DataGlobal.convertToDouble(filter(rs.getString("Strike2")));
                    leg1.getOptionStrikes()[2] = DataGlobal.convertToDouble(filter(rs.getString("Strike3")));
                    leg1.getOptionStrikes()[3] = DataGlobal.convertToDouble(filter(rs.getString("Strike4")));
                    //leg1.getOptionStrikes()[4] = DataGlobal.convertToDouble(filter(rs.getString("Strike5")));
                    leg1.getOptionRatios()[0] = DataGlobal.convertToDouble(filter(rs.getString("Ratio1")));
                    leg1.getOptionRatios()[1] = DataGlobal.convertToDouble(filter(rs.getString("Ratio2")));
                    leg1.getOptionRatios()[2] = DataGlobal.convertToDouble(filter(rs.getString("Ratio3")));
                    leg1.getOptionRatios()[3] = DataGlobal.convertToDouble(filter(rs.getString("Ratio4")));
                    //leg1.getOptionRatios()[4] = DataGlobal.convertToDouble(filter(rs.getString("Ratio5")));
                    leg1.setOptionCross(DataGlobal.convertToDouble(filter(rs.getString("qCross"))));
                    leg1.setOptionProduct(filter(rs.getString("Product")));

                    //targetQuote.setOptionLastTradePricePrivateIncoming(DataGlobal.convertToDouble(filter(rs.getString("qLast"))));
                    //targetQuote.setOptionBidPricePrivateIncoming(filter(rs.getString("Bid")));
                    //targetQuote.setOptionAskPricePrivateIncoming(filter(rs.getString("Ask")));

                    leg2.setOptionContractStartDate(CalendarGlobal.createCalendarInstrance(rs.getString("qStart2"), regex, "-"));
                    leg2.setOptionContractEndDate(CalendarGlobal.createCalendarInstrance(rs.getString("qEnd2"), regex, "-"));
                    leg2.setOptionStrategy(rs.getString("Type2"));
                    leg2.getOptionStrikes()[0] = DataGlobal.convertToDouble(filter(rs.getString("Strike12")));
                    leg2.getOptionStrikes()[1] = DataGlobal.convertToDouble(filter(rs.getString("Strike22")));
                    leg2.getOptionStrikes()[2] = DataGlobal.convertToDouble(filter(rs.getString("Strike32")));
                    leg2.getOptionStrikes()[3] = DataGlobal.convertToDouble(filter(rs.getString("Strike42")));
                    //leg2.getOptionStrikes()[4] = DataGlobal.convertToDouble(filter(rs.getString("Strike52")));
                    leg2.getOptionRatios()[0] = DataGlobal.convertToDouble(filter(rs.getString("Ratio12")));
                    leg2.getOptionRatios()[1] = DataGlobal.convertToDouble(filter(rs.getString("Ratio22")));
                    leg2.getOptionRatios()[2] = DataGlobal.convertToDouble(filter(rs.getString("Ratio32")));
                    leg2.getOptionRatios()[3] = DataGlobal.convertToDouble(filter(rs.getString("Ratio42")));
                    //leg2.getOptionRatios()[4] = DataGlobal.convertToDouble(filter(rs.getString("Ratio52")));
                    leg2.setOptionCross(DataGlobal.convertToDouble(filter(rs.getString("qCross2"))));
                    leg2.setOptionProduct(filter(rs.getString("Product2")));

                    //targetQuote.setFixMapping(fixMapping);
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }//while
        }catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private String filter(String string) {
        string = DataGlobal.denullize(string);
        string = string.toLowerCase();
        if (string.equalsIgnoreCase("0") || string.equalsIgnoreCase("0.0")){
            string = "";
        }
        return string;
    }

}
