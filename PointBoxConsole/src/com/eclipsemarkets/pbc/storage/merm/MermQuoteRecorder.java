/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import com.eclipsemarkets.data.PointBoxQuoteGroup;
import static com.eclipsemarkets.data.PointBoxQuoteGroup.CRUDE;
import com.eclipsemarkets.data.PointBoxQuoteStrategyTerm;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.TextGlobal;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.PbcProperties;
import com.eclipsemarkets.pbc.PbcReleaseUserType;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A MSAccess agent (Thread-Safe and Singleton) stores a collection of OptionQuotes into local database
 * <p>
 * Low-quality thread-safe is offered because it only argues to save the record into the database for
 * MERM-IN-HOUSE situations.
 * <p>
 * @author Zhijun Zhang
 */
class MermQuoteRecorder implements IMermQuoteRecorder{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(MermQuoteRecorder.class.getName());
    }

    private IPbcKernel kernel;
    private MSAccessModifier modifier;
    private File mdb;
    
    MermQuoteRecorder(IPbcKernel kernel, String mdbPath) {
        this.mdb = new File(mdbPath);
        modifier = MSAccessModifier.getMSAccessModifierSingleton(mdb, "", "");
    }

    MermQuoteRecorder(File mdb) {
        this.mdb = mdb;
        modifier = MSAccessModifier.getMSAccessModifierSingleton(mdb, "", "");
    }

    public synchronized boolean isReady() throws SQLException{
        if (NIOGlobal.isValidFile(mdb)){
            return (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)
                    || PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.DEBUG_USERS));
        }else{
            throw new SQLException("Cannot find MS-Access path: " + mdb);
        }
    }

    @Override
    public synchronized void changeMdbPath(String mdbPath) {
        if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
            this.mdb = new File(mdbPath);
            modifier = MSAccessModifier.getMSAccessModifierSingleton(mdb, "", "");
            modifier.setMdbFile(mdb);
        }
    }

    /**
     * todo: zhijun - think about it how to destroy this guy
     */
    @Override
    public void shutdown() {
        if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
            if (modifier != null){
                modifier.shutdown();
            }
        }
    }

    @Override
    public void storeMermOptionQuotes(ArrayList<IPbsysOptionQuote> quotes) throws SQLException {
        if (isReady()){
            for (int i = 0; i < quotes.size(); i++){
                //todo: it is not efficient
                //if (!PbsysRuntime.pbsysRuntimeFactory().isValidBroker(quote.getRemoteBroker().getIMScreenName())){
                //    quote.setSufficientPricingData(false);
                //}
                storeParsedOptionQuote(quotes.get(i));
            }     
        }
    }

    private void storeParsedOptionQuote(IPbsysOptionQuote quote) throws SQLException{
        if (quote.getOriginalInstantMessage().trim().length() > 0){
            if ((quote.getInstantMessage() != null) 
                    && (quote.getInstantMessage().isHistoricalMessage())){
                return;
            }
            synchronized(this){
                //PointBoxLogger.printMessage(">>> MermQuoteRecorder starts working now ...");
                //mark it with a time stamp
                quote.setDbUpdateTimestamp(new GregorianCalendar());
                if (quote.isStorageUpdateRequired()){
                    updateParsedOptionQuote(quote);
                }else{
                    //main_q
                    String[] sqlArray = prepareInsertSqlForMainQ(quote);
                    if (quote.isSufficientPricingData()){
                        try {
                            modifier.execute(new DatabaseDeleteCommand(sqlArray[0]), 10000);
                        } catch (SQLException ex) {
                            logger.log(Level.SEVERE, ex.getMessage(), ex);
                        }
                        try{
                            modifier.execute(new DatabaseInsertCommand(sqlArray[1]), 10000);
                        } catch (SQLException ex) {
                            logger.log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }
                    modifier.execute(new DatabaseInsertCommand(sqlArray[2]), 10000);
                }
            }
        }//if
    }

    private void updateParsedOptionQuote(IPbsysOptionQuote updateQuote) {
        updateQuote.setStorageUpdateRequired(false);
        if (updateQuote.isSufficientPricingData()){
            String[] sqlUpdateArray = prepareInsertSqlForMainQ(updateQuote);
            try {
                //delete the old ones ...
                modifier.execute(new DatabaseDeleteCommand("DELETE * FROM main_q WHERE ID = '" + updateQuote.getStandardQuoteID() + "'"), 10000);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
            try {
                //delete a possible new-one inserted by other applications before this app
                modifier.execute(new DatabaseDeleteCommand(sqlUpdateArray[0]), 10000);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
            try{
                //insert a new one
                modifier.execute(new DatabaseInsertCommand(sqlUpdateArray[1]), 10000);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    private String[] prepareInsertSqlForMainQ(IPbsysOptionQuote quote) {

        PointBoxQuoteGroup aPointBoxQuoteGroup = PointBoxQuoteGroup.convertEnumValueToType(quote.getPbcPricingModel().getSqGroup());
        
        quote.prepareForPricingAndStorage();

        String[] sqlArray = new String[3];  //0 - delete; 1 - insert-MainQ; 2 insert-AllQ
        String quoteID = quote.getStandardQuoteID();

        String leg1_structure_backup = null;
        double leg1_strike_2_backup = 0;
        double leg1_strike_3_backup = 0;
        double leg1_strike_4_backup = 0;
        String leg2_structure_backup = null;
        double leg2_strike_2_backup = 0;
        double leg2_strike_3_backup = 0;
        double leg2_strike_4_backup = 0;
        IPbsysQuoteLeg leg1 = quote.getOptionStrategyLegs().get(0);
        if ((leg1 != null) && (DataGlobal.isNonEmptyNullString(leg1.getOptionStrategy()))){
            leg1_structure_backup = leg1.getOptionStrategy();
            if (leg1_structure_backup.equalsIgnoreCase("FENCE")){
                leg1.setOptionStrategy("CLLR");
            }else if (leg1_structure_backup.equalsIgnoreCase("STRNGL")){
                leg1.setOptionStrategy("stngl");
            }else if (leg1_structure_backup.equalsIgnoreCase("PCNDR")){
                leg1.setOptionStrategy("condor");
            }else if (leg1_structure_backup.equalsIgnoreCase("CCNDR")){
                leg1.setOptionStrategy("condor");
            }else if (leg1_structure_backup.equalsIgnoreCase("PTREE")){
                leg1.setOptionStrategy("put tree");
            }else if (leg1_structure_backup.equalsIgnoreCase("CTREE")){
                leg1.setOptionStrategy("call tree");
            }
            leg1_strike_2_backup = leg1.getOptionStrikes(1);
            leg1_strike_3_backup = leg1.getOptionStrikes(2);
            leg1_strike_4_backup = leg1.getOptionStrikes(3);
            if (PointBoxQuoteStrategyTerm.STRDL.toString().equalsIgnoreCase(leg1.getOptionStrategy())){
                leg1.setOptionStrikes(1, 0.0);
                leg1.setOptionStrikes(2, 0.0);
                leg1.setOptionStrikes(3, 0.0);
            }else if (PointBoxQuoteStrategyTerm.IFLY.toString().equalsIgnoreCase(leg1.getOptionStrategy())){
                leg1.setOptionStrikes(1, leg1_strike_2_backup);
                leg1.setOptionStrikes(2, leg1_strike_4_backup);
                leg1.setOptionStrikes(3, 0.0);
                leg1.setOptionStrategy("iron fly");
            }
            leg1.setOptionStrategy(leg1.getOptionStrategy().toLowerCase());
        }
        IPbsysQuoteLeg leg2 = quote.getOptionStrategyLegs().get(1);
        if ((leg2 != null) && (DataGlobal.isNonEmptyNullString(leg2.getOptionStrategy()))){
            leg2_structure_backup = leg2.getOptionStrategy();
            if (leg2_structure_backup.equalsIgnoreCase("FENCE")){
                leg2.setOptionStrategy("CLLR");
            }else if (leg2_structure_backup.equalsIgnoreCase("STRNGL")){
                leg2.setOptionStrategy("stngl");
            }else if (leg2_structure_backup.equalsIgnoreCase("PCNDR")){
                leg2.setOptionStrategy("condor");
            }else if (leg2_structure_backup.equalsIgnoreCase("CCNDR")){
                leg2.setOptionStrategy("condor");
            }else if (leg2_structure_backup.equalsIgnoreCase("PTREE")){
                leg2.setOptionStrategy("put tree");
            }else if (leg2_structure_backup.equalsIgnoreCase("CTREE")){
                leg2.setOptionStrategy("call tree");
            }
            leg2_strike_2_backup = leg2.getOptionStrikes(1);
            leg2_strike_3_backup = leg2.getOptionStrikes(2);
            leg2_strike_4_backup = leg2.getOptionStrikes(3);
            if (PointBoxQuoteStrategyTerm.STRDL.toString().equalsIgnoreCase(leg2.getOptionStrategy())){
                leg2.setOptionStrikes(1, 0.0);
                leg2.setOptionStrikes(2, 0.0);
                leg2.setOptionStrikes(3, 0.0);
            }else if (PointBoxQuoteStrategyTerm.IFLY.toString().equalsIgnoreCase(leg2.getOptionStrategy())){
                leg1.setOptionStrikes(1, leg2_strike_2_backup);
                leg1.setOptionStrikes(2, leg2_strike_4_backup);
                leg1.setOptionStrikes(3, 0.0);
                leg1.setOptionStrategy("iron fly");
            }
            leg2.setOptionStrategy(leg2.getOptionStrategy().toLowerCase());
        }

        //IMermulatorQuote
        String sqlHeader = "INSERT INTO main_q (Quote";
        String sqlInsertMainQ = "'" + quote.getOriginalInstantMessage() + "'"; //IMermulatorQuote
        //String sqlSelect = "SELECT IMermulatorQuote FROM main_q WHERE IMermulatorQuote = '" + quote.getOriginalInstantMessage() +"'";
        String sqlDelete = "DELETE * FROM main_q WHERE Quote LIKE '" + quote.getOriginalInstantMessage().replaceAll("[ ]", "%") +"'";
        //ID
        sqlHeader = sqlHeader + ", ID";
        //sqlInsertMainQ = sqlInsertMainQ + ", '" + quote.getQuoteId() + quote.getLocalBroker().getIMScreenName() + prepareTimestamp(quote.getOriginalInstantMessageTimestamp()) + "'";
        sqlInsertMainQ = sqlInsertMainQ + ", '" + quoteID + "'";
        sqlDelete = sqlDelete + " AND ID LIKE '%" + (quoteID.split("@")[0]).replaceFirst("^([0-9]+)", "") + "%'";
        //sqlSelect = sqlSelect + " AND ID = '" + quoteID + "'";
        //Broker
        sqlHeader = sqlHeader + ", Broker";
        sqlInsertMainQ = sqlInsertMainQ + ", '" + modifyScreenNameBeforeStoring(quote.getRemoteBroker().getIMScreenName()) + "'";
        sqlDelete = sqlDelete + " AND Broker = '" + modifyScreenNameBeforeStoring(quote.getRemoteBroker().getIMScreenName()) + "'";
        //qTime
        sqlHeader = sqlHeader + ", qTime";
        sqlInsertMainQ = sqlInsertMainQ + ", #" + prepareTimestamp(quote.getOriginalInstantMessageTimestamp()) + "#";
        //sqlSelect = sqlSelect + " AND qTime = #" + prepareTimestamp(quote.getOriginalInstantMessageTimestamp()) + "#";
        //qStart, qEnd, Structure, Strike1, Strike2, Strike3, Strike4, Strike5, Ratio1, Ratio2, Ratio3, Ratio4, Ratio5, qCross
        if (leg1 == null){
            sqlHeader = sqlHeader + ", Structure, Strike1, Strike2, Strike3, Strike4, Strike5, Ratio1, Ratio2, Ratio3, Ratio4, Ratio5, qCross";
            sqlInsertMainQ = sqlInsertMainQ + ", ''"; //Structure
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike1
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike2
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike3
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike4
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike5
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio1
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio2
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio3
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio4
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio5
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //qCross
        }else{
            if (leg1.getOptionContractStartDate() != null){
                sqlHeader = sqlHeader + ", qStart";
                sqlInsertMainQ = sqlInsertMainQ + ", #" + CalendarGlobal.parseToMMddyyyy(leg1.getOptionContractStartDate(), "/") + "#";
                sqlDelete = sqlDelete + " AND qStart = #" + CalendarGlobal.parseToMMddyyyy(leg1.getOptionContractStartDate(), "/") + "#";
            }
            if (leg1.getOptionContractEndDate() != null){
                sqlHeader = sqlHeader + ", qEnd";
                sqlInsertMainQ = sqlInsertMainQ + ", #" + CalendarGlobal.parseToMMddyyyy(leg1.getOptionContractEndDate(), "/") + "#";
                sqlDelete = sqlDelete + " AND qEnd = #" + CalendarGlobal.parseToMMddyyyy(leg1.getOptionContractEndDate(), "/") + "#";
            }
             //Structure
            sqlHeader = sqlHeader + ", Structure, Strike1, Strike2, Strike3, Strike4, Strike5, Ratio1, Ratio2, Ratio3, Ratio4, Ratio5, qCross";
            sqlInsertMainQ = sqlInsertMainQ + ", '" + leg1.getOptionStrategy() + "'";
            sqlDelete = sqlDelete + " AND Structure = '" + leg1.getOptionStrategy() + "'";
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg1.getOptionStrikes()[0]; //Strike1
            sqlDelete = sqlDelete + " AND Strike1 = " + leg1.getOptionStrikes()[0];
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg1.getOptionStrikes()[1]; //Strike2
            //sqlSelect = sqlSelect + " AND Strike2 = " + leg1.getOptionStrikes()[1];
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg1.getOptionStrikes()[2]; //Strike3
            //sqlSelect = sqlSelect + " AND Strike3 = " + leg1.getOptionStrikes()[2];
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg1.getOptionStrikes()[3]; //Strike4
            //sqlSelect = sqlSelect + " AND Strike4 = " + leg1.getOptionStrikes()[2];
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike5
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg1.getOptionRatios()[0]); //Ratio1
            //sqlSelect = sqlSelect + " AND Ratio1 = " + leg1.getOptionRatios()[0];
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg1.getOptionRatios()[1]); //Ratio2
            //sqlSelect = sqlSelect + " AND Ratio2 = " + leg1.getOptionRatios()[1];
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg1.getOptionRatios()[2]); //Ratio3
            //sqlSelect = sqlSelect + " AND Ratio3 = " + leg1.getOptionRatios()[2];
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg1.getOptionRatios()[3]); //Ratio4
            //sqlSelect = sqlSelect + " AND Ratio4 = " + leg1.getOptionRatios()[3];
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio5
            if (leg1.isCrossEmbedded()){
                sqlInsertMainQ = sqlInsertMainQ + ", " + leg1.getOptionCross(); //qCross
            }else{
                sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //qCross
            }
            //sqlSelect = sqlSelect + " AND qCross = " + leg1.getOptionCross();
        }
        switch (aPointBoxQuoteGroup){
            case CRUDE:
                //qLast, Bid, Ask, Underlier, Security, Exercise
                sqlHeader = sqlHeader + ", qLast, Bid, Ask, Underlier, Security, Exercise";
                break;
            default:
                //qLast, Bid, Ask, Product
                sqlHeader = sqlHeader + ", qLast, Bid, Ask, Product";
        }
        sqlInsertMainQ = sqlInsertMainQ + ", " + quote.getTradePrice(); //qLast
        sqlInsertMainQ = sqlInsertMainQ + ", " + prepareIncomingAskBid(quote.getOptionBidPricePrivateIncoming()); //Bid
        sqlInsertMainQ = sqlInsertMainQ + ", " + prepareIncomingAskBid(quote.getOptionAskPricePrivateIncoming()); //Ask
        switch (aPointBoxQuoteGroup){
            case CRUDE:
                if (leg1 == null){
                    sqlInsertMainQ = sqlInsertMainQ + ", '', '', ''";
                }else{
                    sqlInsertMainQ = sqlInsertMainQ + ", '" + leg1.getOptionUnderlier() + "'"; //Underlier
                    sqlInsertMainQ = sqlInsertMainQ + ", '" + leg1.getOptionSecurity() + "'"; //Security
                    sqlInsertMainQ = sqlInsertMainQ + ", '" + leg1.getOptionExercise() + "'"; //Exercise
                }
                break;
            default:
                if (leg1 == null){
                    sqlInsertMainQ = sqlInsertMainQ + ", ''";
                }else{
                    sqlInsertMainQ = sqlInsertMainQ + ", '" + prepareProduct(leg1.getOptionProduct()) + "'"; //Product
                }
        }//switch

        //qStart2, qEnd2, Product2) VALUES (";
        if ((leg2 == null) || leg2.isEmptyLeg()){
            switch (aPointBoxQuoteGroup){
                case CRUDE:
                    sqlHeader = sqlHeader + ", Type2, Strike12, Strike22, Strike32, Strike42, Strike52, Ratio12, Ratio22, Ratio32, Ratio42, Ratio52, qCross2, Underlier2, Security2, Exercise2";
                    break;
                default:
                    sqlHeader = sqlHeader + ", Type2, Strike12, Strike22, Strike32, Strike42, Strike52, Ratio12, Ratio22, Ratio32, Ratio42, Ratio52, qCross2, Product2";
            }
            
            sqlInsertMainQ = sqlInsertMainQ + ", ''"; //Type2
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike12
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike22
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike32
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike42
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike52
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio12
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio22
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio32
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio42
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio52
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //qCross2
            switch (aPointBoxQuoteGroup){
                case CRUDE:
                    sqlInsertMainQ = sqlInsertMainQ + ", ''"; //Underlier2
                    sqlInsertMainQ = sqlInsertMainQ + ", ''"; //Security2
                    sqlInsertMainQ = sqlInsertMainQ + ", ''"; //Exercise2
                    break;
                default:
                    sqlInsertMainQ = sqlInsertMainQ + ", ''"; //Product2
            }
        }else{
            if (leg2.getOptionContractStartDate() != null){
                sqlHeader = sqlHeader + ", qStart2";
                sqlInsertMainQ = sqlInsertMainQ + ", #" + CalendarGlobal.parseToMMddyyyy(leg2.getOptionContractStartDate(), "/") + "#"; //qStart2
                //sqlSelect = sqlSelect + " AND qStart2 = #" + CalendarCommons.parseToMMddyyyy(leg2.getOptionContractStartDate(), "/") + "#";
            }
            if (leg2.getOptionContractEndDate() != null){
                sqlHeader = sqlHeader + ", qEnd2";
                sqlInsertMainQ = sqlInsertMainQ + ", #" + CalendarGlobal.parseToMMddyyyy(leg2.getOptionContractEndDate(), "/") + "#"; //qEnd2
                //sqlSelect = sqlSelect + " AND qEnd2 = #" + CalendarCommons.parseToMMddyyyy(leg2.getOptionContractEndDate(), "/") + "#";
            }
            switch (aPointBoxQuoteGroup){
                case CRUDE:
                    sqlHeader = sqlHeader + ", Type2, Strike12, Strike22, Strike32, Strike42, Strike52, Ratio12, Ratio22, Ratio32, Ratio42, Ratio52, qCross2, Underlier2, Security2, Exercise2";
                    break;
                default:
                    sqlHeader = sqlHeader + ", Type2, Strike12, Strike22, Strike32, Strike42, Strike52, Ratio12, Ratio22, Ratio32, Ratio42, Ratio52, qCross2, Product2";
            }
            
            sqlInsertMainQ = sqlInsertMainQ + ", '" + leg2.getOptionStrategy() + "'"; //Type2
            //sqlSelect = sqlSelect + " AND Type2 = '" + leg2.getOptionStrategy() + "'";
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg2.getOptionStrikes()[0]; //Strike12
            //sqlSelect = sqlSelect + " AND Strike12 = " + leg2.getOptionStrikes()[0];
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg2.getOptionStrikes()[1]; //Strike22
            //sqlSelect = sqlSelect + " AND Strike22 = " + leg2.getOptionStrikes()[1];
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg2.getOptionStrikes()[2]; //Strike32
            //sqlSelect = sqlSelect + " AND Strike32 = " + leg2.getOptionStrikes()[2];
            sqlInsertMainQ = sqlInsertMainQ + ", " + leg2.getOptionStrikes()[3]; //Strike42
            //sqlSelect = sqlSelect + " AND Strike42 = " + leg2.getOptionStrikes()[3];
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Strike52
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg2.getOptionRatios()[0]); //Ratio12
            //sqlSelect = sqlSelect + " AND Ratio12 = " + leg2.getOptionRatios()[0];
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg2.getOptionRatios()[1]); //Ratio22
            //sqlSelect = sqlSelect + " AND Ratio22 = " + leg2.getOptionRatios()[1];
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg2.getOptionRatios()[2]); //Ratio32
            //sqlSelect = sqlSelect + " AND Ratio32 = " + leg2.getOptionRatios()[2];
            sqlInsertMainQ = sqlInsertMainQ + ", " + Math.abs(leg2.getOptionRatios()[3]); //Ratio42
            //sqlSelect = sqlSelect + " AND Ratio42 = " + leg2.getOptionRatios()[3];
            sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //Ratio52
            if (leg2.isCrossEmbedded()){
                sqlInsertMainQ = sqlInsertMainQ + ", " + leg2.getOptionCross(); //qCross2
            }else{
                sqlInsertMainQ = sqlInsertMainQ + ", 0.0"; //qCross2
            }
            switch (aPointBoxQuoteGroup){
                case CRUDE:
                    sqlInsertMainQ = sqlInsertMainQ + ", '" + leg2.getOptionUnderlier() + "', '" + leg2.getOptionSecurity() +"', '" + leg2.getOptionExercise() + "'"; //underlier2, security2, exercise2
                    break;
                default:
                    sqlInsertMainQ = sqlInsertMainQ + ", '" + prepareProduct(leg2.getOptionProduct()) + "'"; //Product2
            }
        }

        switch (aPointBoxQuoteGroup){
            case CRUDE:
                //straddlerun, swaptionexpiry) VALUES ("
                sqlHeader = sqlHeader + ", StraddleRun";
                if(quote.getOriginalInstantMessage().contains("(straddle run)")){
                        sqlInsertMainQ = sqlInsertMainQ + ", 'x'";
                }else{
                        sqlInsertMainQ = sqlInsertMainQ + ", null";
                }
                break;
            default:
                //do nothing
        }

        sqlArray[0] = sqlDelete;
        sqlArray[1] = sqlHeader + ") VALUES (" + sqlInsertMainQ + ")";
        //modify sqlInsertMainQ to be sqlInsertAllQ
        sqlHeader = sqlHeader.replaceFirst("main_q", "all_q");
        String sqlInsertAllQ = sqlInsertMainQ + ", '" + quote.getLocalBroker().getIMScreenName().toLowerCase() + "'"; //mUser
        if (quote.isSufficientPricingData()){
            sqlInsertAllQ = sqlInsertAllQ + ", 'Y'"; //priceQ
        }else{
            sqlInsertAllQ = sqlInsertAllQ + ", 'N'"; //priceQ
        }
        sqlArray[2] = sqlHeader + ", mUser, priceQ) VALUES (" + sqlInsertAllQ + ")";

