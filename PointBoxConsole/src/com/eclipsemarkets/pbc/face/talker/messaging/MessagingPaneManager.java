/**
 * Eclipse Market Solutions LLC
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.IPointBoxDistributionGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.exceptions.OutOfEdtException;
import com.eclipsemarkets.pbc.face.talker.IBuddyListEventListener;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IMessagingBoardState;
import com.eclipsemarkets.pbc.face.talker.IMessagingPaneManager;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXPanel;

/**
 * MessagingPaneManager
 * <P>
 * A singleton manage messaging panel, which contains an instance of MasterMessagingBoard
 * <P>
 * @author Zhijun Zhang
 * Created on Apr 21, 2011 at 3:16:40 PM
 */
public class MessagingPaneManager implements IMessagingPaneManager, IBuddyListEventListener{
    
    private static final Logger logger;
    private static MessagingPaneManager self;
    static{
        logger = Logger.getLogger(MessagingPaneManager.class.getName());
        self = null;
    }
    
    /**
     * if talker is NULL, static self will be returned directly
     * @param talker
     * @return 
     */
    public static MessagingPaneManager getSingleton(IPbcTalker talker){
        if ((self == null) && (talker != null)){
            self = new MessagingPaneManager(talker);
        }
        return self;
    }
    
    public static MessagingPaneManager getSingleton() throws Exception{
        if (self == null){
            throw new Exception("MessagingPaneManager singleton was not created yet. Please use the other getSingleton method.");
        }
        return self;
    }
    
    private final IPbcTalker talker;
    private JXPanel basePanel;
    
    /**
     * Talker's buddy conversation main board
     */
    private MasterMessagingBoard masterMessagingBoard;
    
    private MessagingPaneManagerImpl aMessagingPaneManagerImpl;
    
    private MessagingPaneManager(IPbcTalker talker){
        this.talker = talker;
        
        masterMessagingBoard = new MasterMessagingBoard(talker);
        basePanel = new JXPanel(new BorderLayout());
        basePanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        basePanel.add(masterMessagingBoard, BorderLayout.CENTER);
        
        //aMessagingPaneManagerImpl = new MessagingPaneManagerImpl();
        aMessagingPaneManagerImpl = new SimpleMessagingPaneManagerImpl(talker);
        aMessagingPaneManagerImpl.registerMessagingBoard(masterMessagingBoard);
    }

    @Override
    public void updatePitsCastCheckTree() {
        if (aMessagingPaneManagerImpl != null){
            aMessagingPaneManagerImpl.updatePitsCastCheckTree();
        }
    }

    @Override
    public void selectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        if (aMessagingPaneManagerImpl != null){
            aMessagingPaneManagerImpl.selectBuddyCheckNode(buddy);
        }
    }

    @Override
    public void unselectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        if (aMessagingPaneManagerImpl != null){
            aMessagingPaneManagerImpl.unselectBuddyCheckNode(buddy);
        }
    }

    @Override
    public void notifyArchiveMethodChanged() {
        aMessagingPaneManagerImpl.notifyArchiveMethodChanged();
    }

    @Override
    public void closeGroupTabInFloatingDistributionFrame(IPointBoxDistributionGroup group) {
        aMessagingPaneManagerImpl.closeGroupTabInFloatingDistributionFrame(group);
    }

    @Override
    public void renamePitsCastGroupInMasterFloatingFrame(IGatewayConnectorGroup oldGroup, String newGroupName) {
        aMessagingPaneManagerImpl.renamePitsCastGroupInMasterFloatingFrame(oldGroup, newGroupName);
    }

    @Override
    public void renamePitsFloatingFrame(IGatewayConnectorGroup group, String newGroupName) {
        aMessagingPaneManagerImpl.renamePitsFloatingFrame(group, newGroupName);
    }

    @Override
    public boolean hidePitsFloatingFrame(IGatewayConnectorGroup group) {
        return aMessagingPaneManagerImpl.hidePersistentPitsFloatingFrames(group);
    }

    @Override
    public JFrame findPitsLikeGroupFloatingFrame(IGatewayConnectorGroup group) {
        return aMessagingPaneManagerImpl.findPitsLikeGroupFloatingFrame(group);
    }
    
    @Override
    public void displayPersistentFloatingFrames() {
        //aMessagingPaneManagerImpl.displayPersistentPitsFloatingFrames(talker, masterMessagingBoard);
        aMessagingPaneManagerImpl.displayPersistentPitsCastFloatingFrames(talker, masterMessagingBoard);
        aMessagingPaneManagerImpl.displayPersistentDistributionFloatingFrame(talker);
    }

    @Override
    public void removeFloatingMessagingFrame(String boardId) {
        aMessagingPaneManagerImpl.removeFloatingMessagingFrame(boardId);
    }

    @Override
    public void announceTabClosingEvent(String tabUniqueID) {
        aMessagingPaneManagerImpl.announceTabClosingEvent(tabUniqueID);
    }

    @Override
    public void storeOpenedFloatingFrames() {
        aMessagingPaneManagerImpl.storeOpenedFloatingFrames();
    }

