/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.properties;

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
 * PointBoxSoundProperties
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 16, 2011 at 9:56:57 PM
 */
public class PointBoxSoundProperties {

    private final String propTopic;
    private final String propFileName;

    private Properties prop;
    /**
     * bad messages
     */
    private HashMap<String, String> sounds;

    private static final Logger logger = Logger.getLogger(PointBoxSoundProperties.class.getName());

    private static PointBoxSoundProperties self;
    static{
        self = null;
    }

    private PointBoxSoundProperties() {
        propTopic = "PointBoxSound";
        propFileName = propTopic + ".properties";

        sounds = new HashMap<String, String>();

        prop = new Properties();
        try {
            prop.load(new FileInputStream(propFileName));
            Set<Entry<Object, Object>> entry = prop.entrySet();
            Entry<Object, Object> entryObj;
            Iterator<Entry<Object, Object>> itr = entry.iterator();
            while(itr.hasNext()){
                entryObj = itr.next();
                sounds.put((String)entryObj.getKey(), (String)entryObj.getValue());//add((String)itr.next());
            }
        } catch (FileNotFoundException ex) {
            createProperties();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static PointBoxSoundProperties getSingleton(){
        if (self == null){
            self = new PointBoxSoundProperties();
        }
        return self;
    }

    private void createProperties() {
        prop.setProperty("enableSound", "false");
        prop.setProperty("jReceivedIMSound", "imrcv.wav");
        prop.setProperty("jSentIMSound", "imrcv.wav");
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void addProperties(String key, String value) {
        /*if (sounds.containsKey(key)){
            sounds.remove(key);
        }*/
        sounds.put(key, value);
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
        sounds.remove(key);
        prop.remove(key);
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized boolean isSound(String key) {
        return sounds.containsKey(key);
    }

    public synchronized HashMap getSoundSetting(){
        return sounds;
    }
    public synchronized void saveSettings(HashMap settings){
        for(int i=0; i < settings.size(); i++){
            addProperties((String)settings.keySet().toArray()[i], (String)settings.values().toArray()[i]);
        }
    }
}//PointBoxSoundProperties

