/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import javax.swing.Icon;

/**
 * BuddyCheckNode
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Oct 15, 2010, 11:12:05 AM
 */
class BuddyCheckNode extends EmsCheckNode implements IBuddyCheckNode{
    private static final long serialVersionUID = 1L;
    
    private IGatewayConnectorBuddy buddy;
    
    BuddyCheckNode(IGatewayConnectorBuddy buddy, Icon icon) {
        super(buddy.getIMScreenName(), false, icon);
        this.buddy = buddy;
        setAssociatedObject(buddy);
    }
    
    @Override
    public IGatewayConnectorBuddy getBuddy() {
        return buddy;
    }
}
