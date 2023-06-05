/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker.model;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.IOfflineBuddyGroup;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.TreeModel;

/**
 * IBuddyListDataModel.java
 * <p>
 * @author Zhijun Zhang
 * Created on May 16, 2010, 12:31:56 AM
 */
public interface IBuddyListDataModel extends TreeModel{

    public void addBuddyListDataModelListener(IBuddyListDataModelListener listener);

    public void removeBuddyListDataModelListener(IBuddyListDataModelListener listener);

    public IBuddyTreeNode getBuddyNodeFromDataModel(String buddyUniqueName);

    /**
     * a special offline-group here
     * @return
     */
    public IOfflineBuddyGroup getOfflineGroup();

    /**
     * sort buddy list
     * @deprecated
     */
    public void sortingBuddyList();

    /**
     * if newGroup has existed, it does nothing
     * 
     * @param loginUser
     * @param newGroup
     */
    public void addNewGroupIntoDataModel(IGatewayConnectorBuddy loginUser, IGatewayConnectorGroup newGroup);

    /**
     * if groupUniqueName does not exist or aBuddy has been added, it does nothing
     * 
     * @param loginUser
     * @param aBuddy
     * @param groupUniqueName
     */
    public void addNewBuddyIntoDataModel(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy aBuddy, String groupUniqueName);

    /**
     * Get a buddy instance from data model based on buddyUniqueName
     * @param buddyUniqueName
     * @return
     */
    public IGatewayConnectorBuddy getBuddyFromDataModel(String buddyUniqueName);

    /**
     * Get a buddy instance only from online groups in data model based on buddyUniqueName. This method does
     * not look for it in the offline group.
     * @param buddyUniqueName
     * @return
     */
    public IGatewayConnectorBuddy getBuddyInOnlineGroupsFromDataModel(String buddyUniqueName);

    /**
     * Get a buddy instance only from the offline group in data model based on buddyUniqueName. This method does
     * not look for it in the online groups.
     * @param buddyUniqueName
     * @return
     */
    public IGatewayConnectorBuddy getBuddyInOfflineGroupFromDataModel(String buddyUniqueName);

    /**
     * Get all the groups except "offline group" from the data model
     * @return
     */
    public ArrayList<IGatewayConnectorGroup> getAllOnlineGroupsFromDataStroage();
    /**
     * Move a buddy from oldIndex to newIndex only in its own group on the list model.
     *
     * @see ReorderableJList
     * @param oldIndex
     * @param newIndex
     * @param aBuddy
     * @return
     */
    public boolean changeBuddyPosition(int oldIndex,
                                     int newIndex,
                                     IGatewayConnectorBuddy aBuddy);

    ///////////////////////////////////////

    public boolean isHiddenGroupFromListModel(IGatewayConnectorGroup group);

    public int removeGroupMembersFromListModel(String iMUniqueName);

    public int restoreGroupMembersIntoListModel(String iMUniqueName);

    public int getVisibleItemIndexFromListModel(Object item);

    public int getSizeOfDataStorage();

    public ArrayList<IGatewayConnectorBuddy> getAllBuddiesFromDataModel();

    public ArrayList<Object> getAllBuddiesAndGroupsFromDataModel();

    public ArrayList<String> getGroupUniqueNameListFromDataStorage();

    public ArrayList<String> getOnlineGroupUniqueNameList();

    //public IGatewayConnectorGroup getGroupOfBuddy(IGatewayConnectorBuddy aBuddy);

    public IGatewayConnectorGroup getGroupFromDataModel(String uniqueGroupName);

    public ArrayList<IGatewayConnectorBuddy> getAllOnlineBuddiesFromDataModel();

    public HashMap<IGatewayConnectorGroup, ArrayList<IGatewayConnectorBuddy>> retirveAllGroupsWithMembers();

    public ArrayList<IGatewayConnectorGroup> getAllGroupsFromDataStroage();

    public ArrayList<IGatewayConnectorBuddy> getBuddiesOfGroupFromDataStorage(String uniqueGroupName, boolean onlineRequired);

    public void deleteBuddyFromDataModel(String newBuddyUniqueName);

    public void emptyDataModel();

    public void moveBuddyFromGroupToGroupInDataModel(String buddyUniqueName,
                                                     String oldGroupUniqueName,
                                                     String newGroupUniqueName);

    public ArrayList<String> getGroupUniqueNameListWithoutOfflineGroupFromDataStorage();

    public void buddyContentChanged (String iMUniqueName);

    public PbcBuddyListSettings retrievePbcBuddyListSettings();

}
