/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.util.ArrayList;
import java.util.List;


class TimeLayout
{
   String layoutKey;
   List<Period> periodList = new ArrayList<Period>();

   void cleanUp ()
   {
      for ( Period p : periodList)
      {
         p.name = null;
         p.startTime = null;
         p.endTime = null;
      }

      periodList.clear();
      periodList = null;
      layoutKey = null;
   }


   @Override
   public String toString ()
   {
      return "layoutKey = " + layoutKey + ", periods = " + periodList.toString();
   }

}
