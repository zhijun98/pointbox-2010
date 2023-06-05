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
 * 
 * To allow the user to undo, create a new Undo Action, and associate with some button or hotkey. Then add your DnDJTree as a PropertyChangeListener for that event.
 * @author Zhijun Zhang
 */
public class DnDTreeUndoAction extends AbstractAction {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeUndoAction.class.getName());
    }
	public final static Icon ICON = new ImageIcon("resources/undo.gif");
	public static final String DO_UNDO = "undo";
 
	public DnDTreeUndoAction(String text)
	{
		super(text, DnDTreeUndoAction.ICON);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);
	}
 
	public void actionPerformed(ActionEvent e)
	{
		this.firePropertyChange(DnDTreeUndoAction.DO_UNDO, null, null);
	}
 
}
