/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Zhijun Zhang
 */
public interface IDatabaseQuerier {

    public ResultSet execute(DatabaseSelectCommand sqlCommand) throws SQLException;

}
