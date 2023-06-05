/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer.sim;

import java.awt.Color;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

/**
 *
 * @author Zhijun Zhang, date & time: Oct 31, 2014 - 12:42:06 PM
 */
class SimFormattedValueInputVerifier  extends InputVerifier {

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
