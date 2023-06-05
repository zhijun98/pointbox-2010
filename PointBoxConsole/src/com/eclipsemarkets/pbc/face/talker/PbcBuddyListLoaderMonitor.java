/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.talker.dndtree.IPbcDndBuddyTree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zhijun Zhang, date & time: Jan 5, 2014 - 5:43:09 PM
 */
class PbcBuddyListLoaderMonitor {
        
    private final ArrayList<IGatewayConnectorBuddy> buddyItemPresentedEventBuffer = new ArrayList<IGatewayConnectorBuddy>();
    private final ArrayList<IGatewayConnectorBuddy> buddyStatusChangedEventBuffer = new ArrayList<IGatewayConnectorBuddy>();

    private final int THRESHOLD = 30;   //30 seconds
    private final int sleepStep = 1;    //1 second

    private IGatewayConnectorBuddy loginUser;   //reserved for the future
    //how long its buddy list loading lasts. When the "duration" is overflow the threshold, stopBuddyListLoading() will be invoked
    private int duration = -1;

    private Thread monitorThread = null;

    private IPbcDndBuddyTree jTree;

    public PbcBuddyListLoaderMonitor(IGatewayConnectorBuddy loginUser, IPbcDndBuddyTree jTree) {
        this.loginUser = loginUser;
        this.jTree = jTree;
    }

    synchronized boolean bufferBuddyItemPresentedEventBuddy(IGatewayConnectorBuddy buddy) {
        if (duration >= 0){
            buddyItemPresentedEventBuffer.add(buddy);
            return true;
        }else{
            return false;
        }
    }

    private synchronized List<IGatewayConnectorBuddy> retrieveBuddyItemPresentedEventBuddyList(){
        List<IGatewayConnectorBuddy> result = DataGlobal.copyObjectList(buddyItemPresentedEventBuffer);
        buddyItemPresentedEventBuffer.clear();
        return result;
    }

    synchronized boolean bufferBuddyStatusChangedEventBuddy(IGatewayConnectorBuddy buddy) {
        if (duration >= 0){
            buddyStatusChangedEventBuffer.add(buddy);
            return true;
        }else{
            return false;
        }
    }

    private synchronized List<IGatewayConnectorBuddy> retrieveBuddyStatusChangedEventBuddyList(){
        List<IGatewayConnectorBuddy> result = DataGlobal.copyObjectList(buddyStatusChangedEventBuffer);
        buddyStatusChangedEventBuffer.clear();
        return result;
    }

    synchronized void startMonitor() {
        if ((monitorThread == null) || (!monitorThread.isAlive())){
            //build thread instance...
            monitorThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    while(true){
                        try {
                            Thread.sleep(sleepStep * 1000);
                        } catch (InterruptedException ex) {
                            break;
                        }
                        synchronized(PbcBuddyListLoaderMonitor.this){
                            jTree.handleBuddyItemPresentedEventInBatch(retrieveBuddyItemPresentedEventBuddyList());
                            jTree.handleBuddyStatusChangedEventInBatch(retrieveBuddyStatusChangedEventBuddyList());
                            duration = duration + sleepStep;
                            if (duration > THRESHOLD){
                                stopMonitor();
                                break; //if duration == -1, thread-loop is stopped.
                            }
                        }//synchronized(PbcBuddyListLoaderMonitor.this){
                    }//while...
                }
            });
            monitorThread.start();
        }
        duration = 0;   //start buffer
    }

    synchronized void stopMonitor() {
        if (monitorThread != null){
            monitorThread.interrupt();
            monitorThread = null;
        }
        duration = -1; //stop buffer
    }

    synchronized boolean isStopped() {
        return (duration < 0);
    }

}
