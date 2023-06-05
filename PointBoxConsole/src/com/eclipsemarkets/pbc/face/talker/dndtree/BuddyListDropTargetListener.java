/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.global.PointBoxCursor;
import java.awt.Cursor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.logging.Logger;
import javax.swing.JTree;

/**
 *
 * @author Zhijun Zhang
 */
public class BuddyListDropTargetListener implements DropTargetListener{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(BuddyListDropTargetListener.class.getName());
    }
    
    private JTree dropTargetTree;
    private Cursor originalCursor;

    public BuddyListDropTargetListener(JTree tree) {
        this.dropTargetTree = tree;
        originalCursor = tree.getCursor();
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DnDTreeList.DnDTreeList_FLAVOR) || (dtde.isDataFlavorSupported(DnDMutableTreeNode.DnDNode_FLAVOR))){
            //this.dropTargetTree.setCursor(PointBoxCursor.droppableCursor);              //David does't like this cursor, so hide it
        }else{
            this.dropTargetTree.setCursor(PointBoxCursor.nonDroppableCursor);
        }
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        this.dropTargetTree.setCursor(originalCursor);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        this.dropTargetTree.setCursor(originalCursor);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }
}
