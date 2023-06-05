/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.MessageRecievedEvent;
import com.eclipsemarkets.event.gateway.MessageSentEvent;
import com.eclipsemarkets.event.gateway.SystemLoginRequestInitiatedEvent;
import com.eclipsemarkets.event.runtime.PointBoxConsoleSettingsInitializedEvent;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.ColorName;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.LookAndFeelType;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.IPbcSplashScreen;
import com.eclipsemarkets.pbc.PbcComponent;
import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.face.talker.*;
import com.eclipsemarkets.pbc.face.talker.messaging.BuddyButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.messaging.GroupButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.messaging.MasterMessagingBoard;
import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;
import com.eclipsemarkets.pbc.face.viewer.IViewerTablePanel;
import com.eclipsemarkets.pbc.face.viewer.ViewerFactory;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerSearchFactory;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.PbcAudioFileName;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.pbc.tester.PointBoxTestDialog;
import com.eclipsemarkets.pbc.web.PbcReleaseInformation;
import com.eclipsemarkets.pbc.web.local.IPointBoxWebAgentListener;
import com.eclipsemarkets.pricer.PbcPricerType;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.pbc.clearport.ClearPortLoginFrame;
import com.eclipsemarkets.pbc.pricer.sim.IPbcStructuredQuoteBuilder;
import com.eclipsemarkets.release.PbcMode;
import com.eclipsemarkets.release.PointBoxConfig;
import com.eclipsemarkets.web.controller.service.PointBoxControllerProxy;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.swing.*;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * PbcFace
 * <P>
 * PbcFace is a container of all the GUI components used to display PointBox system
 * GUI for the current user.
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 25, 2011 at 10:44:19 AM
 */
