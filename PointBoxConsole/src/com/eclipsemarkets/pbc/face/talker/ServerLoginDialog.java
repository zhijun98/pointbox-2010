/**
 * Eclipse Market Solutions LLC
 */
/*
 * ServerLoginDialog.java
 *
 * @author Zhijun Zhang
 * Created on May 17, 2010, 12:06:07 PM
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.PbcLoginSettingsChanged;
import com.eclipsemarkets.event.face.talker.TalkerConnectionLostEvent;
import com.eclipsemarkets.event.gateway.GatewayConnectionEvent;
import com.eclipsemarkets.event.gateway.ServerLoginStatusEvent;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.web.ConnectionEventHappened;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.IPbcSplashScreen;
import com.eclipsemarkets.pbc.PbcText;
import com.eclipsemarkets.pbc.PbcTextKey;
import com.eclipsemarkets.pbc.face.IServerLoginWindow;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.PointBoxConnectorID;
import com.eclipsemarkets.web.PointBoxLoginPublicImServiceResponse;
import com.eclipsemarkets.web.PointBoxServiceResult;
import com.eclipsemarkets.web.PointBoxWebServiceResponse;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import java.awt.Color;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Zhijun Zhang
 */
public class ServerLoginDialog extends javax.swing.JFrame implements IServerLoginWindow, IPbcSplashScreen 
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(ServerLoginDialog.class.getName());
    }
    /**
     * Owner of this dialog
     */
    private final IPbcTalker talker;

    /**
     * Type of connectors for this login dialog
     */
    private final GatewayServerType targetServerType;

    /**
     * Key: screenName (not unique name)
     * Value: password
     */
    private final HashMap<String, LoginUserWrapper> loginUserWrappers;

    /**
     * Default message displayed on the dialog
     */
    private final String defaultMessage;

    /**
     * Creates new form ServerLoginDialog
     * @param system
     * @param pointBoxFrame
     * @param modal
     * @param customizer
     */
    private ServerLoginDialog(final IPbcTalker talker,
                              final GatewayServerType targetServerType)
    {   
        initComponents();
        this.talker = talker;
        this.targetServerType = targetServerType;
        loginUserWrappers = new LinkedHashMap<String, LoginUserWrapper>();
        defaultMessage = "Note: Don't save your login on a public computer.";

        jConnectedAccountNames.setModel(new DefaultListModel());
        jConnectedAccountNames.addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object obj = getjConnectedAccountNames().getSelectedValue();
                if (obj instanceof LoginUserWrapper){
                    populateLoginUserWrapperSettingsHelper((LoginUserWrapper)obj);
                }
            }
        });
        jProgressBar.setIndeterminate(false);
        
        jAutoLogin.setVisible(false);
        jAutoLogin.setSelected(false);
        jAutoLogin.setEnabled(false);
        
        setIconImage(getRuntime().getPbcImageSettings().getPointBoxIcon().getImage());
    }
    
    /**
     * Memorized credentials for current server-type under current PBC account login. 
     * key: login name
     * value: password
     */
    private final HashMap<String, String> credentials = new HashMap<String, String>();

    private void cacheCredentials(IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            return;
        }
        synchronized(credentials){
            credentials.put(loginUser.getIMScreenName(), loginUser.getIMPassword());
        }
    }

    private void decacheCredentials(String screenName) {
        synchronized(credentials){
            credentials.remove(screenName);
        }
    }
    
    @Override
    public void loadCredentials(){
        synchronized(credentials){
            credentials.clear();
            ArrayList<PbcBuddyListSettings> aPbcBuddyListSettingsList = getRuntime().getPbcBuddyListSettings();
            PointBoxConnectorID aPointBoxConnectorID;
            for (PbcBuddyListSettings aPbcBuddyListSettings : aPbcBuddyListSettingsList){
                aPointBoxConnectorID = aPbcBuddyListSettings.getConnectorOwner();
                if ((aPointBoxConnectorID != null) && (aPointBoxConnectorID.getGatewayServerType().equalsIgnoreCase(targetServerType.toString()))){
                    credentials.put(aPointBoxConnectorID.getLoginName(), aPointBoxConnectorID.getPassword());
                }
            }//for
        }
    }
    
    private String getPasswordFromCredentials(String screenName){
        synchronized(credentials){
            if (this.jSavePassword.isSelected()){
                return DataGlobal.denullize(credentials.get(screenName));
            }else{
                return "";
            }
        }
    }
    
    private IPbcRuntime getRuntime(){
        return talker.getKernel().getPointBoxConsoleRuntime();
    }

    public static ServerLoginDialog getInstance(IPbcTalker talker,
                                                GatewayServerType targetServerType)
    {
        ServerLoginDialog loginDialog = new ServerLoginDialog(talker, targetServerType);

        loginDialog.initializeServerLoginDialog();
        loginDialog.populateLoginWindowSettings();

        return loginDialog;
    }

    @Override
    public void loadAnonymousLoginSettings() {
        //todo: zzj - initializeServerLoginDialog();
    }

    /**
     * initialize default values (before login)
     */
    private void initializeServerLoginDialog(){
        if (SwingUtilities.isEventDispatchThread()){
            initializeServerLoginDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    initializeServerLoginDialogHelper();
                }
            });
        }
    }
    private void initializeServerLoginDialogHelper(){
        setTitle("PointBox Console");
        jServerLabel.setText("Server:");
        ((TitledBorder)jBasePanel.getBorder()).setTitle("Login to " + targetServerType);

        jServerList.addItem("default");
        jScreenNames.setEditable(true);
        jPassword.setEditable(true);

        this.jPassword.setText("");
        this.jRememberMe.setSelected(false);
        this.jSavePassword.setSelected(false);
        this.jSavePassword.setEnabled(false);
//        this.jAutoLogin.setSelected(false);
//        this.jAutoLogin.setEnabled(false);

        jMessage.setBackground(this.getContentPane().getBackground());
        jMessage.setForeground(Color.black);
        jMessage.setText(defaultMessage);
        
        jScreenNames.setSelectedIndex(-1);
        jScreenNames.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(jScreenNames.getSelectedItem()!=null){
                    jPassword.setText(getPasswordFromCredentials(jScreenNames.getSelectedItem().toString()));
                }
            }
        });
        
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                hideFaceComponent();
            }
        });

        pack();
        setResizable(false);
        //setModal(true);
        super.setVisible(false);
    }
    
    
     private void populatejScreenNames() {
        if (SwingUtilities.isEventDispatchThread()){
            populateScreenNamesHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateScreenNamesHelper();
                }
            });
        }
    }
    private void populateScreenNamesHelper() {
        (new SwingWorker<DefaultComboBoxModel, Void>(){
            @Override
            protected DefaultComboBoxModel doInBackground() throws Exception {
                return constructModelFromCredentials();
            }

            @Override
            protected void done() {
                try {
                    DefaultComboBoxModel model = get();
                    if (model == null){
                        jScreenNames.setModel(new DefaultComboBoxModel());
                        jPassword.setText("");
                    }else{
                        jScreenNames.setModel(model);
                        if(jScreenNames.getModel().getSize()<=0){
                            jScreenNames.setSelectedIndex(-1);
                            jPassword.setText("");
                            jRemove.setEnabled(false);
                        }else{
                            jScreenNames.setSelectedIndex(jScreenNames.getItemCount() - 1);
                            if (jSavePassword.isSelected()){
                                jPassword.setText(getPasswordFromCredentials(jScreenNames.getSelectedItem().toString()));
                            }
                            jRemove.setEnabled(true);
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerLoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                    jScreenNames.setModel(new DefaultComboBoxModel());
                    jPassword.setText("");
                } catch (ExecutionException ex) {
                    Logger.getLogger(ServerLoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                    jScreenNames.setModel(new DefaultComboBoxModel());
                    jPassword.setText("");
                }
            }

            private DefaultComboBoxModel constructModelFromCredentials() {
                DefaultComboBoxModel model = new DefaultComboBoxModel();
                if (jRememberMe.isSelected()){
                    synchronized(credentials){
                        Set<String> screenNames = credentials.keySet();
                        for (String name : screenNames){
                            model.addElement(name.trim());
                        }
                    }
                }
                return model;
            }
        }).execute();
    }

    /**
     * Current selected or the one with lowest index value in the login user list. If empty, NULL returned
     * @return 
     */
    @Override
    public IGatewayConnectorBuddy getCurrentLoginUser() {
        ListModel model = jConnectedAccountNames.getModel();
        if (model.getSize() == 0){
            return null;
        }else{
            if (jConnectedAccountNames.getSelectedValue() instanceof String){
                return GatewayBuddyListFactory.getLoginUserInstance(jConnectedAccountNames.getSelectedValue().toString(), 
                                                                targetServerType);
            }else{
                return GatewayBuddyListFactory.getLoginUserInstance(model.getElementAt(0).toString(), 
                                                                targetServerType);
            }
        }
    }

    private void populateLoginWindowSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            populateLoginWindowSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateLoginWindowSettingsHelper();
                }
            });
        }
    }
    private void populateLoginWindowSettingsHelper(){
        if (PointBoxConsoleProperties.getSingleton().isLoginWindowRememberMe(targetServerType)){
            //this.jScreenName.setText(PointBoxConsoleProperties.getSingleton().retrieveLoginWindowLoginName(targetServerType));
            this.jRememberMe.setSelected(true);
            this.jRememberMe.setEnabled(true);
            this.jSavePassword.setEnabled(true);
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowSavePassword(targetServerType)){
                this.jSavePassword.setSelected(true);
                //this.jAutoLogin.setEnabled(true);
//                if (PointBoxConsoleProperties.getSingleton().isLoginWindowAutoLogin(targetServerType)){
//                    this.jAutoLogin.setSelected(true);
//                }else{
//                    this.jAutoLogin.setSelected(false);
//                }
            } else {
                this.jPassword.setText("");
                this.jSavePassword.setSelected(false);
//                this.jAutoLogin.setSelected(false);
//                this.jAutoLogin.setEnabled(false);
            }
        }else{
            this.jScreenNames.setSelectedIndex(-1);
            this.jPassword.setText("");
            this.jRememberMe.setSelected(false);
            this.jSavePassword.setSelected(false);
            this.jSavePassword.setEnabled(false);
//            this.jAutoLogin.setSelected(false);
//            this.jAutoLogin.setEnabled(false);
        }
    }

    @Override
    public void setVisible(boolean value) {
        if (value){
            displayFaceComponent();
        }else{
            hideFaceComponent();
        }
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
    public void updateSplashScreen(final String msg, final Level level, final long latency) {
        if (SwingUtilities.isEventDispatchThread()){
            jMessage.setText(msg);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jMessage.setText(msg);
                }
            });
        }
    }

    @Override
    public void displayFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            displayFaceComponentHelper(null);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayFaceComponentHelper(null);
                }
            });
        }
    }

    private void displayFaceComponentHelper(String msg){
        IGatewayConnectorBuddy pbimLoginUser = talker.getPointBoxLoginUser();
        if ((pbimLoginUser == null) || (!(BuddyStatus.Online.equals(pbimLoginUser.getBuddyStatus())))){
            JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), "Please log in PointBox console first.");
            return;
        }
        talker.getKernel().switchPbcSplashScreen(this);
        jClose.setEnabled(true);
        jCloseAll.setEnabled(true);
        jLoginBtn.setEnabled(true);
        jScreenNames.setEnabled(true);
        jPassword.setEnabled(true);
        jCancelBtn.setEnabled(true);
        jProgressBar.setIndeterminate(false);
        jMessage.setForeground(Color.black);
        jMessage.setText(defaultMessage);

        populateLoginWindowSettings();
        populatejScreenNames();
              
        pack();
        
        /**
         * TRAC-259: comment-out the following "getCenterPointOfParentWindow". Somehow, 
         * only on the primary screen, there is no black issue for some types of Monitor or 
         * graphics driver. on the secondary monitor screen, some graphics driver can 
         * not handle it correctly
         */
