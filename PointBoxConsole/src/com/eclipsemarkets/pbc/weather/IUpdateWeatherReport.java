/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;


/**
 *
 * @author Rueyfarn Wang
 */
interface IUpdateWeatherReport
{
   public void udpateCityWeatherReport ( String target, CityWeatherTableModel model);
   public void updateOverallSummaryReport (OverallSummaryTableModel model);
   public void reportWebServiceException ( Exception e);
   public void setLastUpdatedAt (final String timeStamp);
}
