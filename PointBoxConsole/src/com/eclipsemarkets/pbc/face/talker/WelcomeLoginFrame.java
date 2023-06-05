/**
 * Eclipse Market Solutions LLC
 *
 * SystemLoginFrame.java
 *
 * @author Zhijun Zhang
 * Created on Sep 16, 2010, 2:26:07 PM
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.talker.TalkerConnectionLostEvent;
import com.eclipsemarkets.event.gateway.GatewayConnectionEvent;
import com.eclipsemarkets.event.gateway.ServerLoginStatusEvent;
import com.eclipsemarkets.event.pricer.PBPricerChangedEvent;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.IPbcSplashScreen;
import com.eclipsemarkets.pbc.PbcProperties;
import com.eclipsemarkets.pbc.PbcText;
import com.eclipsemarkets.pbc.PbcTextKey;
import com.eclipsemarkets.pbc.face.IServerLoginWindow;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pricer.PbcPricerType;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.PointBoxLoginServiceResponse;
import com.eclipsemarkets.web.PointBoxServiceResult;
import static com.eclipsemarkets.web.PointBoxServiceResult.IDNotAuthenticated;
import static com.eclipsemarkets.web.PointBoxServiceResult.LoginConflict;
import static com.eclipsemarkets.web.PointBoxServiceResult.PointBoxGatewayServerNoResponse;
import static com.eclipsemarkets.web.PointBoxServiceResult.PointBoxWebServerNoResponse;
import static com.eclipsemarkets.web.PointBoxServiceResult.RequestExecuted;
import com.eclipsemarkets.web.pbc.PointBoxConsoleSettings;
import java.awt.Color;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

/**
 * During system login procedure, this frame is also used as IPbcSplashScreen
 * @author Zhijun Zhang
 */
public class WelcomeLoginFrame extends javax.swing.JFrame implements IServerLoginWindow, IPbcSplashScreen
{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(WelcomeLoginFrame.class.getName());
    }
    private static final long serialVersionUID = 1L;

    /**
     * Owner of this control
     */
    private final IPbcTalker talker;

    /**
     * control proxy settings
     */
    private ProxySettingsDialog proxySettingsDialog;

    /**
     * This is the person who are using the point-box system.
     */
    private IGatewayConnectorBuddy pbimLoginUser;

    /**
     * Default message displayed on the dialog
     */
    private final String defaultMessage;

    /**
     * todo-sim: display different L&F for different default code?
     */
//    private final HashMap<String, LookAndFeelType> lafTypes;
    
    /** Creates new form SystemLoginFrame
     * @param system
     * @param pointBoxFrame
     */
    private WelcomeLoginFrame(final IPbcTalker talker)
    {
        defaultMessage = "Note: Don't save your login on a public computer.";
        initComponents();
        
//        lafTypes = new HashMap<String, LookAndFeelType>();
//        talker.getKernel().getPointBoxConsoleRuntime().getPbcPricingModelMap();
//        lafTypes.put(QuoteCommodityType.CRUDE_OIL, LookAndFeelType.BusinessBlackSteel);
//        lafTypes.put(QuoteCommodityType.GRAINS, LookAndFeelType.BusinessBlueSteel);
//        lafTypes.put(QuoteCommodityType.EUROPEAN_GAS, LookAndFeelType.OfficeSilver2007);
//        lafTypes.put(QuoteCommodityType.NATURAL_GAS, LookAndFeelType.Nebula);
//        lafTypes.put(QuoteCommodityType.POWER, LookAndFeelType.MistSilver);
//        lafTypes.put(QuoteCommodityType.TALKER, LookAndFeelType.BusinessBlueSteel);
                
        jLoginBtn.setText(LoginButtonText.Login.toString());
        this.talker = talker;
        
        //title
        this.setTitle(PbcProperties.getSingleton().getReleaseCompany());
        ((TitledBorder)jContentPanel.getBorder()).setTitle("Welcome to PointBox Console!");
        
        setIconImage(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon().getImage());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                talker.getKernel().shutdown(getCurrentLoginUser(), true);
            }
        });
    }
    
