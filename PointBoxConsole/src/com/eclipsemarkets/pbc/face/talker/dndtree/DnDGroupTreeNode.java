/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import java.util.logging.Logger;
import javax.swing.tree.TreeNode;

/**
 * Assumption: group's name is unique in the system-wide range and it 
 * is used as a distribution tree node ID.
 *
 * @author Zhijun Zhang
 */
class DnDGroupTreeNode extends DnDMutableTreeNode implements IDnDGroupTreeNode {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(DnDGroupTreeNode.class.getName());
    }

    private IGatewayConnectorGroup gatewayConnectorGroup;
    private boolean boxChecked;
    
    DnDGroupTreeNode(IGatewayConnectorGroup distGroup) {
        setUserObject(distGroup);
        this.gatewayConnectorGroup = distGroup;
        boxChecked = false;
    }

    @Override
    public boolean isBoxChecked() {
        return boxChecked;
    }

    @Override
    public void setBoxChecked(boolean boxChecked) {
        this.boxChecked = boxChecked;
    }

    @Override
    public String getTreeNodeId() {
        return DataGlobal.denullize(gatewayConnectorGroup.getGroupName());
    }

    @Override
    public TreeNode[] getNodePath() {
        return getPath();
    }

    @Override
    public IGatewayConnectorGroup getGatewayConnectorGroup() {
        return gatewayConnectorGroup;
    }
}