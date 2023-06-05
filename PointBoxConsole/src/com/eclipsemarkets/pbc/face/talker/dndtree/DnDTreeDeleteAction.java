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
 * To allow the user to delete nodes, create a new Delete Action, and associate with some button or hotkey. Then add your DnDJTree as a PropertyChangeListener for that event.
 * @author Zhijun Zhang
 */
public class DnDTreeDeleteAction extends AbstractAction {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeDeleteAction.class.getName());
    }
    // public final static Icon ICON = new ImageIcon("resources/find.gif");
    public final static String DO_DELETE = "delete";

    public DnDTreeDeleteAction(String text)
    {
            super(text);
            // super(text, FindAction.ICON);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
            this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
    }
 
    @Override
    public void actionPerformed(ActionEvent e)
    {
            this.firePropertyChange(DnDTreeDeleteAction.DO_DELETE, null, null);
    }
}
