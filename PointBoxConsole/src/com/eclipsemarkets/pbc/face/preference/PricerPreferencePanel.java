/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;
import static com.eclipsemarkets.pbc.face.preference.PreferencePanelType.PricingSettingsPanel;
import java.awt.BorderLayout;
import org.jdesktop.swingx.JXTaskPane;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:34:23 PM
 */
public class PricerPreferencePanel extends PreferencePanel{

    private final JXTaskPane pricingSettingsPanel;
    private final PricingSettingsPanel basePricingSettingsPanel;

    public PricerPreferencePanel(IPbcFace face) {
        super(face);
        //pricingSettingsPanel
        pricingSettingsPanel = new JXTaskPane();
        basePricingSettingsPanel = new PricingSettingsPanel(face);
        componentPanels.add((IPreferenceComponentPanel)basePricingSettingsPanel);
        pricingSettingsPanel.setLayout(new BorderLayout());
        pricingSettingsPanel.add(basePricingSettingsPanel, BorderLayout.CENTER);
        pricingSettingsPanel.setTitle("Pricer Settings");
        pricingSettingsPanel.setCollapsed(false);
        settingsContainer.add(pricingSettingsPanel);
    }
}
