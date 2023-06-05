/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime;

import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import java.util.logging.Logger;

/**
 * PbcRuntimeFactory
 * <P>
 * Factory of runtime for PointBoxConsole
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 23, 2011 at 10:25:51 PM
 */
public class PbcRuntimeFactory {
    private static final Logger logger;
    private static IPbcRuntime pbcRuntime;
    static{
        logger = Logger.getLogger(PbcRuntimeFactory.class.getName());
        pbcRuntime = null;
    }

    private PbcRuntimeFactory() {
    }

    /**
     * if it is not there, it will be created. if kernel is null, NULL will be returned.
     * @param kernel
     * @return
     * @throws PointBoxFatalException
     */
    public static IPbcRuntime getPbconsoleRuntimeSingleton(IPbcKernel kernel) throws PointBoxFatalException {
        if (pbcRuntime == null){
            if (kernel != null){
                pbcRuntime = new PbcRuntime(kernel);
            }
        }
        return pbcRuntime;
    }

}//PbcRuntimeFactory

