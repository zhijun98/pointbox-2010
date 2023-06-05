/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

/**
 *
 * @author Zhijun Zhang
 */
public enum ConfirmNewBuddyDialogType {

    AcceptBuddy("Accept Buddy"),
    AddNewBuddy("Add New Buddy"),
    Unknown("Unknown");
    
    private String term;

    ConfirmNewBuddyDialogType(String term) {
        this.term = term;
    }
    
    public static ConfirmNewBuddyDialogType convertToType(String term) {
        if (term == null) {
            return null;
        }
        if (term.equalsIgnoreCase(AddNewBuddy.toString())) {
            return AddNewBuddy;
        } else if (term.equalsIgnoreCase(AcceptBuddy.toString())) {
            return AcceptBuddy;
        } else {
            return Unknown;
        }
    }
    
    @Override
    public String toString() {
        return term;
    }
}
