/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author Zhijun Zhang
 */
public enum MSAccessFileTypes {

    MSACCESS_FILE_EXT("mdb"),
    MSACCESS_LOCK_FILE_EXT("ldb");

    private String term;
    MSAccessFileTypes(String term){
        this.term = term;
    }
    @Override
    public String toString() {
        return term;
    }

}
