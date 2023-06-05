/**
 * Eclipse Market Solutions LLC
 *
 * ServerSettingsPanel.java
 *
 * @author Zhijun Zhang
 * Created on May 24, 2010, 12:02:19 AM
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.PbcLoginSettingsChanged;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.event.ItemEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Zhijun Zhang
 */
class ServerSettingsPanel extends javax.swing.JPanel implements IPreferenceComponentPanel 
{
    private static final long serialVersionUID = 1L;

    private final GatewayServerType serverType;
    private final IPbcFace face;
    
    private PointBoxConsoleProperties prop;

    ServerSettingsPanel(final IPbcFace face, final GatewayServerType serverType) {
        initComponents();
        prop = PointBoxConsoleProperties.getSingleton();
        this.face = face;
        this.serverType = serverType;
        ((TitledBorder)jServerSettings.getBorder()).setTitle(serverType.toString() + " Settings: ");

        if (serverType != GatewayServerType.PBIM_SERVER_TYPE) {
            jAutoLogin.setVisible(false);
            jAutoLogin.setSelected(false);
            jAutoLogin.setEnabled(false);
        }
        
        populateSettings();
//        jScreenName.addFocusListener(new FocusListener(){
//            @Override
//            public void focusGained(FocusEvent e) {
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                if (prop == null){
//                    return;
//                }
//                String account = jScreenName.getText();
//                if (DataGlobal.isEmptyNullString(account)){
//                }
////                if (!account.equalsIgnoreCase(prop.retrieveLoginWindowLoginName(serverType))){
////                    prop.storeLoginWindowLoginName(account, serverType);
////                    dispatchPbcComponentSettingsChangedEvent();
////                }
//            }
//        });
//
//        jPassword.addFocusListener(new FocusListener(){
//            @Override
//            public void focusGained(FocusEvent e) {
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                if (prop == null){
//                    return;
//                }
//                String password = new String(jPassword.getPassword());
//                if (DataGlobal.isEmptyNullString(password)){
//                }
////                if (!password.equalsIgnoreCase(prop.retrieveLoginWindowLoginPassword(serverType))){
////                    prop.storeLoginWindowLoginPassword(password, serverType);
////                    dispatchPbcComponentSettingsChangedEvent();
////                }
//            }
//        });
    }
    
    private void dispatchPbcComponentSettingsChangedEvent(){
        ServerSettingsPanel.this.face.getKernel()
                .raisePointBoxEvent(new PbcLoginSettingsChanged(PointBoxEventTarget.PbcFace,
                                                                             serverType));
    }

    @Override
    public final void populateSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            populateSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateSettingsHelper();
                }
            });
        }
    }
    
    private void populateSettingsHelper(){
        //setLoginParametersHelper();
        setCheckBoxes();
    }

    @Override
    public void updateSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            updateSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateSettingsHelper();
                }
            });
        }
    }
    
    private void updateSettingsHelper(){
        setCheckBoxes();
    }

