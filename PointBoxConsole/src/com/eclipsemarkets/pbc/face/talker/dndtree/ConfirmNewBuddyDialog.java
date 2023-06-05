/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConfirmNewBuddyDialog.java
 *
 * Created on Nov 23, 2011, 11:17:12 PM
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.BuddyItemPresentedEvent;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.exceptions.PointBoxException;
import com.eclipsemarkets.pbc.face.talker.IMessagingPaneManager;
import com.eclipsemarkets.pbc.face.talker.INewBuddyGroupDialogListener;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.face.talker.PointBoxTalker;
import com.eclipsemarkets.pbc.face.talker.messaging.MasterMessagingBoard;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 *
 * @author Zhijun Zhang
 */
public class ConfirmNewBuddyDialog extends javax.swing.JDialog {

    private final IPbcDndBuddyTree dndTree;
    private IGatewayConnectorBuddy loginUser;
    private IGatewayConnectorBuddy buddy;
    private ConfirmNewBuddyDialogType type;
    private ExecutorService service;
    private final ArrayList<INewBuddyGroupDialogListener> listeners;
    
    private static ConfirmNewBuddyDialog self;
    static {
        self = null;
    }

    public static ConfirmNewBuddyDialog getSingleton(java.awt.Frame parent,
            boolean modal,
            IPbcDndBuddyTree dndTree,
            IGatewayConnectorBuddy loginUser,
            IGatewayConnectorBuddy buddy,
            ArrayList<IGatewayConnectorGroup> distGroups,
            ConfirmNewBuddyDialogType type) {
        if (self == null) {
            self = new ConfirmNewBuddyDialog(parent, modal, dndTree, loginUser, buddy, distGroups, type);
        } else {
            self.updateConfirmNewBuddyDialog(buddy, distGroups, type);
        }
        return self;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b); //To change body of generated methods, choose Tools | Templates.
        toFront();
    }

    /**
     * Creates new form ConfirmNewBuddyDialog
     */
    private ConfirmNewBuddyDialog(java.awt.Frame parent,
            boolean modal,
            IPbcDndBuddyTree dndTree,
            IGatewayConnectorBuddy loginUser,
            IGatewayConnectorBuddy buddy,
            ArrayList<IGatewayConnectorGroup> distGroups,
            ConfirmNewBuddyDialogType type) {
        super(parent, modal);
        initComponents();

        this.dndTree = dndTree;
        this.loginUser = loginUser;
        updateConfirmNewBuddyDialog(buddy, distGroups, type);

        listeners = new ArrayList<INewBuddyGroupDialogListener>();

        service = Executors.newSingleThreadExecutor();
    }

    public final void updateConfirmNewBuddyDialog(IGatewayConnectorBuddy buddy,
            ArrayList<IGatewayConnectorGroup> distGroups,
            ConfirmNewBuddyDialogType type) {
        this.buddy = buddy;
        this.type = type;

        jLoginUser.setText(buddy.getLoginOwner().getIMScreenName() + " of " + buddy.getIMServerType().toString());
        jBuddyName.setText(buddy.getIMScreenName());
        DefaultListModel model = new DefaultListModel();
        for (IGatewayConnectorGroup distGroup : distGroups) {
            model.addElement(distGroup.getGroupName());
        }
        jDistGroupList.setModel(model);
        jDistGroupList.setSelectedIndex(0);  //set a defualt selected value in case that user didn't choose group
        jScrollPane1.setVisible(true);
        jDistGroupList.setVisible(true);
        defaultLable.setVisible(false);
        jGroupFiled.setVisible(false);
        if(model.size()<=0){
            jScrollPane1.setVisible(false);
            jDistGroupList.setVisible(false);
            defaultLable.setVisible(true);
            jGroupFiled.setVisible(true);
        }
        switch (type) {
            case AddNewBuddy:
                prepareForAddNewBuddyHelper();
                break;
            case AcceptBuddy:
                prepareForAcceptBuddyHelper();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Tech Error: wrong type of ConfirmNewBuddyDialog - " + type);
                super.setVisible(false);
        }
    }

    private void prepareForAcceptBuddyHelper() {
        jBuddyName.setEditable(false);
        jBuddyName.setVisible(false);
        jLabel1.setText("Buddy Name: " + jBuddyName.getText());
        jAddAccept.setText("Accept");
        jCancelRefuse.setText("Refuse");
    }

    private void prepareForAddNewBuddyHelper() {
        jBuddyName.setVisible(true);
        jBuddyName.setEditable(true);
        jLabel1.setText("Buddy Name:");
        jAddAccept.setText("Add");
        jCancelRefuse.setText("Cancel");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jDistGroupList = new javax.swing.JList();
        jBuddyName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLoginUser = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jAddAccept = new javax.swing.JButton();
        jCancelRefuse = new javax.swing.JButton();
        jBlock = new javax.swing.JButton();
        defaultLable = new javax.swing.JLabel();
        jGroupFiled = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Confirm: New Buddy ");

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Distribution Groups:"));

        jDistGroupList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jDistGroupList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jDistGroupList);

        jLabel1.setText("Buddy Name:");

        jLabel2.setText("Login User:");

        jLoginUser.setText("PointBox");

        jPanel1.setLayout(new java.awt.GridLayout(1, 2, 2, 0));

        jAddAccept.setText("jButton1");
        jAddAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddAcceptActionPerformed(evt);
            }
        });
        jPanel1.add(jAddAccept);

        jCancelRefuse.setText("jButton2");
        jCancelRefuse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelRefuseActionPerformed(evt);
            }
        });
        jPanel1.add(jCancelRefuse);

        jBlock.setText("Block");
        jBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBlockActionPerformed(evt);
            }
        });

        defaultLable.setText("Group:");

        jGroupFiled.setText("Contacts");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLoginUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jBuddyName, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(32, 32, 32)
                                        .addComponent(defaultLable, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jGroupFiled, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBlock)
                        .addGap(0, 71, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLoginUser)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBuddyName)
                    .addComponent(defaultLable, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jGroupFiled, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBlock))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jAddAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAddAcceptActionPerformed
        switch (type) {
            case AddNewBuddy:
                addNewBuddyGroupToServer();
                break;
            case AcceptBuddy:
                acceptNewBuddyAuthorization();
                break;
            default:
        }
    }//GEN-LAST:event_jAddAcceptActionPerformed

    private void jCancelRefuseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelRefuseActionPerformed
        switch (type) {
            case AddNewBuddy:
                setVisible(false);
                break;
            case AcceptBuddy:
                refuseNewBuddyByDialog();
                break;
            default:
        }
    }//GEN-LAST:event_jCancelRefuseActionPerformed

    private void jBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBlockActionPerformed
        if(JOptionPane.showConfirmDialog(this, "Are you sure to block this buddy?","Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            dndTree.getKernel().storeBlackNameEntry(jBuddyName.getText(),
                                                    dndTree.getKernel().getPointBoxLoginUser().getIMUniqueName(),
                                                    loginUser.getIMUniqueName());
            refuseNewBuddyByDialog();       //block this buddy > refuse this buddy. (So after block,we have to refuse this buddy)
        }
    }//GEN-LAST:event_jBlockActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel defaultLable;
    private javax.swing.JButton jAddAccept;
    private javax.swing.JButton jBlock;
    private javax.swing.JTextField jBuddyName;
    private javax.swing.JButton jCancelRefuse;
    private javax.swing.JList jDistGroupList;
    private javax.swing.JTextField jGroupFiled;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLoginUser;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private void addNewBuddyGroupToServer() {
        String newBuddyName = jBuddyName.getText();
        if (DataGlobal.isEmptyNullString(newBuddyName)) {
            JOptionPane.showMessageDialog(this, "Please give a new buddy name.");
            return;
        }
        final Object groupObj = jDistGroupList.getSelectedValue();
        if (!(groupObj instanceof IGatewayConnectorGroup)){
            JOptionPane.showMessageDialog(this, "Please select a group for this new buddy.");
            return;
        }
        buddy = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(loginUser,
                                                                        newBuddyName,
                                                                        ((IGatewayConnectorGroup)groupObj).getGroupName(), 
                                                                        dndTree.getKernel().getPointBoxConsoleRuntime());
        //request server-side to do it
        dndTree.getKernel().addNewConnectorBuddy(buddy.getLoginOwner(),
                                                (IGatewayConnectorGroup)groupObj,
                                                buddy.getIMScreenName());
        dndTree.loadDistributionGroupWithBuddyNode((IGatewayConnectorGroup)groupObj, buddy);
        setVisible(false);
    }
    
    private void addGroupHelper(final IGatewayConnectorGroup group, final List<IGatewayConnectorBuddy> members){
        dndTree.loadDistributionGroupWithBuddyNodes(group, members);
    }

    private void addQuickGroup(String groupName){
        if (dndTree.isDistGroupExisted(groupName)) {
                JOptionPane.showMessageDialog(this, "The group name has been used. Please give another one.");
            } else {
                IGatewayConnectorGroup group = GatewayBuddyListFactory.getDistributionGroupInstance(dndTree.getKernel().getPointBoxLoginUser(), groupName);
                group.setLoginUser(loginUser);//a new group should be set a login User
                final ArrayList<IGatewayConnectorBuddy> members = new ArrayList<IGatewayConnectorBuddy>();
  
                addGroupHelper(group, members);
    
            }
    }
    
    
    /**
     * it is possible this guy does not exist on the server yet. For example,
     * the local-user did remove a remote user from his buddy list and the
     * remote guy send a message to the local guy. The system will popup this
     * dialog to ask permission from the local user
     */
    private void acceptNewBuddyAuthorization() {
        String groupName;
        /**
         * This following code should be improved. Ideally, users may use of both jDistGroupList and jGroupFiled
         */
        if(jDistGroupList.isVisible()){
            groupName = jDistGroupList.getSelectedValue().toString();
            if (DataGlobal.isEmptyNullString(groupName)){
                groupName = PbcReservedTerms.PbBuddyDefaultGroup.toString();
            }
        }else{
            groupName=jGroupFiled.getText().trim();
            if (DataGlobal.isEmptyNullString(groupName)){
                groupName = PbcReservedTerms.PbBuddyDefaultGroup.toString();
            }
            addQuickGroup(groupName);
        }
        
        buddy.setBuddyGroupName(groupName);  //set buddy's group in case of "Other group" appearing
        dndTree.getKernel().acceptNewBuddyAuthorization(buddy.getLoginOwner(), buddy.getIMScreenName());
        //buddy.setBuddyStatus(BuddyStatus.Online); it is not necessary to be online when subscription request recieved. the remote buddy may have been offline
        dndTree.getKernel().raisePointBoxEvent(new BuddyItemPresentedEvent(PointBoxEventTarget.PbcFace,
                                                                            buddy.getLoginOwner(),
                                                                            buddy));
        
        /**
         * This is only required by AOL buddy and PBIM.
         * <p/>
         * There is a very small chance that PBIM buddy A added B who was offline. After B login and only accept it, 
         * A did not receive B's acceptance. This was caused unknown reason.
         */
        if (((loginUser.getIMServerType().equals(GatewayServerType.PBIM_SERVER_TYPE))
                && (buddy.getIMServerType().equals(GatewayServerType.PBIM_SERVER_TYPE))) 
                || ((loginUser.getIMServerType().equals(GatewayServerType.AIM_SERVER_TYPE))
                && (buddy.getIMServerType().equals(GatewayServerType.AIM_SERVER_TYPE))) )
        {
            service.submit(new Runnable(){
                @Override
                public void run() {
                    dndTree.getKernel().addNewConnectorBuddy(loginUser, 
                                        GatewayBuddyListFactory.getGatewayConnectorGroupInstance(loginUser, buddy.getBuddyGroupName()), 
                                        buddy.getIMScreenName());
                }
            });
        }
        //*******************************************************************************************************
        //fix the bug (First Message From New Buddy Might Be Missed)
        //load the previous messages to the buddy tabs.
        //*******************************************************************************************************
        try {
            IPbcTalker talker = PointBoxTalker.getPointBoxTalkerSingleton();
            List<IPbsysOptionQuote> qts = talker.getBufferedMessagesForNewBuddies(buddy);
            if(qts!=null){
                for (IPbsysOptionQuote qt : qts){                 
                    IMessagingPaneManager manager = talker.getMessagingPaneManager();
                    MasterMessagingBoard board=(MasterMessagingBoard)manager.getMasterMessagingBoard();
                    board.publishQuoteOnMessageTabHelper2(buddy.getLoginOwner(), buddy, qt);   //just flash the tabs to front-end and do NOT publish quote.
                }
            }
        } catch (PointBoxException ex) {
            Logger.getLogger(ConfirmNewBuddyDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        setVisible(false);
    }

    private void refuseNewBuddyByDialog() {
        dndTree.getKernel().refuseNewBuddyAuthorization(buddy.getLoginOwner(), buddy.getIMScreenName());
        fireBuddyRefusedEvent(buddy);
        setVisible(false);
    }

    public void addNewBuddyGroupDialogListener(INewBuddyGroupDialogListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeNewBuddyGroupDialogListener(INewBuddyGroupDialogListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void fireBuddyRefusedEvent(final IGatewayConnectorBuddy buddy) {
        synchronized (listeners) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    for (INewBuddyGroupDialogListener listener : listeners) {
                        listener.buddyRefusedEventHappened(buddy);
                    }
                }
            });
        }
    }
}
