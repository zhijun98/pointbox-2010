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
 * Buddy list case-sensitive issue on group names messed up buddy list persistency. 
 * Here is the case: because historical data for group name, e.g. "friends" has 
 * been stored in the database, it potentially could mess up saving-buddy-list 
 * function on the server-side. For example, mermzzj03 on Yahoo public server has 
 * a group "Friends". When mermzzj03 buddy list is saved on the server, the other 
 * existing group "friends", whose first letter is lower case, will be treated as 
 * a different group. This messed up buddy list persistency.
 *
 * @author Zhijun Zhang
 */
class PbcDndBuddyTreeGroupNodeMap extends HashMap<String, DnDGroupTreeNode> {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcDndBuddyTreeGroupNodeMap.class.getName());
    }

    public PbcDndBuddyTreeGroupNodeMap() {
    }

    @Override
    public DnDGroupTreeNode put(String key, DnDGroupTreeNode value) {
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
    public DnDGroupTreeNode get(Object key) {
        if (key == null){
            return super.get(key);
        }else{
            return super.get(key.toString().toLowerCase());
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends DnDGroupTreeNode> m) {
        //disabled
        logger.log(Level.WARNING, "putAll method is disabled");
    }

    @Override
    public DnDGroupTreeNode remove(Object key) {
        if (key == null){
            return super.remove(key);
        }else{
            return super.remove(key.toString().toLowerCase());
        }
    }
}