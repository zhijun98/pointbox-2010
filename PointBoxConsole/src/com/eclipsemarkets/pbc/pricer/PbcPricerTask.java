/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.AbstractPricerTask;
import com.eclipsemarkets.pricer.IPointBoxPricer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
class PbcPricerTask extends AbstractPricerTask {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcPricerTask.class.getName());
    }

    private IPointBoxPricer pricer;
    
    PbcPricerTask(IPbcKernel kernel, IPointBoxPricer pricer, List<IPbsysOptionQuote> quotes) {
        super(kernel, quotes);
        this.pricer = pricer;
    }

    @Override
    public void run() {
        if (quotes instanceof ArrayList){
            try {
                pricer.evaluateQuotesPrice((ArrayList<IPbsysOptionQuote>)quotes);
            } catch (Exception ex) {
                kernel.updateSplashScreen(ex.getMessage(), Level.INFO, 1000);
            }
        }
    }
    
}
