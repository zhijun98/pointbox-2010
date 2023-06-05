/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

/**
 * IViewerDataModelKernelListener.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 24, 2010, 10:42:48 AM
 */
public interface IViewerDataModelKernelListener {
    
    public void fireTableRowsUpdated(final int rowIndex);

    public void fireWrappersInsertedEvent(int firstRowModelIndex, int lastRowModelIndex);

    /**
     *
     * @param lastQuoteRowIndex - index of the last quote in the data model removed from the kernel
     */
    public void allQuotesRemovedEvent(int lastQuoteRowIndex);
}
