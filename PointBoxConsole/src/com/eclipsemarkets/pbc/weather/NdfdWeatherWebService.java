/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

/**
 * Weather web service WSDL : http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl
 * 
 * @author Rueyfarn Wang
 */
class NdfdWeatherWebService
{

//   private static final Logger logger = Logger.getLogger(NdfdWeatherWebService.class.getName());
//
//   private static String ndfDgenLatLonList(String listLatLon, ProductType product, javax.xml.datatype.XMLGregorianCalendar startTime, javax.xml.datatype.XMLGregorianCalendar endTime, WeatherParametersType weatherParameters)
//   {
//      com.eclipsemarkets.pbc.weather.NdfdXML service = new com.eclipsemarkets.pbc.weather.NdfdXML();
//      com.eclipsemarkets.pbc.weather.NdfdXMLPortType port = service.getNdfdXMLPort();
//      /**
//       * todo: this is a new WS api which is located at "http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl". The old one is located 
//       * at "http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl".
//       * 
//       * The old one has no parameter "UnitType.E or UnitType.M"
//       */
//      return port.ndfDgenLatLonList(listLatLon, product, startTime, endTime, UnitType.E, weatherParameters);
//   }
//
//
//    static String callNdfDgenLatLonList(String listLatLon) throws Exception
//    {
//       String rc = null;
//       try
//       {
//          DatatypeFactory dtf = DatatypeFactory.newInstance();
//          XMLGregorianCalendar  startTime = dtf.newXMLGregorianCalendar();
//
//          Calendar now = Calendar.getInstance();
//          int thisDayOfMonth = now.get(Calendar.DATE);
//          int thisMonth = now.get(Calendar.MONTH) + 1;
//          int thisYear = now.get(Calendar.YEAR);
//
//          startTime.setYear(thisYear);
//          startTime.setMonth(thisMonth);
//          startTime.setDay(thisDayOfMonth);
//
//          
//          now.add(Calendar.DATE, 8);          
//          int futureDateOfMonth = now.get(Calendar.DATE);
//          int futureMonth = now.get(Calendar.MONTH) + 1;
//          int futureYear = now.get(Calendar.YEAR);          
//
//          XMLGregorianCalendar  endTime = dtf.newXMLGregorianCalendar();
//          endTime.setYear(futureYear);
//          endTime.setMonth(futureMonth);
//          endTime.setDay(futureDateOfMonth);
//
//
//          WeatherParametersType wpt = new WeatherParametersType();
//          wpt.setMaxt(true);
//          rc = ndfDgenLatLonList(listLatLon, ProductType.GLANCE, startTime, endTime, wpt);
//       }
//       catch ( Exception exp)
//       {
//          logger.log(Level.SEVERE, "Exception thrown invoking callNdfDgenLatLonList() web service call", exp);
//          throw exp;
//       }
//       return rc;
//    }
//
//    private NdfdWeatherWebService() {
//    }

}
