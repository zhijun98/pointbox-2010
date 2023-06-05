/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.*;
import com.eclipsemarkets.event.parser.QuoteParsedEvent;
import com.eclipsemarkets.event.pricer.QuotePricedEvent;
import com.eclipsemarkets.event.runtime.PointBoxConsoleSettingsInitializedEvent;
import com.eclipsemarkets.gateway.data.*;
import com.eclipsemarkets.gateway.user.*;
import com.eclipsemarkets.gateway.web.ConnectionEventHappened;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.exceptions.PointBoxException;
import com.eclipsemarkets.global.util.EaioUUID;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.IServerLoginWindow;
import com.eclipsemarkets.pbc.face.talker.dist.PbcFloatingFrameTerms;
import com.eclipsemarkets.pbc.face.talker.messaging.MessagingPaneManager;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.pbc.web.PbcReleaseInformation;
import com.eclipsemarkets.pbc.web.PbcReleaseStatus;
import com.eclipsemarkets.release.PointBoxExecutorConfiguration;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.substance.PbconsoleViewerPreviewPainter;
import com.eclipsemarkets.web.PointBoxAccountID;
import com.eclipsemarkets.web.PointBoxConnectorID;
import com.eclipsemarkets.web.PointBoxServiceResult;
import com.eclipsemarkets.web.PointBoxWebServiceResponse;
import com.eclipsemarkets.web.pbc.PointBoxConsoleSettings;
import com.eclipsemarkets.web.pbc.talker.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * A master buddy list panel replaced original distribution list panel. Also, login 
 * entry loaded settings (distribution list) by its return value.
 * 
 * @author Zhijun Zhang
 */
public class PointBoxTalker extends AbstractTalker {

    private static final Logger logger;
    private static IPbcTalker self;
    static {
        logger = Logger.getLogger(PointBoxTalker.class.getName());
        self = null;
    }

    public static IPbcTalker getPointBoxTalkerSingleton(IPbcFace face){
        if (self == null){
            self = new PointBoxTalker(face);
            self.initializeLoginWindows();
        }
        return self;
    }
    
    public static IPbcTalker getPointBoxTalkerSingleton() throws PointBoxException{
        if(self==null){
            throw new PointBoxException("PointBoxTalker has not been initiated!");
        }
        return self;
    }

    public static String generateRegularBuddyListType() {
        return PbcBuddyListType.RegularBuddyList.toString();
    }

    public static IGroupMessagingConfirmDialog createGroupMessagingConfirmDialogInstance(IGatewayConnectorGroup group,
                                                                                        ArrayList<IGatewayConnectorBuddy> buddies,
                                                                                        Window window,
                                                                                        boolean modal) throws PointBoxException
    {
        if (self == null){
            throw new PointBoxException("Talker is not ready for this method.");
        }
        return new GroupMessagingConfirmDialog(self, group, buddies, window, modal);
    }
     
    /**
     * Used to avoid blocking or time consuming operations
     */
    private final ExecutorService service;
    
//    /**
//     * This is PITS group list panel: currently, this data field is not treated as 
//     * masterBuddyListTreePanel because it is simplified by hiding buddies and not 
//     * necessary to handle some events like what masterBuddyListTreePanel does. But, 
//     * it could be required to add those handles like what masterBuddyListTreePanel 
//     * does in the future.
//     */
//    private IPitsGroupListPanel pitsGroupListPanel;
    
    /**
     * This is distribution list panel
     */
    private final IDistributionBuddyListPanel masterBuddyListTreePanel;
    
    /**
     * This is pits-caste list panel
     */
    private final IPitsCastGroupListPanel pitsCastBuddyListTreePanel;

    /**
     * Base panel of the buddy List
     */
    private final JXPanel buddyListBasePanel;
    
    /**
     * The tabbed panel in buddyListBasePanel
     */
    private final JTabbedPane buddyListTabbedPane;

    /**
     * Settings for buddyListTabbedPane
     */
    private final Color defaultBuddyListForeground;
    private final Color defaultBuddyListBackground;
    private final Color selectedBuddyListForeground;
    private final Color selectedBuddyListBackground;
    
    /**
     * Notice: [Case 01] there is a small change that the coming-in message came 
     * in before the buddy list (on the tree panel) is ready. [Case 02] Offline 
     * messages buffered on the server-side are loaded before its login happened. 
     * [Case 03] After login, a message from an unknown buddy (i.e., it cannot find 
     * it on the buddy list yet) comes in.
     * <p/>
     * In addition to the above mentioned cases, this handler also handle "loading" 
     * buddy list panel when users log into the IM servers.
     */
    private final PointBoxTalkerHandler pbcTalkerHandler;

    /**
     * Message Board
     */
    private IMessagingPaneManager messagingPaneManager;
    
    /**
     * Control personalizeFaceComponent being called only once
     */
    private boolean isPersonalized;
     

    /**
     *
     * @param system
     */
    private PointBoxTalker(IPbcFace face) {
        super(face);
        
        isPersonalized = false;
        
        service = Executors.newFixedThreadPool(PointBoxExecutorConfiguration.PointBoxTalker_Service_Control);
        

        buddyListBasePanel = new JXPanel(new BorderLayout());
        buddyListBasePanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        masterBuddyListTreePanel = new DistributionBuddyListPanel(this);
//////        masterBuddyListTreePanel.startBuddyListChangedEventHandler();
        
        pitsCastBuddyListTreePanel = new PitsCastGroupListPanel(this);

        buddyListTabbedPane = new JTabbedPane();
        defaultBuddyListForeground = buddyListTabbedPane.getForeground();
        defaultBuddyListBackground = buddyListTabbedPane.getBackground();
        selectedBuddyListForeground = Color.RED;
        selectedBuddyListBackground = Color.YELLOW;
        buddyListTabbedPane.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                for (int i = 0; i < getBuddyListTabbedPane().getTabCount(); i++){
                    getBuddyListTabbedPane().setBackgroundAt(i, defaultBuddyListBackground);
                    getBuddyListTabbedPane().setForegroundAt(i, defaultBuddyListForeground);
                }
                getBuddyListTabbedPane().setBackgroundAt(getBuddyListTabbedPane().getSelectedIndex(), selectedBuddyListBackground);
                getBuddyListTabbedPane().setForegroundAt(getBuddyListTabbedPane().getSelectedIndex(), selectedBuddyListForeground);
                getBuddyListTabbedPane().grabFocus();
                
                setShowOfflineOption((IBuddyListPanel)getBuddyListTabbedPane().getSelectedComponent());
            }
        });
        try {
            buddyListTabbedPane.putClientProperty(LafWidget.TABBED_PANE_PREVIEW_PAINTER,
                                                  new PbconsoleViewerPreviewPainter());
        } catch (Throwable ex) {}
        
        buddyListTabbedPane.addTab(PbcBuddyListPanelTabName.PITS_CAST.toString(),
                                   getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon16(), //guiEnv.getPbconsoleImageSettings().getYimGreyIcon(),
                                   pitsCastBuddyListTreePanel.getBasePanel());
        
        
        /**
         * The conference panel is hidden because of its unsatisfied stability (11/01/2013)
         */
