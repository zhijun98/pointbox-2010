/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.preference;

import javax.swing.JPanel;

/**
 * IPreferencePanel.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 23, 2010, 10:55:05 PM
 */
public interface IPreferencePanel {

    public JPanel getBasePanel();

//    public void expandPreferencePanel();

    /**
     * Populate all the settings data onto every component setting panel when the preference window is activated
     */
    public void populateSettings();

    /**
     * Update all the settings according to the state of every component setting panel when the preference window is closing
     */
    public void updateSettings();
}
