/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier;
import static com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier.QuoteClass;
import static com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier.QuoteSource;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SortOrder;

/**
 * ViewerTableSorter.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 25, 2010, 12:32:03 PM
 */
class ViewerTableSorter implements Comparator<IPbsysOptionQuoteWrapper>{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(ViewerTableSorter.class.getName());
    }
    private ViewerColumnIdentifier viewerColumnIdentifier;
    private IViewerTablePanel ownerTablePanel;

    ViewerTableSorter(IViewerTablePanel ownerTablePanel, ViewerColumnIdentifier viewerColumnIdentifier) {
        this.ownerTablePanel = ownerTablePanel;
        this.viewerColumnIdentifier = viewerColumnIdentifier;
    }

    @Override
    public int compare(IPbsysOptionQuoteWrapper wrapper01, IPbsysOptionQuoteWrapper wrapper02) {
        if ((wrapper01.getQuoteOwner() == null) && (wrapper02.getQuoteOwner() == null)){
            return 0;
        }
        SortOrder sortOrder = ownerTablePanel.getPbcRuntime().getViewerColumnSortOrder(ownerTablePanel.getViewerTableName(), viewerColumnIdentifier);
        if (sortOrder.equals(SortOrder.DESCENDING)){
            if (wrapper01.getQuoteOwner() == null){
                return -1;
            }
            if (wrapper02.getQuoteOwner() == null){
                return 1;
            }
        }else{
            if (wrapper01.getQuoteOwner() == null){
                return 1;
            }
            if (wrapper02.getQuoteOwner() == null){
                return -1;
            }
        }
        try{
            switch (viewerColumnIdentifier){
                case BuySell:
                    return compareStrings(wrapper01.getBuySellFaceValue(), wrapper02.getBuySellFaceValue());
                case TimeStamp:
                    return compareGregorianCalendars(wrapper01.getTimestampFaceValue(), wrapper02.getTimestampFaceValue());
                case Period:
                    return compareStrings(wrapper01.getPeriodFaceValue(), wrapper02.getPeriodFaceValue());
                case Strike:
                    return compareStrings(wrapper01.getStrikeFaceValue(), wrapper02.getStrikeFaceValue());
                case Structure:
                    return compareStrings(wrapper01.getStructureFaceValue(), wrapper02.getStructureFaceValue());
                case Cross:
                    return compareStrings(wrapper01.getCrossFaceValue(), wrapper02.getCrossFaceValue());
                case Bid:
                    return compareDoubles(wrapper01.getBidFaceValue(), wrapper02.getBidFaceValue());
                case Offer:
                    return compareDoubles(wrapper01.getOfferFaceValue(), wrapper02.getOfferFaceValue());
                case Last:
                    return compareDoubles(wrapper01.getLastFaceValue(), wrapper02.getLastFaceValue());
                case PbsysPrice:
                    return compareDoubles(wrapper01.getPbsysPriceFaceValue(), wrapper02.getPbsysPriceFaceValue());
                case Swap01:
                    return compareDoubles(wrapper01.getSwap01FaceValue(), wrapper02.getSwap01FaceValue());
                case RemoteBrokerHouse:
                    return compareStrings(wrapper01.getRemoteBrokerHouseFaceValue(), wrapper02.getRemoteBrokerHouseFaceValue());
                case Delta:
                    return compareDoubles(wrapper01.getDeltaFaceValue(), wrapper02.getDeltaFaceValue());
                case QuoteMessage:
                    return compareStrings(wrapper01.getQuoteMessageFaceValue(), wrapper02.getQuoteMessageFaceValue());
                case DDelta:
                    return compareDoubles(wrapper01.getDDeltaFaceValue(), wrapper02.getDDeltaFaceValue());
                case Swap02:
                    return compareDoubles(wrapper01.getSwap02FaceValue(), wrapper02.getSwap02FaceValue());
                case Theta:
                    return compareDoubles(wrapper01.getThetaFaceValue(), wrapper02.getThetaFaceValue());
                case Vega:
                    return compareDoubles(wrapper01.getVegaFaceValue(), wrapper02.getVegaFaceValue());
                case Gamma:
                    return compareDoubles(wrapper01.getGammaFaceValue(), wrapper02.getGammaFaceValue());
                case DGamma:
                    return compareDoubles(wrapper01.getDGammaFaceValue(), wrapper02.getDGammaFaceValue());
                case QuoteClass:
                    return compareStrings(wrapper01.getQuoteClassValue(), wrapper02.getQuoteClassValue());
                case QuoteGroup:
                    return compareStrings(wrapper01.getQuoteGroupValue(), wrapper02.getQuoteGroupValue());
                case QuoteCode:
                    return compareStrings(wrapper01.getQuoteCodeValue(), wrapper02.getQuoteCodeValue());
                case QuoteSource:
                    return compareStrings(wrapper01.getQuoteSource(), wrapper02.getQuoteSource());
                case RowNumber:
                    return compareIntegers(wrapper01.getTableModelRowIndex(), wrapper02.getTableModelRowIndex());
                default:
                    return compareObjects(wrapper01, wrapper02);
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return 0;
        }
    }

    private int compareStrings(String s_01, String s_02) {
        if ((s_01 == null) && (s_02 == null)){
            return 0;
        }
        if (s_01 == null){
            return 1;
        }
        if (s_02 == null){
            return -1;
        }
        return s_01.compareTo(s_02);
    }

    private int compareGregorianCalendars(GregorianCalendar gc_01, GregorianCalendar gc_02) {
        if ((gc_01 == null) && (gc_02 == null)){
            return 0;
        }
        if (gc_01 == null){
            return 1;
        }
        if (gc_02 == null){
            return -1;
        }
        return gc_01.compareTo(gc_02);
    }

    private int compareDoubles(Double d_01, Double d_02) {
        if ((d_01 == null) && (d_02 == null)){
            return 0;
        }
        if (d_01 == null){
            return 1;
        }
        if (d_02 == null){
            return -1;
        }
        return d_01.compareTo(d_02);
    }

    private int compareIntegers(Integer i_01, Integer i_02 ){
        if ((i_01 == null) && (i_02 == null)){
            return 0;
        }
        if (i_01 == null){
            return 1;
        }
        if (i_02 == null){
            return -1;
        }
        return i_01.compareTo(i_02);
    }

    private int compareObjects(Object o_01, Object o_02) {
        if ((o_01 == null) && (o_02 == null)){
            return 0;
        }
        if (o_01 == null){
            return 1;
        }
        if (o_02 == null){
            return -1;
        }
        return o_01.toString().compareTo(o_02.toString());
    }

}
