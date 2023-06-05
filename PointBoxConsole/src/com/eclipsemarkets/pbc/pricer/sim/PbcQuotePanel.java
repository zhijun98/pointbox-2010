/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteCodeWrapper;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.RegexGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.parser.PbcSimGuiParser;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;

/**
 *
 * @author Zhijun Zhang
 */
class PbcQuotePanel extends javax.swing.JPanel implements IPbcSimMessagingListener{

    private PbcQuoteFrame owner;

    private Color defaultBackgroundForPrice = new Color(121,210,255);
    private Color defaultForegroundForPrice = Color.BLACK;
    private Color defaultBackgroundForVol = new Color(204,255,204);
    private Color defaultForegroundForVol = Color.BLACK;
    
    private final SimCodeBasedSelectorAgent aCodeBasedSelectorAgent;
    
    /**
     * Creates new form PbcQuotePanel
     */
    PbcQuotePanel(PbcQuoteFrame owner) {
        initComponents();
        
        jStructurePrice.setText(DataGlobal.formatDoubleWithMinMax(0.0, 
                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        
        jLabel7.setVisible(false);
        jLabel8.setVisible(false);
        jLabel9.setVisible(false);
        
        this.owner = owner;
        
        PointBoxQuoteCode selectedCode = PointBoxConsoleProperties.getSingleton().retrieveSelectedPointBoxQuoteCode(owner.getKernel().getPointBoxLoginUser().getIMUniqueName());
        
        //setupBidAndAskSpinners();
        replaceBidAskSpinnerControls();
        
        aCodeBasedSelectorAgent = new SimCodeBasedSelectorAgent(owner, 
                                                                jClassSelector,
                                                                jGroupSelector,
                                                                jCodeSelector);
        aCodeBasedSelectorAgent.initializeSelectors();
        try{
            if (owner.getKernel().getPointBoxConsoleRuntime().getPbcPricingModelMap().containsKey(selectedCode.name())){
                aCodeBasedSelectorAgent.setSelectedSimCode(selectedCode);
            }
        }catch(Exception ex){}
        
    }
    
    private final JFormattedTextField jBidTextField = new JFormattedTextField(new DefaultFormatter());
    private final JFormattedTextField jAskTextField = new JFormattedTextField(new DefaultFormatter()); 
    private void replaceBidAskSpinnerControls(){
        if (SwingUtilities.isEventDispatchThread()){
            replaceBidAskSpinnerControlsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    replaceBidAskSpinnerControlsHelper();
                }
            });
        }
    
    }
    private void replaceBidAskSpinnerControlsHelper(){
        replaceBidAskSpinnerControlsHelperImpl(jBidSpinner, jBidTextField);
        replaceBidAskSpinnerControlsHelperImpl(jAskSpinner, jAskTextField);
    }
    
    private void replaceBidAskSpinnerControlsHelperImpl(final JSpinner aJSpinner, final JFormattedTextField aJFormattedTextField){
        int index = jBidAskPanel.getComponentZOrder(aJSpinner);
        jBidAskPanel.remove(index);
        jBidAskPanel.add(aJFormattedTextField, index);
        aJFormattedTextField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        aJFormattedTextField.setEnabled(false);
        aJFormattedTextField.setInputVerifier(new SimFormattedValueInputVerifier());
        aJFormattedTextField.addPropertyChangeListener("value", new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double value = SwingGlobal.retrieveDoubleFromTextField(aJFormattedTextField);
                if (value == 0.0) {
    //                    tableModel.inputStrikes.put(0, "");
                    aJFormattedTextField.setValue("");
                } else {
    //                    tableModel.inputStrikes.put(0, value);
                    aJFormattedTextField.setValue(DataGlobal.formatDoubleWithMinMax(value,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                    owner.populateQuoteMessageTextField();
                }   
            }
        });
    }

    private void handleBidAskCheckEvent(ItemEvent evt, JFormattedTextField aJFormattedTextField) {
        if (evt.getStateChange() == ItemEvent.SELECTED){
            aJFormattedTextField.setEnabled(true);
            IPbsysOptionQuote quote = owner.calculatePrice(false);
            if (quote != null){
                aJFormattedTextField.setValue(quote.getPrice());
            }
        }
        if (evt.getStateChange() == ItemEvent.DESELECTED){
            aJFormattedTextField.setEnabled(false);
        }
        owner.populateQuoteMessageTextField();
    }
    
    private String generateBidAskMessageTokenHelper(JCheckBox jBidAskCheck, JFormattedTextField aJFormattedTextField) {
        if (jBidAskCheck.isSelected()){
            return DataGlobal.formatDoubleWithMinMax(DataGlobal.convertToDouble(aJFormattedTextField.getText()),
                                                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
                                                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()));
        }else{
            return "";
        }
    }

    private double getSelectedBidAskValueHelper(JFormattedTextField aJFormattedTextField){
        String value = (aJFormattedTextField.getText()).trim();
        if (RegexGlobal.isNumberString(value)){
            return Double.parseDouble(value);
        }else{
            return 0.0;
        }
    }
    
    private void disableBidAskForQuoteFromViewerHelper(JCheckBox jBidAskCheck, JFormattedTextField aJFormattedTextField) {
        jBidAskCheck.setSelected(false);
        aJFormattedTextField.setEnabled(false);
        aJFormattedTextField.setValue(new Double(0));
    }

    private void populateBidAskForQuoteFromViewerHelper(JCheckBox jBidAskCheck, JFormattedTextField aJFormattedTextField, double value) {
        jBidAskCheck.setSelected(true);
        aJFormattedTextField.setEnabled(true);
        aJFormattedTextField.setValue(value);
    }

    private String getSimMarkBidAskFieldValue(JCheckBox aJCheckBox, JFormattedTextField aJFormattedTextField) {
        if (aJCheckBox.isSelected()){
            return DataGlobal.formatDoubleWithMinMax(DataGlobal.convertToDouble(aJFormattedTextField.getText()),
                                                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
                                                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()));
        }else{
            return PbcSimGuiParser.SimMarkNoValue;
        }
    }
    
    private void setupBidAndAskSpinners() {
        owner.setupSpinner(jBidSpinner, true, owner.getSelectedPointBoxQuoteCode());
        owner.setupSpinner(jAskSpinner, true, owner.getSelectedPointBoxQuoteCode());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMessagePanel = new javax.swing.JPanel();
        jQuoteMessageTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jAddLegBtn = new javax.swing.JButton();
        jRemoveLegBtn = new javax.swing.JButton();
        jCodeIncluded = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jCopyBtn = new javax.swing.JButton();
        jSendIM = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPBCastBtn = new javax.swing.JButton();
        jClearBtn = new javax.swing.JButton();
        jBidAskPanel = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jBidCheck = new javax.swing.JCheckBox();
        jBidSpinner = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jClassSelector = new javax.swing.JComboBox();
        jGroupSelector = new javax.swing.JComboBox();
        jCodeSelector = new javax.swing.JComboBox();
        jAskCheck = new javax.swing.JCheckBox();
        jAskSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox3 = new javax.swing.JComboBox();
        jStructurePricePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCalculateBtn = new javax.swing.JButton();
        jStructurePrice = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jVol = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(780, 190));
        setPreferredSize(new java.awt.Dimension(780, 190));

        jMessagePanel.setAutoscrolls(true);
        jMessagePanel.setPreferredSize(new java.awt.Dimension(795, 145));
        jMessagePanel.setLayout(new java.awt.GridLayout(2, 1, 0, 2));

        jQuoteMessageTextField.setEditable(false);
        jQuoteMessageTextField.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jQuoteMessageTextField.setForeground(java.awt.Color.red);
        jQuoteMessageTextField.setAlignmentY(1.0F);
        jQuoteMessageTextField.setMargin(new java.awt.Insets(2, 8, 2, 2));
        jMessagePanel.add(jQuoteMessageTextField);

        jPanel1.setLayout(new java.awt.GridLayout(1, 8, 2, 0));

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jAddLegBtn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jAddLegBtn.setText("Add Leg");
        jAddLegBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddLegBtnActionPerformed(evt);
            }
        });
        jPanel2.add(jAddLegBtn);

        jRemoveLegBtn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jRemoveLegBtn.setText("Remove Leg");
        jRemoveLegBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRemoveLegBtnActionPerformed(evt);
            }
        });
        jPanel2.add(jRemoveLegBtn);

        jPanel1.add(jPanel2);

        jCodeIncluded.setSelected(true);
        jCodeIncluded.setText("Include CODE");
        jCodeIncluded.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCodeIncludedItemStateChanged(evt);
            }
        });
        jPanel1.add(jCodeIncluded);

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        jCopyBtn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jCopyBtn.setText("Copy Quote");
        jCopyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCopyBtnActionPerformed(evt);
            }
        });
        jPanel4.add(jCopyBtn);

        jSendIM.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jSendIM.setText("Send IM");
        jSendIM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSendIMActionPerformed(evt);
            }
        });
        jPanel4.add(jSendIM);

        jPanel1.add(jPanel4);

        jPanel3.setLayout(new java.awt.GridLayout(1, 0));

        jPBCastBtn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jPBCastBtn.setText("PBCast To");
        jPBCastBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPBCastBtnMousePressed(evt);
            }
        });
        jPanel3.add(jPBCastBtn);

        jClearBtn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jClearBtn.setText("Clear");
        jClearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jClearBtnActionPerformed(evt);
            }
        });
        jPanel3.add(jClearBtn);

        jPanel1.add(jPanel3);

        jMessagePanel.add(jPanel1);

        jBidAskPanel.setLayout(new java.awt.GridLayout(4, 7, 3, 5));
        jBidAskPanel.add(jLabel12);
        jBidAskPanel.add(jLabel11);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("VOL");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jBidAskPanel.add(jLabel7);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("CLASS");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jBidAskPanel.add(jLabel4);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("GROUP");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jBidAskPanel.add(jLabel5);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("CODE");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jBidAskPanel.add(jLabel3);

        jBidCheck.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jBidCheck.setText(" BID:");
        jBidCheck.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jBidCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jBidCheckItemStateChanged(evt);
            }
        });
        jBidAskPanel.add(jBidCheck);

        jBidSpinner.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jBidSpinner.setEnabled(false);
        jBidAskPanel.add(jBidSpinner);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("0.0000");
        jBidAskPanel.add(jLabel9);

        jClassSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jClassSelectorItemStateChanged(evt);
            }
        });
        jBidAskPanel.add(jClassSelector);

        jGroupSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jGroupSelectorItemStateChanged(evt);
            }
        });
        jBidAskPanel.add(jGroupSelector);

        jCodeSelector.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jCodeSelector.setForeground(new java.awt.Color(255, 0, 0));
        jCodeSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCodeSelectorItemStateChanged(evt);
            }
        });
        jBidAskPanel.add(jCodeSelector);

        jAskCheck.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jAskCheck.setText("ASK:");
        jAskCheck.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jAskCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jAskCheckItemStateChanged(evt);
            }
        });
        jBidAskPanel.add(jAskCheck);

        jAskSpinner.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jAskSpinner.setEnabled(false);
        jBidAskPanel.add(jAskSpinner);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("0.0000");
        jBidAskPanel.add(jLabel8);
        jBidAskPanel.add(jLabel10);

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("EXCHANGE");
        jLabel13.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jBidAskPanel.add(jLabel13);

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("TYPE");
        jLabel14.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jBidAskPanel.add(jLabel14);
        jBidAskPanel.add(jLabel15);
        jBidAskPanel.add(jLabel16);
        jBidAskPanel.add(jLabel17);
        jBidAskPanel.add(jLabel18);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NYMEX" }));
        jBidAskPanel.add(jComboBox2);

        jBidAskPanel.add(jComboBox3);

        jStructurePricePanel.setLayout(new java.awt.GridLayout(4, 2, 2, 5));
        jStructurePricePanel.add(jLabel1);
        jStructurePricePanel.add(jLabel2);

        jCalculateBtn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jCalculateBtn.setForeground(new java.awt.Color(255, 0, 0));
        jCalculateBtn.setText("STRUCTURE PRICE");
        jCalculateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCalculateBtnActionPerformed(evt);
            }
        });
        jStructurePricePanel.add(jCalculateBtn);

        jStructurePrice.setBackground(new java.awt.Color(121, 210, 255));
        jStructurePrice.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jStructurePrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jStructurePrice.setText("0.0000");
        jStructurePrice.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jStructurePrice.setOpaque(true);
        jStructurePricePanel.add(jStructurePrice);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("VOLATILITY: ");
        jStructurePricePanel.add(jLabel6);

        jVol.setBackground(new java.awt.Color(204, 255, 204));
        jVol.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jVol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVol.setText("0.0000");
        jVol.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jVol.setOpaque(true);
        jStructurePricePanel.add(jVol);
        jStructurePricePanel.add(jLabel19);
        jStructurePricePanel.add(jLabel20);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMessagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 830, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jStructurePricePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBidAskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jStructurePricePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBidAskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jMessagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRemoveLegBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRemoveLegBtnActionPerformed
        owner.removePointBoxQuoteLegPanel();
    }//GEN-LAST:event_jRemoveLegBtnActionPerformed

    private void jAddLegBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAddLegBtnActionPerformed
        owner.addPointBoxQuoteLegPanel(true);
    }//GEN-LAST:event_jAddLegBtnActionPerformed

    private void jCopyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCopyBtnActionPerformed
        if(this.isQuoteMessageReady()){
            if (owner.isCustomSimQuote()){
                owner.displayWarningMessage("This operation is not applied to current quote message which contains custom-strategy. Consider using Send-IM button.", true);
            }else{
                jQuoteMessageTextField.selectAll();
                jQuoteMessageTextField.copy();
            }
        }else{
            owner.displayWarningMessage("Quote message is not completed yet.", true);
        }
    }//GEN-LAST:event_jCopyBtnActionPerformed

    private void jClearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jClearBtnActionPerformed
        owner.removePointBoxQuoteLegPanel();
        owner.clearPbcQuoteFrame();
    }//GEN-LAST:event_jClearBtnActionPerformed

    private void jPBCastBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPBCastBtnMousePressed
        if(this.isQuoteMessageReady()){
            popupSendToMenu(evt);
        }else{
            owner.displayWarningMessage("Quote message is not completed yet.",true);
        }
    }//GEN-LAST:event_jPBCastBtnMousePressed

    private void jCalculateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCalculateBtnActionPerformed
        handleCalculateBtnClicked(true);
    }//GEN-LAST:event_jCalculateBtnActionPerformed
    
    void handleCalculateBtnClicked(boolean displayMessage){
        IPbsysOptionQuote quote = owner.calculatePrice(displayMessage);
        if (quote == null){
            return;
        }
        jStructurePrice.setText(DataGlobal.formatDoubleWithMinMax(quote.getPrice(), 
                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        jVol.setText(PbcGlobal.localFormatStringByDoublePrecision(quote.getVol(), 4, "0"));
    }
    
    private void jClassSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jClassSelectorItemStateChanged
        aCodeBasedSelectorAgent.handleClassSelectorItemStateChanged(evt);
    }//GEN-LAST:event_jClassSelectorItemStateChanged

    private void jGroupSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jGroupSelectorItemStateChanged
        aCodeBasedSelectorAgent.handleGroupSelectorItemStateChanged(evt);
    }//GEN-LAST:event_jGroupSelectorItemStateChanged

    private void jCodeSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCodeSelectorItemStateChanged
        owner.populateQuoteMessageTextField();
        try{
            if (jCodeSelector.getSelectedItem() instanceof PointBoxQuoteCodeWrapper){
                PointBoxQuoteCode selectedCode = ((PointBoxQuoteCodeWrapper)jCodeSelector.getSelectedItem()).getCode();
                PointBoxConsoleProperties.getSingleton().storeSelectedPointBoxQuoteCode(owner.getKernel().getPointBoxLoginUser().getIMUniqueName(), selectedCode);
                owner.refreshGuiForSelectedCode(selectedCode);
                handleCalculateBtnClicked(false);
            }
        }catch (Exception ex){}
    }//GEN-LAST:event_jCodeSelectorItemStateChanged

    private void jSendIMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSendIMActionPerformed
        displaySendIMDialogHelper();
    }//GEN-LAST:event_jSendIMActionPerformed

    private void jBidCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jBidCheckItemStateChanged
        //handleBidAskCheckEvent(evt, jBidSpinner);
        handleBidAskCheckEvent(evt, jBidTextField);
    }//GEN-LAST:event_jBidCheckItemStateChanged

    private void jAskCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jAskCheckItemStateChanged
        handleBidAskCheckEvent(evt, jAskTextField);
    }//GEN-LAST:event_jAskCheckItemStateChanged

    private void jCodeIncludedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCodeIncludedItemStateChanged
        owner.populateQuoteMessageTextField();
    }//GEN-LAST:event_jCodeIncludedItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAddLegBtn;
    private javax.swing.JCheckBox jAskCheck;
    private javax.swing.JSpinner jAskSpinner;
    private javax.swing.JPanel jBidAskPanel;
    private javax.swing.JCheckBox jBidCheck;
    private javax.swing.JSpinner jBidSpinner;
    private javax.swing.JButton jCalculateBtn;
    private javax.swing.JComboBox jClassSelector;
    private javax.swing.JButton jClearBtn;
    private javax.swing.JCheckBox jCodeIncluded;
    private javax.swing.JComboBox jCodeSelector;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JButton jCopyBtn;
    private javax.swing.JComboBox jGroupSelector;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jMessagePanel;
    private javax.swing.JButton jPBCastBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jQuoteMessageTextField;
    private javax.swing.JButton jRemoveLegBtn;
    private javax.swing.JButton jSendIM;
    private javax.swing.JLabel jStructurePrice;
    private javax.swing.JPanel jStructurePricePanel;
    private javax.swing.JLabel jVol;
    // End of variables declaration//GEN-END:variables

    private void popupSendToMenu(MouseEvent e) {
        JPopupMenu sendToMenu = loadCopyAndSendToMenu();
        sendToMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private JPopupMenu loadCopyAndSendToMenu() {
        JPopupMenu sendToMenu = new JPopupMenu();
        final IPbcTalker talker = owner.getKernel().getPointBoxFace().getPointBoxTalker();
        //final IPbsysOptionQuoteWrapper targetQuoteWrapper=frame.getTargetQuoteWrapper();
        ArrayList<IGatewayConnectorGroup> groupList = talker.getPitsCastGroups();
        JMenuItem aGroupMenuItem;
        if ((groupList == null) || (groupList.isEmpty())){
            aGroupMenuItem = new JMenuItem();
            aGroupMenuItem.setText("No PBcast Group yet...");
            sendToMenu.add(aGroupMenuItem);
        }else{
            for (IGatewayConnectorGroup group : groupList){
                aGroupMenuItem = new JMenuItem();
                aGroupMenuItem.setText(group.getGroupName());
                final IGatewayConnectorGroup sendToGroup = group;
                aGroupMenuItem.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        owner.sendStructuredMessageTo(talker.getBuddiesOfPitsCastGroup(sendToGroup), PbcQuotePanel.this);
                    }
                });
                sendToMenu.add(aGroupMenuItem);
            }
        }
        return sendToMenu;
    }
    
    /**
     * 
     * @return - whether or not users want to embedded CODE into the message text
     */
    boolean isCodeIncluded(){
        return jCodeIncluded.isSelected();
    }

    void finalizeQuoteMessageTextField(final String legMessagePartToken) {
        if (SwingUtilities.isEventDispatchThread()){
            finalizeQuoteMessageTextFieldHelper(legMessagePartToken);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    finalizeQuoteMessageTextFieldHelper(legMessagePartToken);
                }
            });
        }
    }
    
    private void finalizeQuoteMessageTextFieldHelper(final String legMessagePartToken) {
        if (jCodeSelector.getSelectedItem() == null){
            return;
        }
        /**
         * bidAskToken is always located at the end of the quote message if it existed
         */
        String bidAskToken = generateBidAskMessageToken();
        String msgText;
        if (bidAskToken == null){
            if (isCodeIncluded()){
                msgText = jCodeSelector.getSelectedItem().toString() 
                    + PbcSimGuiParser.WhiteSpace + legMessagePartToken;
            }else{
                msgText = legMessagePartToken;

            }
        }else{
            if (isCodeIncluded()){
                msgText = jCodeSelector.getSelectedItem().toString() 
                    + PbcSimGuiParser.WhiteSpace + legMessagePartToken + PbcSimGuiParser.WhiteSpace + bidAskToken;
            }else{
                msgText = legMessagePartToken + PbcSimGuiParser.WhiteSpace + bidAskToken;
            }
        }
        
        jQuoteMessageTextField.setText(msgText);
        if (isQuoteMessageReady()){
            jQuoteMessageTextField.setForeground(Color.BLUE);
        }else{
            jQuoteMessageTextField.setForeground(Color.RED);
        }
    }

    /**
     * 
     * @return if it is NULL, it means no Bid/Ask
     */
    private String generateBidAskMessageToken() {
        String bid = generateBidAskMessageTokenHelper(jBidCheck,jBidTextField);
        String ask = generateBidAskMessageTokenHelper(jAskCheck,jAskTextField);
        if ((DataGlobal.isEmptyNullString(bid))&&(DataGlobal.isEmptyNullString(ask))){
            return "";
        }else{
            return bid + PbcSimGuiParser.DataDelimiter + ask;
        }
    }
    
    /**
     * This method will validate the message first. If the message is not ready, 
     * NULL will be returned;
     * @return 
     */
    String getPopulatedQuoteMessage(){
        if (isQuoteMessageReady()){
            return jQuoteMessageTextField.getText();
        }else{
            owner.populateQuoteMessageTextField();
            if (isQuoteMessageReady()){
                return jQuoteMessageTextField.getText();
            }else{
                return null;
            }
        }
    }
    
    boolean isQuoteMessageReady(){
        String qmsg = jQuoteMessageTextField.getText();
        if ((DataGlobal.isNonEmptyNullString(qmsg)) && (!qmsg.contains("?"))){
            return true;
        }else{
            return false;
        }
    }

    /**
     * @return - it could NULL
     */
    PbcPricingModel getSelectedPbcPricingModel() {
        PointBoxQuoteCode code = getSelectedPointBoxQuoteCode();
        PbcPricingModel aPbcPricingModel = owner.getKernel().getPbcPricingModel(code);
        if (aPbcPricingModel == null){
            aPbcPricingModel = owner.getKernel().getDefaultPbcPricingModel();
            if (aPbcPricingModel == null){
                return null;
            }
        }
        return aPbcPricingModel;
    }

    void setSelectedPointBoxQuoteCode(final PointBoxQuoteCode targetCode) {
        if (SwingUtilities.isEventDispatchThread()){
            setSelectedPointBoxQuoteCodeHelper(targetCode);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setSelectedPointBoxQuoteCodeHelper(targetCode);
                }
            });
        }
    }

    private void setSelectedPointBoxQuoteCodeHelper(final PointBoxQuoteCode targetCode) {
        if (targetCode == null){
            return;
        }
        aCodeBasedSelectorAgent.setSelectedSimCode(targetCode);
//        jCodeSelector.setSelectedItem(new PointBoxQuoteCodeWrapper(targetCode));
    }
    
    PointBoxQuoteCode getSelectedPointBoxQuoteCode(){
        if (jCodeSelector.getSelectedItem() instanceof PointBoxQuoteCodeWrapper){
            return ((PointBoxQuoteCodeWrapper)jCodeSelector.getSelectedItem()).getCode();
        }else{
            return owner.getKernel().getDefaultSimCodeFromProperties();
        }
    }

    double getSelectedBidValue(){
        return getSelectedBidAskValueHelper(jBidTextField);
    }

    double getSelectedAskValue(){
        return getSelectedBidAskValueHelper(jAskTextField);
    }

