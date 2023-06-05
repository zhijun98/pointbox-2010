/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger;
import java.awt.AWTEvent;
/**
 * Used to keep track of undo/redo actions. Currently only support add/remove of a single node.
 * @author Zhijun Zhang
 */
public class DnDTreeEvent extends AWTEvent
{

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeEvent.class.getName());
    }
	/**
	 * True if this event is for an add event, false otherwise
	 */
	boolean isAdd;
	/**
	 * The node to add/remove from
	 */
	protected DnDMutableTreeNode destination;
	/**
	 * The node to be added/removed, or the parent of the node that was moved
	 * from
	 */
	protected DnDMutableTreeNode node;
	/**
	 * The index to add/remove node to
	 */
	protected int index;
 
	/**
	 * Creates an event that adds/removes items from the tree at the specified
	 * node and index.
	 * 
	 * @param source
	 * @param add
	 * @param destination
	 * @param node
	 * @param index
	 */
	public DnDTreeEvent(Object source, boolean isAdd, DnDMutableTreeNode destination, DnDMutableTreeNode node, int index)
	{
		super(source, AWTEvent.RESERVED_ID_MAX + 1);
		this.destination = destination;
		this.node = node;
		this.index = index;
		this.isAdd = isAdd;
	}
 
	public DnDTreeEvent invert()
	{
		return new DnDTreeEvent(this.source, !this.isAdd, this.destination, this.node, this.index);
 
	}
 
	/**
	 * @return the destination
	 */
	public DnDMutableTreeNode getDestination()
	{
		return this.destination;
	}
 
	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(DnDMutableTreeNode destination)
	{
		this.destination = destination;
	}
 
	/**
	 * @return the node
	 */
	public DnDMutableTreeNode getNode()
	{
		return this.node;
	}
 
	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(DnDMutableTreeNode node)
	{
		this.node = node;
	}
 
	public boolean isAdd()
	{
		return this.isAdd;
	}
 
	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return this.index;
	}
 
	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}
 
	@Override
	public String toString()
	{
		return "Add remove " + this.destination + " " + this.node + " " + this.index + " " + this.isAdd;
	}
}
