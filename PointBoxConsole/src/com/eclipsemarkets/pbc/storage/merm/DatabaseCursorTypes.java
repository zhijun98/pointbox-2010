/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author Zhijun Zhang
 */
public enum DatabaseCursorTypes {

    INTEGER("integer");

    private String term;
    DatabaseCursorTypes(String term){
        this.term = term;
    }
    @Override
    public String toString() {
        return term;
    }
}
