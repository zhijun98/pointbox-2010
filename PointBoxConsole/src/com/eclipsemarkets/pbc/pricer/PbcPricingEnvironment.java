/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.AbstractPointBoxPricingEnvironment;
import com.eclipsemarkets.pricer.IPricingEnvironmentData;
import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.pricer.data.AtmVolCurveData;
import com.eclipsemarkets.pricer.data.ContractIRCurvesData;
import com.eclipsemarkets.pricer.data.ExpirationsData;
import com.eclipsemarkets.pricer.data.HolidaysData;
import com.eclipsemarkets.pricer.data.IPriceCurveData;
import com.eclipsemarkets.pricer.data.LiborCurveData;
import com.eclipsemarkets.pricer.data.PriceCurveDataNG;
import com.eclipsemarkets.pricer.data.PriceCurveDataPoint;
import com.eclipsemarkets.pricer.data.VolSkewSurfaceData;
import com.eclipsemarkets.pricer.data.VolSkewSurfaceDataPoints;
import com.eclipsemarkets.pricer.data.PbsupportReader;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import com.eclipsemarkets.web.pbc.PricingCurveFileSettings;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract of pricing_runtime for PbcRegularPricer which can be used only at the 
 * PBC side. This class cannot be used at the server-side
 * @author Zhijun Zhang
 */
@SuppressWarnings("UseOfObsoleteCollectionType")
public class PbcPricingEnvironment extends AbstractPointBoxPricingEnvironment{

    //public static final String ext;
    private static final Logger logger;
    private static PbcPricingEnvironment self;
    static {
        logger = Logger.getLogger(PbcPricingEnvironment.class.getName());
        self = null;
    }

    private IPbcKernel kernel;
    
    private PbcPricingEnvironment(IPbcKernel kernel) {
        this.kernel = kernel;
        refreshPricingEnvironment();
    }
    
    static PbcPricingEnvironment getSingleton(IPbcKernel kernel){
        if (self == null){
            self = new PbcPricingEnvironment(kernel);
        }
        return self;
    }

    @Override
    public IPbcKernel getKernel() {
        return kernel;
    }
    
    /**
     * This method is used to re-load (or first-time load) all the data from the pricing settings files for the client-side
     */
    @Override
    public synchronized void refreshPricingEnvironment() {
//        IPointBoxPricingSettings settings = kernel.getPointBoxConsoleRuntime().getPointBoxPricingSettings();
        HashMap<String, PbcPricingModel> aPbcPricingModelMap = kernel.getPointBoxConsoleRuntime().getPbcPricingModelMap();
        IPricingEnvironmentData data;
        boolean result;
        Set<String> codeValues = aPbcPricingModelMap.keySet();
        Iterator<String> itr = codeValues.iterator();
        PbcPricingModel aPbcPricingModel;
        String codeValue; 
        while (itr.hasNext()){
            codeValue = itr.next();
            aPbcPricingModel = aPbcPricingModelMap.get(codeValue);
            //make sure data structure being ready
            data = envData.get(codeValue);
            if (data == null){
                data = new PbcPricingEnvironmentData();
                envData.put(codeValue, data);
            }
            result = loadData(aPbcPricingModel, data);
            if (!result){
                logger.log(Level.WARNING, "{0} failed to load its data.", codeValue);
            }
        }//while
        /**
         * todo: this may cause troubles because it is too restrictive
         * process current codes retrieved from controllers
         */
        //super.setValid(result); //label it is validility
        super.setValid(true);
        
        //PointBoxTracer.recordSevereInformation(logger, ">>> PbcPricingEnvironment-96::initializePricingEnvironment() is ended.");
    }

    private String retrievePointBoxCurveFilePath(PointBoxQuoteCode code, PricingCurveFileSettings[] aPricingCurveFileSettingsArray, PointBoxCurveType pointBoxCurveType) {
        String path = null;
        for (PricingCurveFileSettings aPricingCurveFileSettings : aPricingCurveFileSettingsArray){
            if (pointBoxCurveType.name().equalsIgnoreCase(aPricingCurveFileSettings.getCurveType())){
                path = kernel.getLocalCurveFileFullPath(code, aPricingCurveFileSettings, true);
                break;
            }
        }//for
        return path;
    }

