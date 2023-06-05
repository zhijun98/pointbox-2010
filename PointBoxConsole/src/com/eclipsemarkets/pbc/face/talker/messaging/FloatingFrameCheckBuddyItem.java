/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;

/**
 *
 * @author Zhijun Zhang, date & time: Jul 27, 2013 - 9:21:07 PM
 */
class FloatingFrameCheckBuddyItem extends FloatingFrameCheckAbstractItem{

    private IGatewayConnectorBuddy buddy;
    private BuddyButtonTabComponent tab;

    private FloatingFrameCheckBuddyItem(IGatewayConnectorBuddy member, BuddyButtonTabComponent tab)
    {
        this.buddy = member;
        this.tab=tab;
    }
    
    public static FloatingFrameCheckBuddyItem createNewInstance(IGatewayConnectorBuddy member, BuddyButtonTabComponent tab){
        return new FloatingFrameCheckBuddyItem(member, tab);
    }

   @Override
    public String toString()
    {
        return getBuddy().getIMScreenName();
    }
   
    public IGatewayConnectorBuddy getBuddy() {
        return buddy;
    }

    public void setBuddy(IGatewayConnectorBuddy buddy) {
        this.buddy = buddy;
    }

    public BuddyButtonTabComponent getBuddyButtonTab() {
        return tab;
    }
}
