/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime;

import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyKey;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier;
import com.eclipsemarkets.pbc.runtime.settings.PersistentPbcSettings;
import com.eclipsemarkets.pbc.face.PbcArchiveFormat;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleAudioSettings;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleAccessorySettings;
import com.eclipsemarkets.runtime.IPointBoxParserSettings;
import com.eclipsemarkets.pbc.runtime.settings.IPointBoxTalkerSettings;
import com.eclipsemarkets.runtime.IPointBoxPricingSettings;
import com.eclipsemarkets.runtime.IPointBoxPricingEngineSettings;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.PbcComponent;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.runtime.PointBoxConsoleSettingsInitializedEvent;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.exceptions.PbcRuntimeException;
import com.eclipsemarkets.pbc.face.PbcArchiveStatus;
import com.eclipsemarkets.pbc.face.PbcFaceComponentType;
import com.eclipsemarkets.pbc.face.talker.IBuddyListPanel;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterCriteria;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.settings.IPbcSettings;
import com.eclipsemarkets.pbc.runtime.settings.PbcSettingsFactory;
import com.eclipsemarkets.pbc.runtime.settings.PbcSettingsType;
import com.eclipsemarkets.pbc.runtime.settings.ViewerColumnSorter;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.IGroupMembersRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.IGroupRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.PbconsoleRecordFactory;
import com.eclipsemarkets.release.PointBoxConfig;
import com.eclipsemarkets.web.PointBoxAccountID;
import com.eclipsemarkets.web.PointBoxConnectorID;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import com.eclipsemarkets.web.pbc.PbcSystemFrameLayout;
import com.eclipsemarkets.web.pbc.PbcSystemFrameStyle;
import com.eclipsemarkets.web.pbc.PointBoxConsoleSettings;
import com.eclipsemarkets.web.pbc.PricingCurveFileSettings;
import com.eclipsemarkets.web.pbc.talker.BuddyProfile;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SortOrder;

/**
 * PbcRuntime
 * <P>
 * Todo: eventually, settingStorage should only hold non-persistent information 
 * and its internal pbcSettings only hold persistent information
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 25, 2011 at 9:13:03 AM
 */