    private boolean loadData(PbcPricingModel aPbcPricingModel, IPricingEnvironmentData data) {
        PricingCurveFileSettings[] aPricingCurveFileSettingsArray = aPbcPricingModel.getPricingCurveFileSettingsArray();
        if (aPricingCurveFileSettingsArray == null){
            return false;
        }
        PointBoxQuoteCode code = PointBoxQuoteCode.convertEnumNameToType(aPbcPricingModel.getSqCode());
        boolean result;
        try{
            result = loadHolidaysData((PbcPricingEnvironmentData)data, 
                                       retrievePointBoxCurveFilePath(code, aPricingCurveFileSettingsArray, PointBoxCurveType.Holidays));
            if (result){
                result = loadExpirations((PbcPricingEnvironmentData)data, 
                                         retrievePointBoxCurveFilePath(code, aPricingCurveFileSettingsArray, PointBoxCurveType.Expirations));
            }
            if (result){
                result = loadUnderlier((PbcPricingEnvironmentData)data, 
                                       retrievePointBoxCurveFilePath(code, aPricingCurveFileSettingsArray, PointBoxCurveType.Underlier));
            }
            if (result){
                result = loadInterestRate((PbcPricingEnvironmentData)data, 
                                           retrievePointBoxCurveFilePath(code, aPricingCurveFileSettingsArray, PointBoxCurveType.InterestRate));
            }
            if (result){
                result = loadAtmVolSurface((PbcPricingEnvironmentData)data, 
                                            retrievePointBoxCurveFilePath(code, aPricingCurveFileSettingsArray, PointBoxCurveType.AtmVolCurve));
            }
            if (result){
                result = loadVolSkewSurface((PbcPricingEnvironmentData)data, 
                                            retrievePointBoxCurveFilePath(code, aPricingCurveFileSettingsArray, PointBoxCurveType.VolSkewSurface));
            }
            if (result){
                data.generateVolSmile();
            }
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
            result = false;
        }
        return result;
    }

    @Override
    public synchronized double queryUnderlierByMonthIndex(String mmddyyyy, PointBoxQuoteCode aPointBoxQuoteCode) {
        if (aPointBoxQuoteCode == null){
            aPointBoxQuoteCode = PointBoxQuoteCode.LN;
        }
        IPricingEnvironmentData data = envData.get(aPointBoxQuoteCode.name());
        if (data == null){
            data = envData.get(PointBoxQuoteCode.LN.name());
        }
        return data.retireveOptionUnderlyingPrice(mmddyyyy);
    }
    
    private synchronized boolean loadHolidaysData(PbcPricingEnvironmentData data, String fileFullPath) {
        if ((data == null) || (fileFullPath == null)){
            return false;
        }
        try{
            HolidaysData holidaysData = PbsupportReader.readHolidaysFile(fileFullPath);
            if ((holidaysData != null) && (holidaysData.getAllHolidaysData() != null)){
                Vector<GregorianCalendar> dates = holidaysData.getAllHolidaysData();
                TreeSet<Date> holdays = data.getHolidays();
                holdays.clear();
                for (GregorianCalendar aDate : dates){
                    holdays.add(CalendarGlobal.convertToDate(aDate));
                }
                return (!holdays.isEmpty());
            }else{
                return false;
            }
        } catch (Exception e) {
            PointBoxTracer.recordSevereException(logger, e.getMessage(), e);
            return false;
        }
    }

