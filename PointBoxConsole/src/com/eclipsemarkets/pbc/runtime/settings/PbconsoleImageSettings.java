/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.PbcImageFileName;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Zhijun Zhang
 */
class PbconsoleImageSettings extends PbconsoleSettings implements IPbconsoleImageSettings{

    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbconsoleImageSettings.class.getName());
    }

    private ImageIcon viewerFreezingIcon;
    private ImageIcon viewerScrollingIcon;
    private ImageIcon saveAllIcon;
    private ImageIcon proxyServerIcon;
    private ImageIcon priceSettingsDownloadIcon;
    private ImageIcon chartLineIcon;
    private ImageIcon chartCurveIcon;
    private ImageIcon chartLineEditIcon;
    private ImageIcon chartCurveEditIcon;
    private ImageIcon pageRefreshIcon;
    private ImageIcon tradeEntryIcon;
    private ImageIcon openedGroupIcon;
    private ImageIcon closedGroupIcon;
    private ImageIcon aimGreyIcon;
    private ImageIcon aimRedIcon;
    private ImageIcon aimTinyIcon;
    private ImageIcon aim24Icon;
    private ImageIcon yim24Icon;
    private ImageIcon aimOnline16Icon;
    private ImageIcon yimOnline16Icon;
    private ImageIcon aimOffline16Icon;
    private ImageIcon yimOffline16Icon;
    private ImageIcon yahooGreyIcon;
    private ImageIcon yahooRedIcon;
    private ImageIcon yimTinyIcon;
    private ImageIcon pbGreyIcon;
    private ImageIcon pbRedIcon;
    private ImageIcon pbimTinyIcon;
    private ImageIcon horizontalFrameLayoutIcon;
    private ImageIcon verticalFrameLayoutIcon;
    private ImageIcon interestRateIcon;
    private ImageIcon forwardCurveIcon;
    private ImageIcon volatilityIcon;
    private ImageIcon compIcon;
    private ImageIcon stripIcon;
    private ImageIcon paletteIcon;
    private ImageIcon fontStyleIcon;
    private ImageIcon configIcon;
    private ImageIcon viewerCleanupIcon;
    private ImageIcon floatingFramesIcon;
    private ImageIcon dockingFramesIcon;
    private ImageIcon filterIcon;
    private ImageIcon pointBoxIcon;
    private ImageIcon pointBoxRedIcon;
    private ImageIcon pointBoxYellowIcon;
    private ImageIcon systemShutdownIcon;
    private ImageIcon publishIcon;
    private ImageIcon searchIcon;
    private ImageIcon sortingAZIcon;
    private ImageIcon sortingZAIcon;
    private ImageIcon popUpIcon;
    private ImageIcon lookUpTabIcon;
    private ImageIcon closeAllTabsIcon;
    private ImageIcon renameViewerTabIcon;
    private ImageIcon floatingViewerIcon;
    private ImageIcon exportAllQuotesIcon;
    private ImageIcon closeViewerTabIcon;
    private ImageIcon closeAllViewerTabsIcon;
    private ImageIcon editGroupIcon;
    private ImageIcon deleteGroupIcon;
    private ImageIcon checkIcon;
    private ImageIcon uncheckIcon;
    private ImageIcon broadcastIcon;
    private ImageIcon addGroupIcon;
    private ImageIcon addBuddyIcon;
    private ImageIcon groupGoIcon;
    private ImageIcon templateBroadcastIcon;
    private ImageIcon templateExpandIcon;
    private ImageIcon templateCollapseIcon;
    private ImageIcon templateResetIcon;
    private ImageIcon templateCloseIcon;
    private ImageIcon weatherIcon;
    private ImageIcon soundOnIcon;
    private ImageIcon soundOffIcon;
    private ImageIcon addSingleIcon;
    private ImageIcon addAllIcon;
    private ImageIcon removeSingleIcon;
    private ImageIcon removeAllIcon;
    private ImageIcon onlineUpdateRedIcon;
    private ImageIcon onlineUpdateGreenIcon;
    private ImageIcon archiveIcon;
    private ImageIcon persistIcon;
    private ImageIcon serverPricerIcon;
    private ImageIcon localPricerIcon;
    private ImageIcon addIcon;
    private ImageIcon removeIcon;
    private ImageIcon pointBoxYellowIcon16;
    private ImageIcon pointBoxIcon16;
    private ImageIcon pricerIcon;
    private ImageIcon percentIcon;
    private ImageIcon computationIcon;
    private ImageIcon forwardIcon;
    private ImageIcon volIcon;
    private ImageIcon clearportIcon;
    private ImageIcon pointBoxOfflineIcon16;
    private ImageIcon broadcastBackgroundPng;
    private ImageIcon pitsCastToCurrentPng;
    private ImageIcon pitsCastToAllPng;
    
    
     
    private final String imageFolder;

    PbconsoleImageSettings(IPbcRuntime runtime) {
        super(runtime);

        imageFolder = "resources/images/";

        onlineUpdateRedIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.OnlineUpdateRedIcon));
        onlineUpdateGreenIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.OnlineUpdateGreenIcon));

        addSingleIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AddSinglePng));
        addAllIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AddAllPng));
        removeSingleIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.RemoveSinglePng));
        removeAllIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.RemoveAllPng));

        templateBroadcastIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.TemplateBroadcastPng));
        templateExpandIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.TemplateExpandPng));
        templateCollapseIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.TemplateCollapsePng));
        templateResetIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.TemplateResetPng));
        templateCloseIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.TemplateClosePng));
        pageRefreshIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PageRefreshPng));
        tradeEntryIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.ApplicationFormPng));
        editGroupIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.GroupEditPng));
        deleteGroupIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.GroupDeletePng));
        checkIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.CheckPng));
        uncheckIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.UncheckPng));
        broadcastIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.BroadcastPng));
        addGroupIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.GroupAddPng));
        addBuddyIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.BuddyAddPng));
        groupGoIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.GroupGoPng));
        renameViewerTabIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.RenameViewerTabpng));
        floatingViewerIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.FloatingViewerpng));
        exportAllQuotesIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.ExportAllQuotespng));
        closeViewerTabIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.CloseViewerTabpng));
        closeAllViewerTabsIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.CloseAllViewerTabspng));
        sortingAZIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.SortAZpng));
        sortingZAIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.SortZApng));
        lookUpTabIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.LookUppng));
        popUpIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.popUppng));
        closeAllTabsIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.CloseAll16png));
        openedGroupIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.OpenedGroupFolder));
        closedGroupIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.ClosedGroupFolder));
        proxyServerIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.ProxyServer));
        saveAllIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.SaveAll));
        viewerFreezingIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Pin));
        viewerScrollingIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Go));
        chartLineIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Chart_Line_Image));
        priceSettingsDownloadIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PageRefresh));
        chartCurveIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Chart_Curve_Image));
        chartLineEditIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Chart_Line_Edit));
        chartCurveEditIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Chart_Curve_Edit));
        aimGreyIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AIM_GREY_IMAGE));
        aimRedIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AIM_RED_IMAGE));
        aimTinyIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AIM_TINY_LOGO));
        aim24Icon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AIM_24_IMAGE));
        yim24Icon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.YIM_24_IMAGE));
        aimOnline16Icon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AIM_ONLINE16_IMAGE));
        yimOnline16Icon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.YIM_ONLINE16_IMAGE));
        aimOffline16Icon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.AIM_OFFLINE16_IMAGE));
        yimOffline16Icon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.YIM_OFFLINE16_IMAGE));
        yahooGreyIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.YIM_GREY_IMAGE));
        yahooRedIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.YIM_RED_IMAGE));
        yimTinyIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.YIM_TINY_LOGO));
        pbGreyIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PBIM_GREY_IMAGE));
        pbRedIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PBIM_RED_IMAGE));
        pbimTinyIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PBIM_TINY_LOGO));
        horizontalFrameLayoutIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.RotateH16Png));
        verticalFrameLayoutIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.RotateV16Png));
        interestRateIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.InterestRate36X16png));
        forwardCurveIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.ForwardCurve36X16png));
        volatilityIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Volatility36X16png));
        compIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Comp51X16png));
        stripIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Strip51X16png));
        paletteIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.palette16png));
        fontStyleIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.FontStylepng));
        configIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Config16png));
        viewerCleanupIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.ViewerCleanup16Png));
        floatingFramesIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.FloatingFramesPng));
        dockingFramesIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.DockingFramesPng));
        filterIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Filter16Png));
        pointBoxYellowIcon16 = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PointBoxConsoleYellowLogo16));
        pointBoxIcon16 = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PointBoxConsoleLogo16));
        pointBoxIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PointBoxConsoleLogo24));
        pointBoxRedIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PointBoxConsoleRedLogo100));
        pointBoxYellowIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PointBoxConsoleYellowLogo100));
        systemShutdownIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.shutdown16png));
        publishIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Publish16png));
        searchIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Search16Png));
        weatherIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Weather16Png));
        soundOnIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.SoundOn));
        soundOffIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.SoundOff));
        archiveIcon = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.ArchiveIcon));
        persistIcon= SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PersistIcon));
        serverPricerIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.pricerServerPng));
        localPricerIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.pricerLocalPng));
        addIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Add));
        removeIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Remove));
        pricerIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Pricer_Image));
        percentIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Percent_Image));
        computationIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Comp_Image));
        volIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Volatility_Image));
        forwardIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Foward_Image));
        clearportIcon=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.Clearport_Image));
        pointBoxOfflineIcon16=SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PointBoxOfflineLogo16));
        broadcastBackgroundPng = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.BroadcastBackgroundPng));
        pitsCastToCurrentPng = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PitsCastToCurrentPng));
        pitsCastToAllPng = SwingGlobal.createImageIcon(getImageURL(PbcImageFileName.PitsCastToAllPng));
    }

    @Override
    public void loadPersonalSettings() {
        //todo: zzj - if it need persistent feature, do it here
    }

    @Override
    public void storePersonalSettings() {
        //todo: zzj - if it need persistent feature, do it here
    }

    public PbcSettingsType getPbcSettingsType() {
        return PbcSettingsType.PbcImageSettings;
    }

    @Override
    public URL getImageURL(PbcImageFileName imageFileName) {
        return getClass().getResource(imageFolder + imageFileName);
    }

    @Override
    public ImageIcon getSoundOffIcon() {
        return soundOffIcon;
    }

    @Override
    public ImageIcon getSoundOnIcon() {
        return soundOnIcon;
    }

    @Override
    public ImageIcon getWeatherIcon() {
       return weatherIcon;
    }

    @Override
    public ImageIcon getAddAllIcon() {
        return addAllIcon;
    }

    @Override
    public ImageIcon getAddSingleIcon() {
        return addSingleIcon;
    }

    @Override
    public ImageIcon getRemoveAllIcon() {
        return removeAllIcon;
    }

    @Override
    public ImageIcon getRemoveSingleIcon() {
        return removeSingleIcon;
    }

    @Override
    public ImageIcon getOnlineUpdateGreenIcon() {
        return onlineUpdateGreenIcon;
    }

    @Override
    public ImageIcon getOnlineUpdateRedIcon() {
        return onlineUpdateRedIcon;
    }

    @Override
    public ImageIcon getConnectorBuddyIcon(GatewayServerType gatewayServerType) {
        switch (gatewayServerType){
            case AIM_SERVER_TYPE:
                return aimTinyIcon;
            case YIM_SERVER_TYPE:
                return yimTinyIcon;
            case PBIM_SERVER_TYPE:
                return pbimTinyIcon;
            default:
                return getPointBoxIcon();
        }
    }

    @Override
    public ImageIcon getProxyServerIcon() {
        return proxyServerIcon;
    }

    @Override
    public ImageIcon getConnectorLogo21(GatewayServerType gatewayServerType) {
        switch (gatewayServerType){
            case AIM_SERVER_TYPE:
                return aimOffline16Icon;
            case YIM_SERVER_TYPE:
                return yimOffline16Icon;
            case PBIM_SERVER_TYPE:
                return pointBoxOfflineIcon16;
            default:
                return null;
        }
    }

    @Override
    public ImageIcon getBroadcastBackgroundPng() {
        return broadcastBackgroundPng;
    }

    @Override
    public ImageIcon getPitsCastToCurrentPng() {
        return pitsCastToCurrentPng;
    }

    @Override
    public ImageIcon getPitsCastToAllPng() {
        return pitsCastToAllPng;
    }

    @Override
    public ImageIcon getSaveAllIcon() {
        return saveAllIcon;
    }

    @Override
    public ImageIcon getViewerFreezingIcon() {
        return viewerFreezingIcon;
    }

    @Override
    public ImageIcon getViewerScrollingIcon() {
        return viewerScrollingIcon;
    }

    @Override
    public ImageIcon getAim24Icon() {
        return aim24Icon;
    }

    @Override
    public ImageIcon getYim24Icon() {
        return yim24Icon;
    }

    @Override
    public ImageIcon getAimOnline16Icon() {
        return aimOnline16Icon;
    }

    @Override
    public ImageIcon getYimOnline16Icon() {
        return yimOnline16Icon;
    }

    @Override
    public ImageIcon getAimOffline16Icon() {
        return aimOffline16Icon;
    }

    @Override
    public ImageIcon getYimOffline16Icon() {
        return yimOffline16Icon;
    }

    @Override
    public ImageIcon getPointBoxRedIcon() {
        return pointBoxRedIcon;
    }

    @Override
    public ImageIcon getPointBoxYellowIcon() {
        return pointBoxYellowIcon;
    }

    @Override
    public ImageIcon getTemplateBroadcastIcon() {
        return templateBroadcastIcon;
    }

    @Override
    public ImageIcon getTemplateCloseIcon() {
        return templateCloseIcon;
    }

    @Override
    public ImageIcon getTemplateCollapseIcon() {
        return templateCollapseIcon;
    }

    @Override
    public ImageIcon getTemplateExpandIcon() {
        return templateExpandIcon;
    }

    @Override
    public ImageIcon getTemplateResetIcon() {
        return templateResetIcon;
    }

    @Override
    public ImageIcon getPageRefreshIcon() {
        return pageRefreshIcon;
    }

    @Override
    public ImageIcon getTradeEntryIcon() {
        return tradeEntryIcon;
    }

    @Override
    public ImageIcon getGroupGoIcon() {
        return groupGoIcon;
    }

    @Override
    public ImageIcon getAddGroupIcon() {
        return addGroupIcon;
    }

    @Override
    public ImageIcon getAddBuddyIcon() {
        return addBuddyIcon;
    }

    @Override
    public ImageIcon getBroadcastIcon() {
        return broadcastIcon;
    }

    @Override
    public ImageIcon getCheckIcon() {
        return checkIcon;
    }

    @Override
    public ImageIcon getUncheckIcon() {
        return uncheckIcon;
    }

    @Override
    public ImageIcon getDeleteGroupIcon() {
        return deleteGroupIcon;
    }

    @Override
    public ImageIcon getEditGroupIcon() {
        return editGroupIcon;
    }

    @Override
    public ImageIcon getCloseAllViewerTabsIcon() {
        return closeAllViewerTabsIcon;
    }

    @Override
    public ImageIcon getCloseViewerTabIcon() {
        return closeViewerTabIcon;
    }

    @Override
    public ImageIcon getExportAllQuotesIcon() {
        return exportAllQuotesIcon;
    }

    @Override
    public ImageIcon getFloatingViewerIcon() {
        return floatingViewerIcon;
    }

    @Override
    public ImageIcon getRenameViewerTabIcon() {
        return renameViewerTabIcon;
    }

    @Override
    public ImageIcon getCloseAllTabsIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.CloseAll16png));
        return closeAllTabsIcon;
    }

    @Override
    public ImageIcon getLookUpTabIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.LookUppng));
        return lookUpTabIcon;
    }

    @Override
    public ImageIcon getPopUpIcon() {
        return popUpIcon;
    }

    @Override
    public ImageIcon getSortingAZIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.SortAZpng));
        return sortingAZIcon;
    }

    @Override
    public ImageIcon getSortingZAIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.SortZApng));
        return sortingZAIcon;
    }

    @Override
    public ImageIcon getShutdownIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.shutdown16png));
        return systemShutdownIcon;
    }

    @Override
    public ImageIcon getClosedGroupIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.ClosedGroupFolder));
        return closedGroupIcon;
    }

    @Override
    public ImageIcon getOpenedGroupIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.OpenedGroupFolder));
        return openedGroupIcon;
    }

    @Override
    public ImageIcon getChartCurveIcon() {
        return chartCurveIcon;
    }

    @Override
    public ImageIcon getChartLineIcon() {
        return chartLineIcon;
    }

    @Override
    public ImageIcon getPriceSettingsDownloadIcon() {
        return priceSettingsDownloadIcon;
    }

    @Override
    public ImageIcon getChartCurveEditIcon() {
        return chartCurveEditIcon;
    }

    @Override
    public ImageIcon getChartLineEditIcon() {
        return chartLineEditIcon;
    }

    @Override
    public ImageIcon getPointBoxIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.PointBoxConsoleLogo24));
        return pointBoxIcon;
    }

    @Override
    public ImageIcon getHorizontalFrameLayoutIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.RotateH16Png));
        return horizontalFrameLayoutIcon;
    }

    @Override
    public ImageIcon getVerticalFrameLayoutIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.RotateV16Png));
        return verticalFrameLayoutIcon;
    }

    @Override
    public ImageIcon getAimBuddyIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.AIM_TINY_LOGO));
        return aimTinyIcon;
    }

    @Override
    public ImageIcon getPbimBuddyIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.PBIM_TINY_LOGO));
        return pbimTinyIcon;
    }

    @Override
    public ImageIcon getYahooBuddyIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.YIM_TINY_LOGO));
        return yimTinyIcon;
    }

    @Override
    public ImageIcon getAimGreyIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.AIM_GREY_IMAGE));
        return aimGreyIcon;
    }

    @Override
    public ImageIcon getAimRedIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.AIM_RED_IMAGE));
        return aimRedIcon;
    }

    @Override
    public ImageIcon getPbimGreyIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.PBIM_GREY_IMAGE));
        return pbGreyIcon;
    }

    @Override
    public ImageIcon getPbimRedIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.PBIM_RED_IMAGE));
        return pbRedIcon;
    }

    @Override
    public ImageIcon getYimGreyIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.YIM_GREY_IMAGE));
        return yahooGreyIcon;
    }

    @Override
    public ImageIcon getYimRedIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.YIM_RED_IMAGE));
        return yahooRedIcon;
    }

    @Override
    public ImageIcon getInterestRateIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.InterestRate36X16png));
        return interestRateIcon;
    }

    @Override
    public ImageIcon getForwardCurveIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.ForwardCurve36X16png));
        return forwardCurveIcon;
    }

    @Override
    public ImageIcon getVolatilityIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.Volatility36X16png));
        return volatilityIcon;
    }

    @Override
    public ImageIcon getCompIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.Comp51X16png));
        return compIcon;
    }

    @Override
    public ImageIcon getStripIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.Strip51X16png));
        return stripIcon;
    }

    @Override
    public ImageIcon getPublishIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.Publish16png));
        return publishIcon;
    }

    @Override
    public ImageIcon getPaletteIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.palette16png));
        return paletteIcon;
    }

    @Override
    public ImageIcon getFontStyleIcon() {
        return fontStyleIcon;
    }

    @Override
    public ImageIcon getConfigIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.Config16png));
        return configIcon;
    }

    @Override
    public ImageIcon getViewerCleanupIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.ViewerCleanup16Png));
        return viewerCleanupIcon;
    }

    @Override
    public ImageIcon getFloatingFramesIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.FloatingFramesPng));
        return floatingFramesIcon;
    }

    @Override
    public ImageIcon getDockingFramesIcon() {
        return dockingFramesIcon;
    }

    @Override
    public ImageIcon getFilterIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.Filter16Png));
        return filterIcon;
    }

    @Override
    public ImageIcon getSearchIcon() {
        //return SwingGlobal.createImageIcon(getSwingGlobalImageURL(PbcImageFileName.Search16Png));
        return searchIcon;
    }

    @Override
    public ImageIcon getBuddyImageIcon(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return null;
        }
        ImageIcon logoImg = null;
        GatewayServerType targetServerType = buddy.getIMServerType();
        switch (targetServerType){
            case AIM_SERVER_TYPE:
                logoImg = getAimBuddyIcon();
                break;
            case YIM_SERVER_TYPE:
                logoImg = getYahooBuddyIcon();
                break;
            case PBIM_SERVER_TYPE:
                logoImg = getPbimBuddyIcon();
                break;
        }
        return logoImg;
    }

    /**
     * @return the archiveIcon
     */
    @Override
    public ImageIcon getArchiveIcon() {
        return archiveIcon;
    }

    /**
     * @return the persistIcon
     */
    @Override
    public ImageIcon getPersistIcon() {
        return persistIcon;
    }

    /**
     * @return the serverPricerIcon
     */
    @Override
    public ImageIcon getServerPricerIcon() {
        return serverPricerIcon;
    }

    /**
     * @return the localPricerIcon
     */
    @Override
    public ImageIcon getLocalPricerIcon() {
        return localPricerIcon;
    }

    /**
     * @return the addIcon
     */
    @Override
    public ImageIcon getAddIcon() {
        return addIcon;
    }

    /**
     * @return the removeIcon
     */
    @Override
    public ImageIcon getRemoveIcon() {
        return removeIcon;
    }

    @Override
    public ImageIcon getPointBoxYellowIcon16() {
        return pointBoxYellowIcon16;
    }   

    @Override
    public ImageIcon getPricerIcon() {
        return pricerIcon;
    }

    @Override
    public ImageIcon getPercentIcon() {
        return percentIcon;
    }

    @Override
    public ImageIcon getComputationIcon() {
        return computationIcon;
    }

    @Override
    public ImageIcon getForwardIcon() {
        return forwardIcon;
    }

    @Override
    public ImageIcon getVolIcon() {
        return volIcon;
    }

    @Override
    public ImageIcon getPointBoxIcon16() {
        return pointBoxIcon16;
    }

    @Override
    public ImageIcon getClearportIcon() {
        return clearportIcon;
    }
}
