/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rueyfarn Wang
 */

 class Temperatures
{

   List<Integer> values = new ArrayList<Integer>();
   String units;
   String timeLayout;
   String type;

   Temperatures (String type)
   {
      this.type = type;
   }

   @Override
   public String toString()
   {
     return type + " temperatures, timeLayout = " + timeLayout + ", units = " + units + ", values = " + values.toString();
   }

}