/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.sql.SQLException;

/**
 *
 * @author Zhijun Zhang
 */
public interface IDatabaseModifer{

    /**
     *
     * @param sqlCommand
     * @param timeout - mills
     * @return
     * @throws SQLException
     */
    public int execute(DatabaseInsertCommand sqlCommand, long timeout) throws SQLException;

    /**
     *
     * @param sqlCommand
     * @param timeout
     * @return
     * @throws SQLException
     */
    public int execute(DatabaseUpdateCommand sqlCommand, long timeout) throws SQLException;

    /**
     * 
     * @param sqlCommand
     * @param timeout
     * @return
     * @throws SQLException
     */
    public int execute(DatabaseDeleteCommand sqlCommand, long timeout) throws SQLException;

}
