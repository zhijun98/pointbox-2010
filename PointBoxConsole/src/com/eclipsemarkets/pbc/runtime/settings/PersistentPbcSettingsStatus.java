/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.web.pbc.PbcClientSettings;
import com.eclipsemarkets.web.pbc.PointBoxConsoleSettings;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import com.eclipsemarkets.web.pbc.viewer.PbcViewerSettings;

/**
 *
 * @author Zhijun Zhang, date & time: Jun 5, 2013 - 5:14:23 PM
 */
class PersistentPbcSettingsStatus {

    private static PersistentPbcSettingsStatus self = null;
    
    /**
     * One type of server-side settings is to buffer the change, and a separated 
     * dedicated thread keeps eyes on this flag to request settings update to the 
     * server. Refer to serviceTimer.
     */
    private boolean savingBuddyListRequested;
    /**
     * 
     */
    private boolean savingBuddyProfileRequested;
    /**
     * This only includes status of primitive data fields in PbcClientSettings
     */
    private boolean savingPbcClientSettingsRequested;
    /**
     * PbcViewerSettings[] of PbcClientSettings
     */
    private boolean savingPbcViewerSettingsRequested;
    /**
     * PbcFileSettings[] of PbcClientSettings
     */
    private boolean savingPbcFileSettingsRequested;
    /**
     * PbcWindowsSettings[] of PbcClientSettings
     */
    private boolean savingPbcWindowsSettingsRequested;
    
    public static PersistentPbcSettingsStatus getSingleton(){
        if (self == null){
            self = new PersistentPbcSettingsStatus();
        }
        return self;
    }
    
    public synchronized void initializeServerSideSettingsStatus(PointBoxConsoleSettings pointBoxConsoleSettingsFromServerSide) {
        PbcBuddyListSettings[] aPbcBuddyListSettingsArray = pointBoxConsoleSettingsFromServerSide.getPbcBuddyListSettings();
        if (aPbcBuddyListSettingsArray != null){
            for (PbcBuddyListSettings aPbcBuddyListSettings : aPbcBuddyListSettingsArray){
                if (aPbcBuddyListSettings != null){
                    setSavingBuddyListRequested(aPbcBuddyListSettings.isPersistentRequired());             
                }
            }
        }
        PbcClientSettings aPbcClientSettings = pointBoxConsoleSettingsFromServerSide.getPbcClientSettings();
        if (aPbcClientSettings != null){
            PbcViewerSettings[] aPbcViewerSettingsArray = aPbcClientSettings.getPbcViewerSettings();
            if (aPbcViewerSettingsArray != null){
                setSavingPbcViewerSettingsRequested(false);
                for (PbcViewerSettings aPbcViewerSettings : aPbcViewerSettingsArray){
                    if (aPbcViewerSettings != null){
                        if (aPbcViewerSettings.getPersistentPurpose() != 0){
                            this.setSavingPbcViewerSettingsRequested(true);
                        }
                    }
                }//for
            }
        }
    }

    public boolean isSavingBuddyListRequested() {
        return savingBuddyListRequested;
    }

    public void setSavingBuddyListRequested(boolean savingBuddyListRequested) {
        this.savingBuddyListRequested = savingBuddyListRequested;
    }

    public boolean isSavingBuddyProfileRequested() {
        return savingBuddyProfileRequested;
    }

    public void setSavingBuddyProfileRequested(boolean savingBuddyProfileRequested) {
        this.savingBuddyProfileRequested = savingBuddyProfileRequested;
    }

    public boolean isSavingPbcViewerSettingsRequested() {
        return savingPbcViewerSettingsRequested;
    }

    public void setSavingPbcViewerSettingsRequested(boolean savingPbcViewerSettingsRequested) {
        this.savingPbcViewerSettingsRequested = savingPbcViewerSettingsRequested;
    }
    
    public synchronized boolean isSavingPbcClientSettingsRequested() {
        return savingPbcClientSettingsRequested;
    }

    public synchronized void setSavingPbcClientSettingsRequested(boolean savingPbcClientSettingsRequested) {
        this.savingPbcClientSettingsRequested = savingPbcClientSettingsRequested;
    }

    public synchronized boolean isSavingPbcFileSettingsRequested() {
        return savingPbcFileSettingsRequested;
    }

    public synchronized void setSavingPbcFileSettingsRequested(boolean savingPbcFileSettingsRequested) {
        this.savingPbcFileSettingsRequested = savingPbcFileSettingsRequested;
    }

    public synchronized boolean isSavingPbcWindowsSettingsRequested() {
        return savingPbcWindowsSettingsRequested;
    }

    public synchronized void setSavingPbcWindowsSettingsRequested(boolean savingPbcWindowsSettingsRequested) {
        this.savingPbcWindowsSettingsRequested = savingPbcWindowsSettingsRequested;
    }
}
