/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;

/**
 *
 * @author Zhijun Zhang, date & time: May 21, 2014 - 5:09:12 PM
 */
public class PricingCurveCodeBasedSelectorAgent extends AbstractCodeBasedSelectorAgent{
    private PointBoxQuoteCode targetCode;

    public PricingCurveCodeBasedSelectorAgent(IPbcKernel kernel, PointBoxQuoteCode targetCode) {
        super(kernel);
        if (targetCode == null){
            targetCode = PointBoxQuoteCode.LN;
        }
        this.targetCode = targetCode;
    }

    PointBoxQuoteCode getTargetCode() {
        return targetCode;
    }

    void setTargetCode(PointBoxQuoteCode targetCode) {
        this.targetCode = targetCode;
    }

    @Override
    boolean isValidQuoteType(PointBoxQuoteCode code) {
        return true;
    }

    @Override
    void initializeSelectors() {
        setupClassSelector();
        super.setClassGroupCodeSelectorForSpecificCode(targetCode);
    }

}