//    private void setLoginParametersHelper(){
//        if (prop == null){
//            return;
//        }
//        if (prop.isLoginWindowRememberMe(serverType)){
//           // jScreenName.setText(prop.retrieveLoginWindowLoginName(serverType));
//            if (prop.isLoginWindowSavePassword(serverType)){
//            //    jPassword.setText(prop.retrieveLoginWindowLoginPassword(serverType));
//            }else{
//                jPassword.setText("");
//            }
//        }else{
//            jScreenName.setText("");
//            jPassword.setText("");
//        }
//    }

    private void setCheckBoxes(){
        if (prop == null){
            return;
        }
        if (prop.isLoginWindowRememberMe(serverType)) {
            jRememberMe.setSelected(true);
            jSavePassword.setEnabled(true);
            if (prop.isLoginWindowSavePassword(serverType)) {
                jSavePassword.setSelected(true);
                if (serverType == GatewayServerType.PBIM_SERVER_TYPE) {
                    jAutoLogin.setEnabled(true);
                    if (prop.isLoginWindowAutoLogin(serverType)) {
                        jAutoLogin.setSelected(true);
                    } else {
                        jAutoLogin.setSelected(false);
                    }
                }
            } else {
                jSavePassword.setSelected(false);
                jAutoLogin.setEnabled(false);
                jAutoLogin.setSelected(false);
            }
        } else {
            jRememberMe.setSelected(false);
            jSavePassword.setEnabled(false);
            jSavePassword.setSelected(false);
            jAutoLogin.setEnabled(false);
            jAutoLogin.setSelected(false);
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

        jServerSettings = new javax.swing.JPanel();
        jRememberMe = new javax.swing.JCheckBox();
        jSavePassword = new javax.swing.JCheckBox();
        jAutoLogin = new javax.swing.JCheckBox();

        jServerSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Server Settings: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 0, 0)));
        jServerSettings.setName("jServerSettings"); // NOI18N

        jRememberMe.setText("Remember Me");
        jRememberMe.setName("jRememberMe"); // NOI18N
        jRememberMe.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRememberMeItemStateChanged(evt);
            }
        });

        jSavePassword.setText("Save Password");
        jSavePassword.setName("jSavePassword"); // NOI18N
        jSavePassword.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jSavePasswordItemStateChanged(evt);
            }
        });

        jAutoLogin.setText("Auto Login");
        jAutoLogin.setName("jAutoLogin"); // NOI18N
        jAutoLogin.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jAutoLoginItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jServerSettingsLayout = new javax.swing.GroupLayout(jServerSettings);
        jServerSettings.setLayout(jServerSettingsLayout);
        jServerSettingsLayout.setHorizontalGroup(
            jServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jServerSettingsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAutoLogin)
                    .addComponent(jSavePassword)
                    .addComponent(jRememberMe))
                .addContainerGap(111, Short.MAX_VALUE))
        );
        jServerSettingsLayout.setVerticalGroup(
            jServerSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jServerSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRememberMe)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSavePassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAutoLogin)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jServerSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jServerSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRememberMeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRememberMeItemStateChanged
        if (prop == null){
            return;
        }
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!prop.isLoginWindowRememberMe(serverType)){
                prop.storeLoginWindowRememberMe(true, serverType);
                dispatchPbcComponentSettingsChangedEvent();
                setCheckBoxes();
            }
        }

        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (prop.isLoginWindowRememberMe(serverType)){
                prop.storeLoginWindowRememberMe(false, serverType);
                dispatchPbcComponentSettingsChangedEvent();
                setCheckBoxes();
            }
        }
    }//GEN-LAST:event_jRememberMeItemStateChanged

    private void jSavePasswordItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jSavePasswordItemStateChanged
        if (prop == null){
            return;
        }
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!prop.isLoginWindowSavePassword(serverType)){
                prop.storeLoginWindowSavePassword(true, serverType);
                dispatchPbcComponentSettingsChangedEvent();
                setCheckBoxes();
            }
        }

        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (prop.isLoginWindowSavePassword(serverType)){
                prop.storeLoginWindowSavePassword(false, serverType);
                dispatchPbcComponentSettingsChangedEvent();
                setCheckBoxes();
            }
        }
    }//GEN-LAST:event_jSavePasswordItemStateChanged

    private void jAutoLoginItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jAutoLoginItemStateChanged
        if (prop == null){
            return;
        }
        
        if (serverType != GatewayServerType.PBIM_SERVER_TYPE) {
            return;
        }
        
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!prop.isLoginWindowAutoLogin(serverType)){
                if (JOptionPane.showConfirmDialog(this,
                        "Set PBC auto login will also set Yahoo & AOL auto login.", 
                        "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
                {
                    prop.storeLoginWindowAutoLogin(true, serverType);
                    prop.storeLoginWindowRememberMe(true, GatewayServerType.AIM_SERVER_TYPE);
                    prop.storeLoginWindowRememberMe(true, GatewayServerType.YIM_SERVER_TYPE);
                    prop.storeLoginWindowSavePassword(true, GatewayServerType.AIM_SERVER_TYPE);
                    prop.storeLoginWindowSavePassword(true, GatewayServerType.YIM_SERVER_TYPE);
                    //Auto login property for Yahoo and AIM is not effective since it is hiden
                    prop.storeLoginWindowAutoLogin(true, GatewayServerType.AIM_SERVER_TYPE);
                    prop.storeLoginWindowAutoLogin(true, GatewayServerType.YIM_SERVER_TYPE);
                    
                    dispatchPbcComponentSettingsChangedEvent();
                    setCheckBoxes();
                    //PreferencePanel.getSingleton(face).updateSettings();
                }
            }
        }

        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (prop.isLoginWindowAutoLogin(serverType)){
                prop.storeLoginWindowAutoLogin(false, serverType);
                dispatchPbcComponentSettingsChangedEvent();
                setCheckBoxes();
            }
        }
    }//GEN-LAST:event_jAutoLoginItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAutoLogin;
    private javax.swing.JCheckBox jRememberMe;
    private javax.swing.JCheckBox jSavePassword;
    private javax.swing.JPanel jServerSettings;
    // End of variables declaration//GEN-END:variables

}
