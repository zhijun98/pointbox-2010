/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.parser.QuoteParsedEvent;
import com.eclipsemarkets.event.pricer.PBPricerChangedEvent;
import com.eclipsemarkets.event.pricer.WhenTechPricerUnavaliableEvent;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.exceptions.PointBoxTimeoutException;
import com.eclipsemarkets.pbc.PbcComponent;
import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.AbstractPricerWrapper;
import com.eclipsemarkets.pricer.IPbcPricingAgent;
import com.eclipsemarkets.pricer.PbcPricerType;
import com.eclipsemarkets.pricer.whentech.exception.WtValuationServiceConfigurationException;
import com.eclipsemarkets.pricer.whentech.exception.WtValuationServiceTimeoutException;
import com.eclipsemarkets.pricer.whentech.exception.WtValuationServiceUnavalibaleException;
import com.eclipsemarkets.web.PbcAccountBasedSettings;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PbcPricingAgent
 * <P>
 * This pricer is 100% local pricing component used by PBC. Thus, if PbcPricerType is 
 * not server-like, this pricer will do nothing. 
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 25, 2011 at 10:43:10 AM
 */
public class PbcPricingAgent extends PbcComponent implements IPbcPricingAgent {
    private static final Logger logger;
    private static IPbcPricingAgent self;
    static{
        logger = Logger.getLogger(PbcPricingAgent.class.getName());
        self = null;
    }

    private AbstractPricerWrapper regularPricerWrapper;
    private SimPricerWrapper simPricerWrapper;
    private AbstractPricerWrapper autoPricerWrapper;
    private WhenTechPricerWrapper whenTechPricer;
    private final ArrayList<IPbsysOptionQuote> regularBuffer;
    private final LinkedList<IPbsysOptionQuote> autoBuffer;
    
    private Thread pricingCurveUploadingThread;
    private Thread regularPricingThread;
    private Thread autoPricingThread;
    private Thread whenTechPricingThread;
    
    private final PbcPricerTypeLock pricerTypeLock;
    
    private boolean pricingProhibitted;

    private PbcPricingAgent(IPbcKernel kernel) {
        super(kernel);
        
        pricingProhibitted = false;
        
        kernel.updateSplashScreen("Register " + getKernel().getSoftwareName() + "'s pricer component...", Level.INFO, 100);
        regularBuffer = new ArrayList<IPbsysOptionQuote>();
        autoBuffer = new LinkedList<IPbsysOptionQuote>();
        
        pricerTypeLock = new PbcPricerTypeLock(PbcPricerType.PB);
        
        pricingCurveUploadingThread = null;
        regularPricingThread = null;
        autoPricingThread = null;
        whenTechPricingThread = null;
        
        whenTechPricer = null;
    }
    
    public static IPbcPricingAgent getPbcPricingAgentSingleton(IPbcKernel kernel) throws PointBoxFatalException  {
        if (self == null){
            if (kernel != null){
                self = new PbcPricingAgent(kernel);
            }
        }
        return self;
    }

    /**
     * @param mmddyyyy - first-day of a month e.g. 04012014
     * @param code
     * @return 
     */
    @Override
    public double queryUnderlierByMonthIndex(String mmddyyyy, PointBoxQuoteCode code) {
        return this.getRegularPricerWrapper().queryUnderlierByMonthIndex(mmddyyyy, code);
    }

    /**
     * Refresh curve-data in memory used by pricer. If the curve files are the latest, nothing happens. 
     * If any curve was changed, it will refresh the curve-data in memory which could be a little bit time consuming
     */
    @Override
    public void refreshPricingEnvironment() {
        getAutoPricerWarpper().refreshPricingEnvironmentImmediately();
    }

    @Override
    public List<Object[]> retireveOptionInterestRates(PointBoxQuoteCode code) {
        return this.getRegularPricerWrapper().retireveOptionInterestRates(code);
    }

    @Override
    public LinkedHashMap<GregorianCalendar, String> retrieveAllDescriptiveExpirationData(PointBoxQuoteCode code) {
        return this.getRegularPricerWrapper().retrieveAllDescriptiveExpirationData(code);
    }

    @Override
    public HashMap<String, HashMap<String, String>> retrieveAllDescriptiveExpirationData() {
        return this.getRegularPricerWrapper().retrieveAllDescriptiveExpirationData();
    }

