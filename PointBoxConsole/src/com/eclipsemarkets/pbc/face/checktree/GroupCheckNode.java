/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import java.util.Enumeration;
import javax.swing.Icon;

/**
 * GroupCheckNode
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Oct 15, 2010, 11:11:43 AM
 */
class GroupCheckNode extends EmsCheckNode implements IGroupCheckNode{
    private static final long serialVersionUID = 1L;

    private IGatewayConnectorGroup group;
    
    GroupCheckNode(IGatewayConnectorGroup group, Icon icon) {
        super(group.getGroupName(), true, icon);
        this.group = group;
        setAssociatedObject(group);
    }

    @Override
    public IGatewayConnectorGroup getGroup() {
        return group;
    }

    /**
     * Selects or deselects node.
     *
     * @param isSelected true if the node should be selected, false otherwise.
     */
    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if (children != null) {
            Enumeration nodeEnum = children.elements();
            while (nodeEnum.hasMoreElements()) {
                EmsCheckNode node = (EmsCheckNode) nodeEnum.nextElement();
                node.setSelected(isSelected);
            }
        }
    }
}
