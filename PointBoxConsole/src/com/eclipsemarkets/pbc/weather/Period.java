/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

/**
 *
 * @author Rueyfarn Wang
 */

class Period
{
      String name;
      String startTime;
      String endTime;

      @Override
      public String toString()
      {
         return "name = " + name + ", startTime = " + startTime + ", endTime = " + endTime;
      }
}