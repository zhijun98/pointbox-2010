/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import java.awt.datatransfer.Transferable;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author Zhijun Zhang
 */
class BuddyMessagingBoardDragHandler extends TransferHandler {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(BuddyMessagingBoardDragHandler.class.getName());
    }

    BuddyMessagingBoardDragHandler() {
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof MasterMessagingBoardTabTextLabel){
            return (MasterMessagingBoardTabTextLabel)c;
        }else{
            return null;
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        if (c instanceof MasterMessagingBoardTabTextLabel) {
            return TransferHandler.MOVE;
        }
        return TransferHandler.NONE;
    }

//    @Override
//    protected void exportDone(JComponent source, Transferable data, int action) {
//        if (source instanceof MasterMessagingBoardTabTextLabel){
//            try {
//                String tabUniqueID = data.getTransferData(DataFlavor.stringFlavor).toString();
//                if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
//                    //remove its tab from dragSourceBoard
//                    logger.log(Level.INFO, "remove its tab from dragSourceBoard");
//                    dragSourceBoard.hideButtonTabComponent(tabUniqueID);
//                }
//            } catch (UnsupportedFlavorException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//        }
//    }
}
