/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author Zhijun Zhang
 */
public class MasterMessagingBoardTabTextLabel extends JLabel implements Transferable{

    private static final Logger logger;

    static {
        logger = Logger.getLogger(MasterMessagingBoardTabTextLabel.class.getName());
    }

    private String tabUniqueID;
    private DataFlavor[] dataFlavors;

    public MasterMessagingBoardTabTextLabel(String tabUniqueID, 
                                            String text) {
        super(text);
        this.tabUniqueID = tabUniqueID;
        dataFlavors = new DataFlavor[]{DataFlavor.stringFlavor};
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        //logger.log(Level.INFO, ">>>TabTextLabel::getTransferData ...");
        if ((isTransferable()) && (flavor != null) && (flavor.equals(DataFlavor.stringFlavor))){
            return tabUniqueID;
        }else{
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        //logger.log(Level.INFO, ">>>TabTextLabel::getTransferDataFlavors ...");
        return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        //logger.log(Level.INFO, ">>>TabTextLabel::isDataFlavorSupported ...");
        for (DataFlavor f : dataFlavors) {
            if (f.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    private boolean transferable = true;

    public synchronized boolean isTransferable() {
        return transferable;
    }

    public synchronized void setTransferable(boolean transferable) {
        this.transferable = transferable;
    }
}