    @Override
    public HashMap<String, TreeMap<String, GregorianCalendar>> retrieveAllExpirationData() {
        return this.getRegularPricerWrapper().retrieveAllExpirationData();
    }

    @Override
    public boolean connectToLocalWhenTech() {
        if ((whenTechPricer != null) && (whenTechPricer.isPricingEngineLoaded())){
            return true;
        }else{
            try {
                whenTechPricer = new WhenTechPricerWrapper(getKernel());
            } catch (WtValuationServiceTimeoutException ex) {
                return false;
            } catch (WtValuationServiceUnavalibaleException ex) {
                return false;
            } catch (WtValuationServiceConfigurationException ex) {
                return false;
            } 
            startWtPricingThread();
            return true;
        }
    }

    @Override
    public void suspendPricing() {
        synchronized(this){
            pricingProhibitted = true;
        }
    }

    @Override
    public void resumePricing() {
        synchronized(this){
            pricingProhibitted = false;
        }
    }

    public boolean isPricingProhibitted() {
        synchronized(this){
            return pricingProhibitted;
        }
    }

    private void pricingBufferedQuotes(final AbstractPricerWrapper pricerWarpper, final List<IPbsysOptionQuote> quotes) {
        if (isPricingProhibitted()){
            return;
        }
        if ((quotes != null) && (!quotes.isEmpty())){
            /**
             * call PbcPricerWrapper::pricing();
             */
            pricerWarpper.pricing(quotes);
        }
    }

    private void bufferQuotes(final List<IPbsysOptionQuote> buffer, ArrayList<IPbsysOptionQuote> quotes){
        if (pricerTypeLock.getPricerType().equals(PbcPricerType.PBS)){
            //logger.log(Level.INFO, "PBC is using server-side pricing engine.");
            List<IPbsysOptionQuote> pricedBuffer = new ArrayList<IPbsysOptionQuote>();
            for (IPbsysOptionQuote quote : quotes){
                if (quote.isSufficientPricingData()){
                    pricedBuffer.add(quote);
                }//otherwise, simply drop it
            }//for
            return;
        }
        //logger.log(Level.INFO, "PBC is using local pricing engine.");
        synchronized(buffer){
            for (IPbsysOptionQuote quote : quotes){
                if (quote.isSufficientPricingData()){
                    buffer.add(quote);
                }//otherwise, simply drop it
            }//for
        }
    }

    private List<IPbsysOptionQuote> retrieveQuotes(final List<IPbsysOptionQuote> buffer){
        if (pricerTypeLock.getPricerType().equals(PbcPricerType.PBS)){
            return new ArrayList<IPbsysOptionQuote>();
        }
        ArrayList<IPbsysOptionQuote> quotes = new ArrayList<IPbsysOptionQuote>();
        synchronized(buffer){
            for (IPbsysOptionQuote quote : buffer){
                quotes.add(quote);
            }//for
            if (buffer instanceof ArrayList<?>){
                //regularBuffer does not keep processed-quotes
                buffer.clear();
            }else{ 
                //autoBuffer keep quotes until it surpasses maxSize
                int maxSize = getKernel().getPointBoxConsoleRuntime().getPricingEngineSettings().getAutoPricerConfig().getLatestQuoteNumber();
                if (buffer.size() > maxSize){
                    while (buffer.size() > maxSize){
                        ((LinkedList<IPbsysOptionQuote>)buffer).removeFirst();
                    }//while
                }
            }
        }
        return quotes;
    }

    /**
     * calculating quotes, blocking and waiting for completion...and eventually return.
     * @param quotes
     */
    @Override
    public void evaluateQuotesPrice(ArrayList<IPbsysOptionQuote> quotes) {
        getRegularPricerWrapper().pricing(quotes);
    }

    @Override
    public void evaluatePriceForPbsysOptionQuote(IPbsysOptionQuote aQuote) {
        /**
         * todo-sim: how about "Custom Code" which is UNKNOWN???? How about its pricer type???
         */
        getSimPricerWrapper().setSimCode(PointBoxQuoteCode.convertEnumNameToType(aQuote.getPbcPricingModel().getSqCode()));
        getSimPricerWrapper().checkupPricingEnvironmentAndRefresh();
        getSimPricerWrapper().pricing(aQuote);
    }

    @Override
    public void load() throws PointBoxFatalException {
        getKernel().updateSplashScreen("Load " + getKernel().getSoftwareName() + "'s pricer ...", Level.INFO, 100);
    }

