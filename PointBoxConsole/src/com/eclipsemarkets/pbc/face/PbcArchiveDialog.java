/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PbcArchiveDialog.java
 *
 * Created on Dec 4, 2010, 7:05:26 PM
 */
package com.eclipsemarkets.pbc.face;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.global.XMLGlobal;
import com.eclipsemarkets.global.exceptions.XMLSemanticsException;
import com.eclipsemarkets.pbc.PbcProperties;
import static com.eclipsemarkets.pbc.face.PbcArchiveFormat.FaceTime;
import static com.eclipsemarkets.pbc.face.PbcArchiveFormat.GlobalRelay;
import static com.eclipsemarkets.pbc.face.PbcArchiveFormat.PlainText;
import static com.eclipsemarkets.pbc.face.PbcArchiveStatus.Start;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Zhijun Zhang
 */
class PbcArchiveDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcArchiveDialog.class.getName());
    }

    private final String defaultNotes = "Please choose the folder in which the archived file will be stored."
            + NIOGlobal.lineSeparator() +  NIOGlobal.lineSeparator() + "If you select FaceTime format, the "
            + "archive folder should be the location where FaceTime server collects data. You may consult "
            + "FaceTime server administrator for this information.";
    
    private final String DEFAULT_ARCHIVE_FOLDER = "archive";
    
    private IPbcFace face;
    private final ArrayList<IPbsysInstantMessage> messageBuffer;
    private JFileChooser fileChooser;
    
    private Thread faceTimeArchiveThread;
    private Thread plainTextArchiveThread;
//    private Thread globalRelayArchiveThread;

    /** Creates new form PbcArchiveDialog
     * @param face
     */
    PbcArchiveDialog(final IPbcFace face) {
        super(face.getPointBoxMainFrame(), true);
        initComponents();
        
        this.face = face;
        messageBuffer = new ArrayList<IPbsysInstantMessage>();
        
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Archive Folder");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setApproveButtonText("Select");
        
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });
        this.setModal(false);
        IPbcRuntime runtime = this.getPbcRuntime();
        TreeSet<String> grUserAccountSet = face.getKernel().retrieveGlobalRelayAccountSet();
        if (grUserAccountSet.contains(face.getPointBoxLoginUser().getIMScreenName())){
            runtime.setArchiveFormat(PbcArchiveFormat.GlobalRelay);
            runtime.setArchiveStatus(PbcArchiveStatus.Start);
        }
        customizeDialogByPropertiesHelper(runtime);
