/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewerFilterCriteria.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 16, 2010, 8:52:44 PM
 */
abstract class ViewerFilterCriteria implements IViewerFilterCriteria{
    protected List<PbcFilterPropertySettings> aPbcFilterPropertySettingsList = null;
    protected ViewerFilterCriteriaType searchCriteria;
    protected int searchLegIndex;

    final ArrayList<String> initializeStringValues(ArrayList<String> values) {
        ArrayList<String> result = new ArrayList<String>();
        if (values != null){
            result = new ArrayList<String>();
            for (String value : values){
                if (DataGlobal.isNonEmptyNullString(value)){
                    result.add(value);
                }
            }
        }
        return result;
    }
    
    @Override
    public ViewerFilterCriteriaType getFilterCriteria() {
        return searchCriteria;
    }

    @Override
    public void setFilterCriteria(ViewerFilterCriteriaType searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

//    @Override
//    public ViewerTableType getFilterTarget() {
//        return searchTarget;
//    }
//
//    @Override
//    public void setFilterTarget(ViewerTableType searchTarget) {
//        this.searchTarget = searchTarget;
//    }

    @Override
    public void setFilterLegIndex(int searchLegIndex){
        this.searchLegIndex = searchLegIndex;
    }

    @Override
    public int getFilterLegIndex(){
        return searchLegIndex;
    }
}
