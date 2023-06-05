/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.PointBoxCursor;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
class MessagingBoardDropTargetListener implements DropTargetListener {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(MessagingBoardDropTargetListener.class.getName());
    }

    MasterMessagingBoard dropTargetBoard;
    Cursor originalCursor;

    MessagingBoardDropTargetListener(MasterMessagingBoard dropTarget) {
        this.dropTargetBoard = dropTarget;
        originalCursor = dropTarget.getCursor();
    }
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)){
            this.dropTargetBoard.setCursor(PointBoxCursor.droppableCursor);
        }else{
            this.dropTargetBoard.setCursor(PointBoxCursor.nonDroppableCursor);
        }
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        this.dropTargetBoard.setCursor(originalCursor);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
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
                                                                                                                                     true,
                                                                                                                                     PbcReservedTerms.DefaultPitsLikeFrameTitle.toString());
                    if ((dragBoards == null) || (dragBoards.isEmpty())){
                        //do nothing. actually, this case is impossible
                        logger.log(Level.SEVERE, null, tabUniqueID + " was not associated with any distGroups");
                    }else{
                        for (IMasterMessagingBoard dragBoard : dragBoards){
//                            boolean isExisted = MessagingPaneManager.getSingleton().isButtonTabComponentExisted(tabUniqueID);
//                            if(!isExisted){
//                                MessagingPaneManager.getSingleton(null).dropMessagingTab(
//                                        dropTargetBoard.getBoardId(), dragBoard.getBoardId(), tabUniqueID, true);
//                            }
//                            if(isExisted){
//                                dropTargetBoard.getTalker().getMessagingPaneManager().announceTabClosingEvent(tabUniqueID);
//                                MessagingPaneManager.getSingleton(dropTargetBoard.getTalker())
//                                        .popupMessagingTabFromMaster(tabUniqueID, false); 
////                                makeVoidFrameDisposed();
//                                
//                            }
                            MessagingPaneManager.getSingleton(null).dropMessagingTab(
                                    dropTargetBoard.getBoardId(), dragBoard.getBoardId(), tabUniqueID, true);
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
    
//    private void makeVoidFrameDisposed(){
//        synchronized(MessagingPaneManagerImpl.floatingFrameStorage){
//            Collection<FloatingMessagingFrame> frames =MessagingPaneManagerImpl.floatingFrameStorage.values();
//            ArrayList<FloatingMessagingFrame> droppingFrames = new ArrayList<FloatingMessagingFrame>();
//            for (FloatingMessagingFrame frame : frames){
//                if(frame.getFloatingMessagingBoard().getAllVisibleTabeButtons().size()<=0){
//                    droppingFrames.add(frame);
//                }
//            }
//            for (FloatingMessagingFrame frame : droppingFrames){
//                MessagingPaneManagerImpl.floatingFrameStorage.remove(frame.getFloatingMessagingBoard().getBoardId());
//                frame.dispose();
//            }
//        }         
//    }    
    
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}
}
