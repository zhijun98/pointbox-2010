/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import javax.swing.table.TableColumnModel;

/**
 * IViewerColumnModel.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 9:54:09 AM
 */
public interface IViewerColumnModel {
    
    public TableColumnModel getViewerColumnModel();

    /**
     * It could return -1 of the ViewerColumnIdentifier is not found in the model
     * @param id
     * @return
     */
    public int getViewerColumnModelIndex(ViewerColumnIdentifier id);

    /**
     * server for data model
     * @return
     */
    public int getNumberOfColumns();

    /**
     * server for data model
     * @param columnModelIndex
     * @return
     */
    public ViewerColumnIdentifier getViewerColumnIdentifier(int columnModelIndex);
}
