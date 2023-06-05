/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author xmly
 */
class messageSearchByid extends MessageSearchCriteria {

    int id;

    public messageSearchByid(int id) {
        this.id = id;
    }

    public String getsql() {
        return "AND pb_msgid = " + this.id;
    }
}