//    private LookAndFeelType getLookAndFeelType(QuoteCommodityType commodityType) {
//        return lafTypes.get(commodityType);
//    }

    @Override
    public int connectionCount() {
        return 1;
    }

    @Override
    public IGatewayConnectorBuddy getCurrentLoginUser() {
        return pbimLoginUser;
    }

    /**
     * This should be called from EDT
     * @param talker
     * @return
     */
    static WelcomeLoginFrame getInstance(final IPbcTalker talker)
    {
        WelcomeLoginFrame loginFrame = new WelcomeLoginFrame(talker);
        return loginFrame;
    }

    @Override
    public void closeConnector(IGatewayConnectorBuddy loginUser, boolean lostConnection) {
        if ((loginUser != null) && (loginUser.equals(getCurrentLoginUser()))){
            talker.getKernel().shutdown(getCurrentLoginUser(), true);
        }
    }
    /**
     * Populate settings from the runtime to the current frame
     */
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
        if (PointBoxConsoleProperties.getSingleton().isLoginWindowRememberMe(GatewayServerType.PBIM_SERVER_TYPE)){
            String accountName = PointBoxConsoleProperties.getSingleton().retrievePbimLoginName();
            if (DataGlobal.isEmptyNullString(accountName)){
                this.jScreenName.setText("");
            }else{
                this.jScreenName.setText(accountName.trim());
            }
            this.jRememberMe.setSelected(true);
            this.jRememberMe.setEnabled(true);
            this.jSavePassword.setEnabled(true);
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowSavePassword(GatewayServerType.PBIM_SERVER_TYPE)){
                this.jPassword.setText(PointBoxConsoleProperties.getSingleton().retrievePbimLoginPassword());
                this.jSavePassword.setSelected(true);
                this.jAutoLogin.setEnabled(true);
                if (PointBoxConsoleProperties.getSingleton().isLoginWindowAutoLogin(GatewayServerType.PBIM_SERVER_TYPE)){
                    this.jAutoLogin.setSelected(true);
                }else{
                    this.jAutoLogin.setSelected(false);
                }
            }else{
                this.jPassword.setText("");
                this.jSavePassword.setSelected(false);
                this.jAutoLogin.setSelected(false);
                this.jAutoLogin.setEnabled(false);
            }
        }else{
            this.jScreenName.setText("");
            this.jRememberMe.setSelected(false);
            this.jSavePassword.setSelected(false);
            this.jSavePassword.setEnabled(false);
            this.jAutoLogin.setSelected(false);
            this.jAutoLogin.setEnabled(false);
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
    
    @Override
    public void personalizeFaceComponent() {
    }

    @Override
    public void loadAnonymousLoginSettings() {
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
        publishMessage(msg);
    }

    @Override
    public void loadCredentials() {

    }

    private void displayFaceComponentHelper(String msg){
        if (proxySettingsDialog == null){
            proxySettingsDialog = new ProxySettingsDialog(this, true);
        }
        jMessage.setBackground(this.getContentPane().getBackground());
        
        jLoginBtn.setEnabled(true);
        jScreenName.setEnabled(true);
        jPassword.setEnabled(true);
        jShutdownBtn.setEnabled(true);
        jProgressBar.setIndeterminate(false);
        jMessage.setForeground(Color.black);
        jMessage.setText(defaultMessage);
        
        populateLoginWindowSettings();
        
//        checkReleaseCodeInEDT();
        
//        jPbServerList.setSelectedItem(talker.getKernel().getPointBoxAgentServerIP());
        
        pack();
        
        this.setResizable(false);
//        JFrame mainFrame = talker.getKernel().getPointBoxMainFrame();
//        if ((mainFrame != null) && (mainFrame.isVisible())){
//            this.setLocation(SwingGlobal.getCenterPointOfParentWindow(mainFrame, this));
//        }else{
            this.setLocation(SwingGlobal.getScreenCenterPoint(this));
//        }

        super.setVisible(true);
        
        if ((jAutoLogin.isSelected() && ((jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Login.toString()))))){
            loginPointBox();
        }else{
//            try{
//                if (DataGlobal.isJvmUpdateRequired()){
//                    jMessage.setText("Your current Java: " + DataGlobal.getJavaVersion() + " (Bit: " + DataGlobal.getJvmBitness() 
//                            + ")\n\nWARNING: please update your Java runtime environment to version " + DataGlobal.getRequiredJvmVersion() 
//                            + " or later so as to have full features supported.");
//                    jMessage.setForeground(Color.red);
//                }
//            }catch (Exception ex){
//            }
        }
        
        if (DataGlobal.isNonEmptyNullString(msg)){
            JOptionPane.showMessageDialog(this, msg);
        }
    }
    
    @Override
    public void closeAllConnectors() {
        
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
        super.setVisible(false);
        jLoginBtn.setEnabled(true);
        jScreenName.setEnabled(true);
        jPassword.setEnabled(true);
        jShutdownBtn.setEnabled(true);
        jProgressBar.setIndeterminate(false);
        jMessage.setForeground(Color.black);
        jMessage.setText(defaultMessage);
    }

    private void publishMessage(final String msg){
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
    
    private IGatewayConnectorBuddy decorateGuiForLoginOrKillConflictHelper(String msg){
        //(1) basic validation
        String accountName = jScreenName.getText();
        String password = new String(jPassword.getPassword());
        if (DataGlobal.isEmptyNullString(accountName)){
            jMessage.setForeground(Color.red);
            jMessage.setText("Please type in a valid screen name.");
            return null;
        }else{
            accountName = accountName.trim();
        }
        if (DataGlobal.isEmptyNullString(password)){
            jMessage.setForeground(Color.red);
            jMessage.setText("Please type in a valid password.");
            return null;
        }
        //(2) prepare GUI...
        jLoginBtn.setEnabled(false);
        jScreenName.setEnabled(false);
        jPassword.setEnabled(false);
        jShutdownBtn.setEnabled(true);
        jProgressBar.setIndeterminate(true);
        jMessage.setForeground(Color.blue);
        jMessage.setText(msg);
        //settle down the master of PointBox console....
        pbimLoginUser = GatewayBuddyListFactory.getLoginUserInstance(accountName,
                                                                     //password,
                                                                     GatewayServerType.PBIM_SERVER_TYPE);
        pbimLoginUser.setIMPassword(password);
        return pbimLoginUser;
    }
    
    /**
     * Starting point of PointBoxConsole operations for a user
     */
    private void loginPointBox(){
        if (SwingUtilities.isEventDispatchThread()){
            loginPointBoxHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    loginPointBoxHelper();
                }
            });
        }
    }

    private void forceLoginPointBox() {
        if (SwingUtilities.isEventDispatchThread()){
            forceLoginPointBoxHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    forceLoginPointBoxHelper();
                }
            });
        }
    }

    private void loginPointBoxHelper(){
        jLoginBtn.setEnabled(false);
        decorateGuiForLoginOrKillConflictHelper("Start connecting to PointBox system....");
        if (talker.getKernel().preparePointBoxSystemLogin(pbimLoginUser.getIMScreenName())){
            //(4)launch the login procedure...
            (new LoginPointBoxWorker()).execute();
        }else{
            JOptionPane.showMessageDialog(this, "Your account is not permitted to use PointBox System. Please contact PointBox adminstrator.");
            jScreenName.setEnabled(true);
            jPassword.setEnabled(true);
            jMessage.setForeground(Color.black);
            jMessage.setText(defaultMessage);
            jLoginBtn.setEnabled(true);
            jShutdownBtn.setEnabled(true);
        }
    }
    
    private void forceLoginPointBoxHelper(){
        decorateGuiForLoginOrKillConflictHelper("Start log into PointBox system by killing existing connection....");
        //(4)launch the killing procedure...
        (new ForcePointBoxSystemLogin()).execute();
    }
    
    private void handleLoginRefused(final String eventMessage) {
        if (SwingUtilities.isEventDispatchThread()){
            handleLoginRefusedHelper(eventMessage);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleLoginRefusedHelper(eventMessage);
                }
            });
        }
    }
    private void handleLoginRefusedHelper(final String eventMessage) {
        jLoginBtn.setEnabled(true);
        jScreenName.setEnabled(true);
        jPassword.setEnabled(true);
        jShutdownBtn.setEnabled(true);
        jProgressBar.setIndeterminate(false);
        jMessage.setForeground(Color.red);
        jMessage.setText(eventMessage);
    }
    
    private void handlePointBoxConnectionLost() {
        displayFaceComponent();
        setPbimReconnectionButton();
    }
    
    void setPbimReconnectionButton(){
        if (SwingUtilities.isEventDispatchThread()){
            setPbimReconnectionButtonHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setPbimReconnectionButtonHelper();
                }
            });
        }
    }
    
    private void setPbimReconnectionButtonHelper(){
        jLoginBtn.setText(LoginButtonText.Reconnect.toString());
        jMessage.setForeground(Color.red);
        jMessage.setText("Currently you have no internet connection to the PB console server.");
    }

    @Override
    public void handlePointBoxEvent(PointBoxConsoleEvent event) {
        if (event instanceof GatewayConnectionEvent){
            handleGatewayConnectionEvent((GatewayConnectionEvent)event);
        }else if (event instanceof ServerLoginStatusEvent){
            if (((ServerLoginStatusEvent)event).getServerType().equals(GatewayServerType.PBIM_SERVER_TYPE)){
                publishMessage(((ServerLoginStatusEvent)event).getDescription());
            }
        }else if (event instanceof TalkerConnectionLostEvent){
            /**
             * Do not handle this case since it has another monitor to check PBIM liveness through PointBoxCentralAgent (checkGatewayLiveness)
             */
            talker.getKernel().getPointBoxConsoleWeb().firePointBoxServerWithoutResponseEvent(PbcText.getSingleton().getText(PbcTextKey.Lost_PB_Servers));
            //handleTalkerConnectionLostEvent(((TalkerConnectionLostEvent)event).getLoginUserUnqiueNamesWithoutConnection());
        }
    }
