/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings.record;

import com.eclipsemarkets.runtime.IExpirationSettlementDates;
import com.eclipsemarkets.runtime.IPointBoxAutoPricerConfig;
import com.eclipsemarkets.runtime.IFileInfoRecord;
import com.eclipsemarkets.runtime.PointBoxClientRuntimeFactory;


/**
 * PbconsoleRecordFactory.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 22, 2010, 7:42:29 AM
 */
public class PbconsoleRecordFactory {

    private static IExpirationSettlementDates expirationSettlementDates;
    static{
        expirationSettlementDates = null;
    }

//    public static IPointBoxForwardCurve createPowerForwardCurveRecordInstance(String ownerUniqueName, String fcPath)
//    {
//        return PointBoxClientRuntimeFactory.createPowerForwardCurveInstance(ownerUniqueName, fcPath);
//    }
//
//    public static IPointBoxForwardCurve createNaturalGasForwardCurveRecordInstance(String ownerUniqueName, String fcPath)
//    {
//        return PointBoxClientRuntimeFactory.createNaturalGasForwardCurveInstance(ownerUniqueName, fcPath);
//    }
//
//    public static IPointBoxForwardCurve createCrudeOilForwardCurveRecordInstance(String ownerUniqueName, String fcPath)
//    {
//        return PointBoxClientRuntimeFactory.createCrudeOilForwardCurveInstance(ownerUniqueName, fcPath);
//    }

    /**
     * Currently this information is shared by NG, CO, PR etc.
     * @param pbcRuntime
     * @param filePath
     * @return 
     */
//    public static IExpirationSettlementDates getExpirationSettlementDatesSingleton(String filePath) {
//        if (expirationSettlementDates == null){
//            expirationSettlementDates = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//        }
//        return expirationSettlementDates;
//    }

    public static IBuddyRecord createBuddyRecordInstance(String ownerUniqueName){
        return new BuddyRecord(ownerUniqueName);
    }

    public static IGroupRecord createGroupRecordInstance(String ownerUniqueName){
        return new GroupRecord(ownerUniqueName);
    }

    public static IGroupMembersRecord createGroupMembersRecordInstance(String ownerUniqueName, IGroupRecord groupRecord){
        return new GroupMembersRecord(ownerUniqueName, groupRecord);
    }

    public static IFileInfoRecord createFileInfoRecordInstance(String ownerUniqueName, String fileUniqueName, String filePath){
        return PointBoxClientRuntimeFactory.createFileInfoRecordInstance(ownerUniqueName, fileUniqueName, filePath);
    }

    public static IMessageTabRecord createMessageTabRecordInstance(String ownerUniqueName,
                                                                   String messageTabId) {
        return new MessageTabRecord(ownerUniqueName, messageTabId);
    }

    public static IPointBoxAutoPricerConfig createAutoPricerRecordInstance(String ownerUniqueName) {
        return PointBoxClientRuntimeFactory.createAutoPricerRecordInstance(ownerUniqueName);
    }

    public static IPbconsolePricerRecord createPricerRecordInstance(String ownerUniqueName) {
        return new PbconsolePricerRecord(ownerUniqueName);
    }

    public static IBuddyProfileRecord ceateBuddyProfileRecordInstance(){
        return new BuddyProfileRecord();
    }

    private PbconsoleRecordFactory() {
    }
}
