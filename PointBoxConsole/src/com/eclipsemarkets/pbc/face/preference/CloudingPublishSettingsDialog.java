/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.data.PointBoxCurveType;
import java.util.ArrayList;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 6:27:45 PM
 */
public class CloudingPublishSettingsDialog extends PreferenceDialog{

    public CloudingPublishSettingsDialog(IPbcFace face) {
        super(face, new CloudingPublishPreferencePanel(face));
    }

    public ArrayList<PointBoxCurveType> getSelectedFileTypesForPricingSettingsUploadAdmin() {
        return ((ICloudingPublishPreferencePanel)getPreferencePanel()).getSelectedFileTypesForPricingSettingsUploadAdmin();
    }

}
