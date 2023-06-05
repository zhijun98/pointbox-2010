/**
 * Eclipse Market Solutions LLC
 *
 * ViewerTablePanel.java
 *
 * @author Zhijun Zhang
 * Created on May 21, 2010, 6:51:51 AM
 */

package com.eclipsemarkets.pbc.face.viewer;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.viewer.model.IViewerColumnModel;
import com.eclipsemarkets.pbc.face.viewer.model.IViewerDataModel;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerModelFactory;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterCriteria;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Zhijun Zhang
 */
class ViewerTablePanel extends javax.swing.JPanel implements IViewerTablePanel
{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(ViewerTablePanel.class.getName());
    }
    private static final long serialVersionUID = 1L;

    private final IPbcViewer viewer;
    
    private ViewerTableType viewerTableType;

    private ViewerTableTabPopupMenu popup;

    /**
     * models
     */
    private IViewerColumnModel columnModel;
    private TableColumnModel genericViewerColumnModel;  //synchronized
    private IViewerDataModel viewerDataModel;
    private TableRowSorter<TableModel> tableRowSorter = null;

    @Override
    public synchronized void publishGenericViewerColumnModel(TableColumnModel genericViewerColumnModel) {
        this.genericViewerColumnModel = genericViewerColumnModel;
    }
    
    /**
     * Only it can get it one time. After that, genericViewerColumnModel will be NULL
     * @return
     */
    private synchronized TableColumnModel retrieveGenericViewerColumnModel() {
        TableColumnModel aViewerColumnModel = genericViewerColumnModel;
        genericViewerColumnModel = null;
        return aViewerColumnModel;
    }

    private IPbconsoleImageSettings getImageSettings(){
        return viewer.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
    }
    
    @Override
    public final IPbcRuntime getPbcRuntime(){
        return viewer.getKernel().getPointBoxConsoleRuntime();
    }

    private static boolean isAutomaticallyScrolling;
    
    private final ViewerTable jViewerTable;
    
    private String viewerTabUniqueName;
    
    private ViewerColumnIdentifier currentSortedViewerColumnIdentifier;
    
    ViewerTablePanel(final IPbcViewer viewer,
                    String viewerTabUniqueName,
                    IViewerDataModel dataModel)
    {
        initComponents();

        jViewerTable = new ViewerTable();
        jViewerTable.setAutoCreateColumnsFromModel(false);
        jViewerTable.setName("jViewerTable");
        jViewerTable.setAutoscrolls(true);
        jViewerScrollPane.setViewportView(jViewerTable);

        this.viewer = viewer;
        this.viewerTabUniqueName = viewerTabUniqueName;
        viewerTableType = ViewerTableType.convertEnumValueToType(viewerTabUniqueName);
        if ((viewerTableType == null) || (viewerTableType.equals(ViewerTableType.UNKNOWN))){
            viewerTableType = ViewerTableType.FILTER_RESULT;
        }
//        if (viewerTableType.equals(ViewerTableType.FILTER_RESULT)){
//            getPbcRuntime().addPbcViewerSettings(viewerTabUniqueName);
//        }
        
        currentSortedViewerColumnIdentifier = null;

        this.viewerDataModel = dataModel;
        
        columnModel = null;
        
        jViewerTable.setRowHeight(20);
        jViewerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jViewerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        jViewerTable.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
                try{
                    Point clickedPoint = e.getPoint();
                    int colIndex = jViewerTable.columnAtPoint(clickedPoint);
                    int rowIndex = jViewerTable.rowAtPoint(clickedPoint);
                    if ((colIndex > -1) && (rowIndex > -1)){
                        TableColumnModel columnModel = jViewerTable.getColumnModel();
                        if (columnModel instanceof IViewerColumnModel){
                            ViewerColumnIdentifier id = ((IViewerColumnModel)columnModel).getViewerColumnIdentifier(colIndex);
                            //logger.log(Level.INFO, "Column clicked is {0}", id.toString());
                            if ((id != null) && (id.equals(ViewerColumnIdentifier.RemoteBrokerHouse))){
                                IPbsysOptionQuote quote = viewerDataModel.retrieveQuote(jViewerTable.convertRowIndexToModel(rowIndex));
                                if (quote != null){
                                    if (quote.getInstantMessage().isOutgoing()){
                                        viewer.gatewayConnectorBuddyHighlighted(quote.getInstantMessage().getToUser());
                                    }else{
                                        viewer.gatewayConnectorBuddyHighlighted(quote.getInstantMessage().getFromUser());
                                    }
                                    viewer.getKernel().pushFloatingMessagingFrameToFront();
                                }
                            }
                        }
                    }
                }catch (Exception ex){
                    /**
                     * This is possible. Suppose local user use A login and A's buddies sent messages. After that, A 
                     * log out. And if the local user click buddy name of A from the current Viewer, here exception 
                     * happened "component not found" because A's buddy list panel was closed.
                     */
                    //PointBoxTracer.displaySevereMessage(logger, ex.getMessage());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });
        
        isAutomaticallyScrolling = true;

        popup = null;

        //jViewerTable.setRowSelectionAllowed(true);
        //jViewerTable.setColumnSelectionAllowed(true);
        //jViewerTable.setCellSelectionEnabled(true);
        jViewerTable.setAutoCreateRowSorter(false);

        jViewerTable.addMouseListener(new MouseAdapter() {
            //Simon: here is how to popup a menu and you may make any change on ViewerTableTabPopupMenu
            // so that you can pass in any extra-new parameters into ViewerTableTabPopupMenu by its constructor.

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (popup == null){
                    popup = new ViewerTableTabPopupMenu(viewer, ViewerTablePanel.this);
                }else{
                    popup.loadSendAndCopyItemInEDT();
                }
                if (isAutomaticallyScrolling){
                    popup.enableScrollingViewer();
                }else{
                    popup.disableScrollingViewer();
                }
                //popup.show(e.getComponent(), e.getX(), e.getY());
                Point point = new Point(e.getX(), e.getY());
                if (jViewerTable.rowAtPoint(point) > - 1) {
                    int row = jViewerTable.rowAtPoint(point);
                    int modelrow = jViewerTable.convertRowIndexToModel(row);
                    //int viewCol = jViewerTable.columnAtPoint(point);
                    //PointBoxLogger.printWarningMessage("modelrow - " + modelrow);
                    //if (e.isPopupTrigger() || (viewCol > -1 && (e.isControlDown() || e.isShiftDown()))) {
                        if (e.isPopupTrigger()) {
                            IPbsysOptionQuoteWrapper quoteWrapper = ((IViewerDataModel)jViewerTable.getModel()).retrieveQuoteWrapper(modelrow);
                            if (quoteWrapper != null){
                                if ((getViewerTableType().equals(ViewerTableType.FILTER_RESULT)) || (getViewerTableType().equals(ViewerTableType.UNKNOWN))){
                                    popup.enableFilterRename();
                                }else{
                                    popup.disableFilterRename();
                                }
                                popup.setTargetQuoteWrapper(quoteWrapper);
                                popup.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    //}
                }
            }
        });
        
        jViewerTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                (new SwingWorker<Void, Void>(){
                    @Override
                    protected Void doInBackground() throws Exception {
                        synchronizedViewerTablePanelSettings();
                        return null;
                    }
                }).execute();
              }
            }); 
        //*****************************************************
        
        initializeToolBar();
    }

    @Override
    public void synchronizedViewerTablePanelSettings() {
        if (viewer.getKernel() == null){
            return;
        }
        if (viewer.getKernel().getPointBoxLoginUser() == null){
            return;
        }
        boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(viewer.getKernel().getPointBoxLoginUser().getIMUniqueName());
        if (isViewerSettingsForAllViewers){
            TableColumnModel persistentModel = ViewerTablePanel.this.retrieveGenericViewerColumnModel();
            if (persistentModel == null){
                persistentModel = jViewerTable.getColumnModel();
            }
            //Case-1: users want to change it for all the tabs;
            IViewerTablePanel tablePanel;
            for(String tabName:viewer.getTabStorage().keySet()){
                tablePanel=viewer.getTabStorage().get(tabName.toLowerCase());
                tablePanel.storeViewerTablePanelSettings(tabName, persistentModel);
                tablePanel.publishGenericViewerColumnModel(persistentModel);
//                        tablePanel.personalizeViewerTablePanelByGenericModelSettings(tabName, jViewerTable.getColumnModel());
            }
        }else{
            //Case-2: users want to change it only for the current tab
            storeViewerTablePanelSettings(getViewerTableName(), jViewerTable.getColumnModel());
        }
    }
    
    @Override
    public void personalizeViewerTablePanelByGenericModelSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            personalizeViewerTablePanelByGenericModelSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    personalizeViewerTablePanelByGenericModelSettingsHelper();
                }
            });
        }
    }
    private void personalizeViewerTablePanelByGenericModelSettingsHelper(){
        final TableColumnModel model = this.retrieveGenericViewerColumnModel();
        //NON_NULL model is used to detect whether or not it need to rebuild the column model
        if (model != null){
            IViewerColumnModel aNewColumnModel = ViewerModelFactory.createViewerColumnModelInstance(getPbcRuntime(), viewerTabUniqueName);
            viewerDataModel.setColumnModel(aNewColumnModel);
            jViewerTable.setColumnModel(aNewColumnModel.getViewerColumnModel());
            setTableModelSorterAndFilter();
        }
    } 
    
    @Override
    public void disableAutomaticallyScrollingViewer() {
        if (SwingUtilities.isEventDispatchThread()){
            isAutomaticallyScrolling = false;
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    isAutomaticallyScrolling = false;
                }
            });
        }
    }

    @Override
    public void enableAutomaticallyScrollingViewer() {
        if (SwingUtilities.isEventDispatchThread()){
            isAutomaticallyScrolling = true;
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    isAutomaticallyScrolling = true;
                }
            });
        }
    }
    
    private SortOrder getCurrentSortedViewerColumnIdentifier(){
        if (currentSortedViewerColumnIdentifier == null){
            return SortOrder.UNSORTED;
        }else{
            return getPbcRuntime().getViewerColumnSortOrder(viewerTabUniqueName, currentSortedViewerColumnIdentifier);
        }
    }

    private void setCurrentSortedViewerColumnIdentifier(ViewerColumnIdentifier viewerColumnIdentifier, SortOrder sortOrder) {
        if (currentSortedViewerColumnIdentifier != null){
            getPbcRuntime().setViewerColumnSortOrder(viewerTabUniqueName, currentSortedViewerColumnIdentifier, SortOrder.UNSORTED);
            getPbcRuntime().setViewerColumnSortOrder(viewerTabUniqueName, viewerColumnIdentifier, sortOrder);
        }
        currentSortedViewerColumnIdentifier = viewerColumnIdentifier;
    }
    
    private class ViewerJScrollBarChangeListener implements ChangeListener{
        
        private int incValue;
        private int maxValue;
        
        ViewerJScrollBarChangeListener() {
            this.incValue = 0;
            maxValue = 0;
        }
        
        private synchronized void setIncValue(int incValue){
            this.incValue = incValue;
        }

        private synchronized  int getIncValue() {
            return incValue;
        }

        private synchronized void updateMaxScrollingValue() {
            maxValue += incValue;
        }
        private synchronized int getMaxScrollingValue() {
            return maxValue;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            if (source instanceof BoundedRangeModel) {
                BoundedRangeModel aModel = (BoundedRangeModel) source;
                if (!aModel.getValueIsAdjusting()) {
                    int currentValue = aModel.getValue();
                    int maxScrollingValue = getMaxScrollingValue();
                    if (currentValue > maxScrollingValue){
                        logger.log(Level.INFO, "crolling to currentValue = {0}", currentValue);
                        aModel.setValue(maxScrollingValue);
                    }
                }
            }
        }
    }
    
    private void setTableModelSorterAndFilter()
    {
        TableModel tableModel = (TableModel)viewerDataModel;
        tableModel.addTableModelListener(new TableModelListener(){
            @Override
            public void tableChanged(TableModelEvent e) {
                ((TableRowSorter<TableModel>)jViewerTable.getRowSorter()).sort();
                switch(e.getType()){
                    case TableModelEvent.UPDATE:
                        if (e.getLastRow() >= 0){
                            SortOrder sortOrder = getCurrentSortedViewerColumnIdentifier();
                            if (isAutomaticallyScrolling){
                                if (sortOrder.equals(SortOrder.DESCENDING)){
                                    jViewerScrollPane.getVerticalScrollBar().setValue(0);
                                }else{
                                    int lastRowViewIndex = jViewerTable.convertRowIndexToView(e.getLastRow());
                                    Rectangle currentVisibleRectAfterSorting =  jViewerTable.getVisibleRect();
                                    Rectangle lastCellRect = jViewerTable.getCellRect(lastRowViewIndex+1, 1, true);
                                    Rectangle rect = new Rectangle((int)currentVisibleRectAfterSorting.getX(), 
                                                                   (int)lastCellRect.getY(), 
                                                                   (int)lastCellRect.getWidth(), 
                                                                   (int)lastCellRect.getHeight());
                                    jViewerTable.scrollRectToVisible(rect);
                                }
                            }
                        }
                        break;
                    default:
                        //do nothing
                }
            }
        });
        this.jViewerTable.setModel(tableModel);
        getTableRowSorter(tableModel);
    }
    
    public TableRowSorter<TableModel> getTableRowSorter(TableModel tableModel) {
        if (tableRowSorter == null){
            tableRowSorter = new TableRowSorter<TableModel>(tableModel);
            tableRowSorter.addRowSorterListener(new RowSorterListener(){
                @Override
                public void sorterChanged(RowSorterEvent e) {
                    RowSorter rowSorter = e.getSource();
                    if (rowSorter != null){
                        List sortKeyList = rowSorter.getSortKeys();
                        SortKey sortKey;
                        ViewerColumnIdentifier colId;
                        for (int i = 0; i < sortKeyList.size(); i++){
                            sortKey = (SortKey)sortKeyList.get(i);
                            colId = columnModel.getViewerColumnIdentifier(jViewerTable.convertColumnIndexToModel(sortKey.getColumn()));
                            if (sortKey.getSortOrder() != SortOrder.UNSORTED){
                                setCurrentSortedViewerColumnIdentifier(colId, sortKey.getSortOrder());
//chen2013 - commit-1051: this commit was to fix ticket 449. But it introduce new issues on "Filter" tab which might lose current-pink-line in some situations
//                                if (sortKey.getSortOrder() == SortOrder.DESCENDING) {
//                                    jViewerScrollPane.getVerticalScrollBar().setValue(0);
//                                } else if (sortKey.getSortOrder() == SortOrder.ASCENDING) {
//                                    try{
//                                        int index = viewerDataModel.getRowCursor();
//                                        int newY = 0;
//                                        Rectangle currentVisibleRectAfterSorting =  jViewerTable.getVisibleRect();
//                                        Rectangle lastCellRect = jViewerTable.getCellRect(index-1, 1, true);
//                                        if (currentVisibleRectAfterSorting.getY() > lastCellRect.getY()) {
//                                            newY = (int)lastCellRect.getY() - (int)currentVisibleRectAfterSorting.getHeight() + jViewerTable.getRowHeight();
//                                        } else {
//                                            newY = (int)lastCellRect.getY() + jViewerTable.getRowHeight();
//                                        }
//                                        Rectangle rect = new Rectangle((int)currentVisibleRectAfterSorting.getX(), 
//                                                                                        newY, 
//                                                                                        (int)lastCellRect.getWidth(), 
//                                                                                        (int)lastCellRect.getHeight());
//                                        jViewerTable.scrollRectToVisible(rect);
//                                    }catch (Exception ex){
//                                        logger.log(Level.SEVERE, null, ex);
//                                    }
//                                }
                                break;
                            }
                        }//for
                    }
                }//funtion - sorterChanged
            });
            //set comparator for each column and set sortKeys
            ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
            int colNum = columnModel.getNumberOfColumns();
            ViewerColumnIdentifier colId;
            SortOrder sortOrder;
            boolean flag=true;
            for (int colIndex = 0; colIndex < colNum; colIndex++){
                colId = columnModel.getViewerColumnIdentifier(colIndex);
                tableRowSorter.setComparator(colIndex, new ViewerTableSorter(this, colId));
                sortOrder = getPbcRuntime().getViewerColumnSortOrder(viewerTabUniqueName, colId);
                if (!sortOrder.equals(SortOrder.UNSORTED)){
                    sortKeys.add(new RowSorter.SortKey(colIndex,
                                                       sortOrder));
                    setCurrentSortedViewerColumnIdentifier(colId, sortOrder);
                    flag=false;
                   // break;
                }
            }//for
            tableRowSorter.setSortKeys(sortKeys);

            jViewerTable.setRowSorter(tableRowSorter);

            //if the table dosen't have a sort, it applys "sort by ascending time" order as default.
            if(flag){
                colId = columnModel.getViewerColumnIdentifier(0);  //index 0 refers to the column identifier Time
                currentSortedViewerColumnIdentifier=colId;
                setCurrentSortedViewerColumnIdentifier(colId, SortOrder.ASCENDING);
                jViewerTable.getRowSorter().toggleSortOrder(0);
            }
            //set row filter
            if (viewerTableType != null){
                tableRowSorter.setRowFilter(new ViewerTableFilter(viewer, viewerTableType));
            }
        }
        return tableRowSorter;
    }
    
    @Override
    public void refreshRow(int tableModelRowIndex) {
        viewerDataModel.fireTableRowsUpdated(tableModelRowIndex);
    }

    @Override
    public void refreshTable() {
        if (SwingUtilities.isEventDispatchThread()){
            ((AbstractTableModel)viewerDataModel).fireTableDataChanged();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    ((AbstractTableModel)viewerDataModel).fireTableDataChanged();
                }
            });
        }
    }

    @Override
    public void clearTablePanel() {
        if (SwingUtilities.isEventDispatchThread()){
            viewerDataModel.getDataModelKernel().clearDataModelKernel();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    viewerDataModel.getDataModelKernel().clearDataModelKernel();
                }
            });
        }
    }
    
    @Override
    public void storeViewerTablePanelSettings(String viewerTabUniqueName, TableColumnModel model) {
        TableColumn column;
        IPbcRuntime runtime = getPbcRuntime();
        for (int i = 0; i < model.getColumnCount(); i++){
            column = model.getColumn(i);
            runtime.setViewerColumnWidth(viewerTabUniqueName, (ViewerColumnIdentifier)column.getIdentifier(), column.getPreferredWidth());
            runtime.setViewerColumnPosition(viewerTabUniqueName, (ViewerColumnIdentifier)column.getIdentifier(), i);
        }
    } 

    @Override
    public void storeViewerTablePanelSettings() {
        TableColumnModel model = jViewerTable.getColumnModel();
        TableColumn column;
        IPbcRuntime runtime = getPbcRuntime();
        for (int i = 0; i < model.getColumnCount(); i++){
            column = model.getColumn(i);
            runtime.setViewerColumnWidth(viewerTabUniqueName, (ViewerColumnIdentifier)column.getIdentifier(), column.getWidth());
            runtime.setViewerColumnPosition(viewerTabUniqueName, (ViewerColumnIdentifier)column.getIdentifier(), i);
        }
    }
    
    @Override
    public void personalizeViewerTablePanel() {
        if (SwingUtilities.isEventDispatchThread()){
            personalizeViewerTablePanelHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    personalizeViewerTablePanelHelper();
                }
            });
        }
    }
    
    private void personalizeViewerTablePanelHelper(){
        storeViewerTablePanelSettings();
        //if (columnModel == null){         //after reset column settings, it must be not NULL. We need to re-create the colunmModel after setup the column settings.
            this.columnModel = ViewerModelFactory.createViewerColumnModelInstance(getPbcRuntime(), this.viewerTabUniqueName);
            viewerDataModel.setColumnModel(columnModel);
            jViewerTable.setColumnModel(columnModel.getViewerColumnModel());
            if (columnModel.getNumberOfColumns() > 0){
                setTableModelSorterAndFilter();
            }
        //}
    }

    @Override
    public void installTableRowFilter(ArrayList<IViewerFilterCriteria> criteriaList) {
        if (criteriaList != null && criteriaList.size() > 0) {
            List<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>();
            RowFilter rowFilter;
            for (int i = 0; i < criteriaList.size(); i++) {
                rowFilter = new ViewerTableFilter(viewer, criteriaList.get(i));
                //rowFilter = constructRowFilterWithColumn();
                //if (rowFilter != null) {
                    filters.add(rowFilter);
                //}
            }
            //sorter.setRowFilter(RowFilter.andFilter(filters));
            RowSorter<? extends TableModel> aRowSorter = jViewerTable.getRowSorter();
            if (aRowSorter != null){
                try{
                    ((TableRowSorter<TableModel>)aRowSorter).setRowFilter(null);
                    ((TableRowSorter<TableModel>)aRowSorter).setRowFilter(RowFilter.andFilter(filters));
                }catch(Exception ex){}
            }
        }
    }
    
    private void initializeToolBar(){
        jExportAllQuotes.setIcon(getImageSettings().getExportAllQuotesIcon());
        jExportAllQuotes.setText(null);
        jExportAllQuotes.setActionCommand(ViewerCommand.exportAllRowsIntoTextItem.toString());

        jFloatingViewer.setIcon(getImageSettings().getFloatingViewerIcon());
        jFloatingViewer.setText(null);
        jFloatingViewer.setActionCommand(ViewerCommand.FloatingFrame.toString());

        jRenameViewerTab.setIcon(getImageSettings().getRenameViewerTabIcon());
        jRenameViewerTab.setText(null);
        jRenameViewerTab.setActionCommand(ViewerCommand.RenameViewerTab.toString());

        jCloseViewerTab.setIcon(getImageSettings().getCloseViewerTabIcon());
        jCloseViewerTab.setText(null);
        jCloseViewerTab.setActionCommand(ViewerCommand.CloseViewerTab.toString());

        jCloseAllViewerTabs.setIcon(getImageSettings().getCloseAllViewerTabsIcon());
        jCloseAllViewerTabs.setText(null);
        jCloseAllViewerTabs.setActionCommand(ViewerCommand.CloseAllViewerTabs.toString());
    }

    @Override
    public JPanel getBasePanel() {
        return this;
    }

    @Override
    public ViewerTableType getViewerTableType() {
        return viewerTableType;
    }

    @Override
    public void setViewerTableType(ViewerTableType tableType) {
        this.viewerTableType = tableType;
    }

    @Override
    public String getViewerTableName() {
        if ((viewerTableType == null) || (viewerTableType.equals(ViewerTableType.FILTER_RESULT)) || (viewerTableType.equals(ViewerTableType.UNKNOWN))){
            return viewerTabUniqueName;
        }else{
            return viewerTableType.toString();
        }
    }

    @Override
    public void setViewerTabUniqueName(String viewerTabUniqueName) {
        if (DataGlobal.isEmptyNullString(viewerTabUniqueName)){
            return;
        }
        this.viewerTabUniqueName = viewerTabUniqueName;
    }
    
    public javax.swing.JScrollPane getJViewerScrollPane(){
        return jViewerScrollPane;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar = new javax.swing.JToolBar();
        jExportAllQuotes = new javax.swing.JButton();
        jFloatingViewer = new javax.swing.JButton();
        jRenameViewerTab = new javax.swing.JButton();
        jCloseViewerTab = new javax.swing.JButton();
        jCloseAllViewerTabs = new javax.swing.JButton();
        jBasePanel = new javax.swing.JPanel();
        jViewerScrollPane = new javax.swing.JScrollPane();

        jToolBar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar.setRollover(true);
        jToolBar.setName("jToolBar"); // NOI18N

        jExportAllQuotes.setText("ExAll");
        jExportAllQuotes.setToolTipText("Export all the quotes/messages in the current aggregator...");
        jExportAllQuotes.setFocusable(false);
        jExportAllQuotes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jExportAllQuotes.setName("jExportAllQuotes"); // NOI18N
        jExportAllQuotes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(jExportAllQuotes);

        jFloatingViewer.setText("FtWin");
        jFloatingViewer.setToolTipText("Make the current aggregator floating on your screen ...");
        jFloatingViewer.setFocusable(false);
        jFloatingViewer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jFloatingViewer.setName("jFloatingViewer"); // NOI18N
        jFloatingViewer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(jFloatingViewer);

        jRenameViewerTab.setText("ReNm");
        jRenameViewerTab.setToolTipText("Rename the current aggregator's tab name ...");
        jRenameViewerTab.setFocusable(false);
        jRenameViewerTab.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jRenameViewerTab.setName("jRenameViewerTab"); // NOI18N
        jRenameViewerTab.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(jRenameViewerTab);

        jCloseViewerTab.setText("ClTab");
        jCloseViewerTab.setToolTipText("Close current aggregator...");
        jCloseViewerTab.setFocusable(false);
        jCloseViewerTab.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCloseViewerTab.setName("jCloseViewerTab"); // NOI18N
        jCloseViewerTab.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(jCloseViewerTab);

        jCloseAllViewerTabs.setText("ClAll");
        jCloseAllViewerTabs.setToolTipText("Close all the aggregator tabs ...");
        jCloseAllViewerTabs.setFocusable(false);
        jCloseAllViewerTabs.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCloseAllViewerTabs.setName("jCloseAllViewerTabs"); // NOI18N
        jCloseAllViewerTabs.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(jCloseAllViewerTabs);

        jBasePanel.setName("jBasePanel"); // NOI18N
        jBasePanel.setLayout(new java.awt.BorderLayout());

        jViewerScrollPane.setName("jViewerScrollPane"); // NOI18N
        jBasePanel.add(jViewerScrollPane, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jBasePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jBasePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jBasePanel;
    private javax.swing.JButton jCloseAllViewerTabs;
    private javax.swing.JButton jCloseViewerTab;
    private javax.swing.JButton jExportAllQuotes;
    private javax.swing.JButton jFloatingViewer;
    private javax.swing.JButton jRenameViewerTab;
    private javax.swing.JToolBar jToolBar;
    private javax.swing.JScrollPane jViewerScrollPane;
    // End of variables declaration//GEN-END:variables


}
