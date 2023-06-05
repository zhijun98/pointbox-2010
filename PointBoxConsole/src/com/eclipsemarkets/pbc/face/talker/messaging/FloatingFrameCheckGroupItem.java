/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;

/**
 *
 * @author Zhijun Zhang, date & time: Dec 22, 2013 - 4:57:50 PM
 */
class FloatingFrameCheckGroupItem extends FloatingFrameCheckAbstractItem{

    private IGatewayConnectorGroup group;
    private GroupButtonTabComponent tab;

    private FloatingFrameCheckGroupItem(IGatewayConnectorGroup group, GroupButtonTabComponent tab) {
        this.group = group;
        this.tab = tab;
    }

    public static FloatingFrameCheckGroupItem createNewInstance(IGatewayConnectorGroup group, GroupButtonTabComponent tab){
        return new FloatingFrameCheckGroupItem(group, tab);
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

    public GroupButtonTabComponent getGroupButtonTab() {
        return tab;
    }

    public void setGroupButtonTab(GroupButtonTabComponent tab) {
        this.tab = tab;
    }
}
