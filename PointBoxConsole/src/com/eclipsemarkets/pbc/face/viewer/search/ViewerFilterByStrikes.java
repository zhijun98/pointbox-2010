/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewerFilterByStrikes.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 16, 2010, 9:08:15 PM
 */
class ViewerFilterByStrikes extends ViewerFilterCriteria implements IViewerFilterByStrikes{

    private ArrayList<String> strikeString;

    ViewerFilterByStrikes(ArrayList<String> strikeString) {
        this.strikeString = initializeStringValues(strikeString);
    }

    @Override
    public List<PbcFilterPropertySettings> constructPbcFilterPropertySettingsList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    ViewerFilterByStrikes() {
        strikeString = new ArrayList<String>();
    }

    @Override
    public ArrayList<String> getStrikeString() {
        return strikeString;
    }

    @Override
    public void setStrikeString(ArrayList<String> strikeString) {
        this.strikeString = strikeString;
    }

//    @Override
//    public String serializeHelper() {
//        String state = "";
//
//        if (strikeString != null) {
//            for (int i = 0; i < strikeString.size(); i++) {
//                state = state + strikeString.get(i) + ";";
//            }
//        }
//
//        state = RegexGlobal.serializeString(state, ViewerSearchByStrikesTerms.StrikeString.toString());
//
//        return state;
//    }
//
//    @Override
//    public void deserialize(String state) {
//        String allStrikes = RegexGlobal.deserializeString(state, ViewerSearchByStrikesTerms.StrikeString.toString());
//        strikeString = new ArrayList<String>();
//        for (int i = 0; i < allStrikes.split(";").length; i++) {
//            if (!allStrikes.split(";")[i].isEmpty()) {
//                strikeString.add(allStrikes.split(";")[i]);
//            }
//        }
//    }

    static enum ViewerSearchByStrikesTerms {

        StrikeString("strikeString");
        private String term;

        ViewerSearchByStrikesTerms(String term) {
            this.term = term;
        }

        @Override
        public String toString() {
            return term;
        }
    }
}
