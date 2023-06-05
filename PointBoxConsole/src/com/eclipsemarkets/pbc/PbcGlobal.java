/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.web.pbc.talker.BuddyListBuddyItem;
import com.eclipsemarkets.web.pbc.talker.BuddyListGroupItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A common utility class only for PointBox console.
 * @author Zhijun Zhang
 */
public class PbcGlobal {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcGlobal.class.getName());
    }

    public static boolean isLegalInput(String text){
        Matcher m = Pattern.compile("^[a-zA-Z0-9_\\s]*$").matcher(text);
        return m.matches();
    }

    /**
     * This is used by Viewer's price presentation
     * @param value
     * @param num
     * @param zeroText
     * @return 
     */
    public static String localFormatStringByDoublePrecisionWithoutRemovingNegative(Double value, int num, String zeroText){
        if (value == null){
            return "";
        }
        String result = DataGlobal.formatStringByDoublePrecision(value, num);
        if ((!result.isEmpty()) && (DataGlobal.convertToDouble(result) == 0)){
            return zeroText;
        }
        return result;
    }

    /**
     * This is used by Viewer's price presentation
     * @param value
     * @param num
     * @param zeroText
     * @return 
     */
    public static String localFormatStringByDoublePrecision(Double value, int num, String zeroText){
        if (value == null){
            return "";
        }
        String result = DataGlobal.formatStringByDoublePrecision(value, num);
        if ((!result.isEmpty()) && (DataGlobal.convertToDouble(result) == 0)){
            return zeroText;
        }
        //result = result.replaceFirst("^[-]", "");
        return result;
    }

    public static BuddyListGroupItem[] sortGroupItemsAlphabetically(ArrayList<BuddyListGroupItem> unsortedGroupItems) {
        Collections.sort(unsortedGroupItems, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                return ((BuddyListGroupItem)o1).getGroupName().compareToIgnoreCase(((BuddyListGroupItem)o2).getGroupName());
            }
        });
        return unsortedGroupItems.toArray(new BuddyListGroupItem[0]);
    }

    public static BuddyListGroupItem[] sortGroupItemsByIndex(ArrayList<BuddyListGroupItem> unsortedGroupItems) {
        Collections.sort(unsortedGroupItems, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                if ((o1 instanceof BuddyListGroupItem) && (o2 instanceof BuddyListGroupItem)){
                    if (((BuddyListGroupItem)o1).getGroupIndex() < ((BuddyListGroupItem)o2).getGroupIndex()) {
                        return -1;
                    }else if (((BuddyListGroupItem)o1).getGroupIndex() > ((BuddyListGroupItem)o2).getGroupIndex()){
                        return 1;
                    }else{
                        return 0;
                    }
                }else{
                    return 0;
                }
            }
        });
        return unsortedGroupItems.toArray(new BuddyListGroupItem[0]);
    }

    public static BuddyListBuddyItem[] sortBuddyItemsAlphabetically(ArrayList<BuddyListBuddyItem> unsortedBuddyItems) {
        Collections.sort(unsortedBuddyItems, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                return ((BuddyListBuddyItem)o1).getBuddyName().compareToIgnoreCase(((BuddyListBuddyItem)o2).getBuddyName());
            }
        });
        return unsortedBuddyItems.toArray(new BuddyListBuddyItem[0]);
    }

    public static BuddyListBuddyItem[] sortBuddyItemsByIndex(ArrayList<BuddyListBuddyItem> unsortedBuddyItems) {
        Collections.sort(unsortedBuddyItems, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                if ((o1 instanceof BuddyListBuddyItem) && (o2 instanceof BuddyListBuddyItem)){
                    if (((BuddyListBuddyItem)o1).getBuddyIndex() < ((BuddyListBuddyItem)o2).getBuddyIndex()) {
                        return -1;
                    }else if (((BuddyListBuddyItem)o1).getBuddyIndex() > ((BuddyListBuddyItem)o2).getBuddyIndex()){
                        return 1;
                    }else{
                        return 0;
                    }
                }else{
                    return 0;
                }
            }
        });
        return unsortedBuddyItems.toArray(new BuddyListBuddyItem[0]);
    }

    public static ArrayList<IGatewayConnectorBuddy> sortBuddiesByUniqueNames(ArrayList<IGatewayConnectorBuddy> buddies) {
        Collections.sort(buddies, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                if ((o1 instanceof IGatewayConnectorBuddy) && (o2 instanceof IGatewayConnectorBuddy)){
                    return ((IGatewayConnectorBuddy)o1).getIMUniqueName().compareToIgnoreCase(((IGatewayConnectorBuddy)o2).getIMUniqueName());
                }else{
                    return 0;
                }
            }
        });
        return buddies;
    }

    public static ArrayList<IGatewayConnectorGroup> sortGroupsByUniqueNames(ArrayList<IGatewayConnectorGroup> groups) {
        Collections.sort(groups, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                if ((o1 instanceof IGatewayConnectorGroup) && (o2 instanceof IGatewayConnectorGroup)){
                    return ((IGatewayConnectorGroup)o1).getIMUniqueName().compareToIgnoreCase(((IGatewayConnectorGroup)o2).getIMUniqueName());
                }else{
                    return 0;
                }
            }
        });
        return groups;
    }

    public static ArrayList<IGatewayConnectorBuddy> sortBuddiesByScreenNames(ArrayList<IGatewayConnectorBuddy> buddies) {
        Collections.sort(buddies, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                if ((o1 instanceof IGatewayConnectorBuddy) && (o2 instanceof IGatewayConnectorBuddy)){
                    return ((IGatewayConnectorBuddy)o1).getIMScreenName().compareToIgnoreCase(((IGatewayConnectorBuddy)o2).getIMScreenName());
                }else{
                    return 0;
                }
            }
        });
        return buddies;
    }

    public static ArrayList<IGatewayConnectorGroup> sortGroupsByGroupNames(ArrayList<IGatewayConnectorGroup> groups) {
        Collections.sort(groups, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                if ((o1 instanceof IGatewayConnectorGroup) && (o2 instanceof IGatewayConnectorGroup)){
                    return ((IGatewayConnectorGroup)o1).getGroupName().compareToIgnoreCase(((IGatewayConnectorGroup)o2).getGroupName());
                }else{
                    return 0;
                }
            }
        });
        return groups;
    }

    public static ArrayList<IGatewayConnectorBuddy> copyGatewayConnectorBuddies(ArrayList<IGatewayConnectorBuddy> attendants) {
        ArrayList<IGatewayConnectorBuddy> aCopy = new ArrayList<IGatewayConnectorBuddy>();
        if (attendants != null){
            for (int i = 0; i < attendants.size(); i++){
                aCopy.add(attendants.get(i));
            }
        }
        return aCopy;
    }
}
