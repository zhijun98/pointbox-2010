/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.preference;

/**
 * IPreferenceComponentPanel
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 13, 2011 at 11:14:14 AM
 */
public interface IPreferenceComponentPanel {
    
    /**
     * Populate settings from the memory to the preference component panel when 
     * the preference dialog is displayed
     */
    public void populateSettings();
    
    /**
     * Update settings in the memory according to the state of preference component panel 
     * when the preference dialog is closed
     */
    public void updateSettings();

}
