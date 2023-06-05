/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

/**
 *
 * @author Rueyfarn Wang
 */
class Location
{
   String city;
   String location;
   String latitude;
   String longitude;

   @Override
   public String toString()
   {
         return "( " + location + ", " + latitude + ", " + longitude + " )";
   }
}
