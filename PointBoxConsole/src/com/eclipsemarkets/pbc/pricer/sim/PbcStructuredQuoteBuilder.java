/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxOption;
import com.eclipsemarkets.data.PointBoxQuoteStrategyTerm;
import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.PointBoxOptionPosition;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.parser.PbcSimGuiParser;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;

/**
 *
 * @author Zhijun Zhang, date & time: May 1, 2014 - 1:22:42 PM
 */
public abstract class PbcStructuredQuoteBuilder extends javax.swing.JFrame implements IPbcStructuredQuoteBuilder {

    private IPbcKernel kernel;
    
    private PointBoxQuoteType pointBoxQuoteType;

    public PbcStructuredQuoteBuilder(IPbcKernel kernel, PointBoxQuoteType type) {
        
        this.kernel = kernel;
        this.pointBoxQuoteType = type;
    }
    
    /**
     * This token is used to (1) identify if the message text was constructed by 
     * PbcStructuredQuoteBuilder; (2) embedded every GUI field values in the mark. 
     * @return - if the quote-message text is not ready, NULL is returned;
     */
    String generateSimMarkToken() {
        String fieldsToken = generateSimMarkFieldValuesToken();
        if (fieldsToken == null){
            return null;
        }
        return fieldsToken + PbcSimGuiParser.SimMarkPartDelimiter + PbcSimGuiParser.BaseSimMark;
    }
    
    abstract String generateSimMarkFieldValuesToken();

    @Override
    public PointBoxQuoteType getPointBoxQuoteType() {
        return pointBoxQuoteType;
    }

    @Override
    public IPbcKernel getKernel() {
        return kernel;
    }

