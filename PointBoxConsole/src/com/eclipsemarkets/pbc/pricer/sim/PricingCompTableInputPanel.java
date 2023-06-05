package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.data.PointBoxQuoteStrategyTerm;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import com.eclipsemarkets.pricer.commons.FormatterCommons;

/**
 *
 * @author Christine.Kim
 */
public class PricingCompTableInputPanel extends javax.swing.JPanel implements PropertyChangeListener {
    
    private PricingCompTableModel tableModel;
    
    private String[] callPutOptions = {"", "c", "p"};
    private Vector<String> stuctureOptions;

    private double oldStrikeField1;
    private double oldStrikeField2;
    private double oldStrikeField3;
    private double oldStrikeField4;
    private double oldStrikeField5;

    private double oldRatioField1;
    private double oldRatioField2;
    private double oldRatioField3;
    private double oldRatioField4;
    private double oldRatioField5;

    /** Creates new form PricingCompTableInputPanel */
    public PricingCompTableInputPanel(PricingCompTableModel tableModel) {
        
        this.tableModel = tableModel;

        stuctureOptions = PointBoxQuoteStrategyTerm.getEnumValueVector(false);

        oldStrikeField1 = 0.0;
        oldStrikeField2 = 0.0;
        oldStrikeField3 = 0.0;
        oldStrikeField4 = 0.0;
        oldStrikeField5 = 0.0;

        oldRatioField1 = 0.0;
        oldRatioField2 = 0.0;
        oldRatioField3 = 0.0;
        oldRatioField4 = 0.0;
        oldRatioField5 = 0.0;

        initComponents();
        initListeners();

        strikeField1.setInputVerifier(new ValueInputVerifier());
        strikeField2.setInputVerifier(new ValueInputVerifier());
        strikeField3.setInputVerifier(new ValueInputVerifier());
        strikeField4.setInputVerifier(new ValueInputVerifier());
        strikeField5.setInputVerifier(new ValueInputVerifier());

        ratioField1.setInputVerifier(new ValueInputVerifier());
        ratioField2.setInputVerifier(new ValueInputVerifier());
        ratioField3.setInputVerifier(new ValueInputVerifier());
        ratioField4.setInputVerifier(new ValueInputVerifier());
        ratioField5.setInputVerifier(new ValueInputVerifier());

        strikeField1.addPropertyChangeListener("value", this);
        strikeField2.addPropertyChangeListener("value", this);
        strikeField3.addPropertyChangeListener("value", this);
        strikeField4.addPropertyChangeListener("value", this);
        strikeField5.addPropertyChangeListener("value", this);

        ratioField1.addPropertyChangeListener("value", this);
        ratioField2.addPropertyChangeListener("value", this);
        ratioField3.addPropertyChangeListener("value", this);
        ratioField4.addPropertyChangeListener("value", this);
        ratioField5.addPropertyChangeListener("value", this);

        strikeField1.setValue("");
        strikeField2.setValue("");
        strikeField3.setValue("");
        strikeField4.setValue("");
        strikeField5.setValue("");

        ratioField1.setValue("");
        ratioField2.setValue("");
        ratioField3.setValue("");
        ratioField4.setValue("");
        ratioField5.setValue("");
    }

