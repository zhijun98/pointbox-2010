/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import static com.eclipsemarkets.pbc.pricer.sim.PricingVolSkewSurfaceTableModel.SKEW_START_INDEX;
import com.eclipsemarkets.pricer.commons.FormatterCommons;
import com.eclipsemarkets.web.pbc.PricingCurveFileSettings;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Zhijun Zhang
 */
public class PricingVolSkewSurfaceFrame extends AbstractPricingRuntimeCurveFrame {

    private PricingAdjustionPanel aPricingAdjustionPanel;
    
    /**
     * Creates new form PricingVolSkewSurfaceFrame
     */
    public PricingVolSkewSurfaceFrame(IPbcKernel kernel, PointBoxQuoteCode code) {
        super(kernel, code);
        initComponents();
        initializeCurveFrame(jClassSelector, jGroupSelector, jCodeSelector);
        
        aPricingAdjustionPanel = new PricingAdjustionPanel(this);
        jAdjustionHolder.add(aPricingAdjustionPanel, BorderLayout.CENTER);
    }

    @Override
    String getFrameTitle() {
        return "Volatility Skew Surface";
    }

    @Override
    void updateFrameForTargetCodeImpl(PointBoxQuoteCode code) {
        PricingVolSkewSurfaceTableModel model = new PricingVolSkewSurfaceTableModel(this);
        jTargetTable.setModel(model);
        jTargetTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTargetTable.getTableHeader().setReorderingAllowed(false);
        model.setUpColumns(jTargetTable);
        if (aPricingAdjustionPanel != null){
            aPricingAdjustionPanel.populateMonthInEDT();
        }
    }

    @Override
    JTable getTargetTable() {
        return jTargetTable;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jClassSelector = new javax.swing.JComboBox();
        jGroupSelector = new javax.swing.JComboBox();
        jCodeSelector = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jSave = new javax.swing.JButton();
        jRefresh = new javax.swing.JButton();
        jClose = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTargetTable = new javax.swing.JTable();
        jAdjustionHolder = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 600));

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jPanel2.add(jClassSelector);

        jPanel2.add(jGroupSelector);

        jPanel2.add(jCodeSelector);
        jPanel2.add(jLabel3);

        jSave.setText("Save");
        jSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveActionPerformed(evt);
            }
        });
        jPanel2.add(jSave);

        jRefresh.setText("Refresh");
        jRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRefreshActionPerformed(evt);
            }
        });
        jPanel2.add(jRefresh);

        jClose.setText("Close");
        jClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseActionPerformed(evt);
            }
        });
        jPanel2.add(jClose);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(650);
        jSplitPane1.setDividerSize(1);
        jSplitPane1.setResizeWeight(1.0);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jTargetTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTargetTable);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel3);

        jAdjustionHolder.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(jAdjustionHolder);

        jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseActionPerformed
        dispose();
    }//GEN-LAST:event_jCloseActionPerformed

    private void jRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRefreshActionPerformed
        updateFrameForTargetCode(getTargetCode());
    }//GEN-LAST:event_jRefreshActionPerformed

    private void jSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveActionPerformed
        try {
            
            TableCellEditor aTableCellEditor = jTargetTable.getCellEditor();
            if (aTableCellEditor != null){
                Object obj = aTableCellEditor.getCellEditorValue();
                if (obj != null){
                    aTableCellEditor.stopCellEditing();
                }
            }
            
            PricingVolSkewSurfaceTableModel model = (PricingVolSkewSurfaceTableModel)jTargetTable.getModel();
            Vector<GregorianCalendar> contractDates= model.getAllContractsData();
            int columnTotal = model.getColumnCount();
            int rowTotal = model.getRowCount();
            String skewContent = CalendarGlobal.convertToMDYYYY(new GregorianCalendar());
            Vector<Double> allVolStrikesData = model.getAllVolStrikesData();
            for (int k = PricingVolSkewSurfaceTableModel.SKEW_START_INDEX; k < columnTotal; k++){
                skewContent += "\t" + allVolStrikesData.get(k - SKEW_START_INDEX);
            }
            skewContent += NIOGlobal.lineSeparator();
            String atmContent = "";
            GregorianCalendar contractDate;
            for (int i = 0; i < rowTotal; i++){
                String skewLine = "";
                String atmLine = "";
                for (int j = 0; j < columnTotal; j++){
                    if (j == 0){
                        contractDate = contractDates.get(i);
                        skewLine = Integer.toString(contractDate.get(Calendar.MONTH) + 1)
                                + "/" + contractDate.get(Calendar.DAY_OF_MONTH) 
                                + "/" + contractDate.get(Calendar.YEAR);
                        atmLine = skewLine;
                    }else if (j >= PricingVolSkewSurfaceTableModel.SKEW_START_INDEX){
                        skewLine += "\t" + model.getValueAt(i, j).toString();
                    }else if (j == PricingVolSkewSurfaceTableModel.ATM_VOL_INDEX){
                        atmLine += "\t" + FormatterCommons.format4Dec(DataGlobal.convertToDouble(model.getValueAt(i, j).toString())/100);
                    }
                }
                skewContent += skewLine + NIOGlobal.lineSeparator();
                atmContent += atmLine + NIOGlobal.lineSeparator();
            }
            PricingCurveFileSettings aPricingCurveFileSettings = getKernel().retrievePricingCurveFileSettings(getTargetCode(), PointBoxCurveType.VolSkewSurface);
            String filePath = getKernel().getLocalCurveFileFullPath(getTargetCode(), aPricingCurveFileSettings, true);
            NIOGlobal.writeTextToNewFileUnsafely(skewContent, filePath);
            
            aPricingCurveFileSettings = getKernel().retrievePricingCurveFileSettings(getTargetCode(), PointBoxCurveType.AtmVolCurve);
            filePath = getKernel().getLocalCurveFileFullPath(getTargetCode(), aPricingCurveFileSettings, true);
            NIOGlobal.writeTextToNewFileUnsafely(atmContent, filePath);
        } catch (IOException ex) {
            Logger.getLogger(PricingVolSkewSurfaceFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jAdjustionHolder;
    private javax.swing.JComboBox jClassSelector;
    private javax.swing.JButton jClose;
    private javax.swing.JComboBox jCodeSelector;
    private javax.swing.JComboBox jGroupSelector;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton jRefresh;
    private javax.swing.JButton jSave;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTargetTable;
    // End of variables declaration//GEN-END:variables
}
