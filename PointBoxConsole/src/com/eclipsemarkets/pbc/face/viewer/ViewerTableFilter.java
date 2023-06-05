/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import com.eclipsemarkets.pbc.face.viewer.model.IViewerDataModel;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByBrokers;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByClass;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByCode;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByGroup;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByLocations;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByPeriods;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByStrategies;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterByStrikes;
import com.eclipsemarkets.pbc.face.viewer.search.IViewerFilterCriteria;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerPeriodsOperator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.table.TableModel;

/**
 * ViewerTableFilter.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 25, 2010, 2:33:03 PM
 */
class ViewerTableFilter extends RowFilter<TableModel, Integer>{

    private static final Logger logger;
    static{
        logger = Logger.getLogger(ViewerTableFilter.class.getName());
    }

    private ViewerTableType tableType;
    private PointBoxHiddenMessagesProperties properties;
    private IViewerFilterCriteria criteria;
    private IPbcViewer viewer;

    /**
     * Classic predefined viewer uses this constructor
     * @param viewer
     * @param tableType 
     */
    ViewerTableFilter(IPbcViewer viewer, ViewerTableType tableType) {
        this.tableType = tableType;
        this.criteria = null;
        this.viewer = viewer;
        this.properties = null;
    }

    /**
     * Filter viewer uses this constructor
     * @param viewer
     * @param criteria 
     */
    ViewerTableFilter(IPbcViewer viewer, IViewerFilterCriteria criteria) {
        this.tableType = null;
        this.criteria = criteria;
        this.viewer = viewer;
        this.properties = null;
    }

