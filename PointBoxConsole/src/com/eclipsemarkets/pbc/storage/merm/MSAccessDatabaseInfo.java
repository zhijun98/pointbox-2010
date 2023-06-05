/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.io.File;

/**
 *
 * @author Zhijun.Zhang
 * @deprecated 
 */
class MSAccessDatabaseInfo extends PbsysDatabaseInfo {

    public MSAccessDatabaseInfo(String dbInfo) {
        super(dbInfo);
    }

    public String getDbURL(){
        return dbInfo;
    }

    public File getDbFile(){
        return new File(dbInfo);
    }
}
