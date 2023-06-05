/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer;

import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.TalkerPublishedQuoteEvent;
import com.eclipsemarkets.event.face.ViewerColumnSettingsChangedEvent;
import com.eclipsemarkets.event.gateway.MessageSentBackEvent;
import com.eclipsemarkets.event.parser.QuoteParsedEvent;
import com.eclipsemarkets.event.pricer.QuoteAutoPricedEvent;
import com.eclipsemarkets.event.pricer.QuotePricedEvent;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.data.PbconsoleQuoteFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.IPbcFace;
import static com.eclipsemarkets.pbc.face.viewer.ViewerTableType.FILTER_RESULT;
import com.eclipsemarkets.pbc.face.viewer.model.IViewerDataModel;
import com.eclipsemarkets.pbc.face.viewer.model.IViewerDataModelKernel;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerModelFactory;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterCriteria;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerFilterCriteriaType;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerPeriodsOperator;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerSearchFactory;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.substance.PbconsoleViewerPreviewPainter;
import java.awt.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXPanel;
import org.pushingpixels.lafwidget.LafWidget;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.tabbed.VetoableTabCloseListener;

/**
 * PointBoxViewer.java
 * <p>
 * it consists of a bunch of IViewerTablePanel
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 6:33:40 AM
 */
class PointBoxViewer implements IPbcViewer
{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PointBoxViewer.class.getName());
    }
    private IPbcFace face;

    private JXPanel viewerBasePanel;
    private JTabbedPane viewerTabbedPane;

    /**
     * key: uniqueTabName; value: IViewerTablePanel
     */
    private final HashMap<String, IViewerTablePanel> tabStorage;

    /**
     * key: uniqueFilterTabName
     */
    private HashMap<String, ArrayList<IViewerFilterCriteria>> criteriaListStorage;

    /**
     * publishing quote target
     */
    private IViewerDataModelKernel targetViewerDataModel;

    /**
     * Search tab data model
     */
    private IViewerDataModelKernel searchViewerDataModel;
    
    private boolean isPersonalized;
    
    
    private Point position;
    private JViewport viewport;
    
    private IPbcRuntime getPbcRuntime(){
        return face.getKernel().getPointBoxConsoleRuntime();
    }

    private final int rowEmptyThreshold;

    @Override
    public int getRowEmptyThreshold() {
        return rowEmptyThreshold;
    }
    
    PointBoxViewer(IPbcFace face) {
        this.face = face;
        
        isPersonalized = false;
        
        rowEmptyThreshold = 100;

        criteriaListStorage = new HashMap<String, ArrayList<IViewerFilterCriteria>>();

        viewerBasePanel = new JXPanel(new BorderLayout());
        viewerBasePanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        tabStorage = new HashMap<String, IViewerTablePanel>();

        viewerTabbedPane = new JTabbedPane();
        try {
            viewerTabbedPane.putClientProperty(LafWidget.TABBED_PANE_PREVIEW_PAINTER,
                                                  new PbconsoleViewerPreviewPainter());
        } catch (Throwable ex) {}

        targetViewerDataModel = ViewerModelFactory.createViewerDataModelKernelInstance(this, rowEmptyThreshold);
        searchViewerDataModel = ViewerModelFactory.createViewerDataModelKernelInstance(this, rowEmptyThreshold);

        //initializeStaticViewerTablePanels();    //pre-loaded table panels
        initializeViewerTablePanels();   //saved table panels
        
        setTabCloseGuard();

        viewerBasePanel.add(viewerTabbedPane);
    }
    
    private void initializeViewerTablePanels() {
        ArrayList<String> allViewerTabUniqueNames = getPbcRuntime().getAllViewerTabUniqueNames();
        
        synchronized(tabStorage){
            if ((allViewerTabUniqueNames == null) || (allViewerTabUniqueNames.isEmpty())){
                tabStorage.put(ViewerTableType.ALL_MESSAGES.toString().toLowerCase(),
                               new ViewerTablePanel(this, ViewerTableType.ALL_MESSAGES.toString(), createDataModel(targetViewerDataModel, true, true, true, true)));
                tabStorage.put(ViewerTableType.ALL_QUOTES.toString().toLowerCase(),
                               new ViewerTablePanel(this, ViewerTableType.ALL_QUOTES.toString(), createDataModel(targetViewerDataModel, true, true, true, true)));
                tabStorage.put(ViewerTableType.INCOMING_MESSAGES.toString().toLowerCase(),
                               new ViewerTablePanel(this, ViewerTableType.INCOMING_MESSAGES.toString(), createDataModel(targetViewerDataModel, true, true, true, true)));
                tabStorage.put(ViewerTableType.OUTGOING_MESSAGES.toString().toLowerCase(),
                               new ViewerTablePanel(this, ViewerTableType.OUTGOING_MESSAGES.toString(), createDataModel(targetViewerDataModel, true, true, true, true)));
                tabStorage.put(ViewerTableType.SEARCH_RESULT.toString().toLowerCase(),
                               new ViewerTablePanel(this, ViewerTableType.SEARCH_RESULT.toString(), createDataModel(searchViewerDataModel, false, false, false, false)));
            }else{
                for (String aViewerTabUniqueName : allViewerTabUniqueNames){
                    switch (ViewerTableType.convertEnumValueToType(aViewerTabUniqueName)){
                        case UNKNOWN:
                        case FILTER_RESULT:
                            constructFilterTab(aViewerTabUniqueName, 
                                               constructViewerFilterCriteriaFromRuntimeSettings(aViewerTabUniqueName));
                            break;
                        case SEARCH_RESULT:
                            tabStorage.put(aViewerTabUniqueName.toLowerCase(),
                                   new ViewerTablePanel(this, aViewerTabUniqueName, createDataModel(searchViewerDataModel, false, false, false, false)));
                            break;
                        default:
                            tabStorage.put(aViewerTabUniqueName.toLowerCase(),
                                   new ViewerTablePanel(this, aViewerTabUniqueName, createDataModel(targetViewerDataModel, false, false, false, false)));
                            
                    }//switch
                }//for
            }
        }//synchronized(tabStorage){
        
        //present all the tabs initialized onto GUI
        presentAllTabsFromStorage();

    }

    private void presentAllTabsFromStorage() {
        synchronized(tabStorage){
            //guarantee ViewerTableType.ALL_MESSAGES.toString() to be the first tab
            presentTabFromStorage(ViewerTableType.ALL_MESSAGES.toString());
            presentTabFromStorage(ViewerTableType.ALL_QUOTES.toString());
            presentTabFromStorage(ViewerTableType.INCOMING_MESSAGES.toString());
            presentTabFromStorage(ViewerTableType.OUTGOING_MESSAGES.toString());
            Set<String> keys = tabStorage.keySet();
            Iterator<String> itr = keys.iterator();
            String tabName;
            while(itr.hasNext()){
                tabName = itr.next();
                if (tabName.equalsIgnoreCase(ViewerTableType.FILTER_RESULT.toString().toLowerCase())){
                    presentTabFromStorage(tabName);
                }
            }
        }
    }

    private void presentTabFromStorage(final String tabName){
        if (SwingUtilities.isEventDispatchThread()){
            presentTabFromStorageHelper(tabName);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    presentTabFromStorageHelper(tabName);
                }
            });
        }
    }
    private void presentTabFromStorageHelper(final String tabName){
        if (DataGlobal.isEmptyNullString(tabName)){
            return;
        }
        synchronized(tabStorage){
            if (tabStorage.containsKey(tabName.toLowerCase())){
                int index = viewerTabbedPane.indexOfTab(tabName);
                if (index < 0){ //not on the viewerTabbedPane
                    viewerTabbedPane.addTab(tabName,
                                            null, //guiEnv.getPbconsoleImageSettings().getPbimGreyIcon(),
                                            tabStorage.get(tabName.toLowerCase()).getBasePanel());
                }
            }
        }
    }

    @Override
    public void displayFaceComponent() {
        //todo: zzj - display a stand-alone PointBox viewer here
    }

    @Override
    public void hideFaceComponent() {
        //todo: zzj - hide a stand-alone PointBox viewer here
    }

    /**
     * This method is only called once. Otherwise nothing happened
     */
    @Override
    public void personalizeFaceComponent(){
        if (SwingUtilities.isEventDispatchThread()){
            personalizeFaceComponentHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    personalizeFaceComponentHelper();
                }
            });
        }
    }
    private void personalizeFaceComponentHelper(){
        if (!isPersonalized){
            isPersonalized = true;
            /**
             * Detect the viewer settings
             */
            viewerTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    Component tab = viewerTabbedPane.getSelectedComponent();
                    if (tab instanceof IViewerTablePanel){
                        ((IViewerTablePanel)tab).personalizeViewerTablePanelByGenericModelSettings();
                    }
                }
            });
            ArrayList<String> allViewerTabUniqueNames = getPbcRuntime().getAllViewerTabUniqueNames();
            synchronized(tabStorage){
                Set<String> keys = tabStorage.keySet();
                Iterator<String> itr = keys.iterator();
                IViewerTablePanel tablePanel;
                while(itr.hasNext()){
                    tablePanel = tabStorage.get(itr.next());
                    tablePanel.personalizeViewerTablePanel();
                }
                if (allViewerTabUniqueNames != null) {
                    for (String aViewerTabUniqueName : allViewerTabUniqueNames){
                        switch (ViewerTableType.convertEnumValueToType(aViewerTabUniqueName)){
                            case UNKNOWN:
                            case FILTER_RESULT:
                                constructFilterTab(aViewerTabUniqueName, 
                                                constructViewerFilterCriteriaFromRuntimeSettings(aViewerTabUniqueName));
                                presentTabFromStorage(aViewerTabUniqueName);
                                break;

                        }//switch
                    }//for
                }
                if (tabStorage.containsKey(ViewerTableType.ALL_MESSAGES.toString().toLowerCase())){
                    final int index = viewerTabbedPane.indexOfTab(ViewerTableType.ALL_MESSAGES.toString());
                    if (index > -1){
                        if (SwingUtilities.isEventDispatchThread()){
                            viewerTabbedPane.setSelectedIndex(index);
                        }else{
                            SwingUtilities.invokeLater(new Runnable(){
                                @Override
                                public void run() {
                                    viewerTabbedPane.setSelectedIndex(index);
                                }
                            });
                        }
                    }
                }
            }//synchronized(tabStorage){
        }
    }

    private void handleViewerColumnSettingsChangedEvent(ViewerColumnSettingsChangedEvent event) {
        if (event == null){
            return;
        }
        IViewerTablePanel tablePanel;
        if (PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName())){
            //Case-1: users want to change it for all the tabs;
            /**
             * todo: loop over tabStorage
             */
            for(String tabName:tabStorage.keySet()){
                tablePanel=tabStorage.get(tabName.toLowerCase());
                tablePanel.personalizeViewerTablePanel();
            }
            
        }else{
            //Case-2: users want to change a specific tab. 
            tablePanel = tabStorage.get(event.getViewerTableUniqueName().toLowerCase());
            if (tablePanel != null){
                tablePanel.personalizeViewerTablePanel();
            }
        }
    }

    @Override
    public void releaseFaceComponent() {
        Set<String> keys = tabStorage.keySet();
        Iterator<String> itr = keys.iterator();
        IViewerTablePanel tablePanel;
        while(itr.hasNext()){
            tablePanel = tabStorage.get(itr.next());
            tablePanel.synchronizedViewerTablePanelSettings();
        }
    }

    @Override
    public IPbcKernel getKernel() {
        return face.getKernel();
    }

    @Override
    public JFrame getPointBoxFrame() {
        return face.getPointBoxMainFrame();
    }
    
    @Override
    public void displayClearPortMainFrameWithQuote(IPbsysOptionQuoteWrapper targetQuoteWrapper){
        if(face.getClearPortMainFrame()==null ||face.getClearPortMainFrame().isDisposed()){
            if (JOptionPane.showConfirmDialog(face.getPointBoxMainFrame(),
                    "You should login ClearPort for clearing trade. Do you want to login now?",
                    "Message",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                    face.displayClearPortLoginFrame(face, face.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings());

            }else{ 
            }
        }else{
            face.displayClearPortMainFrameWithQuote(targetQuoteWrapper);
        }
    }

    @Override
    public IGatewayConnectorBuddy getPointBoxLoginUser() {
        return face.getPointBoxLoginUser();
    }

    @Override
    public void gatewayConnectorBuddyHighlighted(IGatewayConnectorBuddy highlightenUser) {
        face.gatewayConnectorBuddyHighlighted(highlightenUser,true);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getSortedBuddyList(GatewayServerType gatewayServerType) {
        return face.getSortedBuddyList(gatewayServerType);
    }
    
    @Override
    public  ArrayList<IGatewayConnectorGroup> getAllGroups(){
        return face.getAllGroups();
    }

    @Override
    public boolean hasFilterTab(String uniqueFilterTabName) {
        if (DataGlobal.isEmptyNullString(uniqueFilterTabName)){
            return true;
        }
        synchronized(tabStorage){
            return (tabStorage.get(uniqueFilterTabName.toLowerCase()) != null);
        }
    }

    @Override
    public void refreshTableRow(int tableModelRowIndex) {
        //logger.log(Level.INFO, " ---debug: refreshTableRow ..............");
        synchronized(tabStorage){
            Set<String> keys = tabStorage.keySet();
            Iterator<String> itr = keys.iterator();
            while(itr.hasNext()){
                tabStorage.get(itr.next()).refreshRow(tableModelRowIndex);
            }
        }
    }

    @Override
    public ArrayList<IPbsysOptionQuote> retrieveAllQuotes() {
        return targetViewerDataModel.retrieveAllQuotes();
    }

    @Override
    public void clearViewer() {
        targetViewerDataModel.clearDataModelKernel();
        searchViewerDataModel.clearDataModelKernel();
    }

    @Override
    public void keepTodayData() {
        targetViewerDataModel.keepTodayDataModelKernel();
        searchViewerDataModel.keepTodayDataModelKernel();
    }
    
    @Override
    public void refreshViewer() {
        //logger.log(Level.INFO, " ---debug: refreshViewer ..............");
        if (SwingUtilities.isEventDispatchThread()){
            refreshViewerHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    refreshViewerHelper();
                }
            });
        }
    }

    private void refreshViewerHelper(){
        synchronized(tabStorage){
            Set<String> keys = tabStorage.keySet();
            Iterator<String> itr = keys.iterator();
            while (itr.hasNext()){
                tabStorage.get(itr.next()).refreshTable();
            }
        }
    }

    @Override
    public void prepareForSearchResult(){
        synchronized(tabStorage){
            IViewerTablePanel searchTab = tabStorage.get(ViewerTableType.SEARCH_RESULT.toString().toLowerCase());
            if (searchTab != null){
                searchTab.clearTablePanel();
                presentTabFromStorage(ViewerTableType.SEARCH_RESULT.toString());
            }
        }
    }

    @Override
    public void publishSearchResult(IPbsysOptionQuote quote) {
        //PbsysOptionQuoteWrapper wrapper;
        //wrapper = new PbsysOptionQuoteWrapper(quote, system.getPbconsolePricer());
        //zzj//wrapper.addPbsysOptionQuoteWrapperListener((IPbsysOptionQuoteWrapperListener)searchDataModel);
        searchViewerDataModel.publishQuote(quote);

    }

    @Override
    public void activateViewerTab(final String tabID) {
        if (DataGlobal.isEmptyNullString(tabID)){
            return;
        }
        synchronized(tabStorage){
            if (tabStorage.containsKey(tabID.toLowerCase())){
                if (SwingUtilities.isEventDispatchThread()){
                    viewerTabbedPane.setSelectedComponent(tabStorage.get(tabID.toLowerCase()).getBasePanel());
                }else{
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            viewerTabbedPane.setSelectedComponent(tabStorage.get(tabID.toLowerCase()).getBasePanel());
                        }
                    });
                }
            }
        }
    }

    @Override
    public IViewerTablePanel getCurrentViewerTablePanel() {
        synchronized(tabStorage){
            Component tab = viewerTabbedPane.getSelectedComponent();
            if (tab instanceof IViewerTablePanel){
                return (IViewerTablePanel)tab;
            }else{
                PointBoxTracer.recordSevereException(logger, new Exception("Should be able to get a IViewerTablePanel"));
                return null;
            }
        }
    }

    /**
     * construct a new filter tab if it was not created. Otherwise it will be presented.
     * @param uniqueTabName
     * @param criteriaList 
     */
    @Override
    public void constructFilterTab(final String uniqueTabName, final ArrayList<IViewerFilterCriteria> criteriaList) {
        if ((criteriaList == null)||(criteriaList.isEmpty())){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            constructFilterTabHelper(uniqueTabName, criteriaList);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    constructFilterTabHelper(uniqueTabName, criteriaList);
                }
            });
        }
    }
    
    private void constructFilterTabHelper(String viewerTabUniqueName, ArrayList<IViewerFilterCriteria> criteriaList) {
        if (DataGlobal.isEmptyNullString(viewerTabUniqueName)){
            return;
        }
        IViewerTablePanel filterTab;
        synchronized(tabStorage){
            if (tabStorage.containsKey(viewerTabUniqueName.toLowerCase())){
                filterTab = tabStorage.get(viewerTabUniqueName.toLowerCase());
                filterTab.installTableRowFilter(criteriaList);  //update criteria
                presentTabFromStorage(viewerTabUniqueName);
            }else{
                filterTab =  new ViewerTablePanel(this, viewerTabUniqueName, createDataModel(targetViewerDataModel, true, true, true, true));
                filterTab.setViewerTableType(ViewerTableType.FILTER_RESULT);
                filterTab.personalizeViewerTablePanel();
                filterTab.installTableRowFilter(criteriaList);
                viewerTabbedPane.addTab(viewerTabUniqueName,
                                        null,
                                        filterTab.getBasePanel());
                viewerTabbedPane.setSelectedIndex(viewerTabbedPane.getTabCount() - 1);
                tabStorage.put(filterTab.getViewerTableName().toLowerCase(), filterTab);
                criteriaListStorage.put(viewerTabUniqueName.toLowerCase(), criteriaList);
            }
        }
    }

    private ArrayList<IViewerFilterCriteria> constructViewerFilterCriteriaFromRuntimeSettings(String uniqueFilterTabName) {
        ArrayList<IViewerFilterCriteria> criteriaList = new ArrayList<IViewerFilterCriteria>();
        if (DataGlobal.isEmptyNullString(uniqueFilterTabName)){
            return criteriaList;
        }
        ArrayList<String> items;
        /*create structures criteria list*/
        if (getPbcRuntime().getViewerFilterBooleanValue(uniqueFilterTabName, FilterPropertyKey.jStructures01_Selected)){

            items = new ArrayList<String>();
            String structure = getPbcRuntime().getViewerFilterStringValue(uniqueFilterTabName, FilterPropertyKey.jStructures01_SelectedItem);
            items.add(DataGlobal.getProperEngineStrategyName(structure));
            items.add(DataGlobal.getProperParsingStrategyName(structure));

            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strategy, items, 0));
        }

        /*create brokers name criteria list*/
        String brokerNames = getPbcRuntime().getViewerFilterStringValue(
                                uniqueFilterTabName, FilterPropertyKey.jBrokerNames_SelectedName);
        if (DataGlobal.isNonEmptyNullString(brokerNames)){
            items = new ArrayList<String>();
            items.add(brokerNames); //index-0-item contains a long ";"-delimited string
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Brokers, items, 0));
        }

        /*create quote class name criteria list*/
        ArrayList<String> quoteClass = FilterPropertyKey.retrieveFilterValueArrayFromPersistency(getPbcRuntime().getViewerFilterStringValue(
                                uniqueFilterTabName, FilterPropertyKey.jClass_SelectedName));
        if (!quoteClass.isEmpty()){
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.QuoteClass, quoteClass, 0));
        }

        /*create quote group name criteria list*/
        ArrayList<String> quoteGroups = FilterPropertyKey.retrieveFilterValueArrayFromPersistency(getPbcRuntime().getViewerFilterStringValue(
                                uniqueFilterTabName, FilterPropertyKey.jGroup_SelectedName));
        if (!quoteGroups.isEmpty()){
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.QuoteGroup, quoteGroups, 0));
        }

        /*create quote code name criteria list*/
        ArrayList<String> quoteCodes = FilterPropertyKey.retrieveFilterValueArrayFromPersistency(getPbcRuntime().getViewerFilterStringValue(
                                uniqueFilterTabName, FilterPropertyKey.jCode_SelectedName));
        if (!quoteCodes.isEmpty()){
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.QuoteCode, quoteCodes, 0));
        }

        /*create strikes criteria list*/
        criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strikes,
                                                 generateStrikesItemsList(uniqueFilterTabName, 0),
                                                 0));

        /*create time period search criteria list*/
        criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Period, null, 0));

        if (getPbcRuntime().getViewerFilterBooleanValue(uniqueFilterTabName, FilterPropertyKey.jLeg2Included)){

            if (getPbcRuntime().getViewerFilterBooleanValue(uniqueFilterTabName, FilterPropertyKey.jStructures02_Selected)){
                items = new ArrayList<String>();
                String structure = getPbcRuntime().getViewerFilterStringValue(uniqueFilterTabName,
                                                                             FilterPropertyKey.jStructures02_SelectedItem);
                items.add(DataGlobal.getProperParsingStrategyName(structure));
                items.add(DataGlobal.getProperEngineStrategyName(structure));

                criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strategy, items, 1));
            }

            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strikes,
                                                     generateStrikesItemsList(uniqueFilterTabName, 1),
                                                     1));

            /*create time period search criteria list*/
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Period, null, 1));
        }
        return criteriaList;
    }

    private ArrayList<String> generateStrikesItemsList(String uniqueFilterTabName, int legIndex){
        ArrayList<String> items = new ArrayList<String>();
        String value;
        switch (legIndex){
            case 0:
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike1Leg01_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike2Leg01_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike3Leg01_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike4Leg01_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                break;
            case 1:
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike1Leg02_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike2Leg02_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike3Leg02_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                value = getPbcRuntime().getViewerFilterStringValue(
                            uniqueFilterTabName, FilterPropertyKey.jStrike4Leg02_text);
                if (DataGlobal.isNonEmptyNullString(value)){
                    items.add(value);
                }
                break;
        }
        return items;
    }

    private IViewerFilterCriteria getViewerFilterCriteria(String uniqueFilterTabName,
                                                          ViewerFilterCriteriaType terms,
                                                          ArrayList<String> items,
                                                          int legIndex)
    {
        IViewerFilterCriteria criteria = null;

        switch(terms){
            case QuoteClass:
                criteria = ViewerSearchFactory.getViewerFilterByClassInstance(items);
                criteria.setFilterCriteria(ViewerFilterCriteriaType.QuoteClass);
                break;
            case QuoteGroup:
                criteria = ViewerSearchFactory.getViewerFilterByGroupInstance(items);
                criteria.setFilterCriteria(ViewerFilterCriteriaType.QuoteGroup);
                break;
            case QuoteCode:
                criteria = ViewerSearchFactory.getViewerFilterByCodeInstance(items);
                criteria.setFilterCriteria(ViewerFilterCriteriaType.QuoteCode);
                break;
            case Strategy:  criteria = ViewerSearchFactory.getViewerFilterByStrategiesInstance(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Strategy);
                            break;
            case Brokers:   criteria = ViewerSearchFactory.getViewerFilterByBrokersInstance(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Brokers);
                            break;
            case Location:  criteria = ViewerSearchFactory.getViewerFilterByLocationsInstance(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Location);
                            break;
            case Period:    GregorianCalendar selectedStartDate, selectedEndDate;
                            ViewerPeriodsOperator startOperator, endOperator;

                            if (legIndex == 0){
                                selectedStartDate = new GregorianCalendar();
                                selectedStartDate.setTimeInMillis(
                                        getPbcRuntime().getViewerFilterLongValue(
                                                                uniqueFilterTabName,
                                                                FilterPropertyKey.leg01_selectedStartDate_value));
                                selectedEndDate = new GregorianCalendar();
                                selectedEndDate.setTimeInMillis(
                                        getPbcRuntime().getViewerFilterLongValue(
                                                                uniqueFilterTabName,
                                                                FilterPropertyKey.leg01_selectedEndDate_value));
                                startOperator = ViewerPeriodsOperator.convertToType(
                                                    getPbcRuntime().getViewerFilterStringValue(
                                                        uniqueFilterTabName, FilterPropertyKey.leg01_startOperator_value));
                                endOperator = ViewerPeriodsOperator.convertToType(
                                                    getPbcRuntime().getViewerFilterStringValue(
                                                        uniqueFilterTabName, FilterPropertyKey.leg01_endOperator_value));
                            }else{
                                selectedStartDate = new GregorianCalendar();
                                selectedStartDate.setTimeInMillis(
                                        getPbcRuntime().getViewerFilterLongValue(
                                                                uniqueFilterTabName,
                                                                FilterPropertyKey.leg02_selectedStartDate_value));
                                selectedEndDate = new GregorianCalendar();
                                selectedEndDate.setTimeInMillis(
                                        getPbcRuntime().getViewerFilterLongValue(
                                                                uniqueFilterTabName,
                                                                FilterPropertyKey.leg02_selectedEndDate_value));
                                startOperator = ViewerPeriodsOperator.convertToType(
                                                    getPbcRuntime().getViewerFilterStringValue(
                                                        uniqueFilterTabName, FilterPropertyKey.leg02_startOperator_value));
                                endOperator = ViewerPeriodsOperator.convertToType(
                                                    getPbcRuntime().getViewerFilterStringValue(
                                                        uniqueFilterTabName, FilterPropertyKey.leg02_endOperator_value));
                            }

                            criteria = ViewerSearchFactory.getViewerFilterByPeriodsInstance(selectedStartDate,
                                                                                             selectedEndDate,
                                                                                             startOperator,
                                                                                             endOperator);

                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Period);
                            break;
            case Strikes:   criteria = ViewerSearchFactory.getViewerFilterByStrikesInstance(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Strikes);
                            break;

        }

        if (criteria != null){
            criteria.setFilterLegIndex(legIndex);
        }

        return criteria;
    }

    @Override
    public void renameFilterResultTab(final String oldTabName, final String newTabName, final ArrayList<IViewerFilterCriteria> newCriteria) {
        if (SwingUtilities.isEventDispatchThread()){
            renameFilterResultTabHelper(oldTabName, newTabName, newCriteria);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    renameFilterResultTabHelper(oldTabName, newTabName, newCriteria);
                }
            });
        }
    }

    private void renameFilterResultTabHelper(final String oldTabName, final String newTabName, final ArrayList<IViewerFilterCriteria> newCriteria) {
        if (DataGlobal.isEmptyNullString(oldTabName)){
            return;
        }
        if (DataGlobal.isEmptyNullString(newTabName)){
            return;
        }
        synchronized(tabStorage){
            IViewerTablePanel filterTab = tabStorage.get(oldTabName.toLowerCase());
            if ((filterTab != null) && (newCriteria != null)){
                if (newTabName.equalsIgnoreCase(oldTabName)){

                    filterTab.setViewerTabUniqueName(newTabName);
                    tabStorage.put(newTabName.toLowerCase(), filterTab);
                    criteriaListStorage.put(newTabName.toLowerCase(), newCriteria);

                }else{
                    int index = viewerTabbedPane.indexOfTab(oldTabName);
                    if (index > -1){
                        viewerTabbedPane.removeTabAt(index);
                    }
                    tabStorage.remove(oldTabName.toLowerCase());
                    criteriaListStorage.remove(oldTabName.toLowerCase());

                    filterTab.setViewerTabUniqueName(newTabName);
                    tabStorage.put(newTabName.toLowerCase(), filterTab);
                    criteriaListStorage.put(newTabName.toLowerCase(), newCriteria);

                    this.getPbcRuntime().renamePbcViewerUniqueName(oldTabName, newTabName, newCriteria);
                }

                presentTabFromStorage(newTabName);
            }
        }
    }
    
    @Override
    public ArrayList<String> retrievedBufferedLocations() {
        return targetViewerDataModel.retrievedBufferedLocations();
    }

    private void handleQuoteParsedEvent(QuoteParsedEvent event) {
        //this processing is moved into "handleTalkerPublishedQuoteEvent"
        if ((event == null) || (event.getParsedQuotes() == null) || (event.getParsedQuotes().isEmpty())){
            return;
        }
        targetViewerDataModel.refreshForQuotesParsedEvent(event.getParsedQuotes());
    }

    @Override
    public void handlePointBoxEvent(PointBoxConsoleEvent event) {
        if (event instanceof TalkerPublishedQuoteEvent){
            handleTalkerPublishedQuoteEvent((TalkerPublishedQuoteEvent)event);
        }else if (event instanceof MessageSentBackEvent){
            handleMessageSentBackEvent((MessageSentBackEvent)event);
        }else if (event instanceof QuoteParsedEvent){
            handleQuoteParsedEvent((QuoteParsedEvent)event);
        }else if (event instanceof QuoteAutoPricedEvent){
            handleQuoteAutoPricedEvent((QuoteAutoPricedEvent)event);
        }else if (event instanceof QuotePricedEvent){
            handleQuotePricedEvent((QuotePricedEvent)event);
        }else if (event instanceof ViewerColumnSettingsChangedEvent){
            handleViewerColumnSettingsChangedEvent((ViewerColumnSettingsChangedEvent)event);
        }
    }

    private void handleQuoteAutoPricedEvent(QuoteAutoPricedEvent event) {
        if ((event == null) || (event.getPricedQuotes() == null) || (event.getPricedQuotes().isEmpty())){
            return;
        }
        targetViewerDataModel.refreshForQuotesPricedEvent(event.getPricedQuotes());
    }

    private void handleQuotePricedEvent(QuotePricedEvent event) {
        if ((event == null) || (event.getPricedQuotes() == null) || (event.getPricedQuotes().isEmpty())){
            return;
        }
        targetViewerDataModel.refreshForQuotesPricedEvent(event.getPricedQuotes());
    }
    
    private void handleMessageSentBackEvent(MessageSentBackEvent event){
        if ((event == null) || (event.getSentQuoteMessage() == null)) {
            return;
        }
        IPbsysOptionQuote originalQuote = PbconsoleQuoteFactory.hookOutgoingMessage(event.getSentQuoteMessage());
        getKernel().raisePointBoxEvent(
                new QuoteParsedEvent(PointBoxEventTarget.PbcFace,
                                     originalQuote));
        getKernel().raisePointBoxEvent(
                new QuoteParsedEvent(PointBoxEventTarget.PbcPricer,
                                     originalQuote));
        getKernel().raisePointBoxEvent(
                new QuoteParsedEvent(PointBoxEventTarget.PbcStorage,
                                     originalQuote));
    }

    /**
     * A quote was just created based on a received message and published onto the corresponding message tab in the talker.
     * Now, viewer is ready to publish such a quote onto viewer tab
     * @param tpqe
     */
    private void handleTalkerPublishedQuoteEvent(TalkerPublishedQuoteEvent event) {
        if ((event == null) || (event.getPublishedQuote() == null)){
            return;
        }
        
        javax.swing.JScrollPane scrollPane=null;
        //decide the type of message and reset diffrent tab table view after updates.
        if(event.getPublishedQuote().getInstantMessage().isOutgoing()){
            
            for(String key: tabStorage.keySet()){
                if(key.equals(ViewerTableType.INCOMING_MESSAGES.toString().toLowerCase())){
                    ViewerTablePanel panel=(ViewerTablePanel)tabStorage.get(key);   //get Incoming tab table view.
                    scrollPane=panel.getJViewerScrollPane(); 
                }
            }
        }else{
             for(String key: tabStorage.keySet()){
                if(key.equals(ViewerTableType.OUTGOING_MESSAGES.toString().toLowerCase())){       //get Outgoing tab table view
                   ViewerTablePanel panel=(ViewerTablePanel)tabStorage.get(key);
                   scrollPane=panel.getJViewerScrollPane(); 
                }
            }
        }        
         viewport = scrollPane.getViewport();
         position = viewport.getViewPosition();

         targetViewerDataModel.publishQuote(event.getPublishedQuote());             //critical code. publis quote on GUI
        
         try {
                Thread.sleep(10);       //make sure all updates in GUI end and then reset the View Position.
            } catch (InterruptedException ex) {
                Logger.getLogger(PointBoxViewer.class.getName()).log(Level.SEVERE, null, ex);
            }

            resetViewPosition();            //reset to previous viewPosition and don't allow Viewer Jump.
    }
    
    private void resetViewPosition(){
        if (SwingUtilities.isEventDispatchThread()){
                    viewport.setViewPosition(position);
            }else{
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        viewport.setViewPosition(position);
                    }
                });
         }
    }
    

    private IViewerDataModel createDataModel(IViewerDataModelKernel dataModelKernel,
                                             boolean enabledAutoPricer,
                                             boolean listenToParser,
                                             boolean listenToPricer,
                                             boolean listenToAutoPricer){
        IViewerDataModel model = ViewerModelFactory.createViewerDataModelInstance(dataModelKernel, null);
        return model;
    }
    
    private void setTabCloseGuard(){
        SubstanceLookAndFeel.registerTabCloseChangeListener(viewerTabbedPane, new VetoableTabCloseListener() {
            @Override
            public void tabClosed(JTabbedPane tabbedPane, Component tabComponent) {
            }
            @Override
            public void tabClosing(final JTabbedPane tabbedPane, final Component tabComponent) {
            }
            @Override
            public boolean vetoTabClosing(JTabbedPane tabbedPane, Component tabComponent) {
                boolean guarded = false;
                String msg = "Discard filter result?";
                if (tabComponent instanceof IViewerTablePanel){
                    ViewerTableType type = ((IViewerTablePanel)tabComponent).getViewerTableType();
                    if (type != null){
                        if (type.equals(ViewerTableType.SEARCH_RESULT)){
                            msg = "Discard search result?";
                            guarded = false;
                        }else if ((type.equals(ViewerTableType.FILTER_RESULT)) || (type.equals(ViewerTableType.UNKNOWN))){
                            msg = "Discard filter result?";
                            guarded = false;
//                        }else if (type.equals(ViewerTableType.INCOMING_MESSAGES)){
//                            msg = "Discard Incoming Messages Tab?";
//                            guarded = false;
//                        }else if (type.equals(ViewerTableType.OUTGOING_MESSAGES)){
//                            msg = "Discard Outgoing Messages Tab?";
//                            guarded = false;
                        }else{
                            guarded = true;
                        }
                    }//Users should never be allowed to close Incoming, Outgoing, All, and Parsed message tabs.
                    if (!guarded){
                        if (JOptionPane.showConfirmDialog(tabComponent, 
                                msg, "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
                            guarded = true;
                        }else{
                            if (tabComponent instanceof IViewerTablePanel){
                                if ((type.equals(ViewerTableType.FILTER_RESULT)) || (type.equals(ViewerTableType.UNKNOWN))){
                                    tabStorage.remove(((IViewerTablePanel)tabComponent).getViewerTableName().toLowerCase());
                                    getPbcRuntime().removePbcViewerSettings(((IViewerTablePanel)tabComponent).getViewerTableName());
                                }
                            }
                        }
                    }
                }//if
                return guarded;
            }
        });
    }

    @Override
    public JPanel getViewerBasePanel() {
        return viewerBasePanel;
    }

//    @Override
//    public void displayStripPricerWithQuote(IPbsysOptionQuoteWrapper targetQuoteWrapper) {
//        face.displayStripPricerWithQuote(targetQuoteWrapper);
//    }

    @Override
    public void displaySimPricerWithQuote(IPbsysOptionQuoteWrapper targetQuoteWrapper, PointBoxQuoteType category) {
        face.displaySimPricerWithQuote(targetQuoteWrapper, category);
    }

    @Override
    public HashMap<String, IViewerTablePanel> getTabStorage() {
        return tabStorage;
    }
}
