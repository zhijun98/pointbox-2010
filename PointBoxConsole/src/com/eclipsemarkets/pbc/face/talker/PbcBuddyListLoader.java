/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import java.util.Collection;
import java.util.HashMap;

/**
 * There are two cases: (1) user just log in and a possibly-very-long buddy-list is loading now. This 
 * procedure continually invoke this method; (2) during user login-session, occasionally, this event 
 * could be invoked. This class works for the case one. The other case is handled by the un-efficient 
 * method - getDndBuddyTree().handleBuddyItemPresentedEvent(buddy);
 * <p/>
 * This implementation is thread-safe
 * @author Zhijun Zhang, date & time: Dec 19, 2013 - 12:01:57 PM
 */
class PbcBuddyListLoader {

    private static PbcBuddyListLoader self;
    static {
        self = null;
    }
    
    /**
     * KEY: loginUser'sunique name; VALUE: PbcBuddyListLoaderMonitor
     */
    private final HashMap<String, PbcBuddyListLoaderMonitor> buddyListLoadingMonitorMap = new HashMap<String, PbcBuddyListLoaderMonitor>();
    
    private PbcBuddyListLoader(){
    }
    
    public static PbcBuddyListLoader getSingleton(){
        if (self == null){
            self = new PbcBuddyListLoader();
        }
        return self;
    }
    
    private synchronized PbcBuddyListLoaderMonitor valideBufferBuddy(IGatewayConnectorBuddy buddy, IBuddyListPanel buddyListPanel){
        if ((buddy == null) || (buddy.getLoginOwner() == null)){
            return null;
        }
        IGatewayConnectorBuddy loginUser = buddy.getLoginOwner();
        return startBuddyListLoading(loginUser, buddyListPanel);
    }
    
    /**
     * 
     * @param buddy
     * @return - whether or not it is buffered (i.e. processed by this loader)
     */
    synchronized boolean bufferBuddyItemPresentedEventBuddy(IGatewayConnectorBuddy buddy, IBuddyListPanel buddyListPanel) {
        PbcBuddyListLoaderMonitor monitor = valideBufferBuddy(buddy, buddyListPanel);
        if (monitor == null){
            return false;
        }else{
            monitor.bufferBuddyItemPresentedEventBuddy(buddy);
            return true;
        }
    }

    /**
     * 
     * @param buddy
     * @return - if it is case (1), this will be true; otherwise, it means this loader does not handle it 
     * and this return-value will be false;
     */
    synchronized boolean bufferBuddyStatusChangedEventBuddy(IGatewayConnectorBuddy buddy, IBuddyListPanel buddyListPanel) {
        PbcBuddyListLoaderMonitor monitor = valideBufferBuddy(buddy, buddyListPanel);
        if (monitor == null){
            return false;
        }else{
            monitor.bufferBuddyStatusChangedEventBuddy(buddy);
            return true;
        }
    }
    
    private String getMonitorMapKey(IGatewayConnectorBuddy loginUser, IBuddyListPanel buddyListPanel){
        return buddyListPanel.getDistListName() + "_" + buddyListPanel.getDistListName()+ "_" + loginUser.getIMUniqueName();
    }

    /**
     * This method is quick and can be safely called multiple times without hurt anything. 
     * After it is started for such a loginUser, it will be automatically stopped after 
     * THRESHOLD (currently THRESHOLD = 60) seconds.
     * @param loginUser
     * @param jTree 
     */
    private synchronized PbcBuddyListLoaderMonitor startBuddyListLoading(IGatewayConnectorBuddy loginUser, IBuddyListPanel buddyListPanel) {
        String key = getMonitorMapKey(loginUser, buddyListPanel);
        PbcBuddyListLoaderMonitor monitor = buddyListLoadingMonitorMap.get(key);
        if ((monitor == null) || (monitor.isStopped())){
            monitor = new PbcBuddyListLoaderMonitor(loginUser, buddyListPanel.getDndBuddyTree());
            monitor.startMonitor();
            buddyListLoadingMonitorMap.put(key, monitor);
        }
        return monitor;
    }

    /**
     * This method is quick and can be safely called multiple times without hurt anything
     * @param loginUser 
     */
    synchronized void stopBuddyListLoading(IGatewayConnectorBuddy loginUser, IBuddyListPanel buddyListPanel) {
        if ((loginUser == null) || (buddyListPanel == null)){
            return;
        }
        String key = getMonitorMapKey(loginUser, buddyListPanel);
        PbcBuddyListLoaderMonitor monitor = buddyListLoadingMonitorMap.get(key);
        //remove it anyway for stop as soon as possible
        buddyListLoadingMonitorMap.remove(key);
        if (monitor != null){
            monitor.stopMonitor();
        }
    }

    /**
     * Stop entire loader
     */
    synchronized void stopBuddyListLoader() {
        Collection<PbcBuddyListLoaderMonitor> monitorList = buddyListLoadingMonitorMap.values();
        for (PbcBuddyListLoaderMonitor monitor : monitorList){
            monitor.stopMonitor();
        }
        buddyListLoadingMonitorMap.clear();
    }
}
