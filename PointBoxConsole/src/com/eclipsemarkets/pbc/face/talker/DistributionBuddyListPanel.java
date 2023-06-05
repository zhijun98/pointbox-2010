/**
 * Eclipse Market Solutions LLC
 *
 * GroupListTreePanel.java
 *
 * @author Zhijun Zhang
 * Created on Jun 25, 2010, 10:05:28 PM
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.pbc.face.talker.dndtree.IPbcDndBuddyTree;
import com.eclipsemarkets.pbc.face.talker.dndtree.PbcDndBuddyTreeFactory;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.web.PointBoxAccountID;
import com.eclipsemarkets.web.pbc.talker.BuddyListBuddyItem;
import com.eclipsemarkets.web.pbc.talker.BuddyListGroupItem;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * A buddy list panel which may contains buddies of various gateway server types
 * @author Zhijun Zhang
 */
public class DistributionBuddyListPanel extends HybridBuddyListPanel implements IDistributionBuddyListPanel
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(DistributionBuddyListPanel.class.getName());
    }
    
    DistributionBuddyListPanel(IPbcTalker talker) {
        super(talker);
        initComponents();
        
        jDisplayGroupFrame.setText(null);
        jDisplayGroupFrame.setIcon(getImageSettings().getClosedGroupIcon());
        jDisplayGroupFrame.setActionCommand(BuddyListPanelCommand.DisplayGroupFrame.name());
        jDisplayGroupFrame.setToolTipText(BuddyListPanelCommand.DisplayGroupFrame+"...");
        jDisplayGroupFrame.setVisible(false);
        
        jImport.setText(null);
        jImport.setIcon(getImageSettings().getGroupGoIcon());
        jImport.setActionCommand(BuddyListPanelCommand.AutoImport.name());
        jImport.setToolTipText(BuddyListPanelCommand.AutoImport+"...");
        
        //jAIMBtn.setText("AIM");
        jCreateGroup.setText(null);
        jCreateGroup.setIcon(getImageSettings().getAddGroupIcon());
        jCreateGroup.setActionCommand(BuddyListPanelCommand.CreateNewGroup.name());
        jCreateGroup.setToolTipText(BuddyListPanelCommand.CreateNewGroup.toString());

        jSortAZ.setText(null);
        jSortAZ.setIcon(getImageSettings().getSortingAZIcon());
        jSortAZ.setActionCommand(BuddyListPanelCommand.SortAZ.name());
        jSortAZ.setToolTipText(BuddyListPanelCommand.SortAZ.toString());

        jSortZA.setText(null);
        jSortZA.setIcon(getImageSettings().getSortingZAIcon());
        jSortZA.setActionCommand(BuddyListPanelCommand.SortZA.name());
        jSortZA.setToolTipText(BuddyListPanelCommand.SortZA.toString());

        jGroupZone.setViewportView(getDndBuddyTree().getBaseTree());
        
        initSearchAutoFill(jSearchField);
        
        jImport.setVisible(false);
    }

    JTextField getJSearchField() {
        return jSearchField;
    }
    
    void makeDisplayGroupFrameButtonVisable(){
        if (SwingUtilities.isEventDispatchThread()){
            jDisplayGroupFrame.setVisible(true);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jDisplayGroupFrame.setVisible(true);
                }
            });
        }
    }

    @Override
    public void displayDistribributionMessageBoard(final String message, final IGatewayConnectorGroup copyToGroup) {
        if (SwingUtilities.isEventDispatchThread()){
            displayDistribributionMessageBoardHelper(message, copyToGroup);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayDistribributionMessageBoardHelper(message, copyToGroup);
                }
            });
        }
    }
    private void displayDistribributionMessageBoardHelper(final String message, final IGatewayConnectorGroup copyToGroup) {
        if ((message == null) || (copyToGroup == null)){
            return;
        }
        fireGroupClickedEvent(copyToGroup, getDndBuddyTree().retrieveBuddiesOfGroup(copyToGroup), message, false);
    }

    /**
     * @deprecated 
     * @param message
     * @param sendToGroup 
     */
    @Override
    public void displayAndSendDistribributionMessageBoard(final String message, final IGatewayConnectorGroup sendToGroup) {
        if (SwingUtilities.isEventDispatchThread()){
            displayAndSendDistribributionMessageBoardHelper(message, sendToGroup);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayAndSendDistribributionMessageBoardHelper(message, sendToGroup);
                }
            });
        }
    }
    
    /**
     * @deprecated 
     * @param message
     * @param sendToGroup 
     */
    private void displayAndSendDistribributionMessageBoardHelper(final String message, final IGatewayConnectorGroup sendToGroup) {
        if ((DataGlobal.isEmptyNullString(message)) || (sendToGroup == null)){
            return;
        }
        fireGroupClickedEvent(sendToGroup, getDndBuddyTree().retrieveBuddiesOfGroup(sendToGroup), message, true);
    }

    @Override
    public String getDistListName() {
        return PbcBuddyListName.DISTRIBUTION_LIST.toString();
    }

    @Override
    public String getBuddyListType() {
        return PbcBuddyListType.BroadcastBuddyList.toString();
    }

    @Override
    IPbcDndBuddyTree createDndBuddyTree() {
        return PbcDndBuddyTreeFactory.createHybridDndBuddyTreePanel(this, PbcBuddyListName.DISTRIBUTION_LIST.toString(), isOfflineDisplayed());
    }

    @Override
    boolean isOfflineDisplayed() {
        return jDisplayOfflineBuddies.isSelected();
    }
    
    @Override
    public boolean isDistGroupExisted(String distGroupName) {
        return getDndBuddyTree().isDistGroupExisted(distGroupName);
    }
    
    @Override
    public boolean isEmptyPanel() {
        return (getDndBuddyTree().isEmpty());
    }

    /**
     * Current PBC account will be returned
     * @return 
     */
    @Override
    public IGatewayConnectorBuddy getMasterLoginUser() {
        return getKernel().getPointBoxLoginUser();
    }
    
    @Override
    void handleConnectorConnectedEventHelper(IGatewayConnectorBuddy loginUser) {
        loginUser.setBuddyStatus(BuddyStatus.Online);
        getDndBuddyTree().refreshDndBuddyTree(loginUser);
    }
    
    @Override
    void handleConnectorDisconnectedEventHelper(final IGatewayConnectorBuddy loginUser) {
        loginUser.setBuddyStatus(BuddyStatus.Offline);
        getDndBuddyTree().refreshDndBuddyTree(loginUser);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getAllLoginUsers() {
        return GatewayBuddyListFactory.getOnlineLoginUsers();
    }

    @Override
    void popupBuddyListMenu(JTree jDndTree, MouseEvent e) {
        TreePath treePath = jDndTree.getPathForLocation(e.getX(), e.getY());
        if (treePath == null){
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
        if (node != null){
            new BasicBuddyListMenu(getTalker(),
                                 this, 
                                 node).show(e.getComponent(), e.getX(), e.getY());
        }
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
    
    private void importPbimBuddyListIntoDistributionPanel() {
        final IPbcRuntime settings = getTalker().getKernel().getPointBoxConsoleRuntime();
        if (settings == null){
            return;
        }
        final PointBoxAccountID accountID = settings.getPointBoxAccountID();
        if (accountID == null){
            return;
        }
        //make sure 
        
        //IGatewayConnectorBuddy buddy = GatewayBuddyListFactory.get,
        (new SwingWorker<Void, BuddyListGroupItem>(){
            @Override
            protected Void doInBackground() throws Exception {
                //get distribution list from the settings
                ArrayList<PbcBuddyListSettings> buddyListSettings = getTalker().getRegularPbcBuddyListSettingsOfLiveConnectors();
                if ((buddyListSettings != null) && (!buddyListSettings.isEmpty())){
                    for (PbcBuddyListSettings aPbcBuddyListSettings : buddyListSettings){
                        populateMasterBuddyList(aPbcBuddyListSettings);
                    }
                }
                return null;
            }
            
            private void populateMasterBuddyList(PbcBuddyListSettings aPbcBuddyListSettings) {
                if (aPbcBuddyListSettings != null){
                    BuddyListGroupItem[] groupItems = sortGroupItems(aPbcBuddyListSettings.getGroupItems());
                    if ((groupItems != null) && (groupItems.length > 0)){
                        BuddyListGroupItem groupItem;
                        BuddyListBuddyItem[] buddyItems;
                        for (int i = 0; i < groupItems.length; i++){
                            groupItem = groupItems[i];
                            if ((groupItem != null) && (DataGlobal.isNonEmptyNullString(groupItem.getGroupName()))){
                                //todo: skip this default group because of redundency group name?
                                buddyItems = groupItem.getBuddyItems();
                                if ((buddyItems != null) && (buddyItems.length > 0)){
                                    publish(groupItem);
                                }
                            }
                            
                        }//groupItems
                    }
                }
            }

            @Override
            protected void process(List<BuddyListGroupItem> chunks) {
                for (BuddyListGroupItem groupItem : chunks){
                    getDndBuddyTree().loadDistributionGroupWithBuddyNodes(accountID, groupItem, sortBuddyItems(groupItem.getBuddyItems()));
                }//for: next chunk
            }

            @Override
            protected void done() {
                getDndBuddyTree().expandDndTreeModel();
                jDisplayOfflineBuddies.setSelected(true);
            }

            private BuddyListGroupItem[] sortGroupItems(BuddyListGroupItem[] groupItems) {
                ArrayList<BuddyListGroupItem> unsortedGroupItems = new ArrayList<BuddyListGroupItem>();
                boolean alphabetic = true;
                for (int i = 0; i < groupItems.length; i++){
                    unsortedGroupItems.add(groupItems[i]);
                    if (groupItems[i].getGroupIndex() > 0){
                        alphabetic = false;
                    }
                }//for
                if (alphabetic){
                    return PbcGlobal.sortGroupItemsAlphabetically(unsortedGroupItems);
                }else{
                    return PbcGlobal.sortGroupItemsByIndex(unsortedGroupItems);
                }
            }

            private BuddyListBuddyItem[] sortBuddyItems(BuddyListBuddyItem[] buddyItems) {
                ArrayList<BuddyListBuddyItem> unsortedBuddyItems = new ArrayList<BuddyListBuddyItem>();
                boolean alphabetic = true;
                for (int i = 0; i < buddyItems.length; i++){
                    unsortedBuddyItems.add(buddyItems[i]);
                    if (buddyItems[i].getBuddyIndex() > 0){
                        alphabetic = false;
                    }
                }//for
                if (alphabetic){
                    return PbcGlobal.sortBuddyItemsAlphabetically(unsortedBuddyItems);
                }else{
                    return PbcGlobal.sortBuddyItemsByIndex(unsortedBuddyItems);
                }
            }

        }).execute();
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
        jToolPanel = new javax.swing.JPanel();
        jToolBar = new javax.swing.JToolBar();
        jDisplayGroupFrame = new javax.swing.JButton();
        jCreateGroup = new javax.swing.JButton();
        jImport = new javax.swing.JButton();
        jSortAZ = new javax.swing.JButton();
        jSortZA = new javax.swing.JButton();
        jDisplayOfflineBuddies = new javax.swing.JCheckBox();
        jToolBar2 = new javax.swing.JToolBar();
        jSearchField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        jGroupZone.setName("jGroupZone"); // NOI18N
        add(jGroupZone, java.awt.BorderLayout.CENTER);

        jToolPanel.setName("jToolPanel"); // NOI18N
        jToolPanel.setLayout(new java.awt.BorderLayout());

        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);
        jToolBar.setName("jToolBar"); // NOI18N

        jDisplayGroupFrame.setText("PBcast");
        jDisplayGroupFrame.setFocusable(false);
        jDisplayGroupFrame.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jDisplayGroupFrame.setName("jDisplayGroupFrame"); // NOI18N
        jDisplayGroupFrame.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jDisplayGroupFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDisplayGroupFrameActionPerformed(evt);
            }
        });
        jToolBar.add(jDisplayGroupFrame);

        jCreateGroup.setText("CG");
        jCreateGroup.setFocusable(false);
        jCreateGroup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCreateGroup.setName("jCreateGroup"); // NOI18N
        jCreateGroup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCreateGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCreateGroupActionPerformed(evt);
            }
        });
        jToolBar.add(jCreateGroup);

        jImport.setText("Import");
        jImport.setFocusable(false);
        jImport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jImport.setName("jImport"); // NOI18N
        jImport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImportActionPerformed(evt);
            }
        });
        jToolBar.add(jImport);

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

        jToolPanel.add(jToolBar, java.awt.BorderLayout.PAGE_START);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N

        jSearchField.setForeground(java.awt.Color.gray);
        jSearchField.setText("search buddy...");
        jSearchField.setName("jSearchField"); // NOI18N
        jToolBar2.add(jSearchField);

        jToolPanel.add(jToolBar2, java.awt.BorderLayout.PAGE_END);

        add(jToolPanel, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void jDisplayOfflineBuddiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDisplayOfflineBuddiesActionPerformed
        displayOfflineBuddies(jDisplayOfflineBuddies.isSelected());
        storeShowOfflineOption(jDisplayOfflineBuddies.isSelected());
    }//GEN-LAST:event_jDisplayOfflineBuddiesActionPerformed

    private void jCreateGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCreateGroupActionPerformed
        displayAddNewGroupDialog();        
    }//GEN-LAST:event_jCreateGroupActionPerformed

    private void jSortAZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSortAZActionPerformed
        sortBuddyListFromA2Z(true);
    }//GEN-LAST:event_jSortAZActionPerformed

    private void jSortZAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSortZAActionPerformed
        sortBuddyListFromZ2A(true);
    }//GEN-LAST:event_jSortZAActionPerformed

    private void jImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jImportActionPerformed
        if (JOptionPane.showConfirmDialog(null, BuddyListPanelCommand.AutoImport+"?",
                                          "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            importPbimBuddyListIntoDistributionPanel();
        }
    }//GEN-LAST:event_jImportActionPerformed

    private void jDisplayGroupFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDisplayGroupFrameActionPerformed
        handleDisplayGroupFrameButtonClickedEvent();
    }//GEN-LAST:event_jDisplayGroupFrameActionPerformed
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton jCreateGroup;
    private javax.swing.JButton jDisplayGroupFrame;
    protected javax.swing.JCheckBox jDisplayOfflineBuddies;
    private javax.swing.JScrollPane jGroupZone;
    protected javax.swing.JButton jImport;
    private javax.swing.JTextField jSearchField;
    private javax.swing.JButton jSortAZ;
    private javax.swing.JButton jSortZA;
    protected javax.swing.JToolBar jToolBar;
    protected javax.swing.JToolBar jToolBar2;
    protected javax.swing.JPanel jToolPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setShowOfflineOption(boolean isShowOffline) {
        displayOfflineBuddies(isShowOffline);
    }

    //this is reserved for the possible implementation
    void handleDisplayGroupFrameButtonClickedEvent() {
        //do nothing
    }

    private enum ConnectorListTerm{
        AllConnectors("All Connectors");

        private String term;
        ConnectorListTerm(String term){
            this.term = term;
        }
        @Override
        public String toString() {
            return term;
        }
    }
}
