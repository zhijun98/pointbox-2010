/**
 * Eclipse Market Solutions LLC
 */
/*
 * PointBoxTesterCaseDialog.java
 *
 * @author Fang Bao
 * Created on Jun 11, 2012, 1:31:59 PM
 */

package com.eclipsemarkets.pbc.tester;

import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.PbcReleaseUserType;
import com.eclipsemarkets.pbc.face.PbsysFileFilter;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.storage.merm.IMermQuoteRetriever;
import com.eclipsemarkets.pbc.storage.merm.MermStorageFactory;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Fang Bao
 */
public class PointBoxTesterCaseDialog extends javax.swing.JDialog {

    public static String accessPath;
    
    private static final long serialVersionUID = 1L;
    
    private final IPbcTalker talker;

    private IGatewayConnectorBuddy loginUser;
       
    private PointBoxTestDialog pointBoxTestDialog;
    
    private boolean running;
    
    private JFileChooser fileChooser;
    
    private TestCaseWrapper wrapper;

    /** Creates new form PointBoxTesterCaseDialog
     * @param system
     * @param parent
     * @param modal
     * @param loginUser
     */
    public PointBoxTesterCaseDialog(PointBoxTestDialog pointBoxTestDialog, IPbcTalker talker, boolean modal, IGatewayConnectorBuddy loginUser) {
        super(pointBoxTestDialog, modal);
        initComponents();
        
        fileChooser = new JFileChooser();

        this.pointBoxTestDialog=pointBoxTestDialog;
        
        this.talker = talker;
        
        this.loginUser=loginUser;

        populateBrokersInEDT();

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setTitle("PBC Tester");
        
        jReceivers.setSelectedIndex(-1);
        
        jStatus.setText("Stopped");
        jStatus.setForeground(Color.RED);
        jAccessPath.setText(accessPath);
        jStop.setEnabled(false);
        setResizable(false);
    }

    public JDialog getBaseDialog() {
        return this;
    }
    
    @Override
    public void setVisible(boolean value) {
        super.setVisible(value);
        if (value){
            setLocation(SwingGlobal.getCenterPointOfParentWindow(talker.getPointBoxFrame(), this));
        }
    }