//    private double getSelectedBidAskValueHelper(JSpinner jSpinner){
//        String value = (((JSpinner.NumberEditor)jSpinner.getEditor()).getTextField().getText()).trim();
//        if (RegexGlobal.isNumberString(value)){
//            return Double.parseDouble(value);
//        }else{
//            return 0.0;
//        }
//    }
    
    void clearPbcQuotePanel() {
        jStructurePrice.setText(DataGlobal.formatDoubleWithMinMax(0.0, 
                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        jStructurePrice.setBackground(defaultBackgroundForPrice);
        jStructurePrice.setForeground(defaultForegroundForPrice);
        jVol.setText("0.0000");
        jVol.setBackground(defaultBackgroundForVol);
        jVol.setForeground(defaultForegroundForVol);
        jBidTextField.setValue(0.0);
        jAskTextField.setValue(0.0);
//        jAskBidWarningMessage.setText("");
    }

    private void displaySendIMDialogHelper() {
        String msg = getPopulatedQuoteMessage();
        if (msg == null){
            owner.displayWarningMessage("Structured message is not constructed completely.", true);
        }else{
            PbcSimDialog aPbcSimDialog = new PbcSimDialog(owner, true, msg);
            aPbcSimDialog.setLocation(new Point(400, 300));
            aPbcSimDialog.setVisible(true);
        }
    }

    @Override
    public void publishStatusMessage(final String msg) {
        if (SwingUtilities.isEventDispatchThread()){
            publishStatusMessageHelper(msg);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    publishStatusMessageHelper(msg);
                }
            });
        }
    }
    
    private void publishStatusMessageHelper(final String msg) {
//        jStatus.setText(msg);
    }

    void populateQuoteFromViewer(IPbsysOptionQuote quote) {
        aCodeBasedSelectorAgent.setClassGroupCodeSelectorForSpecificCode(PointBoxQuoteCode.convertEnumNameToType(quote.getPbcPricingModel().getSqCode()));
        populateBidAskForQuoteFromViewer(quote);
    }

    private void populateBidAskForQuoteFromViewer(IPbsysOptionQuote quote) {
        int bidAskStatus = quote.getBidAskStatus();
        if ((bidAskStatus == 1) || (bidAskStatus == 3)){
            populateBidAskForQuoteFromViewerHelper(jBidCheck, jBidTextField, quote.getBid());
        }else{
            disableBidAskForQuoteFromViewerHelper(jBidCheck, jBidTextField);
        }
        if ((bidAskStatus == 2) || (bidAskStatus == 3)){
            populateBidAskForQuoteFromViewerHelper(jAskCheck, jAskTextField, quote.getAsk());
        }else{
            disableBidAskForQuoteFromViewerHelper(jAskCheck, jAskTextField);
        }
    }