    @Override
    public void personalize() {
        getKernel().updateSplashScreen("Personalize " + getKernel().getSoftwareName() + "'s pricer ...", Level.INFO, 100);
        if (regularPricerWrapper == null){
            regularPricerWrapper = new PbcPricerWrapper(getKernel(), false);
        }
        if (simPricerWrapper == null){
            simPricerWrapper = new SimPricerWrapper(getKernel(), false);
        }
        if (autoPricerWrapper == null){
            autoPricerWrapper = new PbcPricerWrapper(getKernel(), true);
        }
        getKernel().retrievePbcAccountBasedSettings();
        startPbPricingThreads();
    }
    
    private AbstractPricerWrapper getAutoPricerWarpper(){
        return autoPricerWrapper;
    }
    
    private AbstractPricerWrapper getRegularPricerWrapper(){
        return regularPricerWrapper;
    }

    private SimPricerWrapper getSimPricerWrapper() {
        return simPricerWrapper;
    }

    @Override
    public void unload() {
        getKernel().updateSplashScreen("Unload " + getKernel().getSoftwareName() + "'s pricer ...", Level.INFO, 100);
        stopPbPricingThreads();
        stopWtPricingThread();
        if (whenTechPricer != null){
            whenTechPricer.shutdownWhenTechPricer();
        }
    }

    @Override
    public void handleComponentEvent(PointBoxConsoleEvent event) {
        if (event == null){
            return;
        }
        if (event instanceof PBPricerChangedEvent){
            PBPricerChangedEvent evt = (PBPricerChangedEvent)event;
            synchronized (pricerTypeLock){
                pricerTypeLock.setPricerType(evt.getNewPricerType());
            }
        }else if (event instanceof QuoteParsedEvent){
            QuoteParsedEvent qpe = (QuoteParsedEvent)event;
            bufferQuotes(regularBuffer, qpe.getParsedQuotes());
            bufferQuotes(autoBuffer, qpe.getParsedQuotes());
        }
    }

