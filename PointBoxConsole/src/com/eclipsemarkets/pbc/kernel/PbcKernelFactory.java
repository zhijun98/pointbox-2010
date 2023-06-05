/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.kernel;

import com.eclipsemarkets.pbc.IPbcSplashScreen;
import java.util.logging.Logger;

/**
 * PbcKernelFactory
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 28, 2011 at 5:06:52 PM
 */
public class PbcKernelFactory {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbcKernelFactory.class.getName());
    }

    private static IPbcKernel kernel;
    static{
        kernel = null;
    }

    public static IPbcKernel getKernelSingleton(final IPbcSplashScreen splash){
        if (kernel == null){
            kernel = new PbcKernel(splash);
        }
        return kernel;
    }

    private PbcKernelFactory() {
    }

}//PbcKernelFactory

