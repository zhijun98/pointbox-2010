/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pricer.commons.FormatterCommons;
import com.eclipsemarkets.pricer.data.IPriceCurveData;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Fang.Bao
 */
public class CustomForwardCurvePanel extends javax.swing.JPanel {
    
    private PricingForwardCurveFrame owner;
    private IPbcKernel kernel;
    private int panelIndex;
    
    private Vector<String> contractDateOptions;
    private Vector<String> realContractDateOptions;
    /**
     * Creates new form CustomForwardCurvePanel
     */
    public CustomForwardCurvePanel(PricingForwardCurveFrame owner, int panelIndex, IPbcKernel kernel) {
        initComponents();
        
        this.owner = owner;
        this.kernel = kernel; 
        this.panelIndex = panelIndex;
        
        updateStartEndComboBoxes();  
    }
    
    void updateStartEndComboBoxes() {
        
        if(SwingUtilities.isEventDispatchThread()){
            updateStartEndComboBoxesHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateStartEndComboBoxesHelper();
                }
            });
        }
    }
    
    private void updateStartEndComboBoxesHelper(){
        
        Vector<GregorianCalendar> contractDates = owner.getAllContractsData();

        contractDateOptions = new Vector<String>();
        realContractDateOptions = new Vector<String>();
        contractDateOptions.add("");
        for (int i = 0; i < contractDates.size(); i++) {
            contractDateOptions.add(CalendarGlobal.convertToContractMMMYY(contractDates.get(i)));
            realContractDateOptions.add(CalendarGlobal.convertToContractMMMYY(contractDates.get(i)));
        }        
        
        PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
        List<String> stripSpreadStr=prop.retrieveStripsSpreads(kernel.getPointBoxLoginUser().getIMUniqueName());
        if(stripSpreadStr!=null&&!stripSpreadStr.isEmpty()&&stripSpreadStr.size()>=12){
            String[] arr=stripSpreadStr.get(panelIndex).split(",");
            String startMon=arr[0];
            if(!startMon.trim().isEmpty()&&!contractDateOptions.contains(startMon)){
                contractDateOptions.add(1, startMon);
            }
        } 
        
        startComboBox.setModel(new DefaultComboBoxModel(contractDateOptions));
        endComboBox.setModel(new DefaultComboBoxModel(contractDateOptions));
    }
    
   private void computeSpreadValue(){
        int startIndex = startComboBox.getSelectedIndex() - 1;
        int endIndex = endComboBox.getSelectedIndex()-1;

        if (startIndex == -1 && endIndex == -1) {
            priceField.setValue("");

        } else {

            IPriceCurveData priceCurveData = owner.getPriceCurveData();
            if (priceCurveData == null){
                return;
            }
            double tempPrice;
            tempPrice= priceCurveData.getDataAt(startIndex).getPrice()-priceCurveData.getDataAt(endIndex).getPrice();
            priceField.setValue(FormatterCommons.format4Dec(tempPrice));
        }       
   }
    
   private void computeSwapValue() {

        int startIndex = startComboBox.getSelectedIndex() - 1;
        int endIndex = endComboBox.getSelectedIndex()-1;

        if (startIndex == -1 && endIndex == -1) {
            priceField.setValue("");
        }else if(!realContractDateOptions.contains(startComboBox.getSelectedItem().toString())){
            priceField.setValue("");
        }else {

            IPriceCurveData priceCurveData = owner.getPriceCurveData();
            if (priceCurveData == null){
                return;
            }
            //LinkedHashMap<String, PriceCurveDataPoint> allPriceCurveData = tableModel.getAllPriceCurveData();
            int months = endIndex - startIndex + 1;
            double tempPrice = 0.0;
            for (int i = startIndex; i < endIndex + 1; i++) {
                tempPrice += priceCurveData.getDataAt(i).getPrice();
                //tempPrice += allPriceCurveData.get("@" + (i + 1) + "ng").getPrice();
            }
            tempPrice /= months;

            priceField.setValue(FormatterCommons.format4Dec(tempPrice));
        }
    }    

   public void calculateValue(){
        if(stripRadioBtn.isSelected()){
            computeSwapValue();
        }else{
            computeSpreadValue();
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
        startComboBox = new javax.swing.JComboBox();
        endComboBox = new javax.swing.JComboBox();
        stripRadioBtn = new javax.swing.JRadioButton();
        spreadRadioBtn = new javax.swing.JRadioButton();
        priceField = new javax.swing.JFormattedTextField();

        setLayout(new java.awt.GridLayout(1, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "StripsSpreads", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(153, 0, 0)));
        jPanel1.setLayout(new java.awt.GridLayout(5, 0, 0, 2));

        startComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(startComboBox);

        endComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(endComboBox);

        buttonGroup1.add(stripRadioBtn);
        stripRadioBtn.setSelected(true);
        stripRadioBtn.setText("Strip");
        stripRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stripRadioBtnActionPerformed(evt);
            }
        });
        jPanel1.add(stripRadioBtn);

        buttonGroup1.add(spreadRadioBtn);
        spreadRadioBtn.setText("Spread");
        spreadRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spreadRadioBtnActionPerformed(evt);
            }
        });
        jPanel1.add(spreadRadioBtn);

        priceField.setEditable(false);
        priceField.setBackground(new java.awt.Color(121, 210, 255));
        priceField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        priceField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        priceField.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jPanel1.add(priceField);

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void startComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startComboBoxActionPerformed
        int startIndex = startComboBox.getSelectedIndex();
        int endIndex = endComboBox.getSelectedIndex();

        if (startIndex == 0 || startIndex > endIndex) {
            endComboBox.setSelectedIndex(startIndex);
        }
        calculateValue();
    }//GEN-LAST:event_startComboBoxActionPerformed

    private void endComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endComboBoxActionPerformed
        int startIndex = startComboBox.getSelectedIndex();
        int endIndex = endComboBox.getSelectedIndex();

        if (startIndex > endIndex || (startIndex == 0 && endIndex > 0)) {
            startComboBox.setSelectedIndex(endIndex);
        }
        calculateValue();
    }//GEN-LAST:event_endComboBoxActionPerformed

    private void stripRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stripRadioBtnActionPerformed
        calculateValue();
    }//GEN-LAST:event_stripRadioBtnActionPerformed

    private void spreadRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spreadRadioBtnActionPerformed
        calculateValue();
    }//GEN-LAST:event_spreadRadioBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox endComboBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JFormattedTextField priceField;
    private javax.swing.JRadioButton spreadRadioBtn;
    private javax.swing.JComboBox startComboBox;
    private javax.swing.JRadioButton stripRadioBtn;
    // End of variables declaration//GEN-END:variables

    public String getStartMonth(){
        return startComboBox.getSelectedItem().toString();
    }
    
    public String getEndMonth(){
        return endComboBox.getSelectedItem().toString();
    }
    
    public String isStrip(){
        if(stripRadioBtn.isSelected()){
            return "true";
        }else{
            return "false";
        }
    }
    
    public void initCustPanel(String startMon,String endMon,String isStrip){
        if(startMon!=null&&!startMon.isEmpty()){
            startComboBox.setSelectedItem(startMon);
        }
        if(endMon!=null&&!endMon.isEmpty()){
            endComboBox.setSelectedItem(endMon);
        }
        if(isStrip!=null&&!isStrip.isEmpty()){
            if(isStrip.equalsIgnoreCase("true")){
                stripRadioBtn.setSelected(true);
            }else{
                spreadRadioBtn.setSelected(true);
            }
        }
    }
}
