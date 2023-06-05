/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.IPointBoxTalkerSettings;
import com.eclipsemarkets.pbc.runtime.settings.PbcSettingsType;
import com.eclipsemarkets.pbc.runtime.settings.record.IMessageTabRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.PbconsoleRecordFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * PbconsoleTalkerSettings.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Aug 27, 2010, 9:09:39 PM
 */
class PointBoxTalkerSettings extends PbconsoleSettings implements IPointBoxTalkerSettings{

    private HashMap<String, IMessageTabRecord> messageTabRecords;

    PointBoxTalkerSettings(IPbcRuntime runtime) {
        super(runtime);
        messageTabRecords = new HashMap<String, IMessageTabRecord>();
    }

    public synchronized HashMap<String, IMessageTabRecord> getMessageTabRecords() {
        HashMap<String, IMessageTabRecord> records = new HashMap<String, IMessageTabRecord>();
        Set<String> keys = messageTabRecords.keySet();
        Iterator<String> itr = keys.iterator();
        String key;
        while(itr.hasNext()){
            key = itr.next();
            records.put(key, messageTabRecords.get(key));
        }
        return records;
    }

    /**
     * if messageTabID is unknown, it should offer a default record
     * @param messageTabID
     * @return 
     */
    public synchronized IMessageTabRecord getMessageTabRecord(String messageTabID) {
        if (messageTabID == null){
            messageTabID = "";
        }
        messageTabID = messageTabID.trim();
        if (!messageTabRecords.containsKey(messageTabID)){
            messageTabRecords.put(messageTabID, PbconsoleRecordFactory.createMessageTabRecordInstance("", messageTabID));
        }
        return messageTabRecords.get(messageTabID);
    }

    public synchronized PbcSettingsType getPbcSettingsType() {
        return PbcSettingsType.PointBoxTalkerSettings;
    }

    public synchronized void loadPersonalSettings() {
        runtime.getKernel().loadPointBoxTalkerSettings(this);
    }

    public synchronized void storePersonalSettings() {
        runtime.getKernel().storePointBoxTalkerSettings(this);
    }
//
//    @Override
//    void loadImpl() {
//        fireLoadingStatusUpdated(new EmsEntityLifeCycleEvent(this, "Load PointBox talker settings ..."));
//    }
//
//    @Override
//    ISettingsEvent personalizeSettingsImpl(IGatewayConnectorBuddy pointBoxLoginUser) {
//        if (pointBoxLoginUser != null){
//            emsStorage.loadPointBoxTalkerSettings(pointBoxLoginUser, this);
//        }
//        return new SettingsEvent(this);
//    }
//
//    @Override
//    void saveSettingsImpl() {
//        IGatewayConnectorBuddy masterLoginUser = EmsRuntimeFactory.getPbconsoleRuntimeSingleton(emsStorage).getCurrentLogoutMasterUser();
//        emsStorage.storePointBoxTalkerSettings(masterLoginUser, this);
//    }
//
//    @Override
//    void unloadImpl() {
//        fireLoadingStatusUpdated(new EmsEntityLifeCycleEvent(this, "Unload PointBox talker settings ..."));
//    }
//
//    public EmsSettingsType getEmsSettingsType() {
//        return EmsSettingsType.PbconsoleTalkerSettings;
//    }
//
//    public EmsEntityTypeWrapper getEmsEntityTypeWrapper() {
//        return EmsEntityTypeWrapper.EmsSettingsType_PbconsoleTalkerSettings;
//    }

}