//    private void disableBidAskForQuoteFromViewerHelper(JCheckBox jBidAskCheck, JSpinner jBidAskSpinner) {
//        jBidAskCheck.setSelected(false);
//        jBidAskSpinner.setEnabled(false);
//        jBidAskSpinner.setValue(new Double(0));
//    }
//
//    private void populateBidAskForQuoteFromViewerHelper(JCheckBox jBidAskCheck, JSpinner jBidAskSpinner, double value) {
//        jBidAskCheck.setSelected(true);
//        jBidAskSpinner.setEnabled(true);
//        jBidAskSpinner.setValue(value);
//    }

//    private void handleBidAskCheckEvent(ItemEvent evt, JSpinner jBidAskSpinner) {
//        if (evt.getStateChange() == ItemEvent.SELECTED){
//            jBidAskSpinner.setEnabled(true);
//            IPbsysOptionQuote quote = owner.calculatePrice(false);
//            if (quote != null){
//                jBidAskSpinner.setValue(quote.getPrice());
//            }
//        }
//        if (evt.getStateChange() == ItemEvent.DESELECTED){
//            jBidAskSpinner.setEnabled(false);
//        }
//        owner.populateQuoteMessageTextField();
//    }

//    private String generateBidAskMessageTokenHelper(JCheckBox jBidAskCheck, JSpinner jBidAskSpinner) {
//        if (jBidAskCheck.isSelected()){
//            return DataGlobal.formatDoubleWithMinMax(DataGlobal.convertToDouble(((JSpinner.NumberEditor)jBidAskSpinner.getEditor()).getTextField().getText()),
//                                                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
//                                                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()));
//        }else{
//            return "";
//        }
//    }

    /**
     * This method assumes that the quote-message is ready. Refer to isQuoteMessageReady().
     * @return 
     */
    String generateSimMarkFieldValuesToken() {
        if (jCodeSelector.getSelectedItem() instanceof PointBoxQuoteCodeWrapper){
            //CODE#BID#ASK
            String token = ((PointBoxQuoteCodeWrapper)jCodeSelector.getSelectedItem()).getCode().name() + PbcSimGuiParser.SimMarkValueDelimiter;
            token += getSimMarkBidAskFieldValue(jBidCheck, jBidTextField) + PbcSimGuiParser.SimMarkValueDelimiter;
            token += getSimMarkBidAskFieldValue(jAskCheck, jAskTextField);
            return token;
        }else{
            return null;
        }
    }

