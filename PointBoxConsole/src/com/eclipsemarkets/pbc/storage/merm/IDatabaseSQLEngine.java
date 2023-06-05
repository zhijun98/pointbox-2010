/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.io.File;

/**
 *
 * @author Zhijun Zhang
 */
public interface IDatabaseSQLEngine {

    public void startSQLEngine(File mdbFile, String userName, String password);

    public void stopSQLEngine();

}
