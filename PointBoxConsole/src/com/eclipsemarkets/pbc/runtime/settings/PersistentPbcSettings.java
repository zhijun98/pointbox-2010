/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.talker.TalkerConnectionLostEvent;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.global.exceptions.PbcRuntimeException;
import com.eclipsemarkets.pbc.face.PbcArchiveFormat;
import com.eclipsemarkets.pbc.face.PbcArchiveStatus;
import com.eclipsemarkets.pbc.face.PbcFaceComponentType;
import static com.eclipsemarkets.pbc.face.PbcFaceComponentType.FloatingPointBoxFrame;
import com.eclipsemarkets.pbc.face.talker.IBuddyListPanel;
import com.eclipsemarkets.pbc.face.talker.PbcBuddyListType;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyKey;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyValue;
import com.eclipsemarkets.pbc.face.viewer.ViewerTableType;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterCriteria;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.runtime.*;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.PointBoxAccountID;
import com.eclipsemarkets.web.PointBoxConnectorID;
import com.eclipsemarkets.web.pbc.*;
import com.eclipsemarkets.web.pbc.talker.BuddyListBuddyItem;
import com.eclipsemarkets.web.pbc.talker.BuddyListGroupItem;
import com.eclipsemarkets.web.pbc.talker.BuddyProfile;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import com.eclipsemarkets.web.pbc.viewer.PbcViewerColumnSettings;
import com.eclipsemarkets.web.pbc.viewer.PbcViewerSettings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.SortOrder;

/**
 * Wrapper of PointBoxConsoleSettings: synchronization and default values are considered
 * in the implementation.Account ID and default values of client settings are required. 
 * Buddy list settings can be NULL. All the settings are required to be saved into the 
 * system. There is a internal serviceTimer which check the settings every 3 minutes. If 
 * settings was changed, it will be sent to the server for saving into the database.
 * @author Zhijun Zhang
 */
public class PersistentPbcSettings {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PersistentPbcSettings.class.getName());
    }
    
    private IPbcRuntime runtime;
    
    private Timer serviceTimer;

    /**
     * Hold all the settings of PointBoxConsole. This internal settings has to be synchronized by "this"
     */
    private PointBoxConsoleSettings pbcSettings;
    private PbcFileSettingsProxy pbcFileSettingsProxy;
    
    //private PointBoxDefaultProperties localProperties;
    
    private final PersistentPbcSettingsStatus status = PersistentPbcSettingsStatus.getSingleton();
    
    final long initialInterval = 60*1000;
    //every 3 minutes, try to save settings if it was changed
    final long interval = 3*60*1000;
    
    public PersistentPbcSettings(IPbcRuntime runtime) {
        this.runtime = runtime;
        
        /**
         * A default settings
         */
        pbcSettings = new PointBoxConsoleSettings();
    }

    private PbcFileSettingsProxy getPbcFileSettingsProxy() {
        if (pbcFileSettingsProxy == null){
            pbcFileSettingsProxy = new PbcFileSettingsProxy(runtime.getPbcPricingModelMap());
        }
        return pbcFileSettingsProxy;
    }
    
    private IPbcKernel getKernel(){
        return runtime.getKernel();
    }

    /**
     * When PBC got a successful login, this method should be called once.
     * @param pointBoxConsoleSettings
     * @throws PbcRuntimeException 
     */
    public final synchronized void initializeServerSidePointBoxConsoleSettings(PointBoxConsoleSettings pointBoxConsoleSettings) throws PbcRuntimeException {
        //(1) settings itself cannot be NULL
        if (pointBoxConsoleSettings == null){
            throw new PbcRuntimeException("PointBoxConsoleSettings cannot be NULL");
        }
        //(2) settings's account ID cannot be NULL
        if (pointBoxConsoleSettings.getPointBoxAccountID() == null){
            throw new PbcRuntimeException("PointBoxAccountID of PointBoxConsoleSettings cannot be NULL");
        }
        
        this.pbcSettings = pointBoxConsoleSettings;
        
        //(3) clientSettings need default values if it had NULL values. buddyListSettings can be NULL
        PbcClientSettings pbcClientSettings = initializePbcClientSettingsHelper(pointBoxConsoleSettings);
        //(4) set account ID into kernel.
        getKernel().setPointBoxAccountID(pbcClientSettings.getPointBoxAccountID());
        //(5) set the status of settings
        status.initializeServerSideSettingsStatus(pointBoxConsoleSettings);
        //(6) clean up legacy data
        cleanLegacyDataOfPbcSettings();
        //(7) start threading for saving settins and check connection status periodically
        serviceTimer = new Timer();
        serviceTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                try{
                    requestToSavePointBoxConsoleSettings();
                    List<String> result = requestToCheckConnectionOfflineStatus();
                    if ((result != null) && (!result.isEmpty())){
                        getKernel().raisePointBoxEvent(new TalkerConnectionLostEvent(result, PointBoxEventTarget.PbcFace));
                    }
                }catch (Exception ex){}
            }
        }, initialInterval, interval);
    }

    /**
     * clean up legacy data. This method should be called only one time in the beginning 
     * of launching PBC, considering performance.
     */
    private synchronized void cleanLegacyDataOfPbcSettings() {
        if (pbcSettings == null){
            return;
        }
        /**
         * In the regular buddy list, buddies cannot be duplicated in different groups. 
         * However, in history, PBC did permit it happened. The following logic can fix 
         * it.
         */
        PbcBuddyListSettings[] aPbcBuddyListSettingsArray = pbcSettings.getPbcBuddyListSettings();
        if (aPbcBuddyListSettingsArray == null){
            return;
        }
        for (PbcBuddyListSettings aPbcBuddyListSettings : aPbcBuddyListSettingsArray){
            cleanRedundantBuddiesInRegularBuddyListHelper(aPbcBuddyListSettings);
        }//for
        status.setSavingBuddyListRequested(true);
    }

    private void cleanRedundantBuddiesInRegularBuddyListHelper(PbcBuddyListSettings aPbcBuddyListSettings) {
        if (aPbcBuddyListSettings == null){
            return;
        }
        if (!(PbcBuddyListType.RegularBuddyList.toString().equalsIgnoreCase(aPbcBuddyListSettings.getBuddyListType()))){
            return;
        }
        TreeSet<String> existingBuddyNames = new TreeSet<String>();
        BuddyListGroupItem[] aBuddyListGroupItemArray = aPbcBuddyListSettings.getGroupItems();
        if (aBuddyListGroupItemArray != null){
            for (BuddyListGroupItem aBuddyListGroupItem : aBuddyListGroupItemArray){
                if (aBuddyListGroupItem != null){
                    aBuddyListGroupItem.setBuddyItems(cleanRedundantBuddiesHelper(existingBuddyNames, aBuddyListGroupItem.getBuddyItems()));
                }
            }//for
        }
    }

    /**
     * 
     * @param existingBuddyNames - if it is NULL. Otherwise, NULL will be always returned.
     * @param aBuddyListBuddyItemArray - if it is NULL. Otherwise, NULL will be always returned.
     * @return 
     */
    private BuddyListBuddyItem[] cleanRedundantBuddiesHelper(TreeSet<String> existingBuddyNames, BuddyListBuddyItem[] aBuddyListBuddyItemArray) {
        if ((existingBuddyNames == null) || (aBuddyListBuddyItemArray == null)){
            return null;
        }
        ArrayList<BuddyListBuddyItem> aBuddyListBuddyItemList = new ArrayList<BuddyListBuddyItem>();
        for (BuddyListBuddyItem aBuddyListBuddyItem : aBuddyListBuddyItemArray){
            if (aBuddyListBuddyItem != null){
                if (existingBuddyNames.contains(aBuddyListBuddyItem.getBuddyName())){
                    //find duplicated buddies
                    status.setSavingBuddyListRequested(true);
                }else{
                    existingBuddyNames.add(aBuddyListBuddyItem.getBuddyName());
                    aBuddyListBuddyItemList.add(aBuddyListBuddyItem);
                }
            }
        }//for
        return aBuddyListBuddyItemList.toArray(new BuddyListBuddyItem[0]);
    }

    /**
     * This helper method guaranteed aPbcClientSettings in pointBoxConsoleSettings 
     * having at least default data.
     * @param pointBoxConsoleSettings
     * @return - never NULL
     */
    private synchronized PbcClientSettings initializePbcClientSettingsHelper(PointBoxConsoleSettings pointBoxConsoleSettings) {
        PbcClientSettings aPbcClientSettings = pointBoxConsoleSettings.getPbcClientSettings();
        if (aPbcClientSettings == null){
            aPbcClientSettings = new PbcClientSettings();
            pointBoxConsoleSettings.setPbcClientSettings(aPbcClientSettings);
        }
        validatePbcClientSettingsDefaultsHelper(pointBoxConsoleSettings.getPbcClientSettings());
        
        PbcFileSettings[] pbcFileSettings = aPbcClientSettings.getPbcFileSettings();
        if (pbcFileSettings != null){
            boolean changed = false;
            for (int i = 0; i < pbcFileSettings.length; i++){
                if (getPbcFileSettingsProxy().insertPbcFileSettings(pbcFileSettings[i])){
                    changed = true;
                }
            }//for
            if (changed){
                status.setSavingPbcFileSettingsRequested(true);
            }
        }
        return aPbcClientSettings;
    }

    public List<PbcPricingModel> initializebcPricingRuntimeCurveSettings() {
        String loginUserUniqueName = runtime.getKernel().getPointBoxLoginUser().getIMUniqueName();
        return getPbcFileSettingsProxy().initializePricingEnvironmentCurveSettings(runtime, loginUserUniqueName, 
                                                               PointBoxConsoleProperties.getSingleton().retrievePricingRuntimeFolder(loginUserUniqueName), 
                                                               runtime.getPbcPricingModelMap());
    }

    private synchronized void validatePbcClientSettingsDefaultsHelper(PbcClientSettings clientSettings) {
        if (DataGlobal.isEmptyNullString(clientSettings.getArchiveFormat())){
            clientSettings.setArchiveFormat(PbcArchiveFormat.PlainText.toString());
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getArchiveLocation())){
            clientSettings.setArchiveLocation("");
            //status.setSavingPbcClientSettingsRequested(true);
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getArchiveStatus())){
            clientSettings.setArchiveStatus(PbcArchiveStatus.Stop.toString());
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getLocalProxyHost())){
            clientSettings.setLocalProxyHost("");
            //setSavingRequested(true);
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getLocalProxyLogin())){
            clientSettings.setLocalProxyLogin("");
            //setSavingRequested(true);
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getLocalProxyPassword())){
            clientSettings.setLocalProxyPassword("");
            //setSavingRequested(true);
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getMainFrameLaf())){
            clientSettings.setMainFrameLaf("");
            //setSavingRequested(true);
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getMainFrameLayout())){
            clientSettings.setMainFrameLayout(PbcSystemFrameLayout.Vertical.toString());
        }
        if (DataGlobal.isEmptyNullString(clientSettings.getMainFrameStyle())){
            clientSettings.setMainFrameStyle(PbcSystemFrameStyle.Docked.toString());
        }
        if ((clientSettings.getPbcFileSettings() == null) || (clientSettings.getPbcFileSettings().length == 0)){
            initializebcPricingRuntimeCurveSettings();
        }
        if ((clientSettings.getPbcViewerSettings() == null) || (clientSettings.getPbcViewerSettings().length == 0)){
            setDefaultPbcViewerSettings(clientSettings);
        }else{
            PbcViewerSettings[] aPbcViewerSettingsArray = clientSettings.getPbcViewerSettings();
            TreeSet<String> viewerTabNameSet = new TreeSet<String>();
            for (PbcViewerSettings aPbcViewerSettings : aPbcViewerSettingsArray){
                viewerTabNameSet.add(aPbcViewerSettings.getViewerTabName().toLowerCase());
            }//for
            
            List<ViewerTableType> aViewerTableTypeList = ViewerTableType.getEnumTypeList(false);
            for (ViewerTableType aViewerTableType : aViewerTableTypeList){
                if ((!ViewerTableType.FILTER_RESULT.equals(aViewerTableType)) && (!ViewerTableType.UNKNOWN.equals(aViewerTableType))){
                    if (!viewerTabNameSet.contains(aViewerTableType.toString().toLowerCase())){
                        setDefaultPbcViewerSettings(clientSettings, aViewerTableType);
                    }
                }
            }
        }
        if ((clientSettings.getPbcWindowsSettings() == null) || (clientSettings.getPbcWindowsSettings().length == 0)){
            PbcWindowsSettings pbcFrameSettings = new PbcWindowsSettings();
            pbcFrameSettings.setHeight(750);
            pbcFrameSettings.setPointX(-1);
            pbcFrameSettings.setPointY(-1);
            pbcFrameSettings.setWidth(1000);
            pbcFrameSettings.setWindowsUniqueName(PbcFaceComponentType.PointBoxFrame.toString());
            //only one windows
            PbcWindowsSettings[] aPbcWindowsSettingsArray = new PbcWindowsSettings[1];
            aPbcWindowsSettingsArray[0] = pbcFrameSettings;
            clientSettings.setPbcWindowsSettings(aPbcWindowsSettingsArray);
        }
        if (clientSettings.getRefreshPriceInterval() <= 0){
            clientSettings.setRefreshPriceInterval(7*1000);
        }
        if (clientSettings.getRefreshQuoteMax() <= 0){
            clientSettings.setRefreshQuoteMax(250);
        }
    }
    private synchronized void addNewPbcWindowsSettings(PbcClientSettings clientSettings, PbcWindowsSettings aNewPbcWindowsSettings) {
        if (clientSettings == null){
            return;
        }
        if ((aNewPbcWindowsSettings == null)||(DataGlobal.isEmptyNullString(aNewPbcWindowsSettings.getWindowsUniqueName()))){
            return;
        }
        List<PbcWindowsSettings> aNewPbcWindowsSettingsList = new ArrayList<PbcWindowsSettings>();
        PbcWindowsSettings[] pbcWindowsSettingsArray = clientSettings.getPbcWindowsSettings();
        if (pbcWindowsSettingsArray == null){
            aNewPbcWindowsSettingsList.add(aNewPbcWindowsSettings);
        }else{
            boolean isNew = true;
            for (PbcWindowsSettings aPbcWindowsSettings : pbcWindowsSettingsArray){
                if (aNewPbcWindowsSettings.getWindowsUniqueName().equalsIgnoreCase(aPbcWindowsSettings.getWindowsUniqueName())){
                    aNewPbcWindowsSettingsList.add(aNewPbcWindowsSettings);
                    isNew = false;
                }else{
                    aNewPbcWindowsSettingsList.add(aPbcWindowsSettings);
                }
            }//for
            if (isNew){
                aNewPbcWindowsSettingsList.add(aNewPbcWindowsSettings);
            }
        }//if
        clientSettings.setPbcWindowsSettings(aNewPbcWindowsSettingsList.toArray(new PbcWindowsSettings[0]));
    }
    
    private synchronized void setDefaultPbcViewerSettings(PbcClientSettings aPbcClientSettings) {
        if (aPbcClientSettings == null){
            return;
        }
        
        addNewPbcViewerSettings(aPbcClientSettings, 
                                createNewPbcViewerSettings(ViewerTableType.ALL_MESSAGES, ViewerTableType.ALL_MESSAGES.toString()));
        addNewPbcViewerSettings(aPbcClientSettings, 
                                createNewPbcViewerSettings(ViewerTableType.ALL_QUOTES, ViewerTableType.ALL_QUOTES.toString()));
        addNewPbcViewerSettings(aPbcClientSettings, 
                                createNewPbcViewerSettings(ViewerTableType.OUTGOING_MESSAGES, ViewerTableType.OUTGOING_MESSAGES.toString()));
        addNewPbcViewerSettings(aPbcClientSettings, 
                                createNewPbcViewerSettings(ViewerTableType.INCOMING_MESSAGES, ViewerTableType.INCOMING_MESSAGES.toString()));
        addNewPbcViewerSettings(aPbcClientSettings, 
                                createNewPbcViewerSettings(ViewerTableType.SEARCH_RESULT, ViewerTableType.SEARCH_RESULT.toString()));
    }
    
    private synchronized void setDefaultPbcViewerSettings(PbcClientSettings aPbcClientSettings, ViewerTableType aViewerTableType) {
        if (aPbcClientSettings == null){
            return;
        }
        addNewPbcViewerSettings(aPbcClientSettings, 
                                createNewPbcViewerSettings(aViewerTableType, aViewerTableType.toString()));
//        addNewPbcViewerSettings(aPbcClientSettings, 
//                                createNewPbcViewerSettings(ViewerTableType.ALL_QUOTES, ViewerTableType.ALL_QUOTES.toString()));
//        addNewPbcViewerSettings(aPbcClientSettings, 
//                                createNewPbcViewerSettings(ViewerTableType.OUTGOING_MESSAGES, ViewerTableType.OUTGOING_MESSAGES.toString()));
//        addNewPbcViewerSettings(aPbcClientSettings, 
//                                createNewPbcViewerSettings(ViewerTableType.INCOMING_MESSAGES, ViewerTableType.INCOMING_MESSAGES.toString()));
//        addNewPbcViewerSettings(aPbcClientSettings, 
//                                createNewPbcViewerSettings(ViewerTableType.SEARCH_RESULT, ViewerTableType.SEARCH_RESULT.toString()));
    }
    
