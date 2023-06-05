/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Zhijun Zhang
 */
class DnDBuddyTreeNode extends DnDMutableTreeNode implements IDnDBuddyTreeNode{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(DnDBuddyTreeNode.class.getName());
    }

    private DnDGroupTreeNode groupNode;
    private IGatewayConnectorBuddy buddy;
    
    DnDBuddyTreeNode(DnDGroupTreeNode groupNode, IGatewayConnectorBuddy buddy) {
        setUserObject(buddy);
        this.groupNode = groupNode;
        this.buddy = buddy;
    }

    @Override
    public IGatewayConnectorBuddy getGatewayConnectorBuddy() {
        return buddy;
    }

    @Override
    public String getTreeNodeId() {
        return generateTreeNodeId(groupNode, buddy);
    }

    @Override
    public TreeNode[] getNodePath() {
        return getPath();
    }
    
    /**
     * For a NON-regular buddy list, "buddy" can belong to different group nodes 
     * at the same time. This is different from IM-buddy instances which can belong 
     * to only one group
     * @param groupNode
     * @param buddy
     * @return 
     */
    static String generateTreeNodeId(DnDGroupTreeNode groupNode, IGatewayConnectorBuddy buddy){
        String id = "";
        try{
            id = groupNode.getTreeNodeId() + buddy.getIMServerType() + buddy.getLoginOwner().getIMScreenName() + buddy.getIMScreenName();
        }catch (Exception ex){
            logger.log(Level.SEVERE, null, ex);
        }
        return id;
    }
}