    /**
     * 
     * @param quote
     * @param legIndex
     * @param optionFieldArray, e.g. {3.5, p, -1.0}, whose length is exactly 3
     * @return 
     */
    static PointBoxOption createPointBoxOption(IPbsysOptionQuote quote, int legIndex, String[] optionFieldArray) {
        double strikePrice = DataGlobal.convertToDouble(optionFieldArray[0]);
        double ratio = DataGlobal.convertToDouble(optionFieldArray[2]);
        PointBoxOptionPosition optionPosition;
        if (ratio < 0){
            optionPosition = PointBoxOptionPosition.Short;
        }else{
            optionPosition = PointBoxOptionPosition.Long;
        }
        PointBoxQuoteStrategyTerm cpValue = PointBoxQuoteStrategyTerm.convertEnumValueToType(optionFieldArray[1]);
        
        PointBoxOption aPointBoxOption = new PointBoxOption(quote.getPbcPricingModel().getSqCode(),
                                  optionPosition, cpValue, null, strikePrice, legIndex);
        aPointBoxOption.setRatio(ratio);
        return aPointBoxOption;
    }
    
//    /**
//     * 
//     * @param legMsg - [Start?]-[End?] [Strike?]/[Strike?]/[Strike?]/[Strike?]/[Strike?] structure LIVE
//     * [Start?]-[End?] [Strike?]/[Strike?]/[Strike?]/[Strike?]/[Strike?] structure [Ratio?]x[Ratio?]x[Ratio?]x[Ratio?]x[Ratio?] LIVE
//     * @param quoteLeg 
//     */
//    private static void parsePbsysQuoteLeg(String legMsg, IPbsysQuoteLeg quoteLeg) {
//        String[] tokenArray = legMsg.split(PbcStructuredQuoteBuilder.WhiteSpace);
//        if (tokenArray == null){
//            return;
//        }
//        boolean hasRatio;
//        if (tokenArray.length == 4){
//            hasRatio = false;
//        }else if (tokenArray.length == 5){
//            hasRatio = true;
//        }else{
//            return;
//        }
//        
//        parseContractPeriodHelper(tokenArray[0], quoteLeg);
//        parseStrikeValuesHelper(tokenArray[1], quoteLeg);
//        quoteLeg.setOptionStrategy(tokenArray[2]);
//        if (hasRatio){
//            parseRatioValuesHelper(tokenArray[3], quoteLeg);
//            parseCrossValueHelper(tokenArray[4], quoteLeg);
//        }else{
//            parseRatioValuesHelper(null, quoteLeg);
//            parseCrossValueHelper(tokenArray[3], quoteLeg);
//        }
//    }
//
//    /**
//     * 
//     * @param ratioString - if NULL, it means it should be 1 for each field corresponding to strike
//     * @param quoteLeg 
//     */
//    private static void parseRatioValuesHelper(String ratioString, IPbsysQuoteLeg quoteLeg) {
//        if (ratioString == null){
//            double[] strikers = quoteLeg.getOptionStrikes();
//            if (strikers != null){
//                for (int i = 0; i < strikers.length; i++){
//                    if (strikers[i] != 0){
//                        quoteLeg.setOptionRatios(i, 1);
//                    }
//                }//for
//            }
//        }else{
//            String[] ratioArray = ratioString.split(PbcStructuredQuoteBuilder.RatioDelimiter);
//            if (ratioArray != null){
//                for (int i = 0; i < ratioArray.length; i++){
//                    quoteLeg.setOptionRatios(i, DataGlobal.convertToDouble(ratioArray[i]));
//                }
//            }
//        }
//    }
//
//    private static void parseCrossValueHelper(String crossString, IPbsysQuoteLeg quoteLeg) {
//        if (PbcStructuredQuoteBuilder.LiveCross.equalsIgnoreCase(crossString)){
//        
//        }else if (crossString.startsWith(PbcStructuredQuoteBuilder.XCross)){
//            quoteLeg.setOptionCross(DataGlobal.convertToDouble(crossString.substring(1)));
//        }
//    }
//
//    private static void parseStrikeValuesHelper(String strikeString, IPbsysQuoteLeg quoteLeg) {
//        String[] strikerArray = strikeString.split(PbcStructuredQuoteBuilder.DataDelimiter);
//        if (strikerArray != null){
//            for (int i = 0; i < strikerArray.length; i++){
//                quoteLeg.setOptionStrikes(i, DataGlobal.convertToDouble(strikerArray[i]));
//            }//for
//        }
//    }

//    private static void parseContractPeriodHelper(String calendarString, IPbsysQuoteLeg quoteLeg) {
//        String[] calendarArray = calendarString.split(PbcStructuredQuoteBuilder.CalendarDelimiter);
//        if (calendarArray == null){
//            return;
//        }else{
//            if (calendarArray.length == 2){
//                quoteLeg.setOptionContractStartDate(CalendarGlobal.convertFinancialDateToGregorianCalendar(calendarArray[0]));
//                quoteLeg.setOptionContractEndDate(CalendarGlobal.convertFinancialDateToGregorianCalendar(calendarArray[1]));
//            }else if (calendarArray.length == 1){
//                quoteLeg.setOptionContractStartDate(CalendarGlobal.convertFinancialDateToGregorianCalendar(calendarArray[0]));
//                GregorianCalendar endTime = new GregorianCalendar();
//                endTime.setTimeInMillis(quoteLeg.getOptionContractStartDate().getTimeInMillis());
//                quoteLeg.setOptionContractEndDate(CalendarGlobal.convertToEndOfMonth(endTime));
//            }
//        }
//    }
//    
//    private static void parseQuoteLegsAndLoadFields(IPbsysOptionQuote quote){
//        String qmsg = quote.getInstantMessage().getMessageContent();
//        ArrayList<Boolean> customStatusList = parseLegCustomStrategyStatusList(qmsg);
//        qmsg = PbcStructuredQuoteBuilder.trimOffSimLabel(qmsg);
//        qmsg = PbcStructuredQuoteBuilder.trimOffLeadingCode(qmsg);
//        String[] legMessageArray = parseLegMessageArray(qmsg);
//        while (customStatusList.size() < legMessageArray.length){
//            customStatusList.add(false); //default is non-custom
//        }
//        for (int index = 0; index < legMessageArray.length; index++){
//            parsePbsysQuoteLeg(legMessageArray[index], quote.getOptionStrategyLegs().get(index));
//        }
//    }
//    
//    private static String[] parseLegMessageArray(String qmsgWithoutSimLabel){
//        qmsgWithoutSimLabel = qmsgWithoutSimLabel.substring(qmsgWithoutSimLabel.indexOf(PbcStructuredQuoteBuilder.WhiteSpace)+1);
//        qmsgWithoutSimLabel = qmsgWithoutSimLabel.substring(0, qmsgWithoutSimLabel.lastIndexOf(PbcStructuredQuoteBuilder.WhiteSpace));
//        if (qmsgWithoutSimLabel.contains(PbcStructuredQuoteBuilder.LegDelimiter)){
//            return qmsgWithoutSimLabel.split(PbcStructuredQuoteBuilder.LegDelimiter);
//        }else{
//            return new String[]{qmsgWithoutSimLabel.trim()};
//        }
//    }
//    
//    private static void parseBidAskAndLoadFields(IPbsysOptionQuote quote) {
//        String originalMessage = quote.getInstantMessage().getMessageContent();
//        if (!PbcStructuredQuoteBuilder.isBidAskEmbedded(originalMessage)){
//            return;
//        }
//        //trimming off SimLabel
//        String qmsgWithoutSimLabel = PbcStructuredQuoteBuilder.trimOffSimLabel(originalMessage);
//        String bidAskString = qmsgWithoutSimLabel.substring(qmsgWithoutSimLabel.lastIndexOf(PbcStructuredQuoteBuilder.WhiteSpace)+1);
//        String[] bidAskStringArray = bidAskString.split(PbcStructuredQuoteBuilder.DataDelimiter);
//        if (bidAskStringArray != null){
//            if(bidAskStringArray.length == 1){ //e.g. "7.8/"
//                quote.setOptionBidPricePrivateIncoming(bidAskStringArray[0]);
//            }
//            if ((bidAskStringArray.length == 2)){   //e.g. "7.8/8.5" or "/8.5"
//                quote.setOptionBidPricePrivateIncoming(bidAskStringArray[0]);
//                quote.setOptionAskPricePrivateIncoming(bidAskStringArray[1]);
//            }
//        }else{
//            quote.setOptionBidPricePrivateIncoming("");
//            quote.setOptionAskPricePrivateIncoming("");
//        }
//    }
}
