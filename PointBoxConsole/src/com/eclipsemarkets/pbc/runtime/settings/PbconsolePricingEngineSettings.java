/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.runtime.IPointBoxPricingEngineSettings;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.runtime.IPointBoxAutoPricerConfig;
import com.eclipsemarkets.pbc.runtime.settings.record.IPbconsolePricerRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.PbconsoleRecordFactory;
import com.eclipsemarkets.runtime.IPointBoxPricerConfig;

/**
 * PbconsolePricingEngineSettings.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 10, 2010, 9:58:47 PM
 */
class PbconsolePricingEngineSettings extends PbconsoleSettings implements IPointBoxPricingEngineSettings{

    private IPbconsolePricerRecord pricerRecord;
    private IPointBoxAutoPricerConfig autoPricerRecord;

    PbconsolePricingEngineSettings(IPbcRuntime runtime) {
        super(runtime);
        autoPricerRecord = PbconsoleRecordFactory.createAutoPricerRecordInstance(PbcSettingsFactory.getOwnerUniqueName(null));
        pricerRecord = PbconsoleRecordFactory.createPricerRecordInstance(PbcSettingsFactory.getOwnerUniqueName(null));
    }

    public synchronized PbcSettingsType getPbcSettingsType() {
        return PbcSettingsType.PricingEngineSettings;
    }

    @Override
    public synchronized void loadPersonalSettings() {
        runtime.getKernel().loadAutoPricerRecord(autoPricerRecord);
        runtime.getKernel().loadPricerRecord(pricerRecord);
    }

    @Override
    public synchronized void storePersonalSettings() {
        runtime.getKernel().storePricerRecord(pricerRecord);
        runtime.getKernel().storeAutoPricerRecord(autoPricerRecord);
    }

    @Override
    public synchronized IPointBoxPricerConfig getPricerConfig() {
        return pricerRecord;
    }

    @Override
    public synchronized IPointBoxAutoPricerConfig getAutoPricerConfig() {
        return autoPricerRecord;
    }
}
