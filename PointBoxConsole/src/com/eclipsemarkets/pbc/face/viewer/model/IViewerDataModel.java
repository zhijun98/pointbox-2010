/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;

/**
 * IViewerDataModel.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 9:54:28 AM
 */
public interface IViewerDataModel {

    public IPbsysOptionQuote retrieveQuote(int rowIndex);

    public IPbsysOptionQuoteWrapper retrieveQuoteWrapper(int rowIndex);

    public IViewerDataModelKernel getDataModelKernel();

    public void setColumnModel(IViewerColumnModel columnModel);

    public void fireTableRowsUpdated(final int rowIndex);

    /**
     * the model index of the latest row (for quotes) in the table
     * @return
     */
    public int getRowCursor();

    public int getDataModelSize();
}
