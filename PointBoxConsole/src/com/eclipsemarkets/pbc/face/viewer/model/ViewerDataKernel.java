/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.data.PbsysOptionQuoteWrapper;
import com.eclipsemarkets.global.CalendarGlobal;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * ViewerDataKernel
 * <P>
 * ViewerDataKernel is only used by ViewerDataModelKernel. This is a purely data structure without synchronization. 
 * Notice this data structure cannot be synchronized due to dead-lock possibility. It is guarded by ViewerDataModelKernel
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 26, 2011 at 12:23:51 AM
 */
class ViewerDataKernel {

    private static final Logger logger = Logger.getLogger(ViewerDataKernel.class.getName());
    
    private final HashMap<Integer, IPbsysOptionQuoteWrapper> quoteWrappers;
    /**
     * if the number of empty-wrapper is less than, threshold, it will create another the number, 
     * threshold, of new empty-wrappers .
     */
    private final int threshold;
    /**
     * Always point to the next empty wrapper
     */
    private Integer cursor;

    ViewerDataKernel(int threshold) {
        this.threshold = threshold;
        quoteWrappers = new HashMap<Integer, IPbsysOptionQuoteWrapper>();
        resetQuoteWrappersEmpty();
    }
    
    private void resetQuoteWrappersEmpty(){
        quoteWrappers.clear();
        for (int i = 0; i < threshold*2; i++){
            quoteWrappers.put(i, new PbsysOptionQuoteWrapper(i));
        }
        cursor = 0; //poiting to the first empty wrapper
    
    }
    
    ViewerDataAssociationResult associateQuote(IPbsysOptionQuote quote){
        ViewerDataAssociationResult result = new ViewerDataAssociationResult();
        IPbsysOptionQuoteWrapper wrapper = quoteWrappers.get(cursor);
        int rowModelIndex = wrapper.getTableModelRowIndex();
        result.setFirstRowModelIndex(rowModelIndex);
        result.setLastRowModelIndex(rowModelIndex);
        //expand data kernel if necessary
        int currentTotal = quoteWrappers.size();
        if ((currentTotal - rowModelIndex) < threshold){
            int newTotal = currentTotal + threshold;
            for (int i = currentTotal; i < newTotal; i++){
                quoteWrappers.put(i, new PbsysOptionQuoteWrapper(i));
            }
            result.setFirstRowModelIndex(cursor + 1);
            result.setLastRowModelIndex(newTotal - 1);
        }
        wrapper.associate(quote);
        
        cursor++;   //point to the next empty wrapper
        
        result.setWrappper(wrapper);
        return result;
    }

    IPbsysOptionQuoteWrapper getWrapper(int rowModelIndex) {
        return quoteWrappers.get(rowModelIndex);
    }

    void clear() {
        if (SwingUtilities.isEventDispatchThread()){
            resetQuoteWrappersEmpty();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    resetQuoteWrappersEmpty();
                }
            });
        }
    }

    int size() {
        return quoteWrappers.size();
    }

    boolean isEmpty() {
        return quoteWrappers.isEmpty();
    }

    public Integer getCursor() {
        return cursor;
    }

    ArrayList<IPbsysOptionQuote> getAllQuotes() {
        ArrayList<IPbsysOptionQuote> quotes = new ArrayList<IPbsysOptionQuote>();

        Set<Integer> keys = quoteWrappers.keySet();
        Iterator<Integer> itr = keys.iterator();
        IPbsysOptionQuote quote;
        GregorianCalendar today = CalendarGlobal.getToday(0, 0, 0);
        Integer index;
        while (itr.hasNext()){
            index = itr.next();
            if (index > cursor){
                break;
            }
            quote = quoteWrappers.get(index).getQuoteOwner();
            if (quote != null){
                if ((quote.getInstantMessageTimestamp().after(today)) && quote.isSufficientPricingData()){
                    quotes.add(quote);
                }
            }
        }//while
        return quotes;
    }

    List<IPbsysOptionQuote> prepareKeepTodayDataModelKernel() {
        List<IPbsysOptionQuote> todayQuoteMessages = new ArrayList<IPbsysOptionQuote>();
        Set<Integer> keys = quoteWrappers.keySet();
        Iterator<Integer> itr = keys.iterator();
        IPbsysOptionQuoteWrapper quoteWrapper;
        GregorianCalendar today = CalendarGlobal.getToday(0, 0, 0);
        Integer index;
        while (itr.hasNext()){
            index = itr.next();
            if (index > cursor){
                break;
            }
            quoteWrapper = quoteWrappers.get(index);
            if (quoteWrapper.getQuoteOwner() != null){
                if (quoteWrapper.getQuoteOwner().getInstantMessageTimestamp().after(today)){
                    todayQuoteMessages.add(quoteWrapper.getQuoteOwner());
                }
            }
        }//while
        resetQuoteWrappersEmpty();
        return todayQuoteMessages;
    }

    ArrayList<IPbsysOptionQuote> retrieveLatestQuotesForRepricing(int latestQty) {
        ArrayList<IPbsysOptionQuote> quotes = new ArrayList<IPbsysOptionQuote>();
        Set<Integer> keys = quoteWrappers.keySet();
        Iterator<Integer> itr = keys.iterator();
        IPbsysOptionQuoteWrapper aWrapper;
        int guard = 0;
        if (latestQty > 0){
            guard = quoteWrappers.size() - latestQty;
        }
        int counter = 0;
        while (itr.hasNext()){
            aWrapper = quoteWrappers.get(itr.next());
            if (counter >= guard){
                if (aWrapper.getQuoteOwner() != null){
                    if (aWrapper.getQuoteOwner().isSufficientPricingData()){
                        quotes.add(aWrapper.getQuoteOwner());
                    }
                }
            }else{
                //skip the old counter ones
                counter++;
            }
        }
        return quotes;
    }

}//ViewerDataKernel

