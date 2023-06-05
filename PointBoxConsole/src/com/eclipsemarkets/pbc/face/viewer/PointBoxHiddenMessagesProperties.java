/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.viewer;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
public class PointBoxHiddenMessagesProperties {

    private final String propTopic;
    private final String propFileName;

    private Properties prop;
    /**
     * bad messages
     */
    private HashMap<Long, String> badMessages;

    private static final Logger logger = Logger.getLogger(PointBoxHiddenMessagesProperties.class.getName());

    private static PointBoxHiddenMessagesProperties self;
    static{
        self = null;
    }

    private PointBoxHiddenMessagesProperties(IPbcKernel kernel) {
        propTopic = "PointBoxHiddenMessages";
        IGatewayConnectorBuddy pointBoxLoginUser = kernel.getPointBoxLoginUser();
        if (pointBoxLoginUser == null){
            propFileName = propTopic + "_unknown_user.properties";
        }else{
            propFileName = propTopic + "_" +
                           pointBoxLoginUser.getIMServerType() + "_" +
                           pointBoxLoginUser.getIMScreenName() + ".properties";
        }

        badMessages = new HashMap<Long, String>();

        prop = new Properties();
        try {
            prop.load(new FileInputStream(propFileName));
            Set<Entry<Object, Object>> entry = prop.entrySet();
            Entry<Object, Object> entryObj;
            Iterator<Entry<Object, Object>> itr = entry.iterator();
            while(itr.hasNext()){
                entryObj = itr.next();
                badMessages.put(Long.valueOf((String)entryObj.getValue()), (String)entryObj.getKey());//add((String)itr.next());
            }
        } catch (FileNotFoundException ex) {
            createProperties();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static PointBoxHiddenMessagesProperties getSingleton(IPbcKernel kernel){
        if (self == null){
            self = new PointBoxHiddenMessagesProperties(kernel);
        }
        return self;
    }

    private void createProperties() {
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void addProperties(String value, Long timeStamp) {
        if (badMessages.containsValue(value)){
            return;
        }
        badMessages.put(timeStamp, value);
        prop.setProperty(value, timeStamp.toString());
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void removeProperties(String value){
        badMessages.values().remove(value);//remove(value);
        prop.remove(value);
        try {
            FileOutputStream pf = new FileOutputStream(propFileName);
            prop.store(pf, propTopic);
            pf.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized boolean isHiddenMessage(String message) {
        return badMessages.containsValue(message);
    }

    public synchronized ArrayList getHiddenMessageMenu() {
        ArrayList timeStamp = Collections.list(Collections.enumeration(badMessages.keySet()));
        ArrayList messages = new ArrayList();
        int indexMin = 0;
        int sizeOfMenu = 10;
        if(timeStamp != null && !(timeStamp.isEmpty()))
         {
            Collections.sort(timeStamp);
            if(timeStamp.size() > sizeOfMenu)
            {
                indexMin = timeStamp.size()-sizeOfMenu;
            }
            for(int index = timeStamp.size()-1; index >= indexMin; index--)
            {
                messages.add(badMessages.get((Long)timeStamp.get(index)));
            }
        }
        return messages;
    }

}//PointBoxHiddenMessagesProperties


