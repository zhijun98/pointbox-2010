/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.BuddyItemRemovedEvent;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.IPointBoxConferenceGroup;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDBuddyTreeNode;
import com.eclipsemarkets.pbc.face.talker.dndtree.IDnDGroupTreeNode;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.web.PointBoxServiceResult;
import com.eclipsemarkets.web.PointBoxWebServiceResponse;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Zhijun Zhang
 */
class BasicBuddyListMenu extends JPopupMenu implements IBuddyListPopupMenu {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(BasicBuddyListMenu.class.getName());
    }

    private final IPbcTalker talker;
    private final IBuddyListPanel buddyListPanel;
    
    private DefaultMutableTreeNode focusNode;
    private BuddyProfileDialog buddyProfileDialog;
    
    JMenuItem addGroupMenuItem;
    JMenuItem editGroupMenuItem;
    JMenuItem renameGroupMenuItem;
    JMenuItem removeGroupMenuItem;
    JMenuItem addNewBuddyMenuItem;
    JMenuItem deleteBuddyMenuItem;
    JMenuItem moveBuddyMenuItem;
    JMenuItem sortGroupFromA2ZMenuItem;
    JMenuItem sortGroupFromZ2AMenuItem;
    JMenuItem profileMenuItem;
    JMenuItem blockBuddyMenuItem;
    JMenuItem renameFrameNodeItem;
    JMenuItem removeFrameNodeItem;
    JMenuItem modifyPitsGroupNodeItem;

    public BasicBuddyListMenu(final IPbcTalker talker, 
                             final IBuddyListPanel buddyListPanel,
                             final DefaultMutableTreeNode focusNode)
    {    
        this.talker = talker;
        this.buddyListPanel = buddyListPanel;
        this.focusNode = focusNode;
        buddyProfileDialog = null;
        
        addGroupMenuItem = new JMenuItem();
        addGroupMenuItem.setText(BuddyListMenuText.AddGroup.toString());
        addGroupMenuItem.setFont(SwingGlobal.getLabelFont());
        addGroupMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                buddyListPanel.displayAddNewGroupDialog();
            }
        });
        
        editGroupMenuItem = new JMenuItem();
        editGroupMenuItem.setText(BuddyListMenuText.ModifyGroup.toString());
        editGroupMenuItem.setFont(SwingGlobal.getLabelFont());
        editGroupMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                editGroupTreeNodeActionPerformed(e);
            }
        });
        
        renameGroupMenuItem = new JMenuItem();
        renameGroupMenuItem.setText(BuddyListMenuText.RenameGroup.toString());
        renameGroupMenuItem.setFont(SwingGlobal.getLabelFont());
        renameGroupMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (focusNode == null){
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Group.toString());
                    return;
                }
                Object obj = focusNode.getUserObject();
                if (focusNode instanceof IDnDBuddyTreeNode){
                    TreeNode parentNode = focusNode.getParent();
                    if (parentNode instanceof IDnDGroupTreeNode){
                        obj = ((IDnDGroupTreeNode)parentNode).getGatewayConnectorGroup();
                    }
                }
                if (obj instanceof IGatewayConnectorGroup){
                    buddyListPanel.displayRenameGroupDialog((IGatewayConnectorGroup)obj);
                }else{
                    JOptionPane.showMessageDialog(talker.getBuddyListBasePanel(), 
                                                  BuddyListMenuText.Warning_No_Selected_Group.toString());
                }
            }
        });
        
        removeGroupMenuItem = new JMenuItem();
        removeGroupMenuItem.setText(BuddyListMenuText.RemoveGroup.toString());
        removeGroupMenuItem.setFont(SwingGlobal.getLabelFont());
        removeGroupMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (focusNode == null){
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Group.toString());
                    return;
                }
                Object obj = focusNode.getUserObject();
                if (focusNode instanceof IDnDBuddyTreeNode){
                    TreeNode parentNode = focusNode.getParent();
                    if (parentNode instanceof IDnDGroupTreeNode){
                        obj = ((IDnDGroupTreeNode)parentNode).getGatewayConnectorGroup();
                    }
                }
                if (obj instanceof IGatewayConnectorGroup){
                    buddyListPanel.displayRemoveGroupDialog((IGatewayConnectorGroup)obj);
                }else{
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Group.toString());
                }
            }
        });
        
        addNewBuddyMenuItem = new JMenuItem();
        addNewBuddyMenuItem.setText(BuddyListMenuText.AddNewBuddy.toString());
        addNewBuddyMenuItem.setFont(SwingGlobal.getLabelFont());
        addNewBuddyMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buddyListPanel == null){
                    return;
                }
                buddyListPanel.displayAddNewBuddyDialog(null);
            }
        });
        
        deleteBuddyMenuItem = new JMenuItem();
        deleteBuddyMenuItem.setText(BuddyListMenuText.DeleteBuddy.toString());
        deleteBuddyMenuItem.setFont(SwingGlobal.getLabelFont());
        deleteBuddyMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (focusNode instanceof IDnDBuddyTreeNode){
                    buddyListPanel.displayRemoveBuddyDialog(((IDnDBuddyTreeNode)focusNode).getGatewayConnectorBuddy());
                }else{
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Buddy.toString());
                }
            }
        });

        moveBuddyMenuItem = new JMenuItem();
        moveBuddyMenuItem.setText(BuddyListMenuText.ChangeBuddyGroup.toString());
        moveBuddyMenuItem.setFont(SwingGlobal.getLabelFont());
        moveBuddyMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (focusNode instanceof IDnDBuddyTreeNode){
                    buddyListPanel.displayChangeBuddyGroupDialog(((IDnDBuddyTreeNode)focusNode).getGatewayConnectorBuddy());
                }else{
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Buddy.toString());
                }
            }
        });
        
        sortGroupFromA2ZMenuItem = new JMenuItem();
        sortGroupFromA2ZMenuItem.setText(BuddyListMenuText.SortBuddyGroupFromA2Z.toString());
        sortGroupFromA2ZMenuItem.setFont(SwingGlobal.getLabelFont());
        sortGroupFromA2ZMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                IDnDGroupTreeNode focusGroupNode = null;
                if (focusNode instanceof IDnDBuddyTreeNode){
                    TreeNode parentNode = focusNode.getParent();
                    if (parentNode instanceof IDnDGroupTreeNode){
                        focusGroupNode = (IDnDGroupTreeNode)parentNode;
                    }
                }else if (focusNode instanceof IDnDGroupTreeNode){
                    focusGroupNode = (IDnDGroupTreeNode)focusNode;
                }
                if (focusNode == null){
                    buddyListPanel.sortBuddyListFromA2Z(true);
                }else{
                    buddyListPanel.sortBuddyGroupFromA2Z(focusGroupNode, true);
                }
            }
        });
        
        sortGroupFromZ2AMenuItem = new JMenuItem();
        sortGroupFromZ2AMenuItem.setText(BuddyListMenuText.SortBuddyGroupFromZ2A.toString());
        sortGroupFromZ2AMenuItem.setFont(SwingGlobal.getLabelFont());
        sortGroupFromZ2AMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                IDnDGroupTreeNode focusGroupNode = null;
                if (focusNode instanceof IDnDBuddyTreeNode){
                    TreeNode parentNode = focusNode.getParent();
                    if (parentNode instanceof IDnDGroupTreeNode){
                        focusGroupNode = (IDnDGroupTreeNode)parentNode;
                    }
                }else if (focusNode instanceof IDnDGroupTreeNode){
                    focusGroupNode = (IDnDGroupTreeNode)focusNode;
                }
                if (focusNode == null){
                    buddyListPanel.sortBuddyListFromZ2A(true);
                }else{
                    buddyListPanel.sortBuddyGroupFromZ2A(focusGroupNode, true);
                }
            }
        });

        profileMenuItem = new JMenuItem();
        profileMenuItem.setText(BuddyListMenuText.BuddyProfile.toString());
        profileMenuItem.setFont(SwingGlobal.getLabelFont());
        profileMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (focusNode == null){
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Buddy.toString());
                    return;
                }
                Object obj = buddyListPanel.getCurrentSelectedUserObject();
                if (obj instanceof IGatewayConnectorBuddy){
                    displayBuddyProfileHelper((IGatewayConnectorBuddy)obj);
                }else{
                    JOptionPane.showMessageDialog(talker.getBuddyListBasePanel(), BuddyListMenuText.Warning_No_Selected_Buddy.toString());
                }
            }
        });
        
        blockBuddyMenuItem = new JMenuItem();
        blockBuddyMenuItem .setText(BuddyListMenuText.BlockBuddy.toString());
        blockBuddyMenuItem.setFont(SwingGlobal.getLabelFont());
        blockBuddyMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                 if (focusNode instanceof IDnDBuddyTreeNode){
                   if(JOptionPane.showConfirmDialog(talker.getPointBoxFrame(), "Are you sure to block this buddy?","Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                       IGatewayConnectorBuddy targetBuddy=((IDnDBuddyTreeNode)focusNode).getGatewayConnectorBuddy(); 
                       deleteBuddyWorkerExecute(buddyListPanel.getTalker(),targetBuddy);//when the user want to block a buddy, firstly he must delete this buudy from the buddy list and secondly add him into blacknames.
                       
                       new Thread(new AddBlackNamesWorker(targetBuddy)).start();
                   }
                }else{
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Buddy.toString());
                }
            }
        });
        
        modifyPitsGroupNodeItem = new JMenuItem();
        modifyPitsGroupNodeItem.setText(BuddyListMenuText.ModifyPitsGroup.toString());
        modifyPitsGroupNodeItem.setFont(SwingGlobal.getLabelFont());
        modifyPitsGroupNodeItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                editGroupTreeNodeActionPerformed(e);
            }
        });
        
        renameFrameNodeItem = new JMenuItem();
        renameFrameNodeItem.setText(BuddyListMenuText.RenameFrame.toString());
        renameFrameNodeItem.setFont(SwingGlobal.getLabelFont());
        renameFrameNodeItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (focusNode == null){
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Group.toString());
                    return;
                }
                Object obj = focusNode.getUserObject();
                if (focusNode instanceof IDnDBuddyTreeNode){
                    TreeNode parentNode = focusNode.getParent();
                    if (parentNode instanceof IDnDGroupTreeNode){
                        obj = ((IDnDGroupTreeNode)parentNode).getGatewayConnectorGroup();
                    }
                }
                if (obj instanceof IGatewayConnectorGroup){
                    if(buddyListPanel instanceof IPitsCastGroupListPanel){
                        //todo-pitscast: how to handle "renamePitsFrame"
                    }else if (buddyListPanel instanceof IPitsGroupListPanel){
                        ((IPitsGroupListPanel)buddyListPanel).renamePitsFrame((IGatewayConnectorGroup)obj,talker.getPointBoxLoginUser().getIMUniqueName());
                    }
                }else{
                    JOptionPane.showMessageDialog(talker.getBuddyListBasePanel(), 
                                                  BuddyListMenuText.Warning_No_Selected_Frame.toString());
                }
            }
        });   
        
        removeFrameNodeItem = new JMenuItem();
        removeFrameNodeItem.setText(BuddyListMenuText.RemoveFrame.toString());
        removeFrameNodeItem.setFont(SwingGlobal.getLabelFont());
        removeFrameNodeItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (focusNode == null){
                    JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Group.toString());
                    return;
                }
                Object obj = focusNode.getUserObject();
                if (focusNode instanceof IDnDBuddyTreeNode){
                    TreeNode parentNode = focusNode.getParent();
                    if (parentNode instanceof IDnDGroupTreeNode){
                        obj = ((IDnDGroupTreeNode)parentNode).getGatewayConnectorGroup();
                    }
                }
                if (obj instanceof IGatewayConnectorGroup){
                    if(buddyListPanel instanceof IPitsCastGroupListPanel){
                        //todo-pitscast: how to handle "renamePitsFrame"
                    }else if (buddyListPanel instanceof IPitsGroupListPanel){
                        ((IPitsGroupListPanel)buddyListPanel).removePitsFrame((IGatewayConnectorGroup)obj,
                                                                          talker.getPointBoxLoginUser().getIMUniqueName());
                    }
                }else{
                    JOptionPane.showMessageDialog(talker.getPointBoxFrame(), 
                                                  BuddyListMenuText.Warning_No_Selected_Group.toString());
                }
            }
        });
    }//constructor
    
    private void editGroupTreeNodeActionPerformed(ActionEvent e) {
        if (focusNode instanceof IDnDGroupTreeNode){
            buddyListPanel.displayEditGroupDialog(((IDnDGroupTreeNode)focusNode).getGatewayConnectorGroup());
        }else if (focusNode instanceof IDnDBuddyTreeNode){
            TreeNode parentNode = focusNode.getParent();
            if (parentNode instanceof IDnDGroupTreeNode){
                buddyListPanel.displayEditGroupDialog(((IDnDGroupTreeNode)parentNode).getGatewayConnectorGroup());
            }
        }else{
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), 
                                          BuddyListMenuText.Warning_No_Selected_Group.toString());
        }
    }
    
    private void deleteBuddyWorkerExecute(final IPbcTalker talker,final IGatewayConnectorBuddy targetBuddy){
        (new SwingWorker<String, Void>(){
                @Override
                protected String doInBackground() throws Exception {
                    //distribution list...
                    String msg;
                    //delete it from servers....
                    PointBoxWebServiceResponse response = talker.getKernel().deleteConnectorBuddy(targetBuddy.getLoginOwner(), 
                            targetBuddy, GatewayBuddyListFactory.getDistributionGroupInstance(targetBuddy.getLoginOwner(), targetBuddy.getBuddyGroupName()));
                    if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
                        msg = "Blocked buddy " + targetBuddy.getIMScreenName() + " from account " + targetBuddy.getLoginOwner().getIMScreenName() + " of " + targetBuddy.getIMServerType() + ".";                     
                        talker.getKernel().raisePointBoxEvent(
                                            new BuddyItemRemovedEvent(PointBoxEventTarget.PbcFace,
                                                                      targetBuddy.getLoginOwner(),
                                                                      targetBuddy));
                    }else{
                        msg = "Failed to block buddy " + targetBuddy.getIMScreenName() + " from account " + targetBuddy.getLoginOwner().getIMScreenName() + " of " + targetBuddy.getIMServerType() + ".";
                    }
                    return msg;
                }

                @Override
                protected void done() {
                    String msg = "Result: ";
                    try {
                        msg = msg + get();
                        JOptionPane.showMessageDialog(BasicBuddyListMenu.this, msg);
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }).execute();
    }
    
    void constructBuddyListMenu() {
        add(addGroupMenuItem);
        add(editGroupMenuItem);
        add(renameGroupMenuItem);
        add(removeGroupMenuItem);
        add(sortGroupFromA2ZMenuItem);
        add(sortGroupFromZ2AMenuItem);
        add(profileMenuItem);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        constructBuddyListMenu();
        super.show(invoker, x, y);
    }

    void displayBuddyProfileHelper(IGatewayConnectorBuddy buddy){
        //String profileUuid = buddy.getIMUniqueName();
        if ( buddyProfileDialog == null) {
           buddyProfileDialog = new BuddyProfileDialog(talker, true);
           buddyProfileDialog.pack();
        }
        buddyProfileDialog.setBuddyProfileKeysAndLoadValues (buddyListPanel.getMasterLoginUser(), buddy, (IBuddyListPopupMenu)this);
        buddyProfileDialog.setVisible(true);
    }

    @Override
    public void buddyProfileUpdated (IBuddyProfileRecord buddyProfile, IGatewayConnectorBuddy buddy) {
       if ( buddyProfile == null) {
          return;
       }
       talker.getKernel().storeBuddyProfileRecord(buddyProfile);
       buddyListPanel.buddyContentChanged(buddy);
    }

    static enum BuddyListMenuText{
        RenameFrame("Rename Frame"),
        ModifyPitsGroup("Modify PITS Group"),
        BlockBuddy("Block Buddy"),
        AddGroup("Create Group"),
        ModifyGroup("Modify Group"),
        RenameGroup("Rename Group"),
        RemoveGroup("Delete Group"),
        RemoveFrame("Delete Frame"),
        AddNewBuddy("Add Buddy"),
        DeleteBuddy("Delete Buddy"),
        ChangeBuddyGroup("Change Buddy's Group"),
        SortBuddyGroupFromA2Z("Sort (A-Z)"),
        SortBuddyGroupFromZ2A("Sort(Z-A)"),
        BuddyProfile("Buddy Profile"),
        Warning_No_Selected_Buddy("Please right-click your mouse over a buddy."),
        Warning_No_Selected_Group("Please right-click your mouse over a group."),
        Warning_No_Selected_Frame("Please right-click your mouse over a frame node."),
        Warning_No_Selected_Conference("Please right-click your mouse over a conference.");

        private String term;
        BuddyListMenuText(String term){
            this.term = term;
        }
        @Override
        public String toString() {
            return term;
        }
    }
    
    class AddBlackNamesWorker implements Runnable{

        private IGatewayConnectorBuddy targetBuddy;
        
        public AddBlackNamesWorker(IGatewayConnectorBuddy targetBuddy) {
            this.targetBuddy=targetBuddy;
        }
        
        @Override
        public void run() {
            try {
                Thread.sleep(3000);//sleep 3 s. It can guarentee that delete buddy first and then add it into blacknames.
            } catch (InterruptedException ex) {
                Logger.getLogger(BasicBuddyListMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
            talker.getKernel().storeBlackNameEntry(targetBuddy.getIMScreenName(), 
                                                talker.getKernel().getPointBoxLoginUser().getIMUniqueName(), 
                                                targetBuddy.getLoginOwner().getIMUniqueName());
           
        }
        
    }
}
