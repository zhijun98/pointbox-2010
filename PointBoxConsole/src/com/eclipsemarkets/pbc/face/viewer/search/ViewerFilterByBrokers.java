/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewerFilterByBrokers.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 16, 2010, 9:10:51 PM
 */
class ViewerFilterByBrokers extends ViewerFilterCriteria implements IViewerFilterByBrokers {

    private ArrayList<String> brokerNames;

    ViewerFilterByBrokers(ArrayList<String> brokerNames) {
        this.brokerNames = initializeStringValues(brokerNames);
    }

    @Override
    public List<PbcFilterPropertySettings> constructPbcFilterPropertySettingsList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * index-0 item contains a long ";"-delimited string of broker names.
     * @return 
     */
    @Override
    public ArrayList<String> getBrokerUniqueNames() {
        return brokerNames;
    }

    @Override
    public ArrayList<String> getSearchBrokersList() {
        //it's supposed to have a long string in index-0-item
        ArrayList<String> searchBrokersList= new ArrayList<String>();       //deserial the String and convert it into ArrayList
        if ((brokerNames != null) && (!brokerNames.isEmpty())){
            String searchBrokerStr = brokerNames.get(0);
            String[] searchBrokerStrArray = searchBrokerStr.split(";");
            for (int i = 0; i < searchBrokerStrArray.length; i++) {
                 if (DataGlobal.isNonEmptyNullString(searchBrokerStrArray[i])){
                     searchBrokersList.add(searchBrokerStrArray[i]);
                 }
            }
        }
        return searchBrokersList;
    }
}
