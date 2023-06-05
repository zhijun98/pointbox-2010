/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.talker.IFloatingPitsMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 * @date & time: Aug 28, 2012 - 8:28:35 AM
 */
class BuddyMessagingBoardDropTargetListener extends MessagingBoardDropTargetListener{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(BuddyMessagingBoardDropTargetListener.class.getName());
    }

    BuddyMessagingBoardDropTargetListener(MasterMessagingBoard dropTarget) {
        super(dropTarget);
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        this.dropTargetBoard.setCursor(originalCursor);
        Transferable dragTarget = dtde.getTransferable();
        if (dragTarget.isDataFlavorSupported(DataFlavor.stringFlavor)){
            try {
                String tabUniqueID = dragTarget.getTransferData(DataFlavor.stringFlavor).toString();
                if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
                    if(MessagingPaneManagerImpl.isGroupTabID(tabUniqueID)){  //group tab is not allow to dragged. Only double-click group node to generate.
                        return;
                    }
//                    if(MessagingPaneManagerImpl.isGroupTabID(dropTargetBoard.getBoardId()) && MessagingPaneManagerImpl.isBuddyTabID(tabUniqueID)){
//                        return;
//                    }                     
                    //make sure board with frame are ready
                    ArrayList<IMasterMessagingBoard> dragBoards = MessagingPaneManager.getSingleton().acquireFloatingMessagingBoards(tabUniqueID,
                                                                                                                                     false,
                                                                                                                                     PbcReservedTerms.DefaultPitsLikeFrameTitle.toString());
                    if ((dragBoards == null) || (dragBoards.isEmpty())){
                        //do nothing. actually, this case is impossible
                        logger.log(Level.SEVERE, null, tabUniqueID + " was not associated with any distGroups");
                    }else{
                        for (IMasterMessagingBoard dragBoard : dragBoards){
                            if (dragBoard instanceof FloatingPitsMessagingBoard){
                                MessagingPaneManager.getSingleton(null).dropMessagingTab(
                                                                            MessagingPaneManager.getSingleton(null).getMasterMessagingBoard().getBoardId(), 
                                                                            dropTargetBoard.getBoardId(), 
                                                                            tabUniqueID, 
                                                                            true);
                                if (dropTargetBoard instanceof IFloatingPitsMessagingBoard){
                                    IGatewayConnectorBuddy buddyLoginUser = TargetBuddyPair.parseLoginUserForBuddyOfTab(tabUniqueID);
                                    if (buddyLoginUser != null){
                                        IGatewayConnectorBuddy buddy = TargetBuddyPair.parseBuddyOfTab(buddyLoginUser, tabUniqueID);
                                        if (buddy != null){
                                            ((IFloatingPitsMessagingBoard)dropTargetBoard).handlePitsBuddyTabOpened(buddy);
                                        }
                                    }
                                }
                            }else{
                                MessagingPaneManager.getSingleton(null).dropMessagingTab(
                                        dropTargetBoard.getBoardId(), dragBoard.getBoardId(), tabUniqueID, true);
                            }
                        }
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                //logger.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (Exception ex){
                logger.log(Level.SEVERE, null, ex);
            }
        }//if
    }
}
