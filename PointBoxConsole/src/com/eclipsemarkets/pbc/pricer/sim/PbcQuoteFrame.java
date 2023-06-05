/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.MessageSentEvent;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.data.PbconsoleQuoteFactory;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.web.MessagingState;
import com.eclipsemarkets.gateway.web.QuoteLegState;
import com.eclipsemarkets.gateway.web.QuoteState;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.global.util.EaioUUID;
import com.eclipsemarkets.parser.PbcSimGuiParser;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.PointBoxAccountID;
import com.eclipsemarkets.web.PointBoxConnectorID;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.tabbed.VetoableTabCloseListener;

/**
 *
 * @author Zhijun Zhang
 */
public class PbcQuoteFrame extends PbcStructuredQuoteBuilder implements IPbcStructuredQuoteBuilder{
    /**
     * the panel contained by jTopPanel
     */
    private PbcQuotePanel quotePanel;
    
    /**
     * a collection of leg panels contained by jTabbedPane
     * 
     * Potentially, one quote may have many legs. Current only offer two legs for free.
     * 
     * This data field should be synchronized (THREAD-SAFE)
     */
    private final ArrayList<PbcQuoteLegPanel> legPanelList = new ArrayList<PbcQuoteLegPanel>();
    
    /**
     * Creates new form PbcQuoteFrame
     */
    public PbcQuoteFrame(IPbcKernel kernel, PointBoxQuoteType category) {
        super(kernel, category);
        initComponents();
        quotePanel = new PbcQuotePanel(this);
        jTopPanel.add(quotePanel, BorderLayout.CENTER);
            
        addPointBoxQuoteLegPanelHelper(false);
        
        setTabCloseGuard();
        
        setIconImage(kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon().getImage());
        
        pack();
        setLocation(SwingGlobal.getScreenCenterPoint(this));
        
    }

    /**
     * 
     * @param msg which might have already included code
     * @return 
     */
    private boolean isCodeRequired(String msg) {
        if (PbcSimGuiParser.parsePointBoxQuoteCodeFromMessageText(msg) == null){
            return quotePanel.isCodeIncluded();
        }else{
            return false;
        }
    }

    /**
     * This method assumes that the quote-message is ready. Refer to isQuoteMessageReady().
     * @return 
     */
    @Override
    String generateSimMarkFieldValuesToken() {
        synchronized(legPanelList){
            if (quotePanel.isQuoteMessageReady()){
                String token = quotePanel.generateSimMarkFieldValuesToken();
                for (PbcQuoteLegPanel aPbcQuoteLegPanel : legPanelList){
                    token += PbcSimGuiParser.SimMarkPartDelimiter + aPbcQuoteLegPanel.generateSimMarkFieldValuesToken();
                }
                return token;
            }else{
                return null;
            }
        }
    }

    @Override
    public PointBoxQuoteCode getSelectedPointBoxQuoteCode() {
        if (quotePanel == null){
            return getKernel().getSelectedSimCodeFromProperties();
        }
        PointBoxQuoteCode code = quotePanel.getSelectedPointBoxQuoteCode();
        if (code == null){
            return getKernel().getSelectedSimCodeFromProperties();
        }else{
            return code;
        }
    }
    
    @Override
    public int getFormatValueMaxForSelectedPointBoxQuoteCode(PointBoxQuoteCode selectedCode){
        PbcPricingModel model = getKernel().getPbcPricingModel(selectedCode);
        if (model == null){
            return 4;
        }else{
            return model.getSqMaxDecimal();
        }
    }
    
    @Override
    public int getFormatValueMinForSelectedPointBoxQuoteCode(PointBoxQuoteCode selectedCode){
        PbcPricingModel model = getKernel().getPbcPricingModel(selectedCode);
        if (model == null){
            return 2;
        }else{
            return model.getSqMinDecimal();
        }
    }

    @Override
    public void setSelectedPointBoxQuoteCode(PointBoxQuoteCode targetCode) {
        quotePanel.setSelectedPointBoxQuoteCode(targetCode);
    }
    
