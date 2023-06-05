/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.web.pbc.PbcPricingModel;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 5:54:50 PM
 */
public interface ICurvePreferencePanel extends IPreferencePanel{

    public void downloadPricingRuntimeCurveFiles(boolean displayCompleteMessage);

    public void downloadPricingRuntimeCurveFiles(PbcPricingModel aPbcPricingModel, boolean displayCompleteMessage);

    public void uploadPricingRuntimeCurveFiles();

}
