/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zhijun Zhang
 */
public enum BuddyListPanelCommand {

    DisplayGroupFrame("Display in a floating frame"),
    AddNewBuddy("Add a new buddy..."),
    AutoImport("Automatically import all the existing groups of current instant messager connections you have logged in"),
    CreateNewGroup("Create a new group..."),
    OpenConferenceRoom("Call a new conference..."),
    SortAZ("Sort ascendingly..."),
    SortZA("Sort descendingly..."),
    SynGroups("Synchronize and display original buddy list saved on the public instant messaging server..."),
    UNKNOWN("Unknown");
    private String value;

    BuddyListPanelCommand(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }

    public static List<String> getEnumValueList(boolean includeUnknownValue){
        List<String> result = new ArrayList<String>();
        BuddyListPanelCommand[] valueArray = BuddyListPanelCommand.values();
        for (BuddyListPanelCommand valueObj : valueArray){
            if (includeUnknownValue){
                result.add(valueObj.toString());
            }else{
                if (!(valueObj.toString().equalsIgnoreCase(UNKNOWN.toString()))){
                    result.add(valueObj.toString());
                }
            }
        }
        return result;
    }

    public static List<String> getEnumNameList(boolean includeUnknownName){
        List<String> result = new ArrayList<String>();
        BuddyListPanelCommand[] valueArray = BuddyListPanelCommand.values();
        for (BuddyListPanelCommand valueObj : valueArray){
            if (includeUnknownName){
                result.add(valueObj.name());
            }else{
                if (!(valueObj.name().equalsIgnoreCase(UNKNOWN.name()))){
                    result.add(valueObj.name());
                }
            }
        }
        return result;
    }

    //public static BuddyListPanelCommand convertEnumValueToType(String value){
    public static BuddyListPanelCommand convertToType(String value){
        BuddyListPanelCommand result = UNKNOWN;
        BuddyListPanelCommand[] valueArray = BuddyListPanelCommand.values();
        for (BuddyListPanelCommand valueObj : valueArray){
            if (valueObj.toString().equalsIgnoreCase(value)){
                result = valueObj;
            }
        }
        return result;
    }

    public static BuddyListPanelCommand convertEnumNameToType(String name){
        BuddyListPanelCommand result = UNKNOWN;
        BuddyListPanelCommand[] valueArray = BuddyListPanelCommand.values();
        for (BuddyListPanelCommand valueObj : valueArray){
            if (valueObj.name().equalsIgnoreCase(name)){
                result = valueObj;
            }
        }
        return result;
    }
}
