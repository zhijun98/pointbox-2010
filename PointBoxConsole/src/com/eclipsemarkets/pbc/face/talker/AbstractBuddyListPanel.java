/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.ServerLoginStatusEvent;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.talker.dndtree.ConfirmNewBuddyDialog;
import com.eclipsemarkets.pbc.face.talker.dndtree.ConfirmNewBuddyDialogType;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDBuddyTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDGroupTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IPbcDndBuddyTree;
import com.eclipsemarkets.pbc.face.talker.BuddyListBuddyEditor.Purpose;
import com.eclipsemarkets.pbc.face.talker.dndtree.DnDMutableHybridTreeRoot;
////import com.eclipsemarkets.pbc.face.talker.dndtree.IDndBuddyTreeStructureListener;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.PointBoxConnectorID;
import com.eclipsemarkets.web.pbc.talker.BuddyListBuddyItem;
import com.eclipsemarkets.web.pbc.talker.BuddyListGroupItem;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Zhijun Zhang
 */
public abstract class AbstractBuddyListPanel extends JPanel implements IBuddyListPanel/////, IDndBuddyTreeStructureListener 
{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(AbstractBuddyListPanel.class.getName());
    }
    
    private IPbcTalker talker;
    private IPbcDndBuddyTree dndTree;
    
    protected JTable searchTable;
    protected TableRowSorter<DefaultTableModel> rowSorter;
    protected DefaultTableModel searchTableModel;
    protected JTextField jSearchField;
    private JPopupMenu popup;
    
    private final PbcBuddyListLoader buddyListLoader;

    /**
     * listen to buddy event on the list
     */
    private final ArrayList<IBuddyListEventListener> buddyListPanelListeners;
    
//////    private BuddyListChangedEventHandler buddyListChangedEventHandler;

    AbstractBuddyListPanel(IPbcTalker talker) {
        this.talker = talker;
        dndTree = null;
        buddyListPanelListeners = new ArrayList<IBuddyListEventListener>();

        buddyListLoader = PbcBuddyListLoader.getSingleton();
    }

