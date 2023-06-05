/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A splash screen serves for loading status
 * <p>
 * @author Zhijun Zhang
 */
class PbcSplashScreen implements IPbcSplashScreen {
    private static final Logger logger;
    private static final String errMsg;
    static{
        logger = Logger.getLogger(PbcSplashScreen.class.getName());
        errMsg = "Cannot load splash screen. Possibly -splash:splash.jpg option was missed.";
    }
    
    private final long waitingMillionSeconds = 75;
    private final float progressStep = 0.03F;
    private static float currentProgress = 0.0F;

    private final SplashScreen splashScreen;
    private final SplashScreenSettings screenSettings;

    private Graphics2D g2D;

    PbcSplashScreen(SplashScreen splash,
                         SplashScreenSettings screenSettings)
    {
        this.splashScreen = splash;
        this.screenSettings = screenSettings;
        if (splashScreen == null){
            logger.log(Level.SEVERE, errMsg);
            g2D = null;
        }else{
            g2D = splashScreen.createGraphics();
        }
    }

    public boolean isVisible(){
        if (splashScreen == null){
            return false;
        }else{
            return splashScreen.isVisible();
        }
    }

    public void display() {
        if (g2D == null){
            logger.log(Level.SEVERE, errMsg);
        }else{
            updateSplashScreen("Start launching PointBox Console...", Level.INFO, 50);
            try {
                Thread.sleep(waitingMillionSeconds);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 
     * @param msg
     * @param duration
     */
    @Override
    public void updateSplashScreen(String msg, Level level, long duration){
        if (!splashScreen.isVisible()){
            return;
        }
        g2D.setComposite(AlphaComposite.Clear);
        g2D.fillRect(0,0, screenSettings.getWidth(), screenSettings.getHeight());
        g2D.setPaintMode();
        g2D.setColor(Color.RED);
        g2D.drawLine(0, screenSettings.getProgess_line_pos_y(), screenSettings.getWidth(), screenSettings.getProgess_line_pos_y());
        g2D.setColor(Color.yellow);
        g2D.drawString(msg, screenSettings.getText_pos_x(), screenSettings.getText_pos_y());
        currentProgress += progressStep;
        g2D.drawLine(0, screenSettings.getProgess_line_pos_y(), (int)(currentProgress * screenSettings.getWidth()), screenSettings.getProgess_line_pos_y());
        splashScreen.update();
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {
            Logger.getLogger(PbcSplashScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close(){
        if (splashScreen != null){
            splashScreen.close();
        }
    }
}
