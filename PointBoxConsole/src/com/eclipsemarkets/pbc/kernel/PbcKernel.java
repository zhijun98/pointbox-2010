/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.kernel;

import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.gateway.ServerLoginStatusEvent;
import com.eclipsemarkets.gateway.data.*;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.web.ConnectorLivenessQuery;
import com.eclipsemarkets.gateway.web.MessagingState;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.*;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.PbcArchiveFormat;
import com.eclipsemarkets.pbc.face.PbcArchiveStatus;
import com.eclipsemarkets.pbc.pricer.sim.IPbcStructuredQuoteBuilder;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerSearchCriteria;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.IPointBoxTalkerSettings;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.IGroupMembersRecord;
import com.eclipsemarkets.pbc.storage.IPbcStorage;
import com.eclipsemarkets.pbc.storage.PbcDatabaseInstance;
import com.eclipsemarkets.pbc.web.IPointBoxConsoleWebProxy;
import com.eclipsemarkets.pbc.web.PbcReleaseInformation;
import com.eclipsemarkets.pricer.IPbcPricingAgent;
import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.parser.PbcSimGuiParser;
import com.eclipsemarkets.pbc.pricer.sim.PricingInterestRateFrame;
import com.eclipsemarkets.release.PbcMode;
import com.eclipsemarkets.release.PointBoxConfig;
import com.eclipsemarkets.release.PointBoxExecutorConfiguration;
import com.eclipsemarkets.runtime.IPointBoxAutoPricerConfig;
import com.eclipsemarkets.runtime.IPointBoxPricerConfig;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.*;
import com.eclipsemarkets.web.pbc.PbcClientSettings;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import com.eclipsemarkets.web.pbc.PointBoxConsoleSettings;
import com.eclipsemarkets.web.pbc.PricingCurveFileSettings;
import com.eclipsemarkets.web.pbc.talker.BuddyListGroupItem;
import com.eclipsemarkets.web.pbc.talker.BuddyProfile;
import com.eclipsemarkets.web.pbc.talker.PbcBuddyListSettings;
import com.eclipsemarkets.web.pbc.viewer.PbcViewerSettings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.xml.ws.WebServiceException;

/**
 * PbcKernel
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 28, 2011 at 5:07:28 PM
 */
