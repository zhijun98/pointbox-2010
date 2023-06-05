/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author Zhijun.Zhang
 * @deprecated 
 */
public abstract class PbsysDatabaseInfo {
    String dbInfo;

    /**
     *
     * @param dbInfo - it is the basic information hold by PbsysDatabaseInfo. it could be anything whose data type could be String.
     */
    public PbsysDatabaseInfo(String dbInfo) {
        this.dbInfo = dbInfo;
    }
}
