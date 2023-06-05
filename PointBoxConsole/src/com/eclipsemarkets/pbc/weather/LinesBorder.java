/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;


import javax.swing.border.AbstractBorder;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: simon.liang
 * Date: Feb 11, 2009
 * Time: 3:19:48 PM
 */
class LinesBorder extends AbstractBorder implements SwingConstants {
    protected int northThickness;
    protected int southThickness;
    protected int eastThickness;
    protected int westThickness;
    protected Color northColor;
    protected Color southColor;
    protected Color eastColor;
    protected Color westColor;

    public LinesBorder(){
    }

    public LinesBorder(Color color){
        this(color, 1);
    }

    public LinesBorder(Color color, int thickness){
        setColor(color);
        setThickness(thickness);
    }

    public void setThickness(int n) {
        northThickness = n;
        southThickness = n;
        eastThickness = n;
        westThickness = n;
    }

    public void setThickness(Insets insets) {
        northThickness = insets.top;
        southThickness = insets.bottom;
        eastThickness = insets.right;
        westThickness = insets.left;
    }

    public void setColor(Color c) {
        northColor = c;
        southColor = c;
        eastColor = c;
        westColor = c;
    }

    public void setColor(Color c, int direction) {
        switch (direction) {
            case NORTH:
              northColor = c;
              break;
            case SOUTH:
              southColor = c;
              break;
            case EAST:
              eastColor = c;
              break;
            case WEST:
              westColor = c;
              break;
            default:
        }
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(northThickness, westThickness, southThickness, eastThickness);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        return new Insets(northThickness, westThickness, southThickness, eastThickness);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();

        g.setColor(eastColor);
        for (int i = 0; i < westThickness; i++) {
        g.drawLine(x + i, y, x + i, y + height - 1);
        }
        g.setColor(westColor);
        for (int i = 0; i < eastThickness; i++) {
        g.drawLine(x + width - i - 1, y, x + width - i - 1, y + height - 1);
        }
        g.setColor(northColor);
        for (int i = 0; i < northThickness; i++) {
        g.drawLine(x, y + i, x + width - 1, y + i);
        }
        g.setColor(southColor);
        for (int i = 0; i < southThickness; i++) {
        g
        .drawLine(x, y + height - i - 1, x + width - 1, y + height
        - i - 1);
        }

        g.setColor(oldColor);
    }
}
