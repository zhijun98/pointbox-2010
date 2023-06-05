/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.storage.merm;

import com.eclipsemarkets.pbc.storage.merm.MessageSearchCriteria.MessageSearchCriteriaTerms;


/**
 *
 * @author xmly
 */
public interface IMessageSearchCriteria {

    public MessageSearchCriteriaTerms getSearchCriteria();

    public void setSearchCriteria(MessageSearchCriteriaTerms searchCriteria);

    public String getsql();

    
}
