/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.pricer.WhenTechPricerUnavaliableEvent;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import com.eclipsemarkets.data.PointBoxMaturityDate;
import com.eclipsemarkets.data.PointBoxOption;
import com.eclipsemarkets.data.PointBoxQuoteStrategyTerm;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.AbstractPricerWrapper;
import com.eclipsemarkets.pricer.IPointBoxPricer;
import com.eclipsemarkets.pricer.IPointBoxPricingEnvironment;
import com.eclipsemarkets.pricer.IPricingRuntimeManager;
import com.eclipsemarkets.pricer.WtOptionWrapper;
import com.eclipsemarkets.pricer.WtQuoteWrapper;
import com.eclipsemarkets.pricer.whentech.IWtValuationService;
import com.eclipsemarkets.pricer.whentech.WtConfiguration;
import com.eclipsemarkets.pricer.whentech.WtPriceCallback;
import com.eclipsemarkets.pricer.whentech.WtProdConfiguration;
import com.eclipsemarkets.pricer.whentech.WtValuationService;
import com.eclipsemarkets.pricer.whentech.exception.WtValuationServiceConfigurationException;
import com.eclipsemarkets.pricer.whentech.exception.WtValuationServiceRequestException;
import com.eclipsemarkets.pricer.whentech.exception.WtValuationServiceTimeoutException;
import com.eclipsemarkets.pricer.whentech.exception.WtValuationServiceUnavalibaleException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
public class WhenTechPricerWrapper extends AbstractPricerWrapper {

    private static final Logger logger;
    private IWtValuationService wtValuationService;
    static {
        logger = Logger.getLogger(WhenTechPricerWrapper.class.getName());
    }
    /**
     * key = optionString; value = callback
     */
    private final HashMap<String, WtPriceCallback> wtPriceCallbackBuffer;
    
    /**
     * key = "SYMBOL_YYYYMMDD"; Value = WtOptionWrapper which contains future's underlier (i.e. price). This should be synchronized
     */
    private final HashMap<String, WtOptionWrapper> underlierMap;

    public WhenTechPricerWrapper(IPbcKernel kernel) throws WtValuationServiceConfigurationException, 
                                             WtValuationServiceTimeoutException,
                                             WtValuationServiceUnavalibaleException
    {
        super(kernel);
        underlierMap = new HashMap<String, WtOptionWrapper>();
        wtPriceCallbackBuffer = new HashMap<String, WtPriceCallback>();
        WtConfiguration configuration = new WtProdConfiguration();
        //WtConfiguration configuration = new WtUatConfiguration();
        configuration.setReconnectInterval(5);
        configuration.setFileLogPath(NIOGlobal.createFolder("WhenTechLogs"));
        
        wtValuationService = (IWtValuationService)(new WtValuationService(configuration));
//        service.setErrorHandler(new WtErrorHandler(){
//            public void onError(Exception e) {
//                //getKernel().updateSplashScreen("[WhenTech Error] " + e.getMessage(), Level.INFO, 100);
//            }
//
//            public void onError(String errorMessage) {
//                //getKernel().updateSplashScreen("[WhenTech Error] " + errorMessage, Level.INFO, 100);
//            }
//
//            public void onInfo(String message) {
//                getKernel().updateSplashScreen(message, Level.INFO, 100);
//            }
//        });
        connectToLocalWhenTech();
    }

    /**
     * WhenTech pricer never update its own IPointBoxPricingEnvironment since it used the 
     * WhenTech server-side environment.
     * @return 
     */
    @Override
    protected IPointBoxPricingEnvironment getPointBoxPricingEnvironmentInstance() {
        return null;
    }

    @Override
    protected IPointBoxPricer getPricer() {
        return null;
    }

    /**
     * always return false
     * @return 
     */
    @Override
    public boolean isAutoPricing() {
        return false;
    }

    @Override
    protected IPricingRuntimeManager createPricingRuntimeManager() {
        try {
            return new PricingRuntimeManager(kernel, pricingRuntimeFolder);
        } catch (IOException ex) {
            PointBoxTracer.recordSevereException(logger, ex.getMessage(), ex);
            return null;
        }
    }

