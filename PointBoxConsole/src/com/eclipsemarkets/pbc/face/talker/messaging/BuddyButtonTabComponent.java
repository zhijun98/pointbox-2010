/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.talker.IBuddyButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Zhijun Zhang
 */
public class BuddyButtonTabComponent extends ButtonTabComponent implements IBuddyButtonTabComponent{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(BuddyButtonTabComponent.class.getName());
    }
    
    private String buddyTabTitle;
    private TargetBuddyPair pair;
    private IMasterMessagingBoard masterBoard;

    BuddyButtonTabComponent(IMasterMessagingBoard board,
                            String buddyTabTitle,
                            TargetBuddyPair pair,
                            ImageIcon connectorBuddyIcon, 
                            IMasterMessagingBoard masterboard) 
    {
        super(board, buddyTabTitle,
              connectorBuddyIcon,
              pair.getUniqueID(),
              pair.getBuddy().getIMServerType(),
              masterboard);
        
        this.masterBoard = masterboard;
        this.pair = pair;
        this.buddyTabTitle = buddyTabTitle;
    }

    @Override
    public IButtonTabComponent cloneTabButton(IMasterMessagingBoard board) {
        return new BuddyButtonTabComponent(board, buddyTabTitle, new TargetBuddyPair(pair), super.getIcon(),this.masterBoard);
    }

    public IGatewayConnectorBuddy getLoginUser() {
        if (pair == null){
            return null;
        }
        return pair.getLoginUser();
    }

    @Override
    public IGatewayConnectorBuddy getBuddy() {
        if (pair == null){
            return null;
        }
        return pair.getBuddy();
    }

    @Override
    public IGatewayConnectorGroup getGroup() {
        return null;
    }

    @Override
    public void updateBuddyTabName(final String buddyTabName) {
        updateTabTextLabelValue(buddyTabName);
    }
}
