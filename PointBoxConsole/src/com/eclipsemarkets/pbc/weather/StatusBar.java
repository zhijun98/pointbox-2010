/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;


import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;


/**
 * Created by IntelliJ IDEA.
 * User: Simon.Liang
 * Date: Feb 24, 2009
 * Time: 2:52:04 PM
 */
class StatusBar extends JLabel {
    private JLabel timerStatusBar;

    public StatusBar(){
        setLayout(new BorderLayout());
        initializeTimerStatusBar();
        setText("Status Bar");
        setOpaque(true);
        setBackground(new Color(180, 185, 190));

        add(timerStatusBar, BorderLayout.EAST);
    }

    public void initializeTimerStatusBar(){
        timerStatusBar = new JLabel();
        toggleTimerStatusOff();
    }

    public void toggleTimerStatusOff(){
        timerStatusBar.setText("Timer Off");
    }

    public void toggleTimerStatusOn(){
        timerStatusBar.setText("Timer On");
    }
}
