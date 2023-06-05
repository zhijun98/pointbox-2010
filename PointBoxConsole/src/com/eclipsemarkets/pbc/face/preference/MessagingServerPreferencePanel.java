/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.PbcFeature;
import com.eclipsemarkets.pbc.face.IPbcFace;
import static com.eclipsemarkets.pbc.face.preference.PreferencePanelType.AimServerSettingsPanel;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.BorderLayout;
import org.jdesktop.swingx.JXTaskPane;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 7:28:18 PM
 */
public class MessagingServerPreferencePanel extends PreferencePanel{
    
    private JXTaskPane serverSettingsPanel;
    private ServerSettingsPanel baseServerSettingsPanel;

    public MessagingServerPreferencePanel(IPbcFace face, PreferencePanelType type) {
        super(face);
        switch (type){
            case AimServerSettingsPanel:
                initializeServerSettingsPanel(GatewayServerType.AIM_SERVER_TYPE, PbcFeature.AIM, "AIM Settings");
                break;
            case YahooServerSettingsPanel:
                initializeServerSettingsPanel(GatewayServerType.YIM_SERVER_TYPE, PbcFeature.YahooIM, "Yahoo Settings");
                break;
            default:
                initializeServerSettingsPanel(GatewayServerType.PBIM_SERVER_TYPE, null, "PointBox Settings");
                
        }
    }
    
    private void initializeServerSettingsPanel(GatewayServerType serverType, PbcFeature aPbcFeature, String title){
        //serverSettingsPanel;
        serverSettingsPanel = new JXTaskPane();
        baseServerSettingsPanel = new ServerSettingsPanel(face, serverType);
        if ((aPbcFeature == null) || (!face.getKernel().isPbcFeatureDisabled(aPbcFeature))){
            componentPanels.add((IPreferenceComponentPanel)baseServerSettingsPanel);
            serverSettingsPanel.setLayout(new BorderLayout());
            serverSettingsPanel.add(baseServerSettingsPanel, BorderLayout.CENTER);
            serverSettingsPanel.setTitle(title);
            serverSettingsPanel.setCollapsed(false);
            settingsContainer.add(serverSettingsPanel);
        }
    
    }

    @Override
    public void updateSettings() {
        baseServerSettingsPanel.updateSettings();
    }

}
