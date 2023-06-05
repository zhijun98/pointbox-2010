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

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyKey;
import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;
import com.eclipsemarkets.pbc.face.viewer.IViewerTablePanel;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Zhijun Zhang
 */
class ViewerFilterDialog extends javax.swing.JDialog implements IPbconsoleDialog{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(ViewerFilterDialog.class.getName());
    }
    
    private static final String EMPTY_TYPE = "Category...";
    private static final String CLASS_TYPE = "Class";
    private static final TreeSet<String> classValueSet = new TreeSet<String>();
    private static final String GROUP_TYPE = "Group";
    private static final TreeSet<String> groupValueSet = new TreeSet<String>();
    private static final String CODE_TYPE = "Code";
    private static final TreeSet<String> codeValueSet = new TreeSet<String>();
    
    private static final long serialVersionUID = 1L;
    
    private final IPbcViewer viewer;

    private static int defaultFilterId = 1;
    
    private boolean isCalendarInitialized = false;
        
    private boolean isExistingFilterTab = false;
    
    private IPbcRuntime runtime;

    private String oldUniqueFilterTabName;
    
    private boolean forNewFilter;

    //private ViewerSearchDialog searchDialog;

    /** Creates new form ViewerFilterDialog
     * @param system
     * @param parent
     * @param modal
     */
    ViewerFilterDialog(IPbcViewer viewer, boolean modal, boolean forNewFilter) {
        super(viewer.getPointBoxFrame(), modal);
        initComponents();

        this.forNewFilter = forNewFilter;
        this.viewer = viewer;
        runtime = viewer.getKernel().getPointBoxConsoleRuntime();
        
        initializeCgcControls();
        
        jStartDateOperator01.setSelectedItem(">=");
        jEndDateOperator01.setSelectedItem("<=");
        jStartDateOperator02.setSelectedIndex(0);
        jEndDateOperator02.setSelectedIndex(0);

        initializeComponentYears();

        disableLeg2Zone();

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        if (forNewFilter){
            setTitle("New Filter");
        }else{
            setTitle("Edit Filter");
        }
        
        setResizable(false);
        
        if (!forNewFilter){
            populatePersistentCriteria();
        }
    }

    private void initializeCgcControls(){
        if (SwingUtilities.isEventDispatchThread()){
            initializeCgcControlsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    initializeCgcControlsHelper();
                }
            });
        }
    }
    
    private void initializeCgcControlsHelper(){
        HashMap<String, PbcPricingModel> aPbcPricingModelMap = runtime.getPbcPricingModelMap();
        if (aPbcPricingModelMap != null){
            Collection<PbcPricingModel> aPbcPricingModelCollection = aPbcPricingModelMap.values();
            for (PbcPricingModel aPbcPricingModel : aPbcPricingModelCollection){
                classValueSet.add(aPbcPricingModel.getSqClass());
                groupValueSet.add(aPbcPricingModel.getSqGroup());
                codeValueSet.add(aPbcPricingModel.getSqCode());
            }
        }
        DefaultComboBoxModel typeModel = new DefaultComboBoxModel();
        typeModel.addElement(EMPTY_TYPE);
        typeModel.addElement(CLASS_TYPE);
        typeModel.addElement(GROUP_TYPE);
        typeModel.addElement(CODE_TYPE);
        jCgcTypeList.setModel(typeModel);
        jCgcTypeList.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    if (CLASS_TYPE.equalsIgnoreCase(e.getItem().toString())){
                        setupCgcValueList(classValueSet);
                    }else if (GROUP_TYPE.equalsIgnoreCase(e.getItem().toString())){
                        setupCgcValueList(groupValueSet);
                    }else if (CODE_TYPE.equalsIgnoreCase(e.getItem().toString())){
                        setupCgcValueList(codeValueSet);
                    }else{
                        setupCgcValueList(null);
                    }
                }
            }

            private void setupCgcValueList(TreeSet<String> valueSet) {
                DefaultComboBoxModel valueModel = new DefaultComboBoxModel();
                if (valueSet != null){
                    for (String value : valueSet){
                        valueModel.addElement(value);
                    }
                }
                jCgcValueList.setModel(valueModel);
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
        }
    }

    private void initializeComponentYears(){
        GregorianCalendar today = new GregorianCalendar();
        int year = today.get(Calendar.YEAR);
        
        jStartMonth01.setSelectedIndex(-1);
        jStartMonth02.setSelectedIndex(-1);
        jEndMonth01.setSelectedIndex(-1);
        jEndMonth02.setSelectedIndex(-1);
        
        initializeYearControl(jStartYear01, year);
        initializeYearControl(jEndYear01, year);
        initializeYearControl(jStartYear02, year);
        initializeYearControl(jEndYear02, year);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel8 = new javax.swing.JPanel();
        jLeg01 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jEndDateOperator01 = new javax.swing.JComboBox();
        jEndMonth01 = new javax.swing.JComboBox();
        jEndYear01 = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jStartDateOperator01 = new javax.swing.JComboBox();
        jStartMonth01 = new javax.swing.JComboBox();
        jStartYear01 = new javax.swing.JComboBox();
        jLeg02 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jEndDateOperator02 = new javax.swing.JComboBox();
        jEndMonth02 = new javax.swing.JComboBox();
        jEndYear02 = new javax.swing.JComboBox();
        jPanel7 = new javax.swing.JPanel();
        jStartDateOperator02 = new javax.swing.JComboBox();
        jStartMonth02 = new javax.swing.JComboBox();
        jStartYear02 = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jFilter = new javax.swing.JButton();
        jCancel = new javax.swing.JButton();
        jCgcTypeList = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jCgcValueList = new javax.swing.JList();
        jLeg2Included = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        tabNameField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setIconImage(null);

        jPanel8.setName("jPanel8"); // NOI18N
        jPanel8.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        jLeg01.setBorder(javax.swing.BorderFactory.createTitledBorder("Leg 1"));
        jLeg01.setMaximumSize(new java.awt.Dimension(203, 312));
        jLeg01.setMinimumSize(new java.awt.Dimension(203, 312));
        jLeg01.setName("jLeg01"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("End Date"));
        jPanel2.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel2.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        jEndDateOperator01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", "<=", "=", ">", ">=" }));
        jEndDateOperator01.setMaximumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator01.setMinimumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator01.setName("jEndDateOperator01"); // NOI18N
        jEndDateOperator01.setPreferredSize(new java.awt.Dimension(42, 20));
        jPanel2.add(jEndDateOperator01);

        jEndMonth01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jEndMonth01.setMaximumSize(new java.awt.Dimension(37, 20));
        jEndMonth01.setMinimumSize(new java.awt.Dimension(37, 20));
        jEndMonth01.setName("jEndMonth01"); // NOI18N
        jEndMonth01.setPreferredSize(new java.awt.Dimension(37, 20));
        jPanel2.add(jEndMonth01);

        jEndYear01.setMaximumSize(new java.awt.Dimension(53, 20));
        jEndYear01.setMinimumSize(new java.awt.Dimension(53, 20));
        jEndYear01.setName("jEndYear01"); // NOI18N
        jEndYear01.setPreferredSize(new java.awt.Dimension(53, 20));
        jPanel2.add(jEndYear01);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Start Date"));
        jPanel3.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel3.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        jStartDateOperator01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", "<=", "=", ">", ">=" }));
        jStartDateOperator01.setMaximumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator01.setMinimumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator01.setName("jStartDateOperator01"); // NOI18N
        jStartDateOperator01.setPreferredSize(new java.awt.Dimension(42, 20));
        jPanel3.add(jStartDateOperator01);

        jStartMonth01.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jStartMonth01.setMaximumSize(new java.awt.Dimension(37, 20));
        jStartMonth01.setMinimumSize(new java.awt.Dimension(37, 20));
        jStartMonth01.setName("jStartMonth01"); // NOI18N
        jStartMonth01.setPreferredSize(new java.awt.Dimension(37, 20));
        jStartMonth01.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartMonth01ActionPerformed(evt);
            }
        });
        jPanel3.add(jStartMonth01);

        jStartYear01.setMaximumSize(new java.awt.Dimension(53, 20));
        jStartYear01.setMinimumSize(new java.awt.Dimension(53, 20));
        jStartYear01.setName("jStartYear01"); // NOI18N
        jStartYear01.setPreferredSize(new java.awt.Dimension(53, 20));
        jStartYear01.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartYear01ActionPerformed(evt);
            }
        });
        jPanel3.add(jStartYear01);

        javax.swing.GroupLayout jLeg01Layout = new javax.swing.GroupLayout(jLeg01);
        jLeg01.setLayout(jLeg01Layout);
        jLeg01Layout.setHorizontalGroup(
            jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLeg01Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jLeg01Layout.setVerticalGroup(
            jLeg01Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLeg01Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(181, Short.MAX_VALUE))
        );

        jPanel8.add(jLeg01);

        jLeg02.setBorder(javax.swing.BorderFactory.createTitledBorder("Leg 2"));
        jLeg02.setMaximumSize(new java.awt.Dimension(203, 312));
        jLeg02.setMinimumSize(new java.awt.Dimension(203, 312));
        jLeg02.setName("jLeg02"); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("End Date"));
        jPanel6.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel6.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        jEndDateOperator02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "<", "<=", "=", ">", ">=" }));
        jEndDateOperator02.setMaximumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator02.setMinimumSize(new java.awt.Dimension(42, 20));
        jEndDateOperator02.setName("jEndDateOperator02"); // NOI18N
        jEndDateOperator02.setPreferredSize(new java.awt.Dimension(42, 20));
        jPanel6.add(jEndDateOperator02);

        jEndMonth02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jEndMonth02.setMaximumSize(new java.awt.Dimension(37, 20));
        jEndMonth02.setMinimumSize(new java.awt.Dimension(37, 20));
        jEndMonth02.setName("jEndMonth02"); // NOI18N
        jEndMonth02.setPreferredSize(new java.awt.Dimension(37, 20));
        jPanel6.add(jEndMonth02);

        jEndYear02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        jEndYear02.setMaximumSize(new java.awt.Dimension(53, 20));
        jEndYear02.setMinimumSize(new java.awt.Dimension(53, 20));
        jEndYear02.setName("jEndYear02"); // NOI18N
        jEndYear02.setPreferredSize(new java.awt.Dimension(53, 20));
        jPanel6.add(jEndYear02);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Start Date"));
        jPanel7.setMaximumSize(new java.awt.Dimension(171, 57));
        jPanel7.setMinimumSize(new java.awt.Dimension(171, 57));
        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        jStartDateOperator02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "<", "<=", "=", ">", ">=" }));
        jStartDateOperator02.setMaximumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator02.setMinimumSize(new java.awt.Dimension(42, 20));
        jStartDateOperator02.setName("jStartDateOperator02"); // NOI18N
        jStartDateOperator02.setPreferredSize(new java.awt.Dimension(42, 20));
        jPanel7.add(jStartDateOperator02);

        jStartMonth02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" }));
        jStartMonth02.setMaximumSize(new java.awt.Dimension(37, 20));
        jStartMonth02.setMinimumSize(new java.awt.Dimension(37, 20));
        jStartMonth02.setName("jStartMonth02"); // NOI18N
        jStartMonth02.setPreferredSize(new java.awt.Dimension(37, 20));
        jStartMonth02.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartMonth02ActionPerformed(evt);
            }
        });
        jPanel7.add(jStartMonth02);

        jStartYear02.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        jStartYear02.setMaximumSize(new java.awt.Dimension(53, 20));
        jStartYear02.setMinimumSize(new java.awt.Dimension(53, 20));
        jStartYear02.setName("jStartYear02"); // NOI18N
        jStartYear02.setPreferredSize(new java.awt.Dimension(53, 20));
        jStartYear02.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartYear02ActionPerformed(evt);
            }
        });
        jPanel7.add(jStartYear02);

        javax.swing.GroupLayout jLeg02Layout = new javax.swing.GroupLayout(jLeg02);
        jLeg02.setLayout(jLeg02Layout);
        jLeg02Layout.setHorizontalGroup(
            jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLeg02Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jLeg02Layout.setVerticalGroup(
            jLeg02Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLeg02Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(181, Short.MAX_VALUE))
        );

        jPanel8.add(jLeg02);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        jFilter.setText("Save");
        jFilter.setName("jFilter"); // NOI18N
        jFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFilterActionPerformed(evt);
            }
        });
        jPanel1.add(jFilter);

        jCancel.setText("Cancel");
        jCancel.setName("jCancel"); // NOI18N
        jCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jCancel);

        jCgcTypeList.setName("jCgcTypeList"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jCgcValueList.setName("jCgcValueList"); // NOI18N
        jScrollPane1.setViewportView(jCgcValueList);

        jLeg2Included.setText("Leg 2 Included");
        jLeg2Included.setName("jLeg2Included"); // NOI18N
        jLeg2Included.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jLeg2IncludedItemStateChanged(evt);
            }
        });

        jLabel1.setText("Filter Tab Name:");
        jLabel1.setName("jLabel1"); // NOI18N

        tabNameField.setName("tabNameField"); // NOI18N
        tabNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabNameFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCgcTypeList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tabNameField)
                            .addComponent(jLeg2Included, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(140, 140, 140)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jCgcTypeList, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1)
                        .addGap(18, 18, 18)
                        .addComponent(jLeg2Included))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFilterActionPerformed
        
        final String newUniqueFilterTabName = tabNameField.getText();
        if (DataGlobal.isEmptyNullString(newUniqueFilterTabName)){
            JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "You have to give a filter tab name.");
            return;
        }else{
            if (forNewFilter){
                if (viewer.hasFilterTab(newUniqueFilterTabName)){
                    JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "The filter tab name has been used. Please give a different tab name");
                    return;
                }
            }else{
                if (!newUniqueFilterTabName.equalsIgnoreCase(oldUniqueFilterTabName)){
                    if (viewer.hasFilterTab(newUniqueFilterTabName)){
                        JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "The filter tab name has been used. Please give a different tab name");
                        return;
                    }
                }
            }
        }
        
        final ArrayList<IViewerFilterCriteria> criteriaList = new ArrayList<IViewerFilterCriteria>();

        (new SwingWorker<String, Void>(){
            @Override
            protected String doInBackground() throws Exception {
                String uniqueFilterTabName = tabNameField.getText();
                if(uniqueFilterTabName == null || uniqueFilterTabName.trim().isEmpty()){
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
                    uniqueFilterTabName = uniqueFilterTabName.trim();
                }
                
                if (uniqueFilterTabName != null){
                    if(!viewer.hasFilterTab(uniqueFilterTabName)){
                        //new filter
                        runtime.addPbcViewerSettings(uniqueFilterTabName);
                    }
                    loadViewerFilterCriteriaFromGuiControls(uniqueFilterTabName, criteriaList);
                }
                return uniqueFilterTabName;
            }
            
            @Override
            protected void done() {
                try {
                    String uniqueFilterTabName = get();
                    if (uniqueFilterTabName == null){
                        JOptionPane.showMessageDialog(ViewerFilterDialog.this, "Too many filters were created.");
                        setVisible(true);
                    }else if(uniqueFilterTabName.isEmpty()){
                        JOptionPane.showMessageDialog(ViewerFilterDialog.this, "This filter name has already existed! Please change another one.");
                        tabNameField.setText("");
                        setVisible(true);                        
                    }else{
                        viewer.renameFilterResultTab(oldUniqueFilterTabName, uniqueFilterTabName, criteriaList);
                        setVisible(false);
                        //create a new tab viewer for this filter list
                        viewer.constructFilterTab(uniqueFilterTabName, criteriaList);
                    }
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(ViewerFilterDialog.this, ex.getMessage());
                    setVisible(true);
                } catch (ExecutionException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(ViewerFilterDialog.this, ex.getMessage());
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
        runtime.addPbcFilterPropertySettings(uniqueFilterTabName, aPbcFilterPropertySettings);

    }

    private ArrayList<String> processCgcSelectedValues(Object[] selectedValues) throws Exception{
        if ((selectedValues != null) && (selectedValues.length != 0)){
            ArrayList<String> result = new ArrayList<String>();
            for (Object selectedValue : selectedValues){
                result.add(selectedValue.toString());
            }
            return result;
        }else{
            throw new Exception("No values are selected.");
        }
    }
    
    private void loadViewerFilterCriteriaFromGuiControls(String uniqueFilterTabName, ArrayList<IViewerFilterCriteria> criteriaList){
        if (uniqueFilterTabName == null){
            return;
        }
        
        /**
         * Class/Group/Code
         */
        Object cgcObj = jCgcTypeList.getSelectedItem();
        if (cgcObj != null){
            if (CLASS_TYPE.equalsIgnoreCase(cgcObj.toString())){
                try {
                    criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, 
                                                             ViewerFilterCriteriaType.QuoteClass, 
                                                             processCgcSelectedValues(jCgcValueList.getSelectedValues()),
                                                             0));
                } catch (Exception ex) {
                    //Logger.getLogger(ViewerFilterDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else if (GROUP_TYPE.equalsIgnoreCase(cgcObj.toString())){
                try {
                    criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, 
                                                             ViewerFilterCriteriaType.QuoteGroup, 
                                                             processCgcSelectedValues(jCgcValueList.getSelectedValues()),
                                                             0));
                } catch (Exception ex) {
                    //Logger.getLogger(ViewerFilterDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else if (CODE_TYPE.equalsIgnoreCase(cgcObj.toString())){
                try {
                    criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, 
                                                             ViewerFilterCriteriaType.QuoteCode, 
                                                             processCgcSelectedValues(jCgcValueList.getSelectedValues()),
                                                             0));
                } catch (Exception ex) {
                    //Logger.getLogger(ViewerFilterDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        /**
         * create time period search criteria list
         */
        criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Period, null, 0));

        if (jLeg2Included.isSelected()){

            recordFilterProperty(uniqueFilterTabName,
                                 FilterPropertyKey.jLeg2Included,
                                 "true");

            /*create time period search criteria list*/
            criteriaList.add(getViewerFilterCriteria(uniqueFilterTabName, ViewerFilterCriteriaType.Period, null, 1));
        }
    }

    private IViewerFilterCriteria getViewerFilterCriteria(String uniqueFilterTabName,
                                                          ViewerFilterCriteriaType terms,
                                                          ArrayList<String> items,
                                                          int legIndex)
    {
        IViewerFilterCriteria criteria = null;
        switch(terms){
            case QuoteClass:
                criteria = new ViewerFilterByClass(items);
                criteria.setFilterCriteria(ViewerFilterCriteriaType.QuoteClass);
                recordFilterProperty(uniqueFilterTabName,
                                     FilterPropertyKey.jClass_SelectedName,
                                     FilterPropertyKey.generateFilterValueForPersistency(items));
                break;
            case QuoteGroup:
                criteria = new ViewerFilterByGroup(items);
                criteria.setFilterCriteria(ViewerFilterCriteriaType.QuoteGroup);
                recordFilterProperty(uniqueFilterTabName,
                                     FilterPropertyKey.jGroup_SelectedName,
                                     FilterPropertyKey.generateFilterValueForPersistency(items));
                break;
            case QuoteCode:
                criteria = new ViewerFilterByCode(items);
                criteria.setFilterCriteria(ViewerFilterCriteriaType.QuoteCode);
                recordFilterProperty(uniqueFilterTabName,
                                     FilterPropertyKey.jCode_SelectedName,
                                     FilterPropertyKey.generateFilterValueForPersistency(items));
                break;
            case Strategy:  criteria = new ViewerFilterByStrategies(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Strategy);
                            break;
            case Brokers:   criteria = new ViewerFilterByBrokers(items);
                            criteria.setFilterCriteria(ViewerFilterCriteriaType.Brokers);
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

    private void jLeg2IncludedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jLeg2IncludedItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            enableLeg2Zone();
        }else if (evt.getStateChange() == ItemEvent.DESELECTED){
            disableLeg2Zone();
        }
    }//GEN-LAST:event_jLeg2IncludedItemStateChanged

    private void tabNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tabNameFieldActionPerformed

    private void jStartMonth01ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartMonth01ActionPerformed
        updateCalendarSelectors(jStartMonth01, jStartYear01, jEndMonth01, jEndYear01);
    }//GEN-LAST:event_jStartMonth01ActionPerformed

    private void jStartYear01ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartYear01ActionPerformed
        updateCalendarSelectors(jStartMonth01, jStartYear01, jEndMonth01, jEndYear01);
    }//GEN-LAST:event_jStartYear01ActionPerformed

    private void jStartMonth02ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartMonth02ActionPerformed
        updateCalendarSelectors(jStartMonth02, jStartYear02, jEndMonth02, jEndYear02);
    }//GEN-LAST:event_jStartMonth02ActionPerformed

    private void jStartYear02ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartYear02ActionPerformed
        updateCalendarSelectors(jStartMonth02, jStartYear02, jEndMonth02, jEndYear02);
    }//GEN-LAST:event_jStartYear02ActionPerformed

    private void enableLeg2Zone(){
        jStartMonth02.setEnabled(true);
        jStartYear02.setEnabled(true);
        jEndMonth02.setEnabled(true);
        jEndYear02.setEnabled(true);
        jStartDateOperator02.setEnabled(true);
        jEndDateOperator02.setEnabled(true);
    }

    private void disableLeg2Zone(){
        jStartMonth02.setSelectedIndex(0);
        jStartYear02.setSelectedIndex(0);
        jEndMonth02.setSelectedIndex(0);
        jEndYear02.setSelectedIndex(0);
        jStartDateOperator02.setSelectedIndex(0);
        jEndDateOperator02.setSelectedIndex(0);
        jStartMonth02.setEnabled(false);
        jStartYear02.setEnabled(false);
        jEndMonth02.setEnabled(false);
        jEndYear02.setEnabled(false);
        jStartDateOperator02.setEnabled(false);
        jEndDateOperator02.setEnabled(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jCancel;
    private javax.swing.JComboBox jCgcTypeList;
    private javax.swing.JList jCgcValueList;
    private javax.swing.JComboBox jEndDateOperator01;
    private javax.swing.JComboBox jEndDateOperator02;
    private javax.swing.JComboBox jEndMonth01;
    private javax.swing.JComboBox jEndMonth02;
    private javax.swing.JComboBox jEndYear01;
    private javax.swing.JComboBox jEndYear02;
    private javax.swing.JButton jFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jLeg01;
    private javax.swing.JPanel jLeg02;
    private javax.swing.JCheckBox jLeg2Included;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox jStartDateOperator01;
    private javax.swing.JComboBox jStartDateOperator02;
    private javax.swing.JComboBox jStartMonth01;
    private javax.swing.JComboBox jStartMonth02;
    private javax.swing.JComboBox jStartYear01;
    private javax.swing.JComboBox jStartYear02;
    private javax.swing.JTextField tabNameField;
    // End of variables declaration//GEN-END:variables

    private void updateCalendarSelectors(final JComboBox jStartMonth, final JComboBox jStartYear, final JComboBox jEndMonth, final JComboBox jEndYear) {
        if (SwingUtilities.isEventDispatchThread()){
            updateCalendarSelectorsHelper(jStartMonth, jStartYear, jEndMonth, jEndYear);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateCalendarSelectorsHelper(jStartMonth, jStartYear, jEndMonth, jEndYear);
                }
            });
        }
    }

    private void updateCalendarSelectorsHelper(final JComboBox jStartMonth, final JComboBox jStartYear, final JComboBox jEndMonth, final JComboBox jEndYear){
        if (!isCalendarInitialized){
            return;
        }
        GregorianCalendar selectedStartDate = CalendarGlobal.getSelectedGregorianCalendar(
                                CalendarGlobal.getSelectedStringValue(jStartMonth),
                                CalendarGlobal.getSelectedStringValue(jStartYear),
                                1);
        if (selectedStartDate == null){
            return;
        }
        GregorianCalendar selectedEndDate = CalendarGlobal.getSelectedGregorianCalendar(
                                CalendarGlobal.getSelectedStringValue(jEndMonth),
                                CalendarGlobal.getSelectedStringValue(jEndYear),
                                1);
        if ((selectedEndDate == null) || (selectedEndDate.before(selectedStartDate))){
            jEndMonth.setSelectedIndex(jStartMonth.getSelectedIndex());
            jEndYear.setSelectedIndex(jStartYear.getSelectedIndex());
        }
    }
    
    private void populatePersistentCriteria() {
        if (viewer == null){
            return;
        }
        
        IViewerTablePanel currentViewerTablePanel = viewer.getCurrentViewerTablePanel();
        if (currentViewerTablePanel == null){
            return;
        }
        
        oldUniqueFilterTabName = currentViewerTablePanel.getViewerTableName();
        
        /*create quote class name criteria list*/
        ArrayList<String> quoteClasses = FilterPropertyKey.retrieveFilterValueArrayFromPersistency(runtime.getViewerFilterStringValue(
                                oldUniqueFilterTabName, FilterPropertyKey.jClass_SelectedName));
        if (!quoteClasses.isEmpty()){
            jCgcTypeList.setSelectedItem(CLASS_TYPE); 
            setListSelection(jCgcValueList, quoteClasses);
        }
        
        /*create quote group name criteria list*/
        ArrayList<String> quoteGroups = FilterPropertyKey.retrieveFilterValueArrayFromPersistency(runtime.getViewerFilterStringValue(
                                oldUniqueFilterTabName, FilterPropertyKey.jGroup_SelectedName));
        if (!quoteGroups.isEmpty()){
            jCgcTypeList.setSelectedItem(GROUP_TYPE); 
            setListSelection(jCgcValueList, quoteGroups);
        }
        
        /*create quote code name criteria list*/
        ArrayList<String> quoteCodes = FilterPropertyKey.retrieveFilterValueArrayFromPersistency(runtime.getViewerFilterStringValue(
                                oldUniqueFilterTabName, FilterPropertyKey.jCode_SelectedName));
        if (!quoteCodes.isEmpty()){
            jCgcTypeList.setSelectedItem(CODE_TYPE); 
            setListSelection(jCgcValueList, quoteCodes);
        }
        /**
         * Periods
         */
        isCalendarInitialized = false;
        setPeriodsSelection(oldUniqueFilterTabName, 0);
        setPeriodsSelection(oldUniqueFilterTabName, 1);
        isCalendarInitialized = true;
        
        if (isExistingFilterTab){
            tabNameField.setText(oldUniqueFilterTabName);
        }
    }
    
    private void setPeriodsSelection(String uniqueFilterTabName, int legIndex){
        GregorianCalendar selectedStartDate, selectedEndDate;
        ViewerPeriodsOperator startOperator, endOperator;

        long timeValue;
        
        if (legIndex == 0){
            timeValue = runtime.getViewerFilterLongValue(
                                            uniqueFilterTabName,
                                            FilterPropertyKey.leg01_selectedStartDate_value);
            if (timeValue <= 0){
                return;
            }
            selectedStartDate = new GregorianCalendar();
            selectedStartDate.setTimeInMillis(timeValue);
            timeValue = runtime.getViewerFilterLongValue(
                                            uniqueFilterTabName,
                                            FilterPropertyKey.leg01_selectedEndDate_value);
            if (timeValue <= 0){
                return;
            }
            selectedEndDate = new GregorianCalendar();
            selectedEndDate.setTimeInMillis(timeValue);
            startOperator = ViewerPeriodsOperator.convertToType(
                                runtime.getViewerFilterStringValue(
                                    uniqueFilterTabName, FilterPropertyKey.leg01_startOperator_value));
            if (startOperator == null){
                return;
            }
            endOperator = ViewerPeriodsOperator.convertToType(
                                runtime.getViewerFilterStringValue(
                                    uniqueFilterTabName, FilterPropertyKey.leg01_endOperator_value));
            if (endOperator == null){
                return;
            }
            setPeriodsSelectionHelper(selectedStartDate, selectedEndDate, startOperator, endOperator, 0);
        }else{
            timeValue = runtime.getViewerFilterLongValue(
                                            uniqueFilterTabName,
                                            FilterPropertyKey.leg02_selectedStartDate_value);
            if (timeValue <= 0){
                return;
            }
            selectedStartDate = new GregorianCalendar();
            selectedStartDate.setTimeInMillis(timeValue);
            timeValue = runtime.getViewerFilterLongValue(
                                            uniqueFilterTabName,
                                            FilterPropertyKey.leg02_selectedEndDate_value);
            if (timeValue <= 0){
                return;
            }
            selectedEndDate = new GregorianCalendar();
            selectedEndDate.setTimeInMillis(timeValue);
            startOperator = ViewerPeriodsOperator.convertToType(
                                runtime.getViewerFilterStringValue(
                                    uniqueFilterTabName, FilterPropertyKey.leg02_startOperator_value));
            if (startOperator == null){
                return;
            }
            endOperator = ViewerPeriodsOperator.convertToType(
                                runtime.getViewerFilterStringValue(
                                    uniqueFilterTabName, FilterPropertyKey.leg02_endOperator_value));
            if (endOperator == null){
                return;
            }
            setPeriodsSelectionHelper(selectedStartDate, selectedEndDate, startOperator, endOperator, 1);
        }
        isExistingFilterTab = true;
    }

    private void setPeriodsSelectionHelper(GregorianCalendar selectedStartDate, GregorianCalendar selectedEndDate, ViewerPeriodsOperator startOperator, ViewerPeriodsOperator endOperator, int legIndex) {
        if (legIndex == 0){
            setSelectedCalendarOperatorControl(jStartDateOperator01, startOperator, ">");
            setSelectedCalendarOperatorControl(jEndDateOperator01, endOperator, "<");
            setSelectedMonthControl(jStartMonth01, selectedStartDate.get(Calendar.MONTH));
            setSelectedYearControl(jStartYear01, selectedStartDate.get(Calendar.YEAR));
            setSelectedMonthControl(jEndMonth01, selectedEndDate.get(Calendar.MONTH));
            setSelectedYearControl(jEndYear01, selectedEndDate.get(Calendar.YEAR));
            
        
        }else{
            jLeg2Included.setSelected(true);
            setSelectedCalendarOperatorControl(jStartDateOperator02, startOperator, ">");
            setSelectedCalendarOperatorControl(jEndDateOperator02, endOperator, "<");
            setSelectedMonthControl(jStartMonth02, selectedStartDate.get(Calendar.MONTH));
            setSelectedYearControl(jStartYear02, selectedStartDate.get(Calendar.YEAR));
            setSelectedMonthControl(jEndMonth02, selectedEndDate.get(Calendar.MONTH));
            setSelectedYearControl(jEndYear02, selectedEndDate.get(Calendar.YEAR));
        }
    }
    
    private void setSelectedCalendarOperatorControl(JComboBox operatorList, ViewerPeriodsOperator operator, String defaultOperator){
        switch(operator){
            case EqualTo:
                operatorList.setSelectedItem("=");
                break;
            case GreaterThan:
                operatorList.setSelectedItem(">");
                break;
            case GreaterThanEqualTo:
                operatorList.setSelectedItem(">=");
                break;
            case LessThan:
                operatorList.setSelectedItem("<");
                break;
            case LessThanEqualTo:
                operatorList.setSelectedItem("<=");
                break;
            default:
                operatorList.setSelectedItem(defaultOperator);
        }
    
    }

    private void setListSelection(JList jCgcValueList, ArrayList<String> values) {
        jCgcValueList.clearSelection();
        ListModel model = jCgcValueList.getModel();
        int index;
        for (String value : values){
            index = getIndex(model, value);
            if (index >=0) {
                jCgcValueList.addSelectionInterval(index, index);
            }
        }
        isExistingFilterTab = true;
    }

    private int getIndex(ListModel model, Object value) {
        if (value == null) return -1;
        if (model instanceof DefaultListModel) {
            return ((DefaultListModel) model).indexOf(value);
        }
        for (int i = 0; i < model.getSize(); i++) {
            if (value.equals(model.getElementAt(i))) return i;
        }
        return -1;
    }

    private void setSelectedMonthControl(JComboBox aComboBox, int month) {
        switch (month){
            case 0:
                aComboBox.setSelectedItem("F");
                break;
            case 1:
                aComboBox.setSelectedItem("G");
                break;
            case 2:
                aComboBox.setSelectedItem("H");
                break;
            case 3:
                aComboBox.setSelectedItem("J");
                break;
            case 4:
                aComboBox.setSelectedItem("K");
                break;
            case 5:
                aComboBox.setSelectedItem("M");
                break;
            case 6:
                aComboBox.setSelectedItem("N");
                break;
            case 7:
                aComboBox.setSelectedItem("Q");
                break;
            case 8:
                aComboBox.setSelectedItem("U");
                break;
            case 9:
                aComboBox.setSelectedItem("V");
                break;
            case 10:
                aComboBox.setSelectedItem("X");
                break;
            case 11:
                aComboBox.setSelectedItem("Z");
                break;
            default:
                aComboBox.setSelectedItem("F");
                
        }
    }

    private void setSelectedYearControl(JComboBox aComboBox, int year) {
        aComboBox.setSelectedItem(Integer.toString(year));
    }

    private void initializeYearControl(JComboBox yearControl, int year) {
        for (int i = 0; i < 16; i++){
            yearControl.addItem(Integer.toString(year + i));
        }
        yearControl.setSelectedIndex(-1);
    }

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
            setIcon(runtime.getPbcImageSettings().getBuddyImageIcon(buddy));
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
            setIcon(runtime.getPbcImageSettings().getBuddyImageIcon(group.getLoginUser()));
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
