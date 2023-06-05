/**
 * Eclipse Market Solutions LLC
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.pbc.face.talker.IMessagingBoardState;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.PbconsoleQuoteFactory;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.*;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.runtime.settings.record.IMessageTabRecord;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.*;

/**
 * MessagingBoardState
 * <P>
 * Mostly read-only state.
 * <P>
 * @author Zhijun Zhang
 * Created on May 10, 2011 at 5:24:01 PM
 */
class MessagingBoardState implements IMessagingBoardState{
    
    private static final Logger logger = Logger.getLogger(MessagingBoardState.class.getName());
        
    private final String tabButtonUniqueID;
    private final Object stateObject;
    private ArrayList<IGatewayConnectorBuddy> groupMembers;

    private final DataMembersGuardedByState dataMembers;
    
    /**
     * General format of documents used by this board
     */
    private SimpleAttributeSet outgoingUserFontFormat = SwingGlobal.BOLD_BLACK_LOCAL_USER;
    private SimpleAttributeSet incomingUserFontFormat = SwingGlobal.BOLD_BLUE_REMOTE_USER;
    private SimpleAttributeSet outgoingMessageFontFormat = SwingGlobal.BLACK_MESSAGE;
    private SimpleAttributeSet incomingMessageFontFormat = SwingGlobal.BLACK_MESSAGE;
    
    public final static String LINK_ATTRIBUTE = "linkact";
    
    private final IPbcKernel kernel;
    
    /**
     * 
     * @param pair - pair.getUniqueID() is used as tabButtonUniqueID
     */
    MessagingBoardState(IPbcKernel kernel, TargetBuddyPair pair, String tabButtonUniqueID){
        this.kernel = kernel;
        this.tabButtonUniqueID = tabButtonUniqueID;
        stateObject = pair;
        groupMembers = new ArrayList<IGatewayConnectorBuddy>();
        dataMembers = new DataMembersGuardedByState(this);
    }

    /**
     * 
     * @param group - group.getIMUniqueName() is used as tabButtonUniqueID
     */
    MessagingBoardState(IPbcKernel kernel, IGatewayConnectorGroup group, String tabButtonUniqueID){
        this.kernel = kernel;
        this.tabButtonUniqueID = tabButtonUniqueID;
        stateObject = group;
        groupMembers = new ArrayList<IGatewayConnectorBuddy>();
        dataMembers = new DataMembersGuardedByState(this);
    }

//    /**
//     * This is used by fake tabButton
//     * @param tabButton 
//     */
//    MessagingBoardState(String tabButtonUniqueID){
//        this.tabButtonUniqueID = tabButtonUniqueID;
//        stateObject = null;
//        groupMembers = new ArrayList<IGatewayConnectorBuddy>();
//        dataMembers = new DataMembersGuardedByState(this);
//    }

    @Override
    public SimpleAttributeSet getOutgoingUserFontFormat() {
        return outgoingUserFontFormat;
    }

    @Override
    public SimpleAttributeSet getIncomingUserFontFormat() {
        return incomingUserFontFormat;
    }

    @Override
    public SimpleAttributeSet getOutgoingMessageFontFormat() {
        return outgoingMessageFontFormat;
    }

    @Override
    public SimpleAttributeSet getIncomingMessageFontFormat() {
        return incomingMessageFontFormat;
    }

    @Override
    public boolean isGroupState() {
        return (stateObject instanceof IGatewayConnectorGroup);
    }

    @Override
    public void customizeTextFormat(IMessageTabRecord messageTabRecord) {
        //outgoingUserFontFormat
        outgoingUserFontFormat = new SimpleAttributeSet();
        StyleConstants.setForeground(outgoingUserFontFormat, messageTabRecord.getMyForeground());
        StyleConstants.setBold(outgoingUserFontFormat, true);
        StyleConstants.setFontFamily(outgoingUserFontFormat, messageTabRecord.getMyFont().getFamily());
        StyleConstants.setFontSize(outgoingUserFontFormat, messageTabRecord.getMyFont().getSize());
        //outgoingMessageFontFormat
        outgoingMessageFontFormat = new SimpleAttributeSet();
        StyleConstants.setForeground(outgoingMessageFontFormat, messageTabRecord.getMyForeground());
        StyleConstants.setBold(outgoingMessageFontFormat, false);
        StyleConstants.setFontFamily(outgoingMessageFontFormat, messageTabRecord.getMyFont().getFamily());
        StyleConstants.setFontSize(outgoingMessageFontFormat, messageTabRecord.getMyFont().getSize());
        //incomingUserFontFormat
        incomingUserFontFormat = new SimpleAttributeSet();
        StyleConstants.setForeground(incomingUserFontFormat, messageTabRecord.getBuddyForeground());
        StyleConstants.setBold(incomingUserFontFormat, true);
        StyleConstants.setFontFamily(incomingUserFontFormat, messageTabRecord.getBuddyFont().getFamily());
        StyleConstants.setFontSize(incomingUserFontFormat, messageTabRecord.getBuddyFont().getSize());
        //incomingMessageFontFormat
        incomingMessageFontFormat = new SimpleAttributeSet();
        StyleConstants.setForeground(incomingMessageFontFormat, messageTabRecord.getBuddyForeground());
        StyleConstants.setBold(incomingMessageFontFormat, false);
        StyleConstants.setFontFamily(incomingMessageFontFormat, messageTabRecord.getBuddyFont().getFamily());
        StyleConstants.setFontSize(incomingMessageFontFormat, messageTabRecord.getBuddyFont().getSize());
    }

    @Override
    public String getTabButtonUniqueID() {
        return tabButtonUniqueID;
    }

