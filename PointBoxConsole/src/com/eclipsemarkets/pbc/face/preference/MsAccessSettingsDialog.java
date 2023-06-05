/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 7:10:14 PM
 */
public class MsAccessSettingsDialog extends PreferenceDialog{

    public MsAccessSettingsDialog(IPbcFace face) {
        super(face, new MsAccessPreferencePanel(face));
    }
    
    public void notifyMsAccessInterrupted() {
        ((MsAccessPreferencePanel)getPreferencePanel()).notifyMsAccessInterrupted();
    }

}
