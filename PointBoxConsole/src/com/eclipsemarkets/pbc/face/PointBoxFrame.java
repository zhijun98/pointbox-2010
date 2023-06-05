/**
 * Eclipse Market Solutions LLC
 *
 * PointBoxFrame.java
 *
 * @author Zhijun Zhang
 * Created on May 18, 2010, 9:44:16 AM
 */

package com.eclipsemarkets.pbc.face;

import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.ServerNotSupportedEvent;
import com.eclipsemarkets.event.gateway.GatewayConnectionEvent;
import com.eclipsemarkets.event.gateway.MessageRecievedEvent;
import com.eclipsemarkets.event.gateway.MessageSentEvent;
import com.eclipsemarkets.event.pricer.PBPricerChangedEvent;
import com.eclipsemarkets.event.pricer.WhenTechPricerUnavaliableEvent;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IPointBoxDistributionGroup;
import com.eclipsemarkets.gateway.web.ConnectionEventHappened;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.*;
import com.eclipsemarkets.pbc.face.preference.BlockedBuddiesSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.CloudingPublishSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.CurveSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.GeneralSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.MessagingServerSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.MsAccessSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.PreferenceDialog;
import com.eclipsemarkets.pbc.face.preference.PreferencePanelType;
import com.eclipsemarkets.pbc.face.preference.PricerSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.ViewerColumnSettingsDialog;
import com.eclipsemarkets.pbc.face.preference.ViewerFontColorSettingsDialog;
import com.eclipsemarkets.pbc.pricer.sim.IPbcStructuredQuoteBuilder;
import com.eclipsemarkets.pbc.pricer.sim.PbcQuoteFrame;
import com.eclipsemarkets.pbc.face.talker.*;
import com.eclipsemarkets.pbc.face.talker.messaging.MessagingPaneManager;
import com.eclipsemarkets.pbc.face.viewer.IViewerTablePanel;
import com.eclipsemarkets.pbc.face.viewer.ViewerTableType;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.pricer.PbcPricingAgent;
import com.eclipsemarkets.pbc.pricer.sim.PricingCompFrame;
import com.eclipsemarkets.pbc.pricer.sim.PricingForwardCurveFrame;
import com.eclipsemarkets.pbc.pricer.sim.PricingInterestRateFrame;
import com.eclipsemarkets.pbc.pricer.sim.PricingVolSkewSurfaceFrame;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.pbc.weather.EmsWeatherReportFactory;
import com.eclipsemarkets.pbc.weather.IEmsWeatherReport;
import com.eclipsemarkets.pbc.web.PbcReleaseInformation;
import com.eclipsemarkets.pbc.web.PbcReleaseStatus;
import com.eclipsemarkets.pricer.PbcPricerType;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import com.eclipsemarkets.web.pbc.PbcSystemFrameLayout;
import com.eclipsemarkets.web.pbc.PbcSystemFrameStyle;
import com.eclipsemarkets.web.pbc.PricingCurveFileSettings;
import com.l2fprod.common.swing.JFontChooser;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import java.util.List;

/**
 * During system working for local users, this frame is also used as IPbcSplashScreen
 * @author Zhijun Zhang
 */
class PointBoxFrame extends javax.swing.JFrame implements IPbcFaceComponent, IPbcSplashScreen
{
    private static final long serialVersionUID = 1L;
    //private static final String pricingAtServerText;
    //private static final String pricingAtClientText;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PointBoxFrame.class.getName());
        //pricingAtServerText = " (Pricing@Server)";
        //pricingAtClientText = " (PBC Pricing)";
    }

    /**
     * Owner of this control
     */
    private final IPbcFace face;
    
    /**
     * Vertical layout panel of this frame
     */
    private VerticalBasePanel vPanel;

    /**
     * Horizontal layout panel of this frame
     */
    private HorizontalBasePanel hPanel;

    /**
     * Menu of this frame
     */
//    private JMenu mermMenu;

    /**
     * Export all the quote-message from the local derby database into a physical text file
     */
    private PbcArchiveDialog pbcArchiveDialog;
    
    private PbcReloadingDialog pbcReloadingDialog;
    
    private final EnumMap<PointBoxQuoteType, IPbcStructuredQuoteBuilder> simFrameMap = new EnumMap<PointBoxQuoteType, IPbcStructuredQuoteBuilder>(PointBoxQuoteType.class);

    /**
     * Clock at the bottom of this frame
     */
    private Timer clockTimer;

    private final ComponentListener mainFrameComponentListener;
    private final ComponentListener messagingFloatingFrameListener;
    private final ComponentListener buddyListFloatingFrameListener;

    private final WindowListener frameWindowListener;
    
    private JFrame messagingFloatingFrame;

    private JFrame buddyListFloatingFrame;

    private AboutUsDialog aboutDialog = null;

    private PreferenceDialog preferenceDialog;

    private boolean isPersonalized;
    
    /**
     * When the PointBox main frame is minimized, if new messages come in, its icon will flash.
     * This data field should be guarded by synchronization
     */
    private FrameIconFlashingAgent frameIconFlashingAgent;
    
    private final Object frameIconFlashingLocker;
    
    /**
     * Creates new form PointBoxFrame
     * @param face
     */
    private PointBoxFrame(final IPbcFace face) {
        initComponents();
        
        //Essentials
        this.face = face;
        
        isPersonalized = false;
        
        //hide this button temporarily since the server-side pricer is not really ready
        jPricerSetting.setVisible(false);
        
        //jClearBtn.setVisible(false);
        //jClearAllSheets.setVisible(false);
        
        //todo: zzj - Search menu button is disabled from the tool bar and menu, which should be recovered
        jSearch.setVisible(false);
        jSearchBtn.setVisible(false);
        
        preferenceDialog = null;
        
        pbcArchiveDialog = null;
        
        frameIconFlashingAgent = null;
        frameIconFlashingLocker = new Object();
        
        clockTimer = null;
        
        //*************new improvements on GUI, 05/02/2013*******************
        jConnectToAim.setVisible(false);
        jDisconnectFromAIM.setVisible(false);
        jSeparator2.setVisible(false);
        jConnectToYahoo.setVisible(false);
        jDisconnectFromYahoo.setVisible(false);  
        jSeparator3.setVisible(false);
        
        jAllMessagesTab.setVisible(false);
        jAllQuotesTab.setVisible(false);
        jSeparator4.setVisible(false);
        jOutgoingTab.setVisible(false);
        jIncomingTab.setVisible(false);
        jSeparator5.setVisible(false);
        jSeparator16.setVisible(false);
        
        jPricing.setVisible(false);
        
        //jSaveAllBtn.setVisible(false);
        jClearBtn.setVisible(false);
        jFontBtn.setVisible(false);
        jShutdownBtn.setVisible(false);
        
        
        jPBPricer.setVisible(false);
        
        jPointBoxHelp.setVisible(false);
        
        messagingFloatingFrame = new JFrame("PointBox Messenger");
        buddyListFloatingFrame = new JFrame("PointBox Buddy List");
        aboutDialog = new AboutUsDialog(this, true);
        
        mainFrameComponentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (isFloatingStyle()){
                    getPbcRuntime().setPbcWindowsSize(PointBoxFrame.this.getSize(), PbcFaceComponentType.FloatingPointBoxFrame);
                }else{
                    getPbcRuntime().setPbcWindowsSize(PointBoxFrame.this.getSize(), PbcFaceComponentType.PointBoxFrame);
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (isFloatingStyle()){
                    getPbcRuntime().setPbcWindowsLocation(PointBoxFrame.this.getLocation(), PbcFaceComponentType.FloatingPointBoxFrame);
                }else{
                    getPbcRuntime().setPbcWindowsLocation(PointBoxFrame.this.getLocation(), PbcFaceComponentType.PointBoxFrame);
                }
            }
        };
        
        messagingFloatingFrameListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (messagingFloatingFrame != null){
                    getPbcRuntime().setPbcWindowsSize(messagingFloatingFrame.getSize(), PbcFaceComponentType.FloatingMessagingFrame);
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (messagingFloatingFrame != null){
                    getPbcRuntime().setPbcWindowsLocation(messagingFloatingFrame.getLocation(), PbcFaceComponentType.FloatingMessagingFrame);
                }
            }
        };
        
        buddyListFloatingFrameListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (buddyListFloatingFrame != null){
                    getPbcRuntime().setPbcWindowsSize(buddyListFloatingFrame.getSize(), PbcFaceComponentType.FloatingBuddyListFrame);
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (buddyListFloatingFrame != null){
                    getPbcRuntime().setPbcWindowsLocation(buddyListFloatingFrame.getLocation(), PbcFaceComponentType.FloatingBuddyListFrame);
                }
            }
        };
    
        frameWindowListener = new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                face.getKernel().shutdown(face.getPointBoxLoginUser(), true);
            }
        };
      
        //jWTConnect.setVisible(false);
        presentWenTechConnectButton();
        
        jWeatherBtn.setVisible(false);
        jWTConnect.setVisible(false);
        jColorBtn.setVisible(false);
        
        if(!this.getPbcRuntime().getKernel().getPbcReleaseUserType().equals(PbcReleaseUserType.DEBUG_USERS)){
            jTest.setVisible(false);
            //jRollback.setVisible(false);
        }
        //jTest.setVisible(true);
        
        jMenuItem2.setVisible(false);
        
        jOnlineUpdate.setVisible(false);
        
        jUploadBtn.setVisible(false);
        jLegacyUploadBtn.setVisible(false);
    }

    /**
     * image settings of point box
     */
    private IPbconsoleImageSettings getImageSettings(){
        return face.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
    }

    private static PointBoxFrame self;
    static{
        self = null;
    }
    
//    /**
//     * @deprecated 
//     * @return 
//     */
//    private PreferenceDialog getPreferenceDialog() {
//        if (preferenceDialog == null){
//            preferenceDialog = new PreferenceDialog(face);
//        }
//        
//        return preferenceDialog;
//    }

    public static PointBoxFrame getPointBoxFrameSingleton(final IPbcFace face){
        if (self == null){
            self = new PointBoxFrame(face);
        }
        return self;
    }

    @Override
    public void close() {
        hideFaceComponent();
    }

    @Override
    public void display() {
        displayFaceComponent();
    }

    @Override
    public void updateSplashScreen(String msg, Level level, long latency) {
        setStatusMessage(msg);
    }

    @Override
    public void displayFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            displayFaceComponentHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayFaceComponentHelper();
                }
            });
        }
