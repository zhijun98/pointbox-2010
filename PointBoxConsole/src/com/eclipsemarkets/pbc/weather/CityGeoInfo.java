/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Rueyfarn Wang
 */
class CityGeoInfo
{

   /*
   City: Boston, Massachusetts, United States
   Latitude = 42.36000061035156  Longitude: -71.05000305175781

   City: Baltimore, Maryland, United States
   Latitude = 39.279998779296875  Longitude: -76.62000274658203

   City: Atlanta, Georgia, United States
   Latitude = 33.7599983215332  Longitude: -84.38999938964844

   City: Allentown, Pennsylvania, United States
   Latitude = 40.599998474121094  Longitude: -75.47000122070312

   City: Washington, DC, United States
   Latitude = 38.93000030517578  Longitude: -77.06999969482422

   City: Tucson, Arizona, United States
   Latitude = 32.23899841308594  Longitude: -110.97599792480469

   City: Pittsburgh, Pennsylvania, United States
   Latitude = 40.439998626708984  Longitude: -79.97000122070312

   City: Philadelphia, Pennsylvania, United States
   Latitude = 39.939998626708984  Longitude: -75.16000366210938

   City: Newark, New Jersey, United States
   Latitude = 40.7400016784668  Longitude: -74.16999816894531

   City: New York, New York, United States
   Latitude = 40.709999084472656  Longitude: -74.0

   City: Houston, Texas, United States
   Latitude = 29.75  Longitude: -95.33999633789062

   City: Hagerstown, Maryland, United States
   Latitude = 39.630001068115234  Longitude: -77.72000122070312

   City: Dallas, Texas, United States
   Latitude = 32.77000045776367  Longitude: -96.79000091552734

   City: Chicago, Illinois, United States
   Latitude = 41.900001525878906  Longitude: -87.62999725341797

    */

   private static final String SEPARATOR = ":";
   private static final String COMMA = ",";
   private static final String SPACE = " ";

   private static Map<String, CityGeoInfo> latlonToCityMap = new HashMap<String, CityGeoInfo>();
   private static Map<String, CityGeoInfo> cityToCityMap = new TreeMap<String, CityGeoInfo>();

   private final static List<CityGeoInfo> cities = new ArrayList<CityGeoInfo>();
   static
   {
      cities.add ( new CityGeoInfo ("Boston", "Massachusetts", "42.36", "-71.05"));
      cities.add ( new CityGeoInfo ("Baltimore", "Maryland", "39.28", "-76.62"));
      cities.add ( new CityGeoInfo ("Atlanta", "Georgia", "33.76", "-84.39"));
      cities.add ( new CityGeoInfo ("Allentown", "Pennsylvania", "40.60", "-75.47"));
      cities.add ( new CityGeoInfo ("Washington", "DC", "38.93", "-77.07"));
      cities.add ( new CityGeoInfo ("Tucson", "Arizona", "32.24", "-110.98"));
      cities.add ( new CityGeoInfo ("Pittsburgh", "Pennsylvania", "40.44", "-79.97"));                  
      cities.add ( new CityGeoInfo ("Philadelphia", "Pennsylvania", "39.94", "-75.16"));                  
      cities.add ( new CityGeoInfo ("Newark", "New Jersey", "40.74", "-74.17"));                        
      cities.add ( new CityGeoInfo ("New York", "New York", "40.71", "-74.00"));
      cities.add ( new CityGeoInfo ("Houston", "Texas", "29.75", "-95.34"));
      cities.add ( new CityGeoInfo ("Hagerstown", "Maryland", "39.63", "-77.72"));
      cities.add ( new CityGeoInfo ("Dallas", "Texas", "32.77", "-96.79"));
      cities.add ( new CityGeoInfo ("Chicago", "Illinois", "41.90", "-87.63"));


      for ( CityGeoInfo cgi : cities)
      {
        String key = cgi.getLatAndLon();
        latlonToCityMap.put(key, cgi);

        String city = cgi.getCity();
        cityToCityMap.put ( city, cgi);

      }
   }

   private String city;
   private String state;
   private String country = "United States";
   private String latitude;
   private String longitude;

   CityGeoInfo ( String city, String state, String latitude, String longitude)
   {
      this.city = city;
      this.state = state;
      this.latitude = latitude;
      this.longitude = longitude;
   }

   CityGeoInfo ( String city, String state, String country, String latitude, String longitude)
   {
      this.city = city;
      this.state = state;
      this.country = country;
      this.latitude = latitude;
      this.longitude = longitude;
   }

   private String getLatAndLon()
   {
      return this.latitude + SEPARATOR + this.longitude;
   }

   String getLatAndLonForWebService ()
   {
      return this.latitude + COMMA + this.longitude;
   }

   private String getCity ()
   {
      return this.city;
   }


   @Override
   public String toString ()
   {
      return city + ", " + state + ", " + country + "(" + latitude + "," + longitude + ")";
   }


   static String getCityByLatLon ( String lat, String lon)
   {
      String key = lat + SEPARATOR + lon;
      CityGeoInfo cgi  = latlonToCityMap.get(key);
      if ( cgi != null )
      {
         return cgi.city;
      }
      else
      {
         return null;
      }
   }

   static Set<String> getCitySet ()
   {
        return cityToCityMap.keySet();
   }

   
   static String getListOfLatAndLonForWebService ()
   {
      StringBuilder sb = new StringBuilder ();
      Set<String> keySet = getCitySet ();
      Iterator<String> itor = keySet.iterator();
      while ( itor.hasNext())
      {
         String city = itor.next();
         CityGeoInfo cgi = cityToCityMap.get(city);
         String latAndLon = cgi.getLatAndLonForWebService();

         sb.append (latAndLon);
         if ( itor.hasNext())
         {
            sb.append ( SPACE );
         }
      }
      return sb.toString();
   }
}
