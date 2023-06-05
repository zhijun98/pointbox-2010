/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.runtime.PointBoxClientRuntimeFactory;
import com.eclipsemarkets.runtime.IPointBoxPricerPolicySettings;
import com.eclipsemarkets.runtime.PointboxPricingPolicy;

/**
 *
 * @author Zhijun Zhang
 */
class PbconsolePricerContantsSettings extends PbconsoleSettings implements IPbconsolePricerContantsSettings, IPbcSettings {

    private IPointBoxPricerPolicySettings policySettings;
    
    PbconsolePricerContantsSettings(IPbcRuntime runtime) {
        super(runtime);
        policySettings = PointBoxClientRuntimeFactory.createPointBoxPricerPolicySettingsInstance();
    }

    public synchronized PbcSettingsType getPbcSettingsType() {
        return PbcSettingsType.PricerContantsSettings;
    }

    @Override
    public synchronized void loadPersonalSettings() {
    }

    @Override
    public synchronized void storePersonalSettings() {
    }

    @Override
    public boolean hasPointBoxPricingPolicy(String pricingPolicy){
        return policySettings.hasPointBoxPricingPolicy(pricingPolicy);
    }

    @Override
    public synchronized PointboxPricingPolicy getPointBoxPricingPolicy(String pricingPolicy){
        return policySettings.getPointBoxPricingPolicy(pricingPolicy);
    }
}
