/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import java.util.ArrayList;

/**
 * IViewerDataModelKernel.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 24, 2010, 10:31:00 AM
 */
public interface IViewerDataModelKernel {
    
    public void addViewerDataModelKernelListener(IViewerDataModelKernelListener listener);

    public void removeViewerDataModelKernelListener(IViewerDataModelKernelListener listener);

    /**
     * Publish quote onto proper IPbsysOptionQuoteWrapper in the viewer panels
     * @param quote
     */
    public void publishQuote(IPbsysOptionQuote quote);

    /**
     * This method could be time-consuming on processing data
     */
    public void clearDataModelKernel();

    public ArrayList<String> retrievedBufferedLocations();

    public IPbsysOptionQuoteWrapper retrievePbsysOptionQuoteWrapper(int modelIndex);

    public int getRowCount();

    public Object getValueAt(int rowIndex, int columnIndex);

    public void bufferLocations(String location);

    public IPbsysOptionQuote retrieveQuote(int rowIndex);

    public IPbsysOptionQuoteWrapper retrieveQuoteWrapper(int rowIndex);

    /**
     * This method could be time-consuming on processing data
     */
    public void keepTodayDataModelKernel();

    public ArrayList<IPbsysOptionQuote> retrieveAllQuotes();

    public void refreshForQuotesParsedEvent(ArrayList<IPbsysOptionQuote> parsedQuotes);

    public void refreshForQuotesPricedEvent(ArrayList<IPbsysOptionQuote> pricedQuotes);

    /**
     * The cursor pointing to the next empty wrapper
     * @return
     */
    public int getModelCursor();
}
