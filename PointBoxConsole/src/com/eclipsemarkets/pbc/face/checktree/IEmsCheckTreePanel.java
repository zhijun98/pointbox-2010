/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.talker.IGroupListTreePanel;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;

/**
 *
 * @author Zhijun Zhang
 * @version 1.0.1
 */
public interface IEmsCheckTreePanel {

    public void addEmsCheckTreeListener(IEmsCheckTreeListener listerner);

    public void addCheckTreeSelectionListener(IEmsCheckTreeSelectionListener listener);

    public JPanel getBasePanel();

    public JTree getBaseTree();

    public void expandCheckTree();
    
    /**
     * This will make all the nodes, which are associated with buddy, selected
     * @param buddy 
     */
    public void selectBuddyCheckNode(IGatewayConnectorBuddy buddy);
    
    /**
     * This will make all the nodes, which are associated with buddy, unselected
     * @param buddy 
     */
    public void unselectBuddyCheckNode(IGatewayConnectorBuddy buddy);

    public IEmsCheckNode createNewBuddyCheckNode(IGatewayConnectorBuddy buddy, Icon icon);

    public IEmsCheckNode getRootCheckNode();

    public void checkAllTreeNodes();

    public void uncheckAllTreeNodes();

    public void insertGroupBuddyNodePair(IGatewayConnectorGroup group, IGatewayConnectorBuddy buddy, boolean buddySelected);

    public ArrayList<IGatewayConnectorBuddy> retrieveCheckedBuddies();

    public ArrayList<IGatewayConnectorBuddy> retrieveCheckedBuddiesForBroadcast();

    public void removeGroup(IGatewayConnectorGroup group);

    public ArrayList<IGatewayConnectorGroup> retrieveCheckedGroups();

    public ArrayList<IGatewayConnectorGroup> retrieveAllGroups();

    public ArrayList<IGatewayConnectorBuddy> retrieveBuddiesOfGroup(IGatewayConnectorGroup group);

    /**
     * 
     * @param group
     * @param members
     */
    public void ensureFirstMemberVisible(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members);

    public void addBuddyToGroup ( IEmsCheckNode groupNode, IEmsCheckNode buddyNode);
    
    public void removeBuddyFromGroup ( IEmsCheckNode groupNode, IEmsCheckNode buddyNode);
    
    public void removeBuddyFromGroup ( IGatewayConnectorBuddy buddy, IGatewayConnectorGroup group);

    public void setIGroupListTreePanel(IGroupListTreePanel iGroupListTreePanel);

    public boolean isDistGroupExisted(String distGroupName);

    public IEmsCheckNode findGroupCheckNode(IGatewayConnectorGroup group);

    public void removeAllGroups();

}