    final void connectToLocalWhenTech() throws WtValuationServiceTimeoutException, 
                                               WtValuationServiceUnavalibaleException, 
                                               WtValuationServiceConfigurationException 
    {
        try{
            wtValuationService.start();
        }catch(WtValuationServiceConfigurationException ex){
            wtValuationService.stop();
            wtValuationService = null;
            throw ex;
        }
        
        int sec = 0;
        String[] messages = {"Start searching a local WhenTech application ...",
                             "Try to build a channel to a local WhenTech application ... ",
                             "Connecting to a local WhenTech application ... "};
        while(!wtValuationService.isConnected()){
            sec++;
            try {
                Thread.sleep(1000);
                if (sec < 6){
                    kernel.updateSplashScreen(messages[0], 
                            Level.INFO, 0);
                }else if (sec < 10){
                    kernel.updateSplashScreen(messages[1], 
                            Level.INFO, 0);
                }else{
                    kernel.updateSplashScreen(messages[2], 
                            Level.INFO, 0);
                }
            } catch (InterruptedException ex) {
                wtValuationService.stop();
                wtValuationService = null;
                throw new WtValuationServiceTimeoutException(ex.getMessage());
            }
            if (sec > 30){
                wtValuationService.stop();
                wtValuationService = null;
                throw new WtValuationServiceUnavalibaleException("Cannot find a local WhenTech application!");
            }
        }//while
        try {
            Thread.sleep(500);
            if (wtValuationService.isConnected()){
                //initialize future underliers
                initializeFutureUnderliers();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    final void disconnectToLocalWhenTech()
    {
        if (wtValuationService != null){
            wtValuationService.stop();
        }
        wtValuationService = null;
    }

    @Override
    protected Runnable createPricerTask(List<IPbsysOptionQuote> quotes) {
        return new WhenTechPricerTask(kernel, this, quotes);
    }

    @Override
    protected boolean isPricingEngineLoaded() {
        if (wtValuationService == null){
            return false;
        }
        return wtValuationService.isConnected();
    }

    private void loadWtOptionWrapper(List<WtOptionWrapper> aFutureList, String symbol, PointBoxMaturityDate pointBoxMaturityDate) {
        WtOptionWrapper aWtOptionWrapper = new WtOptionWrapper(symbol, 
                                                               PointBoxQuoteStrategyTerm.CALL, 
                                                               pointBoxMaturityDate, 
                                                               0);
        aWtOptionWrapper.setFuture(true);
        aFutureList.add(aWtOptionWrapper);
    }

    private List<WtOptionWrapper> contructFutureListForUnderlierMap() {
        List<WtOptionWrapper> aFutureList = new ArrayList<WtOptionWrapper>();
        GregorianCalendar today = new GregorianCalendar();
        int year = today.get(Calendar.YEAR);
        for (int y = 0; y < 50; y++){
            year += y;
            for (int m = 1; m < 13; m++){
                loadWtOptionWrapper(aFutureList, "NG", new PointBoxMaturityDate(year, m, 1));
                loadWtOptionWrapper(aFutureList, "CL", new PointBoxMaturityDate(year, m, 1));
            }
        }//for
        return aFutureList;
    }

    private void initializeFutureUnderliers() {
        synchronized (underlierMap){
            if (underlierMap.isEmpty()){
                List<WtOptionWrapper> aFutureList = contructFutureListForUnderlierMap();
                String optionString;
                WtPriceCallback wtPriceCallback;
                for (WtOptionWrapper aFuture : aFutureList){
                    optionString = aFuture.getOptionString();
                    if (wtPriceCallbackBuffer.containsKey(optionString)){
                        wtPriceCallback = wtPriceCallbackBuffer.get(optionString);
                        wtPriceCallback.addWtPriceCallbackListener(aFuture);
                    }else{
                        wtPriceCallback = new WtPriceCallback(optionString);
                        wtPriceCallbackBuffer.put(optionString, wtPriceCallback);
                        try {
                                wtPriceCallback.addWtPriceCallbackListener(aFuture);

                                wtValuationService.subscribe(aFuture, wtPriceCallback);

                        } catch (WtValuationServiceRequestException ex) {
                            PointBoxTracer.recordSevereException(logger, 
                                    "Failed to price future for underlier map.", ex);
                        } catch (Exception ex){
                            PointBoxTracer.recordSevereException(logger, 
                                    "Failed to price future for underlier map.", ex);
                        }
                    }
                    underlierMap.put(aFuture.toIdString(), aFuture);
                }//for
            }
        }
    }
    
    private void prepareWtQuoteWrapper(WtQuoteWrapper quoteWrapper){
        IPbsysOptionQuote quote = quoteWrapper.getQuote();
        ArrayList<IPbsysQuoteLeg> legs = quote.getOptionStrategyLegs();
        int count = 0;
        for (IPbsysQuoteLeg leg : legs){
            count++;
            if (count > 2){
                break;
            }else{
                double adjustment = 0.0;
                if (leg.isCrossEmbedded()){
                    adjustment = leg.getOptionCross() - getStripUnderlierForLeg(leg, quote.getPbcPricingModel().getSqCode());
                }
                if (count == 1){
                    quoteWrapper.getQuote().setLeg_01_adjustment(adjustment);
                }else{
                    quoteWrapper.getQuote().setLeg_02_adjustment(adjustment);
                }
            }
        }
    }
    
    private double getStripUnderlierForLeg(IPbsysQuoteLeg leg, String symbol){
        double swap = 0;

        GregorianCalendar gc_start = leg.getOptionContractStartDate();
        GregorianCalendar gc_s = new GregorianCalendar(gc_start.get(Calendar.YEAR), 
                                                       gc_start.get(Calendar.MONTH), 
                                                       gc_start.get(Calendar.DAY_OF_MONTH));
        GregorianCalendar gc_end = leg.getOptionContractEndDate();
        GregorianCalendar gc_e = new GregorianCalendar(gc_end.get(Calendar.YEAR), gc_end.get(Calendar.MONTH), gc_end.get(Calendar.DAY_OF_MONTH));
        int counter = 0;
        if (symbol.equalsIgnoreCase("NG") || symbol.equalsIgnoreCase("LN") || symbol.equalsIgnoreCase("ON")){
            symbol = "NG";
        }else if (symbol.equalsIgnoreCase("CL") || symbol.equalsIgnoreCase("LO")){
            symbol = "CL";
        }
        WtOptionWrapper aWtOptionWrapper;
        while (gc_s.before(gc_e)){
            aWtOptionWrapper = underlierMap.get(symbol+CalendarGlobal.convertToYYYYMMDD(gc_s, ""));
            if (aWtOptionWrapper != null){
                swap += aWtOptionWrapper.getWtPrice();
                counter++;
            }
            gc_s.add(Calendar.MONTH, 1);
        }

        if (counter > 0){
            swap /= counter;
        }

        return swap;
    }

    void invokeWhenTechPricing(WtQuoteWrapper wtQuoteWrapper) {
        if (wtValuationService == null){
            kernel.raisePointBoxEvent(new WhenTechPricerUnavaliableEvent(PointBoxEventTarget.PbcFace,
                                                                   "No connection to the local WhenTech application."));
        }else{
            if (wtQuoteWrapper == null){
                return;
            }
            
            prepareWtQuoteWrapper(wtQuoteWrapper);
            
            //split quote into options
            ArrayList<PointBoxOption> wtOptionLegs = wtQuoteWrapper.getOptionWrapperWithPositions();
            if (wtOptionLegs.isEmpty()){
                return;
            }
            WtPriceCallback wtPriceCallback;
            String optionString;
            for (PointBoxOption optionWrapper : wtOptionLegs){
                optionString = optionWrapper.getOptionString();
                synchronized(wtPriceCallbackBuffer){
                    if (wtPriceCallbackBuffer.containsKey(optionString)){
                        wtPriceCallback = wtPriceCallbackBuffer.get(optionString);
                        wtPriceCallback.addWtPriceCallbackListener(wtQuoteWrapper);
                        ////actually the following line is not necessary because WtValuationService will invoke onPriceUpdate
                        //wtPriceCallback.onPriceUpdate(optionWrapper.getOption(), wtPriceCallback.getPrices());
                    }else{
                        wtPriceCallback = new WtPriceCallback(optionString);
                        wtPriceCallbackBuffer.put(optionString, wtPriceCallback);
                        try {
                                wtPriceCallback.addWtPriceCallbackListener(wtQuoteWrapper);

                                wtValuationService.subscribe(optionWrapper, wtPriceCallback);

                        } catch (WtValuationServiceRequestException ex) {
                            PointBoxTracer.recordSevereException(logger, 
                                    "Quote: " + wtQuoteWrapper.getQuote().getInstantMessage().getMessageContent(), ex);
                        } catch (Exception ex){
                            PointBoxTracer.recordSevereException(logger, 
                                    "Quote: " + wtQuoteWrapper.getQuote().getInstantMessage().getMessageContent(), ex);
                        }
                    }
                }//syn
            }//for
        }
    }
    
    void shutdownWhenTechPricer(){
        if (wtValuationService != null){
            wtValuationService.stop();
        }
    }

    @Override
    protected void priceForSingleQuote(IPbsysOptionQuote aQuote) {
        //do nothing, disabled
    }
    
}
