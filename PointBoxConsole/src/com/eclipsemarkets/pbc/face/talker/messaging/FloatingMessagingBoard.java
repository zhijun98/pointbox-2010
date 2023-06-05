/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.talker.IFloatingMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IFloatingMessagingBoardListener;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class is used by PointBoxTalker01
 * @author Zhijun Zhang
 */
class FloatingMessagingBoard extends MasterMessagingBoard implements IFloatingMessagingBoard{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(FloatingMessagingBoard.class.getName());
    }

    private final ArrayList<IFloatingMessagingBoardListener> listeners;
    
    /**
     * A distribution name for the buddies on this board
     */
    private IGatewayConnectorGroup group;
    
    FloatingMessagingBoard(IPbcTalker talker) {
        super(talker);
        listeners = new ArrayList<IFloatingMessagingBoardListener>();
        
        group = null;
        //showSaveAsGroupMenuItem();  //we don't need this icon now since the requirements of traders
    }
    
    void addFloatingMessagingBoardListener(IFloatingMessagingBoardListener listener){
        synchronized(listeners){
            if (!listeners.contains(listener)){
                listeners.add(listener);
            }
        }
    }
    
    void removeFloatingMessagingBoardListener(IFloatingMessagingBoardListener listener){
        synchronized(listeners){
            listeners.remove(listener);
        }
    }
    
//    void fireNewGroupSaved(IGatewayConnectorGroup group, 
//                           ArrayList<IGatewayConnectorBuddy> members)
//    {
//        synchronized(listeners){
//            for (IFloatingMessagingBoardListener listener : listeners){
//                listener.newGroupSaved(group, members);
//            }
//        }
//    }
    
    void setGroup(IGatewayConnectorGroup group){
        this.group = group;
    }

    IGatewayConnectorGroup getGroup() {
        return group;
    }

    void updateDistributionGroupMembers(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
    }

    void renameGroupInFloatingFrame(IGatewayConnectorGroup oldGroup, String newGroupName) {
    }
}
