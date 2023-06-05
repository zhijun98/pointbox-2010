/**
 * Eclipse Market Solutions LLC
 *
 * ViewerTableFontColorSettingsPanel.java
 *
 * @author Zhijun Zhang
 * Created on May 23, 2010, 3:13:15 PM
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.ViewerColumnSettingsChangedEvent;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.ViewerColumnSorter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang
 */
class ViewerTableColumnSettingsPanel extends javax.swing.JPanel implements IPreferenceComponentPanel
{
    private static final long serialVersionUID = 1L;

    private final IPbcFace face;
    private final String viewerUniqueTabName;
    
    //private JavaFxColorChooser aJavaFxColorChooser = null;;

    ViewerTableColumnSettingsPanel(IPbcFace face, String viewerUniqueTabName) {
        initComponents();

        this.face = face;
        this.viewerUniqueTabName = viewerUniqueTabName;
        allViewerSettingsCheckBox.setVisible(false);
    }
    
//    private JavaFxColorChooser getJavaFxColorChooser(){
//        if (aJavaFxColorChooser == null){
//            aJavaFxColorChooser = new JavaFxColorChooser();
//        }
//        return aJavaFxColorChooser;
//    }
    
    private IPbcRuntime getPointBoxConsoleRuntime(){
        return face.getKernel().getPointBoxConsoleRuntime();
    }