//
//    private void handleTalkerConnectionLostEvent(List<String> loginUserUnqiueNamesWithoutConnection) {
//        if ((loginUserUnqiueNamesWithoutConnection == null) || (loginUserUnqiueNamesWithoutConnection.isEmpty())){
//            return;
//        }
//        try{
//            if (pbimLoginUser == null){
//                PointBoxTracer.recordSevereTechnicalError(logger, new Exception("handleTalkerConnectionLostEvent: this is an impossible case."));
//                return;
//            }
//            /**
//             * Process connections which have been lost
//             */
//            boolean lostConnection = false;
//            for (String loginUserUniqueNameWithoutConnection : loginUserUnqiueNamesWithoutConnection){
//                if (pbimLoginUser.getIMUniqueName().equalsIgnoreCase(loginUserUniqueNameWithoutConnection)){
//                    talker.closeAllPublicConnectors();
//                    pbimLoginUser.setBuddyStatus(BuddyStatus.Offline);
//                    lostConnection = true;
//                }
//            }//for
//            /**
//             * Display login dialog windows for re-login
//             */
//            if (lostConnection){
//                final String msg = "Your PointBox console has lost its connection. Please re-log in.";
//                if (SwingUtilities.isEventDispatchThread()){
//                    handleTalkerConnectionLostEventHelper(msg);
//                }else{
//                    SwingUtilities.invokeLater(new Runnable(){
//                        @Override
//                        public void run() {
//                            handleTalkerConnectionLostEventHelper(msg);
//                        }
//                    });
//                }
//            }
//        }catch (Exception ex){
//            logger.log(Level.SEVERE, ex.getMessage(), ex);
//        }
//    }

    private void handleTalkerConnectionLostEventHelper(String msg) {
        jAutoLogin.setSelected(false);
        PointBoxConsoleProperties.getSingleton().storeLoginWindowAutoLogin(false, GatewayServerType.PBIM_SERVER_TYPE);
        displayFaceComponentHelper(msg);
    }
    
    /**
     * pbcConnection event is raised during/after loginPointBox
     * @param pbcConnectionEvent
     */
    private void handleGatewayConnectionEvent(final GatewayConnectionEvent pbcConnectionEvent) {
        //logger.log(Level.INFO, "zhijun -> line 516......");
        if (pbcConnectionEvent.getServerType().equals(GatewayServerType.PBIM_SERVER_TYPE)){
            switch (pbcConnectionEvent.getConnectionEventHappened()){
                case LOGIN_SUCCEED:
                    //logger.log(Level.INFO, "zhijun -> line 520......");
                    //kernel personalize its components ...
                    pbimLoginUser.setBuddyStatus(BuddyStatus.Online);
                    talker.getKernel().personalize();
                    hideFaceComponent();
                    talker.getKernel().displayPointBoxFrame();
                    break;
                case LOGOUT_SUCCEED:
                    pbimLoginUser.setBuddyStatus(BuddyStatus.Offline);
                    talker.getKernel().destroyPointBoxAccountID();
                    break;
                case LOGIN_REFUSEDED:
                    pbimLoginUser.setBuddyStatus(BuddyStatus.Offline);
                    //talker.getKernel().destroyPointBoxAccountID();
                    //display the description on pbcConnectionEvent
                    handleLoginRefused(pbcConnectionEvent.getEventMessage());
                    break;
                case CONNECTION_LOST:
                    pbimLoginUser.setBuddyStatus(BuddyStatus.Offline);
                    handlePointBoxConnectionLost();
                    break;
                case CONFLICT_LOGIN_REFUSED:
                    pbimLoginUser.setBuddyStatus(BuddyStatus.Offline);
                    //talker.getKernel().destroyPointBoxAccountID();
                    break;
                case LOGIN_CANCELLED:
                    //do not change login user status here because this might be caused by any reason 
                    talker.getKernel().destroyPointBoxAccountID();
                    break;
            }
        }//if
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeGroup = new javax.swing.ButtonGroup();
        jBasePanel = new javax.swing.JPanel();
        jContentPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMessage = new javax.swing.JTextArea();
        jProgressBar = new javax.swing.JProgressBar();
        jButtonPanel = new javax.swing.JPanel();
        jLoginBtn = new javax.swing.JButton();
        jShutdownBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jRememberMe = new javax.swing.JCheckBox();
        jSavePassword = new javax.swing.JCheckBox();
        jAutoLogin = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScreenName = new javax.swing.JTextField();
        jPassword = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jBasePanel.setName("jBasePanel"); // NOI18N

        jContentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Server Login ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 18))); // NOI18N
        jContentPanel.setName("jContentPanel"); // NOI18N

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

        jLoginBtn.setText("Checking Release...");
        jLoginBtn.setName("jLoginBtn"); // NOI18N
        jLoginBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoginBtnActionPerformed(evt);
            }
        });
        jButtonPanel.add(jLoginBtn);

        jShutdownBtn.setText("Shut Down");
        jShutdownBtn.setName("jShutdownBtn"); // NOI18N
        jShutdownBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShutdownBtnActionPerformed(evt);
            }
        });
        jButtonPanel.add(jShutdownBtn);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.GridLayout(3, 1, 2, 3));

        jRememberMe.setText("Remember Me");
        jRememberMe.setName("jRememberMe"); // NOI18N
        jRememberMe.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRememberMeItemStateChanged(evt);
            }
        });
        jPanel2.add(jRememberMe);

        jSavePassword.setText("Save Password");
        jSavePassword.setName("jSavePassword"); // NOI18N
        jSavePassword.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jSavePasswordItemStateChanged(evt);
            }
        });
        jPanel2.add(jSavePassword);

        jAutoLogin.setText("Auto Login");
        jAutoLogin.setName("jAutoLogin"); // NOI18N
        jAutoLogin.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jAutoLoginItemStateChanged(evt);
            }
        });
        jPanel2.add(jAutoLogin);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(2, 0, 0, 14));

        jLabel3.setText("Account:");
        jLabel3.setName("jLabel3"); // NOI18N
        jPanel3.add(jLabel3);

        jLabel4.setText("Password: ");
        jLabel4.setName("jLabel4"); // NOI18N
        jPanel3.add(jLabel4);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(2, 0, 0, 8));

        jScreenName.setName("jScreenName"); // NOI18N
        jScreenName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScreenNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScreenNameFocusLost(evt);
            }
        });
        jScreenName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jScreenNameKeyReleased(evt);
            }
        });
        jPanel1.add(jScreenName);

        jPassword.setName("jPassword"); // NOI18N
        jPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPasswordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jPasswordFocusLost(evt);
            }
        });
        jPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPasswordKeyReleased(evt);
            }
        });
        jPanel1.add(jPassword);

        jButton1.setText("Local Proxy Settings");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jContentPanelLayout = new javax.swing.GroupLayout(jContentPanel);
        jContentPanel.setLayout(jContentPanelLayout);
        jContentPanelLayout.setHorizontalGroup(
            jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jContentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jProgressBar, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonPanel, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jContentPanelLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jContentPanelLayout.createSequentialGroup()
                                .addGroup(jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton1)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 89, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jContentPanelLayout.setVerticalGroup(
            jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jContentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        javax.swing.GroupLayout jBasePanelLayout = new javax.swing.GroupLayout(jBasePanel);
        jBasePanel.setLayout(jBasePanelLayout);
        jBasePanelLayout.setHorizontalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jBasePanelLayout.setVerticalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(jBasePanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPasswordKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Login.toString())){
                loginPointBox();
            }
        }
}//GEN-LAST:event_jPasswordKeyReleased

    private void jScreenNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jScreenNameKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Login.toString())){
                loginPointBox();
            }
        }
}//GEN-LAST:event_jScreenNameKeyReleased

    private void jRememberMeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRememberMeItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!PointBoxConsoleProperties.getSingleton().isLoginWindowRememberMe(GatewayServerType.PBIM_SERVER_TYPE)){
                jSavePassword.setEnabled(true);
                jAutoLogin.setEnabled(false);
                jAutoLogin.setSelected(false);
                //event
                PointBoxConsoleProperties.getSingleton().storeLoginWindowRememberMe(true, GatewayServerType.PBIM_SERVER_TYPE);
            }
        }
        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowRememberMe(GatewayServerType.PBIM_SERVER_TYPE)){
                jSavePassword.setEnabled(false);
                jSavePassword.setSelected(false);
                jAutoLogin.setEnabled(false);
                jAutoLogin.setSelected(false);
                //event
                PointBoxConsoleProperties.getSingleton().storeLoginWindowRememberMe(false, GatewayServerType.PBIM_SERVER_TYPE);
            }
        }
}//GEN-LAST:event_jRememberMeItemStateChanged

    private void jSavePasswordItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jSavePasswordItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jRememberMe.setSelected(true);
            if (!PointBoxConsoleProperties.getSingleton().isLoginWindowSavePassword(GatewayServerType.PBIM_SERVER_TYPE)){
                jRememberMe.setEnabled(true);
                jRememberMe.setSelected(true);
                jAutoLogin.setEnabled(true);
                //event
                PointBoxConsoleProperties.getSingleton().storeLoginWindowSavePassword(true, GatewayServerType.PBIM_SERVER_TYPE);
            }
        }

        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowSavePassword(GatewayServerType.PBIM_SERVER_TYPE)){
                jAutoLogin.setEnabled(false);
                jAutoLogin.setSelected(false);
                //event
                PointBoxConsoleProperties.getSingleton().storeLoginWindowSavePassword(false, GatewayServerType.PBIM_SERVER_TYPE);
            }
        }
}//GEN-LAST:event_jSavePasswordItemStateChanged

    private void jAutoLoginItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jAutoLoginItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!PointBoxConsoleProperties.getSingleton().isLoginWindowAutoLogin(GatewayServerType.PBIM_SERVER_TYPE)){
                jRememberMe.setEnabled(true);
                jRememberMe.setSelected(true);
                jSavePassword.setEnabled(true);
                jSavePassword.setSelected(true);
                if (JOptionPane.showConfirmDialog(this, "Automatically log into the PointBox system? You may changed it back from the preferences window after login.",
                        "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                {
                    PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
                    prop.storeLoginWindowAutoLogin(true, GatewayServerType.PBIM_SERVER_TYPE);
                    prop.storeLoginWindowRememberMe(true, GatewayServerType.YIM_SERVER_TYPE);
                    prop.storeLoginWindowRememberMe(true, GatewayServerType.AIM_SERVER_TYPE);   
                    prop.storeLoginWindowSavePassword(true, GatewayServerType.YIM_SERVER_TYPE);
                    prop.storeLoginWindowSavePassword(true, GatewayServerType.AIM_SERVER_TYPE);
                    //Auto login property for Yahoo and AIM is not effective since they are hiden
                    prop.storeLoginWindowAutoLogin(true, GatewayServerType.YIM_SERVER_TYPE);
                    prop.storeLoginWindowAutoLogin(true, GatewayServerType.AIM_SERVER_TYPE);
                    
                    if (jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Login.toString())){
                        loginPointBox();
                    }
                    jAutoLogin.setSelected(true);
                }else{
                    jAutoLogin.setSelected(false);
                }
            }
        }

        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (PointBoxConsoleProperties.getSingleton().isLoginWindowAutoLogin(GatewayServerType.PBIM_SERVER_TYPE)){
                PointBoxConsoleProperties.getSingleton().storeLoginWindowAutoLogin(false, GatewayServerType.PBIM_SERVER_TYPE);
            }
        }
}//GEN-LAST:event_jAutoLoginItemStateChanged

    private void jLoginBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoginBtnActionPerformed
        if ((jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Login.toString()))
                || (jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Reconnect.toString())))
        {
            loginPointBox();
        }else if (jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.ForceLogin.toString())){
            forceLoginPointBox();
        }
}//GEN-LAST:event_jLoginBtnActionPerformed

    private void jShutdownBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShutdownBtnActionPerformed
        talker.getKernel().shutdown(getCurrentLoginUser(), true);
}//GEN-LAST:event_jShutdownBtnActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        proxySettingsDialog.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jScreenNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScreenNameFocusLost
        String accountName = jScreenName.getText();
        if (DataGlobal.isEmptyNullString(accountName)){
            return;
        }
        PointBoxConsoleProperties.getSingleton().storeLoginWindowLoginName(accountName.trim(),"", GatewayServerType.PBIM_SERVER_TYPE);
    }//GEN-LAST:event_jScreenNameFocusLost

    private void jPasswordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFocusLost
        PointBoxConsoleProperties.getSingleton().storeLoginWindowLoginPassword(new String(jPassword.getPassword()),"","", GatewayServerType.PBIM_SERVER_TYPE);
    }//GEN-LAST:event_jPasswordFocusLost

    private void jScreenNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScreenNameFocusGained
        if (jScreenName.getText() != null){
            jScreenName.setSelectionStart(0);
            jScreenName.setSelectionEnd(jScreenName.getText().length());
        }
    }//GEN-LAST:event_jScreenNameFocusGained

    private void jPasswordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPasswordFocusGained
        jPassword.setSelectionStart(0);
        jPassword.setSelectionEnd(jPassword.getPassword().length);
    }//GEN-LAST:event_jPasswordFocusGained

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAutoLogin;
    private javax.swing.JPanel jBasePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jButtonPanel;
    private javax.swing.JPanel jContentPanel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton jLoginBtn;
    private javax.swing.JTextArea jMessage;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPasswordField jPassword;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JCheckBox jRememberMe;
    private javax.swing.JCheckBox jSavePassword;
    private javax.swing.JTextField jScreenName;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jShutdownBtn;
    private javax.swing.ButtonGroup typeGroup;
    // End of variables declaration//GEN-END:variables

    boolean isAutoLogin() {
        return jAutoLogin.isSelected();
    }

    private class LoginPointBoxWorker extends SwingWorker<PointBoxLoginServiceResponse, String>{
        @Override
        protected PointBoxLoginServiceResponse doInBackground() throws Exception {
            publish("Log in the remote server...");
            if (jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Reconnect.toString())){
                return talker.getKernel().reloginPointBoxSystemServer(pbimLoginUser);
            }else{
                return talker.getKernel().loginPointBoxSystemServer(pbimLoginUser.getIMScreenName(), pbimLoginUser.getIMPassword());
            }
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
                handlePointBoxLoginServiceResponse(get());
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Interrupted Exception] " + ex.getMessage());
                talker.closeBuddyListPanelForLoginUser(GatewayBuddyListFactory.getLoginUserInstance(pbimLoginUser.getIMScreenName(), 
                                                                                            GatewayServerType.PBIM_SERVER_TYPE));
            } catch (ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Execution Exception] " + ex.getMessage());
                talker.closeBuddyListPanelForLoginUser(GatewayBuddyListFactory.getLoginUserInstance(pbimLoginUser.getIMScreenName(), 
                                                                                            GatewayServerType.PBIM_SERVER_TYPE));
            }
        }
    }

    /**
     * Handle the response to the request "loginPointBoxSystemServer"
     * @param loginResponse 
     */
    private void handlePointBoxLoginServiceResponse(PointBoxLoginServiceResponse loginResponse) {
        PointBoxServiceResult result = loginResponse.getResult();
        //display the description on why the request was not successfully processed
        switch(result){
            case RequestExecuted:
                //logger.log(Level.INFO, "zhijun -> line 1018......");
                IGatewayConnectorBuddy loginUser = GatewayBuddyListFactory.getLoginUserInstance(pbimLoginUser.getIMScreenName(), 
                                                                                                GatewayServerType.PBIM_SERVER_TYPE);
                loginUser.setBuddyStatus(BuddyStatus.Online);
                IPbcKernel kernel = talker.getKernel();
                kernel.setPointBoxAccountID(loginResponse.getPointBoxAccountID());
                try {
                    PointBoxConsoleSettings settings = loginResponse.getPointBoxConsoleSettings();
                    if (settings == null){
                        settings = new PointBoxConsoleSettings();
                    }
                    if (settings.getPointBoxAccountID() == null){
                        settings.setPointBoxAccountID(loginResponse.getPointBoxAccountID());
                    }
                    kernel.getPointBoxConsoleRuntime().initializeServerSidePointBoxConsoleSettingsAfterSuccessfulLogin(settings);
                } catch (Exception ex) {
//                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(WelcomeLoginFrame.this, ex.getMessage());
                    //Runtime.getRuntime().exit(1);
                }
                //logger.log(Level.INFO, "zhijun -> line 1030......");
                //(3) prepare buddy list panel for pbimLoginUser
                pbimLoginUser.setBuddyStatus(BuddyStatus.Online);
                talker.openBuddyListPanelForLoginUser(pbimLoginUser);
                //completed the job and just wait for events happened from the server-side
                jMessage.setForeground(Color.blue);
                jMessage.setText("Authenticating your credentials...");
                if (jLoginBtn.getText().equalsIgnoreCase(LoginButtonText.Reconnect.toString())){
                    jLoginBtn.setText(LoginButtonText.Login.toString());
                    hideFaceComponent();
                }
                try {
                    NIOGlobal.deleteFile(PbcProperties.getSingleton().getReleaseRestartFlagFilePath(PbcProperties.getSingleton().getReleaseUserType()));
                } catch (Exception ex) {
                    //logger.log(Level.SEVERE, null, ex);
                }
                //logger.log(Level.INFO, "zhijun -> line 1046......");
                //pricer type
                PbcPricerType oldType = PointBoxConsoleProperties.getSingleton().retrievePbcPricerType(loginUser.getIMUniqueName());
                //logger.log(Level.INFO, "zhijun -> line 1049......");
                kernel.raisePointBoxEvent(new PBPricerChangedEvent(PointBoxEventTarget.PbcPricer,
                                                                                oldType, 
                                                                                oldType,
                                                                                PointBoxConsoleProperties.getSingleton(),
                                                                                kernel));
                //logger.log(Level.INFO, "zhijun -> line 1055......");
                break;
            case IDNotAuthenticated:
                jMessage.setForeground(Color.red);
                jMessage.setText("Login refused. Please check your login and password.");
                break;
            case LoginConflict:
                if ((JOptionPane.showConfirmDialog(this, 
                                                   "Your account has been logged into system from a different workstation! Do you want to login anyway by killing the existing connection?", 
                                                   "Confirm: ", 
                                                   JOptionPane.YES_NO_OPTION) 
                        == JOptionPane.YES_OPTION))
                {
                    jLoginBtn.setText(LoginButtonText.ForceLogin.toString());
                    jScreenName.setEnabled(false);
                    jPassword.setEnabled(false);
                
                }else{
                    jScreenName.setEnabled(true);
                    jPassword.setEnabled(true);
                }
                jProgressBar.setIndeterminate(false);
                jLoginBtn.setEnabled(true);
                jShutdownBtn.setEnabled(true);
                //connector login conflict
                jMessage.setForeground(Color.red);
                jMessage.setText("Your account has been logged into system from a different workstation! You may login away by killing such a connection or shut down.");
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
                jMessage.setText(loginResponse.getDescription());
        }//switch
        if (result.equals(PointBoxServiceResult.RequestExecuted)){
            //logger.log(Level.INFO, "zhijun -> line 1116......");
            talker.getKernel().setPointBoxAccountID(loginResponse.getPointBoxAccountID());
        }else{
            talker.closeBuddyListPanelForLoginUser(GatewayBuddyListFactory.getLoginUserInstance(pbimLoginUser.getIMScreenName(), 
                                                                                        GatewayServerType.PBIM_SERVER_TYPE));
            jLoginBtn.setEnabled(true);
            jScreenName.setEnabled(true);
            jPassword.setEnabled(true);
            jShutdownBtn.setEnabled(true);
            jAutoLogin.setSelected(false);
            jProgressBar.setIndeterminate(false);
            jMessage.setForeground(Color.red);
            //talker.getKernel().destroyPointBoxAccountID();
        }
    }//end of hanldleWebServiceResponseHelper

    /**
     * Login failed because another person used the same account to log in first. ForceOutSystemLogin
     * is used to force such a person be out of connection.
     */
    private class ForcePointBoxSystemLogin extends SwingWorker<PointBoxLoginServiceResponse, Void>{
        
        @Override
        protected PointBoxLoginServiceResponse doInBackground() throws Exception {
            return talker.getKernel().forceLoginPointBoxSystem(pbimLoginUser);
        }

        @Override
        protected void done() {
            try {
                handlePointBoxLoginServiceResponse(get());
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Interrupted Exception] " + ex.getMessage());
                talker.closeBuddyListPanelForLoginUser(GatewayBuddyListFactory.getLoginUserInstance(pbimLoginUser.getIMScreenName(), 
                                                                                            GatewayServerType.PBIM_SERVER_TYPE));
            } catch (ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
                jMessage.setText("[Execution Exception] " + ex.getMessage());
                talker.closeBuddyListPanelForLoginUser(GatewayBuddyListFactory.getLoginUserInstance(pbimLoginUser.getIMScreenName(), 
                                                                                            GatewayServerType.PBIM_SERVER_TYPE));
            }
        }

    }//ForceOutSystemLogin
    
    private static enum LoginButtonText {
        ForceLogin("Login Anyway"),
        Reconnect("Reconnect"),
        Login("Login"),
        Checking_Release("Checking Release...");

        private String term;
        LoginButtonText(String term){
            this.term = term;
        }

        public static LoginButtonText convertToType(String term){
            if (term == null){
                return null;
            }
            if (term.equalsIgnoreCase(Login.toString())){
                return Login;
            }else if (term.equalsIgnoreCase(ForceLogin.toString())){
                return ForceLogin;
            }else if (term.equalsIgnoreCase(Reconnect.toString())){
                return Reconnect;
            }else{
                return Checking_Release;
            }
        }

        @Override
        public String toString() {
            return term;
        }
    }
}