//////    @Override
//////    public void setPersistencyRequired() {
//////        aMessagingPaneManagerImpl.setPersistencyRequired();
//////    }

    /**
     * This is called when talker is personalized
     */
    @Override
    public void personalizeComponent() {
        aMessagingPaneManagerImpl.personalizeStoredMessagingBoards();
    }
    
    @Override
    public void makeBuddyTabIconsOffline(String uniqueLoginName, GatewayServerType targetServerType){
        aMessagingPaneManagerImpl.makeBuddyTabIconsOffline(uniqueLoginName, targetServerType);
    }
    
    @Override
    public void updateBuddyStatus(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy,IPbcTalker talker){
        String tabUnqiueID = aMessagingPaneManagerImpl.updateBuddyStatus(loginUser, buddy,talker);
        if (DataGlobal.isNonEmptyNullString(tabUnqiueID)){
            masterMessagingBoard.updateBuddyStatus(tabUnqiueID, buddy);
        }
    }

    @Override
    public JPanel getBasePanel() {
        return basePanel;
    }

//    void hideButtonTabComponent(ButtonTabComponent tabComponent) {
//        if (tabComponent == null){
//            return;
//        }//getVisibleTabButton
//        synchronized(messagingBoardStorage){
//            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
//            boolean found = false;
//            for (IMasterMessagingBoard messagingBoard : messagingBoards){
//                if (messagingBoard.hasVisibleTabButton(tabComponent)){
//                    found = true;
//                    messagingBoard.hideButtonTabComponent(tabComponent);
//                }
//            }
//            if (!found){
//                masterMessagingBoard.hideButtonTabComponent(tabComponent);
//            }
//        }
//    }