    private synchronized boolean loadExpirations(PbcPricingEnvironmentData data, 
                                                 String fileFullPath) {
        if ((data == null) || (fileFullPath == null)){
            return false;
        }
        //fileFullPath = fileFullPath + NIOGlobal.fileSeparator() + aSymbol.name() + "_" + settingsFileType.name() + ext;
        try{
            ExpirationsData expirationsData = PbsupportReader.readExpirationsFile(fileFullPath);
            if ((expirationsData != null) && (expirationsData.getAllContractsData() != null)){
                LinkedHashMap<String, Date> exps = data.getExpirations();
                exps.clear();
                Vector<GregorianCalendar> contracts = expirationsData.getAllContractsData();
                Collections.sort(contracts, new Comparator<GregorianCalendar>(){
                    @Override
                    public int compare(GregorianCalendar o1, GregorianCalendar o2) {
                        return o1.compareTo(o2);
                    }
                });
                LinkedHashMap<GregorianCalendar, GregorianCalendar> pens = expirationsData.getAllPenExpDatesData();
                for (GregorianCalendar contract : contracts){
                    exps.put(CalendarGlobal.convertToMMDDYYYY(contract), 
                             CalendarGlobal.convertToDate(pens.get(contract)));
                    /**
                     * The following line is to help PointBoxForwardCurve::initializeContractSettlesBadly()
                     * 
                     * Search: "PbcPricingEnvironment::loadExpirations" for more details
                     */
                    //System.out.println("insertSettlementItem(\""+CalendarGlobal.convertToMDYYYY(contract)+"\", \""+CalendarGlobal.convertToMDYYYY(pens.get(contract))+"\");");
                }//for
                data.setAllDescriptiveExpData(expirationsData.getAllDescriptiveExpData());
                return (!exps.isEmpty());
            }else{
                return false;
            }
        } catch (Exception e) {
            PointBoxTracer.recordSevereException(logger, e.getMessage(), e);
            return false;
        }
    }

    private synchronized boolean loadUnderlier(PbcPricingEnvironmentData data, String fileFullPath) {
        if ((data == null) || (fileFullPath == null)){
            return false;
        }
        //fileFullPath = fileFullPath + NIOGlobal.fileSeparator() + aSymbol.name() + "_" + settingsFileType.name() + ext;
        try{
            IPriceCurveData cData;
            //the following logic is used to adapt to PointBoxSupport logic
            cData = PbsupportReader.readPriceCurveFile(fileFullPath);
            if (cData == null){
                return false;
            }else{
                LinkedHashMap<String, Double> underliers = data.getUnderliers();
                underliers.clear();
                boolean result = false;
                if (cData instanceof IPriceCurveData){
                    IPriceCurveData aPriceCurveDataCO = (IPriceCurveData)cData;
                    LinkedHashMap<GregorianCalendar, PriceCurveDataPoint> dataMap = aPriceCurveDataCO.getPriceCurveData();
                    Set<GregorianCalendar> keys = dataMap.keySet();
                    Iterator<GregorianCalendar> itr = keys.iterator();
                    GregorianCalendar key;
                    PriceCurveDataPoint value;
                    while(itr.hasNext()){
                        key = itr.next();
                        value = dataMap.get(key);
                        
                        underliers.put(CalendarGlobal.convertMMddyyyy(new Date(key.getTimeInMillis())), 
                                       value.getPrice());
                    }//while
                    result = (!underliers.isEmpty());
                }else if (cData instanceof PriceCurveDataNG){
                    LinkedHashMap<String, PriceCurveDataPoint> allPriceCurveData = cData.getAllPriceCurveData();
                    PriceCurveDataPoint aPriceCurveDataPoint;

                    LinkedHashMap<String, Date> exps = data.getExpirations();
                    int i = 0;
                    Set<String> keys = exps.keySet();
                    Iterator<String> itr = keys.iterator();
                    while(itr.hasNext()){
                        i++;
                        aPriceCurveDataPoint = allPriceCurveData.get("@" + (i) + "ng");
                        if (aPriceCurveDataPoint != null){
                            underliers.put(itr.next(), 
                                       aPriceCurveDataPoint.getPrice());
                        }
                    }
                    result = (!underliers.isEmpty());
                }
                return result;
            }
        } catch (Exception e) {
            PointBoxTracer.recordSevereException(logger, e.getMessage(), e);
            return false;
        }
    }
   
