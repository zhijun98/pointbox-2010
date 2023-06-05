/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.IPointBoxPricingEnvironment;
import com.eclipsemarkets.pricer.IPricingRuntimeManager;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 27, 2014 - 1:11:54 PM
 */
class SimPricerWrapper extends PbcPricerWrapper {

    private static final Logger logger;
    static{
        logger = Logger.getLogger(SimPricerWrapper.class.getName());
    }

    public SimPricerWrapper(IPbcKernel kernel, boolean autoPricing) {
        super(kernel, autoPricing);
    }
    
    @Override
    public boolean isAutoPricing() {
        return false;
    }

    @Override
    protected IPricingRuntimeManager createPricingRuntimeManager() {
        try {
            return new SimPricingRuntimeManager(kernel, pricingRuntimeFolder);
        } catch (IOException ex) {
            PointBoxTracer.recordSevereException(logger, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected void refreshPricingEnvironment() {
        IPointBoxPricingEnvironment env = getPointBoxPricingEnvironmentInstance();
        if ((env != null) && (env.isValid())){
            env.refreshPricingEnvironment();
            getPricer().setPricingEnvironment(env);
        }
    }

}
