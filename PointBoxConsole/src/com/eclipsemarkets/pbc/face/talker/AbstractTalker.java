/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.web.ConnectorLiveness;
import com.eclipsemarkets.gateway.web.ConnectorLivenessQuery;
import com.eclipsemarkets.pbc.PbcText;
import com.eclipsemarkets.pbc.PbcTextKey;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.IServerLoginWindow;
import com.eclipsemarkets.pbc.web.local.IPointBoxWebAgentListener;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.PointBoxAccountID;
import com.eclipsemarkets.web.PointBoxServiceResult;
import com.eclipsemarkets.web.PointBoxWebServiceResponse;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang
 */
public abstract class AbstractTalker implements IPbcTalker, IPointBoxWebAgentListener{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(AbstractTalker.class.getName());
    }

    /**
     * Owner of this talker
     */
    final IPbcFace face;
    
    /**
     * prevent two dialogs appearing at the same time to confuse users
     */
    private boolean localServerCrashFlag = true;
    private boolean centralServerCrashFlag = true;
    
    /**
     * Hold buddy list panel of specific login users
     */
    private final BuddyListTreePanelStore buddyListTreePanelStore;
    
    WelcomeLoginFrame welcomeLoginFrame;
    private ServerLoginDialog aolLoginDialog;
    private ServerLoginDialog yahooLoginDialog;
    
    private final ArrayList<IPointBoxTalkerComponent> talkerComponents;
    
    /**
     * Loop for monitor login connectors liveness
     */
    private CentralGatewayLivenessMonitor centralGatewayLivenessMonitor;

    public AbstractTalker(IPbcFace face) {
        this.face = face;
        
        centralGatewayLivenessMonitor = null;
        
        welcomeLoginFrame = null;
        aolLoginDialog = null;
        yahooLoginDialog = null;

        talkerComponents = new ArrayList<IPointBoxTalkerComponent>();
        
        buddyListTreePanelStore = new BuddyListTreePanelStore();
    }
    
    /**
     * This method can be called after PointBoxAccountID being ready
     */
    @Override
    public synchronized void startCentralGatewayLivenessMonitor(){
        if ((centralGatewayLivenessMonitor == null) || (!centralGatewayLivenessMonitor.isAlive())){
            centralGatewayLivenessMonitor = new CentralGatewayLivenessMonitor();
            centralGatewayLivenessMonitor.start();
        }
    }
    
    synchronized void stopCentralGatewayLivenessMonitor(){
        if (centralGatewayLivenessMonitor != null){
            centralGatewayLivenessMonitor.interrupt();
            centralGatewayLivenessMonitor = null;
        }
    }

    @Override
    public void displayOfflineBuddies(boolean value) {
        buddyListTreePanelStore.displayOfflineBuddies(value);
    }

    @Override
    public IPbcFace getFace() {
        return face;
    }

    @Override
    public HashMap<IGatewayConnectorBuddy, String> retrieveOriginalServerSideBuddyList(IGatewayConnectorBuddy loginUser) {
        return face.getKernel().getPointBoxConsoleWeb().retrieveOriginalServerSideBuddyList(loginUser);
    }

    @Override
    public ArrayList<PbcBuddyListSettings> getRegularPbcBuddyListSettingsOfLiveConnectors() {
        return buddyListTreePanelStore.getRegularPbcBuddyListSettingsOfLiveConnectors();
    }
    
    @Override
    public void addTalkerComponents(IPointBoxTalkerComponent component){
        synchronized(talkerComponents){
            if (!talkerComponents.contains(component)){
                talkerComponents.add(component);
            }
        }
    }
    
    @Override
    public void removeTalkerComponents(IPointBoxTalkerComponent component){
        synchronized(talkerComponents){
            talkerComponents.remove(component);
        }
    }

    @Override
    public void releaseFaceComponent() {
        synchronized(talkerComponents){
            for (IPointBoxTalkerComponent talkerComponent : talkerComponents){
                talkerComponent.release();
            }
        }
    }
    
    @Override
    public void initializeLoginWindows(){
        if (welcomeLoginFrame == null){
            welcomeLoginFrame = WelcomeLoginFrame.getInstance(this);
        }
        if (aolLoginDialog == null){
            aolLoginDialog = ServerLoginDialog.getInstance(this, GatewayServerType.AIM_SERVER_TYPE);
        }
        if (yahooLoginDialog == null){
            yahooLoginDialog = ServerLoginDialog.getInstance(this, GatewayServerType.YIM_SERVER_TYPE);
        }
    }

    @Override
    public void loginPreviousPersistentConnector(IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            return;
        }
        if ((aolLoginDialog != null) && (GatewayServerType.AIM_SERVER_TYPE.equals(loginUser.getIMServerType()))){
            aolLoginDialog.loginPreviousPersistentConnector(loginUser);
        }else if ((yahooLoginDialog != null) && (GatewayServerType.YIM_SERVER_TYPE.equals(loginUser.getIMServerType()))){
            yahooLoginDialog.loginPreviousPersistentConnector(loginUser);
        }
    }

    @Override
    public IServerLoginWindow getLoginWindow(GatewayServerType gatewayServerType) {
        switch (gatewayServerType){
            case AIM_SERVER_TYPE:
                return aolLoginDialog;
            case PBIM_SERVER_TYPE:
                return welcomeLoginFrame;
            case YIM_SERVER_TYPE:
                return yahooLoginDialog;
            default:
                return null;
        }
    }

    /**
     * This method is to handle the situations of PointBox local server crashing.
     */
    private void handlePointBoxServerWithoutResponseEvent(){
        getKernel().getPointBoxConsoleWeb().stopWebServiceThreads();
        stopCentralGatewayLivenessMonitor();
        if (SwingUtilities.isEventDispatchThread()){
            handlePointBoxServerWithoutResponseEventHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handlePointBoxServerWithoutResponseEventHelper();
                }
            });
        }
    }
    
    private void handlePointBoxServerWithoutResponseEventHelper(){
        if(localServerCrashFlag){   //prevent two dialogs appearing at the same time to confuse users
            centralServerCrashFlag=false;
            localServerCrashFlag=false;
            if (JOptionPane.showConfirmDialog(face.getPointBoxMainFrame(), 
                    PbcText.getSingleton().getText(PbcTextKey.Lost_PB_Servers),
                    PbcText.getSingleton().getText(PbcTextKey.Confirm),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                //face.getKernel().shutdown(face.getPointBoxLoginUser());
                face.getKernel().shutdown(face.getPointBoxLoginUser(), false, true);
            }else{
                face.getKernel().getPointBoxConsoleWeb().requestLogoutPointBoxSystem(face.getKernel().getPointBoxAccountID());
                aolLoginDialog.closeAllConnectors();
                yahooLoginDialog.closeAllConnectors();
                closeBuddyListPanelForLoginUser(face.getKernel().getPointBoxLoginUser());
                welcomeLoginFrame.display();
            }
        }
    }

    @Override
    public void closeAllPublicConnectors() {
        aolLoginDialog.closeAllConnectors();
        yahooLoginDialog.closeAllConnectors();
    }
//
//    private void handleCentralServerCrashedEvent(final String message){
//        getKernel().getPointBoxConsoleWeb().stopWebServiceThreads();
//        stopCentralGatewayLivenessMonitor();
//        if (SwingUtilities.isEventDispatchThread()){
//            handleCentralServerCrashedEventHelper(message);
//        }else{
//            SwingUtilities.invokeLater(new Runnable(){
//                @Override
//                public void run() {
//                    handleCentralServerCrashedEventHelper(message);
//                }
//            });
//        }
//    }
//    
//    private void handleCentralServerCrashedEventHelper(String message){
//        
////        // get the pbc main frame reference
////        JFrame tempFrame = face.getPointBoxMainFrame();
////        // reset the title
////        tempFrame.setTitle(tempFrame.getTitle()+" [WORKING OFFLINE!]");
//        
//        if(centralServerCrashFlag){         //prevent two dialogs appearing at the same time to confuse users
//            localServerCrashFlag = false;
//            centralServerCrashFlag = false;
//            if (JOptionPane.showConfirmDialog(face.getPointBoxMainFrame(), 
//    //                "PointBox central servers are not responsive now. " + NIOGlobal.lineSeparator()
//    //                + "Keep this console offline and shut it down later to re-connect to the central server (YES)? " + NIOGlobal.lineSeparator()
//    //                + "Or Shut it down now (No)?",
//    //                + "Keep this console offline (Yes) Or Shut it down now (No)?",
//    //                "Confirm",
//                    message+ NIOGlobal.lineSeparator()
//                    +"Would you like to shut down now?",
//                    "Confirm",
//                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
//            {
//                //face.getKernel().shutdown(face.getPointBoxLoginUser());
//                face.getKernel().shutdown(face.getPointBoxLoginUser(), false);        //Donot pop out shut-down warning dialog for this situation.
//            }
//        }
//    }
//    
//    @Override
//    public void pointBoxCentralServerWebServiceExceptionHappened(String message) {
//        handleCentralServerCrashedEvent(message);
//    }
//    
//    @Override
//    public void pointBoxCentralServerRuntimeExceptionHappened(RuntimeException ex) {
//        //handleLocalServerCrashedEvent();
//    }
    
    @Override
    public void pointBoxServerWithoutResponseEventHappened(String message) {
        handlePointBoxServerWithoutResponseEvent();
    }

//    /**
//     * 
//     * @param ex 
//     * @deprecated - replaced by pointBoxServerWithoutResponseEventHappened
//     */
//    @Override
//    public void pointBoxLocalServerRuntimeExceptionHappened(RuntimeException ex) {
//        handlePointBoxServerWithoutResponseEvent();
//    }
//
//    /**
//     * 
//     * @param ex 
//     * @deprecated - replaced by pointBoxServerWithoutResponseEventHappened
//     */
//    @Override
//    public void pointBoxLocalServerWebServiceExceptionHappened(WebServiceException ex) {
//        handlePointBoxServerWithoutResponseEvent();
//    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getAllAvaialbleBuddies(boolean sort) {
        return buddyListTreePanelStore.getAllAvaialbleBuddies(sort);
    }

    /**
     * @return the aolLoginDialog
     */
    public ServerLoginDialog getAolLoginDialog() {
        return aolLoginDialog;
    }

    /**
     * @return the yahooLoginDialog
     */
    public ServerLoginDialog getYahooLoginDialog() {
        return yahooLoginDialog;
    }
