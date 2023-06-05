/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage;

import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import java.util.logging.Logger;

/**
 * PbcStorageFactory
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 23, 2011 at 7:37:31 PM
 */
public class PbcStorageFactory {
    private static final Logger logger;
    private static IPbcStorage pbcLocalDerby;
    static{
        logger = Logger.getLogger(PbcStorageFactory.class.getName());
        pbcLocalDerby = null;
    }

    private PbcStorageFactory() {
    }

    /**
     * if kernel is null, NULL will be returned
     * @param kernel
     * @param storageType
     * @return
     * @throws PointBoxFatalException
     */
    public static IPbcStorage getPbcStorageSingleton(IPbcKernel kernel,
                                                     PbcStorageType storageType) throws PointBoxFatalException {
        switch (storageType){
            case LocalDerby:
                if (pbcLocalDerby == null){
                    if (kernel != null){
                        pbcLocalDerby = new PbcStorage(kernel);
                    }
                }
                return pbcLocalDerby;
            case MSAccess:
                throw new PointBoxFatalException("PbcStorage:MSAccess Not yet implemented");
            default:
                throw new PointBoxFatalException("PbcStorage type is not supported");
        }
    }

    /**
     * Get a singleton local derby which is possibly null
     * @return
     */
    public static IPbcStorage getLocalDerbySingleton() {
        return pbcLocalDerby;
    }

}//PbcStorageFactory

