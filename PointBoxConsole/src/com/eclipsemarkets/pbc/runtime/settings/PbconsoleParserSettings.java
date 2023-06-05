/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.runtime.IPointBoxParserSettings;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IPointBoxConsoleUser;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.runtime.PointBoxClientRuntimeFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
class PbconsoleParserSettings extends PbconsoleSettings implements IPbconsoleParserSettings, IPbcSettings{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbconsoleParserSettings.class.getName());
    }
    
    private final IPointBoxParserSettings parserSettings;
    
    //The information for the range "powerBrokers" in the original Excel
    private ArrayList<String> powerBrokers;
    private HashMap<String, IPointBoxConsoleUser> oilBrokers;

    PbconsoleParserSettings(IPbcRuntime runtime) {
        super(runtime);
        
        parserSettings = PointBoxClientRuntimeFactory.createPointBoxParserSettingsInstance();
        
        initializePowerBrokers();
        initializegetRegisteredRemoteUsers();
    }

    @Override
    public int getMonthNumberFromQuoteMonth(String schar) {
        return parserSettings.getMonthNumberFromQuoteMonth(schar);
    }

    @Override
    public int getMonthNumberFromQuoteMonth(char s) {
        return parserSettings.getMonthNumberFromQuoteMonth(s);
    }

    @Override
    public synchronized String cleanByRemoveExpressions(String targetQtMsg) {
        return parserSettings.cleanByRemoveExpressions(targetQtMsg);
    }

    @Override
    public synchronized String cleanByRemoveWordList(String targetQtMsg) {
        return parserSettings.cleanByRemoveWordList(targetQtMsg);
    }

    @Override
    public String confirmMonthCharacter(String em) {
        return parserSettings.confirmMonthCharacter(em);
    }

    @Override
    public String getMonthLetter(int month) {
        return parserSettings.getMonthLetter(month);
    }

    @Override
    public String getRegex(String gNumberKey) {
        return parserSettings.getRegex(gNumberKey);
    }

    public PbcSettingsType getPbcSettingsType() {
        return PbcSettingsType.PbconsoleParserSettings;
    }

    @Override
    public void loadPersonalSettings() {
    }

    @Override
    public void storePersonalSettings() {
    }

    private void initializePowerBrokers(){
        powerBrokers = new ArrayList<String>();
        powerBrokers.add("amerextucker");
        powerBrokers.add("amerexcliff");
        powerBrokers.add("mikedicap");
        powerBrokers.add("otisjoel");
        powerBrokers.add("kirkd100");
        powerBrokers.add("petespec");
    }

    @Override
    public HashMap<String, String> getBuySellParsingKeywords() {
        return parserSettings.getBuySellParsingKeywords();
    }

    /**
     * This method assume the format of "Brokers.imp" in advance
     */
    private synchronized void initializegetRegisteredRemoteUsers(){
        BufferedReader brokerReader = null;
        oilBrokers = new HashMap<String, IPointBoxConsoleUser>();
        String oilBrokersFilePath = "N:\\New York\\Oil\\Data\\brokersOil.imp";
        if ((oilBrokersFilePath != null) && (!oilBrokersFilePath.isEmpty())){
            File brokerFile = new File(oilBrokersFilePath);
            if (brokerFile.exists()){
                try {
                    brokerReader = new BufferedReader(new FileReader(brokerFile));
                    String aLine;
                    int counter = 0;    //1: IM; 2: house; 3: type
                    IPointBoxConsoleUser aBroker = null;
                    while (true) {
                        aLine = brokerReader.readLine();
                        if (aLine == null){
                            break;
                        }

                        switch (counter){
                            case 1: //IM
                                aBroker = GatewayBuddyListFactory.getLoginUserInstance(
                                        aLine.trim(), GatewayServerType.YIM_SERVER_TYPE);
                                break;
                            case 2: //house
                                if (aBroker != null){
                                    aBroker.setHouse(aLine.trim());
                                }
                                break;
                            case 3: //type
                                if (aBroker != null){
                                    aBroker.setHouseType(aLine.trim());
                                    oilBrokers.put(aBroker.getIMScreenName(), aBroker);
                                    counter = 0;
                                }
                                break;
                        }
                        counter++;
                    } //while
                    brokerReader.close();
                } catch (FileNotFoundException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    try {
                        brokerReader.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            }//if
        }
    }
    
    @Override
    public HashMap<String, String> getCheckIndexes() {
        return parserSettings.getCheckIndexes();
    }

    @Override
    public HashMap<String, String> getCheckLocations() {
        return parserSettings.getCheckLocations();
    }

    @Override
    public HashMap<String, String> getFenceVsFence() {
        return parserSettings.getFenceVsFence();
    }

    @Override
    public ArrayList<String> getKeywordsNotForPricing() {
        return parserSettings.getKeywordsNotForPricing();
    }

    @Override
    public ArrayList<String> getKeywordsNotForPricingOil() {
        return parserSettings.getKeywordsNotForPricingOil();
    }

    @Override
    public synchronized HashMap<String, IPointBoxConsoleUser> getOilBrokers() {
        HashMap<String, IPointBoxConsoleUser> dataCopy = new HashMap<String, IPointBoxConsoleUser>();
        Set<String> keys = oilBrokers.keySet();
        Iterator<String> itr = keys.iterator();
        String key;
        while (itr.hasNext()){
            key = itr.next();
            dataCopy.put(key, oilBrokers.get(key));
        }
        return dataCopy;
    }

    @Override
    public LinkedHashMap<String, String> getOptionTypes() {
        return parserSettings.getOptionTypes();
    }

    @Override
    public LinkedHashMap<String, String> getQuoteMonthMappings() {
        return parserSettings.getQuoteMonthMappings();
    }

    @Override
    public ArrayList<String> getPowerBrokers() {
        return DataGlobal.copyArrayListString(powerBrokers);
    }

    @Override
    public HashMap<String, String> getProductMap() {
        return parserSettings.getProductMap();
    }

    @Override
    public HashMap<String, String> getRollStructureMappings() {
        return parserSettings.getRollStructureMappings();
    }

    @Override
    public ArrayList<String> getStructuresBase() {
        return parserSettings.getStructuresBase();
    }

    @Override
    public ArrayList<String> getStructuresPatch01() {
        return parserSettings.getStructuresPatch01();
    }

    @Override
    public ArrayList<String> getThreeWaysSplitters() {
        return parserSettings.getThreeWaysSplitters();
    }

    @Override
    public ArrayList<String> getThreeWaysSplittersOil() {
        return parserSettings.getThreeWaysSplittersOil();
    }

    @Override
    public HashMap<String, String> getLocationSynonyms() {
        return parserSettings.getLocationSynonyms();
    }

    @Override
    public ArrayList<String> getBuddyNamesForNoPricing() {
        return getPowerBrokers();
    }

    @Override
    public HashMap<String, IPointBoxConsoleUser> getSecurityTypedUsers() {
        return this.getOilBrokers();
    }

    @Override
    public int getMonthNumberFromQuoteQuarter(char s) {
        return parserSettings.getMonthNumberFromQuoteQuarter(s);
    }

    @Override
    public int getMonthNumberFromQuoteQuarter(String schar) {
        return parserSettings.getMonthNumberFromQuoteQuarter(schar);
    }

}
