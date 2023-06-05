/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteCodeWrapper;
import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang, date & time: May 20, 2014 - 11:45:58 AM
 */
public abstract class AbstractPricingRuntimeCurveFrame extends javax.swing.JFrame {
    private PricingCurveCodeBasedSelectorAgent aPricingCurveCodeBasedSelectorAgent;

    public AbstractPricingRuntimeCurveFrame(IPbcKernel kernel, PointBoxQuoteCode targetCode) {
        aPricingCurveCodeBasedSelectorAgent = new PricingCurveCodeBasedSelectorAgent(kernel, targetCode);
    }
    
    abstract String getFrameTitle();

    public IPbcKernel getKernel() {
        return aPricingCurveCodeBasedSelectorAgent.getKernel();
    }

    public PointBoxQuoteCode getTargetCode() {
        return aPricingCurveCodeBasedSelectorAgent.getTargetCode();
    }

    void initializeCurveFrame(final JComboBox jClassSelector, final JComboBox jGroupSelector, final JComboBox jCodeSelector){
        aPricingCurveCodeBasedSelectorAgent.loadSelectors(jClassSelector, jGroupSelector, jCodeSelector);
        aPricingCurveCodeBasedSelectorAgent.initializeSelectors();
        
        jClassSelector.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent evt) {
                aPricingCurveCodeBasedSelectorAgent.handleClassSelectorItemStateChanged(evt);
            }
        });
        
        jGroupSelector.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent evt) {
                aPricingCurveCodeBasedSelectorAgent.handleGroupSelectorItemStateChanged(evt);
            }
        });
        
        jCodeSelector.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent evt) {
                if (ItemEvent.SELECTED == evt.getStateChange()){
                    PointBoxQuoteCode targetCode = aPricingCurveCodeBasedSelectorAgent.getTargetCode();
                    PointBoxQuoteCode selectedCode = ((PointBoxQuoteCodeWrapper)jCodeSelector.getSelectedItem()).getCode();
                    if (!(targetCode.name().equalsIgnoreCase(selectedCode.name()))){
                        aPricingCurveCodeBasedSelectorAgent.setTargetCode(selectedCode);
                        updateFrameForTargetCode(selectedCode);
                        PointBoxConsoleProperties.getSingleton().storeSelectedPointBoxQuoteCode(aPricingCurveCodeBasedSelectorAgent.getKernel().getPointBoxLoginUser().getIMUniqueName(), selectedCode);
                        getKernel().getPbcStructuredQuoteBuilder(PointBoxQuoteType.OPTION).setSelectedPointBoxQuoteCode(selectedCode);
                    }
                }
            }
        });
        
        aPricingCurveCodeBasedSelectorAgent.setClassGroupCodeSelectorForSpecificCode(aPricingCurveCodeBasedSelectorAgent.getTargetCode());
        updateFrameForTargetCode(aPricingCurveCodeBasedSelectorAgent.getTargetCode());
    }
    
    void updateFrameForTargetCode(final PointBoxQuoteCode code){
        if (SwingUtilities.isEventDispatchThread()){
            updateFrameForTargetCodeHelper(code);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateFrameForTargetCodeHelper(code);
                }
            });
        }
    }

    
    private String getCurrentTitleForTargetCode(PointBoxQuoteCode targetCode){
        if (targetCode == null){
            return getFrameTitle() + " (" + aPricingCurveCodeBasedSelectorAgent.getTargetCode() + ")";
        }
        String title = getFrameTitle();
        String codeNotes = getKernel().getPointBoxConsoleRuntime().getNotesOfCode(targetCode);
        if (DataGlobal.isEmptyNullString(codeNotes)){
            title += " (" + targetCode.toString() + ")";
        }else{
            title += " (" + targetCode.toString() + " - " + codeNotes + ")";
        }
        return title;
    }
    
    private void updateFrameForTargetCodeHelper(PointBoxQuoteCode targetCode) {
        setTitle(getCurrentTitleForTargetCode(targetCode));
        updateFrameForTargetCodeImpl(targetCode);
    }
    
    abstract void updateFrameForTargetCodeImpl(PointBoxQuoteCode code);

    abstract JTable getTargetTable();
}
