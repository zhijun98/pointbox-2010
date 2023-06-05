/**
 * Eclipse Market Solutions LLC
 *
 * GroupListTreePanel.java
 *
 * @author Zhijun Zhang
 * Created on Jun 25, 2010, 10:05:28 PM
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.BuddyItemPresentedEvent;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDBuddyTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDGroupTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IPbcDndBuddyTree;
import com.eclipsemarkets.pbc.face.talker.dndtree.PbcDndBuddyTreeFactory;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.web.PointBoxConnectorID;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * A buddy list panel which may contains buddies of various gateway server types
 * @author Zhijun Zhang
 */
final class RegularBuddyListPanel extends AbstractBuddyListPanel implements IRegularBuddyListPanel
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
   
    private IPbcTalker talker;
    
    static{
        logger = Logger.getLogger(RegularBuddyListPanel.class.getName());
    }

    private IGatewayConnectorBuddy loginUser;
    
    RegularBuddyListPanel(IPbcTalker talker, IGatewayConnectorBuddy loginUser) {
        super(talker);
        this.talker=talker;
        initComponents();
 
        this.loginUser = loginUser;
        
        jSynGroups.setText(null);
        jSynGroups.setIcon(getImageSettings().getGroupGoIcon());
        jSynGroups.setActionCommand(BuddyListPanelCommand.SynGroups.toString());
        jSynGroups.setToolTipText(BuddyListPanelCommand.SynGroups.toString());
        jSynGroups.setVisible(false);

        jAddBuddy.setText(null);
        jAddBuddy.setIcon(getImageSettings().getAddBuddyIcon());
        jAddBuddy.setActionCommand(BuddyListPanelCommand.AddNewBuddy.toString());
        jAddBuddy.setToolTipText(BuddyListPanelCommand.AddNewBuddy.toString());
        
        jAddGroup.setText(null);
        jAddGroup.setIcon(getImageSettings().getAddGroupIcon());
        jAddGroup.setActionCommand(BuddyListPanelCommand.CreateNewGroup.toString());
        jAddGroup.setToolTipText(BuddyListPanelCommand.CreateNewGroup.toString());

        jSortAZ.setText(null);
        jSortAZ.setIcon(getImageSettings().getSortingAZIcon());
        jSortAZ.setActionCommand(BuddyListPanelCommand.SortAZ.toString());
        jSortAZ.setToolTipText(BuddyListPanelCommand.SortAZ.toString());

        jSortZA.setText(null);
        jSortZA.setIcon(getImageSettings().getSortingZAIcon());
        jSortZA.setActionCommand(BuddyListPanelCommand.SortZA.toString());
        jSortZA.setToolTipText(BuddyListPanelCommand.SortZA.toString());
        
        jGroupZone.setViewportView(getDndBuddyTree().getBaseTree());  
        
        initSearchAutoFill(jSearchField);
    }
    
    @Override
    public void displayOfflineBuddies(final boolean value) {
        getDndBuddyTree().displayDndTree(value);
        if (SwingUtilities.isEventDispatchThread()){
            jDisplayOfflineBuddies.setSelected(value);            
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jDisplayOfflineBuddies.setSelected(value);
                }
            });
        }
    }

    @Override
    public String getDistListName() {
        if (loginUser == null){
            return null;
        }
        return GatewayBuddyListFactory.constructBuddyIMUniqueName(loginUser.getIMServerType().toString(), loginUser.getIMScreenName());
    }

    @Override
    public String getBuddyListType() {
        return PointBoxTalker.generateRegularBuddyListType();
    }

    @Override
    PointBoxConnectorID getDistListOwnerID() {
        PointBoxConnectorID aPointBoxConnectorID = getKernel().getPointBoxConnectorID(loginUser);
        /**
         * If owner-login-user did not log in, it could be NULL in memory. Thus, try settings from 
         * the server
         */
        if (aPointBoxConnectorID == null){
            IPbcRuntime runtime = this.getKernel().getPointBoxConsoleRuntime();
            if (runtime != null){
                aPointBoxConnectorID = runtime.getPointBoxConnectorIdFromPbcBuddyListSettings(loginUser);
            }
        }
        return aPointBoxConnectorID;
    }

    @Override
    IPbcDndBuddyTree createDndBuddyTree() {
        return PbcDndBuddyTreeFactory.createRegularDndBuddyTreePanel(this, loginUser, isOfflineDisplayed());
    }

    @Override
    boolean isOfflineDisplayed() {
        return jDisplayOfflineBuddies.isSelected();
    }

    /**
     * Current PBC account will be returned
     * @return 
     */
    @Override
    public IGatewayConnectorBuddy getMasterLoginUser() {
        return loginUser;
    }
    
    @Override
    void handleConnectorConnectedEventHelper(IGatewayConnectorBuddy loginUser) {
        loginUser.setBuddyStatus(BuddyStatus.Online);
        if (this.loginUser != loginUser){
            this.loginUser = loginUser;
        }
    }
    
    @Override
    void handleConnectorDisconnectedEventHelper(final IGatewayConnectorBuddy loginUser) {
        loginUser.setBuddyStatus(BuddyStatus.Offline);
        getDndBuddyTree().refreshDndBuddyTree(loginUser);
    }

    @Override
    void popupBuddyListMenu(JTree jDndTree, MouseEvent e) {

        TreePath treePath = jDndTree.getPathForLocation(e.getX(), e.getY());
        if (treePath == null){
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
        if ((node instanceof IDnDGroupTreeNode) || (node instanceof IDnDBuddyTreeNode)){
            (new RegularBuddyListMenu(getTalker(),
                                         this, 
                                         node,jDisplayOfflineBuddies.isSelected())).show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jGroupZone = new javax.swing.JScrollPane();
        toolBarsPanel = new javax.swing.JPanel();
        jToolBar = new javax.swing.JToolBar();
        jAddBuddy = new javax.swing.JButton();
        jAddGroup = new javax.swing.JButton();
        jSynGroups = new javax.swing.JButton();
        jSortAZ = new javax.swing.JButton();
        jSortZA = new javax.swing.JButton();
        jDisplayOfflineBuddies = new javax.swing.JCheckBox();
        jToolBar2 = new javax.swing.JToolBar();
        jSearchField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        jGroupZone.setName("jGroupZone"); // NOI18N
        add(jGroupZone, java.awt.BorderLayout.CENTER);

        toolBarsPanel.setName("toolBarsPanel"); // NOI18N
        toolBarsPanel.setLayout(new java.awt.BorderLayout());

        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);
        jToolBar.setName("jToolBar"); // NOI18N

        jAddBuddy.setText("AddBuddy");
        jAddBuddy.setFocusable(false);
        jAddBuddy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jAddBuddy.setName("jAddBuddy"); // NOI18N
        jAddBuddy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jAddBuddy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddBuddyActionPerformed(evt);
            }
        });
        jToolBar.add(jAddBuddy);

        jAddGroup.setText("AddGroup");
        jAddGroup.setFocusable(false);
        jAddGroup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jAddGroup.setName("jAddGroup"); // NOI18N
        jAddGroup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jAddGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddGroupActionPerformed(evt);
            }
        });
        jToolBar.add(jAddGroup);

        jSynGroups.setText("SynGroups");
        jSynGroups.setFocusable(false);
        jSynGroups.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSynGroups.setName("jSynGroups"); // NOI18N
        jSynGroups.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSynGroups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSynGroupsActionPerformed(evt);
            }
        });
        jToolBar.add(jSynGroups);

        jSortAZ.setText("SortAZ");
        jSortAZ.setFocusable(false);
        jSortAZ.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSortAZ.setName("jSortAZ"); // NOI18N
        jSortAZ.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSortAZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSortAZActionPerformed(evt);
            }
        });
        jToolBar.add(jSortAZ);

        jSortZA.setText("SortZA");
        jSortZA.setFocusable(false);
        jSortZA.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSortZA.setName("jSortZA"); // NOI18N
        jSortZA.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSortZA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSortZAActionPerformed(evt);
            }
        });
        jToolBar.add(jSortZA);

        jDisplayOfflineBuddies.setSelected(true);
        jDisplayOfflineBuddies.setText("Show Offline");
        jDisplayOfflineBuddies.setFocusable(false);
        jDisplayOfflineBuddies.setName("jDisplayOfflineBuddies"); // NOI18N
        jDisplayOfflineBuddies.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jDisplayOfflineBuddies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDisplayOfflineBuddiesActionPerformed(evt);
            }
        });
        jToolBar.add(jDisplayOfflineBuddies);

        toolBarsPanel.add(jToolBar, java.awt.BorderLayout.PAGE_START);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N

        jSearchField.setForeground(java.awt.Color.gray);
        jSearchField.setText("search buddy...");
        jSearchField.setName("jSearchField"); // NOI18N
        jToolBar2.add(jSearchField);

        toolBarsPanel.add(jToolBar2, java.awt.BorderLayout.PAGE_END);

        add(toolBarsPanel, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents
       
    private void jDisplayOfflineBuddiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDisplayOfflineBuddiesActionPerformed
        displayOfflineBuddies(jDisplayOfflineBuddies.isSelected());
        storeShowOfflineOption(jDisplayOfflineBuddies.isSelected());
    }//GEN-LAST:event_jDisplayOfflineBuddiesActionPerformed

    private void jAddBuddyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAddBuddyActionPerformed
        if(!BuddyStatus.Online.equals(loginUser.getBuddyStatus())){
              JOptionPane.showMessageDialog(getTalker().getPointBoxFrame(), 
                    "You need to log into " + loginUser.getIMScreenName() + " of " + loginUser.getIMServerType() + " before this operation.");
              return;
        } 
        displayAddNewBuddyDialog(null);
    }//GEN-LAST:event_jAddBuddyActionPerformed

    private void jSortAZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSortAZActionPerformed
        sortBuddyListFromA2Z(true);
    }//GEN-LAST:event_jSortAZActionPerformed

    private void jSortZAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSortZAActionPerformed
        sortBuddyListFromZ2A(true);
    }//GEN-LAST:event_jSortZAActionPerformed

    private void jAddGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAddGroupActionPerformed
        if(!BuddyStatus.Online.equals(loginUser.getBuddyStatus())){
              JOptionPane.showMessageDialog(getTalker().getPointBoxFrame(), 
                    "You need to log into " + loginUser.getIMScreenName() + " of " + loginUser.getIMServerType() + " before this operation.");
              return;
        }        
        displayAddNewGroupDialog();
    }//GEN-LAST:event_jAddGroupActionPerformed

    private void jSynGroupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSynGroupsActionPerformed
        if (BuddyStatus.Online.equals(loginUser.getBuddyStatus())){
            if (JOptionPane.showConfirmDialog(getTalker().getPointBoxFrame(), 
                                            "Are you sure to synchronize and donwload "+loginUser.getIMScreenName()
                                            +" 's original buddy list from "+loginUser.getIMServerType()+" server? "
                                            + "If yes, current buddy list will be permanently gone. "
                                            + "This operation may take couples of seconds.", 
                                            "Confirm:", JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION)
            {
                HashMap<IGatewayConnectorBuddy, String> storedBuddyList = this.getTalker().retrieveOriginalServerSideBuddyList(loginUser);
                if ((storedBuddyList != null) && (!storedBuddyList.isEmpty())){
                    getDndBuddyTree().removeAllNodes();
                    Set<IGatewayConnectorBuddy> keys = storedBuddyList.keySet();
                    Iterator<IGatewayConnectorBuddy> itr = keys.iterator();
                    IGatewayConnectorBuddy buddy;
                    while (itr.hasNext()){
                        buddy = itr.next();
                        buddy.setBuddyGroupName(storedBuddyList.get(buddy));
                        getKernel().raisePointBoxEvent(
                                new BuddyItemPresentedEvent(PointBoxEventTarget.PbcFace,
                                                            loginUser,
                                                            buddy));
                    }
                }
            }
        }else{
            JOptionPane.showMessageDialog(getTalker().getPointBoxFrame(), 
                    "You need to log into " + loginUser.getIMScreenName() + " of " + loginUser.getIMServerType() + " before this operation.");
        }
    }//GEN-LAST:event_jSynGroupsActionPerformed
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAddBuddy;
    private javax.swing.JButton jAddGroup;
    private javax.swing.JCheckBox jDisplayOfflineBuddies;
    private javax.swing.JScrollPane jGroupZone;
    private javax.swing.JTextField jSearchField;
    private javax.swing.JButton jSortAZ;
    private javax.swing.JButton jSortZA;
    private javax.swing.JButton jSynGroups;
    private javax.swing.JToolBar jToolBar;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JPanel toolBarsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setShowOfflineOption(boolean isShowOffline) {
        displayOfflineBuddies(isShowOffline);
    }
}
