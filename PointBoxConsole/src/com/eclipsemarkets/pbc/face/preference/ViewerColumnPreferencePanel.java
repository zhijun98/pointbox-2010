/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import java.awt.BorderLayout;
import java.util.ArrayList;
import org.jdesktop.swingx.JXTaskPane;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:25:17 PM
 */
public class ViewerColumnPreferencePanel extends PreferencePanel{

    public ViewerColumnPreferencePanel(IPbcFace face) {
        super(face);

        //viewerSettingsPanes for tabs in the viewer
        initializeViewerSettingsPanes();
    }
    
    private void initializeViewerSettingsPanes() {
        ArrayList<String> viewerTabUniqueNames = face.getKernel().getPointBoxConsoleRuntime().getAllViewerTabUniqueNames();
        try{
            if (PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName())){
                /**
                 * every viewer has the same settings. thus, use the first tab's settings
                 */
                initializeSingleViewerSettingsPanesHelper(viewerTabUniqueNames.get(0), "Aggregator Settings");
            }else{
                for (int i = 0; i < viewerTabUniqueNames.size(); i++){
                    /**
                     * Populate every viewer settings
                     */
                    initializeSingleViewerSettingsPanesHelper(viewerTabUniqueNames.get(i), viewerTabUniqueNames.get(i) + " Aggregator");
                }
            }
        }catch (Exception ex){
            /**
             * This is the default in case face.getKernel().getPointBoxLoginUser() is NULL (not login yet)
             * every viewer has the same settings. thus, use the first tab's settings
             */
            initializeSingleViewerSettingsPanesHelper(viewerTabUniqueNames.get(0), "Aggregator Settings");
        }
    }
    
    private void initializeSingleViewerSettingsPanesHelper(String viewerTabUniqueName, String viewerTabTitle){
            JXTaskPane viewerSettingsPane = new JXTaskPane();
            ViewerTableColumnSettingsPanel baseViewerTableSettingsPanel = new ViewerTableColumnSettingsPanel(face, viewerTabUniqueName);
            componentPanels.add((IPreferenceComponentPanel)baseViewerTableSettingsPanel);
            viewerSettingsPane.setLayout(new BorderLayout());
            viewerSettingsPane.add(baseViewerTableSettingsPanel, BorderLayout.CENTER);
            viewerSettingsPane.setTitle(viewerTabTitle);
            viewerSettingsPane.setCollapsed(false);
            settingsContainer.add(viewerSettingsPane);
    
    }
}
