/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.global.SwingGlobal;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 *
 * @author Fang.Bao
 */
public class PricingAdjustionPanel extends javax.swing.JPanel {
    private AbstractPricingRuntimeCurveFrame targetFrame;
    /**
     * Creates new form PriceCurveAdjustionPanel
     */
    public PricingAdjustionPanel(AbstractPricingRuntimeCurveFrame frame) {
        this.targetFrame = frame;
        initComponents();
        
        if(this.targetFrame instanceof PricingForwardCurveFrame){
            jRadioButton1.setText("   0.0001");
            jRadioButton2.setText("   0.001");
            jRadioButton3.setText("   0.01");
            jRadioButton1.setVisible(false);
            jRadioButton2.setVisible(false);
            jRadioButton3.setSelected(true);
            percentageLabel.setVisible(false);
        }else if(this.targetFrame instanceof PricingVolSkewSurfaceFrame){
            jRadioButton1.setText("   0.05%");
            jRadioButton2.setText("   0.1%");
            jRadioButton3.setText("   1%");
            percentageLabel.setVisible(true);
        }else{
            
        }
       
        monthList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                    if (super.isSelectedIndex(index0)) {
                            super.removeSelectionInterval(index0, index1);
                    } else {
                            super.addSelectionInterval(index0, index1);
                    }
            }
	});        
        
        monthList.setCellRenderer(new MonthListRender());
        
        preTickValField.setEditable(false);
        
        
        for (Enumeration<AbstractButton> buttons = buttonGroup1.getElements(); buttons.hasMoreElements() ;){
            AbstractButton button = buttons.nextElement(); 
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(jRadioButton7.isSelected()){
                        preTickValField.setEditable(true);
                    }else{
                        preTickValField.setEditable(false);
                    }                        
                }
            });
            
        }
        
        populateMonthInEDT();
        
        if(monthList.getModel().getSize()>0){
            monthList.setSelectedIndex(0);
        }
        
        if(targetFrame instanceof PricingForwardCurveFrame){
            setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Price", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(153, 0, 0))); // NOI18N
        }
        if(targetFrame instanceof PricingVolSkewSurfaceFrame){
            setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ATM Vol(%)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(153, 0, 0))); // NOI18N
        }        
    }

    private void adjustActionInEDT(final boolean increase) {
        if(SwingUtilities.isEventDispatchThread()){
            adjustAction(increase);
        }else{
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    adjustAction(increase);
                }
            });
        }
    }

    public  boolean isNumber(String str) {
        return str.matches("^-?[0-9]+(\\.[0-9]+)?$");
    }    
    
    private void adjustAction(boolean increase) {
        double tickValue=0;
        for (Enumeration<AbstractButton> buttons = buttonGroup1.getElements(); buttons.hasMoreElements() ;){
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                String radioTxt=button.getText().trim();
                if(!radioTxt.isEmpty()){
                    if(targetFrame instanceof PricingVolSkewSurfaceFrame){
                        radioTxt=radioTxt.substring(0,radioTxt.length()-1);
                    }
                    tickValue=Double.parseDouble(radioTxt);
                }else{
                    String val=preTickValField.getText().trim().replaceAll("\\s", "");
                    if(val.startsWith(".")){
                        val="0".concat(val);
                    }
                    if((!val.isEmpty())&&isNumber(val)){
                        tickValue=Double.parseDouble(val);
                    }
                }
            }
        } 
        
        
        int colunmIndex=-1;
        if(targetFrame instanceof PricingForwardCurveFrame){
            colunmIndex = 1;
        }else if(targetFrame instanceof PricingVolSkewSurfaceFrame){
            colunmIndex = 3;
        }else{
            
        }
        updateTableCellValues(colunmIndex, tickValue, 0, increase);
        if(targetFrame instanceof PricingForwardCurveFrame){
            updateTableCellValues(colunmIndex+1, tickValue, 1, increase);
        }
    }
    
    private void updateTableCellValues(int colunmIndex, double tickValue, int offsetRow, boolean increase){
        JTable table = targetFrame.getTargetTable();
        if (table != null){
            int rowNum;
            for(int row : monthList.getSelectedIndices()){
                rowNum = row + offsetRow;
                if ((rowNum > -1) && (rowNum < monthList.getModel().getSize())){
                    double originalVal= (Double.parseDouble(table.getValueAt(rowNum, colunmIndex).toString()));
                    double newVal;
                    if(increase){
                        newVal=originalVal+tickValue;
                    }else{
                        newVal=originalVal-tickValue;
                    }
                    table.setValueAt(newVal, rowNum, colunmIndex);
                }
            }
            table.updateUI();
        }
    
    }

    private void selectAllActionInEDT() {
        if(SwingUtilities.isEventDispatchThread()){
            selectAllAction();
        }else{
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    selectAllAction();
                }
            });
        }
    }

    private void selectAllAction() {
        monthList.clearSelection();
       if(jCheckBox1.isSelected()){
           for(int i=0;i<monthList.getModel().getSize();i++){
               monthList.setSelectedIndex(i);
        }
       }
    }
    
    class MonthListRender extends DefaultListCellRenderer{
        Color originalLabelForeground;
        MonthListRender() {
            originalLabelForeground = this.getBackground();
        }
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            setFont(SwingGlobal.getLabelFont());   
            
            if(isSelected){
                setBackground(Color.YELLOW);
            }else{
                setBackground(originalLabelForeground);
            }
            return this;            
        }        
    }
    
    private void populateMonth(){
        DefaultListModel model=new DefaultListModel(); 
        JTable table = targetFrame.getTargetTable();
        if (table != null){
            for(int i=0;i<table.getRowCount();i++){
                String month=table.getModel().getValueAt(i, 0).toString();
                model.add(i,month);
            }
        }
        monthList.setModel(model);
    }

    
    void populateMonthInEDT() {
        if(SwingUtilities.isEventDispatchThread()){
            populateMonth();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    populateMonth();
                }
            });
        }
    }    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        monthList = new javax.swing.JList();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton7 = new javax.swing.JRadioButton();
        preTickValField = new JFormattedTextField();
        percentageLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Adjustion", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(153, 0, 0)));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        monthList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(monthList);

        jCheckBox1.setText("Select All");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("0.0001");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("0.0005");

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("0.0010");

        jLabel1.setText("Shift:");

        buttonGroup1.add(jRadioButton7);

        preTickValField.setText("0");

        percentageLabel.setText("%");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jRadioButton1)
            .addComponent(jRadioButton2)
            .addComponent(jRadioButton3)
            .addComponent(jLabel1)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jRadioButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(preTickValField, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(percentageLabel))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(preTickValField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(percentageLabel))
                    .addComponent(jRadioButton7, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jButton1.setText("+");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jButton2.setText("-");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        adjustActionInEDT(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        adjustActionInEDT(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        selectAllActionInEDT();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList monthList;
    private javax.swing.JLabel percentageLabel;
    private javax.swing.JTextField preTickValField;
    // End of variables declaration//GEN-END:variables

}