class PbcFace extends PbcComponent implements IPbcFace {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbcFace.class.getName());
    }

    private final ExecutorService soundPlayer;
    
    /**
     * A dedicated monitor release version of this console. If the server-side has updates, 
     * it will pop up update window for users to initiate a procedure of PBC update.
     * 
     * Disabled the flashing icon for update notification
     */
    private PbcReleaseMonitor releaseMonitor;

    
    private IClearPortMainFrame clearPortMainFrame;
    /**
     * This structure  save all the face components in a list so as to be easily
     * operating on every component in the face
     */
    private final EnumMap<PbcFaceComponentType, IPbcFaceComponent> faceComponents;

    PbcFace(IPbcKernel kernel) {
        super(kernel);
        kernel.updateSplashScreen("Register " + getKernel().getSoftwareName() + "'s face component...", Level.INFO, 100);
        soundPlayer = Executors.newSingleThreadExecutor();
        faceComponents = new EnumMap<PbcFaceComponentType, IPbcFaceComponent>(PbcFaceComponentType.class);
        releaseMonitor = new PbcReleaseMonitor();
        try {
            initializeFaceComponents();
        } catch (PointBoxFatalException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void displayDistributionBuddyListPanel() {
        IPbcTalker talker = this.getPointBoxTalker();
        if (talker != null){
            talker.displayDistributionBuddyListPanel();
        }
    }

    @Override
    public void hideDistributionBuddyListPanel() {
        IPbcTalker talker = this.getPointBoxTalker();
        if (talker != null){
            talker.hideDistributionBuddyListPanel();
        }
    }

//    @Override
//    public boolean isDisplayDistributionBuddyListPanelRequired() {
//        PreferencePanel aPreferencePanel = PreferencePanel.getSingleton();
//        if (aPreferencePanel == null){
//            return false;
//        }else{
//            return aPreferencePanel.isDisplayDistributionBuddyListPanelRequired();
//        }
//    }

    @Override
    public void notifyMsAccessInterrupted(final String errMsg) {
        if (SwingUtilities.isEventDispatchThread()){
            notifyMsAccessInterruptedHelper(errMsg);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    notifyMsAccessInterruptedHelper(errMsg);
                }
            });
        }
    }
    
    private void notifyMsAccessInterruptedHelper(final String errMsg) {
        getPointBoxFrame().notifyMsAccessInterrupted();
    }

    @Override
    public void notifyArchiveMethodChanged() {
        this.getPointBoxTalker().notifyArchiveMethodChanged();
    }

    @Override
    public String getArchiveWarningMessage() {
        PointBoxFrame aPointBoxFrame = this.getPointBoxFrame();
        if (aPointBoxFrame == null){
            return null;
        }else{
            return aPointBoxFrame.getArchiveWarningMessage();
        }
    }

    @Override
    public List<String> retrievePbcPricingRuntimeDownloadUrls(PointBoxQuoteCode symbol) {
        return getKernel().retrievePbcPricingRuntimeDownloadUrls(symbol);
    }

    @Override
    public PbcPricerType getPbcPricerType() {
        return PointBoxConsoleProperties.getSingleton().retrievePbcPricerType(getKernel().getPointBoxLoginUser().getIMUniqueName());
    }

    @Override
    public void setPbcPricerType(PbcPricerType aPbcPricerType) {
        PointBoxConsoleProperties.getSingleton().storePbcPricerType(aPbcPricerType, getKernel().getPointBoxLoginUser().getIMUniqueName());
    }

    @Override
    public void load() throws PointBoxFatalException {
        getKernel().updateSplashScreen("Load " + getKernel().getSoftwareName() + "'s GUI components ...", Level.INFO, 100);
        getLoginWindow(GatewayServerType.PBIM_SERVER_TYPE).loadAnonymousLoginSettings();
    }

    @Override
    public void personalize() {
        getKernel().updateSplashScreen("Personalize " + getKernel().getSoftwareName() + "'s GUI components ...", Level.INFO, 100);
        Set<PbcFaceComponentType> keys = faceComponents.keySet();
        Iterator<PbcFaceComponentType> itr = keys.iterator();
        while(itr.hasNext()){
            faceComponents.get(itr.next()).personalizeFaceComponent();
        }
        
        PointBoxControllerProxy.setWebServiceWsdl(PointBoxConfig.getSingleton().getPointBoxControllerWsdl(PointBoxConsoleProperties.getSingleton().retrieveControllerIPforAllUsers(getKernel().getPbcMode())));
            
        if (PointBoxTalker.getPointBoxTalkerSingleton(PbcFace.this) instanceof IPointBoxWebAgentListener){
            getKernel().getPointBoxConsoleWeb().addPointBoxWebAgentListener((IPointBoxWebAgentListener)PointBoxTalker.getPointBoxTalkerSingleton(PbcFace.this));
        }
        if (releaseMonitor != null){
            releaseMonitor.invoke(this);
        }
    }

    /**
     * 
     * @deprecated - This method should be removed
     */
    private void storeMainFrameStateDuringUnloading(){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            if(getPointBoxFrame().isFloatingStyle()){       // when closing, persist the location and size of floating frames into files.
//                persistMainFloatingFrameLoacationAndSize(getPointBoxFrame(),prop);
//                persistBuddyFloatingFrameLoacationAndSize(getPointBoxFrame().getBuddyListFloatingFrame(),prop);
//                persistMessageFloatingFrameLoacationAndSize(getPointBoxFrame().getMessagingFloatingFrame(),prop);
                getPointBoxFrame().getBuddyListFloatingFrame().setVisible(false);
                getPointBoxFrame().getMessagingFloatingFrame().setVisible(false);
            }
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }
    }

    @Override
    public void clickPricerBuddy() {
        this.getPointBoxTalker().clickPricerBuddy();
    }

