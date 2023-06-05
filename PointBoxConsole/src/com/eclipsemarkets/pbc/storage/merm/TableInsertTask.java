/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 *
 * @author Zhijun Zhang
 */
class TableInsertTask implements Callable<Boolean>{

    private IDatabaseModifer aDatabaseModifier;
    private ArrayList<DatabaseInsertCommand> cmdInserts;

    public TableInsertTask(IDatabaseModifer aDatabaseModifier, ArrayList<DatabaseInsertCommand> cmds) {
        this.aDatabaseModifier = aDatabaseModifier;
        this.cmdInserts = cmds;
    }

    public Boolean call() throws Exception {
        for (int i = 0; i < cmdInserts.size(); i++){
            aDatabaseModifier.execute(cmdInserts.get(i), 3000);
        }
        return true;
    }
}
