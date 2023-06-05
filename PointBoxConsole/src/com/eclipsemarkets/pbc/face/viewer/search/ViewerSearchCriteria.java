/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerSearchCriteria;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerPeriodsOperator;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerSearchCriteriaTerms;
import java.sql.Date;
import java.util.ArrayList;

/**
 * ViewerSearchCriteria.java
 * <p>
 * This is a pure data structure which contains all the information for clients to
 * build up SQL query for historical quotes stored in the database
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 23, 2010, 10:48:54 AM
 */
class ViewerSearchCriteria implements IViewerSearchCriteria{
    //criteria for instant message
    private Date fromDate;
    private Date toDate;
    private boolean dateRangeLimited;
    private String keywords;
    private ArrayList<IGatewayConnectorBuddy> brokers = new ArrayList<IGatewayConnectorBuddy>();
    private String brokerName;

    //criteria for quote
    private ViewerSearchCriteriaTerms AndOrStructures;
    private ViewerSearchCriteriaTerms AndOrLocations;
    private ViewerSearchCriteriaTerms AndOrPeriod;
    private ArrayList<String> structures = new ArrayList<String>();
    private ArrayList<String> locations = new ArrayList<String>();
    private Date startDate;
    private ViewerPeriodsOperator startDateOperator;
    private Date endDate;
    private ViewerPeriodsOperator endDateOperator;

    public ViewerPeriodsOperator getEndDateOperator() {
        return endDateOperator;
    }

    public void setEndDateOperator(ViewerPeriodsOperator endDateOperator) {
        this.endDateOperator = endDateOperator;
    }

    public ViewerPeriodsOperator getStartDateOperator() {
        return startDateOperator;
    }

    public void setStartDateOperator(ViewerPeriodsOperator startDateOperator) {
        this.startDateOperator = startDateOperator;
    }

    public ViewerSearchCriteriaTerms getAndOrStructures() {
        return AndOrStructures;
    }

    public void setAndOrStructures(ViewerSearchCriteriaTerms AndOr_01) {
        this.AndOrStructures = AndOr_01;
    }

    public ViewerSearchCriteriaTerms getAndOrLocations() {
        return AndOrLocations;
    }

    public void setAndOrLocations(ViewerSearchCriteriaTerms AndOr_02) {
        this.AndOrLocations = AndOr_02;
    }

    public ViewerSearchCriteriaTerms getAndOrPeriod() {
        return AndOrPeriod;
    }

    public void setAndOrPeriod(ViewerSearchCriteriaTerms AndOrPeriod) {
        this.AndOrPeriod = AndOrPeriod;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public ArrayList<IGatewayConnectorBuddy> getBrokers() {
        return brokers;
    }

    public void setBrokers(Object[] brokers) {
        this.brokers.clear();
        if (brokers == null){
            return;
        }
        if (brokers != null){
            for (int i = 0; i < brokers.length; i++){
                if (brokers[i] instanceof IGatewayConnectorBuddy){
                    this.brokers.add((IGatewayConnectorBuddy)brokers[i]);
                }
            }
        }
    }

    public boolean isDateRangeLimited() {
        return dateRangeLimited;
    }

    public void setDateRangeLimited(boolean dateRangeLimited) {
        this.dateRangeLimited = dateRangeLimited;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ArrayList<String> getStructures() {
        return structures;
    }

    public void setStructures(Object[] structures) {
        this.structures.clear();
        if (structures == null){
            return;
        }
        if (structures != null){
            for (int i = 0; i < structures.length; i++){
                this.structures.add(structures[i].toString());
            }
        }
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

    public void setLocations(Object[] locations) {
        this.locations.clear();
        if (locations == null){
            return;
        }
        if (locations != null){
            for (int i = 0; i < locations.length; i++){
                this.locations.add(locations[i].toString());
            }
        }
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}
