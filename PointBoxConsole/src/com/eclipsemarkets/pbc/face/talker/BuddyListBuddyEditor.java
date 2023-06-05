/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BuddyListBuddyEditor.java
 *
 * Created on Dec 8, 2011, 2:26:51 PM
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.BuddyItemRemovedEvent;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.web.PointBoxServiceResult;
import com.eclipsemarkets.web.PointBoxWebServiceResponse;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Zhijun Zhang
 */
class BuddyListBuddyEditor extends JDialog{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(BuddyListBuddyEditor.class.getName());
    }
    public static BuddyListBuddyEditor createBuddyListBuddyEditorInstance(IPbcTalker talker, 
                                                                        IBuddyListPanel buddyListPanel,
                                                                        IGatewayConnectorBuddy targetBuddy, 
                                                                        IGatewayConnectorGroup targetGroup, 
                                                                        Purpose purpose, 
                                                                        boolean modal)
    {
        return new BuddyListBuddyEditor(talker, buddyListPanel, targetBuddy, targetGroup, purpose, modal);
    }

    private IPbcTalker talker;
    private DefaultListModel distGroupModel;
    
    private IBuddyListPanel buddyListPanel;
    
    private IGatewayConnectorBuddy targetBuddy;
    private IGatewayConnectorGroup targetGroup;
    private Purpose purpose;
    
    private BuddyListBuddyEditor(IPbcTalker talker, 
                        IBuddyListPanel buddyListPanel,
                        IGatewayConnectorBuddy targetBuddy, 
                        IGatewayConnectorGroup targetGroup, 
                        Purpose purpose, 
                        boolean modal) 
    {
        initComponents();
        distGroupModel = new DefaultListModel();
        this.talker = talker;
        this.buddyListPanel = buddyListPanel;
        this.targetBuddy = targetBuddy;
        this.targetGroup = targetGroup;
        this.purpose = purpose;
        
        jEditBuddy.setText(purpose.toString());
        ((TitledBorder)jBuddyPane.getBorder()).setTitle(purpose.toString());
        jDistGroups.setModel(getDistGroupModel());
        switch (purpose){
            case AddBuddy:
                ((TitledBorder)jBuddyPane.getBorder()).setTitle("Add:");
                jBuddyNameLabel.setText("New Buddy Name:");
                jDistGroups.setEnabled(true);
                jBuddyName.setEditable(true);
                jBuddyName.setText("");
                setTitle("Add Buddy:");
                ((TitledBorder)jGroupScrollPane.getBorder()).setTitle("Select A Group:");
                break;
            case MoveBuddy:
                ((TitledBorder)jBuddyPane.getBorder()).setTitle("Current Group: \"" + targetGroup.getGroupName() + "\"");
                jBuddyNameLabel.setText("Existing Buddy Name:");
                jDistGroups.setEnabled(true);
                jBuddyName.setEditable(false);
                jBuddyName.setText(targetBuddy.getIMScreenName());
                setTitle("Change Buddy's Group: ");
                ((TitledBorder)jGroupScrollPane.getBorder()).setTitle("Select Another Group:");
                break;
            case DeleteBuddy:
                ((TitledBorder)jBuddyPane.getBorder()).setTitle("Delete:");
                jBuddyNameLabel.setText("Existing Buddy Name:");
                jDistGroups.setEnabled(false);
                jBuddyName.setEditable(false);
                jBuddyName.setText(targetBuddy.getIMScreenName());
                addAvailableDistGroup(targetGroup);
                setTitle("Delete Buddy:");
                ((TitledBorder)jGroupScrollPane.getBorder()).setTitle("From Group(s):");
                break;
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b){
            setLocation(SwingGlobal.getCenterPointOfParentWindow(talker.getKernel().getPointBoxMainFrame(), this));
        }
    }

    public IPbcTalker getTalker() {
        return talker;
    }

    public final DefaultListModel getDistGroupModel() {
        return distGroupModel;
    }
    
    final void addAvailableDistGroup(final IGatewayConnectorGroup group){
        if (SwingUtilities.isEventDispatchThread()){
            addAvailableDistGroupHelper(group);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    addAvailableDistGroupHelper(group);
                }
            });
        }
    }
    
    private void addAvailableDistGroupHelper(final IGatewayConnectorGroup group){
        if (group == null){
            return;
        }
        GroupWrapper wrapper = new GroupWrapper(group);
        ((DefaultListModel)jDistGroups.getModel()).addElement(wrapper);
        switch(purpose){
            case AddBuddy:
                jDistGroups.setSelectedIndex(0);
                break;
            case MoveBuddy:
                if (group.equals(targetGroup)){
                    jDistGroups.setSelectedValue(wrapper, false);
                }
                break;
            case DeleteBuddy:
                jDistGroups.setSelectedValue(wrapper, false);
                break;
        }
    }

    private void handleAddBuddyHelper() {
        final String buddyName = jBuddyName.getText();
        if (DataGlobal.isEmptyNullString(buddyName)){
            JOptionPane.showMessageDialog(this, "Please give a new name for the buddy.");
            return;
        }
        final Object groupObj = jDistGroups.getSelectedValue();
        if (!(groupObj instanceof GroupWrapper)){
            JOptionPane.showMessageDialog(this, "Please select groups for " + buddyName + ".");
            return;
        }
        //When adding a new buddy, we  we guarantee it always has a group assigned and give a default group "Friends" for the buddy
        final IGatewayConnectorBuddy buddy = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(buddyListPanel.getMasterLoginUser(), 
                                                                                                      buddyName, 
                                                                                                      ((GroupWrapper)groupObj).getGroup().getGroupName(), 
                                                                                                      talker.getKernel().getPointBoxConsoleRuntime());
        if(buddy.getUniqueNickname().equalsIgnoreCase(talker.getPointBoxLoginUser().getUniqueNickname())){
            JOptionPane.showMessageDialog(this, "Cannot add self as a buddy.");
            return;
        }
        if (buddyListPanel.confirmBuddyPresentedInList(buddy) != null){
            JOptionPane.showMessageDialog(this, "Buddy " + buddyName + " has been existing on the list. "
                    + "Please give a new buddy name or click such a buddy to change his/her group.");
            jBuddyName.setText("");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, 
                "Add " + buddyName + " into selected group(s) now?", 
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            (new SwingWorker<String, Void>(){
                @Override
                protected String doInBackground() throws Exception {
                    //getTalker().addNewBuddyIntoDistributionGroup(null, null);
                    IGatewayConnectorBuddy loginUser = buddyListPanel.getMasterLoginUser();
                    PointBoxWebServiceResponse response;
                    //distribution list...
                    String msg;
                    buddyListPanel.addNewBuddyIntoDistGroup(buddy, ((GroupWrapper)groupObj).getGroup());

                    //add it into servers....
                    response = getTalker().getKernel().addNewConnectorBuddy(loginUser, 
                                                                            GatewayBuddyListFactory.getGatewayConnectorGroupInstance(loginUser, buddy.getBuddyGroupName()), 
                                                                            buddy.getIMScreenName());
                    if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
                        msg = "Add new buddy " + buddy.getIMScreenName() + " into account " + loginUser.getIMScreenName() + " of " + buddy.getIMServerType() + ".";
                    }else{
                        msg = "Failed to add new buddy " + buddy.getIMScreenName() + " into account " + loginUser.getIMScreenName() + " of " + buddy.getIMServerType() + ".";
                    }
                    return msg;
                }

                @Override
                protected void done() {
                    String msg = "Result: ";
                    try {
                        msg = msg + get();
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    JOptionPane.showMessageDialog(BuddyListBuddyEditor.this, msg);
                    BuddyListBuddyEditor.this.dispose();
                }
            }).execute();
        }
    }

    private void handleMoveBuddyHelper() {
        if (targetBuddy == null){
            JOptionPane.showMessageDialog(this, "This buddy is unknown.");
            dispose();
            return;
        }
        if (targetGroup == null){
            JOptionPane.showMessageDialog(this, "This buddy was not associated with any distribution groups.");
            dispose();
            return;
        }
        final Object groupObj = jDistGroups.getSelectedValue();
        IGatewayConnectorGroup newGroup = ((GroupWrapper)groupObj).getGroup();
        if ((newGroup == null) || (targetGroup.equals(newGroup))){
            JOptionPane.showMessageDialog(this, "Please select a groups which is different from " + targetGroup.getGroupName() + ".");
            return;
        }
        buddyListPanel.deleteBuddyFromDistGroups(targetBuddy);
        buddyListPanel.addNewBuddyIntoDistGroup(targetBuddy, newGroup);
        
        JOptionPane.showMessageDialog(BuddyListBuddyEditor.this, "Result: move buddy "+targetBuddy.getIMScreenName() +" to "+"group "+newGroup.getGroupName());
        BuddyListBuddyEditor.this.dispose();
    }

    private void handleDeleteBuddyHelper() {
        if (targetBuddy == null){
            JOptionPane.showMessageDialog(this, "This buddy is unknown.");
            dispose();
            return;
        }
        if (targetBuddy.getLoginOwner() == null)
        {
            JOptionPane.showMessageDialog(this, "You need login " + targetBuddy.getIMServerType() + " before you delete it.");
            dispose();
            return;
        }
//        if (!(BuddyStatus.Online.equals(targetBuddy.getLoginOwner().getBuddyStatus())))
//        {
//            JOptionPane.showMessageDialog(this, "You need login " + targetBuddy.getLoginOwner().getIMUniqueName() + " before you delete it.");
//            dispose();
//            return;
//        }
        if (targetGroup == null){
            JOptionPane.showMessageDialog(this, "This buddy was not associated with any distribution groups.");
            dispose();
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete " + targetBuddy.getIMScreenName() 
                + " from all the associated distribution group(s) and its login account " 
                + targetBuddy.getLoginOwner().getIMUniqueName() + " right now?", 
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            (new SwingWorker<String, Void>(){
                @Override
                protected String doInBackground() throws Exception {
                    //distribution list...
                    String msg;
                    //delete it from servers....
                    PointBoxWebServiceResponse response = getTalker().getKernel().deleteConnectorBuddy(targetBuddy.getLoginOwner(), 
                            targetBuddy, GatewayBuddyListFactory.getDistributionGroupInstance(targetBuddy.getLoginOwner(), targetBuddy.getBuddyGroupName()));
                    if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
                        msg = "Deleted buddy " + targetBuddy.getIMScreenName() + " from account " + targetBuddy.getLoginOwner().getIMScreenName() + " of " + targetBuddy.getIMServerType() + ".";
                        talker.getKernel().raisePointBoxEvent(
                                            new BuddyItemRemovedEvent(PointBoxEventTarget.PbcFace,
                                                                      targetBuddy.getLoginOwner(),
                                                                      targetBuddy));
                    }else{
                        msg = "Failed to delete buddy " + targetBuddy.getIMScreenName() + " from account " + targetBuddy.getLoginOwner().getIMScreenName() + " of " + targetBuddy.getIMServerType() + ".";
                    }
                    return msg;
                }

                @Override
                protected void done() {
                    String msg = "Result: ";
                    try {
                        msg = msg + get();
                        JOptionPane.showMessageDialog(BuddyListBuddyEditor.this, msg);
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    BuddyListBuddyEditor.this.dispose();
                }
            }).execute();
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

        jPanel6 = new javax.swing.JPanel();
        jBuddyPane = new javax.swing.JPanel();
        jBuddyNameLabel = new javax.swing.JLabel();
        jBuddyName = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jGroupScrollPane = new javax.swing.JScrollPane();
        jDistGroups = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jEditBuddy = new javax.swing.JButton();
        jCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Buddy:");
        setResizable(false);

        jBuddyPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Add New Buddy:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        jBuddyNameLabel.setText("New Buddy Name:");

        jPanel4.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        javax.swing.GroupLayout jBuddyPaneLayout = new javax.swing.GroupLayout(jBuddyPane);
        jBuddyPane.setLayout(jBuddyPaneLayout);
        jBuddyPaneLayout.setHorizontalGroup(
            jBuddyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBuddyPaneLayout.createSequentialGroup()
                .addGroup(jBuddyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jBuddyPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jBuddyNameLabel))
                    .addGroup(jBuddyPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jBuddyName, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jBuddyPaneLayout.setVerticalGroup(
            jBuddyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBuddyPaneLayout.createSequentialGroup()
                .addComponent(jBuddyNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBuddyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jGroupScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select A Group:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        jDistGroups.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jDistGroups.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jGroupScrollPane.setViewportView(jDistGroups);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBuddyPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jGroupScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBuddyPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jGroupScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        jEditBuddy.setText("Add Buddy");
        jEditBuddy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditBuddyActionPerformed(evt);
            }
        });
        jPanel1.add(jEditBuddy);

        jCancel.setText("Cancel");
        jCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jCancel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jCancelActionPerformed

    private void jEditBuddyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEditBuddyActionPerformed
        switch (purpose){
            case AddBuddy:
                handleAddBuddyHelper();
                break;
            case MoveBuddy:
                handleMoveBuddyHelper();
                break;
            case DeleteBuddy:
                handleDeleteBuddyHelper();
                break;
        }
    }//GEN-LAST:event_jEditBuddyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jBuddyName;
    private javax.swing.JLabel jBuddyNameLabel;
    private javax.swing.JPanel jBuddyPane;
    private javax.swing.JButton jCancel;
    private javax.swing.JList jDistGroups;
    private javax.swing.JButton jEditBuddy;
    private javax.swing.JScrollPane jGroupScrollPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    // End of variables declaration//GEN-END:variables

    static enum Purpose {
        AddBuddy("Add Buddy"),
        MoveBuddy("Move Buddy"),
        DeleteBuddy("Remove Buddy");

        private String term;
        Purpose(String term){
            this.term = term;
        }

        @Override
        public String toString() {
            return term;
        }
    }
    
    
    private class GroupWrapper {
        private IGatewayConnectorGroup group;

        public GroupWrapper(IGatewayConnectorGroup distGroup) {
            this.group = distGroup;
        }

        @Override
        public String toString() {
            if (group == null){
                return super.toString();
            }else{
                return group.getGroupName();
            }
        }

        public IGatewayConnectorGroup getGroup() {
            return group;
        }
    }
    
    private class BuddyWrapper {
        private IGatewayConnectorBuddy buddy;

        public BuddyWrapper(IGatewayConnectorBuddy buddy) {
            this.buddy = buddy;
        }

        @Override
        public String toString() {
            if (buddy == null){
                return super.toString();
            }else{
                return buddy.getIMUniqueName();
            }
        }

        public IGatewayConnectorBuddy getBuddy() {
            return buddy;
        }
    }
}
