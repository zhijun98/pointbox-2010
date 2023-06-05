/**
 * Eclipse Market Solutions LLC
 */
/*
 * ViewerSearchDialog.java
 *
 * @author Zhijun Zhang
 * Created on Jun 22, 2010, 6:14:22 PM
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;
import com.eclipsemarkets.toedter.calendar.JDateChooser;
import com.eclipsemarkets.toedter.calendar.JSpinnerDateEditor;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author Zhijun Zhang
 */
public class ViewerSearchDialog extends javax.swing.JDialog implements IPbconsoleDialog{
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(ViewerSearchDialog.class.getName());
    }

    private final IPbcViewer viewer;
    private final String resultMessage;
    private JDateChooser jFromDateChooser;
    private JDateChooser jToDateChooser;

    /** Creates new form ViewerSearchDialog
     * @param viewer
     * @param modal
     */
    public ViewerSearchDialog(IPbcViewer viewer, boolean modal) {
        super(viewer.getPointBoxFrame(), modal);
        initComponents();

        this.viewer = viewer;
        resultMessage = "Please build up your own search criteria.";
        jMessage.setBackground(jSearchLabel.getBackground());

        initDateRangeZone();
        initSearchCriteria();

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setTitle("Search Historical Quotes/Messages");

        pack();

        setResizable(false);
        //setLocation(SwingGlobal.getCenterPointOfParentWindow(viewer.getPointBoxFrame(), this));
    }

    private void initDateRangeZone(){
        jFromDateChooser = new JDateChooser(null, null, null, new JSpinnerDateEditor());
        jFromDateChooser.setEnabled(false);
        jToDateChooser = new JDateChooser(null, null, null, new JSpinnerDateEditor());
        jToDateChooser.setEnabled(false);
        jDateRange.add(jFromDateChooser);
        jDateRange.add(jToDateChooser);
    }

    private void initSearchCriteria(){
        jAndOrStructures.addItem(ViewerSearchCriteriaTerms.AND);
        jAndOrStructures.addItem(ViewerSearchCriteriaTerms.OR);
        jAndOrStructures.setSelectedItem(ViewerSearchCriteriaTerms.OR);
        jAndOrLocations.addItem(ViewerSearchCriteriaTerms.AND);
        jAndOrLocations.addItem(ViewerSearchCriteriaTerms.OR);
        jAndOrLocations.setSelectedItem(ViewerSearchCriteriaTerms.OR);
        jAndOrPeriod.addItem(ViewerSearchCriteriaTerms.AND);
        jAndOrPeriod.addItem(ViewerSearchCriteriaTerms.OR);
        jAndOrPeriod.setSelectedItem(ViewerSearchCriteriaTerms.OR);

        populateBrokers();
        populateStructures();
        populateLocations();

        initializeComponentYears();
        initDateOperator(jStartDateOperator);
        initDateOperator(jEndDateOperator);
    }

    private void initializeComponentYears(){
        (new SwingWorker<Void, ArrayList<String>>(){
            @Override
            protected Void doInBackground() throws Exception {
                String startingDate = viewer.getKernel().getPointBoxConsoleRuntime().getPointBoxPricingSettings()
                        .getExpirationSettlementDates(viewer.getKernel().getDefaultSimCodeFromProperties()).getFrontContract();
                GregorianCalendar today = new GregorianCalendar();
                Integer startYear = today.get(Calendar.YEAR);
                if (startingDate.contains("/") && startingDate.split("/").length > 1){
                    try{
                        if (Integer.parseInt(startingDate.split("/")[2]) >= startYear){
                            startYear = Integer.parseInt(startingDate.split("/")[2]);
                        }
                    } catch(Exception e){}
                }

                ArrayList<String> chunk;
                for (int i = -1; i < 16; i++){//start from -1 since it could potentially search last year's records
                    chunk = new ArrayList<String>();
                    chunk.add(Integer.toString(startYear + i));
                    chunk.add(Integer.toString(startYear + i));
                    publish(chunk);
                }
                return null;
            }

            @Override
            protected void process(List<ArrayList<String>> chunks) {
                for (ArrayList<String> chunk : chunks){
                    jStartYear.addItem(chunk.get(0));
                    jEndYear.addItem(chunk.get(1));
                }//for
            }

            @Override
            protected void done() {
                //jStartYear.setSelectedIndex(1);
                //jEndYear.setSelectedIndex(1);
            }

        }).execute();
    }

    private void initDateOperator(JComboBox comboBox){
        comboBox.removeAllItems();
        comboBox.addItem(getStringFromViewerPeriodsOperator(ViewerPeriodsOperator.EqualTo));
        comboBox.addItem(getStringFromViewerPeriodsOperator(ViewerPeriodsOperator.GreaterThan));
        comboBox.addItem(getStringFromViewerPeriodsOperator(ViewerPeriodsOperator.GreaterThanEqualTo));
        comboBox.addItem(getStringFromViewerPeriodsOperator(ViewerPeriodsOperator.LessThan));
        comboBox.addItem(getStringFromViewerPeriodsOperator(ViewerPeriodsOperator.LessThanEqualTo));
    }

    private String getStringFromViewerPeriodsOperator(ViewerPeriodsOperator op){
        switch(op){
            case LessThan:
                return "<";
            case LessThanEqualTo:
                return "<=";
            case EqualTo:
                return "=";
            case GreaterThan:
                return ">";
            case GreaterThanEqualTo:
                return ">=";
            default:
            return "";
        }
    }

    private void populateLocations(){
        if (SwingUtilities.isEventDispatchThread()){
            populateStructuresHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateStructuresHelper();
                }
            });
        }
    }

    private void populateStructures(){
        if (SwingUtilities.isEventDispatchThread()){
            populateStructuresHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateStructuresHelper();
                }
            });
        }
    }

    private void populateStructuresHelper(){
        DefaultListModel model = new DefaultListModel();
        jStructureList.setModel(model);
        for (int i = 0; i < DataGlobal.getStructureNamePricingBase().size(); i++){
            model.addElement(DataGlobal.getStructureNamePricingBase().get(i));
        }
    }

    private void populateBrokers() {
        if (SwingUtilities.isEventDispatchThread()){
            DefaultListModel model = new DefaultListModel();
            jBrokerList.setModel(model);
            populateBrokersHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    DefaultListModel model = new DefaultListModel();
                    jBrokerList.setModel(model);
                    populateBrokersHelper();
                }
            });
        }
    }
    private void populateBrokersHelper() {
        (new SwingWorker<Void, IGatewayConnectorBuddy>(){
            @Override
            protected Void doInBackground() throws Exception {
                ArrayList<IGatewayConnectorBuddy> buddyList = viewer.getSortedBuddyList(GatewayServerType.AIM_SERVER_TYPE);
                buddyList.addAll(viewer.getSortedBuddyList(GatewayServerType.YIM_SERVER_TYPE));
                //publish
                for (IGatewayConnectorBuddy buddy : buddyList){
                    publish(buddy);
                }
                return null;
            }

            @Override
            protected void process(List<IGatewayConnectorBuddy> chunks) {
                for (IGatewayConnectorBuddy buddy : chunks){
                    ((DefaultListModel)jBrokerList.getModel()).addElement(buddy);
                }
            }
        }).execute();
    }
    
    @Override
    public void displayDialog() {
        if (SwingUtilities.isEventDispatchThread()){
            displayDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayDialogHelper();
                }
            });
        }
    }

    private void displayDialogHelper(){
        jResult.setText(resultMessage);
        jResult.setForeground(Color.black);
        jResult.setBackground(jSearchLabel.getBackground());
        jSearchButton.setEnabled(true);

        this.populateBrokers();
        this.populateLocations();

        setVisible(true);
    }

    @Override
    public void hideDialog() {
        if (SwingUtilities.isEventDispatchThread()){
            hideDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    hideDialogHelper();
                }
            });
        }
    }

    private void hideDialogHelper(){
        jSearchButton.setEnabled(true);
        setVisible(false);
    }

    @Override
    public JDialog getBaseDialog() {
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jDateRange = new javax.swing.JPanel();
        jNoDateLimit = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMessage = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jSearchButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jKeywords = new javax.swing.JTextField();
        jSearchLabel = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jResult = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jAndOrStructures = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jAndOrLocations = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jStartYear = new javax.swing.JComboBox();
        jStartDateOperator = new javax.swing.JComboBox();
        jStartMonth = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jEndYear = new javax.swing.JComboBox();
        jEndDateOperator = new javax.swing.JComboBox();
        jEndMonth = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jStructureList = new javax.swing.JList();
        jClearStructueList = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        jLocationList = new javax.swing.JList();
        jAndOrPeriod = new javax.swing.JComboBox();
        jClearLocationList = new javax.swing.JCheckBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        jBrokerList = new javax.swing.JList();
        jClearBrokerList = new javax.swing.JCheckBox();
        jBrokerName = new javax.swing.JTextField();
        jStructure = new javax.swing.JTextField();
        jLocation = new javax.swing.JTextField();
        jEnablePeriod = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search (From - To):", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(51, 51, 51))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jDateRange.setName("jDateRange"); // NOI18N
        jDateRange.setPreferredSize(new java.awt.Dimension(125, 80));
        jDateRange.setLayout(new java.awt.GridLayout(2, 1, 10, 5));

        jNoDateLimit.setSelected(true);
        jNoDateLimit.setText("No Date Range");
        jNoDateLimit.setName("jNoDateLimit"); // NOI18N
        jNoDateLimit.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jNoDateLimitItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jNoDateLimit))
                    .addComponent(jDateRange, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jDateRange, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jNoDateLimit)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jMessage.setBackground(new java.awt.Color(236, 233, 216));
        jMessage.setColumns(20);
        jMessage.setEditable(false);
        jMessage.setFont(new java.awt.Font("Tahoma", 0, 11));
        jMessage.setLineWrap(true);
        jMessage.setRows(5);
        jMessage.setText("If instant messages contain \"keywords\" you typed in, the result will be returned. You may also set criteria for legs. \n\nIf any leg matches the criteria you set, such quotes will be returned. \n\nIf nothing was set, search result includes all recorded quotes.");
        jMessage.setWrapStyleWord(true);
        jMessage.setBorder(null);
        jMessage.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jMessage.setName("jMessage"); // NOI18N
        jScrollPane1.setViewportView(jMessage);

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new java.awt.GridLayout(1, 2, 3, 0));

        jSearchButton.setText("Search");
        jSearchButton.setName("jSearchButton"); // NOI18N
        jSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSearchButtonActionPerformed(evt);
            }
        });
        jPanel4.add(jSearchButton);

        jCancelButton.setText("Cancel");
        jCancelButton.setName("jCancelButton"); // NOI18N
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelButtonActionPerformed(evt);
            }
        });
        jPanel4.add(jCancelButton);

        jKeywords.setName("jKeywords"); // NOI18N

        jSearchLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        jSearchLabel.setText("Keywords:");
        jSearchLabel.setName("jSearchLabel"); // NOI18N

        jScrollPane5.setBorder(null);
        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jResult.setBackground(new java.awt.Color(236, 233, 216));
        jResult.setColumns(20);
        jResult.setEditable(false);
        jResult.setFont(new java.awt.Font("Tahoma", 0, 11));
        jResult.setLineWrap(true);
        jResult.setRows(5);
        jResult.setText("Please build up your own search criteria.");
        jResult.setWrapStyleWord(true);
        jResult.setBorder(null);
        jResult.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jResult.setName("jResult"); // NOI18N
        jScrollPane5.setViewportView(jResult);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel3.setText("Search Message:");
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jKeywords, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addComponent(jSearchLabel)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSearchLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jKeywords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search Leg(s) By Criteria: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setText("Brokers:");
        jLabel1.setName("jLabel1"); // NOI18N

        jAndOrStructures.setName("jAndOrStructures"); // NOI18N

        jLabel2.setText("Structures:");
        jLabel2.setName("jLabel2"); // NOI18N

        jAndOrLocations.setName("jAndOrLocations"); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Start Date", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N
        jPanel5.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel5.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel5.setName("jPanel5"); // NOI18N

        jStartYear.setEnabled(false);
        jStartYear.setMaximumSize(new java.awt.Dimension(53, 20));
        jStartYear.setMinimumSize(new java.awt.Dimension(53, 20));
        jStartYear.setName("jStartYear"); // NOI18N
        jStartYear.setPreferredSize(new java.awt.Dimension(53, 20));

        jStartDateOperator.setEnabled(false);
        jStartDateOperator.setMaximumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator.setMinimumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator.setName("jStartDateOperator"); // NOI18N
        jStartDateOperator.setPreferredSize(new java.awt.Dimension(42, 20));

        jStartMonth.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jStartMonth.setEnabled(false);
        jStartMonth.setMaximumSize(new java.awt.Dimension(37, 20));
        jStartMonth.setMinimumSize(new java.awt.Dimension(37, 20));
        jStartMonth.setName("jStartMonth"); // NOI18N
        jStartMonth.setPreferredSize(new java.awt.Dimension(37, 20));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jStartDateOperator, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jStartMonth, 0, 51, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jStartYear, 0, 67, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStartDateOperator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStartYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStartMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "End Date", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N
        jPanel6.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel6.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel6.setName("jPanel6"); // NOI18N

        jEndYear.setEnabled(false);
        jEndYear.setMaximumSize(new java.awt.Dimension(53, 20));
        jEndYear.setMinimumSize(new java.awt.Dimension(53, 20));
        jEndYear.setName("jEndYear"); // NOI18N
        jEndYear.setPreferredSize(new java.awt.Dimension(53, 20));

        jEndDateOperator.setEnabled(false);
        jEndDateOperator.setMaximumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator.setMinimumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator.setName("jEndDateOperator"); // NOI18N
        jEndDateOperator.setPreferredSize(new java.awt.Dimension(42, 20));

        jEndMonth.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jEndMonth.setEnabled(false);
        jEndMonth.setMaximumSize(new java.awt.Dimension(37, 20));
        jEndMonth.setMinimumSize(new java.awt.Dimension(37, 20));
        jEndMonth.setName("jEndMonth"); // NOI18N
        jEndMonth.setPreferredSize(new java.awt.Dimension(37, 20));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jEndDateOperator, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEndMonth, 0, 51, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEndYear, 0, 67, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jEndDateOperator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEndYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEndMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("Locations:");
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jStructureList.setName("jStructureList"); // NOI18N
        jScrollPane2.setViewportView(jStructureList);

        jClearStructueList.setText("Clear Structures");
        jClearStructueList.setName("jClearStructueList"); // NOI18N
        jClearStructueList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jClearStructueListItemStateChanged(evt);
            }
        });

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jLocationList.setName("jLocationList"); // NOI18N
        jScrollPane3.setViewportView(jLocationList);

        jAndOrPeriod.setName("jAndOrPeriod"); // NOI18N

        jClearLocationList.setText("Clear Locations");
        jClearLocationList.setName("jClearLocationList"); // NOI18N
        jClearLocationList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jClearLocationListItemStateChanged(evt);
            }
        });

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jBrokerList.setName("jBrokerList"); // NOI18N
        jScrollPane4.setViewportView(jBrokerList);

        jClearBrokerList.setText("Clear Brokers");
        jClearBrokerList.setName("jClearBrokerList"); // NOI18N
        jClearBrokerList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jClearBrokerListItemStateChanged(evt);
            }
        });

        jBrokerName.setEditable(false);
        jBrokerName.setName("jBrokerName"); // NOI18N

        jStructure.setEditable(false);
        jStructure.setName("jStructure"); // NOI18N

        jLocation.setEditable(false);
        jLocation.setName("jLocation"); // NOI18N

        jEnablePeriod.setText("Enable Period for Search");
        jEnablePeriod.setName("jEnablePeriod"); // NOI18N
        jEnablePeriod.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jEnablePeriodItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jEnablePeriod)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addGap(15, 15, 15)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jAndOrPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jClearLocationList)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE))
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                                .addComponent(jLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                            .addGap(61, 61, 61))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(11, Short.MAX_VALUE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addGap(10, 10, 10)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(jAndOrLocations, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jClearStructueList))
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                                        .addComponent(jStructure, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addGap(24, 24, 24)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(jAndOrStructures, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jClearBrokerList)
                                            .addGap(24, 24, 24))
                                        .addComponent(jBrokerName)
                                        .addComponent(jScrollPane4, 0, 0, Short.MAX_VALUE))))
                            .addGap(61, 61, 61)))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBrokerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAndOrStructures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jClearBrokerList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStructure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAndOrLocations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jClearStructueList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAndOrPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jClearLocationList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jEnablePeriod)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jNoDateLimitItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jNoDateLimitItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jFromDateChooser.setEnabled(false);
            jToDateChooser.setEnabled(false);
        }else if (evt.getStateChange() == ItemEvent.DESELECTED){
            jFromDateChooser.setEnabled(true);
            jToDateChooser.setEnabled(true);
        }
    }//GEN-LAST:event_jNoDateLimitItemStateChanged

    private void jSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSearchButtonActionPerformed
        this.jSearchButton.setEnabled(false);
        viewer.prepareForSearchResult();
        (new SwingWorker<String, IPbsysOptionQuote>(){

            @Override
            protected String doInBackground() throws Exception {
                String result = "No results found. Please try again!";
                IViewerSearchCriteria criteria;
                try{
                    criteria = constructSearchCriteria();
                    ArrayList<IPbsysOptionQuote> data = viewer.getKernel().retrieveHistoricalQuotes(
                            viewer.getPointBoxLoginUser(), criteria);
                    if (!data.isEmpty()){
                        for (IPbsysOptionQuote quote : data){
                            publish(quote);
                        }//for
                        result = "";
                    }
                }catch (Exception ex){
                    result = ex.getMessage();
                }
                return result;
            }

            @Override
            protected void process(List<IPbsysOptionQuote> chunks) {
                for (IPbsysOptionQuote quote : chunks){
                    viewer.publishSearchResult(quote);
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if ((result == null) || (result.isEmpty())){//successful
                        hideDialog();
                    }else{                                      //no results
                        jResult.setForeground(Color.red);
                        jResult.setText(result);
                        jSearchButton.setEnabled(true);
                    }
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                    hideDialog();
                } catch (ExecutionException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                    hideDialog();
                }
            }

            private IViewerSearchCriteria constructSearchCriteria() throws Exception{
                ViewerSearchCriteria criteria = new ViewerSearchCriteria();

                if (jNoDateLimit.isSelected()){
                    criteria.setDateRangeLimited(false);
                    criteria.setFromDate(null);
                    criteria.setToDate(null);
                }else{
                    criteria.setDateRangeLimited(true);
                    Calendar date = jFromDateChooser.getCalendar();
                    if (date == null){
                        criteria.setFromDate(null);
                    }else{

                        GregorianCalendar gDate = new GregorianCalendar(date.get(Calendar.YEAR),
                                                                        date.get(Calendar.MONTH),
                                                                        date.get(Calendar.DAY_OF_MONTH),
                                                                        0, 0, 0);
                        //value.get(Calendar.YEAR)
                        criteria.setFromDate(new Date(gDate.getTimeInMillis()));
                    }
                    date = jToDateChooser.getCalendar();
                    if (date == null){
                        criteria.setToDate(null);
                    }else{
                        GregorianCalendar gDate = new GregorianCalendar(date.get(Calendar.YEAR),
                                                                        date.get(Calendar.MONTH),
                                                                        date.get(Calendar.DAY_OF_MONTH),
                                                                        23, 59, 59);
                        criteria.setToDate(new Date(gDate.getTimeInMillis()));
                    }
                }
                criteria.setKeywords(jKeywords.getText().trim());

                if (jClearBrokerList.isSelected()){
                    criteria.setBrokers(null);
                    criteria.setBrokerName(jBrokerName.getText());
                }else{
                    criteria.setBrokers(jBrokerList.getSelectedValues());
                    criteria.setBrokerName("");
                }
                criteria.setAndOrStructures((ViewerSearchCriteriaTerms)jAndOrStructures.getSelectedItem());

                if (jClearStructueList.isSelected()){
                    criteria.setStructures(new Object[]{jStructure.getText()});
                }else{
                    criteria.setStructures(jStructureList.getSelectedValues());
                }
                criteria.setAndOrLocations((ViewerSearchCriteriaTerms)jAndOrLocations.getSelectedItem());

                if (jClearLocationList.isSelected()){
                    criteria.setLocations(new Object[]{jLocation.getText()});
                }else{
                    criteria.setLocations(jLocationList.getSelectedValues());
                }
                criteria.setAndOrPeriod((ViewerSearchCriteriaTerms)jAndOrPeriod.getSelectedItem());

                if (jEnablePeriod.isSelected()){
                    criteria.setStartDateOperator((ViewerPeriodsOperator)jStartDateOperator.getSelectedItem());
                    criteria.setStartDate(getStartDate());
                    criteria.setEndDateOperator((ViewerPeriodsOperator)jEndDateOperator.getSelectedItem());
                    criteria.setEndDate(getEndDate());
                }else{
                    criteria.setStartDateOperator(null);
                    criteria.setStartDate(null);
                    criteria.setEndDateOperator(null);
                    criteria.setEndDate(null);
                }

                return criteria;
            }

            private Date getStartDate(){
                return new Date(getSelectedGregorianCalendar(jStartMonth, jStartYear, 1).getTimeInMillis());
            }

            private Date getEndDate(){
                return new Date(getSelectedGregorianCalendar(jEndMonth, jEndYear, 1).getTimeInMillis());

            }

        }).execute();
    }//GEN-LAST:event_jSearchButtonActionPerformed



    private GregorianCalendar getSelectedGregorianCalendar(JComboBox monthBox, JComboBox yearBox, int day) {
        String mString = getSelectedStringValue(monthBox);
        String yString = getSelectedStringValue(yearBox);
        if ((mString.isEmpty()) || (yString.isEmpty())){
            return null;
        }else{
            GregorianCalendar result = new GregorianCalendar(Integer.parseInt(yString),
                                                             CalendarGlobal.convertToIntegerMonth(mString)-1, 1, 0, 0, 0);
            if (day > 1){
                result.set(Calendar.DAY_OF_MONTH, result.getMaximum(Calendar.MONTH));
            }
            return result;
        }
    }
    
    private String getSelectedStringValue(JComboBox box){
        Object obj = box.getSelectedItem();
        if ((obj != null) && (obj instanceof String)){
            return ((String)obj).trim();
        }else{
            return "";
        }
    }
    
    private void jClearStructueListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jClearStructueListItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jStructureList.setEnabled(false);
            jStructure.setEditable(true);
        }else if (evt.getStateChange() == ItemEvent.DESELECTED){
            jStructureList.setEnabled(true);
            jStructure.setText("");
            jStructure.setEditable(false);
        }
    }//GEN-LAST:event_jClearStructueListItemStateChanged

    private void jClearLocationListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jClearLocationListItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jLocationList.setEnabled(false);
            jLocation.setEditable(true);
        }else if (evt.getStateChange() == ItemEvent.DESELECTED){
            jLocationList.setEnabled(true);
            jLocation.setText("");
            jLocation.setEditable(false);
        }
    }//GEN-LAST:event_jClearLocationListItemStateChanged

    private void jClearBrokerListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jClearBrokerListItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jBrokerList.setEnabled(false);
            jBrokerName.setEditable(true);
        }else if (evt.getStateChange() == ItemEvent.DESELECTED){
            jBrokerList.setEnabled(true);
            jBrokerName.setText("");
            jBrokerName.setEditable(false);
        }
    }//GEN-LAST:event_jClearBrokerListItemStateChanged

    private void jEnablePeriodItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jEnablePeriodItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jStartDateOperator.setEnabled(true);
            jStartMonth.setEnabled(true);
            jStartYear.setEnabled(true);
            jEndDateOperator.setEnabled(true);
            jEndMonth.setEnabled(true);
            jEndYear.setEnabled(true);
        }else if (evt.getStateChange() == ItemEvent.DESELECTED){
            jStartDateOperator.setEnabled(false);
            jStartMonth.setEnabled(false);
            jStartYear.setEnabled(false);
            jEndDateOperator.setEnabled(false);
            jEndMonth.setEnabled(false);
            jEndYear.setEnabled(false);
        }
    }//GEN-LAST:event_jEnablePeriodItemStateChanged

    private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelButtonActionPerformed
        this.hideDialog();
    }//GEN-LAST:event_jCancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jAndOrLocations;
    private javax.swing.JComboBox jAndOrPeriod;
    private javax.swing.JComboBox jAndOrStructures;
    private javax.swing.JList jBrokerList;
    private javax.swing.JTextField jBrokerName;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JCheckBox jClearBrokerList;
    private javax.swing.JCheckBox jClearLocationList;
    private javax.swing.JCheckBox jClearStructueList;
    private javax.swing.JPanel jDateRange;
    private javax.swing.JCheckBox jEnablePeriod;
    private javax.swing.JComboBox jEndDateOperator;
    private javax.swing.JComboBox jEndMonth;
    private javax.swing.JComboBox jEndYear;
    private javax.swing.JTextField jKeywords;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jLocation;
    private javax.swing.JList jLocationList;
    private javax.swing.JTextArea jMessage;
    private javax.swing.JCheckBox jNoDateLimit;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextArea jResult;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton jSearchButton;
    private javax.swing.JLabel jSearchLabel;
    private javax.swing.JComboBox jStartDateOperator;
    private javax.swing.JComboBox jStartMonth;
    private javax.swing.JComboBox jStartYear;
    private javax.swing.JTextField jStructure;
    private javax.swing.JList jStructureList;
    // End of variables declaration//GEN-END:variables

}
