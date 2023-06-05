/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Rueyfarn Wang
 */
public interface IWeatherReportTableCellRenderer
{

    /**
	 * The main ultra-light color.
	 */
	public static final Color mainUltraLightColor = new Color(250, 252, 255);

	/**
	 * The main extra light color.
	 */
	public static final Color mainExtraLightColor = new Color(240, 245, 250);

	/**
	 * The main light color.
	 */
	public static final Color mainLightColor = new Color(200, 210, 220);

	/**
	 * The main medium color.
	 */
	public static final Color mainMidColor = new Color(180, 185, 190);

	/**
	 * The main dark color.
	 */
	public static final Color mainDarkColor = new Color(80, 85, 90);

	/**
	 * The main ultra-dark color.
	 */
	public static final Color mainUltraDarkColor = new Color(32, 37, 42);

	public static final Color HDD_COLOR = new Color(240, 170, 50);

   public static final Font  TAHOMA_PLAIN_11 = new Font("Tahoma", Font.PLAIN, 11);

   public static final Font  TAHOMA_BOLD_11 = new Font("Tahoma", Font.BOLD, 11);


}
