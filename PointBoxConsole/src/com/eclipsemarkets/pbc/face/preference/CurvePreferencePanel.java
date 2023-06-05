/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import java.awt.BorderLayout;
import org.jdesktop.swingx.JXTaskPane;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:45:29 PM
 */
public class CurvePreferencePanel extends PreferencePanel implements ICurvePreferencePanel{

    private JXTaskPane curveSettingsPanel;
    private FileSettingsPanel baseCurveFileSettingsPanel;

//    private final JXTaskPane grainsSettingsPanel;
//    private final GrainsFileSettingsPanel baseGrainsFileSettingsPanel;
//
//    private final JXTaskPane powerSettingsPanel;
//    private final PowerFileSettingsPanel basePowerFileSettingsPanel;
//
//    private final JXTaskPane crudeOilSettingsPanel;
//    private final CrudeOilFileSettingsPanel baseCrudeOilFileSettingsPanel;

    public CurvePreferencePanel(IPbcFace face) {
        super(face);
        initializeCurvePanel();
    }

    @Override
    public void downloadPricingRuntimeCurveFiles(boolean displayCompleteMessage) {
        baseCurveFileSettingsPanel.downloadPricingRuntimeCurveFiles(displayCompleteMessage);
    }

    @Override
    public void downloadPricingRuntimeCurveFiles(PbcPricingModel aPbcPricingModel, boolean displayCompleteMessage) {
        baseCurveFileSettingsPanel.downloadPricingRuntimeCurveFiles(aPbcPricingModel, displayCompleteMessage);
    }

    @Override
    public void uploadPricingRuntimeCurveFiles() {
        baseCurveFileSettingsPanel.uploadPricingRuntimeCurveFiles();
    }

    void refreshTitle(){
        ((CurveLocationSettingsPanel)baseCurveFileSettingsPanel).refreshTitle();
    }
    
    private void initializeCurvePanel() {
        curveSettingsPanel = new JXTaskPane();
        baseCurveFileSettingsPanel = new CurveLocationSettingsPanel(face); 
        curveSettingsPanel.setTitle("Curve Settings");
//        switch (defaultQuoteCommodityType){
//            case CRUDE_OIL:
//                baseCurveFileSettingsPanel = new CrudeOilFileSettingsPanel(face);
//                curveSettingsPanel.setTitle("Crude Oil Pricing Settings");
//                break;
//            case POWER:
//                baseCurveFileSettingsPanel = new PowerFileSettingsPanel(face);
//                curveSettingsPanel.setTitle("Power Pricing Settings");
//                break;
//            case GRAINS:
//                baseCurveFileSettingsPanel = new GrainsFileSettingsPanel(face);
//                curveSettingsPanel.setTitle("Grains Pricing Settings");
//                break;
//            default:
//                baseCurveFileSettingsPanel = new NaturalGasFileSettingsPanel(face);
//                curveSettingsPanel.setTitle("Natural Gas Pricing Settings");
//        }
        componentPanels.add((IPreferenceComponentPanel)baseCurveFileSettingsPanel);
        curveSettingsPanel.setLayout(new BorderLayout());
        curveSettingsPanel.add(baseCurveFileSettingsPanel, BorderLayout.CENTER);
        curveSettingsPanel.setCollapsed(false);
        settingsContainer.add(curveSettingsPanel);
    }

}
