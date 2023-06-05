/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxConsoleEvent;
import com.eclipsemarkets.event.face.MermAccessRecordChangedEvent;
import com.eclipsemarkets.event.parser.QuoteParsedEvent;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import com.eclipsemarkets.gateway.data.PbconsoleQuoteFactory;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.exceptions.PointBoxException;
import com.eclipsemarkets.pbc.PbcComponent;
import com.eclipsemarkets.pbc.PbcProperties;
import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.PbcReleaseUserType;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerSearchCriteria;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.settings.IPointBoxTalkerSettings;
import com.eclipsemarkets.pbc.runtime.settings.record.*;
import com.eclipsemarkets.pbc.storage.exceptions.DatabaseNotReady;
import com.eclipsemarkets.pbc.storage.merm.IMermQuoteRecorder;
import com.eclipsemarkets.pbc.storage.merm.IMermQuoteRetriever;
import com.eclipsemarkets.pbc.storage.merm.MermStorageFactory;
import com.eclipsemarkets.pbc.storage.sql.*;
import com.eclipsemarkets.pbc.tester.PointBoxTesterCaseDialog;
import com.eclipsemarkets.runtime.IPointBoxAutoPricerConfig;
import com.eclipsemarkets.runtime.IPointBoxPricerConfig;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.Color;
import java.awt.Font;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Abstract of Derby database which is called "PbcStorage". This storage uses of 
 * server-side database and consider "transaction" in implementation. Thus, its 
 * objective is to solve "losing settings" issue and "portable issue" in PbcStoirage1.
 * @author Zhijun Zhang
 */
class PbcStorage extends PbcComponent implements IPbcStorage{

    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbcStorage.class.getName());
    }
    
    //private IPointBoxClientReleasePolicy emsReleasePolicy;

    private IMermQuoteRecorder mermQuoteRecorder;
    private IMermQuoteRetriever mermQuoteRetriever;
    private PbconsoleStorageEngine storageEngine;
    
    private PbcStorageAgent pbcStorageAgent;

    PbcStorage(IPbcKernel kernel) {
        super(kernel);
        kernel.updateSplashScreen("Register " + getKernel().getSoftwareName() + "'s storage component...", Level.INFO, 100);
        storageEngine = new PbconsoleStorageEngine(this);
        mermQuoteRecorder = null;   //lately-loaded, i.e., personalize
        mermQuoteRetriever = null;  //lately-loaded, i.e., personalize
    }

    @Override
    public void load() throws PointBoxFatalException {
        getKernel().updateSplashScreen("Load " + getKernel().getSoftwareName() + "'s storage ...", Level.INFO, 100);
        try {
            storageEngine.invokeStorageEngine();
        } catch (DatabaseNotReady ex) {
            if (ex.getReason().equals(DatabaseNotReady.Reason.INSTANCE_CONFLICT)){
                shutdown(ex);
            }else{
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        pbcStorageAgent = new PbcStorageAgent();
    }

    private String getMermAccessPath() {
        try{
            return PointBoxConsoleProperties.getSingleton().retrieveMermAccessPath();
        }catch (Exception ex){
            //logger.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public void personalize() {
        getKernel().updateSplashScreen("Personalize " + getKernel().getSoftwareName() + "'s storage ...", Level.INFO, 100);
        if (pbcStorageAgent == null){
            pbcStorageAgent = new PbcStorageAgent();
        }
        String msPath = getMermAccessPath();
        if (NIOGlobal.isValidTypedFile(msPath, "mdb")){
            pbcStorageAgent.startAgent();
        }
    }
    
    /**
     * From settings panel, users reset MDB path
     */
    private void populateMermAccessRecordFromSettings(){
        synchronized(this){
            try {
                String msPath = getMermAccessPath();
                if (NIOGlobal.isValidTypedFile(msPath, "mdb")){
                    IMermQuoteRecorder recorder = getMermQuoteRecorder(getKernel());
                    if (recorder == null){
                        pbcStorageAgent.stopAgent();
                    }else{
                        recorder.changeMdbPath(msPath);
                        pbcStorageAgent.startAgent();
                    }
                    //getMermQuoteRetriever().changeMdbPath(msPath);
                }else{
                    pbcStorageAgent.stopAgent();
                }
            } catch (PointBoxException ex) {
                //PointBoxConsoleProperties.getSingleton().storeMermAccessPath(getKernel().getDefaultQuoteCommodityType(), "");
                pbcStorageAgent.stopAgent();
            }
        }
    }

    @Override
    public void unload() {
        getKernel().updateSplashScreen("Shut down " + getKernel().getSoftwareName() + "'s storage ...", Level.INFO, 100);

        pbcStorageAgent.stopAgent();
        try {
            if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
                IMermQuoteRecorder recorder = getMermQuoteRecorder(getKernel());
                if (recorder != null){
                    recorder.shutdown();
                }
                //getMermQuoteRetriever().shutdown();
            }
        } catch (PointBoxException ex) {
            //logger.log(Level.SEVERE, null, ex);
        }
            
        if (storageEngine != null){
            storageEngine.shutdown();
        }
    }

    private void shutdown(final DatabaseNotReady ex){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, ex.getReason().toString());
                Runtime.getRuntime().exit(0);
            }
        });
        try {
            Thread.sleep(60000);    //in case, noboday response to the message box.
        } catch (InterruptedException ex1) {
        }
        Runtime.getRuntime().exit(0);
    }

    private IMermQuoteRecorder getMermQuoteRecorder(IPbcKernel kernel) throws PointBoxException{
        synchronized(this){
            if (mermQuoteRecorder == null){
                 if(getKernel().getPbcReleaseUserType().equals(PbcReleaseUserType.DEBUG_USERS)){
                     String path=PointBoxTesterCaseDialog.accessPath;
                     if(NIOGlobal.isValidTypedFile(path, "mdb"));
                        mermQuoteRecorder = MermStorageFactory.createMermQuoteRecorderSingleton(kernel, path);
                  }
                 if(getKernel().getPbcReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
                    mermQuoteRecorder = MermStorageFactory.createMermQuoteRecorderSingleton(kernel, getMermAccessPath());
                 }
            }
            if (mermQuoteRecorder == null){
                throw new PointBoxException("In valid path for mermQuoteRecorder");
            }
            return mermQuoteRecorder;
        }
    }

    @Override
    public IMermQuoteRetriever getMermQuoteRetriever() throws PointBoxException {
        synchronized(this){
            if (mermQuoteRetriever == null){
                mermQuoteRetriever = MermStorageFactory.createMermQuoteRetrieverInstance(getMermAccessPath());
            }
            if (mermQuoteRetriever == null){
                throw new PointBoxException("In valid path for mermQuoteRetriever");
            }
            return mermQuoteRetriever;
        }
    }

    private void storeOptionQuotesForThirdParty(ArrayList<IPbsysOptionQuote> quotes) throws SQLException {
        if (quotes == null){
            return;
        }
        if (getKernel().getPbcReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)
                || (getKernel().getPbcReleaseUserType().equals(PbcReleaseUserType.DEBUG_USERS)
                && PointBoxTesterCaseDialog.accessPath!=null))
        {
            try {
                IMermQuoteRecorder recorder = getMermQuoteRecorder(getKernel());
                if (recorder != null){
                    recorder.storeMermOptionQuotes(quotes);
                }
            } catch (PointBoxException ex) {
            }
        }
    }

