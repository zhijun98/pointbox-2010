/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 9:03:43 PM
 */
public class BlockedBuddiesSettingsDialog extends PreferenceDialog{

    public BlockedBuddiesSettingsDialog(IPbcFace face) {
        super(face, new BlockedBuddiesPreferencePanel(face));
    }

}
