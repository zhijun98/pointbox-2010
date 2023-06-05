/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger;
import java.awt.AWTEvent;
/**
 *
 * Used to keep track of which events are to be undone/redone together.
 * @author Zhijun Zhang
 */
public class DnDTreeUndoEventCap extends AWTEvent {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeUndoEventCap.class.getName());
    }
	private boolean start;
 
	public DnDTreeUndoEventCap(Object source, boolean start)
	{
		super(source, AWTEvent.RESERVED_ID_MAX + 2);
		this.start = start;
	}
 
	public boolean isStart()
	{
		return this.start;
	}
 
	@Override
	public String toString()
	{
		return "UndoEventCap " + this.start;
	}
}
