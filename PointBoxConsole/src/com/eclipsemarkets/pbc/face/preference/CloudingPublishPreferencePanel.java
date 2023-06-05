/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.data.PointBoxCurveType;
import java.awt.BorderLayout;
import java.util.ArrayList;
import org.jdesktop.swingx.JXTaskPane;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 6:28:33 PM
 */
public class CloudingPublishPreferencePanel extends PreferencePanel implements ICloudingPublishPreferencePanel{
    
    private final JXTaskPane uploadSettingsPanel;
    private final UploadSettingsPanel baseUploadSettingsPanel;

    public CloudingPublishPreferencePanel(IPbcFace face) {
        super(face);
        
        //naturalGasUploadSettingsPanel
        uploadSettingsPanel = new JXTaskPane();
        baseUploadSettingsPanel = new UploadSettingsPanel(face);
        componentPanels.add((IPreferenceComponentPanel)baseUploadSettingsPanel);
        uploadSettingsPanel.setLayout(new BorderLayout());
        uploadSettingsPanel.add(baseUploadSettingsPanel, BorderLayout.CENTER);
        uploadSettingsPanel.setTitle("Cloud Publish Settings");
        uploadSettingsPanel.setCollapsed(false);
        settingsContainer.add(uploadSettingsPanel);
    }

    @Override
    public ArrayList<PointBoxCurveType> getSelectedFileTypesForPricingSettingsUploadAdmin() {
        return baseUploadSettingsPanel.getSelectedFileTypesForPricingSettingsUploadAdmin();
    }

    @Override
    public void updateSettings() {
        baseUploadSettingsPanel.updateSettings();
    }
}