//////
//////    /**
//////     * @return the buddyListTreePanelStore
//////     */
//////    public BuddyListTreePanelStore getBuddyListTreePanelStore() {
//////        return buddyListTreePanelStore;
//////    }
    
    @Override
    public boolean isBuddyListPanelOnPBC(IGatewayConnectorBuddy loginUser){
        return true;
    }

    @Override
    public ArrayList<IBuddyListPanel> getAllBuddyListTreePanels() {
        return buddyListTreePanelStore.getAllBuddyListTreePanels();
    }

    IBuddyListPanel getBuddyListTreePanel(IGatewayConnectorBuddy loginUser) {
        return buddyListTreePanelStore.getBuddyListTreePanel(loginUser);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getCurrentLoginUsers() {
        return buddyListTreePanelStore.getAllLoginUsers();
    }
    
    /**
     * @return 
     */
    @Override
    public ArrayList<IBuddyListPanel> getBuddyListTreePanels(GatewayServerType serverType){
        return buddyListTreePanelStore.getBuddyListTreePanels(serverType);
    }

    /**
     * if there is a panel whose key is the same as the pass-in buddyListTreePanel, 
     * the existing one will be returned. Otherwise, the pass-in buddyListTreePanel 
     * will be returned.
     * @param aBuddyListPanel
     * @return 
     */
    IBuddyListPanel insertPanelIntoStorage(IBuddyListPanel aBuddyListPanel) {
        getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettingsVisibility(aBuddyListPanel, true);
        return buddyListTreePanelStore.insertPanel(aBuddyListPanel);
    }

    void removePanelFromStorage(IBuddyListPanel aBuddyListPanel) {
        getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettingsVisibility(aBuddyListPanel, false);
        buddyListTreePanelStore.removePanel(aBuddyListPanel);
    }
    
    /**
     * Keep eyes at the liveness of central server and PBIM account connection status. This does not check if any 
     * connectors lost their connections.
     * <p/>
     * PointBoxCentral:
     * <p/>
     * Clients should continually (suggested every min) ping this server. It will tell server-side the client-side is still there. 
     * And, meanwhile, the server-side can tell client what happened on the server-side, e.g. conflicted-killing session. If the 
     * server have no information from the client, the server will treat this client as a dead client. And also, if the server 
     * found this client was forced out by another conflict-login, server will logout this client and notify the client-side this 
     * event happened.
     * 
     */
    private class CentralGatewayLivenessMonitor extends Thread {
        private final long interval = 60*1000; //1 min
        private final int failureMax = 2;

        public CentralGatewayLivenessMonitor() {
        }
        
        @Override
        public void run() {
            PointBoxAccountID accountID;
            //accountID possibly is null when the user did not successfully log in yet
            do {
                accountID = face.getKernel().getPointBoxAccountID();
                try {
                    Thread.sleep(interval*3);
                } catch (InterruptedException ex) {
                    accountID = null;
                }
            }while (accountID == null);
            
            if (accountID == null){
                /**
                 * Currently just record it for debug without anything else
                 */
                PointBoxTracer.recordSevereException(logger, 
                        new Exception("Unexpected web response: CentralGatewayLivenessMonitor "
                                    + "has waited for PointBoxAccountID being ready for " + interval 
                                    + " minutes. It is timed out for this thread."));
                return;
            }
            
            IGatewayConnectorBuddy loginUser = getKernel().getPointBoxLoginUser();
            
            ConnectorLivenessQuery connectorLivenessQuery = new ConnectorLivenessQuery();
            connectorLivenessQuery.setPointBoxAccountName(loginUser.getIMScreenName());
            
            ConnectorLiveness query = new ConnectorLiveness();
            query.setConnectorLoginName(loginUser.getIMScreenName());
            query.setGatewayServerType(loginUser.getIMServerType().toString());
            
            ConnectorLiveness[] queryFields = new ConnectorLiveness[1];
            queryFields[0] = query;
            connectorLivenessQuery.setQueries(queryFields);
            
            int failureCounter = 0;
            while(true){
                PointBoxWebServiceResponse response = null;
                try{
                    response = getKernel().checkGatewayLiveness(accountID, connectorLivenessQuery);
                }catch (RuntimeException ex){
                    //PointBoxTracer.displayMessage(logger, ex);
                    failureCounter++;
                    //notice if it keeps trouble too long, it will stop this thread
                    if (failureCounter > failureMax){
                        getKernel().getPointBoxConsoleWeb().firePointBoxServerWithoutResponseEvent(PbcText.getSingleton().getText(PbcTextKey.Lost_PB_Servers));
                        PointBoxTracer.recordSevereException(logger, ex);
                        break;
                    }
                }
                if (response != null){
                    if (PointBoxServiceResult.ForcedOutOfSession.equals(response.getResult())){
                        /**
                         * The server-side force out current connection to PointBox system because of another same account log in from 
                         * other places or stations. And that account requested "Force Login" to kill current connection of this PBC 
                         * instance.
                         */
                        getKernel().getPointBoxConsoleWeb().firePointBoxServerWithoutResponseEvent(PbcText.getSingleton().getText(PbcTextKey.Lost_PB_Servers));
                    }else if (PointBoxServiceResult.ForcedOutOfSessionWithLogoutRequired.equals(response.getResult())){
                        /**
                         * The server-side force out current connection to PointBox system because of another same account log in from 
                         * other places or stations. And that account requested "Force Login" to kill current connection of this PBC 
                         * instance. In this case, this PBC has to log out from the system in silence.
                         */
                        getKernel().getPointBoxConsoleWeb().firePointBoxServerWithoutResponseEvent(PbcText.getSingleton().getText(PbcTextKey.Lost_PB_Servers));
                    }else{
                        if (!PointBoxServiceResult.RequestExecuted.equals(response.getResult())){
                            /**
                             * Currently just record it for debug without anything else
                             */
                            PointBoxTracer.recordSevereException(logger, 
                                    new Exception("Unexpected web response: " + response.getResult() + "::" + response.getDescription()));
                        }
                    }
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    //logger.log(Level.WARNING, ex.getMessage(), ex);
                    break;
                }
            }//while(true)
        }
    }
}
