/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** * * @author Jsupport */
public class AccessDatabaseConnection {
    public static Connection connect() {
        Connection con;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=Data.mdb;";
            con = DriverManager.getConnection(database, "Admin", "64");
        } catch (Exception ex) {
            return null;
        }
        return con;
    }
    
    public void getData(){
        try {
            Statement stmt = connect().createStatement();
            ResultSet rset = stmt.executeQuery("SELECT * FROM tblData");
            if (rset.next()) {
                String name = rset.getString("user_name");
                String email = rset.getString("user_email");
                System.out.println("Name  : " +name +"  Email : "+email);
            }
        } catch (SQLException ex) {
        }
    }
    public static void main(String[] args) {
        new AccessDatabaseConnection().getData();
    }
}