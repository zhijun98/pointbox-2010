/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.pbc.face.viewer.FilterPropertyKey;
import com.eclipsemarkets.web.pbc.viewer.PbcFilterPropertySettings;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * ViewerFilterByPeriods.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 16, 2010, 9:07:03 PM
 */
class ViewerFilterByPeriods extends ViewerFilterCriteria implements IViewerFilterByPeriods {
    private ArrayList<String> periodString;
    private ViewerPeriodsOperator fromOperatorTerm;
    private ViewerPeriodsOperator toOperatorTerm;
    private GregorianCalendar fromDate;
    private GregorianCalendar toDate;

    ViewerFilterByPeriods(ArrayList<String> periodString) {
        this.periodString = initializeStringValues(periodString);
    }

    ViewerFilterByPeriods(){
        periodString = new ArrayList<String>();
    }

    ViewerFilterByPeriods(GregorianCalendar fromDate, GregorianCalendar toDate,
                                    ViewerPeriodsOperator fromOperatorTerm, ViewerPeriodsOperator toOperatorTerm){
        periodString = new ArrayList<String>();
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.fromOperatorTerm = fromOperatorTerm;
        this.toOperatorTerm = toOperatorTerm;
    }
    
    @Override
    public List<PbcFilterPropertySettings> constructPbcFilterPropertySettingsList() {
        if (aPbcFilterPropertySettingsList == null){
            aPbcFilterPropertySettingsList = new ArrayList<PbcFilterPropertySettings>();
            if (searchLegIndex == 0){
                PbcFilterPropertySettings aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg01_selectedStartDate_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(Long.toString(fromDate.getTimeInMillis()));
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
                
                aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg01_selectedEndDate_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(Long.toString(toDate.getTimeInMillis()));
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
                
                aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg01_startOperator_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(fromOperatorTerm.toString());
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
                
                aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg01_endOperator_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(toOperatorTerm.toString());
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
            }else{
                PbcFilterPropertySettings aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg02_selectedStartDate_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(Long.toString(fromDate.getTimeInMillis()));
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
                
                aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg02_selectedEndDate_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(Long.toString(toDate.getTimeInMillis()));
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
                
                aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg02_startOperator_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(fromOperatorTerm.toString());
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
                
                aPbcFilterPropertySettings = new PbcFilterPropertySettings();
                aPbcFilterPropertySettings.setPropertyKey(FilterPropertyKey.leg02_endOperator_value.toString());
                aPbcFilterPropertySettings.setPropertyValue(toOperatorTerm.toString());
                aPbcFilterPropertySettingsList.add(aPbcFilterPropertySettings);
            }
        }
        return aPbcFilterPropertySettingsList;
    }

    public ArrayList<String> getPeriodString() {
        return periodString;
    }

    @Override
    public GregorianCalendar getFromDate(){
        return fromDate;
    }

    @Override
    public GregorianCalendar getToDate(){
        return toDate;
    }

    public void setPeriodString(ArrayList<String> periodString) {
        this.periodString = periodString;
    }

    private GregorianCalendar getSelectedGregorianCalendar(String month, String year, int day) {
        String mString = month;
        String yString = year;
        if ((mString.isEmpty()) || (yString.isEmpty())){
            return null;
        }else{
            GregorianCalendar result = new GregorianCalendar(Integer.parseInt(yString),
                                                                CalendarGlobal.convertToIntegerMonth(mString)-1, 1, 0, 0, 0);
            if (day > 1){
                result.set(Calendar.DAY_OF_MONTH, result.getMaximum(Calendar.MONTH));
            }
            return result;
        }
    }

    @Override
    public ViewerPeriodsOperator getFromOperatorTerm(){
        return fromOperatorTerm;
    }

    @Override
    public ViewerPeriodsOperator getToOperatorTerm(){
        return toOperatorTerm;
    }

//    @Override
//    public String serializeHelper() {
//        String state = "";
//
//        if (periodString != null){
//            for (int i = 0; i < periodString.size(); i++){
//                state = state + periodString.get(i) + ";";
//            }
//        }
//
//        state = RegexGlobal.serializeString(state, ViewerSearchByPeriodsTerms.periodString.toString());
//        if (fromOperatorTerm != null){
//            state = state + RegexGlobal.serializeString(fromOperatorTerm.toString(), ViewerSearchByPeriodsTerms.fromOperatorTerm.toString());
//        }
//        if (toOperatorTerm != null){
//            state = state + RegexGlobal.serializeString(toOperatorTerm.toString(), ViewerSearchByPeriodsTerms.toOperatorTerm.toString());
//        }
//        if (fromDate != null){
//            state = state + RegexGlobal.serializeGregorianCalendar(fromDate, ViewerSearchByPeriodsTerms.fromDate.toString());
//        }
//        if (toDate != null){
//            state = state + RegexGlobal.serializeGregorianCalendar(toDate, ViewerSearchByPeriodsTerms.toDate.toString());
//        }
//
//        return state;
//    }
//
//    @Override
//    public void deserialize(String state) {
//        String allPeriodStrings = RegexGlobal.deserializeString(state, ViewerSearchByPeriodsTerms.periodString.toString());
//        for (int i = 0; i < allPeriodStrings.split(";").length; i++){
//            if (!allPeriodStrings.split(";")[i].isEmpty()){
//                periodString.add(allPeriodStrings.split(";")[i]);
//            }
//        }
//        fromOperatorTerm = ViewerPeriodsOperator.convertToType(RegexGlobal.deserializeString(state, ViewerSearchByPeriodsTerms.fromOperatorTerm.toString()));
//        toOperatorTerm = ViewerPeriodsOperator.convertToType(RegexGlobal.deserializeString(state, ViewerSearchByPeriodsTerms.toOperatorTerm.toString()));
//        fromDate = RegexGlobal.deserializeGregorianCalendar(state, ViewerSearchByPeriodsTerms.fromDate.toString());
//        toDate = RegexGlobal.deserializeGregorianCalendar(state, ViewerSearchByPeriodsTerms.toDate.toString());
//    }
    
    static enum ViewerSearchByPeriodsTerms {
        periodString("periodString"),
        fromOperatorTerm("fromOperatorTerm"),
        toOperatorTerm("toOperatorTerm"),
        fromDate("fromDate"),
        toDate("toDate");

        private String term;
        ViewerSearchByPeriodsTerms(String term){
            this.term = term;
        }
        @Override
        public String toString() {
            return term;
        }
    }

}
