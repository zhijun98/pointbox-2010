/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author xmly
 */
class messageSearchBymessage extends MessageSearchCriteria {

    String msg = "";

    public messageSearchBymessage(String msg) {
        this.msg = msg;
    }

    public String getsql() {
        return " AND pb_message LIKE '*" + msg + "*'";
    }
}
