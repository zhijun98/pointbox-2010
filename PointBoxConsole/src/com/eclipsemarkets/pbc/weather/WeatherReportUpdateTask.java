/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rueyfarn Wang
 */
class WeatherReportUpdateTask extends TimerTask
{

    private static Logger logger = Logger.getLogger(WeatherReportUpdateTask.class.getName());


   String listOfLatAndLon;
   private IUpdateWeatherReport iUpdateWeatherReport;
   WeatherReportUpdateTask(IUpdateWeatherReport iUpdateWeatherReport, String listOfLatAndLon)
   {
      this.iUpdateWeatherReport = iUpdateWeatherReport;
      this.listOfLatAndLon = listOfLatAndLon;
   }


  /**
  * Implements TimerTask's abstract run method.
  */
  public void run()
  {

    if ( logger.isLoggable(Level.FINER))
    {
       logger.log(Level.FINER, "list of lat and lon : " + listOfLatAndLon);
    }
    String xmlReturn = "";
    try
    {
       //xmlReturn = NdfdWeatherWebService.callNdfDgenLatLonList(listOfLatAndLon);
        xmlReturn = "";
    }
    catch (Exception e)
    {
       iUpdateWeatherReport.reportWebServiceException (e);
       return;
    }
    
    if ( logger.isLoggable(Level.FINER))
    {
       logger.log(Level.FINER, "weather report web service query returned");
    }


    NdfdWebServiceParseResult ndfdWSParseResult = NdfdXmlDomParser.parseWeatherServiceXMLResult(xmlReturn);
    Set<String> citySet = ndfdWSParseResult.cityToParametersMap.keySet();
    int cityCount = citySet.size();


    CityWeatherSummary[] summaryArray = new CityWeatherSummary[cityCount];

    int summaryIndex = 0;
    Iterator<String> itor = citySet.iterator();
    while ( itor.hasNext())
    {
       Parameters[] paramsArray = new Parameters[1];

       String city = itor.next();
       paramsArray[0] = ndfdWSParseResult.cityToParametersMap.get(city);

       if ( logger.isLoggable(Level.FINER))
       {
          logger.log(Level.FINER, "weather params = " + paramsArray[0]);
       }

       CityWeatherTableModel wtm = new CityWeatherTableModel(paramsArray, null);

       if ( logger.isLoggable(Level.FINER))
       {
          logger.log(Level.FINER, "weather report update of city " + city);
          logger.log(Level.FINER, wtm.toString());
       }

       iUpdateWeatherReport.udpateCityWeatherReport(city, wtm);
       summaryArray[summaryIndex++] = wtm.getCityWeatherSummary();
    }

    OverallSummaryTableModel ostm = new OverallSummaryTableModel (summaryArray);
    iUpdateWeatherReport.updateOverallSummaryReport (ostm);

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat ("EEE, MMM d, yyyy 'at' HH:mm:ss z");
    String curTimeStamp = sdf.format(cal.getTime());
    iUpdateWeatherReport.setLastUpdatedAt (curTimeStamp);

  }


}
