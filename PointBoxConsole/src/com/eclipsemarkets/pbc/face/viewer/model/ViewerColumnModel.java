/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.ViewerColumnSorter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * ViewerColumnModel.java
 * <p>
 * ViewerColumnModel defines the mappings between IPbsysOptionQuote's fields and columns
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 11:27:37 AM
 */
class ViewerColumnModel extends DefaultTableColumnModel implements IViewerColumnModel
{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(ViewerColumnModel.class.getName());
    }
    private static final long serialVersionUID = 1L;

    private IPbcRuntime runtime;
    private String viewerUniqueTabName;

    private EnumMap<ViewerColumnIdentifier, TableColumn> tableColumnStorage;

    ViewerColumnModel(IPbcRuntime runtime, String viewerUniqueTabName) {
        this.runtime = runtime;
        this.viewerUniqueTabName = viewerUniqueTabName;
        initializeTableColumnStorage();
    }

    private void initializeTableColumnStorage(){
        tableColumnStorage = new EnumMap<ViewerColumnIdentifier, TableColumn>(ViewerColumnIdentifier.class);
        ArrayList<ViewerColumnIdentifier> viewerColumnIdentifiers = runtime.getAllViewerColumnIdentifiers(viewerUniqueTabName, ViewerColumnSorter.SortByPosition);
        if ((viewerColumnIdentifiers == null) || (viewerColumnIdentifiers.isEmpty())){
            viewerColumnIdentifiers = runtime.createDefaultViewerColumnIdentifiers();
        }
        TableColumn tCol;
        //check if all the columns are invisible in the storage
        boolean isAllInvisible = true;
        ViewerColumnIdentifier aViewerColumnIdentifier;
        for (int modelIndex = 0; modelIndex < viewerColumnIdentifiers.size(); modelIndex++){
            aViewerColumnIdentifier = viewerColumnIdentifiers.get(modelIndex);
            if (runtime.isViewerColumnVisible(viewerUniqueTabName, aViewerColumnIdentifier)){
                isAllInvisible = false;
                break;
            }
        }
        for (int modelIndex = 0; modelIndex < viewerColumnIdentifiers.size(); modelIndex++){
            aViewerColumnIdentifier = viewerColumnIdentifiers.get(modelIndex);
            tCol = createTableColumnInstance(modelIndex, aViewerColumnIdentifier);
            tableColumnStorage.put(aViewerColumnIdentifier, tCol);
            if (isAllInvisible){
                addColumn(tCol);
            }else{
                if (runtime.isViewerColumnVisible(viewerUniqueTabName, aViewerColumnIdentifier)){
                    addColumn(tCol);
                }
            }
        }
    }

    private TableColumn createTableColumnInstance(int modelIndex, ViewerColumnIdentifier aViewerColumnIdentifier){
        TableColumn aTableColumn = new TableColumn(modelIndex);
        
        aTableColumn.setCellRenderer(new ViewerColumnCellRenderer(runtime, viewerUniqueTabName, aViewerColumnIdentifier));
        aTableColumn.setHeaderValue(runtime.getViewerColumnHeaderValue(viewerUniqueTabName, aViewerColumnIdentifier));
        aTableColumn.setIdentifier(aViewerColumnIdentifier);
        aTableColumn.setResizable(runtime.isViewerColumnResizable(viewerUniqueTabName, aViewerColumnIdentifier));
        aTableColumn.setPreferredWidth(runtime.getViewerColumnWidth(viewerUniqueTabName, aViewerColumnIdentifier));

        return aTableColumn;
    }

    @Override
    public int getNumberOfColumns() {
        return getColumnCount();
    }

    @Override
    public ViewerColumnIdentifier getViewerColumnIdentifier(int columnModelIndex) {
        return (ViewerColumnIdentifier)getColumn(columnModelIndex).getIdentifier();
    }

    @Override
    public int getViewerColumnModelIndex(ViewerColumnIdentifier id){
        try{
            int modelIndex = getColumnIndex(id);
            logger.log(Level.INFO, "getViewerColumnModelIndex - {0}", id);
            return modelIndex;
        }catch (IllegalArgumentException ex){
            return -1;
        }
    }

    @Override
    public TableColumnModel getViewerColumnModel() {
        return this;
    }

}
