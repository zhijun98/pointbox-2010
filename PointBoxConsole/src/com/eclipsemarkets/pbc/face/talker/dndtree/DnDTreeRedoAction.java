/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger; 
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
 
import javax.swing.*;

/**
 * To allow the user to redo, create a new Redo Action, and associate with some button or hotkey. Then add your DnDJTree as a PropertyChangeListener for that event.
 *
 * @author Zhijun Zhang
 */
public class DnDTreeRedoAction  extends AbstractAction
{

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeRedoAction.class.getName());
    }
	public final static Icon ICON = new ImageIcon("resources/redo.gif");
	public static final String DO_REDO = "redo";
 
	public DnDTreeRedoAction(String text)
	{
		super(text, DnDTreeRedoAction.ICON);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Y);
	}
 
	public void actionPerformed(ActionEvent e)
	{
		this.firePropertyChange(DnDTreeRedoAction.DO_REDO, null, null);
	}
}
