/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings.record;

import com.eclipsemarkets.runtime.PointBoxSettings;
import com.eclipsemarkets.runtime.PointBoxClientRuntimeFactory;
import com.eclipsemarkets.runtime.IPointBoxPricerConfig;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PbconsolePricerRecord.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 10, 2010, 10:52:00 PM
 */
class PbconsolePricerRecord extends PointBoxSettings implements IPbconsolePricerRecord{

    private CopyOnWriteArrayList<String> powerUserUniqueNames;
    private IPointBoxPricerConfig pointBoxPricerRecord;

    PbconsolePricerRecord(String ownerUniqueName) {
        super(ownerUniqueName);
        powerUserUniqueNames = new CopyOnWriteArrayList<String>();
        pointBoxPricerRecord = PointBoxClientRuntimeFactory.createEmptyPointBoxPricerRecordInstance(ownerUniqueName);
    }

    public synchronized boolean isFiveYearLimit() {
        return pointBoxPricerRecord.isFiveYearLimit();
    }

    public synchronized void setFiveYearLimit(boolean fiveYearLimit) {
        pointBoxPricerRecord.setFiveYearLimit(fiveYearLimit);
    }

    public synchronized ArrayList<String> getPowerUserUniqueNames() {
        ArrayList<String> buddyUniqueNames = new ArrayList<String>();
        for (String name: powerUserUniqueNames){
            buddyUniqueNames.add(name);
        }
        return buddyUniqueNames;
    }

    public synchronized void setPowerUserUniqueNames(ArrayList<String> buddyUniqueNames) {
        powerUserUniqueNames.clear();
        for (String name: buddyUniqueNames){
            powerUserUniqueNames.add(name);
        }
    }

    public int getTValueAtExp() {
        return pointBoxPricerRecord.getTValueAtExp();
    }

    public void setTValueAtExp(int tValueAtExp) {
        pointBoxPricerRecord.setTValueAtExp(tValueAtExp);
    }
}
