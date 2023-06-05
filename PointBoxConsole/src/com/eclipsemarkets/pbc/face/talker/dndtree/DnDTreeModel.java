/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger;
import javax.swing.tree.*;
/**
 * This class is almist identical to the DefaultTreeModel except I changed the removeNodeFromParent method to use an absolute comparison to find the correct node to remove. This is necessary because of several problems that arise with insert and copying if the comparison is not absolute (==)
 * 
 * @author Zhijun Zhang
 */
public class DnDTreeModel extends DefaultTreeModel
{

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeModel.class.getName());
    } 
	/**
	 * @param root
	 */
	public DnDTreeModel(TreeNode root)
	{
		super(root);
	}
 
	public DnDTreeModel(TreeNode root, boolean asksAllowsChildren)
	{
		super(root, asksAllowsChildren);
	}
 
	/**
	 * Removes the specified node. Note that the comparison is made absolutely,
	 * ie. must be the exact same object not just equal.
	 * 
	 * @param node
	 */
	@Override
	public void removeNodeFromParent(MutableTreeNode node)
	{
		// get back the index of the node
		DnDMutableTreeNode parent = (DnDMutableTreeNode) node.getParent();
		int sourceIndex = 0;
		for (sourceIndex = 0; sourceIndex < parent.getChildCount() && parent
				.getChildAt(sourceIndex) != node; sourceIndex++)
		{}
		// time to perform the removal
		parent.remove(sourceIndex);
		// need a custom remove event because we manually removed
		// the correct node
		int[] childIndices = new int[1];
		childIndices[0] = sourceIndex;
		Object[] removedChildren = new Object[1];
		removedChildren[0] = node;
		this.nodesWereRemoved(parent, childIndices, removedChildren);
	}
}
