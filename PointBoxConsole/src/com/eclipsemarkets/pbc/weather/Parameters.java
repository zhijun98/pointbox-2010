/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

/**
 *
 * @author Rueyfarn Wang
 */

 class Parameters
{
   static final String EMPTY_STRING = "";
   Temperatures maxTemps = new Temperatures("max") ;
   Temperatures minTemps = new Temperatures("min");
   String applicableLocation;
   String city = EMPTY_STRING;
   String url = EMPTY_STRING;


   Parameters ()
   {

   }

   Parameters(String city, int dayCount)
   {
      for ( int i = 0; i < dayCount; i++)
      {
         maxTemps.values.add(0);
         minTemps.values.add(0);
      }
      this.city = city;
   }


   @Override
   public String toString()
   {
         StringBuilder sb = new StringBuilder();

         sb.append ("applicableLocation = " + applicableLocation + ", ");
         sb.append ("city = " + city + ", ");
         sb.append ( this.maxTemps + "\n");
         sb.append ( this.minTemps + "\n");

         return sb.toString();
   }

   int  getNumberOfDays ()
   {
      int maxSize = this.maxTemps.values.size();
      int minSize = this.minTemps.values.size();
      int rc = java.lang.Math.min(maxSize, minSize);
      return rc;
   }

   int getMinTemp (int i)
   {
      return this.minTemps.values.get(i);
   }

   int getMaxTemp (int i)
   {
      return this.maxTemps.values.get(i);
   }

}
