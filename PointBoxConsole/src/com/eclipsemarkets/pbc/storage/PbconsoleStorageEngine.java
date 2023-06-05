/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.storage;

import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.PbcProperties;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.storage.exceptions.DatabaseNotReady;
import com.eclipsemarkets.pbc.storage.exceptions.DatabaseNotReady.Reason;
import com.eclipsemarkets.pbc.storage.sql.PB_AutoPricerSettingsTable;
import com.eclipsemarkets.pbc.storage.sql.PB_BuddyGroupRelationshipTable;
import com.eclipsemarkets.pbc.storage.sql.PB_BuddyTable;
import com.eclipsemarkets.pbc.storage.sql.PB_FileSettingsTable;
import com.eclipsemarkets.pbc.storage.sql.PB_GroupTable;
import com.eclipsemarkets.pbc.storage.sql.PB_IMServerTable;
import com.eclipsemarkets.pbc.storage.sql.PB_InstantMessageTable;
import com.eclipsemarkets.pbc.storage.sql.PB_LoginDialogSettingsTable;
import com.eclipsemarkets.pbc.storage.sql.PB_MermSettingsTable;
import com.eclipsemarkets.pbc.storage.sql.PB_MessageTabSettingsTable;
import com.eclipsemarkets.pbc.storage.sql.PB_PricerSettingsTable;
import com.eclipsemarkets.pbc.storage.sql.PB_PricingSettingFilesTable;
import com.eclipsemarkets.pbc.storage.sql.PB_QuoteLegTable;
import com.eclipsemarkets.pbc.storage.sql.PB_QuoteLegValueTable;
import com.eclipsemarkets.pbc.storage.sql.PB_QuoteTable;
import com.eclipsemarkets.pbc.storage.sql.PB_ReleaseTable;
import com.eclipsemarkets.pbc.storage.sql.PB_SystemFrameSettingsTable;
import com.eclipsemarkets.pbc.storage.sql.PB_UserProfileTable;
import com.eclipsemarkets.pbc.storage.sql.PB_ViewerTableColumnTable;
import com.eclipsemarkets.pbc.storage.sql.PB_ViewerTablePanelTable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.eclipsemarkets.snaq.db.ConnectionPool;

/**
 * PbconsoleStorageEngine.java
 * <p>
 * This class
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 6, 2010, 9:38:31 PM
 */