    @Override
    public Object getStateObject() {
        return stateObject;
    }

    boolean isForBuddy(){
        return (stateObject instanceof TargetBuddyPair);
    }

    boolean isForGroup(){
        return (stateObject instanceof IGatewayConnectorGroup);
    }

    @Override
    public String getDescriptiveName() {
        if (stateObject instanceof TargetBuddyPair){
            TargetBuddyPair targetBuddyPair = (TargetBuddyPair)stateObject;
            IGatewayConnectorBuddy buddy = targetBuddyPair.getBuddy();
            if ((DataGlobal.isEmptyNullString(buddy.getNickname())) || (buddy.getNickname().equalsIgnoreCase("Unknown"))){
                return " " + buddy.getIMScreenName() + " (" + targetBuddyPair.getLoginUser().getIMScreenName() + ")";
            }else{
                return " " + buddy.getNickname() + " (" + targetBuddyPair.getLoginUser().getIMScreenName() + ")";
            }

        }else if (stateObject instanceof IGatewayConnectorGroup){
            IGatewayConnectorGroup targetGroup = (IGatewayConnectorGroup)stateObject;
            return " " + targetGroup.getIMUniqueName();
        }else{
            return " ";
        }
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getGroupMembers() {
        return groupMembers;
    }

    @Override
    public void setGroupMembers(ArrayList<IGatewayConnectorBuddy> groupMembers) {
        this.groupMembers = groupMembers;
    }

    GatewayServerType getServerType() {
        if (stateObject instanceof TargetBuddyPair){
            return ((TargetBuddyPair)stateObject).getLoginUser().getIMServerType();
        }else if (stateObject instanceof IGatewayConnectorGroup){
            return ((IGatewayConnectorGroup)stateObject).getServerType();
        }else {
            return GatewayServerType.PBIM_SERVER_TYPE;
        }
    }
    
    @Override
    public List<IPbsysOptionQuote> getPublishedQuotesForRefresh(){
        return dataMembers.getPublishedQuotesForPriceRefresh();
    }

    @Override
    public void cleanupHistoryDocument() {
        dataMembers.cleanupHistoryDocument();
    }

    @Override
    public void cleanupEntryDocument() {
        dataMembers.cleanupEntryDocument();
    }

    @Override
    public boolean isArchiveWarningMessageRequired() {
        return dataMembers.isArchiveWarningMessageRequired();
    }

    @Override
    public void setArchiveWarningMessageRequired(boolean value) {
        dataMembers.setArchiveWarningMessageRequired(value);
    }

    @Override
    public void loadMessagingEntryDefaultStyledDocument(final JTextPane messagingEntry) {
        if (messagingEntry == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            dataMembers.loadMessagingEntryDocument(messagingEntry);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    dataMembers.loadMessagingEntryDocument(messagingEntry);
                }
            });
        }
    }

    @Override
    public void loadMessagingHistoryDefaultStyledDocument(final JTextPane messagingHistory) {
        if (messagingHistory == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            dataMembers.loadMessagingHistoryDocument(messagingHistory);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    dataMembers.loadMessagingHistoryDocument(messagingHistory);
                }
            });
        }
    }
    
//    @Override
//    public void scrollToMessagingHistoryBottom(final JTextPane messagingHistory){
//        if (SwingUtilities.isEventDispatchThread()){
//            scrollToMessagingHistoryBottomHelper(messagingHistory);
//        }else{
//            SwingUtilities.invokeLater(new Runnable(){
//                @Override
//                public void run() {
//                    scrollToMessagingHistoryBottomHelper(messagingHistory);
//                }
//            });
//        }
//    }
    
//    private void scrollToMessagingHistoryBottomHelper(final JTextPane messagingHistory){
//        dataMembers.scrollToMessagingHistoryBottom(messagingHistory);
//    }

    @Override
    public Document getCopyOfMessagingHistoryDefaultStyledDocument(IMessageTabRecord record) {
        return dataMembers.getCopyOfMessagingHistoryDefaultStyledDocument(record);
    }
    
    @Override
    public void insertMessageLine(final IPbsysOptionQuote quote, final IMessageTabRecord messageTabRecord) throws BadLocationException{
        if (quote.getInstantMessage() == null){
            return;
        }
        dataMembers.insertMessageLine(quote, messageTabRecord);
    }

    @Override
    public void recordPublishedGroupMessage(IGatewayConnectorBuddy fromUser, IGatewayConnectorGroup targetGroup, String message) {
        if ((DataGlobal.isEmptyNullString(message)) || (fromUser == null) || (targetGroup == null)){
            return;
        }
        IPbsysInstantMessage quoteMessage = PbconsoleQuoteFactory.createPbsysInstantMessageInstance(GatewayServerType.PBIM_DISTRIBUTION_TYPE);
        quoteMessage.setMessageContent(message);
        quoteMessage.setIMServerType(GatewayServerType.PBIM_DISTRIBUTION_TYPE);
        quoteMessage.setFromUser(fromUser);
        quoteMessage.setMessageTimestamp(new GregorianCalendar());
        quoteMessage.setOutgoing(true);
        quoteMessage.setToUser(GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(fromUser, 
                                                                                        targetGroup.getGroupName(), 
                                                                                        null));
        IPbsysOptionQuote groupQuote = PbconsoleQuoteFactory.createPbsysOptionQuoteInstance(kernel.getDefaultPbcPricingModel());
        groupQuote.setPbsysInstantMessage(quoteMessage);
        dataMembers.recordPublishedQuote(groupQuote);
    }
         
}//MessagingBoardState
