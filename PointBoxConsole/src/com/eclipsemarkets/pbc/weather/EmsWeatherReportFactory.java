/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.awt.Image;

/**
 *
 * @author Rueyfarn Wang
 */
public class EmsWeatherReportFactory
{
   private static IEmsWeatherReport instance = null;

   public static  synchronized  IEmsWeatherReport getIEmsWeatherReport (Image frameIconImage)
   {
      if (instance == null)
      {
         instance = new EmsWeatherReport( frameIconImage);
      }
      return instance;
   }

}
