/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.text.DecimalFormat;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Rueyfarn Wang
 */
class CityWeatherTableModel implements TableModel
{
    private static final String EMPTY_STRING = "";
    private static final int    WEATHER_SERVICE_BAND_ROW_COUNT = 4;

    DecimalFormat decDegFormat = new DecimalFormat("#.0\u00BA");

    ReportType type;
    Parameters[] params;
    int        columnCount;


    CityWeatherTableModel ( Parameters[] params, ReportType reportType)
    {
       this.params = params;
       this.columnCount = params[0].getNumberOfDays() + 1;
    }




    String getCity (  )
    {
       return params[0].city;
    }

    int getDayCount()
    {
       return params[0].getNumberOfDays();
    }

    public int getRowCount()
    {
       return 4 * (params.length+1);
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


    public Object getValueAtForColumn0 (int rowIndex)
    {
       int rowCount = this.getRowCount();
       switch ( rowIndex )
       {
          case 0:
             return this.getCity();
          case 1:
             return "summary(hi)";
          case 2:
             return "summary(lo)";
          case 3:
             return "HDD";
          case 4:
             return EMPTY_STRING;
          default:
             int remainder = rowIndex % WEATHER_SERVICE_BAND_ROW_COUNT;
             if ( remainder == 1)
             {
                return params[getParamsIndex(rowIndex)].url;
             }
             return EMPTY_STRING;
       }
    }
    



    public Object getValueAt(int rowIndex, int columnIndex)
    {
       if ( columnIndex == 0 )
       {
          return getValueAtForColumn0 (rowIndex);
       }


       switch ( rowIndex )
       {
          case 0:
                return EMPTY_STRING;
          case 1:
                return getMaxSummary(columnIndex);
          case 2:
                return getMinSummary(columnIndex);
          case 3:
                return getHDD(columnIndex);

          default:
             int remainder = rowIndex % WEATHER_SERVICE_BAND_ROW_COUNT;
             if ( remainder == 1)
             {
                return getDayHighAndLow(getParamsIndex(rowIndex), columnIndex);
             }
             else
             if ( remainder == 2)
             {
                return getDayHigh(getParamsIndex(rowIndex),columnIndex);
             }
             else
             if ( remainder == 3)
             {
                return getDayLow(getParamsIndex(rowIndex),columnIndex);
             }
             return EMPTY_STRING;
        }
     }

    private int getParamsIndex(int rowIndex)
    {
       int quotient = rowIndex / WEATHER_SERVICE_BAND_ROW_COUNT;
       return (quotient -1);
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


    float getMaxSummaryInFloat(int columnIndex)
    {
       float sum = 0.0f;
       int sourceCount = this.params.length;
       if ( sourceCount == 0)
       {
          return 0.0f;
       }

       for ( int i = 0; i < sourceCount; i++)
       {
          sum += (float)this.params[i].maxTemps.values.get(columnIndex-1);
       }

       float average = sum /(float)sourceCount;
       return average;
    }


    String getMaxSummary(int columnIndex)
    {
       float maxSummary = getMaxSummaryInFloat(columnIndex);
       return decDegFormat.format(maxSummary);
    }

    float getMinSummaryInFloat(int columnIndex)
    {
       float sum = 0.0f;
       int sourceCount = this.params.length;
       if ( sourceCount == 0)
       {
          return 0.0f;
       }

       for ( int i = 0; i < sourceCount; i++)
       {
          sum += (float)this.params[i].minTemps.values.get(columnIndex-1);
       }

       float average = sum /(float)sourceCount;
       return average;
    }


    String getMinSummary(int columnIndex)
    {
       float minSummary = getMinSummaryInFloat(columnIndex);
       return decDegFormat.format(minSummary);
    }


    float getHDDinFloat(int columnIndex)
    {
       float hiAverage = this.getMaxSummaryInFloat(columnIndex);
       float loAverage = this.getMinSummaryInFloat(columnIndex);
       float hdd = 65.0f - (hiAverage + loAverage)/2.0f;

       return hdd;
    }

    /**
     * Heating degree day
     * @param columnIndex
     * @return
     */
    String  getHDD(int columnIndex)
    {
       float hdd = getHDDinFloat( columnIndex);
       return decDegFormat.format(hdd);
    }


    // 1 based
    String getDayHigh ( int paramIndex, int index )
    {
       // 0 based
       //return "" + params.getMaxTemp(index -1);
       int maxTemp = params[paramIndex].getMaxTemp(index -1);
       return decDegFormat.format((float)maxTemp);

    }

    String getDayLow ( int paramIndex, int index )
    {
       // 0 based
       //return "" + params.getMinTemp(index -1);
       int minTemp = params[paramIndex].getMinTemp(index -1);
       return decDegFormat.format((float)minTemp);

    }

    String getDayHighAndLow ( int paramIndex, int index)
    {
       int indexToUse = index -1;
       return this.getDayHigh(paramIndex, index) + "/" + this.getDayLow(paramIndex, index);
    }

    @Override
    public String toString ()
    {
       StringBuilder sb = new StringBuilder();
       sb.append ("WeatherTableModel of city " + this.getCity() + " row count = " + this.getRowCount() + ", column count = " + this.getColumnCount() + "\n");
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




    static CityWeatherTableModel getEmptyCityTableModel (String city, int sourceCount, int dayCount)
    {
       Parameters[] paramsArray = new Parameters[sourceCount];
       for ( int i = 0; i < sourceCount; i++)
       {
          paramsArray[i] = new Parameters(city, dayCount);
       }
       CityWeatherTableModel emptyCityWeatherTableModel = new CityWeatherTableModel( paramsArray, null);
       return emptyCityWeatherTableModel;
    }


    CityWeatherSummary getCityWeatherSummary()
    {

       int dayCount = getDayCount();
       String city = getCity();
       SummaryColumn[] summaryColumns = new SummaryColumn[dayCount];
       for ( int i = 0; i < dayCount; i++)
       {
          SummaryColumn sc = new SummaryColumn();
          int columnIndexToUse = i + 1;
          sc.hi = this.getMaxSummary(columnIndexToUse);
          sc.lo = this.getMinSummary(columnIndexToUse);
          sc.hdd = this.getHDD(columnIndexToUse);
          summaryColumns[i] = sc;
       }

       CityWeatherSummary cws = new CityWeatherSummary ( city, dayCount, summaryColumns);
       return cws;
    }
}
