/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.ISimPricingRuntimeManager;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 27, 2014 - 1:13:39 PM
 */
class SimPricingRuntimeManager extends PricingRuntimeManager implements ISimPricingRuntimeManager{

    private static final Logger logger;
    static{
        logger = Logger.getLogger(SimPricingRuntimeManager.class.getName());
    }

    private PointBoxQuoteCode simCode;

    public SimPricingRuntimeManager(IPbcKernel kernel, File filesFolder) throws IOException {
        super(kernel, filesFolder);
    }
    
    @Override
    public synchronized void setSimCode(PointBoxQuoteCode simCode) {
        if ((simCode != null) || (!(simCode.equals(this.simCode)))){
            this.simCode = simCode;
        }
    }

    @Override
    public synchronized PointBoxQuoteCode getSimCode() {
        if (simCode == null){
            simCode = getKernel().getDefaultSimCodeFromProperties();
        }
        return simCode;
    }
}
