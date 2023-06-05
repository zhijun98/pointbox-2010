/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import javax.swing.Icon;

/**
 * EmsCheckNodeFactory.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 29, 2010, 1:10:28 PM
 */
class EmsCheckNodeFactory {
    /**
     *
     * @param buddy who belongs to loginUser's buddy list
     * @param loginUser whose buddy list containing the buddy
     * @param icon
     * @return
     */
    public static EmsCheckNode createBuddyCheckNodeInstance(IGatewayConnectorBuddy buddy, Icon icon){
        return new BuddyCheckNode(buddy, icon);
    }

    /**
     * 
     * @param group which could contains buddies with different server types
     * @param allowsChildren
     * @param icon
     * @return
     */
    public static EmsCheckNode createGroupCheckNodeInstance(IGatewayConnectorGroup group, Icon icon){
        return new GroupCheckNode(group, icon);
    }

    /**
     * This is used to create a root node for a specific tree. The root node is empty, which has String-associated object
     * @param rootName
     * @return
     */
    public static EmsCheckNode createRootInstance(String rootName){
        return new RootCheckNode(rootName);
    }

    static EmsCheckNode createRootInstance(String rootName, Icon rootIcon) {
        return new RootCheckNode(rootName, rootIcon);
    }

    private EmsCheckNodeFactory() {
    }

}
