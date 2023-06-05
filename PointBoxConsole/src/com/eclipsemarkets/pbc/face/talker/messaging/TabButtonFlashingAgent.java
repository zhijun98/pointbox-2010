/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.PointBoxTabFlashProperties;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * TabFlashingAgent
 * <P>
 * A singleton manager to flash all the tabs who got messages from the outside
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 29, 2011 at 10:06:07 AM
 */
class TabButtonFlashingAgent {

    private static final Logger logger = Logger.getLogger(TabButtonFlashingAgent.class.getName());
    private static TabButtonFlashingAgent self;
    static{
        self = null;
    }
    
    /**
     * key: tabUniqueID; value: counter of flashing seconds
     */
    private final HashMap<String, Integer> tabIndices;
    private Thread flashingThread;
    
    private final int THRESHOLD = 10; 
    
    private IMasterMessagingBoard tabManager;
    private HashMap messageTabColorSetting;
    private Color flashingTabForeground_0;
    private Color flashingTabForeground_1;
    private Color flashingTabBackground_0;
    private Color flashingTabBackground_1;
    
    static TabButtonFlashingAgent getSingleton(IMasterMessagingBoard tabManager){
        if (self == null){
            self = new TabButtonFlashingAgent(tabManager);
        }
        return self;
    }
    
    private TabButtonFlashingAgent(IMasterMessagingBoard tabManager) {
        tabIndices = new HashMap<String, Integer>();
        flashingThread = null;
        this.tabManager = tabManager;
        messageTabColorSetting = PointBoxTabFlashProperties.getSingleton().gettabFlashetting();
        flashingTabForeground_0 = Color.BLUE;
        flashingTabBackground_0 = new Color(255, 255, 250);
        flashingTabForeground_1 = Color.decode((String)messageTabColorSetting.get("flashingTabForeground"));//Color.RED;
        flashingTabBackground_1 = Color.decode((String)messageTabColorSetting.get("flashingTabBackground"));//Color.BLUE;
    }
    
    synchronized void startFlashingTab(String tabUniqueID){
        if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
            tabIndices.put(tabUniqueID, 0);   //if index was there, restore counter to 0
            if (flashingThread == null){
                flashingThread = new TabFlashingThread();
                flashingThread.start();
            }
        }
    }
    
    synchronized void stopFlashingTab(String tabUniqueID){
        if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
            tabIndices.remove(tabUniqueID);
            if (tabIndices.isEmpty()){
                if (flashingThread != null){
                    if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
                        List<IButtonTabComponent> tabButtonList = MessagingPaneManager.getSingleton(tabManager.getTalker()).getAllButtonTabComponentsWithUniqueID(tabUniqueID);
                        tabManager.setOpaqueInEDT(tabButtonList, true);
                        tabManager.setTabButtonForegroundAtInEDT(tabButtonList, flashingTabForeground_1);
                        tabManager.setTabButtonBackgroundAtInEDT(tabButtonList, flashingTabBackground_1);
                    }
                    flashingThread.interrupt();
                    flashingThread = null;
                }
            }
        }
    }
    
//    synchronized void stopFlashingTabForDrag(String tabUniqueID){
//        if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
//            tabIndices.remove(tabUniqueID);
//            if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
//                List<IButtonTabComponent> tabButtonList = MessagingPaneManager.getSingleton(tabManager.getTalker()).getAllButtonTabComponentsWithUniqueID(tabUniqueID);
//                tabManager.setOpaqueInEDT(tabButtonList, true);
//                tabManager.setTabButtonForegroundAtInEDT(tabButtonList, flashingTabForeground_1);
//                tabManager.setTabButtonBackgroundAtInEDT(tabButtonList, Color.GRAY);
//            }
//            if (tabIndices.isEmpty()){
//                if (flashingThread != null){
//                    flashingThread.interrupt();
//                    flashingThread = null;
//                }
//            }
//        }
//    }
    
    private synchronized ArrayList<String> getFlashingTabUniqueIDs(){
        ArrayList<String> uniqueIDs = new ArrayList<String>();
        Set<String> keys = tabIndices.keySet();
        Iterator<String> itr = keys.iterator();
        while(itr.hasNext()){
            uniqueIDs.add(itr.next());
        }
        return uniqueIDs;
    }

    private class TabFlashingThread extends Thread{
        @Override
        public void run() {
            Color nextForeground = flashingTabForeground_1;
            Color nextBackground = flashingTabBackground_1;
            ArrayList<String> stoppingIndices;
            while (true){
                final Color currentForeground = nextForeground;
                final Color currentBackground = nextBackground;
                final ArrayList<String> flashingTabUniqueIDs = getFlashingTabUniqueIDs();
                stoppingIndices = new ArrayList<String>();
                int counter;
                List<IButtonTabComponent> tabButtonList;
                for (String tabUniqueID : flashingTabUniqueIDs){
                    if (DataGlobal.isNonEmptyNullString(tabUniqueID)){
                        tabButtonList = MessagingPaneManager.getSingleton(tabManager.getTalker()).getAllButtonTabComponentsWithUniqueID(tabUniqueID);
                        tabManager.setOpaqueInEDT(tabButtonList, true);
                        tabManager.setTabButtonForegroundAtInEDT(tabButtonList, currentForeground);
                        tabManager.setTabButtonBackgroundAtInEDT(tabButtonList, currentBackground);
                    }
                    counter = tabIndices.get(tabUniqueID) + 1;
                    tabIndices.put(tabUniqueID, counter);
                    if (counter > THRESHOLD){
                        stoppingIndices.add(tabUniqueID);
                    }
                }//for
                if (currentForeground.equals(flashingTabForeground_1)){
                    nextForeground = flashingTabForeground_0;
                    nextBackground = flashingTabBackground_0;
                }else{
                    nextForeground = flashingTabForeground_1;
                    nextBackground = flashingTabBackground_1;
                }
                for (String tabUniqueID : stoppingIndices){
                    stopFlashingTab(tabUniqueID);
                }
                try {
                    Thread.sleep(750);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    
    }
}//TabFlashingAgent

