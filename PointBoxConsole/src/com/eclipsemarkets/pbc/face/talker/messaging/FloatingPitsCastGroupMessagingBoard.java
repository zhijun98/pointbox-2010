/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.util.JavaTextPaneWithBackgroundImage;
import com.eclipsemarkets.pbc.face.checktree.EmsCheckTreeFactory;
import com.eclipsemarkets.pbc.face.checktree.IBuddyCheckNode;
import com.eclipsemarkets.pbc.face.checktree.IEmsCheckNode;
import com.eclipsemarkets.pbc.face.checktree.IEmsCheckTreePanel;
import com.eclipsemarkets.pbc.face.checktree.IGroupCheckNode;
import com.eclipsemarkets.pbc.face.talker.IFloatingPitsCastGroupMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.face.talker.dist.PbcFloatingFrameTerms;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang, date & time: Dec 22, 2013 - 12:52:47 PM
 */
class FloatingPitsCastGroupMessagingBoard extends FloatingMessagingBoard implements IFloatingPitsCastGroupMessagingBoard{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(FloatingPitsCastGroupMessagingBoard.class.getName());
    }
    
    private final IEmsCheckTreePanel checkTreePanel; 
    
    private JScrollPane jListScrollPane2;
    
    /**
     * For PitsCast, "group" has non-sense since this board hold all the groups of PitsCast
     * @param talker
     * @param group
     * @param members 
     */
    FloatingPitsCastGroupMessagingBoard(IPbcTalker talker, 
                                        IGatewayConnectorGroup group, 
                                        ArrayList<IGatewayConnectorBuddy> members) {
        super(talker);
        setGroup(group);
        this.jMembesrPanel.setVisible(true);
        this.jSuperParentSplitPane.setDividerSize(10);
        
        checkTreePanel = EmsCheckTreeFactory.createEmsCheckTreePanelComponentInstance(PbcFloatingFrameTerms.PitsCastFrame.toString(), 
                                                                                      talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getConnectorBuddyIcon(GatewayServerType.PBIM_SERVER_TYPE),
                                                                                      true, getTalker().getKernel());
        jListScrollPane2 = new javax.swing.JScrollPane();
        jListScrollPane2.setName("jListScrollPane2");
        jListScrollPane2.setViewportView((JPanel)checkTreePanel);
        
        jMembesrPanel.remove(jListScrollPane);
        //((JPanel)checkTreePanel).setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jMembesrPanel.add(jListScrollPane2, java.awt.BorderLayout.CENTER);
        
        this.jMembesrPanel.setVisible(true);
        this.jSuperParentSplitPane.setDividerSize(10);
        this.jBroadCastScrollPane.setVisible(true);
        this.jParentSplitPane.setDividerSize(10);
        
        modifyBoardMenuBarForPitsCast();
        
        hideCheclAllBox();
    }
    
    @Override
    JTextPane createMessagingEntryControl() {
        //return new JavaTextPaneWithBackgroundImage(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getImageURL(PbcImageFileName.PitsCastToCurrentPng));
        return new JavaTextPaneWithBackgroundImage(null);
    }

    @Override
    public String getBoardId() {
        /**
         * This cannot be changed
         */
        return PbcFloatingFrameTerms.PitsCastFrame.toString();
    }

    /**
     * This method should be invoked only once for PITS-CAST in a PBC session.
     * @param autoSend 
     */
    void populatePitsCastCheckTreeModel(boolean autoSend) {
        updatePitsCastCheckTree();
        /**
         * The followings could be never used
         */
        if (autoSend){
            broadcastCurrentMessage();
        }
    }

    @Override
    public void updatePitsCastCheckTree() {
        if (SwingUtilities.isEventDispatchThread()){
            updatePitsCastCheckTreeHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updatePitsCastCheckTreeHelper();
                }
            });
        }
    }
    
    private void updatePitsCastCheckTreeHelper(){
        synchronized(checkTreePanel){
            IGatewayConnectorBuddy loginUser = talker.getPointBoxLoginUser();
            ArrayList<IGatewayConnectorGroup> existingGroupList = checkTreePanel.retrieveAllGroups();
            TreeSet<String> tabUniqueNames = new TreeSet<String>();
            if (existingGroupList != null){
                for (IGatewayConnectorGroup existingGroup : existingGroupList){
                    tabUniqueNames.add(existingGroup.getIMUniqueName());
                }
            }
            checkTreePanel.removeAllGroups();
            ArrayList<IGatewayConnectorGroup> pitsCastGroupList = getTalker().getPitsCastGroups();
            for (IGatewayConnectorGroup pitsCastGroup : pitsCastGroupList){
                ArrayList<IGatewayConnectorBuddy> buddyMembers = getTalker().getPitsCastBuddyListTreePanel().getBuddiesOfGroup(pitsCastGroup);
                for (IGatewayConnectorBuddy buddy : buddyMembers){
                    checkTreePanel.insertGroupBuddyNodePair(pitsCastGroup, buddy, true);
                }
                if (tabUniqueNames.contains(pitsCastGroup.getIMUniqueName())){
                    tabUniqueNames.remove(pitsCastGroup.getIMUniqueName());
                }else{
                    //a new group
                    this.presentMessagingTab(loginUser, pitsCastGroup, buddyMembers);
                }
            }
            //remove the old group which was removed
            for (String tabUniqueName : tabUniqueNames){
                this.hideButtonTabComponent(tabUniqueName);
            }
        }
    }

    @Override
    public void selectBuddyCheckNode(final IGatewayConnectorBuddy buddy) {
        if (SwingUtilities.isEventDispatchThread()){
            selectBuddyCheckNodeHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    selectBuddyCheckNodeHelper(buddy);
                }
            });
        }
    }
    
    private void selectBuddyCheckNodeHelper(final IGatewayConnectorBuddy buddy) {
        synchronized(checkTreePanel){
            checkTreePanel.selectBuddyCheckNode(buddy);
        }
    }

    @Override
    public void unselectBuddyCheckNode(final IGatewayConnectorBuddy buddy) {
        if (SwingUtilities.isEventDispatchThread()){
            unselectBuddyCheckNodeHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    unselectBuddyCheckNodeHelper(buddy);
                }
            });
        }
    }
    
    private void unselectBuddyCheckNodeHelper(final IGatewayConnectorBuddy buddy) {
        synchronized(checkTreePanel){
            checkTreePanel.unselectBuddyCheckNode(buddy);
        }
    }

    /**
     * This method is replaced by populatePitsCastCheckTreeModel
     * @param isAutoSend 
     */
    @Override
     protected void populateCheckTreeItems(final boolean isAutoSend) {
        //disable this method since it has completely different from DIST and PITS check-tree
    }

    /**
     * @param loginUser
     * @param buddy
     * @param talker 
     */
    @Override
    void updateBuddyStatus(final IGatewayConnectorBuddy loginUser, final IGatewayConnectorBuddy buddy, final IPbcTalker talker) {
        if (SwingUtilities.isEventDispatchThread()){
            jGroupMemberList.updateUI();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jGroupMemberList.updateUI();
                }
            });
        }
