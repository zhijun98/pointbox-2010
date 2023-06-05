/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class OverallSummeryTableCellRenderer extends JLabel implements TableCellRenderer, IWeatherReportTableCellRenderer
{
//    /**
//	 * The main ultra-light color.
//	 */
//	private static final Color mainUltraLightColor = new Color(250, 252, 255);
//
//	/**
//	 * The main extra light color.
//	 */
//	private static final Color mainExtraLightColor = new Color(240, 245, 250);
//
//	/**
//	 * The main light color.
//	 */
//	private static final Color mainLightColor = new Color(200, 210, 220);
//
//	/**
//	 * The main medium color.
//	 */
//	private static final Color mainMidColor = new Color(180, 185, 190);
//
//	/**
//	 * The main dark color.
//	 */
//	private static final Color mainDarkColor = new Color(80, 85, 90);
//
//	/**
//	 * The main ultra-dark color.
//	 */
//	private static final Color mainUltraDarkColor = new Color(32, 37, 42);
//
//	/**
//	 * The foreground color.
//	 */
//	private static final Color foregroundColor = new Color(15, 20, 25);

    protected Border lowerBoundBorder;
    protected Border noBorder;
    protected Border columnBorder;
    protected Border rightBoundBorder;
    protected LinesBorder rightLowerBoundBorder;

    OverallSummeryTableCellRenderer()
    {
        setOpaque(true);
        initializeRightLowerBoundBorder();

        lowerBoundBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, mainLightColor);
        rightBoundBorder = BorderFactory.createMatteBorder(0, 1, 0, 1, mainUltraLightColor);
        noBorder = new EmptyBorder(0,0,0,0);
    }

    void initializeRightLowerBoundBorder()
    {
        Insets inset = new Insets(0, 1, 1, 1);
        rightLowerBoundBorder = new LinesBorder();
        rightLowerBoundBorder.setThickness(inset);
        rightLowerBoundBorder.setColor(mainUltraLightColor, WEST);
        rightLowerBoundBorder.setColor(mainUltraLightColor, EAST);
        rightLowerBoundBorder.setColor(mainLightColor, SOUTH);
    }

    @Override
    public Component getTableCellRendererComponent (JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) 
    {

        if((row % 4 != 0))
        {
            setBackground(mainExtraLightColor);
        }
        else
        {
            setBackground(Color.white);
        }

        
        if(row == 0 || row % 4 == 0)
        {
       
            setBorder(lowerBoundBorder);
        }
        else if(row == 3 || (row % 4 == 3))
        {
            setBorder(rightLowerBoundBorder);
        }
        else if(row % 4 == 4)
        {
            setBorder(noBorder);
        }
        else{
            //setBorder(noBorder);
            setBorder(rightBoundBorder);
        }


        //fonts
        if(row == 0 || row % 4 == 0)
        {
            setFont(TAHOMA_BOLD_11);
            setForeground(Color.BLACK);
        }
        else if(row == 3 || (row % 4 == 3))
        {
            setFont(TAHOMA_BOLD_11);
            setForeground(new Color(240, 170, 50));
        }
        else if(row == 2 || (row % 4 == 2))
        {
            setFont(TAHOMA_BOLD_11);
            setForeground(Color.BLUE);
        }
        else if(row == 1 || (row % 4 == 1))
        {
            setFont(TAHOMA_BOLD_11);
            setForeground(Color.RED);
        }
        else
        {
            setFont(TAHOMA_BOLD_11);
            setForeground(Color.BLACK);
        }

        if(obj != null)
        {
            String text = obj.toString().trim();
            if ( text.length() > 0  )
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