    private synchronized boolean loadInterestRate(PbcPricingEnvironmentData data, String fileFullPath) {
        if ((data == null) || (fileFullPath == null)){
            return false;
        }
       try {
            boolean result = false;
            String irString;
            try {
                irString = NIOGlobal.readFile(fileFullPath);
                irString = irString.trim();
            } catch (Exception ex) {
                return result;
            }
            String[] irArray = irString.split(NIOGlobal.lineSeparator());
            if ((irArray == null) || (irArray.length == 0)){
                return result;
            }
            String[] header = irArray[0].split("\\t");
            //todo-sim: get rid of the format "#MID" files which are used by CO now
            if (header[0].contains("#MID")){    //TN 0.001654
                data.setLibor(true);
                result = loadLiborInterestRate(data, irArray);
            }else{
                data.setLibor(false);
                result = loadContractInterestRate(data, irArray);
            }
            return result;
        } catch (Exception e) {
            PointBoxTracer.recordSevereException(logger, e.getMessage(), e);
            return false;
        }
    }    
    
    private synchronized static GregorianCalendar convertMMddyyyyToGregorianCalendar(String mmddyyyy){
        GregorianCalendar date = new GregorianCalendar();
        if ((mmddyyyy != null) || (mmddyyyy.length() == 8)){
            date.set(Calendar.MONTH, DataGlobal.convertToInteger(mmddyyyy.substring(0,2))-1);
            date.set(Calendar.DAY_OF_MONTH, DataGlobal.convertToInteger(mmddyyyy.substring(2,4)));
            date.set(Calendar.YEAR, DataGlobal.convertToInteger(mmddyyyy.substring(0,2)));
            date.set(Calendar.HOUR, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
        }
        return date;
    }

    private synchronized boolean loadAtmVolSurface(PbcPricingEnvironmentData data, String fileFullPath) {
        if ((data == null) || (fileFullPath == null)){
            return false;
        }
        //fileFullPath = fileFullPath + NIOGlobal.fileSeparator() + aSymbol.name() + "_" + settingsFileType.name() + ext;
        try{
            LinkedHashMap<String, Double> atmData = data.getAtmVolSurface();
            atmData.clear();
            LinkedHashMap<String, Date> exps = data.getExpirations();
            Vector<GregorianCalendar> allContractsData = new Vector<GregorianCalendar>();
            Set<String> keys = exps.keySet();
            Iterator<String> itr = keys.iterator();
            while(itr.hasNext()){
                allContractsData.add(convertMMddyyyyToGregorianCalendar(itr.next()));
            }
            AtmVolCurveData atmCurveData = PbsupportReader.readAtmVolCurveFile(fileFullPath);
            if (atmCurveData == null){
                return false;
            }else{
                atmCurveData.checkToAddContracts(allContractsData);
                LinkedHashMap<GregorianCalendar, Double> atmVols = atmCurveData.getAllAtmVolCurveData();
                Set<GregorianCalendar> k = atmVols.keySet();
                Iterator<GregorianCalendar> it = k.iterator();
                GregorianCalendar key;
                while(it.hasNext()){
                    key = it.next();
                    atmData.put(CalendarGlobal.convertMMddyyyy(new Date(key.getTimeInMillis())), 
                            atmVols.get(key));
                }
                return (!atmData.isEmpty());
            }
        } catch (Exception e) {
            PointBoxTracer.recordSevereException(logger, e.getMessage(), e);
            return false;
        }
    }

    private synchronized boolean loadVolSkewSurface(PbcPricingEnvironmentData data, String fileFullPath) {
        if ((data == null) || (fileFullPath == null)){
            return false;
        }
        //fileFullPath = fileFullPath + NIOGlobal.fileSeparator() + aSymbol.name() + "_" + settingsFileType.name() + ext;
        try{
            LinkedHashMap<String, Date> exps = data.getExpirations();
            Vector<GregorianCalendar> allContractsData = new Vector<GregorianCalendar>();
            Set<String> keys = exps.keySet();
            Iterator<String> itr = keys.iterator();
            while(itr.hasNext()){
                allContractsData.add(convertMMddyyyyToGregorianCalendar(itr.next()));
            }
            VolSkewSurfaceData skewData = PbsupportReader.readVolSkewSurfaceFile(fileFullPath);
            if (skewData == null){
                return false;
            }else{
                skewData.checkToAddContracts(allContractsData);
                
                LinkedHashMap<GregorianCalendar, VolSkewSurfaceDataPoints> points = skewData.getAllVolSkewPointsData();
                LinkedHashMap<String, LinkedHashMap<Double, Double>> skewVolPoints = data.getSkewVolSurface();
                skewVolPoints.clear();
                Set<GregorianCalendar> pointKeys = points.keySet();
                Iterator<GregorianCalendar> pointItr = pointKeys.iterator();
                VolSkewSurfaceDataPoints aVolSkewSurfaceDataPoints;
                GregorianCalendar pointKey;
                String pointKeyStr;
                while(pointItr.hasNext()){
                    pointKey = pointItr.next();
                    pointKeyStr = CalendarGlobal.convertToMMDDYYYY(pointKey);
                    aVolSkewSurfaceDataPoints = points.get(pointKey);
                    skewVolPoints.put(pointKeyStr, aVolSkewSurfaceDataPoints.getAllSkewDataPoints());
                }
                //strikes
                data.setSkewVolStrikes(skewData.getAllVolStrikesData());
                return (!skewVolPoints.isEmpty());
            }
        } catch (Exception e) {
            PointBoxTracer.recordSevereException(logger, e.getMessage(), e);
            return false;
        }
    }

    private synchronized boolean loadContractInterestRate(PbcPricingEnvironmentData data, String[] irArray) {
        LinkedHashMap<String, Double> irData = data.getInterestRates();
        irData.clear();
        ContractIRCurvesData aContractIRCurvesData = new ContractIRCurvesData(irArray);
        LinkedHashMap<GregorianCalendar, Double> vIR = aContractIRCurvesData.getContractIRCurvesData();
        return convertContractIRToGenaralIRDataList(irData, vIR);
    }

    private synchronized boolean loadLiborInterestRate(PbcPricingEnvironmentData data, String[] irArray) throws Exception{
        TreeSet<Date> holidayData = data.getHolidays();
        LiborCurveData aLiborCurveData = new LiborCurveData(irArray);
        if (aLiborCurveData == null){
            return false;
        }else{
            //get aLiborCurveData
            Vector<GregorianCalendar> holidays = new Vector<GregorianCalendar>();
            Iterator<Date> itr = holidayData.iterator();
            while(itr.hasNext()){
                holidays.add(CalendarGlobal.convertDateToGregorianCalendar(itr.next()));
            }
            aLiborCurveData.setUpInterpolationData(holidays);
            //convert to GenaralIRData...
            Vector<GregorianCalendar> liborDays = aLiborCurveData.getLiborDates();
            Vector<Double> interpDays = aLiborCurveData.getInterpExpirationDays();
            Vector<Double> irs = aLiborCurveData.getInterpInterestRates();
            if ((liborDays != null) && (interpDays != null) && (irs != null) 
                    && (liborDays.size() == interpDays.size())
                    && (interpDays.size() == irs.size()))
            {
                data.setLiborCurveData(aLiborCurveData);
                return true;
            }else{
                throw new Exception("Cannot successfully parse out settings file");
            }
        }
    }

    private synchronized boolean convertContractIRToGenaralIRDataList(LinkedHashMap<String, Double> irData, 
            LinkedHashMap<GregorianCalendar, Double> data) 
    {
        Set<GregorianCalendar> keys = data.keySet();
        Iterator<GregorianCalendar> itr = keys.iterator();
        GregorianCalendar key;
        Double value;
        while(itr.hasNext()){
            key = itr.next();
            value = data.get(key);
            
            irData.put(CalendarGlobal.convertMMddyyyy(new Date(key.getTimeInMillis())), 
                       value);
        }//while
        return (!irData.isEmpty());
    }
    
}
