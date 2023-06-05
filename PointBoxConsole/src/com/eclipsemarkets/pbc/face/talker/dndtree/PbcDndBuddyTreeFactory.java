/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.face.talker.IBuddyListPanel;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
public class PbcDndBuddyTreeFactory {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcDndBuddyTreeFactory.class.getName());
    }

    /**
     * 
     * @param ownerBuddyListPanel
     * @param rootName - PbcBuddyListSettings values which is used as PbCentralDistList name in the central database
     * @param displayOfflineBuddies
     * @return 
     */
    public static IPbcDndBuddyTree createHybridDndBuddyTreePanel(IBuddyListPanel ownerBuddyListPanel,
                                                                  String rootName,
                                                                  boolean displayOfflineBuddies) 
    {
        //return new PbcDndBuddyTree(kernel, new HybridBuddyListRenderer(kernel), new DnDMutableHybridTreeRoot(rootName), displayOfflineBuddies);
        return new PbcDndBuddyTree(ownerBuddyListPanel, new RegularBuddyListRenderer(ownerBuddyListPanel.getTalker().getKernel()), 
                                                                                     new DnDMutableHybridTreeRoot(rootName), 
                                                                                     displayOfflineBuddies, null);
    }

    /**
     * @param ownerBuddyListPanel
     * @param loginUser - owner of the current buddy-list panel
     * @param displayOfflineBuddies
     * @return 
     */
    public static IPbcDndBuddyTree createRegularDndBuddyTreePanel(IBuddyListPanel ownerBuddyListPanel,
                                                                  IGatewayConnectorBuddy loginUser,
                                                                  boolean displayOfflineBuddies) 
    {
        return new PbcDndBuddyTree(ownerBuddyListPanel, 
                                   new RegularBuddyListRenderer(ownerBuddyListPanel.getTalker().getKernel()), 
                                                                new DnDMutableTreeNode(loginUser.getIMServerType().toString()), 
                                                                displayOfflineBuddies, 
                                                                loginUser);
    }

    private PbcDndBuddyTreeFactory() {
    }
}