//        setLocation(SwingGlobal.getCenterPointOfParentWindow(talker.getPointBoxFrame(), this));
        this.setLocation(SwingGlobal.getScreenCenterPoint(this));
        
        super.setVisible(true);
        
        if (DataGlobal.isNonEmptyNullString(msg)){
            JOptionPane.showMessageDialog(this, msg);
        }
    }
    
    

    @Override
    public void hideFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            hideFaceComponentHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    hideFaceComponentHelper();
                }
            });
        }
    }

    private void hideFaceComponentHelper(){
        if (talker.getKernel().getPointBoxMainFrame() instanceof IPbcSplashScreen){
            talker.getKernel().switchPbcSplashScreen((IPbcSplashScreen)talker.getKernel().getPointBoxMainFrame());
        }
        
        super.setVisible(false);
        
        jClose.setEnabled(true);
        jCloseAll.setEnabled(true);
        jLoginBtn.setEnabled(true);
        jScreenNames.setEnabled(true);
        jPassword.setEnabled(true);
        jCancelBtn.setEnabled(true);
        jProgressBar.setIndeterminate(false);
        jMessage.setForeground(Color.black);
        jMessage.setText(defaultMessage);
    }

    @Override
    public void personalizeFaceComponent() {
        
    }

    @Override
    public void releaseFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            dispose();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    dispose();
                }
            });
        }
    }

    /**
     * How many current live connections
     * @return
     */
    @Override
    public int connectionCount(){
        synchronized (jConnectedAccountNames.getModel()){
            return jConnectedAccountNames.getModel().getSize();
        }
    }

    private void loginConnector(){
        if (SwingUtilities.isEventDispatchThread()){
            loginConnectorHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    loginConnectorHelper();
                }
            });
        }
    }
    
    private void loginConnectorHelper(){
        //(1) basic validation
        if(jScreenNames.getSelectedItem()==null){
            jMessage.setForeground(Color.red);
            jMessage.setText("Please type in a valid screen name which cannot be empty.");
            return;
        }
        
        String accountName = jScreenNames.getSelectedItem().toString().trim();
        String password = new String(jPassword.getPassword()).trim();
        if (DataGlobal.isEmptyNullString(accountName)){
            jMessage.setForeground(Color.red);
            jMessage.setText("Please type in a valid screen name which cannot be empty.");
            return;
        }
        if (DataGlobal.isEmptyNullString(password)){
            jMessage.setForeground(Color.red);
            jMessage.setText("Please type in a valid password which cannot be empty.");
            return;
        }
        //(2) check if it is connected
        synchronized (jConnectedAccountNames.getModel()){
            //check if it has been there in jConnectedAccountNames
            int total = jConnectedAccountNames.getModel().getSize();
            LoginUserWrapper wrapper;
            for (int  i = 0; i < total; i++){
                wrapper = (LoginUserWrapper)jConnectedAccountNames.getModel().getElementAt(i);
                if (wrapper.getLoginUser().getIMScreenName().equalsIgnoreCase(accountName)){
                    JOptionPane.showMessageDialog(ServerLoginDialog.this, "Account - " + accountName + " has been connected. Please choose another one.");
                    return;
                }
            }
        }
        //(3) prepare GUI
        jClose.setEnabled(false);
        jCloseAll.setEnabled(false);
        jLoginBtn.setEnabled(false);
        jScreenNames.setEnabled(false);
        jPassword.setEnabled(false);
        jCancelBtn.setEnabled(true);
        jProgressBar.setIndeterminate(true);
        jMessage.setForeground(Color.blue);
        jMessage.setText("Start connecting to " + targetServerType);
        
        IGatewayConnectorBuddy loginUser = GatewayBuddyListFactory.getLoginUserInstance(accountName, 
                                                                 targetServerType);
        loginUser.setLoginOwner(talker.getPointBoxLoginUser());
        loginUser.setIMPassword(password);
        
        //(4) prepare buddy list panel for this loginUSer
        loginUser.setBuddyStatus(BuddyStatus.Online);

        //(5)launch the login procedure...
        (new LoginConnectorWorker(loginUser)).execute();
    }

    /**
     * When users choose auto-login from welcome-login window, it may automatically 
     * log into previous connections whose buddy list panels are still visible.
     * @param loginUser 
     */
    void loginPreviousPersistentConnector(IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            return;
        }
        String accountName = loginUser.getIMScreenName();
        if (DataGlobal.isEmptyNullString(accountName)){
            return;
        }
        //(1) load credentials
        this.loadCredentials();
        loginUser.setIMPassword(getPasswordFromCredentials(loginUser.getIMScreenName()));
        
        //(2) check if it is connected
        synchronized (jConnectedAccountNames.getModel()){
            //check if it has been there in jConnectedAccountNames
            int total = jConnectedAccountNames.getModel().getSize();
            LoginUserWrapper wrapper;
            for (int  i = 0; i < total; i++){
                wrapper = (LoginUserWrapper)jConnectedAccountNames.getModel().getElementAt(i);
                if (wrapper.getLoginUser().getIMScreenName().equalsIgnoreCase(accountName)){
                    return;
                }
            }
        }
        //(3) prepare GUI
        jScreenNames.addItem(loginUser.getIMScreenName());
        jPassword.setText(loginUser.getIMPassword());
        
        jClose.setEnabled(false);
        jCloseAll.setEnabled(false);
        jLoginBtn.setEnabled(false);
        jScreenNames.setEnabled(false);
        jPassword.setEnabled(false);
        jCancelBtn.setEnabled(true);
        jProgressBar.setIndeterminate(true);
        jMessage.setForeground(Color.blue);
        jMessage.setText("Start connecting to " + targetServerType);
        
        loginUser.setLoginOwner(talker.getPointBoxLoginUser());
        
        //(4) prepare buddy list panel for this loginUSer
        loginUser.setBuddyStatus(BuddyStatus.Online);
        
        (new LoginConnectorWorker(loginUser)).execute();
    }

    @Override
    public void handlePointBoxEvent(PointBoxConsoleEvent event) {
        if (event instanceof GatewayConnectionEvent){
            handleGatewayConnectionEvent((GatewayConnectionEvent)event);
        }else if (event instanceof ServerLoginStatusEvent){
            publishLoginStatus((ServerLoginStatusEvent)event);
        }else if (event instanceof PbcLoginSettingsChanged){
            if (((PbcLoginSettingsChanged)event).getGatewayServerType().equals(targetServerType)){
                populateLoginWindowSettings();
            }
        }else if (event instanceof TalkerConnectionLostEvent){
            handleTalkerConnectionLostEvent(((TalkerConnectionLostEvent)event).getLoginUserUnqiueNamesWithoutConnection());
        }
    }

    private void handleTalkerConnectionLostEvent(List<String> loginUserUnqiueNamesWithoutConnection) {
        if ((loginUserUnqiueNamesWithoutConnection == null) || (loginUserUnqiueNamesWithoutConnection.isEmpty())){
            return;
        }
        try{
            /**
             * Prepare connection check list which are possibly lost
             */
            HashMap<String, LoginUserWrapper> loginUserWrapperList = new HashMap<String, LoginUserWrapper>();
            ListModel model = jConnectedAccountNames.getModel();
            LoginUserWrapper loginUserWrapperLosingConnection;
            if (model != null){
                for (int i = 0; i < model.getSize(); i++){
                    loginUserWrapperLosingConnection = (LoginUserWrapper)model.getElementAt(i);
                    loginUserWrapperList.put(loginUserWrapperLosingConnection.getLoginUser().getIMUniqueName(), loginUserWrapperLosingConnection);
                }//for
            }
            /**
             * no any login yet for this server-type. And simply do nothing and return;
             */
            if (loginUserWrapperList.isEmpty()){
                return;
            }
            /**
             * Process connections which have been lost
             */
            boolean lostConnection = false;
            String loginNames = "Login user(s): ";
            for (String loginUserUniqueNameWithoutConnection : loginUserUnqiueNamesWithoutConnection){
                if (loginUserWrapperList.containsKey(loginUserUniqueNameWithoutConnection)){
                    loginUserWrapperLosingConnection = loginUserWrapperList.get(loginUserUniqueNameWithoutConnection);
                    closeConnector(loginUserWrapperLosingConnection.getLoginUser(), true);
                    
                    loginNames += loginUserWrapperLosingConnection.getLoginUser().getIMScreenName() + ", ";
                    lostConnection = true;
                }
            }//for
            /**
             * Display login dialog windows for re-login
             */
            if (lostConnection){
                final String msg = loginNames.substring(0, loginNames.length()-2) + " " + PbcText.getSingleton().getText(PbcTextKey.Lost_Connections);
                if (SwingUtilities.isEventDispatchThread()){
                    displayFaceComponentHelper(msg);
                }else{
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            displayFaceComponentHelper(msg);
                        }
                    });
                }
            }
        }catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    /**
     * pbcConnection event is raised during/after loginConnector
     * @param pbcConnectionEvent
     */
    private void handleGatewayConnectionEvent(GatewayConnectionEvent event){
        if (event.getServerType().equals(targetServerType)){
            switch (event.getConnectionEventHappened()){
                case LOGIN_SUCCEED:
                    talker.openBuddyListPanelForLoginUser(event.getLoginUser());
                    hideFaceComponent();
                    event.getLoginUser().setBuddyStatus(BuddyStatus.Online);
                    prepareLoginUserAfterOnline(event.getLoginUser());
                    break;
                case LOGOUT_SUCCEED:
                    talker.getKernel().removePointBoxConnectorID(talker.getKernel().getPointBoxConnectorID(event.getLoginUser()));
                    prepareLoginUserAfterOffline(event.getLoginUser());
                    break;
                case LOGIN_REFUSEDED:
                    talker.getKernel().removePointBoxConnectorID(talker.getKernel().getPointBoxConnectorID(event.getLoginUser()));
                    handleConnectorLoginRefused(event);
                    break;
                case CONNECTION_LOST:
                    handleConnectorLoginLost(event.getLoginUser());
                    break;
                case CONFLICT_LOGIN_REFUSED:
                    talker.getKernel().removePointBoxConnectorID(talker.getKernel().getPointBoxConnectorID(event.getLoginUser()));
                    if (event.getLoginUser() != null){
                        event.getLoginUser().setBuddyStatus(BuddyStatus.Offline);
                    }
//                    logger.log(Level.WARNING, "Server login conflicted.");
                    break;
                case LOGIN_CANCELLED:
                    talker.getKernel().removePointBoxConnectorID(talker.getKernel().getPointBoxConnectorID(event.getLoginUser()));
//                    logger.log(Level.WARNING, "Server login canceled.");
                    break;
            }
        }
    }
    
    private void handleConnectorLoginRefused(final GatewayConnectionEvent event) {
        if (SwingUtilities.isEventDispatchThread()){
            handleLoginRefusedHelper(event);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleLoginRefusedHelper(event);
                }
            });
        }
    }
    private void handleLoginRefusedHelper(final GatewayConnectionEvent event) {
        if (event.getLoginUser() != null){
            event.getLoginUser().setBuddyStatus(BuddyStatus.Offline);
        }
        jClose.setEnabled(true);
        jCloseAll.setEnabled(true);
        jLoginBtn.setEnabled(true);
        jScreenNames.setEnabled(true);
        jPassword.setEnabled(true);
        jCancelBtn.setEnabled(true);
        jProgressBar.setIndeterminate(false);
        jMessage.setForeground(Color.red);
        jMessage.setText(event.getEventMessage());
    }
    
    private void handleConnectorLoginLost(final IGatewayConnectorBuddy loginUser) {
        if (SwingUtilities.isEventDispatchThread()){
            handleLoginLostHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleLoginLostHelper(loginUser);
                }
            });
        }
    }
    
    private void handleLoginLostHelper(IGatewayConnectorBuddy loginUser){
        if (loginUser == null){
            return;
        }
        
        loginUser.setBuddyStatus(BuddyStatus.Offline);
        final DefaultListModel model = ((DefaultListModel)jConnectedAccountNames.getModel());
        model.removeElement(getLoginUserWrapper(loginUser));
        
        jMessage.setForeground(Color.red);
        jMessage.setText("Currently you have no internet connection to the PB console server.");
        jScreenNames.setSelectedItem(loginUser.getIMScreenName());
        
        displayFaceComponent();
    }

    private void prepareLoginUserAfterOnline(final IGatewayConnectorBuddy loginUser){
        if (SwingUtilities.isEventDispatchThread()){
            prepareLoginUserAfterOnlineHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    prepareLoginUserAfterOnlineHelper(loginUser);
                }
            });
        }
    }
    private void prepareLoginUserAfterOnlineHelper(final IGatewayConnectorBuddy loginUser){
        if (loginUser != null){
            loginUser.setBuddyStatus(BuddyStatus.Online);
            LoginUserWrapper wrapper = getLoginUserWrapper(loginUser);
            synchronized(loginUserWrappers){
                loginUserWrappers.put(loginUser.getIMScreenName(), wrapper);
            }
            synchronized (jConnectedAccountNames.getModel()){
                DefaultListModel model = ((DefaultListModel)jConnectedAccountNames.getModel());
                if (!model.contains(wrapper)){
                    model.insertElementAt(wrapper, 0);
                }
            }
        }
    }
    
    private LoginUserWrapper getLoginUserWrapper(IGatewayConnectorBuddy loginUser){
        synchronized(loginUserWrappers){
            LoginUserWrapper wrapper = loginUserWrappers.get(loginUser.getIMScreenName());
            if (wrapper == null){
                wrapper = new LoginUserWrapper(loginUser);
                loginUserWrappers.put(loginUser.getIMScreenName(), wrapper);
            }
            return wrapper;
        }
    }
    
    private void prepareLoginUserAfterOffline(final IGatewayConnectorBuddy loginUser) {
        if (SwingUtilities.isEventDispatchThread()){
            prepareLoginUserAfterOfflineHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    prepareLoginUserAfterOfflineHelper(loginUser);
                }
            });
        }
    }
    private void prepareLoginUserAfterOfflineHelper(final IGatewayConnectorBuddy loginUser){
        if (loginUser != null){
            loginUser.setBuddyStatus(BuddyStatus.Offline);
            synchronized (jConnectedAccountNames.getModel()){
                final DefaultListModel model = ((DefaultListModel)jConnectedAccountNames.getModel());
                model.removeElement(getLoginUserWrapper(loginUser));
            }
            populateLoginUserWrapperSettingsHelper(getLoginUserWrapper(loginUser));
            jMessage.setForeground(Color.red);
            jMessage.setText(loginUser.getIMScreenName() + "'s connection is just cut off.");
        }
    }
    
    /**
     * Publish login status into the message zone on this window
     * @param event
     */
    private void publishLoginStatus(final ServerLoginStatusEvent event) {
        if (event.getServerType().equals(this.targetServerType)){
            if (SwingUtilities.isEventDispatchThread()){
                this.jMessage.setText(event.getDescription());
            }else{
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        jMessage.setText(event.getDescription());
                    }
                });
            }
        }
    }

    private void populateLoginUserWrapperSettingsHelper(LoginUserWrapper aLoginUserWrapper){
        if (aLoginUserWrapper == null){
            return;
        }
        if (aLoginUserWrapper.rememberMe){
            this.jScreenNames.setSelectedItem(aLoginUserWrapper.getLoginUser().getIMScreenName());
            this.jRememberMe.setSelected(true);
            this.jRememberMe.setEnabled(true);
            this.jSavePassword.setEnabled(true);
            if (aLoginUserWrapper.savePassword){
                this.jPassword.setText(aLoginUserWrapper.getLoginUser().getIMPassword());
                this.jSavePassword.setSelected(true);
//                this.jAutoLogin.setEnabled(true);
//                if (aLoginUserWrapper.autoLogin){
//                    this.jAutoLogin.setSelected(true);
//                }else{
//                    this.jAutoLogin.setSelected(false);
//                }
            }else{
                this.jPassword.setText("");
                this.jSavePassword.setSelected(false);
//                this.jAutoLogin.setSelected(false);
//                this.jAutoLogin.setEnabled(false);
            }
        }else{
            this.jScreenNames.setSelectedIndex(-1);
            this.jPassword.setText("");
            this.jRememberMe.setSelected(false);
            this.jSavePassword.setSelected(false);
            this.jSavePassword.setEnabled(false);
//            this.jAutoLogin.setSelected(false);
//            this.jAutoLogin.setEnabled(false);
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

        jBasePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPassword = new javax.swing.JPasswordField();
        jServerLabel = new javax.swing.JLabel();
        jServerList = new javax.swing.JComboBox();
        jRememberMe = new javax.swing.JCheckBox();
        jSavePassword = new javax.swing.JCheckBox();
        jAutoLogin = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMessage = new javax.swing.JTextArea();
        jProgressBar = new javax.swing.JProgressBar();
        jButtonPanel = new javax.swing.JPanel();
        jScreenNames = new javax.swing.JComboBox();
        jLoginBtn = new javax.swing.JButton();
        jCancelBtn = new javax.swing.JButton();
        jRemove = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jConnectedAccountNames = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jClose = new javax.swing.JButton();
        jCloseAll = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jBasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Server Login ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 18))); // NOI18N
        jBasePanel.setName("jBasePanel"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "New Account Login:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText("Username:");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText("Password: ");
        jLabel2.setName("jLabel2"); // NOI18N

        jPassword.setName("jPassword"); // NOI18N
        jPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPasswordKeyReleased(evt);
            }
        });

        jServerLabel.setText("Server:");
        jServerLabel.setName("jServerLabel"); // NOI18N

        jServerList.setName("jServerList"); // NOI18N
        jServerList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jServerListItemStateChanged(evt);
            }
        });

        jRememberMe.setText("Remember Me");
        jRememberMe.setName("jRememberMe"); // NOI18N
        jRememberMe.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRememberMeItemStateChanged(evt);
            }
        });

        jSavePassword.setText("Save Password");
        jSavePassword.setName("jSavePassword"); // NOI18N
        jSavePassword.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jSavePasswordItemStateChanged(evt);
            }
        });

        jAutoLogin.setText("Auto Login");
        jAutoLogin.setName("jAutoLogin"); // NOI18N
        jAutoLogin.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jAutoLoginItemStateChanged(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jMessage.setEditable(false);
        jMessage.setBackground(new java.awt.Color(236, 233, 216));
        jMessage.setColumns(20);
        jMessage.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jMessage.setLineWrap(true);
        jMessage.setRows(5);
        jMessage.setText("Note: Don't save your login on a public computer.");
        jMessage.setWrapStyleWord(true);
        jMessage.setBorder(null);
        jMessage.setMargin(new java.awt.Insets(3, 10, 3, 3));
        jMessage.setName("jMessage"); // NOI18N
        jMessage.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(jMessage);

        jProgressBar.setIndeterminate(true);
        jProgressBar.setMinimumSize(new java.awt.Dimension(10, 10));
        jProgressBar.setName("jProgressBar"); // NOI18N
        jProgressBar.setPreferredSize(new java.awt.Dimension(146, 10));

        jButtonPanel.setName("jButtonPanel"); // NOI18N
        jButtonPanel.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jScreenNames.setName("jScreenNames"); // NOI18N
        jScreenNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jScreenNamesActionPerformed(evt);
            }
        });

        jLoginBtn.setText("Login");
        jLoginBtn.setName("jLoginBtn"); // NOI18N
        jLoginBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoginBtnActionPerformed(evt);
            }
        });

        jCancelBtn.setText("Cancel");
        jCancelBtn.setName("jCancelBtn"); // NOI18N
        jCancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelBtnActionPerformed(evt);
            }
        });

        jRemove.setText("Remove");
        jRemove.setName("jRemove"); // NOI18N
        jRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(jRememberMe))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(jSavePassword))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(jAutoLogin))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLoginBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(jButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jLabel1)
                    .addGap(10, 10, 10)
                    .addComponent(jScreenNames, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jLabel2)
                    .addGap(10, 10, 10)
                    .addComponent(jPassword))
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jServerLabel)
                    .addGap(27, 27, 27)
                    .addComponent(jServerList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                    .addGap(29, 29, 29)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel1))
                    .addComponent(jScreenNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel2))
                    .addComponent(jPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jServerLabel))
                    .addComponent(jServerList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(jRememberMe)
                .addGap(3, 3, 3)
                .addComponent(jSavePassword)
                .addGap(3, 3, 3)
                .addComponent(jAutoLogin)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLoginBtn)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCancelBtn)
                        .addComponent(jRemove))
                    .addComponent(jButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Connected Accounts:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jConnectedAccountNames.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jConnectedAccountNames.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jConnectedAccountNames.setName("jConnectedAccountNames"); // NOI18N
        jScrollPane2.setViewportView(jConnectedAccountNames);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(1, 2, 1, 0));

        jClose.setText("Close");
        jClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseActionPerformed(evt);
            }
        });
        jPanel3.add(jClose);

        jCloseAll.setText("Close All");
        jCloseAll.setName("jCloseAll"); // NOI18N
        jCloseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseAllActionPerformed(evt);
            }
        });
        jPanel3.add(jCloseAll);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jBasePanelLayout = new javax.swing.GroupLayout(jBasePanel);
        jBasePanel.setLayout(jBasePanelLayout);
        jBasePanelLayout.setHorizontalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jBasePanelLayout.setVerticalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBasePanelLayout.createSequentialGroup()
                .addGroup(jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents



    private void jPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPasswordKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            loginConnector();
        }
    }//GEN-LAST:event_jPasswordKeyReleased

    private void jServerListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jServerListItemStateChanged
        //no implementation
    }//GEN-LAST:event_jServerListItemStateChanged

    private void jRememberMeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRememberMeItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!PointBoxConsoleProperties.getSingleton().isLoginWindowRememberMe(targetServerType)){
                jSavePassword.setEnabled(true);
                
                this.populatejScreenNames();
                
                PointBoxConsoleProperties.getSingleton().storeLoginWindowRememberMe(true, targetServerType);
            }
        }
        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowRememberMe(targetServerType)){
                jSavePassword.setEnabled(false);
                jSavePassword.setSelected(false);
                
                this.populatejScreenNames();
                
                PointBoxConsoleProperties.getSingleton().storeLoginWindowRememberMe(false, targetServerType);
            }
        }
    }//GEN-LAST:event_jRememberMeItemStateChanged

    private void jSavePasswordItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jSavePasswordItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jRememberMe.setSelected(true);
            if (!PointBoxConsoleProperties.getSingleton().isLoginWindowSavePassword(targetServerType)){
                jRememberMe.setEnabled(true);
                jRememberMe.setSelected(true);
                
                this.populatejScreenNames();
                
                PointBoxConsoleProperties.getSingleton().storeLoginWindowSavePassword(true, targetServerType);
            }
        }

        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowSavePassword(targetServerType)){
                
                this.populatejScreenNames();
                
                PointBoxConsoleProperties.getSingleton().storeLoginWindowSavePassword(false, targetServerType);
            }
        }
    }//GEN-LAST:event_jSavePasswordItemStateChanged

    private void jAutoLoginItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jAutoLoginItemStateChanged
        //This method will never be called since jAutoLogin has been disabled.
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!PointBoxConsoleProperties.getSingleton().isLoginWindowAutoLogin(targetServerType)){
                jRememberMe.setEnabled(true);
                jRememberMe.setSelected(true);
                jSavePassword.setEnabled(true);
                jSavePassword.setSelected(true);
                
                this.populatejScreenNames();
                
                if (JOptionPane.showConfirmDialog(this, "Automatically log into the remote server? You may changed it back from the preferences window after login.",
                        "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                {
                    PointBoxConsoleProperties.getSingleton().storeLoginWindowAutoLogin(true, targetServerType);
                    loginConnector();
                }else{
//                    jAutoLogin.setSelected(false);
                }
            }
        }

        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowAutoLogin(targetServerType)){
                PointBoxConsoleProperties.getSingleton().storeLoginWindowAutoLogin(false, targetServerType);
            }
        }
    }//GEN-LAST:event_jAutoLoginItemStateChanged

    private void jCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseActionPerformed
        synchronized (jConnectedAccountNames.getModel()){
            int index = jConnectedAccountNames.getSelectedIndex();
            if (index > -1){
                LoginUserWrapper loginUserWrapper = (LoginUserWrapper)jConnectedAccountNames.getModel().getElementAt(index);
                if (JOptionPane.showConfirmDialog(talker.getKernel().getPointBoxMainFrame(),
                                                    "Close the connections of " + loginUserWrapper +" in the above list?",
                                                    "Confirmation",
                                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                    closeConnector(loginUserWrapper.getLoginUser(), false);
                    super.setVisible(false);
                }
            }
        }
    }//GEN-LAST:event_jCloseActionPerformed

    private void jCloseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseAllActionPerformed
        if (JOptionPane.showConfirmDialog(talker.getKernel().getPointBoxMainFrame(),
                "Close all the connections in the above list?", 
                "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            closeAllConnectorsHelper();
            super.setVisible(false);
        }
    }//GEN-LAST:event_jCloseAllActionPerformed

    private void jLoginBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoginBtnActionPerformed
        loginConnector();
    }//GEN-LAST:event_jLoginBtnActionPerformed

    private void jCancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelBtnActionPerformed
        super.setVisible(false);
    }//GEN-LAST:event_jCancelBtnActionPerformed

    private void jRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRemoveActionPerformed
        if((jScreenNames.getSelectedItem() != null)
                &&(!(jScreenNames.getSelectedItem().toString().trim().isEmpty())))
        {
            String screenName = jScreenNames.getSelectedItem().toString().trim();
            IGatewayConnectorBuddy loginUser = GatewayBuddyListFactory.getLoginUserInstance(screenName, targetServerType);
            if (BuddyStatus.Online.equals(loginUser.getBuddyStatus())){
                JOptionPane.showMessageDialog(this, "Sorry, you have to close connection with account " + screenName + "before you remove its credentials.");
            }else{
                if (JOptionPane.showConfirmDialog(this, 
                        "Do you really want to remove the credential and buddy list panel of "+screenName+"?", 
                        "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                {
                    decacheCredentials(screenName);
                    populatejScreenNames();
                    talker.closeBuddyListPanelForLoginUser(loginUser, false);
                    setVisible(false);
                }
            }
        }
    }//GEN-LAST:event_jRemoveActionPerformed

    private void jScreenNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jScreenNamesActionPerformed
        if (jScreenNames.getSelectedItem() != null){
            jPassword.setText(getPasswordFromCredentials(jScreenNames.getSelectedItem().toString()));
        }
    }//GEN-LAST:event_jScreenNamesActionPerformed

    void handleNoConnectionToServers(final String message) {
        if (SwingUtilities.isEventDispatchThread()){
            handleNoConnectionToServersHelper(message);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleNoConnectionToServersHelper(message);
                }
            });
        }
    }
    private void handleNoConnectionToServersHelper(final String message) {
        closeAllConnectors();
        jMessage.setForeground(Color.red);
        jMessage.setText(message);
    }
    
    @Override
    public void closeAllConnectors(){
        if (SwingUtilities.isEventDispatchThread()){
            closeAllConnectorsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    closeAllConnectorsHelper();
                }
            });
        }
    }

    private void closeAllConnectorsHelper(){
        ArrayList<LoginUserWrapper> wrappers = new ArrayList<LoginUserWrapper>();
        synchronized (jConnectedAccountNames.getModel()){
            int total = jConnectedAccountNames.getModel().getSize();
            for (int i = 0; i < total; i++){
                wrappers.add((LoginUserWrapper)jConnectedAccountNames.getModel().getElementAt(i));
            }
        }
        for (LoginUserWrapper loginUserWrapper : wrappers){
            closeConnector(loginUserWrapper.getLoginUser(), false);
        }
    }
    
    @Override
    public void closeConnector(final IGatewayConnectorBuddy loginUser, final boolean lostConnection){
        if (loginUser == null){
            return;
        }
        LoginUserWrapper wrapper = getLoginUserWrapper(loginUser);
        //the following case is a techical exception
        if (wrapper == null){
            logger.log(Level.SEVERE, "Technical exception", new Exception("The expected loginUser should be found in the memory"));
            return;
        }
        (new LogoutConnectorWorker(loginUser, lostConnection)).execute();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAutoLogin;
    private javax.swing.JPanel jBasePanel;
    private javax.swing.JPanel jButtonPanel;
    private javax.swing.JButton jCancelBtn;
    private javax.swing.JButton jClose;
    private javax.swing.JButton jCloseAll;
    private javax.swing.JList jConnectedAccountNames;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton jLoginBtn;
    private javax.swing.JTextArea jMessage;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPasswordField jPassword;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JCheckBox jRememberMe;
    private javax.swing.JButton jRemove;
    private javax.swing.JCheckBox jSavePassword;
    private javax.swing.JComboBox jScreenNames;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel jServerLabel;
    private javax.swing.JComboBox jServerList;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the jConnectedAccountNames
     */
    public javax.swing.JList getjConnectedAccountNames() {
        return jConnectedAccountNames;
    }

    private class LoginConnectorWorker extends SwingWorker<PointBoxLoginPublicImServiceResponse, String>{
        private IGatewayConnectorBuddy currentloginUser;

        LoginConnectorWorker(IGatewayConnectorBuddy currentloginUser) {
            this.currentloginUser = currentloginUser;
        }
        @Override
        protected PointBoxLoginPublicImServiceResponse doInBackground() throws Exception {
            publish("Log in the remote server...");
        
            cacheCredentials(currentloginUser);
        
            return talker.getKernel().loginRemoteServer(currentloginUser);
        }

        @Override
        protected void process(List<String> chunks) {
            for (String msg : chunks){
                jMessage.setText(msg);
            }
        }

        @Override
        protected void done() {
            try {
                hanldleWebServiceResponseHelper(get());
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Interrupted Exception] " + ex.getMessage());
                
                /**
                 * Why comment-out? Because all the buddy list panels are merged into one single panel 
                 * which contains all the groups as "ditribution-groups"
                 */
                talker.closeBuddyListPanelForLoginUser(currentloginUser);
            } catch (ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Execution Exception] " + ex.getMessage());
                
                /**
                 * Why comment-out? Because all the buddy list panels are merged into one single panel 
                 * which contains all the groups as "ditribution-groups"
                 */
                talker.closeBuddyListPanelForLoginUser(currentloginUser);
            }
        }

        private void hanldleWebServiceResponseHelper(PointBoxLoginPublicImServiceResponse response) {
            PointBoxServiceResult result = response.getResult();
            //display the description on why the request was not successfully processed
            switch(result){
                case RequestExecuted:
                    PbcBuddyListSettings aPbcBuddyListSettings = (response).getPbcBuddyListSettings();
                    if (aPbcBuddyListSettings != null){
                        //this directly came from the server-side. thus, it is false for the second parameter
                        getRuntime().updatePbcBuddyListSettings(aPbcBuddyListSettings, false);
                    }
                    //completed the job and just wait for events happened from the server-side
                    jMessage.setForeground(Color.blue);
                    jMessage.setText("Authenticating and loading buddy list...");
                    break;
                case IDNotAuthenticated:
                    jMessage.setForeground(Color.red);
                    jMessage.setText("Login refused. Please check your login and password.");
                    break;
                case PointBoxWebServerNoResponse:
                    jMessage.setForeground(Color.red);
                    jMessage.setText("No web server is responsive at this time. Please contact PointBox adminstration.");
                    break;
                case PointBoxGatewayServerNoResponse:
                    jMessage.setForeground(Color.red);
                    jMessage.setText("No gateway server is responsive at this time. Please contact PointBox adminstration.");
                    break;
                default:
                    jMessage.setForeground(Color.red);
                    jMessage.setText(response.getDescription());
            }
            if (result.equals(PointBoxServiceResult.RequestExecuted)){
                talker.getKernel().addPointBoxConnectorID(response.getPointBoxConnectorID());
                //make tab icons online <- this looks wrong. When many buddy tabs are opened, and login succeed, in this case, some buddy may be still offline
                //talker.getMessagingPaneManager().refreshTabIcons(GatewayBuddyListFactory.constructBuddyIMUniqueName(targetServerType, jScreenNames.getSelectedItem().toString().trim()), targetServerType, true,talker);     
            }else{
                talker.getKernel().removePointBoxConnectorID(response.getPointBoxConnectorID());
                talker.closeBuddyListPanelForLoginUser(currentloginUser);
                jClose.setEnabled(true);
                jCloseAll.setEnabled(true);
                jLoginBtn.setEnabled(true);
                jScreenNames.setEnabled(true);
                jPassword.setEnabled(true);
                jCancelBtn.setEnabled(true);
                //jAutoLogin.setSelected(false);
                jProgressBar.setIndeterminate(false);
                jMessage.setForeground(Color.red);
            }
        }//end of hanldleWebServiceResponseHelper
    }

    private class LogoutConnectorWorker extends SwingWorker<PointBoxWebServiceResponse, String>{
        private IGatewayConnectorBuddy loginUser;
        private boolean lostConnection;
        LogoutConnectorWorker(IGatewayConnectorBuddy loginUser, boolean lostConnection) {
            this.loginUser = loginUser;
            this.lostConnection = lostConnection;
        }
        @Override
        protected PointBoxWebServiceResponse doInBackground() throws Exception {
            publish("Log out " + loginUser.getIMScreenName() + " from the remote server...");
            //before logout, save settings (e.g., distribution list) for PBC
            return talker.getKernel().logoutRemoteServer(loginUser);
        }

        @Override
        protected void process(List<String> chunks) {
            for (String msg : chunks){
                jMessage.setText(msg);
            }
        }

        @Override
        protected void done() {
            jClose.setEnabled(true);
            jCloseAll.setEnabled(true);
            jLoginBtn.setEnabled(true);
            jScreenNames.setEnabled(true);
            jPassword.setEnabled(true);
            jCancelBtn.setEnabled(true);
            jProgressBar.setIndeterminate(false);
            jMessage.setForeground(Color.red);
            try {
                hanldleLogoutWebServiceResponseHelper(get());
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Interrupted Exception] " + ex.getMessage());
                talker.closeBuddyListPanelForLoginUser(loginUser);
            } catch (ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Execution Exception] " + ex.getMessage());
                talker.closeBuddyListPanelForLoginUser(loginUser);
            }
        }

        private void hanldleLogoutWebServiceResponseHelper(PointBoxWebServiceResponse response) {
            PointBoxServiceResult result = response.getResult();
            //display the description on why the request was not successfully processed
            switch(result){
                case RequestExecuted:
                    //completed the job and just wait for events happened from the server-side
                    jMessage.setForeground(Color.black);
                    jMessage.setText("Sent out request to logout");
                    break;
                case PointBoxWebServerNoResponse:
                    jMessage.setForeground(Color.red);
                    jMessage.setText("No web server is responsive at this time. Please contact PointBox adminstration.");
                    break;
                case PointBoxGatewayServerNoResponse:
                    jMessage.setForeground(Color.red);
                    jMessage.setText("No gateway server is responsive at this time. Please contact PointBox adminstration.");
                    break;
                default:
                    jMessage.setForeground(Color.red);
                    jMessage.setText(response.getDescription());
            }
            talker.closeBuddyListPanelForLoginUser(loginUser);
            jClose.setEnabled(true);
            jCloseAll.setEnabled(true);
            jLoginBtn.setEnabled(true);
            jScreenNames.setEnabled(true);
            jPassword.setEnabled(true);
            jCancelBtn.setEnabled(true);
            jProgressBar.setIndeterminate(false);
            jMessage.setForeground(Color.red);

            DefaultListModel model;
            synchronized(getjConnectedAccountNames().getModel()){
                model = ((DefaultListModel)getjConnectedAccountNames().getModel());
                model.removeElement(getLoginUserWrapper(loginUser));
            }
            
            String loginName = loginUser.getIMScreenName();
            if (lostConnection){
                jScreenNames.setSelectedItem(loginName);
                jPassword.setText(getPasswordFromCredentials(loginName));
                jMessage.setForeground(Color.red);
                jMessage.setText("Currently you have no internet connection to the PB console server.");
                talker.getKernel().raisePointBoxEvent(
                        new GatewayConnectionEvent(PointBoxEventTarget.PbcFace,
                                                   loginUser,
                                                   ConnectionEventHappened.LOGOUT_SUCCEED,
                                                   "Currently you have no internet connection to the PB console server."));
                
            }else{
                talker.getKernel().raisePointBoxEvent(
                        new GatewayConnectionEvent(PointBoxEventTarget.PbcFace,
                                                   loginUser,
                                                   ConnectionEventHappened.LOGOUT_SUCCEED,
                                                   "Connection to " + loginName + " has been disconnected."));
            }
            
        }//end of hanldleWebServiceResponseHelper
    }
