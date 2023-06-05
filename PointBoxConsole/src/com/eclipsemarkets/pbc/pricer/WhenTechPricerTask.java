/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.AbstractPricerTask;
import com.eclipsemarkets.pricer.WtQuoteWrapper;
import com.eclipsemarkets.pricer.host.PointBoxQuoteSplitter.PbcQuoteSplitterException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
public class WhenTechPricerTask extends AbstractPricerTask {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(WhenTechPricerTask.class.getName());
    }
    private WhenTechPricerWrapper pricer;

    public WhenTechPricerTask(IPbcKernel kernel, WhenTechPricerWrapper pricer, List<IPbsysOptionQuote> quotes) {
        super(kernel, quotes);
        this.pricer = pricer;
    }

    @Override
    public void run() {
        int numOfQuotes = quotes.size();
        //load quoteStrings
        IPbsysOptionQuote quote;
        for (int i = 0; i < numOfQuotes; i++) {
            quote = quotes.get(i);
            quote.prepareForPricingAndStorage();
            quote.backupParsedResults();
            if (quote.isSufficientPricingData()){
                try {
                    pricer.invokeWhenTechPricing(new WtQuoteWrapper(kernel, pricer, quote));
                } catch (PbcQuoteSplitterException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            }
        }
    }
}
