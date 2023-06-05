/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyKey;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zhijun Zhang, date & time: Sep 7, 2014 - 2:08:16 PM
 */
class ViewerFilterByClass extends ViewerFilterCriteria implements IViewerFilterByClass {

    private ArrayList<String> criteriaValues;

    ViewerFilterByClass(ArrayList<String> criteriaValues) {
        this.criteriaValues = initializeStringValues(criteriaValues);
    }

    /**
     * index-0 item contains a long ";"-delimited string of broker names.
     * @return 
     */
    @Override
    public ArrayList<String> getCriteriaClassValues() {
        return criteriaValues;
    }

    @Override
    public ArrayList<String> getSearchClassValueList() {
        //it's supposed to have a long string in index-0-item
        ArrayList<String> searchClassValueList= new ArrayList<String>();       //deserial the String and convert it into ArrayList
        if ((criteriaValues != null) && (!criteriaValues.isEmpty())){
            String searchBrokerStr = criteriaValues.get(0);
            String[] searchBrokerStrArray = searchBrokerStr.split(",");
            for (int i = 0; i < searchBrokerStrArray.length; i++) {
                 if (DataGlobal.isNonEmptyNullString(searchBrokerStrArray[i])){
                     searchClassValueList.add(searchBrokerStrArray[i]);
                 }
            }
        }
        return searchClassValueList;
    }
    
    @Override
    public List<PbcFilterPropertySettings> constructPbcFilterPropertySettingsList() {
        if (aPbcFilterPropertySettingsList == null){
            aPbcFilterPropertySettingsList = new ArrayList<PbcFilterPropertySettings>();
            PbcFilterPropertySettings aPbcFilterPropertySettings = new PbcFilterPropertySettings();
            aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.jClass_SelectedName.toString());
            aPbcFilterPropertySettings.setPropertyValue(FilterPropertyKey.generateFilterValueForPersistency(criteriaValues));
            aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
        }
        return aPbcFilterPropertySettingsList;
    }
}