//    private String getSimMarkBidAskFieldValue(JCheckBox aJCheckBox, JSpinner jSpinner) {
//        if (aJCheckBox.isSelected()){
//            return DataGlobal.formatDoubleWithMinMax(DataGlobal.convertToDouble(((JSpinner.NumberEditor)jSpinner.getEditor()).getTextField().getText()),
//                                                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
//                                                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()));
//        }else{
//            return PbcSimGuiParser.SimMarkNoValue;
//        }
//    }

    /**
     * 0 = no bid and no ask
     * 1 = have bid bit no ask
     * 2 = have bid and have ask
     * 3 = no bid but have ask
     */
    int getBidAskStatus() {
        int flag = 0;
        
        if (jBidCheck.isSelected()){
            flag = 1;
        }
        
        if (jAskCheck.isSelected()){
            flag = flag + 2;
        }
        
        return flag; 
    }

    void refreshGuiForSelectedCode(final PointBoxQuoteCode selectedCode) {
        if (SwingUtilities.isEventDispatchThread()){
            refreshGuiForSelectedCodeHelper(selectedCode);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    refreshGuiForSelectedCodeHelper(selectedCode);
                }
            });
        }
    }

    private void refreshGuiForSelectedCodeHelper(PointBoxQuoteCode selectedCode) {
        owner.setupSpinner(jBidSpinner, true, selectedCode);
        owner.setupSpinner(jAskSpinner, true, selectedCode);
    }
}