//    void tabButtonClicked(ButtonTabComponent tabComponent) {
//        if (tabComponent == null){
//            return;
//        }//getVisibleTabButton
//        synchronized(messagingBoardStorage){
//            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
//            boolean found = false;
//            for (IMasterMessagingBoard messagingBoard : messagingBoards){
//                if (messagingBoard.hasVisibleTabButton(tabComponent)){
//                    found = true;
//                    messagingBoard.tabButtonClicked(tabComponent);
//                }
//            }
//            if (!found){
//                masterMessagingBoard.tabButtonClicked(tabComponent);
//            }
//        }
//    }
    
    @Override
    public void reorganizeMessagingBoardTabButtons(){
        aMessagingPaneManagerImpl.sortMessagingBoardTabButtons();
    }

    @Override
    public void populateUpdatedBuddyProfile(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy, IBuddyProfileRecord buddyProfile) {
        aMessagingPaneManagerImpl.updateBuddyProfile(loginUser, buddy, buddyProfile);
    }

    @Override
    public void gatewayConnectorBuddyHighlighted(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy) {
        aMessagingPaneManagerImpl.presentMessagingTab(loginUser, buddy);
        masterMessagingBoard.presentMessagingTab(loginUser, buddy);
    }

    @Override
    public void publishQuoteOnMessageTab(final IGatewayConnectorBuddy loginUser, 
                                         final IGatewayConnectorBuddy buddy,
                                         final IPbsysOptionQuote quote) 
    {
        if (loginUser == null){
            PointBoxTracer.recordSevereException(logger, new Exception("publishQuoteOnMessageTab::loginUser is NULL"));
            return;
        }
        if (quote == null){
            return;
        }
        IPbsysInstantMessage msg = quote.getInstantMessage();
        if (msg == null){
            return;
        }
        masterMessagingBoard.publishQuoteOnMessageTab(loginUser, buddy, quote);
    }

    @Override
    public void publishParsedMessage(ArrayList<IPbsysOptionQuote> parsedQuotes) {
        masterMessagingBoard.publishParsedMessage(parsedQuotes);
    }
    
    @Override
    public void publishPricedMessage(ArrayList<IPbsysOptionQuote> pricedQuotes) {
        masterMessagingBoard.publishPricedMessage(pricedQuotes);
    }

    @Override
    public void updateDistributionGroupMembers(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        aMessagingPaneManagerImpl.updateDistributionGroupMembers(group, members);
    }

    @Override
    public void buddyTreeNodeClickedEventHappened(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy) {
        gatewayConnectorBuddyHighlighted(loginUser, buddy);
        aMessagingPaneManagerImpl.handleBuddyTreeNodeClickedEvent(masterMessagingBoard, loginUser, buddy);
    }

    @Override
    public void groupTreeNodeClickedEventHappened(IGatewayConnectorBuddy loginUser, 
                                                  IGatewayConnectorGroup group, 
                                                  ArrayList<IGatewayConnectorBuddy> members) 
    {
        groupTreeNodeClickedEventHappened(loginUser, group, members, null, false);
    }

    @Override
    public void groupTreeNodeClickedEventHappened(IGatewayConnectorBuddy loginUser, 
                                                  IGatewayConnectorGroup group, 
                                                  ArrayList<IGatewayConnectorBuddy> members,
                                                  String message, boolean isAutoSend) 
    {
        aMessagingPaneManagerImpl.presentDistributionFloatingMessagingFrame(talker, loginUser, group, members, message, isAutoSend);
    }

    @Override
    public void pitsCastGroupTreeNodeClickedEventHappened(IGatewayConnectorBuddy loginUser, 
                                                  IGatewayConnectorGroup group, 
                                                  ArrayList<IGatewayConnectorBuddy> members,
                                                  String message, boolean isAutoSend) 
    {
        aMessagingPaneManagerImpl.presentPitsGroupFloatingMessagingFrame(masterMessagingBoard, loginUser, group, members, message, isAutoSend);
    }

    @Override
    public void pitsCastRootTreeNodeClickedEventHappened() {
        aMessagingPaneManagerImpl.presentPitsCastFloatingMessagingFrame("", false);
    }

    @Override
    public void pitsGroupTreeNodeClickedEventHappened(IGatewayConnectorBuddy loginUser, IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members, String message, boolean isAutoSend) {
        aMessagingPaneManagerImpl.presentPitsGroupFloatingMessagingFrame(masterMessagingBoard, loginUser, group, members, message, isAutoSend);
    }
    
    /**
     * If the state exists, it will be returned; if not, a new one created will be returned
     * @param loginUser
     * @param buddy
     * @return
     * @throws OutOfEdtException 
     */
    @Override
    public IMessagingBoardState aquireStateForBuddy(final IMasterMessagingBoard board,
                                                    final IGatewayConnectorBuddy loginUser, 
                                                    final IGatewayConnectorBuddy buddy) 
            throws OutOfEdtException
    {
        return aMessagingPaneManagerImpl.aquireStateForBuddy(board, loginUser, buddy);
    }

    /**
     * If the state exists, it will be returned; if not, a new one created will be returned
     * @param loginUser
     * @param group
     * @param members
     * @return 
     */
    @Override
    public IMessagingBoardState aquireStateForGroup(final IMasterMessagingBoard board,
                                                    final IGatewayConnectorBuddy loginUser, 
                                                    final IGatewayConnectorGroup group, 
                                                    final ArrayList<IGatewayConnectorBuddy> members) 
            throws OutOfEdtException
    {
        return aMessagingPaneManagerImpl.aquireStateForGroup(board, loginUser, group, members);
    }
    
    /**
     * Get a pre-built ButtonTabComponent instance whose id is tabUniqueID
     * @param tabButtonUniqueID
     * @return - null if there is no any pre-built button 
     */
    @Override
    public IButtonTabComponent getButtonTabComponent(IMasterMessagingBoard board, String tabButtonUniqueID) {
        return aMessagingPaneManagerImpl.getButtonTabComponent(board, tabButtonUniqueID);
    }

    List<IButtonTabComponent> getAllButtonTabComponentsWithUniqueID(String tabButtonUniqueID) {
        return aMessagingPaneManagerImpl.getAllButtonTabComponentsWithUniqueID(tabButtonUniqueID);
    }

    /**
     * According to parameters, get the stored state. If it is not, return NULL
     * @param loginUser
     * @param member
     * @return 
     */
    @Override
    public IMessagingBoardState getMessagingBoardState(IGatewayConnectorBuddy loginUser, 
                                                      IGatewayConnectorBuddy buddy) {
        final TargetBuddyPair pair = new TargetBuddyPair(loginUser, buddy);
        return getMessagingBoardState(pair.getUniqueID());
    }
    
    @Override
    public IMessagingBoardState getMessagingBoardState(String tabUniqueID) {
        return aMessagingPaneManagerImpl.getMessagingBoardState(tabUniqueID);
    }

    /**
     * Search similar ButtonTabComponent from the internal data structure, i.e. stateStorage
     * @param searchString
     * @param connectorLoginUser
     * @param sorted
     * @return 
     */
    @Override
    public ArrayList<IButtonTabComponent> searchButtonTabComponentsFromStateStorage(String searchString, 
                                                                                    IGatewayConnectorBuddy connectorLoginUser, 
                                                                                    boolean sorted) 
    {
        return aMessagingPaneManagerImpl.searchButtonTabComponentsFromStateStorage(masterMessagingBoard, searchString, connectorLoginUser, sorted);
    }

    /**
     * Get all the boards, which represents distGroups that contains the buddy represented by tabUniqueID. 
     * Notice tabUniqueID could represent a group.
     * @param tabUniqueID
     * @return 
     */
    ArrayList<IMasterMessagingBoard> acquireFloatingMessagingBoards(String tabUniqueID,boolean IsPresented,String frameName) {
        return aMessagingPaneManagerImpl.acquireFloatingMessagingBoards(masterMessagingBoard, tabUniqueID,IsPresented,frameName);
    }
    
    void popupMessagingTabFromMaster(String tabUniqueID, boolean isMove) {
        dropMessagingTab(masterMessagingBoard.getBoardId(), masterMessagingBoard.getBoardId(), tabUniqueID, isMove);
    }
    
    /**
     * (1) dragSourceId and dropTargetId can never be NULL. Otherwise, nothing is done.
     * (2) whether or not remove tabUniqueID's tab from dragSourceId is defined by "isMove"
     * @param dragSourceId
     * @param dropTargetId
     * @param tabUniqueID
     * @param isMove
     */
    @Override
    public void dropMessagingTab(String dragSourceId, String dropTargetId, String tabUniqueID, boolean isMove) {
        aMessagingPaneManagerImpl.dropMessagingTab(dragSourceId, dropTargetId, tabUniqueID, isMove);
    }

    /**
     * @return the masterMessagingBoard
     */
    @Override
    public MasterMessagingBoard getMasterMessagingBoard() {
        return masterMessagingBoard;
    }

    boolean isButtonTabComponentExisted(String tabUniqueID) {
        return aMessagingPaneManagerImpl.isButtonTabComponentExisted(tabUniqueID);
    }

    FloatingMessagingFrame retrieveFloatingMessagingBoards(String boardId) {
        return aMessagingPaneManagerImpl.retrieveFloatingMessagingFrame(boardId);
    }
    
    /**
     * This requires the corresponding floating frame existed.
     * @param message
     * @param pitsCastGroup
     * @param autoSend
     */
    public void copyToPitsCastMessageBoard(String message, IGatewayConnectorGroup pitsCastGroup, boolean autoSend) {
        aMessagingPaneManagerImpl.copyToPitsCastMessageBoard(message, pitsCastGroup, autoSend);
    }
    
    public void copyToPitsCastMessageFloatingFrame(String message, boolean autoSend) {
        aMessagingPaneManagerImpl.presentPitsCastFloatingMessagingFrame(message, autoSend);
        
    }
}//MessagingPaneManager
