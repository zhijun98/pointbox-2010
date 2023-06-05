/**
 * Eclipse Market Solutions LLC
 */
/*
 * ViewerFilterDialog.java
 *
 * @author Zhijun Zhang
 * Created on Jun 16, 2010, 10:31:59 AM
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.PointBoxFatalException;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.PbcFaceFactory;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyKey;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyValue;
import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Zhijun Zhang
 */
class ViewerFilterDialogWindow extends javax.swing.JDialog implements IPbconsoleDialog{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(ViewerFilterDialogWindow.class.getName());
    }
    private static final long serialVersionUID = 1L;
    
    private final IPbcViewer viewer;

    private static int defaultFilterId = 1;

    //private ViewerSearchDialog searchDialog;

    /** Creates new form ViewerFilterDialog
     * @param system
     * @param parent
     * @param modal
     */
    ViewerFilterDialogWindow(IPbcViewer viewer, boolean modal) {
        super(viewer.getPointBoxFrame(), modal);
        initComponents();

        this.viewer = viewer;

        //searchDialog = new ViewerSearchDialog(system, parent, modal);

        initializeComponentYears();
        initializeStructures();
        populateLocations();
//
//       populateBrokersInEDT();
//       populateGroupsInEDT();
        
        jStartDateOperator01.setSelectedItem(">=");
        jEndDateOperator01.setSelectedItem("<=");
        jStartDateOperator02.setSelectedIndex(0);
        jEndDateOperator02.setSelectedIndex(0);
        jEndYear01.setSelectedIndex(jEndYear01.getItemCount()-1);

        disableLeg2Zone();

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setTitle("Filter Aggregator");

        setSelctionjList(jInTraderGroups);
        setSelctionjList(jExTraderGroups);
        setSelctionjList(jInTraderNames);
        setSelctionjList(jExTraderNames);
        
        jNoBrokerIncluded.setVisible(false);
        setResizable(false);
    }
    
    private void setSelctionjList(JList jList){
        jList.clearSelection();
        jList.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (super.isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				} else {
					super.addSelectionInterval(index0, index1);
				}
			}
		});
    }

    @Override
    public JDialog getBaseDialog() {
        return this;
    }

    @Override
    public void setVisible(boolean value) {
        super.setVisible(value);
        if (value){    
            setLocation(SwingGlobal.getCenterPointOfParentWindow(viewer.getPointBoxFrame(), this));
            populateLocations();
            populateBrokersInEDT();
            populateGroupsInEDT();
        }
    }

    /**
     * todo: disable broker list temporarily
     */
    private void populateBrokersInEDT() {
        if (SwingUtilities.isEventDispatchThread()){
            populateBrokersInEDTHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateBrokersInEDTHelper();
                }
            });
        }
    }
    
    /*
     * Judge whether the buddy existed in the JList 
     */
    private boolean isBuddyExsitedInList(IGatewayConnectorBuddy buddy, JList list){
        for(int i=0;i<list.getModel().getSize();i++){
               if(buddy.equals(list.getModel().getElementAt(i)))
                   return true;           
        }
        return false;
    }
    private void populateBrokersInEDTHelper() {
        
        (new SwingWorker<Void, IGatewayConnectorBuddy>(){
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(50);  //update can be delayed for shouing JList correctly
                jInTraderNames.setModel(new DefaultListModel());
                jExTraderNames.setModel(new DefaultListModel());
                ArrayList<IGatewayConnectorBuddy> buddyList = viewer.getSortedBuddyList(GatewayServerType.AIM_SERVER_TYPE);
                buddyList.addAll(viewer.getSortedBuddyList(GatewayServerType.PBIM_SERVER_TYPE));
                buddyList.addAll(viewer.getSortedBuddyList(GatewayServerType.YIM_SERVER_TYPE));
                //publish
                for (IGatewayConnectorBuddy buddy : buddyList){
                    if(!buddy.getIMUniqueName().equalsIgnoreCase(viewer.getKernel().getPointBoxLoginUser().getIMUniqueName()))
                    publish(buddy);
                }
                return null;
            }

            @Override
            protected void process(List<IGatewayConnectorBuddy> chunks) {
                for (IGatewayConnectorBuddy buddy : chunks){
                    BuddyWrapper buddyWrapper=new BuddyWrapper(buddy);
                    if(!isBuddyExsitedInList(buddy, jInTraderNames))
                         ((DefaultListModel)jInTraderNames.getModel()).addElement(buddyWrapper);  //add buddies into the default model for jList
                    if(!isBuddyExsitedInList(buddy, jExTraderNames))
                         ((DefaultListModel)jExTraderNames.getModel()).addElement(buddyWrapper);
                }
            }

            @Override
            protected void done() {
                jInTraderNames.setSelectedIndex(-1);
                jExTraderNames.setSelectedIndex(-1);
                jInTraderNames.setCellRenderer(new BuddyListRenderer()); //set mode firstly and then set cell renderer
                jExTraderNames.setCellRenderer(new BuddyListRenderer());
            }
        }).execute();
    }
    
    private void populateGroupsInEDT() {
        if (SwingUtilities.isEventDispatchThread()){
            populateGroupsInEDTHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateGroupsInEDTHelper();
                }
            });
        }
    }
    
    private void populateGroupsInEDTHelper() {

        (new SwingWorker<Void, IGatewayConnectorGroup>(){
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(30); //update can be delayed for shouing JList correctly
                jInTraderGroups.setModel(new DefaultListModel());
                jExTraderGroups.setModel(new DefaultListModel());
                ArrayList<IGatewayConnectorGroup> groups = viewer.getAllGroups();
                //publish
                for (IGatewayConnectorGroup group : groups){
                    publish(group);
                }
                return null;
            }

            @Override
            protected void process(List<IGatewayConnectorGroup> chunks) {
                for (IGatewayConnectorGroup group : chunks){
                    GroupWrapper groupWrapper=new GroupWrapper(group);
                    
                    ((DefaultListModel)jInTraderGroups.getModel()).addElement(groupWrapper);  //add buddies into the default model for jList
                    
                    ((DefaultListModel)jExTraderGroups.getModel()).addElement(groupWrapper);
                }
            }

            @Override
            protected void done() {
                jInTraderGroups.setSelectedIndex(-1);
                jExTraderGroups.setSelectedIndex(-1);
                jInTraderGroups.setCellRenderer(new GroupListRenderer());
                jExTraderGroups.setCellRenderer(new GroupListRenderer());
            }
        }).execute();
    }


    private void initializeComponentYears(){
        (new SwingWorker<Void, ArrayList<String>>(){
            @Override
            protected Void doInBackground() throws Exception {
//                String startingDate = viewer.getKernel().getPointBoxConsoleRuntime().getPointBoxPricingSettings()
//                        .getExpirationSettlementDates(viewer.getKernel().getPointBoxConsoleRuntime().getDefaultQuoteCommodity()).getFrontContract();
                GregorianCalendar today = new GregorianCalendar();
                Integer startYear = today.get(Calendar.YEAR);
//                if (startingDate.contains("/") && startingDate.split("/").length > 1){
//                    try{
//                        if (Integer.parseInt(startingDate.split("/")[2]) >= startYear){
//                            startYear = Integer.parseInt(startingDate.split("/")[2]);
//                        }
//                    } catch(Exception e){}
//                }
//                
                ArrayList<String> chunk;
                for (int i = 0; i < 16; i++){
                    chunk = new ArrayList<String>();
                    chunk.add(Integer.toString(startYear + i));
                    chunk.add(Integer.toString(startYear + i));
                    chunk.add(Integer.toString(startYear + i));
                    chunk.add(Integer.toString(startYear + i));
                    publish(chunk);
                }
                return null;
            }

            @Override
            protected void process(List<ArrayList<String>> chunks) {
                for (ArrayList<String> chunk : chunks){
                    jStartYear01.addItem(chunk.get(0));
                    jStartYear02.addItem(chunk.get(1));
                    jEndYear01.addItem(chunk.get(2));
                    jEndYear02.addItem(chunk.get(3));
                }//for
                jStartMonth01.setSelectedIndex(-1);
                jStartMonth02.setSelectedIndex(-1);
                jEndMonth01.setSelectedIndex(-1);
                jEndMonth02.setSelectedIndex(-1);
                jStartYear01.setSelectedIndex(-1);
                jStartYear02.setSelectedIndex(-1);
                jEndYear01.setSelectedIndex(-1);
                jEndYear02.setSelectedIndex(-1);
            }

        }).execute();
    }

    private void initializeStructures(){
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
        jStructures01.removeAllItems();
        jStructures02.removeAllItems();
        for (int i = 0; i < DataGlobal.getStructureNamePricingBase().size(); i++){
            jStructures01.addItem(DataGlobal.getStructureNamePricingBase().get(i));
            jStructures02.addItem(DataGlobal.getStructureNamePricingBase().get(i));
        }
        jStructures01.insertItemAt(" ", 0);
        jStructures01.setSelectedIndex(0);
        jStructures02.insertItemAt(" ", 0);
        jStructures02.setSelectedIndex(0);
    }

    private void populateLocations() {
        if (SwingUtilities.isEventDispatchThread()){
            populateLocationsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateLocationsHelper();
                }
            });
        }
    }
    private void populateLocationsHelper(){
        ArrayList<String> locations = viewer.retrievedBufferedLocations();
        if (locations.size() > 0){
            jLocations01.removeAllItems();
            jLocations02.removeAllItems();
        }
        for (String location : locations){
            try{
                jLocations01.addItem(location);
                jLocations02.addItem(location);
            }catch (Exception e){
            }
        }//for
        jLocations01.addItem("Henry Hub");
        jLocations01.insertItemAt(" ", 0);
        jLocations01.setSelectedIndex(0);
        jLocations02.addItem("Henry Hub");
        jLocations02.insertItemAt(" ", 0);
        jLocations02.setSelectedIndex(0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel8 = new javax.swing.JPanel();
        jNoBrokerIncluded = new javax.swing.JCheckBox();
        jLeg2Included = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jFilter = new javax.swing.JButton();
        jCancel = new javax.swing.JButton();
        jLeg02 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jEndDateOperator02 = new javax.swing.JComboBox();
        jEndMonth02 = new javax.swing.JComboBox();
        jEndYear02 = new javax.swing.JComboBox();
        jStructures02 = new javax.swing.JComboBox();
        jPanel7 = new javax.swing.JPanel();
        jStartDateOperator02 = new javax.swing.JComboBox();
        jStartMonth02 = new javax.swing.JComboBox();
        jStartYear02 = new javax.swing.JComboBox();
        jLocations02 = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jStrike1Leg02 = new javax.swing.JTextField();
        jStrike2Leg02 = new javax.swing.JTextField();
        jStrike3Leg02 = new javax.swing.JTextField();
        jStrike4Leg02 = new javax.swing.JTextField();
        jLeg01 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jEndYear01 = new javax.swing.JComboBox();
        jEndDateOperator01 = new javax.swing.JComboBox();
        jEndMonth01 = new javax.swing.JComboBox();
        jStructures01 = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jStartYear01 = new javax.swing.JComboBox();
        jStartDateOperator01 = new javax.swing.JComboBox();
        jStartMonth01 = new javax.swing.JComboBox();
        jLocations01 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jStrike1Leg01 = new javax.swing.JTextField();
        jStrike2Leg01 = new javax.swing.JTextField();
        jStrike3Leg01 = new javax.swing.JTextField();
        jStrike4Leg01 = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jInTraderNames = new javax.swing.JList();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jExTraderNames = new javax.swing.JList();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jInTraderGroups = new javax.swing.JList();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jExTraderGroups = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        tabNameField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setIconImage(null);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel8.setName("jPanel8"); // NOI18N

        jNoBrokerIncluded.setText("No Broker Included");
        jNoBrokerIncluded.setName("jNoBrokerIncluded"); // NOI18N
        jNoBrokerIncluded.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jNoBrokerIncludedItemStateChanged(evt);
            }
        });

        jLeg2Included.setText("Leg 2 Included");
        jLeg2Included.setName("jLeg2Included"); // NOI18N
        jLeg2Included.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jLeg2IncludedItemStateChanged(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        jFilter.setText("Filter from Aggregator");
        jFilter.setName("jFilter"); // NOI18N
        jFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFilterActionPerformed(evt);
            }
        });

        jCancel.setText("Cancel");
        jCancel.setName("jCancel"); // NOI18N
        jCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelActionPerformed(evt);
            }
        });

        jLeg02.setBorder(javax.swing.BorderFactory.createTitledBorder("Leg 2"));
        jLeg02.setMaximumSize(new java.awt.Dimension(203, 312));
        jLeg02.setMinimumSize(new java.awt.Dimension(203, 312));
        jLeg02.setName("jLeg02"); // NOI18N

        jLabel8.setText("Structure:");
        jLabel8.setName("jLabel8"); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("End Date"));
        jPanel6.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel6.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel6.setName("jPanel6"); // NOI18N

        jEndDateOperator02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "<", "<=", "=", ">", ">=" }));
        jEndDateOperator02.setMaximumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator02.setMinimumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator02.setName("jEndDateOperator02"); // NOI18N
        jEndDateOperator02.setPreferredSize(new java.awt.Dimension(42, 20));

        jEndMonth02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jEndMonth02.setMaximumSize(new java.awt.Dimension(37, 20));
        jEndMonth02.setMinimumSize(new java.awt.Dimension(37, 20));
        jEndMonth02.setName("jEndMonth02"); // NOI18N
        jEndMonth02.setPreferredSize(new java.awt.Dimension(37, 20));

        jEndYear02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        jEndYear02.setMaximumSize(new java.awt.Dimension(53, 20));
        jEndYear02.setMinimumSize(new java.awt.Dimension(53, 20));
        jEndYear02.setName("jEndYear02"); // NOI18N
        jEndYear02.setPreferredSize(new java.awt.Dimension(53, 20));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jEndDateOperator02, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jEndMonth02, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEndYear02, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jEndYear02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jEndDateOperator02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jEndMonth02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jStructures02.setName("jStructures02"); // NOI18N

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Start Date"));
        jPanel7.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel7.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel7.setName("jPanel7"); // NOI18N

        jStartDateOperator02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "<", "<=", "=", ">", ">=" }));
        jStartDateOperator02.setMaximumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator02.setMinimumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator02.setName("jStartDateOperator02"); // NOI18N
        jStartDateOperator02.setPreferredSize(new java.awt.Dimension(42, 20));

        jStartMonth02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jStartMonth02.setMaximumSize(new java.awt.Dimension(37, 20));
        jStartMonth02.setMinimumSize(new java.awt.Dimension(37, 20));
        jStartMonth02.setName("jStartMonth02"); // NOI18N
        jStartMonth02.setPreferredSize(new java.awt.Dimension(37, 20));

        jStartYear02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        jStartYear02.setMaximumSize(new java.awt.Dimension(53, 20));
        jStartYear02.setMinimumSize(new java.awt.Dimension(53, 20));
        jStartYear02.setName("jStartYear02"); // NOI18N
        jStartYear02.setPreferredSize(new java.awt.Dimension(53, 20));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jStartDateOperator02, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jStartMonth02, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jStartYear02, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStartDateOperator02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStartYear02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStartMonth02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLocations02.setName("jLocations02"); // NOI18N

        jLabel9.setText("Location:");
        jLabel9.setName("jLabel9"); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Strikes"));
        jPanel5.setMaximumSize(new java.awt.Dimension(171, 83));
        jPanel5.setMinimumSize(new java.awt.Dimension(171, 83));
        jPanel5.setName("jPanel5"); // NOI18N

        jLabel11.setText("K1");
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel12.setText("K2");
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setText("K3");
        jLabel13.setName("jLabel13"); // NOI18N

        jLabel14.setText("K4");
        jLabel14.setName("jLabel14"); // NOI18N

        jStrike1Leg02.setName("jStrike1Leg02"); // NOI18N
        jStrike1Leg02.setPreferredSize(new java.awt.Dimension(20, 20));

        jStrike2Leg02.setName("jStrike2Leg02"); // NOI18N
        jStrike2Leg02.setPreferredSize(new java.awt.Dimension(20, 20));

        jStrike3Leg02.setName("jStrike3Leg02"); // NOI18N
        jStrike3Leg02.setPreferredSize(new java.awt.Dimension(20, 20));

        jStrike4Leg02.setName("jStrike4Leg02"); // NOI18N
        jStrike4Leg02.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jStrike2Leg02, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStrike1Leg02, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jStrike4Leg02, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStrike3Leg02, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jStrike1Leg02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jStrike2Leg02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jStrike3Leg02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jStrike4Leg02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jLeg02Layout = new javax.swing.GroupLayout(jLeg02);
        jLeg02.setLayout(jLeg02Layout);
        jLeg02Layout.setHorizontalGroup(
            jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLeg02Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jLeg02Layout.createSequentialGroup()
                        .addGroup(jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jStructures02, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLocations02, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jLeg02Layout.setVerticalGroup(
            jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLeg02Layout.createSequentialGroup()
                .addGroup(jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jStructures02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLocations02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLeg01.setBorder(javax.swing.BorderFactory.createTitledBorder("Leg 1"));
        jLeg01.setMaximumSize(new java.awt.Dimension(203, 312));
        jLeg01.setMinimumSize(new java.awt.Dimension(203, 312));
        jLeg01.setName("jLeg01"); // NOI18N

        jLabel2.setText("Structure:");
        jLabel2.setName("jLabel2"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("End Date"));
        jPanel2.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel2.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel2.setName("jPanel2"); // NOI18N

        jEndYear01.setMaximumSize(new java.awt.Dimension(53, 20));
        jEndYear01.setMinimumSize(new java.awt.Dimension(53, 20));
        jEndYear01.setName("jEndYear01"); // NOI18N
        jEndYear01.setPreferredSize(new java.awt.Dimension(53, 20));

        jEndDateOperator01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", "<=", "=", ">", ">=" }));
        jEndDateOperator01.setMaximumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator01.setMinimumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator01.setName("jEndDateOperator01"); // NOI18N
        jEndDateOperator01.setPreferredSize(new java.awt.Dimension(42, 20));

        jEndMonth01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jEndMonth01.setMaximumSize(new java.awt.Dimension(37, 20));
        jEndMonth01.setMinimumSize(new java.awt.Dimension(37, 20));
        jEndMonth01.setName("jEndMonth01"); // NOI18N
        jEndMonth01.setPreferredSize(new java.awt.Dimension(37, 20));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jEndDateOperator01, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEndMonth01, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEndYear01, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jEndDateOperator01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEndYear01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEndMonth01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jStructures01.setName("jStructures01"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Start Date"));
        jPanel3.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel3.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel3.setName("jPanel3"); // NOI18N

        jStartYear01.setMaximumSize(new java.awt.Dimension(53, 20));
        jStartYear01.setMinimumSize(new java.awt.Dimension(53, 20));
        jStartYear01.setName("jStartYear01"); // NOI18N
        jStartYear01.setPreferredSize(new java.awt.Dimension(53, 20));

        jStartDateOperator01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", "<=", "=", ">", ">=" }));
        jStartDateOperator01.setMaximumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator01.setMinimumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator01.setName("jStartDateOperator01"); // NOI18N
        jStartDateOperator01.setPreferredSize(new java.awt.Dimension(42, 20));

        jStartMonth01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jStartMonth01.setMaximumSize(new java.awt.Dimension(37, 20));
        jStartMonth01.setMinimumSize(new java.awt.Dimension(37, 20));
        jStartMonth01.setName("jStartMonth01"); // NOI18N
        jStartMonth01.setPreferredSize(new java.awt.Dimension(37, 20));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jStartDateOperator01, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jStartMonth01, 0, 40, Short.MAX_VALUE)
                .addGap(13, 13, 13)
                .addComponent(jStartYear01, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStartDateOperator01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStartYear01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStartMonth01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLocations01.setName("jLocations01"); // NOI18N

        jLabel3.setText("Location:");
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Strikes"));
        jPanel4.setMaximumSize(new java.awt.Dimension(171, 83));
        jPanel4.setMinimumSize(new java.awt.Dimension(171, 83));
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel5.setText("K1");
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText("K2");
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setText("K3");
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel10.setText("K4");
        jLabel10.setName("jLabel10"); // NOI18N

        jStrike1Leg01.setName("jStrike1Leg01"); // NOI18N
        jStrike1Leg01.setPreferredSize(new java.awt.Dimension(20, 20));

        jStrike2Leg01.setName("jStrike2Leg01"); // NOI18N
        jStrike2Leg01.setPreferredSize(new java.awt.Dimension(20, 20));

        jStrike3Leg01.setName("jStrike3Leg01"); // NOI18N
        jStrike3Leg01.setPreferredSize(new java.awt.Dimension(20, 20));

        jStrike4Leg01.setName("jStrike4Leg01"); // NOI18N
        jStrike4Leg01.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jStrike2Leg01, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStrike1Leg01, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jStrike4Leg01, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStrike3Leg01, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jStrike1Leg01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jStrike2Leg01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jStrike3Leg01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jStrike4Leg01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jLeg01Layout = new javax.swing.GroupLayout(jLeg01);
        jLeg01.setLayout(jLeg01Layout);
        jLeg01Layout.setHorizontalGroup(
            jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLeg01Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jLeg01Layout.createSequentialGroup()
                        .addGroup(jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLocations01, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jStructures01, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jLeg01Layout.setVerticalGroup(
            jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLeg01Layout.createSequentialGroup()
                .addGroup(jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jStructures01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLocations01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Include Buddies"));
        jPanel10.setName("jPanel10"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jInTraderNames.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "trader1", "trader2", "trader3", "trader4", "trader5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jInTraderNames.setName("traderNames"); // NOI18N
        jScrollPane1.setViewportView(jInTraderNames);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Exclude Buddies"));
        jPanel11.setName("jPanel11"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jExTraderNames.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "trader1", "trader2", "trader3", "trader4", "trader5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jExTraderNames.setName("jExTraderNames"); // NOI18N
        jScrollPane2.setViewportView(jExTraderNames);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Include Groups"));
        jPanel12.setName("jPanel12"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jInTraderGroups.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "trader1", "trader2", "trader3", "trader4", "trader5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jInTraderGroups.setName("jInTraderGroups"); // NOI18N
        jScrollPane3.setViewportView(jInTraderGroups);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Exclude Groups"));
        jPanel13.setName("jPanel13"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jExTraderGroups.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "trader1", "trader2", "trader3", "trader4", "trader5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jExTraderGroups.setName("jExTraderGroups"); // NOI18N
        jScrollPane4.setViewportView(jExTraderGroups);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel1.setText("Filter Tab Name:");
        jLabel1.setName("jLabel1"); // NOI18N

        tabNameField.setName("tabNameField"); // NOI18N

        jLabel4.setText("(Optional)");
        jLabel4.setName("jLabel4"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel8Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 492, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jNoBrokerIncluded)
                                .addGap(18, 18, 18)
                                .addComponent(jLeg2Included))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel8Layout.createSequentialGroup()
                                        .addComponent(jLeg01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)
                                        .addComponent(jLeg02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel8Layout.createSequentialGroup()
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tabNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel4)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(23, 23, 23))))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(139, 139, 139)
                .addComponent(jFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLeg2Included)
                    .addComponent(jNoBrokerIncluded))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLeg01, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLeg02, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tabNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCancel)
                    .addComponent(jFilter))
                .addGap(52, 52, 52)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(484, 484, 484)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.ipadx = -19;
        gridBagConstraints.ipady = -528;
        getContentPane().add(jPanel8, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFilterActionPerformed
        final ArrayList<IViewerFilterCriteria> criteriaList = new ArrayList<IViewerFilterCriteria>();
        (new SwingWorker<String, Void>(){
            @Override
            protected String doInBackground() throws Exception {
                String uniqueFilterTabName;
                if(tabNameField.getText()==null||tabNameField.getText().trim().isEmpty()){
                    uniqueFilterTabName = "Filter Result " + defaultFilterId;
                    while (viewer.hasFilterTab(uniqueFilterTabName)){
                        defaultFilterId++;
                        uniqueFilterTabName = "Filter Result " + defaultFilterId;
                        if (defaultFilterId == Integer.MAX_VALUE){
                            defaultFilterId = 1;
                            break;
                        }
                    }
                }else{
                    uniqueFilterTabName=tabNameField.getText().trim();
                    if(viewer.hasFilterTab(uniqueFilterTabName)){
                        return "";
                    }
                }
                
                if (uniqueFilterTabName != null){
                    viewer.getKernel().getPointBoxConsoleRuntime().addPbcViewerSettings(uniqueFilterTabName);
                    constructViewerFilterCriteriaFromGuiControls(uniqueFilterTabName, criteriaList);
                }
                return uniqueFilterTabName;
            }
            
            @Override
            protected void done() {
                setVisible(false);
                try {
                    String uniqueFilterTabName = get();
                    if (uniqueFilterTabName == null){
                        JOptionPane.showMessageDialog(ViewerFilterDialogWindow.this, "Too many filters were created.");
                        setVisible(true);
                    }else if(uniqueFilterTabName.isEmpty()){
                        JOptionPane.showMessageDialog(ViewerFilterDialogWindow.this, "This filter name has already existed! Please change another one.");
                        tabNameField.setText("");
                        setVisible(true);                        
                    }else{
                        //create a new tab viewer for this filter list
//                        for (IViewerFilterCriteria fc : criteriaList){
//                            logger.log(Level.INFO, fc.get);
//                        }
                        viewer.constructFilterTab(uniqueFilterTabName, criteriaList);
                    }
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(ViewerFilterDialogWindow.this, ex.getMessage());
                    setVisible(true);
                } catch (ExecutionException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(ViewerFilterDialogWindow.this, ex.getMessage());
                    setVisible(true);
                }
            }
        
        }).execute();
}//GEN-LAST:event_jFilterActionPerformed

    private void recordFilterProperty(String uniqueFilterTabName,
                                      FilterPropertyKey key,
                                      String value)
    {
        PbcFilterPropertySettings aPbcFilterPropertySettings = new PbcFilterPropertySettings();
        aPbcFilterPropertySettings.setPropertyKey(key.toString());
        aPbcFilterPropertySettings.setPropertyValue(value);
        viewer.getKernel().getPointBoxConsoleRuntime().addPbcFilterPropertySettings(uniqueFilterTabName, aPbcFilterPropertySettings);

    }

//    //trader means broker. They are same.
//    private String constructTraderNamesCriteriaHelper(){
//       Object[] objs = jInTraderNames.getSelectedValues();
//       Object[] objs2 = jExTraderNames.getSelectedValues();   //get objects in jNoTraderName List
//        String brokerNames=null;
//        //construct the String of brokerNames for the JBrokerNameList
//        if (objs.length> 0){
//            for(Object o:objs){
//                 Object obj=((BuddyWrapper)o).getBuddy();
//                 if (obj instanceof IGatewayConnectorBuddy){
//                     String brokerName = ((IGatewayConnectorBuddy)obj).getIMUniqueName();
//                     brokerNames=brokerName+";"+brokerNames;
//                 }    
//            }
//            return brokerNames;
//        }
//        //construct the String of brokerNames for the JNoBrokerNameList. The solution is similar. Just get the non-selected broker names.
//        if(objs2.length>0){        
//            List<Object> list=new ArrayList<Object>();
//            for(int i=0;i<objs2.length;i++){
//                list.add(objs2[i]);
//            }           
//            for(int i=0;i<jExTraderNames.getModel().getSize();i++){
//                Object broker=((BuddyWrapper)jExTraderNames.getModel().getElementAt(i)).getBuddy();
//                if(!list.contains(broker)){
//                     if (broker instanceof IGatewayConnectorBuddy){
//                          String brokerName = ((IGatewayConnectorBuddy)broker).getIMUniqueName();
//                          brokerNames=brokerName+";"+brokerNames;
//                     }    
//                }       
//            }
//            return brokerNames;
//        }
//        
//        return brokerNames;
//    }
    
    
    private String constructTraderNamesCriteriaString(){
       Object[] objsTraderIn = jInTraderNames.getSelectedValues();
       Object[] objsTraderEx = jExTraderNames.getSelectedValues();   //get objects in jNoTraderName List
       Object[] objsGroupIn=jInTraderGroups.getSelectedValues();
       Object[] objsGroupEx=jExTraderGroups.getSelectedValues();

       //1) firstly get the include Buddies collection. 2) get exclude buddies collection 3) include buddies subtracts the exclude buddies
       Set<IGatewayConnectorBuddy> inBuddies=new HashSet<IGatewayConnectorBuddy>();
       IPbcFace face=null;
       try {
               face=PbcFaceFactory.getPbcFaceSingleton(viewer.getKernel());
        } catch (PointBoxFatalException ex) {
               Logger.getLogger(ViewerFilterDialogWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
       if(objsTraderIn.length<=0 && objsGroupIn.length<=0){
           //if User didn't choose any include buddies and include groups, we should consider he choose all buddies
//           for(int i=0;i<jInTraderNames.getModel().getSize();i++){
//               inBuddies.add(((BuddyWrapper)jInTraderNames.getModel().getElementAt(i)).buddy);
//           }
       }else{
            for(Object o:objsGroupIn){
                if (o  instanceof GroupWrapper){
                     IGatewayConnectorGroup group=((GroupWrapper)o).group;
                     if(face!=null){
                         inBuddies.addAll(face.getPointBoxTalker().getBuddiesOfGroup(group));
                     }
                }
            }//for
           for(Object o:objsTraderIn){
               if (o  instanceof BuddyWrapper){
                    IGatewayConnectorBuddy buddy=((BuddyWrapper)o).buddy;
                    inBuddies.add(buddy);
               }
           }
               
       }//else
       
        Set<IGatewayConnectorBuddy> exBuddies=new HashSet<IGatewayConnectorBuddy>();
        Set<IGatewayConnectorBuddy> finalBuddies=new HashSet<IGatewayConnectorBuddy>();
        for(IGatewayConnectorBuddy buddy: inBuddies){
            finalBuddies.add(buddy);
        }
        if(objsTraderEx.length>0 ||objsGroupEx.length>0){
           for(Object o:objsGroupEx){
               if (o  instanceof GroupWrapper){
                    IGatewayConnectorGroup group=((GroupWrapper)o).group;
                    if(face!=null){
                        exBuddies.addAll(face.getPointBoxTalker().getBuddiesOfGroup(group));
                    }
               }
           }
           for(Object o:objsTraderEx){
               if (o  instanceof BuddyWrapper){
                    IGatewayConnectorBuddy buddy=((BuddyWrapper)o).buddy;
                    exBuddies.add(buddy);
               }
           }//for
           for(IGatewayConnectorBuddy exBuddy:exBuddies){
               for(IGatewayConnectorBuddy inBuddy:inBuddies){
                   if(inBuddy.getIMUniqueName().equalsIgnoreCase(exBuddy.getIMUniqueName())){
                       finalBuddies.remove(inBuddy);
                   }
              }//for
           }//for
           
        }//if
       
        String brokerNamesString = null;
        for(IGatewayConnectorBuddy buddy:finalBuddies){
            String brokerName = buddy.getIMUniqueName();
            brokerNamesString=brokerName+";"+brokerNamesString;
        }
        
        return brokerNamesString;
    }
        
    
    
    private void constructViewerFilterCriteriaFromGuiControls(String uniqueFilterTabName, ArrayList<IViewerFilterCriteria> criteriaList){
        if (uniqueFilterTabName == null){
            return;
        }
        ArrayList<String> items;
        /*create structures criteria list*/
        if (jStructures01.getSelectedIndex() > 0){

            recordFilterProperty(uniqueFilterTabName, 
                                 FilterPropertyKey.jStructures01_Selected,
                                 FilterPropertyValue.Boolean_True_value.toString());

            items = new ArrayList<String>();
            items.add(DataGlobal.getProperEngineStrategyName(jStructures01.getSelectedItem().toString()));
            items.add(DataGlobal.getProperParsingStrategyName(jStructures01.getSelectedItem().toString()));
            
            recordFilterProperty(uniqueFilterTabName,
                                 FilterPropertyKey.jStructures01_SelectedItem,
                                 jStructures01.getSelectedItem().toString());

            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strategy, items, 0));
        }

        /*create brokers name criteria list*/
        String brokerNames = constructTraderNamesCriteriaString();
        if(brokerNames != null){    
            if(brokerNames.length()>0){
                brokerNames=brokerNames.substring(0,brokerNames.length()-1);        //Cut off the tail ";"
            }
            items = new ArrayList<String>();
            items.add(brokerNames);
            recordFilterProperty(uniqueFilterTabName,
                                FilterPropertyKey.jBrokerNames_SelectedName,
                                brokerNames);
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Brokers, items, 0));            
        }

        /*create strikes criteria list*/
        criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strikes,
                                                 generateStrikesItemsList(jStrike1Leg01,
                                                                          jStrike2Leg01,
                                                                          jStrike3Leg01,
                                                                          jStrike4Leg01),
                                                 0));
        recordFilterProperty(uniqueFilterTabName,
                             FilterPropertyKey.jStrike1Leg01_text,
                             jStrike1Leg01.getText());
        recordFilterProperty(uniqueFilterTabName,
                             FilterPropertyKey.jStrike2Leg01_text,
                             jStrike2Leg01.getText());
        recordFilterProperty(uniqueFilterTabName,
                             FilterPropertyKey.jStrike3Leg01_text,
                             jStrike3Leg01.getText());
        recordFilterProperty(uniqueFilterTabName,
                             FilterPropertyKey.jStrike4Leg01_text,
                             jStrike4Leg01.getText());

        /*create time period search criteria list*/
        criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Period, null, 0));

        if (jLeg2Included.isSelected()){

            recordFilterProperty(uniqueFilterTabName,
                                 FilterPropertyKey.jLeg2Included,
                                 "true");

            if (jStructures02.getSelectedIndex() > 0){

                recordFilterProperty(uniqueFilterTabName,
                                     FilterPropertyKey.jStructures02_Selected,
                                     "true");

                items = new ArrayList<String>();
                items.add(DataGlobal.getProperParsingStrategyName(jStructures02.getSelectedItem().toString()));
                items.add(DataGlobal.getProperEngineStrategyName(jStructures02.getSelectedItem().toString()));

                recordFilterProperty(uniqueFilterTabName,
                                     FilterPropertyKey.jStructures02_SelectedItem,
                                     "true");

                criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strategy, items, 1));
            }

            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Strikes,
                                                     generateStrikesItemsList(jStrike1Leg02,
                                                                              jStrike2Leg02,
                                                                              jStrike3Leg02,
                                                                              jStrike4Leg02),
                                                     1));
            recordFilterProperty(uniqueFilterTabName,
                                 FilterPropertyKey.jStrike1Leg02_text,
                                 jStrike1Leg01.getText());
            recordFilterProperty(uniqueFilterTabName,
                                 FilterPropertyKey.jStrike2Leg02_text,
                                 jStrike2Leg01.getText());
            recordFilterProperty(uniqueFilterTabName,
                                 FilterPropertyKey.jStrike3Leg02_text,
                                 jStrike3Leg01.getText());
            recordFilterProperty(uniqueFilterTabName,
                                 FilterPropertyKey.jStrike4Leg02_text,
                                 jStrike4Leg01.getText());

            /*create time period search criteria list*/
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Period, null, 1));
        }
    }

    private ArrayList<String> generateStrikesItemsList(JTextField strike1, JTextField strike2, JTextField strike3, JTextField strike4){
        ArrayList<String> items = new ArrayList<String>();

        items = getDoubleConvertedStringFirstEmbeddedNumber(strike1.getText(), items);
        items = getDoubleConvertedStringFirstEmbeddedNumber(strike2.getText(), items);
        items = getDoubleConvertedStringFirstEmbeddedNumber(strike3.getText(), items);
        items = getDoubleConvertedStringFirstEmbeddedNumber(strike4.getText(), items);

        return items;
    }

    private ArrayList<String> getDoubleConvertedStringFirstEmbeddedNumber(String text, ArrayList<String> items){
        String converted = DataGlobal.retrieveFirstEmbeddedNumber(text);
        if (!converted.isEmpty()){
            converted = (new Double(converted)).toString();
            items.add(converted);
        }

        return items;
    }

    private IViewerFilterCriteria getViewerFilterCriteria(String uniqueFilterTabName,
                                                          ViewerFilterCriteriaType terms,
                                                          ArrayList<String> items,
                                                          int legIndex)
    {
        IViewerFilterCriteria criteria = null;
        String tabNameString = "";

        if (items != null && items.size() > 0 && !items.get(0).isEmpty()){
            tabNameString = " " + items.get(0) + " ";
        }

        switch(terms){
            case Strategy:  criteria = new ViewerFilterByStrategies(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Strategy);
                            break;
            case Brokers:   criteria = new ViewerFilterByBrokers(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Brokers);
                            break;
            case QuoteClass:
                criteria = new ViewerFilterByClass(items);
                criteria.setFilterCriteria(ViewerFilterCriteriaType.QuoteClass);
                break;
            case Location:  criteria = new ViewerFilterByLocations(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Location);
                            break;
            case Period:    if ((jStartMonth01.getSelectedIndex() > -1) 
                                    && (jStartYear01.getSelectedIndex() > -1)
                                    && (jEndMonth01.getSelectedIndex() > -1)
                                    && (jEndYear01.getSelectedIndex() > -1))
                            {
                                GregorianCalendar selectedStartDate, selectedEndDate;
                                ViewerPeriodsOperator startOperator, endOperator;
                            
                                if (legIndex == 0){
                                    selectedStartDate = CalendarGlobal.getSelectedGregorianCalendar(
                                                            CalendarGlobal.getSelectedStringValue(jStartMonth01), 
                                                            CalendarGlobal.getSelectedStringValue(jStartYear01),
                                                            1);
                                    selectedEndDate = CalendarGlobal.getSelectedGregorianCalendar(
                                                            CalendarGlobal.getSelectedStringValue(jEndMonth01),
                                                            CalendarGlobal.getSelectedStringValue(jEndYear01), 1);
                                    startOperator = getSelectedViewerSearchByOperatorTerms(jStartDateOperator01);
                                    endOperator = getSelectedViewerSearchByOperatorTerms(jEndDateOperator01);
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg01_selectedStartDate_value,
                                                         Long.toString(selectedStartDate.getTimeInMillis()));
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg01_selectedEndDate_value,
                                                         Long.toString(selectedEndDate.getTimeInMillis()));
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg01_startOperator_value,
                                                         startOperator.toString());
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg01_endOperator_value,
                                                         endOperator.toString());
                                }else{
                                    selectedStartDate = CalendarGlobal.getSelectedGregorianCalendar(
                                                            CalendarGlobal.getSelectedStringValue(jStartMonth02),
                                                            CalendarGlobal.getSelectedStringValue(jStartYear02),
                                                            1);
                                    selectedEndDate = CalendarGlobal.getSelectedGregorianCalendar(
                                                            CalendarGlobal.getSelectedStringValue(jEndMonth02),
                                                            CalendarGlobal.getSelectedStringValue(jEndYear02),
                                                            1);
                                    startOperator = getSelectedViewerSearchByOperatorTerms(jStartDateOperator02);
                                    endOperator = getSelectedViewerSearchByOperatorTerms(jEndDateOperator02);
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg02_selectedStartDate_value,
                                                         Long.toString(selectedStartDate.getTimeInMillis()));
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg02_selectedEndDate_value,
                                                         Long.toString(selectedEndDate.getTimeInMillis()));
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg02_startOperator_value,
                                                         startOperator.toString());
                                    recordFilterProperty(uniqueFilterTabName,
                                                         FilterPropertyKey.leg02_endOperator_value,
                                                         endOperator.toString());
                                }

                                criteria = new ViewerFilterByPeriods(selectedStartDate,
                                                                     selectedEndDate,
                                                                     startOperator,
                                                                     endOperator);

                                criteria.setFilterCriteria(ViewerFilterCriteriaType.Period);
                            }
                            //tabNameString = convertTabNameStringForDates(selectedStartDate, selectedEndDate, legIndex);
                            break;
            case Strikes:   criteria = new ViewerFilterByStrikes(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Strikes);
                            break;

        }

        if (criteria != null){
            criteria.setFilterLegIndex(legIndex);
        }

        return criteria;
    }

    private ViewerPeriodsOperator getSelectedViewerSearchByOperatorTerms(JComboBox box){
        String selectedStringValue = CalendarGlobal.getSelectedStringValue(box);

        if (selectedStringValue.equalsIgnoreCase("<")){
            return ViewerPeriodsOperator.LessThan;
        }

        if (selectedStringValue.equalsIgnoreCase("<=")){
            return ViewerPeriodsOperator.LessThanEqualTo;
        }

        if (selectedStringValue.equalsIgnoreCase("=")){
            return ViewerPeriodsOperator.EqualTo;
        }

        if (selectedStringValue.equalsIgnoreCase(">")){
            return ViewerPeriodsOperator.GreaterThan;
        }

        if (selectedStringValue.equalsIgnoreCase(">=")){
            return ViewerPeriodsOperator.GreaterThanEqualTo;
        }

        return null;
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
        setLocation(SwingGlobal.getScreenCenterPoint(this));
        setVisible(true);
    }

    @Override
    public void hideDialog() {
        if (SwingUtilities.isEventDispatchThread()){
            setVisible(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setVisible(false);
                }
            });
        }
    }

    private void jCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelActionPerformed
        setVisible(false);
}//GEN-LAST:event_jCancelActionPerformed

    private void jNoBrokerIncludedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jNoBrokerIncludedItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
 //jBrokerNames.setSelectedIndex(0);
            jInTraderNames.setSelectedIndex(0);
        }
    }//GEN-LAST:event_jNoBrokerIncludedItemStateChanged

    private void jLeg2IncludedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jLeg2IncludedItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            enableLeg2Zone();
        }else if (evt.getStateChange() == ItemEvent.DESELECTED){
            disableLeg2Zone();
        }
    }//GEN-LAST:event_jLeg2IncludedItemStateChanged

    private void enableLeg2Zone(){
        jStructures02.setEnabled(true);
        jLocations02.setEnabled(true);
        jStartMonth02.setEnabled(true);
        jStartYear02.setEnabled(true);
        jEndMonth02.setEnabled(true);
        jEndYear02.setEnabled(true);
        jStartDateOperator02.setEnabled(true);
        jEndDateOperator02.setEnabled(true);
        jStrike1Leg02.setEnabled(true);
        jStrike2Leg02.setEnabled(true);
        jStrike3Leg02.setEnabled(true);
        jStrike4Leg02.setEnabled(true);
        jStrike1Leg02.setEditable(true);
        jStrike2Leg02.setEditable(true);
        jStrike3Leg02.setEditable(true);
        jStrike4Leg02.setEditable(true);
    }

    private void disableLeg2Zone(){
        jStructures02.setSelectedIndex(0);
        jLocations02.setSelectedIndex(0);
        jStartMonth02.setSelectedIndex(0);
        jStartYear02.setSelectedIndex(0);
        jEndMonth02.setSelectedIndex(0);
        jEndYear02.setSelectedIndex(0);
        jStartDateOperator02.setSelectedIndex(0);
        jEndDateOperator02.setSelectedIndex(0);
        jStrike1Leg02.setEditable(false);
        jStrike2Leg02.setEditable(false);
        jStrike3Leg02.setEditable(false);
        jStrike4Leg02.setEditable(false);
        jStrike1Leg02.setText("");
        jStrike2Leg02.setText("");
        jStrike3Leg02.setText("");
        jStrike4Leg02.setText("");
        jStructures02.setEnabled(false);
        jLocations02.setEnabled(false);
        jStartMonth02.setEnabled(false);
        jStartYear02.setEnabled(false);
        jEndMonth02.setEnabled(false);
        jEndYear02.setEnabled(false);
        jStartDateOperator02.setEnabled(false);
        jEndDateOperator02.setEnabled(false);
        jStrike1Leg02.setEnabled(false);
        jStrike2Leg02.setEnabled(false);
        jStrike3Leg02.setEnabled(false);
        jStrike4Leg02.setEnabled(false);
        jStrike1Leg02.setEditable(false);
        jStrike2Leg02.setEditable(false);
        jStrike3Leg02.setEditable(false);
        jStrike4Leg02.setEditable(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jCancel;
    private javax.swing.JComboBox jEndDateOperator01;
    private javax.swing.JComboBox jEndDateOperator02;
    private javax.swing.JComboBox jEndMonth01;
    private javax.swing.JComboBox jEndMonth02;
    private javax.swing.JComboBox jEndYear01;
    private javax.swing.JComboBox jEndYear02;
    private javax.swing.JList jExTraderGroups;
    private javax.swing.JList jExTraderNames;
    private javax.swing.JButton jFilter;
    private javax.swing.JList jInTraderGroups;
    private javax.swing.JList jInTraderNames;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jLeg01;
    private javax.swing.JPanel jLeg02;
    private javax.swing.JCheckBox jLeg2Included;
    private javax.swing.JComboBox jLocations01;
    private javax.swing.JComboBox jLocations02;
    private javax.swing.JCheckBox jNoBrokerIncluded;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JComboBox jStartDateOperator01;
    private javax.swing.JComboBox jStartDateOperator02;
    private javax.swing.JComboBox jStartMonth01;
    private javax.swing.JComboBox jStartMonth02;
    private javax.swing.JComboBox jStartYear01;
    private javax.swing.JComboBox jStartYear02;
    private javax.swing.JTextField jStrike1Leg01;
    private javax.swing.JTextField jStrike1Leg02;
    private javax.swing.JTextField jStrike2Leg01;
    private javax.swing.JTextField jStrike2Leg02;
    private javax.swing.JTextField jStrike3Leg01;
    private javax.swing.JTextField jStrike3Leg02;
    private javax.swing.JTextField jStrike4Leg01;
    private javax.swing.JTextField jStrike4Leg02;
    private javax.swing.JComboBox jStructures01;
    private javax.swing.JComboBox jStructures02;
    private javax.swing.JTextField tabNameField;
    // End of variables declaration//GEN-END:variables

    class BuddyWrapper {
        private IGatewayConnectorBuddy buddy;
        
        public BuddyWrapper(IGatewayConnectorBuddy buddy){
            this.buddy=buddy;
        }
        
        @Override
        public String toString(){
            return buddy.getIMServerTypeString()+" "+buddy.getNickname();
        }

        /**
         * @return the buddy
         */
        public IGatewayConnectorBuddy getBuddy() {
            return buddy;
        }     
        
    }
    
    class BuddyListRenderer extends DefaultListCellRenderer {

        Color originalLabelForeground;
        BuddyListRenderer() {
            originalLabelForeground = this.getBackground();
        }
        
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            BuddyWrapper  buddyWrapper = (BuddyWrapper) value;
            IGatewayConnectorBuddy buddy=buddyWrapper.buddy;
            setIcon(viewer.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getBuddyImageIcon(buddy));
            setText(buddy.getNickname());
            setFont(SwingGlobal.getLabelFont());
            
            if(isSelected){
                setBackground(Color.YELLOW);
            }else{
                setBackground(originalLabelForeground);
            }
            return this;
        }
    }
    
     class GroupListRenderer extends DefaultListCellRenderer {

        Color originalLabelForeground;
        GroupListRenderer() {
            originalLabelForeground = this.getBackground();
        }
        
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            GroupWrapper  groupWrapper = (GroupWrapper) value;
            IGatewayConnectorGroup group=groupWrapper.group;
            setIcon(viewer.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getBuddyImageIcon(group.getLoginUser()));
            setText("("+group.getLoginUser().getNickname()+") "+group.getGroupName());
            setFont(SwingGlobal.getLabelFont());
            
            if(isSelected){
                setBackground(Color.ORANGE);
            }else{
                setBackground(originalLabelForeground);
            }
            return this;
        }
    }
    
    class GroupWrapper {
        private IGatewayConnectorGroup group;
        
        
        public GroupWrapper(IGatewayConnectorGroup group){
            this.group=group;
        }
        
        @Override
        public String toString(){
            return group.getLoginUser().getIMServerTypeString()+" "+group.getLoginUser().getNickname()+": "+group.getGroupName();
        }
    }
        
}