    @Override
    public final void populateSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            populateSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateSettingsHelper();
                }
            });
        }
    }
    
    private void populateSettingsHelper(){
        applyViewerTableSettingsHelper();
    }

    @Override
    public void updateSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            updateSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateSettingsHelper();
                }
            });
        }
    }
    private void updateSettingsHelper(){
    }

    private void applyViewerTableSettingsHelper() {
        applyViewerTableColumnRecord();
    }

    private void applyViewerTableColumnRecord() {
        if (getPointBoxConsoleRuntime() == null){
            return;
        }
        ArrayList<ViewerColumnIdentifier> viewerColumnIdentifiers = getPointBoxConsoleRuntime().getAllViewerColumnIdentifiers(viewerUniqueTabName, ViewerColumnSorter.SortByIdentifier);
        for (ViewerColumnIdentifier aViewerColumnIdentifier : viewerColumnIdentifiers){
            switch(aViewerColumnIdentifier){
                case BuySell:
                    setCheckBox(jBuySell, aViewerColumnIdentifier);
                    break;
                case TimeStamp:
                    setCheckBox(jTimeStamp, aViewerColumnIdentifier);
                    break;
                case Period:
                    setCheckBox(jPeriod, aViewerColumnIdentifier);
                    break;
                case Strike:
                    setCheckBox(jStrike, aViewerColumnIdentifier);
                    break;
                case Structure:
                    setCheckBox(jStructure, aViewerColumnIdentifier);
                    break;
                case Cross:
                    setCheckBox(jCross, aViewerColumnIdentifier);
                    break;
                case Bid:
                    setCheckBox(jBid, aViewerColumnIdentifier);
                    break;
                case Offer:
                    setCheckBox(jOffer, aViewerColumnIdentifier);
                    break;
                case Last:
                    setCheckBox(jLast, aViewerColumnIdentifier);
                    break;
                case PbsysPrice:
                    setCheckBox(jPbsysPrice, aViewerColumnIdentifier);
                    break;
                case Swap01:
                    setCheckBox(jSwap01, aViewerColumnIdentifier);
                    break;
                case RemoteBrokerHouse:
                    setCheckBox(jRemoteBrokerHouse, aViewerColumnIdentifier);
                    break;
                case Delta:
                    setCheckBox(jDelta, aViewerColumnIdentifier);
                    break;
                case QuoteMessage:
                    setCheckBox(jQuoteMessage, aViewerColumnIdentifier);
                    break;
                case DDelta:
                    setCheckBox(jDDelta, aViewerColumnIdentifier);
                    break;
                case Swap02:
                    setCheckBox(jSwap02, aViewerColumnIdentifier);
                    break;
                case Theta:
                    setCheckBox(jTheta, aViewerColumnIdentifier);
                    break;
                case Vega:
                    setCheckBox(jVega, aViewerColumnIdentifier);
                    break;
                case Gamma:
                    setCheckBox(jGamma, aViewerColumnIdentifier);
                    break;
                case DGamma:
                    setCheckBox(jDGamma, aViewerColumnIdentifier);
                    break;
                case QuoteClass:
                    setCheckBox(jQuoteClass, aViewerColumnIdentifier);
                    break;
                case QuoteGroup:
                    setCheckBox(jQuoteGroup, aViewerColumnIdentifier);
                    break;
                case QuoteCode:
                    setCheckBox(jQuoteCode, aViewerColumnIdentifier);
                    break;
                case QuoteSource:
                    setCheckBox(jQuoteSource, aViewerColumnIdentifier);
                    break;
                case RowNumber:
                    setCheckBox(jRowNumber, aViewerColumnIdentifier);
                    break;
                case Volatility:
                    setCheckBox(jVol, aViewerColumnIdentifier);
                    break;
                case UnderlierType:
                    setCheckBox(jUnderlier, aViewerColumnIdentifier);
                    break;
                default:
            }//switch
        }
    }

    private void setCheckBox(JCheckBox checkBox, ViewerColumnIdentifier aViewerColumnIdentifier) {
        if (getPointBoxConsoleRuntime().isViewerColumnVisible(viewerUniqueTabName, aViewerColumnIdentifier)){
            checkBox.setSelected(true);
        }else{
            checkBox.setSelected(false);
        }
        if(checkBox.getItemListeners().length<=0)   //prevent to repeat adding same listener.
            checkBox.addItemListener(new ViewerColumnCheckBoxListener(viewerUniqueTabName, aViewerColumnIdentifier));
    }

    private class ViewerColumnCheckBoxListener implements ItemListener{
        private String wiewerUniqueTabName;
        private ViewerColumnIdentifier viewerColumnIdentifier;
        ViewerColumnCheckBoxListener(String wiewerUniqueTabName, ViewerColumnIdentifier colRecord) {
            this.viewerColumnIdentifier = colRecord;
            this.wiewerUniqueTabName = wiewerUniqueTabName;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            try{
                boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
                if (e.getStateChange() == ItemEvent.SELECTED){
                    //if (!getPointBoxConsoleRuntime().isViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier)){  
                    //Notice:  If checkbox was selected, it must update to make column visible. So above judgement is unnecessary.
                    //What's important, above judgement will make reset to the function of default settings useless. Because after resetting, it will reset the visible value of every column.
                    if (isViewerSettingsForAllViewers){
                        /**
                         * every viewer has the same settings. thus, use the first tab's settings
                         */
                        for(String viewerUniqueTabName:face.getPointBoxViewer().getTabStorage().keySet()){
                            getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, true);
                        }
                    }else{
                        getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, true);
                    }
                    face.getKernel().raisePointBoxEvent(new ViewerColumnSettingsChangedEvent(PointBoxEventTarget.PbcFace,
                                                                 wiewerUniqueTabName));
                }else if (e.getStateChange() == ItemEvent.DESELECTED){
                    if (isViewerSettingsForAllViewers){
                        /**
                         * every viewer has the same settings. thus, use the first tab's settings
                         */
                        for(String viewerUniqueTabName:face.getPointBoxViewer().getTabStorage().keySet()){
                            getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, false);
                        }
                    }else{
                        getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, false);
                    }
                    face.getKernel().raisePointBoxEvent(new ViewerColumnSettingsChangedEvent(PointBoxEventTarget.PbcFace,
                                                                 wiewerUniqueTabName));
                }
            }catch (Exception ex){
            }
        }
    
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jBid = new javax.swing.JCheckBox();
        jRemoteBrokerHouse = new javax.swing.JCheckBox();
        jBuySell = new javax.swing.JCheckBox();
        jCross = new javax.swing.JCheckBox();
        jDelta = new javax.swing.JCheckBox();
        jDGamma = new javax.swing.JCheckBox();
        jDDelta = new javax.swing.JCheckBox();
        jQuoteClass = new javax.swing.JCheckBox();
        jGamma = new javax.swing.JCheckBox();
        jRowNumber = new javax.swing.JCheckBox();
        jLast = new javax.swing.JCheckBox();
        jQuoteGroup = new javax.swing.JCheckBox();
        jQuoteCode = new javax.swing.JCheckBox();
        jOffer = new javax.swing.JCheckBox();
        jPbsysPrice = new javax.swing.JCheckBox();
        jPeriod = new javax.swing.JCheckBox();
        jQuoteMessage = new javax.swing.JCheckBox();
        jQuoteSource = new javax.swing.JCheckBox();
        jStrike = new javax.swing.JCheckBox();
        jStructure = new javax.swing.JCheckBox();
        jSwap01 = new javax.swing.JCheckBox();
        jSwap02 = new javax.swing.JCheckBox();
        jTheta = new javax.swing.JCheckBox();
        jTimeStamp = new javax.swing.JCheckBox();
        jUnderlier = new javax.swing.JCheckBox();
        jVega = new javax.swing.JCheckBox();
        jVol = new javax.swing.JCheckBox();
        allViewerSettingsCheckBox = new javax.swing.JCheckBox();

        setRequestFocusEnabled(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Visibility of Aggregator Columns:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(6, 5, 3, 1));

        jBid.setText("Bid");
        jBid.setName("jBid"); // NOI18N
        jPanel1.add(jBid);

        jRemoteBrokerHouse.setText("Broker");
        jRemoteBrokerHouse.setName("jRemoteBrokerHouse"); // NOI18N
        jPanel1.add(jRemoteBrokerHouse);

        jBuySell.setText("Buy/Sell");
        jBuySell.setName("jBuySell"); // NOI18N
        jPanel1.add(jBuySell);

        jCross.setText("Cross");
        jCross.setName("jCross"); // NOI18N
        jPanel1.add(jCross);

        jDelta.setText("Delta");
        jDelta.setName("jDelta"); // NOI18N
        jPanel1.add(jDelta);

        jDGamma.setText("True Gamma");
        jDGamma.setName("jDGamma"); // NOI18N
        jPanel1.add(jDGamma);

        jDDelta.setText("True Delta");
        jDDelta.setName("jDDelta"); // NOI18N
        jPanel1.add(jDDelta);

        jQuoteClass.setText("Class");
        jQuoteClass.setName("jQuoteClass"); // NOI18N
        jPanel1.add(jQuoteClass);

        jGamma.setText("Gamma");
        jGamma.setName("jGamma"); // NOI18N
        jPanel1.add(jGamma);

        jRowNumber.setText("ID");
        jRowNumber.setName("jRowNumber"); // NOI18N
        jPanel1.add(jRowNumber);

        jLast.setText("Last");
        jLast.setName("jLast"); // NOI18N
        jPanel1.add(jLast);

        jQuoteGroup.setText("Group");
        jQuoteGroup.setName("jQuoteGroup"); // NOI18N
        jPanel1.add(jQuoteGroup);

        jQuoteCode.setText("Code");
        jQuoteCode.setName("jQuoteCode"); // NOI18N
        jPanel1.add(jQuoteCode);

        jOffer.setText("Offer");
        jOffer.setName("jOffer"); // NOI18N
        jPanel1.add(jOffer);

        jPbsysPrice.setText("PB");
        jPbsysPrice.setName("jPbsysPrice"); // NOI18N
        jPanel1.add(jPbsysPrice);

        jPeriod.setText("Period");
        jPeriod.setName("jPeriod"); // NOI18N
        jPanel1.add(jPeriod);

        jQuoteMessage.setText("Quote");
        jQuoteMessage.setName("jQuoteMessage"); // NOI18N
        jPanel1.add(jQuoteMessage);

        jQuoteSource.setText("Source");
        jQuoteSource.setName("jQuoteSource"); // NOI18N
        jPanel1.add(jQuoteSource);

        jStrike.setText("Strike");
        jStrike.setName("jStrike"); // NOI18N
        jPanel1.add(jStrike);

        jStructure.setText("Structure");
        jStructure.setName("jStructure"); // NOI18N
        jPanel1.add(jStructure);

        jSwap01.setText("Swap01");
        jSwap01.setName("jSwap01"); // NOI18N
        jPanel1.add(jSwap01);

        jSwap02.setText("Swap02");
        jSwap02.setName("jSwap02"); // NOI18N
        jPanel1.add(jSwap02);

        jTheta.setText("Theta");
        jTheta.setName("jTheta"); // NOI18N
        jPanel1.add(jTheta);

        jTimeStamp.setText("Time");
        jTimeStamp.setName("jTimeStamp"); // NOI18N
        jPanel1.add(jTimeStamp);

        jUnderlier.setText("Underlier");
        jUnderlier.setName("jUnderlier"); // NOI18N
        jPanel1.add(jUnderlier);

        jVega.setText("Vega");
        jVega.setName("jVega"); // NOI18N
        jPanel1.add(jVega);

        jVol.setText("Vol");
        jVol.setName("jVol"); // NOI18N
        jPanel1.add(jVol);

        allViewerSettingsCheckBox.setText("Apply aggregator column settings change for all other viewers");
        allViewerSettingsCheckBox.setName("allViewerSettingsCheckBox"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(allViewerSettingsCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(allViewerSettingsCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allViewerSettingsCheckBox;
    private javax.swing.JCheckBox jBid;
    private javax.swing.JCheckBox jBuySell;
    private javax.swing.JCheckBox jCross;
    private javax.swing.JCheckBox jDDelta;
    private javax.swing.JCheckBox jDGamma;
    private javax.swing.JCheckBox jDelta;
    private javax.swing.JCheckBox jGamma;
    private javax.swing.JCheckBox jLast;
    private javax.swing.JCheckBox jOffer;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox jPbsysPrice;
    private javax.swing.JCheckBox jPeriod;
    private javax.swing.JCheckBox jQuoteClass;
    private javax.swing.JCheckBox jQuoteCode;
    private javax.swing.JCheckBox jQuoteGroup;
    private javax.swing.JCheckBox jQuoteMessage;
    private javax.swing.JCheckBox jQuoteSource;
    private javax.swing.JCheckBox jRemoteBrokerHouse;
    private javax.swing.JCheckBox jRowNumber;
    private javax.swing.JCheckBox jStrike;
    private javax.swing.JCheckBox jStructure;
    private javax.swing.JCheckBox jSwap01;
    private javax.swing.JCheckBox jSwap02;
    private javax.swing.JCheckBox jTheta;
    private javax.swing.JCheckBox jTimeStamp;
    private javax.swing.JCheckBox jUnderlier;
    private javax.swing.JCheckBox jVega;
    private javax.swing.JCheckBox jVol;
    // End of variables declaration//GEN-END:variables

}