class PbconsoleStorageEngine {

    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbconsoleStorageEngine.class.getName());
    }

    private final SortedMap<String, Method> databaseConstructionMethods;
    private Properties dbProperties;
    private String databaseLocation;
    private String databaseURL;
    private ConnectionPool connPool;

    private IPbcStorage storage;

    PbconsoleStorageEngine(IPbcStorage storage) {

        databaseConstructionMethods = Collections.synchronizedSortedMap(new TreeMap<String, Method>());
        this.storage = storage;
        dbProperties = null;
        databaseLocation = null;
        databaseURL = null;
        connPool = null;
        
    }

    /**
     * @deprecated 
     * @throws DatabaseNotReady 
     */
    void invokeStorageEngine() throws DatabaseNotReady{
        /**
         * Disable this method - 04/30/2012 Zhijun
         */
        if (true){
            return;
        }
        
        if (storage == null){
            return;
        }
        initializeDatabaseConstructionMethods();

        String dbName = PbcDatabaseInstance.PbconsolStorage.toString();
        initializeStorageLocation(dbName);
        loadDerbyDriver();
        createConnectionPool();
        if(!NIOGlobal.isValidFolder(getDatabaseLocation())) {
            constructDatabase(null);
        }else{
            PbcProperties recordedProp = retrievePointBoxConsoleProperties();
            String latestCode = null;
            if (recordedProp != null){
                latestCode = recordedProp.getRecordedReleaseCode();
                PbcProperties propSingleton = PbcProperties.getSingleton();
                propSingleton.setProxyHost(recordedProp.getProxyHost());
                propSingleton.setProxyPort(recordedProp.getProxyPort());
                propSingleton.setProxyUserName(recordedProp.getProxyUserName());
                propSingleton.setProxyPassword(recordedProp.getProxyPassword());
                //propSingleton.setClientUuidInfo(recordedProp.getClientUuidInfo());
            }
            if (constructDatabase(latestCode)){
                processPointBoxConsolePropertiesSingleton(recordedProp);
            }
        }
    }

    /**
     * Retrieve a list of release_codes recorded in the database. This list is sorted from the latest to the oldest.
     * if the list is empty. it means the database never recorded release code so far.
     * @return
     */
    private PbcProperties retrievePointBoxConsoleProperties() throws DatabaseNotReady{
        PbcProperties recordedProp = PbcProperties.createInstance();
        if (connPool == null){
            return recordedProp;
        }
        try {
            Connection conn = connPool.getConnection();
            PreparedStatement prst = conn.prepareStatement(PB_ReleaseTable.SelectAllRecords);
            ResultSet resultSet = prst.executeQuery();
            while (resultSet.next()) {
//                try{
//                recordedProp.setClientUuidInfo(resultSet.getString(PB_ReleaseTable.Schema.ClientUuidInfo.toString()));
//                }catch (Exception ex){}
                try{
                recordedProp.setLastUpdate(resultSet.getDate(PB_ReleaseTable.Schema.LAST_UPDATE.toString()));
                }catch (Exception ex){}
                try{
                recordedProp.setProxyHost(resultSet.getString(PB_ReleaseTable.Schema.ProxyHost.toString()));
                }catch (Exception ex){}
                try{
                recordedProp.setProxyPassword(resultSet.getString(PB_ReleaseTable.Schema.ProxyPassword.toString()));
                }catch (Exception ex){}
                try{
                recordedProp.setProxyPort(resultSet.getString(PB_ReleaseTable.Schema.ProxyPort.toString()));
                }catch (Exception ex){}
                try{
                recordedProp.setProxyUserName(resultSet.getString(PB_ReleaseTable.Schema.ProxyUser.toString()));
                }catch (Exception ex){}
                try{
                recordedProp.setRecordedReleaseCode(resultSet.getString(PB_ReleaseTable.Schema.RELEASE_CODE.toString()));
                }catch (Exception ex){}
                try{
                recordedProp.setRecordedSoftwareVersion(resultSet.getString(PB_ReleaseTable.Schema.SOFTWARE_VERSION.toString()));
                }catch (Exception ex){}
                break;
            }
            resultSet.close();
            prst.close();
            conn.close();
        } catch (SQLException ex) {
            //throw new DatabaseNotReady(DatabaseNotReady.Reason.INSTANCE_CONFLICT);
            logger.log(Level.WARNING, "PbconsoleStorageEngine failed to be initialized.");
            connPool = null;
            try {
                SwingUtilities.invokeAndWait(new Runnable(){
                    public void run() {
                        JOptionPane.showMessageDialog(null, "There is another PointBox Console application opened. ");
//                        JOptionPane.showMessageDialog(null, "There is another PointBox Console application opened. " + NIOGlobal.lineSeparator()
//                                                           + "Please shut it down or remove it from the memory by " + NIOGlobal.lineSeparator()
//                                                           + "means of Task Manager.");
                    }
                });
            } catch (InterruptedException ex1) {
                logger.log(Level.SEVERE, null, ex1);
            } catch (InvocationTargetException ex1) {
                logger.log(Level.SEVERE, null, ex1);
            }
//            Runtime.getRuntime().exit(1);
            return PbcProperties.createInstance();
        }
        return recordedProp;
    }
    private void processPointBoxConsolePropertiesSingleton(PbcProperties recordedProp){
        PbcProperties propSingleton = PbcProperties.getSingleton();
        if (recordedProp == null){
            recordPointBoxConsoleProperties(propSingleton);
        }else{
            if (!(propSingleton.getReleaseCode().equalsIgnoreCase(recordedProp.getReleaseCode())))
            {
                propSingleton.setProxyHost(recordedProp.getProxyHost());
                propSingleton.setProxyPort(recordedProp.getProxyPort());
                propSingleton.setProxyUserName(recordedProp.getProxyUserName());
                propSingleton.setProxyPassword(recordedProp.getProxyPassword());
//                if (DataGlobal.isNonEmptyNullString(recordedProp.getClientUuidInfo())){
//                    propSingleton.setClientUuidInfo(recordedProp.getClientUuidInfo());
//                }
                storePointBoxConsolePropertiesSingleton(propSingleton);
            }
        }
    }
    
    private void storePointBoxConsolePropertiesSingleton(PbcProperties propSingleton)
    {
        clearPbconsoleReleaseTable();
        recordPointBoxConsoleProperties(propSingleton);
    }
    
    private void recordPointBoxConsoleProperties(PbcProperties prop) {
        if(connPool == null){
            return;
        }
        try {
            Connection dbConnection = connPool.getConnection();
            PreparedStatement pstmt = dbConnection.prepareStatement(PB_ReleaseTable.InsertNewRecord);
            pstmt.setString(1, prop.getSoftwareName());
            pstmt.setString(2, prop.getSoftwareVersion());
            pstmt.setString(3, prop.getReleaseCompany());
            pstmt.setString(4, prop.getReleaseCode());
            pstmt.setTimestamp(5, new Timestamp((prop.getLastUpdate()).getTime()));
            pstmt.setString(6, prop.getProxyHost());
            pstmt.setString(7, prop.getProxyPort());
            pstmt.setString(8, prop.getProxyUserName());
            pstmt.setString(9, prop.getProxyPassword());
            pstmt.setString(10, PointBoxConsoleProperties.getSingleton().getClientUuidInfo());

            pstmt.executeUpdate();
            pstmt.close();
            dbConnection.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void clearPbconsoleReleaseTable() {
        if (connPool == null){
            return;
        }
        try{
            Connection dbConnection = connPool.getConnection();
            PreparedStatement pstmt = dbConnection.prepareStatement(PB_ReleaseTable.DeleteAllRecords);
            pstmt.executeUpdate();
            pstmt.close();
            dbConnection.close();
        }catch (SQLException ex){}
    }

    void shutdown() {
        storePointBoxConsolePropertiesSingleton(PbcProperties.getSingleton());
        if (connPool != null){
            connPool.releaseForcibly();
        }
    }

    Connection getConnection() throws SQLException {
        if (connPool == null){
            throw new SQLException("Connection pool was not initialized successfully.");
        }else{
            return connPool.getConnection();
        }
    }

    //Connection getConnection(long timeout) throws SQLException {
    //    return connPool.getConnection(timeout);
    //}

    /**
     * store all the methods which construct database for releases in the history
     */
    private void initializeDatabaseConstructionMethods(){
        Class<?> c = this.getClass();
        Method[] allDatabaseConstructionMethods  = c.getDeclaredMethods();
        for (Method m : allDatabaseConstructionMethods){
            String mName = m.getName();
            if (mName.startsWith("R_")){
                databaseConstructionMethods.put(mName, m);
            }
        }//for
    }

    /**
     * make ure the database folder there. if it is not there, create such a folder
     */
    private void initializeStorageLocation(String dbLocation) throws DatabaseNotReady{
        // decide on the db system directory
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + "/." + dbLocation;
        System.setProperty("derby.system.home", systemDir);
        databaseLocation = System.getProperty("derby.system.home") + "/" + dbLocation;
        // create the db system directory
        if (!NIOGlobal.isValidFolder(systemDir)){
            try{
                File fileSystemDir = new File(systemDir);
                fileSystemDir.mkdir();
            }catch (Exception ex){
                throw new DatabaseNotReady(Reason.LOCATION_FAILURE);
            }
        }
    }

    /**
     * load Derby driver
     */
    private void loadDerbyDriver() {
        InputStream dbPropInputStream = null;
        dbPropInputStream = PbconsoleStorageEngine.class.getResourceAsStream("PbconsoleStorage.properties");
        dbProperties = new Properties();
        try {
            dbProperties.load(dbPropInputStream);
            try {
                Class c = Class.forName(dbProperties.getProperty("derby.driver"));
                Driver driver = (Driver)c.newInstance();
                DriverManager.registerDriver(driver);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            } catch (InstantiationException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            } catch (ClassNotFoundException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        databaseURL = dbProperties.getProperty("derby.url") +
                PbcDatabaseInstance.PbconsolStorage.toString();
    }

    /**
     * create a connection pool for the derby database
     */
    private void createConnectionPool() {
        connPool = new ConnectionPool(dbProperties.getProperty("user"), //<poolname>,
                                      5,                //<minpool>,
                                      25,               //<maxpool>,
                                      0,                //<maxsize>, no limit
                                      600,              //<idleTimeout>, 10 minutes
                                      getDatabaseURL(), //<url>,
                                      dbProperties.getProperty("user"),     //<username>,
                                      dbProperties.getProperty("password"));    //<password>);
    }

    /**
     * Construct a database from "beginningReleaseCode" which labeled the construction point recorded in
     * historicalDatabaseConstructionMethods. If it is null, it means, this is brand-new database. All
     * the R_MMDDYYYYhhmmss methods should be invoked.
     * <p>
     * @param beginningReleaseCode
     * @return
     */
    private boolean constructDatabase(String beginningReleaseCode) {
        boolean bCreated = true;
        Connection dbConnection = null;
        dbProperties.put("create", "true");
        try {
            dbConnection = DriverManager.getConnection(getDatabaseURL(), dbProperties);
            Collection<Method> rMethods = databaseConstructionMethods.values();
            boolean invoking = false;
            if (DataGlobal.isEmptyNullString(beginningReleaseCode)){
                invoking = true;
            }
            for (Method rMethod : rMethods){
                if (invoking){
                    rMethod.invoke(this, dbConnection);
                }
                if (rMethod.getName().equalsIgnoreCase(beginningReleaseCode)){
                    invoking = true;
                }
            }//for
            dbConnection.close();
        } catch (IllegalAccessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            bCreated = false;
        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            bCreated = false;
        } catch (InvocationTargetException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            bCreated = false;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            bCreated = false;
        }
        dbProperties.remove("create");
        return bCreated;
    }
    
    private boolean R_20110420101826(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(PB_PricerSettingsTable.AddTValueAtExpColumn);
            bCreatedTables = true;
            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }
    
    /**
     * trigger R_20110420101826 
     * @param dbConnection
     * @return 
     */
    private boolean R_20110418233051(Connection dbConnection) {
        return true;
    }
    
    private boolean R_20110415210945(Connection dbConnection){
        return true;
    }

    private boolean R_06122010112600(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(PB_LoginDialogSettingsTable.CreateNewTable);
            statement.execute(PB_SystemFrameSettingsTable.CreateNewTable);
            statement.execute(PB_ViewerTablePanelTable.CreateNewTable);

            statement.execute(PB_ViewerTableColumnTable.DropTable);
            statement.execute(PB_ViewerTableColumnTable.CreateNewTable);

            statement.execute(PB_AutoPricerSettingsTable.CreateNewTable);
            statement.execute(PB_PricerSettingsTable.CreateNewTable);

            //recordPbconsoleReleasePolicy(dbConnection);

            bCreatedTables = true;

            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }

    private boolean R_06242010112600(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();

            statement.execute(PB_InstantMessageTable.DropTable);
            statement.execute(PB_InstantMessageTable.CreateNewTable);

            statement.execute(PB_QuoteTable.DropTable);
            statement.execute(PB_QuoteTable.CreateNewTable);

            statement.execute(PB_QuoteTable.DropTable);
            statement.execute(PB_QuoteTable.CreateNewTable);

            statement.execute(PB_QuoteLegTable.DropTable);
            statement.execute(PB_QuoteLegTable.CreateNewTable);

            statement.execute(PB_QuoteLegValueTable.DropTable);
            statement.execute(PB_QuoteLegValueTable.CreateNewTable);

            //recordPbconsoleReleasePolicy(dbConnection);

            bCreatedTables = true;

            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }

    private boolean R_07262010112600(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();

            //add a new column into PB_SystemFrameSettingsTable
            statement.execute(PB_SystemFrameSettingsTable.AddRecycleDaysColumn);
            statement.execute(PB_QuoteLegValueTable.AddUserOwnerColumn);
            statement.execute(PB_QuoteLegTable.AddUserOwnerColumn);

            //recordPbconsoleReleasePolicy(dbConnection);

            bCreatedTables = true;
            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }

    private boolean R_07262010112601(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();

            statement.execute(PB_PricingSettingFilesTable.CreateNewTable);

            //recordPbconsoleReleasePolicy(dbConnection);

            bCreatedTables = true;
            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }

    private boolean R_08012010112600(Connection dbConnection){
        //this release has no change on database
        return true;
    }

    private boolean R_08152010215900(Connection dbConnection){
        //this release has no change on database
        return true;
    }

    private boolean R_09012010215900(Connection dbConnection){
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();

            statement.execute(PB_MessageTabSettingsTable.CreateNewTable);

            //recordPbconsoleReleasePolicy(dbConnection);

            bCreatedTables = true;
            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }
    private boolean R_20110311144602(Connection dbConnection){
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(PB_LoginDialogSettingsTable.AddCommodityTypeColumn);
            statement.execute(PB_ReleaseTable.AddProxyHostColumn);
            statement.execute(PB_ReleaseTable.AddProxyPortColumn);
            statement.execute(PB_ReleaseTable.AddProxyUserColumn);
            statement.execute(PB_ReleaseTable.AddProxyPasswordColumn);
            statement.execute(PB_ReleaseTable.AddClientUuidInfoColumn);

            //recordPbconsoleReleasePolicy(dbConnection);

            bCreatedTables = true;
            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }
    private boolean R_20110326145318(Connection dbConnection){
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(PB_ViewerTableColumnTable.AddSortOrderColumn);
            bCreatedTables = true;
            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }
    
    /**
     * @param dbConnection
     * @return
     * @see initializeHistoricalDatabaseConstructionMethods
     */
    private boolean R_05292010105030(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(PB_ReleaseTable.CreateNewTable);
            statement.execute(PB_IMServerTable.CreateNewTable);
            statement.execute(PB_GroupTable.CreateNewTable);
            statement.execute(PB_BuddyTable.CreateNewTable);
            statement.execute(PB_BuddyGroupRelationshipTable.CreateNewTable);
            statement.execute(PB_InstantMessageTable.CreateNewTable);
            statement.execute(PB_QuoteTable.CreateNewTable);
            statement.execute(PB_QuoteLegTable.CreateNewTable);
            statement.execute(PB_QuoteLegValueTable.CreateNewTable);
            statement.execute(PB_UserProfileTable.CreateNewTable);
            statement.execute(PB_ViewerTableColumnTable.CreateNewTable);
            statement.execute(PB_FileSettingsTable.CreateNewTable);
            statement.execute(PB_MermSettingsTable.CreateNewTable);

            //initial default values
            //recordPbconsoleReleasePolicy(dbConnection);
            initializePbconsoleIMServersValues(dbConnection);
            //initializeConfigViewerTableColumnValues(dbConnection, ViewerTableType.ALL_MESSAGES);
            //initializeConfigViewerTableColumnValues(dbConnection, ViewerTableType.ALL_QUOTES);
            //initializeConfigViewerTableColumnValues(dbConnection, ViewerTableType.INCOMING_MESSAGES);
            //initializeConfigViewerTableColumnValues(dbConnection, ViewerTableType.OUTGOING_MESSAGES);
            //initializeConfigViewerTableColumnValues(dbConnection, ViewerTableType.SEARCH_RESULT);

            bCreatedTables = true;

            statement.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return bCreatedTables;
    }

    /*
    private void initializeConfigViewerTableColumnValues(Connection dbConnection, ViewerTableType tableType) throws SQLException {
        PreparedStatement pstmt = dbConnection.prepareStatement(PB_ViewerTableColumnTable.InsertNewRecord);

        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.TimeStamp.toString(),
                                                      1, 50, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.BuySell.toString(),
                                                      2, 50, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.QuoteMessage.toString(),
                                                      3, 150, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.RemoteBrokerHouse.toString(),
                                                      4, 50, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Bid.toString(),
                                                      5, 50, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Offer.toString(),
                                                      6, 50, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Strike.toString(),
                                                      7, 75, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.PbsysPrice.toString(),
                                                      8, 50, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Period.toString(),
                                                      9, 75, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Structure.toString(),
                                                      10, 90, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Cross.toString(),
                                                      11, 50, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Swap01.toString(),
                                                      12, 75, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.DDelta.toString(),
                                                      13, 75, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Last.toString(),
                                                      14, 75, (short)1, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Delta.toString(),
                                                      15, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.DGamma.toString(),
                                                      16, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Location.toString(),
                                                      17, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Gamma.toString(),
                                                      18, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Vega.toString(),
                                                      19, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Notes.toString(),
                                                      20, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Swap02.toString(),
                                                      21, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Eqv.toString(),
                                                      22, 50, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.RowNumber.toString(),
                                                      23, 20, (short)0, tableType);
        initializeConfigViewerTableColumnValuesHelper(pstmt,
                                                      ViewerColumnIdentifier.Theta.toString(),
                                                      24, 50, (short)0, tableType);
        try{
            pstmt.close();
        }catch (SQLException ex){}
    }*/
    /*
    private void initializeConfigViewerTableColumnValuesHelper(PreparedStatement pstmt,
                                                               String columnName,
                                                               int position,
                                                               int width,
                                                               short visible,
                                                               ViewerTableType tableType) throws SQLException
    {
        pstmt.setString(1, releasePolicy.getAnonymousUserName());
        pstmt.setString(2, tableType.toString());
        pstmt.setString(3, columnName);
        pstmt.setString(4, columnName);
        pstmt.setInt(5, width);
        pstmt.setInt(6, position);
        pstmt.setShort(7, (short)1);
        pstmt.setShort(8, visible);
        pstmt.executeUpdate();
        pstmt.clearParameters();
    }*/

    /**
     * todo: it is hard-coded. it should be improved
     * @param dbConnection
     * @throws SQLException
     */
    private void initializePbconsoleIMServersValues(Connection dbConnection) throws SQLException {
        PreparedStatement pstmt = dbConnection.prepareStatement(PB_IMServerTable.InsertNewRecord);
        pstmt.setString(1, PbcDatabaseInstance.DefaultEmsUser.toString());
        pstmt.setString(2, GatewayServerType.AIM_SERVER_TYPE.toString());
        pstmt.setString(3, GatewayServerType.AIM_SERVER_TYPE.toString());
        pstmt.setTimestamp(4, new Timestamp((new GregorianCalendar()).getTimeInMillis()));
        pstmt.executeUpdate();
        pstmt.clearParameters();


        pstmt.setString(1, PbcDatabaseInstance.DefaultEmsUser.toString());
        pstmt.setString(2, GatewayServerType.PBIM_SERVER_TYPE.toString());
        pstmt.setString(3, GatewayServerType.PBIM_SERVER_TYPE.toString());
        pstmt.setTimestamp(4, new Timestamp((new GregorianCalendar()).getTimeInMillis()));
        pstmt.executeUpdate();
        pstmt.clearParameters();

        pstmt.setString(1, PbcDatabaseInstance.DefaultEmsUser.toString());
        pstmt.setString(2, GatewayServerType.YIM_SERVER_TYPE.toString());
        pstmt.setString(3, GatewayServerType.YIM_SERVER_TYPE.toString());
        pstmt.setTimestamp(4, new Timestamp((new GregorianCalendar()).getTimeInMillis()));
        pstmt.executeUpdate();
        pstmt.clearParameters();
        try{
            pstmt.close();
        }catch (SQLException ex){}
    }

    private String getDatabaseLocation() {
        return databaseLocation;
    }

    private String getDatabaseURL(){
        return databaseURL;
    }
}
