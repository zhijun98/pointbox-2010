/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face;

import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import java.util.logging.Logger;

/**
 * PbcFaceFactory
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 23, 2011 at 10:28:18 PM
 */
public class PbcFaceFactory {
    private static final Logger logger;
    private static PbcFace pbcFace;
    static{
        logger = Logger.getLogger(PbcFaceFactory.class.getName());
        pbcFace = null;
    }

    private PbcFaceFactory() {
    }

    /**
     * if kernel is null, NULL will be returned
     * @param kernel
     * @return
     * @throws PointBoxFatalException
     */
    public static IPbcFace getPbcFaceSingleton(IPbcKernel kernel) throws PointBoxFatalException  {
        if (pbcFace == null){
            if (kernel != null){
                pbcFace = new PbcFace(kernel);
            }
        }
        return pbcFace;
    }

}//PbcFaceFactory

