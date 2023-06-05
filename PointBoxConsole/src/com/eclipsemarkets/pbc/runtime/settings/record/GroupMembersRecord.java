/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings.record;

import com.eclipsemarkets.runtime.PointBoxSettings;
import java.util.ArrayList;

/**
 * GroupMembersRecord.java
 * <p>
 * a simpel container of one IGroupRecord and its members, i.e., buddyRecords;
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 26, 2010, 9:17:23 AM
 */
class GroupMembersRecord extends PointBoxSettings implements IGroupMembersRecord{

    private IGroupRecord groupRecord;
    ArrayList<IBuddyRecord> buddyRecords;

    GroupMembersRecord(String ownerUniqueName, IGroupRecord groupRecord) {
        super(ownerUniqueName);
        this.groupRecord = groupRecord;
        buddyRecords = new ArrayList<IBuddyRecord>();
    }

    GroupMembersRecord(String ownerUniqueName, IGroupRecord groupRecord, ArrayList<IBuddyRecord> buddyRecords) {
        super(ownerUniqueName);
        this.groupRecord = groupRecord;
        this.buddyRecords = buddyRecords;
    }

    public synchronized IGroupRecord getGroupRecord() {
        return groupRecord;
    }

    public synchronized ArrayList<IBuddyRecord> getMemberRecords() {
        return buddyRecords;
    }

    public synchronized void setBuddyRecords(ArrayList<IBuddyRecord> buddyRecords) {
        this.buddyRecords = buddyRecords;
    }

    public synchronized void setGroupRecord(IGroupRecord groupRecord) {
        this.groupRecord = groupRecord;
    }

}