//        (new SwingWorker<ListModel, Void>(){
//            @Override
//            protected ListModel doInBackground() throws Exception {
//                ListModel currentModel = jGroupMemberList.getModel();
//                ListModel returnedModel = null;
//                IGatewayConnectorBuddy aBuddy;
//                FloatingFrameCheckBuddyItem aBuddyItem;
//                Object obj;
//                String key;
//                ListModel aListModel;
//                Set<String> keys = modelStorage.keySet();
//                Iterator<String> itr = keys.iterator();
//                while(itr.hasNext()){
//                    key = itr.next();
//                    aListModel = modelStorage.get(key);
//                    if (aListModel.equals(currentModel)){
//                        returnedModel = aListModel;
//                    }
//                    for (int i = 0; i < aListModel.getSize(); i++){
//                        obj = aListModel.getElementAt(i);
//                        if (obj instanceof FloatingFrameCheckBuddyItem){
//                            aBuddyItem = (FloatingFrameCheckBuddyItem)obj;
//                            aBuddy = aBuddyItem.getBuddy();
//                            if (aBuddy.getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())){
//                                aBuddyItem.setBuddy(buddy);
//                            }
//                        }
//                    }//for
//                }//while
//                
//                return returnedModel;
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    ListModel returnedModel = get();
//                    if (returnedModel != null){
//                        jGroupMemberList.setModel(returnedModel);
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (ExecutionException ex) {
//                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }).execute();
    }

    /**
     * Update group's members.
     * @param group
     * @param members 
     */
    @Override
    void updateDistributionGroupMembers(final IGatewayConnectorGroup group, final ArrayList<IGatewayConnectorBuddy> members) {
//        if ((group == null) || (members == null)){
//            return;
//        }
//        final ListModel targetListModel = modelStorage.get(group.getIMUniqueName());
//        if (targetListModel == null){
//            return;
//        }
//        (new SwingWorker<ListModel, Void>(){
//            @Override
//            protected ListModel doInBackground() throws Exception {
//                ListModel currentModel = jGroupMemberList.getModel();
//                ListModel returnedModel = null;
//                if (members.isEmpty()){
//                    returnedModel = new DefaultListModel();
//                }else{
//                    HashMap<String, IGatewayConnectorBuddy> membersMap = new HashMap<String, IGatewayConnectorBuddy>();
//                    for (IGatewayConnectorBuddy aMember : members){
//                        membersMap.put(aMember.getIMUniqueName(), aMember);
//                    }
//                    DefaultListModel aNewModel = new DefaultListModel();
//                    IGatewayConnectorBuddy aBuddy;
//                    FloatingFrameCheckBuddyItem aBuddyItem;
//                    Object obj;
//                    for (int i = 0; i < targetListModel.getSize(); i++){
//                        obj = targetListModel.getElementAt(i);
//                        if (obj instanceof FloatingFrameCheckBuddyItem){
//                            aBuddyItem = (FloatingFrameCheckBuddyItem)obj;
//                            aBuddy = aBuddyItem.getBuddy();
//                            if (membersMap.containsKey(aBuddy.getIMUniqueName())){
//                                aNewModel.addElement(aBuddyItem);
//                                membersMap.remove(aBuddy.getIMUniqueName());
//                            }else{
//                                //remove it, do nothing
//                            }
//                        }
//                    }//for
//                    //add new buddies which are left in memberMaps
//                    FloatingFrameCheckBuddyItem aNewBuddyItem;
//                    Collection<IGatewayConnectorBuddy> newMembers = membersMap.values();
//                    for (IGatewayConnectorBuddy aNewMember : newMembers){
//                        aNewBuddyItem = FloatingFrameCheckBuddyItem.createNewInstance(aNewMember, null);
//                        aNewBuddyItem.setSelected(true);
//                        aNewModel.addElement(aNewBuddyItem);
//                    }
//                    modelStorage.put(group.getIMUniqueName(), aNewModel);
//                    
//                    if (currentModel.equals(targetListModel)){
//                        returnedModel = aNewModel;
//                    }
//                }
//                return returnedModel;
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    ListModel returnedModel = get();
//                    if (returnedModel != null){
//                        jGroupMemberList.setModel(returnedModel);
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (ExecutionException ex) {
//                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }).execute();
    }
    
    private ArrayList<IGatewayConnectorBuddy> getPitsCastGroupMembers(String distGroupUniqueName, TreeSet<String> sentBuddyUniqueNameSet){
        if (sentBuddyUniqueNameSet == null){
            sentBuddyUniqueNameSet = new TreeSet<String>();
        }
        ArrayList<IGatewayConnectorBuddy> members =  new ArrayList<IGatewayConnectorBuddy>();
        synchronized (checkTreePanel){
            if (checkTreePanel == null){
                return members;
            }
            IEmsCheckNode root = checkTreePanel.getRootCheckNode();
            if (root != null){
                Enumeration root_nmr = root.getChildrenEnumeration();
                Enumeration group_nmr;
                Object obj;
                IGroupCheckNode groupNode;
                IGroupCheckNode targetGroupNode = null;
                IGatewayConnectorGroup group;
                IGatewayConnectorBuddy buddy;
                while (root_nmr.hasMoreElements()){
                    obj = root_nmr.nextElement();
                    if (obj instanceof IGroupCheckNode){
                        groupNode = (IGroupCheckNode)obj;
                        if (groupNode.isSelected()){
                            group = groupNode.getGroup();
                            if (group.getIMUniqueName().equalsIgnoreCase(distGroupUniqueName)){
                                targetGroupNode = groupNode;
                                break;
                            }
                        }
                    }
                }//while-loop
                if (targetGroupNode != null){
                    group_nmr = targetGroupNode.getChildrenEnumeration();
                    while (group_nmr.hasMoreElements()){
                        obj = group_nmr.nextElement();
                        if (obj instanceof IBuddyCheckNode){
                            if (((IBuddyCheckNode)obj).isSelected()){
                                buddy = ((IBuddyCheckNode)obj).getBuddy();
                                if (!(sentBuddyUniqueNameSet.contains(buddy.getIMUniqueName()))){
                                    members.add(buddy);
                                }
                            }
                        }
                    }
                }
            }//if (root != null){
        }//synchronized (checkTreePanel){
        return members;
    }
    
    /**
     * This will send message for the current high-lighten group
     * @param groupStateUniqueID
     * @param message 
     */
    @Override
    void sendMessage(String groupStateUniqueID, String message){
        if ((DataGlobal.isEmptyNullString(groupStateUniqueID))
                || (DataGlobal.isEmptyNullString(message))){
            return;
        }
        (new MessageSender(groupStateUniqueID, 
                            message, 
                            getPitsCastGroupMembers(groupStateUniqueID, null))).execute();
    }
    
    @Override
    protected void broadcastMessage(String message){
        synchronized (checkTreePanel){
            if (checkTreePanel == null){
                return;
            }
            IEmsCheckNode root = checkTreePanel.getRootCheckNode();
            if (root != null){
                final TreeSet<String> sentBuddyUniqueNameSet = new TreeSet<String>();
                final List<String> groupStateUniqueIDList = new ArrayList<String>();
                Enumeration root_nmr = root.getChildrenEnumeration();
                Enumeration group_nmr;
                Object obj;
                IGroupCheckNode groupNode;
                IGatewayConnectorBuddy buddy;
                String groupUniqueName;
                while (root_nmr.hasMoreElements()){
                    obj = root_nmr.nextElement();
                    if (obj instanceof IGroupCheckNode){
                        if (((IGroupCheckNode)obj).isSelected()){
                            groupNode = (IGroupCheckNode)obj;
                            groupUniqueName = groupNode.getGroup().getIMUniqueName();
                            groupStateUniqueIDList.add(groupUniqueName);
                            group_nmr = groupNode.getChildrenEnumeration();
                            (new MessageSender(groupUniqueName, 
                                                message, 
                                                getPitsCastGroupMembers(groupUniqueName, sentBuddyUniqueNameSet))).execute();
                            //record buddy names who has been sent message
                            while (group_nmr.hasMoreElements()){
                                obj = group_nmr.nextElement();
                                if (obj instanceof IBuddyCheckNode){
                                    if (((IBuddyCheckNode)obj).isSelected()){
                                        buddy = ((IBuddyCheckNode)obj).getBuddy();
                                        sentBuddyUniqueNameSet.add(buddy.getIMUniqueName());
                                    }
                                }
                            }
                        }
                    }
                }//while-loop
//                Set<String> keys = buddyMap.keySet();
//                Iterator<String> itr = keys.iterator();
//                String buddyTabUniqueID;
//                while (itr.hasNext()){
//                    buddyTabUniqueID = itr.next();
//                    (new MessageSender(buddyTabUniqueID, message, null)).execute();
//                }
            }//if (root != null){
        }//synchronized (checkTreePanel){
    }

    @Override
    void renameGroupInFloatingFrame(IGatewayConnectorGroup oldGroup, String newGroupName) {
        renameGroupButtonTabComponent(oldGroup, newGroupName);
    }
}