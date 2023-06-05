/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.awt.Image;
import java.awt.Point;

/**
 *
 * @author Rueyfarn Wang
 */
public interface IEmsWeatherReport
{
    public void setVisible(boolean flag);
    public void startTimer();
    public void setLocation(Point point);
}
