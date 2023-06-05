/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FarewellSplashWindow.java
 *
 * Created on Nov 11, 2010, 9:46:56 AM
 */

package com.eclipsemarkets.pbc.face;

import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.IPbcSplashScreen;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Zhijun Zhang
 */
public class FarewellSplashWindow extends java.awt.Frame implements IPbcFaceComponent, IPbcSplashScreen {
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(FarewellSplashWindow.class.getName());
    }
    
    private IPbcFace face;

    /** Creates new form FarewellSplashWindow */
    private FarewellSplashWindow() {
        initComponents();
    }

    /**
     * This should be called from EDT
     * @return
     */
    static FarewellSplashWindow getInstance(IPbcFace face){
        FarewellSplashWindow frame = new FarewellSplashWindow();
        frame.initialize(face);
        return frame;
    }
    
    private void initialize(IPbcFace face) {
        this.face = face;
        //message alignment
        StyledDocument doc = jMessage.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        //location of this frame
        //setAlwaysOnTop(true);
    }

    public void updateFarewellMessage(final String message) {
        if (SwingUtilities.isEventDispatchThread()){
            jMessage.setText(message);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jMessage.setText(message);
                }
            });
        }
    }

    @Override
    public void handlePointBoxEvent(PointBoxConsoleEvent event) {
    }

    @Override
    public void displayFaceComponent() {
        display();
    }

    @Override
    public void hideFaceComponent() {
        close();
    }

    @Override
    public void personalizeFaceComponent() {

    }

    @Override
    public void releaseFaceComponent() {
        
    }

    @Override
    public void close() {
        if (SwingUtilities.isEventDispatchThread()){
            setVisible(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setVisible(false);
                }
            });
        }
    }
    
    private void displayHelper(){
        setLocation(SwingGlobal.getCenterPointOfParentWindow(face.getPointBoxMainFrame(), this));
        setVisible(true);
    }

    @Override
    public void display() {
        if (SwingUtilities.isEventDispatchThread()){
            displayHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayHelper();
                }
            });
        }
    }

    @Override
    public void updateSplashScreen(String msg, Level level, long latency) {
        updateFarewellMessage(msg);
        try {
            Thread.sleep(latency);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMessage = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(400, 150));
        setResizable(false);
        setTitle("Shut down PointBox Console");
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jMessage.setBorder(null);
        jMessage.setEditable(false);
        jMessage.setFont(new java.awt.Font("Tahoma", 0, 10));
        jMessage.setText("Thank you for using PointBox Console!");
        jMessage.setAutoscrolls(false);
        jMessage.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jMessage.setEnabled(false);
        jMessage.setName("jMessage"); // NOI18N
        jMessage.setOpaque(false);
        jScrollPane1.setViewportView(jMessage);

        jLabel1.setFont(new java.awt.Font("Brush Script MT", 1, 24));
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("Thank you for using PointBox Console! ");
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(27, 27, 27))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        dispose();
    }//GEN-LAST:event_exitForm

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextPane jMessage;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}