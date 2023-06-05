/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.pricer.PbcPricerType;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
class PbcPricerTypeLock {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcPricerTypeLock.class.getName());
    }
    
    private PbcPricerType pricerType;

    PbcPricerTypeLock(PbcPricerType pricerType) {
        this.pricerType = pricerType;
    }

    public synchronized PbcPricerType getPricerType() {
        return pricerType;
    }

    public synchronized void setPricerType(PbcPricerType pricerType) {
        this.pricerType = pricerType;
    }
}