//        /**
//         * Check release for online update
//         */
//        PbcReleaseInformation releaseInfo = face.checkPbcRelease();
//        if (!releaseInfo.getPbcReleaseStatus().equals(PbcReleaseStatus.Latest_Release)){
//            PbcReleaseUpdateDialog.getSingelton(face).displaySimGUI(releaseInfo, PbcReleaseUpdateDialog.Purpose.Update);
//        }
    }
    
    /**
     * Get the panel list name and find out the corresponding settings for customization. 
     * This method will skip "constant" buddy lists: Conference, Distribution, PBIM, and PITS
     */
    private void displayPersistentBuddyListPanel(){
        try{
            /**
             * Get buddy-list name from local properties
             */
            IPbcRuntime runtime = face.getKernel().getPointBoxConsoleRuntime();
            if (runtime != null){
                ArrayList<PbcBuddyListSettings> aBuddyListSettingsList = runtime.getPbcBuddyListSettings();
                if (aBuddyListSettingsList != null){
                    IGatewayConnectorBuddy loginUser = null;
                    for (PbcBuddyListSettings aPbcBuddyListSettings : aBuddyListSettingsList){
                        /**
                         * if this local-persistent buddy list was also persistent in the server, it could be opened.
                         */
                        if (aPbcBuddyListSettings.getConnectorOwner() == null){
                            String distListName = aPbcBuddyListSettings.getBuddyListName();
                            if (DataGlobal.isNonEmptyNullString(distListName)){
                                /**
                                 * Regular buddy list name is loginUser's unique name
                                 */
                                if (PbcBuddyListType.RegularBuddyList.toString().equalsIgnoreCase(aPbcBuddyListSettings.getBuddyListType())){
                                    loginUser = GatewayBuddyListFactory.convertToLoginUserInstance(distListName);
                                }
                            }
                        }else{
                            loginUser = GatewayBuddyListFactory.getLoginUserInstance(aPbcBuddyListSettings.getConnectorOwner().getLoginName(), 
                                                                        GatewayServerType.convertToType(aPbcBuddyListSettings.getConnectorOwner().getGatewayServerType()));
                        }
                        //display a persistent buddy list panel if its corresponding settings existed
                        if (loginUser != null){
                            boolean openPanel = true;
                            if (GatewayServerType.AIM_SERVER_TYPE.equals(loginUser.getIMServerType())){
                                openPanel = !(face.getKernel().isPbcFeatureDisabled(PbcFeature.AIM));
                            }else if (GatewayServerType.YIM_SERVER_TYPE.equals(loginUser.getIMServerType())){
                                openPanel = !(face.getKernel().isPbcFeatureDisabled(PbcFeature.YahooIM));
                            }
                            if (openPanel){
                                face.getPointBoxTalker().openBuddyListPanelForLoginUser(loginUser);
                            }
                        }
                    }//for
                }//if
            }
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }
    }
    
    private void displayPersistentBuddyMessagingTabs(){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            ArrayList<IBuddyListPanel> panels=face.getPointBoxTalker().getAllBuddyListTreePanels();
            Set<String> buddyMessageTabStrs=prop.retrieveBuddyMessageTabStrs(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            for(String str:buddyMessageTabStrs){
                if(str.contains(",")&&str.contains("=")){
                    String[] strArr=str.split(",");
                    String[] buddyArr=strArr[0].split("=");
                    String[] ownerArr=strArr[1].split("=");
                    String buddyName=buddyArr[0];
                    String ownerName=ownerArr[0];
                    String ownerServerType=ownerArr[1];

                    for(IBuddyListPanel panel:panels){
                    if(panel.getMasterLoginUser().getIMScreenName().equals(ownerName)&&panel.getMasterLoginUser().getIMServerType().toString().equals(ownerServerType)){
                        IGatewayConnectorBuddy owner = GatewayBuddyListFactory.getLoginUserInstance(ownerName, 
                                                                        GatewayServerType.convertToType(ownerServerType));
                        IGatewayConnectorBuddy buddy = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(owner, buddyName, face.getKernel().getPointBoxConsoleRuntime());
                        buddy.setLoginOwner(owner);
                        panel.mimicfireBuddyClickedEvent(buddy);
                    }
                    }
                }
            }
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }
    }
    
    private void displayPersistentGroupMessagingTabs(){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            Set<String> groupMessageTabStrs=prop.retrieveGroupMessageTabStrs(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            for(String str:groupMessageTabStrs){
                if(str.contains(",")&&str.contains("=")){
                    String[] strArr=str.split(",");
                    String[] loginUserArr=strArr[0].split("=");
                    String groupname=strArr[1];
                    String loginUserName=loginUserArr[0];
                    String loginUserServerType=loginUserArr[1];

                    IGatewayConnectorBuddy loginUser = GatewayBuddyListFactory.getLoginUserInstance(loginUserName, 
                                                                            GatewayServerType.convertToType(loginUserServerType));
                    IPointBoxDistributionGroup group=GatewayBuddyListFactory.getDistributionGroupInstance(loginUser, groupname);
                    DistributionBuddyListPanel panel=(DistributionBuddyListPanel)face.getPointBoxTalker().getMasterBuddyListTreePanel();
                    ArrayList<IGatewayConnectorBuddy> members=panel.getDndBuddyTree().retrieveBuddiesOfGroup(group);
                    panel.mimicfireGroupClickedEvent(group, members);
                }
            }
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }
    }
    
    /**
     * displaySimGUI a collection of persistent targets here
     */
    private void displayFaceComponentHelper(){
        updateLayoutAndStyle();
        
        PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
        /**
         * Display persistent buddy list panels
         */
        displayPersistentBuddyListPanel();
        
        if(prop.retrieveTabPersistedOption(face.getKernel().getPointBoxLoginUser().getIMUniqueName())){
            /**
             * Display persistent buddy and group message tabs
             */
            displayPersistentBuddyMessagingTabs();
            displayPersistentGroupMessagingTabs();
        }

        if (pbcArchiveDialog == null){
            pbcArchiveDialog = new PbcArchiveDialog(face);
        }
        
        if (!pbcArchiveDialog.isReady()){
            pbcArchiveDialog.setVisible(true);
        }
        
        if(pbcReloadingDialog==null){
            pbcReloadingDialog=PbcReloadingDialog.getSingletonInstance(face.getPointBoxTalker());
        }
        
        if(prop.retrieveReloaderOpenedOption(face.getKernel().getPointBoxLoginUser().getIMUniqueName())&&(!pbcReloadingDialog.isEmptyForLoginUser())){
            pbcReloadingDialog.setVisible(true);
          
//            String settings=prop.retrieveReloaderDefaultSettings(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
//            if(settings.equals("Load all messages")){
//                   pbcReloadingDialog.selectAll();
//                   pbcReloadingDialog.loaderInEDT();
//            }else if(settings.equals("Load last hour")){
//                   pbcReloadingDialog.selectDefault();
//                   pbcReloadingDialog.loaderInEDT();
//            }else{
//                  //do nothing.....
//            }
        }
        face.getPointBoxTalker().setCurrentPanelShowOfflineOption();
        
        /**
         * Bad temp solution: Wait for tree model ready for the following displaySimGUI
         */
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MessagingPaneManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        /**
         * Display persistent floating frames for PITS/DIST/PitsCast
         */
        face.getPointBoxTalker().getMessagingPaneManager().displayPersistentFloatingFrames();
        //displayPersistentFloatingPanel();
        
        //update for producing the floating frame effect if it is float style.
        setVisible(true);
    }

    @Override
    public void hideFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            setVisible(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setVisible(false);
                }
            });
        }
    }

    /**
     * This method is only called once. Otherwise nothing happened
     */
    @Override
    public void personalizeFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            personalizeFaceComponentHelper();
        }else{
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        personalizeFaceComponentHelper();
                    }
                });
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void personalizeFaceComponentHelper(){
        if (!isPersonalized){
            jRollback.setVisible(true);
            if (PbcReleaseUpdateDialog.getSingelton(face).isRollbackValid()){
                jRollback.setEnabled(true);
            }else{
                jRollback.setEnabled(false);
            }
            /**
             * Avoid personalizing redundantly in one open session
             */
            isPersonalized = true;
            customizeMenuBar();
            customizeToolBar();

            if ((vPanel == null) && (hPanel == null)){
                vPanel = VerticalBasePanel.createVerticalBasePanel(face);
                this.jContentPanel.add(vPanel, BorderLayout.CENTER);
                hPanel = null;
            }

            if (clockTimer == null){
                clockTimer = new Timer();
                clockTimer.schedule(new ClockMonitor(), 10, 45000);
            }

            updateLayoutAndStyle();

            finalizeFrameInitialization();

            setStatusPanel();

            removeComponentListener(mainFrameComponentListener);
            addComponentListener(mainFrameComponentListener);
            
            IPbcKernel kernel = face.getKernel();
            
            /**
             * Pre-load SIM frames for different categories
             */
            simFrameMap.put(PointBoxQuoteType.OPTION, new PbcQuoteFrame(kernel, PointBoxQuoteType.OPTION));
            
            //only PB-admins are authorized to upload the curves
            if (kernel.isPbcPricingAdmin()){
                jUploadBtn.setVisible(true);
                jCloudingPublish.setVisible(true);
            }else{
                jUploadBtn.setVisible(false);
                jCloudingPublish.setVisible(false);
            }
            //hide legacy upload button
            jLegacyUploadBtn.setVisible(false);
            
            if (kernel.isPbcFeatureDisabled(PbcFeature.PBMarks)){
                jPsDn.setVisible(false);
            }else{
                jPsDn.setVisible(true);
            }
            if (kernel.isPbcFeatureDisabled(PbcFeature.ClearPort)){
                jCPConnect.setVisible(false);
            }else{
                jCPConnect.setVisible(true);
            }
            if (kernel.isPbcFeatureDisabled(PbcFeature.YahooIM)){
                jYIMBtn.setVisible(false);
                jConnectToYahoo.setEnabled(false);
                jYahooSettings.setEnabled(false);
                jYahooSettings.setVisible(false);
            }else{
                jYIMBtn.setVisible(true);
                jConnectToYahoo.setEnabled(true);
                jYahooSettings.setEnabled(true);
                jYahooSettings.setVisible(true);
            }
            if (kernel.isPbcFeatureDisabled(PbcFeature.AIM)){
                jAIMBtn.setVisible(false);
                jConnectToAim.setEnabled(false);
                jAIMSettings.setEnabled(false);
                jAIMSettings.setVisible(false);
            }else{
                jAIMBtn.setVisible(true);
                jConnectToAim.setEnabled(true);
                jAIMSettings.setEnabled(true);
                jAIMSettings.setVisible(true);
            }
            
            /**
             * Check curves and download the missing ones
             */
            List<PbcPricingModel> aPbcPricingModelList = getPbcRuntime().initializebcPricingRuntimeCurveSettings();
            for (PbcPricingModel aPbcPricingModel : aPbcPricingModelList){
                getCurveSettingsDialog().downloadPricingRuntimeCurveFiles(aPbcPricingModel, false);
            }
        }
    }
    
    private IPbcRuntime getPbcRuntime(){
        return face.getKernel().getPointBoxConsoleRuntime();
    }

    @Override
    public void releaseFaceComponent() {
        if (flashOnlineUpdateThread != null){
            flashOnlineUpdateThread.interrupt();
        }
        if (pbcArchiveDialog != null){
            pbcArchiveDialog.interruptArchivingThreads();
        }
    }

    private boolean ishLayout(){
        return getPbcRuntime().getPbcSystemFrameLayout().equals(PbcSystemFrameLayout.Horizontal);
    }
    
    public boolean isFloatingStyle() {
        return getPbcRuntime().getPbcSystemFrameStyle().equals(PbcSystemFrameStyle.Floating);
    }
    
    void updateLayoutAndStyle(){
        if (ishLayout() && (jHorizontalLayout.isEnabled())){
            switchDockingFrameLayout(true);
        }else{
            switchDockingFrameLayout(false);
        }

        if (isFloatingStyle() && (jFloatingWindow.isEnabled())){
            switchFrameStyle(true);
        }else{
            switchFrameStyle(false);
        }
        
        if (isFloatingStyle()){
            setSize(getPbcRuntime().getPbcWindowsSize(PbcFaceComponentType.FloatingPointBoxFrame));
            Point aPoint = getPbcRuntime().getPbcWindowsLocation(PbcFaceComponentType.FloatingPointBoxFrame);
            if (aPoint == null){
                aPoint = SwingGlobal.getScreenCenterPoint(this);
                getPbcRuntime().setPbcWindowsLocation(aPoint, PbcFaceComponentType.FloatingPointBoxFrame);
            }
            setLocation(aPoint);
        }else{
            setSize(getPbcRuntime().getPbcWindowsSize(PbcFaceComponentType.PointBoxFrame));
            Point aPoint = getPbcRuntime().getPbcWindowsLocation(PbcFaceComponentType.PointBoxFrame);
            if (aPoint == null){
                aPoint = SwingGlobal.getScreenCenterPoint(this);
                getPbcRuntime().setPbcWindowsLocation(aPoint, PbcFaceComponentType.PointBoxFrame);
            }
            setLocation(aPoint);
        }

        setStatusPanel();
    }

    private void setStatusPanel(){
        if (SwingUtilities.isEventDispatchThread()){
            setStatusPanelHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setStatusPanelHelper();
                }
            });
        }
    }

    private void setStatusPanelHelper() {
        this.jStatusBtn.setText("[" + face.getPointBoxLoginUser().getIMScreenName() + "] ");
    }
    
    /**
     * Handle connection events raised from the login windows
     * @param event
     */
    private void handleGatewayConnectionEvent(GatewayConnectionEvent event) {
        if ((event == null) || (event.getLoginUser() == null)){
            return;
        }

        ConnectionEventHappened eventType = event.getConnectionEventHappened();
        String loginUserName = event.getLoginUser().getIMUniqueName();
        switch (eventType){
            case LOGOUT_SUCCEED:
                handleConnectionDisconnected(event);
                break;
            case LOGIN_SUCCEED:
                switchConnectorIcon(event.getServerType(), true);
                setStatusBar(loginUserName, " - Connected successfully.");
                break;
            case LOGIN_REFUSEDED:
                setStatusBar(loginUserName, "Login refused.");
                break;
            case LOGIN_CANCELLED:
                setStatusBar(loginUserName, "Login canceled.");
                break;
            case CONFLICT_LOGIN_REFUSED:
                setStatusBar(loginUserName, "Login conflict!.");
                break;
//            case CONNECTION_LOST:
//                //login window handle it
//                break;
        }
    }

    private void handleConnectionDisconnected(GatewayConnectionEvent event) {
        if (event.getServerType().equals(GatewayServerType.PBIM_SERVER_TYPE)){
            hideFaceComponent();
        }else{
            IGatewayConnectorBuddy loginUser=event.getLoginUser();
            (new LogoutConnectionIconFlasher(event.getServerType())).start();
            setStatusBar(loginUser.getIMUniqueName(), " - Just disconnected.");
            //We don't want this panel disappeared. So reopen buddy list panel on offline status and refresh tab icons as black
            face.getPointBoxTalker().openBuddyListPanelForLoginUser(loginUser);
            face.getPointBoxTalker().getMessagingPaneManager().makeBuddyTabIconsOffline(loginUser.getIMUniqueName(),loginUser.getIMServerType());
        }
    }

    private void switchConnectorIcon(final GatewayServerType serverType, final boolean isOnline) {
        if (SwingUtilities.isEventDispatchThread()){
            switchConnectorIconHelper(serverType, isOnline);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    switchConnectorIconHelper(serverType, isOnline);
                }
            });
        }
    }
    private void switchConnectorIconHelper(final GatewayServerType gatewayServerType, final boolean isOnline) {
        switch(gatewayServerType){
            case AIM_SERVER_TYPE:
                if (isOnline){
                    jAIMBtn.setIcon(getImageSettings().getAimOnline16Icon());
                }else{
                    jAIMBtn.setIcon(getImageSettings().getAimOffline16Icon());
                }
                break;
            case YIM_SERVER_TYPE:
                if (isOnline){
                    jYIMBtn.setIcon(getImageSettings().getYimOnline16Icon());
                }else{
                    jYIMBtn.setIcon(getImageSettings().getYimOffline16Icon());
                }
                break;
        }
    }

    private void setStatusBar(final String talkerName, final String message){
        if (SwingUtilities.isEventDispatchThread()){
            setStatusBarHelper(talkerName, message);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setStatusBarHelper(talkerName, message);
                }
            });
        }
    }

    private void setStatusBarHelper(String talkerName, String message){
        jTalker.setText(talkerName + ": ");
        setStatusMessage(message);
    }

    private void setStatusMessage(final String message){
        if (SwingUtilities.isEventDispatchThread()){
            jStatusMessage.setText(message);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jStatusMessage.setText(message);
                }
            });
        }
    }

    private void customizeMenuBar() {
        if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.PBIM_USERS)){
            jConnectToAim.setVisible(false);
            jDisconnectFromAIM.setVisible(false);
            jSeparator2.setVisible(false);
            jConnectToYahoo.setVisible(false);
            jDisconnectFromYahoo.setVisible(false);
            jSeparator3.setVisible(false);
            jAIMSettings.setVisible(false);
            jYahooSettings.setVisible(false);
        }else{
//            jConnectToAim.setVisible(true);
//            jDisconnectFromAIM.setVisible(true);
//            jSeparator2.setVisible(true);
//            jConnectToYahoo.setVisible(true);
//            jDisconnectFromYahoo.setVisible(true);
//            jSeparator3.setVisible(true);
//            jAIMSettings.setVisible(true);
//            jYahooSettings.setVisible(true);
        }
        //to-do: merm-in-house menu is here
        resetStyleMenuHelper(isFloatingStyle());
        resetLayoutMenuHelper(ishLayout());
        resetPricerLocationHelperinEDT(face.getKernel().isServerPricer());
    }

    public void updateStatusBar(final Integer value, final String msg) {
        if (SwingUtilities.isEventDispatchThread()){
            setProgressBarValueHelper(value, msg);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setProgressBarValueHelper(value, msg);
                }
            });
        }
    }
    private void setProgressBarValueHelper(final Integer value, final String msg) {
        jProgressBar.setValue(value.intValue());
        jStatusMessage.setText(msg);
    }

    public void switchFrameStyle(final boolean floating){
        if (SwingUtilities.isEventDispatchThread()){
            switchFrameStyleHelper(floating);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    switchFrameStyleHelper(floating);
                }
            });
        }
    }

    private void switchFrameStyleHelper(boolean floating) {

        //setVisible(false);

        resetStyleMenuHelper(floating);
        resetStyleToolBarHelper(floating);
        switchStyleHelper(floating);

        //pack();

        face.reorganizeMessagingBoardTabButtons();

        //setVisible(true);
    }

    private void resetStyleMenuHelper(boolean floating){
        if (floating){
            jFloatingWindow.setEnabled(false);
            jDockedWindow.setEnabled(true);
        }else{
            jFloatingWindow.setEnabled(true);
            jDockedWindow.setEnabled(false);
        }
    }

    private void resetStyleToolBarHelper(boolean floating){
        if (floating){
            jForDBtn.setIcon(getImageSettings().getDockingFramesIcon());
            jForDBtn.setText(null);

            jForDBtn.setToolTipText("Dock window");
        }else{
            jForDBtn.setIcon(getImageSettings().getFloatingFramesIcon());
            jForDBtn.setText(null);
            jForDBtn.setToolTipText("Float window");
        }
    }

    private void switchStyleHelper(boolean floating){
        jContentPanel.removeAll();
        if (floating){
            jContentPanel.removeAll();
            jContentPanel.add(face.getViewerBasePanel(), BorderLayout.CENTER);
            jContentPanel.validate();
            populateFloatingStyleSettings();
        }else{
            switchDockingFrameLayout(ishLayout());
        }
    }

    /**
     * Populate floating settings for main frame, buddy list frame, and messaging frame
     */
    private void populateFloatingStyleSettings() {
        IPbcRuntime runtime = this.getPbcRuntime();
        if (runtime == null){
            return;
        }
        //main frame's settings in the floating style
        Dimension fSize = runtime.getPbcWindowsSize(PbcFaceComponentType.FloatingPointBoxFrame);
        if ((fSize == null) || (fSize.getWidth() <= 1000) || (fSize.getHeight() <= 750)){
            fSize = new Dimension(1000, 750);
        }
        setSize(fSize);
        Point fLoaction = runtime.getPbcWindowsLocation(PbcFaceComponentType.FloatingPointBoxFrame);
        if (fLoaction  == null){
            fLoaction = new Point(0, 0);
        }
        Point fRightDownLocation=new Point((int)(fLoaction.x+fSize.getWidth()),(int)(fLoaction.y+fSize.getHeight()));
        if (SwingGlobal.isLocationInScreenBounds(fLoaction)&&SwingGlobal.isLocationInScreenBounds(fRightDownLocation)){
            setLocation(fLoaction);
        }else{
            fLoaction = SwingGlobal.getScreenCenterPoint(this);
            setLocation(fLoaction);
            runtime.setPbcWindowsLocation(fLoaction, PbcFaceComponentType.FloatingPointBoxFrame);
        }
        
        String title = "PointBox Messenger";
        Dimension mSize = runtime.getPbcWindowsSize(PbcFaceComponentType.FloatingMessagingFrame);
        if ((mSize == null) || (mSize.getWidth() <= 0) || (mSize.getHeight() <= 0)){
            mSize=new Dimension(600, 400);
        }
        if (messagingFloatingFrame == null) {
            messagingFloatingFrame = new JFrame(title);
            displayFrameHelper(messagingFloatingFrame,
                                 face.getMessagingBasePanel(),
                                 mSize);
        } else {
            displayFrameHelper(messagingFloatingFrame, null, mSize);
            messagingFloatingFrame.setTitle(title);
        }
        messagingFloatingFrame.removeComponentListener(messagingFloatingFrameListener);
        messagingFloatingFrame.addComponentListener(messagingFloatingFrameListener);
            
        Point mLoaction = runtime.getPbcWindowsLocation(PbcFaceComponentType.FloatingMessagingFrame);
        if (mLoaction == null){
            mLoaction = new Point(50, 50);
        }
        Point mRightDownLocation=new Point((int)(mLoaction.x+mSize.getWidth()),(int)(mLoaction.y+mSize.getHeight()));            
        if (SwingGlobal.isLocationInScreenBounds(mLoaction)&&SwingGlobal.isLocationInScreenBounds(mRightDownLocation)){
            messagingFloatingFrame.setLocation(mLoaction);
        }else{
            mLoaction = SwingGlobal.getScreenCenterPoint(this);
            messagingFloatingFrame.setLocation(mLoaction);
            runtime.setPbcWindowsLocation(mLoaction, PbcFaceComponentType.FloatingMessagingFrame);
        }

        //title = face.getKernel().getSoftwareName() + " - Distribution Manager";
        title = "PointBox Buddy List";
        Dimension bSize = runtime.getPbcWindowsSize(PbcFaceComponentType.FloatingBuddyListFrame);
        if ((bSize == null) || (bSize.getWidth() <= 0) || (bSize.getHeight() <= 0)){
             bSize = new Dimension(400, 600);
        }
        if (buddyListFloatingFrame == null) {
            buddyListFloatingFrame = new JFrame(title);
            displayFrameHelper(buddyListFloatingFrame,
                                 face.getBuddyListBasePanel(),
                                 bSize);
        } else {
            displayFrameHelper(buddyListFloatingFrame, null, bSize);
            buddyListFloatingFrame.setTitle(title);
        }
        buddyListFloatingFrame.removeComponentListener(buddyListFloatingFrameListener);
        buddyListFloatingFrame.addComponentListener(buddyListFloatingFrameListener);
        
        Point bLoaction = runtime.getPbcWindowsLocation(PbcFaceComponentType.FloatingBuddyListFrame);
        if(bLoaction == null){
            bLoaction = new Point(100, 100);
        }
        Point bRightDownLocation=new Point((int)(bLoaction.x+bSize.getWidth()),(int)(bLoaction.y+bSize.getHeight()));            
        if (SwingGlobal.isLocationInScreenBounds(bLoaction)&&SwingGlobal.isLocationInScreenBounds(bRightDownLocation)){
            buddyListFloatingFrame.setLocation(bLoaction);
        }else{
            bLoaction = SwingGlobal.getScreenCenterPoint(this);
            buddyListFloatingFrame.setLocation(bLoaction);
            runtime.setPbcWindowsLocation(bLoaction, PbcFaceComponentType.FloatingBuddyListFrame);
        }
    }

    private void displayFrameHelper(final JFrame aFrame,
                                    final Component aPanel,
                                    final Dimension size) {
        if (aPanel != null) {
            //IOCommons.printMessage(">>> displayFloatingFrame ...");
            aFrame.setResizable(true);
            aFrame.getRootPane().getContentPane().add(aPanel);
            aFrame.setSize(size);

            aFrame.setIconImage(getImageSettings().getPointBoxIcon().getImage());

            Point center = SwingGlobal.getScreenCenterPoint(aFrame);

            aFrame.setLocation(center);
            aFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            aFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowDeiconified(WindowEvent e) {
                    synchronized(frameIconFlashingLocker){
                        if (frameIconFlashingAgent != null){
                            frameIconFlashingAgent.interrupt();
                            frameIconFlashingAgent = null;
                        }
                    }
                }
                @Override
                public void windowClosing(WindowEvent e) {
                    if (isFloatingStyle()){
                        getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);
                    }
                    switchDockingFrameLayout(ishLayout());
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    synchronized(frameIconFlashingLocker){
                        if (frameIconFlashingAgent != null){
                            frameIconFlashingAgent.interrupt();
                            frameIconFlashingAgent = null;
                        }
                    }
                }
            });
        }
        aFrame.invalidate();
        aFrame.pack();
        aFrame.setVisible(true);
    }

    private void dockingFramesHelper() {
        if (messagingFloatingFrame != null) {
            messagingFloatingFrame.setVisible(false);
            messagingFloatingFrame.dispose();
            messagingFloatingFrame = null;
        }
        if (buddyListFloatingFrame != null) {
            buddyListFloatingFrame.setVisible(false);
            buddyListFloatingFrame.dispose();
            buddyListFloatingFrame = null;
        }
    }

    /**
     * This method argues the current frame style has to be "docking" style
     */
    public void switchDockingFrameLayout(final boolean hLayout){
        if (SwingUtilities.isEventDispatchThread()){
            switchFrameLayoutHelper(hLayout);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    switchFrameLayoutHelper(hLayout);
                }
            });
        }
    }
    private void switchFrameLayoutHelper(boolean hLayout){

        //This will make sure it is not floating style. If it is, it will switch it back to docking style
        dockingFramesHelper();

        //setVisible(false);

        resetLayoutMenuHelper(hLayout);
        resetLayoutToolBarHelper(hLayout);
        switchLayoutHelper(hLayout);

        pack();

        face.reorganizeMessagingBoardTabButtons();
        
        //setVisible(true);
    }

    private void switchLayoutHelper(boolean hLayout) {
        jContentPanel.removeAll();
        if (hLayout){
            hPanel = HorizontalBasePanel.createHorizontalBasePanel(face);
            this.jContentPanel.add(hPanel, BorderLayout.CENTER);
            vPanel = null;
        }else{
            vPanel = VerticalBasePanel.createVerticalBasePanel(face);
            this.jContentPanel.add(vPanel, BorderLayout.CENTER);
            hPanel = null;
        }
    }

    private void resetLayoutMenuHelper(boolean hLayout){
        if (hLayout){
            jHorizontalLayout.setEnabled(false);
            jVerticalLayout.setEnabled(true);
        }else{
            jHorizontalLayout.setEnabled(true);
            jVerticalLayout.setEnabled(false);
        }
    }
    
    private void resetPricerLocationHelper(boolean isServerPricer){
        String status = jStatusBtn.getText();
//        status = status.replace(pricingAtServerText, "");
//        status = status.replace(pricingAtClientText, "");
         
        if(isServerPricer){
            jLocalPricerMenu.setEnabled(true);
            jServerPricerMenu.setEnabled(false);

            jPricerSetting.setIcon(getImageSettings().getLocalPricerIcon());
            jPricerSetting.setActionCommand("ChangeToLocal-sidePricer");
            jPricerSetting.setText(null);
            jPricerSetting.setToolTipText("Change to local pricer");        
            
//            status += pricingAtServerText;
            jStatusBtn.setText(status);
         }else{
            try {
                //register local pricer
                face.getKernel().registerPbcComponent(PbcPricingAgent.getPbcPricingAgentSingleton(face.getKernel()));
                 
                jLocalPricerMenu.setEnabled(false);
                jServerPricerMenu.setEnabled(true);
                 
                jPricerSetting.setIcon(getImageSettings().getServerPricerIcon());
                jPricerSetting.setActionCommand("ChangeToServer-sidePricer");
                jPricerSetting.setText(null);
                jPricerSetting.setToolTipText("Change to server-side pricer"); 
            
//                status += pricingAtClientText;
                jStatusBtn.setText(status);
            } catch (PointBoxFatalException ex) {
                Logger.getLogger(PointBoxFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void resetPricerLocationHelperinEDT(final boolean isServerPricer){
        if(SwingUtilities.isEventDispatchThread()){
            resetPricerLocationHelper(isServerPricer);
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    resetPricerLocationHelper(isServerPricer);
                }
            });
        }
    }

    private void resetLayoutToolBarHelper(boolean hLayout){
        if (hLayout){
            jVorHBtn.setIcon(getImageSettings().getVerticalFrameLayoutIcon());
            jVorHBtn.setText(null);
            jVorHBtn.setToolTipText("Horizontal layout");
        }else{
            jVorHBtn.setIcon(getImageSettings().getHorizontalFrameLayoutIcon());
            jVorHBtn.setText(null);
            jVorHBtn.setToolTipText("Vertical layout");
        }
    }

    private String psdnToolTipText = "Synchronize your local pricing settings by downloading the latest data from PointBox server";
    
    private void customizeToolBar() {
        
        jPBIMBtn.setText("PBIM");
        jPBIMBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        jPBIMBtn.setIcon(getImageSettings().getPbimBuddyIcon());
        jPBIMBtn.setActionCommand(PbconsoleMenuCommand.CONNECT_PB_CONNECT_MENU_ITEM.toString());
        jPBIMBtn.setToolTipText(PbconsoleMenuCommand.CONNECT_PB_CONNECT_MENU_ITEM.toString());
        
        jAIMBtn.setText("AIM");
        jAIMBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        jAIMBtn.setIcon(getImageSettings().getAimOffline16Icon());
        jAIMBtn.setActionCommand(PbconsoleMenuCommand.CONNECT_AOL_CONNECT_MENU_ITEM.toString());
        jAIMBtn.setToolTipText(PbconsoleMenuCommand.CONNECT_AOL_CONNECT_MENU_ITEM.toString());

        jYIMBtn.setText("Yahoo");
        jYIMBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        jYIMBtn.setIcon(getImageSettings().getYimOffline16Icon());
        jYIMBtn.setActionCommand(PbconsoleMenuCommand.CONNECT_YAHOO_CONNECT_MENU_ITEM.toString());
        jYIMBtn.setToolTipText(PbconsoleMenuCommand.CONNECT_YAHOO_CONNECT_MENU_ITEM.toString());

        if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.PBIM_USERS)){
            jAIMBtn.setVisible(false);
            jYIMBtn.setVisible(false);
        }else{
            jAIMBtn.setVisible(true);
            jYIMBtn.setVisible(true);
        }
        
        resetStyleToolBarHelper(isFloatingStyle());

        resetLayoutToolBarHelper(ishLayout());

        jColorBtn.setIcon(getImageSettings().getPaletteIcon());
        jColorBtn.setText(null);

        jFontBtn.setIcon(getImageSettings().getFontStyleIcon());
        jFontBtn.setText(null);

        jSaveAllBtn.setIcon(getImageSettings().getSaveAllIcon());
        jSaveAllBtn.setToolTipText("Save current working settings of PointBox console.");
        jSaveAllBtn.setText(null);

        jClearBtn.setIcon(getImageSettings().getPageRefreshIcon());
        jClearBtn.setText(null);

        jFilterBtn.setIcon(getImageSettings().getFilterIcon());
        jFilterBtn.setText(null);

        jSearchBtn.setIcon(getImageSettings().getSearchIcon());
        jSearchBtn.setText(null);

        jPsDn.setIcon(getImageSettings().getPriceSettingsDownloadIcon());
        jPsDn.setText("PB Marks");
        jPsDn.setToolTipText(psdnToolTipText + ".");
        jPsDn.setHorizontalTextPosition(SwingConstants.RIGHT);
        
        jFCBtn.setIcon(getImageSettings().getForwardIcon());
        jFCBtn.setText("Forward Curve");
        jFCBtn.setHorizontalTextPosition(SwingConstants.RIGHT);

        jVSBtn.setIcon(getImageSettings().getVolIcon());
        jVSBtn.setText("Volatility");
        jVSBtn.setHorizontalTextPosition(SwingConstants.RIGHT);

        jIRBtn.setIcon(getImageSettings().getPercentIcon());
        jIRBtn.setText("Interest Rate");
        jIRBtn.setHorizontalTextPosition(SwingConstants.RIGHT);

        jCompBtn.setIcon(getImageSettings().getComputationIcon());
        jCompBtn.setText("Term");
        jCompBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        jCompBtn.setVisible(false);

