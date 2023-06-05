/**
 * Eclipse Market Solutions LLC
 */
/*
 * GroupDistributionControlDialog.java
 *
 * @author Zhijun Zhang
 * Created on Jun 27, 2010, 10:47:31 PM
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.IPointBoxDistributionGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.*;

/**
 * It is dedicated for editing distribution groups from MasterBuddyListPanel
 *
 * @author Zhijun Zhang
 */
class BuddyListGroupEditor extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private IPbcTalker talker;
    private IBuddyListPanel buddyListPanel;
    private Purpose purpose;
    
    /**
     *
     * @param talker
     * @param buddyListPanel - the panel asked to edit group
     * @param modal
     * @param purpose
     */
    BuddyListGroupEditor(IPbcTalker talker,
            IBuddyListPanel buddyListPanel,
            boolean modal,
            Purpose purpose) {
        super(talker.getPointBoxFrame(), modal);
        initComponents();
        this.talker = talker;

        this.buddyListPanel = buddyListPanel;
        this.purpose = purpose;

        jAddSelectedBuddies.setIcon(getImageSettings().getAddSingleIcon());
        jAddAllMembers.setIcon(getImageSettings().getAddAllIcon());
        jRemoveSelectedMembers.setIcon(getImageSettings().getRemoveSingleIcon());
        jRemoveAllMembers.setIcon(getImageSettings().getRemoveAllIcon());

        jAvailableMemberList.setModel(new DefaultListModel());
        jAvailableMemberList.setCellRenderer(new BuddyListCellRenderer());

        jGroupMemberList.setModel(new DefaultListModel());
        jGroupMemberList.setCellRenderer(new BuddyListCellRenderer());
        switch (purpose) {
            case AddGroup:
                this.setTitle("Create New Groups");
                jAddEditDelete.setText("Add Group");
                break;
            case EditGroup:
                this.setTitle("Edit Group");
                jGroupName.setEditable(false);
                jGroupName.setForeground(Color.blue);
                jAddEditDelete.setText("Save Group");
                jMessage.setText("Note: every time, you are able to modify one group.");
                break;
            case DeleteGroup:
                this.setTitle("Delete Group");
                jAddEditDelete.setText("Delete Group");
                jMessage.setForeground(Color.red);
                jMessage.setText("Do you really want to delete this entire group?");
                jGroupName.setEditable(false);
                jGroupName.setForeground(Color.blue);
                jGroupDescription.setEditable(false);
                jAvailableMemberList.setEnabled(false);
                jGroupMemberList.setEnabled(false);
                jAddSelectedBuddies.setEnabled(false);
                jAddAllMembers.setEnabled(false);
                jRemoveSelectedMembers.setEnabled(false);
                jRemoveAllMembers.setEnabled(false);
                break;
        }
    }

    @Override
    public void setVisible(boolean value) {
        super.setVisible(value);
        if (value){
            setLocation(SwingGlobal.getCenterPointOfParentWindow(talker.getKernel().getPointBoxMainFrame(), this));
        }
    }

    private IPbconsoleImageSettings getImageSettings() {
        return talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
    }

    private ListModel constructAvailableBuddyModel() {
        ArrayList<IGatewayConnectorBuddy> buddies = buddyListPanel.getAllBuddies(true);
        DefaultListModel model = new DefaultListModel();
        for (IGatewayConnectorBuddy buddy : buddies) {
            //avoid redundant objects
            if (!model.contains(buddy)){
                model.addElement(buddy);
            }
        }
        return model;
    }

    /**
     * @param group
     * @param members
     */
    void displayDialog(final IGatewayConnectorGroup group, final ArrayList<IGatewayConnectorBuddy> members) {
        if (SwingUtilities.isEventDispatchThread()) {
            displayDialogHelper(group, members);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    displayDialogHelper(group, members);
                }
            });
        }
    }

    private void displayDialogHelper(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        jAvailableMemberList.setModel(constructAvailableBuddyModel());
        jGroupMemberList.setModel(new DefaultListModel());
        switch (purpose) {
            case AddGroup:
                this.setVisible(true);
                break;
            case EditGroup:
                this.setTitle("Edit Group - " + group);
                populateRecordHelper(group, members);
                this.setVisible(true);
                break;
            case DeleteGroup:
                this.setTitle("Delete Group - " + group);
                populateRecordHelper(group, members);
                this.setVisible(true);
                break;
        }
    }

    private void populateRecordHelper(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        jGroupName.setText(group.getGroupName());
        //jGroupDescription.setText("");
        jGroupDescription.setText(group.getGroupDescription());       
        for (IGatewayConnectorBuddy buddy : members) {
            if (selectMemberFromList(GatewayBuddyListFactory.constructBuddyIMUniqueName(buddy.getIMServerType(),
                    buddy.getIMScreenName()),
                    jAvailableMemberList)) {
                moveSelectedMembers(jAvailableMemberList, jGroupMemberList);
            }
        }
    }
    
    /**
     * After add/edit/delete, group names of all the buddies should be confirmed. 
     */
    private void confirmBuddyGroupNamesAfterOperation(){
        String groupName = jGroupName.getText();
        if (DataGlobal.isEmptyNullString(groupName)){
            return;
        }
        IGatewayConnectorBuddy buddy;
        DefaultListModel model = (DefaultListModel) jGroupMemberList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            buddy = (IGatewayConnectorBuddy) model.get(i);
            //confirm group name for every members
            buddy.setBuddyGroupName(groupName);
        }//for
        model = (DefaultListModel) jAvailableMemberList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            buddy = (IGatewayConnectorBuddy) model.get(i);
            //this kind of buddies are removed from the current group
            if (groupName.equalsIgnoreCase(buddy.getBuddyGroupName())){
                buddy.setBuddyGroupName(PbcReservedTerms.PbBuddyDefaultGroup.toString());
            }
        }//for
    }

    private synchronized void moveSelectedMembers(JList list1, JList list2) {
        if (!list1.isSelectionEmpty()) {
            int[] indices = list1.getSelectedIndices();
            DefaultListModel model1 = (DefaultListModel) list1.getModel();
            DefaultListModel model2 = (DefaultListModel) list2.getModel();
            ArrayList<Object> members = new ArrayList<Object>();
            IGatewayConnectorBuddy buddy;
            for (int i = 0; i < indices.length; i++) {
                buddy = (IGatewayConnectorBuddy) model1.get(indices[i]);
                model2.insertElementAt(buddy, SwingGlobal.calculateEmelementIndex(model2, buddy));
                members.add(model1.get(indices[i]));
            }
            for (int i = 0; i < members.size(); i++) {
                model1.removeElement(members.get(i));
            }
        }
    }

    private synchronized void moveAllMembers(JList list1, JList list2) {
        DefaultListModel model1 = (DefaultListModel) list1.getModel();
        DefaultListModel model2 = (DefaultListModel) list2.getModel();
        //ArrayList<Object> members = new ArrayList<Object>();
        IGatewayConnectorBuddy buddy;
        for (int i = 0; i < model1.getSize(); i++) {
            buddy = (IGatewayConnectorBuddy) model1.get(i);
            model2.insertElementAt(buddy, SwingGlobal.calculateEmelementIndex(model2, buddy));
            //members.add(model1.get(i));

        }
        /*
         * for (int i = 0; i < members.size(); i++){
         * model1.removeElement(members.get(i));
         *
         * }
         */
        model1.clear();
    }

    private void addNewGroupHelper() {
        final String groupName = jGroupName.getText().trim();
        if ((groupName == null) || (groupName.isEmpty())||!PbcGlobal.isLegalInput(groupName)) {
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), "Only alphabetic characters and digit numbers are legal for group name!");
        } else {
            if ((buddyListPanel instanceof DistributionBuddyListPanel) && talker.checkGroupNameRedundancy(groupName)) {
                JOptionPane.showMessageDialog(talker.getPointBoxFrame(), "The group name has been used as a distribution, PITS or conference. Please give another one.");
            } else {
                final ListModel model = jGroupMemberList.getModel();
                IGatewayConnectorGroup group = GatewayBuddyListFactory.getDistributionGroupInstance(talker.getKernel().getPointBoxLoginUser(), groupName);
                group.setLoginUser(buddyListPanel.getMasterLoginUser());//a new group should be set a login User
                ArrayList<IGatewayConnectorBuddy> members = new ArrayList<IGatewayConnectorBuddy>();
                prepareForSaveGroup(model, group, members);
                buddyListPanel.addNewDistributionGroup(group, members);
                buddyListPanel.highlightGatewayConnectorGroup(group);
                buddyListPanel.expandListPanel();
                
//                if (buddyListPanel instanceof PitsCastGroupListPanel){
//                    talker.handlePitsCastGroupAdded(group, members);
//                }
                
                confirmBuddyGroupNamesAfterOperation();
                
                dispose();
            }
        }
    }

    private void prepareForSaveGroup(ListModel model,
            IGatewayConnectorGroup group,
            ArrayList<IGatewayConnectorBuddy> members) 
    {
        group.setGroupDescription(jGroupDescription.getText());

        Object obj;
        for (int i = 0; i < model.getSize(); i++) {
            obj = model.getElementAt(i);
            if (obj instanceof IGatewayConnectorBuddy) {
                if(buddyListPanel instanceof RegularBuddyListPanel){
                    //regular buddy-list only permit buddy displaying in one place
                    buddyListPanel.deleteBuddyFromDistGroups((IGatewayConnectorBuddy) (obj));
                }
                members.add((IGatewayConnectorBuddy) (obj));
            }
        }

    }

    private void editGroupHelper() {
        final String groupName = jGroupName.getText().trim();
        final ListModel model = jGroupMemberList.getModel();
        if (model.getSize() == 0) {
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), "Please choose members for this group.");
        } else {
            IGatewayConnectorGroup group = GatewayBuddyListFactory.getDistributionGroupInstance(talker.getKernel().getPointBoxLoginUser(), groupName);
            ArrayList<IGatewayConnectorBuddy> members = new ArrayList<IGatewayConnectorBuddy>();
            prepareForSaveGroup(model, group, members);

            ArrayList<IGatewayConnectorBuddy> avalibleMembers = prepareForUnSavedGroup(members, group);// get a List that's made up of the members who had not belonged to any group.
            buddyListPanel.editDistributionGroup(group, members);
            
            if (!(buddyListPanel instanceof DistributionBuddyListPanel)) {
                IGatewayConnectorGroup newGroup = GatewayBuddyListFactory.getDistributionGroupInstance(talker.getKernel().getPointBoxLoginUser(), "Recent Buddies");
                ArrayList<IGatewayConnectorBuddy> previousMembers = buddyListPanel.getBuddiesOfGroup(newGroup);   //previous group Members
                ArrayList<IGatewayConnectorBuddy> totalRecentMembers = combineMembers(previousMembers, avalibleMembers); //combine for total Recent Buddys group
                if(totalRecentMembers!=null && totalRecentMembers.size()>0){
                    buddyListPanel.editDistributionGroup(newGroup, totalRecentMembers);  //create the new group "Recent Buddies" with the members who don't have group
                }
            }
            buddyListPanel.highlightGatewayConnectorGroup(group);
            
            confirmBuddyGroupNamesAfterOperation();
            
            dispose();
        }
    }

    private ArrayList<IGatewayConnectorBuddy> combineMembers(ArrayList<IGatewayConnectorBuddy> members1, ArrayList<IGatewayConnectorBuddy> members2) {
        ArrayList<IGatewayConnectorBuddy> returnMembers = new ArrayList<IGatewayConnectorBuddy>();
        for (IGatewayConnectorBuddy buddy : members1) {
            returnMembers.add(buddy);
        }
        for (IGatewayConnectorBuddy buddy : members2) {
            if (!members1.contains(buddy)) {
                returnMembers.add(buddy);
            }
        }
        return returnMembers;
    }

    private ArrayList<IGatewayConnectorBuddy> prepareForUnSavedGroup(ArrayList<IGatewayConnectorBuddy> members, IGatewayConnectorGroup group) {
        ArrayList<IGatewayConnectorBuddy> returnMembers = new ArrayList<IGatewayConnectorBuddy>();
        ArrayList<IGatewayConnectorBuddy> groupMembers = buddyListPanel.getBuddiesOfGroup(group);
        for (IGatewayConnectorBuddy member : groupMembers) {
            if (!members.contains(member)) {
                returnMembers.add(member);
            }
        }
        return returnMembers;
    }

    private void deleteGroupHelper() {
        final String groupName = jGroupName.getText().trim();
        boolean deletePermitted = true;
        if (buddyListPanel instanceof RegularBuddyListPanel) {
            if (groupName.equalsIgnoreCase(PbcReservedTerms.PbBuddyDefaultGroup.toString())) {
                JOptionPane.showMessageDialog(talker.getPointBoxFrame(), 
                        "This reserved group cannot be deleted.");
                return;
            }
            final ListModel model = jGroupMemberList.getModel();
            if (model.getSize() != 0) {
                deletePermitted = false;
            }
        }
        if (deletePermitted) {
            final IPointBoxDistributionGroup group = GatewayBuddyListFactory.getDistributionGroupInstance(talker.getKernel().getPointBoxLoginUser(), groupName);
            buddyListPanel.deleteDistributionGroup(group);
            GatewayBuddyListFactory.destroyDistributionGroupInstance(group);
            talker.closeGroupTabInFloatingDistributionFrame(group);
            
            confirmBuddyGroupNamesAfterOperation();
            
        } else {
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), "Cannot delete this group. Please move or delete all the members of this group first.");
        }
        
        dispose();
    }

    private boolean selectMemberFromList(String buddyUniqueName, JList jMemberList) {
        boolean memberSelected = false;
        ListModel model = jMemberList.getModel();
        Object obj;
        for (int i = 0; i < model.getSize(); i++) {
            obj = model.getElementAt(i);
            if (obj instanceof IGatewayConnectorBuddy) {
                if (((IGatewayConnectorBuddy) obj).getIMUniqueName().equalsIgnoreCase(buddyUniqueName)) {
                    jMemberList.setSelectedIndex(i);
                    memberSelected = true;
                    break;
                }
            }
        }
        return memberSelected;
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
        jAvailableMemberList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jAddSelectedBuddies = new javax.swing.JButton();
        jAddAllMembers = new javax.swing.JButton();
        jRemoveSelectedMembers = new javax.swing.JButton();
        jRemoveAllMembers = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jGroupMemberList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jGroupName = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jGroupDescription = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jMessage = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jAddEditDelete = new javax.swing.JButton();
        jCancel = new javax.swing.JButton();

        setTitle("Distribution Group Control");

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jAvailableMemberList.setName("jAvailableMemberList"); // NOI18N
        jAvailableMemberList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jAvailableMemberListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jAvailableMemberList);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(4, 1, 0, 10));

        jAddSelectedBuddies.setName("jAddSelectedBuddies"); // NOI18N
        jAddSelectedBuddies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddSelectedBuddiesActionPerformed(evt);
            }
        });
        jPanel1.add(jAddSelectedBuddies);

        jAddAllMembers.setName("jAddAllMembers"); // NOI18N
        jAddAllMembers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddAllMembersActionPerformed(evt);
            }
        });
        jPanel1.add(jAddAllMembers);

        jRemoveSelectedMembers.setName("jRemoveSelectedMembers"); // NOI18N
        jRemoveSelectedMembers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRemoveSelectedMembersActionPerformed(evt);
            }
        });
        jPanel1.add(jRemoveSelectedMembers);

        jRemoveAllMembers.setName("jRemoveAllMembers"); // NOI18N
        jRemoveAllMembers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRemoveAllMembersActionPerformed(evt);
            }
        });
        jPanel1.add(jRemoveAllMembers);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jGroupMemberList.setName("jGroupMemberList"); // NOI18N
        jGroupMemberList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jGroupMemberListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jGroupMemberList);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText("Group Name: ");
        jLabel1.setName("jLabel1"); // NOI18N

        jGroupName.setName("jGroupName"); // NOI18N
        jGroupName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jGroupNameKeyReleased(evt);
            }
        });

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jGroupDescription.setColumns(20);
        jGroupDescription.setRows(5);
        jGroupDescription.setName("jGroupDescription"); // NOI18N
        jScrollPane3.setViewportView(jGroupDescription);

        jLabel4.setText("Description:");
        jLabel4.setName("jLabel4"); // NOI18N

        jMessage.setText("Note: please login your IMs for available buddies ");
        jMessage.setName("jMessage"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jMessage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane3)
                            .addComponent(jGroupName, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jMessage)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText("Available Buddies:");
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText("Group Members:");
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jAddEditDelete.setText("Save");
        jAddEditDelete.setName("jAddEditDelete"); // NOI18N
        jAddEditDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddEditDeleteActionPerformed(evt);
            }
        });
        jPanel3.add(jAddEditDelete);

        jCancel.setText("Cancel");
        jCancel.setName("jCancel"); // NOI18N
        jCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelActionPerformed(evt);
            }
        });
        jPanel3.add(jCancel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(134, 134, 134)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(68, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jCancelActionPerformed

    private void jAddEditDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAddEditDeleteActionPerformed
        processCommand();
    }//GEN-LAST:event_jAddEditDeleteActionPerformed

    private void jAddSelectedBuddiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAddSelectedBuddiesActionPerformed
        moveSelectedMembers(jAvailableMemberList, jGroupMemberList);
    }//GEN-LAST:event_jAddSelectedBuddiesActionPerformed

    private void jAddAllMembersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAddAllMembersActionPerformed
        moveAllMembers(jAvailableMemberList, jGroupMemberList);
    }//GEN-LAST:event_jAddAllMembersActionPerformed

    private void jRemoveSelectedMembersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRemoveSelectedMembersActionPerformed
        moveSelectedMembers(jGroupMemberList, jAvailableMemberList);
    }//GEN-LAST:event_jRemoveSelectedMembersActionPerformed

    private void jRemoveAllMembersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRemoveAllMembersActionPerformed
        moveAllMembers(jGroupMemberList, jAvailableMemberList);
    }//GEN-LAST:event_jRemoveAllMembersActionPerformed

    private void jAvailableMemberListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jAvailableMemberListMouseClicked
        if (evt.getClickCount() >= 2) {
            moveSelectedMembers(jAvailableMemberList, jGroupMemberList);
        }
    }//GEN-LAST:event_jAvailableMemberListMouseClicked

    private void jGroupMemberListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jGroupMemberListMouseClicked
        if (evt.getClickCount() >= 2) {
            moveSelectedMembers(jGroupMemberList, jAvailableMemberList);
        }
    }//GEN-LAST:event_jGroupMemberListMouseClicked

    private void jGroupNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jGroupNameKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            processCommand();
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jGroupNameKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAddAllMembers;
    private javax.swing.JButton jAddEditDelete;
    private javax.swing.JButton jAddSelectedBuddies;
    private javax.swing.JList jAvailableMemberList;
    private javax.swing.JButton jCancel;
    private javax.swing.JTextArea jGroupDescription;
    private javax.swing.JList jGroupMemberList;
    private javax.swing.JTextField jGroupName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jMessage;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton jRemoveAllMembers;
    private javax.swing.JButton jRemoveSelectedMembers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

    private void processCommand() {
        switch (purpose) {
            case AddGroup:
                addNewGroupHelper();
                break;
            case EditGroup:
                editGroupHelper();
                break;
            case DeleteGroup:
                deleteGroupHelper();
                break;
        }
        talker.updatePitsCastCheckTree();
    }

    class BuddyListCellRenderer extends JLabel implements ListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if ((value != null) && (value instanceof IGatewayConnectorBuddy)) {
                this.setOpaque(true);
                setFont(SwingGlobal.getLabelFont());
                IGatewayConnectorBuddy buddy = (IGatewayConnectorBuddy) value;
                ImageIcon logoImg = getImageSettings().getBuddyImageIcon(buddy);
                if (logoImg == null) {
                    setText(buddy.getNickname() + " ("+buddy.getBuddyGroupName()+")");
                } else {
                    setIcon(logoImg);
                    setText(buddy.getNickname() + " ("+buddy.getBuddyGroupName()+")");
                }
                if (isSelected) {
                    setBackground(SwingGlobal.getColor(SwingGlobal.ColorName.DARK_BLUE));
                    setForeground(Color.white);
                } else {
                    setBackground(Color.white);
                    if (buddy.getBuddyStatus().equals(BuddyStatus.Online)) {
                        setForeground(Color.black);
                    } else {
                        setForeground(Color.DARK_GRAY);
                    }
                }
            }
            return this;
        }
    }

    static enum Purpose {

        AddGroup("AddGroup"),
        EditGroup("EditGroup"),
        DeleteGroup("DeleteGroup");
        private String term;

        Purpose(String term) {
            this.term = term;
        }

        public static Purpose convertToType(String term) {
            if (term == null) {
                return null;
            }
            if (term.equalsIgnoreCase("AddGroup")) {
                return AddGroup;
            } else if (term.equalsIgnoreCase("EditGroup")) {
                return EditGroup;
            } else if (term.equalsIgnoreCase("DeleteGroup")) {
                return DeleteGroup;
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return term;
        }
    }
}
