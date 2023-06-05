/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IFloatingPitsMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.face.talker.IPitsCastGroupListPanel;
import com.eclipsemarkets.pbc.face.talker.IPitsGroupListPanel;
import com.eclipsemarkets.pbc.face.talker.dist.PbcFloatingFrameTerms;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Board for PITS floating frames
 * 
 * @author Zhijun Zhang
 * @date & time: Aug 26, 2012 - 8:43:34 PM
 */
class FloatingPitsMessagingBoard extends FloatingMessagingBoard implements IFloatingPitsMessagingBoard{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(FloatingDistGroupMessagingBoard.class.getName());
    }
    
    private String buddyBoardId;
    
    private String pitsGroupName;

    /**
     * 
     * @param talker
     * @param group
     * @param buddy
     * @param buddyBoardId
     * @param pitsGroupName - this is the same as PitsGroup's floating frame's title (Bad design).
     */
    FloatingPitsMessagingBoard(IPbcTalker talker, 
                               IGatewayConnectorGroup group, 
                               IGatewayConnectorBuddy buddy, 
                               String buddyBoardId, 
                               String pitsGroupName) 
    {
        super(talker);
        
        this.pitsGroupName = pitsGroupName;
        
        setMessagingBoardDropTargetListener(new BuddyMessagingBoardDropTargetListener(this));
        
        this.buddyBoardId = buddyBoardId;
        setGroup(group);
        initJTextPaneInEDT();
        
        this.jMembesrPanel.setVisible(true);
        this.jSuperParentSplitPane.setDividerSize(10);        
    }
    
    private void initJTextPaneInEDT(){
        if(SwingUtilities.isEventDispatchThread()){
            initJTextPanelHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    initJTextPanelHelper();
                }
            });
        }
    }
    
    @Override
    protected void populateCheckTreeItemsSlowlyInEDT(){
        List<BuddyButtonTabComponent> aBuddyButtonTabComponentList = new ArrayList<BuddyButtonTabComponent>();
        DefaultListModel model = new DefaultListModel();
        BuddyButtonTabComponent buddyTab;
        for(IButtonTabComponent tab : getAllVisibleTabeButtons()){
            if(tab instanceof BuddyButtonTabComponent){
                buddyTab = (BuddyButtonTabComponent)tab;
                aBuddyButtonTabComponentList.add(buddyTab);
                model.addElement(FloatingFrameCheckBuddyItem.createNewInstance(buddyTab.getBuddy(),buddyTab));
            }
        }//for
        jGroupMemberList.setModel(model);
        jGroupMemberList.setSelectedIndex(-1);
        jCheckAll.setSelected(true);
    }
    
    @Override
    protected void populateCheckTreeItems(final boolean isAutoSend) {
        new SwingWorker<List<BuddyButtonTabComponent>, Void>(){
            @Override
            protected List<BuddyButtonTabComponent> doInBackground() throws Exception {
                List<BuddyButtonTabComponent> aBuddyButtonTabComponentList = new ArrayList<BuddyButtonTabComponent>();
                for(IButtonTabComponent tab : getAllVisibleTabeButtons()){
                    if(tab instanceof BuddyButtonTabComponent){
                       aBuddyButtonTabComponentList.add((BuddyButtonTabComponent)tab);
                    }
                }//for
                return aBuddyButtonTabComponentList;
            }
            
            @Override
            protected void done() {
                try {
                    List<BuddyButtonTabComponent> aBuddyButtonTabComponentList = get();
                    jGroupMemberList.setModel(new DefaultListModel());
                    for (BuddyButtonTabComponent tab : aBuddyButtonTabComponentList){
                        ((DefaultListModel)jGroupMemberList.getModel()).addElement(FloatingFrameCheckBuddyItem.createNewInstance(tab.getBuddy(),tab)); 
                    }
                    jGroupMemberList.setSelectedIndex(-1);
                    jCheckAll.setSelected(true);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FloatingPitsMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(FloatingPitsMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }
    
    private void initJTextPanelHelper(){
        this.jBroadCastScrollPane.setVisible(true);
        this.jParentSplitPane.setDividerSize(10);
    }

    @Override
    public String getPitsGroupName() {
        return pitsGroupName;
    }

    @Override
    public String getBoardId() {
        return getPitsGroupName();
        //return buddyBoardId;
    }

    @Override
    public void handlePitsBuddyTabOpened(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        //save current PITS group list
        IPitsGroupListPanel panel = talker.getPitsLikeTabPanelByTabName(PbcFloatingFrameTerms.PITSFrame);
        if (panel != null){
            panel.addBuddyIntoPitsGroup(buddy, pitsGroupName);
        }
        talker.getPitsCastBuddyListTreePanel().addBuddyIntoPitsGroup(buddy, pitsGroupName);
    }

    @Override
    public void handlePitsBuddyTabClosed(final IGatewayConnectorBuddy buddy) {
        (new SwingWorker<ListModel, Void>(){
            @Override
            protected ListModel doInBackground() throws Exception {
                return removePitsBuddyCheckItem();
            }

            @Override
            protected void done() {
                try {
                    jGroupMemberList.setModel(get());
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
                //save current PITS group list
                IPitsGroupListPanel panel = talker.getPitsLikeTabPanelByTabName(PbcFloatingFrameTerms.PITSFrame);
                if (panel != null){
                    panel.removeBuddyFromPitsGroup(buddy, pitsGroupName);
                }
                ((IPitsCastGroupListPanel)talker.getPitsCastBuddyListTreePanel()).removeBuddyFromPitsGroup(buddy, pitsGroupName);
            }

            private ListModel removePitsBuddyCheckItem() {
                ListModel model = jGroupMemberList.getModel();
                DefaultListModel aNewModel = new DefaultListModel();
                IGatewayConnectorBuddy aBuddy;
                FloatingFrameCheckBuddyItem aBuddyItem;
                Object obj;
                for (int i = 0; i < model.getSize(); i++){
                    obj = model.getElementAt(i);
                    if (obj instanceof FloatingFrameCheckBuddyItem){
                        aBuddyItem = (FloatingFrameCheckBuddyItem)obj;
                        aBuddy = aBuddyItem.getBuddy();
                        if (!aBuddy.getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())){
                            aNewModel.addElement(aBuddyItem);
                        }
                    }else{
                        aNewModel.add(i, obj);
                    }
                }
                return aNewModel;
            }
        }).execute();
    }
}

