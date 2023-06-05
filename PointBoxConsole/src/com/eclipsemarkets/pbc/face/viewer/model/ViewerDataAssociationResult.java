/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import java.util.logging.Logger;

/**
 * ViewerDataAssociationResult
 * <P>
 * a data structure contains information of association between a quote and a wrapper in ViewerDataKernel 
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 26, 2011 at 9:16:48 AM
 */
public class ViewerDataAssociationResult {

    private static final Logger logger = Logger.getLogger(ViewerDataAssociationResult.class.getName());

    private IPbsysOptionQuoteWrapper wrappper;
    
    private int firstRowModelIndex;

    private int lastRowModelIndex;

    public int getFirstRowModelIndex() {
        return firstRowModelIndex;
    }

    public void setFirstRowModelIndex(int firstRowModelIndex) {
        this.firstRowModelIndex = firstRowModelIndex;
    }

    public int getLastRowModelIndex() {
        return lastRowModelIndex;
    }

    public void setLastRowModelIndex(int lastRowModelIndex) {
        this.lastRowModelIndex = lastRowModelIndex;
    }

    public IPbsysOptionQuoteWrapper getWrappper() {
        return wrappper;
    }

    public void setWrappper(IPbsysOptionQuoteWrapper wrappper) {
        this.wrappper = wrappper;
    }

}//ViewerDataAssociationResult

