/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.pricer.QuoteAutoPricedEvent;
import com.eclipsemarkets.event.pricer.QuotePricedEvent;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.AbstractPricerWrapper;
import com.eclipsemarkets.pricer.IPointBoxPricer;
import com.eclipsemarkets.pricer.IPointBoxPricingEnvironment;
import com.eclipsemarkets.pricer.IPricingRuntimeManager;
import com.eclipsemarkets.pricer.PointBoxPricer;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper of Java-based pricer - this is the wrapper of so called local pricer
 * @author Zhijun Zhang
 */
class PbcPricerWrapper extends AbstractPricerWrapper {

    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbcPricerWrapper.class.getName());
    }
    
    private PointBoxPricer pricer;
    private boolean autoPricing;
    
    PbcPricerWrapper(IPbcKernel kernel, boolean autoPricing) {
        super(kernel);
        /**
         * This local pricer has to be lazy-loaded because it has to wait for user login completed 
         * so as to get the pricing environment location.
         */
        this.autoPricing = autoPricing;
        pricer = null;
    }
    
    /**
     * calculating quotes, blocking and waiting for completion......and eventually return
     * @param quotes
     */
    @Override
    public void pricing(List<IPbsysOptionQuote> quotes) {
        if ((quotes == null) || (quotes.isEmpty())) {
            return;
        }
        if (checkupPricingEnvironmentAndRefresh()){
            if (isAutoPricing()){
                while (getPricer().isRefreshingEnvironment()){
                    try {
                        /**
                         * wait for its completeness 
                         */
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }//while
                if (!getPricer().isRefreshingEnvironment()){
                    submitForPricing(quotes);
                }
                kernel.raisePointBoxEvent(new QuoteAutoPricedEvent(PointBoxEventTarget.PbcFace,
                                                                    quotes));
            }else{
                //in the case of this regular pricer, don't wait for refreshingEnvironment completed
                submitForPricing(quotes);
                kernel.raisePointBoxEvent(new QuotePricedEvent(PointBoxEventTarget.PbcFace,
                                                                    quotes));
            }
        }
    }
    
    /**
     * 
     * @return - whether or not pricing is necessary for auto-pricer
     */
    boolean checkupPricingEnvironmentAndRefresh(){
        boolean isPricingSettingsChanged = isPricingSettingsChanged();
        if (isPricingSettingsChanged){
            refreshPricingEnvironment();
        }
        if (isAutoPricing()){
            if (isPricingSettingsChanged){
                return true;
            }else{
                return false;
            }
        }else{
            //Regular pricer always price
            return true;
        }
    }
    
    @Override
    public boolean isAutoPricing() {
        return autoPricing;
    }

    @Override
    protected IPricingRuntimeManager createPricingRuntimeManager() {
        try {
            return new PricingRuntimeManager(kernel, pricingRuntimeFolder);
        } catch (IOException ex) {
            PointBoxTracer.recordSevereException(logger, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected IPointBoxPricer getPricer() {
        if (pricer == null){
            pricer = new PointBoxPricer(getPointBoxPricingEnvironmentInstance());
        }
        return pricer;
    }

    @Override
    protected IPointBoxPricingEnvironment getPointBoxPricingEnvironmentInstance() {
        if (kernel == null){
            return null;
        }
        IPointBoxPricingEnvironment env = PbcPricingEnvironment.getSingleton(kernel);
        if (env.isValid()){
            return env;
        }else{
            return null;
        }
    }

    @Override
    protected Runnable createPricerTask(List<IPbsysOptionQuote> quotes) {
        return new PbcPricerTask(kernel, getPricer(), quotes);
    }

    @Override
    protected void priceForSingleQuote(IPbsysOptionQuote aQuote) {
        try {
            getPricer().evaluatePriceForSingleQuote(aQuote);
        } catch (Exception ex) {
            Logger.getLogger(PbcPricerWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected boolean isPricingEngineLoaded() {
        return true;
    }
    
}