//        jGlobalRelay.setVisible(false);
        notifyServerOfArchiveInfo();
        
        initializeDisclaimerButton();
    }
    
    private void startArchiveDialog(){
        if (SwingUtilities.isEventDispatchThread()){
            startArchiveDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    startArchiveDialogHelper();
                }
            });
        }
    }
    
    private void startArchiveDialogHelper(){
        IPbcRuntime runtime = getPbcRuntime();
        String archiveLocation = jArchiveFolder.getText();
        jArchiveFolder.setForeground(Color.black);
        
        if ((!NIOGlobal.isValidFolder(archiveLocation)) && (!jGlobalRelay.isSelected())){
            File newFolder = SwingGlobal.getFolderPatchByFileChooser(fileChooser, archiveLocation);
            if (NIOGlobal.isValidFolder(newFolder)) {
                jArchiveFolder.setText(newFolder.getAbsolutePath());
                jArchiveFolder.setForeground(Color.blue);
                archiveLocation = jArchiveFolder.getText();
                runtime.setArchiveLocation(archiveLocation);
            } else {
                JOptionPane.showMessageDialog(this, "Please choose a proper location for archiving messages.");
                return;
            }
        }           
        //default format
        PbcArchiveFormat archiveFormat = PbcArchiveFormat.PlainText;
        if (jFaceTime.isSelected()){
            archiveFormat = PbcArchiveFormat.FaceTime;
            if ((faceTimeArchiveThread == null) || (!faceTimeArchiveThread.isAlive())){
                faceTimeArchiveThread = new Thread(new FaceTimeArchivingLoop());
                faceTimeArchiveThread.start();
                jPlainText.setEnabled(false);
                jGlobalRelay.setEnabled(false);
            }
        } else if (jPlainText.isSelected()) {
            archiveFormat = PbcArchiveFormat.PlainText;
            if ((plainTextArchiveThread == null) || (!plainTextArchiveThread.isAlive())){
                plainTextArchiveThread = new Thread(new PlainTextArchivingLoop());
                plainTextArchiveThread.start();
                jFaceTime.setEnabled(false);
                jGlobalRelay.setEnabled(false);
            }
        } else if (jGlobalRelay.isSelected()) {
            //logger.log(Level.INFO, "Lets us global relay!");
            archiveFormat = PbcArchiveFormat.GlobalRelay;
            jPlainText.setEnabled(false);
            jFaceTime.setEnabled(false);
        }
        runtime.setArchiveFormat(archiveFormat);
        runtime.setArchiveStatus(PbcArchiveStatus.Start);
        
        ((TitledBorder)jArchivePanel.getBorder()).setTitle("Current Status: " + PbcArchiveStatus.Start);
        jStart.setEnabled(false);
        jStop.setEnabled(true);
        
        this.notifyServerOfArchiveInfo();
        face.notifyArchiveMethodChanged();
    }
    
    /**
     * Set the archive folder to be empty and clear prop
     * @param stopReason 
     */
    private void stopArchiveDialog(final String stopReason){
        if (SwingUtilities.isEventDispatchThread()){
            stopArchiveDialogHelper(stopReason);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    stopArchiveDialogHelper(stopReason);
                }
            });
        }
    }
    private void stopArchiveDialogHelper(final String stopReason){   
        interruptArchivingThreads();
        
        getPbcRuntime().setArchiveStatus(PbcArchiveStatus.Stop);
        ((TitledBorder)jArchivePanel.getBorder()).setTitle("Current Status: " + PbcArchiveStatus.Stop);
        //jFaceTime.setSelected(true);
        if (DataGlobal.isEmptyNullString(stopReason)){
            jArchiveFolder.setText(defaultNotes);
        }else{
            jArchiveFolder.setText(stopReason);
        }
        jStart.setEnabled(true);
        jStop.setEnabled(false);
                
        jPlainText.setEnabled(true);
        jFaceTime.setEnabled(true);
        jGlobalRelay.setEnabled(true);
        
        this.notifyServerOfArchiveInfo();
    }
    
    /**
     * Send current archive information set by this dialog to the server-side
     */
    public void notifyServerOfArchiveInfo(){
        IPbcRuntime runtime = getPbcRuntime();
        this.face.getKernel().requestToSetupPointBoxClientArchive(getPbcRuntime().getPointBoxAccountID(), 
                                                                  runtime.getArchiveFormat(),
                                                                  runtime.getArchiveStatus());
    }
    
    /**
     * Whether or not an automatic archive (face-time or message format) is set up ready. 
     * When the main frame is initialized, this method will be called so as to pop up this 
     * dialog for users to confirm archiving operation.
     * @return 
     */
    boolean isReady() {
        boolean result;
        String archiveFolder;
        IPbcRuntime runtime = getPbcRuntime();
        archiveFolder = runtime.getArchiveLocation();
        if ((DataGlobal.isEmptyNullString(archiveFolder)) || (!NIOGlobal.isValidFolder(archiveFolder))){
            result = false;
        }else{
            PbcArchiveStatus status;
            status = runtime.getArchiveStatus();
            switch (status){
                case Start:
                    result = true;
                    break;
                default:
                    result = false;
            }
        }
        if (!result){
            stopArchiveDialog(jArchiveFolder.getText());
        }
        return result;
    }
    
    @Override
    public void setVisible(boolean value) {
        if (value){
            setLocation(SwingGlobal.getCenterPointOfParentWindow(face.getPointBoxMainFrame(), this));
        }
        super.setVisible(value);
    }
    
    private IPbcRuntime getPbcRuntime(){
        return face.getKernel().getPointBoxConsoleRuntime();
    }
    
    private void customizeDialogByPropertiesHelper(IPbcRuntime runtime){
        String archiveFolder = runtime.getArchiveLocation();
        if ((DataGlobal.isEmptyNullString(archiveFolder)) || (!NIOGlobal.isValidFolder(archiveFolder))){
            if (!NIOGlobal.isValidFolder(DEFAULT_ARCHIVE_FOLDER)) {
                NIOGlobal.createFolder(DEFAULT_ARCHIVE_FOLDER);
            }
            archiveFolder = new File(DEFAULT_ARCHIVE_FOLDER).getAbsolutePath();
            runtime.setArchiveLocation(archiveFolder);
        }
        jArchiveFolder.setText(archiveFolder);
        PbcArchiveFormat format = runtime.getArchiveFormat();
        switch (format){
            case FaceTime:
                jFaceTime.setSelected(true);
                break;
            case PlainText:
                jPlainText.setSelected(true);
                break;
            case GlobalRelay:
                jGlobalRelay.setSelected(true);
                break;
            default:
                runtime.setArchiveFormat(PbcArchiveFormat.PlainText);
                jPlainText.setSelected(true);
        }
        PbcArchiveStatus status = runtime.getArchiveStatus();
        switch (status){
            case Start:
                startArchiveDialog();
                break;
            default:
                stopArchiveDialog(jArchiveFolder.getText());
                runtime.setArchiveStatus(PbcArchiveStatus.Stop);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jArchivePanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jFaceTime = new javax.swing.JRadioButton();
        jPlainText = new javax.swing.JRadioButton();
        jGlobalRelay = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jArchiveFolder = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jNotes = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jSelectArchiveFolder = new javax.swing.JButton();
        jDisclaimer = new javax.swing.JButton();
        jStart = new javax.swing.JButton();
        jStop = new javax.swing.JButton();
        jClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Archive Conversation:");
        setResizable(false);

        jArchivePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Current Status: "));
        jArchivePanel.setName("jArchivePanel"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Archive Format"));
        jPanel3.setName("jPanel3"); // NOI18N

        buttonGroup1.add(jFaceTime);
        jFaceTime.setText("FaceTime Format");
        jFaceTime.setName("jFaceTime"); // NOI18N

        buttonGroup1.add(jPlainText);
        jPlainText.setSelected(true);
        jPlainText.setText("Plain Text File");
        jPlainText.setName("jPlainText"); // NOI18N

        buttonGroup1.add(jGlobalRelay);
        jGlobalRelay.setText("Global Relay Message");
        jGlobalRelay.setName("jGlobalRelay"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jGlobalRelay)
                    .addComponent(jPlainText)
                    .addComponent(jFaceTime))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPlainText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFaceTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jGlobalRelay)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Archive Folder: "));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jArchiveFolder.setEditable(false);
        jArchiveFolder.setBackground(new java.awt.Color(236, 233, 216));
        jArchiveFolder.setColumns(20);
        jArchiveFolder.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jArchiveFolder.setLineWrap(true);
        jArchiveFolder.setRows(5);
        jArchiveFolder.setWrapStyleWord(true);
        jArchiveFolder.setBorder(null);
        jArchiveFolder.setName("jArchiveFolder"); // NOI18N
        jScrollPane1.setViewportView(jArchiveFolder);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Notes: "));
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jNotes.setBackground(new java.awt.Color(236, 233, 216));
        jNotes.setColumns(20);
        jNotes.setEditable(false);
        jNotes.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jNotes.setLineWrap(true);
        jNotes.setRows(5);
        jNotes.setText("Please choose the folder in which the archived file will be stored. \n\nIf you select FaceTime format, the archive folder should be the location where FaceTime server collects data. You may consult FaceTime server administrator for this information. ");
        jNotes.setWrapStyleWord(true);
        jNotes.setBorder(null);
        jNotes.setName("jNotes"); // NOI18N
        jScrollPane2.setViewportView(jNotes);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        jSelectArchiveFolder.setText("Set Folder");
        jSelectArchiveFolder.setName("jSelectArchiveFolder"); // NOI18N
        jSelectArchiveFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSelectArchiveFolderActionPerformed(evt);
            }
        });
        jPanel1.add(jSelectArchiveFolder);

        jDisclaimer.setText("Disable Disclaimer");
        jDisclaimer.setToolTipText("Switch on/off disclaimer message on PBC");
        jDisclaimer.setName("jDisclaimer"); // NOI18N
        jDisclaimer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDisclaimerActionPerformed(evt);
            }
        });
        jPanel1.add(jDisclaimer);

        jStart.setText("Start");
        jStart.setName("jStart"); // NOI18N
        jStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartActionPerformed(evt);
            }
        });
        jPanel1.add(jStart);

        jStop.setText("Stop");
        jStop.setName("jStop"); // NOI18N
        jStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStopActionPerformed(evt);
            }
        });
        jPanel1.add(jStop);

        jClose.setText("Close");
        jClose.setName("jClose"); // NOI18N
        jClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseActionPerformed(evt);
            }
        });
        jPanel1.add(jClose);

        javax.swing.GroupLayout jArchivePanelLayout = new javax.swing.GroupLayout(jArchivePanel);
        jArchivePanel.setLayout(jArchivePanelLayout);
        jArchivePanelLayout.setHorizontalGroup(
            jArchivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jArchivePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jArchivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(jArchivePanelLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1)))
                .addContainerGap())
        );
        jArchivePanelLayout.setVerticalGroup(
            jArchivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jArchivePanelLayout.createSequentialGroup()
                .addGroup(jArchivePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jArchivePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jArchivePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseActionPerformed
        closeDialog();
    }//GEN-LAST:event_jCloseActionPerformed

    private void closeDialog(){
        if (SwingUtilities.isEventDispatchThread()){
            closeDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    closeDialogHelper();
                }
            });
        }
    }
    
    private void closeDialogHelper(){
        if (!isReady()){
            if (JOptionPane.showConfirmDialog(this, 
                    "Stop archive instant messaging?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                setVisible(false);
            }
        }else{
            setVisible(false);
        }
    }
    
    private void jStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartActionPerformed
        this.startArchiveDialog();
    }//GEN-LAST:event_jStartActionPerformed

    private void jStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStopActionPerformed
        this.stopArchiveDialog(jArchiveFolder.getText());
    }//GEN-LAST:event_jStopActionPerformed

    private void jSelectArchiveFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSelectArchiveFolderActionPerformed
        File oldFolder = new File(jArchiveFolder.getText());
        if (!NIOGlobal.isValidFolder(oldFolder)){
            oldFolder = null;
        }
        String oldFolderPath = null;
        if (oldFolder != null){
            oldFolderPath = oldFolder.getAbsolutePath();
        }
        File newFolder = SwingGlobal.getFolderPatchByFileChooser(fileChooser, oldFolderPath);
        IPbcRuntime runtime = getPbcRuntime();
        if (newFolder != null){
            String newFolderPath = newFolder.getAbsolutePath();
            if ((oldFolderPath == null)){
                jArchiveFolder.setText(newFolder.getAbsolutePath());
                runtime.setArchiveLocation(newFolderPath);
            }else{
                if (oldFolderPath.equalsIgnoreCase(newFolderPath)){
                }else{
                    jArchiveFolder.setText(newFolder.getAbsolutePath());
                    runtime.setArchiveLocation(newFolderPath);
                }
            }
        }
    }//GEN-LAST:event_jSelectArchiveFolderActionPerformed

    private void jDisclaimerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDisclaimerActionPerformed
        IGatewayConnectorBuddy loginUser = face.getPointBoxLoginUser();
        boolean currentResult = PointBoxConsoleProperties.getSingleton().isDisclaimerMessageDisplayed(loginUser);
        if (currentResult){
            PointBoxConsoleProperties.getSingleton().setDisclaimerMessageDisplayed(loginUser, false);
        }else{
            PointBoxConsoleProperties.getSingleton().setDisclaimerMessageDisplayed(loginUser, true);
        }
        initializeDisclaimerButton();
        
        face.notifyArchiveMethodChanged();
    }//GEN-LAST:event_jDisclaimerActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextArea jArchiveFolder;
    private javax.swing.JPanel jArchivePanel;
    private javax.swing.JButton jClose;
    private javax.swing.JButton jDisclaimer;
    private javax.swing.JRadioButton jFaceTime;
    private javax.swing.JRadioButton jGlobalRelay;
    private javax.swing.JTextArea jNotes;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jPlainText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton jSelectArchiveFolder;
    private javax.swing.JButton jStart;
    private javax.swing.JButton jStop;
    // End of variables declaration//GEN-END:variables

    private ArrayList<IPbsysInstantMessage> retrieveInstantMessages(){
        
        synchronized (messageBuffer){
            ArrayList<IPbsysInstantMessage> msgs = new ArrayList<IPbsysInstantMessage>();
            for (IPbsysInstantMessage message : messageBuffer){
                msgs.add(message);
            }
            messageBuffer.clear();
            return msgs;
        }
    }

    public void bufferInstantMessages(IPbsysInstantMessage message){
        synchronized (messageBuffer){
            messageBuffer.add(message);
        }
    }

    /**
     * When PBC is closed, this method will be called
     */
    synchronized void interruptArchivingThreads() { 
        if (faceTimeArchiveThread != null){
            faceTimeArchiveThread.interrupt();
            faceTimeArchiveThread = null;
        }
        if (plainTextArchiveThread != null){
            plainTextArchiveThread.interrupt();
            plainTextArchiveThread = null;
        }
//        if (globalRelayArchiveThread != null) {
//            globalRelayArchiveThread.interrupt();
//            globalRelayArchiveThread = null;
//        }
        
    }

    private final String defaultGlobalRelayWarningMessage = "Disclaimer: All chats are recorded by Global Relay Communication.";
    private final String defaultFaceTimeWarningMessage = "Disclaimer: All chats are recorded by FaceTime.";
    private final String defaultPlainTextWarningMessage = "Disclaimer: All chats are recorded by PointBox logging agent.";
    
    String getArchiveWarningMessage() {
        IPbcRuntime runtime = this.getPbcRuntime();
        if (PbcArchiveStatus.Start.equals(runtime.getArchiveStatus())){
            switch(runtime.getArchiveFormat()){
                case GlobalRelay:
                    return defaultGlobalRelayWarningMessage;
                case FaceTime:
                    return defaultFaceTimeWarningMessage;
                case PlainText:
                    return defaultPlainTextWarningMessage;
                default:
                    return null;
            }
        }else{
            return null;
        }
    }

    private void initializeDisclaimerButton() {
        if (SwingUtilities.isEventDispatchThread()){
            initializeDisclaimerButtonHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    initializeDisclaimerButtonHelper();
                }
            });
        }
    }
    
    private void initializeDisclaimerButtonHelper(){
        boolean currentResult = PointBoxConsoleProperties.getSingleton().isDisclaimerMessageDisplayed(face.getPointBoxLoginUser());
        if (currentResult){
            jDisclaimer.setText("Disable Disclaimer");
        }else{
            jDisclaimer.setText("Enable Disclaimer");
        }
    }
    
    private class MessageList extends ArrayList<IPbsysInstantMessage> {

        private final String cssStyle = "<style id=\"color\" type=\"text/css\" media=\"all\" scoped>"
                    + ".imSendColor,\n" + ".imSendColor1 {\n" 
                    + "   color: #035;\n" + "   font-weight: bold;\n" 
                    + "}\n" + ".imSendColor2 {	\n" + "   color: #090;\n" 
                    + "   font-weight:bold;\n" + "}\n" + ".imSendColor3 {\n" 
                    + "   color: #900;\n" + "   font-weight:bold;\n" + "}\n"
                    + "</style>\n";
        
        private final String MSG_TYPE = "Mitsui-IM";
        private final String SOFTWARE_NAME = PbcProperties.getSingleton().getSoftwareName();
        private final String SOFTWARE_VERSION = PbcProperties.getSingleton().getSoftwareVersion();
        private final String SOFTWARE_RELEASE = PbcProperties.getSingleton().getBuildVersion();
        private IGatewayConnectorBuddy account = null;
        
        private Writer htmlWriter = null;
        private Writer textWriter = null;
        
        private final int PORT = 465;
        private final boolean AUTH = true;
        private final String HOST = "smtp.globalrelay.net";
        private final String USERNAME = "username@globalrelay.net";
        private final String PASSWORD = "password";
        
        private Date startDate = null;
        private Date endDate = null;
        private HashMap<IGatewayConnectorBuddy, Integer> msgCount;
        
        public MessageList(IGatewayConnectorBuddy account) {
            super();
            this.account = account;
            msgCount = new HashMap<IGatewayConnectorBuddy, Integer>();
        }
        
        @Override
        public boolean add(IPbsysInstantMessage message) {
            if (!msgCount.containsKey(message.getToUser())) {
                msgCount.put(message.getToUser(), 0);
            }
            if (!msgCount.containsKey(message.getFromUser())) {
                msgCount.put(message.getFromUser(), 0);
            }
            
            IGatewayConnectorBuddy buddy = message.getFromUser();  
            msgCount.put(buddy, msgCount.get(buddy) + 1);
            return super.add(message); 
        }
        
        @Override
        public void clear() {
            startDate = null;
            endDate = null;
            htmlWriter = null;
            textWriter = null;
            msgCount.clear();
            super.clear();
        }
        
        public int createHTMLFile(IGatewayConnectorBuddy buddy) throws IOException {
           String HTMLPath = getArchivingHTMLFileNamePath(buddy.getIMUniqueName());          
           if (this.size() == 0) {
               return 0;
           }
           try {
               initializeHTMLDoc(HTMLPath);
               for (IPbsysInstantMessage message : this) {
                   insertMessageElementHTML(message);
               }
               finalizeHTMLFile();
                               
               sendEmail(HTMLPath);
               htmlWriter = null;
               textWriter = null;
               if (DataGlobal.isNonEmptyNullString(HTMLPath)) { 
                   return this.size();
               } else {
                   PointBoxTracer.recordSevereException(logger, new Exception("Cannot find archiving path"));
               }
           } catch (IOException ex) {
               PointBoxTracer.recordSevereException(logger, ex);
           }
           return 0;
        }
        
         private void initializeHTMLDoc(String tempFileNameWithoutExtension) {
            try {
                if (htmlWriter == null) {
                    htmlWriter = new FileWriter(tempFileNameWithoutExtension + ".html");
                }
                if (textWriter == null) {
                    textWriter = new FileWriter(tempFileNameWithoutExtension + ".txt");
                }

                htmlWriter.write("<!DOCTYPE html>\n" + "<html>\n" + "  <head>\n" 
                        + "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-type\">\n"
                        + "    <title>Global Relay MessageArchive</title>\n" + "  </head>\n"
                        + "  <body>\n");
                htmlWriter.write(cssStyle);
                htmlWriter.write("<h3>Login user: " + account.getIMUniqueName() 
                            + "</h3>\n");
                htmlWriter.write("<b> " + account.getIMUniqueName() 
                            + "</b> starts conversations at "
                            + this.get(0).getMessageTimestamp().getTime()
                            + "<br>\n");
                textWriter.write("Login user: " + account.getIMUniqueName() 
                            + NIOGlobal.lineSeparator());
                textWriter.write(account.getIMUniqueName() 
                            + " starts conversations at "
                            + this.get(0).getMessageTimestamp().getTime()
                            + NIOGlobal.lineSeparator() + NIOGlobal.lineSeparator());
                htmlWriter.write("    <table border=\"1\">\n");
                htmlWriter.write("        <tr><th>User</th>\n" + 
                                 "        <th>Sent Msgs</th></tr>\n");
                textWriter.write(String.format("%-20s  %-10s" + NIOGlobal.lineSeparator(), "User", "Sent Msgs"));
                for (IGatewayConnectorBuddy buddy : msgCount.keySet()) {
                    htmlWriter.write("      <tr>\n" 
                            + "        <td>" + buddy.getIMUniqueName() + "</td>\n"
                            + "        <td>" + msgCount.get(buddy) + "</td>\n"
                            + "    </tr>\n");
                    textWriter.write(String.format("%-20s  %-3d" + NIOGlobal.lineSeparator(), 
                            buddy.getIMUniqueName(), msgCount.get(buddy)));
                }
                int total = 0;
                for (IGatewayConnectorBuddy buddy : msgCount.keySet()) {
                    total += msgCount.get(buddy);
                }
                htmlWriter.write("        <tr><td><b>Total</b></td>"
                                +"        <td><b>" + total + "</b></td></tr>\n");
                htmlWriter.write("    </table>\n" + "    <br>\n");                
                htmlWriter.write("    <table>\n");
                textWriter.write(String.format("%-20s  %-3d" + NIOGlobal.lineSeparator(), "Total", total));
                textWriter.write(NIOGlobal.lineSeparator());
            } catch(IOException e) {
                System.err.println("Exception in initializing HTML file: " + e);
            }
        }
      
        private void insertMessageElementHTML(IPbsysInstantMessage message) throws IOException {
            DateFormat format = new SimpleDateFormat("E, d MMM y kk:mm:ss z");
            Date date = message.getMessageTimestamp().getTime();
            String content = message.getMessageContent();
            String sender = message.getFromUser().getIMUniqueName();
            String receiver = message.getToUser().getIMUniqueName();
            String sendColor;
            
            Date msgDate = message.getMessageTimestamp().getTime();
            if (startDate == null || msgDate.before(startDate)) {
                startDate = msgDate;
            }
            
            if (endDate == null || msgDate.after(endDate)) {
                endDate = msgDate;
            }
            
            if (message.isOutgoing()) {
                sendColor = "imSendColor1";
            } else {
                sendColor = "imSendColor3";
            }
            
            htmlWriter.write("      <tr>\n" + "        <td>\n" 
                    + "          <span class=\"" + sendColor + "\">");
            htmlWriter.write("(" + format.format(date) +") " + message.getIMServerType().toString()
                        + " : " + sender + " &rarr; " + receiver);
            htmlWriter.write("</span>\n" + "        </td>\n" + "      </tr>\n");
            htmlWriter.write("      <tr>\n" + "        <td>\n");
            htmlWriter.write(content);
            htmlWriter.write("        </td>\n" + "      </tr>\n");
            htmlWriter.write("      <tr>\n" + "        <td style=\"height=10px\"></td>\n" + "      </tr>\n");
            
            textWriter.write("(" + format.format(date) +") " + message.getIMServerType().toString()
                        + " : " + sender + " -> " + receiver + NIOGlobal.lineSeparator());
            textWriter.write(content + NIOGlobal.lineSeparator() + NIOGlobal.lineSeparator());

        }
        
        private String createMessageId(String path) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                FileInputStream fis = new FileInputStream(path + ".html");
                int nread;
                byte[] data = new byte[1024];
                while ((nread = fis.read(data)) != -1) {
                    md.update(data, 0, nread);
                }
                byte[] digest = md.digest();
                
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < digest.length; i++) {
                    sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
                }
                return  sb.toString();
            } catch (Exception e) {
            }
              return "";
        }
        
        private long getDuration() {
            if (startDate == null || endDate == null) {
                return 0;
            }
            return (endDate.getTime() - startDate.getTime()) / (60 * 1000) + 1;
        }
        
        class CustomizedMimeMessage extends MimeMessage {
            private String messageId;
                
            public CustomizedMimeMessage(Session session) {
                super(session);
            }
                
            @Override
            protected void updateMessageID() throws MessagingException {
                setHeader("Mesage-ID", messageId);
            }
                
            public String getMessageId() {
                return messageId;
            }
                
            public void setMessageId(String messageId) {
                this.messageId = messageId;
            }
        };
        
        private String getEmailAddr(IGatewayConnectorBuddy buddy) {
            String emailAddr = buddy.getIMScreenName();
            switch (buddy.getIMServerType()) {
                case AIM_SERVER_TYPE:
                    emailAddr += "@aol.com";
                    break;
                case YIM_SERVER_TYPE:
                    emailAddr += "@yahoo.com";
                    break;
                case PBIM_SERVER_TYPE:
                case PBIM_DISTRIBUTION_TYPE:
                case PBIM_CONFERENCE_TYPE:
                    emailAddr += "@pointbox";
                    break;
                case UNKNOWN:            
            }
            return emailAddr;
        }
        
        private void sendEmail(String tempFileNameWithoutExtension) {
            Properties props = new Properties();
            props.put("mail.smtp.host", HOST);
            props.put("mail.smtp.port", PORT);
            props.put("mail.smtp.starttls.enable", true);
            Authenticator authenticator = null;
            if (AUTH) {
                props.put("mail.smtp.auth", true);
                authenticator = new Authenticator() {
                    private PasswordAuthentication pa = new PasswordAuthentication(USERNAME, PASSWORD);
                    
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return pa;
                    }
                };
            }
            
            Session session = Session.getInstance(props, authenticator);
            CustomizedMimeMessage message = new CustomizedMimeMessage(session);
              
            try {
                msgCount.remove(account);
                InternetAddress[] address = new InternetAddress[msgCount.keySet().size() + 1];
                int i = 0;
                for (IGatewayConnectorBuddy buddy : msgCount.keySet()) {
                    address[i] = new InternetAddress(getEmailAddr(buddy));
                    i++;
                }
                address[i] = new InternetAddress(getEmailAddr(account));
                message.setRecipients(Message.RecipientType.TO, address);
                message.setSubject("Chat: " + (msgCount.keySet().size() + 1) + " Users, " 
                            + this.size() + " Messages, " + getDuration() + " Minutes\n"
                            , "UTF-8");
                message.setSentDate(startDate);
                message.setFrom(new InternetAddress(getEmailAddr(account)));
                //Set to address
                
                message.setHeader("Mime-Version", "1.0");
                message.setHeader("X-GlobalRelay-MsgType", MSG_TYPE);
                
                BufferedReader reader = new BufferedReader(new FileReader(tempFileNameWithoutExtension + ".txt"));
                StringBuilder sb = new StringBuilder();
                String line; 
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(NIOGlobal.lineSeparator());
                }
                
                //Create HTTP body and plain-text body       
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(sb.toString(), "utf-8");
                textPart.setHeader("Content-Transfer-Encoding", "binary");
                
                reader = new BufferedReader(new FileReader(tempFileNameWithoutExtension + ".html"));
                sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(NIOGlobal.lineSeparator());
                }
                
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(sb.toString(), "text/html; charset=utf-8");
                htmlPart.setHeader("Content-Transfer-Encoding", "binary");
             
                Multipart mult = new MimeMultipart();
                mult.addBodyPart(htmlPart);
                mult.addBodyPart(textPart);
                message.setContent(mult);
                
                message.setHeader("Message-Id", "<" + createMessageId(tempFileNameWithoutExtension) + "@iminterpreter.globalrelay.net>");
                message.setHeader("Content-Transfer-Encoding", "binary");
                DateFormat format = new SimpleDateFormat("E, dd MMM yy kk:mm:ss z");
                Date date = this.get(this.size() - 1).getMessageTimestamp().getTime();
                message.setHeader("Date", format.format(date));
                message.saveChanges();
                
                File headers = new File(tempFileNameWithoutExtension + ".eml");
                if (!headers.exists()) {
                    headers.createNewFile();
                }
                OutputStream os = new FileOutputStream(headers);
                message.writeTo(os);
                os.close();
            } 
            catch (MessagingException me) {
                logger.warning(me.toString());
            }
            catch (IOException e) {
                logger.warning(e.toString());
            }
        
        }
        
        private String getArchivingHTMLFileNamePath(String buddyName) {
            File archiveFolder = new File(getPbcRuntime().getArchiveLocation());
            if (NIOGlobal.isValidFolder(archiveFolder)) {
                return archiveFolder.getAbsolutePath()
                        + "\\" + face.getKernel().getPointBoxLoginUser().getIMServerType()
                        + "_" + face.getKernel().getPointBoxLoginUser().getIMScreenName()
                        + "_" + buddyName.replace(' ', '_')
                        + "_" + CalendarGlobal.getCurrentMMddyyyy("_")
                        + "_" + CalendarGlobal.getCurrentHHmmssSS("_");
            } else {
                return null;
            }
        }
       
       private void finalizeHTMLFile() throws IOException {
            Date processTime = new Date();
            htmlWriter.write("    </table>\n" + "    <footer>\n");
            htmlWriter.write("<br>");
            htmlWriter.write("SOFTWARE: " + SOFTWARE_NAME + " (" + SOFTWARE_VERSION + ")\n"
                    + "<br>RELEASE: " + SOFTWARE_RELEASE + "\n"
                    + "<br>PROCESS TIME: " + processTime + "\n");
            htmlWriter.write("    </footer>\n" + "  </body>\n" + "</html>\n");
            htmlWriter.flush();
            htmlWriter.close();
            
            textWriter.write(NIOGlobal.lineSeparator()
                        + "SOFTWARE: " + SOFTWARE_NAME + " (" + SOFTWARE_VERSION +")"
                        + NIOGlobal.lineSeparator()
                        + "RELEASE: " + SOFTWARE_RELEASE + NIOGlobal.lineSeparator()
                        + "PROCESS TIME: " + processTime + NIOGlobal.lineSeparator()); 
            
            textWriter.flush();
            textWriter.close();
        }
    }
    
    private class GlobalRelayArchivingLoop implements Runnable {
      
        private final Map<IGatewayConnectorBuddy, MessageList> messages
                        = new HashMap<IGatewayConnectorBuddy, MessageList>();
        
        /**
         *  Subject Header : ALl participants, #messages exchanged, duration.
         *  TO: ALL Participants email address.
         *  From: Conversation initializer.
         */
    
        @Override
        public void run() {
            IPbcRuntime runtime = getPbcRuntime();
            if (runtime != null) {
                File archiveFolder = new File(runtime.getArchiveLocation());
                if (NIOGlobal.isValidFolder(archiveFolder)) {
                    PointBoxTracer.displayMessage(logger, "GlobalRelayArchive is started...");
                    while (true) {
                        processMessagesHTML();
                        try {
                            Thread.sleep(1000*60*3);
                        } catch (InterruptedException ex) {
                            PointBoxTracer.displayMessage(logger, "GlobayRelayArchive is interruptted");
                            processMessagesHTML();
                            break;
                        }
                    }
                    saveHTMLFiles();
                }
            }
        }
        
        private void processMessagesHTML() {
            ArrayList<IPbsysInstantMessage> receivedMessages = retrieveInstantMessages();
            try {
                if (!receivedMessages.isEmpty()) {
                    for (IPbsysInstantMessage message : receivedMessages) {
                        insertMessages(message);
                    }
                    saveHTMLFiles();
                }
            }
            catch (IOException e) {
                logger.log(Level.WARNING, "Faild to save html file");
            }
        }
        
        //Retrieve messages and oragnize them into HashMap
        private void insertMessages(IPbsysInstantMessage msg) throws IOException {
            IGatewayConnectorBuddy buddy = msg.isOutgoing() ? msg.getFromUser() 
                                    : msg.getToUser();
            MessageList msgs = messages.get(buddy);
            if (msgs == null) {
                msgs = new MessageList(buddy);
                messages.put(buddy, msgs);
            }
            msgs.add(msg);
        }
        
        //For each converstion(represented by buddy name), save a html file
        private void saveHTMLFiles() {
            try {
                for (IGatewayConnectorBuddy buddy : messages.keySet()) {
                    messages.get(buddy).createHTMLFile(buddy);
                    messages.get(buddy).clear();
                }
            }
            catch (IOException e) {
                logger.log(Level.WARNING, "Faild to save html files");                
            }
        }         
    }
        
    private class FaceTimeArchivingLoop implements Runnable{
        private Document doc = null;
        private int messageCounter = 0;
        
        @Override
        public void run() {
            IPbcRuntime runtime = getPbcRuntime();
            if (runtime != null){
                File archiveFolder = new File(runtime.getArchiveLocation());
                if (NIOGlobal.isValidFolder(archiveFolder)){
                    PointBoxTracer.displayMessage(logger, "FaceTimeArchivingLoop is started...");
                    while(true){
                        processFaceTimeMessages();
                        try {
                            Thread.sleep(1000*60*3);
                        } catch (InterruptedException ex) {
                            PointBoxTracer.displayMessage(logger, "FaceTimeArchivingLoop is interrupted.");
                            processFaceTimeMessages();
                            break;
                        }
                    }//while
                    saveXmlFile();
                }
            }
        }
        
        private void saveXmlFile(){
            if (doc != null){
                try {
                    String xmlPath = getArchivingXmlFileNamePath();
                    if (DataGlobal.isNonEmptyNullString(xmlPath)){
                        XMLGlobal.saveXMLDocumentDOM(doc, xmlPath);
                        //logger.log(Level.INFO, "saveXMLDocumentDOM - {0}", xmlPath);
                    }else{
                        PointBoxTracer.recordSevereException(logger, new Exception("Cannot find archiving path"));
                    }
                } catch (TransformerException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                } catch (IOException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                }
                doc = null;
                messageCounter = 0;
            }
        }
        
        private void initializeXMLDoc(){
            if (doc == null){
                
                doc = XMLGlobal.createEmptyXmlDocument(FaceTimeXmlTags.FileDump.toString());
                
                Element root = doc.getDocumentElement();
                
                Element cf = XMLGlobal.createEmptyElement(root, FaceTimeXmlTags.Conversation_FileDump.toString());
                XMLGlobal.setAttributeValue(cf, FaceTimeXmlTags.Attr_Perspective.toString(), face.getKernel().getSoftwareName()+ "_" + face.getKernel().getPbcReleaseCode());
                
                Element e = XMLGlobal.createEmptyElement(cf, FaceTimeXmlTags.RoomID_Conversation_FileDump.toString());
                e.setNodeValue(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
                
                e = XMLGlobal.createEmptyElement(cf, FaceTimeXmlTags.StartTimeUTC_Conversation_FileDump.toString());
                e.setNodeValue(Long.toString((new GregorianCalendar()).getTimeInMillis()));
                
            }
        }

        private void processFaceTimeMessages() {
            ArrayList<IPbsysInstantMessage> messages = retrieveInstantMessages();
            if (!messages.isEmpty()){
                //logger.log(Level.INFO, "processFaceTimeMessages - Message Count: {0}", messages.size());
                initializeXMLDoc();
                int max = 25;
                for (IPbsysInstantMessage message : messages){
                    processFaceTimeMessage(message);
                }//for
                if (messageCounter >= max){
                    saveXmlFile();
                }
            }//if
        }

        private void processFaceTimeMessage(IPbsysInstantMessage message) {
            if ((message == null) || (doc == null)){
                return;
            }
            messageCounter++;
            String loginName;
            String buddyName;
            
            if (message.isOutgoing()){
                loginName = message.getFromUser().getIMUniqueName();
                buddyName = message.getToUser().getIMUniqueName();
            }else{
                loginName = message.getToUser().getIMUniqueName();
                buddyName = message.getFromUser().getIMUniqueName();
            }
            String conversationID = loginName + "_" + buddyName;
            NodeList nodes = doc.getElementsByTagName(FaceTimeXmlTags.ParticipantEntered_Conversation_FileDump.toString());
            
            Node node;
            String conversationIDValue;
            boolean foundParticipant = false;
            for (int i = 0; i < nodes.getLength(); i++){
                node = nodes.item(i);
                try {
                    conversationIDValue = XMLGlobal.getStringValueFromSubelement((Element)node, 
                            FaceTimeXmlTags.ConversationID_ParticipantEntered_Conversation_FileDump.toString());
                    if (conversationID.equalsIgnoreCase(conversationIDValue)){
                        foundParticipant = true;
                        break;
                    }
                } catch (XMLSemanticsException ex) {
                    PointBoxTracer.displayMessage(logger, ex);
                }
            }//for
            if (!foundParticipant){
                insertParticipantElement(loginName, conversationID);
                insertParticipantElement(buddyName, conversationID);
            }
            insertMessageElement(message, conversationID);
        }
    
        
        private String getArchivingXmlFileNamePath() {
            File archiveFolder = new File(getPbcRuntime().getArchiveLocation());
            if (NIOGlobal.isValidFolder(archiveFolder)){
                return archiveFolder.getAbsolutePath() 
                        + "/" + face.getKernel().getPointBoxLoginUser().getIMServerType()
                        + "_" + face.getKernel().getPointBoxLoginUser().getIMScreenName()
                        + "_" + CalendarGlobal.getCurrentMMddyyyy("_")
                        + "_" + CalendarGlobal.getCurrentHHmmssSS("_")
                        +".xml";
            }else{
                return null;
            }
        }

        private void insertParticipantElement(String loginName, String conversationID) {
            long timeStamp = (new GregorianCalendar()).getTimeInMillis();
            Element eBase = XMLGlobal.getFirstElementByTagName(doc, FaceTimeXmlTags.Conversation_FileDump.toString());
            Element eMsg = XMLGlobal.createEmptyElement(eBase, FaceTimeXmlTags.ParticipantEntered_Conversation_FileDump.toString());
            Element e = XMLGlobal.createEmptyElement(eMsg, FaceTimeXmlTags.LoginName_ParticipantEntered_Conversation_FileDump.toString());
            XMLGlobal.setTextContent(e, loginName);
            e = XMLGlobal.createEmptyElement(eMsg, FaceTimeXmlTags.ConversationID_ParticipantEntered_Conversation_FileDump.toString());
            XMLGlobal.setTextContent(e, conversationID);
            e = XMLGlobal.createEmptyElement(eMsg, FaceTimeXmlTags.DateTimeUTC_ParticipantEntered_Conversation_FileDump.toString());
            XMLGlobal.setTextContent(e, Long.toString(timeStamp));
        }

        private void insertMessageElement(IPbsysInstantMessage message, String conversationID) {
            long timeStamp = message.getMessageTimestamp().getTimeInMillis();
            String content = message.getMessageContent();
            String userName;
            if (message.isOutgoing()){
                userName = message.getFromUser().getIMUniqueName();
            }else{
                userName = message.getFromUser().getIMUniqueName();
            }
            Element eBase = XMLGlobal.getFirstElementByTagName(doc, FaceTimeXmlTags.Conversation_FileDump.toString());
            Element eMsg = XMLGlobal.createEmptyElement(eBase, FaceTimeXmlTags.Message_Conversation_FileDump.toString());
            Element e = XMLGlobal.createEmptyElement(eMsg, FaceTimeXmlTags.LoginName_Message_Conversation_FileDump.toString());
            XMLGlobal.setTextContent(e, userName);
            e = XMLGlobal.createEmptyElement(eMsg, FaceTimeXmlTags.Content_Message_Conversation_FileDump.toString());
            XMLGlobal.setTextContent(e, content);
            e = XMLGlobal.createEmptyElement(eMsg, FaceTimeXmlTags.ConversationID_Message_Conversation_FileDump.toString());
            XMLGlobal.setTextContent(e, conversationID);
            e = XMLGlobal.createEmptyElement(eMsg, FaceTimeXmlTags.DateTimeUTC_Message_Conversation_FileDump.toString());
            XMLGlobal.setTextContent(e, Long.toString(timeStamp));
        }
    
    }
    
    private class PlainTextArchivingLoop implements Runnable{
        @Override
        public void run() {
            PointBoxTracer.displayMessage(logger, "PlainTextArchivingLoop is started...");
            while(true){
                processPlainTextMessage();
                try {
                    Thread.sleep(1000*60);
                } catch (InterruptedException ex) {
                    PointBoxTracer.displayMessage(logger, "PlainTextArchivingLoop is interrupted.");
                    processPlainTextMessage();
                    break;
                }
            }//while
        }

        private void processPlainTextMessage() {
            int breakMax = 25;
            ArrayList<IPbsysInstantMessage> messages = retrieveInstantMessages();
            if (!messages.isEmpty()){
                StringBuilder sb = new StringBuilder();
                int counter = 0;
                for (IPbsysInstantMessage message : messages){
                    sb.append(CalendarGlobal.getMMDDYY(message.getMessageTimestamp(), "-"));
                    sb.append("@").append(CalendarGlobal.getHHmmss(message.getMessageTimestamp(), ":"));
                    sb.append("  ").append(message.getIMServerType().toString());
                    sb.append("  From: ").append(message.getFromUser().getIMScreenName());
                    sb.append("  To: ").append(message.getToUser().getIMScreenName());
                    sb.append("  ").append(message.getMessageContent());
                    sb.append(NIOGlobal.lineSeparator());
                    counter++;
                    if (counter > breakMax){//take a break
                        counter = 0;    //reset
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            PointBoxTracer.recordSevereException(logger, ex);
                        }
                    }
                }//for
                try {
                    String filePath = getTextFilePath();
                    if (filePath != null){
                        NIOGlobal.appendTextLineToFile(sb.toString(), filePath);
                    }
                } catch (IOException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            }
        }

        private String getTextFilePath() {
            File archiveFolder = new File(getPbcRuntime().getArchiveLocation());
            if (NIOGlobal.isValidFolder(archiveFolder)){
                return archiveFolder.getAbsolutePath() 
                        + "/" + face.getKernel().getPointBoxLoginUser().getIMServerType()
                        + "_" + face.getKernel().getPointBoxLoginUser().getIMScreenName()
                        + "_" + CalendarGlobal.getCurrentyyyyMMdd("_");
            }else{
                return null;
            }
        }
    }
}





