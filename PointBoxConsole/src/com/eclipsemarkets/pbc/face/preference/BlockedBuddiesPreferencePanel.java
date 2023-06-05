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
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 9:05:28 PM
 */
public class BlockedBuddiesPreferencePanel extends PreferencePanel{
    
    private final JXTaskPane blackNamesSettingPanel;
    private final BlackNamesSettingPanel baseBlackNamesSettingPanel;

    public BlockedBuddiesPreferencePanel(IPbcFace face) {
        super(face);

        //blackNamesSettingPanel
        blackNamesSettingPanel = new JXTaskPane();
        baseBlackNamesSettingPanel = new BlackNamesSettingPanel(face);
        componentPanels.add((IPreferenceComponentPanel)baseBlackNamesSettingPanel);
        blackNamesSettingPanel.setLayout(new BorderLayout());
        blackNamesSettingPanel.add(baseBlackNamesSettingPanel, BorderLayout.CENTER);
        blackNamesSettingPanel.setTitle("Blocked Buddies Settings");
        blackNamesSettingPanel.setCollapsed(false);
        settingsContainer.add(blackNamesSettingPanel);
    }

}
