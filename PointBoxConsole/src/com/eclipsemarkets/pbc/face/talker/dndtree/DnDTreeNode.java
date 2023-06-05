/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger;

/**
 * A simple node used for the undo/redo tracker.
 * @author Zhijun Zhang
 */
public class DnDTreeNode<E> {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeNode.class.getName());
    }
	public DnDTreeNode<E> prev;
	public DnDTreeNode<E> next;
	public E data;
 
	public DnDTreeNode(E data)
	{
		this.data = data;
		this.prev = null;
		this.next = null;
	}
 
	public void linkNext(DnDTreeNode<E> node)
	{
		if (node == null)
		{
			// unlink next, return
			this.unlinkNext();
			return;
		}
		if (node.prev != null)
		{
			// need to unlink previous node from node
			node.unlinkPrev();
		}
		if (this.next != null)
		{
			// need to unlink next node from this
			this.unlinkNext();
		}
		this.next = node;
		node.prev = this;
	}
 
	public void linkPrev(DnDTreeNode<E> node)
	{
		if (node == null)
		{
			this.unlinkPrev();
			return;
		}
		if (node.next != null)
		{
			// need to unlink next node from node
			node.unlinkNext();
		}
		if (this.prev != null)
		{
			// need to unlink prev from this
			this.unlinkPrev();
		}
		this.prev = node;
		node.next = this;
	}
 
	public void unlinkNext()
	{
		this.next.prev = null;
		this.next = null;
	}
 
	public void unlinkPrev()
	{
		this.prev.next = null;
		this.prev = null;
	}
}