    /**
     * todo-sim: this method and its design on the server-side should be changed thoroughly
     */
    private void startPbPricingThreads() {
        final PbcAccountBasedSettings aPbcPricingAdminSettings = this.getKernel().getPointBoxAccountID().getPbcAccountBasedSettings();
        if ((pricingCurveUploadingThread == null) && (aPbcPricingAdminSettings != null) && (aPbcPricingAdminSettings.getUploadCurveType() == 2)){
            if (aPbcPricingAdminSettings.getFrequency() < 10){
                aPbcPricingAdminSettings.setFrequency(10);
            }
            pricingCurveUploadingThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    int frq = aPbcPricingAdminSettings.getFrequency();
                    try {
                        Thread.sleep(frq * 1000);
                    } catch (InterruptedException ex) {
                    }
                    while(true){
                        PbcAccountBasedSettings refreshedPbcPricingAdminSettings = getKernel().retrievePbcAccountBasedSettings();
                        if (refreshedPbcPricingAdminSettings.getUploadCurveType() != 2){
                            //it was changed from the controller
                            break;
                        }
                        frq = refreshedPbcPricingAdminSettings.getFrequency();
                        if (frq < 10){
                            frq = 10;
                        }
                        synchronized (pricerTypeLock){
                            if (pricerTypeLock.getPricerType().equals(PbcPricerType.PB)){
                                getKernel().uploadAllEmsCurves();
                                //getKernel().uploadLegacyEmsCurves();
                            }
                        }//syn
                        try {
                            Thread.sleep(frq * 1000);
                        } catch (InterruptedException ex) {
                            //PointBoxTracer.recordSevereException(logger, ex);
                            break;
                        }
                    }//while
                }//run
            });
        }
        if (regularPricingThread == null){
            regularPricingThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException ex) {
                    }
                    while(true){
                        synchronized (pricerTypeLock){
                            if (pricerTypeLock.getPricerType().equals(PbcPricerType.PB)){
                                final List<IPbsysOptionQuote> quotes = retrieveQuotes(regularBuffer);
                                pricingBufferedQuotes(getRegularPricerWrapper(), quotes);
                            }
                        }//syn
                        try {
                            Thread.sleep(750);
                        } catch (InterruptedException ex) {
                            //PointBoxTracer.recordSevereException(logger, ex);
                            break;
                        }
                    }//while
                }//run
            });
        }
        if (autoPricingThread == null){
            autoPricingThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(getKernel().getPointBoxConsoleRuntime().getPricingEngineSettings().getAutoPricerConfig().getInitialDelay());
                    } catch (InterruptedException ex) {
                    }
                    while(true){
                        //logger.log(Level.INFO, "autoPricer is working ...@{0}", CalendarGlobal.getCurrentHHmmss());
                        synchronized (pricerTypeLock){
                            if (pricerTypeLock.getPricerType().equals(PbcPricerType.PB)){
                                ////System.out.println("regularPricingThread -> pricing ......");
                                final List<IPbsysOptionQuote> quotes = retrieveQuotes(autoBuffer);
                                pricingBufferedQuotes(getAutoPricerWarpper(), quotes);
                            }
                        }//syn
                        try {
                            long sec = getKernel().getPointBoxConsoleRuntime().getPricingEngineSettings().getAutoPricerConfig().getPricingFrequency();
                            if (sec < 5000){
                                sec = 5000;
                            }
                            Thread.sleep(sec);
                        } catch (InterruptedException ex) {
                            //PointBoxTracer.recordSevereException(logger, ex);
                            break;
                        }
                    }//while
                }//run
            });
        }
        if ((pricingCurveUploadingThread != null) && (!pricingCurveUploadingThread.isAlive())){
            pricingCurveUploadingThread.start();
        }
        if ((regularPricingThread != null) && (!regularPricingThread.isAlive())){
            //logger.log(Level.INFO, ">>> regularPricingThread is started now .....");
            regularPricingThread.start();
        }
        if ((autoPricingThread != null) && (!autoPricingThread.isAlive())){
            autoPricingThread.start();
        }
    }

    private void stopPbPricingThreads() {
        if (pricingCurveUploadingThread != null){
            try {
                NIOGlobal.stopThread(pricingCurveUploadingThread, 1*1000);
            } catch (InterruptedException ex) {
                //PointBoxTracer.recordSevereException(logger, ex);
            } catch (PointBoxTimeoutException ex) {
                //PointBoxTracer.recordSevereException(logger, ex);
            }
            pricingCurveUploadingThread = null;
        }
        if (regularPricingThread != null){
            try {
                NIOGlobal.stopThread(regularPricingThread, 1*1000);
            } catch (InterruptedException ex) {
                //PointBoxTracer.recordSevereException(logger, ex);
            } catch (PointBoxTimeoutException ex) {
                //PointBoxTracer.recordSevereException(logger, ex);
            }
            regularPricingThread = null;
        }
        if (autoPricingThread != null){
            try {
                NIOGlobal.stopThread(autoPricingThread, 1*1000);
            } catch (InterruptedException ex) {
                //PointBoxTracer.recordSevereException(logger, ex);
            } catch (PointBoxTimeoutException ex) {
                //PointBoxTracer.recordSevereException(logger, ex);
            }
            autoPricingThread = null;
        }
    }

    private void startWtPricingThread() {
        whenTechPricingThread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(750);
                } catch (InterruptedException ex) {
                }
                while(true){
                    if ((whenTechPricer != null) && (whenTechPricer.isPricingEngineLoaded())){
                        synchronized (pricerTypeLock){
                            if (pricerTypeLock.getPricerType().equals(PbcPricerType.WT)){
                                //System.out.println("whenTechPricingThread -> pricing ......");
                                try{
                                    pricingBufferedQuotes(whenTechPricer, retrieveQuotes(regularBuffer));
                                }catch (Exception ex){}
                            }//if
                        }//syn
                        try {
                            Thread.sleep(750);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }else{
                        if (whenTechPricer != null){
                            whenTechPricer.disconnectToLocalWhenTech();
                        }
                        whenTechPricer = null;
                        getKernel().raisePointBoxEvent(new WhenTechPricerUnavaliableEvent(PointBoxEventTarget.PbcFace,
                                                                   "No connection to the local WhenTech application."));
                        break;//stop this thread
                    }//if
                }//while
            }//run
        });
        
        whenTechPricingThread.start();
    }

    private void stopWtPricingThread() {
        if (whenTechPricingThread != null){
            try {
                NIOGlobal.stopThread(autoPricingThread, 1*1000);
            } catch (InterruptedException ex) {
                PointBoxTracer.recordSevereException(logger, ex);
            } catch (PointBoxTimeoutException ex) {
                PointBoxTracer.recordSevereException(logger, ex);
            }
            whenTechPricingThread = null;
        }
    }
    
}//PbcPricer