//        jStripBtn.setIcon(getImageSettings().getPricerIcon());
//        jStripBtn.setText("Pricer");
//        jStripBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
//        jStripBtn.setVisible(false);
        
        jOsimBtn.setIcon(getImageSettings().getPricerIcon());
        jOsimBtn.setText("Option Pricer");
        jOsimBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        
        jCPConnect.setIcon(getImageSettings().getClearportIcon());
        jCPConnect.setHorizontalTextPosition(SwingConstants.RIGHT);

        //jPublishBtn.setIcon(getImageSettings().getChartCurveEditIcon());
        //jPublishBtn.setText("Edit Curves");
        //jPublishBtn.setHorizontalTextPosition(SwingConstants.RIGHT);

        jWeatherBtn.setIcon(getImageSettings().getWeatherIcon());
        jWeatherBtn.setText("Weather");
        jWeatherBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        
        jOnlineUpdate.setIcon(getImageSettings().getOnlineUpdateGreenIcon());
        jOnlineUpdate.setText("Update Release");
        jOnlineUpdate.setHorizontalTextPosition(SwingConstants.RIGHT);
        //jOnlineUpdate.setVisible(false);

        if(Boolean.valueOf((String)getSoundSettings().get("enableSound")) == false){
            jSoundBtn.setIcon(getImageSettings().getSoundOnIcon());
        }
        else {
            jSoundBtn.setIcon(getImageSettings().getSoundOffIcon());
        }
        jSoundBtn.setText(null);

        jViewerScrolling.setIcon(getImageSettings().getViewerFreezingIcon());
        jViewerScrolling.setActionCommand("DisableViewerScrolling");
        jViewerScrolling.setText(null);
        jViewerScrolling.setToolTipText("Disable current aggregator scrolling");
        
        jShutdownBtn.setIcon(getImageSettings().getShutdownIcon());
        jShutdownBtn.setText(null);
        
    }
    
    private HashMap getSoundSettings(){
        return face.getKernel().getPointBoxConsoleRuntime().getPbcAudioSettings().getSoundSetting();
    }
    
    private void finalizeFrameInitialization(){
        //setTitle(PointBoxReleasePolicy.getSingleton().getSoftwareName());
        setTitle("PointBox Console");
        setIconImage(getImageSettings().getPointBoxIcon().getImage());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        removeWindowListener(frameWindowListener);
        addWindowListener(frameWindowListener);

        pack();

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        setMaximizedBounds(env.getMaximumWindowBounds());
        
        if (isFloatingStyle()){
            setSize(getPbcRuntime().getPbcWindowsSize(PbcFaceComponentType.FloatingPointBoxFrame));
            Point aPoint = getPbcRuntime().getPbcWindowsLocation(PbcFaceComponentType.FloatingPointBoxFrame);
            if(aPoint != null){
                setLocation(aPoint);
            }
        }else{
            setSize(getPbcRuntime().getPbcWindowsSize(PbcFaceComponentType.PointBoxFrame));
            Point aPoint = getPbcRuntime().getPbcWindowsLocation(PbcFaceComponentType.PointBoxFrame);
            if(aPoint != null){
                setLocation(aPoint);
            }
        }
    }

    public void testerMinimizeConsole() {
        if (SwingUtilities.isEventDispatchThread()){
            setState(Frame.ICONIFIED);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setState(Frame.ICONIFIED);
                }
            });
        }
    }

    void updateSystemFrameSize(final Dimension newSize) {
        if (SwingUtilities.isEventDispatchThread()){
            setSize(newSize);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setSize(newSize);
                }
            });
        }
    }
    /**
     * Handle all the events raised from the kernel
     * @param event
     */
    @Override
    public void handlePointBoxEvent(final PointBoxConsoleEvent event) {
        if (event instanceof MessageRecievedEvent){
            handleMessageRecievedEvent((MessageRecievedEvent)event);
        }else if (event instanceof MessageSentEvent){
            handleMessageSentEvent((MessageSentEvent)event);
        }else if (event instanceof GatewayConnectionEvent){
            handleGatewayConnectionEvent((GatewayConnectionEvent)event);
        }else if (event instanceof ServerNotSupportedEvent){
            handleServerNotSupportedEvent((ServerNotSupportedEvent)event);
        }else if (event instanceof WhenTechPricerUnavaliableEvent){
            handleWhenTechPricerUnavaliableEvent((WhenTechPricerUnavaliableEvent)event);
        }
    }
    
    private void switchToPBPricer(){
        switchPricerTextColorHelper();
        if (face.getKernel().isServerPricer()){
            face.getKernel().raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                        PbcPricerType.WTS, 
                                                                        PbcPricerType.PBS,
                                                                        PointBoxConsoleProperties.getSingleton(),
                                                                        face.getKernel()));
        }else{
            face.getKernel().raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                        PbcPricerType.WT, 
                                                                        PbcPricerType.PB,
                                                                        PointBoxConsoleProperties.getSingleton(),
                                                                        face.getKernel()));
        }
    }
    
    private void switchToWTPricer(){
        switchPricerTextColorHelper();
        face.getKernel().raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                    PbcPricerType.PB,
                                                                    PbcPricerType.WT,
                                                                    PointBoxConsoleProperties.getSingleton(),
                                                                    face.getKernel()));
    }

    private void handleWhenTechPricerUnavaliableEvent(WhenTechPricerUnavaliableEvent pBPricerUnavaliableEvent) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                //jRollback.setEnabled(false);
                JOptionPane.showMessageDialog(PointBoxFrame.this, 
                        "No connection to the local WhenTech application."
                        + NIOGlobal.lineSeparator()
                        + "WhenTech pricer is disabled in PointBox console.");
                presentWenTechConnectButton();
            }
        });
    }
    
    private void presentWenTechConnectButton(){
        if (SwingUtilities.isEventDispatchThread()){
            presentWenTechConnectButtonHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    presentWenTechConnectButtonHelper();
                }
            });
        }
    }

    private void presentWenTechConnectButtonHelper() {
        jWTPricer.setVisible(false);
        jWTConnect.setVisible(true);
        jPBPricer.setSelected(true);
        switchToPBPricer();
    }

    private void handleServerNotSupportedEvent(ServerNotSupportedEvent event) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                jRollback.setEnabled(false);
                JOptionPane.showMessageDialog(PointBoxFrame.this, 
                        "Warning: your PointBox console's release version is not support by PointBox server anymore!" 
                        + NIOGlobal.lineSeparator() 
                        + "You have to update your release now.");
                
                checkOnlineUpdateClicked();
            }
        });
    }

    private void handleMessageSentEvent(MessageSentEvent event){
        if (event == null){
            return;
        }
        IPbsysOptionQuote msgQuote = event.getSentQuoteMessage();
        if (msgQuote == null) {
            return;
        }
        IPbsysInstantMessage msg = msgQuote.getInstantMessage();
        if (msg == null){
            return;
        }
        String leading = " - To ";
        pbcArchiveDialog.bufferInstantMessages(msg);
        
        if (DataGlobal.isEmptyNullString(msg.getToUser().getNickname())){
            updateSplashScreen(leading + msg.getToUser().getIMUniqueName()
                                                  + ": " + msg.getMessageContent(), Level.INFO, 10);
        }else{
            updateSplashScreen(leading + msg.getToUser().getNickname()
                                                  + ": " + msg.getMessageContent(), Level.INFO, 10);
        }
    }
    
    private void handleMessageRecievedEvent(MessageRecievedEvent event){
        if (event == null){
            return;
        }
        IPbsysOptionQuote msgQuote = event.getReceievedQuoteMessage();
        if (msgQuote == null) {
            return;
        }
        IPbsysInstantMessage msg = msgQuote.getInstantMessage();
        if (msg == null){
            return;
        }
        if ((getExtendedState() == JFrame.ICONIFIED) || (!isActive())){
            synchronized(frameIconFlashingLocker){
                if ((frameIconFlashingAgent == null) || (!frameIconFlashingAgent.isAlive())){
                    frameIconFlashingAgent = new FrameIconFlashingAgent();
                    frameIconFlashingAgent.start();
                }
            }
        }
        pbcArchiveDialog.bufferInstantMessages(msg);
        if (DataGlobal.isEmptyNullString(msg.getFromUser().getNickname())){
            updateSplashScreen(" - " + msg.getFromUser().getIMUniqueName()
                                                  + " said: " + msg.getMessageContent(), Level.INFO, 10);
        }else{
            updateSplashScreen(" - " + msg.getFromUser().getNickname()
                                                  + " said: " + msg.getMessageContent(), Level.INFO, 10);
        }
    }

    private Thread flashOnlineUpdateThread;
    void handleReleaseUpdateRequired(PbcReleaseInformation releaseInfo) {
        if (!releaseInfo.getPbcReleaseStatus().equals(PbcReleaseStatus.Latest_Release)){
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jRollback.setEnabled(false);
                }
            });
        }
    }

    void pushFloatingMessagingFrameToFront() {
        if (SwingUtilities.isEventDispatchThread()){
            pushFloatingMessagingFrameToFrontHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    pushFloatingMessagingFrameToFrontHelper();
                }
            });
        }
    }
    private void pushFloatingMessagingFrameToFrontHelper(){
        if (messagingFloatingFrame.getExtendedState() == JFrame.ICONIFIED){
            messagingFloatingFrame.setExtendedState(JFrame.NORMAL);
        }
        messagingFloatingFrame.toFront();
    }

    /**
     * @return the pbcReloadingDialog
     */
    public PbcReloadingDialog getPbcReloadingDialog() {
        if(pbcReloadingDialog==null){
            pbcReloadingDialog=PbcReloadingDialog.getSingletonInstance(face.getPointBoxTalker());
        }
        return pbcReloadingDialog;
    }

    String getArchiveWarningMessage() {
        return this.pbcArchiveDialog.getArchiveWarningMessage();
    }

    private class ClockMonitor extends TimerTask{
        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()){
                jClock.setText(CalendarGlobal.getCurrentMMDDYY("/") + " " + CalendarGlobal.getCurrentHHmm(":") + "  ");
            }else{
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        jClock.setText(CalendarGlobal.getCurrentMMDDYY("/") + " " + CalendarGlobal.getCurrentHHmm(":") + "  ");
                    }
                });
            }
        }
    }     

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPricerGroup = new javax.swing.ButtonGroup();
        jToolBarV = new javax.swing.JToolBar();
        jSaveAllBtn = new javax.swing.JButton();
        jClearBtn = new javax.swing.JButton();
        jFilterBtn = new javax.swing.JButton();
        jSearchBtn = new javax.swing.JButton();
        jForDBtn = new javax.swing.JButton();
        jVorHBtn = new javax.swing.JButton();
        jColorBtn = new javax.swing.JButton();
        jFontBtn = new javax.swing.JButton();
        jSoundBtn = new javax.swing.JButton();
        jViewerScrolling = new javax.swing.JButton();
        jPricerSetting = new javax.swing.JButton();
        jShutdownBtn = new javax.swing.JButton();
        jToolBarH = new javax.swing.JToolBar();
        jOsimBtn = new javax.swing.JButton();
        jCompBtn = new javax.swing.JButton();
        jFCBtn = new javax.swing.JButton();
        jVSBtn = new javax.swing.JButton();
        jIRBtn = new javax.swing.JButton();
        jPsDn = new javax.swing.JButton();
        jCPConnect = new javax.swing.JButton();
        jOnlineUpdate = new javax.swing.JButton();
        jUploadBtn = new javax.swing.JButton();
        jLegacyUploadBtn = new javax.swing.JButton();
        jPBIMBtn = new javax.swing.JButton();
        jAIMBtn = new javax.swing.JButton();
        jYIMBtn = new javax.swing.JButton();
        jWeatherBtn = new javax.swing.JButton();
        jPBPricer = new javax.swing.JRadioButton();
        jWTPricer = new javax.swing.JRadioButton();
        jWTConnect = new javax.swing.JButton();
        jContentPanel = new javax.swing.JPanel();
        jStatusBar = new javax.swing.JToolBar();
        jStatusBtn = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        jClock = new javax.swing.JLabel();
        jSeparator14 = new javax.swing.JToolBar.Separator();
        jProgressBar = new javax.swing.JProgressBar();
        jMsgSep = new javax.swing.JToolBar.Separator();
        jTalker = new javax.swing.JLabel();
        jStatusMessage = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jGateway = new javax.swing.JMenu();
        jConnectToAim = new javax.swing.JMenuItem();
        jDisconnectFromAIM = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jConnectToYahoo = new javax.swing.JMenuItem();
        jDisconnectFromYahoo = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jShutdown = new javax.swing.JMenuItem();
        jViewer = new javax.swing.JMenu();
        jAllMessagesTab = new javax.swing.JMenuItem();
        jAllQuotesTab = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jOutgoingTab = new javax.swing.JMenuItem();
        jIncomingTab = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jFilter = new javax.swing.JMenuItem();
        jSearch = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        jLoaderItem = new javax.swing.JMenuItem();
        jClearAllSheets = new javax.swing.JMenuItem();
        jPricing = new javax.swing.JMenu();
        jFC = new javax.swing.JMenuItem();
        jVS = new javax.swing.JMenuItem();
        jIR = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jComp = new javax.swing.JMenuItem();
        jOsim = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jLocalPricerMenu = new javax.swing.JMenuItem();
        jServerPricerMenu = new javax.swing.JMenuItem();
        jSettings = new javax.swing.JMenu();
        jGeneralSettings = new javax.swing.JMenuItem();
        jViewerFontColorSettings = new javax.swing.JMenuItem();
        jViewerColumnSettings = new javax.swing.JMenuItem();
        jPricerSettings = new javax.swing.JMenuItem();
        jCurveSettings = new javax.swing.JMenuItem();
        jCloudingPublish = new javax.swing.JMenuItem();
        jMsAccessSettings = new javax.swing.JMenuItem();
        jPBSettings = new javax.swing.JMenuItem();
        jAIMSettings = new javax.swing.JMenuItem();
        jYahooSettings = new javax.swing.JMenuItem();
        jBlockedBuddies = new javax.swing.JMenuItem();
        jOptions = new javax.swing.JMenu();
        jFloatingWindow = new javax.swing.JMenuItem();
        jDockedWindow = new javax.swing.JMenuItem();
        jFont1 = new javax.swing.JMenuItem();
        jColor1 = new javax.swing.JMenuItem();
        jVerticalLayout = new javax.swing.JMenuItem();
        jHorizontalLayout = new javax.swing.JMenuItem();
        jHelp = new javax.swing.JMenu();
        jPointBoxHelp = new javax.swing.JMenuItem();
        jSaveWorkspaceMenuItem = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jCheckForUpdates = new javax.swing.JMenuItem();
        jRollback = new javax.swing.JMenuItem();
        jAboutUs = new javax.swing.JMenuItem();
        jTest = new javax.swing.JMenu();
        jPBCTest = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToolBarV.setFloatable(false);
        jToolBarV.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBarV.setRollover(true);
        jToolBarV.setName("jToolBarV"); // NOI18N

        jSaveAllBtn.setText("SaveAll");
        jSaveAllBtn.setFocusable(false);
        jSaveAllBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSaveAllBtn.setName("jSaveAllBtn"); // NOI18N
        jSaveAllBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSaveAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveAllBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jSaveAllBtn);

        jClearBtn.setText("Clear");
        jClearBtn.setToolTipText("Keep today's quotes or remove all quotes"); // NOI18N
        jClearBtn.setFocusable(false);
        jClearBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jClearBtn.setName("jClearBtn"); // NOI18N
        jClearBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jClearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jClearBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jClearBtn);

        jFilterBtn.setText("Filter");
        jFilterBtn.setToolTipText("Create filter");
        jFilterBtn.setFocusable(false);
        jFilterBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jFilterBtn.setName("jFilterBtn"); // NOI18N
        jFilterBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFilterBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jFilterBtn);

        jSearchBtn.setText("Search");
        jSearchBtn.setToolTipText("Search historical quote messages");
        jSearchBtn.setFocusable(false);
        jSearchBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSearchBtn.setName("jSearchBtn"); // NOI18N
        jSearchBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSearchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSearchBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jSearchBtn);

        jForDBtn.setText("ForD");
        jForDBtn.setFocusable(false);
        jForDBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jForDBtn.setName("jForDBtn"); // NOI18N
        jForDBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jForDBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jForDBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jForDBtn);

        jVorHBtn.setText("VorH");
        jVorHBtn.setFocusable(false);
        jVorHBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jVorHBtn.setName("jVorHBtn"); // NOI18N
        jVorHBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jVorHBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jVorHBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jVorHBtn);

        jColorBtn.setText("Color");
        jColorBtn.setToolTipText("Choose Color");
        jColorBtn.setFocusable(false);
        jColorBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jColorBtn.setName("jColorBtn"); // NOI18N
        jColorBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jColorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jColorBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jColorBtn);

        jFontBtn.setText("Font");
        jFontBtn.setToolTipText("Choose Font");
        jFontBtn.setFocusable(false);
        jFontBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jFontBtn.setName("jFontBtn"); // NOI18N
        jFontBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jFontBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFontBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jFontBtn);

        jSoundBtn.setText("Sound");
        jSoundBtn.setToolTipText("Enable/Disable PointBox Console Sound");
        jSoundBtn.setFocusable(false);
        jSoundBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSoundBtn.setName("jSoundBtn"); // NOI18N
        jSoundBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSoundBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSoundBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jSoundBtn);

        jViewerScrolling.setText("Scrolling");
        jViewerScrolling.setToolTipText("");
        jViewerScrolling.setFocusable(false);
        jViewerScrolling.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jViewerScrolling.setName("jViewerScrolling"); // NOI18N
        jViewerScrolling.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jViewerScrolling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewerScrollingActionPerformed(evt);
            }
        });
        jToolBarV.add(jViewerScrolling);

        jPricerSetting.setText("Pricer");
        jPricerSetting.setToolTipText("");
        jPricerSetting.setFocusable(false);
        jPricerSetting.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPricerSetting.setName("jPricerSetting"); // NOI18N
        jPricerSetting.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPricerSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPricerSettingActionPerformed(evt);
            }
        });
        jToolBarV.add(jPricerSetting);

        jShutdownBtn.setText("Shutdown"); // NOI18N
        jShutdownBtn.setToolTipText("Shutdown PointBox Console?");
        jShutdownBtn.setFocusable(false);
        jShutdownBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jShutdownBtn.setName("jShutdownBtn"); // NOI18N
        jShutdownBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jShutdownBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShutdownBtnActionPerformed(evt);
            }
        });
        jToolBarV.add(jShutdownBtn);

        getContentPane().add(jToolBarV, java.awt.BorderLayout.LINE_START);

        jToolBarH.setFloatable(false);
        jToolBarH.setRollover(true);
        jToolBarH.setName("jToolBarH"); // NOI18N

        jOsimBtn.setText("OSIM");
        jOsimBtn.setFocusable(false);
        jOsimBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jOsimBtn.setName("jOsimBtn"); // NOI18N
        jOsimBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jOsimBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOsimBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jOsimBtn);

        jCompBtn.setText("Term");
        jCompBtn.setToolTipText("Comp");
        jCompBtn.setFocusable(false);
        jCompBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCompBtn.setName("jCompBtn"); // NOI18N
        jCompBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCompBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCompBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jCompBtn);

        jFCBtn.setText("FC");
        jFCBtn.setToolTipText("Forward Curve");
        jFCBtn.setFocusable(false);
        jFCBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jFCBtn.setName("jFCBtn"); // NOI18N
        jFCBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jFCBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFCBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jFCBtn);

        jVSBtn.setText("VS");
        jVSBtn.setToolTipText("Volatility Surface");
        jVSBtn.setFocusable(false);
        jVSBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jVSBtn.setName("jVSBtn"); // NOI18N
        jVSBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jVSBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jVSBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jVSBtn);

        jIRBtn.setText("IR");
        jIRBtn.setToolTipText("Interest Rates");
        jIRBtn.setFocusable(false);
        jIRBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jIRBtn.setName("jIRBtn"); // NOI18N
        jIRBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jIRBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jIRBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jIRBtn);

        jPsDn.setText("PsDn");
        jPsDn.setFocusable(false);
        jPsDn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPsDn.setName("jPsDn"); // NOI18N
        jPsDn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPsDn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPsDnActionPerformed(evt);
            }
        });
        jToolBarH.add(jPsDn);

        jCPConnect.setText("ClearPort");
        jCPConnect.setToolTipText("Login to CME ClearPort");
        jCPConnect.setFocusable(false);
        jCPConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCPConnect.setName("jCPConnect"); // NOI18N
        jCPConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCPConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCPConnectActionPerformed(evt);
            }
        });
        jToolBarH.add(jCPConnect);

        jOnlineUpdate.setText("Update");
        jOnlineUpdate.setFocusable(false);
        jOnlineUpdate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jOnlineUpdate.setName("jOnlineUpdate"); // NOI18N
        jOnlineUpdate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jOnlineUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOnlineUpdateActionPerformed(evt);
            }
        });
        jToolBarH.add(jOnlineUpdate);

        jUploadBtn.setText("Upload EMS");
        jUploadBtn.setFocusable(false);
        jUploadBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jUploadBtn.setName("jUploadBtn"); // NOI18N
        jUploadBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jUploadBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jUploadBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jUploadBtn);

        jLegacyUploadBtn.setText("Legacy EMS");
        jLegacyUploadBtn.setFocusable(false);
        jLegacyUploadBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLegacyUploadBtn.setName("jLegacyUploadBtn"); // NOI18N
        jLegacyUploadBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jLegacyUploadBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLegacyUploadBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jLegacyUploadBtn);

        jPBIMBtn.setText("PB");
        jPBIMBtn.setFocusable(false);
        jPBIMBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPBIMBtn.setMaximumSize(new java.awt.Dimension(92, 21));
        jPBIMBtn.setMinimumSize(new java.awt.Dimension(92, 21));
        jPBIMBtn.setName("jPBIMBtn"); // NOI18N
        jPBIMBtn.setPreferredSize(new java.awt.Dimension(92, 21));
        jPBIMBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPBIMBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPBIMBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(Box.createHorizontalGlue());
        jToolBarH.add(jPBIMBtn);

        jAIMBtn.setText("AIM");
        jAIMBtn.setFocusable(false);
        jAIMBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jAIMBtn.setMaximumSize(new java.awt.Dimension(92, 21));
        jAIMBtn.setMinimumSize(new java.awt.Dimension(92, 21));
        jAIMBtn.setName("jAIMBtn"); // NOI18N
        jAIMBtn.setPreferredSize(new java.awt.Dimension(92, 21));
        jAIMBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jAIMBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAIMBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jAIMBtn);

        jYIMBtn.setText("YIM");
        jYIMBtn.setFocusable(false);
        jYIMBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jYIMBtn.setMaximumSize(new java.awt.Dimension(92, 21));
        jYIMBtn.setMinimumSize(new java.awt.Dimension(92, 21));
        jYIMBtn.setName("jYIMBtn"); // NOI18N
        jYIMBtn.setPreferredSize(new java.awt.Dimension(92, 21));
        jYIMBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jYIMBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jYIMBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jYIMBtn);

        jWeatherBtn.setText("Weather");
        jWeatherBtn.setFocusable(false);
        jWeatherBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jWeatherBtn.setName("jWeatherBtn"); // NOI18N
        jWeatherBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jWeatherBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jWeatherBtnActionPerformed(evt);
            }
        });
        jToolBarH.add(jWeatherBtn);

        jPricerGroup.add(jPBPricer);
        jPBPricer.setForeground(new java.awt.Color(255, 0, 0));
        jPBPricer.setSelected(true);
        jPBPricer.setText("PB Price");
        jPBPricer.setFocusable(false);
        jPBPricer.setName("jPBPricer"); // NOI18N
        jPBPricer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPBPricer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPBPricerActionPerformed(evt);
            }
        });
        jToolBarH.add(jPBPricer);

        jPricerGroup.add(jWTPricer);
        jWTPricer.setText("WhenTech Price");
        jWTPricer.setFocusable(false);
        jWTPricer.setName("jWTPricer"); // NOI18N
        jWTPricer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jWTPricer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jWTPricerActionPerformed(evt);
            }
        });
        jToolBarH.add(jWTPricer);

        jWTConnect.setForeground(new java.awt.Color(0, 0, 255));
        jWTConnect.setText("WhenTech");
        jWTConnect.setToolTipText("Connect to a local WhenTech which is running...");
        jWTConnect.setFocusable(false);
        jWTConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jWTConnect.setName("jWTConnect"); // NOI18N
        jWTConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jWTConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jWTConnectActionPerformed(evt);
            }
        });
        jToolBarH.add(jWTConnect);

        getContentPane().add(jToolBarH, java.awt.BorderLayout.PAGE_START);

        jContentPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jContentPanel.setName("jContentPanel"); // NOI18N
        jContentPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(jContentPanel, java.awt.BorderLayout.CENTER);

        jStatusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jStatusBar.setFloatable(false);
        jStatusBar.setRollover(true);
        jStatusBar.setAlignmentY(0.5F);
        jStatusBar.setName("jStatusBar"); // NOI18N

        jStatusBtn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jStatusBtn.setForeground(new java.awt.Color(0, 0, 255));
        jStatusBtn.setText("Status: Crude Oil"); // NOI18N
        jStatusBtn.setContentAreaFilled(false);
        jStatusBtn.setFocusable(false);
        jStatusBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jStatusBtn.setName("jStatusBtn"); // NOI18N
        jStatusBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jStatusBar.add(jStatusBtn);

        jSeparator12.setName("jSeparator12"); // NOI18N
        jStatusBar.add(jSeparator12);

        jClock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jClock.setText("Date/Time   ");
        jClock.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jClock.setName("jClock"); // NOI18N
        jStatusBar.add(jClock);

        jSeparator14.setName("jSeparator14"); // NOI18N
        jStatusBar.add(jSeparator14);

        jProgressBar.setMaximumSize(new java.awt.Dimension(175, 15));
        jProgressBar.setMinimumSize(new java.awt.Dimension(175, 15));
        jProgressBar.setName("jProgressBar"); // NOI18N
        jProgressBar.setPreferredSize(new java.awt.Dimension(175, 15));
        jProgressBar.setRequestFocusEnabled(false);
        jStatusBar.add(jProgressBar);

        jMsgSep.setName("jMsgSep"); // NOI18N
        jStatusBar.add(jMsgSep);

        jTalker.setText(" Talker -  ");
        jTalker.setName("jTalker"); // NOI18N
        jStatusBar.add(jTalker);

        jStatusMessage.setText("message:"); // NOI18N
        jStatusMessage.setMaximumSize(new java.awt.Dimension(500, 21));
        jStatusMessage.setMinimumSize(new java.awt.Dimension(300, 21));
        jStatusMessage.setName("jStatusMessage"); // NOI18N
        jStatusMessage.setPreferredSize(new java.awt.Dimension(300, 21));
        jStatusBar.add(jStatusMessage);

        getContentPane().add(jStatusBar, java.awt.BorderLayout.PAGE_END);

        jMenuBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jMenuBar.setName("jMenuBar"); // NOI18N

        jGateway.setText("File"); // NOI18N
        jGateway.setName("jGateway"); // NOI18N

        jConnectToAim.setText("Connect to AIM"); // NOI18N
        jConnectToAim.setName("jConnectToAim"); // NOI18N
        jConnectToAim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jConnectToAimActionPerformed(evt);
            }
        });
        jGateway.add(jConnectToAim);

        jDisconnectFromAIM.setText("Disconnect from AIM"); // NOI18N
        jDisconnectFromAIM.setEnabled(false);
        jDisconnectFromAIM.setName("jDisconnectFromAIM"); // NOI18N
        jDisconnectFromAIM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDisconnectFromAIMActionPerformed(evt);
            }
        });
        jGateway.add(jDisconnectFromAIM);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jGateway.add(jSeparator2);

        jConnectToYahoo.setText("Connect to Yahoo"); // NOI18N
        jConnectToYahoo.setName("jConnectToYahoo"); // NOI18N
        jConnectToYahoo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jConnectToYahooActionPerformed(evt);
            }
        });
        jGateway.add(jConnectToYahoo);

        jDisconnectFromYahoo.setText("Disconnect from Yahoo"); // NOI18N
        jDisconnectFromYahoo.setEnabled(false);
        jDisconnectFromYahoo.setName("jDisconnectFromYahoo"); // NOI18N
        jDisconnectFromYahoo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDisconnectFromYahooActionPerformed(evt);
            }
        });
        jGateway.add(jDisconnectFromYahoo);
        jGateway.add(jSeparator3);

        jMenuItem1.setText("Archive");
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jGateway.add(jMenuItem1);

        jShutdown.setText("Exit"); // NOI18N
        jShutdown.setName("jShutdown"); // NOI18N
        jShutdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShutdownActionPerformed(evt);
            }
        });
        jGateway.add(jShutdown);

        jMenuBar.add(jGateway);

        jViewer.setText("Aggregator"); // NOI18N
        jViewer.setName("jViewer"); // NOI18N

        jAllMessagesTab.setText("All Messages Sheet"); // NOI18N
        jAllMessagesTab.setName("jAllMessagesTab"); // NOI18N
        jAllMessagesTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAllMessagesTabActionPerformed(evt);
            }
        });
        jViewer.add(jAllMessagesTab);

        jAllQuotesTab.setText("All Quotes Sheet"); // NOI18N
        jAllQuotesTab.setName("jAllQuotesTab"); // NOI18N
        jAllQuotesTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAllQuotesTabActionPerformed(evt);
            }
        });
        jViewer.add(jAllQuotesTab);
        jViewer.add(jSeparator4);

        jOutgoingTab.setText("Outgoing Messages Sheet"); // NOI18N
        jOutgoingTab.setName("jOutgoingTab"); // NOI18N
        jOutgoingTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOutgoingTabActionPerformed(evt);
            }
        });
        jViewer.add(jOutgoingTab);

        jIncomingTab.setText("Incoming Messages Sheet"); // NOI18N
        jIncomingTab.setName("jIncomingTab"); // NOI18N
        jIncomingTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jIncomingTabActionPerformed(evt);
            }
        });
        jViewer.add(jIncomingTab);
        jViewer.add(jSeparator5);

        jFilter.setText("Create Filter Tab"); // NOI18N
        jFilter.setName("jFilter"); // NOI18N
        jFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFilterActionPerformed(evt);
            }
        });
        jViewer.add(jFilter);

        jSearch.setText("Search Historical Quotes"); // NOI18N
        jSearch.setName("jSearch"); // NOI18N
        jSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSearchActionPerformed(evt);
            }
        });
        jViewer.add(jSearch);

        jSeparator16.setName("jSeparator16"); // NOI18N
        jViewer.add(jSeparator16);

        jLoaderItem.setText("Load Message History");
        jLoaderItem.setName("jLoaderItem"); // NOI18N
        jLoaderItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoaderItemActionPerformed(evt);
            }
        });
        jViewer.add(jLoaderItem);

        jClearAllSheets.setText("Clear Aggregator"); // NOI18N
        jClearAllSheets.setName("jClearAllSheets"); // NOI18N
        jClearAllSheets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jClearAllSheetsActionPerformed(evt);
            }
        });
        jViewer.add(jClearAllSheets);

        jMenuBar.add(jViewer);

        jPricing.setText("Pricing"); // NOI18N
        jPricing.setName("jPricing"); // NOI18N

        jFC.setText("Forward Curve"); // NOI18N
        jFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFCActionPerformed(evt);
            }
        });
        jPricing.add(jFC);

        jVS.setText("Volatility Surface"); // NOI18N
        jVS.setName("jVS"); // NOI18N
        jVS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jVSActionPerformed(evt);
            }
        });
        jPricing.add(jVS);

        jIR.setText("Interest Rates"); // NOI18N
        jIR.setName("jIR"); // NOI18N
        jIR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jIRActionPerformed(evt);
            }
        });
        jPricing.add(jIR);
        jPricing.add(jSeparator6);

        jComp.setText("Comp"); // NOI18N
        jComp.setName("jComp"); // NOI18N
        jComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCompActionPerformed(evt);
            }
        });
        jPricing.add(jComp);

        jOsim.setText("Option Pricer"); // NOI18N
        jOsim.setName("jOsim"); // NOI18N
        jOsim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOsimActionPerformed(evt);
            }
        });
        jPricing.add(jOsim);

        jSeparator8.setName("jSeparator8"); // NOI18N
        jPricing.add(jSeparator8);

        jLocalPricerMenu.setText("Local Pricer");
        jLocalPricerMenu.setName("jLocalPricerMenu"); // NOI18N
        jLocalPricerMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLocalPricerMenuActionPerformed(evt);
            }
        });
        jPricing.add(jLocalPricerMenu);

        jServerPricerMenu.setText("Server Pricer");
        jServerPricerMenu.setName("jServerPricerMenu"); // NOI18N
        jServerPricerMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jServerPricerMenuActionPerformed(evt);
            }
        });
        jPricing.add(jServerPricerMenu);

        jMenuBar.add(jPricing);

        jSettings.setText("Settings");
        jSettings.setName("jSettings"); // NOI18N

        jGeneralSettings.setText("General"); // NOI18N
        jGeneralSettings.setName("jGeneralSettings"); // NOI18N
        jGeneralSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGeneralSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jGeneralSettings);

        jViewerFontColorSettings.setText("Aggregator Font & Color");
        jViewerFontColorSettings.setName("jViewerFontColorSettings"); // NOI18N
        jViewerFontColorSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewerFontColorSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jViewerFontColorSettings);

        jViewerColumnSettings.setText("Aggregator Columns");
        jViewerColumnSettings.setName("jViewerColumnSettings"); // NOI18N
        jViewerColumnSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewerColumnSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jViewerColumnSettings);

        jPricerSettings.setText("Pricing Folder"); // NOI18N
        jPricerSettings.setName("jPricerSettings"); // NOI18N
        jPricerSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPricerSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jPricerSettings);

        jCurveSettings.setText("Curves");
        jCurveSettings.setName("jCurveSettings"); // NOI18N
        jCurveSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCurveSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jCurveSettings);

        jCloudingPublish.setText("Clouding Publish");
        jCloudingPublish.setName("jCloudingPublish"); // NOI18N
        jCloudingPublish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloudingPublishActionPerformed(evt);
            }
        });
        jSettings.add(jCloudingPublish);

        jMsAccessSettings.setText("MS Access");
        jMsAccessSettings.setName("jMsAccessSettings"); // NOI18N
        jMsAccessSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMsAccessSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jMsAccessSettings);

        jPBSettings.setText("PBIM"); // NOI18N
        jPBSettings.setName("jPBSettings"); // NOI18N
        jPBSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPBSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jPBSettings);

        jAIMSettings.setText("AIM"); // NOI18N
        jAIMSettings.setName("jAIMSettings"); // NOI18N
        jAIMSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAIMSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jAIMSettings);

        jYahooSettings.setText("Yahoo");
        jYahooSettings.setName("jYahooSettings"); // NOI18N
        jYahooSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jYahooSettingsActionPerformed(evt);
            }
        });
        jSettings.add(jYahooSettings);

        jBlockedBuddies.setText("Block Buddies");
        jBlockedBuddies.setName("jBlockedBuddies"); // NOI18N
        jBlockedBuddies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBlockedBuddiesActionPerformed(evt);
            }
        });
        jSettings.add(jBlockedBuddies);

        jMenuBar.add(jSettings);

        jOptions.setText("Layout");
        jOptions.setName("jOptions"); // NOI18N

        jFloatingWindow.setText("Floating Windows"); // NOI18N
        jFloatingWindow.setName("jFloatingWindow"); // NOI18N
        jFloatingWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFloatingWindowActionPerformed(evt);
            }
        });
        jOptions.add(jFloatingWindow);

        jDockedWindow.setText("Docked Windows"); // NOI18N
        jDockedWindow.setName("jDockedWindow"); // NOI18N
        jDockedWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDockedWindowActionPerformed(evt);
            }
        });
        jOptions.add(jDockedWindow);

        jFont1.setText("Aggregator Font"); // NOI18N
        jFont1.setName("jFont1"); // NOI18N
        jFont1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFont1ActionPerformed(evt);
            }
        });
        jOptions.add(jFont1);

        jColor1.setText("Aggregator Color");
        jColor1.setName("jColor1"); // NOI18N
        jColor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jColor1ActionPerformed(evt);
            }
        });
        jOptions.add(jColor1);

        jVerticalLayout.setText("Horizontal Layout"); // NOI18N
        jVerticalLayout.setName("jVerticalLayout"); // NOI18N
        jVerticalLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jVerticalLayoutActionPerformed(evt);
            }
        });
        jOptions.add(jVerticalLayout);

        jHorizontalLayout.setText("Vertical Layout"); // NOI18N
        jHorizontalLayout.setName("jHorizontalLayout"); // NOI18N
        jHorizontalLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHorizontalLayoutActionPerformed(evt);
            }
        });
        jOptions.add(jHorizontalLayout);

        jMenuBar.add(jOptions);

        jHelp.setText("Help"); // NOI18N
        jHelp.setName("jHelp"); // NOI18N

        jPointBoxHelp.setText("User Guide"); // NOI18N
        jPointBoxHelp.setName("jPointBoxHelp"); // NOI18N
        jPointBoxHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPointBoxHelpActionPerformed(evt);
            }
        });
        jHelp.add(jPointBoxHelp);

        jSaveWorkspaceMenuItem.setText("Save Workspace");
        jSaveWorkspaceMenuItem.setName("jSaveWorkspaceMenuItem"); // NOI18N
        jSaveWorkspaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveWorkspaceMenuItemActionPerformed(evt);
            }
        });
        jHelp.add(jSaveWorkspaceMenuItem);

        jMenuItem2.setText("Recover Workspace");
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jHelp.add(jMenuItem2);

        jCheckForUpdates.setText("Check for Updates ...");
        jCheckForUpdates.setName("jCheckForUpdates"); // NOI18N
        jCheckForUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckForUpdatesActionPerformed(evt);
            }
        });
        jHelp.add(jCheckForUpdates);

        jRollback.setText("Rollback Updates ...");
        jRollback.setName("jRollback"); // NOI18N
        jRollback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRollbackActionPerformed(evt);
            }
        });
        jHelp.add(jRollback);

        jAboutUs.setText("Contact Us"); // NOI18N
        jAboutUs.setName("jAboutUs"); // NOI18N
        jAboutUs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAboutUsActionPerformed(evt);
            }
        });
        jHelp.add(jAboutUs);

        jMenuBar.add(jHelp);

        jTest.setText("Tester");
        jTest.setName("jTest"); // NOI18N

        jPBCTest.setText("PBC Test");
        jPBCTest.setName("jPBCTest"); // NOI18N
        jPBCTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPBCTestActionPerformed(evt);
            }
        });
        jTest.add(jPBCTest);

        jMenuBar.add(jTest);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShutdownActionPerformed
        face.getKernel().shutdown(face.getPointBoxLoginUser(), true);
    }//GEN-LAST:event_jShutdownActionPerformed

    private void jShutdownBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShutdownBtnActionPerformed
        face.getKernel().shutdown(face.getPointBoxLoginUser(), true);
    }//GEN-LAST:event_jShutdownBtnActionPerformed

    private void jAIMBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAIMBtnActionPerformed
        face.displayConnectorLoginDialog(GatewayServerType.AIM_SERVER_TYPE);
    }//GEN-LAST:event_jAIMBtnActionPerformed

    private void jYIMBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jYIMBtnActionPerformed
        face.displayConnectorLoginDialog(GatewayServerType.YIM_SERVER_TYPE);
    }//GEN-LAST:event_jYIMBtnActionPerformed

    private void jConnectToAimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jConnectToAimActionPerformed
        face.displayConnectorLoginDialog(GatewayServerType.AIM_SERVER_TYPE);
    }//GEN-LAST:event_jConnectToAimActionPerformed

    private void jConnectToYahooActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jConnectToYahooActionPerformed
        face.displayConnectorLoginDialog(GatewayServerType.YIM_SERVER_TYPE);
    }//GEN-LAST:event_jConnectToYahooActionPerformed

    private void jDisconnectFromAIMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDisconnectFromAIMActionPerformed
        face.getLoginWindow(GatewayServerType.AIM_SERVER_TYPE);
    }//GEN-LAST:event_jDisconnectFromAIMActionPerformed

    private void jDisconnectFromYahooActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDisconnectFromYahooActionPerformed
        face.getLoginWindow(GatewayServerType.YIM_SERVER_TYPE);
    }//GEN-LAST:event_jDisconnectFromYahooActionPerformed

    private void jFCBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFCBtnActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayForwardCurveFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayForwardCurveFrame(selectedCode);
        }
    }//GEN-LAST:event_jFCBtnActionPerformed

    private void jFCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFCActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayForwardCurveFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayForwardCurveFrame(selectedCode);
        }
    }//GEN-LAST:event_jFCActionPerformed

    
    private void displayForwardCurveFrame(PointBoxQuoteCode code) {
        PricingForwardCurveFrame frame = new PricingForwardCurveFrame(face.getKernel(), code);
        frame.setState(Frame.NORMAL);
        frame.setLocationRelativeTo(this);
        frame.setIconImage(getImageSettings().getPointBoxIcon().getImage());
        frame.setVisible(true);
    }
    
    private void jVSBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVSBtnActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayVolSkewSurfaceFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayVolSkewSurfaceFrame(selectedCode);
        }
    }//GEN-LAST:event_jVSBtnActionPerformed

    private void jVSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVSActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayVolSkewSurfaceFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayVolSkewSurfaceFrame(selectedCode);
        }
    }//GEN-LAST:event_jVSActionPerformed

    private void displayVolSkewSurfaceFrame(PointBoxQuoteCode code) {
        PricingVolSkewSurfaceFrame frame = new PricingVolSkewSurfaceFrame(face.getKernel(), code);
        frame.setState(Frame.NORMAL);
        frame.setLocationRelativeTo(this);
        frame.setIconImage(getImageSettings().getPointBoxIcon().getImage());
        frame.setVisible(true);
    }

    private void jIRBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jIRBtnActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayInterestRateCurveFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayInterestRateCurveFrame(selectedCode);
        }
    }//GEN-LAST:event_jIRBtnActionPerformed

    private void jIRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jIRActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayInterestRateCurveFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayInterestRateCurveFrame(selectedCode);
        }
    }//GEN-LAST:event_jIRActionPerformed

    private void displayInterestRateCurveFrame(PointBoxQuoteCode code) {
        PricingInterestRateFrame frame = new PricingInterestRateFrame(face.getKernel(), code);
        frame.setState(Frame.NORMAL);
        frame.setLocationRelativeTo(this);
        frame.setIconImage(getImageSettings().getPointBoxIcon().getImage());
        frame.setVisible(true);
    }
    
    private void jCompBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCompBtnActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayCompFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayCompFrame(selectedCode);
        }
    }//GEN-LAST:event_jCompBtnActionPerformed

    private void displayCompFrame(PointBoxQuoteCode code) {
        PricingCompFrame frame = new PricingCompFrame(face.getKernel(), code);
        frame.setState(Frame.NORMAL);
        frame.setLocationRelativeTo(this);
        frame.setIconImage(getImageSettings().getPointBoxIcon().getImage());
        frame.setVisible(true);
    }
    
    private void jOsimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOsimActionPerformed
        displayOptionPricerFrame();
    }//GEN-LAST:event_jOsimActionPerformed

    public IPbcStructuredQuoteBuilder getPbcStructuredQuoteBuilder(PointBoxQuoteType category){
        return simFrameMap.get(category);
    }
    
    void displaySimPricerWithQuote(IPbsysOptionQuoteWrapper targetQuoteWrapper, PointBoxQuoteType category) {
        IPbcStructuredQuoteBuilder builder = getPbcStructuredQuoteBuilder(category);
        if (builder == null){
            SwingGlobal.displayMessageDialog(this, "This operation is not supported.");
        }else{
            if (builder.populateQuoteFromViewer(targetQuoteWrapper)){
                builder.displaySimGUI();
            }
        }
    }
    
    public void displayOptionPricerFrame() {
        IPbcStructuredQuoteBuilder builder = getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION);
        if (builder == null){
            SwingGlobal.displayMessageDialog(this, "This operation is not supported.");
        }else{
            builder.displaySimGUI();
        }
    }
    
    private void jVorHBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVorHBtnActionPerformed
        if (ishLayout()){
            getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Vertical);
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);     //make sure it is still the Docked mode
            switchDockingFrameLayout(false);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }else{
            getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Horizontal);
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);     //make sure it is still the Docked mode
            switchDockingFrameLayout(true);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }
    }//GEN-LAST:event_jVorHBtnActionPerformed

    private void jVerticalLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVerticalLayoutActionPerformed
        if (ishLayout()){
            getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Vertical);
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);   //make sure it is still the Docked mode
            switchDockingFrameLayout(false);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }
    }//GEN-LAST:event_jVerticalLayoutActionPerformed

    private void jHorizontalLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jHorizontalLayoutActionPerformed
        if (!ishLayout()){
            getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Horizontal);
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);
            switchDockingFrameLayout(true);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }
    }//GEN-LAST:event_jHorizontalLayoutActionPerformed

    private void jForDBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jForDBtnActionPerformed
        if (isFloatingStyle()){
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);
            getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Horizontal);
            switchFrameStyle(false);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }else{
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Floating);
            switchFrameStyle(true);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }
    }//GEN-LAST:event_jForDBtnActionPerformed

    private void jFloatingWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFloatingWindowActionPerformed
        if (!isFloatingStyle()){
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Floating);
            switchFrameStyle(true);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }
    }//GEN-LAST:event_jFloatingWindowActionPerformed

    private void jDockedWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDockedWindowActionPerformed
        if (isFloatingStyle()){
            getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);
            getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Horizontal);       //add default layout for docked style
            switchFrameStyle(false);
