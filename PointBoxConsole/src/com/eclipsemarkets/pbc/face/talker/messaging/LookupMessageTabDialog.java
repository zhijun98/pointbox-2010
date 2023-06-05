/**
 * Eclipse Market Solutions LLC
 */
/*
 * LookupMessageTabDialog.java
 *
 * @author Zhijun Zhang
 * Created on Jul 27, 2010, 1:23:10 PM
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.face.talker.IBuddyListPanel;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

/**
 *
 * @author Zhijun Zhang
 */
class LookupMessageTabDialog extends javax.swing.JDialog {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(LookupMessageTabDialog.class.getName());
    }
    private static final long serialVersionUID = 1L;

    private MasterMessagingBoard masterMessagingBoard;
    private IPbcTalker talker;

    LookupMessageTabDialog(IPbcTalker talker,
                          boolean modal,
                          MasterMessagingBoard tabManager)
    {
        super(talker.getPointBoxFrame(), modal);
        initComponents();
        this.talker = talker;
        this.masterMessagingBoard = tabManager;
        
        jMessageTabList.setModel(new DefaultListModel());
        jMessageTabList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jMessageTabList.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    MasterMessagingBoard board = LookupMessageTabDialog.this.masterMessagingBoard;
                    board.hideButtonTabComponent((ButtonTabComponent)jMessageTabList.getSelectedValue());
                    MessagingPaneManager.getSingleton(board.getTalker())
                            .popupMessagingTabFromMaster(((ButtonTabComponent)jMessageTabList.getSelectedValue()).getTabUniqueID(), false);
                    LookupMessageTabDialog.this.setVisible(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
    }

    @Override
    public void setVisible(boolean value){
        super.setVisible(value);
        if (value){
            setLocation(SwingGlobal.getCenterPointOfParentWindow(talker.getPointBoxFrame(), this));
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

        jPanel1 = new javax.swing.JPanel();
        jAOL = new javax.swing.JCheckBox();
        jPB = new javax.swing.JCheckBox();
        jYahoo = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jTabName = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLookupBtn = new javax.swing.JButton();
        jCloseBtn = new javax.swing.JButton();
        jAll = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMessageTabList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Lookup Message Tab:");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lookup Openned Message Tab:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jAOL.setSelected(true);
        jAOL.setText("AOL");
        jAOL.setEnabled(false);
        jAOL.setName("jAOL"); // NOI18N

        jPB.setSelected(true);
        jPB.setText("PointBox");
        jPB.setEnabled(false);
        jPB.setName("jPB"); // NOI18N

        jYahoo.setSelected(true);
        jYahoo.setText("Yahoo");
        jYahoo.setEnabled(false);
        jYahoo.setName("jYahoo"); // NOI18N

        jLabel1.setText("Buddy name contains: ");
        jLabel1.setName("jLabel1"); // NOI18N

        jTabName.setName("jTabName"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        jLookupBtn.setText("Lookup");
        jLookupBtn.setName("jLookupBtn"); // NOI18N
        jLookupBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLookupBtnActionPerformed(evt);
            }
        });
        jPanel2.add(jLookupBtn);

        jCloseBtn.setText("Close");
        jCloseBtn.setName("jCloseBtn"); // NOI18N
        jCloseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseBtnActionPerformed(evt);
            }
        });
        jPanel2.add(jCloseBtn);

        jAll.setSelected(true);
        jAll.setText("ALL");
        jAll.setName("jAll"); // NOI18N
        jAll.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jAllItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAll)
                    .addComponent(jYahoo)
                    .addComponent(jPB)
                    .addComponent(jAOL)
                    .addComponent(jLabel1)
                    .addComponent(jTabName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jAOL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jYahoo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAll)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lookup Results:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jMessageTabList.setName("jMessageTabList"); // NOI18N
        jScrollPane1.setViewportView(jMessageTabList);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 169, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.getAccessibleContext().setAccessibleName("Lookup Opened Message Tab:");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCloseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseBtnActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jCloseBtnActionPerformed

    private void jLookupBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLookupBtnActionPerformed
        jMessageTabList.setModel(new DefaultListModel());
        ArrayList<IBuddyListPanel> panels;
        if (jAll.isSelected()){
            panels = talker.getBuddyListTreePanels(GatewayServerType.AIM_SERVER_TYPE);
            for (IBuddyListPanel panel : panels){
                lookupTabFromIMHelper(panel.getMasterLoginUser());
            }
            panels = talker.getBuddyListTreePanels(GatewayServerType.PBIM_SERVER_TYPE);
            for (IBuddyListPanel panel : panels){
                lookupTabFromIMHelper(panel.getMasterLoginUser());
            }
            panels = talker.getBuddyListTreePanels(GatewayServerType.YIM_SERVER_TYPE);
            for (IBuddyListPanel panel : panels){
                lookupTabFromIMHelper(panel.getMasterLoginUser());
            }
        }else{
            if (jAOL.isSelected()){
                panels = talker.getBuddyListTreePanels(GatewayServerType.AIM_SERVER_TYPE);
                for (IBuddyListPanel panel : panels){
                    lookupTabFromIMHelper(panel.getMasterLoginUser());
                }
            }
            if (jPB.isSelected()){
                panels = talker.getBuddyListTreePanels(GatewayServerType.PBIM_SERVER_TYPE);
                for (IBuddyListPanel panel : panels){
                    lookupTabFromIMHelper(panel.getMasterLoginUser());
                }
            }
            if (jYahoo.isSelected()){
                panels = talker.getBuddyListTreePanels(GatewayServerType.YIM_SERVER_TYPE);
                for (IBuddyListPanel panel : panels){
                    lookupTabFromIMHelper(panel.getMasterLoginUser());
                }
            }
        }
    }//GEN-LAST:event_jLookupBtnActionPerformed

    private void jAllItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jAllItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jAOL.setEnabled(false);
            jPB.setEnabled(false);
            jYahoo.setEnabled(true);
        }
        if (evt.getStateChange() == ItemEvent.DESELECTED){
            jAOL.setEnabled(true);
            jPB.setEnabled(true);
            jYahoo.setEnabled(true);
        }
    }//GEN-LAST:event_jAllItemStateChanged

    private void lookupTabFromIMHelper(final IGatewayConnectorBuddy connectorLoginUser){
        (new SwingWorker<ArrayList<IButtonTabComponent>, Void>() {
            @Override
            protected ArrayList<IButtonTabComponent> doInBackground() throws Exception {
                return MessagingPaneManager.getSingleton().searchButtonTabComponentsFromStateStorage(jTabName.getText().trim(), connectorLoginUser, true);
            }
            @Override
            protected void done() {
                ArrayList<IButtonTabComponent> tabButtons;
                try {
                    tabButtons = get();
                    if (tabButtons != null){
                        DefaultListModel listModel = (DefaultListModel)jMessageTabList.getModel();
                        for (int i = 0; i != tabButtons.size(); ++i) {
                            listModel.addElement(tabButtons.get(i));
                        }
                        //jMessageTabList.updateUI();
                    }//if
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                } catch (ExecutionException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }).execute();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAOL;
    private javax.swing.JCheckBox jAll;
    private javax.swing.JButton jCloseBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jLookupBtn;
    private javax.swing.JList jMessageTabList;
    private javax.swing.JCheckBox jPB;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTabName;
    private javax.swing.JCheckBox jYahoo;
    // End of variables declaration//GEN-END:variables

}