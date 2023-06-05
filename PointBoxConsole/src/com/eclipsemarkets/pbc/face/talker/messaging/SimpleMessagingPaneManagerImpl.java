/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IFloatingPitsMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simplified MessagingPaneManagerImpl which permit the followings: (1) When user 
 * pops out a buddy tab the tab should go into a new floating window; (2) User should 
 * be able to drag buddy tab from one floating window into another. 
 * 
 * @author Zhijun Zhang
 * @date & time: Aug 26, 2012 - 11:10:11 AM
 */
class SimpleMessagingPaneManagerImpl extends MessagingPaneManagerImpl{
    
    private static final Logger logger;
    static{
        logger = Logger.getLogger(SimpleMessagingPaneManagerImpl.class.getName());
    }

    public SimpleMessagingPaneManagerImpl(IPbcTalker talker) {
        super(talker);
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
    @Override
    ArrayList<IMasterMessagingBoard> acquireFloatingMessagingBoards(IMasterMessagingBoard masterMessagingBoard, 
                                                                    String tabUniqueID, 
                                                                    boolean isPresented, 
                                                                    String floatingFrameName) 
    {
        IPbcTalker talker = masterMessagingBoard.getTalker();
        ArrayList<IMasterMessagingBoard> groupBoards = new ArrayList<IMasterMessagingBoard>();
        HashMap<String, IButtonTabComponent> buttonTabs = buttonTabStorage.get(masterMessagingBoard.getBoardId());
        if (buttonTabs == null){
            //logger.log(Level.SEVERE, null, new Exception("buttonTabs - " + buttonTabs + " should have been initiated already."));
            return groupBoards;
        }        
        IButtonTabComponent buttonTab = buttonTabs.get(tabUniqueID);
        if (buttonTab == null){
            logger.log(Level.SEVERE, null, new Exception("tabUniqueID - " + tabUniqueID + " should have been initiated already."));
            return groupBoards;
        }
        ArrayList<IGatewayConnectorGroup> distGroups;
        if (buttonTab instanceof BuddyButtonTabComponent){
            //process for buddyButtoneTabeComponent
            IGatewayConnectorBuddy buddy =((BuddyButtonTabComponent)buttonTab).getBuddy();
            distGroups = talker.getAssociatedDistributionGroups(buddy);
            synchronized(messagingBoardStorage){
                Set<String> messagingBoardIds = messagingBoardStorage.keySet();
                if ((distGroups == null) || (distGroups.isEmpty())) {
                    distGroups = new ArrayList<IGatewayConnectorGroup>();
                    distGroups.add(GatewayBuddyListFactory.getDefaultDistributionGroupInstance(buddy.getLoginOwner()));
                }//for
                IGatewayConnectorGroup distGroup = distGroups.get(0);
                if (messagingBoardIds.contains(tabUniqueID)){
                   groupBoards.add(retrieveMessagingBoard(tabUniqueID));
                }else{
                    IFloatingPitsMessagingBoard board = null;
                    if (PbcReservedTerms.DefaultPitsLikeFrameTitle.toString().equalsIgnoreCase(floatingFrameName)){
                        IMasterMessagingBoard aBoard = this.retrieveMessagingBoard(floatingFrameName);
                        if (aBoard instanceof IFloatingPitsMessagingBoard){
                            board = (IFloatingPitsMessagingBoard)aBoard;
                        }
                    }
                    if (board == null){
                        //creat a fresh new board and present its frame
                        board = new FloatingPitsMessagingBoard(talker, 
                                                            distGroup, 
                                                            buddy, 
                                                            ((BuddyButtonTabComponent)buttonTab).getTabUniqueID(), 
                                                            floatingFrameName);
                    }
                    registerMessagingBoard(board);
                    groupBoards.add(board);
                    //diaply frame
                    if(isPresented){
                        presentFloatingMessagingFrame(talker, board,floatingFrameName);
                    }
                }
            }
        }else if (buttonTab instanceof GroupButtonTabComponent){
            distGroups = new ArrayList<IGatewayConnectorGroup>();
            distGroups.add(((GroupButtonTabComponent)buttonTab).getGroup());
            //process for groupButtoneTabeComponent
            synchronized(messagingBoardStorage){
                Set<String> messagingBoardIds = messagingBoardStorage.keySet();
                for (IGatewayConnectorGroup distGroup : distGroups){
                    if (messagingBoardIds.contains(distGroup.getGroupName())){
                        groupBoards.add(retrieveMessagingBoard(distGroup.getGroupName()));
                        //diaply frame
                        if(isPresented){
                            presentFloatingMessagingFrame(talker, retrieveMessagingBoard(distGroup.getGroupName()),floatingFrameName);
                        }                         
                    }else{
                        //creat a fresh new board and present its frame
                        FloatingDistGroupMessagingBoard board = new FloatingDistGroupMessagingBoard(talker, distGroup, talker.getBuddiesOfGroup(distGroup));
                        registerMessagingBoard(board);
                        groupBoards.add(board);
                        //diaply frame
                        presentFloatingMessagingFrame(talker, board,floatingFrameName);
                    }
                }//for
            }
        }else{
            return groupBoards;
        }
        return groupBoards;
    }
}
