/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * BuddyListTreePanelStore
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Oct 11, 2010, 4:58:35 PM
 */
public class BuddyListTreePanelStore {

    /**
     * Key: loginUser's unique name
     */
    private final HashMap<String, IBuddyListPanel> buddyListTreePanelStore;

    BuddyListTreePanelStore() {
        buddyListTreePanelStore = new HashMap<String, IBuddyListPanel>();
    }

    synchronized ArrayList<IGatewayConnectorBuddy> getAllLoginUsers() {
        ArrayList<IGatewayConnectorBuddy> loginUsers = new ArrayList<IGatewayConnectorBuddy>();
        Set<String> keys = buddyListTreePanelStore.keySet();
        Iterator<String> itr = keys.iterator();
        IBuddyListPanel panel;
        while (itr.hasNext()){
            panel = buddyListTreePanelStore.get(itr.next());
            if (panel.getMasterLoginUser() != null){
                loginUsers.add(panel.getMasterLoginUser());
            }
        }//while
        return loginUsers;
    }

    synchronized ArrayList<IGatewayConnectorBuddy> getAllAvaialbleBuddies(boolean sort) {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        Set<String> keys = buddyListTreePanelStore.keySet();
        Iterator<String> itr = keys.iterator();
        IBuddyListPanel panel;
        while (itr.hasNext()){
            panel = buddyListTreePanelStore.get(itr.next());
            if (panel.getMasterLoginUser() != null){
                buddies.addAll(panel.getAllBuddies(false));
            }
        }//while
        if (sort){
            PbcGlobal.sortBuddiesByUniqueNames(buddies);
        }
        return buddies;
    }

    synchronized boolean isLoginUser(IGatewayConnectorBuddy user) {
        boolean result = false;
        Set<String> keys = buddyListTreePanelStore.keySet();
        Iterator<String> itr = keys.iterator();
        IBuddyListPanel panel;
        while (itr.hasNext()){
            panel = buddyListTreePanelStore.get(itr.next());
            if (panel.getMasterLoginUser() != null){
                if (panel.getMasterLoginUser().equals(user)){
                    result = true;
                    break;
                }
            }
        }//while
        return result;
    }

    synchronized void displayOfflineBuddies(boolean value) {
        Set<String> keys = buddyListTreePanelStore.keySet();
        Iterator<String> itr = keys.iterator();
        IBuddyListPanel panel;
        while (itr.hasNext()){
            panel = buddyListTreePanelStore.get(itr.next());
            panel.displayOfflineBuddies(value);
        }//while
    }

    synchronized ArrayList<PbcBuddyListSettings> getRegularPbcBuddyListSettingsOfLiveConnectors() {
        ArrayList<PbcBuddyListSettings> pbcBuddyListSettings = new ArrayList<PbcBuddyListSettings>();
        Set<String> keys = buddyListTreePanelStore.keySet();
        Iterator<String> itr = keys.iterator();
        IBuddyListPanel panel;
        while (itr.hasNext()){
            panel = buddyListTreePanelStore.get(itr.next());
            if (BuddyStatus.Online.equals(panel.getMasterLoginUser().getBuddyStatus())){
                pbcBuddyListSettings.add(panel.constructPbcBuddyListSettings());
            }
        }//while
        return pbcBuddyListSettings;
    }

    synchronized ArrayList<IBuddyListPanel> getBuddyListTreePanels(GatewayServerType serverType) {
        ArrayList<IBuddyListPanel> panels = new ArrayList<IBuddyListPanel>();
        if (serverType != null){
            Set<String> keys = buddyListTreePanelStore.keySet();
            Iterator<String> itr = keys.iterator();
            IBuddyListPanel panel;
            while (itr.hasNext()){
                panel = buddyListTreePanelStore.get(itr.next());
                if (panel.getMasterLoginUser().getIMServerType().equals(serverType)){
                    panels.add(panel);
                }
            }//while
        }
        return panels;
    }

    public synchronized ArrayList<IBuddyListPanel> getAllBuddyListTreePanels() {
        ArrayList<IBuddyListPanel> panels = new ArrayList<IBuddyListPanel>();
        Set<String> keys = buddyListTreePanelStore.keySet();
        Iterator<String> itr = keys.iterator();
        IBuddyListPanel panel;
        while (itr.hasNext()){
            panel = buddyListTreePanelStore.get(itr.next());
            panels.add(panel);
        }//while
        return panels;
    }

    synchronized IBuddyListPanel getBuddyListTreePanel(IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            return null;
        }
        return getBuddyListTreePanel(loginUser.getIMUniqueName());
    }

    private synchronized IBuddyListPanel getBuddyListTreePanel(String loginUserUniqueName) {
        if ((loginUserUniqueName == null) || (loginUserUniqueName.isEmpty())){
            return null;
        }
        return buddyListTreePanelStore.get(loginUserUniqueName);
    }

    /**
     * if there is a panel whose key is the same as the pass-in buddyListTreePanel, 
     * the existing one will be returned. Otherwise, the pass-in buddyListTreePanel 
     * will be returned.
     * @param buddyListTreePanel
     * @return 
     */
    synchronized IBuddyListPanel insertPanel(IBuddyListPanel buddyListTreePanel) {
        if (buddyListTreePanel == null){
            return null;
        }
        IGatewayConnectorBuddy loginUser = buddyListTreePanel.getMasterLoginUser();
        if (loginUser == null){
            return null;
        }
        IBuddyListPanel existingPanel = this.getBuddyListTreePanel(loginUser);
        if (existingPanel == null){
            buddyListTreePanelStore.put(loginUser.getIMUniqueName(), buddyListTreePanel);
            return buddyListTreePanel;
        }else{
            return existingPanel;
        }
    }

    synchronized void removePanel(IBuddyListPanel buddyListTreePanel) {
        if (buddyListTreePanel == null){
            return;
        }
        IGatewayConnectorBuddy loginUser = buddyListTreePanel.getMasterLoginUser();
        if (loginUser == null){
            return;
        }
        buddyListTreePanelStore.remove(loginUser.getIMUniqueName());
    }
}
