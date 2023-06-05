/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:24:07 PM
 */
public class ViewerFontColorSettingsDialog extends PreferenceDialog{

    public ViewerFontColorSettingsDialog(IPbcFace face) {
        super(face, new ViewerFontColorPreferencePanel(face));
    }
    
}
