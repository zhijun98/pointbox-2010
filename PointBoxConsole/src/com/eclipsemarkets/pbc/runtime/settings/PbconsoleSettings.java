/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import java.util.logging.Logger;

/**
 * PbconsoleSettings
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Feb 28, 2011 at 11:03:02 AM
 */
 abstract class PbconsoleSettings implements IPbcSettings{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbconsoleSettings.class.getName());
    }

    IPbcRuntime runtime;

    PbconsoleSettings(IPbcRuntime runtime) {
        this.runtime = runtime;
    }

}//PbconsoleSettings

