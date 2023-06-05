/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.parser.QuoteParsedEvent;
import com.eclipsemarkets.gateway.data.IBroadcastedMessage;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.release.PointBoxExecutorConfiguration;
import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewerDataModelKernel.java
 * <p>
 * ViewerDataModelKernel is shared by a collection of ViewerDataModels
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 24, 2010, 9:09:40 AM
 */
class ViewerDataModelKernel implements IViewerDataModelKernel{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(ViewerDataModelKernel.class.getName());
    }

    private final IPbcViewer viewer;
    //date structure
    private final TreeSet<String> bufferedLocations;
    
    private final ViewerDataKernel dataKernel;
    
    //listeners
    private final ArrayList<IViewerDataModelKernelListener> listeners;
    
    private ExecutorService fireService;

    private static HashSet<String> broadcastedMessageUuids;
    static{
        broadcastedMessageUuids = new HashSet<String>();
    }
    
    private HashMap<String, HashMap<String, String>> allDescriptiveExpData;
    
    ViewerDataModelKernel(IPbcViewer viewer, int threshold) {
        this.viewer = viewer;
        fireService = Executors.newFixedThreadPool(PointBoxExecutorConfiguration.ViewerDataModelKernel_Service_Control);
        bufferedLocations = new TreeSet<String>();
        listeners = new ArrayList<IViewerDataModelKernelListener>();
        dataKernel = new ViewerDataKernel(threshold); 
        allDescriptiveExpData = new HashMap<String, HashMap<String, String>>();
    }

    private HashMap<String, HashMap<String, String>> getAllDescriptiveExpData() {
        if ((allDescriptiveExpData == null) || (allDescriptiveExpData.isEmpty())){
            allDescriptiveExpData = viewer.getKernel().retrieveAllDescriptiveExpirationData();
        }
        return allDescriptiveExpData;
    }

    @Override
    public synchronized int getModelCursor() {
        return dataKernel.getCursor();
    }

    private void fireWrappersInsertedEvent(final int firstRowModelIndex, final int lastRowModelIndex){
        //logger.log(Level.INFO, " ---debug: fireQuotePublishedEvent ..............");
        fireService.submit(new Runnable(){
            @Override
            public void run() {
                for (IViewerDataModelKernelListener listener : listeners){
                    listener.fireWrappersInsertedEvent(firstRowModelIndex, lastRowModelIndex);
                }
            }
        });
    }

    private void fireTableRowsUpdated(final int rowModelIndex){
        //logger.log(Level.INFO, " ---debug: fireQuotePublishedEvent ..............");
        fireService.submit(new Runnable(){
            @Override
            public void run() {
                for (IViewerDataModelKernelListener listener : listeners){
                    listener.fireTableRowsUpdated(rowModelIndex);
                }
            }
        });
    }

    private void fireAllQuotesRemovedEvent(final int lastQuoteRowIndex){
        fireService.submit(new Runnable(){
            @Override
            public void run() {
                for (IViewerDataModelKernelListener listener : listeners){
                    listener.allQuotesRemovedEvent(lastQuoteRowIndex);
                }
            }
        });
    }

    @Override
    public synchronized void addViewerDataModelKernelListener(IViewerDataModelKernelListener listener){
        if (!listeners.contains(listener)){
            listeners.add(listener);
        }
    }
    
    @Override
    public synchronized void removeViewerDataModelKernelListener(IViewerDataModelKernelListener listener){
        listeners.remove(listener);
    }

    @Override
    public synchronized IPbsysOptionQuote retrieveQuote(int rowIndex) {
        IPbsysOptionQuoteWrapper wrapper = retrieveQuoteWrapper(rowIndex);
        if (wrapper == null){
            return null;
        }else{
            return wrapper.getQuoteOwner();
        }
    }

    @Override
    public synchronized IPbsysOptionQuoteWrapper retrieveQuoteWrapper(int rowIndex) {
        if ((rowIndex < 0) || (rowIndex >= getRowCount())){
            return null;
        }else{
            return dataKernel.getWrapper(rowIndex);
        }
    }

    @Override
    public synchronized ArrayList<IPbsysOptionQuote> retrieveAllQuotes() {
        return dataKernel.getAllQuotes();
    }

    @Override
    public synchronized void publishQuote(final IPbsysOptionQuote quote){
        if (quote == null){
            return;
        }
        IPbsysInstantMessage aPbsysInstantMessage = quote.getInstantMessage();
        //(1) get the next empty wrapper instance
        //(2) associate quote with such a wrapper instance
        if (aPbsysInstantMessage instanceof IBroadcastedMessage){
            String uuid = ((IBroadcastedMessage)aPbsysInstantMessage).getToGroupUuid().toString();
            if (broadcastedMessageUuids.contains(uuid)){
                return;
            }else{
                broadcastedMessageUuids.add(uuid);
            }
        }
        bufferLocations(quote.getOptionStrategyLegs().get(0).getOptionProduct());
        bufferLocations(quote.getOptionStrategyLegs().get(1).getOptionProduct());
        ViewerDataAssociationResult result = dataKernel.associateQuote(quote);
        IPbsysOptionQuoteWrapper aQuoteWrapper = result.getWrappper();
        if (result.getLastRowModelIndex() > result.getFirstRowModelIndex()){
            fireWrappersInsertedEvent(result.getFirstRowModelIndex(), result.getLastRowModelIndex());
        }else{
            fireTableRowsUpdated(aQuoteWrapper.getTableModelRowIndex());
        }
        
        ArrayList<IPbsysOptionQuote> quotes = new ArrayList<IPbsysOptionQuote>();
        quotes.add(quote);
        IPbcKernel kernel = viewer.getKernel();
        kernel.raisePointBoxEvent(
                new QuoteParsedEvent(PointBoxEventTarget.PbcFace,
                                     quotes));
        kernel.raisePointBoxEvent(
                new QuoteParsedEvent(PointBoxEventTarget.PbcPricer,
                                     quotes));
        kernel.raisePointBoxEvent(
                new QuoteParsedEvent(PointBoxEventTarget.PbcStorage,
                                     quotes));
    }

    @Override
    public synchronized void keepTodayDataModelKernel() {
        List<IPbsysOptionQuote> todayQuoteMessages = dataKernel.prepareKeepTodayDataModelKernel();
        IPbsysOptionQuoteWrapper aQuoteWrapper;
        ViewerDataAssociationResult result;
        for (int i = 0; i < todayQuoteMessages.size(); i++){
            result = dataKernel.associateQuote(todayQuoteMessages.get(i));
            aQuoteWrapper = result.getWrappper();
            if (result.getLastRowModelIndex() > result.getFirstRowModelIndex()){
                fireWrappersInsertedEvent(result.getFirstRowModelIndex(), result.getLastRowModelIndex());
            }else{
                fireTableRowsUpdated(aQuoteWrapper.getTableModelRowIndex());
            }
        }
    }

    @Override
    public synchronized void clearDataModelKernel() {
        int total = dataKernel.size();
        if (total > 0){
            dataKernel.clear();
            bufferedLocations.clear();
        }
    }

    @Override
    public synchronized ArrayList<String> retrievedBufferedLocations() {
        ArrayList<String> locations = new ArrayList<String>();
        Iterator<String> itr = bufferedLocations.iterator();
        while (itr.hasNext()){
            locations.add(itr.next());
        }
        return locations;
    }

    @Override
    public synchronized IPbsysOptionQuoteWrapper retrievePbsysOptionQuoteWrapper(int modelIndex) {
        return dataKernel.getWrapper(modelIndex);
    }

    @Override
    public synchronized int getRowCount() {
        return dataKernel.size();
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {

        if (dataKernel.isEmpty()){
            return "";
        }else{
            return dataKernel.getWrapper(rowIndex);
        }
    }

    @Override
    public synchronized void bufferLocations(String location) {
        if ((location == null) || (location.isEmpty())){
            return;
        }
        bufferedLocations.add(location);
    }

    /**
     * if latestQty == -1, it implies all the quotes need to be re-priced
     * @param latestQty
     * @return
     */
    private synchronized ArrayList<IPbsysOptionQuote> retrieveLatestQuotesForRepricing(int latestQty) {
        return dataKernel.retrieveLatestQuotesForRepricing(latestQty);
    }

    @Override
    public synchronized void refreshForQuotesParsedEvent(ArrayList<IPbsysOptionQuote> parsedQuotes) {
        //logger.log(Level.INFO, " ---debug: refreshForQuotesParsedEvent ..............");
        IPbsysOptionQuoteWrapper wrapper;
        for (IPbsysOptionQuote quote : parsedQuotes){
            wrapper = dataKernel.getWrapper(quote.getWrapperModelRowIndex());
            if (wrapper != null){
                wrapper.prepareParsedWrapper();
                //fireTableRowsUpdated(wrapper.getTableModelRowIndex());
                wrapper.setPeriod(formatTradingPeriod(wrapper.getQuoteOwner()));
            }
        }
    }


    private String formatTradingPeriod(IPbsysOptionQuote quote) {
        if (quote == null){
            return "";
        }else{
            PbcPricingModel pModel = quote.getPbcPricingModel();
            String tradingPeriod1;
            String tradingPeriod2 = "";
            if (PointBoxQuoteCode.IA.name().equalsIgnoreCase(pModel.getSqCode())){
                tradingPeriod1 = tradingPeriodForIaStyleHelper(pModel, quote.getOptionStrategyLegs().get(0));
                if (!quote.getOptionStrategyLegs().get(1).isEmptyLeg()){
                    tradingPeriod2 = tradingPeriodForIaStyleHelper(pModel, quote.getOptionStrategyLegs().get(1));
                }
            }else{
                tradingPeriod1 = tradingPeriodHelper(pModel, quote.getOptionStrategyLegs().get(0));
                if (!quote.getOptionStrategyLegs().get(1).isEmptyLeg()){
                    tradingPeriod2 = tradingPeriodHelper(pModel, quote.getOptionStrategyLegs().get(1));
                }
            }

            if (tradingPeriod2.isEmpty()){
                if (PointBoxQuoteCode.IA.name().equalsIgnoreCase(pModel.getSqCode())){
                    return tradingPeriod1;
                }else{
                    return tradingPeriod1.toUpperCase();
                }
            }else{
                if (PointBoxQuoteCode.IA.name().equalsIgnoreCase(pModel.getSqCode())){
                    return tradingPeriod1 + " vs " + tradingPeriod2;
                }else{
                    return tradingPeriod1.toUpperCase() + " vs " + tradingPeriod2.toUpperCase();
                }
            }
        }
    }

    private String tradingPeriodForIaStyleHelper(PbcPricingModel pModel, IPbsysQuoteLeg leg) {
        GregorianCalendar start = leg.getOptionContractStartDate();
        GregorianCalendar end = leg.getOptionContractEndDate();
        if ((start == null) || (end == null)){
            return "";
        }
        try {
            return CalendarGlobal.convertToIaStyledMMMDD(start) + "-" + CalendarGlobal.convertToIaStyledMMMDD(end);
        } catch (Exception ex) {
            Logger.getLogger(ViewerDataModelKernel.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    private String tradingPeriodHelper(PbcPricingModel pModel, IPbsysQuoteLeg leg) {
        //MermQuoteLeg leg1 = quote.getOptionStrategyLegs().get(0);
        if (leg == null){
            return "";
        }else{
            String period = tradingPeriodHelper_descriptive_style(pModel, leg);
            if (period == null){
                return tradingPeriodHelper_date_style(leg);
            }else{
                return period;
            }
        }
    }
    
    private String tradingPeriodHelper_descriptive_style(PbcPricingModel pModel, IPbsysQuoteLeg leg){
        HashMap<String, String> dateMap = getAllDescriptiveExpData().get(pModel.getSqCode());
        String emey = "";
        String smsy = "";
        GregorianCalendar start = leg.getOptionContractStartDate();
        GregorianCalendar end = leg.getOptionContractEndDate();
        if (start != null){
            smsy = dateMap.get(CalendarGlobal.convertToContractMMMYY(start));
        }
        if (end != null){
            emey = dateMap.get(CalendarGlobal.convertToContractMMMYY(end));
        }
        
        if (smsy == null){
            if ((PointBoxQuoteCode.KD.name().equalsIgnoreCase(pModel.getSqCode())) || (PointBoxQuoteCode.Uxx.name().equalsIgnoreCase(pModel.getSqCode()))){
                if (!dateMap.isEmpty()){
                    for (String p : dateMap.values()){
                        smsy = p;
                        break;
                    }//for
                }
            }
        }
        
        if (smsy == null){
            return null;
        }else{
            if ((emey == null) || (emey.isEmpty()) || emey.equals(smsy)){
                return smsy.toUpperCase();
            }else{
                return smsy.toUpperCase() + "-" + emey.toUpperCase();
            }
        }
    }
    
    private String tradingPeriodHelper_date_style(IPbsysQuoteLeg leg){
        String emey = "";
        String smsy = "";
        GregorianCalendar start = leg.getOptionContractStartDate();
        GregorianCalendar end = leg.getOptionContractEndDate();

        if (start != null){
            String sm = CalendarGlobal.convertToFinancialMonth(start.get(Calendar.MONTH)+1);
            String sy = Integer.toString(start.get(Calendar.YEAR));
            if (sy.length() == 4){
                sy = sy.substring(2);
            }
            smsy = sm.toUpperCase() + sy;
        }
        if (end != null){
            String em = CalendarGlobal.convertToFinancialMonth(end.get(Calendar.MONTH)+1);
            String ey = Integer.toString(end.get(Calendar.YEAR));
            if (ey.length() == 4){
                ey = ey.substring(2);
            }
            emey = em.toUpperCase() + ey;
        }
        if (emey.isEmpty() || emey.equals(smsy)){
            return smsy.toUpperCase();
        }else{
            return smsy.toUpperCase() + "-" + emey.toUpperCase();
        }
    }
    
    @Override
    public synchronized void refreshForQuotesPricedEvent(ArrayList<IPbsysOptionQuote> pricedQuotes) {
        IPbsysOptionQuoteWrapper wrapper;
        for (IPbsysOptionQuote quote : pricedQuotes){
            wrapper = dataKernel.getWrapper(quote.getWrapperModelRowIndex());
            if (wrapper != null){
                wrapper.preparePricedWrapper();
                wrapper.setStructureFaceValue(quote.getStructureFaceValue());
            }
        }
    }
}
