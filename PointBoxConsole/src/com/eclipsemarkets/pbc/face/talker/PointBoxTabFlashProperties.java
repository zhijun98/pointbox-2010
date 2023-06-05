/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PointBoxHiddenMessagesProperties
 * <P>
 * {Insert class description here}
 * <P>
 * @author Andal FeQuiere Jr
 * Created on Nov 16, 2010 at 11:48:35 PM
 */
public class PointBoxTabFlashProperties {

    private final String propTopic;
    private final String propFileName;

    private Properties prop;
    /**
     * bad messages
     */
    private HashMap<String, String> properties;

    private static final Logger logger = Logger.getLogger(PointBoxTabFlashProperties.class.getName());

    private static PointBoxTabFlashProperties self;
    static{
        self = null;
    }

    private PointBoxTabFlashProperties() {
        propTopic = "PointBoxTabFlash";
        propFileName = propTopic + ".properties";

        properties = new HashMap<String, String>();

        prop = new Properties();
        try {
            prop.load(new FileInputStream(propFileName));
            Set<Entry<Object, Object>> entry = prop.entrySet();
            Entry<Object, Object> entryObj;
            Iterator<Entry<Object, Object>> itr = entry.iterator();
            while(itr.hasNext()){
                entryObj = itr.next();
                properties.put((String)entryObj.getKey(), (String)entryObj.getValue());//add((String)itr.next());
            }
        } catch (FileNotFoundException ex) {
            createProperties();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static PointBoxTabFlashProperties getSingleton(){
        if (self == null){
            self = new PointBoxTabFlashProperties();
        }
        return self;
    }

    private void createProperties() {
        prop.setProperty("flashingTabForeground", String.valueOf(Color.RED.getRGB()));
        properties.put("flashingTabForeground", String.valueOf(Color.RED.getRGB()));
        prop.setProperty("flashingTabBackground", String.valueOf(Color.BLUE.getRGB()));
        properties.put("flashingTabBackground", String.valueOf(Color.BLUE.getRGB()));
        prop.setProperty("flashingFrequency", "10");
        properties.put("flashingFrequency", "10");
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void addProperties(String key, String value) {
        /*if (tabFlash.containsKey(key)){
            tabFlash.remove(key);
        }*/
        properties.put(key, value);
        prop.setProperty(key, value);
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void removeProperties(String key){
        properties.remove(key);
        prop.remove(key);
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized boolean isTabFlash(String key) {
        return properties.containsKey(key);
    }

    public synchronized HashMap gettabFlashetting(){
        return properties;
    }
    public synchronized void saveSettings(HashMap settings){
        for(int i=0; i < settings.size(); i++){
            addProperties((String)settings.keySet().toArray()[i], (String)settings.values().toArray()[i]);
        }
    }
}//PointBoxHiddenMessagesProperties