//    private void storeBuddyListPanelsDuringUnloading(){
//        try{
//            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
//            if (getKernel().getPointBoxLoginUser() != null){
//                HashSet<String> persistentRegularPanelListNames = new HashSet<String>();
////                for(int i=0;i<getPointBoxTalker().getBuddyListTabbedPane().getTabCount();i++){
//////                    if(!(GatewayServerType.PBIM_DISTRIBUTION_TYPE.toString().equalsIgnoreCase(getPointBoxTalker().getBuddyListTabbedPane().getTitleAt(i))
//////                            || GatewayServerType.PBIM_CONFERENCE_TYPE.toString().equalsIgnoreCase(getPointBoxTalker().getBuddyListTabbedPane().getTitleAt(i))))
//////                    {
////////                        AbstractBuddyListPanel panel=(AbstractBuddyListPanel)getPointBoxTalker().getBuddyListTabbedPane().getComponentAt(i);
////////                        if(!panel.getMasterLoginUser().getIMServerType().equals(GatewayServerType.PBIM_SERVER_TYPE)){
////////                            persistentRegularPanelListNames.add(panel.getDistListName());
////////                        }
//////                    }
////                }//for
//                prop.storeBuddyListPanelStrs(getKernel().getPointBoxLoginUser().getIMUniqueName(), 
//                                             prop.generateBuddyListPanelStrs(getKernel().getPointBoxLoginUser().getIMUniqueName(), persistentRegularPanelListNames));
//                /**
//                 * Check if there are deprecated regular buddy-list which were closed and not necessary to be persistent anymore 
//                 */
//                this.getKernel().getPointBoxConsoleRuntime().updatePersistentRegularBuddyListSettings(persistentRegularPanelListNames);
//        
//            }
//        }catch (Exception ex){
//            PointBoxTracer.recordSevereException(logger, ex);
//        }
//    }

    private void storeMessagingTabsDuringUnloading(){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            if (getKernel().getPointBoxLoginUser() != null){
                prop.removeProperties(PointBoxConsoleProperties.BuddyMessageTab_+getKernel().getPointBoxLoginUser().getIMUniqueName());
                prop.removeProperties(PointBoxConsoleProperties.GroupMessageTab_+getKernel().getPointBoxLoginUser().getIMUniqueName());
                //1)store buddyMessageTabs(In here, we store 'buddyscreenname=buddyserverType,ownerscreenname=ownerserverType')
                //2)store groupMessageTabs(In here, we store 'loginuserScreenname=loginuserServertype,groupname')
                ArrayList<IButtonTabComponent> tabs=((MasterMessagingBoard)getPointBoxTalker().getMessagingPaneManager()
                        .getMasterMessagingBoard()).getAllVisibleTabeButtons();
                for(IButtonTabComponent tab:tabs){
                    if(tab instanceof BuddyButtonTabComponent){
                        BuddyButtonTabComponent buddyTab=(BuddyButtonTabComponent)tab;
                        prop.storeBuddyMessageTabStrs(getKernel().getPointBoxLoginUser().getIMUniqueName(),
                                                    buddyTab.getBuddy().getIMScreenName(),
                                                    buddyTab.getBuddy().getIMServerType(),
                                                    buddyTab.getLoginUser().getIMScreenName(),
                                                    buddyTab.getLoginUser().getIMServerType());
                    }
                    if(tab instanceof GroupButtonTabComponent){
                        GroupButtonTabComponent groupTab=(GroupButtonTabComponent)tab;
                        prop.storeGroupMessageTabStrs(getKernel().getPointBoxLoginUser().getIMUniqueName(),groupTab.getLoginUser(), groupTab.getGroup().getGroupName());
                    }
                }
            }
        }catch (Exception ex){
            //PointBoxTracer.recordSevereException(logger, ex);
        }
    }
        
    private void storeFloatingBuddyFramesDuringUnloading(){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            if (getKernel().getPointBoxLoginUser() != null){
//                prop.cleanupFloatingBuddyMessageFrames(getKernel().getPointBoxLoginUser().getIMUniqueName());
                getPointBoxTalker().getMessagingPaneManager().storeOpenedFloatingFrames();
            }
        }catch (Exception ex){
            //PointBoxTracer.recordSevereException(logger, ex);
        }
    }
    
    private void storeSpliterLocationDuringUnloading(){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            if (getKernel().getPointBoxLoginUser() != null){
                getPointBoxFrame().storeDockingSpliterLocationDuringUnloading();
            }
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }        
    }    
    
    @Override
    public void unload() {
        releaseMonitor.shutdown();
        
//        if (PreferencePanel.getSingleton() != null){
//            PreferencePanel.getSingleton().unloadPreferencePanel();
//        }
        
        storeMainFrameStateDuringUnloading();
//        storeBuddyListPanelsDuringUnloading();
        storeMessagingTabsDuringUnloading();
        storeFloatingBuddyFramesDuringUnloading();
        storeSpliterLocationDuringUnloading();
        
        getKernel().updateSplashScreen("Unload " + getKernel().getSoftwareName() + "'s GUI components ...", Level.INFO, 100);
        
        Set<PbcFaceComponentType> keys = faceComponents.keySet();
        Iterator<PbcFaceComponentType> itr = keys.iterator();
        IPbcFaceComponent faceComponent;
        while(itr.hasNext()){
            faceComponent = faceComponents.get(itr.next());
            faceComponent.hideFaceComponent();
            faceComponent.releaseFaceComponent();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
//    private void persistMainFloatingFrameLoacationAndSize(JFrame frame,PointBoxConsoleProperties prop){
//        Dimension size=frame.getSize();
//        Point location=frame.getLocation();
//        prop.storeFrameLocationAndSize(location, size, FrameType.MAIN_FRAME_TYPE);
//    }
//    
//    private void persistBuddyFloatingFrameLoacationAndSize(JFrame frame,PointBoxConsoleProperties prop){
//        Dimension size=frame.getSize();
//        Point location=frame.getLocation();
//        prop.storeFrameLocationAndSize(location, size, FrameType.BUDDY_FRAME_TYPE);    
//    }
//    
//    private void persistMessageFloatingFrameLoacationAndSize(JFrame frame,PointBoxConsoleProperties prop){
//       Dimension size=frame.getSize();
//       Point location=frame.getLocation();
//       prop.storeFrameLocationAndSize(location, size, FrameType.MESSAGE_FRAME_TYPE);  
//    }

    @Override
    public void suspendPricer() {
        getKernel().suspendPricer();
    }

    @Override
    public void resumePricer() {
        getKernel().resumePricer();
    }
    
    @Override
    public PbcReleaseInformation checkPbcRelease() {
        return getKernel().checkPbcRelease();
    }

    @Override
    public void pushFloatingMessagingFrameToFront() {
        getPointBoxFrame().pushFloatingMessagingFrameToFront();
    }

    @Override
    public void notifyReleaseUpdateRequired(final PbcReleaseInformation releaseInfo) {
        getPointBoxFrame().handleReleaseUpdateRequired(releaseInfo);
    }

    @Override
    public void displayPbcReleaseUpdateDialog(final PbcReleaseInformation releaseInfo) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                PbcReleaseUpdateDialog dialog = PbcReleaseUpdateDialog.getSingelton(PbcFace.this);
                dialog.display(releaseInfo, PbcReleaseUpdateDialog.Purpose.Update);
            }
        });
    }

    @Override
    public void displayPbcReleaseRollbackDialog(final PbcReleaseInformation releaseInfo) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                PbcReleaseUpdateDialog dialog = PbcReleaseUpdateDialog.getSingelton(PbcFace.this);
                dialog.display(releaseInfo, PbcReleaseUpdateDialog.Purpose.Rollback);
            }
        });
    }

    @Override
    public IPbcSplashScreen getFarewellSplashWindow() {
        return (IPbcSplashScreen)faceComponents.get(PbcFaceComponentType.FarewellSplashWindow);
    }

    @Override
    public IGatewayConnectorBuddy getPointBoxLoginUser() {
        return getLoginWindow(GatewayServerType.PBIM_SERVER_TYPE).getCurrentLoginUser();
    }

    @Override
    public void displayConnectorLoginDialog(GatewayServerType gatewayServerType) {
        getLoginWindow(gatewayServerType).displayFaceComponent();
    }

    @Override
    public void reorganizeMessagingBoardTabButtons() {
        getPointBoxTalker().reorganizeMessagingBoardTabButtons();
    }

    @Override
    public IPbcTalker getPointBoxTalker(){
        return (IPbcTalker)faceComponents.get(PbcFaceComponentType.PointBoxTalker);
    }

    @Override
    public void gatewayConnectorBuddyHighlighted(IGatewayConnectorBuddy highlightenUser,boolean isHightlightOnMsgBoard) {
        getPointBoxTalker().gatewayConnectorBuddyHighlighted(highlightenUser,isHightlightOnMsgBoard);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getSortedBuddyList(GatewayServerType gatewayServerType) {
        return getPointBoxTalker().getSortedBuddyList(gatewayServerType);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getSortedBuddyList(GatewayServerType gatewayServerType, boolean onlineBuddyRequired) {
        return getPointBoxTalker().getSortedBuddyList(gatewayServerType, onlineBuddyRequired);
    }
    
    @Override
    public  ArrayList<IGatewayConnectorGroup> getAllGroups(){
         ArrayList<IBuddyListPanel> panels = getBuddyListTreePanels(GatewayServerType.AIM_SERVER_TYPE);
         panels.addAll(getBuddyListTreePanels(GatewayServerType.YIM_SERVER_TYPE));
         panels.addAll(getBuddyListTreePanels(GatewayServerType.PBIM_SERVER_TYPE));
         ArrayList<IGatewayConnectorGroup> groups = new ArrayList<IGatewayConnectorGroup>();
          for (IBuddyListPanel panel : panels){
            groups.addAll(panel.getAllGroups(false, false));
        }
         return groups;
    }
    
    
    @Override
    public ArrayList<IBuddyListPanel> getBuddyListTreePanels(GatewayServerType gatewayServerType) {
        return getPointBoxTalker().getBuddyListTreePanels(gatewayServerType);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getAllLoginUsers() {
        ArrayList<IBuddyListPanel> panels = getBuddyListTreePanels(GatewayServerType.AIM_SERVER_TYPE);
        panels.addAll(getBuddyListTreePanels(GatewayServerType.YIM_SERVER_TYPE));
        panels.addAll(getBuddyListTreePanels(GatewayServerType.PBIM_SERVER_TYPE));
        ArrayList<IGatewayConnectorBuddy> loginUsers = new ArrayList<IGatewayConnectorBuddy>();
        for (IBuddyListPanel panel : panels){
            loginUsers.add(panel.getMasterLoginUser());
        }
        return loginUsers;
    }

    @Override
    public JPanel getBuddyListBasePanel() {
        return getPointBoxTalker().getBuddyListBasePanel();
    }

    @Override
    public JPanel getMessagingBasePanel() {
        return getPointBoxTalker().getMessagingBasePanel();
    }

    @Override
    public boolean checkDistributionListTreePanelReadinessForTemplate() {
        return getPointBoxTalker().checkDistributionListTreePanelReadinessForTemplate();
    }

    @Override
    public ArrayList<IGatewayConnectorGroup> getPitsCastGroups() {
        return getPointBoxTalker().getPitsCastGroups();
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfGroups(ArrayList<IGatewayConnectorGroup> groups) {
        return getPointBoxTalker().getBuddiesOfDistributionGroups(groups);
    }

    @Override
    public void activateGroupListTreeTab() {
        getPointBoxTalker().activateGroupListTreeTab();
    }

    @Override
    public IPbcViewer getPointBoxViewer(){
        return (IPbcViewer)faceComponents.get(PbcFaceComponentType.PointBoxViewer);
    }

    @Override
    public IViewerTablePanel getCurrentViewerTablePanel() {
        return getPointBoxViewer().getCurrentViewerTablePanel();
    }

    @Override
    public void refreshViewerTableRow(int tableModelRowIndex) {
        getPointBoxViewer().refreshTableRow(tableModelRowIndex);
    }

    @Override
    public void clearViewer() {
        getPointBoxViewer().clearViewer();
    }

    @Override
    public void keepTodayData() {
        getPointBoxViewer().keepTodayData();
    }
    
    @Override
    public void activateViewerTab(String tabID) {
        getPointBoxViewer().activateViewerTab(tabID);
    }

    @Override
    public void displayFilterViewer(boolean model, boolean newFilter) {
        ViewerSearchFactory.getViewerFilterDialogInstance(getPointBoxViewer(), true, newFilter).displayDialog();//no singletan here
    }

    @Override
    public void displaySearchViewer(boolean model) {
        ViewerSearchFactory.getViewerSearchDialogSingleton(getPointBoxViewer(), true).displayDialog();
    }

    @Override
    public JPanel getViewerBasePanel() {
        return getPointBoxViewer().getViewerBasePanel();
    }

    @Override
    public IServerLoginWindow getLoginWindow(GatewayServerType gatewayServerType) {
        return this.getPointBoxTalker().getLoginWindow(gatewayServerType);
    }
    /**
     * initialize GUI components which are used in PbcFace's constructor
     */
    private void initializeFaceComponents() throws PointBoxFatalException {
        
        try {
            //load the entire GUI layer and wait until all the GUI components ready
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    getKernel().updateSplashScreen("Initialize default look-and-feel...", Level.INFO, 100);
                    SwingGlobal.switchSubstanceLookAndFeel(LookAndFeelType.Nebula);
                    UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CLOSE_BUTTONS_PROPERTY, Boolean.TRUE);
                    UIManager.put(SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.TRUE);
                    UIManager.put("TabbedPane.selected", ColorName.getColor(ColorName.VERY_LIGHT_YELLOW));

                    getKernel().updateSplashScreen("Initialize PointBox main frame...", Level.INFO, 100);
                    faceComponents.put(PbcFaceComponentType.PointBoxFrame,
                                       PointBoxFrame.getPointBoxFrameSingleton(PbcFace.this));
                    
                    getKernel().updateSplashScreen("Initialize PointBox login windows...", Level.INFO, 100);
                    faceComponents.put(PbcFaceComponentType.FarewellSplashWindow,
                                       FarewellSplashWindow.getInstance(PbcFace.this));
                    
                    getKernel().updateSplashScreen("Initialize PointBox talker...", Level.INFO, 100);
                    faceComponents.put(PbcFaceComponentType.PointBoxTalker,
                                       PointBoxTalker.getPointBoxTalkerSingleton(PbcFace.this));
                    getKernel().updateSplashScreen("Initialize PointBox talker and aggregator...", Level.INFO, 100);
                    faceComponents.put(PbcFaceComponentType.PointBoxViewer,
                                       ViewerFactory.getPointBoxViewerSingleton(PbcFace.this));
                }
            });
        } catch (InterruptedException ex) {
            throw new PointBoxFatalException(ex.getMessage());
        } catch (InvocationTargetException ex) {
            throw new PointBoxFatalException(ex.getMessage());
        }
    }
    
    @Override
    public void displayTesterDialog() {
        (PointBoxTestDialog.getSingletonInstance(getPointBoxTalker())).display();
    }


    @Override
    public void updatePointBoxFrameStatusBar(Integer value, String message) {
        getPointBoxFrame().updateStatusBar(value, message);
    }
    
    PointBoxFrame getPointBoxFrame(){
        return (PointBoxFrame)faceComponents.get(PbcFaceComponentType.PointBoxFrame);
    }

    @Override
    public void updateSystemFrameLayoutAndStyle() {
        getPointBoxFrame().updateLayoutAndStyle();
    }

    @Override
    public void updateSystemFrameSize(Dimension newSize) {
        getPointBoxFrame().updateSystemFrameSize(newSize);
    }
    
    @Override
     public void updateSoundSettings() {
        getPointBoxFrame().updateSoundSettings();
    }