    private void populateBrokersInEDT() {
        if (SwingUtilities.isEventDispatchThread()){
            populateBrokersInEDTHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateBrokersInEDTHelper();
                }
            });
        }
    }
    private void populateBrokersInEDTHelper() {
        jReceivers.removeAllItems();
        (new SwingWorker<Void, IGatewayConnectorBuddy>(){
            @Override
            protected Void doInBackground() throws Exception {
                ArrayList<IGatewayConnectorBuddy> buddyList =GatewayBuddyListFactory.getSortedBuddyList(loginUser);
                //publish
                for (IGatewayConnectorBuddy buddy : buddyList){
                    publish(buddy);
                }
                return null;
            }

            @Override
            protected void process(List<IGatewayConnectorBuddy> chunks) {
                for (IGatewayConnectorBuddy buddy : chunks){
                    if(!isBuddyExsitedInList(buddy, jReceivers))
                        jReceivers.addItem(buddy);
                }
            }

            @Override
            protected void done() {
                jReceivers.insertItemAt(" ", 0);
                jReceivers.setSelectedIndex(-1);
            }
        }).execute();
    }

 
    /*
     * Judge whether the buddy existed in the JList 
     */
    private boolean isBuddyExsitedInList(IGatewayConnectorBuddy buddy, javax.swing.JComboBox list){
        for(int i=0;i<list.getModel().getSize();i++){
               if(buddy.equals(list.getModel().getElementAt(i)))
                   return true;           
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel8 = new javax.swing.JPanel();
        jFilterLabel = new javax.swing.JLabel();
        jReceivers = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMessageFrequency = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jAccessPathSelect = new javax.swing.JButton();
        jAccessPath = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMessageContent = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jStart = new javax.swing.JButton();
        jStop = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel8.setName("jPanel8"); // NOI18N

        jFilterLabel.setText("Message Receiver:");
        jFilterLabel.setName("jFilterLabel"); // NOI18N

        jReceivers.setName("jReceivers"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        jLabel1.setText("Message Frequency:");
        jLabel1.setName("jLabel1");

        jMessageFrequency.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jMessageFrequency.setText("5");
        jMessageFrequency.setName("jMessageFrequency");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("MERM Access Database:"));
        jPanel2.setName("jPanel2");

        jLabel2.setText("Access Full Path:");
        jLabel2.setName("jLabel2");

        jAccessPathSelect.setText("File Path");
        jAccessPathSelect.setName("jAccessPathSelect");
        jAccessPathSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAccessPathSelectActionPerformed(evt);
            }
        });

        jAccessPath.setEditable(false);
        jAccessPath.setName("jAccessPath");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 280, Short.MAX_VALUE)
                        .addComponent(jAccessPathSelect))
                    .addComponent(jAccessPath, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jAccessPathSelect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAccessPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setName("jScrollPane1");

        jMessageContent.setColumns(20);
        jMessageContent.setLineWrap(true);
        jMessageContent.setRows(5);
        jMessageContent.setText("Test message. Please ignore this garbage message.");
        jMessageContent.setName("jMessageContent");
        jScrollPane1.setViewportView(jMessageContent);

        jLabel3.setText("Message Content:");
        jLabel3.setName("jLabel3");

        jLabel4.setText("Tester Status:");
        jLabel4.setName("jLabel4");

        jStart.setText("Start");
        jStart.setName("jStart");
        jStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartActionPerformed(evt);
            }
        });

        jStop.setText("Stop");
        jStop.setName("jStop");
        jStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStopActionPerformed(evt);
            }
        });

        jLabel5.setText("s / msg");
        jLabel5.setName("jLabel5");

        jStatus.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jStatus.setText("Stop");
        jStatus.setName("jStatus");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 492, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                                .addGap(291, 291, 291)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel8Layout.createSequentialGroup()
                                        .addComponent(jStart, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jStop, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel8Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jFilterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jReceivers, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jMessageFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jReceivers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFilterLabel)
                    .addComponent(jLabel1)
                    .addComponent(jMessageFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jStatus))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jStop, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                            .addComponent(jStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );

        jPanel2.getAccessibleContext().setAccessibleName("Test Access Database:");

        getContentPane().add(jPanel8, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jAccessPathSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAccessPathSelectActionPerformed
        if (filePathSelect(jAccessPath)){
            accessPath=jAccessPath.getText();
        }
    }//GEN-LAST:event_jAccessPathSelectActionPerformed

    private boolean filePathSelect(JTextField jFilePathField){
        String existingDirectory = jFilePathField.getText();
        String newPath = getFileFromFileChooserWithLastDirectory(new PbsysFileFilter("mdb"), existingDirectory);
        File selectedPathFile = new File(newPath);
        if (selectedPathFile.exists() && (newPath.length() > 0)) {
            if (!newPath.equalsIgnoreCase(jFilePathField.getText())){
                if (JOptionPane.showConfirmDialog(this, 
                                                  "This file path will apply for all test cases. Do you want to change it now?",
                                                  "Confirm", JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION)
                {
                    if (newPath.endsWith(".mdb")){
                        jFilePathField.setText(newPath);
                    }
                    return true;
                }
            }
        } else {
            //JOptionPane.showMessageDialog(this, "The directory or file you chose does not exist.");
        }
        return false;
    }
    
    String getFileFromFileChooserWithLastDirectory(PbsysFileFilter filter, String lastSelectedPath){
        String result;
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        if (filter != null){
            fileChooser.setFileFilter(filter);
        }

        if (!lastSelectedPath.isEmpty()){
            File existingDirectoryOrFile = new File(lastSelectedPath);
            if (existingDirectoryOrFile.isDirectory()){
                fileChooser.setCurrentDirectory(existingDirectoryOrFile);
            }
            else if (existingDirectoryOrFile.isFile()){
                fileChooser.setSelectedFile(existingDirectoryOrFile);
            }
        }

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            result = fileChooser.getSelectedFile().getAbsolutePath();
        }
        else{
            return "";
        }

        if (filter != null && !result.isEmpty() && !result.substring(result.lastIndexOf("."),result.length()).equalsIgnoreCase(filter.getDescription())){
            return result + filter.getDescription();
        }

        return result;
    }
    
    
    private void jStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartActionPerformed
      if(!validationInEDT()){
            JOptionPane.showMessageDialog(this, "Please input correct params!");
            return;
        }
        startTest();
        changeUItoUnAvaliable();
    }//GEN-LAST:event_jStartActionPerformed

    private void jStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStopActionPerformed
        stopTest();
        changeUItoAvaliable();
        
    }//GEN-LAST:event_jStopActionPerformed

    public boolean isDouble( String input )   
    {   
        try  
        {   
            Double.parseDouble( input );   
            return true;   
        }   
        catch( Exception e)   
        {   
            return false;   
        }   
    }  

    private boolean validationInEDT(){
         if(jReceivers.getSelectedItem()==null){
            return false;
        }
         if(jMessageFrequency.getText().trim().isEmpty()||(!isDouble(jMessageFrequency.getText()))){
             return false;
         }
         if(Double.parseDouble(jMessageFrequency.getText())<1){  // can not be less than 1 s
             return false;
         }
         if(jMessageContent.getText().trim().isEmpty()){
             return false;
         }
         return true;
    }
    private void startTest(){
        if(wrapper==null){
            wrapper=new TestCaseWrapper(loginUser, (IGatewayConnectorBuddy)jReceivers.getSelectedItem(), "running...", this) ;
            DefaultListModel dModel=(DefaultListModel)pointBoxTestDialog.getjTestCases().getModel();

            dModel.addElement(wrapper);
        }else{
            wrapper.setStatus("running...");
            wrapper.setReceiver((IGatewayConnectorBuddy)jReceivers.getSelectedItem());
        }
        
        running=true;
        new Thread(new SendMessageThread()).start();
    }
    
    private void stopTest() {
        wrapper.setStatus("stopped");
        running=false;
    }
    
    private void changeUItoUnAvaliable(){
        jReceivers.setEnabled(false);
        jMessageContent.setEditable(false);
        jMessageFrequency.setEditable(false);
        jAccessPathSelect.setEnabled(false);
        jStart.setEnabled(false);
        jStop.setEnabled(true);
        jStatus.setText("Running...");
        jStatus.setForeground(Color.GREEN);
    }
    
    private void changeUItoAvaliable(){
        jReceivers.setEnabled(true);
        jMessageContent.setEditable(true);
        jMessageFrequency.setEditable(true);
        jAccessPathSelect.setEnabled(true);
        jStart.setEnabled(true);
        jStop.setEnabled(false);
        jStatus.setText("Stopped");
        jStatus.setForeground(Color.RED);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jAccessPath;
    private javax.swing.JButton jAccessPathSelect;
    private javax.swing.JLabel jFilterLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextArea jMessageContent;
    private javax.swing.JTextField jMessageFrequency;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JComboBox jReceivers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jStart;
    private javax.swing.JLabel jStatus;
    private javax.swing.JButton jStop;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }


    class SendMessageThread implements Runnable{
        
        public SendMessageThread() {
            
        }
        
        @Override
        public void run() {
            IMermQuoteRetriever mermQuoteRetriever = MermStorageFactory.createMermQuoteRetrieverInstance(accessPath);
            ArrayList<String> msgs = new ArrayList<String>();
            if (mermQuoteRetriever != null){
                msgs = mermQuoteRetriever.retrieveAllHistoricalMessages(PbcReleaseUserType.MERM_USERS);
            }
            long interval=(long)Double.parseDouble(jMessageFrequency.getText())*1000;
            if ((msgs == null) || (msgs.isEmpty())){
                while(isRunning()){
                    talker.sendMessageToBuddy(loginUser, (IGatewayConnectorBuddy)jReceivers.getSelectedItem(), jMessageContent.getText(), null,true);
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PointBoxTesterCaseDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else{
                for (String msg : msgs){
                    talker.sendMessageToBuddy(loginUser, (IGatewayConnectorBuddy)jReceivers.getSelectedItem(), msg, null,true);
                    final String finalMsg = msg;
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            jMessageContent.setText(finalMsg);
                        }
                    });
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PointBoxTesterCaseDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
    }
}
