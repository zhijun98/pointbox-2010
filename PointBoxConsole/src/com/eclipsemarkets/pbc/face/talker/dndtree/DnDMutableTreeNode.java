/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.io.Serializable;
 
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
/**
 * The standard DefaultMutableTreeNode doesn't implement the Transferable interface, vital to use Java's framework for transfering data. Fortunately, it's not too hard to implement the interface. It's also very important that DnDMutableTreeNode be serializable. The default drag and drop framework requires that everything be serializable (including all the data inside). This was a common problem I ran into when I tried this on a custom object I created that wasn't serializable. The beauty of the Serializable interface, though, is that there are no methods you have to implement (makes me wonder why all objects aren't serializable).
 * 
 * Because the data I had was highly dependent on where it was in the tree (particularly what children it had) I designed this node to function so you could implement sub-classes and they would still function correctly.
 *
 * @author Zhijun Zhang (original author helloworld922)
 */
public class DnDMutableTreeNode extends DefaultMutableTreeNode implements Transferable, Serializable,
		Cloneable
{
    
    private static final long serialVersionUID = 4816704492774592665L;
    private static final Logger logger;
    static {
        logger = Logger.getLogger(DnDMutableTreeNode.class.getName());
    }
 
    /**
     * data flavor used to get back a DnDMutableTreeNode from data transfer
     */
    public static final DataFlavor DnDNode_FLAVOR = new DataFlavor(DnDMutableTreeNode.class,
                    "Drag and drop Node");

    /**
     * list of all flavors that this DnDMutableTreeNode can be transfered as
     */
    protected static DataFlavor[] flavors = { DnDMutableTreeNode.DnDNode_FLAVOR };

    public DnDMutableTreeNode()
    {
            super();
    }

    /**
     * Constructs
     * 
     * @param data
     */
    public DnDMutableTreeNode(Serializable data)
    {
            super(data);
    }

    /**
     * Determines if we can add a certain node as a child of this node.
     * 
     * @param node
     * @return
     */
    public boolean canAdd(DnDMutableTreeNode node)
    {
            if (node != null)
            {
                    // if (!this.equals(node.getParent()))
                    // {
                    if ((!this.equals(node)))
                    {
                            return true;
                    }
                    // }
            }
            return false;
    }

    /**
     * @param dataFlavor
     * @return
     */
    public boolean canImport(DataFlavor flavor)
    {
        return this.isDataFlavorSupported(flavor);
    }

    /**
     * Dummy clone. Just returns this
     * 
     * @return
     */
    @Override
    public Object clone()
    {
            DnDMutableTreeNode node = this.cloneNode();
            for (int i = 0; i < this.getChildCount(); i++)
            {
                    node.add((MutableTreeNode) ((DnDMutableTreeNode) this.getChildAt(i)).clone());
            }

            return node;
    }

    /**
     * 
     * @return
     */
    public DnDMutableTreeNode cloneNode()
    {
            DnDMutableTreeNode node = new DnDMutableTreeNode((Serializable) this.userObject);
            node.setAllowsChildren(this.getAllowsChildren());
            return node;
    }

    /**
     * Checks this node for equality with another node. To be equal, this node
     * and all of it's children must be equal. Note that the parent/ancestors do
     * not need to match at all.
     * 
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
            if (o == null)
            {
                    return false;
            }
            else if (!(o instanceof DnDMutableTreeNode))
            {
                    return false;
            }
            else if (!this.equalsNode((DnDMutableTreeNode) o))
            {
                    return false;
            }
            else if (this.getChildCount() != ((DnDMutableTreeNode) o).getChildCount())
            {
                    return false;
            }
            // compare all children
            for (int i = 0; i < this.getChildCount(); i++)
            {
                    if (!this.getChildAt(i).equals(((DnDMutableTreeNode) o).getChildAt(i)))
                    {
                            return false;
                    }
            }
            // they are equal!
            return true;
    }

    /**
     * Compares if this node is equal to another node. In this method, children
     * and ancestors are not taken into concideration.
     * 
     * @param node
     * @return
     */
    public boolean equalsNode(DnDMutableTreeNode node)
    {
            if (node != null)
            {
                    if (this.getAllowsChildren() == node.getAllowsChildren())
                    {
                            if (this.getUserObject() != null)
                            {
                                    if (this.getUserObject().equals(node.getUserObject()))
                                    {
                                            return true;
                                    }
                            }
                            else
                            {
                                    if (node.getUserObject() == null)
                                    {
                                            return true;
                                    }
                            }
                    }
            }
            return false;
    }

    /**
     * Gets the index node should be inserted at to maintain sorted order. Also
     * performs checking to see if that node can be added to this node. By
     * default, DnDMutableTreeNode adds children at the end.
     * 
     * @param node
     * @return the index to add at, or -1 if node can not be added
     */
    public int getAddIndex(DnDMutableTreeNode node)
    {
            if (!this.canAdd(node))
            {
                    return -1;
            }
            return this.getChildCount();
    }

    /**
     * @param flavor
     * @return
     * @throws UnsupportedFlavorException
     * @throws IOException
     **/
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
            if (this.canImport(flavor))
            {
                    return this;
            }
            else
            {
                    throw new UnsupportedFlavorException(flavor);
            }
    }

    /**
     * @return
     **/
    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
            return DnDMutableTreeNode.flavors;
    }

    /**
     * @param temp
     * @return
     */
    public int indexOfNode(DnDMutableTreeNode node)
    {
            if (node == null)
            {
                    throw new NullPointerException();
            }
            else
            {
                    for (int i = 0; i < this.getChildCount(); i++)
                    {
                            if (this.getChildAt(i).equals(node))
                            {
                                    return i;
                            }
                    }
                    return -1;
            }
    }

    /**
     * @param flavor
     * @return
     **/
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
            DataFlavor[] flavs = this.getTransferDataFlavors();
            for (int i = 0; i < flavs.length; i++)
            {
                    if (flavs[i].equals(flavor))
                    {
                            return true;
                    }
            }
            return false;
    }

    public Icon getIcon() {
        return null;
    }
}
