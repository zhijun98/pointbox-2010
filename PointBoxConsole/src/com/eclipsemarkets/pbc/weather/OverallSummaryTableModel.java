/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.text.DecimalFormat;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Rueyfarn Wang
 */
class OverallSummaryTableModel implements TableModel
{
    private static final String EMPTY_STRING = "";
    private static final int    WEATHER_SERVICE_BAND_ROW_COUNT = 4;

    private static  OverallSummaryTableModel emptyOverallSummaryWeatherTableModel;

    DecimalFormat decDegFormat = new DecimalFormat("#.0\u00BA");

    ReportType type;
    CityWeatherSummary[] summaryArray;
    int        columnCount;


    OverallSummaryTableModel (CityWeatherSummary[] summaryArray)
    {
       this.summaryArray = summaryArray;
       this.columnCount = summaryArray[0].getColumnCount();
    }


    public int getRowCount()
    {
       return 4 * (summaryArray.length );
    }

    public int getColumnCount()
    {
        return columnCount;
    }

    public String getColumnName(int columnIndex)
    {
       return "";
    }

    public Class<?> getColumnClass(int columnIndex)
    {
       return java.lang.String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
       return false;
    }


    public Object getValueAt (int row, int column )
    {
       int summaryIndex = row /  WEATHER_SERVICE_BAND_ROW_COUNT;
       int modRowIndex = row % WEATHER_SERVICE_BAND_ROW_COUNT;
       return this.summaryArray[summaryIndex].getValueAt(modRowIndex, column);
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
       // no op
    }

    public void addTableModelListener(TableModelListener l)
    {
       // no op
    }

    public void removeTableModelListener(TableModelListener l)
    {
       // no op
    }


    @Override
    public String toString ()
    {
       StringBuilder sb = new StringBuilder();
       sb.append (   "Overall summary weather report row count = "
                   + this.getRowCount()
                   + ", column count = "
                   + this.getColumnCount() + "\n"
                 );

       for ( int i = 0; i < this.getRowCount(); i++)
       {
          sb.append ( "row " + i + " : ");
          for ( int j = 0; j < this.getColumnCount(); j++)
          {
             sb.append ( "[" + this.getValueAt(i,j) + "] " );
          }
          sb.append ( "\n");
       }
       sb.append ("\n");
       return sb.toString();
    }


    static OverallSummaryTableModel getEmptyOverallSummaryCityTableModel (List<String> cityList, int dayCount)
    {
       int cityCount = cityList.size();
       if ( emptyOverallSummaryWeatherTableModel == null )
       {          
          CityWeatherSummary[] summaryArray = new CityWeatherSummary[cityCount];
          for ( int i = 0; i < cityCount; i++)
          {
             String city = cityList.get(i);
             summaryArray[i] = CityWeatherSummary.getEmptyCityWeatherSummary(city, dayCount);
          }
          emptyOverallSummaryWeatherTableModel = new OverallSummaryTableModel (summaryArray);
       }

       //System.out.println (  "emptyOverallSummaryWeatherTableModel cityCount = "
       //                    + cityCount
       //                    + ", dayCount = "
       //                    + dayCount
       //                    + "\n"
       //                    +  emptyOverallSummaryWeatherTableModel
       //                    );
       return emptyOverallSummaryWeatherTableModel;
    }

}
