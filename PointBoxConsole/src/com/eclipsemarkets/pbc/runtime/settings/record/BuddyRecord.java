/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings.record;

import com.eclipsemarkets.runtime.PointBoxSettings;
import com.eclipsemarkets.storage.constant.GatewayServerType;

/**
 * BuddyRecord.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 28, 2010, 11:03:01 AM
 */
class BuddyRecord extends PointBoxSettings implements IBuddyRecord{

    private String buddyUniqueName;
    private GatewayServerType serverType;
    private String buddyScreenName;
    private String password;
    private String nickName;
    private String profileId;

    BuddyRecord(String ownerUniqueName) {
        super(ownerUniqueName);
    }

    public synchronized String getBuddyScreenName() {
        return buddyScreenName;
    }

    public synchronized void setBuddyScreenName(String buddyScreenName) {
        this.buddyScreenName = buddyScreenName;
    }

    public synchronized String getBuddyUniqueName() {
        return buddyUniqueName;
    }

    public synchronized void setBuddyUniqueName(String buddyUniqueName) {
        this.buddyUniqueName = buddyUniqueName;
    }

    public synchronized String getNickName() {
        return nickName;
    }

    public synchronized void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized String getProfileId() {
        return profileId;
    }

    public synchronized void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public synchronized GatewayServerType getServerType() {
        return serverType;
    }

    public synchronized void setServerType(GatewayServerType serverType) {
        this.serverType = serverType;
    }

}