//        for (int i = 0; i < sqlArray.length; i++){
//            logger.log(Level.INFO, "sqlArray [{0}] >>> {1}", new Object[]{i, sqlArray[i]});
//        }
        
        if ((leg1 != null) && (DataGlobal.isNonEmptyNullString(leg1.getOptionStrategy()))){
            if (PointBoxQuoteStrategyTerm.STRDL.toString().equalsIgnoreCase(leg1.getOptionStrategy())){
                leg1.setOptionStrikes(1, leg1_strike_2_backup);
                leg1.setOptionStrikes(2, leg1_strike_3_backup);
                leg1.setOptionStrikes(3, leg1_strike_4_backup);
            }else if (PointBoxQuoteStrategyTerm.IFLY.toString().equalsIgnoreCase(leg1.getOptionStrategy())){
                leg1.setOptionStrikes(1, leg1_strike_2_backup);
                leg1.setOptionStrikes(2, leg1_strike_3_backup);
                leg1.setOptionStrikes(3, leg1_strike_4_backup);
                leg1.setOptionStrategy(PointBoxQuoteStrategyTerm.IFLY.toString());
            } 
            if (leg1_structure_backup != null){
                leg1.setOptionStrategy(leg1_structure_backup);
            }
            leg1.setOptionStrategy(leg1.getOptionStrategy().toUpperCase());
        }
        if ((leg2 != null) && (DataGlobal.isNonEmptyNullString(leg2.getOptionStrategy()))){
            if (PointBoxQuoteStrategyTerm.STRDL.toString().equalsIgnoreCase(leg2.getOptionStrategy())){
                leg2.setOptionStrikes(1, leg2_strike_2_backup);
                leg2.setOptionStrikes(2, leg2_strike_3_backup);
                leg2.setOptionStrikes(3, leg2_strike_4_backup);
            }else if (PointBoxQuoteStrategyTerm.IFLY.toString().equalsIgnoreCase(leg2.getOptionStrategy())){
                leg2.setOptionStrikes(1, leg2_strike_2_backup);
                leg2.setOptionStrikes(2, leg2_strike_3_backup);
                leg2.setOptionStrikes(3, leg2_strike_4_backup);
                leg2.setOptionStrategy(PointBoxQuoteStrategyTerm.IFLY.toString());
            }
            if (leg2_structure_backup != null){
                leg2.setOptionStrategy(leg2_structure_backup);
            }
            leg2.setOptionStrategy(leg2.getOptionStrategy().toUpperCase());
        }
        return sqlArray;
    }

    private String modifyScreenNameBeforeStoring(String screenName){
        screenName = screenName.toLowerCase();
        screenName = TextGlobal.squeezeOutWhiteSpaces(screenName);
        return screenName;
    }

    private String prepareIncomingAskBid(String incomingAskBid){
        //convert incoming ask/bid
        if (incomingAskBid.isEmpty()){
            incomingAskBid = "0";
        }
        return incomingAskBid;
    }

    /**
     * todo: this is disabled. it should be moved to the outside of this class. SQL preparation
     * should be done outside of this package
     * @param product
     * @return
     */
    private String prepareProduct(String product){
        /*
        if ((product != null) && (product.isEmptyLeg())){
            //convert locations
            if (PbsysRuntime.pbsysRuntimeFactory() != null){
                HashMap<String, String> locationMap = PbsysRuntime.pbsysRuntimeFactory().getPbsysParserSettings().getLocationSynonyms();
                if (locationMap.containsKey(product)){
                    product = locationMap.get(product);
                }
            }
        }
         */
        return product;
    }

    private String prepareTimestamp(String timestamp){
        //convert timestamp
        return timestamp.replaceAll(" @ ", " ");
    }

}