class PbcRuntime extends PbcComponent implements IPbcRuntime {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbcRuntime.class.getName());
    }
    /**
     * Settings for WelcomeLoginFrame
     * todo: eventually, this settings should be replaced by pointBoxConsoleSettings
     */
    private final EnumMap<PbcSettingsType, IPbcSettings> settingStorage;
    /**
     * Hold all the settings of PointBoxConsole. This internal settings has to be synchronized by "this"
     */
    private final PersistentPbcSettings pbcSettings;

    private HashMap<String, PbcPricingModel> pbcPricingModelMap = null;
    
    PbcRuntime(IPbcKernel kernel) {
        super(kernel);
        
        kernel.updateSplashScreen("Register " + getKernel().getSoftwareName() + "'s runtime component...", Level.INFO, 100);
        settingStorage = new EnumMap<PbcSettingsType, IPbcSettings>(PbcSettingsType.class);
        pbcSettings = new PersistentPbcSettings(this);
        initializeSettings();
    }

    @Override
    public PbcPricingModel getPbcPricingModel(PointBoxQuoteCode code) {
        if (code == null){
            return null;
        }
        PbcPricingModel aPbcPricingModel = getPbcPricingModelMap().get(code.name());
        if (aPbcPricingModel == null){
            aPbcPricingModel = pbcPricingModelMap.get(PointBoxQuoteCode.LN.name());
        }
        return aPbcPricingModel;
    }

    /**
     * Retrieve nodes of the code
     * @param code
     * @return 
     */
    @Override
    public String getNotesOfCode(PointBoxQuoteCode code){
        PbcPricingModel codeModel = getPbcPricingModel(code);
        if (codeModel == null){
            return "";
        }
        return codeModel.getSqMemo();
    }
    
    @Override
    public PricingCurveFileSettings getPricingCurveFileSettings(PointBoxQuoteCode code, PointBoxCurveType curveType){
        if (curveType == null){
            return null;
        }
        PbcPricingModel aPbcPricingModel = getPbcPricingModel(code);
        if (aPbcPricingModel == null){
            return null;
        }
        PricingCurveFileSettings result = null;
        PricingCurveFileSettings[] aPricingCurveFileSettingsArray = aPbcPricingModel.getPricingCurveFileSettingsArray();
        if (aPricingCurveFileSettingsArray != null){
            for (PricingCurveFileSettings aPricingCurveFileSettings : aPricingCurveFileSettingsArray){
                if (curveType.name().equalsIgnoreCase(aPricingCurveFileSettings.getCurveType())){
                    result = aPricingCurveFileSettings;
                    break;
                }
            }//for
        }
        return result;
    }

    /**
     *  
     * @param aPbcBuddyListSettings which is used to insert or update existing ones in the settings
     * @param changed - if it requires change settings status so that it push it to the server for persistency 
     */
    @Override
    public void updatePbcBuddyListSettings(PbcBuddyListSettings aPbcBuddyListSettings, boolean changed) {
        aPbcBuddyListSettings.setVisible(true);
        pbcSettings.updatePbcBuddyListSettings(aPbcBuddyListSettings, changed);
    }

    /**
     * 
     * @param aPbcBuddyListSettings - a buddy list settings should be removed
     */
    @Override
    public void updatePbcBuddyListSettingsVisibility(IBuddyListPanel aBuddyListPanel, boolean visible) {
        pbcSettings.updatePbcBuddyListSettingsVisibility(aBuddyListPanel, visible);
    }

    /**
     * This method should be invoked as soon as a user login succeeded. This method should be called 
     * only on this specific purpose. For other purposes, use of other API methods. Initialize 
     * runtime internal PointBoxConsoleSettings according to server-side pointBoxConsoleSettings. 
     * After initialization is completed, PointBoxConsoleSettingsInitializedEvent will be raised.
     * @param pointBoxConsoleSettings
     * @throws PbcRuntimeException 
     */
    @Override
    public void initializeServerSidePointBoxConsoleSettingsAfterSuccessfulLogin(PointBoxConsoleSettings pointBoxConsoleSettings) throws PbcRuntimeException {
        this.pbcSettings.initializeServerSidePointBoxConsoleSettings(pointBoxConsoleSettings);
        getKernel().raisePointBoxEvent(new PointBoxConsoleSettingsInitializedEvent(pointBoxConsoleSettings,
                                                                                   PointBoxEventTarget.PbcFace));
    }

    @Override
    public PbcArchiveFormat getArchiveFormat() {
        return pbcSettings.getArchiveFormat();
    }

    @Override
    public String getArchiveLocation() {
        return pbcSettings.getArchiveLocation();
    }

    @Override
    public PbcArchiveStatus getArchiveStatus() {
        return pbcSettings.getArchiveStatus();
    }

    @Override
    public void setArchiveFormat(PbcArchiveFormat format) {
        pbcSettings.setArchiveFormat(format);
    }

    @Override
    public void setArchiveLocation(String archiveLocation) {
        pbcSettings.setArchiveLocation(archiveLocation);
    }

    @Override
    public void setArchiveStatus(PbcArchiveStatus status) {
        pbcSettings.setArchiveStatus(status);
    }

    @Override
    public void setPbcWindowsLocation(Point location, PbcFaceComponentType type) {
        pbcSettings.setPbcWindowsLocation(location, type);
    }

    @Override
    public Point getPbcWindowsLocation(PbcFaceComponentType type) {
        return pbcSettings.getPbcWindowsLocation(type);
    }

    @Override
    public void setPbcWindowsSize(Dimension size, PbcFaceComponentType type) {
        pbcSettings.setPbcWindowsSize(size, type);
    }

    @Override
    public Dimension getPbcWindowsSize(PbcFaceComponentType type) {
        return pbcSettings.getPbcWindowsSize(type);
    }

    @Override
    public PbcSystemFrameStyle getPbcSystemFrameStyle() {
        return pbcSettings.getPbcSystemFrameStyle();
    }

    @Override
    public void setPbcSystemFrameStyle(PbcSystemFrameStyle pbcSystemFrameStyle) {
        pbcSettings.setPbcSystemFrameStyle(pbcSystemFrameStyle);
    }

    @Override
    public void setPbcSystemFrameLayout(PbcSystemFrameLayout pbcSystemFrameLayout) {
        pbcSettings.setPbcSystemFrameLayout(pbcSystemFrameLayout);
    }

    @Override
    public PbcSystemFrameLayout getPbcSystemFrameLayout() {
        return pbcSettings.getPbcSystemFrameLayout();
    }
