/**
 * Eclipse Market Solutions LLC
 */
/*
 * GroupMessagingConfirmDialog.java
 *
 * @author Zhijun Zhang
 * Created on Jun 25, 2010, 11:48:33 AM
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.lang.GatewayTerms;
import com.eclipsemarkets.pbc.CopyCutPastePopupMenu;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.checktree.EmsCheckTreeFactory;
import com.eclipsemarkets.pbc.face.checktree.IEmsCheckTreePanel;
import com.eclipsemarkets.pbc.face.checktree.IEmsCheckTreeSelectionListener;
import com.eclipsemarkets.pbc.face.checktree.IEmsCheckNode;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang
 */
class GroupMessagingConfirmDialog extends javax.swing.JDialog implements IGroupMessagingConfirmDialog,
                                                                         IEmsCheckTreeSelectionListener
{
    private static final long serialVersionUID = 1L;

    private IPbcTalker talker;
    //private String groupUniqueName;
    private IGatewayConnectorGroup group;
    private ArrayList<IGatewayConnectorBuddy> buddies;
    private IEmsCheckTreePanel emsCheckTreePanel;

    private CopyCutPastePopupMenu ccpPopup;
    
    /**
     * 
     * @param system
     * @param groupUniqueName
     * @param buddies
     * @param pointBoxFrame
     * @param modal
     */
    GroupMessagingConfirmDialog(IPbcTalker talker,
                                IGatewayConnectorGroup group,
                                ArrayList<IGatewayConnectorBuddy> buddies,
                                Window window,
                                boolean modal)
    {
        super(talker.getPointBoxFrame(), modal);
        initComponents();

        this.talker = talker;
        this.group = group;

        this.buddies = buddies;

        initializeEmsCheckTreePanel();
        
        ccpPopup = new CopyCutPastePopupMenu();

        jMessageEntry.addMouseListener(new MouseAdapter() {
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
                    ccpPopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        jMembersZone.setViewportView(emsCheckTreePanel.getBaseTree());

        setTitle("Broadcasting to " + getGroupUniqueName());
        
        setLocationRelativeTo(window);

    }

    private void initializeEmsCheckTreePanel() {
        if (group == null){
            emsCheckTreePanel = EmsCheckTreeFactory.createEmsCheckTreePanelComponentInstance(GatewayTerms.DistributionGroupName.toString(), true, talker.getKernel());
        }else{
            emsCheckTreePanel = EmsCheckTreeFactory.createEmsCheckTreePanelComponentInstance(group.getIMUniqueName(), true, talker.getKernel());
        }
        emsCheckTreePanel.addCheckTreeSelectionListener(this);

        IEmsCheckNode groupNode = emsCheckTreePanel.getRootCheckNode();
        IEmsCheckNode memberNode;
        IGatewayConnectorBuddy aMember;
        IPbconsoleImageSettings imageSettings = talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
        for (int j = 0; j < buddies.size(); j++){
            aMember = buddies.get(j);
            memberNode = emsCheckTreePanel.createNewBuddyCheckNode(aMember,
                                                                   imageSettings.getConnectorBuddyIcon(aMember.getIMServerType()));
            memberNode.setAssociatedObject(aMember);
            groupNode.addChildCheckNode(memberNode);
        }//for
        emsCheckTreePanel.checkAllTreeNodes();
        emsCheckTreePanel.getBaseTree().updateUI();
    }
    
    public void updateStatus(String status) {
        
    }

//    public boolean isAnonymousLogin() {
//        return pointBoxFrame.isAnonymousLogin();
//    }

    public ArrayList<IGatewayConnectorBuddy> getGroupMembers() {
        return buddies;
    }

    public void displayConsole() {
        displayConsole(null);
    }

    public void displayConsole(final String message) {
        if (SwingUtilities.isEventDispatchThread()){
            displayConsoleHelper(message);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    displayConsoleHelper(message);
                }
            });
        }
    }
    private void displayConsoleHelper(String message) {
        if ((message == null) || (message.isEmpty())){
            this.jMessageEntry.setText("");
        }else{
            this.jMessageEntry.setText(message);
        }
        setVisible(true);
    }

    public void hideConsole() {
        if (SwingUtilities.isEventDispatchThread()){
            setVisible(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    setVisible(false);
                }
            });
        }
    }

    public Window getAWTWindow() {
        return this;
    }

    public void confirmForBroadcasting(String message) {
        //if (this.jCheckBox1.isSelected()){
            tryToBroadcastHelper(message);
        //}else{
        //    displayConsole(message);
        //}
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMessageZone = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMessageEntry = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jBroadcastButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jMembersZone = new javax.swing.JScrollPane();

        setResizable(false);

        jMessageZone.setName("jMessageZone"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jMessageEntry.setName("jMessageEntry"); // NOI18N
        jMessageEntry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jMessageEntryKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jMessageEntry);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(2, 1, 0, 3));

        jBroadcastButton.setText("Broadcast");
        jBroadcastButton.setName("jBroadcastButton"); // NOI18N
        jBroadcastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBroadcastButtonActionPerformed(evt);
            }
        });
        jPanel1.add(jBroadcastButton);

        jCancelButton.setText("Cancel");
        jCancelButton.setName("jCancelButton"); // NOI18N
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(jCancelButton);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jMessageZoneLayout = new javax.swing.GroupLayout(jMessageZone);
        jMessageZone.setLayout(jMessageZoneLayout);
        jMessageZoneLayout.setHorizontalGroup(
            jMessageZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMessageZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jMessageZoneLayout.setVerticalGroup(
            jMessageZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMessageZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jMessageZone, java.awt.BorderLayout.PAGE_END);

        jPanel2.setName("jPanel2"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jMembersZone.setName("jMembersZone"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 307, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jMembersZone, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 151, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jMembersZone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelButtonActionPerformed
        hideConsole();
    }//GEN-LAST:event_jCancelButtonActionPerformed

    private void jBroadcastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBroadcastButtonActionPerformed
        tryToBroadcastHelper(jMessageEntry.getText());
    }//GEN-LAST:event_jBroadcastButtonActionPerformed

    private void jMessageEntryKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jMessageEntryKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            //todo: do we really want to offer this shortcut in the confirmation stage?
            //tryToBroadcastHelper(jMessage.getText());
        }
    }//GEN-LAST:event_jMessageEntryKeyReleased

    private void tryToBroadcastHelper(String msg) {
        if ((msg == null) || (msg.trim().isEmpty())){
            JOptionPane.showMessageDialog(talker.getPointBoxFrame(), "Please type in your message first ...");
        }else{
            ArrayList<IGatewayConnectorBuddy> checkedBuddies = emsCheckTreePanel.retrieveCheckedBuddiesForBroadcast();
            if (checkedBuddies.isEmpty()){
                JOptionPane.showMessageDialog(talker.getPointBoxFrame(), "Please choose who you are broadcasting to ...");
            }else{
                talker.distributeInstantMessages(msg.trim(), group, checkedBuddies);
                hideConsole();
            }
        }
    }

    public String getGroupUniqueName() {
        if (group == null){
            return GatewayTerms.DistributionGroupName.toString();
        }else{
            return group.getIMUniqueName();
        }
    }

    /**
     * @see IEmsCheckTreeSelectionListener
     * @param aCheckNode
     */
    @Override
    public void checkNodeSelected(IEmsCheckNode aCheckNode) {
        Object obj = aCheckNode.getAssociatedObject();
        if ((obj != null) && (obj instanceof IGatewayConnectorBuddy)){
            if (BuddyStatus.Offline.equals(((IGatewayConnectorBuddy)obj).getBuddyStatus())){
                if (JOptionPane.showConfirmDialog(talker.getPointBoxFrame(),
                                                  "This member is offline. Send messages to this member anyway?",
                                                  "Offline member:",
                                                  JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                {
                    return;
                }
            }
            aCheckNode.setSelected(true);
        }
    }

    public ArrayList<IGatewayConnectorBuddy> getCheckedGroupMembers(){
        ArrayList<IGatewayConnectorBuddy> checkedMembers = new ArrayList<IGatewayConnectorBuddy>();
        IEmsCheckNode groupNode = emsCheckTreePanel.getRootCheckNode();
        Enumeration children = groupNode.getChildrenEnumeration();
        IEmsCheckNode node;
        while (children.hasMoreElements()){
            node = (IEmsCheckNode)(children.nextElement());
            if (node.isSelected()){
                checkedMembers.add((IGatewayConnectorBuddy)(node.getAssociatedObject()));
            }
        }

        return checkedMembers;
    }

    /**
     * @see IEmsCheckTreeSelectionListener
     * @param aCheckNode
     */
    public void checkNodeUnselected(IEmsCheckNode aCheckNode) {
        aCheckNode.setSelected(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBroadcastButton;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JScrollPane jMembersZone;
    private javax.swing.JTextPane jMessageEntry;
    private javax.swing.JPanel jMessageZone;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