//    private final String goodNymexFtpUrl = "ftp.cmegroup.com/pub/settle/stlnymex";
//    private final String badNymexFtpUrl = "ftp.cme.com/pub/settle/stlnymex";
    
    private synchronized void addNewPbcFileSettings(PbcClientSettings clientSettings, 
                                                    String codeBasedFileName,
                                                    String location) 
    {
        if (clientSettings == null){
            return;
        }
        if (codeBasedFileName == null){
            return;
        }
        location = DataGlobal.denullize(location);
        PbcFileSettings[] pbcFileSettings = clientSettings.getPbcFileSettings();
        int max;
        if (pbcFileSettings == null){
            max = 1;
        }else{
            max = pbcFileSettings.length + 1;
        }
        PbcFileSettings[] newPbcFileSettings = new PbcFileSettings[max];
        PbcFileSettings aPbcFileSettings = null;
        if (max > 1){
            for (int i = 0; i < max - 1; i++){
                if (pbcFileSettings[i].getFileUniqueName().equalsIgnoreCase(codeBasedFileName)){
                    aPbcFileSettings = pbcFileSettings[i];
                    break;
                }
                newPbcFileSettings[i] = pbcFileSettings[i];
            }
        }
        if (aPbcFileSettings == null){
            //a new file path
            aPbcFileSettings = createNewPbcFileSettingsInstance(codeBasedFileName, location);
            getPbcFileSettingsProxy().insertPbcFileSettings(aPbcFileSettings);
            newPbcFileSettings[max-1] = aPbcFileSettings;
            clientSettings.setPbcFileSettings(newPbcFileSettings);
            status.setSavingPbcFileSettingsRequested(true);
        }else{
            //update location
            if (!location.equalsIgnoreCase(aPbcFileSettings.getFileLocation())){
                aPbcFileSettings.setFileLocation(location);
                initializeCurves(codeBasedFileName, location);
                status.setSavingPbcFileSettingsRequested(true);
            }
        }
    }
    
    private synchronized PbcFileSettings createNewPbcFileSettingsInstance(String codeBasedFileName,
                                                             String location)
    {
        PbcFileSettings aPbcFileSettings = new PbcFileSettings();
        aPbcFileSettings.setFileDescription(location);
        aPbcFileSettings.setFileLocation(location);
        aPbcFileSettings.setFileUniqueName(codeBasedFileName);
        initializeCurves(codeBasedFileName, location);
        return aPbcFileSettings;
    }

    private synchronized PbcWindowsSettings getPbcWindowsSettings(PbcFaceComponentType type){
        PbcWindowsSettings[] pbcWindowsSettings = pbcSettings.getPbcClientSettings().getPbcWindowsSettings();
        PbcWindowsSettings settings = null;
        for (int i = 0; i < pbcWindowsSettings.length; i++){
            if (pbcWindowsSettings[i].getWindowsUniqueName().equalsIgnoreCase(type.toString())){
                settings = pbcWindowsSettings[i];
                break;
            }
        }//for
        return settings;
    }

    public synchronized void setPbcWindowsLocation(Point location, PbcFaceComponentType type) {
        if (pbcSettings == null){
            return;
        }
        if (location == null){
            return;
        }
        boolean isChanged = false;
        PbcWindowsSettings settings = getPbcWindowsSettings(type);
        if (settings == null){
            isChanged = true;
            settings = new PbcWindowsSettings();
            settings.setWindowsUniqueName(type.toString());
            settings.setPointX((int)location.getX());
            settings.setPointY((int)location.getY());
            addNewPbcWindowsSettings(pbcSettings.getPbcClientSettings(), settings);
        }else{
            if (settings.getPointX() != (int)location.getX()){
                isChanged = true;
                settings.setPointX((int)location.getX());
            }
            if (settings.getPointY() != (int)location.getY()){
                isChanged = true;
                settings.setPointY((int)location.getY());
            }
        }
        status.setSavingPbcWindowsSettingsRequested(isChanged);
    }

    /**
     * @return - possibly NULL
     */
    public synchronized Point getPbcWindowsLocation(PbcFaceComponentType type) {
        if (pbcSettings == null){
            return null;
        }
        PbcWindowsSettings settings = getPbcWindowsSettings(type);
        if (settings == null){
            return null;
        }
        int x=settings.getPointX();
        int y=settings.getPointY();
        Point location=new Point(x,y);
        int width=settings.getWidth();
        int height=settings.getHeight();
        Point rightDownLocation=new Point(x+width,y+height);
        
        if (!(SwingGlobal.isLocationInScreenBounds(location))){
            return null;
        }
        if (!(SwingGlobal.isLocationInScreenBounds(rightDownLocation))){
            return null;
        }      

        return location;
    }

    public synchronized void setPbcWindowsSize(Dimension size, PbcFaceComponentType type) {
        if (pbcSettings == null){
            return;
        }
        if (size == null){
            return;
        }
        boolean isChanged = false;
        PbcWindowsSettings settings = getPbcWindowsSettings(type);
        if (settings == null){
            isChanged = true;
            settings = new PbcWindowsSettings();
            settings.setWindowsUniqueName(type.toString());
            settings.setHeight((int)size.getHeight());
            settings.setWidth((int)size.getWidth());
            addNewPbcWindowsSettings(pbcSettings.getPbcClientSettings(), settings);
        }else{
            if (settings.getHeight() != (int)size.getHeight()){
                isChanged = true;
                settings.setHeight((int)size.getHeight());
            }
            if (settings.getWidth() != (int)size.getWidth()){
                isChanged = true;
                settings.setWidth((int)size.getWidth());
            }
        }
        status.setSavingPbcWindowsSettingsRequested(isChanged);
    }

    public synchronized Dimension getPbcWindowsSize(PbcFaceComponentType type) {
        PbcWindowsSettings settings = getPbcWindowsSettings(type);
        if (settings == null){
            switch (type){
                case PointBoxFrame:
                    return new Dimension(1000, 750);
                case FloatingPointBoxFrame:
                    return new Dimension(800, 600);
                case FloatingMessagingFrame:
                    return new Dimension(600, 400);
                case FloatingBuddyListFrame:
                    return new Dimension(400, 600);
                default:
                    return null;
            }
        }else{
            return new Dimension(settings.getWidth(), settings.getHeight());
        }
    }

    public synchronized void setBpa_BgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getBpa_BgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setBpaBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setBpa_FgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getBpa_FgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setBpaForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setLatestRowBackground(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getLatestRowBackground(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setLatestRowBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setLatestRowForeground(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getLatestRowForeground(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setLatestRowForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setMsgBgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getMsgBgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setMsgBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setMsgFgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getMsgFgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setMsgForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setOutgoingBackground(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getOutgoingBackground(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setOutgoingBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setOutgoingForeground(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getOutgoingForeground(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setOutgoingForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setPa_BgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getPa_BgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setPaBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setPa_FgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getPa_FgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setPaForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setPb_BgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getPb_BgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setPbBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setPb_FgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getPb_FgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setPbForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setPbimQtFgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getPbimQtFgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setPbimQtForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setQtBgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getQtBgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setQtBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setPbimQtBgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getPbimQtBgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setPbimQtBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setQtFgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getQtFgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setQtForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setSelectedRowBackground(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getSelectedRowBackground(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setSelectedRowBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setSkippedQtBgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getSkippedQtBgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setSkippedQtBackgroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setSelectedRowForeground(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getSelectedRowForeground(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setSelectedRowForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setSkippedQtFgColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getSkippedQtFgColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setSkippedQtForegroundRgb(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setViewerGeneralColor(String viewerUniqueTabName, Color color) {
        if (color == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getViewerGeneralColor(viewerUniqueTabName).equals(color)){
                aPbcViewerSettings.setGeneralColor(color.getRGB());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized void setViewerGeneralFont(String viewerUniqueTabName, Font font) {
        if (font == null){
            return;
        }
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            if (!getViewerGeneralFont(viewerUniqueTabName).equals(font)){
                aPbcViewerSettings.setFontFamily(font.getFamily());
                aPbcViewerSettings.setFontStyle(font.getStyle());
                aPbcViewerSettings.setFontSize(font.getSize());
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }

    public synchronized Font getViewerGeneralFont(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return SwingGlobal.getLabelFont();
        }else{
            return new Font(aPbcViewerSettings.getFontFamily(),
                            aPbcViewerSettings.getFontStyle(),
                            aPbcViewerSettings.getFontSize());
        }
    }

    /**
     * 
     * @param viewerUniqueTabName
     * @return - if viewerUniqueTabName cannot be found or NULL, a default value will be returned.
     */
    public synchronized Color getViewerGeneralColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.BLACK;
        }else{
            return new Color(aPbcViewerSettings.getGeneralColor());
        }
    }

    public synchronized Color getBpa_FgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.BLACK;
        }else{
            return new Color(aPbcViewerSettings.getBpaForegroundRgb());
        }
    }

    public synchronized Color getBpa_BgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(255, 255, 153);
        }else{
            return new Color(aPbcViewerSettings.getBpaBackgroundRgb());
        }
    }

    public synchronized Color getLatestRowForeground(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.BLACK;
        }else{
            return new Color(aPbcViewerSettings.getLatestRowForegroundRgb());
        }
    }

    public synchronized Color getLatestRowBackground(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(255,204,204);
        }else{
            return new Color(aPbcViewerSettings.getLatestRowBackgroundRgb());
        }
    }

    public synchronized Color getMsgBgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.WHITE;
        }else{
            return new Color(aPbcViewerSettings.getMsgBackgroundRgb());
        }
    }

    public synchronized Color getMsgFgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.BLACK;
        }else{
            return new Color(aPbcViewerSettings.getMsgForegroundRgb());
        }
    }

    public synchronized Color getOutgoingBackground(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(204,204,255);
        }else{
            return new Color(aPbcViewerSettings.getOutgoingBackgroundRgb());
        }
    }

    public synchronized Color getOutgoingForeground(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.BLACK;
        }else{
            return new Color(aPbcViewerSettings.getOutgoingForegroundRgb());
        }
    }

    public synchronized Color getPa_BgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(0, 166, 0);
        }else{
            return new Color(aPbcViewerSettings.getPaBackgroundRgb());
        }
    }

    public synchronized Color getPa_FgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(255, 255, 51);
        }else{
            return new Color(aPbcViewerSettings.getPaForegroundRgb());
        }
    }

    public synchronized Color getPb_BgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(166, 0, 0);
        }else{
            return new Color(aPbcViewerSettings.getPbBackgroundRgb());
        }
    }

    public synchronized Color getPb_FgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(255, 255, 51);
        }else{
            return new Color(aPbcViewerSettings.getPbForegroundRgb());
        }
    }

    public synchronized Color getPbimQtBgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(0, 102, 0);
        }else{
            return new Color(aPbcViewerSettings.getPbimQtBackgroundRgb());
        }
    }

    public synchronized Color getPbimQtFgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.WHITE;
        }else{
            return new Color(aPbcViewerSettings.getPbimQtForegroundRgb());
        }
    }

    public synchronized Color getQtBgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(0, 0, 153);
        }else{
            return new Color(aPbcViewerSettings.getQtBackgroundRgb());
        }
    }

    public synchronized Color getQtFgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.WHITE;
        }else{
            return new Color(aPbcViewerSettings.getQtForegroundRgb());
        }
    }

    public synchronized Color getSelectedRowBackground(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.YELLOW;
        }else{
            return new Color(aPbcViewerSettings.getQtForegroundRgb());
        }
    }

    public synchronized Color getSelectedRowForeground(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.RED;
        }else{
            return new Color(aPbcViewerSettings.getQtForegroundRgb());
        }
    }

    public synchronized Color getSkippedQtBgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return new Color(255, 255, 153);
        }else{
            return new Color(aPbcViewerSettings.getSkippedQtBackgroundRgb());
        }
    }

    public synchronized Color getSkippedQtFgColor(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return Color.BLACK;
        }else{
            return new Color(aPbcViewerSettings.getSkippedQtForegroundRgb());
        }
    }

    public synchronized long getViewerFilterLongValue(String uniqueFilterTabName, FilterPropertyKey filterPropertyKey) {
        long result = -1;
        PbcViewerSettings aPbcViewerSettings = this.getPbcViewerSettings(uniqueFilterTabName);
        if ((aPbcViewerSettings != null) && (filterPropertyKey != null)){
            PbcFilterPropertySettings[] pbcFilterPropertySettings = aPbcViewerSettings.getPbcFilterPropertySettings();
            if ((pbcFilterPropertySettings == null) || pbcFilterPropertySettings.length == 0){
                result = -1;
            }else{
                for (int i = 0; i < pbcFilterPropertySettings.length; i++){
                    if (pbcFilterPropertySettings[i].getPropertyKey().equalsIgnoreCase((filterPropertyKey.toString()))){
                        try{
                            result = Long.parseLong(pbcFilterPropertySettings[i].getPropertyValue());
                        }catch (NumberFormatException ex){
                            result = -1;
                        }
                        break;
                    }
                }//for
            }
        }
        return result;
    }

    public synchronized String getViewerFilterStringValue(String uniqueFilterTabName, FilterPropertyKey filterPropertyKey) {
        String result = "";
        PbcViewerSettings aPbcViewerSettings = this.getPbcViewerSettings(uniqueFilterTabName);
        if ((aPbcViewerSettings != null) && (filterPropertyKey != null)){
            PbcFilterPropertySettings[] pbcFilterPropertySettings = aPbcViewerSettings.getPbcFilterPropertySettings();
            if ((pbcFilterPropertySettings == null) || pbcFilterPropertySettings.length == 0){
                result = "";
            }else{
                for (int i = 0; i < pbcFilterPropertySettings.length; i++){
                    if (pbcFilterPropertySettings[i].getPropertyKey().equalsIgnoreCase((filterPropertyKey.toString()))){
                        result = pbcFilterPropertySettings[i].getPropertyValue().toString();
                        break;
                    }
                }//for
            }
        }
        return result;
    }
    private synchronized void initializeCurves(String codeBasedFileName, String filePath){
        if (NIOGlobal.isValidFile(filePath)){
//            switch (fileType){
//                case CO_ForwardCurve_BRT:
//                    crudeForwardCurve_brt = PointBoxClientRuntimeFactory.createCrudeOilForwardCurveInstance(filePath);
//                    break;
//                case CO_ForwardCurve_WTI:
//                    crudeForwardCurve_wti = PointBoxClientRuntimeFactory.createCrudeOilForwardCurveInstance(filePath);
//                    break;
//                case GR_ForwardCurve_ZC:
//                    grainsForwardCurve_zc = PointBoxClientRuntimeFactory.createGrainsForwardCurveInstance(filePath);
//                    break;
//                case GR_ForwardCurve_ZM:
//                    grainsForwardCurve_zm = PointBoxClientRuntimeFactory.createGrainsForwardCurveInstance(filePath);
//                    break;
//                case GR_ForwardCurve_ZS:
//                    grainsForwardCurve_zs = PointBoxClientRuntimeFactory.createGrainsForwardCurveInstance(filePath);
//                    break;
//                case GR_ForwardCurve_ZW:
//                    grainsForwardCurve_zw = PointBoxClientRuntimeFactory.createGrainsForwardCurveInstance(filePath);
//                    break;
//                case GR_ForwardCurve_ZL:
//                    grainsForwardCurve_zl = PointBoxClientRuntimeFactory.createGrainsForwardCurveInstance(filePath);
//                    break;
//                case CO_Expirations_BRT:
//                    crudeOilExpirations_brt = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case CO_Expirations_WTI:
//                    crudeOilExpirations_wti = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case GR_Expirations_ZC:
//                    grainsExpirations_zc = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case GR_Expirations_ZM:
//                    grainsExpirations_zm = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case GR_Expirations_ZS:
//                    grainsExpirations_zs = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case GR_Expirations_ZW:
//                    grainsExpirations_zw = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case GR_Expirations_ZL:
//                    grainsExpirations_zl = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case NG_ForwardCurve:
//                    naturalGasForwardCurve = PointBoxClientRuntimeFactory.createNaturalGasForwardCurveInstance(filePath);
//                    break;
//                case NG_Expirations:
//                    naturalGasExpirationSettlementDates = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//                case PR_ForwardCurve:
//                    powerForwardCurve = PointBoxClientRuntimeFactory.createPowerForwardCurveInstance(filePath);
//                    break;
//                case PR_Expirations:
//                    powerExpirationSettlementDates = PointBoxClientRuntimeFactory.createExpirationSettlementDatesInstance(filePath);
//                    break;
//            }
        }
    }

    public synchronized IPbconsoleAccessorySettings getPbconsoleAccessorySettings() {
        return getPbcFileSettingsProxy();
    }

    public synchronized IPointBoxPricingSettings getPointBoxPricingSettings() {
        return getPbcFileSettingsProxy();
    }

    public synchronized PointBoxAccountID getPointBoxAccountID() {
        if (pbcSettings == null){
            return null;
        }
        return pbcSettings.getPointBoxAccountID();
    }

    public synchronized PointBoxConnectorID getPointBoxConnectorIdFromPbcBuddyListSettings(IGatewayConnectorBuddy loginUser) {
        if ((pbcSettings == null) || (loginUser == null)){
            return null;
        }
        return pbcSettings.getPointBoxConnectorIdFromPbcBuddyListSettings(loginUser);
    }

    public synchronized PbcArchiveFormat getArchiveFormat() {
        if (pbcSettings == null){
            return PbcArchiveFormat.PlainText;
        }
        return PbcArchiveFormat.convertToType(pbcSettings.getPbcClientSettings().getArchiveFormat());
    }

    public synchronized String getArchiveLocation() {
        if (pbcSettings == null){
            return "";
        }
        return DataGlobal.denullize(pbcSettings.getPbcClientSettings().getArchiveLocation());
    }

    public synchronized PbcArchiveStatus getArchiveStatus() {
        if (pbcSettings == null){
            return PbcArchiveStatus.Stop;
        }
        return PbcArchiveStatus.convertToType(pbcSettings.getPbcClientSettings().getArchiveStatus());
    }

    public synchronized void setArchiveFormat(PbcArchiveFormat format) {
        if (pbcSettings == null){
            return;
        }
        if (format == null){
            return;
        }
        if (!format.toString().equalsIgnoreCase(pbcSettings.getPbcClientSettings().getArchiveFormat())){
            pbcSettings.getPbcClientSettings().setArchiveFormat(format.toString());
            status.setSavingPbcClientSettingsRequested(true);
        }
    }

    public synchronized void setArchiveLocation(String archiveLocation) {
        if (pbcSettings == null){
            return;
        }
        archiveLocation = DataGlobal.denullize(archiveLocation);
        if (!archiveLocation.equalsIgnoreCase(pbcSettings.getPbcClientSettings().getArchiveLocation())){
            pbcSettings.getPbcClientSettings().setArchiveLocation(archiveLocation);
            status.setSavingPbcClientSettingsRequested(true);
        }
    }

    public synchronized void setArchiveStatus(PbcArchiveStatus archiveStatus) {
        if (pbcSettings == null){
            return;
        }
        if (archiveStatus == null){
            return;
        }
        if (!archiveStatus.toString().equalsIgnoreCase(pbcSettings.getPbcClientSettings().getArchiveStatus())){
            pbcSettings.getPbcClientSettings().setArchiveStatus(archiveStatus.toString());
            status.setSavingPbcClientSettingsRequested(true);
        }
    }

    public synchronized PbcBuddyListSettings getPbcBuddyListSettings(String buddyListType, String distListName) {
        if ((DataGlobal.isEmptyNullString(buddyListType)) || (DataGlobal.isEmptyNullString(distListName))){
            return null;
        }
        PbcBuddyListSettings[] settingsArray = pbcSettings.getPbcBuddyListSettings();
        if ((settingsArray != null) && (settingsArray.length > 0)){
            PbcBuddyListSettings result = null;
            for (PbcBuddyListSettings aPbcBuddyListSettings : settingsArray){
                if ((buddyListType.equalsIgnoreCase(aPbcBuddyListSettings.getBuddyListType()))
                        && (distListName.equalsIgnoreCase(aPbcBuddyListSettings.getBuddyListName())))
                {
                    result = aPbcBuddyListSettings;
                    break;
                }
            }
            return result;
        }else{
            return null;
        }
    }

    /**
     * This method exposed internal PbcBuddyListSettings instances
     * @return 
     */
    public synchronized ArrayList<PbcBuddyListSettings> getPbcBuddyListSettings() {
        ArrayList<PbcBuddyListSettings> settingsList = new ArrayList<PbcBuddyListSettings>();
        if (pbcSettings != null){
            PbcBuddyListSettings[] settings = pbcSettings.getPbcBuddyListSettings();
            if ((settings != null) && (settings.length > 0)){
                settingsList.addAll(Arrays.asList(settings));
            }
        }
        return settingsList;
    }
    
    public synchronized ArrayList<BuddyProfile> getPbcBuddyProfiles() {
        ArrayList<BuddyProfile> profileList = new ArrayList<BuddyProfile>();
        if (pbcSettings != null){
            BuddyProfile[] profiles = pbcSettings.getBuddyProfiles();
            if ((profiles != null) && (profiles.length > 0)){
                profileList.addAll(Arrays.asList(profiles));
            }
        }
        return profileList;
    }

    public synchronized void updatePbcBuddyProfiles(BuddyProfile aBuddyProfile) {
        if (aBuddyProfile == null){
            return;
        }
        ArrayList<BuddyProfile> profileList = getPbcBuddyProfiles();
        BuddyProfile existingBuddyProfile = null;
        if ((profileList != null) && (!profileList.isEmpty())){
            String aProfileUUID=aBuddyProfile.getGatewayServerType()+aBuddyProfile.getScreenName(); //screen name+server type decides which buddy
            for (BuddyProfile pbcBuddyProfile : profileList){
                String profileUUID=pbcBuddyProfile.getGatewayServerType()+pbcBuddyProfile.getScreenName();
                if (profileUUID.equalsIgnoreCase(aProfileUUID)){ 
                    existingBuddyProfile = pbcBuddyProfile;
                    break;
                }
            }
            if (existingBuddyProfile != null){
                profileList.remove(existingBuddyProfile);
            }
        }
        profileList.add(aBuddyProfile);
        aBuddyProfile.setPersistentPurpose(1);
        status.setSavingBuddyProfileRequested(true);
        pbcSettings.setBuddyProfiles(profileList.toArray(new BuddyProfile[0]));
    }
    
    public synchronized BuddyProfile retrievePbcBuddyProfiles(BuddyProfile aBuddyProfile) {
        if (aBuddyProfile == null){
            return null;
        }
        ArrayList<BuddyProfile> profileList = getPbcBuddyProfiles();
        if ((profileList != null) && (!profileList.isEmpty())){
            String aProfileUUID=aBuddyProfile.getGatewayServerType()+aBuddyProfile.getScreenName();//screen name+server type decides which buddy
            for (BuddyProfile pbcBuddyProfile : profileList){
                String profileUUID=pbcBuddyProfile.getGatewayServerType()+pbcBuddyProfile.getScreenName();
                if (profileUUID.equalsIgnoreCase(aProfileUUID)){
                    return pbcBuddyProfile;
                }
            }
        }
        return null;

    }

    public synchronized BuddyProfile getPbcBuddyProfile(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return null;
        }
        ArrayList<BuddyProfile> profileList = getPbcBuddyProfiles();
        if ((profileList != null) && (!profileList.isEmpty())){
            String aProfileUUID=buddy.getIMServerType().toString()+buddy.getIMScreenName();//screen name+server type decides which buddy
            for (BuddyProfile pbcBuddyProfile : profileList){
                String profileUUID=pbcBuddyProfile.getGatewayServerType()+pbcBuddyProfile.getScreenName();
                if (profileUUID.equalsIgnoreCase(aProfileUUID)){
                    return pbcBuddyProfile;
                }
            }
        }
        return null;
    }

    public synchronized PbcSystemFrameStyle getPbcSystemFrameStyle() {
        if (pbcSettings == null){
            return PbcSystemFrameStyle.Docked;
        }
        return PbcSystemFrameStyle.convertToType(this.pbcSettings.getPbcClientSettings().getMainFrameStyle());
    }

    public synchronized void setPbcSystemFrameStyle(PbcSystemFrameStyle pbcSystemFrameStyle) {
        if (pbcSystemFrameStyle == null){
            return;
        }
        if (!pbcSystemFrameStyle.equals(getPbcSystemFrameStyle())){
            pbcSettings.getPbcClientSettings().setMainFrameStyle(pbcSystemFrameStyle.toString());
            status.setSavingPbcClientSettingsRequested(true);
        }
    }

    public synchronized PbcSystemFrameLayout getPbcSystemFrameLayout() {
        if (pbcSettings == null){
            return PbcSystemFrameLayout.Horizontal;
        }
        return PbcSystemFrameLayout.convertToType(pbcSettings.getPbcClientSettings().getMainFrameLayout());
    }

    public synchronized void setPbcSystemFrameLayout(PbcSystemFrameLayout pbcSystemFrameLayout) {
        if (pbcSystemFrameLayout == null){
            return;
        }
        if (!pbcSystemFrameLayout.equals(getPbcSystemFrameLayout())){
            pbcSettings.getPbcClientSettings().setMainFrameLayout(pbcSystemFrameLayout.toString());
            status.setSavingPbcClientSettingsRequested(true);
        }
    }

    public synchronized void setGeneralColorForAllPbcViewerSettings(Color color) {
        if (pbcSettings == null){
            return;
        }
        PbcClientSettings aPbcClientSettings = pbcSettings.getPbcClientSettings();
        if (aPbcClientSettings == null){
            return;
        }
        PbcViewerSettings[] allPbcViewerSettings = aPbcClientSettings.getPbcViewerSettings();
        if (allPbcViewerSettings != null){
            for (int i = 0; i < allPbcViewerSettings.length; i++){
                if (allPbcViewerSettings[i].getGeneralColor() != color.getRGB()){
                    allPbcViewerSettings[i].setGeneralColor(color.getRGB());
                    allPbcViewerSettings[i].setPersistentPurpose(1);
                    status.setSavingPbcViewerSettingsRequested(true);
                }
            }
        }
    }

    public synchronized void setGeneralFontForAllPbcViewerSettings(Font font) {
        if (pbcSettings == null){
            return;
        }
        PbcClientSettings aPbcClientSettings = pbcSettings.getPbcClientSettings();
        if (aPbcClientSettings == null){
            return;
        }
        PbcViewerSettings[] allPbcViewerSettings = aPbcClientSettings.getPbcViewerSettings();
        if (allPbcViewerSettings != null){
            for (int i = 0; i < allPbcViewerSettings.length; i++){
                if (!allPbcViewerSettings[i].getFontFamily().equalsIgnoreCase(font.getFamily())){
                    allPbcViewerSettings[i].setFontFamily(font.getFamily());
                    allPbcViewerSettings[i].setPersistentPurpose(1);
                    status.setSavingPbcViewerSettingsRequested(true);
                }
                if (allPbcViewerSettings[i].getFontSize() != font.getSize()){
                    allPbcViewerSettings[i].setFontSize(font.getSize());
                    allPbcViewerSettings[i].setPersistentPurpose(1);
                    status.setSavingPbcViewerSettingsRequested(true);
                }
                if (allPbcViewerSettings[i].getFontStyle() != font.getStyle()){
                    allPbcViewerSettings[i].setFontStyle(font.getStyle());
                    allPbcViewerSettings[i].setPersistentPurpose(1);
                    status.setSavingPbcViewerSettingsRequested(true);
                }
            }
        }
    }

    public synchronized ArrayList<String> getAllViewerTabUniqueNames() {
        ArrayList<String> tabUniqueNames = new ArrayList<String>();
        if (pbcSettings == null){
            return tabUniqueNames;
        }
        PbcClientSettings aPbcClientSettings = pbcSettings.getPbcClientSettings();
        if (aPbcClientSettings == null){
            return tabUniqueNames;
        }
        PbcViewerSettings[] allPbcViewerSettings = aPbcClientSettings.getPbcViewerSettings();
        if (allPbcViewerSettings == null){
            return tabUniqueNames;
        }else{
            for (int i = 0; i < allPbcViewerSettings.length; i++){
                tabUniqueNames.add(allPbcViewerSettings[i].getViewerTabName());
            }
            return tabUniqueNames;
        }
    }

    public synchronized ArrayList<ViewerColumnIdentifier> getAllViewerColumnIdentifiers(String viewerUniqueTabName, ViewerColumnSorter viewerColumnSorter) {
        ArrayList<ViewerColumnIdentifier> viewerColumnIdentifiers = new ArrayList<ViewerColumnIdentifier>();
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings != null){
            PbcViewerColumnSettings[] columns = aPbcViewerSettings.getPbcViewerColumnSettings();
            if (columns != null){
                if (viewerColumnSorter.equals(ViewerColumnSorter.SortByPosition)){
                    Arrays.sort(columns, new Comparator(){
                        @Override
                        public int compare(Object o1, Object o2) {
                            if (((PbcViewerColumnSettings)o1).getPosition() < ((PbcViewerColumnSettings)o2).getPosition()){
                                return -1;
                            }else if (((PbcViewerColumnSettings)o1).getPosition() > ((PbcViewerColumnSettings)o2).getPosition()){
                                return 1;
                            }else{
                                return 0;
                            }
                        }
                    });
                }
                for (int i = 0; i < columns.length; i++){
                    viewerColumnIdentifiers.add(ViewerColumnIdentifier.convertEnumValueToType(columns[i].getViewerColumnIdentifier()));
                }
            }
            if (viewerColumnSorter.equals(ViewerColumnSorter.SortByIdentifier)){
                Collections.sort(viewerColumnIdentifiers, new Comparator(){
                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((ViewerColumnIdentifier)o1).toString().compareTo(((ViewerColumnIdentifier)o2).toString());
                    }
                });
            }
        }
        return viewerColumnIdentifiers;
    }

    public ArrayList<ViewerColumnIdentifier> createDefaultViewerColumnIdentifiers() {
        ArrayList<ViewerColumnIdentifier> viewerColumnIdentifiers = new ArrayList<ViewerColumnIdentifier>();
        
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.TimeStamp);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.QuoteClass);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.QuoteGroup);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.QuoteCode);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.Period);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.Strike);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.Structure);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.Cross);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.Bid);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.Offer);
        viewerColumnIdentifiers.add(ViewerColumnIdentifier.PbsysPrice);
        
        return viewerColumnIdentifiers;
    }
    
    /**
     * Get settings of a specific viewer panel under the "viewerUniqueTabName"
     * @param viewerUniqueTabName
     * @return - if the "viewerUniqueTabName" cannot be found, NULL will be returned
     */
    private synchronized PbcViewerSettings getPbcViewerSettings(String viewerUniqueTabName) {
        if (DataGlobal.isEmptyNullString(viewerUniqueTabName)){
            return null;
        }
        if (pbcSettings == null){
            return null;
        }
        PbcClientSettings aPbcClientSettings = pbcSettings.getPbcClientSettings();
        if (aPbcClientSettings == null){
            return null;
        }
        PbcViewerSettings[] allPbcViewerSettings = aPbcClientSettings.getPbcViewerSettings();
        if (allPbcViewerSettings == null){
            return null;
        }else{
            PbcViewerSettings aPbcViewerSettings = null;
            for (int i = 0; i < allPbcViewerSettings.length; i++){
                if (allPbcViewerSettings[i].getViewerTabName().equalsIgnoreCase(viewerUniqueTabName)){
                    aPbcViewerSettings = allPbcViewerSettings[i];
                    break;
                }
            }
            return aPbcViewerSettings;
        }
    }
    
    private synchronized PbcViewerColumnSettings getPbcViewerColumnSettings(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, int persistentPurpose){
        PbcViewerColumnSettings aPbcViewerColumnSettings = null;
        if (viewerColumnIdentifier != null){
            PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
            if (aPbcViewerSettings != null){
                if (persistentPurpose != 0){
                    aPbcViewerSettings.setPersistentPurpose(persistentPurpose);
                }
                PbcViewerColumnSettings[] columns = aPbcViewerSettings.getPbcViewerColumnSettings();
                if (columns != null){
                    for (int i = 0; i < columns.length; i++){
                        if (columns[i].getViewerColumnIdentifier().equalsIgnoreCase(viewerColumnIdentifier.toString())){
                            aPbcViewerColumnSettings = columns[i];
                            break;
                        }
                    }
                }
            }
        }
        return aPbcViewerColumnSettings;
    }

    public synchronized boolean isViewerColumnVisible(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 0);
        if (column == null){
            return false;
        }else{
            return column.isVisible();
        }
    }

    public synchronized String getViewerColumnHeaderValue(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 0);
        if (column == null){
            return "";
        }else{
            if (ViewerColumnIdentifier.DDelta.equals(viewerColumnIdentifier)){
                return "True Delta";
            }
            if (ViewerColumnIdentifier.DGamma.equals(viewerColumnIdentifier)){
                return "True Gamma";
            }
            return column.getHeaderValue();
        }
    }

    public synchronized int getViewerColumnWidth(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 0);
        if (column == null){
            return 75;
        }else{
            return column.getWidth();
        }
    }

    public synchronized int getViewerColumnPosition(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 0);
        if (column == null){
            return -1;
        }else{
            return column.getPosition();
        }
    }

    public synchronized SortOrder getViewerColumnSortOrder(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 0);
        if (column == null){
            return SortOrder.UNSORTED;
        }else{
            if (DataGlobal.isEmptyNullString(column.getSortOrder())){
                return SortOrder.UNSORTED;
            }else if (SortOrder.ASCENDING.toString().equalsIgnoreCase(column.getSortOrder())){
                return SortOrder.ASCENDING;
            }else if (SortOrder.DESCENDING.toString().equalsIgnoreCase(column.getSortOrder())){
                return SortOrder.DESCENDING;
            }else{
                return SortOrder.UNSORTED;
            }
        }
    }

    public synchronized boolean isViewerColumnResizable(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 0);
        if (column == null){
            return true;
        }else{
            return column.isResizable();
        }
    }

    public synchronized void setViewerColumnVisible(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, boolean value) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 1);
        if (column != null){
            column.setVisible(value);
            status.setSavingPbcViewerSettingsRequested(true);
        }
    }

    public synchronized void setViewerColumnWidth(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, int width) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 1);
        if (column != null){
            column.setWidth(width);
            status.setSavingPbcViewerSettingsRequested(true);
        }
    }

    public synchronized void setViewerColumnPosition(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, int position) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 1);
        if (column != null){
            column.setPosition(position);
            status.setSavingPbcViewerSettingsRequested(true);
        }
    }

    public synchronized void setViewerColumnSortOrder(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, SortOrder sortOrder) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 1);
        if (column != null){
            column.setSortOrder(sortOrder.toString());
            status.setSavingPbcViewerSettingsRequested(true);
        }
    }

    public synchronized void setViewerColumnHeaderValue(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, String headerValue) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 1);
        if (column != null){
            column.setHeaderValue(headerValue);
            status.setSavingPbcViewerSettingsRequested(true);
        }
    }

    public synchronized void setViewerColumnResizable(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, boolean value) {
        PbcViewerColumnSettings column = getPbcViewerColumnSettings(viewerUniqueTabName, viewerColumnIdentifier, 1);
        if (column != null){
            column.setResizable(value);
            status.setSavingPbcViewerSettingsRequested(true);
        }
    }
    
    public synchronized void updatePbcBuddyListSettingsVisibility(IBuddyListPanel targetBuddyListPanel, boolean visible) {
        if (targetBuddyListPanel == null){
            return;
        }
        String distListName = targetBuddyListPanel.getDistListName();
        if (DataGlobal.isEmptyNullString(distListName)){
            return;
        }
        PbcBuddyListSettings[] existingPbcBuddyListSettingsArray = pbcSettings.getPbcBuddyListSettings();
        if ((existingPbcBuddyListSettingsArray != null) && (existingPbcBuddyListSettingsArray.length > 0)){
            ArrayList<PbcBuddyListSettings> aNewPbcBuddyListSettingsList = new ArrayList<PbcBuddyListSettings>();
            PbcBuddyListSettings targetPbcBuddyListSettings = null;
            for (PbcBuddyListSettings existingPbcBuddyListSettings : existingPbcBuddyListSettingsArray){
                if (existingPbcBuddyListSettings != null){
                    if (distListName.equalsIgnoreCase(existingPbcBuddyListSettings.getBuddyListName())){
                        targetPbcBuddyListSettings = existingPbcBuddyListSettings;
                    }
                    aNewPbcBuddyListSettingsList.add(existingPbcBuddyListSettings);
                }
            }//for
            //a new list....
            if (targetPbcBuddyListSettings == null){
                targetPbcBuddyListSettings = targetBuddyListPanel.constructPbcBuddyListSettings();
                aNewPbcBuddyListSettingsList.add(targetPbcBuddyListSettings);
            }
            //update...
            if (targetPbcBuddyListSettings != null){
                targetPbcBuddyListSettings.setVisible(visible);
                targetPbcBuddyListSettings.setPersistentRequired(true);
                pbcSettings.setPbcBuddyListSettings(aNewPbcBuddyListSettingsList.toArray(new PbcBuddyListSettings[0]));
                status.setSavingBuddyListRequested(true);
            }
        }
    }

    /**
     * 
     * @param targetPbcBuddyListSettings which is used to insert or update existing ones in the settings
     * @param changed - if it requires change settings status so that it push it to the server for persistency 
     */
    public synchronized void updatePbcBuddyListSettings(PbcBuddyListSettings targetPbcBuddyListSettings, boolean changed) {
        if (targetPbcBuddyListSettings == null){
            return;
        }
        String distListName = targetPbcBuddyListSettings.getBuddyListName();
        if (DataGlobal.isEmptyNullString(distListName)){
            return;
        }
        PbcBuddyListSettings[] existingPbcBuddyListSettingsArray = pbcSettings.getPbcBuddyListSettings();
        if ((existingPbcBuddyListSettingsArray == null) || (existingPbcBuddyListSettingsArray.length == 0)){
            PbcBuddyListSettings[] aNewPbcBuddyListSettingsArray = new PbcBuddyListSettings[1];
            aNewPbcBuddyListSettingsArray[0] = targetPbcBuddyListSettings;
            pbcSettings.setPbcBuddyListSettings(aNewPbcBuddyListSettingsArray);
            if (changed){
                targetPbcBuddyListSettings.setPersistentRequired(true);
                status.setSavingBuddyListRequested(true);
            }
        }else{
            boolean isNew = true;
            ArrayList<PbcBuddyListSettings> aNewPbcBuddyListSettingsList = new ArrayList<PbcBuddyListSettings>();
            for (PbcBuddyListSettings existingPbcBuddyListSettings : existingPbcBuddyListSettingsArray){
                if (existingPbcBuddyListSettings != null){
                    if (distListName.equalsIgnoreCase(existingPbcBuddyListSettings.getBuddyListName())){
                        aNewPbcBuddyListSettingsList.add(targetPbcBuddyListSettings);
                        isNew = false;
                    }else{
                        aNewPbcBuddyListSettingsList.add(existingPbcBuddyListSettings);
                    }
                }
            }//for
            //a new list....
            if (isNew){
                aNewPbcBuddyListSettingsList.add(targetPbcBuddyListSettings);
            }
            //update...
            pbcSettings.setPbcBuddyListSettings(aNewPbcBuddyListSettingsList.toArray(new PbcBuddyListSettings[0]));
            if (changed){
                targetPbcBuddyListSettings.setPersistentRequired(true);
                status.setSavingBuddyListRequested(true);
            }
        }
    }
    
    /**
     * Add one specific filter property into the settings of uniqueFilterTabName
     * @param uniqueFilterTabName
     * @param aPbcFilterPropertySettings 
     */
    public synchronized void addPbcFilterPropertySettings(String viewerUniqueTabName, PbcFilterPropertySettings aPbcFilterPropertySettings) {
        PbcViewerSettings aPbcViewerSettings = getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            return;
        }
        if (aPbcFilterPropertySettings == null){
            return;
        }
        PbcFilterPropertySettings[] pbcFilterPropertySettingsArray = aPbcViewerSettings.getPbcFilterPropertySettings();
        ArrayList<PbcFilterPropertySettings> aPbcFilterPropertySettingsList = new ArrayList<PbcFilterPropertySettings>();
        PbcFilterPropertySettings existingPbcFilterPropertySettings = null;
        if (pbcFilterPropertySettingsArray != null){
            for (PbcFilterPropertySettings aPbcFilterPropertySettingsArrayItem : pbcFilterPropertySettingsArray){
                if (aPbcFilterPropertySettingsArrayItem.getPropertyKey().equalsIgnoreCase(aPbcFilterPropertySettings.getPropertyKey())){
                    existingPbcFilterPropertySettings = aPbcFilterPropertySettingsArrayItem;
                    break;
                }
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettingsArrayItem);
            }
        }
        if (existingPbcFilterPropertySettings == null){
            //add a new filter
            aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
            aPbcViewerSettings.setPbcFilterPropertySettings(aPbcFilterPropertySettingsList.toArray(new PbcFilterPropertySettings[0]));
            aPbcViewerSettings.setPersistentPurpose(1);
            status.setSavingPbcViewerSettingsRequested(true);
        }else{
            //update existing value...
            if (!existingPbcFilterPropertySettings.getPropertyValue().equalsIgnoreCase(aPbcFilterPropertySettings.getPropertyValue())){
                existingPbcFilterPropertySettings.setPropertyValue(DataGlobal.denullize(aPbcFilterPropertySettings.getPropertyValue()));
                aPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
    }
    
    private synchronized PbcViewerColumnSettings[] createDefaultPbcViewerColumnSettings(ViewerTableType viewerTableType){
        ArrayList<PbcViewerColumnSettings> pbcViewerColumnSettings = new ArrayList<PbcViewerColumnSettings>();
        
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Bid));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.BuySell));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Cross));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.DDelta));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.DGamma));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Delta));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.QuoteClass));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.QuoteGroup));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.QuoteCode));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.QuoteSource));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Gamma));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Last));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Offer));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.PbsysPrice));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Period));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.QuoteMessage));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.RemoteBrokerHouse));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.RowNumber));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Strike));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Structure));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Swap01));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Swap02));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Theta));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.TimeStamp));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.UnderlierType));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Vega));
        pbcViewerColumnSettings.add(createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier.Volatility));
        
        return pbcViewerColumnSettings.toArray(new PbcViewerColumnSettings[0]);
    }

    private synchronized PbcViewerColumnSettings createDefaultPbcViewerColumnSettingsHelper(ViewerColumnIdentifier columnIdentifier) {
        int width = 75;
        boolean visible = false;
        int position = -1;
        switch (columnIdentifier){
            case TimeStamp:
                width = 75;
                visible = true;
                position = 1;
                break;
            case RemoteBrokerHouse:
                width = 90;
                visible = true;
                position = 2;
                break;
            case QuoteCode:
                width = 40;
                visible = true;
                position = 3;
                break;
            case Period:
                width = 80;
                visible = true;
                position = 4;
                break;
            case Strike:
                width = 100;
                visible = true;
                position = 5;
                break;
            case Structure:
                width = 100;
                visible = true;
                position = 6;
                break;
            case Cross:
                width = 100;
                visible = true;
                position = 7;
                break;
            case Bid:
                width = 50;
                visible = true;
                position = 8;
                break;
            case Offer:
                width = 50;
                visible = true;
                position = 9;
                break;
            case PbsysPrice:
                width = 50;
                visible = true;
                position = 10;
                break;
            case Volatility:
                width = 50;
                visible = true;
                position = 11;
                break;
            case Swap01:
                width = 50;
                visible = true;
                position = 12;
                break;
            case QuoteMessage:
                width = 325;
                visible = true;
                position = 13;
                break;
            case DDelta:
                width = 55;
                visible = true;
                position = 14;
                break;
            case Last:
                width = 55;
                visible = true;
                position = 15;
                break;
            case QuoteClass:
                width = 45;
                visible = true;
                position = 16;
                break;
            case QuoteGroup:
                width = 45;
                visible = true;
                position = 17;
                break;
            case QuoteSource:
                width = 55;
                visible = true;
                position = 18;
                break;
            case BuySell:
                width = 75;
                visible = false;
                position = 19;
                break;
            case Delta:
                width = 55;
                visible = false;
                position = 20;
                break;
            case DGamma:
                width = 55;
                visible = false;
                position = 21;
                break;
            case Gamma:
                width = 55;
                visible = false;
                position = 22;
                break;
            case Vega:
                width = 55;
                visible = false;
                position = 23;
                break;
            case Theta:
                width = 55;
                visible = false;
                position = 24;
                break;
            case Swap02:
                width = 50;
                visible = false;
                position = 25;
                break;
            case UnderlierType:
                width = 75;
                visible = false;
                position = 26;
                break;
            case RowNumber:
                width = 75;
                visible = false;
                position = 27;
                break;
            default:
        }
        PbcViewerColumnSettings aPbcViewerColumnSettings = new PbcViewerColumnSettings();
        aPbcViewerColumnSettings.setHeaderValue(columnIdentifier.toString());
        aPbcViewerColumnSettings.setPosition(position);
        aPbcViewerColumnSettings.setResizable(true);
        aPbcViewerColumnSettings.setSortOrder(SortOrder.UNSORTED.toString());
        aPbcViewerColumnSettings.setViewerColumnIdentifier(columnIdentifier.toString());
        aPbcViewerColumnSettings.setVisible(visible);
        aPbcViewerColumnSettings.setWidth(width);
        
        return aPbcViewerColumnSettings;
    }
    
    public synchronized boolean getViewerFilterBooleanValue(String uniqueFilterTabName, FilterPropertyKey filterPropertyKey) {
        boolean result = false;
        PbcViewerSettings aPbcViewerSettings = this.getPbcViewerSettings(uniqueFilterTabName);
        if ((aPbcViewerSettings != null) && (filterPropertyKey != null)){
            PbcFilterPropertySettings[] pbcFilterPropertySettings = aPbcViewerSettings.getPbcFilterPropertySettings();
            if ((pbcFilterPropertySettings == null) || pbcFilterPropertySettings.length == 0){
                result = false;
            }else{
                for (int i = 0; i < pbcFilterPropertySettings.length; i++){
                    if (pbcFilterPropertySettings[i].getPropertyKey().equalsIgnoreCase((filterPropertyKey.toString()))){
                        if (pbcFilterPropertySettings[i].getPropertyValue().equalsIgnoreCase(FilterPropertyValue.Boolean_True_value.toString())){
                            result = true;
                        }else{
                            result = false;
                        }
                        break;
                    }
                }//for
            }
        }
        return result;
    }

    private synchronized void addNewPbcViewerSettings(PbcClientSettings clientSettings, PbcViewerSettings aNewPbcViewerSettings){
        if (clientSettings == null){
            return;
        }
        if ((aNewPbcViewerSettings == null) || (DataGlobal.isEmptyNullString(aNewPbcViewerSettings.getViewerTabName()))){
            return;
        }
        List<PbcViewerSettings> aNewPbcViewerSettingsList = new ArrayList<PbcViewerSettings>();
        PbcViewerSettings[] pbcViewerSettingsArray = clientSettings.getPbcViewerSettings();
        if (pbcViewerSettingsArray == null){
            aNewPbcViewerSettingsList.add(aNewPbcViewerSettings);
        }else{
            boolean isNew = true;
            for (PbcViewerSettings aPbcViewerSettings : pbcViewerSettingsArray){
                if (aNewPbcViewerSettings.getViewerTabName().equalsIgnoreCase(aPbcViewerSettings.getViewerTabName())){
                    aNewPbcViewerSettingsList.add(aNewPbcViewerSettings);
                    isNew = false;
                }else{
                    aNewPbcViewerSettingsList.add(aPbcViewerSettings);
                }
            }
            if (isNew){
                aNewPbcViewerSettingsList.add(aNewPbcViewerSettings);
                aNewPbcViewerSettings.setPersistentPurpose(1);
                status.setSavingPbcViewerSettingsRequested(true);
            }
        }
        clientSettings.setPbcViewerSettings(aNewPbcViewerSettingsList.toArray(new PbcViewerSettings[0]));
    }

    private synchronized PbcViewerSettings createNewPbcViewerSettings(ViewerTableType viewerTableType, String viewerUniqueTabName) {
        boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(runtime.getKernel().getPointBoxLoginUser().getIMUniqueName());
        PbcViewerSettings aPbcViewerSettings = null;
        if (isViewerSettingsForAllViewers){
            aPbcViewerSettings = createPbcViewerSettingsByGenericSettings(viewerUniqueTabName);
        }
        if (aPbcViewerSettings == null){
            aPbcViewerSettings = new PbcViewerSettings();
            aPbcViewerSettings.setViewerTabName(viewerUniqueTabName);
            aPbcViewerSettings.setBpaBackgroundRgb(getBpa_BgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setBpaForegroundRgb(getBpa_FgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setFontFamily(SwingGlobal.getLabelFont().getFamily());
            aPbcViewerSettings.setFontSize(SwingGlobal.getLabelFont().getSize());
            aPbcViewerSettings.setFontStyle(SwingGlobal.getLabelFont().getStyle());
            aPbcViewerSettings.setGeneralColor(getViewerGeneralColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setLatestRowBackgroundRgb(getLatestRowBackground(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setLatestRowForegroundRgb(getLatestRowForeground(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setMsgBackgroundRgb(getMsgBgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setMsgForegroundRgb(getMsgFgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setOutgoingBackgroundRgb(getOutgoingBackground(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setOutgoingForegroundRgb(getOutgoingForeground(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setPaBackgroundRgb(getPa_BgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setPaForegroundRgb(getPa_FgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setPbBackgroundRgb(getPb_BgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setPbForegroundRgb(getPb_FgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setPbimQtBackgroundRgb(getPbimQtBgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setPbimQtForegroundRgb(getPbimQtFgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setQtBackgroundRgb(getQtBgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setQtForegroundRgb(getQtFgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setSelectedRowBackgroundRgb(getSelectedRowBackground(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setSelectedRowForegroundRgb(getSelectedRowForeground(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setSkippedQtBackgroundRgb(getSkippedQtBgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setSkippedQtForegroundRgb(getSkippedQtFgColor(viewerUniqueTabName).getRGB());
            aPbcViewerSettings.setPbcViewerColumnSettings(createDefaultPbcViewerColumnSettings(viewerTableType));
        }
        return aPbcViewerSettings;
    }

    /**
     * Request PointBoxLocal server to check out whether or not current online connections are still online.
     * @return 
     */
    private synchronized List<String> requestToCheckConnectionOfflineStatus() {
        IGatewayConnectorBuddy loginUser;
        PointBoxAccountID accountID = this.getPointBoxAccountID();
        PointBoxConnectorID aPointBoxConnectorID;
        ArrayList<PointBoxConnectorID> aPointBoxConnectorIDList = new ArrayList<PointBoxConnectorID>();
        PbcBuddyListSettings[] aPbcBuddyListSettingsArray = pbcSettings.getPbcBuddyListSettings();
        for (PbcBuddyListSettings aPbcBuddyListSettings : aPbcBuddyListSettingsArray){
            if (PbcBuddyListType.RegularBuddyList.toString().equalsIgnoreCase(aPbcBuddyListSettings.getBuddyListType())){
                aPointBoxConnectorID = aPbcBuddyListSettings.getConnectorOwner();
                loginUser = GatewayBuddyListFactory.getLoginUserInstance(aPointBoxConnectorID.getLoginName(), 
                                                                         GatewayServerType.convertToType(aPointBoxConnectorID.getGatewayServerType()));
                if ((loginUser != null) && (BuddyStatus.Online.equals(loginUser.getBuddyStatus()))){
                    aPointBoxConnectorIDList.add(aPointBoxConnectorID);
                }
            }
        }
        return getKernel().requestToCheckConnectionOfflineStatus(accountID, aPointBoxConnectorIDList);
    }
    
    /**
     * This method will be possibly (if settings was changed) called  periodically 
     * (every 3 minutes).
     */
    private synchronized void requestToSavePointBoxConsoleSettings() {
        if (pbcSettings == null){
            return;
        }
        PointBoxAccountID aPointBoxAccountID = pbcSettings.getPointBoxAccountID();
        PbcClientSettings aPbcClientSettings = pbcSettings.getPbcClientSettings();
        if (status.isSavingPbcClientSettingsRequested()){
            getKernel().requestToSavePbcClientSettingsRecord(aPointBoxAccountID, aPbcClientSettings);
            status.setSavingPbcClientSettingsRequested(false);
        }
        
        /**
         * Clean up DIST/CONF/DIST here since they are not supported
         */
        if (pbcSettings.cleanupLegacyPbcBuddyListSettings()){
            status.setSavingBuddyListRequested(true);
        }
        
        if (status.isSavingBuddyListRequested()){
            getKernel().requestToSavePbcBuddyListSettingsCollection(aPointBoxAccountID, 
                                                                    pbcSettings.getPbcBuddyListSettings());
            status.setSavingBuddyListRequested(false);
        }
        
        if (status.isSavingPbcViewerSettingsRequested()){
            getKernel().requestToSavePbcViewerSettingsCollection(aPointBoxAccountID, aPbcClientSettings);
            status.setSavingPbcViewerSettingsRequested(false);
        }
        
        if (status.isSavingPbcFileSettingsRequested()){
            getKernel().requestToSavePbcFileSettings(aPointBoxAccountID, aPbcClientSettings);
            status.setSavingPbcFileSettingsRequested(false);
        }
        
        if (status.isSavingPbcWindowsSettingsRequested()){
            getKernel().requestToSavePbcWindowsSettings(aPointBoxAccountID, aPbcClientSettings);
            status.setSavingPbcWindowsSettingsRequested(false);
        }
        
        if (status.isSavingBuddyProfileRequested()){
            getKernel().requestToSaveBuddyProfiles(aPointBoxAccountID, pbcSettings);
            status.setSavingBuddyProfileRequested(false);
        }
    }
    
    /**
     * This method is possibly time-consuming, which is possibly invoked as IPbcRuntime. 
     * For example, during PBC unloading, this will be called if settings was changed
     * unload() being invoked
     */
    public synchronized void savePbcSettingsSynchronously() {
        this.requestToSavePointBoxConsoleSettings();
    }

    public synchronized void addPbcViewerSettings(String viewerUniqueTabName) {
        PbcViewerSettings aPbcViewerSettings = this.getPbcViewerSettings(viewerUniqueTabName);
        if (aPbcViewerSettings == null){
            PbcClientSettings clientSettings = pbcSettings.getPbcClientSettings();
            if (clientSettings != null){
//                this.requestToRemovePbcViewerSettings(clientSettings, viewerUniqueTabName);
                this.addNewPbcViewerSettings(clientSettings, createNewPbcViewerSettings(ViewerTableType.convertEnumValueToType(viewerUniqueTabName), 
                                                                                        viewerUniqueTabName));
            }
        }
    }

    public synchronized void renamePbcViewerUniqueName(String oldTabName, String newTabName, ArrayList<IViewerFilterCriteria> newCriteria) {
        if (DataGlobal.isEmptyNullString(newTabName)){
            return;
        }
        PbcClientSettings clientSettings = pbcSettings.getPbcClientSettings();
        if (clientSettings == null){
            return;
        }
        this.requestToRemovePbcViewerSettings(clientSettings, oldTabName);
        this.addNewPbcViewerSettings(clientSettings, createNewPbcViewerSettings(ViewerTableType.convertEnumValueToType(newTabName), 
                                                                                newTabName));
        List<PbcFilterPropertySettings> aPbcFilterPropertySettingsList;
        for (IViewerFilterCriteria aCriteria : newCriteria){
            aPbcFilterPropertySettingsList = aCriteria.constructPbcFilterPropertySettingsList();
            for (PbcFilterPropertySettings aPbcFilterPropertySettings : aPbcFilterPropertySettingsList){
                addPbcFilterPropertySettings(newTabName, aPbcFilterPropertySettings);
            }
        }
    }

    public synchronized void resetViewerDefaultSettings(String viewerUniqueTabName) {
        PbcClientSettings clientSettings = pbcSettings.getPbcClientSettings();
        this.requestToRemovePbcViewerSettings(clientSettings, viewerUniqueTabName);
        this.addNewPbcViewerSettings(clientSettings, createNewPbcViewerSettings(ViewerTableType.convertEnumValueToType(viewerUniqueTabName), 
                                                                                viewerUniqueTabName));
    }
    
    /**
     * Remove the settings of uniqueFilterTabName
     * @param uniqueFilterTabName 
     */
    public synchronized void removePbcViewerSettings(String viewerUniqueTabName){
        if (DataGlobal.isEmptyNullString(viewerUniqueTabName)){
            return;
        }
        if (pbcSettings == null){
            return;
        }
        PbcClientSettings aPbcClientSettings = pbcSettings.getPbcClientSettings();
        if (aPbcClientSettings == null){
            return;
        }
        requestToRemovePbcViewerSettings(aPbcClientSettings, viewerUniqueTabName);
    }
    
    private synchronized void requestToRemovePbcViewerSettings(PbcClientSettings clientSettings, String viewerUniqueTabName) {
        PointBoxAccountID aPointBoxAccountID = getPointBoxAccountID();
        if (aPointBoxAccountID == null){
            return;
        }
        if (DataGlobal.isEmptyNullString(viewerUniqueTabName)){
            return;
        }
        if (clientSettings == null){
            return;
        }
        final PbcViewerSettings[] pbcViewerSettings = clientSettings.getPbcViewerSettings();
        if ((pbcViewerSettings == null) || (pbcViewerSettings.length == 0)){
            return;
        }
        ArrayList<PbcViewerSettings> aDeletingPbcViewerSettingsList = new ArrayList<PbcViewerSettings>();
        //prepare a copy of data for deletion
        ArrayList<PbcViewerSettings> newPbcViewerSettings = new ArrayList<PbcViewerSettings>();
        for (PbcViewerSettings aPbcViewerSettings : pbcViewerSettings){
            //avoid adding viewerUniqueTabName
            if (aPbcViewerSettings.getViewerTabName().equalsIgnoreCase(viewerUniqueTabName)){
                aDeletingPbcViewerSettingsList.add(aPbcViewerSettings);
            }else{
                //keep ones that exists
                newPbcViewerSettings.add(aPbcViewerSettings);
            }
        }
        //delete it
        getKernel().requestToRemovePbcViewerSettingsCollection(aPointBoxAccountID, aDeletingPbcViewerSettingsList);
        //update in-memory settings
        if (newPbcViewerSettings.isEmpty()){
            clientSettings.setPbcViewerSettings(null);
        }else{
            clientSettings.setPbcViewerSettings(newPbcViewerSettings.toArray(new PbcViewerSettings[0]));
        }
    }

    private PbcViewerSettings createPbcViewerSettingsByGenericSettings(String viewerUniqueTabName) {
        PbcViewerSettings genericSettings = this.getPbcViewerSettings(ViewerTableType.ALL_QUOTES.toString());
        PbcViewerSettings aPbcViewerSettings = null;
        if (genericSettings != null){
            aPbcViewerSettings = new PbcViewerSettings();
            aPbcViewerSettings.setViewerTabName(viewerUniqueTabName);
            aPbcViewerSettings.setBpaBackgroundRgb(genericSettings.getBpaBackgroundRgb());
            aPbcViewerSettings.setBpaForegroundRgb(genericSettings.getBpaForegroundRgb());
            aPbcViewerSettings.setFontFamily(genericSettings.getFontFamily());
            aPbcViewerSettings.setFontSize(genericSettings.getFontSize());
            aPbcViewerSettings.setFontStyle(genericSettings.getFontStyle());
            aPbcViewerSettings.setGeneralColor(genericSettings.getGeneralColor());
            aPbcViewerSettings.setLatestRowBackgroundRgb(genericSettings.getLatestRowBackgroundRgb());
            aPbcViewerSettings.setLatestRowForegroundRgb(genericSettings.getLatestRowForegroundRgb());
            aPbcViewerSettings.setMsgBackgroundRgb(genericSettings.getMsgBackgroundRgb());
            aPbcViewerSettings.setMsgForegroundRgb(genericSettings.getMsgForegroundRgb());
            aPbcViewerSettings.setOutgoingBackgroundRgb(genericSettings.getOutgoingBackgroundRgb());
            aPbcViewerSettings.setOutgoingForegroundRgb(genericSettings.getOutgoingForegroundRgb());
            aPbcViewerSettings.setPaBackgroundRgb(genericSettings.getPaBackgroundRgb());
            aPbcViewerSettings.setPaForegroundRgb(genericSettings.getPaForegroundRgb());
            aPbcViewerSettings.setPbBackgroundRgb(genericSettings.getPbBackgroundRgb());
            aPbcViewerSettings.setPbForegroundRgb(genericSettings.getPbForegroundRgb());
            aPbcViewerSettings.setPbimQtBackgroundRgb(genericSettings.getPbimQtBackgroundRgb());
            aPbcViewerSettings.setPbimQtForegroundRgb(genericSettings.getPbimQtForegroundRgb());
            aPbcViewerSettings.setQtBackgroundRgb(genericSettings.getQtBackgroundRgb());
            aPbcViewerSettings.setQtForegroundRgb(genericSettings.getQtForegroundRgb());
            aPbcViewerSettings.setSelectedRowBackgroundRgb(genericSettings.getSelectedRowBackgroundRgb());
            aPbcViewerSettings.setSelectedRowForegroundRgb(genericSettings.getSelectedRowForegroundRgb());
            aPbcViewerSettings.setSkippedQtBackgroundRgb(genericSettings.getSkippedQtBackgroundRgb());
            aPbcViewerSettings.setSkippedQtForegroundRgb(genericSettings.getSkippedQtForegroundRgb());
            aPbcViewerSettings.setPbcViewerColumnSettings(createPbcViewerColumnSettingsByCopy(genericSettings.getPbcViewerColumnSettings()));
        }
        return aPbcViewerSettings;
    }

    private PbcViewerColumnSettings[] createPbcViewerColumnSettingsByCopy(PbcViewerColumnSettings[] genericPbcViewerColumnSettingsArray) {
        if (genericPbcViewerColumnSettingsArray == null){
            return null;
        }else{
            
            List<PbcViewerColumnSettings> aPbcViewerColumnSettingsList = new ArrayList<PbcViewerColumnSettings>();
            PbcViewerColumnSettings aPbcViewerColumnSettings;
            for (PbcViewerColumnSettings genericColumnSettings : genericPbcViewerColumnSettingsArray){
                aPbcViewerColumnSettings = new PbcViewerColumnSettings();
                aPbcViewerColumnSettings.setHeaderValue(genericColumnSettings.getHeaderValue());
                aPbcViewerColumnSettings.setPosition(genericColumnSettings.getPosition());
                aPbcViewerColumnSettings.setResizable(genericColumnSettings.isResizable());
                aPbcViewerColumnSettings.setSortOrder(genericColumnSettings.getSortOrder());
                aPbcViewerColumnSettings.setViewerColumnIdentifier(genericColumnSettings.getViewerColumnIdentifier());
                aPbcViewerColumnSettings.setVisible(genericColumnSettings.isVisible());
                aPbcViewerColumnSettings.setWidth(genericColumnSettings.getWidth());
                aPbcViewerColumnSettingsList.add(aPbcViewerColumnSettings);
            }
            return aPbcViewerColumnSettingsList.toArray(new PbcViewerColumnSettings[0]);
        }
    }
    
    private class PbcFileSettingsProxy extends PointBoxPricingCurveSettings implements IPbconsoleAccessorySettings {
        
        /**
         * KEY = Code-based curve file name
         */
        private final HashMap<String, PbcFileSettings> pbcFileSettingsStorage;
        PbcFileSettingsProxy(HashMap<String, PbcPricingModel> pbcPricingModelMap) {
            super(pbcPricingModelMap);
            this.pbcFileSettingsStorage = new HashMap<String, PbcFileSettings>();
        }
        /**
         * 
         * @param fileSettings
         * @return changed or not
         */
        private synchronized boolean insertPbcFileSettings(PbcFileSettings fileSettings) {
            if ((fileSettings == null) || (DataGlobal.isEmptyNullString(fileSettings.getFileLocation()))){
                return false;
            }
            boolean result = false;
            if (!pbcFileSettingsStorage.containsKey(fileSettings.getFileUniqueName())){
                pbcFileSettingsStorage.put(fileSettings.getFileUniqueName(), fileSettings);
                result = true;
            }
            return result;
        }

        @Override
        public synchronized URL getPointBoxConsoleAgreementURL() {
            return getClass().getResource("resources/agreement.txt");
        }

        @Override
        public synchronized IFileInfoRecord getNymexFtpAddressRecord() {
            return getFileInfoRecord(PointBoxLegacyFileType.nymexFtpAddress.name());
        }

        @Override
        public synchronized IFileInfoRecord getFileInfoRecord(String codeBasedFileName) {
            return new FileInfoRecordProxy(pbcFileSettingsStorage.get(codeBasedFileName));
        }

        @Override
        public synchronized ArrayList<IFileInfoRecord> getPbconsoleFileInfoRecordList() {
            ArrayList<IFileInfoRecord> fileRecords = new ArrayList<IFileInfoRecord>();
            Set<String> keys = pbcFileSettingsStorage.keySet();
            Iterator<String> itr = keys.iterator();
            while (itr.hasNext()){
                fileRecords.add(new FileInfoRecordProxy(pbcFileSettingsStorage.get(itr.next())));
            }
            return fileRecords;
        }

        @Override
        public synchronized ArrayList<String> getPbconsoleSettingsFileTypeList() {        
            ArrayList<String> ids = new ArrayList<String>();
            Set<String> keys = pbcFileSettingsStorage.keySet();
            Iterator<String> itr = keys.iterator();
            while (itr.hasNext()){
                ids.add(itr.next());
            }
            return ids;
        }

        @Override
        public synchronized void setFileInfoRecord(String ownerUnqiueName, String codeBasedFileName, String filePath) {
            filePath = DataGlobal.denullize(filePath);
            if (pbcFileSettingsStorage.containsKey(codeBasedFileName)){
                if (!filePath.equalsIgnoreCase(pbcFileSettingsStorage.get(codeBasedFileName).getFileLocation())){
                    pbcFileSettingsStorage.get(codeBasedFileName.toString()).setFileLocation(filePath);
//                    getLocalPropertiesInstance().setProperty(fileType.toString(), filePath);
                    initializeCurves(codeBasedFileName, filePath);
                }
            }else{
                addNewPbcFileSettings(pbcSettings.getPbcClientSettings(), codeBasedFileName, filePath);
            }
        }
    }
    
    /**
     * A wrapper of PbcFileSettings which is in the PointBoxConsoleSettings
     */
    private class FileInfoRecordProxy implements IFileInfoRecord{

        private PbcFileSettings fileSettings;
        
        private FileInfoRecordProxy(PbcFileSettings fileSettings) {
            this.fileSettings = fileSettings;
        }

        @Override
        public synchronized String getFilePath() {
            if (fileSettings == null){
                return "";
            }
            return fileSettings.getFileLocation();
        }

        @Override
        public synchronized String getFileUniqueName() {
            if (fileSettings == null){
                return "";
            }
            return fileSettings.getFileUniqueName();
        }

        @Override
        public synchronized void setFilePath(String filePath) {
            if (fileSettings == null){
                return;
            }
            filePath = DataGlobal.denullize(filePath);
            if (!filePath.equalsIgnoreCase(fileSettings.getFileLocation())){
                fileSettings.setFileLocation(filePath);
                initializeCurves(fileSettings.getFileUniqueName(), 
                                 filePath);
                status.setSavingPbcFileSettingsRequested(true);
            }
        }

        /**
         * Read-only
         * @param fileUniqueName 
         */
        @Override
        public synchronized void setFileUniqueName(String fileUniqueName) {
        }

        @Override
        public synchronized String getOwnerUniqueName() {
            if (pbcSettings == null){
                return null;
            }else{
                return pbcSettings.getPointBoxAccountID().getLoginName();
            }
        }

        /**
         * Read-only
         * @param ownerName 
         */
        @Override
        public synchronized void setOwnerUniqueName(String ownerName) {
        }
    
    }
}
