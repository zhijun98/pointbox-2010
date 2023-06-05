/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Zhijun Zhang
 */
public class GroupButtonTabComponent extends ButtonTabComponent {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(GroupButtonTabComponent.class.getName());
    }
    
    private IGatewayConnectorBuddy loginUser;
    private IGatewayConnectorGroup group;
    private ArrayList<IGatewayConnectorBuddy> members;
    
    GroupButtonTabComponent(IMasterMessagingBoard board,
                            IGatewayConnectorBuddy loginUser, 
                            IGatewayConnectorGroup group, 
                            ArrayList<IGatewayConnectorBuddy> members,
                            ImageIcon openedGroupIcon)
    {
        super(board,
              group.getGroupName(),
              openedGroupIcon,
              group.getIMUniqueName(),
              group.getServerType(),
              null);
        this.loginUser = loginUser;
        this.group = group;
        this.members = members;
    }

    @Override
    public IButtonTabComponent cloneTabButton(IMasterMessagingBoard board) {
        return new GroupButtonTabComponent(board, loginUser, group, members, getIcon());
    }

    @Override
    public IGatewayConnectorBuddy getBuddy() {
        return null;
    }

    @Override
    public IGatewayConnectorGroup getGroup() {
        return group;
    }

    public IGatewayConnectorBuddy getLoginUser() {
        return loginUser;
    }

    public ArrayList<IGatewayConnectorBuddy> getMembers() {
        return members;
    }

    void updateGroupName(String newGroupName) {
        if (DataGlobal.isEmptyNullString(newGroupName)){
            return;
        }
        group.setGroupName(newGroupName);
        updateTabTextLabelValue(newGroupName);
    }
}
