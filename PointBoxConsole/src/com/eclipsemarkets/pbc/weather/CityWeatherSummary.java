/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

/**
 *
 * @author Rueyfarn Wang
 */
 class CityWeatherSummary
{
    private String city;
    private int dayCount;
    private SummaryColumn[] summaryColumns;
    public  static final String EMPTY_STRING = "";

    CityWeatherSummary (String city, int dayCount, SummaryColumn[] summaryColumns)
    {
       this.city = city;
       this.dayCount = dayCount;
       this.summaryColumns = summaryColumns;
    }

    int getRowCount ()
    {
       return 4;
    }

    int getColumnCount ()
    {
       return dayCount + 1;
    }

    private Object getValueAtColumn0 ( int row)
    {
          switch ( row )
          {
             case 0:
                return city;
             case 1:
                return "Summary(hi)";
             case 2:
                return "Summary(lo)";
             case 3:
                return "HDD";
             default:
                return EMPTY_STRING;
          }

    }

    Object getValueAt (int row, int column)
    {
       if ( column == 0 )
       {
          return getValueAtColumn0 ( row);
       }

       int summaryIndex = column -1;
       switch (row)
       {
          case 1:
             return  summaryColumns[summaryIndex].hi;
          case 2:
             return  summaryColumns[summaryIndex].lo;
          case 3:
             return  summaryColumns[summaryIndex].hdd;
          default:
              return EMPTY_STRING;
       }
    }

    static CityWeatherSummary getEmptyCityWeatherSummary (String city, int dayCount)
    {
       SummaryColumn[] summaryColumns = new SummaryColumn[dayCount];
       for ( int i = 0; i < dayCount; i++)
       {
          summaryColumns[i] = new SummaryColumn();
       }
       CityWeatherSummary  cws = new CityWeatherSummary ( city, dayCount, summaryColumns);
       return cws;
    }

}
