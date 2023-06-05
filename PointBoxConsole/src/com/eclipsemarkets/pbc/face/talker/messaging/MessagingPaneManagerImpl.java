/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.IPointBoxDistributionGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.exceptions.OutOfEdtException;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IFloatingPitsCastGroupMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IFloatingPitsMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IMessagingBoardState;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.face.talker.dist.PbcFloatingFrameTerms;
import com.eclipsemarkets.pbc.kernel.DistributionFloatingFrameSettings;
import com.eclipsemarkets.pbc.kernel.GeneralFloatingFrameSettings;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang
 * @date & time: Aug 26, 2012 - 11:10:50 AM
 */
abstract class MessagingPaneManagerImpl {
    
    private static final Logger logger;
    static{
        logger = Logger.getLogger(MessagingPaneManagerImpl.class.getName());
    }
    
    /**
     * Cache all the live boards
     */
    final HashMap<String, IMasterMessagingBoard> messagingBoardStorage;
    
    /**
     * The data in this storage should not be removed because of consideration on efficiency in synchronization
     * key: tabUniqueID
     * value: state
     */
    private final HashMap<String, IMessagingBoardState> stateStorage;
    
    /**
     * Storage of all the buttonTabs
     * <boardId, <tabUniqueID, ButtonTabComponent>>
     */
    final HashMap<String, HashMap<String, IButtonTabComponent>> buttonTabStorage;
    
    /**
     * Storage of all the floating frames for PITS and DIST
     */
    private final HashMap<String, FloatingMessagingFrame> floatingFrameStorage = new HashMap<String, FloatingMessagingFrame>();

    /**
     * Talker
     */
    private IPbcTalker talker;
    MessagingPaneManagerImpl(IPbcTalker talker) {
        this.talker = talker;
        stateStorage = new HashMap<String, IMessagingBoardState>();
        buttonTabStorage = new HashMap<String, HashMap<String, IButtonTabComponent>>();
        messagingBoardStorage = new HashMap<String, IMasterMessagingBoard>();
    }
    
