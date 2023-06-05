/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:44:52 PM
 */
public class CurveSettingsDialog extends PreferenceDialog{
    
    public CurveSettingsDialog(IPbcFace face) {
        super(face, new CurvePreferencePanel(face));
    }
        
    @Override
    public void displayPreferenceDialog() {
        ((CurvePreferencePanel)getPreferencePanel()).refreshTitle();
        super.displayPreferenceDialog();
    }

}
