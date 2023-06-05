/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.data.PointBoxCurveType;
import java.util.ArrayList;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 6:33:49 PM
 */
public interface ICloudingPublishPreferencePanel extends IPreferencePanel{

    /**
     * Selected file types for uploading pricing runtime settings to server. Only valid for administrator
     */
    public ArrayList<PointBoxCurveType> getSelectedFileTypesForPricingSettingsUploadAdmin();

}
