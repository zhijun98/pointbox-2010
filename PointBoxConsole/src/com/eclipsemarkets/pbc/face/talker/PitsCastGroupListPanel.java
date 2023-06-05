/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDBuddyTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDGroupTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IPbcDndBuddyTree;
import com.eclipsemarkets.pbc.face.talker.dndtree.PbcDndBuddyTreeFactory;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * PitsCastGroupListPanel should be singleton in PBC-wide although implementation 
 * is not singleton-like. 
 * 
 * @author Zhijun Zhang, date & time: Dec 21, 2013 - 9:30:54 AM
 */
public class PitsCastGroupListPanel extends PitsGroupListPanel implements IPitsCastGroupListPanel{
    
    private static PitsCastGroupListPanel self;
    static{
        self = null;
    }
    
    PitsCastGroupListPanel(IPbcTalker talker) {
        super(talker, PbcBuddyListPanelTabName.PITS_CAST.toString());
        //makeDisplayGroupFrameButtonVisable();
    }

    @Override
    void handleDisplayGroupFrameButtonClickedEvent() {
        if (SwingUtilities.isEventDispatchThread()){
            fireTreeRootClickedEvent();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    fireTreeRootClickedEvent();
                }
            });
        }
    }

    @Override
    public String getDistListName() {
        return PbcBuddyListName.PITSCAST_LIST.toString();
    }

    @Override
    public String getBuddyListType() {
        return PbcBuddyListType.PitsCastList.toString();
    }
    
    @Override
    IPbcDndBuddyTree createDndBuddyTree() {
        return PbcDndBuddyTreeFactory.createHybridDndBuddyTreePanel(this, PbcBuddyListPanelTabName.PITS_CAST.toString(), isOfflineDisplayed());
    }
    
    @Override
    void popupBuddyListMenu(JTree jDndTree, MouseEvent e) {

        TreePath treePath = jDndTree.getPathForLocation(e.getX(), e.getY());
        if (treePath == null){
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
        if ((node instanceof IDnDGroupTreeNode) || (node instanceof IDnDBuddyTreeNode)){
            (new PitsCastListPanelMenu(getTalker(),  //SaveBuddiesFramesPanelMenu
                                         this, 
                                         node)).show(e.getComponent(), e.getX(), e.getY());
        }
    }  
    
    @Override
    void customizePitsGroupListPanelFace() {
//        this.jToolBar.remove(this.jDisplayOfflineBuddies);
//        this.jToolPanel.remove(this.jToolBar2);
        
        getDndBuddyTree().removeAllNodes();
        getDndBuddyTree().getBaseTree().expandRow(0);
    }
    
}
