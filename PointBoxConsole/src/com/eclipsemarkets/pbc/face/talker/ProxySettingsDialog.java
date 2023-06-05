/**
 * Eclipse Market Solutions LLC
 */
/*
 * ProxySettingsDialog.java
 *
 * @author Zhijun Zhang
 * Created on Oct 20, 2010, 9:36:13 PM
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.PbcProperties;
import com.eclipsemarkets.pbc.PbcProxyInfo;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author Zhijun Zhang
 */
public class ProxySettingsDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;

    /** Creates new form ProxySettingsDialog
     * @param parent
     * @param modal
     */
    public ProxySettingsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        populateProperties();
        initializePbcProxySettingsDialog();
    }

    private void initializePbcProxySettingsDialog(){
        setTitle("Connect Using Proxy");
        setLocation(SwingGlobal.getScreenCenterPoint(this));
    }

    /**
     * Create a new PbcProxyInfo instance according to the current state of this control
     * @return
     */
    PbcProxyInfo getPbcProxyInfo() {
        PbcProxyInfo info = new PbcProxyInfo();

        if (DataGlobal.isEmptyNullString(jProxyHost.getText())){
            info.setProxyHost("");
        }else{
            info.setProxyHost(jProxyHost.getText());
        }
        if (DataGlobal.isEmptyNullString(jProxyPort.getText())){
            info.setProxyPort("");
        }else{
            info.setProxyPort(jProxyPort.getText());
        }
        if (DataGlobal.isEmptyNullString(jProxyLogin.getText())){
            info.setProxyLogin("");
        }else{
            info.setProxyLogin(jProxyLogin.getText());
        }
        char[] pwd = jProxyPassword.getPassword();
        if ((pwd != null) && (pwd.length > 0)){
            info.setProxyPassword(new String());
        }else{
            info.setProxyPassword("");
        }

        return info;
    }

    @Override
    public void setVisible(boolean value) {
        super.setVisible(value);
    }
    
    private void populateProperties(){
        PbcProperties prop = PbcProperties.getSingleton();
        String host = prop.getProxyHost();
        String port = prop.getProxyPort();
        String login = prop.getProxyUserName();
        String password = prop.getProxyPassword();
        jProxyHost.setText(host);
        jProxyPort.setText(port);
        jProxyLogin.setText(login);
        jProxyPassword.setText(password);

        Properties props = new Properties(System.getProperties());
        props.put("http.proxySet", "true");
        props.put("http.proxyHost", host);
        props.put("http.proxyPort", port);
        props.put("http.proxyUser ", login);
        props.put("http.proxyPassword", password);
        Properties newprops = new Properties(props);
        System.setProperties(newprops);
    }
    
    private void updateProperties(){
        String host = jProxyHost.getText();
        String port = jProxyPort.getText();
        String login = jProxyLogin.getText();
        String password = new String(jProxyPassword.getPassword());

//        if ((host.isEmpty()) || (port.isEmpty())){
//            JOptionPane.showMessageDialog(null, "Proxy Server and/or Port Number cannot be empty.");
//            return;
//        }
        
        PbcProperties prop = PbcProperties.getSingleton();
        prop.setProxyHost(host);
        prop.setProxyPassword(password);
        prop.setProxyPort(port);
        prop.setProxyUserName(login);

        Properties props = new Properties(System.getProperties());
        props.put("http.proxySet", "true");
        props.put("http.proxyHost", host);
        props.put("http.proxyPort", port);
        props.put("http.proxyUser ", login);
        props.put("http.proxyPassword", password);
        Properties newprops = new Properties(props);
        System.setProperties(newprops);
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
        jSetProxy = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jProxyHost = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jProxyPort = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jProxyLogin = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jProxyPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Proxy settings:");

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(1, 2, 2, 0));

        jSetProxy.setText("Save");
        jSetProxy.setName("jSetProxy"); // NOI18N
        jSetProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSetProxyActionPerformed(evt);
            }
        });
        jPanel1.add(jSetProxy);

        jButton1.setText("Clear");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jCancel.setText("Cancel");
        jCancel.setName("jCancel"); // NOI18N
        jCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jCancel);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText("Proxy Server (IP or Valid Domain Name):");
        jLabel1.setName("jLabel1"); // NOI18N

        jProxyHost.setName("jProxyHost"); // NOI18N

        jLabel2.setText("Port:");
        jLabel2.setName("jLabel2"); // NOI18N

        jProxyPort.setName("jProxyPort"); // NOI18N

        jLabel6.setFont(new java.awt.Font("Arial Black", 1, 11));
        jLabel6.setText(":");
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jProxyHost, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(12, 12, 12)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jProxyPort, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel3.setText("Authentication (Optional):");
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText("User Name:");
        jLabel4.setName("jLabel4"); // NOI18N

        jProxyLogin.setName("jProxyLogin"); // NOI18N

        jLabel5.setText("Password:");
        jLabel5.setName("jLabel5"); // NOI18N

        jProxyPassword.setName("jProxyPassword"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                    .addComponent(jProxyLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel5))
                    .addComponent(jProxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(26, 26, 26))
                    .addComponent(jProxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jProxyLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelActionPerformed
        jProxyHost.setText("");
        jProxyPort.setText("");
        jProxyLogin.setText("");
        jProxyPassword.setText("");

        Properties props = System.getProperties();
        props.put("http.proxySet", "false");
        props.put("http.proxyHost", "");
        props.put("http.proxyPort", "");
        props.put("http.proxyUser ", "");
        props.put("http.proxyPassword", "");

        setVisible(false);
    }//GEN-LAST:event_jCancelActionPerformed

    
    private void jSetProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSetProxyActionPerformed
        updateProperties();
        if (JOptionPane.showConfirmDialog(this, "Close this window?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            setVisible(false);
        }
    }//GEN-LAST:event_jSetProxyActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jProxyHost.setText("");
        jProxyPort.setText("");
        jProxyLogin.setText("");
        jProxyPassword.setText("");
        updateProperties();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jCancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField jProxyHost;
    private javax.swing.JTextField jProxyLogin;
    private javax.swing.JPasswordField jProxyPassword;
    private javax.swing.JTextField jProxyPort;
    private javax.swing.JButton jSetProxy;
    // End of variables declaration//GEN-END:variables
}