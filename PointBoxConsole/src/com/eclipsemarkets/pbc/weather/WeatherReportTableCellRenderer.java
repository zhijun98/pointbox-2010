/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;

class WeatherReportTableCellRenderer extends JLabel implements TableCellRenderer, IWeatherReportTableCellRenderer
{

	/**
	 * The foreground color.
	 */
	private static final Color foregroundColor = new Color(15, 20, 25);

    protected Border lowerBoundBorder;
    protected Border noBorder;
    protected Border columnBorder;
    protected Border rightBoundBorder;
    protected LinesBorder rightLowerBoundBorder;

    WeatherReportTableCellRenderer(){
        setOpaque(true);

        initializeRightLowerBoundBorder();

        lowerBoundBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, mainLightColor);
        rightBoundBorder = BorderFactory.createMatteBorder(0, 1, 0, 1, mainUltraLightColor);
        noBorder = new EmptyBorder(0,0,0,0);
    }

    void initializeRightLowerBoundBorder(){
        Insets inset = new Insets(0, 1, 1, 1);
        rightLowerBoundBorder = new LinesBorder();
        rightLowerBoundBorder.setThickness(inset);
        rightLowerBoundBorder.setColor(mainUltraLightColor, WEST);
        rightLowerBoundBorder.setColor(mainUltraLightColor, EAST);
        rightLowerBoundBorder.setColor(mainLightColor, SOUTH);
    }


    public Component getTableCellRendererComponent (JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column)
    {
       int modRowIndex = row % 4;
       if ( modRowIndex  >= 1 && modRowIndex <=3 )
       {
          setBackground(mainExtraLightColor);
       }
       else
       {
          setBackground(Color.white);
       }

       if ( modRowIndex == 0 || modRowIndex == 3)
       {
          setBorder(rightLowerBoundBorder);
       }
       else
       {
          setBorder(rightBoundBorder);
       }

      if ( row == 0)
      {
         setFont(TAHOMA_BOLD_11);
         setForeground(Color.BLACK);
      }
      else
      if ( row == 1)
      {
         setFont(TAHOMA_BOLD_11);
         setForeground(Color.RED);
      }
      else
      if ( row == 2)
      {
         setFont(TAHOMA_BOLD_11);
         setForeground(Color.BLUE);
      }
      else
      if ( row == 3)
      {
         setFont(TAHOMA_BOLD_11);
         setForeground(HDD_COLOR);
      }
      else
      {
         if ( modRowIndex == 1 )
         {
            setFont(TAHOMA_BOLD_11);
            setForeground(Color.BLACK);
         }
         else
         if ( modRowIndex == 2 )
         {
            setFont(TAHOMA_PLAIN_11);
            setForeground(Color.RED);
         }
         else
         if ( modRowIndex == 3 )
         {
            setFont(TAHOMA_PLAIN_11);
            setForeground(Color.BLUE);
         }
      }

      if(obj != null)
      {
         String text = obj.toString().trim();
         if ( text.length() > 0 )
         {
            setToolTipText(text);
         }
         setText(text);
      }
      else
      {
         setText("");
         setToolTipText("           ");
      }
      return this;
   }

   void setColumnBorder(Border border)
   {
      columnBorder = border;
   }
}
