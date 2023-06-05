/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker.model;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * BuddyTreeNode.java
 * <p>
 * @author Zhijun Zhang
 * Created on May 16, 2010, 12:35:40 AM
 */
class BuddyTreeNode extends DefaultMutableTreeNode implements IBuddyTreeNode{
    private static final long serialVersionUID = 1L;

    BuddyTreeNode(IGatewayConnectorBuddy connectorBuddy) {
        super(connectorBuddy);
        if (connectorBuddy == null){
            throw new RuntimeException("[TECH] connectorBuddy for BuddyTreeNode cannot be NULL.");
        }
    }

    public TreeNode[] getBuddyNodePath() {
        return getPath();
    }

    public IGatewayConnectorBuddy getAssociatedConnectorBuddy(){
        return (IGatewayConnectorBuddy)getUserObject();
    }

    public String getIMUniqueName(){
        return getAssociatedConnectorBuddy().getIMUniqueName();
    }

//    @Override
//    public String toString() {
//        return getAssociatedConnectorBuddy().getNickname();
//    }
}