//    @Override
//    public void addPbcDndBuddyTreeListener(IPbcDndBuddyTreeListener listener) {
//        if (dndTree != null){
//            dndTree.addPbcDndBuddyTreeListener(listener);
//        }
//    }
//
//    @Override
//    public void removePbcDndBuddyTreeListener(IPbcDndBuddyTreeListener listener) {
//        if (dndTree != null){
//            dndTree.removePbcDndBuddyTreeListener(listener);
//        }
//    }
    
    abstract IPbcDndBuddyTree createDndBuddyTree();

    abstract void popupBuddyListMenu(JTree jDndTree, MouseEvent e);

    @Override
    public void pbcLoginUserPricerBuddyClicked() {
        IGatewayConnectorBuddy pbimLoginUser = getMasterLoginUser();
        if ((pbimLoginUser != null) && (GatewayServerType.PBIM_SERVER_TYPE.equals(pbimLoginUser.getIMServerType()))){
            IGatewayConnectorGroup group = getDndBuddyTree().retrieveGroupOfBuddy(pbimLoginUser);
            if (group != null){
                this.fireBuddyClickedEvent(group, pbimLoginUser);
            }
        }
    }

    @Override
    public void releaseBuddyListPanel() {
        buddyListLoader.stopBuddyListLoader();
    }
    
    @Override
    public IPbcDndBuddyTree getDndBuddyTree(){
        if (dndTree == null){
            dndTree = createDndBuddyTree();
//            final JTree jDndTree = dndTree.getBaseTree();
            dndTree.getBaseTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            dndTree.getBaseTree().addTreeSelectionListener(new TreeSelectionListener(){
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                }
            });
            dndTree.getBaseTree().addMouseListener (
                new MouseAdapter () {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        maybeShowPopup(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        maybeShowPopup(e);
                    }

                    private void maybeShowPopup(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            if(!BuddyStatus.Online.equals(getMasterLoginUser().getBuddyStatus())){
                                JOptionPane.showMessageDialog(getTalker().getPointBoxFrame(), 
                                        "You need to log into " + getMasterLoginUser().getIMScreenName() + " of " + getMasterLoginUser().getIMServerType() + " before this operation.");
                                return;
                              }
                            popupBuddyListMenu(dndTree.getBaseTree(), e);
                        }
                    }

                    @Override
                    public void mouseClicked(MouseEvent e){
                        TreePath treePath = dndTree.getBaseTree().getPathForLocation(e.getX(), e.getY());
                        if (treePath == null){
                            return;
                        }
                        DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();
                        if (e.getClickCount() >= 2){
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)(dndTree.getBaseTree().getLastSelectedPathComponent());
                            processTreeNodeSelection (node);
                        }else{
                            if (clickedNode instanceof IDnDGroupTreeNode){
                                if (((IDnDGroupTreeNode)clickedNode).isBoxChecked()){
                                    ((IDnDGroupTreeNode)clickedNode).setBoxChecked(false);
                                }else{
                                    ((IDnDGroupTreeNode)clickedNode).setBoxChecked(true);
                                }
                            }
                        }
                        if (clickedNode instanceof IDnDGroupTreeNode){
                        }else if (clickedNode instanceof IDnDBuddyTreeNode){
                        }else{
                            //root keeps expanded
                            dndTree.getBaseTree().expandPath(treePath);
                        }
                        if(!(AbstractBuddyListPanel.this instanceof RegularBuddyListPanel)){
                            dndTree.getBaseTree().expandPath(treePath);   //force distribution-list "group-level nodes" always expanded when double click the group node
                        }
                        dndTree.getBaseTree().updateUI();
                    }
                });
        }
        return dndTree;
    }
    
    /**
     * @return 
     */
    abstract PointBoxConnectorID getDistListOwnerID();
    
    @Override
    public boolean checkBuddyListSettingsIdentity(PbcBuddyListSettings pbcBuddyListSettings) {
        if (pbcBuddyListSettings == null){
            return  false;
        }else{
            if (getBuddyListType().equalsIgnoreCase(pbcBuddyListSettings.getBuddyListType())){
//                pbcBuddyListSettings.setBuddyListName(getDistListName());
                return this.getDistListName().equalsIgnoreCase(pbcBuddyListSettings.getBuddyListName());
            }else{
                return false;
            }
        }
    }
    
    /**
     * if nothing there, NULL will be returned
     * @return  
     */
    @Override
    public PbcBuddyListSettings constructPbcBuddyListSettings() {
        PbcBuddyListSettings aPbcBuddyListSettings = getDndBuddyTree().constructPbcBuddyListSettings();
        aPbcBuddyListSettings.setBuddyListName(getDistListName());
        aPbcBuddyListSettings.setSessionOwner(getKernel().getPointBoxAccountID());
        aPbcBuddyListSettings.setConnectorOwner(getDistListOwnerID());
        aPbcBuddyListSettings.setBuddyListType(getBuddyListType());
        return aPbcBuddyListSettings;
    }

    @Override
    public void addNewBuddyIntoDistGroup(final IGatewayConnectorBuddy buddy, final IGatewayConnectorGroup distGroup) {
        if ((buddy == null)||(distGroup == null)){
            return;
        }
        getDndBuddyTree().loadDistributionGroupWithBuddyNode(distGroup, buddy);
    }
    
    @Override
    public void deleteBuddyFromDistGroups(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            getDndBuddyTree().removeAssociatedBuddyNodes(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    getDndBuddyTree().removeAssociatedBuddyNodes(buddy);
                }
            });
        }
    }

    @Override
    public boolean isGroupExisted(String groupName) {
        //check some reserved keywords
        if (PbcReservedTerms.DefaultPitsLikeFrameTitle.toString().equalsIgnoreCase(groupName)){
            return true;
        }
        return getDndBuddyTree().isDistGroupExisted(groupName);
    }

    @Override
    public boolean isBuddyExisted(IGatewayConnectorBuddy buddy) {
        return getDndBuddyTree().isBuddyNodeLoaded(buddy);
    }

    @Override
    public ArrayList<IGatewayConnectorGroup> getAssociatedDistributionGroups(IGatewayConnectorBuddy buddy) {
        return getDndBuddyTree().retrieveAssociatedGroupsOfBuddy(buddy);
    }

    @Override
    public void sortBuddyListFromA2Z(boolean isPersistentRequired) {
        getDndBuddyTree().sortFromA2Z(isPersistentRequired);
    }

    @Override
    public void sortBuddyGroupFromA2Z(IDnDGroupTreeNode focusGroupNode, boolean isPersistentRequired) {
        getDndBuddyTree().sortFromA2Z(focusGroupNode, isPersistentRequired);
    }

    @Override
    public void sortBuddyListFromZ2A(boolean isPersistentRequired) {
        getDndBuddyTree().sortFromZ2A(isPersistentRequired);
    }

    @Override
    public void sortBuddyGroupFromZ2A(IDnDGroupTreeNode focusGroupNode, boolean isPersistentRequired) {
        getDndBuddyTree().sortFromZ2A(focusGroupNode, isPersistentRequired);
    }
    
    @Override
    public IPbcTalker getTalker() {
        return talker;
    }
    
    final IPbcKernel getKernel(){
        return talker.getKernel();
    }
    
    final IPbconsoleImageSettings getImageSettings(){
        return getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
    }

    @Override
    public JPanel getBasePanel() {
        return this;
    }
    
    /**
     * define whether or not display offline buddies
     * @return 
     */
    abstract boolean isOfflineDisplayed();

    @Override
    public void addBuddyListPanelListener(IBuddyListEventListener listener) {
        synchronized(buddyListPanelListeners){
            if (!buddyListPanelListeners.contains(listener)){
                buddyListPanelListeners.add(listener);
            }
        }
    }

    @Override
    public void removeBuddyListPanelListener(IBuddyListEventListener listener) {
        synchronized(buddyListPanelListeners){
            buddyListPanelListeners.remove(listener);
        }
    }

    void fireBuddyClickedEvent(IGatewayConnectorGroup group, IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        IGatewayConnectorBuddy masterLoginUser = buddy.getLoginOwner();
        synchronized(buddyListPanelListeners){
            if (masterLoginUser != null){
                for (int i = 0; i < buddyListPanelListeners.size(); i++){
                    buddyListPanelListeners.get(i).buddyTreeNodeClickedEventHappened(masterLoginUser, buddy);
                }
            }
        }
    }
    
    @Override
    public void mimicfireBuddyClickedEvent(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        IGatewayConnectorBuddy masterLoginUser = buddy.getLoginOwner();
        synchronized(buddyListPanelListeners){
            if (masterLoginUser != null){
                for (int i = 0; i < buddyListPanelListeners.size(); i++){
                    buddyListPanelListeners.get(i).buddyTreeNodeClickedEventHappened(masterLoginUser, buddy);
                }
            }
        }
    }

    void fireTreeRootClickedEvent() {
        synchronized(buddyListPanelListeners){
            if(this instanceof IPitsCastGroupListPanel){
                for (int i = 0; i < buddyListPanelListeners.size(); i++){
                    buddyListPanelListeners.get(i).pitsCastRootTreeNodeClickedEventHappened();
                }//for
            }else {
                //do nothing
            }//if
        }//synchronized
    }

    void fireGroupClickedEvent(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        fireGroupClickedEvent(group, members, null, false);
    }
    
    void fireGroupClickedEvent(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members, String message, boolean isAutoSend) {
        synchronized(buddyListPanelListeners){
            if ((members != null) && (members.size() > 0)){
                if(this instanceof IPitsCastGroupListPanel){
                    for (int i = 0; i < buddyListPanelListeners.size(); i++){
                        buddyListPanelListeners.get(i).pitsCastGroupTreeNodeClickedEventHappened(getTalker().getPointBoxLoginUser(), group, members, message, isAutoSend);
                    }//for
                }else if(this instanceof IPitsGroupListPanel){
                    for (int i = 0; i < buddyListPanelListeners.size(); i++){
                        buddyListPanelListeners.get(i).pitsGroupTreeNodeClickedEventHappened(getTalker().getPointBoxLoginUser(), group, members, message, isAutoSend);
                    }//for
                }else {
                    for (int i = 0; i < buddyListPanelListeners.size(); i++){
                        buddyListPanelListeners.get(i).groupTreeNodeClickedEventHappened(getTalker().getPointBoxLoginUser(), group, members, message, isAutoSend);
                    }//for
                }//if
            }//if
        }//synchronized
    }
    
    @Override
    public void mimicfireGroupClickedEvent(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        synchronized(buddyListPanelListeners){
            if ((members != null) && (members.size() > 0)){
                for (int i = 0; i < buddyListPanelListeners.size(); i++){
                    buddyListPanelListeners.get(i).groupTreeNodeClickedEventHappened(getTalker().getPointBoxLoginUser(), group, members);
                }
            }
        }
    }

    @Override
    public void buddyContentChanged(IGatewayConnectorBuddy buddy){
    }

    @Override
    public void refreshPanel() {
        getDndBuddyTree().refreshDndBuddyTree(null);
    }

    @Override
    public void expandListPanel() {
        if (SwingUtilities.isEventDispatchThread()){
            getDndBuddyTree().expandDndTreeModel();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    getDndBuddyTree().expandDndTreeModel();
                }
            });
        }
    }

    /**
     * 
     * @return - null is possible
     */
    @Override
    public Object getCurrentSelectedUserObject() {
        Object obj = getDndBuddyTree().getBaseTree().getLastSelectedPathComponent();
        if (obj instanceof DefaultMutableTreeNode) {
            return ((DefaultMutableTreeNode)obj).getUserObject();
        }else{
            return null;
        }
    }
    
