/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXTaskPane;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 7:11:00 PM
 */
public class MsAccessPreferencePanel extends PreferencePanel implements IMsAccessPreferencePanel{

    private final JXTaskPane mermSettingsPanel;
    private final MermSettingsPanel baseMermSettingsPanel;

    public MsAccessPreferencePanel(IPbcFace face) {
        super(face);

        //mermSettingsPanel
        mermSettingsPanel = new JXTaskPane();
        baseMermSettingsPanel = new MermSettingsPanel(face);
        componentPanels.add((IPreferenceComponentPanel)baseMermSettingsPanel);
        mermSettingsPanel.setLayout(new BorderLayout());
        mermSettingsPanel.add(baseMermSettingsPanel, BorderLayout.CENTER);
        mermSettingsPanel.setTitle("MS Access Settings (Optional)");
        mermSettingsPanel.setCollapsed(false);
        settingsContainer.add(mermSettingsPanel);
    }

    @Override
    public void notifyMsAccessInterrupted() {
        if (SwingUtilities.isEventDispatchThread()){
            notifyMsAccessInterruptedHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    notifyMsAccessInterruptedHelper();
                }
            });
        }
    }

    private void notifyMsAccessInterruptedHelper() {
        if (baseMermSettingsPanel instanceof MermSettingsPanel){
            MermSettingsPanel aMermSettingsPanel = (MermSettingsPanel)baseMermSettingsPanel;
            String mdbPath = aMermSettingsPanel.getMdbPath();
            aMermSettingsPanel.disableMsAccess(mdbPath);
            aMermSettingsPanel.invokeAutomaticMsAccessConnection();
        }
    }

    /**
     * This method is called by face.unload()
     */
    @Override
    public void unloadPreferencePanel() {
        if (baseMermSettingsPanel instanceof MermSettingsPanel){
            ((MermSettingsPanel)baseMermSettingsPanel).unloadMermSettingsPanel();
        }
    }

}
