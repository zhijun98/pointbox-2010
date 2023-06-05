/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.properties;

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
 * PointBoxTabFlashProperties
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 16, 2011 at 9:57:49 PM
 */
public class PointBoxTabFlashProperties {

    private final String propTopic;
    private final String propFileName;

    private Properties prop;
    /**
     * bad messages
     */
    private HashMap<String, String> tabFlash;

    private static final Logger logger = Logger.getLogger(PointBoxTabFlashProperties.class.getName());

    private static PointBoxTabFlashProperties self;
    static{
        self = null;
    }

    private PointBoxTabFlashProperties() {
        propTopic = "PointBoxTabFlash";
        propFileName = propTopic + ".properties";

        tabFlash = new HashMap<String, String>();

        prop = new Properties();
        try {
            prop.load(new FileInputStream(propFileName));
            Set<Entry<Object, Object>> entry = prop.entrySet();
            Entry<Object, Object> entryObj;
            Iterator<Entry<Object, Object>> itr = entry.iterator();
            while(itr.hasNext()){
                entryObj = itr.next();
                tabFlash.put((String)entryObj.getKey(), (String)entryObj.getValue());//add((String)itr.next());
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
        tabFlash.put("flashingTabForeground", String.valueOf(Color.RED.getRGB()));
        prop.setProperty("flashingTabBackground", String.valueOf(Color.BLUE.getRGB()));
        tabFlash.put("flashingTabBackground", String.valueOf(Color.BLUE.getRGB()));
        prop.setProperty("flashingFrequency", "10");
        tabFlash.put("flashingFrequency", "10");
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
        tabFlash.put(key, value);
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
        tabFlash.remove(key);
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
        return tabFlash.containsKey(key);
    }

    public synchronized HashMap gettabFlashetting(){
        return tabFlash;
    }
    public synchronized void saveSettings(HashMap settings){
        for(int i=0; i < settings.size(); i++){
            addProperties((String)settings.keySet().toArray()[i], (String)settings.values().toArray()[i]);
        }
    }


}//PointBoxTabFlashProperties

