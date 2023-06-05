/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc;

import com.eclipsemarkets.pbc.face.PbcFaceFactory;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PbcKernelFactory;
import com.eclipsemarkets.pbc.pricer.PbcPricingAgent;
import com.eclipsemarkets.pbc.runtime.PbcRuntimeFactory;
import com.eclipsemarkets.pbc.storage.PbcStorageFactory;
import com.eclipsemarkets.pbc.storage.PbcStorageType;
import com.eclipsemarkets.pbc.web.IPointBoxConsoleWebProxy;
import com.eclipsemarkets.web.PointBoxWebProxyFactory;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import java.awt.SplashScreen;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * PointBoxConsoleLauncher
 * <P>
 * Initialize PointBox console client-side application
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 23, 2011 at 5:18:21 PM
 */
public class PointBoxConsoleLauncher {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PointBoxConsoleLauncher.class.getName());
    }

    private PointBoxConsoleLauncher() {
    }

    private static IPbcKernel invokeKernal(PbcSplashScreen splash) throws PointBoxFatalException {

        IPbcKernel kernel = PbcKernelFactory.getKernelSingleton(splash);

        splash.updateSplashScreen("Launch " + kernel.getSoftwareName() + "(" + kernel.getSoftwareVersion() + ") now...", Level.INFO, 300);

        kernel.registerPbcComponent(PbcStorageFactory.getPbcStorageSingleton(kernel, PbcStorageType.LocalDerby));
        IPointBoxConsoleWebProxy web = PointBoxWebProxyFactory.getPointBoxWebProxySingelton(kernel);
        if (web instanceof IPbcComponent){
            kernel.registerPbcComponent((IPbcComponent)web);
        }
        kernel.registerPbcComponent(PbcRuntimeFactory.getPbconsoleRuntimeSingleton(kernel));
        kernel.registerPbcComponent(PbcPricingAgent.getPbcPricingAgentSingleton(kernel));
        kernel.registerPbcComponent(PbcFaceFactory.getPbcFaceSingleton(kernel));

        kernel.invoke();
        
        return kernel;
    }

    private static void launchPointBoxConsole(boolean alreadyRunning) {
        try {
            //launch splash screen ...
            SplashScreenSettings pbconsole_splash = new SplashScreenSettings(460, 239, 163, 5, 176);
            PbcSplashScreen splash = new PbcSplashScreen(SplashScreen.getSplashScreen(), pbconsole_splash);
            splash.display();
            if (alreadyRunning){
                final String msg = "There is already an instance of PBC running on this "
                        + "computer. If PBC has recently been \n closed, please wait a few "
                        + "moments while the application saves your workspace settings. "
                        + "\nOtherwise, if you use the same login, the other connection could "
                        + "possibly be cutoff. \n\nDo you want to continue to launch PointBox "
                        + "Console?\n\n";
//                if(SwingUtilities.isEventDispatchThread()){
//                    JOptionPane.showMessageDialog(null, msg);
//                }else{
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            JOptionPane.showMessageDialog(null, msg);
//                        }
//                    });
//                }
                if (JOptionPane.showConfirmDialog(null, msg, "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                    invokeKernal(splash);
                }
            }else{
                //invokeKernel PointBox console kernel
                invokeKernal(splash);
            }
        } catch (PointBoxFatalException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null, "[Fatal Error] " + ex.getMessage());
                Runtime.getRuntime().exit(1);
        }
    }
    
    public static void main(String[] args) {
        String appId = PointBoxConsoleLauncher.class.getCanonicalName();
	boolean alreadyRunning;
	try {
            JUnique.acquireLock(appId, new MessageHandler() {
                @Override
                public String handle(String message) {
                    // A brand new argument received! Handle it!
                    return null;
                }
            });
            alreadyRunning = false;
	} catch (AlreadyLockedException e) {
            alreadyRunning = true;
	}
        launchPointBoxConsole(alreadyRunning);
//        launchPointBoxConsole(false);
    }
}//PointBoxConsoleLauncher

