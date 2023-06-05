/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Rueyfarn Wang
 */
class NdfdWebServiceParseResult
{
   String url;
   Map<String, Location>  locationMap = new TreeMap<String, Location>();
   Map<String, Parameters> parametersMap = new TreeMap<String, Parameters>();
   Map<String, TimeLayout> timeLayoutMap = new TreeMap<String, TimeLayout>();
   Map<String, Parameters> cityToParametersMap = new TreeMap<String, Parameters>();



   NdfdWebServiceParseResult ( String url)
   {
      this.url = url;
   }

   void printResult ()
   {
         System.out.println("\nlocation Map :\n");
         System.out.println(locationMap);
         System.out.println("\ntimeLayout Map :\n");
         System.out.println(timeLayoutMap);
         System.out.println("\nparameters Map :\n");
         System.out.println(parametersMap);
   }

   void updateParametersWithCity ()
   {
      Set<String> keySet = parametersMap.keySet();
      Iterator<String> itor = keySet.iterator();
      while ( itor.hasNext())
      {
         String key = itor.next();
         Parameters params = parametersMap.get(key);
         Location loc = this.locationMap.get(params.applicableLocation);
         String city = CityGeoInfo.getCityByLatLon(loc.latitude, loc.longitude );
         params.city = city;

         cityToParametersMap.put(params.city, params);
      }
   }

   Parameters getNewParametersInstance ()
   {
      Parameters params = new Parameters();
      params.url = url;
      return params;
   }

}
