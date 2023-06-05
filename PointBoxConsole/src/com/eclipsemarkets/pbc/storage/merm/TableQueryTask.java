/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
class TableQueryTask implements Callable<ResultSet>{

    private IDatabaseQuerier aDatabaseQuerier;
    private DatabaseSelectCommand cmdSelect;

    public TableQueryTask(IDatabaseQuerier aDatabaseQuerier, DatabaseSelectCommand cmds) {
        this.aDatabaseQuerier = aDatabaseQuerier;
        this.cmdSelect = cmds;
    }

    public ResultSet call() {
        try {
            return aDatabaseQuerier.execute(cmdSelect);
        } catch (SQLException ex) {
            Logger.getLogger(TableQueryTask.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
