/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Purpose of this class is to speed up "search".
 * <p/>
 * <distGroup.getGroupName(), <buddy.getIMUniqueName(), DnDBuddyTreeNode>> <br/>
 * Notice: distGroup could be different from buddy's group because, in NON-regular 
 * buddy list, buddy could belong to different GroupNode (i.e. distGroup)
 * 
 * @author Zhijun Zhang, date & time: Dec 13, 2013 - 9:12:51 AM
 */
public class PbcDndBuddyTreeBuddyNodeMap extends HashMap<String, HashMap<String, DnDBuddyTreeNode>> {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcDndBuddyTreeBuddyNodeMap.class.getName());
    }

    public PbcDndBuddyTreeBuddyNodeMap() {
    }

    @Override
    public HashMap<String, DnDBuddyTreeNode> put(String key, HashMap<String, DnDBuddyTreeNode> value) {
        if (key == null){
            return super.put(key, value);
        }else{
            return super.put(key.toLowerCase(), value);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null){
            return super.containsKey(key);
        }else{
            return super.containsKey(key.toString().toLowerCase());
        }
    }

    @Override
    public HashMap<String, DnDBuddyTreeNode> get(Object key) {
        if (key == null){
            return super.get(key);
        }else{
            return super.get(key.toString().toLowerCase());
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends HashMap<String, DnDBuddyTreeNode>> m) {
        //disabled
        logger.log(Level.WARNING, "putAll method is disabled");
    }

    @Override
    public HashMap<String, DnDBuddyTreeNode> remove(Object key) {
        if (key == null){
            return super.remove(key);
        }else{
            return super.remove(key.toString().toLowerCase());
        }
    }

}
