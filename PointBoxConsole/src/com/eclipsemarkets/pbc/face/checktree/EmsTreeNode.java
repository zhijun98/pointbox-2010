/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Zhijun Zhang
 */
abstract class EmsTreeNode extends DefaultMutableTreeNode implements Transferable {
    private Icon closedImage = null;
    private Icon openImage = null;

    boolean isSelected;

    final static int TREE = 0;
    final static int STRING = 1;
    final static int PLAIN_TEXT = 1;

    final public static DataFlavor DEFAULT_MUTABLE_TREENODE_FLAVOR = new DataFlavor(
            DefaultMutableTreeNode.class, "Default Mutable Tree Node");
    static DataFlavor flavors[] = {DEFAULT_MUTABLE_TREENODE_FLAVOR, DataFlavor.stringFlavor, DataFlavor.plainTextFlavor};
    DefaultMutableTreeNode data;

    static final DataFlavor[] DATA_FLAVORS = {new DataFlavor(EmsTreeNode.class, "JiveTreeNodeFlavor")};
    Object associatedObject;

    /*
    * Create node with closedImage.
    * @param userObject  Name to display
    * @param allowsChildren Specify if node allows children
    * @param img Specify closedImage to use.
    */
    public EmsTreeNode(EmsTreeFolder folder, Icon openImage, Icon closedImage) {
        super(folder.getDisplayName(), true);
        this.openImage = openImage;
        this.closedImage = closedImage;
        associatedObject = folder;
    }

    /**
     * Create parent node.
     *
     * @param name           the name of the node.
     * @param allowsChildren true if the node allows children.
     * @param openImage
     * @param closedImage
     */
    public EmsTreeNode(String name, boolean allowsChildren, Icon openImage, Icon closedImage) {
        super(name, allowsChildren);
        if (allowsChildren) {
        this.openImage = openImage;
        this.closedImage = closedImage;
        }
    }

    /**
     * Creates a new JiveTreeNode.
     *
     * @param o              the object to use.
     * @param allowsChildren true if it allows children.
     */
    public EmsTreeNode(Object o, boolean allowsChildren) {
        super(o, allowsChildren);
    }

    /**
     * Creates a new JiveTreeNode from a TreeItem.
     *
     * @param item the <code>TreeItem</code>
     */
    public EmsTreeNode(EmsTreeItem item) {
        super(item.getDisplayName(), false);
        associatedObject = item;
    }

    /**
     * Createa new JiveTreeNode from a TreeItem and Image.
     *
     * @param item the <code>TreeItem</code> to use.
     * @param img  the image to use in the node.
     */
    public EmsTreeNode(EmsTreeItem item, Icon img) {
        this(item);
        closedImage = img;
    }

    /**
     * Creates a new JiveTreeNode.
     *
     * @param userobject the object to use in the node. Note: By default, the node
     *                   will not allow children.
     */
    public EmsTreeNode(String userobject) {
        super(userobject);
    }

    /**
     * Creates a new JiveTreeNode.
     *
     * @param userObject    the userObject to use.
     * @param allowChildren true if it allows children.
     * @param icon          the image to use in the node.
     */
    public EmsTreeNode(String userObject, boolean allowChildren, Icon icon) {
        super(userObject, allowChildren);
        closedImage = icon;
        openImage = icon;
    }

    /**
     * Returns the default image used.
     *
     * @return the default image used.
     */
    public Icon getIcon() {
        return closedImage;
    }

    /**
     * Return the icon that is displayed when the node is expanded.
     *
     * @return the open icon.
     */
    public Icon getOpenIcon() {
        return openImage;
    }

    /**
     * Returns the icon that is displayed when the node is collapsed.
     *
     * @return the closed icon.
     */
    public Icon getClosedIcon() {
        return closedImage;
    }

    /**
     * Sets the default icon.
     *
     * @param icon the icon.
     */
    public void setIcon(Icon icon) {
        closedImage = icon;
    }

    /**
     * Returns the associated object used. The associated object is used to store associated data objects
     * along with the node.
     *
     * @return the object.
     */
    public Object getAssociatedObject() {
        return associatedObject;
    }

    /**
     * Returns the associated object.
     *
     * @param o the associated object.
     */
    public void setAssociatedObject(Object o) {
        this.associatedObject = o;
    }

    /**
     * Returns true if a parent with the specified name is found.
     *
     * @param parentName the name of the parent.
     * @return true if parent found.
     */
    public final boolean hasParent(String parentName) {
        EmsTreeNode emsParent = (EmsTreeNode)getParent();
        while (true) {
            if (emsParent.getAssociatedObject() == null) {
                break;
            }
            final EmsTreeFolder folder = (EmsTreeFolder)emsParent.getAssociatedObject();
            if (folder.getDisplayName().equals(parentName)) {
                return true;
            }
            emsParent = (EmsTreeNode)emsParent.getParent();
        }
        return false;
    }


    /**
     * Transferable implementation
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        //return DATA_FLAVORS;
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVORS[0];

    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (this.isDataFlavorSupported(flavor)) {
            return this;
        }

        throw new UnsupportedFlavorException(flavor);
    }


}