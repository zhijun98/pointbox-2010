/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewerFilterByStrategies.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 16, 2010, 9:11:28 PM
 */
class ViewerFilterByStrategies extends ViewerFilterCriteria implements IViewerFilterByStrategies{

    private ArrayList<String> strategies;

    ViewerFilterByStrategies(ArrayList<String> strategies) {
        this.strategies = initializeStringValues(strategies);
    }

    ViewerFilterByStrategies() {
        strategies = new ArrayList<String>();
    }

    @Override
    public List<PbcFilterPropertySettings> constructPbcFilterPropertySettingsList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<String> getStrategies() {
        return strategies;
    }

    @Override
    public void setStrategies(ArrayList<String> strategies) {
        this.strategies = strategies;
    }
//
//    @Override
//    public String serializeHelper() {
//        String state = "";
//
//        if (strategies != null) {
//            for (int i = 0; i < strategies.size(); i++) {
//                state = state + strategies.get(i) + ";";
//            }
//        }
//
//        state = RegexGlobal.serializeString(state, ViewerSearchByStrategiesTerms.strategies.toString());
//
//        return state;
//    }
//
//    @Override
//    public void deserialize(String state) {
//        String allStrategies = RegexGlobal.deserializeString(state, ViewerSearchByStrategiesTerms.strategies.toString());
//        strategies = new ArrayList<String>();
//        for (int i = 0; i < allStrategies.split(";").length; i++) {
//            if (!allStrategies.split(";")[i].isEmpty()) {
//                strategies.add(allStrategies.split(";")[i]);
//            }
//        }
//    }

    static enum ViewerSearchByStrategiesTerms {

        strategies("strategies");
        private String term;

        ViewerSearchByStrategiesTerms(String term) {
            this.term = term;
        }

        @Override
        public String toString() {
            return term;
        }
    }
}
