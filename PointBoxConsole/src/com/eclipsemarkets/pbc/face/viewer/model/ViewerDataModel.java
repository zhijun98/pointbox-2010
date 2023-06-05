/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * ViewerDataModel.java
 * <p>
 * ViewerDataModel has ability to re-price its data (quotes) by means of its internal autoPricer.
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 11:14:35 AM
 */
class ViewerDataModel extends AbstractTableModel implements IViewerDataModel,
                                                            IViewerDataModelKernelListener
{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(ViewerDataModel.class.getName());
    }
    private static final long serialVersionUID = 1L;
    private IViewerColumnModel columnModel; //who know how to get value from the corresponding field of a quote
    private IViewerDataModelKernel dataModelKernel;
    private Map<Integer, Integer> viewColumnIndexToKernerlColumnIndex = new HashMap<Integer,Integer>();

    /**
     *
     * @param columnModel
     * @param autoPricing - if enable autoPricer
     */
    ViewerDataModel(IViewerDataModelKernel dataModelKernel, IViewerColumnModel columnModel) {

        this.dataModelKernel = dataModelKernel;
        this.setColumnModel(columnModel);
        dataModelKernel.addViewerDataModelKernelListener(this);
    }

    @Override
    public int getDataModelSize() {
        return dataModelKernel.getRowCount();
    }

    @Override
    public IPbsysOptionQuote retrieveQuote(int rowIndex) {
        return dataModelKernel.retrieveQuote(rowIndex);
    }

    @Override
    public IPbsysOptionQuoteWrapper retrieveQuoteWrapper(int rowIndex) {
        return dataModelKernel.retrieveQuoteWrapper(rowIndex);
    }

    @Override
    public IViewerDataModelKernel getDataModelKernel() {
        return dataModelKernel;
    }

    @Override
    public synchronized void setColumnModel(IViewerColumnModel columnModel) {

        this.columnModel = columnModel;
        if ( columnModel != null) {
           TableColumnModel tcm = columnModel.getViewerColumnModel();
           if ( tcm != null ) {
              this.viewColumnIndexToKernerlColumnIndex.clear();
              int columnCount = tcm.getColumnCount();
              for ( int viewColumnIndex = 0; viewColumnIndex< columnCount; viewColumnIndex++)  {
                 TableColumn tc = tcm.getColumn(viewColumnIndex);
                 int kernelModelIndex = tc.getModelIndex();
                 tc.setModelIndex(viewColumnIndex);
                 viewColumnIndexToKernerlColumnIndex.put(viewColumnIndex, kernelModelIndex);
              }
           }
       }
    }

    @Override
    public synchronized Class getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public synchronized int getColumnCount() {
        return columnModel.getNumberOfColumns();
    }

    @Override
    public int getRowCursor() {
        return dataModelKernel.getModelCursor();
    }

    @Override
    public int getRowCount() {
        return dataModelKernel.getRowCount();
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {

        int KernelColumnIndex = this.viewColumnIndexToKernerlColumnIndex.get(columnIndex);        
        return dataModelKernel.getValueAt(rowIndex, KernelColumnIndex);
    }

    @Override
    public void allQuotesRemovedEvent(final int lastQuoteRowIndex) {
        if (SwingUtilities.isEventDispatchThread()){
            fireTableRowsDeleted(0, lastQuoteRowIndex);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    fireTableRowsDeleted(0, lastQuoteRowIndex);
                }
            });
        }
    }

    @Override
    public void fireWrappersInsertedEvent(final int firstRowModelIndex, final int lastRowModelIndex) {
        if (SwingUtilities.isEventDispatchThread()){
            fireWrappersInsertedEventHelper(firstRowModelIndex, lastRowModelIndex);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    fireWrappersInsertedEventHelper(firstRowModelIndex, lastRowModelIndex);
                }
            });
        }
    }

    private void fireWrappersInsertedEventHelper(int firstRowModelIndex, int lastRowModelIndex) {
        if (firstRowModelIndex > lastRowModelIndex){
            return;
        }
        int rowNum = getRowCount() - 1;
        if ((firstRowModelIndex > rowNum) || (lastRowModelIndex > rowNum)){
            return;
        }
        try{
            fireTableRowsInserted(firstRowModelIndex, lastRowModelIndex);
        }catch(java.lang.IndexOutOfBoundsException ex) {
            //todo: don't know why it happened
        }
    }

    @Override
    public synchronized void fireTableRowsUpdated(final int rowIndex){
        if ((rowIndex < getRowCount()) && (rowIndex >= 0)){
            if (SwingUtilities.isEventDispatchThread()){
                this.fireTableRowsUpdated(rowIndex, rowIndex);
            }else{
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        fireTableRowsUpdated(rowIndex, rowIndex);
                    }
                });
            }
        }
    }
}