/**
 * Importing IM Conversations for FaceTime
<?xml version="1.0" encoding="UTF-8" ?>
<FileDump>
    <Conversation Perspective="Trading_group_channel1">
        <RoomID>Trading</RoomID>
        <StartTimeUTC>1149708684</StartTimeUTC>
        <ParticipantEntered>
            <LoginName>Trading_group_channel1</LoginName>
            <DateTimeUTC>1149708684</DateTimeUTC>
            <InternalFlag>false</InternalFlag>
            <ConversationID>Trading1</ConversationID>
        </ParticipantEntered>
        <ParticipantEntered>
            <LoginName>admin1</LoginName>
            <DateTimeUTC>1149708684</DateTimeUTC>
            <InternalFlag>false</InternalFlag>
            <ConversationID>Trading1</ConversationID>
        </ParticipantEntered>
        <ParticipantEntered>
            <LoginName>user1</LoginName>
            <DateTimeUTC>1149708684</DateTimeUTC>
            <InternalFlag>false</InternalFlag>
            <ConversationID>Trading1</ConversationID>
        </ParticipantEntered>
        <ParticipantEntered>
            <LoginName>user2</LoginName>
            <DateTimeUTC>1149708684</DateTimeUTC>
            <InternalFlag>false</InternalFlag>
            <ConversationID>Trading1</ConversationID>
        </ParticipantEntered>
        <FileTransferStarted>
            <LoginName>admin1</LoginName>
            <UserFileName>transferedFiles\rower1.jpg</UserFileName>
            <FileName>transferedFiles\16200606071431248685rower1.jpg
            </FileName>
            <DateTimeUTC>1149708684</DateTimeUTC>
        </FileTransferStarted>
        <FileTransferEnded>
            <LoginName>admin1</LoginName>
            <UserFileName>transferedFiles\rower1.jpg</UserFileName>
            <FileName>transferedFiles\16200606071431248685rower1
            .jpg</FileName>
            <Status>completed</Status>
            <DateTimeUTC>1149708684</DateTimeUTC>
        </FileTransferEnded>
        <Message>
            <LoginName>admin1</LoginName>
            <Content>now here's a file transfer http://192.168.5.100/
            website/download?fileURL=16200606071431248685/
            rower.jpg (55.9 K)</Content>
            <Base64Content>Ym05M0lHaGxjbVVuY3lCaElHWnBiR1VnZE
            hKaGJuTm1aWElnYUhSMGNEb3ZMekU1TWk0eE5qZ3VOUzR
            4TURBdmQyVmljMmwwWlM5a2IzZHViRzloWkQ5bWFXeGx
            WVkpNUFRFMk1qQXdOakEyTURjeE5ETXhNalE0TmpnMUwz
            SnZkMlZ5TG1wd1p5QW9OVFV1T1NCTEtRPT0=</
            Base64Content>
            <DateTimeUTC>1149708684</DateTimeUTC>
            <ConversationID>Trading1</ConversationID>
        </Message>
        <FileTransferStarted>
            <LoginName>user1</LoginName>
            <UserFileName>transferedFiles\compliancelog1.xml
            </UserFileName>
            <FileName>transferedFiles\18200606071431422436compliance
            log1.xml
            </FileName>
            <DateTimeUTC>1149708702</DateTimeUTC>
        </FileTransferStarted>
        <FileTransferEnded>
            <LoginName>user1</LoginName>
            <UserFileName>transferedFiles\compliancelog1.xml
            </UserFileName>
            <FileName>transferedFiles\18200606071431422436compliance
            log1.xml
            </FileName>
            <Status>completed</Status>
            <DateTimeUTC>1149708702</DateTimeUTC>
        </FileTransferEnded>
        <Message>
            <LoginName>user1</LoginName>
            <Content>and another file transfer http://192.168.5.100/
            website/download?fileURL=18200606071431422436/
            compliancelog.xml (< 1 K)
            </Content>
            <Base64Content>WVc1a0lHRnViM1JvWlhJZ1ptbHNaU0IwY21Gd
            WMyWmxjaUJvZEhSd09pOHZNVGt5TGpFMk9DNDFMakV3T
            UM5M1pXSnphWFJsTDJSdmQyNXNiMkZrUDJacGJHVlZVa3c
            5TVRneU1EQTJNRFl3TnpFME16RTBNakkwTXpZdlkyOXRjR3
            hwWVc1alpXeHZaeTU0Yld3Z0tEd2dNU0JMS1E9PQ==</
            Base64Content>
            <DateTimeUTC>1149708702</DateTimeUTC>
            <ConversationID>Trading1</ConversationID>
        </Message>
    </Conversation>
</FileDump>
 */