    @Override
    public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        Integer rowIndex = entry.getIdentifier();
        //PointBoxLogger.printWarningMessage("entry.getIdentifier() -> " + rowIndex);
        if (entry.getModel() instanceof IViewerDataModel){
            IPbsysOptionQuoteWrapper wrapper = ((IViewerDataModel)entry.getModel()).getDataModelKernel()
                                                    .retrievePbsysOptionQuoteWrapper(rowIndex.intValue());
            if (wrapper == null){
                return false;
            }
            if (wrapper.getQuoteOwner() == null){
                return true;
            }
            if (tableType != null){
                //Regular tab viewers
                return switchWorkingForTableType(wrapper);
            }else if (criteria != null){
                //Filter tab viewers
                return switchWorkingForCriteria(wrapper.getQuoteOwner());
            }else{
                return true;
            }
        }else{
            return true;
        }
    }

    private boolean switchWorkingForTableType(IPbsysOptionQuoteWrapper wrapper) {
        switch (tableType){
            case ALL_MESSAGES:
                return isNotHiddenMessage(wrapper.getQuoteOwner().getInstantMessage().getMessageContent());
            case ALL_QUOTES:
                try{
                    return wrapper.getQuoteOwner().isSufficientPricingData() &&
                            isNotHiddenMessage(wrapper.getQuoteOwner().getInstantMessage().getMessageContent());
                }catch (Exception ex){
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                    return false;
                }
            case OUTGOING_MESSAGES:
                try{
                    return wrapper.getQuoteOwner().getInstantMessage().isOutgoing() &&
                            isNotHiddenMessage(wrapper.getQuoteOwner().getInstantMessage().getMessageContent());
                }catch (Exception ex){
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                    return false;
                }
            case INCOMING_MESSAGES:
                try{
                    return !(wrapper.getQuoteOwner().getInstantMessage().isOutgoing()) &&
                            isNotHiddenMessage(wrapper.getQuoteOwner().getInstantMessage().getMessageContent());
                }catch (Exception ex){
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                    return false;
                }
            default:
                return true;
        }
    }

    private boolean switchWorkingForCriteria(IPbsysOptionQuote quote) {
        IPbsysQuoteLeg quoteLeg = quote.getOptionStrategyLegs().get(criteria.getFilterLegIndex());
        if (criteria instanceof IViewerFilterByStrategies){
            IViewerFilterByStrategies ct = (IViewerFilterByStrategies)criteria;
            ArrayList<String> strategies = ct.getStrategies();
            if ((strategies == null) || (strategies.isEmpty())){
                return true;
            }else{
                boolean result = false;
                String searchStructureTemp;
                for (int i = 0; i < ct.getStrategies().size(); i++) {
                    searchStructureTemp = ct.getStrategies().get(i);
                    if (quoteLeg.getOptionStrategy().equalsIgnoreCase(searchStructureTemp)) {
                        result = true;
                        break;
                    }
                }
                return result;
            }
        }else if (criteria instanceof IViewerFilterByLocations){
            IViewerFilterByLocations ct = (IViewerFilterByLocations)criteria;
            ArrayList<String> locations = ct.getLocations();
            if ((locations == null) || (locations.isEmpty())){
                return true;
            }else{
                boolean result = false;
                String searchLocationTemp;
                for (int i = 0; i < locations.size(); i++) {
                    searchLocationTemp = ct.getLocations().get(i);
                    if (quoteLeg.getOptionProduct().equalsIgnoreCase(searchLocationTemp)) {
                        result = true;
                        break;
                    }
                }
                return result;
            }
        }else if (criteria instanceof IViewerFilterByClass){
            IViewerFilterByClass ct = (IViewerFilterByClass)criteria;
            ArrayList<String> searchClasses = ct.getCriteriaClassValues();
            if ((searchClasses == null) || (searchClasses.isEmpty())){
                return true;
            }else{
                boolean result = false;
                String classValue = quote.getPbcPricingModel().getSqClass();
                for (int i = 0; i < searchClasses.size(); i++) {
                    if (searchClasses.get(i).equalsIgnoreCase(classValue)) {
                        result = true;
                        break;
                    }
                }
                return result;
            }
        }else if (criteria instanceof IViewerFilterByGroup){
            IViewerFilterByGroup ct = (IViewerFilterByGroup)criteria;
            ArrayList<String> searchGroups = ct.getCriteriaGroupValues();
            if ((searchGroups == null) || (searchGroups.isEmpty())){
                return true;
            }else{
                boolean result = false;
                String groupValue = quote.getPbcPricingModel().getSqGroup();
                for (int i = 0; i < searchGroups.size(); i++) {
                    if (searchGroups.get(i).equalsIgnoreCase(groupValue)) {
                        result = true;
                        break;
                    }
                }
                return result;
            }
        }else if (criteria instanceof IViewerFilterByCode){
            IViewerFilterByCode ct = (IViewerFilterByCode)criteria;
            ArrayList<String> searchCodes = ct.getCriteriaCodeValues();
            if ((searchCodes == null) || (searchCodes.isEmpty())){
                return true;
            }else{
                boolean result = false;
                String codeValue = quote.getPbcPricingModel().getSqCode();
                for (int i = 0; i < searchCodes.size(); i++) {
                    if (searchCodes.get(i).equalsIgnoreCase(codeValue)) {
                        result = true;
                        break;
                    }
                }
                return result;
            }
        }else if (criteria instanceof IViewerFilterByBrokers){
            IViewerFilterByBrokers ct = (IViewerFilterByBrokers)criteria;
            ArrayList<String> searchBrokers = ct.getBrokerUniqueNames();
            if ((searchBrokers == null) || (searchBrokers.isEmpty())){
                return true;
            }else{
                ArrayList<String> searchBrokersList = ct.getSearchBrokersList();
                if ((searchBrokersList == null) || (searchBrokersList.isEmpty())){
                    return true;
                }else{
                    boolean result = false;
                    String brokerTemp;
                    if(quote.getInstantMessage().isOutgoing()){
                        brokerTemp = quote.getLocalBroker().getIMUniqueName();
                    }else{
                        brokerTemp = quote.getRemoteBroker().getIMUniqueName();
                    }
                    for (int i = 0; i < searchBrokersList.size(); i++) {
                        if (searchBrokersList.get(i).equalsIgnoreCase(brokerTemp)) {
                            result = true;
                            break;
                        }
                    }
                    return result;
                }
            }
        }else if (criteria instanceof IViewerFilterByStrikes){
            IViewerFilterByStrikes ct = (IViewerFilterByStrikes)criteria;
            ArrayList<String> searchStrikesList = ct.getStrikeString();
            if ((searchStrikesList == null) || (searchStrikesList.isEmpty())){
                return true;
            }else{
                boolean result = false;
                Double tempDouble;
                for (int i = 0; i < quoteLeg.getOptionStrikes().length; i++) {
                    tempDouble = new Double(quoteLeg.getOptionStrikes(i));
                    if (searchStrikesList.contains(tempDouble.toString()) && quoteLeg.getOptionStrikes()[i] != 0.0) {
                        result = true;
                        break;
                    }
                }
                return result;
            }
        }else if (criteria instanceof IViewerFilterByPeriods){
            IViewerFilterByPeriods ct = (IViewerFilterByPeriods)criteria;
            ViewerPeriodsOperator fromOperatorTerm = ct.getFromOperatorTerm();
            if (fromOperatorTerm == null){
                return true;
            }
            ViewerPeriodsOperator toOperatorTerm = ct.getToOperatorTerm();
            if (toOperatorTerm == null){
                return true;
            }
            GregorianCalendar start = ct.getFromDate();
            if (start == null){
                return true;
            }
            GregorianCalendar end = ct.getToDate();
            if (end == null){
                return true;
            }
            end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
            
            if ((quoteLeg.getOptionContractStartDate() == null || quoteLeg.getOptionContractEndDate() == null)) {
                return false;
            } else {
                boolean result = false;
//                        String strStart_1 = CalendarGlobal.convertToMMddyyyyHHmmss(start, "-", "@", ":");
//                        System.out.println(">>>> strStart_1: " + strStart_1);
//                        String strStart_2 = CalendarGlobal.convertToMMddyyyyHHmmss(quoteLeg.getOptionContractStartDate(), "-", "@", ":");
//                        System.out.println(">>>> strStart_2: " + strStart_2);
                if (fromOperatorTerm == ViewerPeriodsOperator.LessThan) {
                    result = quoteLeg.getOptionContractStartDate().before(start);
                } else if (fromOperatorTerm == ViewerPeriodsOperator.LessThanEqualTo) {
                    result = quoteLeg.getOptionContractStartDate().before(start) || quoteLeg.getOptionContractStartDate().equals(start);
                } else if (fromOperatorTerm == ViewerPeriodsOperator.EqualTo) {
                    result = quoteLeg.getOptionContractStartDate().equals(start);
                } else if (fromOperatorTerm == ViewerPeriodsOperator.GreaterThan) {
                    result = quoteLeg.getOptionContractStartDate().after(start);
                } else if (fromOperatorTerm == ViewerPeriodsOperator.GreaterThanEqualTo) {
                    result = quoteLeg.getOptionContractStartDate().after(start) || quoteLeg.getOptionContractStartDate().equals(start);
                }

                if (result) {
//                        String strEnd_1 = CalendarGlobal.convertToMMddyyyyHHmmss(end, "-", "@", ":");
//                        System.out.println(">>>> strEnd_1: " + strEnd_1);
//                        String strEnd_2 = CalendarGlobal.convertToMMddyyyyHHmmss(quoteLeg.getOptionContractEndDate(), "-", "@", ":");
//                        System.out.println(">>>> strEnd_2: " + strEnd_2);
                    if (toOperatorTerm == ViewerPeriodsOperator.LessThan) {
                        result = result && quoteLeg.getOptionContractEndDate().before(end);
                    } else if (toOperatorTerm == ViewerPeriodsOperator.LessThanEqualTo) {
                        result = result && (quoteLeg.getOptionContractEndDate().before(end) || quoteLeg.getOptionContractEndDate().equals(end));
                    } else if (toOperatorTerm == ViewerPeriodsOperator.EqualTo) {
                        result = result && quoteLeg.getOptionContractEndDate().equals(end);
                    } else if (toOperatorTerm == ViewerPeriodsOperator.GreaterThan) {
                        result = result && quoteLeg.getOptionContractEndDate().after(end);
                    } else if (toOperatorTerm == ViewerPeriodsOperator.GreaterThanEqualTo) {
                        result = result && (quoteLeg.getOptionContractEndDate().after(end) || quoteLeg.getOptionContractEndDate().equals(end));
                    }
                }
                return result;
            }
        }else{
            return false;
        }
    }

    private boolean isNotHiddenMessage(String message)
    {
        if (properties == null){
            properties = PointBoxHiddenMessagesProperties.getSingleton(viewer.getKernel());
        }
        return !(properties.isHiddenMessage(message));
    }
}
