/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer.sim;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Zhijun Zhang, date & time: Sep 25, 2014 - 10:31:08 AM
 */
public abstract class AbstractPbcCurveTableModel extends AbstractTableModel{
    
    void displayCurveWarningMessage(){
        displayCurveWarningMessage("Please check curve files which may be corrupted. This curve window cannot load it correctly.");
    }

    void displayCurveWarningMessage(final String msg){
        if (SwingUtilities.isEventDispatchThread()){
            displayCurveWarningMessageHelper(msg);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayCurveWarningMessageHelper(msg);
                }
            });
        }
    }

    private void displayCurveWarningMessageHelper(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }
    
}
