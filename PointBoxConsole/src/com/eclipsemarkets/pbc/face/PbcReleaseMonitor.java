/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face;

import com.eclipsemarkets.pbc.web.PbcReleaseInformation;
import com.eclipsemarkets.pbc.web.PbcReleaseStatus;
import java.util.logging.Logger;

/**
 * PbcReleaseMonitor
 * <P>
 * A dedicated monitor release version of this console. If the server-side has updates, 
 * it will pop up update window for users to initiate a procedure of PBC update
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 20, 2011 at 11:57:57 AM
 */
class PbcReleaseMonitor {

    private static final Logger logger = Logger.getLogger(PbcReleaseMonitor.class.getName());

    private IPbcFace face;
    
    private Thread pbcReleaseChecker;

    private final long INTERVAL;
    
    PbcReleaseMonitor() {
        face = null;
        INTERVAL = 24*60*60*1000; //24 hour/One day
        pbcReleaseChecker = new Thread(new PbcReleaseChecker());
    }
    
    /**
     * invoke release monitor
     */
    void invoke(IPbcFace face) {
        this.face = face;
        if ((pbcReleaseChecker != null) && (!pbcReleaseChecker.isAlive())){
            pbcReleaseChecker.start();
        }
    }

    void shutdown() {
        if (pbcReleaseChecker != null) {
            pbcReleaseChecker.interrupt();
            pbcReleaseChecker = null;
        }
    }

    private class PbcReleaseChecker implements Runnable{
        @Override
        public void run() {
            try {
                while (true){
                    if (face != null){
                        PbcReleaseInformation releaseInfo = face.checkPbcRelease();
                        if (!releaseInfo.getPbcReleaseStatus().equals(PbcReleaseStatus.Latest_Release)){
////                            if (releaseInfo.getPbcReleaseStatus().equals(PbcReleaseStatus.Supported_Previous_Release)){
//                                face.notifyReleaseUpdateRequired(releaseInfo);
////                            }else{
////                                face.getKernel().raisePointBoxEvent(new ServerNotSupportedEvent(PointBoxEventTarget.PbcFace));
////                            }
                            face.displayPbcReleaseUpdateDialog(releaseInfo);
                        }
                        Thread.sleep(INTERVAL);
                    }
                }//while
            } catch (InterruptedException ex) {
                //logger.log(Level.SEVERE, null, ex);
            }
        }
    }
}//PbcReleaseMonitor

