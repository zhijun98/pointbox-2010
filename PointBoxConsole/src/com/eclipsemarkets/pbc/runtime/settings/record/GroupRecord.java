/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings.record;

import com.eclipsemarkets.runtime.PointBoxSettings;
import com.eclipsemarkets.storage.constant.GatewayServerType;

/**
 * GroupRecord.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 28, 2010, 10:59:03 AM
 */
class GroupRecord extends PointBoxSettings implements IGroupRecord {

    private String groupUniqueName;
    private GatewayServerType serverType;
    private String groupDescription;
    private String groupName;

    GroupRecord(String ownerUniqueName) {
        super(ownerUniqueName);
    }

    public synchronized String getGroupDescription() {
        return groupDescription;
    }

    public synchronized void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public synchronized String getGroupName() {
        return groupName;
    }

    public synchronized void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public synchronized String getGroupUniqueName() {
        return groupUniqueName;
    }

    public synchronized void setGroupUniqueName(String groupUniqueName) {
        this.groupUniqueName = groupUniqueName;
    }

    public synchronized GatewayServerType getServerType() {
        return serverType;
    }

    public synchronized void setServerType(GatewayServerType serverType) {
        this.serverType = serverType;
    }

}