//
//    private class ForceOutConnectorLogin extends SwingWorker<PointBoxWebServiceResponse, Void>{
//        
//        /**
//         * Reserved for the future usage
//         */
//        private String conflictWsdl;
//
//        ForceOutConnectorLogin(String conflictWsdl) {
//            this.conflictWsdl = conflictWsdl;
//        }
//        
//        @Override
//        protected PointBoxWebServiceResponse doInBackground() throws Exception {
//            IGatewayConnectorBuddy loginUser = GatewayBuddyListFactory.getLoginUserInstance(jScreenNames.getSelectedItem().toString(), 
//                                                                                             targetServerType);
//            loginUser.setIMPassword(new String(jPassword.getPassword()));
//            return talker.getKernel().forceOutRemoteServer(loginUser);
//        }
//
//        @Override
//        protected void done() {
//            try {
//                PointBoxWebServiceResponse response = get();
//                jClose.setEnabled(true);
//                jCloseAll.setEnabled(true);
//                jLoginBtn.setEnabled(true);
//                jScreenNames.setEnabled(true);
//                jPassword.setEnabled(true);
//                jCancelBtn.setEnabled(true);
//                jProgressBar.setIndeterminate(false);
//                if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
//                    jMessage.setText("The other login with the same account has been forced out. Please try your account now.");
//                }else{
//                    jMessage.setText(response.getDescription());
//                }
//            } catch (InterruptedException ex) {
//                logger.log(Level.SEVERE, null, ex);
//                jMessage.setText("[Interrupted Exception] " + ex.getMessage());
//            } catch (ExecutionException ex) {
//                logger.log(Level.SEVERE, null, ex);
//                jMessage.setText("[Execution Exception] " + ex.getMessage());
//            }
//        }
//
//    }//ForceOutSystemLogin
//    
    public class LoginUserWrapper{
        private IGatewayConnectorBuddy loginUser;
        private boolean rememberMe;
        private boolean savePassword;
        private boolean autoLogin;

        public LoginUserWrapper(IGatewayConnectorBuddy loginUser) {
            this.loginUser = loginUser;
            this.rememberMe = jRememberMe.isSelected();
            this.savePassword = jSavePassword.isSelected();
            this.autoLogin = PointBoxConsoleProperties.getSingleton().isLoginWindowAutoLogin(loginUser.getIMServerType());
        }


        @Override
        public String toString() {
            return loginUser.getIMScreenName();
        }

        /**
         * @return the loginUser
         */
        public IGatewayConnectorBuddy getLoginUser() {
            return loginUser;
        }
        
    }
}
