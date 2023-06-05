/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

/**
 *
 * @author Zhijun Zhang, date & time: Dec 22, 2013 - 5:00:24 PM
 */
abstract class FloatingFrameCheckAbstractItem {
    private boolean isSelected = true; //default value is true

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }
}