//    @Override
//    public JDialog getPointBoxPreferenceDialog() {
//        return getPointBoxFrame().getPreferenceDialog();
//    }

    @Override
    public JFrame getPointBoxMainFrame() {
        return (JFrame)faceComponents.get(PbcFaceComponentType.PointBoxFrame);
    }

    @Override
    public void uploadAllEmsCurves() {
        ((PointBoxFrame)faceComponents.get(PbcFaceComponentType.PointBoxFrame)).uploadAllEmsCurves();
    }

    @Override
    public void uploadLegacyEmsCurves() {
        ((PointBoxFrame)faceComponents.get(PbcFaceComponentType.PointBoxFrame)).uploadLegacyEmsCurves();
    }

    @Override
    public void loadDone(){
        getPointBoxFrame().getPbcReloadingDialog().loadDone();
    }
    
    @Override
    public void publishStatusMessage(String statusMessage) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void displayPointBoxFrame() {
        faceComponents.get(PbcFaceComponentType.PointBoxFrame).displayFaceComponent();
    }

    @Override
    public void handleComponentEvent(PointBoxConsoleEvent event) {
        if (event == null){
            return;
        }
        /**
         * Play sound for messaging...
         */
        if (event instanceof MessageRecievedEvent){
            playInstantMessageSound("jReceivedIMSound");
        } else if (event instanceof MessageSentEvent){     //Handle the event for sound according to different events.
            playInstantMessageSound("jSentIMSound");
        }
        /**
         * Broadcast event to other components
         */
        getLoginWindow(GatewayServerType.AIM_SERVER_TYPE).handlePointBoxEvent(event);
        getLoginWindow(GatewayServerType.PBIM_SERVER_TYPE).handlePointBoxEvent(event);
        getLoginWindow(GatewayServerType.YIM_SERVER_TYPE).handlePointBoxEvent(event);
        getPointBoxTalker().handlePointBoxEvent(event);
        getPointBoxViewer().handlePointBoxEvent(event);
        getPointBoxFrame().handlePointBoxEvent(event);
        if (event instanceof PointBoxConsoleSettingsInitializedEvent){
            PointBoxConsoleSettingsInitializedEvent pbcsie = (PointBoxConsoleSettingsInitializedEvent)event;
            
            if ((pbcsie != null) && (pbcsie.getPointBoxConsoleSettings() != null)){
                getKernel().raisePointBoxEvent(new SystemLoginRequestInitiatedEvent(pbcsie.getPointBoxConsoleSettings().getPointBoxAccountID(),
                                                                                   "PoitnBoxConsole is ready for web data polling.",
                                                                                   PointBoxEventTarget.PbcWeb,
                                                                                   GatewayServerType.PBIM_SERVER_TYPE));
            }
        }
    }

    private HashMap getSoundSettings(){
        return getKernel().getPointBoxConsoleRuntime().getPbcAudioSettings().getSoundSetting();
    }
    private EnumMap<PbcAudioFileName, URL> getSoundList(){
        return getKernel().getPointBoxConsoleRuntime().getPbcAudioSettings().getSoundList();
    }
    
    /**
     * This method is fast since it uses of soundPlayer submit service
     * @param soundType 
     */
    private void playInstantMessageSound(final String soundType){
        soundPlayer.submit(new Runnable(){
            @Override
            public void run() {
                final HashMap soundSettings = getSoundSettings();
                final EnumMap<PbcAudioFileName, URL> soundList = getSoundList();
                synchronized (soundSettings){
                    if (soundSettings == null){
                        return;
                    }
                    if (DataGlobal.isEmptyNullString((String)soundSettings.get("enableSound"))){
                        return;
                    }
                    if(((String)soundSettings.get("enableSound")).equals("true") && soundSettings.containsKey(soundType) 
                            && !((String)soundSettings.get(soundType)).equals("No Sounds"))
                    {
                        try {
                            // Load resources from jar file.
                           URL soundURL;
                            String soundSettingOpt = (String)soundSettings.get(soundType);
                            if(soundList.containsKey(PbcAudioFileName.converToType(soundSettingOpt))){
                                soundURL = soundList.get(PbcAudioFileName.converToType(soundSettingOpt));
                            }else{
                                soundURL = new URL(soundSettingOpt);
                            }
                            if (soundURL == null){
                                return;
                            }
                            Line.Info linfo = new Line.Info (Clip.class);
                            Line line = AudioSystem.getLine (linfo);
                            Clip buhClip = (Clip) line;
                            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL);
                            buhClip.open(ais);
                            buhClip.start();
                         }
                        catch ( Exception e ) {
                            //logger.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                }
            }
        });
    }
    
    @Override
    public  void displayClearPortLoginFrame(final IPbcFace face,final IPbconsoleImageSettings setting ){
        if(SwingUtilities.isEventDispatchThread()){
             new ClearPortLoginFrame(face,setting).setVisible(true);
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ClearPortLoginFrame(face,setting).setVisible(true);
                }
            });
        }
    }    
    
    
    //display clearPortMainFrame. If it's already created, just show it.
    //if not, show the login frame.
    @Override
    public boolean displayClearPortMainFrame(){
        if(clearPortMainFrame==null || clearPortMainFrame.isDisposed()){
            return false;
        }else{
            clearPortMainFrame.displayFrame();
            return true;
        }
    }
    
    @Override
    public void displayClearPortMainFrameWithQuote(IPbsysOptionQuoteWrapper targetQuoteWrapper){
        clearPortMainFrame.displayFrameWithQuote(targetQuoteWrapper);
    }

    /**
     * @return the clearPortMainFrame
     */
    @Override
    public IClearPortMainFrame getClearPortMainFrame() {
        return clearPortMainFrame;
    }

    /**
     * @param clearPortMainFrame the clearPortMainFrame to set
     */
    @Override
    public void setClearPortMainFrame(IClearPortMainFrame clearPortMainFrame) {
        this.clearPortMainFrame = clearPortMainFrame;
    }

//    @Override
//    public void displayStripPricerWithQuote(IPbsysOptionQuoteWrapper targetQuoteWrapper) {
//        getPointBoxFrame().displayStripPricerWithQuote(targetQuoteWrapper);
//    }

    @Override
    public IPbcStructuredQuoteBuilder getPbcStructuredQuoteBuilder(PointBoxQuoteType category) {
        return getPointBoxFrame().getPbcStructuredQuoteBuilder(category);
    }

    @Override
    public void displaySimPricerWithQuote(IPbsysOptionQuoteWrapper targetQuoteWrapper, PointBoxQuoteType category) {
        getPointBoxFrame().displaySimPricerWithQuote(targetQuoteWrapper, category);
    }
    
    @Override
    public void displayOptionPricerFrame(){
        getPointBoxFrame().displayOptionPricerFrame();
    }
    
    @Override
    public void clearViewerOptions(){
        getPointBoxFrame().clearViewerTabs();
    }
}//PbcFace

