/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.storage.merm;

import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import java.util.ArrayList;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterCriteria;

/**
 *
 * @author xmly
 */
public interface IPbsysDBOperation {

    public int saveMessage(IPbsysInstantMessage MSG);

    public int saveQuote(IPbsysOptionQuote Quote);

    public boolean saveLeg(IPbsysQuoteLeg leg);

    public ArrayList<IPbsysOptionQuote> searchQuotes(ArrayList<IViewerFilterCriteria> criteriaList);

    public ArrayList<IPbsysInstantMessage> searchMessages(ArrayList<IMessageSearchCriteria> criteriaList);

    public ArrayList<IPbsysOptionQuote> retrieveQuotes(ArrayList<IViewerFilterCriteria> criteriaList);

    public ArrayList<IPbsysInstantMessage> retrieveMessages(ArrayList<IMessageSearchCriteria> criteriaList);

    public int getOptiontypeid(String optiontype);

    public int saveOptiontypeid(String optiontype);

    public int getCommodityid(String commoditytype);

    public int saveCommodityid(String commoditytype);

    public int getMSGid(IPbsysInstantMessage MSG);
}
