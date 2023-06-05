/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 7:26:20 PM
 */
public class MessagingServerSettingsDialog extends PreferenceDialog{
    
    public MessagingServerSettingsDialog(IPbcFace face, PreferencePanelType type) {
        super(face, new MessagingServerPreferencePanel(face, type));
    }

}
