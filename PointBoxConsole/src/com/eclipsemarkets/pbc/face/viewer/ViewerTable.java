/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.viewer;

import java.awt.Rectangle;
import java.util.logging.Logger;
import javax.swing.JTable;

/**
 *
 * @author Zhijun Zhang
 */
public class ViewerTable extends JTable {
    private static final Logger logger;
    static {
        logger = Logger.getLogger(ViewerTable.class.getName());
    }

    @Override
    public void scrollRectToVisible(Rectangle aRect) {
        if(getAutoscrolls()) {
            super.scrollRectToVisible(aRect);
        }
    }
}
