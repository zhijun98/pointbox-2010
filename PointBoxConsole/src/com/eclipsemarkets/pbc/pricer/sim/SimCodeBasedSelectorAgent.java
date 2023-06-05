/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteType;
import javax.swing.JComboBox;

/**
 *
 * @author Zhijun Zhang, date & time: May 21, 2014 - 4:51:40 PM
 */
public class SimCodeBasedSelectorAgent extends AbstractCodeBasedSelectorAgent{
    
    private PbcQuoteFrame owner;

    public SimCodeBasedSelectorAgent(PbcQuoteFrame owner, JComboBox jClassSelector, JComboBox jGroupSelector, JComboBox jCodeSelector) {
        super(owner.getKernel(), jClassSelector, jGroupSelector, jCodeSelector);
        this.owner = owner;
    }

    @Override
    void initializeSelectors() {
        setupClassSelector();
        setDefaultSimCode();
    }
    
    private void setDefaultSimCode(){
        setClassGroupCodeSelectorForSpecificCode(getKernel().getDefaultSimCodeFromProperties());
    }
    
    void setSelectedSimCode(PointBoxQuoteCode selectedCode){
        setClassGroupCodeSelectorForSpecificCode(selectedCode);
    }
    
    @Override
    boolean isValidQuoteType(PointBoxQuoteCode code){
        PointBoxQuoteType type = getKernel().retrievePointBoxQuoteTypeFromPriceModel(code);
        if (type == null){
            return false;
        }else{
            return type.equals(owner.getPointBoxQuoteType());
        }
    }

}
