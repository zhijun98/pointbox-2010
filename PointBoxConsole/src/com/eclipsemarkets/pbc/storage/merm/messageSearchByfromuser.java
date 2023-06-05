/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author xmly
 */
class messageSearchByfromuser extends MessageSearchCriteria {

    String fromuser = "";

    public messageSearchByfromuser(String fromuser) {
        this.fromuser = fromuser;
    }

    public String getsql() {
        return "AND pb_from_user = '" + fromuser + "'";
    }
}
