/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

/**
 *
 * @author Zhijun Zhang, date & time: Apr 22, 2014 - 7:14:25 PM
 */
public interface IMsAccessPreferencePanel extends IPreferencePanel{
    
    public void notifyMsAccessInterrupted();

    /**
     * This method is called by face.unload()
     */
    public void unloadPreferencePanel();

}
