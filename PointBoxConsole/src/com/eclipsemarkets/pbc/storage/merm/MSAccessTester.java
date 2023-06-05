/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.io.File;
import java.sql.ResultSet;
import com.eclipsemarkets.global.ConstantGlobal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhijun Zhang
 */
class MSAccessTester {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(MSAccessTester.class.getName());
    }

    public static void main(String[] args){
        final String mdbFilePath = "C:\\Documents and Settings\\Zhijun Zhang\\My Documents\\Projects\\qdb_test_java.mdb";

        Thread t01 = (new Thread(new Runnable(){
            public void run() {
                IDatabaseModifer agent = MSAccessModifier.getMSAccessModifierSingleton(new File(mdbFilePath), "", "");
                try{
                    for (int i = 0; i < 10000; i++){
                        try {
                            agent.execute(new DatabaseInsertCommand("INSERT INTO all_q (Quote, ID, Broker, qTime, Structure, Strike1, Strike2, Strike3, Strike4, Strike5, Ratio1, Ratio2, Ratio3, Ratio4, Ratio5, qCross, qLast, Bid, Ask, Product, Type2, Strike12, Strike22, Strike32, Strike42, Strike52, Ratio12, Ratio22, Ratio32, Ratio42, Ratio52, qCross2, Product2, mUser, priceQ) VALUES ('yes, please save it', '0mermzzj312/20/2009@08:32:08guangzhou-mermzzj2YIM083208656', 'mermzzj3', #12/20/2009 08:32:08#, '', 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, '', '', 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, '', 'mermzzj2', 'N')"), 3000);
                        } catch (SQLException ex) {
                            Logger.getLogger(MSAccessTester.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Thread.sleep(100);
                    }//for
                } catch (InterruptedException ex) {}
            }
        }));
        t01.setName("MSAccessTester - main() - 01");
        t01.start();

        Thread t02 = (new Thread (new Runnable(){
            public void run() {
                IDatabaseQuerier agent = MSAccessQuerier.getMSAccessQuerierInstance(new File(mdbFilePath), "", "");
                try{
                    for (int i = 0; i < 10000; i++){
                        try {
                            ResultSet rs = agent.execute(new DatabaseSelectCommand("SELECT * FROM all_q"));
                        } catch (SQLException ex) {
                            Logger.getLogger(MSAccessTester.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Thread.sleep(500);
                    }//for
                } catch (InterruptedException ex) {}
            }
        }));
        t02.setName("MSAccessTester - main() - 01");
        t02.start();
        try {
            Thread.sleep(ConstantGlobal.millisecondsOfOneMintue * 3);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private MSAccessTester() {
    }

}
