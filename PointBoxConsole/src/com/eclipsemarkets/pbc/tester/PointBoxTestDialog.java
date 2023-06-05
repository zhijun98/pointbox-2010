/**
 * Eclipse Market Solutions LLC
 */
/*
 * ServerLoginDialog.java
 *
 * @author Zhijun Zhang
 * Created on May 17, 2010, 12:06:07 PM
 */

package com.eclipsemarkets.pbc.tester;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.talker.AbstractTalker;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.face.talker.ServerLoginDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;


/**
 *
 * @author Fang Bao
 */
public class PointBoxTestDialog extends javax.swing.JDialog 
{
    private static final long serialVersionUID = 1L;

    /**
     * Owner of this dialog
     */
    private final IPbcTalker talker;
    private static PointBoxTestDialog self;

    static {
        self = null;
    }
    
    /**
     * Creates new form ServerLoginDialog
     * @param system
     * @param pointBoxFrame
     * @param modal
     * @param customizer
     */
    private PointBoxTestDialog(final IPbcTalker talker)
    {   
        initComponents();

        this.talker = talker;
        
        jPBUser.setText(talker.getKernel().getPointBoxLoginUser().getIMScreenName());
        jConnectedAOLAccountNames.setModel(((ServerLoginDialog)((AbstractTalker)talker).getAolLoginDialog()).getjConnectedAccountNames().getModel());  
        jConnectedAOLAccountNames.setCellRenderer(new ConnectedAccountListRenderer());
        jConnectedAOLAccountNames.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    jConnectedYahooAccountNames.clearSelection();
                }
                if (evt.getClickCount() == 2) {
                          displayTestParamDialog(jConnectedAOLAccountNames);
                }
            }
        });
        jConnectedYahooAccountNames.setModel(((ServerLoginDialog)((AbstractTalker)talker).getYahooLoginDialog()).getjConnectedAccountNames().getModel());
        jConnectedYahooAccountNames.setCellRenderer(new ConnectedAccountListRenderer());
        jConnectedYahooAccountNames.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    jConnectedAOLAccountNames.clearSelection();
                }
                if (evt.getClickCount() == 2) {
                          displayTestParamDialog(jConnectedYahooAccountNames);
                }
            }
        });
        jTestCases.setModel(new DefaultListModel());
        jTestCases.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource(); 
                if (evt.getClickCount() == 2) {
                         int index = list.locationToIndex(evt.getPoint());
                         popOutDialog(index);
                }
            }
        });
   
    }
    
    private void popOutDialog(int index) {
               PointBoxTesterCaseDialog dialog=(PointBoxTesterCaseDialog)((TestCaseWrapper)jTestCases.getModel().getElementAt(index)).getTestCase();
               dialog.setVisible(true);
     }

    public static PointBoxTestDialog getSingletonInstance(IPbcTalker talker)
    {
        if(self==null){
            self= new PointBoxTestDialog(talker);

            self.initializeTestDialog();

            return self;
        }
        return self;
    }


    /**
     * initialize default values (before login)
     */
    private void initializeTestDialog(){
        if (SwingUtilities.isEventDispatchThread()){
            initializeTestDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    initializeTestDialogHelper();
                }
            });
        }
    }
    private void initializeTestDialogHelper(){
        setTitle("PointBox Tester");

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                hideFaceComponent();
            }
        });

        pack();
        setResizable(false);
        setModal(true);
        super.setVisible(false);
    }


    @Override
    public void setVisible(boolean value) {
        if (value){
            displayFaceComponent();
        }else{
            hideFaceComponent();
        }
    }

    public void close() {
        hideFaceComponent();
    }

    public void display() {
        displayFaceComponent();
    }

    

    public void displayFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            displayFaceComponentHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayFaceComponentHelper();
                }
            });
        }
    }

    private void displayFaceComponentHelper(){
         
        pack();

        super.setVisible(true);
        
        setLocation(SwingGlobal.getCenterPointOfParentWindow(talker.getPointBoxFrame(), this));
    }
    
    

    public void hideFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            hideFaceComponentHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    hideFaceComponentHelper();
                }
            });
        }
    }

    private void hideFaceComponentHelper(){       
        super.setVisible(false);
      
    }

  

    public void releaseFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            dispose();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    dispose();
                }
            });
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

        jBasePanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jConnectedYahooAccountNames = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jNewYahooTest = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTestCases = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jCheck = new javax.swing.JButton();
        jRemove = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jNewPBTest = new javax.swing.JButton();
        jPBUser = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jConnectedAOLAccountNames = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        jNewAOLTest = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jBasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "PBC Tester", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 18), new java.awt.Color(0, 0, 0))); // NOI18N
        jBasePanel.setName("jBasePanel"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Yahoo Connected Accounts:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), java.awt.Color.blue)); // NOI18N
        jPanel2.setForeground(new java.awt.Color(0, 51, 204));
        jPanel2.setToolTipText("");
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jConnectedYahooAccountNames.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jConnectedYahooAccountNames.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jConnectedYahooAccountNames.setName("jConnectedYahooAccountNames"); // NOI18N
        jScrollPane2.setViewportView(jConnectedYahooAccountNames);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(1, 2, 1, 0));

        jNewYahooTest.setText("NEW TEST");
        jNewYahooTest.setName("jNewYahooTest");
        jNewYahooTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNewYahooTestActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jNewYahooTest, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jNewYahooTest, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(133, 133, 133)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Test Cases:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), java.awt.Color.blue)); // NOI18N
        jPanel4.setForeground(new java.awt.Color(0, 51, 204));
        jPanel4.setName("jPanel4");

        jScrollPane3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane3.setName("jScrollPane3");

        jTestCases.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jTestCases.setForeground(new java.awt.Color(0, 153, 51));
        jTestCases.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTestCases.setName("jTestCases");
        jScrollPane3.setViewportView(jTestCases);

        jPanel5.setName("jPanel5");
        jPanel5.setLayout(new java.awt.GridLayout(1, 2, 1, 0));

        jCheck.setText("CHECK");
        jCheck.setName("jCheck");
        jCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckActionPerformed(evt);
            }
        });

        jRemove.setText("REMOVE");
        jRemove.setName("jRemove");
        jRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(jCheck)
                                .addGap(36, 36, 36)
                                .addComponent(jRemove)))
                        .addGap(0, 32, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(20, 20, 20)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current PB Account:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), java.awt.Color.blue)); // NOI18N
        jPanel1.setForeground(new java.awt.Color(0, 51, 204));
        jPanel1.setName("jPanel1");

        jNewPBTest.setText("NEW TEST");
        jNewPBTest.setName("jNewPBTest");
        jNewPBTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNewPBTestActionPerformed(evt);
            }
        });

        jPBUser.setFont(new java.awt.Font("Times New Roman", 2, 16)); // NOI18N
        jPBUser.setForeground(java.awt.Color.red);
        jPBUser.setText("PBUserName");
        jPBUser.setName("jPBUser");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPBUser, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jNewPBTest, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPBUser, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jNewPBTest, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current AOL Connected Accounts:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), java.awt.Color.blue)); // NOI18N
        jPanel6.setForeground(java.awt.Color.blue);
        jPanel6.setName("jPanel6");

        jScrollPane4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane4.setName("jScrollPane4");

        jConnectedAOLAccountNames.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jConnectedAOLAccountNames.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jConnectedAOLAccountNames.setName("jConnectedAOLAccountNames");
        jScrollPane4.setViewportView(jConnectedAOLAccountNames);

        jPanel7.setName("jPanel7");
        jPanel7.setLayout(new java.awt.GridLayout(1, 2, 1, 0));

        jNewAOLTest.setText("NEW TEST");
        jNewAOLTest.setName("jNewAOLTest");
        jNewAOLTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNewAOLTestActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jNewAOLTest, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jNewAOLTest, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(133, 133, 133)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jBasePanelLayout = new javax.swing.GroupLayout(jBasePanel);
        jBasePanel.setLayout(jBasePanelLayout);
        jBasePanelLayout.setHorizontalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jBasePanelLayout.createSequentialGroup()
                        .addGroup(jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jBasePanelLayout.setVerticalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jBasePanelLayout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jBasePanelLayout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel4.getAccessibleContext().setAccessibleName("Current AOL Connected Accounts:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jNewAOLTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNewAOLTestActionPerformed
        displayTestParamDialog(jConnectedAOLAccountNames);
    }//GEN-LAST:event_jNewAOLTestActionPerformed

    private void jNewYahooTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNewYahooTestActionPerformed
        displayTestParamDialog(jConnectedYahooAccountNames);
    }//GEN-LAST:event_jNewYahooTestActionPerformed

    private void jNewPBTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNewPBTestActionPerformed
       displayTestParamDialogHelper2();
    }//GEN-LAST:event_jNewPBTestActionPerformed

    private void jCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckActionPerformed
        int index=jTestCases.getSelectedIndex();
        if(index<0){
            JOptionPane.showMessageDialog(this, "Please select a test case!");
            return;
        }
        popOutDialog(index);
    }//GEN-LAST:event_jCheckActionPerformed

    private void jRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRemoveActionPerformed
        int index=jTestCases.getSelectedIndex();
        if(index<0){
            JOptionPane.showMessageDialog(this, "Please select a test case!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, 
                                                  "Are you sure to remove this test case?",
                                                  "Confirm", JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION)
            removeTestCase(index);
    }//GEN-LAST:event_jRemoveActionPerformed

    private void removeTestCase(int index){
        TestCaseWrapper wrapper=(TestCaseWrapper)jTestCases.getModel().getElementAt(index);
        wrapper.getTestCase().setRunning(false);
        ((DefaultListModel)jTestCases.getModel()).remove(index);
    }
    private void displayTestParamDialog(final javax.swing.JList jConnectedAccountNames){
         if (SwingUtilities.isEventDispatchThread()){
             displayTestParamDialogHelper(jConnectedAccountNames);
         }else{
             SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                   displayTestParamDialogHelper(jConnectedAccountNames);
                }
            });
         }
    }

    private void displayTestParamDialogHelper(javax.swing.JList jConnectedAccountNames){
         synchronized (jConnectedAccountNames.getModel()){
            int index = jConnectedAccountNames.getSelectedIndex();
            if (index > -1){
                ServerLoginDialog.LoginUserWrapper loginUserWrapper = (ServerLoginDialog.LoginUserWrapper)jConnectedAccountNames.getModel().getElementAt(index);
                
                    createTest(loginUserWrapper.getLoginUser());
                    //super.setVisible(true);
               
            }else{
                JOptionPane.showMessageDialog(this, "Please select a connected account for test!");
            }
        }
    }
    
    private void displayTestParamDialogHelper2(){
        createTest(talker.getKernel().getPointBoxLoginUser());
    }
  


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jBasePanel;
    private javax.swing.JButton jCheck;
    private javax.swing.JList jConnectedAOLAccountNames;
    private javax.swing.JList jConnectedYahooAccountNames;
    private javax.swing.JButton jNewAOLTest;
    private javax.swing.JButton jNewPBTest;
    private javax.swing.JButton jNewYahooTest;
    private javax.swing.JLabel jPBUser;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JButton jRemove;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JList jTestCases;
    // End of variables declaration//GEN-END:variables

    private void createTest(IGatewayConnectorBuddy loginUser) {
        (new PointBoxTesterCaseDialog(this,talker,true,loginUser)).setVisible(true);
    }

    /**
     * @return the jTestCases
     */
    public javax.swing.JList getjTestCases() {
        return jTestCases;
    }
    
    
    class ConnectedAccountListRenderer extends DefaultListCellRenderer {

        Color originalLabelForeground;
        ConnectedAccountListRenderer() {
            originalLabelForeground = this.getBackground();
        }
        
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            ServerLoginDialog.LoginUserWrapper loginUserWrapper = (ServerLoginDialog.LoginUserWrapper)value;
            IGatewayConnectorBuddy loginUser=loginUserWrapper.getLoginUser();
            setIcon(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getBuddyImageIcon(loginUser));
            setText(loginUser.getNickname());
            setFont(SwingGlobal.getLabelFont());
            
            if(isSelected){
                setBackground(Color.YELLOW);
            }else{
                setBackground(originalLabelForeground);
            }
            return this;
        }
        
    }
      
}
