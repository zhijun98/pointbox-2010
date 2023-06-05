/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.talker.IFloatingDistGroupMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IMessagingBoardState;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.ListModel;

/**
 * This is used for Distribution Frame.
 * @author Zhijun Zhang
 */
class FloatingDistGroupMessagingBoard extends FloatingMessagingBoard implements IFloatingDistGroupMessagingBoard{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(FloatingDistGroupMessagingBoard.class.getName());
    }
    
    private final HashMap<String, ListModel> modelStorage;

    FloatingDistGroupMessagingBoard(IPbcTalker talker, IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        super(talker);
        setGroup(group);
        modelStorage = new HashMap<String, ListModel>();
        this.jMembesrPanel.setVisible(true);
        this.jSuperParentSplitPane.setDividerSize(10);
    }

    @Override
    public String getBoardId() {
        if (getGroup() == null){
            return super.getBoardId();
        }else{
            return getGroup().getGroupName();
        }
    }
    
    @Override
     protected void populateCheckTreeItems(final boolean isAutoSend) {
        if(SwingUtilities.isEventDispatchThread()){
           populateCheckTreeItemsInEDTHelper(isAutoSend);
       }else{
           SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    populateCheckTreeItemsInEDTHelper(isAutoSend);
                }
            });
       }
    }
    
    private void populateCheckTreeItemsInEDTHelper(final boolean isAutoSend) {
        ListModel model= modelStorage.get(targetStateUniqueID);
        if(model == null){
            (new FloatingFrameCheckBuddyItemsPopulator(targetStateUniqueID, isAutoSend)).execute();
        }else{
            jGroupMemberList.setModel(model);
            if(isAutoSend){
                (new FloatingFrameCheckBuddyItemsPopulator(targetStateUniqueID, isAutoSend)).execute();
            }else{
                return;
            }
        }
    }
    
    private class FloatingFrameCheckBuddyItemsPopulator extends SwingWorker<List<IGatewayConnectorBuddy>, Void>{
        
        private boolean isAutoSend;
        private String stateUniqueID;

        public FloatingFrameCheckBuddyItemsPopulator(String stateUniqueID, boolean isAutoSend) {
            this.stateUniqueID = stateUniqueID;
            this.isAutoSend = isAutoSend;
        }
        
        @Override
        protected List<IGatewayConnectorBuddy> doInBackground() throws Exception {
            ArrayList<IGatewayConnectorBuddy> aGatewayConnectorBuddyList = new ArrayList<IGatewayConnectorBuddy>();
            IMessagingBoardState targetState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(stateUniqueID);
            if (targetState!=null){
                for (IGatewayConnectorBuddy member : targetState.getGroupMembers()){
                    aGatewayConnectorBuddyList.add(member);
                }
            }//if
            return aGatewayConnectorBuddyList;
        }

        @Override
        protected void done() {
            try {
                List<IGatewayConnectorBuddy> result = get();
                jGroupMemberList.setModel(new DefaultListModel());
                for (IGatewayConnectorBuddy member : result){
                      ((DefaultListModel)jGroupMemberList.getModel()).addElement(FloatingFrameCheckBuddyItem.createNewInstance(member,null)); 
                }
                jGroupMemberList.setSelectedIndex(-1);
                jCheckAll.setSelected(true);
                modelStorage.put(stateUniqueID,jGroupMemberList.getModel());
                if (isAutoSend){
                    broadcastCurrentMessage();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(FloatingDistGroupMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                ListModel model= modelStorage.get(stateUniqueID);
                if(model!=null){
                    jGroupMemberList.setModel(model);
                }
            } catch (ExecutionException ex) {
                ListModel model= modelStorage.get(stateUniqueID);
                if(model!=null){
                    jGroupMemberList.setModel(model);
                }
            }
        }
    }

    @Override
    void updateBuddyStatus(final IGatewayConnectorBuddy loginUser, final IGatewayConnectorBuddy buddy, final IPbcTalker talker) {
        (new SwingWorker<ListModel, Void>(){
            @Override
            protected ListModel doInBackground() throws Exception {
                ListModel currentModel = jGroupMemberList.getModel();
                ListModel returnedModel = null;
                IGatewayConnectorBuddy aBuddy;
                FloatingFrameCheckBuddyItem aBuddyItem;
                Object obj;
                String key;
                ListModel aListModel;
                Set<String> keys = modelStorage.keySet();
                Iterator<String> itr = keys.iterator();
                while(itr.hasNext()){
                    key = itr.next();
                    aListModel = modelStorage.get(key);
                    if (aListModel.equals(currentModel)){
                        returnedModel = aListModel;
                    }
                    for (int i = 0; i < aListModel.getSize(); i++){
                        obj = aListModel.getElementAt(i);
                        if (obj instanceof FloatingFrameCheckBuddyItem){
                            aBuddyItem = (FloatingFrameCheckBuddyItem)obj;
                            aBuddy = aBuddyItem.getBuddy();
                            if (aBuddy.getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())){
                                aBuddyItem.setBuddy(buddy);
                            }
                        }
                    }//for
                }//while
                
                return returnedModel;
            }

            @Override
            protected void done() {
                try {
                    ListModel returnedModel = get();
                    if (returnedModel != null){
                        jGroupMemberList.setModel(returnedModel);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).execute();
    }

    /**
     * Update group's members.
     * @param group
     * @param members 
     */
    @Override
    void updateDistributionGroupMembers(final IGatewayConnectorGroup group, final ArrayList<IGatewayConnectorBuddy> members) {
        if ((group == null) || (members == null)){
            return;
        }
        final ListModel targetListModel = modelStorage.get(group.getIMUniqueName());
        if (targetListModel == null){
            return;
        }
        (new SwingWorker<ListModel, Void>(){
            @Override
            protected ListModel doInBackground() throws Exception {
                ListModel currentModel = jGroupMemberList.getModel();
                ListModel returnedModel = null;
                if (members.isEmpty()){
                    returnedModel = new DefaultListModel();
                }else{
                    HashMap<String, IGatewayConnectorBuddy> membersMap = new HashMap<String, IGatewayConnectorBuddy>();
                    for (IGatewayConnectorBuddy aMember : members){
                        membersMap.put(aMember.getIMUniqueName(), aMember);
                    }
                    DefaultListModel aNewModel = new DefaultListModel();
                    IGatewayConnectorBuddy aBuddy;
                    FloatingFrameCheckBuddyItem aBuddyItem;
                    Object obj;
                    for (int i = 0; i < targetListModel.getSize(); i++){
                        obj = targetListModel.getElementAt(i);
                        if (obj instanceof FloatingFrameCheckBuddyItem){
                            aBuddyItem = (FloatingFrameCheckBuddyItem)obj;
                            aBuddy = aBuddyItem.getBuddy();
                            if (membersMap.containsKey(aBuddy.getIMUniqueName())){
                                aNewModel.addElement(aBuddyItem);
                                membersMap.remove(aBuddy.getIMUniqueName());
                            }else{
                                //remove it, do nothing
                            }
                        }
                    }//for
                    //add new buddies which are left in memberMaps
                    FloatingFrameCheckBuddyItem aNewBuddyItem;
                    Collection<IGatewayConnectorBuddy> newMembers = membersMap.values();
                    for (IGatewayConnectorBuddy aNewMember : newMembers){
                        aNewBuddyItem = FloatingFrameCheckBuddyItem.createNewInstance(aNewMember, null);
                        aNewBuddyItem.setSelected(true);
                        aNewModel.addElement(aNewBuddyItem);
                    }
                    modelStorage.put(group.getIMUniqueName(), aNewModel);
                    
                    if (currentModel.equals(targetListModel)){
                        returnedModel = aNewModel;
                    }
                }
                return returnedModel;
            }

            @Override
            protected void done() {
                try {
                    ListModel returnedModel = get();
                    if (returnedModel != null){
                        jGroupMemberList.setModel(returnedModel);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).execute();
    }
}