//            systemFrameSettings.fireSettingsUpdated(this, new SettingsEvent(systemFrameRecord));
        }
    }//GEN-LAST:event_jDockedWindowActionPerformed

    private void jColorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jColorBtnActionPerformed
        displayColorDialogHelper();
    }//GEN-LAST:event_jColorBtnActionPerformed

    private void displayColorDialogHelper(){
        Color color = JColorChooser.showDialog(this,
                        "Color chooser", new Color(23, 45, 200));
        if (color != null){
            face.getKernel().getPointBoxConsoleRuntime().setGeneralColorForAllPbcViewerSettings(color);
        }
    }

    private void jFontBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFontBtnActionPerformed
        displayFontDialogHelper();
    }//GEN-LAST:event_jFontBtnActionPerformed

    private void displayFontDialogHelper(){
        Font font = JFontChooser.showDialog(this,
                                            "Font Chooser",
                                            SwingGlobal.getLabelFont());
        if (font != null){
            face.getKernel().getPointBoxConsoleRuntime().setGeneralFontForAllPbcViewerSettings(font);
        }
    }

    private PreferenceDialog generalSettingsDialog;
    private PreferenceDialog getGeneralSettingsDialog(){
        if (generalSettingsDialog == null){
            generalSettingsDialog = new GeneralSettingsDialog(face);
        }
        return generalSettingsDialog;
    }
    
    private PreferenceDialog blockedBuddiesSettingsDialog;
    private PreferenceDialog getBlockedBuddiesSettingsDialog(){
        if (blockedBuddiesSettingsDialog == null){
            blockedBuddiesSettingsDialog = new BlockedBuddiesSettingsDialog(face);
        }
        return blockedBuddiesSettingsDialog;
    }
    
    private PreferenceDialog msAccessSettingsDialog;
    private PreferenceDialog getMsAccessSettingsDialog(){
        if (msAccessSettingsDialog == null){
            msAccessSettingsDialog = new MsAccessSettingsDialog(face);
        }
        return msAccessSettingsDialog;
    }
    
    private PreferenceDialog cloudingPublishSettingsDialog;
    private PreferenceDialog getCloudingPublishSettingsDialog(){
        if (cloudingPublishSettingsDialog == null){
            cloudingPublishSettingsDialog = new CloudingPublishSettingsDialog(face);
        }
        return cloudingPublishSettingsDialog;
    }
    
    private PreferenceDialog curveSettingsDialog;
    /**
     * This method can be used after user-login
     * @return 
     */
    private PreferenceDialog getCurveSettingsDialog(){
        if (curveSettingsDialog == null){
            curveSettingsDialog = new CurveSettingsDialog(face);
        }
        return curveSettingsDialog;
    }
    
    private PreferenceDialog viewerFontColorSettingsDialog;
    private PreferenceDialog getViewerFontColorSettingsDialog(){
        if (viewerFontColorSettingsDialog == null){
            viewerFontColorSettingsDialog = new ViewerFontColorSettingsDialog(face);
        }
        return viewerFontColorSettingsDialog;
    }
    
    private PreferenceDialog viewerColumnSettingsDialog;
    private PreferenceDialog getViewerColumnSettingsDialog(){
        if (viewerColumnSettingsDialog == null){
            viewerColumnSettingsDialog = new ViewerColumnSettingsDialog(face);
        }
        return viewerColumnSettingsDialog;
    }
    
    private PreferenceDialog pricerSettingsDialog;
    private PreferenceDialog getPricerSettingsDialog(){
        if (pricerSettingsDialog == null){
            pricerSettingsDialog = new PricerSettingsDialog(face);
        }
        return pricerSettingsDialog;
    }

    private PreferenceDialog aimSettingsDialog;
    private PreferenceDialog getAimSettingsDialog() {
        if (aimSettingsDialog == null){
            aimSettingsDialog = new MessagingServerSettingsDialog(face, PreferencePanelType.AimServerSettingsPanel);
        }
        return aimSettingsDialog;
    }

    private PreferenceDialog yahooSettingsDialog;
    private PreferenceDialog getYahooSettingsDialog() {
        if (yahooSettingsDialog == null){
            yahooSettingsDialog = new MessagingServerSettingsDialog(face, PreferencePanelType.YahooServerSettingsPanel);
        }
        return yahooSettingsDialog;
    }

    private PreferenceDialog pbimSettingsDialog;
    private PreferenceDialog getPbimSettingsDialog() {
        if (pbimSettingsDialog == null){
            pbimSettingsDialog = new MessagingServerSettingsDialog(face, PreferencePanelType.PbimServerSettingsPanel);
        }
        return pbimSettingsDialog;
    }

    void notifyMsAccessInterrupted() {
        ((MsAccessSettingsDialog)getMsAccessSettingsDialog()).notifyMsAccessInterrupted();
    }
    
    private void jGeneralSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGeneralSettingsActionPerformed
        getGeneralSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jGeneralSettingsActionPerformed

    private void jAIMSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAIMSettingsActionPerformed
        getAimSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jAIMSettingsActionPerformed

    private void jPricerSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPricerSettingsActionPerformed
        getPricerSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jPricerSettingsActionPerformed

    private void jFilterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFilterBtnActionPerformed
        face.displayFilterViewer(true, true);
    }//GEN-LAST:event_jFilterBtnActionPerformed

    private void jSearchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSearchBtnActionPerformed
        face.displaySearchViewer(true);
    }//GEN-LAST:event_jSearchBtnActionPerformed

    private void jClearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jClearBtnActionPerformed
        clearViewerTabs();
    }//GEN-LAST:event_jClearBtnActionPerformed

    public void clearViewerTabs(){
        if(SwingUtilities.isEventDispatchThread()){
            clearViewerTabsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    clearViewerTabsHelper(); 
                }
            });
        }
    }
    
    private void clearViewerTabsHelper(){
        JPanel panel=new JPanel();
        panel.setLayout(new BorderLayout());
        JRadioButton keepTodayMessages = new JRadioButton("Keep Today's Messages");
        JRadioButton clearAllMessages = new JRadioButton("Clear All Messages");
        ButtonGroup btnGroup=new ButtonGroup();
        btnGroup.add(keepTodayMessages);
        btnGroup.add(clearAllMessages);
        panel.add(keepTodayMessages,BorderLayout.NORTH);
        panel.add (clearAllMessages,BorderLayout.SOUTH);
        keepTodayMessages.setSelected(true);
      
        int answer = JOptionPane.showConfirmDialog(this,panel,
                        "Confirmation", JOptionPane.OK_CANCEL_OPTION);
        if (answer == JOptionPane.OK_OPTION){
            jClearBtn.setEnabled(false);
            if(keepTodayMessages.isSelected()){
                (new SwingWorker<Void, Void>(){
                    @Override
                    protected Void doInBackground() throws Exception {
                        face.keepTodayData();
                        return null;
                    }
                    @Override
                    protected void done() {
                        jClearBtn.setEnabled(true);
                    }
                }).execute();
            }else{
                (new SwingWorker<Void, Void>(){
                    @Override
                    protected Void doInBackground() throws Exception {
                        face.clearViewer();
                        return null;
                    }
                    @Override
                    protected void done() {
                        jClearBtn.setEnabled(true);
                    }
                }).execute();
            }
        }
    }

    private void jAboutUsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAboutUsActionPerformed
        aboutDialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(this, aboutDialog));
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_jAboutUsActionPerformed

    private void jCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCompActionPerformed
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName());
        if (selectedCode == null){
            displayCompFrame(getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).getSelectedPointBoxQuoteCode());
        }else{
            displayCompFrame(selectedCode);
        }
    }//GEN-LAST:event_jCompActionPerformed

    private void jAllMessagesTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAllMessagesTabActionPerformed
        face.activateViewerTab(ViewerTableType.ALL_MESSAGES.toString());
    }//GEN-LAST:event_jAllMessagesTabActionPerformed

    private void jAllQuotesTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAllQuotesTabActionPerformed
        face.activateViewerTab(ViewerTableType.ALL_QUOTES.toString());
    }//GEN-LAST:event_jAllQuotesTabActionPerformed

    private void jOutgoingTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOutgoingTabActionPerformed
        face.activateViewerTab(ViewerTableType.OUTGOING_MESSAGES.toString());
    }//GEN-LAST:event_jOutgoingTabActionPerformed

    private void jIncomingTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jIncomingTabActionPerformed
        face.activateViewerTab(ViewerTableType.INCOMING_MESSAGES.toString());
    }//GEN-LAST:event_jIncomingTabActionPerformed

    private void jClearAllSheetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jClearAllSheetsActionPerformed
        clearViewerTabs();
    }//GEN-LAST:event_jClearAllSheetsActionPerformed

    private void jFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFilterActionPerformed
        face.displayFilterViewer(true, true);
    }//GEN-LAST:event_jFilterActionPerformed

    private void jSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSearchActionPerformed
        face.displaySearchViewer(true);
    }//GEN-LAST:event_jSearchActionPerformed

    private void jYahooSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jYahooSettingsActionPerformed
        getYahooSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jYahooSettingsActionPerformed

    private void jPointBoxHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPointBoxHelpActionPerformed
        HelpBrowser browers = new HelpBrowser(String.valueOf(getClass().getResource("resources/PBHelp.htm")));
    }//GEN-LAST:event_jPointBoxHelpActionPerformed

    private void jWeatherBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jWeatherBtnActionPerformed
        Image frameIconImage =  getImageSettings().getPointBoxRedIcon().getImage();
        IEmsWeatherReport iEWR = EmsWeatherReportFactory.getIEmsWeatherReport(frameIconImage);
        iEWR.setVisible(true);
        iEWR.setLocation(this.getLocation());
        iEWR.startTimer();
    }//GEN-LAST:event_jWeatherBtnActionPerformed

    //private LocalProxySettingsDialog proxySettingsDialog = new LocalProxySettingsDialog(this.getBaseFrame(), true);
    
    private void jSaveAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveAllBtnActionPerformed
        //pbcArchiveDialog.setVisible(true);
        this.saveAllWorkSpaceHelper();
    }//GEN-LAST:event_jSaveAllBtnActionPerformed

    private void jSoundBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSoundBtnActionPerformed
        String sound = (String)getSoundSettings().get("enableSound");
        if(sound.equals("true")){
            jSoundBtn.setIcon(getImageSettings().getSoundOnIcon());
            getSoundSettings().put("enableSound", "false");
        }
        else{
            jSoundBtn.setIcon(getImageSettings().getSoundOffIcon());
            getSoundSettings().put("enableSound", "true");
        }
    }//GEN-LAST:event_jSoundBtnActionPerformed

    private void jCheckForUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckForUpdatesActionPerformed
        checkOnlineUpdateClicked();
    }//GEN-LAST:event_jCheckForUpdatesActionPerformed

    private void checkOnlineUpdateClicked(){
        (new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                PbcReleaseInformation releaseInfo = face.checkPbcRelease();
                face.displayPbcReleaseUpdateDialog(releaseInfo);
                if (flashOnlineUpdateThread != null){
                    flashOnlineUpdateThread.interrupt();
                    flashOnlineUpdateThread = null;
                }
                return null;
            }
        }).execute();
    }
    
    private void jOnlineUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOnlineUpdateActionPerformed
        checkOnlineUpdateClicked();
    }//GEN-LAST:event_jOnlineUpdateActionPerformed

    private void jRollbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRollbackActionPerformed
        checkOnlineRollbackClicked();
    }//GEN-LAST:event_jRollbackActionPerformed

    private void jPBPricerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPBPricerActionPerformed
        switchToPBPricer();
    }//GEN-LAST:event_jPBPricerActionPerformed
    
    private void jWTPricerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jWTPricerActionPerformed
        switchToWTPricer();
    }//GEN-LAST:event_jWTPricerActionPerformed

    private void jWTConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jWTConnectActionPerformed
        jWTConnect.setText("Connect...");
        (new SwingWorker<Boolean, Void>(){
            @Override
            protected Boolean doInBackground() throws Exception {
                return face.getKernel().connectToLocalWhenTech();
            }

            @Override
            protected void done() {
                try {
                    jWTConnect.setText("WhenTech");
                    if (get()){
                        jWTConnect.setVisible(false);
                        jWTPricer.setSelected(true);
                        jWTPricer.setVisible(true);
                        switchToWTPricer();
                    }else{
                        JOptionPane.showMessageDialog(PointBoxFrame.this, "Cannot find or connect to a local running WhenTech application.");
                    }
                } catch (InterruptedException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                } catch (ExecutionException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            }
        }).execute();
    }//GEN-LAST:event_jWTConnectActionPerformed

    private void jViewerScrollingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jViewerScrollingActionPerformed
        if (jViewerScrolling.getActionCommand().equalsIgnoreCase("DisableViewerScrolling")){
            IViewerTablePanel viewerTab = face.getCurrentViewerTablePanel();
            viewerTab.disableAutomaticallyScrollingViewer();
            jViewerScrolling.setActionCommand("EnableViewerScrolling");
            jViewerScrolling.setToolTipText("Enable current aggregator scrolling");
            jViewerScrolling.setIcon(getImageSettings().getViewerScrollingIcon());
        }else if (jViewerScrolling.getActionCommand().equalsIgnoreCase("EnableViewerScrolling")){
            IViewerTablePanel viewerTab = face.getCurrentViewerTablePanel();
            viewerTab.enableAutomaticallyScrollingViewer();
            jViewerScrolling.setActionCommand("DisableViewerScrolling");
            jViewerScrolling.setToolTipText("Disable current aggregator scrolling");
            jViewerScrolling.setIcon(getImageSettings().getViewerFreezingIcon());
        }
    }//GEN-LAST:event_jViewerScrollingActionPerformed
    
    private void jPBCTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPBCTestActionPerformed
        face.displayTesterDialog();
    }//GEN-LAST:event_jPBCTestActionPerformed

    private void jLoaderItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoaderItemActionPerformed
        pbcReloadingDialog.setVisible(true);
    }//GEN-LAST:event_jLoaderItemActionPerformed

    private void jPricerSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPricerSettingActionPerformed
       if(face.getKernel().isServerPricer()){
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure to change to local pricer?",
                    "Confirm", JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION) 
            {
                PbcPricerType oldType = PointBoxConsoleProperties.getSingleton().retrievePbcPricerType(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
                face.getKernel().raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                            oldType, 
                                                                            PbcPricerType.convertToClientType(oldType),
                                                                            PointBoxConsoleProperties.getSingleton(),
                                                                            face.getKernel()));
                resetPricerLocationHelperinEDT(false);
            }           
       }else{
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure to change to server pricer?",
                    "Confirm", JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION) 
            {
                PbcPricerType oldType = PointBoxConsoleProperties.getSingleton().retrievePbcPricerType(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
                face.getKernel().raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                            oldType, 
                                                                            PbcPricerType.convertToServerType(oldType),
                                                                            PointBoxConsoleProperties.getSingleton(),
                                                                            face.getKernel()));
                resetPricerLocationHelperinEDT(true);
            }           
       }
    }//GEN-LAST:event_jPricerSettingActionPerformed

    private void jLocalPricerMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLocalPricerMenuActionPerformed
            if (JOptionPane.showConfirmDialog(this,
                "Are you sure to change to local pricer?",
                "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) 
            {
                PbcPricerType oldType = PointBoxConsoleProperties.getSingleton().retrievePbcPricerType(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
                face.getKernel().raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                            oldType, 
                                                                            PbcPricerType.convertToClientType(oldType),
                                                                            PointBoxConsoleProperties.getSingleton(),
                                                                            face.getKernel()));
                resetPricerLocationHelperinEDT(false);
                jLocalPricerMenu.setEnabled(false);
                jServerPricerMenu.setEnabled(true);                
            }           
    }//GEN-LAST:event_jLocalPricerMenuActionPerformed

    private void jServerPricerMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jServerPricerMenuActionPerformed
            if (JOptionPane.showConfirmDialog(this,
                "Are you sure to change to server pricer?",
                "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) 
            {
                PbcPricerType oldType = PointBoxConsoleProperties.getSingleton().retrievePbcPricerType(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
                face.getKernel().raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                            oldType, 
                                                                            PbcPricerType.convertToServerType(oldType),
                                                                            PointBoxConsoleProperties.getSingleton(),
                                                                            face.getKernel()));
                resetPricerLocationHelperinEDT(true);
                jLocalPricerMenu.setEnabled(true);
                jServerPricerMenu.setEnabled(false);                   
            }
    }//GEN-LAST:event_jServerPricerMenuActionPerformed

    private void jCPConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCPConnectActionPerformed
        if(!face.displayClearPortMainFrame()){   //display failed so displaySimGUI the login frame.
            face.displayClearPortLoginFrame(face, getImageSettings());
        }
    }//GEN-LAST:event_jCPConnectActionPerformed

    private void jPsDnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPsDnActionPerformed
        /**
         * todo-sim: it should look at check-boxes for each code before upload files
         */
        if (JOptionPane.showConfirmDialog(this, "Do you want to download curve files of every PointBox quote code?", "Confirm:", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            this.getCurveSettingsDialog().downloadPricingRuntimeCurveFiles(true);
        }
    }//GEN-LAST:event_jPsDnActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        pbcArchiveDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jFont1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFont1ActionPerformed
        displayFontDialogHelper();
    }//GEN-LAST:event_jFont1ActionPerformed

    private void jPBSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPBSettingsActionPerformed
        getPbimSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jPBSettingsActionPerformed

    private void jColor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jColor1ActionPerformed
        displayColorDialogHelper();
    }//GEN-LAST:event_jColor1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if (JOptionPane.showConfirmDialog(this, 
                "This operation will use a previous backup settings to overwrite your current settings and you need restart this program. "
                + "Do you really want to do it right now?", 
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            if (PointBoxConsoleProperties.getSingleton().rollbackPointBoxConsoleProperties(null)){
                JOptionPane.showMessageDialog(this, "Your settings is successfully recovered from the previous settings. "
                        + "Please restart PointBoxConsole to have it.");
            }else{
                JOptionPane.showMessageDialog(this, "Your settings cannot be recovered from any previous settings which may not exist.");
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jPBIMBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPBIMBtnActionPerformed
        face.clickPricerBuddy();
    }//GEN-LAST:event_jPBIMBtnActionPerformed

    private void saveAllWorkSpaceHelper(){
        jSaveWorkspaceMenuItem.setEnabled(false);
        jSaveAllBtn.setEnabled(false);
        (new SwingWorker<Void, Void>(){
        @Override
        protected Void doInBackground() throws Exception {
            face.getKernel().requestToSaveCurrentPbcSettings();
            Thread.sleep(1500);
            return null;
        }

        @Override
        protected void done() {
            jSaveWorkspaceMenuItem.setEnabled(true);
            jSaveAllBtn.setEnabled(true);
        }
    }).execute();
    }
    
    private void jSaveWorkspaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveWorkspaceMenuItemActionPerformed
        saveAllWorkSpaceHelper();
    }//GEN-LAST:event_jSaveWorkspaceMenuItemActionPerformed

    String uploadAllEmsCurves(){
        if (!face.getKernel().isPbcPricingAdmin()){
            return "This account is not authorized to upload files";
        }
        HashMap<String, PbcPricingModel> aPbcPricingModelMap = face.getKernel().getPointBoxConsoleRuntime().getPbcPricingModelMap();
        Collection<PbcPricingModel> aPbcPricingModelCollection = aPbcPricingModelMap.values();
        PointBoxQuoteCode code;
        PointBoxCurveType type;
        String filePath;
        PricingCurveFileSettings[] aPricingCurveFileSettingsArray;
        String status = null;
        for (PbcPricingModel aPbcPricingModel : aPbcPricingModelCollection){
            aPricingCurveFileSettingsArray = aPbcPricingModel.getPricingCurveFileSettingsArray();
            if (aPricingCurveFileSettingsArray != null){
                code = PointBoxQuoteCode.convertEnumNameToType(aPbcPricingModel.getSqCode());
                for (PricingCurveFileSettings aPricingCurveFileSettings : aPricingCurveFileSettingsArray){
                    type = PointBoxCurveType.convertEnumValueToType(aPricingCurveFileSettings.getCurveType());
                    filePath = face.getKernel().getLocalCurveFileFullPath(code, aPricingCurveFileSettings, true);
                    status = face.getKernel().uploadPricingRuntimeSettingsFilesToPointBoxServer(code, 
                                                                                                type, 
                                                                                                filePath);
                    if (DataGlobal.isNonEmptyNullString(status)){
                        break;
                    }
                }//for
            }
            if (DataGlobal.isNonEmptyNullString(status)){
                break;
            }
        }//for
        if (DataGlobal.isEmptyNullString(status)){
            return "Complete uploading all the curve files.";
        }else{
            return status;
        }
    }
    
    private void jUploadBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUploadBtnActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Upload curve files onto PointBox server now?", "Confirm:", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
            return;
        }
        (new SwingWorker<String, Void>(){
            @Override
            protected String doInBackground() throws Exception {
                return uploadAllEmsCurves();
            }

            @Override
            protected void done() {
                try {
                    JOptionPane.showMessageDialog(PointBoxFrame.this, get());
                } catch (InterruptedException ex) {
                    Logger.getLogger(PointBoxFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(PointBoxFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).execute();
        //        final CloudingPublishSettingsDialog aPreferenceDialog = (CloudingPublishSettingsDialog)getCloudingPublishSettingsDialog();
//        final IPbcKernel kernel = face.getKernel();
//        final ArrayList<PointBoxCurveType> selectedCurveTypes = aPreferenceDialog.getSelectedFileTypesForPricingSettingsUploadAdmin();  
//        final ArrayList<ArrayList<Object>> filePathObjects = new ArrayList<ArrayList<Object>>();
//        
//        final ArrayList<PointBoxQuoteCode> aPointBoxQuoteCodeList = kernel.getPointBoxConsoleRuntime().getPointBoxQuoteCodeList();
//        if ((aPointBoxQuoteCodeList == null) || (aPointBoxQuoteCodeList.isEmpty())){
//            JOptionPane.showMessageDialog(PointBoxFrame.this, "Current commodity has no upload service support from PointBox system yet.");
//            return;
//        }
//        IPointBoxPricingSettings pricingSettings = kernel.getPointBoxConsoleRuntime().getPointBoxPricingSettings();
//        if (pricingSettings == null){
//            JOptionPane.showMessageDialog(PointBoxFrame.this, "Please set up your pricing runtime settings from Preferences window.");
//            return;
//        }
//        //validate file paths....
//        List<PointBoxCurveType> types;
//        ArrayList<Object> filePathObjectArrayList;
//        String statusMsg = null;
//        String filePath;
//        String uploadedPricingFileNameList = "";
//        for (PointBoxQuoteCode aPointBoxQuoteCode : aPointBoxQuoteCodeList){
//            if ((selectedCurveTypes == null) || (selectedCurveTypes.isEmpty())){
//                types = PointBoxCurveType.getStandardPricingSettingsTypes(false);
//            }else{
//                types = selectedCurveTypes;
//            }
//            for (PointBoxCurveType type : types){
//                filePath = pricingSettings.retrievePricingSettingFilePath(aPointBoxQuoteCode.toString(), type);
//                filePathObjectArrayList = new ArrayList<Object>();
//                filePathObjectArrayList.add(aPointBoxQuoteCode);
//                filePathObjectArrayList.add(type);
//                filePathObjectArrayList.add(filePath);
//                if (NIOGlobal.isValidFile(filePath)){
//                    uploadedPricingFileNameList += (new File(filePath)).getName() + NIOGlobal.lineSeparator();
//                    filePathObjects.add(filePathObjectArrayList);
//                }else{
//                    filePathObjectArrayList.remove(aPointBoxQuoteCode);
//                    statusMsg = "Cannot find a valid " + type + " file for " + aPointBoxQuoteCode;
//                    break;
//                }
//            }//for
//            if (statusMsg != null){
//                filePathObjects.clear();
//                break;
//            }
//        }//for
//        if (statusMsg != null){
//            JOptionPane.showMessageDialog(PointBoxFrame.this, statusMsg);
//            return;
//        }      
//        if (JOptionPane.showConfirmDialog(this, "Upload the following pricing runtime curve files to PointBox server?" 
//                + NIOGlobal.lineSeparator() + NIOGlobal.lineSeparator() + uploadedPricingFileNameList
//                + NIOGlobal.lineSeparator() + "If they are not correct, you may always reset them up from Preference.", 
//                "Confirm:", 
//                JOptionPane.YES_NO_OPTION)
//                == JOptionPane.YES_OPTION)
//       {
//            (new SwingWorker<String, Void>(){
//                
//                @Override
//                protected String doInBackground() throws Exception {
//                    return face.getKernel().uploadPricingRuntimeSettingsFilesToPointBoxServers(filePathObjects);
//                }
//
//                @Override
//                protected void done() {
//                    try {
//                        String result = get();
//                        if (result == null){
//                            result = "Your pricing runtime settings has been successfuly uploaded.";
//                        }
//                        JOptionPane.showMessageDialog(PointBoxFrame.this, result);
//                    } catch (InterruptedException ex) {
//                        PointBoxTracer.recordSevereException(logger, ex);
//                    } catch (ExecutionException ex) {
//                        PointBoxTracer.recordSevereException(logger, ex);
//                    }
//                }
//            }).execute();
//        }
                }//GEN-LAST:event_jUploadBtnActionPerformed

    private void jOsimBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOsimBtnActionPerformed
        displayOptionPricerFrame();
    }//GEN-LAST:event_jOsimBtnActionPerformed

    private void jViewerFontColorSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jViewerFontColorSettingsActionPerformed
        getViewerFontColorSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jViewerFontColorSettingsActionPerformed

    private void jCurveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCurveSettingsActionPerformed
        getCurveSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jCurveSettingsActionPerformed

    private void jCloudingPublishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloudingPublishActionPerformed
        getCloudingPublishSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jCloudingPublishActionPerformed

    private void jMsAccessSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMsAccessSettingsActionPerformed
        getMsAccessSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jMsAccessSettingsActionPerformed

    private void jBlockedBuddiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBlockedBuddiesActionPerformed
        getBlockedBuddiesSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jBlockedBuddiesActionPerformed

    private void jLegacyUploadBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLegacyUploadBtnActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Upload legacy LN curve files onto PointBox servers (PRODUCTION) now?", 
                                        "Confirm:", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
        {
            return;
        }
        (new SwingWorker<String, Void>(){
            @Override
            protected String doInBackground() throws Exception {
                return uploadLegacyEmsCurves();
            }

            @Override
            protected void done() {
                try {
                    JOptionPane.showMessageDialog(PointBoxFrame.this, get());
                } catch (InterruptedException ex) {
                    Logger.getLogger(PointBoxFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(PointBoxFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).execute();
    }//GEN-LAST:event_jLegacyUploadBtnActionPerformed

    private void jViewerColumnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jViewerColumnSettingsActionPerformed
        getViewerColumnSettingsDialog().displayPreferenceDialog();
    }//GEN-LAST:event_jViewerColumnSettingsActionPerformed
    
    String uploadLegacyEmsCurves() {
        if (!face.getKernel().isPbcPricingAdmin()){
            return "This account is not authorized to upload files";
        }
        String symbol_LN = "LN";
        String status = face.getKernel().uploadLegancyLnCurveFilesToPointBoxServer(symbol_LN, "Underlier");
        if (DataGlobal.isEmptyNullString(status)){
            status = face.getKernel().uploadLegancyLnCurveFilesToPointBoxServer(symbol_LN, "InterestRate");
        }
        if (DataGlobal.isEmptyNullString(status)){
            status = face.getKernel().uploadLegancyLnCurveFilesToPointBoxServer(symbol_LN, "AtmVolCurve");
        }
        if (DataGlobal.isEmptyNullString(status)){
            status = face.getKernel().uploadLegancyLnCurveFilesToPointBoxServer(symbol_LN, "VolSkewSurface");
        }
        if (DataGlobal.isEmptyNullString(status)){
            status = face.getKernel().uploadLegancyLnCurveFilesToPointBoxServer(symbol_LN, "Expirations");
        }
        if (DataGlobal.isEmptyNullString(status)){
            status = face.getKernel().uploadLegancyLnCurveFilesToPointBoxServer(symbol_LN, "Holidays");
        }

        if (DataGlobal.isEmptyNullString(status)){
            return "Complete uploading all the legacy LN curve files.";
        }else{
            return status;
        }
    }
    
    private void switchPricerTextColorHelper(){
        if (SwingUtilities.isEventDispatchThread()){
            if (jWTPricer.isSelected()){
                jPBPricer.setForeground(Color.black);
                jWTPricer.setForeground(Color.red);
            }else{
                jPBPricer.setForeground(Color.red);
                jWTPricer.setForeground(Color.black);
            }
        }
    }
    
    private void checkOnlineRollbackClicked(){
        (new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                PbcReleaseInformation releaseInfo = face.checkPbcRelease();
                face.displayPbcReleaseRollbackDialog(releaseInfo);
                return null;
            }
        }).execute();
    }

     /**
     * @return the messagingFloatingFrame
     */
    public JFrame getMessagingFloatingFrame() {
        return messagingFloatingFrame;                  //add setters for persistency of floating frame's position and location
    }

    /**
     * @return the buddyListFloatingFrame
     */
    public JFrame getBuddyListFloatingFrame() {
        return buddyListFloatingFrame;
    }
    
    
    //update the sound components on the PBC frame.
    public void updateSoundSettings(){
        String sound = (String)getSoundSettings().get("enableSound");
        if(sound.equals("false")){
            jSoundBtn.setIcon(getImageSettings().getSoundOnIcon());
            getSoundSettings().put("enableSound", "false");
        }
        else{
            jSoundBtn.setIcon(getImageSettings().getSoundOffIcon());
            getSoundSettings().put("enableSound", "true");
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAIMBtn;
    private javax.swing.JMenuItem jAIMSettings;
    private javax.swing.JMenuItem jAboutUs;
    private javax.swing.JMenuItem jAllMessagesTab;
    private javax.swing.JMenuItem jAllQuotesTab;
    private javax.swing.JMenuItem jBlockedBuddies;
    private javax.swing.JButton jCPConnect;
    private javax.swing.JMenuItem jCheckForUpdates;
    private javax.swing.JMenuItem jClearAllSheets;
    private javax.swing.JButton jClearBtn;
    private javax.swing.JLabel jClock;
    private javax.swing.JMenuItem jCloudingPublish;
    private javax.swing.JMenuItem jColor1;
    private javax.swing.JButton jColorBtn;
    private javax.swing.JMenuItem jComp;
    private javax.swing.JButton jCompBtn;
    private javax.swing.JMenuItem jConnectToAim;
    private javax.swing.JMenuItem jConnectToYahoo;
    private javax.swing.JPanel jContentPanel;
    private javax.swing.JMenuItem jCurveSettings;
    private javax.swing.JMenuItem jDisconnectFromAIM;
    private javax.swing.JMenuItem jDisconnectFromYahoo;
    private javax.swing.JMenuItem jDockedWindow;
    private javax.swing.JMenuItem jFC;
    private javax.swing.JButton jFCBtn;
    private javax.swing.JMenuItem jFilter;
    private javax.swing.JButton jFilterBtn;
    private javax.swing.JMenuItem jFloatingWindow;
    private javax.swing.JMenuItem jFont1;
    private javax.swing.JButton jFontBtn;
    private javax.swing.JButton jForDBtn;
    private javax.swing.JMenu jGateway;
    private javax.swing.JMenuItem jGeneralSettings;
    private javax.swing.JMenu jHelp;
    private javax.swing.JMenuItem jHorizontalLayout;
    private javax.swing.JMenuItem jIR;
    private javax.swing.JButton jIRBtn;
    private javax.swing.JMenuItem jIncomingTab;
    private javax.swing.JButton jLegacyUploadBtn;
    private javax.swing.JMenuItem jLoaderItem;
    private javax.swing.JMenuItem jLocalPricerMenu;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMsAccessSettings;
    private javax.swing.JToolBar.Separator jMsgSep;
    private javax.swing.JButton jOnlineUpdate;
    private javax.swing.JMenu jOptions;
    private javax.swing.JMenuItem jOsim;
    private javax.swing.JButton jOsimBtn;
    private javax.swing.JMenuItem jOutgoingTab;
    private javax.swing.JMenuItem jPBCTest;
    private javax.swing.JButton jPBIMBtn;
    private javax.swing.JRadioButton jPBPricer;
    private javax.swing.JMenuItem jPBSettings;
    private javax.swing.JMenuItem jPointBoxHelp;
    private javax.swing.ButtonGroup jPricerGroup;
    private javax.swing.JButton jPricerSetting;
    private javax.swing.JMenuItem jPricerSettings;
    private javax.swing.JMenu jPricing;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JButton jPsDn;
    private javax.swing.JMenuItem jRollback;
    private javax.swing.JButton jSaveAllBtn;
    private javax.swing.JMenuItem jSaveWorkspaceMenuItem;
    private javax.swing.JMenuItem jSearch;
    private javax.swing.JButton jSearchBtn;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JMenuItem jServerPricerMenu;
    private javax.swing.JMenu jSettings;
    private javax.swing.JMenuItem jShutdown;
    private javax.swing.JButton jShutdownBtn;
    private javax.swing.JButton jSoundBtn;
    private javax.swing.JToolBar jStatusBar;
    private javax.swing.JButton jStatusBtn;
    private javax.swing.JLabel jStatusMessage;
    private javax.swing.JLabel jTalker;
    private javax.swing.JMenu jTest;
    private javax.swing.JToolBar jToolBarH;
    private javax.swing.JToolBar jToolBarV;
    private javax.swing.JButton jUploadBtn;
    private javax.swing.JMenuItem jVS;
    private javax.swing.JButton jVSBtn;
    private javax.swing.JMenuItem jVerticalLayout;
    private javax.swing.JMenu jViewer;
    private javax.swing.JMenuItem jViewerColumnSettings;
    private javax.swing.JMenuItem jViewerFontColorSettings;
    private javax.swing.JButton jViewerScrolling;
    private javax.swing.JButton jVorHBtn;
    private javax.swing.JButton jWTConnect;
    private javax.swing.JRadioButton jWTPricer;
    private javax.swing.JButton jWeatherBtn;
    private javax.swing.JButton jYIMBtn;
    private javax.swing.JMenuItem jYahooSettings;
    // End of variables declaration//GEN-END:variables

    private class FrameIconFlashingAgent extends Thread{
        @Override
        public void run() {
            final Image image_yellow = getImageSettings().getPointBoxYellowIcon().getImage();
            final Image image_red = getImageSettings().getPointBoxRedIcon().getImage();
            while(true) {
                try {
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            setIconImage(image_yellow);
                        }
                    });
                    Thread.sleep(1500);
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            setIconImage(image_red);
                        }
                    });
                    Thread.sleep(1500);
                    if ((getExtendedState() != JFrame.ICONIFIED) && (isActive())){
                        interrupt();
                    }
                } //while
                catch (InterruptedException ex) {
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            setIconImage(image_red);
                        }
                    });
                    break;
                }
            }//while
        }

    }//class: FrameIconFlashingAgent
    
    private class LogoutConnectionIconFlasher extends Thread{
        private GatewayServerType gatewayServerType;
        private LogoutConnectionIconFlasher(GatewayServerType gatewayServerType) {
             this.gatewayServerType = gatewayServerType;
        }
        @Override
        public void run() {
            for (int i = 0; i < 10; i++){
                if (i % 2 == 0){
                    switchConnectorIcon(gatewayServerType, false);
                }else{
                    switchConnectorIcon(gatewayServerType, true);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LogoutConnectionIconFlasher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }//for
            finalizeConnectorIcon();
        }

        private void finalizeConnectorIcon() {
            IServerLoginWindow dialog = face.getLoginWindow(gatewayServerType);
            if (dialog != null){
                if (dialog.connectionCount() > 0){
                    switchConnectorIcon(gatewayServerType, true);
                }else{
                    switchConnectorIcon(gatewayServerType, false);
                }
            }
        }
    }//class: LogoutConnectionIconFlasher
    
    public void storeDockingSpliterLocationDuringUnloading(){
        if(vPanel!=null){
            vPanel.storeDockingSpliterLocation();
        }
        if(hPanel!=null){
            hPanel.storeDockingSpliterLocation();
        }
    }
    
    public void setupSpliterLocationDuringInitialization(){
        if(vPanel!=null){
            vPanel.setupSpliterLocationDuringInitialization();
        }
        if(hPanel!=null){
            hPanel.setupSpliterLocationDuringInitialization();
        }        
    }    
}