//
//    @Override
//    public void updatePersistentRegularBuddyListSettings(HashSet<String> currentRegularBuddyListNames) {
//        pbcSettings.updatePersistentRegularBuddyListSettings(currentRegularBuddyListNames);
//    }

    @Override
    public ArrayList<PbcBuddyListSettings> getPbcBuddyListSettings() {
        return pbcSettings.getPbcBuddyListSettings();
    }

    @Override
    public PbcBuddyListSettings getPbcBuddyListSettings(String buddyListType, String distListName) {
        return pbcSettings.getPbcBuddyListSettings(buddyListType, distListName);
    }

    @Override
    public ArrayList<BuddyProfile> getPbcBuddyProfiles() {
        return pbcSettings.getPbcBuddyProfiles();
    }
//
//    @Override
//    public void setPbcBuddyListSettings(ArrayList<PbcBuddyListSettings> newPbcBuddyListSettings) {
//        pbcSettings.setPbcBuddyListSettings(newPbcBuddyListSettings);
//    }

//////    @Override
//////    public void updatePbcBuddyListSettings(PbcBuddyListSettings aPbcBuddyListSettings) {
//////        pbcSettings.updatePbcBuddyListSettings(aPbcBuddyListSettings);
//////    }
    
    @Override
    public void updatePbcBuddyProfiles(BuddyProfile aBuddyProfile) {
        pbcSettings.updatePbcBuddyProfiles(aBuddyProfile);
    }

    @Override
     public BuddyProfile retrievePbcBuddyProfiles(BuddyProfile aBuddyProfile) {
        return pbcSettings.retrievePbcBuddyProfiles(aBuddyProfile);
    }

    @Override
    public BuddyProfile getPbcBuddyProfile(IGatewayConnectorBuddy buddy) {
        return pbcSettings.getPbcBuddyProfile(buddy);
    }
    
    @Override
    public PointBoxAccountID getPointBoxAccountID() {
        return pbcSettings.getPointBoxAccountID();
    }

    @Override
    public PointBoxConnectorID getPointBoxConnectorIdFromPbcBuddyListSettings(IGatewayConnectorBuddy loginUser) {
        return pbcSettings.getPointBoxConnectorIdFromPbcBuddyListSettings(loginUser);
    }

    @Override
    public IPbconsoleAccessorySettings getPbconsoleAccessorySettings() {
        return pbcSettings.getPbconsoleAccessorySettings();
    }

    @Override
    public IPointBoxPricingSettings getPointBoxPricingSettings() {
        return pbcSettings.getPointBoxPricingSettings();
    }

    @Override
    public void setGeneralColorForAllPbcViewerSettings(Color color) {
        pbcSettings.setGeneralColorForAllPbcViewerSettings(color);
    }

    @Override
    public void setGeneralFontForAllPbcViewerSettings(Font font) {
        pbcSettings.setGeneralFontForAllPbcViewerSettings(font);
    }

    @Override
    public ArrayList<String> getAllViewerTabUniqueNames() {
        if (pbcSettings == null){
            return new ArrayList<String>();
        }
        return pbcSettings.getAllViewerTabUniqueNames();
    }

    @Override
    public ArrayList<ViewerColumnIdentifier> getAllViewerColumnIdentifiers(String viewerUniqueTabName, ViewerColumnSorter viewerColumnSorter) {
        return pbcSettings.getAllViewerColumnIdentifiers(viewerUniqueTabName, viewerColumnSorter);
    }

    @Override
    public ArrayList<ViewerColumnIdentifier> createDefaultViewerColumnIdentifiers() {
        return pbcSettings.createDefaultViewerColumnIdentifiers();
    }

    @Override
    public Color getBpa_BgColor(String viewerUniqueTabName) {
        return pbcSettings.getBpa_BgColor(viewerUniqueTabName);
    }

    @Override
    public Color getBpa_FgColor(String viewerUniqueTabName) {
        return pbcSettings.getBpa_FgColor(viewerUniqueTabName);
    }

    @Override
    public Color getLatestRowBackground(String viewerUniqueTabName) {
        return pbcSettings.getLatestRowBackground(viewerUniqueTabName);
    }

    @Override
    public Color getLatestRowForeground(String viewerUniqueTabName) {
        return pbcSettings.getLatestRowForeground(viewerUniqueTabName);
    }

    @Override
    public Color getMsgBgColor(String viewerUniqueTabName) {
        return pbcSettings.getMsgBgColor(viewerUniqueTabName);
    }

    @Override
    public Color getMsgFgColor(String viewerUniqueTabName) {
        return pbcSettings.getMsgFgColor(viewerUniqueTabName);
    }

    @Override
    public Color getOutgoingBackground(String viewerUniqueTabName) {
        return pbcSettings.getOutgoingBackground(viewerUniqueTabName);
    }

    @Override
    public Color getOutgoingForeground(String viewerUniqueTabName) {
        return pbcSettings.getOutgoingForeground(viewerUniqueTabName);
    }

    @Override
    public Color getPa_BgColor(String viewerUniqueTabName) {
        return pbcSettings.getPa_BgColor(viewerUniqueTabName);
    }

    @Override
    public Color getPa_FgColor(String viewerUniqueTabName) {
        return pbcSettings.getPa_FgColor(viewerUniqueTabName);
    }

    @Override
    public Color getPb_BgColor(String viewerUniqueTabName) {
        return pbcSettings.getPb_BgColor(viewerUniqueTabName);
    }

    @Override
    public Color getPb_FgColor(String viewerUniqueTabName) {
        return pbcSettings.getPb_FgColor(viewerUniqueTabName);
    }

    @Override
    public Color getPbimQtBgColor(String viewerUniqueTabName) {
        return pbcSettings.getPbimQtBgColor(viewerUniqueTabName);
    }

    @Override
    public Color getPbimQtFgColor(String viewerUniqueTabName) {
        return pbcSettings.getPbimQtFgColor(viewerUniqueTabName);
    }

    @Override
    public Color getQtBgColor(String viewerUniqueTabName) {
        return pbcSettings.getQtBgColor(viewerUniqueTabName);
    }

    @Override
    public Color getQtFgColor(String viewerUniqueTabName) {
        return pbcSettings.getQtFgColor(viewerUniqueTabName);
    }

    @Override
    public Color getSelectedRowBackground(String viewerUniqueTabName) {
        return pbcSettings.getSelectedRowBackground(viewerUniqueTabName);
    }

    @Override
    public Color getSelectedRowForeground(String viewerUniqueTabName) {
        return pbcSettings.getSelectedRowForeground(viewerUniqueTabName);
    }

    @Override
    public Color getSkippedQtBgColor(String viewerUniqueTabName) {
        return pbcSettings.getSkippedQtBgColor(viewerUniqueTabName);
    }

    @Override
    public Color getSkippedQtFgColor(String viewerUniqueTabName) {
        return pbcSettings.getSkippedQtFgColor(viewerUniqueTabName);
    }

    @Override
    public Color getViewerGeneralColor(String viewerUniqueTabName) {
        return pbcSettings.getViewerGeneralColor(viewerUniqueTabName);
    }

    @Override
    public Font getViewerGeneralFont(String viewerUniqueTabName) {
        return pbcSettings.getViewerGeneralFont(viewerUniqueTabName);
    }

    @Override
    public void setBpa_BgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setBpa_BgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setBpa_FgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setBpa_FgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setLatestRowBackground(String viewerUniqueTabName, Color color) {
        pbcSettings.setLatestRowBackground(viewerUniqueTabName, color);
    }

    @Override
    public void setLatestRowForeground(String viewerUniqueTabName, Color color) {
        pbcSettings.setLatestRowForeground(viewerUniqueTabName, color);
    }

    @Override
    public void setMsgBgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setMsgBgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setMsgFgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setMsgFgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setOutgoingBackground(String viewerUniqueTabName, Color color) {
        pbcSettings.setOutgoingBackground(viewerUniqueTabName, color);
    }

    @Override
    public void setOutgoingForeground(String viewerUniqueTabName, Color color) {
        pbcSettings.setOutgoingForeground(viewerUniqueTabName, color);
    }

    @Override
    public void setPa_BgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setPa_BgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setPa_FgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setPa_FgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setPb_BgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setPb_BgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setPb_FgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setPb_FgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setPbimQtBgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setPbimQtBgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setPbimQtFgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setPbimQtFgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setQtBgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setQtBgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setQtFgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setQtFgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setSelectedRowBackground(String viewerUniqueTabName, Color color) {
        pbcSettings.setSelectedRowBackground(viewerUniqueTabName, color);
    }

    @Override
    public void setSelectedRowForeground(String viewerUniqueTabName, Color color) {
        pbcSettings.setSelectedRowForeground(viewerUniqueTabName, color);
    }

    @Override
    public void setSkippedQtBgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setSkippedQtBgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setSkippedQtFgColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setSkippedQtFgColor(viewerUniqueTabName, color);
    }

    @Override
    public void setViewerGeneralColor(String viewerUniqueTabName, Color color) {
        pbcSettings.setViewerGeneralColor(viewerUniqueTabName, color);
    }

    @Override
    public void setViewerGeneralFont(String viewerUniqueTabName, Font font) {
        pbcSettings.setViewerGeneralFont(viewerUniqueTabName, font);
    }

    @Override
    public void resetViewerDefaultSettings(String viewerUniqueTabName) {
        pbcSettings.resetViewerDefaultSettings(viewerUniqueTabName);
    }

    @Override
    public boolean isViewerColumnVisible(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        return pbcSettings.isViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier);
    }

    @Override
    public String getViewerColumnHeaderValue(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        return pbcSettings.getViewerColumnHeaderValue(viewerUniqueTabName, viewerColumnIdentifier);
    }

    @Override
    public int getViewerColumnWidth(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        return pbcSettings.getViewerColumnWidth(viewerUniqueTabName, viewerColumnIdentifier);
    }

    @Override
    public int getViewerColumnPosition(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        return pbcSettings.getViewerColumnPosition(viewerUniqueTabName, viewerColumnIdentifier);
    }

    @Override
    public SortOrder getViewerColumnSortOrder(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        return pbcSettings.getViewerColumnSortOrder(viewerUniqueTabName, viewerColumnIdentifier);
    }

    @Override
    public boolean isViewerColumnResizable(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier) {
        return pbcSettings.isViewerColumnResizable(viewerUniqueTabName, viewerColumnIdentifier);
    }

    @Override
    public void setViewerColumnVisible(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, boolean value) {
        pbcSettings.setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, value);
    }

    @Override
    public void setViewerColumnWidth(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, int width) {
        pbcSettings.setViewerColumnWidth(viewerUniqueTabName, viewerColumnIdentifier, width);
    }

    @Override
    public void setViewerColumnPosition(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, int position) {
        pbcSettings.setViewerColumnPosition(viewerUniqueTabName, viewerColumnIdentifier, position);
    }

    @Override
    public void setViewerColumnSortOrder(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, SortOrder sortOrder) {
        pbcSettings.setViewerColumnSortOrder(viewerUniqueTabName, viewerColumnIdentifier, sortOrder);
    }

    @Override
    public void setViewerColumnHeaderValue(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, String headerValue) {
        pbcSettings.setViewerColumnHeaderValue(viewerUniqueTabName, viewerColumnIdentifier, headerValue);
    }

    @Override
    public void setViewerColumnResizable(String viewerUniqueTabName, ViewerColumnIdentifier viewerColumnIdentifier, boolean value) {
        pbcSettings.setViewerColumnResizable(viewerUniqueTabName, viewerColumnIdentifier, value);
    }
    
    @Override
    public void removePbcViewerSettings(String viewerUniqueTabName) {
        pbcSettings.removePbcViewerSettings(viewerUniqueTabName);
    }

    @Override
    public void addPbcViewerSettings(String viewerUniqueTabName) {
        pbcSettings.addPbcViewerSettings(viewerUniqueTabName);
    }

    @Override
    public void addPbcFilterPropertySettings(String viewerUniqueTabName, PbcFilterPropertySettings aPbcFilterPropertySettings) {
        pbcSettings.addPbcFilterPropertySettings(viewerUniqueTabName, aPbcFilterPropertySettings);
    }

    @Override
    public boolean getViewerFilterBooleanValue(String uniqueFilterTabName, FilterPropertyKey filterPropertyKey) {
        return pbcSettings.getViewerFilterBooleanValue(uniqueFilterTabName, filterPropertyKey);
    }

    @Override
    public long getViewerFilterLongValue(String uniqueFilterTabName, FilterPropertyKey filterPropertyKey) {
        return pbcSettings.getViewerFilterLongValue(uniqueFilterTabName, filterPropertyKey);
    }

    @Override
    public String getViewerFilterStringValue(String uniqueFilterTabName, FilterPropertyKey filterPropertyKey) {
        return pbcSettings.getViewerFilterStringValue(uniqueFilterTabName, filterPropertyKey);
    }

    @Override
    public void renamePbcViewerUniqueName(String oldTabName, String newTabName, ArrayList<IViewerFilterCriteria> newCriteria) {
        pbcSettings.renamePbcViewerUniqueName(oldTabName, newTabName, newCriteria);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void load() throws PointBoxFatalException {
        getKernel().updateSplashScreen("Load " + getKernel().getSoftwareName() + "'s runtime environment...", Level.INFO, 100);
    }
    
    @Override
    public void personalize() {
        getKernel().updateSplashScreen("Personalize " + getKernel().getSoftwareName() + "'s runtime environment...", Level.INFO, 100);
    }

    @Override
    public HashMap<String, PbcPricingModel> getPbcPricingModelMap() {
        if (pbcPricingModelMap == null){
            pbcPricingModelMap = getKernel().retrievePbcPricingModel();
        }
        return pbcPricingModelMap;
    }

    @Override
    public ArrayList<PointBoxQuoteCode> getPointBoxQuoteCodeList() {
        HashMap<String, PbcPricingModel> aPbcPricingModelMap = getPbcPricingModelMap();
        Collection<PbcPricingModel> aPbcPricingModelCollection = aPbcPricingModelMap.values();
        ArrayList<PointBoxQuoteCode> result = new ArrayList<PointBoxQuoteCode>();
        for (PbcPricingModel aPbcPricingModel : aPbcPricingModelCollection){
            result.add(PointBoxQuoteCode.convertEnumNameToType(aPbcPricingModel.getSqCode()));
        }
        return result;
    }

    @Override
    public void requestToSaveCurrentPbcSettings() {
        pbcSettings.savePbcSettingsSynchronously();
    }

    /**
     * @deprecated 
     * @param code
     * @param type
     * @param filePath
     * @return 
     */
    private boolean prepareDefaultCurveFileByCode(PointBoxQuoteCode code, PointBoxCurveType type, String filePath){
        String defaultFolder = PointBoxConfig.getDefaultPricingRuntimeFolder();
        try {
            NIOGlobal.copyFile(new File(defaultFolder + NIOGlobal.fileSeparator() + code.name()+"_"+type.name()+".imp"), new File(filePath));
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PbcRuntime.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(PbcRuntime.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    
    }

    @Override
    public String getLocalCurveFileFullPath(PointBoxQuoteCode code, PointBoxCurveType curveType, boolean guaranteed) {
        return getLocalCurveFileFullPath(code, getPricingCurveFileSettings(code, curveType), guaranteed);
    }
    
    @Override
    public String getLocalCurveFileFullPath(PointBoxQuoteCode code, PricingCurveFileSettings aPricingCurveFileSettings, boolean guaranteed) {
        PointBoxCurveType type = PointBoxCurveType.convertEnumValueToType(aPricingCurveFileSettings.getCurveType());
        String filePath = PointBoxConsoleProperties.getSingleton().retrievePricingRuntimeFolder(getKernel().getPointBoxLoginUser().getIMUniqueName())
                + NIOGlobal.fileSeparator() + aPricingCurveFileSettings.getClientFileName();
        if (guaranteed && (!NIOGlobal.isValidFile(filePath))){
            /**
             * This has to be improved by downloading
             */
            if (!prepareDefaultCurveFileByCode(code, type, filePath)){
                prepareDefaultCurveFileByCode(PointBoxQuoteCode.LN, type, filePath);
            }
        }
        return filePath;
    }

    @Override
    public void unload() {
        getKernel().updateSplashScreen("Unload " + getKernel().getSoftwareName() + "'s runtime environment...", Level.INFO, 100);
        //unload non-persistent settings
        Set<PbcSettingsType> keys = settingStorage.keySet();
        Iterator<PbcSettingsType> itr = keys.iterator();
        while(itr.hasNext()){
            settingStorage.get(itr.next()).storePersonalSettings();
        }
        //persistent settings
        pbcSettings.savePbcSettingsSynchronously();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<PbcPricingModel> initializebcPricingRuntimeCurveSettings() {
        return pbcSettings.initializebcPricingRuntimeCurveSettings();
    }
    
    private void initializeSettings(){
        settingStorage.put(PbcSettingsType.PbcImageSettings,
                           PbcSettingsFactory.getPbconsoleImageSettingsSingleton(this));
        settingStorage.put(PbcSettingsType.PbcAudioSettings,
                           PbcSettingsFactory.getPbconsoleAudioSettingsSingleton(this));
        /**
         * Preference, parser, and pricer settings
         */
        settingStorage.put(PbcSettingsType.PbconsoleActionSettings,
                           PbcSettingsFactory.getPbconsoleActionSettingsSingleton(this));
        settingStorage.put(PbcSettingsType.PricingEngineSettings,
                           PbcSettingsFactory.getPricingEngineSettingsSingleton(this));
        settingStorage.put(PbcSettingsType.PbconsoleParserSettings,
                           PbcSettingsFactory.getPbconsoleParserContantsSettingsSingleton(this));
        settingStorage.put(PbcSettingsType.PricerContantsSettings,
                           PbcSettingsFactory.getPbconsolePricerContantsSettingsSingleton(this));
        settingStorage.put(PbcSettingsType.PointBoxTalkerSettings,
                           PbcSettingsFactory.getPointBoxTalkerSettingsSingleton(this));
    }

    @Override
    public IBuddyRecord createBuddyRecordInstance(String ownerName) {
        return PbconsoleRecordFactory.createBuddyRecordInstance(ownerName);
    }

    @Override
    public IGroupRecord createGroupRecordInstance(String ownerName) {
        return PbconsoleRecordFactory.createGroupRecordInstance(ownerName);
    }

    @Override
    public IGroupMembersRecord createGroupMembersRecordInstance(String groupUniqueName, IGroupRecord group) {
        return PbconsoleRecordFactory.createGroupMembersRecordInstance(groupUniqueName, group);
    }

    @Override
    public IBuddyProfileRecord createBuddyProfileRecordInstance() {
        return PbconsoleRecordFactory.ceateBuddyProfileRecordInstance();
    }

    @Override
    public IPointBoxParserSettings getPbconsoleParserSettings() {
        return (IPointBoxParserSettings)settingStorage.get(PbcSettingsType.PbconsoleParserSettings);
    }

    @Override
    public IPbconsoleAudioSettings getPbcAudioSettings() {
        return (IPbconsoleAudioSettings)settingStorage.get(PbcSettingsType.PbcAudioSettings);
    }

    @Override
    public IPbconsoleImageSettings getPbcImageSettings() {
        return (IPbconsoleImageSettings)settingStorage.get(PbcSettingsType.PbcImageSettings);
    }

    @Override
    public IPointBoxTalkerSettings getPointBoxTalkerSettings() {
        return (IPointBoxTalkerSettings)settingStorage.get(PbcSettingsType.PointBoxTalkerSettings);
    }

    @Override
    public IPointBoxPricingEngineSettings getPricingEngineSettings() {
        return (IPointBoxPricingEngineSettings)settingStorage.get(PbcSettingsType.PricingEngineSettings);
    }

    @Override
    public void handleComponentEvent(PointBoxConsoleEvent event) {
    }

}//PbcRuntime

