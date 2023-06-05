/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.StringIgnoreCaseComparator;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

/**
 * EmsSortedCheckTreePanel.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 29, 2010, 1:08:15 PM
 */
class EmsSortedCheckTreePanel extends EmsCheckTreePanel implements IEmsSortedCheckTreePanel{
    private static final long serialVersionUID = 1L;

    EmsSortedCheckTreePanel(String rootName, IPbcKernel kernel) {
        super(rootName, kernel);
    }
    
    EmsSortedCheckTreePanel(String rootName, Icon rootIcon, IPbcKernel kernel) {
        super(rootName, rootIcon, kernel);
    }

    @Override
    public synchronized void insertGroupBuddyNodePair(IGatewayConnectorGroup group, IGatewayConnectorBuddy buddy, boolean buddySelected) {
        DefaultTreeModel model = ((DefaultTreeModel)baseTree.getModel());


        IEmsCheckNode rootCheckNode = getRootCheckNode();
        IEmsCheckNode groupNode = rootCheckNode.retrieveChildrenNode(group);
        if (groupNode == null){
            groupNode = EmsCheckNodeFactory.createGroupCheckNodeInstance(group, getImageIcon(GatewayServerType.PBIM_SERVER_TYPE));
            model.insertNodeInto((DefaultMutableTreeNode)groupNode, (MutableTreeNode)rootCheckNode,
                                 calculateSortedNodeIndex((DefaultMutableTreeNode)rootCheckNode, groupNode.getFullName()));
            fireGroupCheckNodeInsertedEvent(groupNode);
        }
        IEmsCheckNode buddyNode = groupNode.retrieveChildrenNode(buddy);
        if (buddyNode == null){
            buddyNode = EmsCheckNodeFactory.createBuddyCheckNodeInstance(buddy, getImageIcon(buddy.getIMServerType()));
            buddyNode.setAssociatedObject(buddy);

            model.insertNodeInto((DefaultMutableTreeNode)buddyNode, 
                                 (DefaultMutableTreeNode)groupNode,
                                 calculateSortedNodeIndex((DefaultMutableTreeNode)groupNode, buddyNode.getFullName()));
            fireBuddyCheckNodeInsertedEvent(buddyNode);
        }
        if (buddySelected){
            if (!groupNode.isSelected()){
                groupNode.setSelected(true);
                //fire event???
            }
        }
        buddyNode.setSelected(buddySelected);
        expandCheckTree();
    }

    private synchronized int calculateSortedNodeIndex(DefaultMutableTreeNode parent, String newMemberName) {
        ArrayList<String> memberNames = new ArrayList<String>();
        memberNames.add(newMemberName);
        String memberName = "";
        for (int i = 0; i < parent.getChildCount(); i++){
            memberName = parent.getChildAt(i).toString();
            memberNames.add(memberName);
        }
        Collections.sort(memberNames, new StringIgnoreCaseComparator());
        for (int i = 0; i < memberNames.size(); i++){
            if (memberNames.get(i).equalsIgnoreCase(newMemberName)){
                return i;
            }
        }
        return -1;
    }
}