    private String getCurrentTitleForSelectedCode(){
        String title = "Option Pricer: ";
        PointBoxQuoteCode code = getSelectedPointBoxQuoteCode();
        String codeNotes = getKernel().getPointBoxConsoleRuntime().getNotesOfCode(code);
        if (DataGlobal.isEmptyNullString(codeNotes)){
            title += code.toString();
        }else{
            title += code.toString() + " - " + codeNotes;
        }
        return title;
    }

    @Override
    public void displaySimGUI() {
        if (SwingUtilities.isEventDispatchThread()){
            displaySimGUIHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displaySimGUIHelper();
                }
            });
        }
    }
    
    private void displaySimGUIHelper(){
        setTitle(getCurrentTitleForSelectedCode());
//        refreshGuiForSelectedCode(getSelectedPointBoxQuoteCode());
        this.setExtendedState(Frame.NORMAL);
        setVisible(true);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMasterSplitPane = new javax.swing.JSplitPane();
        jTopPanel = new javax.swing.JPanel();
        jTabbedPane = new javax.swing.JTabbedPane();

        setTitle("Option Pricer");
        setMinimumSize(new java.awt.Dimension(800, 600));
        setResizable(false);

        jMasterSplitPane.setDividerLocation(200);
        jMasterSplitPane.setDividerSize(1);
        jMasterSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jMasterSplitPane.setPreferredSize(new java.awt.Dimension(800, 600));
        jMasterSplitPane.setRequestFocusEnabled(false);

        jTopPanel.setLayout(new javax.swing.BoxLayout(jTopPanel, javax.swing.BoxLayout.LINE_AXIS));
        jMasterSplitPane.setLeftComponent(jTopPanel);

        jTabbedPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jMasterSplitPane.setRightComponent(jTabbedPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jMasterSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jMasterSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jMasterSplitPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JPanel jTopPanel;
    // End of variables declaration//GEN-END:variables

    void addPointBoxQuoteLegPanel(final boolean displayWarningMessageDialog) {
        if (SwingUtilities.isEventDispatchThread()){
            addPointBoxQuoteLegPanelHelper(displayWarningMessageDialog);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    addPointBoxQuoteLegPanelHelper(displayWarningMessageDialog);
                }
            });
        }
    }

    private void addPointBoxQuoteLegPanelHelper(boolean displayWarningMessageDialog) {
        synchronized(legPanelList){
            if (legPanelList.size() >= 2){
                if (displayWarningMessageDialog){
                    JOptionPane.showMessageDialog(this, "Your account service supports at most 2 legs.");
                }
            }else{
                PbcQuoteLegPanel aPointBoxQuoteLegPanel = new PbcQuoteLegPanel(legPanelList.size() + 1, 
                                                                               this);
                legPanelList.add(aPointBoxQuoteLegPanel);
                jTabbedPane.addTab(aPointBoxQuoteLegPanel.getLegTabName(), aPointBoxQuoteLegPanel);
                jTabbedPane.setSelectedIndex(legPanelList.size() - 1);
                
                populateQuoteMessageTextField();
            }
        }
    }

    void removePointBoxQuoteLegPanel() {
        if (SwingUtilities.isEventDispatchThread()){
            removePointBoxQuoteLegPanelHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    removePointBoxQuoteLegPanelHelper();
                }
            });
        }
    }

    private void removePointBoxQuoteLegPanelHelper() {
        synchronized(legPanelList){
            if (legPanelList.size() > 1){
                int tabIndex = jTabbedPane.getTabCount()-1;
                jTabbedPane.removeTabAt(tabIndex);
                legPanelList.remove(tabIndex);
                
                populateQuoteMessageTextField();
            }
        }
    }
    
    /**
     * @deprecated 
     * @param jSpinner
     * @param negativePermitted
     * @param selectedCode 
     */
    void setupSpinner(JSpinner jSpinner, boolean negativePermitted, PointBoxQuoteCode selectedCode) {
        double min;
        if (negativePermitted){
            min = -100.0000;
        }else{
            min = 0.0000;
        }
        double value = 0.0000;
        double max = 100.0000;
        double stepSize = 0.0005;
        
        Object originalValue = jSpinner.getValue();
        
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, stepSize);
        jSpinner.setModel(model);
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)jSpinner.getEditor();
        DecimalFormat format = editor.getFormat();
        format.setMinimumFractionDigits(this.getFormatValueMaxForSelectedPointBoxQuoteCode(selectedCode));
        JFormattedTextField aJTextField = editor.getTextField();
        
        Dimension d = jSpinner.getPreferredSize();
        d.width = 65;
        jSpinner.setPreferredSize(d);
        aJTextField.setValue(0.0000);
        aJTextField.getDocument().addDocumentListener(new DocumentListener(){

            @Override
            public void insertUpdate(DocumentEvent e) {
                populateQuoteMessageTextField();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                populateQuoteMessageTextField();
            }
        });
        try{
            if (originalValue instanceof Double){
                jSpinner.setValue(originalValue);
            }
        }catch(Exception ex){}
    }

    void populateQuoteMessageTextField() {
        synchronized(legPanelList){
            if (!legPanelList.isEmpty()){
                String message = legPanelList.get(0).generateLegMessageToken();
                for (int i = 1; i < legPanelList.size(); i++){
                    message += PbcSimGuiParser.LegDelimiter + legPanelList.get(i).generateLegMessageToken();
                }
                quotePanel.finalizeQuoteMessageTextField(message);
            }
        }
    }

    void refreshGuiForSelectedCode(final PointBoxQuoteCode selectedCode) {
        if (SwingUtilities.isEventDispatchThread()){
            refreshGuiForSelectedCodeHelper(selectedCode);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    refreshGuiForSelectedCodeHelper(selectedCode);
                }
            });
        }
    }

    private void refreshGuiForSelectedCodeHelper(PointBoxQuoteCode selectedCode) {
        setTitle(getCurrentTitleForSelectedCode());
        quotePanel.refreshGuiForSelectedCode(selectedCode);
        synchronized(legPanelList){
            if (!legPanelList.isEmpty()){
                for (int i = 0; i < legPanelList.size(); i++){
                    legPanelList.get(i).refreshGuiForSelectedCode(selectedCode);
                }
            }
        }
    }

    void clearPbcQuoteFrame() {
        quotePanel.clearPbcQuotePanel();
        synchronized(legPanelList){
            if (!legPanelList.isEmpty()){
                for (PbcQuoteLegPanel aPbcQuoteLegPanel :legPanelList){
                    aPbcQuoteLegPanel.clearPbcQuoteLegPanel();
                }
            }
        }
    }

    /**
     * Calculate each PUT/CALL option's prices
     * 
     * @param legIndex
     * @param optionIndex
     * @return - when GUI is not completed for the corresponding option, NULL is returned
     */
    IPbsysOptionQuote calculateOptionPrice(int legIndex, int optionIndex) {
        try {
            IPbsysOptionQuote quote = generatePbsysStructuredOptionQuote(legIndex, optionIndex);
            quote.setPbcPricingModel(getKernel().getPointBoxConsoleRuntime().getPbcPricingModelMap().get(getSelectedPointBoxQuoteCode().name()));
            getKernel().pricePbsysSingleQuote(quote);
            return quote;
        } catch (Exception ex) {
            return null;
        }
    }

    IPbsysOptionQuote calculatePrice(boolean displayMessage) {
        String qmsg = quotePanel.getPopulatedQuoteMessage();
        if (qmsg == null){
            displayWarningMessage("Pricing failed! Structured message is not completed.", displayMessage);
            return null;
        }else{
            /**
             * Calculation: current pricer cannot always calculate the price correctly 
             * except some handled structures and call/put. Thus, the calculation will 
             * count on "the leg table's result" instead of the pricer. The pricer only 
             * does the job of calculateOptionPrice().
             */
            try {
                IPbsysOptionQuote quote = generatePbsysStructuredQuote(qmsg, false);
                getKernel().pricePbsysSingleQuote(quote);
                synchronized(legPanelList){
                    for (PbcQuoteLegPanel legPanel : legPanelList){
                        legPanel.updatePanelForPricedQuote(quote);
                    }
                }
                return quote;
            } catch (Exception ex) {
                displayWarningMessage("Pricing failed! " + ex.getMessage(), displayMessage);
                Logger.getLogger(PbcQuoteFrame.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    @Override
    public boolean populateQuoteFromViewer(IPbsysOptionQuoteWrapper targetQuoteWrapper) {    
        if (targetQuoteWrapper == null){
            return false;
        }
        IPbsysOptionQuote quote = targetQuoteWrapper.getQuoteOwner();
        if (targetQuoteWrapper == null){
            return false;
        }
        
        synchronized(legPanelList){
            clearPbcQuoteFrame();
            
            removePointBoxQuoteLegPanel();

            PointBoxQuoteCode code = PointBoxQuoteCode.convertEnumNameToType(quote.getPbcPricingModel().getSqCode());
            quotePanel.populateQuoteFromViewer(quote);
            legPanelList.get(0).populateQuoteFromViewer(code, quote.getOptionStrategyLegs().get(0));
            if (!quote.getOptionStrategyLegs().get(1).isEmptyLeg()){
                addPointBoxQuoteLegPanel(false);
                legPanelList.get(1).populateQuoteFromViewer(code, quote.getOptionStrategyLegs().get(1));
            }
            populateQuoteMessageTextField();
            handleCalculateBtnClicked(false);
        }
        return true;
    }

    private IPbsysOptionQuote generatePbsysStructuredQuote(String qmsg, boolean simLabelRequired) throws Exception {
        IPbsysOptionQuote quote = PbconsoleQuoteFactory.createStructuredOptionQuoteInstance(quotePanel.getSelectedPbcPricingModel());
        quote.setPbsysInstantMessage(generatePbsysStructuredInstantMessage(qmsg, simLabelRequired));
        quote.setSufficientPricingData(true);
        quote.setBidAskStatus(quotePanel.getBidAskStatus());
        quote.setOptionAskPricePrivateIncoming(DataGlobal.convertToString(quotePanel.getSelectedAskValue(), 4));
        quote.setOptionBidPricePrivateIncoming(DataGlobal.convertToString(quotePanel.getSelectedBidValue(), 4));
        PbcQuoteLegPanel legPanel;
        synchronized(legPanelList){
            for (int i = 0; i < legPanelList.size(); i++){
                legPanel = legPanelList.get(i);
                legPanel.loadPbsysQuoteLeg(quote, i);
            }
        }
        quote.setStructuredMessageQuote(true);
        return quote;
    }
    
    private IPbsysOptionQuote generatePbsysStructuredOptionQuote(int legIndex, int optionIndex) throws Exception {
        IPbsysOptionQuote quote = PbconsoleQuoteFactory.createStructuredOptionQuoteInstance(quotePanel.getSelectedPbcPricingModel());
        quote.setPbsysInstantMessage(generateEmptyPbsysInstantMessageForOption());
        quote.setSufficientPricingData(true);
        synchronized(legPanelList){
            legPanelList.get(legIndex).loadPbsysQuoteLegForOption(quote.getOptionStrategyLegs().get(legIndex), optionIndex);
        }
//        if (quote.getOptionStrategyLegs().size() < 2){
//            quote.addNewPbsysQuoteLeg();
//        }
        return quote;
    }
    
    boolean isCustomSimQuote() {
        boolean result = false;
        synchronized(legPanelList){
            for (PbcQuoteLegPanel aPbcQuoteLegPanel : legPanelList){
                if (aPbcQuoteLegPanel.isCustomSimLeg()){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    private IPbsysInstantMessage generateEmptyPbsysInstantMessageForOption(){
        return generatePbsysStructuredInstantMessage("empty message", false);
    }

    /**
     * 
     * @param qmsg
     * @param simMarkRequired - this should be true only for sending IPbsysInstantMessage.
     * @return 
     */
    private IPbsysInstantMessage generatePbsysStructuredInstantMessage(String qmsg, boolean simMarkRequired) {
        if (simMarkRequired){
            String simMarkToken = generateSimMarkToken();
            if (DataGlobal.isNonEmptyNullString(simMarkToken)){
                qmsg += PbcSimGuiParser.WhiteSpace + simMarkToken;
            }
            if (isCodeRequired(qmsg)){
                //qmsg has no CODE yet...
                qmsg = quotePanel.getSelectedPbcPricingModel().getSqCode() + PbcSimGuiParser.WhiteSpace + qmsg;
            }
        }
        IGatewayConnectorBuddy loginUser = getKernel().getPointBoxLoginUser();
        IPbsysInstantMessage quoteMessage = PbconsoleQuoteFactory.createPbsysInstantMessageInstance(GatewayServerType.PBIM_SERVER_TYPE);
        quoteMessage.setMessageContent(qmsg);
        quoteMessage.setOutgoing(true);
        IGatewayConnectorBuddy buddy = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(getKernel().getPointBoxLoginUser(), loginUser.getIMScreenName(), null);
        quoteMessage.setToUser(buddy);
        quoteMessage.setFromUser(buddy);
        quoteMessage.setPbcMessageUuid((new EaioUUID()).toString());
        quoteMessage.setMessageTimestamp(new GregorianCalendar());
        return quoteMessage;
    }

    void displayWarningMessage(final String msg, final boolean displayMessage) {
        if (SwingUtilities.isEventDispatchThread()){
            displayWarningMessageHelper(msg, displayMessage);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayWarningMessageHelper(msg, displayMessage);
                }
            });
        }
    }
    
    private void displayWarningMessageHelper(final String msg, final boolean displayMessage) {
        if (displayMessage){
            JOptionPane.showMessageDialog(this, msg);
        }
    }

    /**
     * Constructed structured message and send it out by means of PitsCast
     * @param aBuddyList 
     */
    void sendStructuredMessageTo(final ArrayList<IGatewayConnectorBuddy> aBuddyList, 
                                 final IPbcSimMessagingListener listener) 
    {
        (new SwingWorker<Void, String>(){
            @Override
            protected Void doInBackground() throws Exception {
                if (quotePanel.isQuoteMessageReady()){
                    if ((aBuddyList == null) || (aBuddyList.isEmpty())){
                        publish("");
                        return null;
                    }
                    PointBoxAccountID accountID = getKernel().getPointBoxAccountID();
                    IPbsysInstantMessage aPbsysInstantMessage;
                    IPbsysOptionQuote quote;
                    PointBoxConnectorID connectorID;
                    for (IGatewayConnectorBuddy buddy : aBuddyList){
                        try{
                            if (BuddyStatus.Online.equals(buddy.getBuddyStatus())){
                                connectorID = GatewayBuddyListFactory.convertToPointBoxConnectorID(buddy.getLoginOwner());
                                if (GatewayServerType.PBIM_SERVER_TYPE.toString().equalsIgnoreCase(connectorID.getGatewayServerType())){
                                    //if it is not PBIM, it has to be regular message without "SimLabel"
                                    aPbsysInstantMessage = generatePbsysStructuredInstantMessage(quotePanel.getPopulatedQuoteMessage(), true);
                                }else{
                                    //if it is not PBIM, it has to be regular message without "SimLabel"
                                    aPbsysInstantMessage = generatePbsysStructuredInstantMessage(quotePanel.getPopulatedQuoteMessage(), false);
                                }
                                aPbsysInstantMessage.setOutgoing(true);
                                aPbsysInstantMessage.setToUser(buddy);
                                aPbsysInstantMessage.setFromUser(buddy.getLoginOwner());
                                //send SIM...
                                getKernel().sendInstantMessage(accountID, connectorID, aPbsysInstantMessage);
                                //publish this sent message
                                quote = generatePbsysStructuredQuote(quotePanel.getPopulatedQuoteMessage(), false);
                                quote.getInstantMessage().setToUser(buddy);
                                quote.getInstantMessage().setOutgoing(true);
                                PbconsoleQuoteFactory.registerOutgoingMessageQuote(quote);
                                getKernel().raisePointBoxEvent(new MessageSentEvent(PointBoxEventTarget.PbcFace, quote));
                                publish("Sent to " + connectorID.getIDString());
                            }
                        }catch (Exception ex){
                            Logger.getLogger(PbcQuoteFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }//try
                        Thread.sleep(1000);
                    }//for-loop
                    return null;
                }else{
                    publish("SIM is not constructed yet.");
                    return null;
                }
            }

//            @Override
//            protected void process(List<String> chunks) {
//                for (String msg : chunks){
//                    listener.publishStatusMessage(msg);
//                }
//            }

            @Override
            protected void done() {
                //listener.publishStatusMessage("SIM is done. " + CalendarGlobal.convertToHHmmss(new Date(), ":"));
            }
            
        }).execute();
    }

    private MessagingState constructStructuredMessagingState(ArrayList<IGatewayConnectorBuddy> buddiesOfPitsCastGroup) {
        MessagingState aStructuredMessagingState = new MessagingState();
        aStructuredMessagingState.setFromUserName(getKernel().getPointBoxAccountID().getLoginName());
        aStructuredMessagingState.setMessage(quotePanel.getPopulatedQuoteMessage());
        aStructuredMessagingState.setMsgUuid((new EaioUUID()).toString());
        aStructuredMessagingState.setPbcMessageUuid((new EaioUUID()).toString());
        aStructuredMessagingState.setServerType(getKernel().getPointBoxAccountID().getGatewayServerType());
        aStructuredMessagingState.setStatus(1); //outgoing
        aStructuredMessagingState.setTimeInMillisStamp((new GregorianCalendar()).getTimeInMillis());
        aStructuredMessagingState.setToUserName(constructPitsCastMemberListForStructuredMessagingState(buddiesOfPitsCastGroup));
        
        QuoteState aQuoteState = constructStructuredQuoteState();
        aQuoteState.setMsgUuid(aStructuredMessagingState.getMsgUuid());
        aStructuredMessagingState.setQuoteState(aQuoteState);
        
        return aStructuredMessagingState;
    }

    private QuoteState constructStructuredQuoteState() {
        QuoteState aQuoteState = new QuoteState();
        aQuoteState.setAsk(quotePanel.getSelectedAskValue());
        aQuoteState.setBid(quotePanel.getSelectedBidValue());
        aQuoteState.setPricable(true);
        aQuoteState.setPricingTimestamp((new GregorianCalendar()).getTimeInMillis());
        aQuoteState.setStructureCode(PbcSimGuiParser.LegDelimiter);
        aQuoteState.setQuoteUuid((new EaioUUID()).toString());
        ArrayList<QuoteLegState> aQuoteLegStateList = new ArrayList<QuoteLegState>();
        QuoteLegState aQuoteLegState;
        synchronized(legPanelList){
            for (PbcQuoteLegPanel legPanel : legPanelList){
                aQuoteLegState = legPanel.constructQuoteLegState();
                aQuoteLegState.setQuoteUuid(aQuoteState.getQuoteUuid());
                aQuoteLegStateList.add(aQuoteLegState);
            }
        }
        aQuoteState.setQuoteLegStates(aQuoteLegStateList.toArray(new QuoteLegState[0]));
        return aQuoteState;
    }

    private String constructPitsCastMemberListForStructuredMessagingState(ArrayList<IGatewayConnectorBuddy> buddiesOfPitsCastGroup) {
        String toUserList = "";
        for (IGatewayConnectorBuddy buddy : buddiesOfPitsCastGroup){
            if (GatewayServerType.PBIM_SERVER_TYPE.equals(buddy.getIMServerType())){
                if (BuddyStatus.Online.equals(buddy.getBuddyStatus())){
                    toUserList += buddy.getIMScreenName() + MessagingState.delimiter;
                }
            }
        }
        return toUserList.substring(0, toUserList.length()-1);
    }

    void handleCalculateBtnClicked(boolean displayMessage) {
        quotePanel.handleCalculateBtnClicked(displayMessage);
    }

    private void setTabCloseGuard(){
        SubstanceLookAndFeel.registerTabCloseChangeListener(jTabbedPane, new VetoableTabCloseListener() {

            @Override
            public boolean vetoTabClosing(JTabbedPane jtp, Component cmpnt) {
                return true;
            }

            @Override
            public void tabClosing(JTabbedPane jtp, Component cmpnt) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void tabClosed(JTabbedPane jtp, Component cmpnt) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        });
    }
}
