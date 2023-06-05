/**
 * Eclipse Market Solutions LLC
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.DataGlobal;
import java.util.logging.Logger;

/**
 * TargetBuddyPair
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on May 10, 2011 at 5:20:50 PM
 */
class TargetBuddyPair {
    
    private static final Logger logger;
    private static final String delimiter;
    static{
        logger = Logger.getLogger(TargetBuddyPair.class.getName());
        delimiter = ":";
    }

    private String uniqueID;
    private IGatewayConnectorBuddy loginUser;
    private IGatewayConnectorBuddy buddy;

    TargetBuddyPair(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy) {
        this.loginUser = loginUser;
        this.buddy = buddy;
        uniqueID = generateTargetBuddyPairStorageID(loginUser, buddy);
    }

    TargetBuddyPair(TargetBuddyPair pair) {
        if (pair == null){
            this.loginUser = null;
            this.buddy = null;
            uniqueID = null;
        }else{
            this.loginUser = pair.getLoginUser();
            this.buddy = pair.getBuddy();
            uniqueID = generateTargetBuddyPairStorageID(loginUser, buddy);
        }
    }

    String getUniqueID(){
        return uniqueID;
    }

    IGatewayConnectorBuddy getBuddy() {
        return buddy;
    }

    IGatewayConnectorBuddy getLoginUser() {
        return loginUser;
    }

    /**
     * @deprecated 
     * @param tabUniqueID
     * @param buddy
     * @return 
     */
    static boolean isTargetBuddyPairStorageIdEndedWithBuddy(String tabUniqueID, IGatewayConnectorBuddy buddy) {
        if ((buddy == null) || (DataGlobal.isEmptyNullString(tabUniqueID))){
            return false;
        }
        return tabUniqueID.endsWith(delimiter + buddy.getIMUniqueName());
    }
    
    /**
     * if any one of parameters is NULL, it will return null
     * @param loginUser
     * @param buddy
     * @return 
     */
    static String generateTargetBuddyPairStorageID(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy){
        if ((loginUser == null) || (buddy == null)){
            return null;
        }else{
            return loginUser.getIMUniqueName() + delimiter + buddy.getIMUniqueName();
        }
    }
    
    static IGatewayConnectorBuddy parseBuddyOfTab(IGatewayConnectorBuddy loginUser, String tabUniqueID){
        if ((loginUser == null) || (DataGlobal.isEmptyNullString(tabUniqueID)) || (!tabUniqueID.contains(delimiter))){
            return null;
        }
        String[] data = tabUniqueID.split(delimiter);
        if ((data == null) || (data.length != 2)){
            return null;
        }else{
            return GatewayBuddyListFactory.convertToBuddyInstance(loginUser, data[1]);
        }
    }
    
    static IGatewayConnectorBuddy parseLoginUserForBuddyOfTab(String tabUniqueID){
        if ((DataGlobal.isEmptyNullString(tabUniqueID)) || (!tabUniqueID.contains(delimiter))){
            return null;
        }
        String[] data = tabUniqueID.split(delimiter);
        if ((data == null) || (data.length != 2)){
            return null;
        }else{
            return GatewayBuddyListFactory.convertToLoginUserInstance(data[0]);
        }
    }
}//TargetBuddyPair