class PbcKernel implements IPbcKernel{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbcKernel.class.getName());
    }

    private final InvokeWorker invokeWorker;

    /**
     * invoke() uses the pass-in splash window; shutdown uses face-offered splash window
     */
    private IPbcSplashScreen splash;

    private IPbcFace face;
    private IPbcRuntime runtime;
    private IPbcStorage storage;
    private IPbcPricingAgent pricingAgent;
    //private IPbcParser parser;
    private IPointBoxConsoleWebProxy web;
    
    /**
     * After PBC log in the remote PointBoxCentral server, the server will assign a pointBoxAccountID 
     * back to the local PBC if everything is fine on the server-side in the request-login procedure. 
     * This is used to store this info.
     * 
     * Store the current pointBoxAccountID assigned by PointBoxCentral server. It does not mean this 
     * accountID is valid for login. For example, when PBC use a bad password to log in, the PointBoxCentral 
     * server also return a pointBoxAccountID.
     */
    private PointBoxAccountID pointBoxAccountID;
    
    private final ArrayList<PointBoxConnectorID> pointBoxConnectorIDs;
    
    /**
     * Service for raisePointBoxEvent() method
     */
    private final ExecutorService eventService;
    
    PbcKernel(final IPbcSplashScreen splash) {
        this.splash = splash;
        eventService = Executors.newFixedThreadPool(PointBoxExecutorConfiguration.PbcKernel_Service_Control);
        invokeWorker = new InvokeWorker();
        pointBoxAccountID = null;
        pointBoxConnectorIDs = new ArrayList<PointBoxConnectorID>();
    }

    @Override
    public IPbsysOptionQuote parseStructuredInstantMessageToConstructStructuredQuote(IPbsysInstantMessage quoteMessage) {
        PbcPricingModel aPbcPricingModel = this.getPbcPricingModel(PbcSimGuiParser.parseStructuredInstantMessageCode(quoteMessage));
        return PbcSimGuiParser.parseStructuredInstantMessageToConstructStructuredQuote(quoteMessage, aPbcPricingModel);
    }

    @Override
    public void refreshPricingEnvironment() {
        pricingAgent.refreshPricingEnvironment();
    }

    /**
     * A table model which contains data for a specific code.
     * @param code
     * @return 
     * @see PricingInterestRateFrame
     */
    @Override
    public List<Object[]> retireveOptionInterestRates(PointBoxQuoteCode code){
        return pricingAgent.retireveOptionInterestRates(code);
    }

    @Override
    public LinkedHashMap<GregorianCalendar, String> retrieveAllDescriptiveExpirationData(PointBoxQuoteCode code) {
        return pricingAgent.retrieveAllDescriptiveExpirationData(code);
    }

    @Override
    public HashMap<String, HashMap<String, String>> retrieveAllDescriptiveExpirationData() {
        return pricingAgent.retrieveAllDescriptiveExpirationData();
    }

    @Override
    public HashMap<String, TreeMap<String, GregorianCalendar>> retrieveAllExpirationData() {
        return pricingAgent.retrieveAllExpirationData();
    }

    /**
     * @param mmddyyyy - first-day of a month e.g. 04012014
     * @param code
     * @return 
     */
    @Override
    public double queryUnderlierByMonthIndex(String mmddyyyy, PointBoxQuoteCode code) {
        return pricingAgent.queryUnderlierByMonthIndex(mmddyyyy, code);
    }

    @Override
    public List<String> requestToCheckConnectionOfflineStatus(PointBoxAccountID accountID, ArrayList<PointBoxConnectorID> aPointBoxConnectorIDList) {
        return web.checkConnectionOfflineStatus(accountID, aPointBoxConnectorIDList);
    }

    @Override
    public PricingCurveFileSettings retrievePricingCurveFileSettings(PointBoxQuoteCode code, PointBoxCurveType curveType) {
        if ((curveType == null) || (code == null)){
            return null;
        }
        HashMap<String, PbcPricingModel> modelMap = web.retrievePbcPricingModel(getPointBoxAccountID());
        PricingCurveFileSettings result = null;
        if (modelMap != null){
            PbcPricingModel model = modelMap.get(code.name());
            if (model != null){
                PricingCurveFileSettings[] aPricingCurveFileSettingsArray = model.getPricingCurveFileSettingsArray();
                if (aPricingCurveFileSettingsArray != null){
                    for (PricingCurveFileSettings aPricingCurveFileSettings : aPricingCurveFileSettingsArray){
                        if (curveType.name().equalsIgnoreCase(aPricingCurveFileSettings.getCurveType())){
                            result = aPricingCurveFileSettings;
                            break;
                        }
                    }//for-loop
                }
            }
        }
        return result;
    }
    
    /**
     * Retrieve a server-side PbcPricingModel from controller for local PBC instances. 
     * This is a time-consuming method. Usually, use of runtime to get this information 
     * for faster results.
     * @return - HashMap<Code, Model>
     */
    @Override
    public HashMap<String, PbcPricingModel> retrievePbcPricingModel(){
        HashMap<String, PbcPricingModel> result = web.retrievePbcPricingModel(getPointBoxAccountID());
        if (result != null){
            Collection<PbcPricingModel> aPbcPricingModelCollection = result.values();
            PricingCurveFileSettings[] aPricingCurveFileSettingsArray;
            PointBoxQuoteCode code;
            String filePath;
            String url;
            for (PbcPricingModel aPbcPricingModel : aPbcPricingModelCollection){
                aPricingCurveFileSettingsArray = aPbcPricingModel.getPricingCurveFileSettingsArray();
                if (aPricingCurveFileSettingsArray != null){
                    code = PointBoxQuoteCode.convertEnumNameToType(aPbcPricingModel.getSqCode());
                    if (!PointBoxQuoteCode.UNKNOWN.equals(code)){
                        for (PricingCurveFileSettings aPricingCurveFileSettings : aPricingCurveFileSettingsArray){
                            filePath = getLocalCurveFileFullPath(code, aPricingCurveFileSettings, true);
                            if (!NIOGlobal.isValidFile(filePath)){
                                url = PointBoxConfig.generateCurveFileDownloadURL(getSelectedControllerIPwithPort(), 
                                                                     aPricingCurveFileSettings.getServerFileName());
                                try {
                                    NIOGlobal.downloadFileFromWeb(new URL(url), new File(filePath), true);
                                } catch (MalformedURLException ex) {
                                    Logger.getLogger(PbcKernel.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }//if
                        }//for
                    }//if
                }
            }//for
        }//if
        return result;
    }

    /**
     * 
     * @return 
     */
    @Override
    public boolean isPbcPricingAdmin() {
        PointBoxAccountID aPointBoxAccountID = this.getPointBoxAccountID();
        if (aPointBoxAccountID == null){
            return false;
        }else{
            PbcAccountBasedSettings aPbcPricingAdminSettings = aPointBoxAccountID.getPbcAccountBasedSettings();
            if (aPbcPricingAdminSettings == null){
                retrievePbcAccountBasedSettings();
                aPbcPricingAdminSettings = aPointBoxAccountID.getPbcAccountBasedSettings();
            }
            if (aPbcPricingAdminSettings == null){
                return false;
            }
            //Curve Upload Type: 2 = automatic upload; 1 = manual upload; 0 = upload is disabled
            return aPbcPricingAdminSettings.getUploadCurveType() > 0;
        }
    }

    @Override
    public boolean isPbcFeatureDisabled(PbcFeature aPbcFeature) {
        if (aPbcFeature == null){
            return false;
        }
        PointBoxAccountID aPointBoxAccountID = this.getPointBoxAccountID();
        if (aPointBoxAccountID == null){
            return false;
        }else{
            PbcAccountBasedSettings aPbcAccountBasedSettings = aPointBoxAccountID.getPbcAccountBasedSettings();
            if (aPbcAccountBasedSettings == null){
                retrievePbcAccountBasedSettings();
                aPbcAccountBasedSettings = aPointBoxAccountID.getPbcAccountBasedSettings();
            }
            if (aPbcAccountBasedSettings == null){
                /**
                 * default: if users were not explicitly authorized from the controller, 
                 * they will be disabled to user features
                 */
                return true;
            }
            
            String[] enabledFeatures = aPbcAccountBasedSettings.getPbcFeatureEnabledArray();
            if ((enabledFeatures == null) || (enabledFeatures.length == 0)){
                return true;
            }else{
                boolean result = true;
                for (String feature : enabledFeatures){
                    if (aPbcFeature.toString().equalsIgnoreCase(feature)){
                        result = false;
                        break;
                    }
                }
                return result;
            }
        }
    }

//    @Override
//    public PbcPricingAdminSettings retrievePbcPricingAdminSettings() {
//        PointBoxAccountID aPointBoxAccountID = this.getPointBoxAccountID();
//        if (aPointBoxAccountID == null){
//            return null;
//        }else{
//            /**
//             * Currently, this feature is only available for NATURAL_GAS
//             */
//            if (QuoteCommodityType.NATURAL_GAS.equals(aPointBoxAccountID.getDefaultQuoteCommodity().getCommodityType())){
//                PbcPricingAdminSettings aPbcPricingAdminSettings = web.retrievePbcPricingAdminSettings(aPointBoxAccountID.getLoginName());
//                if (aPbcPricingAdminSettings == null){
//                    aPbcPricingAdminSettings = new PbcPricingAdminSettings();
//                    aPbcPricingAdminSettings.setDescription("");
//                    aPbcPricingAdminSettings.setFrequency(0);
//                    aPbcPricingAdminSettings.setPbimAccountName(aPointBoxAccountID.getLoginName());
//                    aPbcPricingAdminSettings.setUploadCurveType(0);
//                }
//                aPointBoxAccountID.setPbcPricingAdminSettings(aPbcPricingAdminSettings);
//                return aPbcPricingAdminSettings;
//            }else{
//                return null;
//            }
//        }
//    }
    
    @Override
    public IPbcStructuredQuoteBuilder getPbcStructuredQuoteBuilder(PointBoxQuoteType category) {
        return face.getPbcStructuredQuoteBuilder(category);
    }
    
    @Override
    public PbcAccountBasedSettings retrievePbcAccountBasedSettings() {
        PointBoxAccountID aPointBoxAccountID = this.getPointBoxAccountID();
        if (aPointBoxAccountID == null){
            return null;
        }else{
            PbcAccountBasedSettings aPbcAccountBasedSettings = web.retrievePbcAccountBasedSettings(aPointBoxAccountID.getLoginName());
            if (aPbcAccountBasedSettings == null){
                aPbcAccountBasedSettings = new PbcAccountBasedSettings();
                aPbcAccountBasedSettings.setDescription("");
                aPbcAccountBasedSettings.setFrequency(0);
                aPbcAccountBasedSettings.setPbimAccountName(aPointBoxAccountID.getLoginName());
                aPbcAccountBasedSettings.setUploadCurveType(0);
                aPbcAccountBasedSettings.setPbcFeatureDisabledArray(null);
            }
            aPointBoxAccountID.setPbcAccountBasedSettings(aPbcAccountBasedSettings);
            return aPbcAccountBasedSettings;
        }
    }

    @Override
    public boolean preparePointBoxSystemLogin(String pbimAccountName) {
        return web.preparePointBoxSystemLogin(pbimAccountName);
    }

    @Override
    public List<String> retrievePbcPricingRuntimeDownloadUrls(PointBoxQuoteCode symbol) {
        if (web == null){
            return new ArrayList<String>();
        }else{
            return web.retrievePbcPricingRuntimeDownloadUrls(symbol);
        }
    }

    @Override
    public void suspendPricer() {
        if (pricingAgent != null){
            pricingAgent.suspendPricing();
        }
    }

    @Override
    public void resumePricer() {
        if (pricingAgent != null){
            pricingAgent.resumePricing();
        }
    }

    @Override
    public void destroyPointBoxAccountID() {
        removePointBoxConnectorID(this.pointBoxAccountID);
        this.pointBoxAccountID = null;
    }
    
    @Override
    public PointBoxAccountID getPointBoxAccountID() {
        synchronized(this){
            return pointBoxAccountID;
        }
    }

    /**
     * @deprecated 
     * @param distListName
     * @param visible 
     */
    @Override
    public void requestToStoreRegularBuddyListPanelVisibility(String distListName, boolean visible) {
        web.requestToStoreRegularBuddyListPanelVisibility(getPointBoxAccountID(), distListName, visible);
    }

    @Override
    public String publishUploadedPricingRuntimeSettingsFilesToPointBoxServer() {
        return web.publishUploadedPricingRuntimeSettingsFilesToPointBoxServer(getPointBoxAccountID());
    }

    /**
     * This method demand caller has been logged in. Otherwise, NULL returned
     * @return 
     */
    @Override
    public PbcMode getPbcMode() {
        if (getPointBoxAccountID() == null){
            return null;
        }
        return web.getPbcMode(this.getPointBoxAccountID().getLoginName());
    }

    @Override
    public PointBoxQuoteType retrievePointBoxQuoteTypeFromPriceModel(PointBoxQuoteCode code) {
        PbcPricingModel model = getPbcPricingModel(code);
        if (model == null){
            return null;
        }else{
            return PointBoxQuoteType.convertEnumValueToType(model.getSqType());
        }
    }

    @Override
    public PbcPricingModel getPbcPricingModel(PointBoxQuoteCode code) {
        return getPointBoxConsoleRuntime().getPbcPricingModel(code);
    }

    @Override
    public PbcPricingModel getDefaultPbcPricingModel() {
        return getPbcPricingModel(getDefaultSimCodeFromProperties());
    }

    @Override
    public PointBoxQuoteCode getDefaultSimCodeFromProperties() {
        return PointBoxConsoleProperties.getSingleton().retrieveDefaultPointBoxQuoteCode(this.getPointBoxLoginUser().getIMUniqueName());
    }

    @Override
    public PointBoxQuoteCode getSelectedSimCodeFromProperties() {
        return PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(getPointBoxLoginUser().getIMUniqueName());
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getAllBuddies(GatewayServerType gatewayServerType, boolean onlineBuddyRequired) {
        return face.getSortedBuddyList(gatewayServerType, onlineBuddyRequired);
    }

    @Override
    public String getSelectedControllerIPwithPort() {
        return web.getSelectedControllerIPwithPort();
    }
    
    /**
     * @deprecated - this method is just a temp solution to make backward compatibility.
     * @param symbol
     * @param type
     * @return - empty or null returned to flag uploading success. Otherwise it is error -message
     */
    @Override
    public String uploadLegancyLnCurveFilesToPointBoxServer(String symbol,
                                                            String type) 
    {
        //this is disabled for legacy curve upload
        return "";
//        PointBoxConfig config = PointBoxConfig.getSingleton();
//        String errorMsg;
//        if (isPbcPricingAdmin()){
//            PbcMode aPbcMode = face.getKernel().getPbcMode();
//            switch (aPbcMode){
//                case Staging:
//                case Developer:
//                    errorMsg = web.uploadLegancyLnCurveFilesToPointBoxServer(getPointBoxAccountID(), config.getProductionPbcPricingAdminUploadServletUrl(), symbol, type);
//                    break;
//                default:
//                    errorMsg = "This version does not support this operation.";
//                    
//            }
//        }else{
//            errorMsg = "You are not authorized to upload pricing runtime settings to PointBox server.";
//        }
//        return errorMsg;
    }
    
//    private String uploadPricingRuntimeSettingsFilesToPointBoxServer(String uploadServletUrl, ArrayList<ArrayList<Object>> filePathObjects) {
//        try{
//            String statusMsg = null;
//            PointBoxAccountID accountID = getPointBoxAccountID();
//            for (ArrayList<Object> filePathObject : filePathObjects){
//                statusMsg = web.uploadPricingRuntimeSettingsFilesToPointBoxServer(accountID, 
//                                                                                  uploadServletUrl,
//                                                                                  (PointBoxQuoteCode)filePathObject.get(0),         //symbol
//                                                                                  (PointBoxCurveType)filePathObject.get(1),   //type 
//                                                                                  filePathObject.get(2).toString());                    //filePath
//                if (statusMsg != null){
//                    break;
//                }
//            }//for
//            if (statusMsg == null){
//                return null;
//            }else{
//                return statusMsg;
//            }
//        }catch(Exception ex){
//            return "Exception -" + ex.getMessage() + "- failed to upload pricing runtime files. ";
//        }
//    }

    /**
     * 
     * @param code
     * @param type
     * @param filePath
     * @return - error message
     */
    @Override
    public String uploadPricingRuntimeSettingsFilesToPointBoxServer(PointBoxQuoteCode code, PointBoxCurveType type, String filePath) {
        PbcMode userPbcMode = web.getPbcMode(getPointBoxAccountID().getLoginName());
        PointBoxConfig config = PointBoxConfig.getSingleton();
        switch(userPbcMode){
            case Production:
            case Staging:
                web.uploadPricingRuntimeSettingsFilesToPointBoxServer(getPointBoxAccountID(),
                                                                            config.getPbcPricingAdminUploadServletUrl(
                                                                                PointBoxConsoleProperties.getSingleton().retrieveControllerIPforAllUsers(PbcMode.Staging)),
                                                                            code,
                                                                            type, 
                                                                            filePath);
                return web.uploadPricingRuntimeSettingsFilesToPointBoxServer(getPointBoxAccountID(),
                                                                            config.getPbcPricingAdminUploadServletUrl(
                                                                                PointBoxConsoleProperties.getSingleton().retrieveControllerIPforAllUsers(PbcMode.Production)),
                                                                            code,
                                                                            type, 
                                                                            filePath);
            default:
                return web.uploadPricingRuntimeSettingsFilesToPointBoxServer(getPointBoxAccountID(),
                                                                            config.getPbcPricingAdminUploadServletUrl(
                                                                                PointBoxConsoleProperties.getSingleton().retrieveControllerIPforAllUsers(userPbcMode)),
                                                                            code,
                                                                            type, 
                                                                            filePath);
                
        }
    }

    @Override
    public void uploadAllEmsCurves() {
        face.uploadAllEmsCurves();
    }

    @Override
    public void uploadLegacyEmsCurves() {
        face.uploadLegacyEmsCurves();
    }

    @Override
    public String getLocalCurveFileFullPath(PointBoxQuoteCode code, PricingCurveFileSettings aPricingCurveFileSettings, boolean guaranteed) {
        return runtime.getLocalCurveFileFullPath(code, aPricingCurveFileSettings, guaranteed);
    }

    @Override
    public String getLocalCurveFileFullPath(PointBoxQuoteCode code, PointBoxCurveType curveType, boolean guaranteed) {
        return runtime.getLocalCurveFileFullPath(code, curveType, guaranteed);
    }

    @Override
    public void setPointBoxAccountID(PointBoxAccountID pointBoxAccountID) {
        if (pointBoxAccountID == null){
            return;
        }
        synchronized(this){
            if (this.pointBoxAccountID != null) {
                removePointBoxConnectorID(this.pointBoxAccountID);
            }
            this.pointBoxAccountID = pointBoxAccountID;
            addPointBoxConnectorID(pointBoxAccountID);
        }
    }

    @Override
    public void addPointBoxConnectorID(PointBoxConnectorID pointBoxConnectorID) {
        if (pointBoxConnectorID == null){
            return;
        }
        synchronized(pointBoxConnectorIDs){
            if (!pointBoxConnectorIDs.contains(pointBoxConnectorID)){
                pointBoxConnectorIDs.add(pointBoxConnectorID);
            }
        }
    }

    @Override
    public PointBoxConnectorID getPointBoxConnectorID(IGatewayConnectorBuddy loginUser) {
        if (loginUser == null){
            return null;
        }
        synchronized(pointBoxConnectorIDs){
            for (PointBoxConnectorID pointBoxConnectorID : pointBoxConnectorIDs){
                if (pointBoxConnectorID != null){
                    if (pointBoxConnectorID.getLoginName().equalsIgnoreCase(loginUser.getIMScreenName()) 
                            && pointBoxConnectorID.getGatewayServerType().equalsIgnoreCase(loginUser.getIMServerType().toString()))
                    {
                        return pointBoxConnectorID;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public void removePointBoxConnectorID(PointBoxConnectorID pointBoxConnectorID) {
        synchronized(pointBoxConnectorIDs){
            pointBoxConnectorIDs.remove(pointBoxConnectorID);
        }
    }

    @Override
    public ArrayList<PointBoxConnectorID> getPointBoxConnectorIDList() {
        ArrayList<PointBoxConnectorID> connectorIDs = new ArrayList<PointBoxConnectorID>();
        synchronized(pointBoxConnectorIDs){
            for (PointBoxConnectorID pointBoxConnectorID : pointBoxConnectorIDs){
                connectorIDs.add(pointBoxConnectorID);
            }
        }
        return connectorIDs;
    }

    @Override
    public boolean isServerPricer() {
        PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
        if (getPointBoxLoginUser() == null){
            return false;
        }
        switch(prop.retrievePbcPricerType(getPointBoxLoginUser().getUniqueNickname())){
            case PB:
            case WT:
                return false;
            case PBS:
            case WTS:
                return true;
            default:
                return false;
        }
    }

    @Override
    public IGatewayConnectorBuddy getPointBoxLoginUser() {
        if (face == null){
            return null;
        }
        return face.getPointBoxLoginUser();
    }
    
    @Override
    public IPbcFace getPointBoxFace(){
        return face;
    }
    
    @Override
    public IPbcRuntime getPointBoxConsoleRuntime(){
        return runtime;
    }

    @Override
    public IPointBoxConsoleWebProxy getPointBoxConsoleWeb() {
        return web;
    }

    /**
     * Invoke kernel so as to launch PointBox console
     */
    @Override
    public void invoke() {
        if (!invokeWorker.isAlive()){
            invokeWorker.start();
        }
    }

    @Override
    public void pushFloatingMessagingFrameToFront() {
        face.pushFloatingMessagingFrameToFront();
    }

    /**
     * Call this method to "nicely" shut down the entire PointBox Console.
     * @param pbimLoginUser - it could be NULL. But if it is NULL, it means the current
     * users never get any successful login. All of his records will be erased by the
     * server according to its client-info, i.e., UUID
     * @param shutDownPBC - whether or not shut down PBC after sent out logout-request to the server
     */
    @Override
    public void shutdown(IGatewayConnectorBuddy pbimLoginUser, boolean shutDownPBC) {
        (new ShutdownWorker(shutDownPBC)).execute();
    }
    
    @Override
    public void shutdown(IGatewayConnectorBuddy pbimLoginUser,boolean hasWarningDialog, boolean shutDownPBC) {
        (new ShutdownWorker(hasWarningDialog, shutDownPBC)).execute();
    }

    @Override
    public String getCompanyName() {
        return PbcProperties.getSingleton().getReleaseCompany();
    }

    @Override
    public String getSoftwareName() {
        return PbcProperties.getSingleton().getSoftwareName();
    }

    @Override
    public String getSoftwareVersion() {
        return PbcProperties.getSingleton().getSoftwareVersion();
    }

    @Override
    public String getPbcReleaseCode() {
        return PbcProperties.getSingleton().getReleaseCode();
    }

    @Override
    public PbcReleaseUserType getPbcReleaseUserType() {
        return PbcProperties.getSingleton().getReleaseUserType();
    }

    @Override
    public void notifyReleaseUpdateRequired(PbcReleaseInformation releaseInfo) {
        face.notifyReleaseUpdateRequired(releaseInfo);
    }

    @Override
    public void displayPbcReleaseUpdateDialog(PbcReleaseInformation releaseInfo) {
        face.displayPbcReleaseUpdateDialog(releaseInfo);
    }

    @Override
    public void displayPointBoxFrame() {
        face.displayPointBoxFrame();
    }

    @Override
    public JFrame getPointBoxMainFrame() {
        return face.getPointBoxMainFrame();
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> getAllLoginUsersOnFace() {
        return face.getAllLoginUsers();
    }
    
    @Override
    public TreeMap<String, IGroupMembersRecord> retrieveGroupMembersRecord() {
        return storage.retrieveGroupMembersRecord(face.getPointBoxLoginUser());
    }

    @Override
    public void storeGroupMembersRecords(TreeMap<String, IGroupMembersRecord> groupMemberRecords) {
        if (face.getPointBoxLoginUser() == null){
            storage.storeGroupMembersRecords(PbcDatabaseInstance.DefaultEmsUser.toString(), groupMemberRecords);
        }else{
            storage.storeGroupMembersRecords(face.getPointBoxLoginUser().getIMUniqueName(), groupMemberRecords);
        }
    }

    @Override
    public void loadAutoPricerRecord(IPointBoxAutoPricerConfig autoPricerRecord) {
        storage.loadAutoPricerRecord(autoPricerRecord, face.getPointBoxLoginUser());
    }

    private void loadBuddyProfileRecord(IBuddyProfileRecord buddyProfileRecord) {
        BuddyProfile buddyProfile = getPointBoxConsoleRuntime().retrievePbcBuddyProfiles(convertToBuddyProfile(buddyProfileRecord));
        convertToBuddyProfileRecord(buddyProfile, buddyProfileRecord);
    }

    @Override
    public IBuddyProfileRecord retrieveBuddyProfileRecord(IGatewayConnectorBuddy buddy) {
       if (buddy == null){
           return null;
       }
       if (buddy.getLoginOwner() == null){
           return null;
       }
       IBuddyProfileRecord buddyProfile = getPointBoxConsoleRuntime().createBuddyProfileRecordInstance();
       buddyProfile.setUserOwner(buddy.getLoginOwner().getIMUniqueName());
       buddyProfile.setProfileUuid(buddy.getIMScreenName());
       buddyProfile.setGatewayServerType(buddy.getIMServerType().toString());
       buddyProfile.setScreenName(buddy.getIMScreenName());
       buddyProfile.setNickName(buddy.getNickname());
       loadBuddyProfileRecord(buddyProfile);
       return buddyProfile;
    }

    /**
     * Neeed a convertion for using previous saving mode
     * @param buddyProfile
     * @param profileRecord
     * @return 
     */
    private IBuddyProfileRecord convertToBuddyProfileRecord(BuddyProfile buddyProfile,IBuddyProfileRecord profileRecord){
        if(buddyProfile!=null){
            profileRecord.setFirstName(buddyProfile.getFirstName());
            profileRecord.setLastName(buddyProfile.getLastName());
            profileRecord.setNickName(buddyProfile.getNickName());
            profileRecord.setNotes(buddyProfile.getDescription());
            
            profileRecord.setWorkCity(buddyProfile.getCity());
            profileRecord.setWorkEmail(buddyProfile.getEmail());
            profileRecord.setWorkPhone(buddyProfile.getPhone());
            profileRecord.setWorkState(buddyProfile.getStateName());
            profileRecord.setWorkStreet(buddyProfile.getStreet());
            profileRecord.setWorkZip(buddyProfile.getZip());
        }
        return profileRecord;
    }

    @Override
    public int storeBuddyProfileRecord(IBuddyProfileRecord buddyProfile) {
        getPointBoxConsoleRuntime().updatePbcBuddyProfiles(convertToBuddyProfile(buddyProfile));
        return 0;
    }
    
    /**
     * todo: consider only use one class, i.e., get rid of one of them (IBuddyProfileRecord and BuddyProfile)
     * @param profileRecord
     * @return 
     */
    private BuddyProfile convertToBuddyProfile(IBuddyProfileRecord profileRecord){
        BuddyProfile buddyProfile=new BuddyProfile();

        buddyProfile.setBirthday((new GregorianCalendar()).getTimeInMillis());
        buddyProfile.setCity(profileRecord.getWorkCity());
        buddyProfile.setDescription(profileRecord.getNotes());
        buddyProfile.setEmail(profileRecord.getWorkEmail());
        buddyProfile.setFirstName(profileRecord.getFirstName());
        buddyProfile.setGatewayServerType(profileRecord.getGatewayServerType());
        buddyProfile.setLastName(profileRecord.getLastName());
        buddyProfile.setNickName(profileRecord.getNickName());
        buddyProfile.setPhone(profileRecord.getWorkPhone());
        buddyProfile.setScreenName(profileRecord.getScreenName());
        buddyProfile.setOwnerScreenName(profileRecord.getUserOwner());
        buddyProfile.setStateName(profileRecord.getWorkState());
        buddyProfile.setStreet(profileRecord.getWorkStreet());
        buddyProfile.setZip(profileRecord.getWorkZip());

        return buddyProfile;
    }

    @Override
    public void deleteGroupMemberRecord(IGroupMembersRecord groupMembersRecord) {
        storage.deleteGroupMemberRecord(groupMembersRecord);
    }

    @Override
    public ArrayList<IPbsysOptionQuote> retrieveHistoricalQuotes(IGatewayConnectorBuddy pointBoxLoginUser, IViewerSearchCriteria criteria) {
        return storage.retrieveHistricalQuotes(pointBoxLoginUser, criteria);
    }

    @Override
    public ArrayList<IPbsysOptionQuote> retrieveTodayQuotes(IGatewayConnectorBuddy pointBoxLoginUser) {
        return storage.retrieveTodayQuotes(pointBoxLoginUser);
    }

    @Override
    public void storeAutoPricerRecord(IPointBoxAutoPricerConfig autoPricerRecord) {
        if (face.getPointBoxLoginUser() == null){
            autoPricerRecord.setOwnerUniqueName(PbcDatabaseInstance.DefaultEmsUser.toString());
        }else{
            autoPricerRecord.setOwnerUniqueName(face.getPointBoxLoginUser().getIMUniqueName());
        }
        storage.storeAutoPricerRecord(autoPricerRecord);
    }

    @Override
    public void loadPricerRecord(IPointBoxPricerConfig pricerRecord) {
        storage.loadPricerRecord(pricerRecord, face.getPointBoxLoginUser());
    }

    @Override
    public void storePricerRecord(IPointBoxPricerConfig pricerRecord) {
        if (face.getPointBoxLoginUser() == null){
            pricerRecord.setOwnerUniqueName(PbcDatabaseInstance.DefaultEmsUser.toString());
        }else{
            pricerRecord.setOwnerUniqueName(face.getPointBoxLoginUser().getIMUniqueName());
        }
        storage.storePricerRecord(pricerRecord);
    }

    /**
     * 
     * @param settings 
     * @deprecated 
     */
    @Override
    public void loadPointBoxTalkerSettings(IPointBoxTalkerSettings settings) {
        storage.loadPointBoxTalkerSettings(face.getPointBoxLoginUser(), settings);
    }

    /**
     * 
     * @param settings 
     * @deprecated 
     */
    @Override
    public void storePointBoxTalkerSettings(IPointBoxTalkerSettings settings) {
        storage.storePointBoxTalkerSettings(face.getPointBoxLoginUser(), settings);
    }

    /**
     * If PBC is re-opened by users, this method invokes a background thread to load today's previous quote-messages
     */
    @Override
    public void loadQuoteMessages(List<String> filepathes) {
        if (web != null){
            web.loadTodayQuoteMessages(filepathes);
        }
    }

    /**
     * @deprecated 
     */
    @Override
    public ArrayList<IPbsysInstantMessage> retrieveTodayMessages() {
        return storage.retrieveTodayMessages(face.getPointBoxLoginUser());
    }

    @Override
    public String sendStructuredQuoteMessage(PointBoxAccountID pointBoxAccountID, MessagingState structuredMessage) {
        return web.sendStructuredQuoteMessage(pointBoxAccountID, structuredMessage);
    }

    @Override
    public boolean requestToSetupPointBoxClientArchive(PointBoxAccountID aPointBoxAccountID, PbcArchiveFormat pbcArchiveFormat, PbcArchiveStatus pbcArchiveStatus) {
        return web.requestToSetupPointBoxClientArchive(aPointBoxAccountID, pbcArchiveFormat, pbcArchiveStatus);
    }

    @Override
    public void requestToSaveCurrentPbcSettings() {
        runtime.requestToSaveCurrentPbcSettings();
    }

    @Override
    public void requestToRemovePbcViewerSettingsCollection(PointBoxAccountID aPointBoxAccountID, List<PbcViewerSettings> aPbcViewerSettingsList) {
        web.requestToRemovePbcViewerSettingsCollection(aPointBoxAccountID, aPbcViewerSettingsList);
    }

    @Override
    public void requestToSaveBuddyProfiles(PointBoxAccountID aPointBoxAccountID, PointBoxConsoleSettings pbcSettings) {
        web.requestToSaveBuddyProfiles(aPointBoxAccountID, pbcSettings);
    }

    @Override
    public void requestToSavePbcWindowsSettings(PointBoxAccountID aPointBoxAccountID, PbcClientSettings aPbcClientSettings) {
        web.requestToSavePbcWindowsSettings(aPointBoxAccountID, aPbcClientSettings);
    }

    @Override
    public void requestToSavePbcFileSettings(PointBoxAccountID aPointBoxAccountID, PbcClientSettings aPbcClientSettings) {
        web.requestToSavePbcFileSettings(aPointBoxAccountID, aPbcClientSettings);
    }

    @Override
    public void requestToSavePbcViewerSettingsCollection(PointBoxAccountID aPointBoxAccountID, PbcClientSettings pbcClientSettings) {
        web.requestToSavePbcViewerSettingsCollection(aPointBoxAccountID, pbcClientSettings);
    }

    @Override
    public void requestToSavePbcClientSettingsRecord(PointBoxAccountID aPointBoxAccountID, PbcClientSettings pbcClientSettings) {
        web.requestToSavePbcClientSettingsRecord(aPointBoxAccountID, pbcClientSettings);
    }

    @Override
    public void requestToSavePbcBuddyListSettingsCollection(PointBoxAccountID aPointBoxAccountID, PbcBuddyListSettings[] aPbcBuddyListSettingsArray) {
        web.requestToSavePbcBuddyListSettingsCollection(aPointBoxAccountID, aPbcBuddyListSettingsArray);
    }

    @Override
    public BuddyListGroupItem requestToRetrieveConferenceRecord(PointBoxAccountID pointBoxAccountID, String hosterScreenName, String confUuid) {
        return web.requestToRetrieveConferenceRecord(pointBoxAccountID, hosterScreenName, confUuid);
    }

    @Override
    public TreeSet<String> retrieveGlobalRelayAccountSet() {
        List<String> aList = web.retrieveGlobalRelayUserList();
        TreeSet<String> result = new TreeSet<String>();
        if (aList != null){
            for (String aName : aList){
                result.add(aName);
            }
        }
        return result;
    }

    @Override
    public ArrayList<IPbsysInstantMessage> retrieveTodayMessages(IGatewayConnectorBuddy pointBoxLoginUser) {
        return storage.retrieveTodayMessages(pointBoxLoginUser);
    }

    @Override
    public PbcReleaseInformation checkPbcRelease() {
        return web.checkPointBoxConsoleRelease();
    }

    @Override
    public PointBoxWebServiceResponse checkGatewayLiveness(PointBoxAccountID accountID, ConnectorLivenessQuery connectorLivenessQuery) {
        return web.checkGatewayLiveness(accountID, connectorLivenessQuery);
    }
    
    @Override
    public PointBoxLoginServiceResponse loginPointBoxSystemServer(String accountName, String password) {
        //Delegate to PbcWeb so as to login....
        PointBoxLoginServiceResponse response = web.requestLoginPointBoxSystem(accountName, password);
        if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
            //start the thread polling data...
            raisePointBoxEvent(new ServerLoginStatusEvent(PointBoxEventTarget.PbcFace,
                                                              "Start communicating with the PointBox server...",
                                                              GatewayServerType.PBIM_SERVER_TYPE));

        }
        return response;
    }

    @Override
    public PointBoxLoginServiceResponse reloginPointBoxSystemServer(IGatewayConnectorBuddy pbimLoginUser) {
        //Delegate to PbcWeb so as to login....
        PointBoxLoginServiceResponse response = web.requestReLoginPointBoxSystem(pbimLoginUser);
        if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
            //start the thread polling data...
            raisePointBoxEvent(new ServerLoginStatusEvent(PointBoxEventTarget.PbcFace,
                                                              "Start re-communicating with the PointBox server...",
                                                              GatewayServerType.PBIM_SERVER_TYPE));

        }
        return response;
    }

    @Override
    public PointBoxLoginPublicImServiceResponse loginRemoteServer(IGatewayConnectorBuddy loginUser) {
        //Delegate to PbcWeb so as to login....
        PointBoxLoginPublicImServiceResponse response = web.requestLoginRemoteServer(getPointBoxAccountID(), loginUser);
        if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
            //start the thread polling data...
            raisePointBoxEvent(new ServerLoginStatusEvent(PointBoxEventTarget.PbcFace,
                                                          "Start communicating with a remote server of " + loginUser.getIMServerType() + "...",
                                                          loginUser.getIMServerType()));

        }
        return response;
    }

    @Override
    public PointBoxWebServiceResponse logoutRemoteServer(IGatewayConnectorBuddy loginUser) {
        PointBoxWebServiceResponse response = web.requestLogoutRemoteServer(getPointBoxAccountID(),
                                                                            getPointBoxConnectorID(loginUser));
        if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
            //start the thread polling data...
            raisePointBoxEvent(new ServerLoginStatusEvent(PointBoxEventTarget.PbcFace,
                                                          "Waiting for logout result of" + loginUser.getIMScreenName() + "...",
                                                          loginUser.getIMServerType()));

        }
        return response;
    }

    /**
     * This is used to log into system by killing existing connection under the same account.
     * <p>
     * This method is blocking-mode.
     * @param accountName
     * @param password
     * @return
     */
    @Override
    public PointBoxLoginServiceResponse forceLoginPointBoxSystem(IGatewayConnectorBuddy pointBoxLoginUser) {
        return web.requestForceLoginPointBoxSystem(pointBoxLoginUser);
    }

    @Override
    public PointBoxWebServiceResponse sendInstantMessage(PointBoxAccountID pointBoxAccountID,
                                                         PointBoxConnectorID pointBoxConnectorID,
                                                         IPbsysInstantMessage quoteMessage) {
        return web.requestSendInstantMessage(pointBoxAccountID, pointBoxConnectorID, quoteMessage);
    }

    @Override
    public void distributeInstantMessages(String message, IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> buddiesWithLoginUsers) {
        (new MessageBroadcastor(message, group, buddiesWithLoginUsers)).execute();
    }

    @Override
    public PointBoxWebServiceResponse addNewConnectorBuddy(IGatewayConnectorBuddy masterLoginUser, IGatewayConnectorGroup existingGroup, String newBuddyName) {
        return web.requestAddNewConnectorBuddy(this.getPointBoxAccountID(),
                                               this.getPointBoxConnectorID(masterLoginUser), 
                                               existingGroup, newBuddyName);
    }

    @Override
    public IGatewayConnectorGroup addNewConnectorGroup(IGatewayConnectorBuddy masterLoginUser, String groupName) {
        return web.requestAddNewConnectorGroup(this.getPointBoxAccountID(),
                                               this.getPointBoxConnectorID(masterLoginUser), groupName);
    }

    @Override
    public PointBoxWebServiceResponse refuseNewBuddyAuthorization(IGatewayConnectorBuddy masterLoginUser, String newBuddyName) {
        return web.requestRefuseNewBuddyAuthorization(this.getPointBoxAccountID(),
                                               this.getPointBoxConnectorID(masterLoginUser), newBuddyName);
    }

    @Override
    public PointBoxWebServiceResponse changeConnectorBuddyFromGroupToGroup(IGatewayConnectorBuddy masterLoginUser, 
                                                                           String oldGroupName, 
                                                                           String newGroupName, 
                                                                           IGatewayConnectorBuddy aBuddy, 
                                                                           boolean invokeNotification) 
    {
        return web.requestChangeConnectorBuddyFromGroupToGroup(this.getPointBoxAccountID(),
                                                               this.getPointBoxConnectorID(masterLoginUser),
                                                               oldGroupName, newGroupName, aBuddy, invokeNotification);
    }

    @Override
    public PointBoxWebServiceResponse acceptNewBuddyAuthorization(IGatewayConnectorBuddy masterLoginUser, String newBuddyName) {
        return web.requestAcceptNewBuddyAuthorization(this.getPointBoxAccountID(),
                                                      this.getPointBoxConnectorID(masterLoginUser), 
                                                      newBuddyName);
    }

    @Override
    public PointBoxWebServiceResponse deleteConnectorBuddy(IGatewayConnectorBuddy masterLoginUser, IGatewayConnectorBuddy aBuddy, IGatewayConnectorGroup aGroup) {
        return web.requestDeleteConnectorBuddy(this.getPointBoxAccountID(),
                                               this.getPointBoxConnectorID(masterLoginUser), 
                                               aBuddy, aGroup);
    }

    @Override
    public void notifyMsAccessInterrupted(SQLException ex) {
        String errMsg = "Failed to store records into MERM Access databse. Please confirm and reset its connection from Preference windows.";
        if (face != null){
            face.notifyMsAccessInterrupted(errMsg);
        }
        if ((ex == null) || (DataGlobal.isEmptyNullString(ex.getMessage()))){
            updateSplashScreen(errMsg, 
                                Level.SEVERE, 
                                10);
        }else{
            updateSplashScreen(ex.getMessage(), 
                                Level.SEVERE, 
                                10);
        }
    }

    @Override
    public void updateSplashScreen(String msg, Level level, long duration) {
        if ((splash != null) && (splash.isVisible())){
            splash.updateSplashScreen(msg, level, duration);
        }
    }

    @Override
    public void switchPbcSplashScreen(IPbcSplashScreen screen) {
        splash = screen;
    }

    @Override
    public boolean connectToLocalWhenTech() {
        return pricingAgent.connectToLocalWhenTech();
    }

    @Override
    public void tryToPriceQuotes(ArrayList<IPbsysOptionQuote> quotes) {
        pricingAgent.evaluateQuotesPrice(quotes);
    }

    @Override
    public void pricePbsysSingleQuote(IPbsysOptionQuote aQuote) {
        pricingAgent.evaluatePriceForPbsysOptionQuote(aQuote);
    }
    
    @Override
    public void registerPbcComponent(IPbcComponent component) {
        if (component instanceof IPbcFace){
            face = (IPbcFace)component;
        }else if (component instanceof IPbcRuntime){
            runtime = (IPbcRuntime)component;
        }else if (component instanceof IPbcStorage){
            storage = (IPbcStorage)component;
        }else if (component instanceof IPbcPricingAgent){
            pricingAgent = (IPbcPricingAgent)component;
//        }else if (component instanceof IPbcParser){
//            parser = (IPbcParser)component;
        }else if (component instanceof IPointBoxConsoleWebProxy){
            web = (IPointBoxConsoleWebProxy)component;
        }else{
            logger.log(Level.WARNING, "The type of IPbcComponent instance is unknown to the engine.");
        }
    }

    /**
     * load all the components into memory so as to be ready for use by GUI
     * @throws PointBoxFatalException
     */
    private void loadComponents() throws PointBoxFatalException{
        updateSplashScreen("Start loading components of " + getSoftwareName() + "'s kernel...", Level.INFO, 300);
        //the loading sequence is important
        if (storage == null){
            throw new PointBoxFatalException("PointBox storage was not ready successfully.");
        }
        storage.load();
        if (web == null){
            throw new PointBoxFatalException("PointBox client-side web agent was not launched successfully.");
        }
        if (web instanceof IPbcComponent){
            ((IPbcComponent)web).load();
        }
        if (runtime == null){
            throw new PointBoxFatalException("PointBox runtime environment was not initialized successfully.");
        }
        runtime.load();
//        if (parser == null){
//            throw new PointBoxFatalException("PointBox parser was not loaded successfully.");
//        }
//        parser.load();
        if (pricingAgent == null){
            throw new PointBoxFatalException("PointBox pricer was not loaded successfully.");
        }
        pricingAgent.load();
        if (face == null){
            throw new PointBoxFatalException("PointBox console was not created successfully.");
        }
        face.load();
    }

    @Override
    public void personalize() {
        IGatewayConnectorBuddy pointBoxLoginUser = face.getPointBoxLoginUser();
        if (pointBoxLoginUser == null){
            return;
        }
        updateSplashScreen("Start personalizing components of " + getSoftwareName() + "'s kernel for " + pointBoxLoginUser.getIMScreenName() + "...", Level.INFO, 300);
        //personalize components
        runtime.personalize();
        storage.personalize();
        if (web instanceof IPbcComponent){
            ((IPbcComponent)web).personalize();
        }
//        parser.personalize();
        pricingAgent.personalize();
        face.personalize();
        //switch splash screen
        splash = (IPbcSplashScreen)face.getPointBoxMainFrame();
    }

    /**
     * Unload all the components (i.e., release the resources) before shut down
     */
    private void unloadComponents(){
        try{
            if (face != null){
                updateSplashScreen("Start unloading components of " + getSoftwareName() + "'s kernel...", Level.INFO, 300);
                //start unloading...
                face.unload();
            }
            if (pricingAgent != null){
                pricingAgent.unload();
            }
            if (storage != null){
                storage.unload();
            }
            if (runtime != null){
                runtime.unload();
            }
            if (web instanceof IPbcComponent){
                ((IPbcComponent)web).unload();
            }
        }catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    
    /**
     * Invoke the entire PointBox console
     */
    private class InvokeWorker extends Thread{
        @Override
        public void run(){
            try {
                loadComponents();
                //display GUI ...
                face.getLoginWindow(GatewayServerType.PBIM_SERVER_TYPE).displayFaceComponent();
                if (splash.isVisible()){
                    splash.close();
                    splash = (IPbcSplashScreen)face.getLoginWindow(GatewayServerType.PBIM_SERVER_TYPE);
                }
            }catch (WebServiceException ex){
                handleInvokeWorkerException(ex);
            } catch (Exception ex) {
                handleInvokeWorkerException(ex);
            }
        }//run

        private void handleInvokeWorkerException(Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            unloadComponents();
            //display fatal error window before shut down
            final String errMsg = ex.getMessage();
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(face.getPointBoxMainFrame(), "[Fatal Error] " + errMsg);
                    Runtime.getRuntime().exit(1);
                }
            });
        }

    }//InvokeWorker

    private class ShutdownWorker extends SwingWorker<Void, Void>{

        private int confirmed;
        private boolean unloaded;
        private boolean hasWarningDialog=true;          //when shut down the PBC, it will pop out the box depended on this flag
        private boolean shutDownPBC;
        
        /**
         * @param shutDownPBC - whether or not shut down PBC after sent out logout-request to the server
         */
        ShutdownWorker(boolean shutDownPBC) {
            confirmed = -1;
            unloaded = false;
            this.shutDownPBC = shutDownPBC;
        }
        
        /**
         * @param hasWarningDialog
         * @param shutDownPBC - whether or not shut down PBC after sent out logout-request to the server 
         */
        ShutdownWorker(boolean hasWarningDialog, boolean shutDownPBC) {
            this.hasWarningDialog = hasWarningDialog;
            confirmed = -1;
            unloaded = false;
            this.shutDownPBC = shutDownPBC;
        }

        @Override
        protected Void doInBackground() throws Exception {
            SwingUtilities.invokeAndWait(new Runnable(){
                @Override
                public void run() {
                    
                    if(hasWarningDialog){       //as a defualt, it will pop out the dialog.
                        if (JOptionPane.showConfirmDialog(face.getPointBoxMainFrame(),
                                "Shutdown PointBox Console?",
                                "PointBox Console",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                        {
                            confirmed = 1;
                        }else{
                            confirmed = 0;
                        }
                    }else{
                        confirmed = 1;
                    }
                }
            });
            try{
                if (confirmed == 1){
                    publish();

                    if (getPointBoxAccountID() == null){
                        //say bye-bye to the server and no any successful login yet
                        web.requestFarewellToPointBoxSystem();
                    }else{
                        web.requestLogoutPointBoxSystem(getPointBoxAccountID());
                    }
                    if (shutDownPBC){
                        //unload...
                        unloadComponents();

                        unloaded = true;
                    }
                }
            }catch (Exception ex){}
            return null;
        }//end 

        @Override
        protected void process(List<Void> chunks) {
            //face.getFarewellSplashWindow().display();
        }

        @Override
        protected void done() {
            if (!shutDownPBC){
                //return control to an offline PBC
                return;
            }
            try {
                get(60, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //quit from JVM
                Runtime.getRuntime().exit(0);
            } catch (ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
                //quit from JVM
                Runtime.getRuntime().exit(0);
            } catch (TimeoutException ex) {
                logger.log(Level.SEVERE, null, ex);
                //quit from JVM
                Runtime.getRuntime().exit(0);
            }
            if (confirmed == 1){
                try{
                    if (!unloaded){
                        //unload...
                        unloadComponents();
                    }
                }catch (Exception ex){
                    //quit from JVM
                    Runtime.getRuntime().exit(0);
                }
                //quit from JVM
                Runtime.getRuntime().exit(0);
            }
        }
    }//class

    private void updatePointBoxFrameStatusBar(Integer value, String message) {
        face.updatePointBoxFrameStatusBar(value, message);
    }

    @Override
    public void raisePointBoxEvent(final PointBoxConsoleEvent event) {
        eventService.submit(new RaisePointBoxEventTask(event));
    }
    
    private class RaisePointBoxEventTask implements Runnable{
        private PointBoxConsoleEvent event;
        RaisePointBoxEventTask(PointBoxConsoleEvent event) {
            this.event = event;
        }
        @Override
        public void run() {
            switch (event.getPointBoxEventTarget()){
                case PbcFace:
                    face.handleComponentEvent(event);
                    break;
                case PbcRuntime:
                    runtime.handleComponentEvent(event);
                    break;
                case PbcWeb:
                    if (web instanceof IPbcComponent){
                        ((IPbcComponent)web).handleComponentEvent(event);
                    }
                    break;
                case PbcStorage:
                    storage.handleComponentEvent(event);
                    break;
//                case PbcParser:
//                    parser.handleComponentEvent(event);
//                    break;
                case PbcPricer:
                    pricingAgent.handleComponentEvent(event);
                    break;
                default:
                    logger.log(Level.WARNING, "An unknown event raised can not be be dispatched");
            }//switch
        }
    
    }

    /**
     * All the broadcasting messages will be processed by this class
     */
    private class MessageBroadcastor extends SwingWorker<PointBoxWebServiceResponse, Float> {
        private final ArrayList<IGatewayConnectorBuddy> buddies;
        private final String message;
        private final IGatewayConnectorGroup group;

        MessageBroadcastor(final String message, IGatewayConnectorGroup group, 
                           final ArrayList<IGatewayConnectorBuddy> buddies)
        {
            this.buddies = buddies;
            this.message = message;
            this.group = group;

            addPropertyChangeListener(new PropertyChangeListener(){
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress".equals(evt.getPropertyName())) {
                        updatePointBoxFrameStatusBar((Integer)evt.getNewValue(), "Broadcasting ...");
                    }
                }
            });
        }

        @Override
        protected PointBoxWebServiceResponse doInBackground() throws Exception {
            IBroadcastedMessage broadcastedMessage = PbconsoleQuoteFactory.createBroadcastedMessageInstance(group, buddies);
            broadcastedMessage.setMessageContent(message);
            //msg.setToUser(buddy);
            broadcastedMessage.setOutgoing(true);
            //msg.setIMServerType(serverType);
//            broadcastedMessage.setMessageTimestamp(new GregorianCalendar());
            //msg.setFromUser(loginUser);
            broadcastedMessage.setMessageContent(message);
            return web.requestBroadcastInstantMessage(getPointBoxAccountID(), 
                                                      getPointBoxAccountID(), 
                                                      broadcastedMessage);
        }

        @Override
        protected void done() {
            try {
                PointBoxWebServiceResponse response = get();
                if (response.getResult().equals(PointBoxServiceResult.RequestExecuted)){
                    updateSplashScreen("Broadcasted to member of " + group.getGroupName(), Level.INFO, 10);
                }else{
                    updateSplashScreen("Failed to broadcast member of " + group.getGroupName(), Level.INFO, 10);
                }
            } catch (InterruptedException ex) {
                PointBoxTracer.recordSevereException(logger, ex);
            } catch (ExecutionException ex) {
                PointBoxTracer.recordSevereException(logger, ex);
            }
        }
    }

    @Override
    public void storeBlackNameEntry(String blackBuddyScreenName, String pbcLoginUserUniqueName,String buddyOwnerloginUserUniqueName) {
        web.storeBlackNameEntry(blackBuddyScreenName, pbcLoginUserUniqueName, buddyOwnerloginUserUniqueName);
    }

    @Override
    public void removeBlackNameEntry(String pbcLoginUserUniqueName, String blackBuddyScreenName, String blackBuddyServerType, String blackBuddyOwnerLoginUserScreenName) {
        web.removeBlackNameEntry(pbcLoginUserUniqueName, blackBuddyScreenName, blackBuddyServerType, blackBuddyOwnerLoginUserScreenName);
    }

    @Override
    public List<String> retrieveAllBlackNames(String pbcLoginUserUniqueName) {
        return web.retrieveAllBlackNames(pbcLoginUserUniqueName);
    }
    
}//PbcKernel

