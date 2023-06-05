/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.global.SwingGlobal.ColorName;
import com.eclipsemarkets.parser.PbsysViewerBuySellTerms;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import java.awt.Color;

/**
 * ViewerColumnCellRenderer.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 25, 2010, 10:42:05 AM
 */
class ViewerColumnCellRenderer extends ViewerCellRenderer {
    private static final long serialVersionUID = 1L;

    ViewerColumnCellRenderer(IPbcRuntime runtime, 
                             String viewerUniqueTabName,
                             ViewerColumnIdentifier columnIdentifier)
    {
        super(runtime, viewerUniqueTabName, columnIdentifier);
    }

    @Override
    void decorateSpecificColumn(IPbsysOptionQuoteWrapper wrapper) {
        switch(columnIdentifier){
            case BuySell:
                decorateBuySellColumn(wrapper);
                break;
            case QuoteMessage:
                decorateQuoteMessageColumn(wrapper);
                break;
            case PbsysPrice:
                decoratePriceColumn(wrapper);
                break;
            case RemoteBrokerHouse:
                decorateRemoteBrokerHouseColumn(wrapper);
                break;
            default:
                //do nothing
        }
    }

    /**
     * This method has to guarantee no NULL returned
     * @param wrapper
     * @return
     */
    @Override
    public String getTableCellValueText(IPbsysOptionQuoteWrapper wrapper) {
        String value = "";
        if (wrapper != null){
            
            int bidAskStatus = 0;
            if (wrapper.getQuoteOwner() != null){
                bidAskStatus = wrapper.getQuoteOwner().getBidAskStatus();
            }
            switch (columnIdentifier){
                case BuySell:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = wrapper.getBuySellFaceValue();
                    }
                    break;
                case TimeStamp:
                    if (wrapper.getQuoteOwner() == null){
                        value = "";
                    }else{
                        value = CalendarGlobal.convertToHHmmssAmPm(wrapper.getTimestampFaceValue(), ":");
                    }
                    break;
                case Period:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = wrapper.getPeriodFaceValue();
                    }
                    break;
                case Strike:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = wrapper.getStrikeFaceValue();
                    }
                    break;
                case Structure:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = wrapper.getStructureFaceValue();
                    }
                    break;
                case Cross:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = wrapper.getCrossFaceValue();
                    }
                    break;
                case Bid:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        if ((bidAskStatus == 1) || (bidAskStatus == 3)){
                            //value = PbcGlobal.localFormatStringByDoublePrecisionWithoutRemovingNegative(wrapper.getBidFaceValue(), 4, "0.0000");
                            if (wrapper.getBidFaceValue() != null){
                                value = DataGlobal.formatDoubleWithMinMax(wrapper.getBidFaceValue(), wrapper.getMinFractionNum(), wrapper.getMaxFractionNum());
                            }
                        }else{
                            value = "-";
                        }
                    }
                    break;
                case Offer:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else if ((bidAskStatus == 2) || (bidAskStatus == 3)){
                        //value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getOfferFaceValue(), 4, "0.0000");
                        if (wrapper.getOfferFaceValue() != null){
                            value = DataGlobal.formatDoubleWithMinMax(wrapper.getOfferFaceValue(), wrapper.getMinFractionNum(), wrapper.getMaxFractionNum());
                        }
                    }else{
                        value = "-";
                    }
                    break;
                case Last:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        //value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getLastFaceValue(), 4, "-");
                        if (wrapper.getLastFaceValue() != null){
                            value = DataGlobal.formatDoubleWithMinMax(wrapper.getLastFaceValue(), wrapper.getMinFractionNum(), wrapper.getMaxFractionNum());
                        }
                    }
                    break;
                case PbsysPrice:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        //value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getPbsysPriceFaceValue(), 4, "0");
                        if (wrapper.getPbsysPriceFaceValue() != null){
                            value = DataGlobal.formatDoubleWithMinMax(wrapper.getPbsysPriceFaceValue(), wrapper.getMinFractionNum(), wrapper.getMaxFractionNum());
                        }
                    }
                    break;
                case Swap01:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        //value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getSwap01FaceValue(), 4, "0");
                        if (wrapper.getSwap01FaceValue() != null){
                            value = DataGlobal.formatDoubleWithMinMax(wrapper.getSwap01FaceValue(), wrapper.getMinFractionNum(), wrapper.getMaxFractionNum());
                        }
                    }
                    break;
                case RemoteBrokerHouse:
                    if (wrapper.getQuoteOwner() == null){
                        value = "";
                    }else{
                        value = wrapper.getRemoteBrokerHouseFaceValue();
                    }
                    break;
                case Delta:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getDeltaFaceValue(), 4, "0");
                    }
                    break;
                case QuoteMessage:
                    if (wrapper.getQuoteOwner() == null){
                        value = "";
                    }else{
                        value = wrapper.getQuoteMessageFaceValue();
                    }
                    break;
                case DDelta:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getDDeltaFaceValue(), 4, "0");
                    }
                    break;
                case Swap02:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        if (wrapper.getSwap02FaceValue() != null){
                            value = DataGlobal.formatDoubleWithMinMax(wrapper.getSwap02FaceValue(), wrapper.getMinFractionNum(), wrapper.getMaxFractionNum());
                        }
                    }
                    break;
                case Theta:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getThetaFaceValue(), 4, "0");
                    }
                    break;
                case Vega:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getVegaFaceValue(), 4, "0");
                    }
                    break;
                case Gamma:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getGammaFaceValue(), 4, "0");
                    }
                    break;
                case DGamma:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        value = "";
                    }else{
                        value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getDGammaFaceValue(), 4, "0");
                    }
                    break;
                case QuoteClass:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        return "";
                    }
                    value = wrapper.getQuoteClassValue();
                    break;
                case QuoteGroup:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        return "";
                    }
                    value = wrapper.getQuoteGroupValue();
                    break;
                case QuoteCode:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        return "";
                    }
                    value = wrapper.getQuoteCodeValue();
                    break;
                case QuoteSource:
                    value = wrapper.getQuoteSource();
                    break;
                case RowNumber:
                    if (wrapper.getQuoteOwner() == null){
                        value = "";
                    }else{
                        value = wrapper.getRowNumberFaceValue();
                    }
                    break;
                case Volatility:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        return "";
                    }
                    value = PbcGlobal.localFormatStringByDoublePrecision(wrapper.getVolatilityFaceValue(), 4, "");
                    if (DataGlobal.isEmptyNullString(value)){
                        value = "";
                    }
                    break;
                case UnderlierType:
                    if ((wrapper.getQuoteOwner() == null) || (!wrapper.getQuoteOwner().isSufficientPricingData())){
                        return "";
                    }
                    value = wrapper.getUnderlierVlaue();
                    if (DataGlobal.isEmptyNullString(value)){
                        value = "";
                    }
                    break;
                default:
                    value = "";
            }
        }
        return value;
    }

    private void decoratePriceColumn(IPbsysOptionQuoteWrapper wrapper) {
        if (wrapper.getQuoteOwner() == null){
            return;
        }
        Double price = wrapper.getPbsysPriceFaceValue();
        Double ask = wrapper.getOfferFaceValue();
        Double bid = wrapper.getBidFaceValue();
        String priceStr = PbcGlobal.localFormatStringByDoublePrecision(price, 4, "0");
        if ((priceStr != null) && (!priceStr.isEmpty()) && (!priceStr.contains("999"))){
            if (price >= 0.0001){
                /**
                 * lloydbloom <09:50:36> if no bid and no ask then yellow
                 * lloydbloom <09:51:20> if price > bid and no ask then yellow
                 * lloydbloom <09:52:42> but if price < bid and no ask then red as always
                 * lloydbloom <09:53:14> default color is yellow
                 * zzj [09:54:54] you did not mention  "equal" case. That's why I ask default color
                 * lloydbloom <09:55:49> if price=>bid and no ask then yellow (should have read like this)
                 */
                int bidAskStatus = wrapper.getQuoteOwner().getBidAskStatus();
                if (bidAskStatus == 0){         //no bid and no ask
                    setForeground(runtime.getBpa_FgColor(viewerUniqueTabName));
                    setBackground(runtime.getBpa_BgColor(viewerUniqueTabName));  //yellow-like
                }else if (bidAskStatus == 1){   //only bid but no ask
                    if (price >= bid){
                        setForeground(runtime.getBpa_FgColor(viewerUniqueTabName));
                        setBackground(runtime.getBpa_BgColor(viewerUniqueTabName));  //yellow-like
                    }else{
                        setForeground(runtime.getPb_FgColor(viewerUniqueTabName));
                        setBackground(runtime.getPb_BgColor(viewerUniqueTabName));        //red-like
                    }
                }else if (bidAskStatus == 2){   //only ask but no bid
                    if (price > ask){
                        setForeground(runtime.getPa_FgColor(viewerUniqueTabName));
                        setBackground(runtime.getPa_BgColor(viewerUniqueTabName));        //dark-green-like
                    }else {
                        setForeground(runtime.getBpa_FgColor(viewerUniqueTabName));
                        setBackground(runtime.getBpa_BgColor(viewerUniqueTabName));  //yellow-like
                    }
                }else if (bidAskStatus == 3){   //both
                    if (price > ask){
                        setForeground(runtime.getPa_FgColor(viewerUniqueTabName));
                        setBackground(runtime.getPa_BgColor(viewerUniqueTabName));        //dark-green-like
                    }else if (price < bid){
                        setForeground(runtime.getPb_FgColor(viewerUniqueTabName));
                        setBackground(runtime.getPb_BgColor(viewerUniqueTabName));        //red-like
                    }else if ((price >= bid) && (price <= ask)){
                        setForeground(runtime.getBpa_FgColor(viewerUniqueTabName));
                        setBackground(runtime.getBpa_BgColor(viewerUniqueTabName));  //yellow
                    }
                }
            }
        }//if
    }

    private void decorateQuoteMessageColumn(IPbsysOptionQuoteWrapper wrapper) {
        IPbsysOptionQuote quote = wrapper.getQuoteOwner();
        if (quote == null){
            return;
        }
        if (quote.getInstantMessage().isOutgoing()){
            setBackground(runtime.getOutgoingBackground(viewerUniqueTabName));
            setForeground(runtime.getOutgoingForeground(viewerUniqueTabName));
        }else{
            if (quote.getRemoteBroker().getHouseType().equalsIgnoreCase("power")){
                setBackground(runtime.getSkippedQtBgColor(viewerUniqueTabName));
                setForeground(runtime.getSkippedQtBgColor(viewerUniqueTabName));
            }else{
                if (quote.isSufficientPricingData()){
                    if (quote.getRemoteBroker().getIMServerType().equals(GatewayServerType.PBIM_SERVER_TYPE)){
                        setBackground(runtime.getPbimQtBgColor(viewerUniqueTabName));        //dark green
                        setForeground(runtime.getPbimQtFgColor(viewerUniqueTabName));    //white
                    }else{
                        setBackground(runtime.getQtBgColor(viewerUniqueTabName));        //dark blue
                        setForeground(runtime.getQtFgColor(viewerUniqueTabName));
                    }
                }else{
                    setForeground(runtime.getMsgFgColor(viewerUniqueTabName));
                    setBackground(runtime.getMsgBgColor(viewerUniqueTabName));
                }//if
            }
        }
    }

    private void decorateRemoteBrokerHouseColumn(IPbsysOptionQuoteWrapper wrapper) {
        this.setForeground(Color.BLUE);
    }

    private void decorateBuySellColumn(IPbsysOptionQuoteWrapper wrapper) {
        String buySellFaceValue = wrapper.getBuySellFaceValue();
        if (DataGlobal.isNonEmptyNullString(buySellFaceValue)){
            if (buySellFaceValue.equalsIgnoreCase(PbsysViewerBuySellTerms.BUY.toString())){
                setForeground(Color.RED);
            }else if (buySellFaceValue.equalsIgnoreCase(PbsysViewerBuySellTerms.SELL.toString())){
                setForeground(SwingGlobal.getColor(ColorName.DARK_GREEN));
            }else if (buySellFaceValue.equalsIgnoreCase(PbsysViewerBuySellTerms.TRADE.toString())){
                setForeground(SwingGlobal.getColor(ColorName.DARK_BLUE));
            }
        }
    }
}
