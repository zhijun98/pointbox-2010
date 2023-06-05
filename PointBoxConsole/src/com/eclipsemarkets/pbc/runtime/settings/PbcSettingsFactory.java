/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.runtime.IPointBoxPricingEngineSettings;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.runtime.IPointBoxPricerPolicySettings;
import com.eclipsemarkets.runtime.IPointBoxParserSettings;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.storage.PbcDatabaseInstance;
import java.util.logging.Logger;

/**
 * PbcSettingsFactory
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 29, 2011 at 10:08:16 AM
 */
public class PbcSettingsFactory {
    private static final Logger logger;
    private static IPbconsoleImageSettings pbconsoleImageSettings;
    private static IPbconsoleAudioSettings pbconsoleAudioSettings;
    private static IPointBoxParserSettings pbconsoleParserContantsSettings;
    private static IPointBoxPricerPolicySettings pbconsolePricerContantsSettings;
    private static IPointBoxPricingEngineSettings pricingEngineSettings;
    private static IPbconsoleActionSettings pbconsoleActionSettings;
    private static IPointBoxTalkerSettings pointBoxTalkerSettings;
    static{
        logger = Logger.getLogger(PbcSettingsFactory.class.getName());
        pbconsoleAudioSettings = null;
        pbconsoleParserContantsSettings = null;
        pbconsolePricerContantsSettings = null;
        pricingEngineSettings = null;
        pbconsoleActionSettings = null;
        pointBoxTalkerSettings = null;
    }

    private PbcSettingsFactory() {
    }

    public static String getOwnerUniqueName(IGatewayConnectorBuddy pointBoxLoginUser){
        String ownerUniqueName;
        if (pointBoxLoginUser == null){
            ownerUniqueName = PbcDatabaseInstance.DefaultEmsUser.toString();
        }else{
            ownerUniqueName = pointBoxLoginUser.getIMUniqueName();
        }
        return ownerUniqueName;
    }

    public static IPointBoxTalkerSettings getPointBoxTalkerSettingsSingleton(IPbcRuntime runtime){
        if (pointBoxTalkerSettings == null){
            pointBoxTalkerSettings = new PointBoxTalkerSettings(runtime);
        }
        return pointBoxTalkerSettings;
    }

    public static IPbconsoleActionSettings getPbconsoleActionSettingsSingleton(IPbcRuntime runtime){
        if (pbconsoleActionSettings == null){
            pbconsoleActionSettings = new PbconsoleActionSettings(runtime);
        }
        return pbconsoleActionSettings;
    }

    public static IPbcSettings getPricingEngineSettingsSingleton(IPbcRuntime runtime) {
        if (pricingEngineSettings == null){
            pricingEngineSettings = new PbconsolePricingEngineSettings(runtime);
        }
        return (IPbcSettings)pricingEngineSettings;
    }

    public static IPbcSettings getPbconsolePricerContantsSettingsSingleton(IPbcRuntime runtime){
        if (pbconsolePricerContantsSettings == null){
            pbconsolePricerContantsSettings = new PbconsolePricerContantsSettings(runtime);
        }
        if (pbconsolePricerContantsSettings instanceof IPbcSettings){
            return (IPbcSettings)pbconsolePricerContantsSettings;
        }else{
            PointBoxTracer.displayMessage(logger, "[TECH] Type error", new Exception("PricerContantsSettings has to be IPbcSettings also"));
            return null;
        }
    }

    public static IPbcSettings getPbconsoleParserContantsSettingsSingleton(IPbcRuntime runtime){
        if (pbconsoleParserContantsSettings == null){
            pbconsoleParserContantsSettings = new PbconsoleParserSettings(runtime);
        }
        if (pbconsoleParserContantsSettings instanceof IPbcSettings){
            return (IPbcSettings)pbconsoleParserContantsSettings;
        }else{
            PointBoxTracer.displayMessage(logger, "[TECH] Type error", new Exception("PbconsoleParserSettings has to be IPbcSettings also"));
            return null;
        }
    }

    public static IPbconsoleImageSettings getPbconsoleImageSettingsSingleton(IPbcRuntime runtime) {
        if (pbconsoleImageSettings == null){
            pbconsoleImageSettings = new PbconsoleImageSettings(runtime);
        }
        return pbconsoleImageSettings;
    }

    public static IPbconsoleAudioSettings getPbconsoleAudioSettingsSingleton(IPbcRuntime runtime) {
        if (pbconsoleAudioSettings == null){
            pbconsoleAudioSettings = new PbcAudioSettings(runtime);
        }
        return pbconsoleAudioSettings;
    }
}//PbcSettingsFactory

