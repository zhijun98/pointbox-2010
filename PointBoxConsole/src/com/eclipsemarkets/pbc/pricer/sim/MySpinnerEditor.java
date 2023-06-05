/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pricer.commons.FormatterCommons;
import java.awt.Component;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.EventObject;
import javax.swing.*;

/**
 *
 * @author Fang.Bao
 */
public class MySpinnerEditor extends DefaultCellEditor
{
        JSpinner spinner;
        JSpinner.DefaultEditor editor;
        JTextField textField;
        boolean valueSet;
        Object value;
        int digit;

        // Initializes the spinner.
        public MySpinnerEditor (Object value,int digit) {
            super(new JTextField());
            this.value=value;
            this.digit=digit;
            initSpinner();
            
            editor = ((JSpinner.DefaultEditor)spinner.getEditor());
            textField = editor.getTextField();
            textField.addFocusListener( new FocusListener() {
                @Override
                public void focusGained( FocusEvent fe ) {
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            if ( valueSet ) {
                                textField.setCaretPosition(1);
                            }
                        }
                    });
                }
                @Override
                public void focusLost( FocusEvent fe ) {
                }
            });
            textField.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed( ActionEvent ae ) {
                    stopCellEditing();
                }
            });
        }

        // Prepares the spinner component and returns it.
        @Override
        public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column
        ) {
            if ( valueSet ) {
                //System.out.println("spinner.getValue() >>> " + spinner.getValue());
            }else{
                spinner.setValue(Double.parseDouble(value+""));
            }
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    textField.requestFocus();
                }
            });
            return spinner;
        }

    @Override
        public boolean isCellEditable( EventObject eo ) {
            if ( eo instanceof KeyEvent ) {
                KeyEvent ke = (KeyEvent)eo;
                textField.setText(String.valueOf(ke.getKeyChar()));

                valueSet = true;
            } else {
                valueSet = false;
            }
            return true;
        }

    // Returns the spinners current value.
    @Override
    public Object getCellEditorValue() {
        return spinner.getValue();
    }

    @Override
        public boolean stopCellEditing() {
            try {
                editor.commitEdit();
                spinner.commitEdit();
            } catch ( java.text.ParseException e ) {
                JOptionPane.showMessageDialog(null,
                    "Invalid value, discarding.");
            }
            return super.stopCellEditing();
        }
        
        private void initSpinner(){
            double min = -100.0000;
            double initialValue = 0.0000;
            double max = 100.0000;
            double stepSize = 0.0001;
            if(digit==3){
                 min = -100.000;
                 initialValue = 0.000;
                 max = 100.000;
                 stepSize = 0.005;
                 
                SpinnerNumberModel model = new SpinnerNumberModel(initialValue, min, max, stepSize);
                spinner=new JSpinner(model);
                JSpinner.NumberEditor myEditor = (JSpinner.NumberEditor)spinner.getEditor();
                DecimalFormat format = myEditor.getFormat();
                format.setMinimumFractionDigits(3);
                myEditor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
                if (value != null){
                    spinner.setValue(Double.parseDouble(FormatterCommons.format3Dec((DataGlobal.convertToDouble(value+"")))));
                }
                return;
                
            }
            SpinnerNumberModel model = new SpinnerNumberModel(initialValue, min, max, stepSize);
            spinner=new JSpinner(model);
            JSpinner.NumberEditor myEditor = (JSpinner.NumberEditor)spinner.getEditor();
            DecimalFormat format = myEditor.getFormat();
            format.setMinimumFractionDigits(4);
            myEditor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
            spinner.setValue(Double.parseDouble(FormatterCommons.format4Dec(value)));
        }         
}
