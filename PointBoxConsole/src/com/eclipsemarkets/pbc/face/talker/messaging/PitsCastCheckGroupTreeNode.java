/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.talker.dndtree.DnDMutableTreeNode;

/**
 *
 * @author Zhijun Zhang, date & time: Dec 22, 2013 - 11:21:46 PM
 */
public class PitsCastCheckGroupTreeNode extends DnDMutableTreeNode{
    
    private boolean isSelected = true; //default value is true

    private IGatewayConnectorGroup group;

    public PitsCastCheckGroupTreeNode(IGatewayConnectorGroup group) {
        this.group = group;
    }

    @Override
     public String toString()
     {
         return getGroup().getGroupName();
     }

     public IGatewayConnectorGroup getGroup() {
         return group;
     }

     public void setGroup(IGatewayConnectorGroup group) {
         this.group = group;
     }

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }

}