//        buddyListTabbedPane.addTab("Conference",
//                                   null, //guiEnv.getPbconsoleImageSettings().getYimGreyIcon(),
//                                   conferenceBuddyListTreePanel.getBasePanel());
        setTabCloseGuard();
        buddyListBasePanel.add(buddyListTabbedPane);
        
        //this should be initialized at the end of constructor
        pbcTalkerHandler = new PointBoxTalkerHandler();
    }

    @Override
    public void clickPricerBuddy() {
        IBuddyListPanel aBuddyListPanel = getBuddyListTreePanel(this.getPointBoxLoginUser());
        if (aBuddyListPanel != null){
            aBuddyListPanel.pbcLoginUserPricerBuddyClicked();
        }
    }

    @Override
    public void displayDistributionBuddyListPanel() {
        if (SwingUtilities.isEventDispatchThread()){
            buddyListTabbedPane.addTab(PbcBuddyListPanelTabName.DISTRIBUTION.toString(),
                                       getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon16(),
                                       masterBuddyListTreePanel.getBasePanel());
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    buddyListTabbedPane.addTab(PbcBuddyListPanelTabName.DISTRIBUTION.toString(),
                                               getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon16(),
                                               masterBuddyListTreePanel.getBasePanel());
                }
            });
        }
    }

    @Override
    public void hideDistributionBuddyListPanel() {
        if (SwingUtilities.isEventDispatchThread()){
            buddyListTabbedPane.remove(masterBuddyListTreePanel.getBasePanel());
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    buddyListTabbedPane.remove(masterBuddyListTreePanel.getBasePanel());
                }
            });
        }
    }

    @Override
    public void updatePitsCastCheckTree() {
        if (messagingPaneManager != null){
            messagingPaneManager.updatePitsCastCheckTree();
        }
    }

    @Override
    public void selectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        if (messagingPaneManager != null){
            messagingPaneManager.selectBuddyCheckNode(buddy);
        }
    }

    @Override
    public void unselectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        if (messagingPaneManager != null){
            messagingPaneManager.unselectBuddyCheckNode(buddy);
        }
    }

    @Override
    public void notifyArchiveMethodChanged() {
        messagingPaneManager.notifyArchiveMethodChanged();
    }

    @Override
    public String getArchiveWarningMessage() {
        if (PointBoxConsoleProperties.getSingleton().isDisclaimerMessageDisplayed(face.getPointBoxLoginUser())){
            return face.getArchiveWarningMessage();
        }else{
            return null;
        }
    }

    @Override
    public void closeGroupTabInFloatingDistributionFrame(IPointBoxDistributionGroup group) {
        messagingPaneManager.closeGroupTabInFloatingDistributionFrame(group);
    }

    @Override
    public boolean hidePitsFloatingFrame(IGatewayConnectorGroup group) {
        return messagingPaneManager.hidePitsFloatingFrame(group);
    }

    @Override
    public JFrame findPitsLikeGroupFloatingFrame(IGatewayConnectorGroup group) {
        return messagingPaneManager.findPitsLikeGroupFloatingFrame(group);
    }

    @Override
    public void renamePitsCastGroupInMasterFloatingFrame(IGatewayConnectorGroup oldGroup, String newGroupName) {
        messagingPaneManager.renamePitsCastGroupInMasterFloatingFrame(oldGroup, newGroupName);
    }

    @Override
    public void renamePitsFloatingFrame(IGatewayConnectorGroup group, String newGroupName) {
        messagingPaneManager.renamePitsFloatingFrame(group, newGroupName);
    }

    @Override
    public void displayOfflineBuddies(boolean value) {
        super.displayOfflineBuddies(value);
        masterBuddyListTreePanel.displayOfflineBuddies(value);
        pitsCastBuddyListTreePanel.displayOfflineBuddies(value);
    }

    @Override
    public void sendMessageToBuddy(IGatewayConnectorBuddy fromUser, IGatewayConnectorBuddy toUser, String message, EaioUUID pbcMsgUuid,boolean isRealSent) {
        if ((fromUser == null) || (toUser == null) || (DataGlobal.isEmptyNullString(message))){
            return;
        }
        if ((message != null) && (!message.trim().isEmpty())){
            //create IPbsysInstantMessage for sending over the web...
            IPbsysInstantMessage quoteMessage = PbconsoleQuoteFactory.createPbsysInstantMessageInstance(fromUser.getIMServerType());
            quoteMessage.setMessageContent(message);
            quoteMessage.setIMServerType(fromUser.getIMServerType());
            quoteMessage.setFromUser(fromUser);
            quoteMessage.setMessageTimestamp(new GregorianCalendar());
            quoteMessage.setOutgoing(true);
            quoteMessage.setToUser(toUser);
            
            if (pbcMsgUuid != null){
                quoteMessage.setPbcMessageUuid(pbcMsgUuid.toString());
            }
            
            IPbcKernel kernel = getKernel();
            if(isRealSent){
                //send the message
                PointBoxWebServiceResponse response = kernel.sendInstantMessage(kernel.getPointBoxAccountID(), 
                                                                        kernel.getPointBoxConnectorID(fromUser),
                                                                        quoteMessage);

                IPbsysOptionQuote quote = PbconsoleQuoteFactory.createPbsysOptionQuoteInstance(kernel.getDefaultPbcPricingModel());
                quote.setPbsysInstantMessage(quoteMessage);
                
                PbconsoleQuoteFactory.registerOutgoingMessageQuote(quote);

                kernel.raisePointBoxEvent(new MessageSentEvent(PointBoxEventTarget.PbcFace, quote));

                if (!response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
                    kernel.updateSplashScreen("One invalid instant message cannot been sent out."
                                                            + " [" + response.getResult() + "]",
                                                        Level.SEVERE, 10);
                }
            }else{
                IPbsysOptionQuote quote = PbconsoleQuoteFactory.createPbsysOptionQuoteInstance(getKernel().getDefaultPbcPricingModel());
                quote.setPbsysInstantMessage(quoteMessage);
                kernel.raisePointBoxEvent(new MessageSentEvent(PointBoxEventTarget.PbcFace, quote));
            }
        }
    }

    /**
     * NULL will be returned since this method was not supported by PointBoxTalker anymore
     * @param aBuddyListPanel
     * @return 
     */
    @Override
    public JPopupMenu createBuddyListPopupMenu(IBuddyListPanel aBuddyListPanel) {
        return null;
    }

    @Override
    public void reorganizeMessagingBoardTabButtons() {
        this.messagingPaneManager.reorganizeMessagingBoardTabButtons();
    }

    @Override
    public void displayFaceComponent() {
        //todo: zzj - display a stand-alone PointBox talker here
    }

    @Override
    public void hideFaceComponent() {
        //todo: zzj - hide a stand-alone PointBox talker here
        welcomeLoginFrame.setVisible(false);
    }
    
    /**
     * This method is only called once.
     */
    @Override
    public void personalizeFaceComponent() {
        if (!isPersonalized){
            isPersonalized = true;
            if (PointBoxConsoleProperties.getSingleton().isDisplayDistributionBuddyListPanelRequired(face.getPointBoxLoginUser())){
                buddyListTabbedPane.addTab(PbcBuddyListPanelTabName.DISTRIBUTION.toString(),
                                           getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon16(), //guiEnv.getPbconsoleImageSettings().getYimGreyIcon(),
                                           masterBuddyListTreePanel.getBasePanel());
            }
            getMessagingPaneManager().personalizeComponent();
            startCentralGatewayLivenessMonitor();
            pbcTalkerHandler.startHandler();
            getKernel().raisePointBoxEvent(new SystemLoginRequestFinishedEvent(PointBoxEventTarget.PbcWeb, GatewayServerType.PBIM_SERVER_TYPE));
        }
    }

    @Override
    public void releaseFaceComponent() {
//////        if (getMessagingPaneManager() != null){
//////            getMessagingPaneManager().setPersistencyRequired();
//////        }
//////        
//////        if (!masterBuddyListTreePanel.stopBuddyListChangedEventHandler()){
//////            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(masterBuddyListTreePanel.constructPbcBuddyListSettings());
//////        }
//////        if (!conferenceBuddyListTreePanel.stopBuddyListChangedEventHandler()){
//////            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(conferenceBuddyListTreePanel.constructPbcBuddyListSettings());
//////        }
//////        ArrayList<IBuddyListPanel> buddyListPanels = getBuddyListTreePanelStore().getAllBuddyListTreePanels();
//////        for (IBuddyListPanel aBuddyListPanel : buddyListPanels){
//////            if (!aBuddyListPanel.stopBuddyListChangedEventHandler()){
//////                getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(aBuddyListPanel.constructPbcBuddyListSettings());
//////            }
//////        }
        ArrayList<IBuddyListPanel> panelList = getAllBuddyListTreePanels();
        for (IBuddyListPanel panel : panelList){
            panel.releaseBuddyListPanel();
        }
        
        super.releaseFaceComponent();
        stopCentralGatewayLivenessMonitor();
        this.pbcTalkerHandler.stopHandler();
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
    public IGatewayConnectorBuddy getPointBoxLoginUser() {
        return face.getPointBoxLoginUser();
    }

    @Override
    public void distributeInstantMessages(String message, IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> checkedBuddies) {
        face.getKernel().distributeInstantMessages(message, group, checkedBuddies);
    }

    private void handleGatewayConnectionEvent(GatewayConnectionEvent event) {
        if ((event == null) || (event.getLoginUser() == null)){
            return;
        }
        ConnectionEventHappened eventType = event.getConnectionEventHappened();
        switch (eventType){
            case LOGOUT_SUCCEED:
                handleConnectorDisconnectedEvent(event.getLoginUser());
                break;
            case LOGIN_SUCCEED:
                handleConnectorConnectedEvent(event.getLoginUser());
                break;
            case LOGIN_REFUSEDED:
                handleConnectorRefusedEvent(event.getLoginUser());
                break;
            case LOGIN_CANCELLED:
                break;
            case CONFLICT_LOGIN_REFUSED:
                break;
            case CONNECTION_LOST:
                closeBuddyListPanelForLoginUser(event.getLoginUser());
                break;
        }
    }

    private void handleConnectorRefusedEvent(final IGatewayConnectorBuddy loginUser) {
        if (SwingUtilities.isEventDispatchThread()){
            handleConnectorRefusedEventHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleConnectorRefusedEventHelper(loginUser);
                }
            });
        }
    }

    private void handleConnectorRefusedEventHelper(final IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            logger.log(Level.SEVERE, "Technical error", new Exception("Parameter loginUser cannot be NULL"));
            return;
        }
        masterBuddyListTreePanel.handleConnectorDisconnectedEvent(loginUser);
        pitsCastBuddyListTreePanel.handleConnectorDisconnectedEvent(loginUser);
        IBuddyListPanel aBuddyListPanel = getBuddyListTreePanel(loginUser);
        if (aBuddyListPanel != null){
            aBuddyListPanel.handleConnectorDisconnectedEvent(loginUser);
            buddyListTabbedPane.remove(aBuddyListPanel.getBasePanel());
        }
    }
    private void handleConnectorDisconnectedEvent(final IGatewayConnectorBuddy loginUser) {
        closeBuddyListPanelForLoginUser(loginUser);
    }
    
    private void handleConnectorConnectedEvent(final IGatewayConnectorBuddy loginUser) {
        if (SwingUtilities.isEventDispatchThread()){
            handleConnectorConnectedEventHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleConnectorConnectedEventHelper(loginUser);
                }
            });
        }
    }
    
    private void handleConnectorConnectedEventHelper(final IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            logger.log(Level.SEVERE, "Technical error", new Exception("Parameter loginUser cannot be NULL"));
            return;
        }
        
        masterBuddyListTreePanel.handleConnectorConnectedEvent(loginUser);
        pitsCastBuddyListTreePanel.handleConnectorConnectedEvent(loginUser);
        
        if (GatewayServerType.PBIM_SERVER_TYPE.equals(loginUser.getIMServerType())){
            this.getLoginWindow(GatewayServerType.AIM_SERVER_TYPE).loadCredentials();
            this.getLoginWindow(GatewayServerType.YIM_SERVER_TYPE).loadCredentials();
            startCentralGatewayLivenessMonitor();
        }
        
        IBuddyListPanel aBuddyListPanel = getBuddyListTreePanel(loginUser);
        if (aBuddyListPanel != null){
            aBuddyListPanel.handleConnectorConnectedEvent(loginUser);
        }
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getSortedBuddyList(IGatewayConnectorBuddy loginUser) {
        return GatewayBuddyListFactory.getSortedBuddyList(loginUser);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getSortedBuddyList(GatewayServerType serverType) {
        return GatewayBuddyListFactory.getSortedBuddyList(serverType);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getSortedBuddyList(GatewayServerType serverType, boolean onlineBuddyRequired) {
        return GatewayBuddyListFactory.getSortedBuddyList(serverType, onlineBuddyRequired);
    }

    @Override
    public ArrayList<String> getSortedBuddyUniqueNameList(IGatewayConnectorBuddy loginUser){
        return GatewayBuddyListFactory.getSortedBuddyUniqueNameList(loginUser);
    }

    /**
     * 
     * @param clickedBroker
     * @see IGatewayBuddyHighlightingListener
     */
    @Override
    public void gatewayConnectorBuddyHighlighted(final IGatewayConnectorBuddy clickedBroker,final boolean isHightlightOnMsgBoard) {
        if (SwingUtilities.isEventDispatchThread()){
            gatewayConnectorBuddyHighlightedHelper(clickedBroker,isHightlightOnMsgBoard);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    gatewayConnectorBuddyHighlightedHelper(clickedBroker,isHightlightOnMsgBoard);
                }
            });
        }
    }

    @Override
    public void populateUpdatedBuddyProfile(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy, IBuddyProfileRecord buddyProfile) {
        getMessagingPaneManager().populateUpdatedBuddyProfile(loginUser, buddy, buddyProfile);
        getKernel().raisePointBoxEvent(
            new BuddyStatusChangedEvent(PointBoxEventTarget.PbcFace,
                                        buddy.getLoginOwner(),
                                        buddy));
    }
    
    private void gatewayConnectorBuddyHighlightedHelper(IGatewayConnectorBuddy clickedBroker,boolean isHightlightOnMsgBoard) {
        //logger.log(Level.INFO, "clickedBroker - {0}", clickedBroker.getLoginOwner().getIMUniqueName());
        if (clickedBroker != null){
            IGatewayConnectorBuddy loginUser = clickedBroker.getLoginOwner();
            if (loginUser != null){
                masterBuddyListTreePanel.highlightGatewayConnectorBuddy(clickedBroker);
                pitsCastBuddyListTreePanel.highlightGatewayConnectorBuddy(clickedBroker);
                //highlight it on DistributionBuddyListPanel...
                IBuddyListPanel panel = getBuddyListTreePanel(loginUser);
                if (panel != null){
                    if ((panel.getBasePanel() != null) && (panel.getBasePanel() instanceof Component)){
                        //buddyListTabbedPane.setSelectedComponent(panel.getBasePanel());
                        panel.highlightGatewayConnectorBuddy(clickedBroker);
                    }
                }
                //hightlight it on MessagingBoard
                if(isHightlightOnMsgBoard)
                    getMessagingPaneManager().gatewayConnectorBuddyHighlighted(loginUser, clickedBroker);
                
            }else{
                logger.log(Level.SEVERE, "clickedBroker {0} lost its login owner.", clickedBroker.getIMUniqueName());
            }
        }
    }
    
    @Override
    public IMessagingPaneManager getMessagingPaneManager(){
        if (messagingPaneManager == null){
            if(SwingUtilities.isEventDispatchThread()){
                messagingPaneManager = MessagingPaneManager.getSingleton(this);
            }else{
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        messagingPaneManager = MessagingPaneManager.getSingleton(PointBoxTalker.this);
                    }
                });
            }
        }
        return messagingPaneManager;
    }

    @Override
    public JPanel getMessagingBasePanel() {
        //return messagingPaneManager.getBasePanel();
        return getMessagingPaneManager().getBasePanel();
    }

    @Override
    public void displayDistribributionMessageBoard(String message, IGatewayConnectorGroup copyToGroup) {
        if (masterBuddyListTreePanel != null){
            masterBuddyListTreePanel.displayDistribributionMessageBoard(message, copyToGroup);
        }
    }

    @Override
    public void displayAndSendDistribributionMessageBoard(String message, IGatewayConnectorGroup sendToGroup) {
        if (masterBuddyListTreePanel != null){
            masterBuddyListTreePanel.displayAndSendDistribributionMessageBoard(message, sendToGroup);
        }
    }

    @Override
    public void displayPitsCastMessageBoardForCopyTo(String message, IGatewayConnectorGroup copyToGroup) {
        if (pitsCastBuddyListTreePanel instanceof PitsCastGroupListPanel){
            ((PitsCastGroupListPanel)pitsCastBuddyListTreePanel).displayDistribributionMessageBoard("", copyToGroup);
            MessagingPaneManager.getSingleton(this).copyToPitsCastMessageBoard(message, copyToGroup, false);
        }
    }

    @Override
    public void displayPitsCastMessageBoardForSendTo(String message, IGatewayConnectorGroup sendToGroup) {
        if (pitsCastBuddyListTreePanel instanceof PitsCastGroupListPanel){
            ((PitsCastGroupListPanel)pitsCastBuddyListTreePanel).displayDistribributionMessageBoard("", sendToGroup);
            MessagingPaneManager.getSingleton(this).copyToPitsCastMessageBoard(message, sendToGroup, true);
        }
    }

    @Override
    public void displayPitsCastMessageFrameForCopyPasteQuoteMessage(String message) {
        MessagingPaneManager.getSingleton(this).copyToPitsCastMessageFloatingFrame(message, false);
    }

    @Override
    public void displayPitsCastMessageFrameForSendQuoteMessage(String message) {
        MessagingPaneManager.getSingleton(this).copyToPitsCastMessageFloatingFrame(message, true);
    }
        
    @Override
    public void setCurrentPanelShowOfflineOption(){
        setShowOfflineOption((IBuddyListPanel)getBuddyListTabbedPane().getSelectedComponent());
     }
    
    
    private void setShowOfflineOption(IBuddyListPanel panel){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            if(getKernel().getPointBoxLoginUser()!=null){
                boolean isShowOffline=prop.retrieveShowOfflineOption(getKernel().getPointBoxLoginUser().getIMUniqueName());
                panel.setShowOfflineOption(isShowOffline);
            }
            
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }          
    }

    @Override
    public IPitsGroupListPanel getPitsLikeTabPanelByTabName(PbcFloatingFrameTerms pbcFloatingFrameName) {
        if (pbcFloatingFrameName == null){
            return null;
        }
        String tabName = pbcFloatingFrameName.toString();
        int index = buddyListTabbedPane.indexOfTab(tabName) ;
        if (index < 0){
            return null;
        }else{
            Component panel = buddyListTabbedPane.getComponentAt(index);
            if(panel instanceof IPitsCastGroupListPanel){
                //todo-pitscast: how to handle "getPitsLikeTabPanelByTabName"? just return NULL?
                return null;
            }else if (panel instanceof IPitsGroupListPanel){
                return (IPitsGroupListPanel)panel;
            }else{
                return null;
            }
        }
    }

    /**
     * Notice that this method does not assume there should be only one PITS panel. Thus, 
     * it leaves the door open for the future when there may have multiple PITS panels.
     * 
     * @param tabName
     * @return 
     */
    @Override
    public IPitsGroupListPanel acquirePitsLikeGroupListTabPanelInEDT(final String tabName)
    {
        if (!SwingUtilities.isEventDispatchThread()){
            return null;
        }
        int index = buddyListTabbedPane.indexOfTab(tabName) ;
        if (index < 0){
            PitsGroupListPanel pitsGroupListPanel = new PitsGroupListPanel(this, tabName);
            if (messagingPaneManager instanceof IBuddyListEventListener){
                pitsGroupListPanel.addBuddyListPanelListener((IBuddyListEventListener)messagingPaneManager);
            }
            buddyListTabbedPane.addTab(tabName, getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon16(), pitsGroupListPanel);
            buddyListTabbedPane.setSelectedComponent(pitsGroupListPanel);
            return pitsGroupListPanel;
        }else{
            Component panel = buddyListTabbedPane.getComponentAt(index);
            if(panel instanceof IPitsCastGroupListPanel){
                //todo-pitscast: how to handle "acquirePitsLikeGroupListTabPanelInEDT"? just return NULL?
                return null;
            }else if (panel instanceof IPitsGroupListPanel){
                return (IPitsGroupListPanel)panel;
            }else{
                return null;
            }
        }
    }
    
    /**
     * Used by Trade-entry-form
     * @deprecated - TradeEntryForm (i.e. template) has been deprecated
     * @return 
     */
    @Override
    public boolean checkDistributionListTreePanelReadinessForTemplate() {
        return (!masterBuddyListTreePanel.isEmptyPanel());
    }

    @Override
    public ArrayList<IGatewayConnectorGroup> getPitsCastGroups() {
        //return masterBuddyListTreePanel.getAllGroups(false, false);
        return pitsCastBuddyListTreePanel.getAllGroups(false, false);
    }

    @Override
    public ArrayList<IGatewayConnectorGroup> getPitsGroups() {
        ArrayList<IGatewayConnectorGroup> result = new ArrayList<IGatewayConnectorGroup>();
        IPitsGroupListPanel aPitsGroupListPanel = getPitsLikeTabPanelByTabName(PbcFloatingFrameTerms.PITSFrame);
        if (aPitsGroupListPanel != null){
            return aPitsGroupListPanel.getAllGroups(false, false);
        }
        return result;
    }

    /**
     * Used by MessagingPaneManager::acquireFloatingMessagingBoards and BuddyMessagingBoardDropTargetListener
     * @param buddy
     * @return 
     */
    @Override
    public ArrayList<IGatewayConnectorGroup> getAssociatedDistributionGroups(IGatewayConnectorBuddy buddy){
        if (buddy == null){
            return new ArrayList<IGatewayConnectorGroup>();
        }else{
            IBuddyListPanel panel = getBuddyListTreePanel(buddy.getLoginOwner());
            if (panel == null){
                return new ArrayList<IGatewayConnectorGroup>();
            }
            return panel.getAssociatedDistributionGroups(buddy);
        }
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfDistGroup(IGatewayConnectorGroup distGroup) {
        if (masterBuddyListTreePanel == null){
            return new ArrayList<IGatewayConnectorBuddy>();
        }else{
            return masterBuddyListTreePanel.getBuddiesOfGroup(distGroup);
        }
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfPitsCastGroup(IGatewayConnectorGroup distGroup) {
        if (pitsCastBuddyListTreePanel == null){
            return new ArrayList<IGatewayConnectorBuddy>();
        }else{
            return pitsCastBuddyListTreePanel.getBuddiesOfGroup(distGroup);
        }
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfPitsGroup(IGatewayConnectorGroup distGroup) {
        IPitsGroupListPanel aPitsGroupListPanel = this.getPitsLikeTabPanelByTabName(PbcFloatingFrameTerms.PITSFrame);
        if (aPitsGroupListPanel == null){
            return new ArrayList<IGatewayConnectorBuddy>();
        }else{
            return aPitsGroupListPanel.getBuddiesOfGroup(distGroup);
        }
    }

    /**
     * Used by MessagingPaneManager::acquireFloatingMessagingBoards and BuddyMessagingBoardDropTargetListener
     * @param distGroup
     * @return 
     */
    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfGroup(IGatewayConnectorGroup distGroup) {
        if (distGroup == null){
            return new ArrayList<IGatewayConnectorBuddy>();
        }else{
            IBuddyListPanel panel = getBuddyListTreePanel(distGroup.getLoginUser());
            if (panel == null){
                return new ArrayList<IGatewayConnectorBuddy>();
            }
            return panel.getBuddiesOfGroup(distGroup);
        }
    }

    /**
     * Used by Trade-entry-form
     * @param groups
     * @return 
     */
    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfDistributionGroups(ArrayList<IGatewayConnectorGroup> groups) {
        return masterBuddyListTreePanel.getBuddiesOfGroups(groups);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfPitsCastGroups(ArrayList<IGatewayConnectorGroup> groups) {
        return pitsCastBuddyListTreePanel.getBuddiesOfGroups(groups);
    }
    
    /**
     * Used by Trade-entry-form
     * @deprecated - Trade-entry-form has been deprecated
     * @param gatewayServerType
     * @return 
     */
    @Override
    public void activateGroupListTreeTab() {
        if (SwingUtilities.isEventDispatchThread()){
            buddyListTabbedPane.setSelectedComponent(masterBuddyListTreePanel.getBasePanel());
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    getBuddyListTabbedPane().setSelectedComponent(getMasterBuddyListTreePanel().getBasePanel());
                }
            });
        }
    }

    @Override
    public void handlePointBoxEvent(PointBoxConsoleEvent event) {
        IBuddyListPanel panel;
        if (event instanceof MessageRecievedEvent){
            handleMessageRecievedEvent((MessageRecievedEvent)event);
        }else if (event instanceof MessageSentEvent){
            handleMessageSentEvent((MessageSentEvent)event);
        }else if (event instanceof GatewayConnectionEvent){
            handleGatewayConnectionEvent((GatewayConnectionEvent)event);
        }else if (event instanceof QuoteParsedEvent){
            handleQuoteParsedEvent((QuoteParsedEvent)event);
        }else if (event instanceof QuotePricedEvent){
            handleQuotePricedEvent((QuotePricedEvent)event);
        }else if (event instanceof PointBoxConsoleSettingsInitializedEvent){
            PointBoxConsoleSettingsInitializedEvent pbcsie = (PointBoxConsoleSettingsInitializedEvent)event;
            PointBoxConsoleSettings aPointBoxConsoleSettings = pbcsie.getPointBoxConsoleSettings();
            //preparePbimMyselfBuddy(aPointBoxConsoleSettings);
            if (aPointBoxConsoleSettings != null){
                if ((aPointBoxConsoleSettings.getPbcBuddyListSettings() != null) && (aPointBoxConsoleSettings.getPbcBuddyListSettings().length > 0)){
                    populateSettingsForHybridBuddyListTreePanel(aPointBoxConsoleSettings.getPointBoxAccountID(),
                                             aPointBoxConsoleSettings.getPbcBuddyListSettings());
                }
            }
        }else if (event instanceof BuddyItemPresentedEvent){
            //logger.log(Level.INFO, "handlePointBoxEvent >>> BuddyItemPresentedEvent ...");
            BuddyItemPresentedEvent pEvt = (BuddyItemPresentedEvent)event;
            if (pEvt.getBuddy() != null){
                try{
                    if(getBuddyListTreePanel(pEvt.getLoginUser())==null){
                        openBuddyListPanelForLoginUserAndWait(pEvt.getLoginUser());
                    }
                    panel = getBuddyListTreePanel(pEvt.getLoginUser());
                    if (panel != null){
                        //ask buddy list panel to populate the buddy
                        panel.handleBuddyItemPresentedEvent(pEvt.getBuddy());
                    }
                }catch(Exception e){
                }
            }
        }else if (event instanceof BuddyItemRemovedEvent){
            BuddyItemRemovedEvent rEvt = (BuddyItemRemovedEvent)event;
            if (rEvt.getBuddy() != null){
                masterBuddyListTreePanel.handleBuddyItemRemovedEvent(rEvt.getBuddy());
                pitsCastBuddyListTreePanel.handleBuddyItemRemovedEvent(rEvt.getBuddy());
                panel = getBuddyListTreePanel(rEvt.getLoginUser());
                if (panel != null){
                    panel.handleBuddyItemRemovedEvent(rEvt.getBuddy());
                }
            }
        }else if (event instanceof BuddySubscriptionEvent){
            BuddySubscriptionEvent sEvt = (BuddySubscriptionEvent)event;
            if (sEvt.getChangedBuddy() != null){
                if (sEvt.isSubscribed()){
                    //masterBuddyListTreePanel.handleBuddySubscriptionEvent(sEvt.getChangedBuddy());
                    panel = getBuddyListTreePanel(sEvt.getLoginUser());
                    if (panel != null){
                        panel.handleBuddySubscriptionEvent(sEvt.getChangedBuddy());
                    }
                }else{
                    //masterBuddyListTreePanel.handleBuddyUnsubscriptionEvent(sEvt.getChangedBuddy());
                    //conferenceBuddyListTreePanel.handleBuddyUnsubscriptionEvent(sEvt.getChangedBuddy());
                    panel = getBuddyListTreePanel(sEvt.getLoginUser());
                    if (panel != null){
                        panel.handleBuddyUnsubscriptionEvent(sEvt.getChangedBuddy());
                    }
                }
            }
        }else if (event instanceof BuddyStatusChangedEvent){
            BuddyStatusChangedEvent scEvt = (BuddyStatusChangedEvent)event;
            IGatewayConnectorBuddy buddy = scEvt.getChangedBuddy();
            if (buddy != null){
                /**
                 * Update buddy lists (DIST, PITS, CONF, and REGULAR)
                 */
                int num = buddyListTabbedPane.getTabCount();
                Object panelObj;
                for (int i = 0; i < num; i++){
                    panelObj = buddyListTabbedPane.getComponentAt(i);
                    if (panelObj instanceof IBuddyListPanel){
                        //ask buddy list panel to populate the buddy's status
                        ((IBuddyListPanel)panelObj).handleBuddyStatusChangedEvent(buddy);
                    }
                }
                /**
                 * Update message boards state relevant to buddy status change
                 */
                face.getPointBoxTalker().getMessagingPaneManager().updateBuddyStatus(scEvt.getLoginUser(), buddy,this);
            }
        }
    }
    
    /**
     * Populate conference/distribution/Pits/PitsCast buddy list settings
     * @param accountID
     * @param buddyListSettings 
     */
    private void populateSettingsForHybridBuddyListTreePanel(PointBoxAccountID accountID, PbcBuddyListSettings[] buddyListSettings) {
        if ((accountID == null)){
            PointBoxTracer.displayMessage(logger, new Exception("TECH ERR: accountID cannot be NULL."));
            return;
        }
        boolean saveMasterSettings = true;
        boolean savePitsCastSettings = true;
        PbcBuddyListSettings pitsPbcBuddyListSettings = null;
        for (int i = 0; i < buddyListSettings.length; i++){
            if (buddyListSettings[i] != null){
                if (masterBuddyListTreePanel.checkBuddyListSettingsIdentity(buddyListSettings[i])){
                    masterBuddyListTreePanel.populatePbcBuddyListSettings(accountID, 
                                                                          buddyListSettings[i]);
                    saveMasterSettings = false;
                }else if (pitsCastBuddyListTreePanel.checkBuddyListSettingsIdentity(buddyListSettings[i])){
                    pitsCastBuddyListTreePanel.populatePbcBuddyListSettings(accountID, 
                                                                          buddyListSettings[i]);
                    savePitsCastSettings = false;
                }else if (PbcBuddyListType.PitsGroupList.toString().equalsIgnoreCase(buddyListSettings[i].getBuddyListType())){
                    pitsPbcBuddyListSettings = buddyListSettings[i];
                }
            }
        }///for
        
        if (pitsPbcBuddyListSettings != null){
            if ((pitsPbcBuddyListSettings != null) 
                    && (pitsPbcBuddyListSettings.getGroupItems() != null) 
                    && (pitsPbcBuddyListSettings.getGroupItems().length > 0))
            {
                transferPitsGroupsIntoPitsCastBuddyListPanel(accountID, pitsPbcBuddyListSettings);
                savePitsCastSettings = true;
            }
        }
        
        if (saveMasterSettings){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(masterBuddyListTreePanel.constructPbcBuddyListSettings(), true);
//////            masterBuddyListTreePanel.handleBuddyListChangedEvent();
        }
        if (savePitsCastSettings){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(pitsCastBuddyListTreePanel.constructPbcBuddyListSettings(), true);
//////            masterBuddyListTreePanel.handleBuddyListChangedEvent();
        }
    }

    private void transferPitsGroupsIntoPitsCastBuddyListPanel(final PointBoxAccountID accountID, final PbcBuddyListSettings pitsPbcBuddyListSettings) {
        if (SwingUtilities.isEventDispatchThread()){
            transferPitsGroupsIntoPitsCastBuddyListPanelHelper(accountID, pitsPbcBuddyListSettings);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    transferPitsGroupsIntoPitsCastBuddyListPanelHelper(accountID, pitsPbcBuddyListSettings);
                }
            });
        }
    }
    private void transferPitsGroupsIntoPitsCastBuddyListPanelHelper(final PointBoxAccountID accountID, final PbcBuddyListSettings pitsGroupBuddyListSettings) {
//        IPitsGroupListPanel aPitsGroupListPanel = this.acquirePitsLikeGroupListTabPanelInEDT(pbcBuddyListSettings.getBuddyListName());
        IPitsGroupListPanel pitsGroupListPanel = null;
        String tabName = pitsGroupBuddyListSettings.getBuddyListName();
        int index = buddyListTabbedPane.indexOfTab(tabName) ;
        if (index < 0){
            pitsGroupListPanel = new PitsGroupListPanel(this, tabName);
        }else{
            Component panel = buddyListTabbedPane.getComponentAt(index);
            if (panel instanceof IPitsGroupListPanel){
                pitsGroupListPanel = (IPitsGroupListPanel)panel;
            }
        }
        if (pitsGroupListPanel != null){
            pitsGroupListPanel.populatePbcBuddyListSettings(accountID, pitsGroupBuddyListSettings);
            BuddyListGroupItem[] aBuddyListGroupItemArray = pitsGroupBuddyListSettings.getGroupItems();
            for (BuddyListGroupItem aBuddyListGroupItem : aBuddyListGroupItemArray){
                pitsGroupListPanel.removePitsFrame(GatewayBuddyListFactory.getDistributionGroupInstance(GatewayBuddyListFactory.getLoginUserInstance(accountID.getLoginName(),
                                                                                                                                                     GatewayServerType.convertToType(accountID.getGatewayServerType())), 
                                                                                                        aBuddyListGroupItem.getGroupName()), 
                                                    tabName);
            }
            pitsCastBuddyListTreePanel.populatePbcBuddyListSettings(accountID, 
                                                                      pitsGroupBuddyListSettings);
        }
    }

    /**
     * Get the message and populate onto the talker. After that, viewer, pricer and parse continually work on it.
     * @param event
     */
    private void handleMessageRecievedEvent(MessageRecievedEvent event) {
        if (event == null){
            return;
        }
        //(1) validation of event (and message)
        //(2) get who receive the message from buddyListTreePanelStore
        //(3) if it is unknown buddy, buffer his messages and pop up accept-buddy dialog
        //(4) if it is exisitng buddy, prepare message tab for this person
        //(5) publish message on the tab and then broadcast...done
        IPbsysOptionQuote msgQuote = event.getReceievedQuoteMessage();
        if (msgQuote == null) {
            return;
        }
        IPbsysInstantMessage msg = msgQuote.getInstantMessage();
        if (msg == null){
            return;
        }
        if (msg.isMessageReadyForDistribution()){
            final IGatewayConnectorBuddy possibleNewBuddy = msg.getFromUser();
            if (possibleNewBuddy == null){
                PointBoxTracer.recordSevereException(logger, "Technical Error",
                        new Exception("Instant message instance has no FromUser which implies technical exceptions raised."));
                //return;
            }else{
                try {
                    IGatewayConnectorBuddy existingBuddy = null;
                    final IBuddyListPanel panel = getBuddyListTreePanel(msg.getToUser());
                    if (panel != null){
                        existingBuddy = panel.confirmBuddyPresentedInList(possibleNewBuddy);
                    }
                    //IGatewayConnectorBuddy existingBuddy = masterBuddyListTreePanel.confirmBuddyPresentedInList(possibleNewBuddy);
                    /**
                     * Notice: [Case 01] there is a small change that the coming-in message came in before the buddy list 
                     * (on the tree panel) is ready. [Case 02] Offline messages buffered on the server-side are loaded before 
                     * its login happened. [Case 03] After login, a message from an unknown buddy (i.e., it cannot find it on 
                     * the buddy list yet) comes in. 
                     */
                    if (existingBuddy == null) {
                        logger.log(Level.WARNING, "Offline messages or new unknown buddy''s message....{0}", possibleNewBuddy.getIMUniqueName());
                        if (possibleNewBuddy.getLoginOwner() == null){
                            switch (possibleNewBuddy.getIMServerType()){
                                case AIM_SERVER_TYPE:
                                case YIM_SERVER_TYPE:
                                    possibleNewBuddy.setLoginOwner(msg.getToUser());
                                    break;
                                default:
                                    if (getPointBoxLoginUser() == null){
                                        logger.log(Level.SEVERE, null, new Exception("[TECH ERROR] talker.getPointBoxLoginUser() cannot be NULL at this time point."));
                                        possibleNewBuddy.setLoginOwner(msg.getToUser());
                                    }else{
                                        possibleNewBuddy.setLoginOwner(getPointBoxLoginUser());
                                    }
                            }
                        }
                        pbcTalkerHandler.bufferQuoteFromUnknownBuddy(msgQuote);
                    }else{
                        IGatewayConnectorBuddy loginOwner = existingBuddy.getLoginOwner();
                        if (loginOwner == null){
                            PointBoxTracer.recordSevereException(logger, new Exception("publishQuoteOnMessageTab::loginUser is NULL"));
                        }else{
                            /**
                             * if a buddy presence is offline, his login-owner is online, if messages 
                             * from this buddy still come in, it means this buddy actually online. This 
                             * problem is caused by legacy issue on inconsistent data in the database. 
                             * The reason is this buddy was not really successfully added onto the public 
                             * server. In this case, here IF-zone code is used to remedy this deficiency.
                             */
                            if ((BuddyStatus.Online.equals(loginOwner.getBuddyStatus()))
                                    && (!BuddyStatus.Online.equals(existingBuddy.getBuddyStatus()))
                                    && (!msg.isHistoricalMessage()))
                            {
                                existingBuddy.setBuddyStatus(BuddyStatus.Online);
                                getKernel().addNewConnectorBuddy(loginOwner, 
                                                                 GatewayBuddyListFactory.getGatewayConnectorGroupInstance(loginOwner, 
                                                                                                                          existingBuddy.getBuddyGroupName()), 
                                                                                                                          existingBuddy.getIMScreenName());
                                getKernel().raisePointBoxEvent(
                                    new BuddyStatusChangedEvent(PointBoxEventTarget.PbcFace,
                                                                existingBuddy.getLoginOwner(),
                                                                existingBuddy));
                            }
                            getMessagingPaneManager().publishQuoteOnMessageTab(existingBuddy.getLoginOwner(), 
                                                                               existingBuddy, msgQuote);
                        }
                    }
                } catch (Exception ex) {
                    PointBoxTracer.recordSevereException(logger, 
                            "This is a bug that is potentially able to cause messages only shown up in viewer but not on talker's message tab", 
                            ex);
                }
            }
        }else{
            face.getKernel().updateSplashScreen(" [WARNING] A message received is not qualified to be published.", Level.INFO, 10);
            PointBoxTracer.recordSevereException(logger, new Exception("A message received is not qualified to be published."));
        }//if
    }
    
    private void handleMessageSentEvent(MessageSentEvent event) {
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
        if (msg.isMessageReadyForDistribution()){
            final IGatewayConnectorBuddy possibleNewBuddy = msg.getToUser();
            if (possibleNewBuddy == null){
                logger.log(Level.SEVERE, "Instant message instance has no FromUser which implies technical exceptions raised.",
                            new Exception("Instant message instance has no FromUser which implies technical exceptions raised."));
                //return;
            }else{
                try {
                    final IBuddyListPanel panel = getBuddyListTreePanel(msg.getFromUser());
                    if (panel == null){
                        /**
                         * This is possible now. If user load historical messages, whose buddy list panel may not be loaded yet.
                         */
                        //PointBoxTracer.recordSevereException(logger, new Exception("Cannot find a IBuddyListTreePanel for " + msg.getToUser() + ", which is expected to be there."));
                        return;
                    }
                    /**
                     * Confirm it is still on the buddy list
                     */
                    IGatewayConnectorBuddy existingBuddy = panel.confirmBuddyPresentedInList(possibleNewBuddy);
                    
                    //IGatewayConnectorBuddy existingBuddy = masterBuddyListTreePanel.confirmBuddyPresentedInList(possibleNewBuddy);
                    if (existingBuddy == null) {
                        //logger.log(Level.WARNING, "Message sent to a non-exsiting buddy {0} was canneled ....", possibleNewBuddy.getIMUniqueName());
                    }else{
                        if (possibleNewBuddy.getLoginOwner() == null){
                            switch (possibleNewBuddy.getIMServerType()){
                                case AIM_SERVER_TYPE:
                                case YIM_SERVER_TYPE:
                                    possibleNewBuddy.setLoginOwner(existingBuddy.getLoginOwner());
                                    break;
                                default:
                                    if (getPointBoxLoginUser() == null){
                                        logger.log(Level.SEVERE, null, new Exception("[TECH ERROR] talker.getPointBoxLoginUser() cannot be NULL at this time point."));
                                        possibleNewBuddy.setLoginOwner(existingBuddy.getLoginOwner());
                                    }else{
                                        possibleNewBuddy.setLoginOwner(getPointBoxLoginUser());
                                    }
                            }
                        }
                        getMessagingPaneManager().publishQuoteOnMessageTab(existingBuddy.getLoginOwner(), existingBuddy, msgQuote);
                    }
                } catch (Exception ex) {
                    PointBoxTracer.recordSevereException(logger, 
                            "This is a bug that is potentially able to cause messages only shown up in viewer but not on talker's message tab", 
                            ex);
                }
            }
        }else{
            face.getKernel().updateSplashScreen(" [WARNING] A message sent is not qualified to be published.", Level.INFO, 10);
            logger.log(Level.WARNING, "A message sent is not qualified to be published.");
        }
    }

    @Override
    public boolean isBuddyListPanelOnPBC(IGatewayConnectorBuddy loginUser){
         IBuddyListPanel panel = getBuddyListTreePanel(loginUser);
         if(panel==null){
             return false;
         }
         return true;
    }
    /**
     * Handle quotes which are just priced
     * @param qpe
     */
    private void handleQuoteParsedEvent(QuoteParsedEvent qpe) {
        this.messagingPaneManager.publishParsedMessage(qpe.getParsedQuotes());
    }

    /**
     * Handle quotes which are just priced
     * @param qpe
     */
    private void handleQuotePricedEvent(QuotePricedEvent qpe) {
        this.messagingPaneManager.publishPricedMessage(qpe.getPricedQuotes());
    }

    /**
     * When loginUser try to log in a server, its buddy list panel should be ready so as to 
     * handle buddy-list events to build up buddy list panel. 
     * <p/>
     * This method is called as soon as case-01: Welcome-login windows successfully got into 
     * the system; case-02: ServerLoginDialog successfully got into a specific IM server
     * 
     * @param loginUser
     */
    @Override
    public void openBuddyListPanelForLoginUser(final IGatewayConnectorBuddy loginUser) {
        if (SwingUtilities.isEventDispatchThread()){
            openBuddyListPanelForLoginUserHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    openBuddyListPanelForLoginUserHelper(loginUser);
                }
            });
        }
    }
    
    /**
     * When loginUser try to log in a server, its buddy list panel should be ready so as to 
     * handle buddy-list events to build up buddy list panel
     * @param loginUser
     */
    private void openBuddyListPanelForLoginUserAndWait(final IGatewayConnectorBuddy loginUser) {
        if (SwingUtilities.isEventDispatchThread()){
            openBuddyListPanelForLoginUserHelper(loginUser);
        }else{
            try {
                SwingUtilities.invokeAndWait(new Runnable(){
                    @Override
                    public void run() {
                        openBuddyListPanelForLoginUserHelper(loginUser);
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(PointBoxTalker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(PointBoxTalker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }    
    
    private void openBuddyListPanelForLoginUserHelper(final IGatewayConnectorBuddy loginUser) {
        if(BuddyStatus.Online.equals(loginUser.getBuddyStatus())){ 
            //listen to this new loginuser's buddies status
            masterBuddyListTreePanel.handleConnectorConnectedEvent(loginUser);
            pitsCastBuddyListTreePanel.handleConnectorConnectedEvent(loginUser);
            masterBuddyListTreePanel.addBuddyListPanelListener((IBuddyListEventListener)getMessagingPaneManager());
            pitsCastBuddyListTreePanel.addBuddyListPanelListener((IBuddyListEventListener)getMessagingPaneManager());
        }
        IBuddyListPanel aBuddyListPanel = getBuddyListTreePanel(loginUser);
        if (aBuddyListPanel == null){
            //create a new buddy list panel
            aBuddyListPanel = new RegularBuddyListPanel(this, loginUser);
            ArrayList<PbcBuddyListSettings> aBuddyListSettingsList;
            if (getKernel().getPointBoxConsoleRuntime() == null){
                aBuddyListSettingsList = new ArrayList<PbcBuddyListSettings>();
            }else{
                aBuddyListSettingsList = getKernel().getPointBoxConsoleRuntime().getPbcBuddyListSettings();
            }
            /**
             * find buddy list settings for populating the list 
             */
            for (PbcBuddyListSettings aPbcBuddyListSettings : aBuddyListSettingsList){
                //customize buddy list of the settings is available
                if (aBuddyListPanel.checkBuddyListSettingsIdentity(aPbcBuddyListSettings))
                {
                    //rebuild connectorID if necessary
                    if (aPbcBuddyListSettings.getConnectorOwner() == null){
                        PointBoxAccountID accountID = getKernel().getPointBoxConsoleRuntime().getPointBoxAccountID();
                        PointBoxConnectorID connectorID = new PointBoxConnectorID();
                        connectorID.setAccountEntityID(accountID.getAccountEntityID());
                        connectorID.setPointBoxQuoteCodeValue(accountID.getPointBoxQuoteCodeValue());
                        connectorID.setGatewayServerType(loginUser.getIMServerType().toString());
                        connectorID.setLoginName(loginUser.getIMScreenName());
                        connectorID.setPassword(loginUser.getIMPassword());
                        connectorID.setReleaseUserType(accountID.getReleaseUserType());
                        aPbcBuddyListSettings.setConnectorOwner(connectorID);
                    }
                    aBuddyListPanel.populatePbcBuddyListSettings(aPbcBuddyListSettings.getConnectorOwner(), aPbcBuddyListSettings);
                    break;
                }
            }//for
            aBuddyListPanel = insertPanelIntoStorage(aBuddyListPanel);
            if (aBuddyListPanel != null){
                getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(aBuddyListPanel.constructPbcBuddyListSettings(), true);
                aBuddyListPanel.setRealTimePersistentRequired(true);
                if (getMessagingPaneManager() instanceof IBuddyListEventListener){
                    aBuddyListPanel.addBuddyListPanelListener((IBuddyListEventListener)getMessagingPaneManager());
                }
                
                if (welcomeLoginFrame.isAutoLogin()){
                    PbcReleaseInformation releaseInfo = face.checkPbcRelease();
                    if (releaseInfo.getPbcReleaseStatus().equals(PbcReleaseStatus.Latest_Release)){
                        //automatically log into public IM servers only when PBC is the latest.
                        if(!BuddyStatus.Online.equals(loginUser.getBuddyStatus())){
                            String pwd = PointBoxConsoleProperties.getSingleton().retrieveLoginWindowLoginPassword(loginUser.getIMScreenName(),
                                    this.getFace().getPointBoxLoginUser().getIMUniqueName(),
                                    loginUser.getIMServerType());
                            loginUser.setIMPassword(pwd);
                            loginPreviousPersistentConnector(loginUser);
                            //getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettingsVisibility(aBuddyListPanel, true);
                        }
                    }
                }
            }
        }
        presentBuddyListTreePanel(loginUser);
    }
    
    @Override
    public void presentBuddyListTreePanel(final IGatewayConnectorBuddy loginUser) {
        if (SwingUtilities.isEventDispatchThread()){
            presentBuddyListTreePanelHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    presentBuddyListTreePanelHelper(loginUser);
                }
            });
        }
    }
    private void presentBuddyListTreePanelHelper(IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            if (buddyListTabbedPane.getComponentCount() > 0){
                buddyListTabbedPane.setSelectedIndex(0);
            }
        }
        IBuddyListPanel aBuddyListPanel = getBuddyListTreePanel(loginUser);
        if (aBuddyListPanel != null){
            boolean foundTab = false;
            for (int index = 0; index < buddyListTabbedPane.getTabCount(); index++){
                if (buddyListTabbedPane.getComponentAt(index).equals(aBuddyListPanel.getBasePanel())){
                    buddyListTabbedPane.setSelectedIndex(index);
                    buddyListTabbedPane.setIconAt(index, face.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorBuddyIcon(loginUser.getIMServerType()));
                    foundTab = true;    //already on the panel
                    break;
                }
            }
            if (!foundTab){
                //set proper image icons for panel
                Icon icon;
                if(BuddyStatus.Online.equals(loginUser.getBuddyStatus())){
                    icon=face.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorBuddyIcon(loginUser.getIMServerType());
                }else{
                    icon=face.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorLogo21(loginUser.getIMServerType());  //black icons
                }
                buddyListTabbedPane.addTab(loginUser.getIMScreenName(),
                                           icon,
                                           aBuddyListPanel.getBasePanel());
                buddyListTabbedPane.setSelectedIndex(buddyListTabbedPane.getTabCount()-1);
            }
        }
    }

    @Override
    public void closeBuddyListPanelForLoginUser(final IGatewayConnectorBuddy logoutUser, final boolean memorizeBuddyListPanel) {
        if (SwingUtilities.isEventDispatchThread()){
            closeBuddyListTreePanelForLoginUserHelper(logoutUser, memorizeBuddyListPanel);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    closeBuddyListTreePanelForLoginUserHelper(logoutUser, memorizeBuddyListPanel);
                }
            });
        }
    }
    
    @Override
    public void closeBuddyListPanelForLoginUser(final IGatewayConnectorBuddy logoutUser){
        if (SwingUtilities.isEventDispatchThread()){
            closeBuddyListTreePanelForLoginUserHelper(logoutUser, true);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    closeBuddyListTreePanelForLoginUserHelper(logoutUser, true);
                }
            });
        }
    }

    private void closeBuddyListTreePanelForLoginUserHelper(final IGatewayConnectorBuddy logoutUser, final boolean memorizeBuddyListPanel){
        if (logoutUser != null){
            masterBuddyListTreePanel.handleConnectorDisconnectedEvent(logoutUser);
            pitsCastBuddyListTreePanel.handleConnectorDisconnectedEvent(logoutUser);
            //logger.log(Level.INFO, "connectorDisconnectedEventHelper - logoutUser = {0}", logoutUser.getIMUniqueName());
            IBuddyListPanel aBuddyListPanel = getBuddyListTreePanel(logoutUser);
            if (aBuddyListPanel != null){
                aBuddyListPanel.handleConnectorDisconnectedEvent(logoutUser);
                buddyListTabbedPane.remove(aBuddyListPanel.getBasePanel());
                if (!memorizeBuddyListPanel){
                    removePanelFromStorage(aBuddyListPanel);
                }
            }
        }
    }

    private void setTabCloseGuard(){
        SubstanceLookAndFeel.registerTabCloseChangeListener(buddyListTabbedPane, new VetoableTabCloseListener() {
            @Override
            public void tabClosed(JTabbedPane tabbedPane, Component tabComponent) {
            }
            @Override
            public void tabClosing(JTabbedPane tabbedPane, Component tabComponent) {
                if (tabComponent instanceof IBuddyListPanel){
                    removePanelFromStorage((IBuddyListPanel)tabComponent);
                    //getKernel().requestToStoreRegularBuddyListPanelVisibility(((IBuddyListPanel)tabComponent).getDistListName(), false);
                }
            }
            @Override
            public boolean vetoTabClosing(JTabbedPane tabbedPane, Component tabComponent) {
                if (tabComponent instanceof RegularBuddyListPanel){
                    IGatewayConnectorBuddy loginUser = ((RegularBuddyListPanel)tabComponent).getMasterLoginUser();
                    if (loginUser == null){
                        return false;
                    }else{
                        if (loginUser.getIMServerType().equals(GatewayServerType.PBIM_SERVER_TYPE)){
                            return true;
                        }else{
                            boolean disconnect=true;
                            if(BuddyStatus.Online.equals(loginUser.getBuddyStatus())){
                                disconnect = (JOptionPane.showConfirmDialog(face.getPointBoxMainFrame(),
                                                                    "Disconnect from this connector - "+loginUser.getIMUniqueName()+"?", "Confirmation",
                                                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
                            }
                            if (disconnect){
                                /**
                                 * use of IServerLoginWindow to execute logout
                                 */
                                IServerLoginWindow dialog = face.getLoginWindow(loginUser.getIMServerType());
                                if (dialog != null&&getKernel().getPointBoxConnectorID(loginUser)!=null){
                                    dialog.closeConnector(loginUser, false);
                                    return true;
                                }else{
                                    /**
                                     * This is to disable users to close the regular buddy list panel 
                                     * after users log out from their regular buddy list panel.
                                     */
                                    return true;
                                }
                            }else{
                                return true;
                            }
                        }//if
                    }//if
                }else{
                    return true;
                }
            }
        });
    }

    @Override
    public JPanel getBuddyListBasePanel() {
        return buddyListBasePanel;
    }

    @Override
    public JPanel getBasePanel() {
        return buddyListBasePanel;
    }

    @Override
    public List<IPbsysOptionQuote> getBufferedMessagesForNewBuddies(IGatewayConnectorBuddy buddy) {
        return pbcTalkerHandler.getBufferedMessagesForNewBuddies(buddy);
    }

    /**
     * @return the buddyListTabbedPane
     */
    @Override
    public JTabbedPane getBuddyListTabbedPane() {
        return buddyListTabbedPane;
    }

    /**
     * @return the masterBuddyListTreePanel
     */
    @Override
    public IDistributionBuddyListPanel getMasterBuddyListTreePanel() {
        return masterBuddyListTreePanel;
    }

    @Override
    public IPitsCastGroupListPanel getPitsCastBuddyListTreePanel() {
        return pitsCastBuddyListTreePanel;
    }

    @Override
    public boolean checkGroupNameRedundancy(String groupName) {
        if (masterBuddyListTreePanel != null 
                && masterBuddyListTreePanel.isGroupExisted(groupName)) 
        {
                 return true;
        }
        
        if (pitsCastBuddyListTreePanel != null 
                && pitsCastBuddyListTreePanel.isGroupExisted(groupName)) 
        {
                 return true;
        }
        //Now we only have one PITS panel
        IPitsGroupListPanel pitsGroupListPanel = getPitsLikeTabPanelByTabName(PbcFloatingFrameTerms.PITSFrame);
        if (pitsGroupListPanel != null
                && pitsGroupListPanel.isGroupExisted(groupName)) {
            return true;
        }
        
        return false;
    }
   
//    /**
//     * Every PBIM account should add itself to be its own buddy so that it can use 
//     * of it to do pricing through messaging itself. 
//     * @param aPointBoxConsoleSettings 
//     */
//    private void preparePbimMyselfBuddy(PointBoxConsoleSettings aPointBoxConsoleSettings) {
//        boolean prepareMyselgBuddy = true;
//        String pbimLoginName = null;
//        //check if it need to prepare add myself to be buddy
//        if (aPointBoxConsoleSettings != null){
//            PbcBuddyListSettings[] aPbcBuddyListSettingsList = aPointBoxConsoleSettings.getPbcBuddyListSettings();
//            if ((aPbcBuddyListSettingsList != null)){
//                BuddyListGroupItem[] aBuddyListGroupItemList;
//                BuddyListBuddyItem[] aBuddyListBuddyItemList;
//                for (PbcBuddyListSettings aPbcBuddyListSettings : aPbcBuddyListSettingsList){
//                    if (aPbcBuddyListSettings.getConnectorOwner() != null){
//                        if (GatewayServerType.PBIM_SERVER_TYPE.toString().equalsIgnoreCase(aPbcBuddyListSettings.getConnectorOwner().getGatewayServerType())){
//                            pbimLoginName = aPbcBuddyListSettings.getConnectorOwner().getLoginName();
//                            aBuddyListGroupItemList = aPbcBuddyListSettings.getGroupItems();
//                            if (aBuddyListGroupItemList != null){
//                                for (BuddyListGroupItem aBuddyListGroupItem : aBuddyListGroupItemList){
//                                    aBuddyListBuddyItemList = aBuddyListGroupItem.getBuddyItems();
//                                    if (aBuddyListBuddyItemList != null){
//                                        for (BuddyListBuddyItem aBuddyListBuddyItem : aBuddyListBuddyItemList){
//                                            if (aBuddyListBuddyItem.getBuddyName().equalsIgnoreCase(pbimLoginName)){
//                                                prepareMyselgBuddy = false;
//                                                break;
//                                            }
//                                        }//for
//                                    }
//                                    if (!prepareMyselgBuddy){
//                                        break;
//                                    }
//                                }//for
//                            }
//                        }
//                    }
//                    if (!prepareMyselgBuddy){
//                        break;
//                    }
//                }//for
//            }
//        }//if
//        //do the job...
//        if ((prepareMyselgBuddy) && (pbimLoginName != null)){
//            IGatewayConnectorBuddy pbimiLoginUser = GatewayBuddyListFactory.getLoginUserInstance(pbimLoginName, GatewayServerType.PBIM_SERVER_TYPE);
//            if (pbimiLoginUser instanceof IPbimConnectorBuddy){
//                getKernel().addNewConnectorBuddy(pbimiLoginUser, 
//                                                 GatewayBuddyListFactory.getPbimBuddyGroupInstance(PbBuddyGroupTerms.PbBuddyDefaultGroup.toString(), (IPbimConnectorBuddy)pbimiLoginUser), 
//                                                 pbimiLoginUser.getIMScreenName());
//            }
//        }
//    }
    
//    private void prepareMyselfBuddy(PointBoxConnectorID pbimConnectorOwner, BuddyListGroupItem friendsBuddyListGroupItem, PbcBuddyListSettings pbimPbcBuddyListSettings) {
//        //make it has a friends group
//        if (friendsBuddyListGroupItem == null){
//            friendsBuddyListGroupItem = new BuddyListGroupItem();
//            friendsBuddyListGroupItem.setGroupName(PbBuddyGroupTerms.PbBuddyDefaultGroup.toString());
//            friendsBuddyListGroupItem.setConnectorOwner(pbimConnectorOwner);
//            BuddyListGroupItem[] aBuddyListGroupItemList = pbimPbcBuddyListSettings.getGroupItems();
//            if ((aBuddyListGroupItemList == null) || (aBuddyListGroupItemList.length == 0)){
//                pbimPbcBuddyListSettings.setGroupItems(new BuddyListGroupItem[1]);
//                pbimPbcBuddyListSettings.getGroupItems()[0] = friendsBuddyListGroupItem;
//            }else{
//                BuddyListGroupItem[] aNewBuddyListGroupItemList = new BuddyListGroupItem[aBuddyListGroupItemList.length+1];
//                aNewBuddyListGroupItemList[0] = friendsBuddyListGroupItem;
//                System.arraycopy(aBuddyListGroupItemList, 0, aNewBuddyListGroupItemList, 1, aBuddyListGroupItemList.length);
//                pbimPbcBuddyListSettings.setGroupItems(aNewBuddyListGroupItemList);
//            }
//        }
//        BuddyListBuddyItem[] aBuddyListBuddyItemList = friendsBuddyListGroupItem.getBuddyItems();
//        BuddyListBuddyItem myselfBuddyItem = new BuddyListBuddyItem();
//        myselfBuddyItem.setBuddyName(pbimConnectorOwner.getLoginName());
//        myselfBuddyItem.setBuddyNickName("Pricer");
//        myselfBuddyItem.setServerType(GatewayServerType.PBIM_SERVER_TYPE.toString());
//        myselfBuddyItem.setLoginOwnerName(pbimConnectorOwner.getLoginName());
//        if ((aBuddyListBuddyItemList == null) || (aBuddyListBuddyItemList.length == 0)){
//            friendsBuddyListGroupItem.setBuddyItems(new BuddyListBuddyItem[1]);
//            friendsBuddyListGroupItem.getBuddyItems()[0] = myselfBuddyItem;
//        }else{
//            BuddyListBuddyItem[] aNewBuddyListBuddyItemList = new BuddyListBuddyItem[aBuddyListBuddyItemList.length+1];
//            aNewBuddyListBuddyItemList[0] = myselfBuddyItem;
//            System.arraycopy(aBuddyListBuddyItemList, 0, aNewBuddyListBuddyItemList, 1, aBuddyListBuddyItemList.length);
//            friendsBuddyListGroupItem.setBuddyItems(aNewBuddyListBuddyItemList);
//        }
//    }
    
    /**
     * Notice: [Case 01] there is a small change that the coming-in message came 
     * in before the buddy list (on the tree panel) is ready. [Case 02] Offline 
     * messages buffered on the server-side are loaded before its login happened. 
     * [Case 03] After login, a message from an unknown buddy (i.e., it cannot find 
     * it on the buddy list yet) comes in.
     * <p/>
     * In addition to the above mentioned cases, this handler also handle "loading" 
     * buddy list panel when users log into the IM servers.
     * <p/>
     * This implementation is thread-safe
     */
    public class PointBoxTalkerHandler implements INewBuddyGroupDialogListener{
        
        private final HashMap<String, BufferedQuotesForNewBuddy> aBufferedQuotesForNewBuddyStorage;
        private final HashSet<String> refusedBuddyUniqueNames;
        
        private Thread handlingThread;
        
        public PointBoxTalkerHandler() {
            /**
             * key: a buddy's unique name (BufferedQuotesForNewBuddy's buddy)
             * value: BufferedQuotesForNewBuddy for a buddy
             */
            aBufferedQuotesForNewBuddyStorage = new HashMap<String, BufferedQuotesForNewBuddy>();
            /**
             * buddy's unique names 
             */
            refusedBuddyUniqueNames = new HashSet<String>();
            /**
             * Working thread for handling all the cases
             */
            handlingThread = new Thread(new Runnable(){
                @SuppressWarnings("SleepWhileInLoop")
                @Override
                public void run() {
                    IPbsysInstantMessage msg;
                    while(true){
                        ArrayList<BufferedQuotesForNewBuddy> aBufferedQuotesForNewBuddyList = retrieveBufferedQuotesForNewBuddyList();
                        ArrayList<IPbsysOptionQuote> qts;
                        for (BufferedQuotesForNewBuddy aBufferedQuotesForNewBuddy : aBufferedQuotesForNewBuddyList){
                            if (isReadyForPublishing(aBufferedQuotesForNewBuddy)){
                                //publish all the messages of this buddy...
                                qts = aBufferedQuotesForNewBuddy.getBufferedQuotes();
                                for (IPbsysOptionQuote qt : qts){
                                    msg = qt.getInstantMessage();
                                    //logger.log(Level.INFO, "publish all the offline messages...");
                                    if (msg.isOutgoing()){
                                        getMessagingPaneManager().publishQuoteOnMessageTab(msg.getFromUser(), 
                                                                                           msg.getFromUser().getLoginOwner(), 
                                                                                           qt);
                                    }else{
                                        getMessagingPaneManager().publishQuoteOnMessageTab(msg.getToUser(), 
                                                                                           msg.getFromUser(),//msg.getToUser().getLoginOwner(),   //fixed ticket #330
                                                                                           qt);
                                    }
                                }
                            }else{
                                //confirmed it is an unknown person
                                if (isUnknownBuddy(aBufferedQuotesForNewBuddy)){
                                    final IGatewayConnectorBuddy processingPossibleNewBuddy = aBufferedQuotesForNewBuddy.getPossibleNewBuddy();
                                    final IBuddyListPanel panel = getBuddyListTreePanel(processingPossibleNewBuddy.getLoginOwner());
                                    //avoid blocking by service
                                    if ((panel != null) && (!panel.isBuddyExisted(processingPossibleNewBuddy))){
                                        service.submit(new Runnable(){
                                            @Override
                                            public void run() {
                                                //pop-up dialog to ask user to accept or refuse it....
                                                panel.acceptPossibleNewBuddy(processingPossibleNewBuddy, PointBoxTalkerHandler.this);
                                            }
                                        });
                                    }
                                }
                            }
                        }//for
                        
                        //give a break for the next retrieveBufferedQuotesForNewBuddyList...
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            //out of the while-loop
                            break;
                        }
                    }//while
                }//run

                private boolean isReadyForPublishing(BufferedQuotesForNewBuddy aBufferedQuotesForNewBuddy) {
                    IGatewayConnectorBuddy processingPossibleNewBuddy = aBufferedQuotesForNewBuddy.getPossibleNewBuddy();
                    final IBuddyListPanel panel = getBuddyListTreePanel(processingPossibleNewBuddy.getLoginOwner());
                    if (panel == null){
                        return false;
                    }
                    IGatewayConnectorBuddy existingBuddy = panel.confirmBuddyPresentedInList(processingPossibleNewBuddy);
                    //IGatewayConnectorBuddy existingBuddy = masterBuddyListTreePanel.confirmBuddyPresentedInList(possibleNewBuddy);
                    if (existingBuddy == null){
                        //increase counter for possibleNewBuddy
                        aBufferedQuotesForNewBuddy.setCounter(aBufferedQuotesForNewBuddy.getCounter() + 1);
                        return false;
                    }else{
                        return true;
                    }
                }

                private boolean isUnknownBuddy(BufferedQuotesForNewBuddy aBufferedQuotesForNewBuddy){
                    if (aBufferedQuotesForNewBuddy.getCounter() >= 15){
                        return true;
                    }else{
                        return false;
                    }
                }
            });
        }
        
        private synchronized ArrayList<BufferedQuotesForNewBuddy> retrieveBufferedQuotesForNewBuddyList(){
            ArrayList<BufferedQuotesForNewBuddy> result = new ArrayList<BufferedQuotesForNewBuddy>();
            Collection<BufferedQuotesForNewBuddy> aBufferedQuotesForNewBuddyCollection = aBufferedQuotesForNewBuddyStorage.values();
            if (aBufferedQuotesForNewBuddyCollection != null){
                for (BufferedQuotesForNewBuddy aBufferedQuotesForNewBuddy : aBufferedQuotesForNewBuddyCollection){
                    result.add(aBufferedQuotesForNewBuddy);
                }
            }
            aBufferedQuotesForNewBuddyStorage.clear();
            return result;
        }

        private synchronized void bufferQuoteFromUnknownBuddy(IPbsysOptionQuote quote) {
            if (quote == null){
                return;
            }
            IPbsysInstantMessage msg = quote.getInstantMessage();
            if (msg == null){
                return;
            }
            final IGatewayConnectorBuddy possibleNewBuddy = msg.getFromUser();
            if (possibleNewBuddy == null){
                return;
            }
            String buddyUniqueName = possibleNewBuddy.getIMUniqueName();
            if (DataGlobal.isEmptyNullString(buddyUniqueName)){
                return;
            }
            //check if it is refused by local user
            if (refusedBuddyUniqueNames.contains(buddyUniqueName)){
                //drop his messages...
                return;
            }
            /**
             * Special case: the remote buddy is not on the buddy list yet. There is a 
             * chance that messages continually comes in and pop-up dialog does not get 
             * the decision from the user yet. In this case, only buffer the message
             */
            BufferedQuotesForNewBuddy aBufferedQuotesForNewBuddy = aBufferedQuotesForNewBuddyStorage.get(buddyUniqueName);
            if (aBufferedQuotesForNewBuddy == null) {
                aBufferedQuotesForNewBuddy = new BufferedQuotesForNewBuddy();
                aBufferedQuotesForNewBuddy.setPossibleNewBuddy(possibleNewBuddy);
                aBufferedQuotesForNewBuddy.setCounter(0);
                aBufferedQuotesForNewBuddyStorage.put(buddyUniqueName, aBufferedQuotesForNewBuddy);
            }
            //buffer the data
            aBufferedQuotesForNewBuddy.getBufferedQuotes().add(quote);
        }//bufferMessage

        private synchronized void recordBlackName(IGatewayConnectorBuddy aBlackBuddy) {
            if (aBlackBuddy != null){
                String buddyUniqueName = aBlackBuddy.getIMUniqueName();
                refusedBuddyUniqueNames.add(buddyUniqueName);
                aBufferedQuotesForNewBuddyStorage.remove(buddyUniqueName);
            }
        }

        @Override
        public synchronized void buddyRefusedEventHappened(IGatewayConnectorBuddy buddy) {
            recordBlackName(buddy);
        }
        
        /**
         * fix the bug (First Message From New Buddy Might Be Missed) 
         * load the previous messages to the buddy tabs.
         * 
         * @return 
         */
        private synchronized  List<IPbsysOptionQuote> getBufferedMessagesForNewBuddies(IGatewayConnectorBuddy buddy) {
            BufferedQuotesForNewBuddy aBufferedQuotesForNewBuddy = aBufferedQuotesForNewBuddyStorage.get(buddy.getIMUniqueName());
            if (aBufferedQuotesForNewBuddy == null){
                return new ArrayList<IPbsysOptionQuote>();
            }else{
                return DataGlobal.copyObjectList(aBufferedQuotesForNewBuddy.getBufferedQuotes());
            }
        }

        private synchronized void startHandler(){
            if (handlingThread != null){
                handlingThread.start();
            }
        }
        
        private synchronized void stopHandler() {
            if (handlingThread != null){
                handlingThread.interrupt();
                handlingThread = null;
            }
        }
    }//class PointBoxTalkerHandler
    
    /**
     * Data structure used for PointBoxTalkerHandler
     */
    private class BufferedQuotesForNewBuddy{
        private IGatewayConnectorBuddy possibleNewBuddy = null;
        private int counter = 0;    //how many times this person has been checked for readiness
        private  ArrayList<IPbsysOptionQuote> bufferedQuotes = new ArrayList<IPbsysOptionQuote>();

        public IGatewayConnectorBuddy getPossibleNewBuddy() {
            return possibleNewBuddy;
        }

        public void setPossibleNewBuddy(IGatewayConnectorBuddy possibleNewBuddy) {
            this.possibleNewBuddy = possibleNewBuddy;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        public ArrayList<IPbsysOptionQuote> getBufferedQuotes() {
            return bufferedQuotes;
        }

        public void setBufferedQuotes(ArrayList<IPbsysOptionQuote> bufferedQuotes) {
            this.bufferedQuotes = bufferedQuotes;
        }
    }//class BufferedQuotesForNewBuddy
}