//////    @Override
//////    public void handleBuddyListChangedEvent() {
//////        if (buddyListChangedEventHandler == null){
//////            return;
//////        }
//////        buddyListChangedEventHandler.bufferCommands(BuddyListChangedEventHandler.command);
//////    }

    @Override
    public void displayRemoveBuddyDialog(final IGatewayConnectorBuddy buddy) {
        if (SwingUtilities.isEventDispatchThread()){
            displayRemoveBuddyDialogHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayRemoveBuddyDialogHelper(buddy);
                }
            });
        }
    }
    
    private void displayRemoveBuddyDialogHelper(IGatewayConnectorBuddy buddy){
        ArrayList<IGatewayConnectorGroup> tagertGroups = getDndBuddyTree().retrieveAssociatedGroupsOfBuddy(buddy);
        IGatewayConnectorGroup tagertGroup;
        if ((tagertGroups == null) || (tagertGroups.isEmpty())){
            tagertGroup = null;
        }else{
            tagertGroup = tagertGroups.get(0);
        }
        BuddyListBuddyEditor dialog = BuddyListBuddyEditor.createBuddyListBuddyEditorInstance(getTalker(), this, buddy, tagertGroup, Purpose.DeleteBuddy, true);
        
        dialog.pack();
        
        dialog.setVisible(true);
        dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getTalker().getPointBoxFrame(), dialog));
    }

    @Override
    public void displayEditGroupDialog(final IGatewayConnectorGroup group) {
        if (SwingUtilities.isEventDispatchThread()){
            displayEditGroupDialogHelper(group);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayEditGroupDialogHelper(group);
                }
            });
        }
    }

    private void displayEditGroupDialogHelper(final IGatewayConnectorGroup group) {
        if (group == null){
            return;
        }
        //Special logic for protecting default buddy group (i.e. PbcReservedTerms.PbBuddyDefaultGroup) on buddy list
        if (PbcReservedTerms.PbBuddyDefaultGroup.toString().equalsIgnoreCase(group.getGroupName())){
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), 
                                          PbcReservedTerms.PbBuddyDefaultGroup.toString() 
                    + " is default group which cannot be directly modified or deleted."
                    + " You may modify it by moving its members to a new or other existing groups.");
            return;
        }
        BuddyListGroupEditor dialog = new BuddyListGroupEditor(getTalker(),
                                                                 this,
                                                                 true,
                                                                 BuddyListGroupEditor.Purpose.EditGroup);
        dialog.displayDialog(group, getDndBuddyTree().retrieveBuddiesOfGroup(group));
        dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getTalker().getPointBoxFrame(), dialog));
    }

    @Override
    public void displayRemoveGroupDialog(final IGatewayConnectorGroup group) {
        if (SwingUtilities.isEventDispatchThread()){
            displayRemoveGroupDialogHelper(group);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayRemoveGroupDialogHelper(group);
                }
            });
        }
    }
    
    private void displayRemoveGroupDialogHelper(IGatewayConnectorGroup group){
        if (group == null){
            return;
        }
        //Special logic for protecting default buddy group (i.e. PbcReservedTerms.PbBuddyDefaultGroup) on buddy list
        if (PbcReservedTerms.PbBuddyDefaultGroup.toString().equalsIgnoreCase(group.getGroupName())){
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), 
                                          PbcReservedTerms.PbBuddyDefaultGroup.toString() 
                    + " is default group which cannot be directly modified or deleted."
                    + " You may modify it by moving its members to a new or other existing groups.");
            return;
        }
        BuddyListGroupEditor dialog = new BuddyListGroupEditor(getTalker(),
                                                                 this,
                                                                 true,
                                                                 BuddyListGroupEditor.Purpose.DeleteGroup);
        dialog.displayDialog(group, getDndBuddyTree().retrieveBuddiesOfGroup(group));
        dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getTalker().getPointBoxFrame(), dialog));
    }
    
    @Override
    public void displayAddNewGroupDialog() {
        if (SwingUtilities.isEventDispatchThread()){
            displayAddNewGroupDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayAddNewGroupDialogHelper();
                }
            });
        }
    }

    void displayAddNewGroupDialogHelper(){
        BuddyListGroupEditor dialog = new BuddyListGroupEditor(getTalker(),
                                                                     this,
                                                                     true,
                                                                     BuddyListGroupEditor.Purpose.AddGroup);
        dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getTalker().getPointBoxFrame(), dialog));
        dialog.displayDialog(null, null);
    }
    
    @Override
    public void displayAddNewBuddyDialog(final ITalkerBuddyGroupDialogCustomizer customizer) {
        if (SwingUtilities.isEventDispatchThread()){
            displayAddNewBuddyDialogHelper(customizer);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayAddNewBuddyDialogHelper(customizer);
                }
            });
        }
    }

    private void displayAddNewBuddyDialogHelper(ITalkerBuddyGroupDialogCustomizer customizer) {
        BuddyListBuddyEditor dialog = BuddyListBuddyEditor.createBuddyListBuddyEditorInstance(getTalker(), this, null, null, Purpose.AddBuddy, true);
        ArrayList<IGatewayConnectorGroup> groups = getDndBuddyTree().retrieveAllGroups(true);
        for (IGatewayConnectorGroup group : groups){
            dialog.addAvailableDistGroup(group);
        }
        
        dialog.pack();
        
        dialog.setVisible(true);
        
        dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getTalker().getPointBoxFrame(), dialog));
    }

    @Override
    public void displayChangeBuddyGroupDialog(final IGatewayConnectorBuddy buddy) {
        if (SwingUtilities.isEventDispatchThread()){
            displayChangeBuddyGroupDialogHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayChangeBuddyGroupDialogHelper(buddy);
                }
            });
        }
    }
    
    private void displayChangeBuddyGroupDialogHelper(final IGatewayConnectorBuddy buddy) {
        ArrayList<IGatewayConnectorGroup> tagertGroups = getDndBuddyTree().retrieveAssociatedGroupsOfBuddy(buddy);
        IGatewayConnectorGroup tagertGroup;
        if ((tagertGroups == null) || (tagertGroups.isEmpty())){
            tagertGroup = null;
        }else{
            tagertGroup = tagertGroups.get(0);
        }
        BuddyListBuddyEditor dialog = BuddyListBuddyEditor.createBuddyListBuddyEditorInstance(getTalker(), this, buddy, 
                                            tagertGroup, Purpose.MoveBuddy, true);
        ArrayList<IGatewayConnectorGroup> groups = getDndBuddyTree().retrieveAllGroups(true);
        for (IGatewayConnectorGroup group : groups){
            dialog.addAvailableDistGroup(group);
        }
        
        dialog.pack();
        
        dialog.setVisible(true);
        
        dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getTalker().getPointBoxFrame(), dialog));
    }

    @Override
    public void displayRenameGroupDialog(final IGatewayConnectorGroup group) {
        if (SwingUtilities.isEventDispatchThread()){
            displayRenameGroupDialogHelper(group);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayRenameGroupDialogHelper(group);
                }
            });
        }
    }

    private void displayRenameGroupDialogHelper(final IGatewayConnectorGroup groupWithOldName) {
        //Special logic for protecting default buddy group (i.e. PbcReservedTerms.PbBuddyDefaultGroup) on buddy list
        if (PbcReservedTerms.PbBuddyDefaultGroup.toString().equalsIgnoreCase(groupWithOldName.getGroupName())){
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), 
                                          PbcReservedTerms.PbBuddyDefaultGroup.toString() 
                    + " is default group which cannot be directly modified or deleted."
                    + " You may modify it by moving its members to a new or other existing groups.");
            return;
        }
        
        final String newGroupName = JOptionPane.showInputDialog(
                talker.getKernel().getPointBoxMainFrame(), 
                "Old group name: " + groupWithOldName.getGroupName(), 
                "Rename Group", JOptionPane.INFORMATION_MESSAGE);
        if(newGroupName==null){
            //do nothing
        }else if (DataGlobal.isEmptyNullString(newGroupName)){
            JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                          "You may not give an empty name for the selected group");
        }else if(!PbcGlobal.isLegalInput(newGroupName)){
                        JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                          "Only alphabetic characters and digit numbers are legal for group name!");
        }else{
            String oldGroupName = groupWithOldName.getGroupName();
            if (!newGroupName.equalsIgnoreCase(oldGroupName)){
                //For DIST, use talker to check their name; For regular, check name in its own panel
                if (((this instanceof DistributionBuddyListPanel) && (talker.checkGroupNameRedundancy(newGroupName)))
                       || this.isGroupExisted(newGroupName)){                   
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  "The group name has been used as a distribution, PITS or conference. Please give another new one.");
                }else if(PbcReservedTerms.PbBuddyDefaultGroup.toString().equalsIgnoreCase(newGroupName)){
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  "Friends is an reserved-word of PBC. Please give another new one.");
                }else{
                    final JFrame frame = talker.findPitsLikeGroupFloatingFrame(groupWithOldName);
                    getDndBuddyTree().renameDnDGroupTreeNode(groupWithOldName, newGroupName);
                    talker.updatePitsCastCheckTree();
                    if (frame != null){
                        if (SwingUtilities.isEventDispatchThread()){
                            frame.setTitle(newGroupName);
                        }else{
                            SwingUtilities.invokeLater(new Runnable(){
                                @Override
                                public void run() {
                                    frame.setTitle(newGroupName);
                                }
                            });
                        }
                    }
                    talker.renamePitsCastGroupInMasterFloatingFrame(groupWithOldName, newGroupName);
                    refreshPanel();
                }
            }
        }
    }

    /**
     * Fire events for buddy-clicked or group-clicked
     * @param node 
     */
    void processTreeNodeSelection (DefaultMutableTreeNode node ) {
       if (node == null){
           //Nothing is selected.
           return;
       }
       if (node instanceof IDnDBuddyTreeNode) {
           fireBuddyClickedEvent(((IDnDGroupTreeNode)node.getParent()).getGatewayConnectorGroup(), 
                                 ((IDnDBuddyTreeNode)node).getGatewayConnectorBuddy());
       }else if (node instanceof IDnDGroupTreeNode) {
           if(!(this instanceof RegularBuddyListPanel))
                fireGroupClickedEvent(((IDnDGroupTreeNode)node).getGatewayConnectorGroup(),
                                 getDndBuddyTree().retrieveBuddiesOfGroupNode(((IDnDGroupTreeNode)node)));
       }else if (node instanceof DnDMutableHybridTreeRoot){
           fireTreeRootClickedEvent();
       }
    }
    
    /**
     * This implementation has no offline group. The paramter onlineRequired has no effects.
     * @param onlineRequired - ignored in this method
     * @return 
     */
    @Override
    public ArrayList<IGatewayConnectorGroup> getAllGroups(boolean onlineRequired, boolean sortByUniqueName) {
        return getDndBuddyTree().retrieveAllGroups(sortByUniqueName);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getAllBuddies(boolean sort) {
        return getDndBuddyTree().retrieveAllBuddies(sort);
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfGroups(ArrayList<IGatewayConnectorGroup> groups) {
        return getDndBuddyTree().retrieveBuddiesOfGroups(groups);
    }

    /**
     * This method expects that Parameter group should be IPointBoxDistributionGroup
     * @param group
     * @return 
     */
    @Override
    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfGroup(IGatewayConnectorGroup group) {
        return getDndBuddyTree().retrieveBuddiesOfGroup(group);
    }

    /**
     * loginUser just successfully log into the system.
     * @param loginUser 
     */
    @Override
    public void handleConnectorConnectedEvent(final IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            return;
        }
        /**
         * DOES NOT STTART HERE. Instead, it is started at handleBuddyItemPresentedEvent 
         * and handleBuddyStatusChangedEventInBatch
         */
        //buddyListLoader.startBuddyListLoading(loginUser, getDndBuddyTree());
        if (SwingUtilities.isEventDispatchThread()){
            handleConnectorConnectedEventHelper(loginUser);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleConnectorConnectedEventHelper(loginUser);
                }
            });
        }
    }
    
    abstract void handleConnectorConnectedEventHelper(IGatewayConnectorBuddy loginUser);

    @Override
    public void handleConnectorDisconnectedEvent(final IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            return;
        }
        //in case buddyListLoader is still working for this loginUser 
        buddyListLoader.stopBuddyListLoading(loginUser, this);
        (new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                ArrayList<IGatewayConnectorBuddy> buddies = GatewayBuddyListFactory.retrieveBuddiesOfLoginUser(loginUser);
                for (IGatewayConnectorBuddy buddy : buddies){
                    buddy.setBuddyStatus(BuddyStatus.Offline);
                }
                return null;
            }

            @Override
            protected void done() {
                handleConnectorDisconnectedEventHelper(loginUser);
            }
        }).execute();
    }
    
    abstract void handleConnectorDisconnectedEventHelper(final IGatewayConnectorBuddy loginUser);

    @Override
    public void handleBuddyItemRemovedEvent(final IGatewayConnectorBuddy buddy) {
        if (SwingUtilities.isEventDispatchThread()){
            getDndBuddyTree().removeAssociatedBuddyNodes(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    getDndBuddyTree().removeAssociatedBuddyNodes(buddy);
                }
            });
        }
    }

    /**
     * This method will handle BuddyItemPresentedEvent are raised from the server-side (not client-side, 
     * e.g. ChangeBuddyGroupEvent). This method may ignore "group" of the buddy instance 
     * if the buddy has been existed in the panel. if buddy status is UNKNOWN, it will be ignored also 
     * because it means server-side does not know the status either.
     * 
     * @param gatewayConnectorBuddy
     */
    @Override
    public void handleBuddyItemPresentedEvent(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (!buddyListLoader.bufferBuddyItemPresentedEventBuddy(buddy, this)){
            //buddyListLoader refused to work for it. It means this event is in the case (2) 
            logger.log(Level.INFO, "handleBuddyItemPresentedEvent >>> buddy ...{0}", buddy.getIMUniqueName());
            getDndBuddyTree().handleBuddyItemPresentedEvent(buddy);
        }
    }
    
    /**
     * This method will handle BuddyStatusChangedEvent which are raised from the server-side (not client-side, 
     * e.g. ChangeBuddyGroupEvent). This method may ignore "group information" in the buddy instance if such buddy 
     * has been existed in the panel.
     * 
     * @param gatewayConnectorBuddy
     */
    @Override
    public void handleBuddyStatusChangedEvent(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (!buddyListLoader.bufferBuddyStatusChangedEventBuddy(buddy, this)){
            logger.log(Level.INFO, "handleBuddyStatusChangedEvent >>> buddy ...{0}", buddy.getLoginOwner().getIMUniqueName());
            getDndBuddyTree().handleBuddyStatusChangedEvent(buddy);
        }
    }

    @Override
    public void handleBuddySubscriptionEvent(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            handleBuddySubscriptionEventHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleBuddySubscriptionEventHelper(buddy);
                }
            });
        }
    }
      
    private void handleBuddySubscriptionEventHelper(final IGatewayConnectorBuddy buddy) {
        getDndBuddyTree().handleBuddySubscriptionEvent(buddy);
    }
    
    @Override
    public void handleBuddyUnsubscriptionEvent(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            handleBuddyUnsubscriptionEventHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleBuddyUnsubscriptionEventHelper(buddy);
                }
            });
        }
    }
    
    private void handleBuddyUnsubscriptionEventHelper(final IGatewayConnectorBuddy buddy) {
        IPbcKernel kernel = this.getKernel();
        String msg = buddy.getIMScreenName() + " just removed " + kernel.getPointBoxLoginUser().getIMScreenName() + " from his/her buddy list.";
        kernel.updateSplashScreen(msg, Level.INFO, 1000);
        getDndBuddyTree().removeAssociatedBuddyNodes(buddy);
        handleBuddyStatusChangedEvent(buddy);
    }

    @Override
    public void highlightGatewayConnectorGroup(final IGatewayConnectorGroup distGroup) {
        getDndBuddyTree().expandDndTreeForGroup(distGroup);
    }

    /**
     * Highlight clickedBuddy on this panel
     * @param clickedBuddy 
     */
    @Override
    public void highlightGatewayConnectorBuddy(final IGatewayConnectorBuddy clickedBuddy) {
        getDndBuddyTree().expandDndTreeForBuddy(clickedBuddy, true);
    }

    /**
     * 
     * @param possibleNewBuddy
     * @return 
     */
    @Override
    public IGatewayConnectorBuddy confirmBuddyPresentedInList(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return null;
        }
        if (getDndBuddyTree().isBuddyNodeDisplayed(buddy)){
            return buddy;
        }else{
            return null;
        }
    }

    /**
     * Add a new group node in the buddy list tree, which contains buddy nodes for every member
     * @param group
     * @param members 
     */
    @Override
    public void addNewDistributionGroup(final IGatewayConnectorGroup group, final List<IGatewayConnectorBuddy> members){
        if (group == null){
            return;
        }
        getDndBuddyTree().loadDistributionGroupWithBuddyNodes(group, members);
    }

    @Override
    public void editDistributionGroup(final IGatewayConnectorGroup group, final ArrayList<IGatewayConnectorBuddy> members) {
        if (group == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            editDistributionGroupHelper(group, members);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    editDistributionGroupHelper(group, members);
                }
            });
        }
    }
    
    private void editDistributionGroupHelper(final IGatewayConnectorGroup group, final ArrayList<IGatewayConnectorBuddy> members){
        deleteDistributionGroup(group);
        addNewDistributionGroup(group, members);
        talker.getMessagingPaneManager().updateDistributionGroupMembers(group, members);
    }
    
    @Override
    public void deleteDistributionGroup(final IGatewayConnectorGroup group) {
        if (SwingUtilities.isEventDispatchThread()){
            deleteDistributionGroupHelper(group);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    deleteDistributionGroupHelper(group);
                }
            });
        }
    }
    
    protected void storeShowOfflineOption(boolean isShowOffline){
        try{
            PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
            if (getKernel().getPointBoxLoginUser() != null){
                prop.storeShowOfflineOption(isShowOffline, getKernel().getPointBoxLoginUser().getIMUniqueName());
            }
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }           
    }    
    
    /**
     * @param group 
     */
    private void deleteDistributionGroupHelper(IGatewayConnectorGroup group){
        getDndBuddyTree().removeGroupNode(group);
        getDndBuddyTree().getBaseTree().updateUI();
        getDndBuddyTree().expandDndTreeModel();
    }

    @Override
    public void acceptPossibleNewBuddy(final IGatewayConnectorBuddy possibleNewBuddy, final INewBuddyGroupDialogListener listener) {
        if (SwingUtilities.isEventDispatchThread()){
            acceptPossibleNewBuddyHelper(possibleNewBuddy, listener);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    acceptPossibleNewBuddyHelper(possibleNewBuddy, listener);
                }
            });
        }
    }

    private void acceptPossibleNewBuddyHelper(IGatewayConnectorBuddy possibleNewBuddy, INewBuddyGroupDialogListener listener) {
        if ((possibleNewBuddy.getLoginOwner() != null) 
                && (!possibleNewBuddy.getLoginOwner().getIMUniqueName().equalsIgnoreCase(possibleNewBuddy.getIMUniqueName()))
                && (BuddyStatus.Online.equals(possibleNewBuddy.getLoginOwner().getBuddyStatus())))
        {
            ConfirmNewBuddyDialog dialog = ConfirmNewBuddyDialog.getSingleton(
                    null, false, this.getDndBuddyTree(), possibleNewBuddy.getLoginOwner(), 
                    possibleNewBuddy, getDndBuddyTree().retrieveAllGroups(true), ConfirmNewBuddyDialogType.AcceptBuddy);
            dialog.addNewBuddyGroupDialogListener(listener);
            dialog.setVisible(true);
            dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getTalker().getPointBoxFrame(), dialog));
        }
    }

    @Override
    public void setRealTimePersistentRequired(boolean b) {
        /**
         * Turn on flag for real-time persistent request
         */
        getDndBuddyTree().setRealTimePersistentRequired(true);
    }
    
    @Override
    public void populatePbcBuddyListSettings(final PointBoxConnectorID ownerID, final PbcBuddyListSettings pbcBuddyListSettings) {
        if (ownerID == null){
            return;
        }
        if (pbcBuddyListSettings == null){
            return;
        }
        //IGatewayConnectorBuddy buddy = GatewayBuddyListFactory.get,
        (new SwingWorker<Void, BuddyListGroupItem>(){
            @Override
            protected Void doInBackground() throws Exception {
                getKernel().raisePointBoxEvent(new ServerLoginStatusEvent(PointBoxEventTarget.PbcFace,
                                                                            "Loading a buddy list..." + pbcBuddyListSettings.getBuddyListName()+"...",
                                                                            GatewayServerType.PBIM_SERVER_TYPE));
                BuddyListGroupItem[] groupItems = sortGroupItems(pbcBuddyListSettings.getGroupItems());
                if ((groupItems != null) && (groupItems.length > 0)){
                    BuddyListGroupItem groupItem;
                    String changeFriendsGroupStr;
                    if (AbstractBuddyListPanel.this instanceof PitsCastGroupListPanel){
                        changeFriendsGroupStr = PbcBuddyListPanelTabName.PITS_CAST.toString();
                    }else if (AbstractBuddyListPanel.this instanceof DistributionBuddyListPanel){
                        changeFriendsGroupStr = PbcBuddyListPanelTabName.DISTRIBUTION.toString();
                    }else{
                        changeFriendsGroupStr = null;
                    }
                    for (int i = 0; i < groupItems.length; i++){
                        groupItem = groupItems[i];
                        if ((groupItem != null) && (DataGlobal.isNonEmptyNullString(groupItem.getGroupName()))){
                            if (changeFriendsGroupStr != null){
                                if (PbcReservedTerms.PbBuddyDefaultGroup.toString().equalsIgnoreCase(groupItem.getGroupName())){
                                    groupItem.setGroupName(changeFriendsGroupStr+"_"+PbcReservedTerms.PbBuddyDefaultGroup.toString());
                                    getDndBuddyTree().setRealTimePersistentRequired(true);
                                }
                            }//if
                            publish(groupItem);
                        }
                    }//groupItems
                }
                /**
                 * Turn on flag for real-time persistent request
                 */
                getDndBuddyTree().setRealTimePersistentRequired(true);
                return null;
            }

            @Override
            protected void process(List<BuddyListGroupItem> chunks) {
                for (BuddyListGroupItem groupItem : chunks){
                    getDndBuddyTree().loadDistributionGroupWithBuddyNodes(ownerID, groupItem, sortBuddyItems(groupItem.getBuddyItems()));
                }//for: next chunk
            }

            @Override
            protected void done() {
                getDndBuddyTree().expandDndTreeModel();
                /**
                 * PITS is permitted to be expandable. But its initial state is collapsed on the secondary group level.
                 * Thus, after getDndBuddyTree().expandDndTreeModel() is called, permit users to click group node to expand 
                 * to see member nodes.
                 */
                getDndBuddyTree().enableSecondaryNodeExpanded();
            }

            private BuddyListGroupItem[] sortGroupItems(BuddyListGroupItem[] groupItems) {
                if (groupItems == null){
                    return null;
                }
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
                if (buddyItems != null){
                    for (int i = 0; i < buddyItems.length; i++){
                        unsortedBuddyItems.add(buddyItems[i]);
                        if (buddyItems[i].getBuddyIndex() > 0){
                            alphabetic = false;
                        }
                    }//for
                }
                if (alphabetic){
                    return PbcGlobal.sortBuddyItemsAlphabetically(unsortedBuddyItems);
                }else{
                    return PbcGlobal.sortBuddyItemsByIndex(unsortedBuddyItems);
                }
            }

        }).execute();
    }
    
    protected void initSearchAutoFill(final JTextField jSearchField){
        searchTableModel = new DefaultTableModel();
        //initTableModel();

        rowSorter = new TableRowSorter<DefaultTableModel>(searchTableModel);
        searchTable = new JTable(searchTableModel);
        searchTable.setRowSorter(rowSorter);
        searchTable.setFillsViewportHeight(true);
        searchTable.getColumnModel().setColumnSelectionAllowed(false);
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        searchTable.getTableHeader().setReorderingAllowed(false);

        searchTable.setGridColor(Color.WHITE);
        this.jSearchField = jSearchField;
        jSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                    showPopup(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                    showPopup(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                    showPopup(e);
            }
        });

        jSearchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                switch(code)
                {
                    case KeyEvent.VK_UP:
                    {
                            cycleTableSelectionUp();
                            break;
                    }

                    case KeyEvent.VK_DOWN:
                    {
                            cycleTableSelectionDown();
                            break;
                    }

                    case KeyEvent.VK_LEFT:
                    {
                            //Do whatever you want here
                            break;
                    }

                    case KeyEvent.VK_RIGHT:
                    {
                            //Do whatever you want here
                            break;
                    }

                    case KeyEvent.VK_ENTER:
                    {
                        chooseInEDT();
                        break;
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });

        KeyStroke keyStroke = KeyStroke.getKeyStroke("ESCAPE");
        jSearchField.getInputMap().put(keyStroke, "ESCAPE");
        jSearchField.getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    //Do what you wish here with the escape key.
            }
        });
        jSearchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initTableModelInEDT();
                jSearchField.setText("");
                jSearchField.setForeground(Color.BLACK);
                searchTable.setPreferredSize(new Dimension((int)jSearchField.getSize().getWidth(),80));
            }

        });

        searchTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent evt) {
                chooseInEDT(); 
            }
        });
        popup = new JPopupMenu();
        popup.add(searchTable);
        popup.setVisible(false);
        popup.setBorder(BorderFactory.createEmptyBorder());        
    }

    private void newFilter() {
        RowFilter<DefaultTableModel, Object> rf;
        try {
            rf = RowFilter.regexFilter(getFilterText(), 0);
        }
        catch(PatternSyntaxException e) {
            return;
        }
        rowSorter.setRowFilter(rf);
    }

    private String getFilterText() {
        String orig = jSearchField.getText();
        return "("+orig+")|("+orig.toLowerCase()+")|("+orig.toUpperCase()+")";
    }

    private void showPopup(DocumentEvent e) {
        if(e.getDocument().getLength() > 0) {
            if(!popup.isVisible()) { 
                Rectangle r = jSearchField.getBounds();
                popup.show(jSearchField, (r.x-4), (r.y+16));
                popup.setVisible(true);
            }

            newFilter();
            jSearchField.grabFocus();

        }
        else {
            popup.setVisible(false);
        }
    }

    private void chooseHelper(){
        IGatewayConnectorBuddy buddy;
        if(searchTable.getSelectedRow()>=0){
            Object obj = (searchTable.getValueAt(searchTable.getSelectedRow(), 0));
            if (obj instanceof IGatewayConnectorBuddy){
                buddy = (IGatewayConnectorBuddy)obj;
            }else{
                return;
            }
        }else{
            String buddyName = jSearchField.getText();
            if((buddy=isFound(buddyName))==null){
                JOptionPane.showMessageDialog(this, "nothing found!");
                comeToOriginal();
                return;
            }
        }
        talker.gatewayConnectorBuddyHighlighted(buddy,false);
        comeToOriginal();
        fireBuddyClickedEvent(null, buddy);
    }

    private void comeToOriginal(){
        jSearchField.setText("search buddy...");
        jSearchField.setForeground(Color.GRAY);
        popup.setVisible(false);
        grabFocus();
    }


    private IGatewayConnectorBuddy isFound(String buddyName){
        if (buddyName != null){
            ArrayList<IGatewayConnectorBuddy> buddies=getDndBuddyTree().retrieveAllBuddies(true); 
            for(IGatewayConnectorBuddy buddy:buddies){
                if((buddy.getIMScreenName().equalsIgnoreCase(buddyName)) || (buddyName.equalsIgnoreCase(buddy.getNickname()))){
                    return buddy;
                }
            }
        }
        return null;
    }

    private void chooseInEDT(){
        if(SwingUtilities.isEventDispatchThread()){
            chooseHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    chooseHelper();
                }
            });
        }
    }

    private void cycleTableSelectionUp() {
        ListSelectionModel selModel = searchTable.getSelectionModel();
        int index0 = selModel.getMinSelectionIndex();
        if(index0 > 0) {
            selModel.setSelectionInterval(index0-1, index0-1);
        }
    }

    private void cycleTableSelectionDown() {
        ListSelectionModel selModel = searchTable.getSelectionModel();
        int index0 = selModel.getMinSelectionIndex();
        if(index0 == -1) {
            selModel.setSelectionInterval(0, 0);
        }
        else if(index0 > -1) {
            selModel.setSelectionInterval(index0+1, index0+1);
        }
    }

    private void initTableModelInEDT(){
        if(SwingUtilities.isEventDispatchThread()){
        initTableModelHelper();
        }else{
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    initTableModelHelper();
                }
            });
         }
    }        

    private void initTableModelHelper(){
        new SwingWorker<Void, IGatewayConnectorBuddy>(){
            IGatewayConnectorBuddy[][] data;
            @Override
            protected Void doInBackground() throws Exception {               
                ArrayList<IGatewayConnectorBuddy> buddies=getDndBuddyTree().retrieveAllBuddies(true);
                data=new IGatewayConnectorBuddy[buddies.size()][1];
                //publish
                for (IGatewayConnectorBuddy buddy : buddies){
                    if(buddy!=null)
                       publish(buddy);
                 }
                return null;
            }

            @Override
            protected void process(List<IGatewayConnectorBuddy> chunks) {
                int i=0;
                for (IGatewayConnectorBuddy buddy : chunks){
                    data[i][0]=buddy;
                    i++;
                }
            }

            @Override
            protected void done() {
                String[] columns = new String[] {"A"};
 		searchTableModel.setDataVector(data, columns);
                searchTable.getColumnModel().getColumn(0).setCellRenderer(new CustomCellRenderer());
                 
            }
        }.execute();       
    }
    
    class CustomCellRenderer extends DefaultTableCellRenderer{
        
        Color originalLabelForeground;
        CustomCellRenderer() {
            originalLabelForeground = this.getBackground();
        }
        
       @Override
       public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column) 
       {
            IGatewayConnectorBuddy buddy=(IGatewayConnectorBuddy) value;
            Icon icon;
            if(buddy.getIMServerType().equals(GatewayServerType.AIM_SERVER_TYPE)){
                icon=talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getAimBuddyIcon();
            }else if(buddy.getIMServerType().equals(GatewayServerType.YIM_SERVER_TYPE)){
                icon=talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getYahooBuddyIcon();
            }else{
                icon=talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getPbimBuddyIcon();
            }
            setIcon(icon);
            if (DataGlobal.isEmptyNullString(buddy.getNickname())){
                setText(" " + buddy.getIMScreenName());
            }else{
                setText(" " + buddy.getIMScreenName() + " ("+buddy.getNickname()+")");
            }
           
            if(isSelected){
                setBackground(Color.GRAY);
            }else{
                setBackground(originalLabelForeground);
            }   
            return this;
       }
    }
}
