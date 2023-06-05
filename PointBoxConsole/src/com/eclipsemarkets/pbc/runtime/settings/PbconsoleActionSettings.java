/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleActionSettings;
import com.eclipsemarkets.pbc.runtime.settings.PbcSettingsType;

/**
 *
 * @author Zhijun Zhang
 */
class PbconsoleActionSettings extends PbconsoleSettings implements IPbconsoleActionSettings{

    private boolean currentSystemFrameHorizontalLayout;
    
    PbconsoleActionSettings(IPbcRuntime runtime) {
        super(runtime);
        currentSystemFrameHorizontalLayout = true;
    }

    public synchronized PbcSettingsType getPbcSettingsType() {
        return PbcSettingsType.PbconsoleActionSettings;
    }

    public synchronized void loadPersonalSettings() {
        //todo: zzj - get it from database?
    }

    public synchronized void storePersonalSettings() {
        //todo: zzj - save it into database?
    }
//
//    ISettingsEvent personalizeSettingsImpl(IGatewayConnectorBuddy pointBoxLoginUser) {
//        return new SettingsEvent();
//    }
//
//    @Override
//    void saveSettingsImpl() {
//
//    }
//
//    @Override
//    void loadImpl() {
//        fireLoadingStatusUpdated(new EmsEntityLifeCycleEvent(this, "Load PointBox action settings ..."));
//        currentSystemFrameHorizontalLayout = true;
//    }
//
//    @Override
//    void unloadImpl() {
//        fireLoadingStatusUpdated(new EmsEntityLifeCycleEvent(this, "Unload PointBox action settings ..."));
////    }
//
//    public EmsSettingsType getEmsSettingsType() {
//        return EmsSettingsType.PbconsoleActionSettings;
//    }
//
//    public EmsEntityTypeWrapper getEmsEntityTypeWrapper() {
//        return EmsEntityTypeWrapper.EmsSettingsType_PbconsoleActionSettings;
//    }

    public synchronized boolean isSystemFrameHorizontalLayout() {
        return currentSystemFrameHorizontalLayout;
    }

    public synchronized void setCurrentSystemFrameHorizontalLayout(boolean currentSystemFrameHorizontalLayout) {
        this.currentSystemFrameHorizontalLayout = currentSystemFrameHorizontalLayout;
    }
}
