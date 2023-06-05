/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteCodeWrapper;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComboBox;

/**
 *
 * @author Zhijun Zhang, date & time: May 21, 2014 - 4:38:31 PM
 */
public abstract class AbstractCodeBasedSelectorAgent {

    private IPbcKernel kernel;
    private JComboBox jClassSelector;
    private JComboBox jGroupSelector;
    private JComboBox jCodeSelector;
    
    /**
     * HashMap<Class, HashMap<Group, Code>>
     */
    private final HashMap<String, HashMap<String, ArrayList<String>>> cgcItemModel = new HashMap<String, HashMap<String, ArrayList<String>>>();
    private String selectedClass;
    private String selectedGroup;

    AbstractCodeBasedSelectorAgent(IPbcKernel kernel) {
        this.kernel = kernel;
    }

    void loadSelectors(JComboBox jClassSelector, JComboBox jGroupSelector, JComboBox jCodeSelector) {
        this.jClassSelector = jClassSelector;
        this.jGroupSelector = jGroupSelector;
        this.jCodeSelector = jCodeSelector;
    }

    public AbstractCodeBasedSelectorAgent(IPbcKernel kernel, JComboBox jClassSelector, JComboBox jGroupSelector, JComboBox jCodeSelector) {
        this.kernel = kernel;
        this.jClassSelector = jClassSelector;
        this.jGroupSelector = jGroupSelector;
        this.jCodeSelector = jCodeSelector;
    }
    
    abstract boolean isValidQuoteType(PointBoxQuoteCode code);

    abstract void initializeSelectors();

    public IPbcKernel getKernel() {
        return kernel;
    }
    
    void setupClassSelector() {
        /**
         * Setup cgcItems
         */
        HashMap<String, PbcPricingModel> pbcPricingModelMap = getKernel().getPointBoxConsoleRuntime().getPbcPricingModelMap();
        jClassSelector.removeAllItems();
        cgcItemModel.clear();
        Set<String> keys = pbcPricingModelMap.keySet();
        Iterator<String> itr = keys.iterator();
        String classValue;
        String groupValue;
        String codeValue;
        PbcPricingModel aPbcPricingModel;
        while(itr.hasNext()){
            codeValue = itr.next();
            aPbcPricingModel = pbcPricingModelMap.get(codeValue);
            classValue = aPbcPricingModel.getSqClass();
            groupValue = aPbcPricingModel.getSqGroup();
            if (!cgcItemModel.containsKey(classValue)){
                cgcItemModel.put(classValue, new HashMap<String, ArrayList<String>>());
            }
            if (!cgcItemModel.get(classValue).containsKey(groupValue)){
                cgcItemModel.get(classValue).put(groupValue, new ArrayList<String>());
            }
            if (!cgcItemModel.get(classValue).get(groupValue).contains(codeValue)){
                cgcItemModel.get(classValue).get(groupValue).add(codeValue);
            }
        }//while
        /**
         * setup class-selector
         */
        keys = cgcItemModel.keySet();
        for (String classKeyValue : keys){
            jClassSelector.addItem(classKeyValue);
        }
        setClassGroupCodeSelectorForSpecificClass(jClassSelector.getSelectedItem().toString());
    }
    
    /**
     * 
     * @param targetClass 
     */
    void setClassGroupCodeSelectorForSpecificClass(String targetClass) {
        /**
         * Setup selected class and relevant selectors
         */
        selectedClass = jClassSelector.getSelectedItem().toString();
        jClassSelector.setSelectedItem(targetClass);
        if (jClassSelector.getSelectedItem() != null){
            setupGroupSelector(cgcItemModel.get(selectedClass));
        }
    }
    
    /**
     * 
     * @param targetGroup 
     */
    void setGroupGroupCodeSelectorForSpecificGroup(String targetGroup) {
        /**
         * Setup selected class and relevant selectors
         */
        selectedGroup = jGroupSelector.getSelectedItem().toString();
        jGroupSelector.setSelectedItem(targetGroup);
        if (jGroupSelector.getSelectedItem() != null){
            HashMap<String, ArrayList<String>> gcItems = cgcItemModel.get(selectedClass);
            setupCodeSelector(gcItems.get(selectedGroup));
        }
    }
    
    void setClassGroupCodeSelectorForSpecificCode(PointBoxQuoteCode targetCode){
        if (targetCode == null){
            return;
        }
        HashMap<String, PbcPricingModel> pbcPricingModelMap = kernel.getPointBoxConsoleRuntime().getPbcPricingModelMap();
        PbcPricingModel defaultModel = pbcPricingModelMap.get(targetCode.name());
        if (defaultModel != null){
            setClassGroupCodeSelectorForSpecificClass(defaultModel.getSqClass());
            setGroupGroupCodeSelectorForSpecificGroup(defaultModel.getSqGroup());
            
            jCodeSelector.setSelectedItem(new PointBoxQuoteCodeWrapper(targetCode));
        }
    
    }

    private void setupGroupSelector(HashMap<String, ArrayList<String>> gcItems) {
        jGroupSelector.removeAllItems();
        Set<String> keys = gcItems.keySet();
        Iterator<String> itr = keys.iterator();
        String gValue;
        while(itr.hasNext()){
            gValue = itr.next();
            jGroupSelector.addItem(gValue);
        }//while
        /**
         * Setup selected group and relevant selectors
         */
        setGroupGroupCodeSelectorForSpecificGroup(jGroupSelector.getSelectedItem().toString());
    }

    private void setupCodeSelector(ArrayList<String> cValueList) {
        jCodeSelector.removeAllItems();
        PointBoxQuoteCode code;
        for (String cValue : cValueList){
            code = PointBoxQuoteCode.convertEnumNameToType(cValue);
            if (isValidQuoteType(code)){
                jCodeSelector.addItem(new PointBoxQuoteCodeWrapper(code));
            }
        }
    }

    void handleClassSelectorItemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if (!jClassSelector.getSelectedItem().toString().equalsIgnoreCase(selectedClass)){
                setClassGroupCodeSelectorForSpecificClass(jClassSelector.getSelectedItem().toString());
            }
        }
    }

    void handleGroupSelectorItemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.DESELECTED){
            if ((jGroupSelector.getSelectedItem() != null)){
                if (!jGroupSelector.getSelectedItem().toString().equalsIgnoreCase(selectedGroup)){
                   selectedGroup = jGroupSelector.getSelectedItem().toString();
                   setupCodeSelector(cgcItemModel.get(selectedClass).get(selectedGroup));
                }
            }
        }
    }
}