    boolean isButtonTabComponentExisted(String tabUniqueID){
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> frames = floatingFrameStorage.values();
            for (FloatingMessagingFrame frame : frames){
                ArrayList<IButtonTabComponent> tabs = frame.getFloatingMessagingBoard().getAllVisibleTabeButtons();
                for(IButtonTabComponent tab : tabs){
                    if(tab.getTabUniqueID().equals(tabUniqueID)){
                        return true;
                    }
                }
            }   
        }
        return false;
    }

    /**
     * Register a messaging board into internal storage
     * @param aMasterMessagingBoard 
     */
    void registerMessagingBoard(IMasterMessagingBoard aMasterMessagingBoard) {
        if (aMasterMessagingBoard == null){
            return;
        }
        synchronized(messagingBoardStorage){
            if (aMasterMessagingBoard instanceof IFloatingPitsMessagingBoard){
                messagingBoardStorage.put(((IFloatingPitsMessagingBoard)aMasterMessagingBoard).getPitsGroupName(), aMasterMessagingBoard);
            }else{
                messagingBoardStorage.put(aMasterMessagingBoard.getBoardId(), aMasterMessagingBoard);
            }
        }
    }
    
    IMasterMessagingBoard retrieveMessagingBoard(String boardKey){
        if (DataGlobal.isEmptyNullString(boardKey)){
            return null;
        }
        synchronized(messagingBoardStorage){
            return messagingBoardStorage.get(boardKey);
        }
    }

    void personalizeStoredMessagingBoards() {
        synchronized(messagingBoardStorage){
            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
            for (IMasterMessagingBoard messagingBoard : messagingBoards){
                messagingBoard.personalizeMessagingBoard();
            }
        }
    }

    void sortMessagingBoardTabButtons() {
        synchronized(messagingBoardStorage){
            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
            for (IMasterMessagingBoard messagingBoard : messagingBoards){
                messagingBoard.sortMessagingBoardTabButtonsInEDT(true);
            }
        }
    }

    void updateBuddyProfile(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy, IBuddyProfileRecord buddyProfile) {
        synchronized(messagingBoardStorage){
            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
            for (IMasterMessagingBoard messagingBoard : messagingBoards){
                if (messagingBoard.hasVisibleTabForBuddy(loginUser, buddy)){
                    messagingBoard.updateBuddyProfile(loginUser, buddy, buddyProfile);
                }
            }
        }
    }

    void presentMessagingTab(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy) {
        synchronized(messagingBoardStorage){
            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
            for (IMasterMessagingBoard messagingBoard : messagingBoards){
                if (messagingBoard.hasVisibleTabForBuddy(loginUser, buddy)){
                    messagingBoard.presentMessagingTab(loginUser, buddy);
                }
            }
        }
    }

    void handleBuddyTreeNodeClickedEvent(IMasterMessagingBoard masterMessagingBoard,
                                         IGatewayConnectorBuddy loginUser, 
                                         IGatewayConnectorBuddy buddy) {
//        synchronized(messagingBoardStorage){
//            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
//            for (IMasterMessagingBoard messagingBoard : messagingBoards){
//                if (messagingBoard.hasMembership(buddy)){
//                    dropMessagingTab(masterMessagingBoard.getBoardId(), 
//                                     messagingBoard.getBoardId(), 
//                                     TargetBuddyPair.generateTargetBuddyPairStorageID(loginUser, buddy), 
//                                     false);
//                }
//            }
//        }
    }
    
    void presentPitsGroupFloatingMessagingFrame(final IMasterMessagingBoard masterMessagingBoard, 
                                         IGatewayConnectorBuddy loginUser, 
                                         IGatewayConnectorGroup group, 
                                         ArrayList<IGatewayConnectorBuddy> members, 
                                         String message, 
                                         boolean isAutoSend) 
    {
        //we use groupDescription to save frameOwnerTabUniqueID which is very very important for looking up frames in PointBoxPropterties
        String pitsGroupFrameName = group.getGroupName();
        synchronized(floatingFrameStorage){
            if(floatingFrameStorage.get(pitsGroupFrameName)!=null){
                floatingFrameStorage.get(pitsGroupFrameName).setState(Frame.NORMAL);
                floatingFrameStorage.get(pitsGroupFrameName).toFront();
                return;
            }
        }

        if ((members != null) && (!members.isEmpty())){
            ////make sure its tab on the masterMessagingBoard 
            presentMessagingTab(members.get(0).getLoginOwner(), 
                                members.get(0));
            masterMessagingBoard.presentMessagingTab(members.get(0).getLoginOwner(), 
                                                     members.get(0), 
                                                     message);
            displayPitsGroupFloatingMessagingFrameHelper(masterMessagingBoard, loginUser, group, members);
        }
    }

    private void displayPitsGroupFloatingMessagingFrameHelper(IMasterMessagingBoard masterMessagingBoard, 
                                                              IGatewayConnectorBuddy loginUser,
                                                              IGatewayConnectorGroup group, 
                                                              ArrayList<IGatewayConnectorBuddy> members) 
    {
        if ((loginUser == null) || (group == null) || (members == null)){
            return;
        }
        String pitsFrameGroupName = group.getGroupName();
        //frameOwnerTabUniqueID = prop.parseFloatingBuddyFrameOwnerTabeUniqueID(frameRecord);
        //display frame by frame...
        //make sure its tab on the masterMessagingBoard which the following "acquireFloatingMessagingBoards" will validate
        String frameOwnerTabUniqueID = TargetBuddyPair.generateTargetBuddyPairStorageID(members.get(0).getLoginOwner(), members.get(0));
//        handleBuddyTreeNodeClickedEvent(masterMessagingBoard, 
//                                        loginUser,
//                                        members.get(0));
//        GatewayBuddyListFactory.
        //make sure board with frame are ready
        IMasterMessagingBoard aFrameMasterMessagingBoard = null;
        ArrayList<IMasterMessagingBoard> dragBoards = acquireFloatingMessagingBoards(masterMessagingBoard, 
                                                                                     frameOwnerTabUniqueID,
                                                                                     true,
                                                                                     pitsFrameGroupName);
        if ((dragBoards != null) && (!dragBoards.isEmpty())){
            for (IMasterMessagingBoard dragBoard : dragBoards){
                if (dragBoard instanceof FloatingPitsMessagingBoard){
                    MessagingPaneManager.getSingleton(null).dropMessagingTab(
                            masterMessagingBoard.getBoardId(), dragBoard.getBoardId(), frameOwnerTabUniqueID, true);
                    aFrameMasterMessagingBoard = dragBoard;
                    break;
                }
            }//for
            //drop all the tabs onto this frame...
            if (aFrameMasterMessagingBoard != null){
                String tabUniqueID;
                for (IGatewayConnectorBuddy member : members){
                    loginUser = member.getLoginOwner();
                    if (loginUser != null){
                        tabUniqueID = TargetBuddyPair.generateTargetBuddyPairStorageID(loginUser, member);
                        //make sure its tab on the masterMessagingBoard
                        presentMessagingTab(loginUser, member);
                        masterMessagingBoard.presentMessagingTab(loginUser, member);
                        handleBuddyTreeNodeClickedEvent(masterMessagingBoard, 
                                                       loginUser,
                                                       member);
                        MessagingPaneManager.getSingleton(null).dropMessagingTab(
                               masterMessagingBoard.getBoardId(), aFrameMasterMessagingBoard.getBoardId(), tabUniqueID, true);
                    }
                }//for
            }
        }
    }
    
    /**
     * 
     * @param message - if it is not empty or NULL, it will be copy-pasted onto "broadcast" data entry
     * @param autoSend - automatically send message or not
     */
    void presentPitsCastFloatingMessagingFrame(String message, boolean autoSend) 
    {
        synchronized(messagingBoardStorage){
            ArrayList<IGatewayConnectorGroup> pitsCastGroupList = talker.getPitsCastGroups();
            if ((pitsCastGroupList == null) || pitsCastGroupList.isEmpty()){
                return;
            }
            IGatewayConnectorBuddy loginUser = talker.getPointBoxLoginUser();
            IGatewayConnectorGroup distGroup = pitsCastGroupList.get(0);
            ArrayList<IGatewayConnectorBuddy> members = talker.getPitsCastBuddyListTreePanel().getBuddiesOfGroup(distGroup);
            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
            FloatingPitsCastGroupMessagingBoard aFloatingDistGroupMessagingBoard = null;
            //find out the existing board for distribution frame
            for (IMasterMessagingBoard messagingBoard : messagingBoards){
                if(messagingBoard instanceof FloatingPitsCastGroupMessagingBoard){
                    aFloatingDistGroupMessagingBoard = (FloatingPitsCastGroupMessagingBoard)messagingBoard;
                    break;
                }
            }
            if (aFloatingDistGroupMessagingBoard == null){
                //there is not distribution frame yet
                aFloatingDistGroupMessagingBoard = new FloatingPitsCastGroupMessagingBoard(talker, distGroup, talker.getBuddiesOfGroup(distGroup));
                
//                talker.getPitsCastBuddyListTreePanel().addPbcDndBuddyTreeListener(aFloatingDistGroupMessagingBoard);
                
                for (IGatewayConnectorGroup pitsCastGroup : pitsCastGroupList){
                    aFloatingDistGroupMessagingBoard.presentMessagingTab(loginUser, pitsCastGroup, members);
                }
                aFloatingDistGroupMessagingBoard.populatePitsCastCheckTreeModel(false);
                this.registerMessagingBoard(aFloatingDistGroupMessagingBoard);
                //diaply frame
                presentFloatingMessagingFrame(talker, aFloatingDistGroupMessagingBoard, PbcFloatingFrameTerms.PitsCastFrame.toString());
            }else{
                //there existed a distribution frame
                aFloatingDistGroupMessagingBoard.presentMessagingTab(loginUser, distGroup, members);
                FloatingMessagingFrame aFloatingMessagingFrame;
                synchronized(floatingFrameStorage){
                    aFloatingMessagingFrame = floatingFrameStorage.get(aFloatingDistGroupMessagingBoard.getBoardId());
                }
                if (aFloatingMessagingFrame != null){
                    //make sure the frame displayed
                    aFloatingMessagingFrame.setVisible(true);
                    aFloatingMessagingFrame.toFront();
                    aFloatingMessagingFrame.setState(Frame.NORMAL);
                }
            }
            if (DataGlobal.isNonEmptyNullString(message)){
                aFloatingDistGroupMessagingBoard.insertBroadcastMessage(message);
                if (autoSend){
                    aFloatingDistGroupMessagingBoard.broadcastCurrentMessage();
                }
            }
        }
    }
    
    /**
     * @param talker
     * @param loginUser
     * @param distGroup
     * @param members
     * @param message
     * @param isAutoSend - from viewer's shortcut menu, there are two shortcut menu items "Copy To" and "Send To". 
     * The first one's isAutoSend will be false; the second one will be true; 
     */
    void presentDistributionFloatingMessagingFrame(IPbcTalker talker,
                                         IGatewayConnectorBuddy loginUser, 
                                         IGatewayConnectorGroup distGroup, 
                                         ArrayList<IGatewayConnectorBuddy> members, 
                                         String message, boolean isAutoSend) 
    {
        synchronized(messagingBoardStorage){
            Collection<IMasterMessagingBoard> messagingBoards = messagingBoardStorage.values();
            FloatingDistGroupMessagingBoard aFloatingDistGroupMessagingBoard = null;
            //find out the existing board for distribution frame
            for (IMasterMessagingBoard messagingBoard : messagingBoards){
                if(messagingBoard instanceof FloatingDistGroupMessagingBoard){
                    aFloatingDistGroupMessagingBoard = (FloatingDistGroupMessagingBoard)messagingBoard;
                    break;
                }
            }
            if (aFloatingDistGroupMessagingBoard == null){
                //there is not distribution frame yet
                aFloatingDistGroupMessagingBoard = new FloatingDistGroupMessagingBoard(talker, distGroup, talker.getBuddiesOfGroup(distGroup));
                aFloatingDistGroupMessagingBoard.presentMessagingTab(loginUser, distGroup, members);
                this.registerMessagingBoard(aFloatingDistGroupMessagingBoard);
                //diaply frame
                presentFloatingMessagingFrame(talker, aFloatingDistGroupMessagingBoard, PbcFloatingFrameTerms.DistributionFrame.toString());
            }else{
                //there existed a distribution frame
                aFloatingDistGroupMessagingBoard.presentMessagingTab(loginUser, distGroup, members);
                FloatingMessagingFrame aFloatingMessagingFrame;
                synchronized(floatingFrameStorage){
                    aFloatingMessagingFrame = floatingFrameStorage.get(aFloatingDistGroupMessagingBoard.getBoardId());
                }
                if (aFloatingMessagingFrame != null){
                    //make sure the frame displayed
                    aFloatingMessagingFrame.setVisible(true);
                    aFloatingMessagingFrame.toFront();
                    aFloatingMessagingFrame.setState(Frame.NORMAL);
                }
            }
            if (DataGlobal.isNonEmptyNullString(message)){
                aFloatingDistGroupMessagingBoard.presentMessagingTab(loginUser, 
                                                                    distGroup, 
                                                                    members, message, isAutoSend);
            }
        }
    }
    
    /**
     * 
     * @param masterMessagingBoard
     * @param tabUniqueID
     * @param isPresented
     * @param floatingFrameName - id to detect the floating frame in the storage. For example, PITS floating frame will use 
     * corresponding PITS-group's name as floatingFrameName.
     * @return 
     */
    ArrayList<IMasterMessagingBoard> acquireFloatingMessagingBoards(IMasterMessagingBoard masterMessagingBoard, 
                                                                    String tabUniqueID,
                                                                    boolean isPresented,
                                                                    String floatingFrameName) {
        //IPbcTalker talker = masterMessagingBoard.getTalker();
        ArrayList<IMasterMessagingBoard> groupBoards = new ArrayList<IMasterMessagingBoard>();
        IButtonTabComponent buttonTab = buttonTabStorage.get(masterMessagingBoard.getBoardId()).get(tabUniqueID);
        if (buttonTab == null){
            logger.log(Level.SEVERE, null, new Exception("tabUniqueID - " + tabUniqueID + " should have been initiated already."));
            return groupBoards;
        }
        ArrayList<IGatewayConnectorGroup> distGroups;
        if (buttonTab instanceof BuddyButtonTabComponent){
            distGroups = talker.getAssociatedDistributionGroups(((BuddyButtonTabComponent)buttonTab).getBuddy());
        }else if (buttonTab instanceof GroupButtonTabComponent){
            distGroups = new ArrayList<IGatewayConnectorGroup>();
            distGroups.add(((GroupButtonTabComponent)buttonTab).getGroup());
        }else{
            return groupBoards;
        }
        synchronized(messagingBoardStorage){
            Set<String> messagingBoardIds = messagingBoardStorage.keySet();
            for (IGatewayConnectorGroup distGroup : distGroups){
                if (messagingBoardIds.contains(distGroup.getGroupName())){
                    groupBoards.add(retrieveMessagingBoard(distGroup.getGroupName()));
                    //diaply frame
                    if(isPresented){
                        presentFloatingMessagingFrame(talker, 
                                                      retrieveMessagingBoard(distGroup.getGroupName()),
                                                      floatingFrameName);
                    }                    
                }else{
                    //creat a fresh new board and present its frame
                    FloatingDistGroupMessagingBoard board = new FloatingDistGroupMessagingBoard(talker, distGroup, talker.getBuddiesOfGroup(distGroup));
                    this.registerMessagingBoard(board);
                    groupBoards.add(board);
                    //diaply frame
                    if(isPresented){
                        presentFloatingMessagingFrame(talker, 
                                                      board,
                                                      floatingFrameName);
                    }
                }
            }//for
        }
        return groupBoards;
    }

    void presentFloatingMessagingFrame(final IPbcTalker talker, final IMasterMessagingBoard board,final String frameName) {
        if (SwingUtilities.isEventDispatchThread()){
            presentFloatingMessagingFrameHelper(talker, board,frameName);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    presentFloatingMessagingFrameHelper(talker, board,frameName);
                }
            });
        }
    }
    
    private void presentFloatingMessagingFrameHelper(final IPbcTalker talker, 
                                                     final IMasterMessagingBoard board, 
                                                     final String frameName) {
        FloatingMessagingFrame f;
        synchronized(floatingFrameStorage){
            f = floatingFrameStorage.get(board.getBoardId());
            if(f != null){
                f.setVisible(true);
                f.toFront();
                return;
            }
            if (!(board instanceof FloatingMessagingBoard)){
                return;
            }
            final FloatingMessagingFrame frame = new FloatingMessagingFrame((FloatingMessagingBoard)board, this, frameName);
            frame.setIconImage(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon().getImage());
            frame.addWindowListener(new WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent e) {
                    synchronized(messagingBoardStorage){
                        //only keep PITS floating boards the memory. Give up PitsCast and Dist boards
                        if (frame.getFloatingMessagingBoard() instanceof FloatingPitsMessagingBoard) {
                            talker.removeTalkerComponents(frame);
                            //todo: how to decide this action?
                            messagingBoardStorage.remove(frame.getFloatingMessagingBoard().getBoardId());
                            HashMap<String, IButtonTabComponent> tabButtons = buttonTabStorage.get(frame.getFloatingMessagingBoard().getBoardId());
                            if(tabButtons!=null){
                                for(Map.Entry<String,IButtonTabComponent> entry:tabButtons.entrySet()){
                                     IButtonTabComponent tab=entry.getValue();
                                     messagingBoardStorage.remove(tab.getTabUniqueID());//remove the tabs who are embeded in this board
                                }
                            }
                        }//if
                    }
                    //Only remove PITS floating frame if it is closed by users. Keep PitsCast and Dist boards
                    synchronized(floatingFrameStorage){
                        if (board instanceof FloatingPitsMessagingBoard){
                            floatingFrameStorage.remove(board.getBoardId());
                        }
                    }
                }
            });
            talker.addTalkerComponents(frame);
            floatingFrameStorage.put(board.getBoardId(), frame);
            
            frame.setVisible(true);
        }//synchronized(floatingFrameStorage){
    }

    void removeFloatingMessagingFrame(final String frameKey) {
        synchronized(floatingFrameStorage){
            floatingFrameStorage.remove(frameKey);
        }
    }
    
    public static boolean isGroupTabID(String tabUniqueID){
         //"TargetBuddyPair.parseLoginUserForBuddyOfTab(tabUniqueID)==null" mean it's a groupTab ID
        if(TargetBuddyPair.parseLoginUserForBuddyOfTab(tabUniqueID)==null&&!tabUniqueID.contains("-")){
            return true;
        }
        return false;
    }
    
    public static boolean isBuddyTabID(String tabUniqueID){
        if(TargetBuddyPair.parseLoginUserForBuddyOfTab(tabUniqueID)!=null){
            return true;
        }
        return false;    
    }   
      
    void dropMessagingTab(String dragSourceId, String dropTargetId, String tabUniqueID, boolean isMove) {
        dropMessagingTab(dragSourceId, dropTargetId, tabUniqueID, isMove, null, false);
    }
      
    void dropMessagingTab(String dragSourceId, String dropTargetId, String tabUniqueID, boolean isMove, String message, boolean isAutoSend) {
        if ((DataGlobal.isEmptyNullString(dragSourceId))
                || (DataGlobal.isEmptyNullString(dropTargetId))
                || (DataGlobal.isEmptyNullString(tabUniqueID)))
        {
            logger.log(Level.INFO, null, new Exception("Something is NULL"));
            return;
        }
        IMasterMessagingBoard dragSource = retrieveMessagingBoard(dragSourceId);
        if (dragSource == null){
            logger.log(Level.INFO, null, new Exception("dragSource is NULL"));
            return;
        }
        IMasterMessagingBoard dropTarget = retrieveMessagingBoard(dropTargetId);
        if (dropTarget == null){
            logger.log(Level.INFO, null, new Exception("dropTarget is NULL"));
            return;
        }     
        IButtonTabComponent tabButtonObj = getButtonTabComponent(dragSource, tabUniqueID);
        if (tabButtonObj == null){
            logger.log(Level.INFO, null, new Exception("tabButtonObj is NULL"));
            return;
        }
        
        if(isGroupTabID(tabUniqueID)){
            dragSource.hideButtonTabComponent(tabUniqueID);
        }
        tabButtonObj = getButtonTabComponent(dropTarget, tabUniqueID);
        if (tabButtonObj == null){
            tabButtonObj = getButtonTabComponent(dragSource, tabUniqueID);
            if (tabButtonObj!= null){
                HashMap<String, IButtonTabComponent> tabButtons = this.buttonTabStorage.get(dropTarget.getBoardId());
                if (tabButtons == null){
                    tabButtons = new HashMap<String, IButtonTabComponent>();
                    buttonTabStorage.put(dropTarget.getBoardId(), tabButtons);
                }
                tabButtonObj = tabButtonObj.cloneTabButton(dropTarget);
                tabButtons.put(tabUniqueID, tabButtonObj);
            }
        }else{
            tabButtonObj.setBoard(dropTarget);
        }
        if (tabButtonObj == null){
            logger.log(Level.INFO, null, new Exception("tabButtonObj eventually is NULL"));
        }else{
            presentMessagingTab(dropTargetId, tabButtonObj, message, isAutoSend);
        }
    }
    
    /**
     * Show a specific tab content in a floating board 
     * @param boardId
     * @param tabButtonObj 
     */
    private void presentMessagingTab(final String boardId, 
                                     final IButtonTabComponent tabButtonObj, 
                                     final String message, final boolean isAutoSend) {
        //logger.log(Level.INFO, ">>> boardId - " + boardId);
        if (SwingUtilities.isEventDispatchThread()){
            presentMessagingTabHelper(boardId, tabButtonObj, message, isAutoSend);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    presentMessagingTabHelper(boardId, tabButtonObj, message, isAutoSend);
                }
            });
        }
    }
    
    private void presentMessagingTabHelper(final String boardId, 
                                           final IButtonTabComponent tabButtonObj,
                                           final String message, final boolean isAutoSend) 
    {
        if (tabButtonObj != null){
            IMasterMessagingBoard board = retrieveMessagingBoard(boardId);
            if (board == null){
                logger.log(Level.SEVERE, null, new Exception("This is impossible. MessagingPaneManager::getFloatingMessagingBoards() should have initiated it already."));
            }
            //make sure tabButton on the board of the frame
            if (tabButtonObj instanceof BuddyButtonTabComponent){
                BuddyButtonTabComponent tabButton = (BuddyButtonTabComponent)tabButtonObj;
                //logger.log(Level.INFO, ">>> tabButton = " + tabButton.getBuddy().getIMUniqueName());
                board.presentMessagingTab(tabButton.getLoginUser(), tabButton.getBuddy(), message);
            }else if (tabButtonObj instanceof GroupButtonTabComponent){
                GroupButtonTabComponent tabButton = (GroupButtonTabComponent)tabButtonObj;
                board.presentMessagingTab(tabButton.getLoginUser(), 
                                          tabButton.getGroup(), 
                                          tabButton.getMembers(), message, isAutoSend);
            }
        }
    }

    List<IButtonTabComponent> getAllButtonTabComponentsWithUniqueID(String tabButtonUniqueID) {
        List<IButtonTabComponent> result = new ArrayList<IButtonTabComponent>();
        Collection<HashMap<String, IButtonTabComponent>> buttonPools = buttonTabStorage.values();
        IButtonTabComponent buttonTab;
        for (HashMap<String, IButtonTabComponent> buttonPool : buttonPools){
            buttonTab = buttonPool.get(tabButtonUniqueID);
            if (buttonTab != null){
                result.add(buttonTab);
            }
        }
        return result;
    }

    IButtonTabComponent getButtonTabComponent(IMasterMessagingBoard board, String tabUniqueID) {
        if (board == null){
            return null;
        }
        HashMap<String, IButtonTabComponent> tabButtons = buttonTabStorage.get(board.getBoardId());
        if (tabButtons == null){
            return null;
        }else{
            return tabButtons.get(tabUniqueID);
        }
    }

    ArrayList<IButtonTabComponent> searchButtonTabComponentsFromStateStorage(MasterMessagingBoard masterMessagingBoard, 
                                                                             String searchString, 
                                                                             IGatewayConnectorBuddy connectorLoginUser, 
                                                                             boolean sorted) 
    {   
        ArrayList<IButtonTabComponent> result = new ArrayList<IButtonTabComponent>();
        synchronized (stateStorage){
            Set<String> keys = stateStorage.keySet();
            Iterator<String> itr = keys.iterator();
            String tabID;
            IButtonTabComponent tabButton;
            searchString = searchString.toLowerCase();
            while(itr.hasNext()){
                tabID = itr.next();
                if (tabID.contains(searchString)){
                    tabButton = getButtonTabComponent(masterMessagingBoard, tabID);
                    if (tabButton != null){
                        result.add(tabButton);
                    }
                }
            }//while
        }//synchronized
        if (sorted){
            Collections.sort(result, new ButtonTabComponentComparator(true));
        }
        return result;
    }
    
    private String getPricerBuddyDescriptiveName(TargetBuddyPair pair){
        if (pair == null){
            return PbcReservedTerms.UNKNOWN.toString();
        }
        IGatewayConnectorBuddy loginUser = pair.getLoginUser();
        IGatewayConnectorBuddy buddy = pair.getBuddy();
        if ((loginUser != null)
                && (GatewayServerType.PBIM_SERVER_TYPE.equals(loginUser.getIMServerType()))
                && (loginUser.getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())))
        {
            return PbcReservedTerms.PricerBuddy.toString();
        } else {
            if (PbcReservedTerms.PricerBuddy.toString().equalsIgnoreCase(buddy.getNickname())){
                buddy.setNickname(buddy.getIMScreenName());
            }
            return pair.getBuddy().getNickname();
        }
    }

    IMessagingBoardState getMessagingBoardState(String tabUniqueID) {
        return stateStorage.get(tabUniqueID);
    }
    
    IMessagingBoardState aquireStateForBuddy(final IMasterMessagingBoard board,
                                             final IGatewayConnectorBuddy loginUser, 
                                             final IGatewayConnectorBuddy buddy) 
            throws OutOfEdtException
    {
        if ((loginUser == null) || (buddy == null)){
            throw new RuntimeException("MasterMessagingBoardStateManager::aquireStateForBuddy = Bad parameters for aquireStateForBuddy");
        }
        IMessagingBoardState stateForBuddy = null;
        if (board instanceof MasterMessagingBoard){
            //IPbcTalker talker = board.getTalker();
            final TargetBuddyPair pair = new TargetBuddyPair(loginUser, buddy);
            synchronized (stateStorage){
                stateForBuddy = stateStorage.get(pair.getUniqueID());
                if (stateForBuddy == null){
                //if (stateForBuddy == null &&(!talker.getKernel().getPointBoxLoginUser().getIMUniqueName().equals(buddy.getIMUniqueName()))){
                   //based on the status of login user, we give approriate image icon on message tabs.
                    ImageIcon icon;
                    if (loginUser.getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())){//PBIM self-buddy: Pricer
                        //colorful icons (online)
                        icon=talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorBuddyIcon(pair.getBuddy().getIMServerType());
                    }else{
                        if(BuddyStatus.Online.equals(loginUser.getBuddyStatus())&&BuddyStatus.Online.equals(buddy.getBuddyStatus())){
                            //colorful icons (online)
                            icon=talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorBuddyIcon(pair.getBuddy().getIMServerType());
                        }else{
                            //black icons (offline)
                            icon=talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorLogo21(pair.getBuddy().getIMServerType());
                        }
                    }
                    IMasterMessagingBoard masterboard = MessagingPaneManager.getSingleton(talker).getMasterMessagingBoard();
                    ButtonTabComponent tabButton = new BuddyButtonTabComponent(board, 
                                                                               getPricerBuddyDescriptiveName(pair), 
                                                                               pair,
                                                                               icon, 
                                                                               masterboard);
                    HashMap<String, IButtonTabComponent> tabButtons = buttonTabStorage.get(board.getBoardId());
                    if (tabButtons == null){
                        tabButtons = new HashMap<String, IButtonTabComponent>();
                        buttonTabStorage.put(board.getBoardId(), tabButtons);
                    }
                    tabButtons.put(tabButton.getTabUniqueID(), tabButton);
                    stateForBuddy = new MessagingBoardState(talker.getKernel(), pair, tabButton.getTabUniqueID());
                    stateStorage.put(stateForBuddy.getTabButtonUniqueID(), stateForBuddy);
                }
            }//synchronized
        }else{
            logger.log(Level.SEVERE, null, new Exception("The parameter board is not MasterMessagingBoard"));
        }
        return stateForBuddy;
    }

    IMessagingBoardState aquireStateForGroup(final IMasterMessagingBoard board,
                                            final IGatewayConnectorBuddy loginUser, 
                                            final IGatewayConnectorGroup group, 
                                            final ArrayList<IGatewayConnectorBuddy> members) 
            throws OutOfEdtException
    {
        if (!SwingUtilities.isEventDispatchThread()){
            throw new OutOfEdtException();
        }
        if (group == null){
            throw new RuntimeException("Bad parameters for aquireStateForGroup");
        }
        
        IMessagingBoardState stateForGroup = null;
        
        if (board instanceof MasterMessagingBoard){
            synchronized (stateStorage){
                stateForGroup = stateStorage.get(group.getIMUniqueName());
                if (stateForGroup == null){
                    ButtonTabComponent groupTabButton = new GroupButtonTabComponent(board, loginUser, group, members,
                                                                          board.getTalker().getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getOpenedGroupIcon());
//                    logger.log(Level.INFO, null, new Exception("GroupButtonTabComponent::BoardID - " + board.getBoardId() 
//                            + " = tabButton.getTabUniqueID() -> " + tabButton.getTabUniqueID() 
//                            + "tabButton.getBoard().getBoardId() -> " + tabButton.getBoard().getBoardId()));
                    if (board instanceof FloatingPitsCastGroupMessagingBoard){
                        groupTabButton.makeClosable(false);
                    }
                    
                    HashMap<String, IButtonTabComponent> tabButtons = buttonTabStorage.get(board.getBoardId());
                    if (tabButtons == null){
                        tabButtons = new HashMap<String, IButtonTabComponent>();
                        buttonTabStorage.put(board.getBoardId(), tabButtons);
                    }
                    tabButtons.put(groupTabButton.getTabUniqueID(), groupTabButton);
    //                if (largestTabButtonWidth < tabButton.getPreferredSize().width){
    //                    largestTabButtonWidth = tabButton.getPreferredSize().width;
    //                }
                    stateForGroup = new MessagingBoardState(talker.getKernel(), group, groupTabButton.getTabUniqueID());
                    stateForGroup.setGroupMembers(members);
                    stateStorage.put(groupTabButton.getTabUniqueID(), stateForGroup);
                }
            }//synchronized
        }else{
            logger.log(Level.SEVERE, null, new Exception("The parameter board is not MasterMessagingBoard"));
        }
        return stateForGroup;
    }

    void closeGroupTabInFloatingDistributionFrame(IPointBoxDistributionGroup group) {
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> floatingFrames = floatingFrameStorage.values();
            for (FloatingMessagingFrame floatingFrame : floatingFrames){
                if (PbcFloatingFrameTerms.DistributionFrame.toString().equalsIgnoreCase(floatingFrame.getTitle())){
                    floatingFrame.getFloatingMessagingBoard().hideButtonTabComponent(group.getIMUniqueName());
                    ArrayList<IButtonTabComponent> visibleTabs = floatingFrame.getFloatingMessagingBoard().getAllVisibleTabeButtons();
                    if ((visibleTabs == null) || (visibleTabs.isEmpty())){
                        final FloatingMessagingFrame distFloatingFrame = floatingFrame;
                        if (SwingUtilities.isEventDispatchThread()){
                            distFloatingFrame.setVisible(false);
                        }else{
                            SwingUtilities.invokeLater(new Runnable(){
                                @Override
                                public void run() {
                                    distFloatingFrame.setVisible(false);
                                }
                            });
                        }
                    }
                    break;
                }
            }
        }
    }

    void renamePitsCastGroupInMasterFloatingFrame(IGatewayConnectorGroup oldGroup, String newGroupName) {
        if ((oldGroup == null) || (DataGlobal.isEmptyNullString(newGroupName))){
            return;
        }
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> floatingFrames = floatingFrameStorage.values();
            for (FloatingMessagingFrame floatingFrame : floatingFrames){
                if (PbcFloatingFrameTerms.PitsCastFrame.toString().equalsIgnoreCase(floatingFrame.getTitle())){
                    floatingFrame.renameGroupInFloatingFrame(oldGroup, newGroupName);
                    break;
                }
            }
        }
    }

    void renamePitsFloatingFrame(IGatewayConnectorGroup group, String newGroupName) {
        if ((group == null) || (DataGlobal.isEmptyNullString(newGroupName))){
            return;
        }
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> floatingFrames = floatingFrameStorage.values();
            for (FloatingMessagingFrame floatingFrame : floatingFrames){
                if (group.getGroupName().equalsIgnoreCase(floatingFrame.getTitle())){
                    floatingFrame.setTitle(newGroupName);
                    break;
                }
            }
        }
    }
    
    JFrame findPitsLikeGroupFloatingFrame(IGatewayConnectorGroup group) {
        if (group == null){
            return null;
        }
        JFrame result = null;
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> floatingFrames = floatingFrameStorage.values();
            for (FloatingMessagingFrame floatingFrame : floatingFrames){
                if (group.getGroupName().equalsIgnoreCase(floatingFrame.getTitle())){
                    result = floatingFrame;
                    break;
                }
            }//for
        }
        return result;
    }

    /**
     * 
     * @param group
     * @return - whether or not group's floating frame was visible
     */
    boolean hidePersistentPitsFloatingFrames(IGatewayConnectorGroup group) {
        if (group == null){
            return false;
        }
        boolean result = false;
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> floatingFrames = floatingFrameStorage.values();
            for (FloatingMessagingFrame floatingFrame : floatingFrames){
                if (group.getGroupName().equalsIgnoreCase(floatingFrame.getTitle())){
                    result = floatingFrame.isVisible();
                    floatingFrame.setVisible(false);
                    break;
                }
            }//for
        }
        return result;
    }

    /**
     * @deprecated 
     * @param talker
     * @param masterMessagingBoard 
     */
    void displayPersistentPitsFloatingFrames(IPbcTalker talker, MasterMessagingBoard masterMessagingBoard) {
        IGatewayConnectorBuddy pbcLoginUser = talker.getPointBoxLoginUser();
        ArrayList<IGatewayConnectorGroup> pitsGroupList = talker.getPitsGroups();
        ArrayList<String> pitsGroupNameList = new ArrayList<String>();
        for (IGatewayConnectorGroup aGroup : pitsGroupList){
            pitsGroupNameList.add(aGroup.getGroupName());
        }
        //get corresponding frame settings
        PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
        HashMap<String, GeneralFloatingFrameSettings> settingsMap = prop.retrieveOpenedPitsFloatingFrameSettingsList(pbcLoginUser.getIMUniqueName(), 
                                                                                                                     pitsGroupNameList);
        //display persistent frames
        GeneralFloatingFrameSettings frameSettings;
        for (IGatewayConnectorGroup aGroup : pitsGroupList){
            frameSettings = settingsMap.get(aGroup.getGroupName());
            if ((frameSettings != null) && (frameSettings.isVisible())){
                this.presentPitsGroupFloatingMessagingFrame(masterMessagingBoard, 
                                                            pbcLoginUser,
                                                            aGroup,
                                                            talker.getBuddiesOfPitsGroup(aGroup), 
                                                            "", 
                                                            false);
            }
        }
    }

    /**
     * 
     * @param talker
     * @param masterMessagingBoard 
     */
    void displayPersistentPitsCastFloatingFrames(IPbcTalker talker, MasterMessagingBoard masterMessagingBoard) {
        IGatewayConnectorBuddy pbcLoginUser = talker.getPointBoxLoginUser();
        PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
        //(1)get PitsCast group-name list...
        ArrayList<IGatewayConnectorGroup> pitsGroupList = talker.getPitsCastGroups();
        ArrayList<String> pitsGroupNameList = new ArrayList<String>();
        for (IGatewayConnectorGroup aGroup : pitsGroupList){
            pitsGroupNameList.add(aGroup.getGroupName());
        }
        //(2)get persistent frame settings
        HashMap<String, GeneralFloatingFrameSettings> settingsMap = prop.retrieveOpenedPitsFloatingFrameSettingsList(pbcLoginUser.getIMUniqueName(), 
                                                                                                                     pitsGroupNameList);
        //(3)display persistent frames
        GeneralFloatingFrameSettings frameSettings;
        for (IGatewayConnectorGroup aGroup : pitsGroupList){
            frameSettings = settingsMap.get(aGroup.getGroupName());
            if ((frameSettings != null) && (frameSettings.isVisible())){
                this.presentPitsGroupFloatingMessagingFrame(masterMessagingBoard, 
                                                            pbcLoginUser,
                                                            aGroup,
                                                            talker.getBuddiesOfPitsCastGroup(aGroup), 
                                                            "", 
                                                            false);
            }
        }
        
        frameSettings = prop.retrieveOpenedPitsCastFloatingFrameSettings(pbcLoginUser.getIMUniqueName());
        if ((frameSettings != null) && (frameSettings.isVisible())){
            this.presentPitsCastFloatingMessagingFrame("", false);
        }
    }
    
    void displayPersistentDistributionFloatingFrame(IPbcTalker talker) {
        PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
        IGatewayConnectorBuddy pbcLoginUser = talker.getPointBoxLoginUser();
        //check if the distribution buddy list panel is represented or not
        if (!(prop.isDisplayDistributionBuddyListPanelRequired(pbcLoginUser))){
            return;
        }
        DistributionFloatingFrameSettings frameSettings = prop.retrieveOpenedDistributionFloatingFrameSettings(pbcLoginUser.getIMUniqueName());
        if ((frameSettings != null) && (frameSettings.isVisible())
                && (frameSettings.getDistGroupNameList() != null)
                && (!frameSettings.getDistGroupNameList().isEmpty()))
        {
            IGatewayConnectorGroup distGroup;
            ArrayList<IGatewayConnectorBuddy> members;
            List<String> distGroupNameList = frameSettings.getDistGroupNameList();
            for (String distGroupName : distGroupNameList){
                distGroup = GatewayBuddyListFactory.getDistributionGroupInstance(pbcLoginUser, distGroupName);
                if (distGroup != null){
                    members = talker.getBuddiesOfDistGroup(distGroup);
                    presentDistributionFloatingMessagingFrame(talker,
                                                     pbcLoginUser, 
                                                     distGroup, 
                                                     members, 
                                                     "", false);
                }
            }//for
        }
    }
    
    /**
     * Update buddy status of relevant items
     * @param loginUser
     * @param buddy
     * @param talker
     * @return tabUnqiueID for finding out the buddy message tab on master messaging board. It could be NULL
     */
    public String updateBuddyStatus(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy, IPbcTalker talker){
        String tabUnqiueID = null;
        //update tab buttons
        for(Map.Entry<String, HashMap<String, IButtonTabComponent>> entry : buttonTabStorage.entrySet()){
            for(Map.Entry<String, IButtonTabComponent> subEntry:entry.getValue().entrySet())
            {
                IButtonTabComponent tab = subEntry.getValue();
                if(tab instanceof BuddyButtonTabComponent){
                   BuddyButtonTabComponent buddyTab = (BuddyButtonTabComponent)tab;
                   if(buddyTab.getLoginUser().getIMUniqueName().equals(loginUser.getIMUniqueName())&&buddyTab.getBuddy().getIMUniqueName().equals(buddy.getIMUniqueName())){
                       if(BuddyStatus.Online.equals(buddy.getBuddyStatus())){
                           buddyTab.setIcon(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorBuddyIcon(buddy.getIMServerType()));
                       }else{
                           buddyTab.setIcon(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorLogo21(buddy.getIMServerType()));
                       }
                       buddyTab.updateBuddyTabName(buddy.getNickname());
                       tabUnqiueID = buddyTab.getTabUniqueID();
                   }
                }
            }           
        }
        //update relevant buddy items on the floating frames
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> aFloatingMessagingFrameCollection = floatingFrameStorage.values();
            if (aFloatingMessagingFrameCollection != null){
                for (FloatingMessagingFrame aFloatingMessagingFrame : aFloatingMessagingFrameCollection){
                    aFloatingMessagingFrame.updateBuddyStatus(loginUser, buddy, talker);
                }
            }
        }
        return tabUnqiueID;
    }

    /**
     * This method ONLY update members of a group who stays on the "Distribution Frame"
     * @param group
     * @param members 
     */
    void updateDistributionGroupMembers(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        //update relevant floating frame who contains "group"
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> aFloatingMessagingFrameCollection = floatingFrameStorage.values();
            if (aFloatingMessagingFrameCollection != null){
                for (FloatingMessagingFrame aFloatingMessagingFrame : aFloatingMessagingFrameCollection){
                    aFloatingMessagingFrame.updateDistributionGroupMembers(group, members);
                }
            }
        }
    }
    
    public void makeBuddyTabIconsOffline(String uniqueLoginName, 
                                GatewayServerType targetServerType){
        for(Map.Entry<String, HashMap<String, IButtonTabComponent>> entry:buttonTabStorage.entrySet()){
            for(Map.Entry<String, IButtonTabComponent> subEntry:entry.getValue().entrySet()){
                IButtonTabComponent tab = subEntry.getValue();
                 if(tab instanceof BuddyButtonTabComponent){
                        BuddyButtonTabComponent buddyTab=(BuddyButtonTabComponent)tab;
                        if(buddyTab.getLoginUser().getIMUniqueName().equals(uniqueLoginName)){
                            buddyTab.setIcon(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorLogo21(targetServerType));
                        }
                 }
                if(tab instanceof GroupButtonTabComponent){
                    //do nothing. Groups are dist groups, which are always online.
                    //But if this tab is the owner for the distribution group frame, we must update the groupMemberList on it.
                    IMasterMessagingBoard board;
                    String groupTabID = tab.getTabUniqueID();
                    //tabId is like this "groupname", boardId is like this "[Distribution Group] groupname"
                    String groupBoardID = groupTabID.replace(ButtonTabComponent.DistributionGroup, "").trim(); 
                    if((board = retrieveMessagingBoard(groupBoardID))!=null){
                        board.updateMemberList();
                    }
                }

            }
        }
        
    }

    /**
     * This method is called during PBC unloading stage.
     */
    void storeOpenedFloatingFrames() {
        PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
        FloatingMessagingBoard board;
        synchronized(floatingFrameStorage){
            Collection<FloatingMessagingFrame> frames = floatingFrameStorage.values();
            for (FloatingMessagingFrame frame : frames){
                if (frame.isVisible()){
                    board = frame.getFloatingMessagingBoard();
                    if ((board instanceof FloatingPitsMessagingBoard) && (frame.isVisible())){
                        //save the frame with ownerTabUniqueID
                        prop.storeOpenedPitsFloatingFrame(board.getTalker().getPointBoxLoginUser().getIMUniqueName(),
                                                        frame.getTitle(),
                                                        frame.getLocation(), 
                                                        frame.getSize(),
                                                        frame.isVisible());
                    }else if ((board instanceof FloatingDistGroupMessagingBoard) && (frame.isVisible())){
                        //save the frame with ownerTabUniqueID
                        prop.storeOpenedDistributionFloatingFrame(board.getTalker().getPointBoxLoginUser().getIMUniqueName(),
                                                                board.getAllVisibleGroupNameList(),
                                                                frame.getLocation(), 
                                                                frame.getSize(), true);
                    }else if ((board instanceof FloatingPitsCastGroupMessagingBoard) && (frame.isVisible())){
                        //save the frame with ownerTabUniqueID
                        prop.storeOpenedPitsCastFloatingFrame(board.getTalker().getPointBoxLoginUser().getIMUniqueName(),
                                                            board.getAllVisibleGroupNameList(),
                                                            frame.getLocation(), 
                                                            frame.getSize(), true);
                    }
                }//if
            }//for
        }
    }

    void announceTabClosingEvent(String tabUniqueID) {
        synchronized(messagingBoardStorage){
            Collection<IMasterMessagingBoard> boards = messagingBoardStorage.values();
            try {
                for (IMasterMessagingBoard board : boards){
                    if (board instanceof FloatingMessagingBoard){
                        if (board.hasVisibleTabButton(tabUniqueID)){
                            board.hideButtonTabComponent(tabUniqueID);
                            if (!board.hasVisibleTabButtons()){
                                removeFloatingMessagingFrame(board.getBoardId());
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                PointBoxTracer.recordSevereException(logger, ex);
            }
        }
    }

    FloatingMessagingFrame retrieveFloatingMessagingFrame(String aFloatingMessagingFrameStorageKey) {
        synchronized(floatingFrameStorage){
            return floatingFrameStorage.get(aFloatingMessagingFrameStorageKey);
        }
    }

    void notifyArchiveMethodChanged() {
        synchronized (stateStorage){
            Set<String> keys = stateStorage.keySet();
            Iterator<String> itr = keys.iterator();
            IMessagingBoardState aMessagingBoardState;
            boolean displayWarningMessage = PointBoxConsoleProperties.getSingleton().isDisclaimerMessageDisplayed(talker.getPointBoxLoginUser());
            while(itr.hasNext()){
                aMessagingBoardState = stateStorage.get(itr.next());
                if (aMessagingBoardState != null){
                    
                    aMessagingBoardState.setArchiveWarningMessageRequired(displayWarningMessage);
                }
            }//while
        }//synchronized
    }

    /**
     * This requires the corresponding floating frame existed.
     * @param message
     * @param pitsCastGroup 
     * @param autoSend
     */
    void copyToPitsCastMessageBoard(String message, IGatewayConnectorGroup pitsCastGroup, boolean autoSend) {
        if ((DataGlobal.isEmptyNullString(message)) || (pitsCastGroup == null)){
            return;
        }
        synchronized(floatingFrameStorage){
            FloatingMessagingFrame frame = floatingFrameStorage.get(pitsCastGroup.getGroupName());
            frame.copyBoradcastMessageToBoard(message, autoSend);
        }
    }

    void updatePitsCastCheckTree() {
        synchronized(messagingBoardStorage){
            IMasterMessagingBoard board = messagingBoardStorage.get(PbcFloatingFrameTerms.PitsCastFrame.toString());
            if (board instanceof IFloatingPitsCastGroupMessagingBoard){
                ((IFloatingPitsCastGroupMessagingBoard)board).updatePitsCastCheckTree();
            }
        }
    }

    void selectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        synchronized(messagingBoardStorage){
            IMasterMessagingBoard board = messagingBoardStorage.get(PbcFloatingFrameTerms.PitsCastFrame.toString());
            if (board instanceof IFloatingPitsCastGroupMessagingBoard){
                ((IFloatingPitsCastGroupMessagingBoard)board).selectBuddyCheckNode(buddy);
            }
        }
    }

    void unselectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        synchronized(messagingBoardStorage){
            IMasterMessagingBoard board = messagingBoardStorage.get(PbcFloatingFrameTerms.PitsCastFrame.toString());
            if (board instanceof IFloatingPitsCastGroupMessagingBoard){
                ((IFloatingPitsCastGroupMessagingBoard)board).unselectBuddyCheckNode(buddy);
            }
        }
    }
}
