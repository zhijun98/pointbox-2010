/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.search;

import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * ViewerSearchFactory.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 17, 2010, 1:06:15 AM
 */
public class ViewerSearchFactory {
    private static IPbconsoleDialog viewerFilterDialog;
    private static IPbconsoleDialog viewerSearchDialog;
    static{
        viewerFilterDialog = null;
        viewerSearchDialog = null;
    }

    public static IViewerFilterCriteria getViewerFilterByStrategiesInstance(ArrayList<String> items){
        return new ViewerFilterByStrategies(items);
    }

    public static IViewerFilterCriteria getViewerFilterByBrokersInstance(ArrayList<String> items){
        return new ViewerFilterByBrokers(items);
    }

    public static IViewerFilterCriteria getViewerFilterByClassInstance(ArrayList<String> items){
        return new ViewerFilterByClass(items);
    }

    public static IViewerFilterCriteria getViewerFilterByGroupInstance(ArrayList<String> items){
        return new ViewerFilterByGroup(items);
    }

    public static IViewerFilterCriteria getViewerFilterByCodeInstance(ArrayList<String> items){
        return new ViewerFilterByCode(items);
    }

    public static IViewerFilterCriteria getViewerFilterByLocationsInstance(ArrayList<String> items){
        return new ViewerFilterByLocations(items);
    }

    public static IViewerFilterCriteria getViewerFilterByStrikesInstance(ArrayList<String> items){
        return new ViewerFilterByStrikes(items);
    }

    public static IViewerFilterCriteria getViewerFilterByPeriodsInstance(GregorianCalendar selectedStartDate,
                                                                         GregorianCalendar selectedEndDate,
                                                                         ViewerPeriodsOperator startOperator,
                                                                         ViewerPeriodsOperator endOperator)
    {
        return new ViewerFilterByPeriods(selectedStartDate,
                                         selectedEndDate,
                                         startOperator,
                                         endOperator);
    }
    
    public static IPbconsoleDialog getViewerFilterDialogInstance(IPbcViewer viewer, boolean modal, boolean newFilter){
        return new ViewerFilterDialog(viewer, modal, newFilter);
    }

    public static IPbconsoleDialog getViewerSearchDialogSingleton(IPbcViewer viewer, boolean modal){
        if (viewerSearchDialog == null){
            viewerSearchDialog = new ViewerSearchDialog(viewer, modal);
        }
        return viewerSearchDialog;
    }

    private ViewerSearchFactory() {
    }
}
