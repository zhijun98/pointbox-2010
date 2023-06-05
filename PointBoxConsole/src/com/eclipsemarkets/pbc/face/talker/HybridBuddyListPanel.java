/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.web.PointBoxConnectorID;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
abstract class HybridBuddyListPanel extends AbstractBuddyListPanel implements IHybridBuddyListPanel{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(HybridBuddyListPanel.class.getName());
    }

    @Override
    PointBoxConnectorID getDistListOwnerID() {
        return getKernel().getPointBoxAccountID();
    }

    public HybridBuddyListPanel(IPbcTalker talker) {
        super(talker);
    }

    /**
     * Get all the available buddies in the current PBC
     * @param sort
     * @return 
     */
    @Override
    public ArrayList<IGatewayConnectorBuddy> getAllBuddies(boolean sort) {
        return getTalker().getAllAvaialbleBuddies(sort);
    }
}
