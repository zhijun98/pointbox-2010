/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 * ViewerCellRenderer.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 24, 2010, 6:51:33 PM
 */
abstract class ViewerCellRenderer extends JTextField implements TableCellRenderer
{
    private static final long serialVersionUID = 1L;

    final IPbcRuntime runtime;
    
    final String viewerUniqueTabName;
    
    final ViewerColumnIdentifier columnIdentifier;

    //predefined static settings
    Color backgroundColor_1;
    Color backgroundColor_2;

    /**
     * 
     * @param runtime
     * @param columnIdentifier
     */
    ViewerCellRenderer(IPbcRuntime runtime, 
                       String viewerUniqueTabName,
                       ViewerColumnIdentifier columnIdentifier)
    {

        this.runtime = runtime;
        this.viewerUniqueTabName = viewerUniqueTabName;
        this.columnIdentifier = columnIdentifier;

        this.setFont(runtime.getViewerGeneralFont(viewerUniqueTabName));
        this.setForeground(runtime.getViewerGeneralColor(viewerUniqueTabName));
        
        this.backgroundColor_1 = Color.WHITE;
        this.backgroundColor_2 = SwingGlobal.getColor(SwingGlobal.ColorName.VERY_LIGHT_GREY);

        this.setEditable(false);
    }

    void setGeneralSettings(int row){
        this.setFont(runtime.getViewerGeneralFont(viewerUniqueTabName));
        this.setForeground(runtime.getViewerGeneralColor(viewerUniqueTabName));
        if ((row+1) % 2 == 0){
            this.setBackground(backgroundColor_1);
        }else{
            this.setBackground(backgroundColor_2);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        /**
         * General settings
         */
        setGeneralSettings(row);
        /**
         * selected row setting
         */
        if (isSelected){
            this.setBackground(runtime.getSelectedRowBackground(viewerUniqueTabName));
        }
        /**
         * Latest row setting
         */
        int modelRowCursor = ((IViewerDataModel)table.getModel()).getRowCursor();
        if (modelRowCursor > 1){
            int viewRowCursor;
            try{
                viewRowCursor = table.convertRowIndexToView(modelRowCursor - 1);
            }catch (java.lang.IndexOutOfBoundsException ex){
                return this;
            }
            if (viewRowCursor == row){
                this.setBackground(runtime.getLatestRowBackground(viewerUniqueTabName));
            }
        }
        /**
         * decorate specific column and set text
         */
        if (value instanceof IPbsysOptionQuoteWrapper){
            decorateSpecificColumn((IPbsysOptionQuoteWrapper)value);
            setText(getTableCellValueText((IPbsysOptionQuoteWrapper)value));
        }else{
            this.setText("");
        }
        table.setRowHeight(row, getPreferredSize().height);
        return this;
    }

    /**
     * This method has to guarantee no NULL returned
     * @param wrapper
     * @return
     */
    abstract String getTableCellValueText(IPbsysOptionQuoteWrapper wrapper);
    abstract void decorateSpecificColumn(IPbsysOptionQuoteWrapper wrapper);
    
}
