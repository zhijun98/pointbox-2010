/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewerFilterByLocations.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 16, 2010, 9:09:47 PM
 */
class ViewerFilterByLocations extends ViewerFilterCriteria implements IViewerFilterByLocations{

    private ArrayList<String> locations;

    ViewerFilterByLocations(ArrayList<String> locations) {
        this.locations = initializeStringValues(locations);
    }

    @Override
    public List<PbcFilterPropertySettings> constructPbcFilterPropertySettingsList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    ViewerFilterByLocations() {
        locations = new ArrayList<String>();
    }

    @Override
    public ArrayList<String> getLocations() {
        return locations;
    }

    @Override
    public void setLocations(ArrayList<String> strategies) {
        this.locations = strategies;
    }
//
//    @Override
//    public String serializeHelper() {
//        String state = "";
//
//        if (locations != null) {
//            for (int i = 0; i < locations.size(); i++) {
//                state = state + locations.get(i) + ";";
//            }
//        }
//
//        state = RegexGlobal.serializeString(state, ViewerSearchByLocationsTerms.locations.toString());
//
//        return state;
//    }

//    @Override
//    public void deserialize(String state) {
//        String allLocationNames = RegexGlobal.deserializeString(state, ViewerSearchByLocationsTerms.locations.toString());
//        locations = new ArrayList<String>();
//        for (int i = 0; i < allLocationNames.split(";").length; i++) {
//            if (!allLocationNames.split(";")[i].isEmpty()) {
//                locations.add(allLocationNames.split(";")[i]);
//            }
//        }
//    }

    static enum ViewerSearchByLocationsTerms {

        locations("locations");
        private String term;

        ViewerSearchByLocationsTerms(String term) {
            this.term = term;
        }

        @Override
        public String toString() {
            return term;
        }
    }
}