//    public EmsEntityTypeWrapper getEmsEntityTypeWrapper() {
//        return EmsEntityTypeWrapper.EmsStorageType_PointBoxConsole;
//    }

    private String getOwnerUniqueName(IGatewayConnectorBuddy pointBoxLoginUser){
        String ownerUniqueName;
        if (pointBoxLoginUser == null){
            ownerUniqueName = PbcDatabaseInstance.DefaultEmsUser.toString();
        }else{
            ownerUniqueName = pointBoxLoginUser.getIMUniqueName();
        }
        return ownerUniqueName;
    }

    /**
     * @deprecated 
     * @param record
     * @param pointBoxLoginUser
     * @return 
     */
    @Override
    public boolean loadAutoPricerRecord(IPointBoxAutoPricerConfig record, IGatewayConnectorBuddy pointBoxLoginUser) {
        
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        record = null;
        boolean result = false;
        if (record == null){
            return result;
        }
        String ownerName = getOwnerUniqueName(pointBoxLoginUser);
        try{
            Connection conn = storageEngine.getConnection();
            PreparedStatement prst = conn.prepareStatement(PB_AutoPricerSettingsTable.SelectSpecificRecord);

            prst.setString(1, ownerName);
            prst.setString(2, "1");  //todo: currently assume this table only one record
            ResultSet resultSet = prst.executeQuery();

            while(resultSet.next()){
                if (resultSet.getShort(PB_AutoPricerSettingsTable.Schema.StopRefreshingPrice.toString()) == 1){
                    record.setRepricingStopped(true);
                }else{
                    record.setRepricingStopped(false);
                }
                if (resultSet.getShort(PB_AutoPricerSettingsTable.Schema.RefreshAllQuotes.toString()) == 1){
                    record.setRepricingAll(true);
                }else{
                    record.setRepricingStopped(false);
                }
                record.setOwnerUniqueName(resultSet.getString(PB_SystemFrameSettingsTable.Schema.UserOwner.toString()));

                result = true;
                break;
            }//while
            resultSet.close();
            prst.close();
            conn.close();
        }catch (SQLException ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * @deprecated 
     * @param record 
     */
    @Override
    public void storeAutoPricerRecord(IPointBoxAutoPricerConfig record) {
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        record = null;
        
        if (record == null){
            return;
        }
        try{
            Connection conn = storageEngine.getConnection();
            PreparedStatement deleteStmt = conn.prepareStatement(PB_AutoPricerSettingsTable.DeleteSpecificRecord);
            PreparedStatement insertStmt = conn.prepareStatement(PB_AutoPricerSettingsTable.InsertNewRecord);

            deleteStmt.setString(1, record.getOwnerUniqueName());
            deleteStmt.setString(2, "1");  //todo: currently assume this table only one record
            deleteStmt.executeUpdate();
            
            insertStmt.setString(1, record.getOwnerUniqueName());
            insertStmt.setString(2, "1");
            insertStmt.setInt(3, record.getLatestQuoteNumber());
            insertStmt.setLong(4, record.getInitialDelay());
            insertStmt.setLong(5, record.getPricingFrequency());
            insertStmt.setShort(6, (record.isRepricingAll() ? (short)1 : (short)0));
            insertStmt.setShort(7, (record.isRepricingStopped() ? (short)1 : (short)0));
            insertStmt.executeUpdate();

            deleteStmt.close();
            insertStmt.close();
            conn.close();
        }catch (SQLException ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    /**
     * @deprecated 
     * @param record
     * @param pointBoxLoginUser
     * @return 
     */
    @Override
    public boolean loadPricerRecord(IPointBoxPricerConfig record, IGatewayConnectorBuddy pointBoxLoginUser) {
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return false;
        }
        
        boolean result = false;
        if (record == null){
            return result;
        }
        String ownerName = getOwnerUniqueName(pointBoxLoginUser);
        try{
            Connection conn = storageEngine.getConnection();
            PreparedStatement prst = conn.prepareStatement(PB_PricerSettingsTable.SelectSpecificRecord);

            prst.setString(1, ownerName);
            prst.setString(2, "1");  //todo: currently assume this table only one record
            ResultSet resultSet = prst.executeQuery();

            while(resultSet.next()){
                if (resultSet.getShort(PB_PricerSettingsTable.Schema.FiveYearLimit.toString()) == 1){
                    record.setFiveYearLimit(true);
                }else{
                    record.setFiveYearLimit(false);
                }
                record.setOwnerUniqueName(resultSet.getString(PB_PricerSettingsTable.Schema.UserOwner.toString()));
                record.setTValueAtExp(resultSet.getInt(PB_PricerSettingsTable.Schema.TValueAtExp.toString()));
                result = true;
                break;
            }//while
            resultSet.close();
            prst.close();
            conn.close();
        }catch (SQLException ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * @deprecated 
     * @param record 
     */
    @Override
    public void storePricerRecord(IPointBoxPricerConfig record) {
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return;
        }
        
        if (record == null){
            return;
        }
        try{
            Connection conn = storageEngine.getConnection();
            PreparedStatement deleteStmt = conn.prepareStatement(PB_PricerSettingsTable.DeleteSpecificRecord);
            PreparedStatement insertStmt = conn.prepareStatement(PB_PricerSettingsTable.InsertNewRecord);

            deleteStmt.setString(1, record.getOwnerUniqueName());
            deleteStmt.setString(2, "1");  //todo: currently assume this table only one record
            deleteStmt.executeUpdate();

            insertStmt.setString(1, record.getOwnerUniqueName());
            insertStmt.setString(2, "1");
            insertStmt.setShort(3, (record.isFiveYearLimit() ? (short)1 : (short)0));
            insertStmt.setInt(4, record.getTValueAtExp());
            insertStmt.executeUpdate();

            deleteStmt.close();
            insertStmt.close();
            conn.close();
        }catch (SQLException ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    /**
     * @deprecated 
     * @param pointBoxLoginUser
     * @param pointBoxTalkerSettings 
     */
    @Override
    public void storePointBoxTalkerSettings(IGatewayConnectorBuddy pointBoxLoginUser, 
                                            IPointBoxTalkerSettings pointBoxTalkerSettings)
    {
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return;
        }
        
        if (pointBoxTalkerSettings == null){
            return;
        }
        String ownerId = getOwnerUniqueName(pointBoxLoginUser);
        try{
            Connection conn = storageEngine.getConnection();

            PreparedStatement deleteStmt = conn.prepareStatement(PB_MessageTabSettingsTable.DeleteRecordsByOwner);
            deleteStmt.setString(1, ownerId);
            deleteStmt.executeUpdate();
            deleteStmt.clearParameters();
            deleteStmt.close();

            PreparedStatement insertStmt = conn.prepareStatement(PB_MessageTabSettingsTable.InsertNewRecord);
            IMessageTabRecord messageTabRecord;
            HashMap<String, IMessageTabRecord> records = pointBoxTalkerSettings.getMessageTabRecords();
            Set<String> keys = records.keySet();
            Iterator<String> itr = keys.iterator();
            while (itr.hasNext()){
                messageTabRecord = records.get(itr.next());
                insertStmt.setString(1, ownerId);
                insertStmt.setString(2, messageTabRecord.getMessageTabID());
                insertStmt.setString(3, messageTabRecord.getMyFont().getFamily());
                insertStmt.setInt(4, messageTabRecord.getMyFont().getStyle());
                insertStmt.setInt(5, messageTabRecord.getMyFont().getSize());
                insertStmt.setInt(6, messageTabRecord.getMyForeground().getRGB());
                insertStmt.setString(7, messageTabRecord.getBuddyFont().getFamily());
                insertStmt.setInt(8, messageTabRecord.getBuddyFont().getStyle());
                insertStmt.setInt(9, messageTabRecord.getBuddyFont().getSize());
                insertStmt.setInt(10, messageTabRecord.getBuddyForeground().getRGB());
                insertStmt.setShort(11, (messageTabRecord.isDisplayTimestamp() ? (short)1 : (short)0));
                insertStmt.setShort(12, (messageTabRecord.isDisplayPrices() ? (short)1 : (short)0));
                insertStmt.executeUpdate();
                insertStmt.clearParameters();
            }//while: next file_path
            insertStmt.close();
            conn.close();
        }catch (SQLException ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    /**
     * @deprecated 
     * @param pointBoxLoginUser
     * @param pointBoxTalkerSettings
     * @return 
     */
    @Override
    public boolean loadPointBoxTalkerSettings(IGatewayConnectorBuddy pointBoxLoginUser,
                                              IPointBoxTalkerSettings pointBoxTalkerSettings)
    {
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return false;
        }
        
        
        boolean result = false;
        String ownerId = getOwnerUniqueName(pointBoxLoginUser);
        if (pointBoxTalkerSettings != null){
            try{
                Connection conn = storageEngine.getConnection();
                ResultSet resultSet;
                PreparedStatement prst = conn.prepareStatement(PB_MessageTabSettingsTable.SelectRecordsByOwner);
                IMessageTabRecord messageTabRecord;
                String messageTabID;
                prst.setString(1, ownerId);
                resultSet = prst.executeQuery();
                while(resultSet.next()){
                    messageTabID = resultSet.getString(PB_MessageTabSettingsTable.Schema.MessageTabID.toString());
                    
                    messageTabRecord = pointBoxTalkerSettings.getMessageTabRecord(messageTabID);
                    messageTabRecord.setOwnerUniqueName(ownerId);
                    messageTabRecord.setMessageTabID(messageTabID);
                    messageTabRecord.setBuddyFont(new Font(resultSet.getString(PB_MessageTabSettingsTable.Schema.BuddyFontFamily.toString()),
                                                   resultSet.getInt(PB_MessageTabSettingsTable.Schema.BuddyFontStyle.toString()),
                                                   resultSet.getInt(PB_MessageTabSettingsTable.Schema.BuddyFontSize.toString())));
                    messageTabRecord.setBuddyForeground(new Color(resultSet.getInt(PB_MessageTabSettingsTable.Schema.BuddyForeground.toString())));
                    messageTabRecord.setMyFont(new Font(resultSet.getString(PB_MessageTabSettingsTable.Schema.MyFontFamily.toString()),
                                                   resultSet.getInt(PB_MessageTabSettingsTable.Schema.MyFontStyle.toString()),
                                                   resultSet.getInt(PB_MessageTabSettingsTable.Schema.MyFontSize.toString())));
                    messageTabRecord.setMyForeground(new Color(resultSet.getInt(PB_MessageTabSettingsTable.Schema.MyForeground.toString())));
                    if (resultSet.getShort(PB_MessageTabSettingsTable.Schema.DisplayPrices.toString()) == 1){
                        messageTabRecord.setDisplayPrices(true);
                    }else{
                        messageTabRecord.setDisplayPrices(false);
                    }
                    if (resultSet.getShort(PB_MessageTabSettingsTable.Schema.DisplayTimestamp.toString()) == 1){
                        messageTabRecord.setDisplayTimestamp(true);
                    }else{
                        messageTabRecord.setDisplayTimestamp(false);
                    }
                }//while

                result = true;

                resultSet.close();
                prst.clearParameters();
                prst.close();
                conn.close();
            }catch (SQLException ex){
                logger.log(Level.WARNING, ex.getMessage(), ex);
                result = false;
            }
        }
        return result;
    }
    
    /**
     * @deprecated 
     * @param pointBoxLoginUser
     * @return 
     */
    @Override
    public ArrayList<IPbsysInstantMessage> retrieveTodayMessages(IGatewayConnectorBuddy pointBoxLoginUser) {

        ArrayList<IPbsysInstantMessage> data = new ArrayList<IPbsysInstantMessage>();
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return data;
        }
        
        if (pointBoxLoginUser != null){
            try{
                Connection conn = storageEngine.getConnection();

                PreparedStatement selectStmt = conn.prepareStatement(PB_InstantMessageTable.SelectHistoricalMessages);
                selectStmt.setTimestamp(1, new Timestamp(CalendarGlobal.getToday(0, 0, 0).getTimeInMillis()));
                selectStmt.setTimestamp(2, new Timestamp((new GregorianCalendar()).getTimeInMillis()));
                selectStmt.setString(3, pointBoxLoginUser.getIMUniqueName());
                ResultSet resultSet = selectStmt.executeQuery();
                IPbsysInstantMessage msg;
                GatewayServerType serverType;
                GregorianCalendar messageTimestamp;
                while (resultSet.next()){
                    serverType = GatewayServerType.convertToType(
                            resultSet.getString(PB_InstantMessageTable.Schema.IM_SERVER_TYPE.toString()));
                    //message instance with server type
                    msg = PbconsoleQuoteFactory.createPbsysInstantMessageInstance(serverType);
                    //message content
                    msg.setMessageContent(resultSet.getString(
                            PB_InstantMessageTable.Schema.MESSAGE.toString()));
                    //timstamp
                    messageTimestamp = new GregorianCalendar();
                    messageTimestamp.setTimeInMillis(resultSet.getTimestamp(PB_InstantMessageTable.Schema.MESSAGE_TIMESTAMP.toString()).getTime());
                    msg.setMessageTimestamp(messageTimestamp);
                    //toUser and fromeUser
                    msg.setFromUser(GatewayBuddyListFactory.getLoginUserInstance(
                            resultSet.getString(PB_InstantMessageTable.Schema.FROM_SCREEN_NAME.toString()), 
                                                serverType));
                    msg.setToUser(GatewayBuddyListFactory.getLoginUserInstance(
                            resultSet.getString(PB_InstantMessageTable.Schema.TO_SCREEN_NAME.toString()), 
                            serverType));
                    //outgoing or incoming
                    if (resultSet.getShort(PB_InstantMessageTable.Schema.OUTGOING.toString()) == 1){
                        msg.setOutgoing(true);
                    }else{
                        msg.setOutgoing(false);
                    }
                    //serverType
                    msg.setIMServerType(serverType);
                    msg.setHistoricalMessage(true);
                    data.add(msg);
                }//while
                resultSet.close();
                selectStmt.close();
                conn.close();
            }catch (SQLException ex){
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }

        return data;
    }

    /**
     * @deprecated 
     * @param pointBoxLoginUser
     * @param criteria
     * @return 
     */
    @Override
    public ArrayList<IPbsysOptionQuote> retrieveHistricalQuotes(IGatewayConnectorBuddy pointBoxLoginUser,
                                                                IViewerSearchCriteria criteria) {
        ArrayList<IPbsysOptionQuote> quotes = new ArrayList<IPbsysOptionQuote>();
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return quotes;
        }
        
        if (pointBoxLoginUser != null){
            ArrayList<IPbsysInstantMessage> messages = new ArrayList<IPbsysInstantMessage>();
            try{
                Connection conn = storageEngine.getConnection();
                PreparedStatement selectStmt = constructStatementForHistoricalMessageSearch(conn, pointBoxLoginUser, criteria);
                ResultSet resultSet = selectStmt.executeQuery();
                IPbsysInstantMessage msg;
                GatewayServerType serverType;
                GregorianCalendar messageTimestamp;
                while (resultSet.next()){
                    serverType = GatewayServerType.convertToType(
                            resultSet.getString(PB_InstantMessageTable.Schema.IM_SERVER_TYPE.toString()));
                    //message instance with server type
                    msg = PbconsoleQuoteFactory.createPbsysInstantMessageInstance(serverType);
                    msg.setPbcMessageUuid(resultSet.getString(
                            PB_InstantMessageTable.Schema.MESSAGE_UUID.toString()));
                    //message content
                    msg.setMessageContent(resultSet.getString(
                            PB_InstantMessageTable.Schema.MESSAGE.toString()));
                    //timstamp
                    messageTimestamp = new GregorianCalendar();
                    messageTimestamp.setTimeInMillis(resultSet.getTimestamp(PB_InstantMessageTable.Schema.MESSAGE_TIMESTAMP.toString()).getTime());
                    msg.setMessageTimestamp(messageTimestamp);

                    //PointBoxLogger.printWarningMessage("messageTimestamp -> " + CalendarGlobal.convertToHHmmssSS(messageTimestamp, ":"));

                    //toUser and fromeUser
                    msg.setFromUser(GatewayBuddyListFactory.getLoginUserInstance(
                            resultSet.getString(PB_InstantMessageTable.Schema.FROM_SCREEN_NAME.toString()), 
                            serverType));
                    msg.setToUser(GatewayBuddyListFactory.getLoginUserInstance(
                            resultSet.getString(PB_InstantMessageTable.Schema.TO_SCREEN_NAME.toString()), 
                            serverType));
                    //outgoing or incoming
                    if (resultSet.getShort(PB_InstantMessageTable.Schema.OUTGOING.toString()) == 1){
                        msg.setOutgoing(true);
                    }else{
                        msg.setOutgoing(false);
                    }
                    //serverType
                    msg.setIMServerType(serverType);
                    msg.setHistoricalMessage(true);
                    messages.add(msg);
                }//while

                resultSet.close();
                selectStmt.close();

                if (!messages.isEmpty()){
                    for (int i = 0; i < messages.size(); i++){
                        msg = messages.get(i);
                        IPbsysOptionQuote quote = retrieveSpecificQuoteByMessage(conn, msg);
                        if (quote != null){
                            quotes.add(quote);
                        }
                    }//for
                }
                conn.close();
            }catch (SQLException ex){
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return quotes;
    }

    /**
     * @deprecated 
     * @param pointBoxLoginUser
     * @return 
     */
    @Override
    public ArrayList<IPbsysOptionQuote> retrieveTodayQuotes(IGatewayConnectorBuddy pointBoxLoginUser) {
        ArrayList<IPbsysOptionQuote> quotes = new ArrayList<IPbsysOptionQuote>();
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return quotes;
        }
        if (pointBoxLoginUser != null){
            ArrayList<IPbsysInstantMessage> messages = new ArrayList<IPbsysInstantMessage>();
            try{
                Connection conn = storageEngine.getConnection();
                //PreparedStatement selectStmt = constructStatementForHistoricalMessageSearch(conn, pointBoxLoginUser, criteria);
                String sql = PB_InstantMessageTable.SelectAllMessages + " where " 
                            + PB_InstantMessageTable.Schema.MESSAGE_TIMESTAMP + " > ? and " 
                            + PB_InstantMessageTable.Schema.MESSAGE_TIMESTAMP + " < ?";
                PreparedStatement selectStmt = conn.prepareStatement(sql);
                GregorianCalendar dateTime = new GregorianCalendar();
                dateTime.set(Calendar.HOUR_OF_DAY, 0);
                dateTime.set(Calendar.MINUTE, 0);
                dateTime.set(Calendar.SECOND, 0);
                selectStmt.setDate(1, new Date(dateTime.getTimeInMillis()));
                selectStmt.setDate(2, new Date(dateTime.getTimeInMillis() + (24 * 60 * 60 * 1000)));
                ResultSet resultSet = selectStmt.executeQuery();
                IPbsysInstantMessage msg;
                GatewayServerType serverType;
                GregorianCalendar messageTimestamp;
                while (resultSet.next()){
                    serverType = GatewayServerType.convertToType(
                            resultSet.getString(PB_InstantMessageTable.Schema.IM_SERVER_TYPE.toString()));
                    //message instance with server type
                    msg = PbconsoleQuoteFactory.createPbsysInstantMessageInstance(serverType);
                    msg.setPbcMessageUuid(resultSet.getString(
                            PB_InstantMessageTable.Schema.MESSAGE_UUID.toString()));
                    //message content
                    msg.setMessageContent(resultSet.getString(
                            PB_InstantMessageTable.Schema.MESSAGE.toString()));
                    //timstamp
                    messageTimestamp = new GregorianCalendar();
                    messageTimestamp.setTimeInMillis(resultSet.getTimestamp(PB_InstantMessageTable.Schema.MESSAGE_TIMESTAMP.toString()).getTime());
                    msg.setMessageTimestamp(messageTimestamp);

                    //PointBoxLogger.printWarningMessage("messageTimestamp -> " + CalendarGlobal.convertToHHmmssSS(messageTimestamp, ":"));

                    //toUser and fromeUser
                    msg.setFromUser(GatewayBuddyListFactory.getLoginUserInstance(
                            resultSet.getString(PB_InstantMessageTable.Schema.FROM_SCREEN_NAME.toString()), 
                            serverType));
                    msg.setToUser(GatewayBuddyListFactory.getLoginUserInstance(
                            resultSet.getString(PB_InstantMessageTable.Schema.TO_SCREEN_NAME.toString()), 
                            serverType));
                    //outgoing or incoming
                    if (resultSet.getShort(PB_InstantMessageTable.Schema.OUTGOING.toString()) == 1){
                        msg.setOutgoing(true);
                    }else{
                        msg.setOutgoing(false);
                    }
                    //serverType
                    msg.setIMServerType(serverType);
                    msg.setHistoricalMessage(true);
                    messages.add(msg);
                }//while

                resultSet.close();
                selectStmt.close();

                if (!messages.isEmpty()){
                    for (int i = 0; i < messages.size(); i++){
                        msg = messages.get(i);
                        IPbsysOptionQuote quote = retrieveSpecificQuoteByMessage(conn, msg);
                        if (quote != null){
                            quotes.add(quote);
                        }
                    }//for
                }
                conn.close();
            }catch (SQLException ex){
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return quotes;
    }

    private IPbsysOptionQuote retrieveSpecificQuoteByMessage(Connection conn,
                                                               IPbsysInstantMessage msg) throws SQLException
    {
        if (msg == null){
            return null;
        }
        IPbsysOptionQuote quote = null;
        
        PreparedStatement selectStmt = conn.prepareStatement(PB_QuoteTable.SelectSpecificQuoteByMessageID);
        if (msg.getPbcMessageUuid() == null){
            return null;
        }
        selectStmt.setString(1, msg.getPbcMessageUuid());
        ResultSet resultSet = selectStmt.executeQuery();
        ArrayList<IPbsysQuoteLeg> legs;
        while (resultSet.next()){
            quote = PbconsoleQuoteFactory.createPbsysOptionQuoteInstance(getKernel().getDefaultPbcPricingModel());
            quote.setInstantMessage(msg);
            quote.setQuoteUuid(resultSet.getString(PB_QuoteTable.Schema.QUOTE_UUID.toString()));
            quote.setOptionBidPricePrivateIncoming(Double.toString(resultSet.getBigDecimal(
                    PB_QuoteTable.Schema.BID.toString()).doubleValue()));
            quote.setOptionAskPricePrivateIncoming(Double.toString(resultSet.getBigDecimal(
                    PB_QuoteTable.Schema.ASK.toString()).doubleValue()));
            quote.setTradePrice(resultSet.getBigDecimal(PB_QuoteTable.Schema.TRADE.toString()).doubleValue());
            quote.setPrice(resultSet.getBigDecimal(PB_QuoteTable.Schema.PRICE.toString()).doubleValue());
//            quote.setVol(resultSet.getBigDecimal(PB_QuoteTable.Schema.v.toString()).doubleValue());
            quote.setDelta(resultSet.getBigDecimal(PB_QuoteTable.Schema.DELTA.toString()).doubleValue());
            quote.setVega(resultSet.getBigDecimal(PB_QuoteTable.Schema.VEGA.toString()).doubleValue());
            quote.setTheta(resultSet.getBigDecimal(PB_QuoteTable.Schema.THETA.toString()).doubleValue());
            quote.setGamma(resultSet.getBigDecimal(PB_QuoteTable.Schema.GAMMA.toString()).doubleValue());
            quote.setDDelta(resultSet.getBigDecimal(PB_QuoteTable.Schema.DDELTA.toString()).doubleValue());
            quote.setDGamma(resultSet.getBigDecimal(PB_QuoteTable.Schema.DGAMMA.toString()).doubleValue());
            quote.setMeanF01(resultSet.getBigDecimal(PB_QuoteTable.Schema.MEANF01.toString()).doubleValue());
            quote.setMeanF02(resultSet.getBigDecimal(PB_QuoteTable.Schema.MEANF02.toString()).doubleValue());
            int p = (int)resultSet.getShort(PB_QuoteTable.Schema.ISSUFFICIENTFORPRICING.toString());
            if (p == 1){
                quote.setSufficientPricingData(true);
            }else{
                quote.setSufficientPricingData(false);
            }
            legs = quote.getOptionStrategyLegs();
            for (int i = 0; i < legs.size(); i++){
                loadSpecificLegByQuote(conn, quote, legs.get(i), i);
            }
        }//while
        resultSet.close();
        selectStmt.close();

        return quote;
    }

    private void loadSpecificLegByQuote(Connection conn, IPbsysOptionQuote quote, IPbsysQuoteLeg leg, int legIndex) throws SQLException {
        if (quote.getQuoteUuid() == null){
            return;
        }
        PreparedStatement selectStmt = conn.prepareStatement(PB_QuoteLegTable.SelectSpecificQuoteLeg);
        selectStmt.setString(1, quote.getQuoteUuid());
        selectStmt.setInt(2, legIndex);
        ResultSet resultSet = selectStmt.executeQuery();
        GregorianCalendar gDate;
        Date date;
        while (resultSet.next()){
            leg.setOptionStrategy(resultSet.getString(PB_QuoteLegTable.Schema.STRUCTURE.toString()));
            gDate = new GregorianCalendar();
            date = resultSet.getDate(PB_QuoteLegTable.Schema.CONTRACT_START.toString());
            if (date != null){
                gDate.setTimeInMillis(date.getTime());
                leg.setOptionContractStartDate(gDate);
            }
            gDate = new GregorianCalendar();
            date = resultSet.getDate(PB_QuoteLegTable.Schema.CONTRACT_END.toString());
            if (date != null){
                gDate.setTimeInMillis(date.getTime());
                leg.setOptionContractEndDate(gDate);
            }
            leg.setOptionCross(resultSet.getBigDecimal(PB_QuoteLegTable.Schema.CROSS.toString()).doubleValue());
            leg.setOptionProduct(resultSet.getString(PB_QuoteLegTable.Schema.LOCATION.toString()));
            loadLegValues(conn, quote, leg, legIndex);
        }//while
        resultSet.close();
        selectStmt.close();
    }

    private void loadLegValues(Connection conn, IPbsysOptionQuote quote, IPbsysQuoteLeg leg, int legIndex) throws SQLException {
        if (quote.getQuoteUuid() == null){
            return;
        }
        PreparedStatement selectStmt = conn.prepareStatement(PB_QuoteLegValueTable.SelectSpecificQuoteLegValues);
        selectStmt.setString(1, quote.getQuoteUuid());
        selectStmt.setInt(2, legIndex);
        ResultSet resultSet = selectStmt.executeQuery();
        double[] strikes = leg.getOptionStrikes();
        double[] ratios = leg.getOptionRatios();
        int valueIndex;
        while (resultSet.next()){
            valueIndex = resultSet.getInt(PB_QuoteLegValueTable.Schema.VALUE_INDEX.toString());
            strikes[valueIndex] = resultSet.getBigDecimal(PB_QuoteLegValueTable.Schema.STRIKE.toString()).doubleValue();
            ratios[valueIndex] = resultSet.getBigDecimal(PB_QuoteLegValueTable.Schema.RATIO.toString()).doubleValue();
        }//while
        resultSet.close();
        selectStmt.close();
    }

    private PreparedStatement constructStatementForHistoricalMessageSearch(Connection conn,
                                                                           IGatewayConnectorBuddy pointBoxLoginUser,
                                                                           IViewerSearchCriteria criteria) throws SQLException
    {
        String sql = PB_InstantMessageTable.SelectAllMessages + " where ";
        boolean hasDateRange = false;
        boolean hasKeywords = false;
        boolean hasBrokers = false;
        if (criteria.isDateRangeLimited()){
            if ((criteria.getFromDate() != null) && (criteria.getToDate() != null)){
                sql = sql + PB_InstantMessageTable.Schema.MESSAGE_TIMESTAMP + " > ? " +
                            "and " + PB_InstantMessageTable.Schema.MESSAGE_TIMESTAMP + " < ?";
                hasDateRange = true;
            }
        }
        String keyword = criteria.getKeywords().trim();
        if ((keyword != null) && (!keyword.isEmpty())){
            if (hasDateRange){
                sql += " and (";
            }
            sql = sql + PB_InstantMessageTable.Schema.MESSAGE + " like '%" + keyword + "%')";
            hasKeywords = true;
        }
        String brokerName = criteria.getBrokerName().trim();
        if ((brokerName != null) && (!brokerName.isEmpty())){
            if (hasDateRange || hasKeywords){
                sql += " and ";
            }
            sql = sql + "((" + PB_InstantMessageTable.Schema.FROM_SCREEN_NAME + " like '%" + brokerName+ "%') OR " +
                    "(" + PB_InstantMessageTable.Schema.TO_SCREEN_NAME + " like '%" + brokerName+ "%'))";
            hasBrokers = true;
        }else{
            ArrayList<IGatewayConnectorBuddy> brokers = criteria.getBrokers();
            if (!brokers.isEmpty()){
                if (hasDateRange || hasKeywords){
                    if (hasDateRange || hasKeywords){
                        sql += " and ";
                    }
                }
                sql += "(";
                int total = brokers.size();
                for (int i = 0; i < total; i++){
                    brokerName = brokers.get(i).getIMScreenName();
                    sql = sql + "(((" + PB_InstantMessageTable.Schema.FROM_SCREEN_NAME + " like '%" + brokerName+ "%') OR " +
                            "(" + PB_InstantMessageTable.Schema.TO_SCREEN_NAME + " like '%" + brokerName+ "%')) AND " +
                            PB_InstantMessageTable.Schema.IM_SERVER_TYPE + " = '" + brokers.get(i).getIMServerType() + "')";
                    if (i != (total - 1) ){
                        sql += " OR ";
                    }
                }
                sql += ")";
                hasBrokers = true;
            }
        }

        if (hasDateRange || hasKeywords || hasBrokers){
            sql += " and ";
        }
        sql = sql + "(" + PB_InstantMessageTable.Schema.UserOwner + " = '" + pointBoxLoginUser.getIMUniqueName() + "')";

        PreparedStatement selectStmt = conn.prepareStatement(sql);
        if (hasDateRange){
            selectStmt.setDate(1, criteria.getFromDate());
            selectStmt.setDate(2, criteria.getToDate());
        }

        return selectStmt;
    }

    /**
     * @deprecated 
     * @param pointBoxLoginUser
     * @return 
     */
    @Override
    public TreeMap<String, IGroupMembersRecord> retrieveGroupMembersRecord(IGatewayConnectorBuddy pointBoxLoginUser) {
        TreeMap<String, IGroupMembersRecord> result = new TreeMap<String, IGroupMembersRecord>();
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return result;
        }
        if (pointBoxLoginUser != null){
            try {
                String ownerName = pointBoxLoginUser.getIMUniqueName();
                Connection conn = storageEngine.getConnection();
                PreparedStatement selectStmt = conn.prepareStatement(PB_BuddyGroupRelationshipTable.SelectJoinedGroupsBuddies);
                selectStmt.setString(1, ownerName);
                ResultSet resultSet = selectStmt.executeQuery();

                IGroupMembersRecord groupMembersRecord;
                IGroupRecord group;
                String groupUniqueName;

                //HashMap<String, IGroupRecord> groupMonitor = new HashMap<String, IGroupRecord>();
                IBuddyRecord buddy;
                String buddyUnqiueName;
                //HashMap<String, IBuddyRecord> buddyMonitor = new HashMap<String, IBuddyRecord>();
                GatewayServerType serverType;
                while (resultSet.next()){
                    serverType = GatewayServerType.convertToType(resultSet.getString(PB_BuddyTable.Schema.IM_SERVER_TYPE.toString()));
                    groupUniqueName = resultSet.getString(PB_GroupTable.Schema.GROUP_UNIQUE_NAME.toString());
                    buddyUnqiueName = resultSet.getString(PB_BuddyTable.Schema.BUDDY_UNIQUE_NAME.toString());
                    groupMembersRecord = result.get(groupUniqueName);
                    if (groupMembersRecord == null){
                        //group = PbconsoleRecordFactory.createGroupRecordInstance(ownerName);
                        group = getKernel().getPointBoxConsoleRuntime().createGroupRecordInstance(ownerName);
                        group.setGroupUniqueName(groupUniqueName);
                        group.setGroupDescription(resultSet.getString(PB_GroupTable.Schema.GROUP_DESCRIPTION.toString()));
                        group.setGroupName(resultSet.getString(PB_GroupTable.Schema.GROUP_NAME.toString()));
                        group.setOwnerUniqueName(ownerName);
                        group.setServerType(GatewayServerType.PBIM_DISTRIBUTION_TYPE);    //useless since group could be hybrid
                        //groupMembersRecord = PbconsoleRecordFactory.createGroupMembersRecordInstance(groupUniqueName, group);
                        groupMembersRecord = getKernel().getPointBoxConsoleRuntime().createGroupMembersRecordInstance(groupUniqueName, group);
                        result.put(groupUniqueName, groupMembersRecord);
                    
                    }
                    //buddy = buddyMonitor.get(buddyUnqiueName);
                    //if (buddy == null){
                    //buddy = PbconsoleRecordFactory.createBuddyRecordInstance(ownerName);
                    buddy = getKernel().getPointBoxConsoleRuntime().createBuddyRecordInstance(ownerName);
                    buddy.setBuddyScreenName(resultSet.getString(PB_BuddyTable.Schema.BUDDY_SCREEN_NAME.toString()));
                    buddy.setBuddyUniqueName(buddyUnqiueName);
                    buddy.setNickName(resultSet.getString(PB_BuddyTable.Schema.NICKNAME.toString()));
                    buddy.setOwnerUniqueName(ownerName);
                    buddy.setPassword(resultSet.getString(PB_BuddyTable.Schema.PASSWORD.toString()));
                    buddy.setProfileId(resultSet.getString(PB_BuddyTable.Schema.PROFILE_ID.toString()));
                    buddy.setServerType(serverType);
                    groupMembersRecord.getMemberRecords().add(buddy);
                    //buddyMonitor.put(buddyUnqiueName, buddy);
                    //}
                }//while

                resultSet.close();
                selectStmt.close();
                conn.close();
            } catch (SQLException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return result;
    }

    /**
     * @deprecated 
     * @param ownerName
     * @param groupMemberRecords 
     */
    @Override
    public void storeGroupMembersRecords(String ownerName, TreeMap<String, IGroupMembersRecord> groupMemberRecords) {
        try {
            Connection conn = storageEngine.getConnection();

            Set<String> keys = groupMemberRecords.keySet();
            Iterator<String> itr = keys.iterator();
            IGroupMembersRecord record;
            PreparedStatement stmtDeleteGroup = conn.prepareStatement(PB_GroupTable.DeleteSpecificRecord);
            PreparedStatement stmtInsertGroup = conn.prepareStatement(PB_GroupTable.InsertNewRecord);
            PreparedStatement stmtDeleteBuddy = conn.prepareStatement(PB_BuddyTable.DeleteSpecificRecord);
            PreparedStatement stmtInsertBuddy = conn.prepareStatement(PB_BuddyTable.InsertNewRecord);
            PreparedStatement stmtDeleteRelation = conn.prepareStatement(PB_BuddyGroupRelationshipTable.DeleteSpecificRecord);
            PreparedStatement stmtInsertRelation = conn.prepareStatement(PB_BuddyGroupRelationshipTable.InsertNewRecord);
            Date date = new Date((new GregorianCalendar()).getTimeInMillis());
            IGroupRecord groupRecord;
            ArrayList<IBuddyRecord> memberRecords;
            String groupUniqueName;
            while (itr.hasNext()){
                record = groupMemberRecords.get(itr.next());

                groupRecord = record.getGroupRecord();
                groupUniqueName = groupRecord.getGroupUniqueName();

                stmtDeleteGroup.setString(1, ownerName);
                stmtDeleteGroup.setString(2, groupUniqueName);
                stmtDeleteGroup.executeUpdate();
                stmtDeleteGroup.clearParameters();

                stmtInsertGroup.setString(1, ownerName);
                stmtInsertGroup.setString(2, groupUniqueName);
                stmtInsertGroup.setString(3, groupRecord.getServerType().toString());
                stmtInsertGroup.setString(4, groupRecord.getGroupName());
                stmtInsertGroup.setString(5, groupRecord.getGroupDescription());
                stmtInsertGroup.setDate(6, date);
                stmtInsertGroup.executeUpdate();
                stmtInsertGroup.clearParameters();

                memberRecords = record.getMemberRecords();
                for (IBuddyRecord memberRecord : memberRecords){

                    stmtDeleteBuddy.setString(1, ownerName);
                    stmtDeleteBuddy.setString(2, memberRecord.getBuddyUniqueName());
                    stmtDeleteBuddy.executeUpdate();
                    stmtDeleteBuddy.clearParameters();

                    stmtInsertBuddy.setString(1, ownerName);
                    stmtInsertBuddy.setString(2, memberRecord.getBuddyUniqueName());
                    stmtInsertBuddy.setString(3, memberRecord.getServerType().toString());
                    stmtInsertBuddy.setString(4, memberRecord.getBuddyScreenName());
                    stmtInsertBuddy.setString(5, memberRecord.getPassword());
                    stmtInsertBuddy.setString(6, "");     //todo: nickname
                    stmtInsertBuddy.setString(7, "");     //todo: profile_id
                    stmtInsertBuddy.setDate(8, date);
                    stmtInsertBuddy.executeUpdate();
                    stmtInsertBuddy.clearParameters();

                    stmtDeleteRelation.setString(1, ownerName);
                    stmtDeleteRelation.setString(2, groupUniqueName);
                    stmtDeleteRelation.setString(3, memberRecord.getBuddyUniqueName());
                    stmtDeleteRelation.executeUpdate();
                    stmtDeleteRelation.clearParameters();

                    stmtInsertRelation.setString(1, ownerName);
                    stmtInsertRelation.setString(2, groupUniqueName);
                    stmtInsertRelation.setString(3, memberRecord.getBuddyUniqueName());
                    stmtInsertRelation.setDate(4, date);
                    stmtInsertRelation.executeUpdate();
                    stmtInsertRelation.clearParameters();
                }//for
            }
            stmtDeleteGroup.close();
            stmtInsertGroup.close();
            stmtDeleteBuddy.close();
            stmtInsertBuddy.close();
            stmtDeleteRelation.close();
            stmtInsertRelation.close();
            conn.close();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    /**
     * @deprecated 
     * @param record 
     */
    @Override
    public void deleteGroupMemberRecord(IGroupMembersRecord record) {
        
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return;
        }
        
        try {
            Connection conn = storageEngine.getConnection();

            PreparedStatement stmtDeleteGroup = conn.prepareStatement(PB_GroupTable.DeleteSpecificRecord);
            PreparedStatement stmtDeleteBuddy = conn.prepareStatement(PB_BuddyTable.DeleteSpecificRecord);
            PreparedStatement stmtDeleteRelation = conn.prepareStatement(PB_BuddyGroupRelationshipTable.DeleteSpecificRecordByOwnerGroup);
            
            IGroupRecord groupRecord = record.getGroupRecord();
            String groupUniqueName = groupRecord.getGroupUniqueName();
            String ownerName = groupRecord.getOwnerUniqueName();

            stmtDeleteGroup.setString(1, ownerName);
            stmtDeleteGroup.setString(2, groupUniqueName);
            stmtDeleteGroup.executeUpdate();
            stmtDeleteGroup.close();

            stmtDeleteRelation.setString(1, ownerName);
            stmtDeleteRelation.setString(2, groupUniqueName);
            stmtDeleteRelation.executeUpdate();
            stmtDeleteRelation.close();

            ArrayList<IBuddyRecord> memberRecords = record.getMemberRecords();
            for (IBuddyRecord memberRecord : memberRecords){
                stmtDeleteBuddy.setString(1, ownerName);
                stmtDeleteBuddy.setString(2, memberRecord.getBuddyUniqueName());
                stmtDeleteBuddy.executeUpdate();
                stmtDeleteBuddy.clearParameters();
            }//for

            stmtDeleteBuddy.close();
            conn.close();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    @Override
    public void handleComponentEvent(PointBoxConsoleEvent event) {
        if (event == null){
            return;
        }
        if (event instanceof MermAccessRecordChangedEvent){
            if (PbcProperties.getSingleton().getReleaseUserType().equals(PbcReleaseUserType.MERM_USERS)){
                populateMermAccessRecordFromSettings();
            }
        }else if (event instanceof QuoteParsedEvent){
            QuoteParsedEvent qpe = (QuoteParsedEvent)event;
            pbcStorageAgent.bufferQuotes(qpe.getParsedQuotes());
        }
    }
    
    private class PbcStorageAgent{
        private Thread storingThread;
        private ExecutorService storingService;
        private final ArrayList<IPbsysOptionQuote> buffer;
        private PbcStorageAgent() {
            this.buffer = new ArrayList<IPbsysOptionQuote>();
            this.storingService = Executors.newSingleThreadExecutor();
            this.storingThread = null;
        }

        void startAgent(){
            /**
             * When storingThread is interrupted or stopped, storingService becomes NULL
             */
            if (storingService == null){
                storingService = Executors.newSingleThreadExecutor(); 
            }
            
            if (storingThread == null){
                storingThread = new Thread(new PbcStorageCycling());
            }
            if (!storingThread.isAlive()){
                storingThread.start();
            }
            getKernel().updateSplashScreen("MS Access engine is started.", Level.INFO, 500);
        }

        void stopAgent(){
            if (storingThread != null){
                storingThread.interrupt();
                storingThread = null;
            }
            if (storingService != null){
                storingService.shutdown();
                storingService = null;
            }
            getKernel().updateSplashScreen("MS Access engine is stopped.", Level.INFO, 500);
        }

        void bufferQuotes(final ArrayList<IPbsysOptionQuote> parsedQuotes) {
            synchronized(buffer){
                buffer.addAll(parsedQuotes);
            }
        }

        private ArrayList<IPbsysOptionQuote> retrieveQuotes() {
            ArrayList<IPbsysOptionQuote> quotes = new ArrayList<IPbsysOptionQuote>();
            synchronized(buffer){
                for (IPbsysOptionQuote quote : buffer){
                    quotes.add(quote);
                }//for
                buffer.clear();
            }
            return quotes;
        }

        /**
         * This is used for MERM storing their own message quotes
         */
        private class PbcStorageCycling implements Runnable{
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1500);
                        storingService.submit(new Runnable(){
                            @Override
                            public void run() {
                                ArrayList<IPbsysOptionQuote> quotes = retrieveQuotes();
                                if (!quotes.isEmpty()){
                                    try {
                                        storeOptionQuotesForThirdParty(quotes);
                                        for (IPbsysOptionQuote quote : quotes){
                                            quote.getInstantMessage().setHistoricalMessage(true);
                                        }
                                    } catch (SQLException ex) {
                                        /**
                                         * Re-buffer those quotes which were not successfully processed.
                                         */
                                        bufferQuotes(quotes);
                                        PointBoxTracer.recordSevereException(logger, ex);
                                        /**
                                         * Notify this event happened
                                         */
                                        getKernel().notifyMsAccessInterrupted(ex);
                                        /**
                                         * Stop storage agent
                                         */
                                        stopAgent();
                                    }//try
                                }
                            }
                        });
                    } catch (InterruptedException ex) {
                        //logger.log(Level.SEVERE, null, ex);
                        break;
                    }
                }
            }
        }
    }
}
