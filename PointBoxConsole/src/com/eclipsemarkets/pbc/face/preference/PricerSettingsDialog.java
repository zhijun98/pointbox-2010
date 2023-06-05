/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:33:40 PM
 */
public class PricerSettingsDialog extends PreferenceDialog{

    public PricerSettingsDialog(IPbcFace face) {
        super(face, new PricerPreferencePanel(face));
    }
}
