/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker.model;

import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.talker.model.IGroupTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * GroupTreeNode.java
 * <p>
 * @author Zhijun Zhang
 * Created on May 16, 2010, 12:34:09 AM
 */
class GroupTreeNode extends DefaultMutableTreeNode implements IGroupTreeNode{
    private static final long serialVersionUID = 1L;

    GroupTreeNode(IGatewayConnectorGroup connectorGroup) {
        super(connectorGroup);
        if (connectorGroup == null){
            throw new RuntimeException("[TECH] connectorGroup for GroupTreeNode cannot be NULL.");
        }
    }
    public IGatewayConnectorGroup getGatewayConnectorGroup(){
        return (IGatewayConnectorGroup)getUserObject();
    }

    public String getIMUniqueName(){
        return getGatewayConnectorGroup().getIMUniqueName();
    }

    @Override
    public String toString() {
        return getGatewayConnectorGroup().getGroupName();
    }
}
