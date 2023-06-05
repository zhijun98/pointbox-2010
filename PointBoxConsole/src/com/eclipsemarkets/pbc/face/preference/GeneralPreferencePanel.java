/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;
import java.awt.BorderLayout;
import org.jdesktop.swingx.JXTaskPane;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:14:14 PM
 */
public class GeneralPreferencePanel extends PreferencePanel{
    
    private final JXTaskPane generalSettingsPanel;
    private final GeneralSettingsPanel baseGeneralSettingsPanel;

    public GeneralPreferencePanel(IPbcFace face) {
        super(face);
        //generalSettingsPanel
        generalSettingsPanel = new JXTaskPane();
        baseGeneralSettingsPanel = new GeneralSettingsPanel(face);
        componentPanels.add((IPreferenceComponentPanel)baseGeneralSettingsPanel);
        generalSettingsPanel.setLayout(new BorderLayout());
        generalSettingsPanel.add(baseGeneralSettingsPanel, BorderLayout.CENTER);
        generalSettingsPanel.setTitle("General Settings");
        generalSettingsPanel.setCollapsed(false);
        settingsContainer.add(generalSettingsPanel);
    }
}