    public void setTableModel(PricingCompTableModel tableModel) {
        this.tableModel = tableModel;
    }

//    private void setupStructureOptions() {
//        stuctureOptions = new Vector<String>();
//        stuctureOptions.add("");
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.FENCE.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.STRDL.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.STRNGL.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.CALL.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.CSPRD.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.CFLY.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.CCNDR.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.CTREE.toString());
////        stuctureOptions.add(PointBoxQuoteStrategyTerm.CRATIO.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.PUT.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.PSPRD.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.PFLY.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.PCNDR.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.PTREE.toString());
////        stuctureOptions.add(PointBoxQuoteStrategyTerm.PRATIO.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.IFLY.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.ICNDR.toString());
//        stuctureOptions.add(PointBoxQuoteStrategyTerm.CONV.toString());
////        stuctureOptions.add(PointBoxQuoteStrategyTerm.CSWPN.toString());
////        stuctureOptions.add(PointBoxQuoteStrategyTerm.PSWPN.toString());
//    }

    private void initListeners() {
        initKeyListenerHelper(structureComboBox);

        initKeyListenerHelper(strikeField1);
        initKeyListenerHelper(strikeField2);
        initKeyListenerHelper(strikeField3);
        initKeyListenerHelper(strikeField4);
        initKeyListenerHelper(strikeField5);

        initKeyListenerHelper(callPutComboBox1);
        initKeyListenerHelper(callPutComboBox2);
        initKeyListenerHelper(callPutComboBox3);
        initKeyListenerHelper(callPutComboBox4);
        initKeyListenerHelper(callPutComboBox5);

        initKeyListenerHelper(ratioField1);
        initKeyListenerHelper(ratioField2);
        initKeyListenerHelper(ratioField3);
        initKeyListenerHelper(ratioField4);
        initKeyListenerHelper(ratioField5);

        initMouseListenerHelper(strikeField1);
        initMouseListenerHelper(strikeField2);
        initMouseListenerHelper(strikeField3);
        initMouseListenerHelper(strikeField4);
        initMouseListenerHelper(strikeField5);

        initMouseListenerHelper(ratioField1);
        initMouseListenerHelper(ratioField2);
        initMouseListenerHelper(ratioField3);
        initMouseListenerHelper(ratioField4);
        initMouseListenerHelper(ratioField5);

        initItemListener(callPutComboBox1, PricingCompTableModel.OPTION_1_INDEX);
        initItemListener(callPutComboBox2, PricingCompTableModel.OPTION_2_INDEX);
        initItemListener(callPutComboBox3, PricingCompTableModel.OPTION_3_INDEX);
        initItemListener(callPutComboBox4, PricingCompTableModel.OPTION_4_INDEX);
        initItemListener(callPutComboBox5, PricingCompTableModel.OPTION_5_INDEX);
    }

    private void initKeyListenerHelper(JComponent c) {
        c.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    calculate();
                }
            }
        });
    }

    private void initMouseListenerHelper(JFormattedTextField field) {
        field.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ((JFormattedTextField) evt.getSource()).selectAll();
            }
        });
    }

    private void initItemListener(final JComboBox comboBox, final int option) {
        comboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tableModel.inputCallPuts.put(option, comboBox.getSelectedItem().toString());
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        Object source = evt.getSource();
        double value;

        if (source == strikeField1) {
            value = SwingGlobal.retrieveDoubleFromTextField(strikeField1);
            if (oldStrikeField1 != value) {
                oldStrikeField1 = value;
                tableModel.inputStrikes.put(PricingCompTableModel.OPTION_1_INDEX, value);
                if (value == 0.0) {
                    strikeField1.setValue("");
                } else {
                    strikeField1.setValue(FormatterCommons.format4Dec(oldStrikeField1));
                }
            } else {
                if (value != 0.0) {
                    strikeField1.setValue(FormatterCommons.format4Dec(oldStrikeField1));
                }
            }
        } else if (source == strikeField2) {
            value = SwingGlobal.retrieveDoubleFromTextField(strikeField2);
            if (oldStrikeField2 != value) {
                oldStrikeField2 = value;
                tableModel.inputStrikes.put(PricingCompTableModel.OPTION_2_INDEX, value);
                if (value == 0.0) {
                    strikeField2.setValue("");
                } else {
                    strikeField2.setValue(FormatterCommons.format4Dec(oldStrikeField2));
                }
            } else {
                if (value != 0.0) {
                    strikeField2.setValue(FormatterCommons.format4Dec(oldStrikeField2));
                }
            }
        } else if (source == strikeField3) {
            value = SwingGlobal.retrieveDoubleFromTextField(strikeField3);
            if (oldStrikeField3 != value) {
                oldStrikeField3 = value;
                tableModel.inputStrikes.put(PricingCompTableModel.OPTION_3_INDEX, value);
                if (value == 0.0) {
                    strikeField3.setValue("");
                } else {
                    strikeField3.setValue(FormatterCommons.format4Dec(oldStrikeField3));
                }
            } else {
                if (value != 0.0) {
                    strikeField3.setValue(FormatterCommons.format4Dec(oldStrikeField3));
                }
            }
        } else if (source == strikeField4) {
            value = SwingGlobal.retrieveDoubleFromTextField(strikeField4);
            if (oldStrikeField4 != value) {
                oldStrikeField4 = value;
                tableModel.inputStrikes.put(PricingCompTableModel.OPTION_4_INDEX, value);
                if (value == 0.0) {
                    strikeField4.setValue("");
                } else {
                    strikeField4.setValue(FormatterCommons.format4Dec(oldStrikeField4));
                }
            } else {
                if (value != 0.0) {
                    strikeField4.setValue(FormatterCommons.format4Dec(oldStrikeField4));
                }
            }
        } else if (source == strikeField5) {
            value = SwingGlobal.retrieveDoubleFromTextField(strikeField5);
            if (oldStrikeField5 != value) {
                oldStrikeField5 = value;
                tableModel.inputStrikes.put(PricingCompTableModel.OPTION_5_INDEX, value);
                if (value == 0.0) {
                    strikeField5.setValue("");
                } else {
                    strikeField5.setValue(FormatterCommons.format4Dec(oldStrikeField5));
                }
            } else {
                if (value != 0.0) {
                    strikeField5.setValue(FormatterCommons.format4Dec(oldStrikeField5));
                }
            }
        } else if (source == ratioField1) {
            value = SwingGlobal.retrieveDoubleFromTextField(ratioField1);
            if (oldRatioField1 != value) {
                oldRatioField1 = value;
                tableModel.inputRatios.put(PricingCompTableModel.OPTION_1_INDEX, value);
                if (value == 0.0) {
                    ratioField1.setValue("");
                } else {
                    ratioField1.setValue(FormatterCommons.format1Dec(oldRatioField1));
                }
            } else {
                if (value != 0.0) {
                    ratioField1.setValue(FormatterCommons.format1Dec(oldRatioField1));
                }
            }
        } else if (source == ratioField2) {
            value = SwingGlobal.retrieveDoubleFromTextField(ratioField2);
            if (oldRatioField2 != value) {
                oldRatioField2 = value;
                tableModel.inputRatios.put(PricingCompTableModel.OPTION_2_INDEX, value);
                if (value == 0.0) {
                    ratioField2.setValue("");
                } else {
                    ratioField2.setValue(FormatterCommons.format1Dec(oldRatioField2));
                }
            } else {
                if (value != 0.0) {
                    ratioField2.setValue(FormatterCommons.format1Dec(oldRatioField2));
                }
            }
        } else if (source == ratioField3) {
            value = SwingGlobal.retrieveDoubleFromTextField(ratioField3);
            if (oldRatioField3 != value) {
                oldRatioField3 = value;
                tableModel.inputRatios.put(PricingCompTableModel.OPTION_3_INDEX, value);
                if (value == 0.0) {
                    ratioField3.setValue("");
                } else {
                    ratioField3.setValue(FormatterCommons.format1Dec(oldRatioField3));
                }
            } else {
                if (value != 0.0) {
                    ratioField3.setValue(FormatterCommons.format1Dec(oldRatioField3));
                }
            }
        } else if (source == ratioField4) {
            value = SwingGlobal.retrieveDoubleFromTextField(ratioField4);
            if (oldRatioField4 != value) {
                oldRatioField4 = value;
                tableModel.inputRatios.put(PricingCompTableModel.OPTION_4_INDEX, value);
                if (value == 0.0) {
                    ratioField4.setValue("");
                } else {
                    ratioField4.setValue(FormatterCommons.format1Dec(oldRatioField4));
                }
            } else {
                if (value != 0.0) {
                    ratioField4.setValue(FormatterCommons.format1Dec(oldRatioField4));
                }
            }
        } else if (source == ratioField5) {
            value = SwingGlobal.retrieveDoubleFromTextField(ratioField5);
            if (oldRatioField5 != value) {
                oldRatioField5 = value;
                tableModel.inputRatios.put(PricingCompTableModel.OPTION_5_INDEX, value);
                if (value == 0.0) {
                    ratioField5.setValue("");
                } else {
                    ratioField5.setValue(FormatterCommons.format1Dec(oldRatioField5));
                }
            } else {
                if (value != 0.0) {
                    ratioField5.setValue(FormatterCommons.format1Dec(oldRatioField5));
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify t
     * his code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        structureLabel = new javax.swing.JLabel();
        structureComboBox = new javax.swing.JComboBox(stuctureOptions);
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        calculateButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        strikeLabel = new javax.swing.JLabel();
        strikeField1 = new javax.swing.JFormattedTextField();
        strikeField2 = new javax.swing.JFormattedTextField();
        strikeField3 = new javax.swing.JFormattedTextField();
        strikeField4 = new javax.swing.JFormattedTextField();
        strikeField5 = new javax.swing.JFormattedTextField();
        callPutLabel = new javax.swing.JLabel();
        callPutComboBox1 = new javax.swing.JComboBox(callPutOptions);
        callPutComboBox2 = new javax.swing.JComboBox(callPutOptions);
        callPutComboBox3 = new javax.swing.JComboBox(callPutOptions);
        callPutComboBox4 = new javax.swing.JComboBox(callPutOptions);
        callPutComboBox5 = new javax.swing.JComboBox(callPutOptions);
        ratioLabel = new javax.swing.JLabel();
        ratioField1 = new javax.swing.JFormattedTextField();
        ratioField2 = new javax.swing.JFormattedTextField();
        ratioField3 = new javax.swing.JFormattedTextField();
        ratioField4 = new javax.swing.JFormattedTextField();
        ratioField5 = new javax.swing.JFormattedTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new java.awt.Dimension(600, 120));
        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridLayout(4, 6, 2, 5));

        structureLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        structureLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        structureLabel.setText("Structure:     ");
        jPanel2.add(structureLabel);

        structureComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                structureComboBoxActionPerformed(evt);
            }
        });
        jPanel2.add(structureComboBox);
        jPanel2.add(jLabel1);
        jPanel2.add(jLabel2);

        calculateButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        calculateButton.setText("Calculate");
        calculateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculateButtonActionPerformed(evt);
            }
        });
        jPanel2.add(calculateButton);

        clearButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        jPanel2.add(clearButton);

        strikeLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        strikeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        strikeLabel.setText("Strike:     ");
        jPanel2.add(strikeLabel);
        jPanel2.add(strikeField1);
        jPanel2.add(strikeField2);
        jPanel2.add(strikeField3);
        jPanel2.add(strikeField4);
        jPanel2.add(strikeField5);

        callPutLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        callPutLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        callPutLabel.setText("Call/Put:     ");
        jPanel2.add(callPutLabel);

        jPanel2.add(callPutComboBox1);

        jPanel2.add(callPutComboBox2);

        jPanel2.add(callPutComboBox3);

        jPanel2.add(callPutComboBox4);

        jPanel2.add(callPutComboBox5);

        ratioLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ratioLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        ratioLabel.setText("Ratio:     ");
        jPanel2.add(ratioLabel);
        jPanel2.add(ratioField1);
        jPanel2.add(ratioField2);
        jPanel2.add(ratioField3);
        jPanel2.add(ratioField4);
        jPanel2.add(ratioField5);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void structureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_structureComboBoxActionPerformed

        String structure = structureComboBox.getSelectedItem().toString();

        if (structure.isEmpty()) {
            enableComponentSelections(true, true, true, true, true);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.FENCE.toString())) {
            clearCallPutsAndRatios();
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            if (SwingGlobal.retrieveDoubleFromTextField(strikeField1) < SwingGlobal.retrieveDoubleFromTextField(strikeField2)) {
                callPutComboBox1.setSelectedIndex(2);
                callPutComboBox2.setSelectedIndex(1);
                ratioField1.setValue(1.0);
                ratioField2.setValue(-1.0);
            } else {
                callPutComboBox1.setSelectedIndex(1);
                callPutComboBox2.setSelectedIndex(2);
                ratioField1.setValue(-1.0);
                ratioField2.setValue(1.0);
            }
            
        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.STRDL.toString())) {
            clearCallPutsAndRatios();
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            double value = SwingGlobal.retrieveDoubleFromTextField(strikeField1);
            if (value != 0.0) {
                strikeField2.setValue(value);
            }
            callPutComboBox1.setSelectedIndex(1);
            callPutComboBox2.setSelectedIndex(2);
            ratioField1.setValue(1.0);
            ratioField2.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.STRNGL.toString())) {
            clearCallPutsAndRatios();
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            if (SwingGlobal.retrieveDoubleFromTextField(strikeField1) < SwingGlobal.retrieveDoubleFromTextField(strikeField2)) {
                callPutComboBox1.setSelectedIndex(2);
                callPutComboBox2.setSelectedIndex(1);
            } else {
                callPutComboBox1.setSelectedIndex(1);
                callPutComboBox2.setSelectedIndex(2);
            }
            ratioField1.setValue(1.0);
            ratioField2.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CALL.toString())) {
            clearCallPutsAndRatios();
            strikeField2.setValue("");
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, false, false, false, false);

            callPutComboBox1.setSelectedIndex(1);
            ratioField1.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CSPRD.toString())) {
            clearCallPutsAndRatios();
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            callPutComboBox1.setSelectedIndex(1);
            callPutComboBox2.setSelectedIndex(1);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CFLY.toString())) {
            clearCallPutsAndRatios();
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            callPutComboBox1.setSelectedIndex(1);
            callPutComboBox2.setSelectedIndex(1);
            callPutComboBox3.setSelectedIndex(1);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-2.0);
            ratioField3.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CCNDR.toString())) {
            clearCallPutsAndRatios();
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            callPutComboBox1.setSelectedIndex(1);
            callPutComboBox2.setSelectedIndex(1);
            callPutComboBox3.setSelectedIndex(1);
            callPutComboBox4.setSelectedIndex(1);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);
            ratioField3.setValue(-1.0);
            ratioField4.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CTREE.toString())) {
            clearCallPutsAndRatios();
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            //buy low, sell mid, sell high
            callPutComboBox1.setSelectedIndex(1);
            callPutComboBox2.setSelectedIndex(1);
            callPutComboBox3.setSelectedIndex(1);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);
            ratioField3.setValue(-1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(strikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(strikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(strikeField3);
            if (strike2 < strike1 && strike2 < strike3) {
                ratioField1.setValue(-1.0);
                ratioField2.setValue(1.0);
                ratioField3.setValue(-1.0);
            } else if (strike3 < strike1 && strike3 < strike2) {
                ratioField1.setValue(-1.0);
                ratioField2.setValue(-1.0);
                ratioField3.setValue(1.0);
            }

//        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CRATIO.toString())) {
//            clearCallPutsAndRatios();
//            strikeField3.setValue("");
//            strikeField4.setValue("");
//            strikeField5.setValue("");
//
//            enableComponentSelections(true, true, false, false, false);
//
//            callPutComboBox1.setSelectedIndex(1);
//            callPutComboBox2.setSelectedIndex(1);
//            ratioField1.setValue(1.0);
//            ratioField2.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PUT.toString())) {
            clearCallPutsAndRatios();
            strikeField2.setValue("");
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, false, false, false, false);

            callPutComboBox1.setSelectedIndex(2);
            ratioField1.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PSPRD.toString())) {
            clearCallPutsAndRatios();
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            callPutComboBox1.setSelectedIndex(2);
            callPutComboBox2.setSelectedIndex(2);
            ratioField1.setValue(-1.0);
            ratioField2.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PFLY.toString())) {
            clearCallPutsAndRatios();
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            callPutComboBox1.setSelectedIndex(2);
            callPutComboBox2.setSelectedIndex(2);
            callPutComboBox3.setSelectedIndex(2);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-2.0);
            ratioField3.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PCNDR.toString())) {
            clearCallPutsAndRatios();
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            callPutComboBox1.setSelectedIndex(2);
            callPutComboBox2.setSelectedIndex(2);
            callPutComboBox3.setSelectedIndex(2);
            callPutComboBox4.setSelectedIndex(2);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);
            ratioField3.setValue(-1.0);
            ratioField4.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PTREE.toString())) {
            clearCallPutsAndRatios();
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            //buy high, sell mid, sell low
            callPutComboBox1.setSelectedIndex(2);
            callPutComboBox2.setSelectedIndex(2);
            callPutComboBox3.setSelectedIndex(2);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);
            ratioField3.setValue(-1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(strikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(strikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(strikeField3);
            if (strike2 > strike1 && strike2 > strike3) {
                ratioField1.setValue(-1.0);
                ratioField2.setValue(1.0);
                ratioField3.setValue(-1.0);
            } else if (strike3 > strike1 && strike3 > strike2) {
                ratioField1.setValue(-1.0);
                ratioField2.setValue(-1.0);
                ratioField3.setValue(1.0);
            }
//
//        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PRATIO.toString())) {
//            clearCallPutsAndRatios();
//            strikeField3.setValue("");
//            strikeField4.setValue("");
//            strikeField5.setValue("");
//
//            enableComponentSelections(true, true, false, false, false);
//
//            callPutComboBox1.setSelectedIndex(2);
//            callPutComboBox2.setSelectedIndex(2);
//            ratioField1.setValue(1.0);
//            ratioField2.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.IFLY.toString())) {
            clearCallPutsAndRatios();
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            //low is +put
            //mid is -put
            //mid is -call
            //high is +call
            callPutComboBox1.setSelectedIndex(2);
            callPutComboBox2.setSelectedIndex(2);
            callPutComboBox3.setSelectedIndex(1);
            callPutComboBox4.setSelectedIndex(1);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);
            ratioField3.setValue(-1.0);
            ratioField4.setValue(1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(strikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(strikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(strikeField3);
            double strike4 = SwingGlobal.retrieveDoubleFromTextField(strikeField4);
            if (strike1 <= strike2 && strike2 == strike3 && strike3 <= strike4) {
            } else if (strike1 >= strike2 && strike2 == strike3 && strike3 >= strike4) {
                callPutComboBox1.setSelectedIndex(1);
                callPutComboBox2.setSelectedIndex(1);
                callPutComboBox3.setSelectedIndex(2);
                callPutComboBox4.setSelectedIndex(2);
                ratioField1.setValue(1.0);
                ratioField2.setValue(-1.0);
                ratioField3.setValue(-1.0);
                ratioField4.setValue(1.0);
            } else if (strike1 == strike2) {
            } else if (strike1 == strike3) {
            } else if (strike1 == strike4) {
            } else if (strike2 == strike4) {
            } else if (strike3 == strike4) {
            }

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.ICNDR.toString())) {
            clearCallPutsAndRatios();
            strikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            //low is +call
            //2nd low is -call
            //2nd high is -put
            //high is +put
            callPutComboBox1.setSelectedIndex(1);
            callPutComboBox2.setSelectedIndex(1);
            callPutComboBox3.setSelectedIndex(2);
            callPutComboBox4.setSelectedIndex(2);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);
            ratioField3.setValue(-1.0);
            ratioField4.setValue(1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(strikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(strikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(strikeField3);
            double strike4 = SwingGlobal.retrieveDoubleFromTextField(strikeField4);

            //KEEP WORKING ON THIS
            if (strike1 <= strike2 && strike2 <= strike3 && strike3 <= strike4) {
            } else if (strike1 >= strike2 && strike2 >= strike3 && strike3 >= strike4) {
                callPutComboBox1.setSelectedIndex(2);
                callPutComboBox2.setSelectedIndex(2);
                callPutComboBox3.setSelectedIndex(1);
                callPutComboBox4.setSelectedIndex(1);
                ratioField1.setValue(1.0);
                ratioField2.setValue(-1.0);
                ratioField3.setValue(-1.0);
                ratioField4.setValue(1.0);
            } else if (strike2 <= strike1 && strike1 <= strike3 && strike3 <= strike4) {
                callPutComboBox1.setSelectedIndex(1);
                callPutComboBox2.setSelectedIndex(1);
                callPutComboBox3.setSelectedIndex(2);
                callPutComboBox4.setSelectedIndex(2);
                ratioField1.setValue(-1.0);
                ratioField2.setValue(1.0);
                ratioField3.setValue(-1.0);
                ratioField4.setValue(1.0);
            } else if (strike2 >= strike1 && strike1 >= strike3 && strike3 >= strike4) {
                callPutComboBox1.setSelectedIndex(2);
                callPutComboBox2.setSelectedIndex(2);
                callPutComboBox3.setSelectedIndex(1);
                callPutComboBox4.setSelectedIndex(1);
                ratioField1.setValue(-1.0);
                ratioField2.setValue(1.0);
                ratioField3.setValue(-1.0);
                ratioField4.setValue(1.0);
            } else if (strike1 <= strike2 && strike2 <= strike4 && strike4 <= strike3) {
                callPutComboBox1.setSelectedIndex(1);
                callPutComboBox2.setSelectedIndex(1);
                callPutComboBox3.setSelectedIndex(2);
                callPutComboBox4.setSelectedIndex(2);
                ratioField1.setValue(1.0);
                ratioField2.setValue(-1.0);
                ratioField3.setValue(1.0);
                ratioField4.setValue(-1.0);
            } else if (strike1 <= strike3 && strike3 <= strike2 && strike2 <= strike4) {
                callPutComboBox1.setSelectedIndex(1);
                callPutComboBox2.setSelectedIndex(2);
                callPutComboBox3.setSelectedIndex(1);
                callPutComboBox4.setSelectedIndex(2);
                ratioField1.setValue(1.0);
                ratioField2.setValue(-1.0);
                ratioField3.setValue(-1.0);
                ratioField4.setValue(1.0);
            }

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CONV.toString())) {
            clearCallPutsAndRatios();
            strikeField3.setValue("");
            strikeField4.setValue("");
            strikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            callPutComboBox1.setSelectedIndex(2);
            callPutComboBox2.setSelectedIndex(1);
            ratioField1.setValue(1.0);
            ratioField2.setValue(-1.0);
//
//        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CSWPN.toString())) {
//            clearCallPutsAndRatios();
//            strikeField2.setValue("");
//            strikeField3.setValue("");
//            strikeField4.setValue("");
//            strikeField5.setValue("");
//
//            enableComponentSelections(true, false, false, false, false);
//
//            callPutComboBox1.setSelectedIndex(1);
//            ratioField1.setValue(1.0);
//
//        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PSWPN.toString())) {
//            clearCallPutsAndRatios();
//            strikeField2.setValue("");
//            strikeField3.setValue("");
//            strikeField4.setValue("");
//            strikeField5.setValue("");
//
//            enableComponentSelections(true, false, false, false, false);
//
//            callPutComboBox1.setSelectedIndex(2);
//            ratioField1.setValue(1.0);
        }
}//GEN-LAST:event_structureComboBoxActionPerformed

    private void clearCallPutsAndRatios() {
        ratioField1.setValue("");
        ratioField2.setValue("");
        ratioField3.setValue("");
        ratioField4.setValue("");
        ratioField5.setValue("");

        callPutComboBox1.setSelectedIndex(0);
        callPutComboBox2.setSelectedIndex(0);
        callPutComboBox3.setSelectedIndex(0);
        callPutComboBox4.setSelectedIndex(0);
        callPutComboBox5.setSelectedIndex(0);
    }

    private void enableComponentSelections(boolean choice1, boolean choice2, boolean choice3, boolean choice4, boolean choice5) {
        strikeField1.setEnabled(choice1);
        ratioField1.setEnabled(choice1);
        callPutComboBox1.setEnabled(choice1);

        strikeField2.setEnabled(choice2);
        ratioField2.setEnabled(choice2);
        callPutComboBox2.setEnabled(choice2);

        strikeField3.setEnabled(choice3);
        ratioField3.setEnabled(choice3);
        callPutComboBox3.setEnabled(choice3);

        strikeField4.setEnabled(choice4);
        ratioField4.setEnabled(choice4);
        callPutComboBox4.setEnabled(choice4);

        strikeField5.setEnabled(choice5);
        ratioField5.setEnabled(choice5);
        callPutComboBox5.setEnabled(choice5);
    }

    private void calculateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calculateButtonActionPerformed
        tableModel.reloadData();
        calculate();
    }//GEN-LAST:event_calculateButtonActionPerformed

    private void calculate() {
        if (SwingUtilities.isEventDispatchThread()) {
            tableModel.fireTableDataChanged();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tableModel.fireTableDataChanged();
                }
            });
        }
    }

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed

        enableComponentSelections(true, true, true, true, true);

        structureComboBox.setSelectedIndex(0);

        strikeField1.setValue("");
        strikeField2.setValue("");
        strikeField3.setValue("");
        strikeField4.setValue("");
        strikeField5.setValue("");

        ratioField1.setValue("");
        ratioField2.setValue("");
        ratioField3.setValue("");
        ratioField4.setValue("");
        ratioField5.setValue("");

        callPutComboBox1.setSelectedIndex(0);
        callPutComboBox2.setSelectedIndex(0);
        callPutComboBox3.setSelectedIndex(0);
        callPutComboBox4.setSelectedIndex(0);
        callPutComboBox5.setSelectedIndex(0);
        
        if (SwingUtilities.isEventDispatchThread()) {
            tableModel.fireTableDataChanged();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tableModel.fireTableDataChanged();
                }
            });
        }        
    }//GEN-LAST:event_clearButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calculateButton;
    private javax.swing.JComboBox callPutComboBox1;
    private javax.swing.JComboBox callPutComboBox2;
    private javax.swing.JComboBox callPutComboBox3;
    private javax.swing.JComboBox callPutComboBox4;
    private javax.swing.JComboBox callPutComboBox5;
    private javax.swing.JLabel callPutLabel;
    private javax.swing.JButton clearButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JFormattedTextField ratioField1;
    private javax.swing.JFormattedTextField ratioField2;
    private javax.swing.JFormattedTextField ratioField3;
    private javax.swing.JFormattedTextField ratioField4;
    private javax.swing.JFormattedTextField ratioField5;
    private javax.swing.JLabel ratioLabel;
    private javax.swing.JFormattedTextField strikeField1;
    private javax.swing.JFormattedTextField strikeField2;
    private javax.swing.JFormattedTextField strikeField3;
    private javax.swing.JFormattedTextField strikeField4;
    private javax.swing.JFormattedTextField strikeField5;
    private javax.swing.JLabel strikeLabel;
    private javax.swing.JComboBox structureComboBox;
    private javax.swing.JLabel structureLabel;
    // End of variables declaration//GEN-END:variables

    class ValueInputVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {

            String value = ((JFormattedTextField) input).getText();
            if (value.isEmpty()) {
                input.setBackground(Color.WHITE);
                return true;
            } else {
                try {
                    Double.parseDouble(value);
                    input.setBackground(Color.WHITE);
                    return true;
                } catch (Exception ex) {
                    input.setBackground(Color.RED);
                    return false;
                }
            }
        }
    }
}
