/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDBuddyTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDGroupTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IPbcDndBuddyTree;
import com.eclipsemarkets.pbc.face.talker.dndtree.PbcDndBuddyTreeFactory;
import com.eclipsemarkets.pbc.kernel.GeneralFloatingFrameSettings;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * This class represents a tab panel for PITS-like group panel in the buddy list zone
 * @author Zhijun Zhang
 */
public class PitsGroupListPanel extends DistributionBuddyListPanel implements IPitsGroupListPanel{
    
    private static PitsGroupListPanel self;
    static{
        self = null;
    }
    
    private String tabName;
    
    public PitsGroupListPanel(IPbcTalker talker, String tabName) {
        super(talker);
        this.tabName = tabName;
        customizePitsGroupListPanelFace();
    }
    
    /**
     * Modify GUI-face of DistributionBuddyListPanel for PITS-like panels
     */
    void customizePitsGroupListPanelFace(){
        //mopdify tool bar...
        this.jToolBar.remove(this.jImport);
        this.jToolBar.remove(this.jCreateGroup);
//        this.jToolBar.remove(this.jDisplayOfflineBuddies);
        this.jToolPanel.remove(this.jToolBar2);
        
        getDndBuddyTree().disableSecondaryNodeExpanded();
        getDndBuddyTree().removeAllNodes();
        getDndBuddyTree().getBaseTree().expandRow(0);
    }

    /**
     * Check if pitsGroupName has been used somewhere on the panels in the buddy list zone
     * @param pitsGroupName
     * @return 
     */
    private boolean isPitsGroupExsitedOnPanels(String pitsGroupName){
        if(isGroupExisted(pitsGroupName) 
                || getTalker().checkGroupNameRedundancy(pitsGroupName))
        {
            return true;
        }
         return false;
    }

    @Override
    public String getDistListName() {
        /**
         * Here it does not use PbcBuddyListSettings.PitsL because it could be multiple PITS 
         * tab panels in the future. tabName can be unique
         */
        return tabName;
    }

    @Override
    public String getBuddyListType() {
        return PbcBuddyListType.PitsGroupList.toString();
    }
    
    @Override
    IPbcDndBuddyTree createDndBuddyTree() {
        return PbcDndBuddyTreeFactory.createHybridDndBuddyTreePanel(this, PbcBuddyListName.PITS_LIST.toString(), isOfflineDisplayed());
    }
    
    @Override
    void popupBuddyListMenu(JTree jDndTree, MouseEvent e) {

        TreePath treePath = jDndTree.getPathForLocation(e.getX(), e.getY());
        if (treePath == null){
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
        if ((node instanceof IDnDGroupTreeNode) || (node instanceof IDnDBuddyTreeNode)){
            (new PitsGroupListPanelMenu(getTalker(),  //SaveBuddiesFramesPanelMenu
                                         this, 
                                         node)).show(e.getComponent(), e.getX(), e.getY());
        }
    }  

    @Override
    public void addBuddyIntoPitsGroup(final IGatewayConnectorBuddy buddy, final String pitsGroupName) {
        getDndBuddyTree().loadExistingGroupWithBuddyNode(pitsGroupName, buddy);
    }

    @Override
    public void removeBuddyFromPitsGroup(final IGatewayConnectorBuddy buddy, final String groupName) {
        if (SwingUtilities.isEventDispatchThread()){
            getDndBuddyTree().removeBuddyFromGroup(buddy, groupName);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    getDndBuddyTree().removeBuddyFromGroup(buddy, groupName);
                }
            });
        }
    }
    
    @Override
    public void renamePitsFrame(IGatewayConnectorGroup group,
                            String pbcloginUserUniqueName)
    {
        String newGroupName = JOptionPane.showInputDialog(this,"Please input a new name for change:");
        if(DataGlobal.isNonEmptyNullString(newGroupName)){
            if(newGroupName.isEmpty()||!PbcGlobal.isLegalInput(newGroupName)){
                JOptionPane.showMessageDialog(this, "Only alphabetic characters and digit numbers are legal for group name!");
                return;
            }
            if(isPitsGroupExsitedOnPanels(newGroupName)){
                JOptionPane.showMessageDialog(this, "This name has exsited on Distribution panel or current panel. Please change a new one!");
                return;                    
            }
            String oldGroupName = group.getGroupName();
            PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
            GeneralFloatingFrameSettings oldSettings = prop.retrieveOpenedPitsFloatingFrameSettings(pbcloginUserUniqueName, oldGroupName);
            //remove old persistent settings
            prop.removeOpenedPitsFloatingFrameSettings(pbcloginUserUniqueName, oldGroupName);
            //save new perisstet settings
            if ((oldSettings != null) && (oldSettings.getLocation() != null) && (oldSettings.getSize() != null)){
                prop.storeOpenedPitsFloatingFrame(pbcloginUserUniqueName, newGroupName, oldSettings.getLocation(), oldSettings.getSize(), true);
            }else{
                prop.storeOpenedPitsFloatingFrame(pbcloginUserUniqueName, newGroupName, null, null, true);
            }
            getTalker().renamePitsFloatingFrame(group, newGroupName);
            //change group name
            group.setGroupName(newGroupName);
            //refresh GUI
            refreshPanel();
            //save this buddy list settings on the server
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(constructPbcBuddyListSettings(), true);
            JOptionPane.showMessageDialog(this, "Rename completed!");
        }
    }
    
    @Override
    public void removePitsFrame(IGatewayConnectorGroup group,String pbloginUserName){
        //if(JOptionPane.showConfirmDialog(this, "Are you sure to remove this frame?","Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){   
            deleteDistributionGroup(group);
            getTalker().hidePitsFloatingFrame(group);
            //JOptionPane.showMessageDialog(this, "Deletion completed!");
            PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
            prop.removeOpenedPitsFloatingFrameSettings(pbloginUserName, group.getGroupName());
        //}
    }
}
